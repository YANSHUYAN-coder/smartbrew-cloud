package com.coffee.common.config;

import io.minio.MinioClient;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MinIO 配置类
 * 读取 application.yml 中的 minio 配置并注册 MinioClient Bean
 * 只有在类路径中存在 MinioClient 时才会加载此配置
 */
@Data
@Configuration
@ConditionalOnClass(MinioClient.class)
@ConfigurationProperties(prefix = "minio")
public class MinioConfig {

    /**
     * MinIO 服务地址 (例如 http://localhost:9000)
     */
    private String endpoint;

    /**
     * 访问密钥 (Access Key)
     */
    private String accessKey;

    /**
     * 私有密钥 (Secret Key)
     */
    private String secretKey;

    /**
     * 默认存储桶名称 (例如 coffee-bucket)
     */
    private String bucketName;

    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}