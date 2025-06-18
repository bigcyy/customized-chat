package com.cyy.chat.controller;

import com.cyy.chat.controller.dto.ModelConfigDto;
import com.cyy.chat.controller.dto.ModelDto;
import com.cyy.chat.controller.vo.ModelVO;
import com.cyy.chat.controller.vo.ProviderVO;
import com.cyy.chat.model.Model;
import com.cyy.chat.provider.ModelFactory;
import com.cyy.chat.provider.ModelProvider;
import com.cyy.chat.service.IModelService;
import com.cyy.common.converter.BeanConverter;
import com.cyy.common.enums.ModelType;
import com.cyy.common.exception.ClientGlobalException;
import com.cyy.common.exception.SystemGlobalException;
import com.cyy.common.utils.R;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/v1/setting")
@Tag(name = "设置模块", description = "设置模块相关接口")
public class SettingController {

    @Resource
    private ModelFactory modelFactory;

    @Resource
    private IModelService modelServiceImpl;

    @GetMapping("/{provider}/models")
    @Operation(
            summary = "获取指定提供商支持的模型列表",
            description = "获取指定提供商支持的模型列表"
    )
    public R listModels(@PathVariable @Parameter(description = "提供商名称", required = true, example = "OpenAI") String provider){
        return R.ok().data("models", modelFactory.getProvider(provider).listSupportedModels());
    }

    @GetMapping("/providers")
    @Operation(
            summary = "获取支持的提供商列表",
            description = "获取支持的提供商列表，包括模型提供商名称、模型提供商 icon 路径"
    )
    public R listProviders(){
        return R.ok().data("providers", modelFactory.listProviders());
    }

    @GetMapping("/model/types")
    @Operation(
            summary = "获取系统支持的模型类型",
            description = "获取系统支持的模型类型"
    )
    public R listModelTypes(){
        return R.ok().data("types", ModelType.values());
    }

    @GetMapping("/{provider}/types")
    @Operation(
            summary = "获取指定提供商支持的模型类型",
            description = "获取指定提供商支持的模型类型"
    )
    public R listModelTypes(@PathVariable @Parameter(description = "提供商名称", required = true, example = "OpenAI") String provider){
        return R.ok().data("types", modelFactory.getProvider(provider).listSupportedModelTypes());
    }

    @GetMapping("/{provider}/{type}/models")
    @Operation(
            summary = "获取指定提供商支持的具体类型的模型列表",
            description = "获取指定提供商支持的具体类型的模型列表"
    )
    public R listModelsByType(@PathVariable @Parameter(description = "提供商名称", required = true, example = "OpenAI") String provider,
                              @PathVariable @Parameter(description = "模型类型", required = true, example = "LLM") ModelType type) {
        // todo 支持根据 type 获取
        return R.ok()
                .data("models", modelFactory.getProvider(provider).listSupportedModels());
    }

    @PostMapping("/models")
    @Operation(
            summary = "添加模型",
            description = "添加模型"
    )
    public R addModel(@RequestBody ModelDto modelDto){

        // 检查模型类型是否为枚举值
        if (Arrays.stream(ModelType.values()).noneMatch(type -> type.name().equals(modelDto.getModelType()))) {
            throw new ClientGlobalException("模型类型不合法");
        }

        Model model = BeanConverter.source(modelDto).target(Model.class)
                .streamMap(ModelDto::getModelConfig)
                .to((target,config)->{
                    try {
                        String configString = new ObjectMapper().writeValueAsString(config);
                        target.setModelConfig(configString);
                    } catch (JsonProcessingException e) {
                        throw new SystemGlobalException("模型配置格式转换错误");
                    }
                }).convert();
        // id 为自增，防止前端主动传入 id
        model.setId(null);
        modelServiceImpl.saveModel(model);
        return R.ok();
    }

    @GetMapping("/models")
    @Operation(
            summary = "获取所有已保存的模型列表",
            description = "获取所有已保存的模型列表"
    )
    public R getModels(){
        List<Model> models = modelServiceImpl.list();
        List<ModelVO> modelDtoList = models.stream().map(
                model -> BeanConverter.source(model).target(ModelVO.class)
                        .streamMap(Model::getModelType)
                        .to((target,type)->target.setModelType(ModelType.valueOf(type)))
                        .streamMap(Model::getProvider)
                        .to((target,provider)->{
                            ModelProvider modelProvider = modelFactory.getProvider(model.getProvider());
                            ProviderVO providerVO = new ProviderVO();
                            providerVO.setProviderName(modelProvider.getProviderName());
                            providerVO.setProviderIconPath(modelProvider.getProviderIconPath());
                            target.setProvider(providerVO);
                        }).convert())
                .toList();
        return R.ok().data("models",modelDtoList);
    }

    @GetMapping("/models/{modelId}")
    @Operation(
            summary = "获取模型详情",
            description = "获取模型详情"
    )
    public R getModel(
            @Parameter(description = "模型 id", required = true, example = "1")
            @PathVariable Long modelId){
        Model model = modelServiceImpl.getById(modelId);
        ModelDto modelDto = BeanConverter.source(model).target(ModelDto.class)
                .streamMap(Model::getModelConfig)
                .to((target, config) -> {
                    try {
                        target.setModelConfig(new ObjectMapper().readValue(config, ModelConfigDto.class));
                    } catch (Exception e) {
                        throw new SystemGlobalException("模型配置格式转换错误");
                    }
                })
                .convert();
        return R.ok().data("model",modelDto);
    }

    @DeleteMapping("/models/{modelId}")
    @Operation(
            summary = "删除模型",
            description = "删除模型"
    )
    public R deleteModel(@PathVariable String modelId) {
        modelServiceImpl.removeById(modelId);
        return R.ok();
    }


    @PutMapping("/models")
    @Operation(
            summary = "更新模型",
            description = "更新模型"
    )
    public R updateModel(@RequestBody ModelDto modelDto) {
        // 模型id检查
        if(modelDto.getId() == null){
            throw new ClientGlobalException("模型id不能为空");
        }

        // 模型类型检查
        if(Arrays.stream(ModelType.values()).noneMatch(type -> type.name().equals(modelDto.getModelType()))){
            throw new ClientGlobalException("模型类型不合法");
        }

        Model model = BeanConverter.source(modelDto).target(Model.class)
                .streamMap(ModelDto::getModelConfig)
                .to((target,config)->{
                    try {
                        String json = new ObjectMapper().writeValueAsString(config);
                        target.setModelConfig(json);
                    } catch (JsonProcessingException e) {
                        throw new SystemGlobalException("模型配置格式转换错误");
                    }
                }).convert();


        // 检查是否可以连接
        boolean canConnect = modelServiceImpl.checkModel(model);
        if(!canConnect){
            throw new SystemGlobalException("模型无法连接");
        }

        // 更新模型
        modelServiceImpl.updateById(model);

        // 返回模型
        Model newModel = modelServiceImpl.getById(modelDto.getId());
        ModelDto newModelDto = BeanConverter.source(newModel).target(ModelDto.class)
                .streamMap(Model::getModelConfig)
                .to((target,config)->{
                    try {
                        ModelConfigDto modelConfigDto = new ObjectMapper().readValue(config, ModelConfigDto.class);
                        target.setModelConfig(modelConfigDto);
                    }catch (Exception e) {
                        throw new SystemGlobalException("无法将字符串转换为模型配置");
                    }
                })
                .convert();

        return R.ok().data("model",newModelDto);
    }
}
