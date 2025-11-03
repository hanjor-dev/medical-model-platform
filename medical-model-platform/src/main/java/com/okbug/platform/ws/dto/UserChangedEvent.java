package com.okbug.platform.ws.dto;

/**
 * WebSocket 事件：用户信息发生变更
 */
public class UserChangedEvent {
    public final String event = "USER_CHANGED";
    /** 使用字符串承载长整型以避免前端精度丢失 */
    public String userId;

    public UserChangedEvent() {
    }

    public UserChangedEvent(String userId) {
        this.userId = userId;
    }
}


