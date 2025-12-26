package com.coffee.system.controller.app;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coffee.common.dto.PageParam;
import com.coffee.common.result.Result;
import com.coffee.system.domain.vo.IntegrationHistoryVO;
import com.coffee.system.service.UmsMemberIntegrationHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户积分明细控制器
 */
@RestController
@RequestMapping("/app/integration")
public class AppIntegrationHistoryController {

    @Autowired
    private UmsMemberIntegrationHistoryService integrationHistoryService;

    /**
     * 获取当前用户的积分明细列表
     */
    @GetMapping("/history")
    public Result<Page<IntegrationHistoryVO>> getMyIntegrationHistory(PageParam pageParam) {
        return Result.success(integrationHistoryService.getMyIntegrationHistory(pageParam));
    }
}

