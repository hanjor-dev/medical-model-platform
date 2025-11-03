package com.okbug.platform.ws;

import cn.hutool.log.Log;
import cn.hutool.log.LogFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.okbug.platform.ws.dto.NewAnnouncementEvent;
import com.okbug.platform.ws.dto.NewMessageEvent;
import com.okbug.platform.ws.dto.RoleChangedEvent;
import com.okbug.platform.ws.dto.TeamInvitationEvent;
import com.okbug.platform.ws.dto.TeamJoinRequestEvent;
import com.okbug.platform.ws.dto.TeamMemberChangedEvent;
import com.okbug.platform.ws.dto.UserAnnouncementEvent;
import com.okbug.platform.ws.dto.UserChangedEvent;
import com.okbug.platform.ws.dto.UserMessageEvent;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * 通用实时 WebSocket 处理器
 * 管理多个 topic 的会话集合并提供通用推送能力
 */
@Component
public class RealtimeWebSocketHandler extends TextWebSocketHandler {
    private static final Log logger = LogFactory.get();

    // 维护 topic -> sessions 的映射
    private static final Map<String, Set<WebSocketSession>> topicSessions = new ConcurrentHashMap<>();
    // 维护 userId -> sessions 的映射（用于定向推送）
    private static final Map<String, Set<WebSocketSession>> userSessions = new ConcurrentHashMap<>();

    private final ObjectMapper objectMapper;
    public RealtimeWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private static Set<WebSocketSession> getTopicSet(String topic) {
        return topicSessions.computeIfAbsent(topic, k -> new CopyOnWriteArraySet<>());
    }

