package com.coffee.admin.controller;


import com.coffee.ai.service.CoffeeAiService;
import com.coffee.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * AI 数据管理控制器
 */
@Tag(name = "AI管理", description = "向量数据库数据维护")
@RestController
@RequestMapping("/system/ai")
public class AdminAiController {
    @Autowired
    private CoffeeAiService coffeeAiService;

    @Operation(summary = "导入 CSV 知识库数据")
    @PostMapping("/importData")
    public Result<String> importData(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.failed("上传文件不能为空");
        }
        String message = coffeeAiService.importData(file);
        return Result.success(message);
    }

    @Operation(summary = "自动同步数据库商品到 RAG")
    @PostMapping("/syncDatabase")
    public Result<String> syncDatabase() {
        String message = coffeeAiService.syncDatabaseToVectorStore();
        return Result.success(message);
    }

}