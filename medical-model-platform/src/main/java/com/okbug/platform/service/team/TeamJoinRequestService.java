/**
 * 团队加入申请服务接口
 * 定义加入申请的创建、审批、撤销、分页查询等核心业务能力；
 * Controller 层仅负责鉴权与参数接收，所有业务规则、异常抛出与通知在 Service 层完成。
 */
package com.okbug.platform.service.team;

import java.time.LocalDateTime;

public interface TeamJoinRequestService {
    /**
     * 是否存在未处理的加入申请
     *
     * @param teamId 团队ID，不能为空
     * @param userId 用户ID，不能为空
     * @return true 表示存在待处理申请；false 表示不存在
     */
    boolean existsPending(Long teamId, Long userId);

    /**
     * 创建加入申请（待审批）并返回请求ID
     *
     * 注意：该方法不做业务校验（如限流、幂等、权限等），仅做数据落库。
     * 外部应优先调用更高层的 {@link #submitJoinRequest(Long, com.okbug.platform.dto.team.JoinRequestSubmit)}。
     *
     * @param teamId 团队ID
     * @param userId 申请用户ID
     * @param teamCode 团队加入码（可选）
     * @param requestReason 申请理由（可选）
     * @return 新建的申请记录ID
     */
    Long createPending(Long teamId, Long userId, String teamCode, String requestReason);

    /**
     * 审批加入申请：通过或拒绝
     *
     * 内部包含：权限校验、重复处理校验、成员入组（通过时）、通知下发。
     *
     * @param requestId 加入申请ID
     * @param operatorUserId 操作人用户ID
     * @param approve 是否通过
     * @param reason 处理备注
     */
    void processJoinRequest(Long requestId, Long operatorUserId, boolean approve, String reason);

    /**
     * 提交加入申请（含限流/幂等/成员校验/团队校验/通知）
     *
     * @param userId 当前登录用户ID
     * @param request 提交请求体（包含 teamId/teamCode/requestReason）
     */
    void submitJoinRequest(Long userId, com.okbug.platform.dto.team.JoinRequestSubmit request);

    /**
     * 撤销加入申请（仅申请人可撤销，含通知）
     *
     * @param requestId 加入申请ID
     * @param userId 申请人用户ID
     */
    void cancelJoinRequest(Long requestId, Long userId);

    /**
     * 分页查询加入申请列表（管理权限校验内置）
     *
     * @param operatorUserId 查询人用户ID（需要具备团队管理权限）
     * @param teamId 团队ID
     * @param pageNum 页码（从1开始）
     * @param pageSize 每页大小
     * @param status 申请状态过滤（可选，未传则查询全部 0/1/2）
     * @param applicantUserId 申请人过滤（可选）
     * @return 分页结果
     */
    com.baomidou.mybatisplus.core.metadata.IPage<com.okbug.platform.vo.team.TeamJoinRequestVO> listJoinRequests(
            Long operatorUserId,
            Long teamId,
            Integer pageNum,
            Integer pageSize,
            Integer status,
            Long applicantUserId,
            String applicantKeyword,
            LocalDateTime beginTime,
            LocalDateTime endTime,
            String requestReasonKeyword);

    /**
     * 获取加入申请详情（管理权限校验内置）
     *
     * @param operatorUserId 查询人
     * @param requestId 申请ID
     * @return 详情
     */
    com.okbug.platform.vo.team.TeamJoinRequestVO getJoinRequestDetail(Long operatorUserId, Long requestId);
}


