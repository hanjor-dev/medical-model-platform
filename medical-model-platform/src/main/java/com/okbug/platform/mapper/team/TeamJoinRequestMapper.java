/**
 * 团队加入申请数据访问接口：team_join_requests 表的基础CRUD
 */
package com.okbug.platform.mapper.team;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.okbug.platform.entity.team.TeamJoinRequest;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TeamJoinRequestMapper extends BaseMapper<TeamJoinRequest> {
}


