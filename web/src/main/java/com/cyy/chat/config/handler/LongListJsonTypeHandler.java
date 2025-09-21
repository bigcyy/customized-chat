package com.cyy.chat.config.handler;

import com.fasterxml.jackson.core.type.TypeReference;

import java.sql.SQLException;
import java.util.List;

/**
 * Long List类型处理器
 * 用于处理Java List<Long>与数据库JSON字段的自动转换
 */
public class LongListJsonTypeHandler extends JsonTypeHandler<List<Long>> {
    
    @Override
    protected List<Long> parseJson(String jsonString) throws SQLException {
        if (jsonString == null || jsonString.trim().isEmpty()) {
            return null;
        }
        try {
            return objectMapper.readValue(jsonString, new TypeReference<List<Long>>() {});
        } catch (Exception e) {
            throw new SQLException("解析JSON Long List失败", e);
        }
    }
}
