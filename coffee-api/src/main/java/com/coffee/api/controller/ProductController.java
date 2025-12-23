package com.coffee.api.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coffee.common.dto.ProductSearchParam;
import com.coffee.common.result.Result;
import com.coffee.system.domain.entity.Product;
import com.coffee.system.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * 公共商品接口
 * 提供 C 端和管理端共用的商品搜索功能
 */
@RestController
@RequestMapping("/api/product")
@Tag(name = "公共-商品接口", description = "提供 C 端和管理端共用的商品搜索接口")
@AllArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * 搜索商品（公共接口）
     * C 端和管理端都可以调用此接口
     * - C 端：自动过滤只返回上架商品（status=1）
     * - 管理端：可以通过 status 参数筛选，不传则返回所有状态
     * 
     * 权限控制：
     * - 需要登录认证
     * - C 端用户和管理端用户都可以访问
     */
    @GetMapping("/search")
    @Operation(summary = "搜索商品", description = "根据关键词、状态、分类等条件搜索商品。C端自动只返回上架商品，管理端支持状态筛选")
    public Result<Page<Product>> search(@ModelAttribute ProductSearchParam searchParam) {
        // 判断是否为管理端请求（通过权限判断）
        // 如果用户有 pms:product:list 权限，说明是管理端，允许状态筛选
        // 否则是 C 端，固定只返回上架商品
        boolean isAdmin = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("pms:product:list"));
        
        if (!isAdmin) {
            // C 端固定只返回上架商品
            searchParam.setStatus(1);
        }
        // 管理端：如果传了 status 参数就按状态筛选，不传则返回所有
        
        Page<Product> page = productService.search(searchParam);
        return Result.success(page);
    }
}

