package com.okbug.platform.domain.event;

import java.time.Instant;

/**
 * 领域事件基类接口
 * 所有业务事件实现该接口，便于统一发布与订阅处理。
 */
public interface DomainEvent {

    /** 事件唯一键（去重/幂等用，可返回业务键或事件ID） */
    String getEventKey();

    /** 事件产生时间 */
    default Instant getOccurredAt() {
        return Instant.now();
    }
}


