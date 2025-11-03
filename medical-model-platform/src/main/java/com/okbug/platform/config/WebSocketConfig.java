package com.okbug.platform.config;

import com.okbug.platform.ws.RealtimeWebSocketHandler;
import com.okbug.platform.ws.WebSocketTopics;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * WebSocket配置类
 * 配置WebSocket端点和处理器
 * 
 * @author hanjor
 * @version 1.0
 * @date 2024-12-12 16:00
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    @Autowired
    private RealtimeWebSocketHandler permissionSocketHandler;

    @Override
    public void registerWebSocketHandlers(@NonNull WebSocketHandlerRegistry registry) {
        registry.addHandler(permissionSocketHandler, WebSocketTopics.TOPIC_ROLE_CHANGED)
                .setAllowedOrigins("*");
        registry.addHandler(permissionSocketHandler, WebSocketTopics.TOPIC_USER_CHANGED)
                .setAllowedOrigins("*");
        registry.addHandler(permissionSocketHandler, WebSocketTopics.TOPIC_USER_MESSAGE)
                .setAllowedOrigins("*");
        registry.addHandler(permissionSocketHandler, WebSocketTopics.TOPIC_USER_ANNOUNCEMENT)
                .setAllowedOrigins("*");
    }
} 