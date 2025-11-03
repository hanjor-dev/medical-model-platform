/**
 * 团队加入申请控制器：加入申请相关API（占位，无具体接口）
 */
package com.okbug.platform.controller.team;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.dto.team.JoinRequestProcess;
import com.okbug.platform.dto.team.JoinRequestSubmit;
import com.okbug.platform.service.team.TeamJoinRequestService;
import com.okbug.platform.common.annotation.OperationLog;
import com.okbug.platform.common.enums.OperationModule;
import com.okbug.platform.common.enums.OperationType;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/team/join-request")
@RequiredArgsConstructor
@Tag(name = "团队加入申请接口")
public class TeamJoinRequestController {

    private final TeamJoinRequestService teamJoinRequestService;

    /**
     * 提交加入申请
     * 业务逻辑在 Service 层进行：限流/幂等/团队与成员校验/通知。
     */
    @PostMapping
    @Operation(summary = "提交加入申请")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.USER, typeEnum = OperationType.CREATE, description = "提交加入申请", async = true)
    public ApiResult<Void> submit(@Valid @RequestBody JoinRequestSubmit request) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("API submit join-request: userId={}, teamId={}", userId, request.getTeamId());
        teamJoinRequestService.submitJoinRequest(userId, request);
        return ApiResult.success();
    }

    /**
     * 查询团队的加入申请列表（分页）
     */
    @GetMapping("/{teamId}")
    @Operation(summary = "查询团队的加入申请列表（分页，status可选，未传则查询全部）")
    @SaCheckLogin
    public ApiResult<com.baomidou.mybatisplus.core.metadata.IPage<com.okbug.platform.vo.team.TeamJoinRequestVO>> listPending(
            @Parameter(description = "团队ID") @PathVariable Long teamId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "状态(0/1/2)") @RequestParam(required = false) Integer status,
            @Parameter(description = "申请人用户ID") @RequestParam(required = false) Long userId,
            @Parameter(description = "申请人关键词(昵称或用户名)") @RequestParam(required = false) String applicantKeyword,
            @Parameter(description = "开始时间(yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") java.time.LocalDateTime beginTime,
            @Parameter(description = "结束时间(yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") java.time.LocalDateTime endTime,
            @Parameter(description = "备注/申请理由关键词") @RequestParam(required = false) String requestReasonKeyword
    ) {
        Long operatorUserId = StpUtil.getLoginIdAsLong();
        if (status != null && (status < 0 || status > 2)) {
            return ApiResult.error(com.okbug.platform.common.base.ErrorCode.PARAM_INVALID, "status 取值必须为 0/1/2");
        }
        log.debug("API list join-requests: operator={}, teamId={}, pageNum={}, pageSize={}, status={}, applicant={}, kw={}, time=[{} - {}], reasonKw={}",
                operatorUserId, teamId, pageNum, pageSize, status, userId, applicantKeyword, beginTime, endTime, requestReasonKeyword);
        return ApiResult.success(teamJoinRequestService.listJoinRequests(operatorUserId, teamId, pageNum, pageSize, status, userId, applicantKeyword, beginTime, endTime, requestReasonKeyword));
    }

    /**
     * 获取加入申请详情
     */
    @GetMapping("/detail/{requestId}")
    @Operation(summary = "获取加入申请详情")
    @SaCheckLogin
    public ApiResult<com.okbug.platform.vo.team.TeamJoinRequestVO> detail(@Parameter(description = "加入申请ID") @PathVariable Long requestId) {
        Long operatorUserId = StpUtil.getLoginIdAsLong();
        return ApiResult.success(teamJoinRequestService.getJoinRequestDetail(operatorUserId, requestId));
    }

    /**
     * 审批加入申请：通过或拒绝
     */
    @PostMapping("/process")
    @Operation(summary = "审批加入申请：通过或拒绝")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.USER, typeEnum = OperationType.UPDATE, description = "审批加入申请", async = true)
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> process(@Valid @RequestBody JoinRequestProcess request) {
        Long adminUserId = StpUtil.getLoginIdAsLong();
        log.info("API process join-request: operator={}, requestId={}, approve={}", adminUserId, request.getRequestId(), request.getApprove());
        teamJoinRequestService.processJoinRequest(request.getRequestId(), adminUserId, Boolean.TRUE.equals(request.getApprove()), request.getReason());
        return ApiResult.success();
    }

    /**
     * 撤销加入申请（申请人自撤销）
     */
    @DeleteMapping("/{requestId}")
    @Operation(summary = "撤销加入申请（申请人自撤销）")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.USER, typeEnum = OperationType.DELETE, description = "撤销加入申请", async = true)
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> cancel(@Parameter(description = "加入申请ID") @PathVariable Long requestId) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("API cancel join-request: requestId={}, userId={}", requestId, userId);
        teamJoinRequestService.cancelJoinRequest(requestId, userId);
        return ApiResult.success();
    }
}


