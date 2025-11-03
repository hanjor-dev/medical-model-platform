package com.okbug.platform.dto.referral;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 引荐用户信息响应DTO
 * 
 * 用于返回通过引荐码注册的用户信息，包含用户基本信息、注册时间、贡献积分等
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 16:30:00
 */
@Data
@Builder
@Schema(name = "引荐用户信息响应")
public class ReferralUserResponse {
    
    @Schema(description = "用户ID", example = "123", title = "用户的唯一标识")
    private Long userId;
    
    @Schema(description = "用户昵称", example = "张三", title = "用户的显示昵称")
    private String nickname;
    
    @Schema(description = "用户名", example = "zhangsan", title = "用户的登录用户名")
    private String username;
    
    @Schema(description = "邮箱地址", example = "zhangsan@example.com", title = "用户的邮箱地址")
    private String email;
    
    @Schema(description = "用户头像", example = "https://example.com/avatar.jpg", title = "用户头像URL地址")
    private String avatar;
    
    @Schema(description = "注册时间", example = "2025-01-15T10:30:00", title = "用户通过引荐码注册的时间")
    private LocalDateTime registerTime;
    
    @Schema(description = "贡献积分", example = "100", title = "该用户为引荐人贡献的积分数量")
    private Integer contributionCredits;
    
    @Schema(description = "引荐来源", example = "微信分享", title = "用户通过哪种方式获得引荐码")
    private String source;
    
    @Schema(description = "最后登录时间", example = "2025-01-20T15:45:00", title = "用户最后一次登录的时间，可能为空（表示从未登录）")
    private LocalDateTime lastLoginTime;
    
    @Schema(description = "引荐时间", example = "2025-01-15T10:30:00", title = "用户被引荐的时间")
    private LocalDateTime referralTime;
    
    // ================ 便捷方法 ================
    
    /**
     * 创建成功的引荐用户响应
     */
    public static ReferralUserResponse createSuccess(Long userId, String nickname, String username, String email, 
                                                   String avatar, LocalDateTime registerTime, Integer contributionCredits, 
                                                   String source, LocalDateTime lastLoginTime,
                                                   LocalDateTime referralTime) {
        return ReferralUserResponse.builder()
                .userId(userId)
                .nickname(nickname)
                .username(username)
                .email(email)
                .avatar(avatar)
                .registerTime(registerTime)
                .contributionCredits(contributionCredits)
                .source(source)
                .lastLoginTime(lastLoginTime)
                .referralTime(referralTime)
                .build();
    }
    
    /**
     * 创建基础引荐用户响应
     */
    public static ReferralUserResponse createBasic(Long userId, String nickname, String username, String email, LocalDateTime registerTime) {
        return ReferralUserResponse.builder()
                .userId(userId)
                .nickname(nickname)
                .username(username)
                .email(email)
                .registerTime(registerTime)
                .contributionCredits(0)
                .source("直接引荐")
                .lastLoginTime(null) // 基础响应中最后登录时间为空
                .referralTime(registerTime)
                .build();
    }
} 
