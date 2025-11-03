/**
 * 公告阅读明细实体：系统/消息模块 - 用户公告阅读记录
 *
 * 设计说明：
 * - 对应表：announcement_reads
 * - 唯一约束：(announcement_id, user_id)
 * - 记录用户对公告的已读状态与时间
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
@TableName("announcement_reads")
public class AnnouncementRead implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 公告ID */
    private Long announcementId;

    /** 用户ID */
    private Long userId;

    /** 阅读时间 */
    private LocalDateTime readAt;

    /** 逻辑删除标记(0:正常 1:删除) */
    @TableLogic
    private Integer isDeleted;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
}


