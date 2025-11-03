package com.okbug.platform.service.system.message;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.dto.system.message.request.MessageCreateRequest;
import com.okbug.platform.dto.system.message.request.MessageQueryRequest;
import com.okbug.platform.dto.system.message.response.MessageVO;

/**
 * 消息服务：系统/消息模块
 */
public interface MessageService {

    /**
     * 创建站内消息
     *
     * @param request 创建请求，包含接收人、模板、变量、调度时间
     * @param operatorId 操作人ID，不能为空
     * @param operatorName 操作人名称，可为空
     * @return 消息ID
     * @throws com.okbug.platform.common.base.ServiceException 参数缺失/无效、模板不存在或被禁用时抛出
     */
    Long create(MessageCreateRequest request, Long operatorId, String operatorName);

    /**
     * 取消未完成的站内消息
     *
     * @param id 消息ID
     * @param operatorId 操作人ID
     * @param operatorName 操作人名称
     * @throws com.okbug.platform.common.base.ServiceException 消息不存在或状态不允许取消时抛出
     */
    void cancel(Long id, Long operatorId, String operatorName);

    /**
     * 分页查询站内消息
     *
     * @param request 查询条件
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @return 分页数据
     */
    Page<MessageVO> page(MessageQueryRequest request, long pageNum, long pageSize);

    /**
     * 从领域事件创建消息（领域事件驱动入口）
     *
     * 说明：
     * - 事件订阅者根据业务事件选择模板、构造变量与调度时间后调用
     * - 本方法只负责创建站内消息记录与必要的 WS 通知，渠道路由由上层 Router 控制
     *
     * @param request 创建请求
     * @param operatorId 操作人ID（系统事件可传0或具体触发人）
     * @param operatorName 操作人名
     * @return 消息ID
     */
    Long createFromEvent(MessageCreateRequest request, Long operatorId, String operatorName);

    /**
     * 获取用户自己的消息详情
     */
    MessageVO getOwnDetail(Long id, Long userId);

    /**
     * 将用户的某条消息标记为已读
     */
    void markRead(Long id, Long userId);

    /**
     * 将该用户全部消息标记为已读
     */
    void markAllRead(Long userId);

    /**
     * 统计该用户全部消息的未读总数
     */
    long countUnreadTotal(Long userId);
}


