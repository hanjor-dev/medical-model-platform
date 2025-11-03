package com.okbug.platform.service.system.message;

import com.okbug.platform.entity.system.message.PushTask;

/**
 * 推送任务服务：负责消息推送任务的排队与状态管理
 */
public interface PushTaskService {

    /**
     * 入队创建推送任务
     *
     * @param messageId 关联消息ID
     * @param maxAttempts 最大重试次数
     * @return 任务ID
     * @throws com.okbug.platform.common.base.ServiceException 业务状态不允许入队时抛出
     */
    Long enqueue(Long messageId, int maxAttempts);

    /**
     * 标记任务成功
     *
     * @param taskId 任务ID
     */
    void markSuccess(Long taskId);

    /**
     * 标记任务失败
     *
     * @param taskId 任务ID
     * @param error 失败原因
     * @param scheduleRetry 是否安排重试
     */
    void markFailed(Long taskId, String error, boolean scheduleRetry);

    /**
     * 根据ID查询任务
     *
     * @param id 任务ID
     * @return 任务实体，可能为null
     */
    PushTask getById(Long id);
}


