package com.coffee.common.util;

import com.coffee.common.config.MinioConfig;
import io.minio.*;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * MinIO 操作工具类
 * 用于文件上传、下载、获取预览地址等
 * 只有在类路径中存在 MinioClient 时才会创建此 Bean
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(MinioClient.class)
public class MinioUtil {

    private final MinioClient minioClient;
    private final MinioConfig minioConfig;

    /**
     * 检查存储桶是否存在，不存在则创建，并配置 CORS
     */
    public void createBucketIfNotExists() {
        String bucketName = minioConfig.getBucketName();
        try {
            boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                log.info("MinIO bucket '{}' created successfully.", bucketName);
            }
            // 设置 bucket 为公开读权限
            setBucketPolicyPublic(bucketName);
            // 配置 CORS（允许跨域访问）
            setBucketCors(bucketName);
        } catch (Exception e) {
            log.error("Failed to check/create MinIO bucket.", e);
            throw new RuntimeException("MinIO bucket operation failed");
        }
    }

    /**
     * 设置存储桶为公开读权限
     * 这样可以直接通过 URL 访问文件，无需签名
     */
    private void setBucketPolicyPublic(String bucketName) {
        try {
            // MinIO 公开读策略 JSON
            String policy = "{\n" +
                    "  \"Version\": \"2012-10-17\",\n" +
                    "  \"Statement\": [\n" +
                    "    {\n" +
                    "      \"Effect\": \"Allow\",\n" +
                    "      \"Principal\": {\n" +
                    "        \"AWS\": [\"*\"]\n" +
                    "      },\n" +
                    "      \"Action\": [\"s3:GetObject\"],\n" +
                    "      \"Resource\": [\"arn:aws:s3:::" + bucketName + "/*\"]\n" +
                    "    }\n" +
                    "  ]\n" +
                    "}";
            
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(policy)
                            .build()
            );
            log.info("MinIO bucket '{}' set to public read.", bucketName);
        } catch (Exception e) {
            log.warn("Failed to set bucket policy to public for '{}': {}. You may need to set it manually in MinIO console.", bucketName, e.getMessage());
            // 不抛出异常，允许继续执行，用户可以手动在 MinIO 控制台设置
        }
    }

    /**
     * 配置存储桶的 CORS 规则
     * 允许浏览器跨域访问图片
     * 
     * 注意：MinIO Java SDK 可能不支持直接设置 CORS，建议使用以下方式之一：
     * 1. 使用命令行工具 mc: mc cors set myminio/coffee-bucket cors.json
     * 2. 在 MinIO 控制台的 Access 或 Anonymous 页面配置
     * 3. 继续使用后端代理方案（当前方案）
     */
    private void setBucketCors(String bucketName) {
        // MinIO Java SDK 8.5.7 可能不支持 setBucketCors 方法
        // 这里只记录日志，提示用户手动配置或使用代理方案
        log.info("MinIO bucket '{}' CORS configuration skipped. Please configure CORS manually using:", bucketName);
        log.info("  1. MinIO Console: Access/Anonymous page");
        log.info("  2. Command line: mc cors set myminio/{}/cors.json", bucketName);
        log.info("  3. Or continue using the proxy solution (current approach)");
        
        // 如果需要通过代码配置，可以使用 MinIO Admin API 或 REST API
        // 但通常建议使用命令行工具或控制台配置
    }

    /**
     * 上传文件
     *
     * @param file     前端上传的文件对象
     * @param dirPath  文件存储目录 (例如 "product/" 或 "avatar/")
     * @return 文件的完整访问 URL (或相对路径，取决于业务需求)
     */
    public String uploadFile(MultipartFile file, String dirPath) {
        // 1. 确保桶存在
        createBucketIfNotExists();

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new RuntimeException("File name cannot be empty");
        }

        // 2. 生成唯一文件名: dirPath + uuid + extension
        // 例如: avatar/550e8400-e29b-41d4-a716-446655440000.jpg
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = dirPath + UUID.randomUUID().toString().replace("-", "") + suffix;

        try {
            InputStream inputStream = file.getInputStream();
            // 3. 执行上传
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            
            inputStream.close();
            
            // 4. 返回文件访问路径
            // 如果桶是公开的 (public)，直接拼接 URL 返回
            // 格式: endpoint/bucketName/objectName
            return minioConfig.getEndpoint() + "/" + minioConfig.getBucketName() + "/" + objectName;

        } catch (Exception e) {
            log.error("File upload failed: {}", objectName, e);
            throw new RuntimeException("File upload failed");
        }
    }

    /**
     * 上传用户头像 (封装 uploadFile)
     */
    public String uploadAvatar(MultipartFile file) {
        return uploadFile(file, "avatar/");
    }

    /**
     * 上传商品图片 (封装 uploadFile)
     */
    public String uploadProductImage(MultipartFile file) {
        return uploadFile(file, "product/");
    }

    /**
     * 获取文件的临时预览链接 (带签名，有效期内可访问)
     * 适用于私有桶 (Private Bucket)
     *
     * @param objectName 文件在桶中的路径 (例如 "avatar/xxx.jpg")
     * @return 签名 URL
     */
    public String getPresignedUrl(String objectName) {
        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(minioConfig.getBucketName())
                            .object(objectName)
                            .expiry(24, TimeUnit.HOURS) // 24小时有效
                            .build()
            );
        } catch (Exception e) {
            log.error("Failed to get presigned URL for object: {}", objectName, e);
            return null;
        }
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件的完整 URL 或 objectName
     */
    public void removeFile(String fileUrl) {
        try {
            // 从 URL 中解析出 objectName
            // 假设 URL 是: http://localhost:9000/coffee-bucket/avatar/xxx.jpg
            // 我们需要提取: avatar/xxx.jpg
            String bucketName = minioConfig.getBucketName();
            String prefix = minioConfig.getEndpoint() + "/" + bucketName + "/";
            
            String objectName = fileUrl;
            if (fileUrl.startsWith(prefix)) {
                objectName = fileUrl.replace(prefix, "");
            }

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build()
            );
            log.info("File removed successfully: {}", objectName);
        } catch (Exception e) {
            log.error("Failed to remove file: {}", fileUrl, e);
            throw new RuntimeException("Remove file failed");
        }
    }
}