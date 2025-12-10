package com.coffee.app.controller;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.coffee.common.result.Result;
import com.coffee.common.vo.ProductDetailVO;
import com.coffee.system.domain.Product;
import com.coffee.system.domain.SkuStock;
import com.coffee.system.service.ProductService;
import com.coffee.system.service.SkuStockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
     */
    @GetMapping("/menu")
    public Result<Map<String, List<Product>>> getMenu() {
        Map<String, List<Product>> menuMap = productService.getMenu();
        return Result.success(menuMap);
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