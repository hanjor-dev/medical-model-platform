/**
 * 团队成员数据访问接口：team_members 表的基础CRUD
 */
package com.okbug.platform.mapper.team;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.okbug.platform.entity.team.TeamMember;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TeamMemberMapper extends BaseMapper<TeamMember> {
}


