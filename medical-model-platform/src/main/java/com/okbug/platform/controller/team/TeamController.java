/**
 * 团队管理控制器：团队相关API（占位，无具体接口）
 *
 * 规范：
 * - 所有接口统一返回 ApiResult<T>
 * - 业务异常由 ServiceException 抛出并由全局异常处理
 */
package com.okbug.platform.controller.team;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.dto.team.AddMemberRequest;
import com.okbug.platform.dto.team.CreateTeamRequest;
import com.okbug.platform.dto.team.UpdateMemberRequest;
import com.okbug.platform.dto.team.UpdateTeamRequest;
import com.okbug.platform.dto.team.TransferOwnerRequest;
import com.okbug.platform.dto.team.SetTeamStatusRequest;
import com.okbug.platform.entity.team.Team;
import com.okbug.platform.service.security.TeamAccessService;
import com.okbug.platform.service.team.TeamService;
import com.okbug.platform.vo.team.TeamVO;
 
import com.okbug.platform.common.annotation.OperationLog;
import com.okbug.platform.common.enums.OperationModule;
import com.okbug.platform.common.enums.OperationType;
import com.okbug.platform.ws.RealtimeWebSocketHandler;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
 
import org.springframework.web.bind.annotation.RequestParam;
 

@Slf4j
@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
@Tag(name = "团队管理接口")
public class TeamController {

    private final TeamService teamService;
    // 保留注入（未来可用于细粒度鉴权拦截器），当前业务委派到 service 层
    @SuppressWarnings("unused")
    private final TeamAccessService teamAccessService;
    private final RealtimeWebSocketHandler websocketHandler;
    

