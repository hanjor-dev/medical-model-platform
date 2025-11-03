/**
 * 用户实体类：对应数据库users表
 * 
 * 功能描述：
 * 1. 用户基础信息存储
 * 2. 支持推荐关系和层级管理
 * 3. 登录状态和安全信息管理
 * 4. 软删除和审计字段支持
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-14 23:55:00
 */
package com.okbug.platform.entity.auth;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("users")
public class User {
    
    /**
     * 用户ID（主键）
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;
    
    /**
     * 用户名（唯一，注册后不可修改）
     */
    private String username;
    
    /**
     * 邮箱（唯一，可选）
     */
    private String email;
    
    /**
     * 手机号（可选）
     */
    private String phone;
    
    /**
     * 密码（BCrypt加密）
     */
    private String password;
    
    /**
     * 昵称（可修改）
     */
    private String nickname;
    
    /**
     * 头像URL
     */
    private String avatar;
    
    /**
     * 账户状态（0:禁用 1:启用）
     */
    private Integer status;
    
    /**
     * 用户角色（USER:普通用户 ADMIN:管理员 SUPER_ADMIN:超级管理员）
     */
    private String role;
    
    /**
     * 上级用户ID（ADMIN管理的子用户）
     */
    private Long parentUserId;
    
    /**
     * 个人推荐码（8位唯一码）
     */
    private String referralCode;
    
    /**
     * 推荐人用户ID
     */
    private Long referrerUserId;
    
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    
    /**
     * 最后登录IP
     */
    private String lastLoginIp;
    
    /**
     * 登录失败次数（用于账户锁定）
     */
    private Integer loginFailCount;
    
    /**
     * 登录锁定时间（超过此时间自动解锁）
     */
    private LocalDateTime loginLockTime;
    
    /**
     * 逻辑删除标记（0:正常 1:删除）
     */
    @TableLogic
    private Integer isDeleted;
    
    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
    
    /**
     * 创建人（可为空，用户注册时无登录用户）
     */
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.IGNORED)
    private Long createBy;
    
    /**
     * 更新人（可为空）
     */
    @TableField(fill = FieldFill.INSERT_UPDATE, insertStrategy = FieldStrategy.IGNORED, updateStrategy = FieldStrategy.IGNORED)
    private Long updateBy;
    
    // ================ 常量定义 ================
    
    /**
     * 用户状态：禁用
     */
    public static final int STATUS_DISABLED = 0;
    
    /**
     * 用户状态：启用
     */
    public static final int STATUS_ENABLED = 1;
    
    /**
     * 角色：普通用户
     */
    public static final String ROLE_USER = "USER";
    
    /**
     * 角色：管理员
     */
    public static final String ROLE_ADMIN = "ADMIN";
    
    /**
     * 角色：超级管理员
     */
    public static final String ROLE_SUPER_ADMIN = "SUPER_ADMIN";
    
    // ================ 便利方法 ================
    
    /**
     * 判断用户是否已启用
     */
    public boolean isEnabled() {
        return STATUS_ENABLED == this.status;
    }
    
    /**
     * 判断用户是否被禁用
     */
    public boolean isDisabled() {
        return STATUS_DISABLED == this.status;
    }
    
    /**
     * 判断是否为普通用户
     */
    public boolean isUser() {
        return ROLE_USER.equals(this.role);
    }
    
    /**
     * 判断是否为管理员
     */
    public boolean isAdmin() {
        return ROLE_ADMIN.equals(this.role);
    }
    
    /**
     * 判断是否为超级管理员
     */
    public boolean isSuperAdmin() {
        return ROLE_SUPER_ADMIN.equals(this.role);
    }
    
    /**
     * 判断是否有管理权限（ADMIN或SUPER_ADMIN）
     */
    public boolean hasAdminRole() {
        return isAdmin() || isSuperAdmin();
    }
    
    /**
     * 判断账户是否被锁定
     */
    public boolean isLocked() {
        return loginLockTime != null && loginLockTime.isAfter(LocalDateTime.now());
    }
    
    /**
     * 重置登录失败次数
     */
    public void resetLoginFailCount() {
        this.loginFailCount = 0;
        this.loginLockTime = null;
    }
    
    /**
     * 增加登录失败次数
     */
    public void increaseLoginFailCount() {
        if (this.loginFailCount == null) {
            this.loginFailCount = 0;
        }
        this.loginFailCount++;
    }
    
    /**
     * 设置登录锁定时间
     * 
     * @param lockDurationMinutes 锁定时长（分钟）
     */
    public void setLoginLock(int lockDurationMinutes) {
        this.loginLockTime = LocalDateTime.now().plusMinutes(lockDurationMinutes);
    }
    
    /**
     * 更新最后登录信息
     * 
     * @param loginIp 登录IP
     */
    public void updateLastLogin(String loginIp) {
        this.lastLoginTime = LocalDateTime.now();
        this.lastLoginIp = loginIp;
        this.resetLoginFailCount();
    }
} 