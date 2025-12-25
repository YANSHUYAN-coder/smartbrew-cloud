package com.coffee.ai.config;

import com.alibaba.cloud.ai.memory.redis.RedisChatMemoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import redis.clients.jedis.JedisPooled;

import java.net.URI;

/**
 * AI 相关配置
 * 手动创建 VectorStore 以避免 Lettuce 和 Jedis 冲突
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

    // Vector Store Redis Config
    @Value("${spring.ai.vectorstore.redis.uri}")
    private String vectorStoreRedisUri;
    @Value("${spring.ai.vectorstore.redis.index}")
    private String vectorStoreIndex;
    @Value("${spring.ai.vectorstore.redis.prefix}")
    private String vectorStorePrefix;
    @Value("${spring.ai.vectorstore.redis.initialize-schema:false}")
    private boolean initializeSchema;

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

    /**
     * 手动创建 RedisVectorStore，使用独立的 JedisPooled 客户端，避免与 Spring Data Redis 的 Lettuce 冲突
     * 使用 @Primary 确保优先使用手动配置的 bean
     */
    @Bean
    @Primary
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        log.info("手动创建 RedisVectorStore，使用独立的 JedisPooled 客户端");
        // 创建独立的 JedisPooled 客户端
        // URI 格式: redis://:password@host:port
        JedisPooled jedisPooled = new JedisPooled(URI.create(vectorStoreRedisUri));

        // 配置 RedisVectorStore 使用 Builder 模式
        return RedisVectorStore.builder(jedisPooled, embeddingModel)
                .indexName(vectorStoreIndex)
                .prefix(vectorStorePrefix)
                .initializeSchema(initializeSchema)
                .build();
    }
}