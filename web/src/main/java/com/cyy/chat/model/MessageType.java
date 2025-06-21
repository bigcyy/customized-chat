package com.cyy.chat.model;

import lombok.Getter;

@Getter
public enum MessageType {

    USER("user"),
    AI("assistant"),
    SYSTEM("system"),
    TOOL_EXECUTION_RESULT("tool_execution_result");


    private final String role;

    MessageType(String role) {
        this.role = role;
    }

    public static MessageType valueOfRole(String role) {
        for (MessageType type : MessageType.values()) {
            if (type.role.equals(role)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown role: " + role);
    }
}
