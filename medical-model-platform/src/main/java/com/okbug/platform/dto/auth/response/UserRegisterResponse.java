/**
 * 用户注册响应DTO：封装用户注册成功后的响应数据
 * 
 * 功能描述：
 * 1. 封装注册成功后返回的用户信息
 * 2. 包含用户ID、用户名和推荐码
 * 3. 不包含敏感信息如密码
 * 4. 用于前端展示和后续操作
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 00:50:00
 */
package com.okbug.platform.dto.auth.response;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

@Data
@Schema(description = "用户注册响应")
public class UserRegisterResponse {
    
    /**
     * 用户ID
     */
    @Schema(description = "用户ID", example = "1234567890")
    private Long userId;
    
    /**
     * 用户名
     */
    @Schema(description = "用户名", example = "zhang123")
    private String username;
    
    /**
     * 用户推荐码
     */
    @Schema(description = "推荐码", example = "ABC12345")
    private String referralCode;
    
    // ================ 便利方法 ================
    
    /**
     * 创建注册响应对象
     */
    public static UserRegisterResponse create(Long userId, String username, String referralCode) {
        UserRegisterResponse response = new UserRegisterResponse();
        response.setUserId(userId);
        response.setUsername(username);
        response.setReferralCode(referralCode);
        return response;
    }
} 
