/**
 * Sa-Token StpInterface实现：为Sa-Token提供用户的角色与权限列表
 *
 * 实现策略：
 * - 权限：返回用户生效的权限编码列表（仅来源于用户-权限 ALLOW；超级管理员拥有全部启用权限）
 * - 角色：返回用户主角色（若用户不存在则返回空列表）
 */
package com.okbug.platform.config;

import cn.dev33.satoken.stp.StpInterface;
import com.okbug.platform.entity.auth.User;
import com.okbug.platform.mapper.auth.UserMapper;
import com.okbug.platform.service.permission.PermissionService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StpInterfaceImpl implements StpInterface {

    private final PermissionService permissionService;
    private final UserMapper userMapper;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        try {
            Long userId = toLong(loginId);
            if (userId == null) {
                return Collections.emptyList();
            }
            List<String> codes = permissionService.getEffectivePermissionCodes(userId);
            return codes != null ? codes : Collections.emptyList();
        } catch (Exception e) {
            log.warn("获取用户权限列表失败, loginId: {}", loginId, e);
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        try {
            Long userId = toLong(loginId);
            if (userId == null) {
                return Collections.emptyList();
            }
            User user = userMapper.selectById(userId);
            if (user == null || user.getRole() == null) {
                return Collections.emptyList();
            }
            List<String> roles = new ArrayList<>(1);
            roles.add(user.getRole());
            return roles;
        } catch (Exception e) {
            log.warn("获取用户角色列表失败, loginId: {}", loginId, e);
            return Collections.emptyList();
        }
    }

    private Long toLong(Object loginId) {
        if (loginId == null) {
            return null;
        }
        if (loginId instanceof Long) {
            return (Long) loginId;
        }
        try {
            return Long.parseLong(String.valueOf(loginId));
        } catch (Exception e) {
            return null;
        }
    }
}


