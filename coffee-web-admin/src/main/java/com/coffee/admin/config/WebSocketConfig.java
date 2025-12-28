package com.coffee.admin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册一个 WebSocket 端点，前端连接这个地址
        // setAllowedOriginPatterns("*") 允许跨域
        registry.addEndpoint("/ws/server")
                .setAllowedOriginPatterns("*")
                .withSockJS(); // 开启 SockJS 支持
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 客户端订阅路径前缀，例如 /topic/newOrder
        registry.enableSimpleBroker("/topic");
        // 服务端接收客户端发送消息的前缀（本功能暂不用，但通常会配）
        registry.setApplicationDestinationPrefixes("/app");
    }
}