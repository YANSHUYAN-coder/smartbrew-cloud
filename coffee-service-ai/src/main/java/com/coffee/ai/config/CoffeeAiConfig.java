package com.coffee.ai.config;

import com.alibaba.cloud.ai.memory.redis.RedisChatMemoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * AI 相关配置
 */
@Configuration
@Slf4j
@RequiredArgsConstructor
public class CoffeeAiConfig {
    @Value("${spring.ai.memory.redis.host}")
    private String redisHost;
    @Value("${spring.ai.memory.redis.port}")
    private int redisPort;
    @Value("${spring.ai.memory.redis.password}")
    private String password;
    @Value("${spring.ai.memory.redis.timeout}")
    private int redisTimeout;
    @Bean
    @Primary
    public RedisChatMemoryRepository redisChatMemoryRepository() {
        return RedisChatMemoryRepository.builder()
                .host(redisHost)
                .port(redisPort)
                .password(password)
                .timeout(redisTimeout)
                .build();
    }

    @Bean
    public ChatMemory redisChatMemory(RedisChatMemoryRepository chatMemoryRepository) {
        return MessageWindowChatMemory.builder()
                .maxMessages(10)
                .chatMemoryRepository(chatMemoryRepository)
                .build();
    }
}