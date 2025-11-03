/**
 * 团队数据访问接口：teams 表的基础CRUD
 *
 * 说明：
 * - 仅继承 MyBatis-Plus BaseMapper，禁止编写注解SQL/XML
 * - 复杂查询通过 Service 层使用 Wrapper 组合实现
 */
package com.okbug.platform.mapper.team;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.okbug.platform.entity.team.Team;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TeamMapper extends BaseMapper<Team> {
}


