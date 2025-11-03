package com.okbug.platform.service.system.message.impl;

import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.entity.system.message.PushTask;
import com.okbug.platform.mapper.system.message.PushTaskMapper;
import com.okbug.platform.service.system.message.PushTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class PushTaskServiceImpl implements PushTaskService {

    private final PushTaskMapper pushTaskMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 入队推送任务。
     *
     * @param messageId 关联消息ID
     * @param maxAttempts 最大重试次数
     * @return 任务ID
     */
    public Long enqueue(Long messageId, int maxAttempts) {
        PushTask t = new PushTask();
        t.setMessageId(messageId);
        t.setScheduledTime(LocalDateTime.now());
        t.setAttemptCount(0);
        t.setMaxAttempts(maxAttempts);
        t.setStatus(0);
        t.setCreateTime(LocalDateTime.now());
        t.setUpdateTime(LocalDateTime.now());
        pushTaskMapper.insert(t);
        return t.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 标记任务成功。
     */
    public void markSuccess(Long taskId) {
        PushTask t = pushTaskMapper.selectById(taskId);
        if (t == null) throw new ServiceException(ErrorCode.TASK_NOT_FOUND);
        t.setStatus(2);
        t.setUpdateTime(LocalDateTime.now());
        pushTaskMapper.updateById(t);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    /**
     * 标记任务失败，可选择调度重试（固定5分钟后）。
     */
    public void markFailed(Long taskId, String error, boolean scheduleRetry) {
        PushTask t = pushTaskMapper.selectById(taskId);
        if (t == null) throw new ServiceException(ErrorCode.TASK_NOT_FOUND);
        t.setAttemptCount((t.getAttemptCount() == null ? 0 : t.getAttemptCount()) + 1);
        t.setLastError(error);
        if (scheduleRetry && t.getAttemptCount() < (t.getMaxAttempts() == null ? 3 : t.getMaxAttempts())) {
            t.setStatus(0);
            t.setNextRetryTime(LocalDateTime.now().plusMinutes(5));
        } else {
            t.setStatus(3);
        }
        t.setUpdateTime(LocalDateTime.now());
        pushTaskMapper.updateById(t);
    }

    @Override
    /**
     * 根据ID获取任务。
     */
    public PushTask getById(Long id) {
        return pushTaskMapper.selectById(id);
    }
}