    private static Set<WebSocketSession> getUserSet(String userId) {
        return userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArraySet<>());
    }

    private static String getQueryParam(java.net.URI uri, String key) {
        if (uri == null) return null;
        String query = uri.getQuery();
        if (query == null || query.isEmpty()) return null;
        for (String part : query.split("&")) {
            int idx = part.indexOf('=');
            if (idx > 0) {
                String k = part.substring(0, idx);
                if (key.equals(k)) return part.substring(idx + 1);
            } else if (key.equals(part)) {
                return "";
            }
        }
        return null;
    }

    @Override
    public void afterConnectionEstablished(@NonNull WebSocketSession session) {
        java.net.URI uri = session.getUri();
        String path = uri != null ? uri.getPath() : "";
        // 解析 userId（如果客户端在连接时拼接了 ?userId=xxx）
        String userId = getQueryParam(uri, "userId");
        if (path.endsWith(WebSocketTopics.TOPIC_ROLE_CHANGED)) {
            getTopicSet(WebSocketTopics.TOPIC_ROLE_CHANGED).add(session);
            logger.info("WS 连接: topic=ROLE_CHANGED, session={}", session.getId());
        } else if (path.endsWith(WebSocketTopics.TOPIC_USER_CHANGED)) {
            getTopicSet(WebSocketTopics.TOPIC_USER_CHANGED).add(session);
            logger.info("WS 连接: topic=USER_CHANGED, session={}", session.getId());
        } else if (path.endsWith(WebSocketTopics.TOPIC_USER_MESSAGE)) {
            // 用户消息必须携带 userId，拒绝无 userId 的连接
            if (userId != null && !userId.isEmpty()) {
                getUserSet(userId).add(session);
                logger.info("WS 连接: USER_MESSAGE, userId={}, session={}", userId, session.getId());
            } else {
                logger.warn("WS 拒绝连接: USER_MESSAGE 需要 userId, session={}", session.getId());
                try { session.close(new CloseStatus(1008, "userId required")); } catch (Exception ignore) {}
            }
        } else if (path.endsWith(WebSocketTopics.TOPIC_USER_ANNOUNCEMENT)) {
            // 公告允许全局主题连接，同时可选地将带有 userId 的会话加入 user 映射
            getTopicSet(WebSocketTopics.TOPIC_USER_ANNOUNCEMENT).add(session);
            logger.info("WS 连接: topic=USER_ANNOUNCEMENT, session={}", session.getId());
            if (userId != null && !userId.isEmpty()) {
                getUserSet(userId).add(session);
            }
        } else if (path.endsWith(WebSocketTopics.TOPIC_TEAM_MEMBER_CHANGED)) {
            getTopicSet(WebSocketTopics.TOPIC_TEAM_MEMBER_CHANGED).add(session);
            logger.info("WS 连接: topic=TEAM_MEMBER_CHANGED, session={}", session.getId());
        } else if (path.endsWith(WebSocketTopics.TOPIC_TEAM_INVITATION_CHANGED)) {
            getTopicSet(WebSocketTopics.TOPIC_TEAM_INVITATION_CHANGED).add(session);
            logger.info("WS 连接: topic=TEAM_INVITATION_CHANGED, session={}", session.getId());
        } else if (path.endsWith(WebSocketTopics.TOPIC_TEAM_JOIN_REQUEST_CHANGED)) {
            getTopicSet(WebSocketTopics.TOPIC_TEAM_JOIN_REQUEST_CHANGED).add(session);
            logger.info("WS 连接: topic=TEAM_JOIN_REQUEST_CHANGED, session={}", session.getId());
        }
    }

    @Override
    public void afterConnectionClosed(@NonNull WebSocketSession session, @NonNull CloseStatus status) {
        // 从所有 topic 集合中移除该会话
        for (Set<WebSocketSession> set : topicSessions.values()) {
            set.remove(session);
        }
        // 从所有 user 集合中移除该会话
        for (Set<WebSocketSession> set : userSessions.values()) {
            set.remove(session);
        }
        logger.info("WebSocket 断开: {}", session.getId());
    }

    // ================= 通用推送 =================
    public void sendToTopic(String topic, String message) {
        sendMessageToSessions(getTopicSet(topic), message);
    }

    // ================= 兼容便捷方法 =================
    public void sendRoleChangedMessage(String role) {
        sendEvent(WebSocketTopics.TOPIC_ROLE_CHANGED, new RoleChangedEvent(role));
    }

    public void sendUserChangedMessage(Long userId) {
        sendEvent(WebSocketTopics.TOPIC_USER_CHANGED, new UserChangedEvent(String.valueOf(userId)));
    }

    public void sendUserMessage(Long userId, String messageType, Object data) {
        UserMessageEvent event = new UserMessageEvent(String.valueOf(userId), messageType, data);
        sendEventToUserOrTopic(WebSocketTopics.TOPIC_USER_MESSAGE, String.valueOf(userId), event);
    }

    public void sendUserAnnouncement(Long userId, String announcementType, Object data) {
        UserAnnouncementEvent event = new UserAnnouncementEvent(String.valueOf(userId), announcementType, data);
        sendEventToUserOrTopic(WebSocketTopics.TOPIC_USER_ANNOUNCEMENT, String.valueOf(userId), event);
    }

    public void sendNewMessageNotification(Long userId) {
        NewMessageEvent event = new NewMessageEvent(String.valueOf(userId));
        sendEventToUserOrTopic(WebSocketTopics.TOPIC_USER_MESSAGE, String.valueOf(userId), event);
    }

    public void sendNewAnnouncementNotification(Long userId, Long announcementId, String title, String content) {
        NewAnnouncementEvent event = new NewAnnouncementEvent(String.valueOf(userId), String.valueOf(announcementId), title, content);
        sendEventToUserOrTopic(WebSocketTopics.TOPIC_USER_ANNOUNCEMENT, String.valueOf(userId), event);
    }

    // Team domain push helpers
    public void sendTeamMemberChanged(Long teamId, Long userId, String action, String role) {
        TeamMemberChangedEvent event = new TeamMemberChangedEvent(teamId, userId, action, role);
        sendEvent(WebSocketTopics.TOPIC_TEAM_MEMBER_CHANGED, event);
    }

    public void sendTeamInvitationChanged(Long teamId, String token, String action, Long actorUserId) {
        TeamInvitationEvent event = new TeamInvitationEvent(teamId, token, action, actorUserId);
        sendEvent(WebSocketTopics.TOPIC_TEAM_INVITATION_CHANGED, event);
    }

    public void sendTeamJoinRequestChanged(Long teamId, Long requestId, String action, Long actorUserId) {
        TeamJoinRequestEvent event = new TeamJoinRequestEvent(teamId, requestId, action, actorUserId);
        sendEvent(WebSocketTopics.TOPIC_TEAM_JOIN_REQUEST_CHANGED, event);
    }

    private void sendEvent(String topic, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            logger.info("WS 推送: topic={}, payload={}", topic, payload);
            sendToTopic(topic, payload);
        } catch (JsonProcessingException e) {
            logger.error("WS 事件序列化失败: {}", e.getMessage());
        }
    }

    private void sendEventToUserOrTopic(String topic, String userId, Object event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            Set<WebSocketSession> sessions = userSessions.get(userId);
            if (sessions != null && !sessions.isEmpty()) {
                logger.info("WS 定向推送: userId={}, topic={}, payload={}", userId, topic, payload);
                sendMessageToSessions(sessions, payload);
            } else {
                if (WebSocketTopics.TOPIC_USER_ANNOUNCEMENT.equals(topic)) {
                    // 公告：无定向连接时回退广播
                    logger.info("WS 回退广播: userId={}, topic={}, payload={}", userId, topic, payload);
                    sendToTopic(topic, payload);
                } else {
                    // 用户消息：不回退
                    logger.warn("WS 未投递: 无连接的 userId={}, topic={}", userId, topic);
                }
            }
        } catch (JsonProcessingException e) {
            logger.error("WS 事件序列化失败: {}", e.getMessage());
        }
    }

    /**
     * 发送消息到指定会话集合
     */
    private void sendMessageToSessions(Set<WebSocketSession> sessions, String message) {
        for (WebSocketSession session : sessions) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                logger.error("WebSocket 消息发送失败: {}", e.getMessage());
            }
        }
    }
}
