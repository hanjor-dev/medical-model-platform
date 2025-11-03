package com.okbug.platform.entity.system.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 推送任务：系统/消息模块 - 异步执行
 */
@Data
@TableName("notify_push_tasks")
public class PushTask implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 关联消息ID */
    private Long messageId;

    /** 计划执行时间 */
    private LocalDateTime scheduledTime;

    /** 下次重试时间（可选） */
    private LocalDateTime nextRetryTime;

    /** 尝试次数 */
    private Integer attemptCount;

    /** 最大尝试次数 */
    private Integer maxAttempts;

    /** 任务状态：0=待执行 1=执行中 2=成功 3=失败 4=已取消 */
    private Integer status;

    /** 最后一次错误信息 */
    private String lastError;

    @TableLogic
    private Integer isDeleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long createBy;
    private Long updateBy;
}


