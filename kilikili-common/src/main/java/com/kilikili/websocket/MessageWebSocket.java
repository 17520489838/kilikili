package com.kilikili.websocket;

import com.kilikili.component.RedisComponent;
import com.kilikili.entity.dto.TokenUserInfoDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/message")
@Component
public class MessageWebSocket {

    private static final Logger logger = LoggerFactory.getLogger(MessageWebSocket.class);
    private static final ConcurrentHashMap<String, Session> SESSION_MAP = new ConcurrentHashMap<>();

    private static RedisComponent redisComponent;

    @Resource
    public void setRedisComponent(RedisComponent redisComponent) {
        MessageWebSocket.redisComponent = redisComponent;
    }

    @OnOpen
    public void onOpen(Session session) {
        String token = session.getQueryString();
        if (token != null && token.startsWith("token=")) {
            token = token.substring(6);
        }
        if (token == null || token.isEmpty()) {
            try {
                session.close();
            } catch (IOException e) {
                logger.error("WebSocket close error", e);
            }
            return;
        }
        TokenUserInfoDto tokenUserInfoDto = redisComponent.getTokenUserInfo(token);
        if (tokenUserInfoDto == null) {
            try {
                session.close();
            } catch (IOException e) {
                logger.error("WebSocket close error", e);
            }
            return;
        }
        SESSION_MAP.put(tokenUserInfoDto.getUserId(), session);
        logger.info("WebSocket connected: userId={}", tokenUserInfoDto.getUserId());
    }

    @OnClose
    public void onClose(Session session) {
        String userId = getUserIdBySession(session);
        if (userId != null) {
            SESSION_MAP.remove(userId);
            logger.info("WebSocket disconnected: userId={}", userId);
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        String userId = getUserIdBySession(session);
        logger.error("WebSocket error: userId={}", userId, error);
    }

    public static void sendMessage(String userId, String message) {
        Session session = SESSION_MAP.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
            } catch (IOException e) {
                logger.error("WebSocket send message error: userId={}", userId, e);
            }
        }
    }

    private static String getUserIdBySession(Session session) {
        for (java.util.Map.Entry<String, Session> entry : SESSION_MAP.entrySet()) {
            if (entry.getValue().equals(session)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
