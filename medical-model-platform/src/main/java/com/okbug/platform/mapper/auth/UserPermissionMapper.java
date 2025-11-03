/**
 * 用户权限覆盖数据访问接口：提供用户级权限覆盖的数据库操作
 */
package com.okbug.platform.mapper.auth;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.okbug.platform.entity.auth.UserPermission;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface UserPermissionMapper extends BaseMapper<UserPermission> {
    
    /**
     * 物理删除指定用户的所有权限覆盖记录
     */
    @Delete("DELETE FROM user_permissions WHERE user_id = #{userId}")
    int deleteByUserIdPhysical(@Param("userId") Long userId);

    /**
     * 物理删除指定用户的“额外权限”记录：保留其角色权限对应的行（如存在）
     * 当 rolePermissionIds 为空或为 null 时，将删除该用户的全部 user_permissions 记录。
     */
    @Delete({
        "<script>",
        "DELETE FROM user_permissions",
        " WHERE user_id = #{userId}",
        " <if test='rolePermissionIds != null and rolePermissionIds.size() > 0'>",
        "   AND permission_id NOT IN",
        "   <foreach collection='rolePermissionIds' item='pid' open='(' separator=',' close=')'>",
        "     #{pid}",
        "   </foreach>",
        " </if>",
        "</script>"
    })
    int deleteExtrasPreservingRole(@Param("userId") Long userId, @Param("rolePermissionIds") List<Long> rolePermissionIds);

    /**
     * 物理删除：按用户与权限ID列表删除 user_permissions 行
     */
    @Delete({
        "<script>",
        "DELETE FROM user_permissions",
        " WHERE user_id = #{userId}",
        "   AND permission_id IN",
        "   <foreach collection='permissionIds' item='pid' open='(' separator=',' close=')'>",
        "     #{pid}",
        "   </foreach>",
        "</script>"
    })
    int deleteByUserAndPermissionIdsPhysical(@Param("userId") Long userId, @Param("permissionIds") List<Long> permissionIds);
}


