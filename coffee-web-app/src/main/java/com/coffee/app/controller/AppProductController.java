package com.coffee.app.controller;

import com.coffee.common.result.Result;
import com.coffee.common.vo.MenuVO;
import com.coffee.common.vo.ProductDetailVO;
import com.coffee.system.domain.entity.Product;
import com.coffee.system.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * C端商品菜单接口
 */
@RestController
@RequestMapping("/app/product")
@Tag(name = "C端-商品菜单接口", description = "提供小程序/APP 端使用的商品菜单与详情查询接口")
public class AppProductController {

    @Autowired
    private ProductService productService;

    /**
     * 1. 首页菜单列表 (按分类分组)
     * 返回结构：{"拿铁": [Product1, Product2], "美式": [...]}
     * @deprecated 使用 getMenuVO() 替代
     */
    @GetMapping("/menu")
    @Deprecated
    @Operation(summary = "【已废弃】按分类分组的菜单列表", description = "旧版菜单接口，返回 Map<分类名称, 商品列表> 结构，已被 /menu/vo 替代")
    public Result<Map<String, List<Product>>> getMenu() {
        Map<String, List<Product>> menuMap = productService.getMenu();
        return Result.success(menuMap);
    }

    /**
     * 获取菜单数据（新格式，前端直接使用）
     * 返回结构：{categories: [{id, name}], products: [{id, name, categoryId, ...}]}
     */
    @GetMapping("/menu/vo")
    @Operation(summary = "获取菜单数据（新格式）", description = "返回包含分类列表与商品列表的结构，前端可直接用于渲染点单页")
    public Result<MenuVO> getMenuVO() {
        MenuVO menuVO = productService.getMenuVO();
        return Result.success(menuVO);
    }

    /**
     * 2. 商品详情 (包含SKU规格)
     * 用户点击商品图片时调用
     */
    @GetMapping("/detail/{id}")
    @Operation(summary = "获取商品详情", description = "根据商品ID获取详情信息（包含 SKU 规格列表），用于前端规格弹窗展示")
    public Result<ProductDetailVO> getDetail(
            @Parameter(description = "商品ID") @PathVariable("id") Long id) {
        ProductDetailVO vo = productService.getMenuDetail(id);
        if (vo == null){
            return Result.failed("商品不存在");
        }
        return Result.success(vo);
    }
    
    // 搜索接口已移至 coffee-api 模块的 ProductController，路径：/api/product/search
}