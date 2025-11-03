/**
 * 用户登录响应DTO：封装用户登录成功后的响应数据
 * 
 * 功能描述：
 * 1. 封装登录成功后返回的用户信息和Token
 * 2. 包含Sa-Token、用户基本信息和权限列表
 * 3. 不包含敏感信息如密码
 * 4. 用于前端认证和权限控制
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 00:55:00
 */
package com.okbug.platform.dto.auth.response;

import lombok.Data;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Data
@Schema(description = "用户登录响应")
public class UserLoginResponse {
    
    /**
     * 访问令牌（Sa-Token）
     */
    @Schema(description = "访问令牌", example = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9...")
    private String token;
    
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
     * 用户名
     */
    @Schema(description = "昵称", example = "张三")
    private String nickname;
    
    /**
     * 用户角色
     */
    @Schema(description = "用户角色", example = "USER")
    private String role;
    
    /**
     * 用户权限列表（完整权限对象，包含前端菜单生成所需的所有信息）
     */
    @Schema(description = "权限列表（完整权限对象）")
    private List<LoginPermissionInfo> permissions;

    /**
     * 当前团队ID（若用户加入多个团队，取默认或最近使用）
     */
    @Schema(description = "当前团队ID", example = "9876543210")
    private Long currentTeamId;

    /**
     * 当前团队名称
     */
    @Schema(description = "当前团队名称", example = "放射科团队")
    private String currentTeamName;
    
    /**
     * 当前团队是否被禁用（true 表示禁用，仅限制变更操作，不影响只读）
     */
    @Schema(description = "当前团队是否禁用", example = "true")
    private Boolean teamDisabled;
    
    // ================ 便利方法 ================
    
    /**
     * 创建登录响应对象
     */
    public static UserLoginResponse create(String token, Long userId, String username, String nickname, String role, List<LoginPermissionInfo> permissions) {
        UserLoginResponse response = new UserLoginResponse();
        response.setToken(token);
        response.setUserId(userId);
        response.setUsername(username);
        response.setNickname(nickname);
        response.setRole(role);
        response.setPermissions(permissions);
        return response;
    }

    /**
     * 设置当前团队便捷方法（链式）
     */
    public UserLoginResponse withCurrentTeam(Long teamId, String teamName) {
        this.currentTeamId = teamId;
        this.currentTeamName = teamName;
        return this;
    }
    
    /**
     * 设置团队禁用状态（链式）
     */
    public UserLoginResponse withTeamDisabled(Boolean disabled) {
        this.teamDisabled = disabled;
        return this;
    }
} 