    @GetMapping
    @Operation(summary = "分页查询团队列表（普通用户仅返回所属团队；管理员/超管可见范围更大）")
    @SaCheckLogin
    public ApiResult<IPage<TeamVO>> list(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "关键字") @RequestParam(required = false) String keyword
    ) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        return ApiResult.success(teamService.listTeams(currentUserId, pageNum, pageSize, keyword));
    }

    @PostMapping
    @Operation(summary = "创建团队并设当前用户为OWNER")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.USER, typeEnum = OperationType.CREATE, description = "创建团队", async = true)
    public ApiResult<TeamVO> create(@Valid @RequestBody CreateTeamRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        Team team = teamService.createTeam(currentUserId, request.getTeamName(), request.getDescription());
        websocketHandler.sendTeamMemberChanged(team.getId(), currentUserId, "add", com.okbug.platform.entity.team.TeamMember.ROLE_OWNER);
        return ApiResult.success(teamService.getTeamDetail(currentUserId, team.getId()));
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取团队详情（成员可见，管理操作需额外校验）")
    @SaCheckLogin
    public ApiResult<TeamVO> detail(@Parameter(description = "团队ID") @PathVariable Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        return ApiResult.success(teamService.getTeamDetail(currentUserId, id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新团队信息")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.USER, typeEnum = OperationType.UPDATE, description = "更新团队信息", async = true)
    public ApiResult<Void> update(@Parameter(description = "团队ID") @PathVariable Long id,
                                  @Valid @RequestBody UpdateTeamRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        teamService.updateTeamSecure(currentUserId, id, request.getTeamName(), request.getDescription(), request.getStatus());
        return ApiResult.success();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除团队（软删）")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.USER, typeEnum = OperationType.DELETE, description = "删除团队", async = true)
    public ApiResult<Void> delete(@Parameter(description = "团队ID") @PathVariable Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        teamService.softDeleteTeamSecure(currentUserId, id);
        return ApiResult.success();
    }

    @GetMapping("/preview-by-code")
    @Operation(summary = "通过团队码预览团队信息（无需成员身份）")
    @SaCheckLogin
    public ApiResult<TeamVO> previewByCode(
            @Parameter(description = "团队码") @RequestParam String teamCode
    ) {
        return ApiResult.success(teamService.previewByCode(teamCode));
    }

    @GetMapping("/{id}/members")
    @Operation(summary = "成员列表（分页）")
    @SaCheckLogin
    public ApiResult<IPage<com.okbug.platform.vo.team.TeamMemberVO>> listMembers(
            @Parameter(description = "团队ID") @PathVariable Long id,
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") Integer pageNum,
            @Parameter(description = "每页大小") @RequestParam(defaultValue = "10") Integer pageSize,
            @Parameter(description = "角色") @RequestParam(required = false) String role,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status
    ) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        return ApiResult.success(teamService.listMembers(currentUserId, id, pageNum, pageSize, role, status));
    }

    @PostMapping("/{id}/members")
    @Operation(summary = "添加成员")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.USER, typeEnum = OperationType.CREATE, description = "添加团队成员", async = true)
    public ApiResult<Void> addMember(@Parameter(description = "团队ID") @PathVariable Long id,
                                     @Valid @RequestBody AddMemberRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        teamService.addMemberSecure(currentUserId, id, request.getUserId(), request.getRole());
        websocketHandler.sendTeamMemberChanged(id, request.getUserId(), "add", request.getRole());
        return ApiResult.success();
    }

    @PutMapping("/{id}/members/{userId}")
    @Operation(summary = "更新成员角色/状态")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.USER, typeEnum = OperationType.UPDATE, description = "更新团队成员", async = true)
    public ApiResult<Void> updateMember(@Parameter(description = "团队ID") @PathVariable Long id,
                                        @Parameter(description = "用户ID") @PathVariable Long userId,
                                        @Valid @RequestBody UpdateMemberRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        teamService.updateMemberSecure(currentUserId, id, userId, request.getRole(), request.getStatus());
        websocketHandler.sendTeamMemberChanged(id, userId, "update", request.getRole());
        return ApiResult.success();
    }

    @DeleteMapping("/{id}/members/{userId}")
    @Operation(summary = "移除成员")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.USER, typeEnum = OperationType.DELETE, description = "移除团队成员", async = true)
    public ApiResult<Void> removeMember(@Parameter(description = "团队ID") @PathVariable Long id,
                                        @Parameter(description = "用户ID") @PathVariable Long userId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        teamService.removeMemberSecure(currentUserId, id, userId);
        websocketHandler.sendTeamMemberChanged(id, userId, "remove", null);
        return ApiResult.success();
    }

    @PostMapping("/{id}/transfer-owner")
    @Operation(summary = "转移团队拥有者（仅当前OWNER可执行）")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.USER, typeEnum = OperationType.UPDATE, description = "转移团队拥有者", async = true)
    public ApiResult<Void> transferOwner(@Parameter(description = "团队ID") @PathVariable Long id,
                                         @Valid @RequestBody TransferOwnerRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        teamService.transferOwnerSecure(currentUserId, id, request.getToUserId());
        websocketHandler.sendTeamMemberChanged(id, request.getToUserId(), "transfer_owner", com.okbug.platform.entity.team.TeamMember.ROLE_OWNER);
        return ApiResult.success();
    }

    @PostMapping("/{id}/exit")
    @Operation(summary = "退出团队（当前登录用户退出自身所在团队，OWNER不可退出）")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.USER, typeEnum = OperationType.DELETE, description = "退出团队", async = true)
    public ApiResult<Void> exitTeam(@Parameter(description = "团队ID") @PathVariable Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        teamService.exitTeamSelf(currentUserId, id);
        return ApiResult.success();
    }

    @PostMapping("/{id}/dissolve")
    @Operation(summary = "解散团队（仅OWNER可执行，软删除团队并禁用成员关系）")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.USER, typeEnum = OperationType.DELETE, description = "解散团队", async = true)
    public ApiResult<Void> dissolveTeam(@Parameter(description = "团队ID") @PathVariable Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        teamService.dissolveTeamSecure(currentUserId, id);
        return ApiResult.success();
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "设置团队状态（仅 SUPER_ADMIN）")
    @SaCheckLogin
    @SaCheckRole("SUPER_ADMIN")
    @OperationLog(moduleEnum = OperationModule.USER, typeEnum = OperationType.UPDATE, description = "设置团队状态(超级管理员)", async = true)
    public ApiResult<Void> setStatus(@Parameter(description = "团队ID") @PathVariable Long id,
                                     @Valid @RequestBody SetTeamStatusRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        teamService.setTeamStatusBySuperAdmin(currentUserId, id, request.getStatus());
        return ApiResult.success();
    }
    
    
}


