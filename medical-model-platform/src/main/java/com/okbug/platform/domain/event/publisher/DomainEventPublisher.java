package com.okbug.platform.domain.event.publisher;

import com.okbug.platform.domain.event.DomainEvent;

/**
 * 领域事件发布器抽象：可由 Spring 事件或消息队列实现
 */
public interface DomainEventPublisher {

    void publish(DomainEvent event);
}


