/**
 * 团队成员分页查询请求
 */
package com.okbug.platform.dto.team;

import lombok.Data;

@Data
public class TeamMemberPageRequest {
    /** 页码，从1开始 */
    private Integer pageNum;
    /** 每页大小，默认10 */
    private Integer pageSize;
    /** 过滤：角色(OWNER/ADMIN/MEMBER) */
    private String role;
    /** 过滤：状态(0/1) */
    private Integer status;
}
