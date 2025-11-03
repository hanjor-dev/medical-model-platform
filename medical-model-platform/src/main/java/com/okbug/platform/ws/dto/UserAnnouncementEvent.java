package com.okbug.platform.ws.dto;

/**
 * WebSocket 事件：用户公告通用消息
 */
public class UserAnnouncementEvent {
    public final String event = "USER_ANNOUNCEMENT";
    public String userId;
    public String type;
    public Object data;

    public UserAnnouncementEvent() {
    }

    public UserAnnouncementEvent(String userId, String type, Object data) {
        this.userId = userId;
        this.type = type;
        this.data = data;
    }
}


