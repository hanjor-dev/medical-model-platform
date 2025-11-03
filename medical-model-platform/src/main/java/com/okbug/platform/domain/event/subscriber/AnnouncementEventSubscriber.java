package com.okbug.platform.domain.event.subscriber;

import com.okbug.platform.domain.event.events.AnnouncementPublishedEvent;
import com.okbug.platform.ws.RealtimeWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 公告相关事件订阅者
 * - 监听公告发布事件，触发 WS 推送（广播给订阅公告的客户端）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AnnouncementEventSubscriber {

    private final RealtimeWebSocketHandler webSocketHandler;

    @EventListener
    public void onAnnouncementPublished(AnnouncementPublishedEvent event) {
        log.info("[DomainEvent] onAnnouncementPublished: id={}, title={}", event.getAnnouncementId(), event.getTitle());
        try {
            // 0L: 广播场景（客户端根据自身过滤是否展示）
            webSocketHandler.sendNewAnnouncementNotification(0L, event.getAnnouncementId(), event.getTitle(), "");
        } catch (Exception e) {
            log.warn("WS 推送公告事件失败: id={}, err={}", event.getAnnouncementId(), e.getMessage());
        }
    }
}


