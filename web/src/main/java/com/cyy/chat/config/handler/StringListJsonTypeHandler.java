package com.cyy.chat.config.handler;

import com.fasterxml.jackson.core.type.TypeReference;

import java.sql.SQLException;
import java.util.List;

/**
 * List类型处理器
 * 用于处理Java List<String>与数据库JSON字段的自动转换
 */
public class StringListJsonTypeHandler extends JsonTypeHandler<List<String>> {
    
    @Override
    protected List<String> parseJson(String jsonString) throws SQLException {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonString, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            throw new SQLException("解析JSON List失败", e);
        }
    }
}
