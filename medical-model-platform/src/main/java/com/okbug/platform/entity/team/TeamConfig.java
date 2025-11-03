/**
 * 团队配置实体：对应数据库表 team_configs
 *
 * 功能描述：
 * 1. 存储团队级配置项（如积分结算模式等）
 * 2. 支持键唯一性与软删除
 * 3. 具备基础审计字段
 */
package com.okbug.platform.entity.team;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = false)
@TableName("team_configs")
public class TeamConfig {

    /** 主键ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 团队ID */
    private Long teamId;

    /** 配置键 */
    private String configKey;

    /** 配置值（字符串或JSON字符串） */
    private String configValue;

    /** 配置类型(STRING/NUMBER/BOOLEAN/JSON) */
    private String configType;

    /** 配置描述 */
    private String description;

    /** 逻辑删除(0:正常 1:删除) */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    /** 更新时间 */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    /** 创建人 */
    @TableField(fill = FieldFill.INSERT, insertStrategy = FieldStrategy.IGNORED)
    private Long createBy;

    /** 更新人 */
    @TableField(fill = FieldFill.INSERT_UPDATE, insertStrategy = FieldStrategy.IGNORED, updateStrategy = FieldStrategy.IGNORED)
    private Long updateBy;
}


