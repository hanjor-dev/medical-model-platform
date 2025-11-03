package com.okbug.platform.mapper.system.message;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.okbug.platform.entity.system.message.Announcement;
import org.apache.ibatis.annotations.Mapper;

/**
 * 公告表 Mapper 接口：系统/消息模块
 *
 * 说明：
 * - 禁止使用注解 SQL，使用 MyBatis-Plus 提供的通用方法
 */
@Mapper
public interface AnnouncementMapper extends BaseMapper<Announcement> {
}


