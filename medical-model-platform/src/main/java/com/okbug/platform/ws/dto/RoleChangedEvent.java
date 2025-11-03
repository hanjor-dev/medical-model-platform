package com.okbug.platform.ws.dto;

/**
 * WebSocket 事件：角色发生变更
 */
public class RoleChangedEvent {
    public final String event = "ROLE_CHANGED";
    public String role;

    public RoleChangedEvent() {
    }

    public RoleChangedEvent(String role) {
        this.role = role;
    }
}


