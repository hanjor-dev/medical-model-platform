package com.okbug.platform.controller.credit;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.annotation.SaCheckRole;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.common.base.ApiResult;
import com.okbug.platform.entity.credit.CreditRedeemCode;
import com.okbug.platform.service.credit.CreditRedeemCodeService;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.okbug.platform.common.annotation.OperationLog;
import com.okbug.platform.common.enums.OperationModule;
import com.okbug.platform.common.enums.OperationType;

@Slf4j
@RestController
@RequestMapping("/credits/redeem")
@RequiredArgsConstructor
@Validated
@Tag(name = "积分兑换码管理")
public class CreditRedeemCodeController {

    private final CreditRedeemCodeService redeemCodeService;

    @PostMapping("/generate")
    @Operation(summary = "生成兑换码（仅超级管理员菜单权限）")
    @SaCheckPermission("credit-system:user-credits")
    @SaCheckRole("SUPER_ADMIN")
    @OperationLog(moduleEnum = OperationModule.CREDIT, typeEnum = OperationType.CREATE, description = "生成积分兑换码", async = true)
    public ApiResult<CreditRedeemCode> generate(
            @Parameter(description = "积分类型编码") @NotBlank @RequestParam String creditTypeCode,
            @Parameter(description = "数量(正数)") @NotNull @RequestParam BigDecimal amount,
            @Parameter(description = "过期时间，格式yyyy-MM-ddTHH:mm:ss或yyyy-MM-dd HH:mm:ss") @RequestParam(required = false) String expireTime,
            @Parameter(description = "备注") @RequestParam(required = false) String remark
    ) {
        LocalDateTime exp = null;
        if (expireTime != null && !expireTime.trim().isEmpty()) {
            exp = LocalDateTime.parse(expireTime.replace(" ", "T"));
        }
        CreditRedeemCode code = redeemCodeService.generate(creditTypeCode, amount, exp, remark);
        return ApiResult.success("生成成功", code);
    }

    @GetMapping("/page")
    @Operation(summary = "兑换码分页列表")
    @SaCheckPermission("credit-system:user-credits")
    @SaCheckRole("SUPER_ADMIN")
    public ApiResult<IPage<CreditRedeemCode>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") @Min(1) Integer pageNum,
            @Parameter(description = "大小") @RequestParam(defaultValue = "10") @Min(1) Integer pageSize,
            @Parameter(description = "关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "状态") @RequestParam(required = false) Integer status
    ) {
        IPage<CreditRedeemCode> result = redeemCodeService.page(new Page<>(pageNum, pageSize), keyword, status);
        return ApiResult.success(result);
    }

    @PostMapping("/use")
    @Operation(summary = "使用兑换码（当前用户）")
    @SaCheckLogin
    @OperationLog(moduleEnum = OperationModule.CREDIT, typeEnum = OperationType.UPDATE, description = "使用积分兑换码", async = true)
    public ApiResult<Boolean> redeem(@Parameter(description = "兑换码KEY") @NotBlank @RequestParam String codeKey) {
        boolean ok = redeemCodeService.redeem(codeKey);
        return ok ? ApiResult.success(true) : ApiResult.error("兑换失败");
    }

    @GetMapping("/info")
    @Operation(summary = "查询兑换码信息（预览）")
    @SaCheckLogin
    public ApiResult<CreditRedeemCode> info(@Parameter(description = "兑换码KEY") @NotBlank @RequestParam String codeKey) {
        CreditRedeemCode info = redeemCodeService.getInfo(codeKey);
        return ApiResult.success(info);
    }
}


