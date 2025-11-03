/**
 * 引荐码验证结果DTO：引荐码验证相关的数据传输对象
 * 
 * 核心功能：
 * 1. 在Service和Controller层之间传输引荐码验证结果
 * 2. 包含验证状态、消息和详细信息
 * 3. 提供Builder模式构建结果对象
 * 4. 支持前端验证结果展示
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 16:00:00
 */
package com.okbug.platform.dto.referral;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 引荐码验证结果DTO
 * 用于返回引荐码验证的结果信息，包括验证状态、消息等
 */
@Data
@Builder
@Schema(name = "引荐码验证结果")
public class ReferralValidationResult {
    
    /**
     * 验证是否有效
     */
    @Schema(description = "验证是否有效", example = "true", title = "true表示有效，false表示无效")
    private Boolean isValid;
    
    /**
     * 验证结果消息
     */
    @Schema(description = "验证结果消息", example = "引荐码有效", title = "验证结果的描述信息")
    private String message;
    
    /**
     * 引荐码
     */
    @Schema(description = "引荐码", example = "ABC12345", title = "被验证的引荐码")
    private String referralCode;
    
    /**
     * 验证时间
     */
    @Schema(description = "验证时间", example = "2025-01-15T16:00:00", title = "验证执行的时间")
    private String validationTime;
    
    /**
     * 便利方法：创建有效验证结果
     * 
     * @param referralCode 引荐码
     * @return 有效验证结果
     */
    public static ReferralValidationResult createValidResult(String referralCode) {
        return ReferralValidationResult.builder()
                .isValid(true)
                .message("引荐码有效")
                .referralCode(referralCode)
                .validationTime(java.time.LocalDateTime.now().toString())
                .build();
    }
    
    /**
     * 便利方法：创建无效验证结果
     * 
     * @param referralCode 引荐码
     * @param message 无效原因消息
     * @return 无效验证结果
     */
    public static ReferralValidationResult createInvalidResult(String referralCode, String message) {
        return ReferralValidationResult.builder()
                .isValid(false)
                .message(message)
                .referralCode(referralCode)
                .validationTime(java.time.LocalDateTime.now().toString())
                .build();
    }
} 
