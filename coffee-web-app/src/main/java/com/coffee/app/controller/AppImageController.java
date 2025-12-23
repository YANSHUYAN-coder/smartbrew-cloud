package com.coffee.app.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

/**
 * 图片代理接口
 * 用于解决跨域和网络访问问题
 */
@Slf4j
@RestController
@RequestMapping("/app/image")
@RequiredArgsConstructor
@Tag(name = "C端-图片代理接口", description = "通过后端代理转发 MinIO 图片，解决跨域和网络访问问题")
public class AppImageController {

    /**
     * 图片代理接口
     * 通过后端转发 MinIO 的图片，解决跨域问题
     * 
     * 使用方式：将 MinIO URL 中的域名部分替换为后端地址
     * 原 URL: http://192.168.248.128:9000/coffee-bucket/product/xxx.png
     * 新 URL: http://your-backend/api/app/image/proxy?url=http://192.168.248.128:9000/coffee-bucket/product/xxx.png
     */
    @GetMapping("/proxy")
    @Operation(summary = "图片代理", description = "通过后端代理转发图片，解决跨域和网络访问问题")
    public ResponseEntity<InputStreamResource> proxyImage(
            @Parameter(description = "原始图片 URL") @RequestParam("url") String imageUrl) {
        try {
            // 验证 URL 是否合法（允许访问 MinIO 和其他 HTTPS 图片）
            if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                return ResponseEntity.badRequest().build();
            }

            // 创建连接
            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            
            // 设置请求头
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            // 获取响应
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.warn("Failed to fetch image: {}, response code: {}", imageUrl, responseCode);
                return ResponseEntity.notFound().build();
            }

            // 获取内容类型
            String contentType = connection.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                contentType = "image/jpeg"; // 默认类型
            }

            // 读取图片流
            InputStream inputStream = connection.getInputStream();
            
            // 设置响应头
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setCacheControl("public, max-age=31536000"); // 缓存一年
            // 添加 CORS 响应头（虽然已经有全局配置，但这里明确设置更安全）
            headers.setAccessControlAllowOrigin("*");
            headers.setAccessControlAllowMethods(Arrays.asList(HttpMethod.GET, HttpMethod.HEAD, HttpMethod.OPTIONS));
            headers.setAccessControlAllowHeaders(Arrays.asList("*"));
            
            return ResponseEntity.ok()
                    .headers(headers)
                    .body(new InputStreamResource(inputStream));
                    
        } catch (Exception e) {
            log.error("Error proxying image: {}", imageUrl, e);
            return ResponseEntity.status(500).build();
        }
    }
}

