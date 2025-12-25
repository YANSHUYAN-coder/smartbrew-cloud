package com.coffee.app;

import org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.mybatis.spring.annotation.MapperScan;

/**
 * C端用户 APP 启动入口
 * 扫描范围：包括本模块以及依赖的 service 模块
 * 排除 RedisVectorStoreAutoConfiguration，使用手动配置以避免 bean 冲突
 */
@SpringBootApplication(exclude = {RedisVectorStoreAutoConfiguration.class})
@ComponentScan(basePackages = "com.coffee")
@MapperScan("com.coffee.**.mapper")
public class CoffeeAppApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoffeeAppApplication.class, args);
        System.out.println("(♥◠‿◠)ﾉﾞ  智咖·云 [用户端 APP] 启动成功   ლ(´ڡ`ლ)ﾞ");
    }
}