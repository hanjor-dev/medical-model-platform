package com.okbug.platform.ws;

/**
 * WebSocket 主题常量集中管理
 */
public final class WebSocketTopics {
    private WebSocketTopics() {}

    public static final String TOPIC_ROLE_CHANGED = "/topic/permission-role-changed";
    public static final String TOPIC_USER_CHANGED = "/topic/permission-user-changed";
    public static final String TOPIC_USER_MESSAGE = "/topic/user-message";
    public static final String TOPIC_USER_ANNOUNCEMENT = "/topic/user-announcement";
    // Team domain topics
    public static final String TOPIC_TEAM_MEMBER_CHANGED = "/topic/team-member-changed";
    public static final String TOPIC_TEAM_INVITATION_CHANGED = "/topic/team-invitation-changed";
    public static final String TOPIC_TEAM_JOIN_REQUEST_CHANGED = "/topic/team-join-request-changed";
}


