/**
 * 团队加入申请分页查询请求
 */
package com.okbug.platform.dto.team;

import lombok.Data;

@Data
public class TeamJoinRequestPageRequest {
    /** 页码，从1开始 */
    private Integer pageNum;
    /** 每页大小，默认10 */
    private Integer pageSize;
    /** 过滤：状态(0待审/1通过/2拒绝) */
    private Integer status;
    /** 过滤：申请人用户ID */
    private Long userId;
}
