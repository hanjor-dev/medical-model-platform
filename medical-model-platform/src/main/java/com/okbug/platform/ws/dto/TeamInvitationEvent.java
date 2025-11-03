package com.okbug.platform.ws.dto;

/**
 * WebSocket 事件：团队邀请变更
 */
public class TeamInvitationEvent {
    private Long teamId;
    private String token;
    private String action; // created/revoked/accepted
    private Long actorUserId;

    public TeamInvitationEvent() {}

    public TeamInvitationEvent(Long teamId, String token, String action, Long actorUserId) {
        this.teamId = teamId;
        this.token = token;
        this.action = action;
        this.actorUserId = actorUserId;
    }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public Long getActorUserId() { return actorUserId; }
    public void setActorUserId(Long actorUserId) { this.actorUserId = actorUserId; }
}


