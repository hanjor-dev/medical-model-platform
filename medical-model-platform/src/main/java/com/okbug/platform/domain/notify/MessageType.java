package com.okbug.platform.domain.notify;

public enum MessageType {
    SYSTEM("system"),
    TASK("task"),
    MARKETING("marketing"),
    CREDIT("credit");

    private final String code;

    MessageType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}


