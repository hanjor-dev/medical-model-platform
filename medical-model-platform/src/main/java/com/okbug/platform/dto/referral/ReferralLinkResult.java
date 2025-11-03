/**
 * 引荐链接生成结果DTO：引荐链接生成相关的数据传输对象
 * 
 * 核心功能：
 * 1. 在Service和Controller层之间传输引荐链接生成结果
 * 2. 包含生成状态、链接和详细信息
 * 3. 提供Builder模式构建结果对象
 * 4. 支持前端链接生成结果展示
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 16:05:00
 */
package com.okbug.platform.dto.referral;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

/**
 * 引荐链接生成结果DTO
 * 用于返回引荐链接生成的结果信息，包括生成状态、链接等
 */
@Data
@Builder
@Schema(name = "引荐链接生成结果")
public class ReferralLinkResult {
    
    /**
     * 生成是否成功
     */
    @Schema(description = "生成是否成功", example = "true", title = "true表示成功，false表示失败")
    private Boolean isSuccess;
    
    /**
     * 结果消息
     */
    @Schema(description = "结果消息", example = "生成引荐链接成功", title = "生成结果的描述信息")
    private String message;
    
    /**
     * 引荐码
     */
    @Schema(description = "引荐码", example = "ABC12345", title = "用于生成链接的引荐码")
    private String referralCode;
    
    /**
     * 生成的引荐链接
     */
    @Schema(description = "生成的引荐链接", example = "http://localhost:8081/register?refCode=ABC12345", title = "完整的引荐注册链接")
    private String referralLink;
    
    /**
     * 生成时间
     */
    @Schema(description = "生成时间", example = "2025-01-15T16:05:00", title = "链接生成的时间")
    private String generationTime;
    
    /**
     * 便利方法：创建成功生成结果
     * 
     * @param referralCode 引荐码
     * @param referralLink 生成的链接
     * @return 成功生成结果
     */
    public static ReferralLinkResult createSuccessResult(String referralCode, String referralLink) {
        return ReferralLinkResult.builder()
                .isSuccess(true)
                .message("生成引荐链接成功")
                .referralCode(referralCode)
                .referralLink(referralLink)
                .generationTime(java.time.LocalDateTime.now().toString())
                .build();
    }
    
    /**
     * 便利方法：创建失败生成结果
     * 
     * @param referralCode 引荐码
     * @param message 失败原因消息
     * @return 失败生成结果
     */
    public static ReferralLinkResult createFailureResult(String referralCode, String message) {
        return ReferralLinkResult.builder()
                .isSuccess(false)
                .message(message)
                .referralCode(referralCode)
                .referralLink(null)
                .generationTime(java.time.LocalDateTime.now().toString())
                .build();
    }
} 
