/**
 * 公告实体：系统/消息模块 - 公告主表实体
 *
 * 设计说明：
 * - 对应表：announcements
 * - 使用 MyBatis-Plus 进行 ORM 映射
 * - 采用逻辑删除标记 is_deleted（0:正常 1:删除）
 * - 时间字段使用 LocalDateTime，遵循后端全局 Jackson 配置
 *
 * 关键字段：
 * - status: 0=草稿 1=已发布 2=已下线
 * - forceRead: 是否强制阅读（1 表示前端需拦截处理）
 * - activeFrom/activeTo: 生效/失效时间窗，用于筛选可见公告
 */
package com.okbug.platform.entity.system.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("announcements")
public class Announcement implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 公告ID（主键） */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 标题（最大200字符） */
    private String title;

    /** 富文本内容（已在服务层进行安全过滤） */
    private String content;

    /** 优先级（越大越靠前） */
    private Integer priority;

    /** 是否强制阅读(0:否 1:是) */
    private Integer forceRead;

    /** 生效时间（可空） */
    private LocalDateTime activeFrom;

    /** 失效时间（可空） */
    private LocalDateTime activeTo;

    /** 定时发布时间（可空） */
    private LocalDateTime scheduleTime;

    /** 是否已进行到点首次推送(0:否 1:是) */
    private Integer notified;

    /** 首次到点推送时间 */
    private LocalDateTime firstPushTime;

    /** 状态(0:草稿 1:已发布 2:已下线) */
    private Integer status;

    /** 逻辑删除标记(0:正常 1:删除) */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;

    /** 创建人 */
    private Long createBy;

    /** 更新人 */
    private Long updateBy;
}


