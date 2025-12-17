package com.coffee.app.controller;

import com.coffee.common.result.Result;
import com.coffee.common.vo.MenuVO;
import com.coffee.common.vo.ProductDetailVO;
import com.coffee.system.domain.entity.Product;
import com.coffee.system.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * C端商品菜单接口
 */
@RestController
@RequestMapping("/app/product")
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
    public Result<Map<String, List<Product>>> getMenu() {
        Map<String, List<Product>> menuMap = productService.getMenu();
        return Result.success(menuMap);
    }

    /**
     * 获取菜单数据（新格式，前端直接使用）
     * 返回结构：{categories: [{id, name}], products: [{id, name, categoryId, ...}]}
     */
    @GetMapping("/menu/vo")
    public Result<MenuVO> getMenuVO() {
        MenuVO menuVO = productService.getMenuVO();
        return Result.success(menuVO);
    }

    /**
     * 2. 商品详情 (包含SKU规格)
     * 用户点击商品图片时调用
     */
    @GetMapping("/detail/{id}")
    public Result<ProductDetailVO> getDetail(@PathVariable("id") Long id) {
        ProductDetailVO vo = productService.getMenuDetail(id);
        if (vo == null){
            return Result.failed("商品不存在");
        }
        return Result.success(vo);
    }
}