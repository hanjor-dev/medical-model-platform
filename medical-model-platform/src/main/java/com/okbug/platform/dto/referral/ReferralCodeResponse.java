/**
 * 引荐码信息响应DTO：引荐码相关的数据传输对象
 * 
 * 核心功能：
 * 1. 在Controller和Service层之间传输引荐码信息
 * 2. 包含用户的引荐码、引荐链接、分享描述等信息
 * 3. 提供Builder模式构建响应对象
 * 4. 支持前端引荐码展示和分享功能
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 15:30:00
 */
package com.okbug.platform.dto.referral;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 引荐码信息响应DTO
 * 用于返回用户的引荐码相关信息，包括引荐码、引荐链接、分享描述等
 */
@Data
@Builder
@Schema(name = "引荐码信息响应")
public class ReferralCodeResponse {
    
    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "123", title = "当前用户的唯一标识")
    private Long userId;
    
    /**
     * 引荐码
     */
    @Schema(description = "引荐码", example = "ABC12345", title = "用户的8位唯一引荐码")
    private String referralCode;
    
    /**
     * 引荐链接
     */
    @Schema(description = "引荐链接", example = "http://localhost:8081/register?refCode=ABC12345", title = "完整的引荐注册链接")
    private String referralLink;
    
    /**
     * 分享描述
     */
    @Schema(description = "分享描述", example = "分享给朋友，获得积分奖励", title = "用于分享时的描述文案")
    private String shareDescription;
    
    /**
     * 创建时间
     */
    @Schema(description = "创建时间", example = "2025-01-15T10:30:00", title = "引荐码的创建时间")
    private LocalDateTime createTime;
    
    /**
     * 便利方法：创建默认的分享描述
     * 
     * @return 默认分享描述
     */
    public String getDefaultShareDescription() {
        if (shareDescription != null && !shareDescription.trim().isEmpty()) {
            return shareDescription;
        }
        return "分享给朋友，获得积分奖励";
    }
    
    /**
     * 便利方法：检查引荐码是否有效
     * 
     * @return 引荐码是否有效
     */
    public boolean hasValidReferralCode() {
        return referralCode != null && !referralCode.trim().isEmpty();
    }
    
    /**
     * 便利方法：检查引荐链接是否有效
     * 
     * @return 引荐链接是否有效
     */
    public boolean hasValidReferralLink() {
        return referralLink != null && !referralLink.trim().isEmpty();
    }
} 
