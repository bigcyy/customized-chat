package com.cyy.chat.service.impl;

import com.cyy.chat.controller.dto.ApplicationDto;
import com.cyy.chat.controller.vo.AiResponseVO;
import com.cyy.chat.memory.JdbcMemoryRepository;
import com.cyy.chat.model.Application;
import com.cyy.chat.model.ChatMessage;
import com.cyy.chat.dao.ChatMessageMapper;
import com.cyy.chat.model.ChatSession;
import com.cyy.chat.model.McpServer;
import com.cyy.chat.model.McpSetting;
import com.cyy.chat.model.Model;
import com.cyy.chat.model.ModelSetting;
import com.cyy.chat.model.ToolExecutionReq;
import com.cyy.chat.model.ToolExecutionDetail;
import com.cyy.chat.provider.ModelFactory;
import com.cyy.chat.service.Agent;
import com.cyy.chat.service.IChatMessageService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cyy.chat.service.IModelService;
import com.cyy.chat.service.IMcpServerService;
import com.cyy.chat.utils.RoleTypeAdaptor;
import com.cyy.common.exception.ApplicationNoModelConfigException;
import com.cyy.common.exception.SystemGlobalException;
import com.cyy.common.utils.SnowFlakeIdGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.langchain4j.data.message.AiMessage;
import dev.langchain4j.data.message.ChatMessageType;
import dev.langchain4j.mcp.McpToolProvider;
import dev.langchain4j.mcp.client.DefaultMcpClient;
import dev.langchain4j.mcp.client.McpClient;
import dev.langchain4j.mcp.client.transport.McpTransport;
import dev.langchain4j.mcp.client.transport.http.HttpMcpTransport;
import dev.langchain4j.mcp.client.transport.stdio.StdioMcpTransport;
import dev.langchain4j.data.message.SystemMessage;
import dev.langchain4j.data.message.UserMessage;
import dev.langchain4j.memory.chat.ChatMemoryProvider;
import dev.langchain4j.memory.chat.MessageWindowChatMemory;
import dev.langchain4j.model.chat.StreamingChatModel;
import dev.langchain4j.service.AiServices;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author CYY
 * @since 2025-03-14
 */
@Slf4j
@Service
public class ChatMessageServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements IChatMessageService {

    @Resource
    private IModelService modelService;

    @Resource
    private ModelFactory modelFactory;

    @Resource
    private JdbcMemoryRepository jdbcMemoryRepository;

    @Resource
    private IMcpServerService mcpServerService;

    @Override
    public Flux<AiResponseVO> chat(Application application, ChatSession chatSession, ChatMessage userMessage) {
        // 保存当前信息
        StreamingChatModel chatModel = buildStreamingChatModel(application.getModelId());

        ModelSetting modelSetting;
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            modelSetting = objectMapper.readValue(application.getModelSetting(), ModelSetting.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to parse model setting: {}", application.getModelSetting(), e);
            throw new SystemGlobalException("Failed to parse model setting: " + application.getModelSetting());
        }

        // 获取 mcp clients
        List<McpClient> mcpClients = buildMcpClientsFromServerIds(application.getMcpServerIds());

        // 构建 tool provider
        McpToolProvider mcpToolProvider = new McpToolProvider.Builder()
                .mcpClients(mcpClients)
                .build();

        ChatMemoryProvider memoryProvider = id -> {
            if (id == null) {
                throw new SystemGlobalException("Chat memory ID cannot be null");
            }
            return MessageWindowChatMemory.builder()
                    .maxMessages(modelSetting.getChatMemory() == null ? 10 : modelSetting.getChatMemory())
                    .chatMemoryStore(jdbcMemoryRepository)
                    .id(id)
                    .build();
        };

        Agent agent = AiServices.builder(Agent.class)
                .streamingChatModel(chatModel)
                .chatMemoryProvider(memoryProvider)
                .toolProvider(mcpToolProvider)
                .build();

        Sinks.Many<AiResponseVO> sink = Sinks.many().unicast().onBackpressureBuffer();

        agent.chat(chatSession.getId(), userMessage.getMessageText())
                .onPartialResponse(item -> {
                    AiResponseVO res = AiResponseVO.builder()
                            .message(item)
                            .build();
                    sink.tryEmitNext(res);
                })
                .onToolExecuted(toolExecution -> {
                    ToolExecutionDetail executionDetail = ToolExecutionDetail.from(toolExecution);
                    AiResponseVO res = AiResponseVO.builder()
                            .toolExecutionDetail(executionDetail)
                            .build();
                    sink.tryEmitNext(res);
                })
                .onCompleteResponse(aiMessageResponse -> {
                    AiResponseVO res = AiResponseVO.builder()
                            .isEnd(true)
                            .build();
                    sink.tryEmitNext(res);
                    sink.tryEmitComplete();
                })
                .onError(err ->{
                    AiResponseVO errResp = AiResponseVO.builder()
                            .message(err.getMessage())
                            .isError(true)
                            .isEnd(true)
                            .build();
                    sink.tryEmitNext(errResp);
                    sink.tryEmitComplete();
                })
                .start();

        return sink.asFlux();
    }

