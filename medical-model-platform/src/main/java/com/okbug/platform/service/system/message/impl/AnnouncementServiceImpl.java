package com.okbug.platform.service.system.message.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.dto.system.message.request.AnnouncementCreateRequest;
import com.okbug.platform.dto.system.message.request.AnnouncementQueryRequest;
import com.okbug.platform.dto.system.message.request.AnnouncementUpdateRequest;
import com.okbug.platform.dto.system.message.response.AnnouncementVO;
import com.okbug.platform.entity.system.message.Announcement;
import com.okbug.platform.mapper.system.message.AnnouncementMapper;
import com.okbug.platform.mapper.system.message.AnnouncementReadMapper;
import com.okbug.platform.service.system.message.AnnouncementService;
import com.okbug.platform.domain.event.publisher.DomainEventPublisher;
import com.okbug.platform.domain.event.events.AnnouncementPublishedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import com.okbug.platform.entity.system.message.AnnouncementRead;
import org.springframework.amqp.core.MessageDeliveryMode;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import static com.okbug.platform.config.RabbitAnnouncementConfig.*;

/**
 * 公告服务实现：系统/消息模块
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AnnouncementServiceImpl implements AnnouncementService {

    private final AnnouncementMapper announcementMapper;
    private final AnnouncementReadMapper announcementReadMapper;
    private final DomainEventPublisher domainEventPublisher;
    private final RabbitTemplate rabbitTemplate;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(AnnouncementCreateRequest request, Long operatorId, String operatorName) {
        log.info("[Announcement] create start, operatorId={}, title={}", operatorId, request.getTitle());
        validateWindow(request.getActiveFrom(), request.getActiveTo());

        Announcement a = new Announcement();
        a.setTitle(request.getTitle());
        a.setContent(request.getContent());
        a.setPriority(request.getPriority());
        a.setForceRead(nullToZero(request.getForceRead()));
        a.setActiveFrom(request.getActiveFrom());
        a.setActiveTo(request.getActiveTo());
        a.setStatus(0);
        a.setCreateTime(LocalDateTime.now());
        a.setUpdateTime(LocalDateTime.now());
        a.setCreateBy(operatorId);
        a.setUpdateBy(operatorId);
        announcementMapper.insert(a);

        log.info("[Announcement] create success, id={}", a.getId());

        return a.getId();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void update(Long id, AnnouncementUpdateRequest request, Long operatorId, String operatorName) {
        log.info("[Announcement] update start, id={}, operatorId={}", id, operatorId);
        Announcement db = announcementMapper.selectById(id);
        if (db == null) {
            throw new ServiceException(ErrorCode.NOTIFY_ANNOUNCEMENT_NOT_FOUND);
        }
        if (!Integer.valueOf(0).equals(db.getStatus())) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "仅草稿状态的公告允许更新");
        }
        validateWindow(request.getActiveFrom(), request.getActiveTo());

        db.setTitle(request.getTitle());
        db.setContent(request.getContent());
        db.setPriority(request.getPriority());
        db.setForceRead(nullToZero(request.getForceRead()));
        db.setActiveFrom(request.getActiveFrom());
        db.setActiveTo(request.getActiveTo());
        db.setUpdateTime(LocalDateTime.now());
        db.setUpdateBy(operatorId);
        announcementMapper.updateById(db);
        log.info("[Announcement] update success, id={}", id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void publish(Long id, Long operatorId, String operatorName) {
        log.info("[Announcement] publish start, id={}, operatorId={}", id, operatorId);
        Announcement db = announcementMapper.selectById(id);
        if (db == null) {
            throw new ServiceException(ErrorCode.NOTIFY_ANNOUNCEMENT_NOT_FOUND);
        }
        if (!Integer.valueOf(0).equals(db.getStatus())) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "仅草稿状态的公告允许发布");
        }
        db.setStatus(1);
        db.setUpdateTime(LocalDateTime.now());
        db.setUpdateBy(operatorId);
        announcementMapper.updateById(db);
        log.info("[Announcement] publish success, id={}", id);

        // 到点推送安排：若生效窗口已到则立即推送并标记；否则投递延时消息，等待到点推送
        LocalDateTime now = LocalDateTime.now();
        if (db.getActiveFrom() == null || !db.getActiveFrom().isAfter(now)) {
            // 立即触发领域事件，由订阅者负责 WS 推送
            domainEventPublisher.publish(new AnnouncementPublishedEvent(db.getId(), db.getTitle()));
            log.info("已发布公告并触发领域事件: id={}", db.getId());
            Announcement upd = new Announcement();
            upd.setId(db.getId());
            upd.setNotified(1);
            upd.setFirstPushTime(now);
            upd.setUpdateTime(now);
            upd.setUpdateBy(operatorId);
            announcementMapper.updateById(upd);
        } else {
            long delayMs = Math.max(0, Duration.between(now, db.getActiveFrom()).toMillis());
            Map<String, Object> payload = new HashMap<>();
            payload.put("announcementId", db.getId());
            payload.put("title", db.getTitle());
            payload.put("activeFrom", db.getActiveFrom().toString());
            rabbitTemplate.convertAndSend(EXCHANGE_DELAY, RK_DELAY, payload, message -> {
                message.getMessageProperties().setExpiration(String.valueOf(delayMs));
                message.getMessageProperties().setDeliveryMode(MessageDeliveryMode.PERSISTENT);
                return message;
            });
            log.info("已投递延时推送: id={}, delayMs={}", db.getId(), delayMs);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void offline(Long id, Long operatorId, String operatorName) {
        log.info("[Announcement] offline start, id={}, operatorId={}", id, operatorId);
        Announcement db = announcementMapper.selectById(id);
        if (db == null) {
            throw new ServiceException(ErrorCode.NOTIFY_ANNOUNCEMENT_NOT_FOUND);
        }
        if (!Integer.valueOf(1).equals(db.getStatus())) {
            throw new ServiceException(ErrorCode.OPERATION_NOT_ALLOWED, "仅已发布状态的公告允许下线");
        }
        db.setStatus(2);
        db.setUpdateTime(LocalDateTime.now());
        db.setUpdateBy(operatorId);
        announcementMapper.updateById(db);
        log.info("[Announcement] offline success, id={}", id);
    }

    @Override
    public Page<AnnouncementVO> pageAdmin(AnnouncementQueryRequest request, long pageNum, long pageSize) {
        log.info("[Announcement] page admin, status={}, from={}, to={}", request.getStatus(), request.getFromTime(), request.getToTime());
        Page<Announcement> page = new Page<>(pageNum, pageSize);
        LambdaQueryWrapper<Announcement> qw = new LambdaQueryWrapper<>();
        if (request.getStatus() != null) {
            qw.eq(Announcement::getStatus, request.getStatus());
        }
        if (request.getFromTime() != null) {
            qw.ge(Announcement::getCreateTime, request.getFromTime());
        }
        if (request.getToTime() != null) {
            qw.le(Announcement::getCreateTime, request.getToTime());
        }
        qw.orderByDesc(Announcement::getCreateTime);
        Page<Announcement> entityPage = announcementMapper.selectPage(page, qw);
        Page<AnnouncementVO> voPage = new Page<>(pageNum, pageSize, entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public Page<AnnouncementVO> pageVisible(long pageNum, long pageSize) {
        log.info("[Announcement] page visible");
        Page<Announcement> page = new Page<>(pageNum, pageSize);
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Announcement> qw = new LambdaQueryWrapper<>();
        qw.eq(Announcement::getStatus, 1)
          .and(w -> w.isNull(Announcement::getActiveFrom).or().le(Announcement::getActiveFrom, now))
          .and(w -> w.isNull(Announcement::getActiveTo).or().ge(Announcement::getActiveTo, now))
          .orderByDesc(Announcement::getPriority)
          .orderByDesc(Announcement::getCreateTime);
        Page<Announcement> entityPage = announcementMapper.selectPage(page, qw);
        Page<AnnouncementVO> voPage = new Page<>(pageNum, pageSize, entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public AnnouncementVO getById(Long id) {
        Announcement a = announcementMapper.selectById(id);
        if (a == null) {
            throw new ServiceException(ErrorCode.NOTIFY_ANNOUNCEMENT_NOT_FOUND);
        }
        return toVO(a);
    }

    @Override
    public Page<AnnouncementVO> pageVisibleUnread(Long userId, long pageNum, long pageSize) {
        log.info("[Announcement] page visible unread, userId={}", userId);
        Page<Announcement> page = new Page<>(pageNum, pageSize);
        LocalDateTime now = LocalDateTime.now();
        LambdaQueryWrapper<Announcement> qw = new LambdaQueryWrapper<>();
        qw.eq(Announcement::getStatus, 1)
          .and(w -> w.isNull(Announcement::getActiveFrom).or().le(Announcement::getActiveFrom, now))
          .and(w -> w.isNull(Announcement::getActiveTo).or().ge(Announcement::getActiveTo, now))
          .notExists("select 1 from announcement_reads ar where ar.announcement_id = announcements.id and ar.user_id = {0}", userId)
          .orderByDesc(Announcement::getPriority)
          .orderByDesc(Announcement::getCreateTime);
        Page<Announcement> entityPage = announcementMapper.selectPage(page, qw);
        Page<AnnouncementVO> voPage = new Page<>(pageNum, pageSize, entityPage.getTotal());
        voPage.setRecords(entityPage.getRecords().stream().map(this::toVO).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void read(Long announcementId, Long userId) {
        log.info("[Announcement] read mark, announcementId={}, userId={}", announcementId, userId);
        if (announcementId == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "公告ID不能为空");
        }
        if (userId == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "用户未登录");
        }
        Announcement a = announcementMapper.selectById(announcementId);
        if (a == null) {
            throw new ServiceException(ErrorCode.NOTIFY_ANNOUNCEMENT_NOT_FOUND);
        }
        // 幂等写入：若不存在则插入
        LambdaQueryWrapper<AnnouncementRead> qw = new LambdaQueryWrapper<>();
        qw.eq(AnnouncementRead::getAnnouncementId, announcementId)
          .eq(AnnouncementRead::getUserId, userId);
        AnnouncementRead exist = announcementReadMapper.selectOne(qw);
        if (exist == null) {
            AnnouncementRead ar = new AnnouncementRead();
            ar.setAnnouncementId(announcementId);
            ar.setUserId(userId);
            ar.setReadAt(LocalDateTime.now());
            ar.setCreateTime(LocalDateTime.now());
            ar.setUpdateTime(LocalDateTime.now());
            announcementReadMapper.insert(ar);
            log.info("[Announcement] read recorded, announcementId={}, userId={}", announcementId, userId);
        }
    }

    /**
     * 校验时间窗口：要求 activeFrom <= activeTo；任一为 null 视为无限端。
     *
     * @param from 生效起始
     * @param to   生效终止
     * @throws ServiceException 当时间窗口非法时抛出
     */
    private void validateWindow(LocalDateTime from, LocalDateTime to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ServiceException(ErrorCode.NOTIFY_SCHEDULE_INVALID, "生效时间必须早于失效时间");
        }
    }

    /**
     * 将可能为 null 的 Integer 转换为 0。
     */
    private int nullToZero(Integer v) {
        return v == null ? 0 : v;
    }

    /**
     * 实体转 VO（私有方法）。
     */
    private AnnouncementVO toVO(Announcement a) {
        AnnouncementVO vo = new AnnouncementVO();
        BeanUtils.copyProperties(a, vo);
        return vo;
    }
}


