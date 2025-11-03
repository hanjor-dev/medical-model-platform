package com.okbug.platform.service.system.message;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.dto.system.message.request.AnnouncementCreateRequest;
import com.okbug.platform.dto.system.message.request.AnnouncementQueryRequest;
import com.okbug.platform.dto.system.message.request.AnnouncementUpdateRequest;
import com.okbug.platform.dto.system.message.response.AnnouncementVO;

/**
 * 公告服务接口：系统/消息模块
 */
public interface AnnouncementService {

    /**
     * 创建公告（草稿）
     *
     * @param request 公告创建请求
     * @param operatorId 操作人ID
     * @param operatorName 操作人名称
     * @return 公告ID
     */
    Long create(AnnouncementCreateRequest request, Long operatorId, String operatorName);

    /**
     * 更新公告（仅草稿可更新）
     *
     * @param id 公告ID
     * @param request 更新请求
     * @param operatorId 操作人ID
     * @param operatorName 操作人名称
     */
    void update(Long id, AnnouncementUpdateRequest request, Long operatorId, String operatorName);

    /** 发布公告（立即或定时） */
    void publish(Long id, Long operatorId, String operatorName);

    /** 下线公告 */
    void offline(Long id, Long operatorId, String operatorName);

    /**
     * 管理端分页查询
     *
     * @param request 查询条件
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页数据
     */
    Page<AnnouncementVO> pageAdmin(AnnouncementQueryRequest request, long pageNum, long pageSize);

    /** 用户端可见公告列表（按优先级倒序） */
    Page<AnnouncementVO> pageVisible(long pageNum, long pageSize);

    /** 用户端未读且可见公告列表（按优先级倒序） */
    Page<AnnouncementVO> pageVisibleUnread(Long userId, long pageNum, long pageSize);

    /** 根据ID获取公告详情 */
    AnnouncementVO getById(Long id);

    /**
     * 标记已读
     *
     * @param announcementId 公告ID
     * @param userId 用户ID
     */
    void read(Long announcementId, Long userId);
}


