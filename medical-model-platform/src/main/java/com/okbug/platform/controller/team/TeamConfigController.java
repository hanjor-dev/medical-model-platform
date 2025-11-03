/**
 * 团队配置控制器：团队配置相关API（占位，无具体接口）
 */
package com.okbug.platform.controller.team;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.dto.team.UpdateTeamConfigRequest;
import com.okbug.platform.service.security.TeamAccessService;
import com.okbug.platform.service.team.TeamConfigService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

import com.okbug.platform.common.annotation.OperationLog;
import com.okbug.platform.common.enums.OperationModule;
import com.okbug.platform.common.enums.OperationType;

@Slf4j
@RestController
@RequestMapping("/team/config")
@RequiredArgsConstructor
@Tag(name = "团队配置接口")
public class TeamConfigController {

    private final TeamConfigService teamConfigService;
    // 保留注入（未来可用于细粒度鉴权拦截器），当前业务委派到 service 层
    private final TeamAccessService teamAccessService;

    /**
     * 获取团队配置（全部键值对）
     */
    @GetMapping("/{teamId}")
    @Operation(summary = "获取团队配置")
    @SaCheckLogin
    public ApiResult<Map<String, String>> getConfigs(@Parameter(description = "团队ID") @PathVariable Long teamId) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        log.debug("API get team-configs: operator={}, teamId={}", currentUserId, teamId);
        return ApiResult.success(teamConfigService.getConfigs(currentUserId, teamId));
    }

    /**
     * 更新团队配置（覆盖写入）
     */
    @PutMapping
    @Operation(summary = "更新团队配置")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.USER, typeEnum = OperationType.UPDATE, description = "更新团队配置", async = true)
    public ApiResult<Void> updateConfigs(@Valid @RequestBody UpdateTeamConfigRequest request) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        log.info("API update team-configs: operator={}, teamId={}, keys={}",
                currentUserId, request.getTeamId(), request.getConfigs() == null ? 0 : request.getConfigs().size());
        teamConfigService.updateConfigs(currentUserId, request);
        return ApiResult.success();
    }
}


