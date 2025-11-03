package com.okbug.platform.service.system.message.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
 
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.dto.system.message.request.MessageCreateRequest;
import com.okbug.platform.dto.system.message.request.MessageQueryRequest;
import com.okbug.platform.dto.system.message.response.MessageVO;
import com.okbug.platform.entity.system.message.Message;
import com.okbug.platform.mapper.system.message.MessageMapper;
import com.okbug.platform.mapper.system.message.MessageReadMapper;
import com.okbug.platform.service.system.message.MessageService;
import com.okbug.platform.service.system.SystemDictService;
import com.okbug.platform.dto.system.DictDataDTO;
import com.okbug.platform.service.system.message.dispatcher.MessageDispatcher;
 
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
 
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageMapper messageMapper;
    private final MessageReadMapper messageReadMapper;
    private final MessageDispatcher messageDispatcher;
    private final SystemDictService systemDictService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(MessageCreateRequest request, Long operatorId, String operatorName) {
        // 基本校验
        if (request.getUserId() == null || request.getContent() == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "缺少必要参数");
        }

        // 调度时间健壮性校验：不允许调度到过去
        if (request.getScheduleTime() != null && request.getScheduleTime().isBefore(LocalDateTime.now())) {
            throw new ServiceException(ErrorCode.PARAM_INVALID, "调度时间不能早于当前时间");
        }

        log.info("创建站内消息: operatorId={}, userId={}, scheduleTime={}",
                operatorId, request.getUserId(), request.getScheduleTime());

        Message msg = new Message();
        msg.setUserId(request.getUserId());
        msg.setMessageType(request.getMessageType().code());
        msg.setTitle(request.getTitle());
        msg.setContent(request.getContent());
        msg.setScheduleTime(request.getScheduleTime());
        msg.setStatus(0);
        msg.setCreateTime(LocalDateTime.now());
        msg.setUpdateTime(LocalDateTime.now());
        msg.setCreateBy(operatorId);
        msg.setUpdateBy(operatorId);
        messageMapper.insert(msg);
        log.info("消息创建成功: id={}, userId={}", msg.getId(), msg.getUserId());

        // 立即发送：由分发器统一进行多渠道路由与推送；延迟发送交由调度器
        if (msg.getScheduleTime() == null || !msg.getScheduleTime().isAfter(LocalDateTime.now())) {
            boolean success;
            try {
                success = messageDispatcher.dispatch(msg);
            } catch (Exception e) {
                // 任何分发异常都不能影响主业务流程：仅记录并标记失败
                log.warn("消息分发异常(不影响业务): id={}, userId={}, error={}", msg.getId(), msg.getUserId(), e.getMessage());
                success = false;
            }

            msg.setStatus(success ? 2 : 4);
            msg.setUpdateTime(LocalDateTime.now());
            messageMapper.updateById(msg);
        }

        return msg.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancel(Long id, Long operatorId, String operatorName) {
        log.info("取消站内消息: id={}, operatorId={}", id, operatorId);
        Message message = messageMapper.selectById(id);
        if (message == null) {
            throw new ServiceException(ErrorCode.NOTIFY_MESSAGE_NOT_FOUND);
        }
        if (message.getStatus() != null && (message.getStatus() == 2 || message.getStatus() == 3)) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "已完成或已取消的消息不可取消");
        }
        message.setStatus(3);
        message.setUpdateTime(LocalDateTime.now());
        message.setUpdateBy(operatorId);
        messageMapper.updateById(message);
        log.info("消息取消成功: id={}", id);
    }

    @Override
    public Page<MessageVO> page(MessageQueryRequest request, long pageNum, long pageSize) {
        log.debug("分页查询消息: userId={}, messageType={}, status={}, pageNum={}, pageSize={}",
                request.getUserId(), request.getMessageType(), request.getStatus(), pageNum, pageSize);
        LambdaQueryWrapper<Message> qw = new LambdaQueryWrapper<>();
        if (request.getUserId() != null) {
            qw.eq(Message::getUserId, request.getUserId());
        }
        if (request.getMessageType() != null && !request.getMessageType().isEmpty()) {
            qw.eq(Message::getMessageType, request.getMessageType());
        }
        if (request.getStatus() != null) {
            qw.eq(Message::getStatus, request.getStatus());
        }
        qw.orderByDesc(Message::getCreateTime);
        Page<Message> page = new Page<>(pageNum, pageSize);
        Page<Message> data = messageMapper.selectPage(page, qw);

        Page<MessageVO> voPage = new Page<>();
        voPage.setCurrent(data.getCurrent());
        voPage.setSize(data.getSize());
        voPage.setTotal(data.getTotal());
        voPage.setPages(data.getPages());
        List<Message> records = data.getRecords();
        Map<Long, Boolean> readMap = new HashMap<>();
        if (request.getUserId() != null && !records.isEmpty()) {
            Set<Long> ids = records.stream().map(Message::getId).collect(Collectors.toSet());
            readMap.putAll(
                messageReadMapper.selectList(new LambdaQueryWrapper<com.okbug.platform.entity.system.message.MessageRead>()
                        .in(com.okbug.platform.entity.system.message.MessageRead::getMessageId, ids)
                        .eq(com.okbug.platform.entity.system.message.MessageRead::getUserId, request.getUserId()))
                    .stream().collect(Collectors.toMap(r -> r.getMessageId(), r -> true, (a, b) -> a))
            );
        }
        // 预取类型字典，建立 value->label 映射（DICT_4.2 下的子项）
        Map<String, String> typeValueToLabel = new HashMap<>();
        try {
            List<DictDataDTO> items = systemDictService.getChildrenOptionsByCode("DICT_4.2");
            if (items != null) {
                for (DictDataDTO it : items) {
                    if (it.getValue() != null && it.getLabel() != null) {
                        typeValueToLabel.put(it.getValue().trim().toLowerCase(), it.getLabel());
                    }
                }
            }
        } catch (Exception ignore) { }

        voPage.setRecords(records.stream().map(m -> {
            MessageVO vo = new MessageVO();
            vo.setId(m.getId());
            vo.setUserId(m.getUserId());
            vo.setMessageType(m.getMessageType());
            if (m.getMessageType() != null) {
                String key = m.getMessageType().trim().toLowerCase();
                vo.setMessageTypeLabel(typeValueToLabel.getOrDefault(key, m.getMessageType()));
            }
            vo.setTitle(m.getTitle());
            vo.setContent(m.getContent());
            vo.setScheduleTime(m.getScheduleTime());
            vo.setStatus(m.getStatus());
            vo.setCreateTime(m.getCreateTime());
            vo.setIsRead(readMap.getOrDefault(m.getId(), false));
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public long countUnreadTotal(Long userId) {
        if (userId == null) {
            throw new ServiceException(ErrorCode.PARAM_INVALID, "用户ID不能为空");
        }
        // 总消息数
        long totalCount = messageMapper.selectCount(new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId));
        if (totalCount == 0L) {
            return 0L;
        }
        // 已读数量（按用户统计）
        long readCount = messageReadMapper.selectCount(new LambdaQueryWrapper<com.okbug.platform.entity.system.message.MessageRead>()
                .eq(com.okbug.platform.entity.system.message.MessageRead::getUserId, userId));
        return Math.max(0L, totalCount - readCount);
    }

    /**
     * 从领域事件创建消息：语义与 create 基本一致，保留独立入口便于权限与审计
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createFromEvent(MessageCreateRequest request, Long operatorId, String operatorName) {
        return create(request, operatorId, operatorName);
    }

    @Override
    public MessageVO getOwnDetail(Long id, Long userId) {
        Message m = messageMapper.selectById(id);
        if (m == null) {
            throw new ServiceException(ErrorCode.NOTIFY_MESSAGE_NOT_FOUND);
        }
        if (!m.getUserId().equals(userId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "无权访问该消息");
        }
        MessageVO vo = new MessageVO();
        vo.setId(m.getId());
        vo.setUserId(m.getUserId());
        vo.setMessageType(m.getMessageType());
        try {
            List<DictDataDTO> items = systemDictService.getChildrenOptionsByCode("DICT_4.2");
            if (items != null && m.getMessageType() != null) {
                String key = m.getMessageType().trim().toLowerCase();
                String label = items.stream()
                        .filter(it -> it.getValue() != null && key.equals(it.getValue().trim().toLowerCase()))
                        .map(DictDataDTO::getLabel)
                        .findFirst().orElse(null);
                if (label != null) {
                    vo.setMessageTypeLabel(label);
                } else {
                    vo.setMessageTypeLabel(m.getMessageType());
                }
            }
        } catch (Exception ignore) { vo.setMessageTypeLabel(m.getMessageType()); }
        vo.setTitle(m.getTitle());
        vo.setContent(m.getContent());
        vo.setScheduleTime(m.getScheduleTime());
        vo.setStatus(m.getStatus());
        vo.setCreateTime(m.getCreateTime());
        boolean isRead = messageReadMapper.selectCount(new LambdaQueryWrapper<com.okbug.platform.entity.system.message.MessageRead>()
                .eq(com.okbug.platform.entity.system.message.MessageRead::getMessageId, id)
                .eq(com.okbug.platform.entity.system.message.MessageRead::getUserId, userId)) > 0;
        vo.setIsRead(isRead);
        return vo;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long id, Long userId) {
        Message m = messageMapper.selectById(id);
        if (m == null) {
            throw new ServiceException(ErrorCode.NOTIFY_MESSAGE_NOT_FOUND);
        }
        if (!m.getUserId().equals(userId)) {
            throw new ServiceException(ErrorCode.FORBIDDEN, "无权标记该消息");
        }
        Long count = messageReadMapper.selectCount(new LambdaQueryWrapper<com.okbug.platform.entity.system.message.MessageRead>()
                .eq(com.okbug.platform.entity.system.message.MessageRead::getMessageId, id)
                .eq(com.okbug.platform.entity.system.message.MessageRead::getUserId, userId));
        if (count == 0) {
            com.okbug.platform.entity.system.message.MessageRead read = new com.okbug.platform.entity.system.message.MessageRead();
            read.setMessageId(id);
            read.setUserId(userId);
            read.setReadAt(LocalDateTime.now());
            read.setCreateTime(LocalDateTime.now());
            read.setUpdateTime(LocalDateTime.now());
            messageReadMapper.insert(read);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead(Long userId) {
        // 查询该用户未读消息ID集合
        List<Long> myMessageIds = messageMapper.selectList(new LambdaQueryWrapper<Message>()
                .eq(Message::getUserId, userId)
        ).stream().map(Message::getId).collect(Collectors.toList());
        if (myMessageIds.isEmpty()) {
            return;
        }
        Set<Long> alreadyRead = messageReadMapper.selectList(new LambdaQueryWrapper<com.okbug.platform.entity.system.message.MessageRead>()
                .eq(com.okbug.platform.entity.system.message.MessageRead::getUserId, userId)
                .in(com.okbug.platform.entity.system.message.MessageRead::getMessageId, myMessageIds))
            .stream().map(com.okbug.platform.entity.system.message.MessageRead::getMessageId).collect(Collectors.toSet());
        LocalDateTime now = LocalDateTime.now();
        for (Long mid : myMessageIds) {
            if (!alreadyRead.contains(mid)) {
                com.okbug.platform.entity.system.message.MessageRead read = new com.okbug.platform.entity.system.message.MessageRead();
                read.setMessageId(mid);
                read.setUserId(userId);
                read.setReadAt(now);
                read.setCreateTime(now);
                read.setUpdateTime(now);
                messageReadMapper.insert(read);
            }
        }
    }
}


