package com.coffee.app.controller;

import com.coffee.ai.service.CoffeeAiService;
import com.coffee.common.context.UserContext;
import com.coffee.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/coffee")
public class CoffeeController {
    @Autowired
    private CoffeeAiService coffeeAiService;

    /**
     * 新增的RAG问答接口，明确展示查询向量数据库的过程
     *
     * @param question 用户的问题
     * @return AI基于检索到的信息生成的回答
     */
    @GetMapping("/rag")
    public Result<String> rag(@RequestParam("question") String question) {
        Long userId = UserContext.getUserId();
        if (userId == null){
            return Result.failed("请先登录");
        }
        // 调用 service 中配置好 RAG 的对话能力
        String answer = coffeeAiService.chat("你是智咖云的服务员，你需要回答用户的问题。", question,userId);
        return Result.success(answer);
    }
}
