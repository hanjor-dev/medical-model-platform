package com.okbug.platform.dto.credit.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;
import java.math.BigDecimal;

/**
 * 用户 + 积分账户汇总响应
 */
@Data
@Schema(description = "用户积分汇总响应")
public class UserCreditsSummaryResponse {

    @Schema(description = "用户ID", example = "1")
    private Long userId;

    @Schema(description = "用户名", example = "alice")
    private String username;

    @Schema(description = "昵称", example = "Alice")
    private String nickname;

    @Schema(description = "角色", example = "ADMIN")
    private String role;

    @Schema(description = "积分账户列表")
    private List<CreditBalanceResponse.CreditAccountInfo> accounts;

    @Schema(description = "总消耗积分（所有类型累计）", example = "1234.56")
    private BigDecimal totalConsumed;
}


