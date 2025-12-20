package com.coffee.admin.controller;


import com.coffee.ai.service.CoffeeAiService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.InputStreamReader;
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

    @PostMapping("/importData")
    public String importData() {
        return coffeeAiService.importData();
    }

    @PostMapping("/search")
    public List<Document> search(@RequestParam String query) {
        return coffeeAiService.search(query);
    }

}