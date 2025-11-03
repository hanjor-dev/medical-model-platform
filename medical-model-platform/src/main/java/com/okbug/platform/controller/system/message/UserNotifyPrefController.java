package com.okbug.platform.controller.system.message;

import cn.dev33.satoken.stp.StpUtil;
import cn.dev33.satoken.annotation.SaCheckLogin;
import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.common.base.ServiceException;
import com.okbug.platform.common.base.ErrorCode;
import com.okbug.platform.entity.system.message.UserNotifyChannelPref;
import com.okbug.platform.entity.system.message.UserNotifyTypePref;
import com.okbug.platform.service.system.message.UserNotifyPreferenceService;
import com.okbug.platform.service.system.SystemDictService;
import com.okbug.platform.dto.system.DictDataDTO;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import com.okbug.platform.common.annotation.OperationLog;
import com.okbug.platform.common.enums.OperationModule;
import com.okbug.platform.common.enums.OperationType;

/**
 * 用户通知偏好接口
 */
@Tag(name = "用户-通知偏好")
@RestController
@RequestMapping("/notify/user/prefs")
@RequiredArgsConstructor
public class UserNotifyPrefController {

    private final UserNotifyPreferenceService prefService;
    private final SystemDictService systemDictService;

    @Operation(summary = "获取我的通知偏好-渠道")
    @GetMapping("/channels")
    @SaCheckLogin
    public ApiResult<java.util.List<UserNotifyChannelPref>> myChannelPrefs() {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResult.success(prefService.listChannelPrefs(userId));
    }

    @Operation(summary = "保存我的通知偏好-渠道")
    @PutMapping("/channels")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.SYSTEM, typeEnum = OperationType.UPDATE, description = "保存我的通知偏好-渠道", async = true)
    public ApiResult<Void> saveMyChannelPrefs(@Parameter(description = "渠道偏好列表") @RequestBody java.util.List<UserNotifyChannelPref> prefs) {
        Long userId = StpUtil.getLoginIdAsLong();
        if (prefs == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "请求体不能为空");
        }
        prefService.saveOrUpdateChannelPrefs(userId, prefs, userId, String.valueOf(userId));
        return ApiResult.success();
    }

    @Operation(summary = "获取我的通知偏好-类型")
    @GetMapping("/types")
    @SaCheckLogin
    public ApiResult<java.util.List<UserNotifyTypePref>> myTypePrefs() {
        Long userId = StpUtil.getLoginIdAsLong();
        return ApiResult.success(prefService.listTypePrefs(userId));
    }

    @Operation(summary = "保存我的通知偏好-类型")
    @PutMapping("/types")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.SYSTEM, typeEnum = OperationType.UPDATE, description = "保存我的通知偏好-类型", async = true)
    public ApiResult<Void> saveMyTypePrefs(@Parameter(description = "类型偏好列表") @RequestBody java.util.List<UserNotifyTypePref> prefs) {
        Long userId = StpUtil.getLoginIdAsLong();
        if (prefs == null) {
            throw new ServiceException(ErrorCode.PARAM_MISSING, "请求体不能为空");
        }
        prefService.saveOrUpdateTypePrefs(userId, prefs, userId, String.valueOf(userId));
        return ApiResult.success();
    }

    @Operation(summary = "获取通知渠道选项（来自字典 DICT_4.1）")
    @GetMapping("/options/channels")
    @SaCheckLogin
    public ApiResult<java.util.List<DictDataDTO>> channelOptions() {
        return ApiResult.success(systemDictService.getChildrenOptionsByCode("DICT_4.1"));
    }

    @Operation(summary = "获取通知类型选项（来自字典 DICT_4.2）")
    @GetMapping("/options/types")
    @SaCheckLogin
    public ApiResult<java.util.List<DictDataDTO>> typeOptions() {
        return ApiResult.success(systemDictService.getChildrenOptionsByCode("DICT_4.2"));
    }
}
