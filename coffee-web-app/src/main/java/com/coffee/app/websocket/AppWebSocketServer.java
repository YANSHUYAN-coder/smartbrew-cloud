package com.coffee.app.websocket;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户端 WebSocket 服务
 * 监听路径: /ws/app/{userId}
 * * 修复说明：
 * 1. 确认项目其他模块（如 security）编译通过，说明是 Spring Boot 2.x 环境。
 * 2. 这里必须使用 javax.websocket 包。
 * 3. 请确保 pom.xml 中已添加 spring-boot-starter-websocket 依赖。
 */
@ServerEndpoint("/ws/app/{userId}")
@Component
@Slf4j
public class AppWebSocketServer {

    /**
     * 存放每个客户端对应的 WebSocket 对象
     * Key: userId, Value: Session
     */
    private static final ConcurrentHashMap<String, Session> webSocketMap = new ConcurrentHashMap<>();

    /**
     * 连接建立成功调用的方法
     */
    @OnOpen
    public void onOpen(Session session, @PathParam("userId") String userId) {
        if (userId != null) {
            webSocketMap.put(userId, session);
            log.info("用户连接 WebSocket: {}, 当前在线人数: {}", userId, webSocketMap.size());
        }
    }

    /**
     * 连接关闭调用的方法
     */
    @OnClose
    public void onClose(@PathParam("userId") String userId) {
        if (userId != null) {
            webSocketMap.remove(userId);
            log.info("用户断开 WebSocket: {}", userId);
        }
    }

    /**
     * 收到客户端消息后调用的方法
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        log.info("收到客户端消息: {}", message);
        // 回复心跳检测
        if ("ping".equals(message)) {
            try {
                session.getBasicRemote().sendText("pong");
            } catch (IOException e) {
                log.error("回复心跳失败", e);
            }
        }
    }

    @OnError
    public void onError(Session session, Throwable error) {
        log.error("WebSocket 发生错误", error);
    }

    /**
     * 向指定用户发送消息
     * @param userId 用户ID
     * @param message 消息内容 (JSON格式)
     */
    public static void sendInfo(String userId, String message) {
        Session session = webSocketMap.get(userId);
        if (session != null && session.isOpen()) {
            try {
                session.getBasicRemote().sendText(message);
                log.info("推送消息给用户 {}: {}", userId, message);
            } catch (IOException e) {
                log.error("推送消息失败", e);
            }
        } else {
            log.warn("用户 {} 不在线，消息未发送", userId);
        }
    }
}