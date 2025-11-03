package com.okbug.platform.entity.system.message;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户通知渠道偏好：user_notify_channel_prefs
 */
@Data
@TableName("user_notify_channel_prefs")
public class UserNotifyChannelPref implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    private Long userId;

    /** 渠道编码: inbox/email/sms */
    private String channelCode;

    /** 是否开启该渠道(0/1) */
    private Integer enabled;

    /** 免打扰时段(HH:mm-HH:mm) */
    private String doNotDisturb;

    @TableLogic
    private Integer isDeleted;

    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}


