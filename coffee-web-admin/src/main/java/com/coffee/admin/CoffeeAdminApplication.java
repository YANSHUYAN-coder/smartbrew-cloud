package com.coffee.admin;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.ai.vectorstore.redis.autoconfigure.RedisVectorStoreAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * B端管理后台 启动入口
 * 扫描范围：包括本模块以及依赖的 system, ai 模块
 * 排除 RedisVectorStoreAutoConfiguration，使用手动配置以避免 bean 冲突
 * 定时任务在此启动，用于系统级别的定时任务（如每日刷新优惠券库存等）
 */
@SpringBootApplication(exclude = {RedisVectorStoreAutoConfiguration.class})
@ComponentScan(basePackages = "com.coffee")
@MapperScan("com.coffee.**.mapper")
@EnableScheduling
public class CoffeeAdminApplication {
    public static void main(String[] args) {
        SpringApplication.run(CoffeeAdminApplication.class, args);
        System.out.println("(Visualize)  智咖·云 [管理后台 Admin] 启动成功   (Visualize)");
        System.out.println("后台文档上传与订单管理服务已就绪...");
    }
}