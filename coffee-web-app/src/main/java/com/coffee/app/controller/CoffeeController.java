package com.coffee.app.controller;

import com.coffee.ai.service.CoffeeAiService;
import com.coffee.common.context.UserContext;
import com.coffee.common.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/coffee/chat")
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

    /**
     * 获取聊天历史记录接口
     * * @return 历史消息列表
     */
    @GetMapping("/history")
    public Result<List<Map<String, String>>> getHistory() {
        Long userId = UserContext.getUserId();
        if (userId == null){
            return Result.failed("请先登录");
        }

        // 直接使用 userId 作为 chatId 获取历史记录
        // 确保了用户只能看到自己的记录，安全性更高
        List<Map<String, String>> history = coffeeAiService.getHistory(userId);

        return Result.success(history);
    }

    /**
     * 清除指定会话的聊天记录
     */
    @PostMapping("/clear")
    public Result<String> clearHistory() {
        Long userId = UserContext.getUserId();
        if (userId == null){
            return Result.failed("请先登录");
        }
        coffeeAiService.clearHistory(userId);
        return Result.success("清除成功");
    }
}