    @Override
    public Flux<AiResponseVO> tempChat(ApplicationDto application, ChatSession chatSession, ChatMessage userMessage, List<ChatMessage> chatHistories) {

        // 获取应用对应的模型信息
        StreamingChatModel chatModel = buildStreamingChatModel(application.getModelId());

        // 获取 mcp clients
        List<McpClient> mcpClients = buildMcpClientsFromServerIds(application.getMcpServerIds());

        // 构建 tool provider
        McpToolProvider mcpToolProvider = new McpToolProvider.Builder()
                .mcpClients(mcpClients)
                .build();

        ChatMemoryProvider memoryProvider = id -> {
            if (id == null) {
                throw new SystemGlobalException("Chat memory ID cannot be null");
            }
            return MessageWindowChatMemory.builder()
                    .maxMessages(application.getModelSetting().getChatMemory() == null ? 10 : application.getModelSetting().getChatMemory())
                    .id(id)
                    .build();
        };

        // todo 从 memory 缓存中读
        if(chatHistories != null && !chatHistories.isEmpty()){
            chatHistories.sort(Comparator.comparing(ChatMessage::getMessageIndex));
            toLangChainMessageList(chatHistories).forEach(memoryProvider.get(chatSession.getId())::add);
        }

        Agent agent = AiServices.builder(Agent.class)
                .streamingChatModel(chatModel)
                .chatMemoryProvider(memoryProvider)
                .toolProvider(mcpToolProvider)
                .build();

        Sinks.Many<AiResponseVO> sink = Sinks.many().unicast().onBackpressureBuffer();

        agent.chat(userMessage.getSessionId(), userMessage.getMessageText())
                .onPartialResponse(item -> {
                    AiResponseVO res = AiResponseVO.builder()
                            .message(item)
                            .build();
                    sink.tryEmitNext(res);
                })
                .onToolExecuted(toolExecution -> {
                    ToolExecutionDetail executionDetail = ToolExecutionDetail.from(toolExecution);
                    AiResponseVO res = AiResponseVO.builder()
                            .toolExecutionDetail(executionDetail)
                            .build();
                    sink.tryEmitNext(res);
                })
                .onCompleteResponse(aiMessageResponse -> {
                    AiResponseVO res = AiResponseVO.builder()
                            .isEnd(true)
                            .build();
                    sink.tryEmitNext(res);
                    sink.tryEmitComplete();
                })
                .onError(err ->{
                    AiResponseVO errResp = AiResponseVO.builder()
                            .message(err.getMessage())
                            .isError(true)
                            .isEnd(true)
                            .build();
                    sink.tryEmitNext(errResp);
                    sink.tryEmitComplete();
                })
                .start();

        return sink.asFlux();
    }

    /**
     * 根据模型 id 创建模型
     * @param modelId 模型 id
     */
    private StreamingChatModel buildStreamingChatModel(Long modelId){
        if(modelId == null){
            throw new ApplicationNoModelConfigException();
        }
        Model model = modelService.getById(modelId);
        if(model == null) {
            throw new ApplicationNoModelConfigException();
        }
        return modelFactory
                .getProvider(model.getProvider())
                .getStreamingChatModel(model.getApiUrl(), model.getApiKey(), model.getModelName());
    }


