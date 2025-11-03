package com.okbug.platform.ws.dto;

/**
 * WebSocket 事件：团队成员变更
 */
public class TeamMemberChangedEvent {
    private Long teamId;
    private Long userId;
    private String action; // add/update/remove
    private String role;   // current role if applicable

    public TeamMemberChangedEvent() {}

    public TeamMemberChangedEvent(Long teamId, Long userId, String action, String role) {
        this.teamId = teamId;
        this.userId = userId;
        this.action = action;
        this.role = role;
    }

    public Long getTeamId() { return teamId; }
    public void setTeamId(Long teamId) { this.teamId = teamId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}


