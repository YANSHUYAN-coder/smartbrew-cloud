package com.coffee.system.controller;

import com.coffee.common.result.Result;
import com.coffee.system.domain.Product;
import com.coffee.system.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/product")
@AllArgsConstructor
public class ProductController {


    private final ProductService productService;

    // 1. 获取商品列表
    @GetMapping("/list")
    public Result<List<Product>> list() {
        List<Product> list = productService.list();
        return Result.success(list);
    }

    // 2. 添加商品
    @PostMapping("/add")
    public Result<String> add(@RequestBody Product product) {
        boolean success = productService.save(product);
        return success ? Result.success("添加成功") : Result.failed("添加失败");
    }
    
    // 3. 删除商品
    @DeleteMapping("/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        boolean success = productService.removeById(id);
        return success ? Result.success("删除成功") : Result.failed("删除失败");
    }
}