    private dev.langchain4j.data.message.ChatMessage toLangChainMessage(ChatMessage message){
        if(message == null){
            throw new SystemGlobalException("消息为空");
        }
        ChatMessageType type = RoleTypeAdaptor.getMsgType(message.getRole());
        return switch (type) {
            case USER -> new UserMessage(message.getMessageText());
            case AI -> new AiMessage(message.getMessageText());
            case SYSTEM -> new SystemMessage(message.getMessageText());
            // 添加其他消息类型的映射，例如 TOOL_EXECUTION_RESULT, TOOL_EXECUTION_REQUEST 等
            default -> throw new IllegalArgumentException("Unsupported chat message role: " + message.getRole());
        };
    }

    /**
     * 将 ChatMessage 转为 LangChain4j 的 Message
     * @param messages 消息列表
     * @return SpringAi 的 Message 列表
     */
    private List<dev.langchain4j.data.message.ChatMessage> toLangChainMessageList(List<ChatMessage> messages){
        messages = messages == null ? new ArrayList<>() : messages;
        return messages.stream().map(this::toLangChainMessage).collect(Collectors.toList());
    }

    private List<McpClient> toMcpClients(List<McpTransport> mcpTransports){
        mcpTransports = mcpTransports == null ? new ArrayList<>() : mcpTransports;
        return mcpTransports.stream()
                .map(transport -> this.toMcpClient(transport, String.valueOf(SnowFlakeIdGenerator.generateId())))
                .collect(Collectors.toList());
    }

    private McpClient toMcpClient(McpTransport mcpTransport ,String key){
        return new DefaultMcpClient.Builder()
                .key(key)
                .transport(mcpTransport)
                .build();
    }
    /**
     * 根据 MCP 服务器 ID 列表构建 MCP 客户端
     * @param mcpServerIds MCP 服务器 ID 列表
     * @return MCP 客户端列表
     */
    private List<McpClient> buildMcpClientsFromServerIds(List<Long> mcpServerIds) {
        if (mcpServerIds == null || mcpServerIds.isEmpty()) {
            return List.of();
        }
        
        List<McpServer> mcpServers = mcpServerService.listByIds(mcpServerIds);
        List<McpTransport> mcpTransports = toMcpTransportsFromServers(mcpServers);
        return toMcpClients(mcpTransports);
    }

    /**
     * 从 McpServer 列表转换为 McpTransport 列表
     * @param mcpServers MCP 服务器列表
     * @return MCP 传输层列表
     */
    private List<McpTransport> toMcpTransportsFromServers(List<McpServer> mcpServers) {
        if (mcpServers == null || mcpServers.isEmpty()) {
            return List.of();
        }
        
        List<McpTransport> transports = new ArrayList<>();
        for (McpServer server : mcpServers) {
            if (!server.getEnabled() || server.getIsDeleted()) {
                continue;
            }
            
            McpTransport transport = createMcpTransportFromServer(server);
            if (transport != null) {
                transports.add(transport);
            }
        }
        return transports;
    }

    /**
     * 从单个 McpServer 创建 McpTransport
     * @param server MCP 服务器
     * @return MCP 传输层
     */
    private McpTransport createMcpTransportFromServer(McpServer server) {
        if ("sse".equalsIgnoreCase(server.getType())) {
            if (server.getSseUrl() == null || server.getSseUrl().trim().isEmpty()) {
                log.warn("SSE URL is empty for server: {}", server.getServerName());
                return null;
            }
            return new HttpMcpTransport.Builder()
                    .sseUrl(server.getSseUrl())
                    .logRequests(true)
                    .logResponses(true)
                    .build();
        } else if ("stdio".equalsIgnoreCase(server.getType())) {
            if (server.getCommand() == null || server.getCommand().trim().isEmpty()) {
                log.warn("Command is empty for server: {}", server.getServerName());
                return null;
            }
            List<String> cmd = new ArrayList<>();
            cmd.add(server.getCommand());
            if (server.getArgs() != null) {
                server.getArgs().stream()
                        .filter(arg -> arg != null && !arg.trim().isEmpty())
                        .forEach(cmd::add);
            }
            return new StdioMcpTransport.Builder()
                    .command(cmd)
                    .logEvents(true)
                    .build();
        } else {
            log.warn("Unknown transport type: {} for server: {}", server.getType(), server.getServerName());
            return null;
        }
    }

}
