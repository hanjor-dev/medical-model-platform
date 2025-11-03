package com.okbug.platform.ws.dto;

/**
 * WebSocket 事件：团队加入申请变更
 */
public class TeamJoinRequestEvent {
    private Long teamId;
    private Long requestId;
    private String action; // submitted/approved/rejected
    private Long actorUserId;

    public TeamJoinRequestEvent() {}

    public TeamJoinRequestEvent(Long teamId, Long requestId, String action, Long actorUserId) {
        this.teamId = teamId;
        this.requestId = requestId;
        this.action = action;
        this.actorUserId = actorUserId;
    }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public Long getRequestId() { return requestId; }
    public void setRequestId(Long requestId) { this.requestId = requestId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Long getActorUserId() { return actorUserId; }
    public void setActorUserId(Long actorUserId) { this.actorUserId = actorUserId; }
}


