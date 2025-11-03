/**
 * 用户管理服务接口：定义用户管理相关的业务操作
 * 
 * 功能描述：
 * 1. 用户个人信息管理
 * 2. 密码修改功能
 * 3. 子账号管理（ADMIN权限）
 * 4. 用户查询和更新操作
 * 
 * @author hanjor
 * @version 1.0
 * @date 2025-01-15 01:15:00
 */
package com.okbug.platform.service.user;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.okbug.platform.dto.auth.request.UserRegisterRequest;
import com.okbug.platform.dto.auth.response.UserRegisterResponse;
import com.okbug.platform.dto.user.request.ChangePasswordRequest;
import com.okbug.platform.dto.user.request.UserProfileUpdateRequest;
import com.okbug.platform.entity.auth.User;

public interface UserService {
    
    /**
     * 获取当前用户信息
     */
    User getCurrentUserProfile();
    
    /**
     * 修改当前用户信息
     */
    User updateCurrentUserProfile(UserProfileUpdateRequest request);
    
    /**
     * 修改当前用户密码
     */
    void changePassword(ChangePasswordRequest request);
    
    /**
     * 获取子账号列表（ADMIN权限）
     */
    IPage<User> getSubUsers(Page<User> page, Long teamId);
    
    /**
     * 创建子账号（ADMIN权限）
     */
    UserRegisterResponse createSubUser(UserRegisterRequest request);

    /**
     * 管理员更新子用户信息
     */
    User updateSubUser(Long userId, UserProfileUpdateRequest request);

    /**
     * 管理员删除子用户
     */
    void deleteSubUser(Long userId);

    /**
     * 管理员切换子用户状态（启用/禁用）
     */
    void toggleSubUserStatus(Long userId, Integer status);

    /**
     * 管理员重置子用户密码（使用系统默认密码）
     * 
     * @param userId 目标用户ID
     * @return 重置后的明文默认密码（用于前端提示管理员）
     */
    String resetSubUserPassword(Long userId);
} 