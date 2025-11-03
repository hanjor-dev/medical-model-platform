package com.okbug.platform.entity.system.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 消息实体：系统/消息模块 - 用户定向消息
 */
@Data
@TableName("notify_messages")
public class Message implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /** 接收用户ID */
    private Long userId;

    /** 消息类型编码（system/task/credit/marketing） */
    private String messageType;

    /** 消息标题（可选，不传则用模板名称） */
    private String title;

    /** 消息正文内容（直接存储） */
    private String content;

    /** 计划推送时间（可选，null 表示立即） */
    private LocalDateTime scheduleTime;

    /** 消息状态：0=待调度 1=已入队 2=已完成 3=已取消 4=失败 */
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


