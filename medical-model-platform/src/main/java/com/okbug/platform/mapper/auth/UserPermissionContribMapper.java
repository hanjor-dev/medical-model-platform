package com.okbug.platform.mapper.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.okbug.platform.entity.auth.UserPermissionContrib;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserPermissionContribMapper extends BaseMapper<UserPermissionContrib> {

    /**
     * 物理删除指定角色对这些用户的贡献行
     */
    @Delete("DELETE FROM user_permission_contrib WHERE source_type = 'ROLE' AND source_id = #{role} AND user_id = #{userId}")
    int deleteRoleContributionForUser(@Param("role") String role, @Param("userId") Long userId);
}


