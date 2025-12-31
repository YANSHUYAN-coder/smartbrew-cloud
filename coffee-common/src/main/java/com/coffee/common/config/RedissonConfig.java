package com.coffee.common.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson 分布式锁配置
 * 用于保护关键业务操作，防止并发问题
 */
@Configuration
public class RedissonConfig {

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;

    @Value("${spring.data.redis.port:6379}")
    private int redisPort;

    @Value("${spring.data.redis.password:}")
    private String redisPassword;

    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        
        // 单机模式配置
        String address = "redis://" + redisHost + ":" + redisPort;
        config.useSingleServer()
                .setAddress(address)
                .setDatabase(redisDatabase);
        
        // 如果有密码，设置密码
        if (redisPassword != null && !redisPassword.isEmpty()) {
            config.useSingleServer().setPassword(redisPassword);
        }
        
        // 连接池配置
        config.useSingleServer()
                .setConnectionPoolSize(10)  // 连接池大小
                .setConnectionMinimumIdleSize(5)  // 最小空闲连接数
                .setIdleConnectionTimeout(10000)  // 空闲连接超时时间
                .setConnectTimeout(3000)  // 连接超时时间
                .setTimeout(3000)  // 命令超时时间
                .setRetryAttempts(3)  // 重试次数
                .setRetryInterval(1500);  // 重试间隔
        
        return Redisson.create(config);
    }
}

