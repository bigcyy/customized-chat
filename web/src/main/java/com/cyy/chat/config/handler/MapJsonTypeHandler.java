package com.cyy.chat.config.handler;

import com.fasterxml.jackson.core.type.TypeReference;

import java.sql.SQLException;
import java.util.Map;

/**
 * Map类型处理器
 * 用于处理Java Map与数据库JSON字段的自动转换
 */
public class MapJsonTypeHandler extends JsonTypeHandler<Map<String, String>> {
    
    @Override
    protected Map<String, String> parseJson(String jsonString) throws SQLException {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonString, new TypeReference<Map<String, String>>() {});
        } catch (Exception e) {
            throw new SQLException("解析JSON Map失败", e);
        }
    }
}