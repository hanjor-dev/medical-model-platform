/**
 * 团队成员控制器：团队成员相关API（占位，无具体接口）
 */
package com.okbug.platform.controller.team;

import com.okbug.platform.service.team.TeamMemberService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/team/member")
@RequiredArgsConstructor
@Tag(name = "团队成员接口")
public class TeamMemberController {

    private final TeamMemberService teamMemberService;
}


