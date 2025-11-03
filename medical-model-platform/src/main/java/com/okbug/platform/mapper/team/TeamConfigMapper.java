/**
 * 团队配置数据访问接口：team_configs 表的基础CRUD
 */
package com.okbug.platform.mapper.team;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.okbug.platform.entity.team.TeamConfig;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TeamConfigMapper extends BaseMapper<TeamConfig> {
}


