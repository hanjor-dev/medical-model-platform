package com.okbug.platform.entity.system.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户通知类型偏好：user_notify_type_prefs
 */
@Data
@TableName("user_notify_type_prefs")
public class UserNotifyTypePref implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    /** 类型编码: system/task/credit/marketing 等 */
    private String typeCode;

    /** 是否开启该类型(0/1) */
    private Integer enabled;

    @TableLogic
    private Integer isDeleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


