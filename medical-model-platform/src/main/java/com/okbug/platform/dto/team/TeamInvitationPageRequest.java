/**
 * 团队邀请分页查询请求
 */
package com.okbug.platform.dto.team;

import lombok.Data;

@Data
public class TeamInvitationPageRequest {
    /** 页码，从1开始 */
    private Integer pageNum;
    /** 每页大小，默认10 */
    private Integer pageSize;
    /** 过滤：状态(PENDING/ACCEPTED/REJECTED/EXPIRED 对应 0/1/2/3) */
    private Integer status;
    /** 过滤：被邀请人（用户ID） */
    private Long invitedUserId;
}
