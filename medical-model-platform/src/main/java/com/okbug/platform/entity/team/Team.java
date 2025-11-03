/**
 * 团队实体：对应数据库表 teams
 *
 * 功能描述：
 * 1. 管理团队基础信息与归属
 * 2. 支持软删除与审计字段
 * 3. 预留状态字段支持禁用/启用
 *
 * 代码规范：
 * - 遵循《阿里巴巴Java开发规范》与项目统一风格
 * - 仅使用 MyBatis-Plus Java API，不使用注解SQL/XML
 * - 字段命名与数据库列采用下划线到驼峰的映射
 *
 * @author han
 * @version 1.0
 * @date 2025-09-30 10:00:00
 */
package com.okbug.platform.entity.team;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("teams")
public class Team {

    /**
     * 团队ID（主键）
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 团队名称
     */
    private String teamName;

    /**
     * 团队码（用于加入团队）
     */
    private String teamCode;

    /**
     * 团队描述
     */
    private String description;

    /**
     * 团队拥有者用户ID
     */
    private Long ownerUserId;

    /**
     * 状态(0:禁用 1:启用)
     */
    private Integer status;

    /**
     * 逻辑删除(0:正常 1:删除)
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
     * 创建人
     */
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.IGNORED)
    private Long createBy;

    /**
     * 更新人
     */
    @TableField(fill = FieldFill.INSERT_UPDATE, insertStrategy = FieldStrategy.IGNORED, updateStrategy = FieldStrategy.IGNORED)
    private Long updateBy;

    // ================ 常量 ================

    /** 禁用 */
    public static final int STATUS_DISABLED = 0;
    /** 启用 */
    public static final int STATUS_ENABLED = 1;
}


