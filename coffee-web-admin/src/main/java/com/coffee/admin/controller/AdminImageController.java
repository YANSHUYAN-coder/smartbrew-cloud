package com.coffee.admin.controller;

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
 * 图片代理接口 (管理端)
 * 用于解决跨域和网络访问问题
 */
@Slf4j
@RestController
@RequestMapping("/app/image") // 保持与 C 端接口路径一致，方便前端逻辑复用
@RequiredArgsConstructor
@Tag(name = "管理端-图片代理接口", description = "通过后端代理转发 MinIO 图片")
public class AdminImageController {

    @GetMapping("/proxy")
    @Operation(summary = "图片代理", description = "通过后端代理转发图片")
    public ResponseEntity<InputStreamResource> proxyImage(
            @Parameter(description = "原始图片 URL") @RequestParam("url") String imageUrl) {
        try {
            if (!imageUrl.startsWith("http://") && !imageUrl.startsWith("https://")) {
                return ResponseEntity.badRequest().build();
            }

            URL url = new URL(imageUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.warn("Failed to fetch image: {}, response code: {}", imageUrl, responseCode);
                return ResponseEntity.notFound().build();
            }

            String contentType = connection.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                contentType = "image/jpeg";
            }

            InputStream inputStream = connection.getInputStream();
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setCacheControl("public, max-age=31536000");
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
