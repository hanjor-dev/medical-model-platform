package com.okbug.platform.service.system.message.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.okbug.platform.entity.system.message.Message;
import com.okbug.platform.mapper.system.message.MessageMapper;
import com.okbug.platform.service.system.message.dispatcher.MessageDispatcher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageScheduler {

    private final MessageMapper messageMapper;
    private final MessageDispatcher dispatcher;

    // 每30秒扫描一次待推送消息
    @Scheduled(fixedDelay = 30000)
    public void scanAndDispatch() {
        try {
            LambdaQueryWrapper<Message> qw = new LambdaQueryWrapper<>();
            qw.eq(Message::getStatus, 0)
              .isNotNull(Message::getScheduleTime)
              .le(Message::getScheduleTime, LocalDateTime.now());

            List<Message> dueList = messageMapper.selectList(qw);
            if (dueList == null || dueList.isEmpty()) {
                return;
            }

            log.info("[MessageScheduler] 待触达消息: {} 条", dueList.size());
            for (Message m : dueList) {
                boolean ok = false;
                try {
                    ok = dispatcher.dispatch(m);
                } catch (Exception e) {
                    log.warn("[MessageScheduler] 分发失败: id={}, userId={}, error={}", m.getId(), m.getUserId(), e.getMessage());
                }
                m.setStatus(ok ? 2 : 4);
                m.setUpdateTime(LocalDateTime.now());
                try {
                    messageMapper.updateById(m);
                } catch (Exception e) {
                    log.warn("[MessageScheduler] 更新状态失败: id={}, error={}", m.getId(), e.getMessage());
                }
            }
        } catch (Exception e) {
            log.error("[MessageScheduler] 调度执行异常", e);
        }
    }
}


