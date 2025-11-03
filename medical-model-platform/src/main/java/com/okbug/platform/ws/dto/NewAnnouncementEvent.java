package com.okbug.platform.ws.dto;

/**
 * WebSocket 事件：新公告通知
 */
public class NewAnnouncementEvent {
    public final String event = "NEW_ANNOUNCEMENT";
    public String userId;
    public String announcementId;
    public String title;
    public String content;

    public NewAnnouncementEvent() {
    }

    public NewAnnouncementEvent(String userId, String announcementId, String title, String content) {
        this.userId = userId;
        this.announcementId = announcementId;
        this.title = title;
        this.content = content;
    }
}


