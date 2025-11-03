package com.okbug.platform.ws.dto;

/**
 * WebSocket 事件：新消息通知
 */
public class NewMessageEvent {
    public final String event = "NEW_MESSAGE";
    public String userId;

    public NewMessageEvent() {
    }

    public NewMessageEvent(String userId) {
        this.userId = userId;
    }
}


