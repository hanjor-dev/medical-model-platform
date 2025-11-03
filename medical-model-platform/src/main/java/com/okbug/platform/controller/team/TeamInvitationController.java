/**
 * 团队邀请控制器：团队邀请相关API（占位，无具体接口）
 */
package com.okbug.platform.controller.team;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.dto.team.InvitationAcceptRequest;
import com.okbug.platform.dto.team.InvitationCreateRequest;
import com.okbug.platform.entity.team.TeamInvitation;
import com.okbug.platform.service.team.TeamInvitationService;
import com.okbug.platform.common.annotation.OperationLog;
import com.okbug.platform.common.enums.OperationModule;
import com.okbug.platform.common.enums.OperationType;
import com.okbug.platform.service.security.TeamAccessService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/team/invitation")
@RequiredArgsConstructor
@Tag(name = "团队邀请接口")
public class TeamInvitationController {

    private final TeamInvitationService teamInvitationService;
    // 保留注入（未来可用于细粒度鉴权拦截器），当前业务委派到 service 层
    private final TeamAccessService teamAccessService;

    /**
     * 创建邀请
     * 限流与幂等在接口内实现；权限在 TeamAccessService 校验。
     */
    @PostMapping
    @Operation(summary = "创建邀请")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.USER, typeEnum = OperationType.CREATE, description = "创建团队邀请", async = true)
    public ApiResult<String> create(@Valid @RequestBody InvitationCreateRequest request) {
        Long inviterUserId = StpUtil.getLoginIdAsLong();
        log.info("API create invitation: inviter={}, teamId={}, invitedUserId={}, email={}, phone={}, role={}",
                inviterUserId, request.getTeamId(), request.getInvitedUserId(), request.getInvitedEmail(), request.getInvitedPhone(), request.getRole());
        String token = teamInvitationService.createInvitation(inviterUserId, request.getTeamId(), request.getInvitedUserId(), request.getInvitedEmail(), request.getInvitedPhone(), request.getRole());
        return ApiResult.success(token);
    }

    /**
     * 获取邀请基础链接（用于前端拼接完整邀请 URL）。
     */
    @GetMapping("/base-url")
    @Operation(summary = "获取团队邀请基础链接（从系统配置）")
    @SaCheckLogin
    public ApiResult<String> getInviteBaseUrl() {
        return ApiResult.success(teamInvitationService.getInviteBaseUrl());
    }

    /**
     * 邀请列表分页查询
     */
    @GetMapping("/{teamId}")
    @Operation(summary = "查询团队的邀请列表（分页，可选过滤）")
    @SaCheckLogin
    public ApiResult<com.baomidou.mybatisplus.core.metadata.IPage<TeamInvitation>> list(
            @Parameter(description = "团队ID") @PathVariable Long teamId,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "状态(0/1/2/3)") @RequestParam(required = false) Integer status,
            @Parameter(description = "被邀请用户ID") @RequestParam(required = false) Long invitedUserId
    ) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        log.debug("API list invitations: operator={}, teamId={}, pageNum={}, pageSize={}, status={}, invitedUserId={}",
                currentUserId, teamId, pageNum, pageSize, status, invitedUserId);
        return ApiResult.success(teamInvitationService.listInvitations(currentUserId, teamId, pageNum, pageSize, status, invitedUserId));
    }

    /**
     * 撤回邀请
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "撤回邀请")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.USER, typeEnum = OperationType.DELETE, description = "撤回团队邀请", async = true)
    public ApiResult<Void> revoke(@Parameter(description = "邀请ID") @PathVariable Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        log.info("API revoke invitation: operator={}, invitationId={}", currentUserId, id);
        teamInvitationService.revokeInvitation(id, currentUserId);
        return ApiResult.success();
    }

    /**
     * 接受邀请
     */
    @PostMapping("/accept")
    @Operation(summary = "接受邀请")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.USER, typeEnum = OperationType.UPDATE, description = "接受团队邀请", async = true)
    @Transactional(rollbackFor = Exception.class)
    public ApiResult<Void> accept(@Valid @RequestBody InvitationAcceptRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("API accept invitation: userId={}, token=***masked***", userId);
        teamInvitationService.acceptInvitation(request.getToken(), userId);
        return ApiResult.success();
    }
}


