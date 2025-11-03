/**
 * 团队邀请数据访问接口：team_invitations 表的基础CRUD
 */
package com.okbug.platform.mapper.team;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.okbug.platform.entity.team.TeamInvitation;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TeamInvitationMapper extends BaseMapper<TeamInvitation> {
}


