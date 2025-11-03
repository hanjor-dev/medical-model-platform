package com.okbug.platform.domain.event.events;

import com.okbug.platform.domain.event.DomainEvent;

/**
 * 事件：公告已发布
 */
public class AnnouncementPublishedEvent implements DomainEvent {
    private final Long announcementId;
    private final String title;

    public AnnouncementPublishedEvent(Long announcementId, String title) {
        this.announcementId = announcementId;
        this.title = title;
    }

    public Long getAnnouncementId() {
        return announcementId;
    }

    public String getTitle() {
        return title;
    }

    @Override
    public String getEventKey() {
        return "ANNOUNCEMENT_PUBLISHED:" + announcementId;
    }
}


