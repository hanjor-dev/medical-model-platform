package com.okbug.platform.ws.dto;

/**
 * WebSocket 事件：用户消息
 */
public class UserMessageEvent {
    public final String event = "USER_MESSAGE";
    public String userId;
    public String type;
    public Object data;

    public UserMessageEvent() {
    }

    public UserMessageEvent(String userId, String type, Object data) {
        this.userId = userId;
        this.type = type;
        this.data = data;
    }
}


