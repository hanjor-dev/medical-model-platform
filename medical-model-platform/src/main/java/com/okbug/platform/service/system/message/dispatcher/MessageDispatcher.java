package com.okbug.platform.service.system.message.dispatcher;

import com.okbug.platform.entity.system.message.Message;

/**
 * 消息分发器：根据用户偏好与渠道，完成对消息的多渠道下发
 */
public interface MessageDispatcher {

    /**
     * 分发消息到有效渠道
     *
     * @param message 已持久化的消息实体
     * @return 是否至少一个渠道分发成功
     */
    boolean dispatch(Message message);
}


