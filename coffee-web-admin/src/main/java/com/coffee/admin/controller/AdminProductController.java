package com.coffee.admin.controller;

import com.coffee.common.result.Result;
import com.coffee.system.domain.entity.Product;
import com.coffee.system.domain.dto.ProductDTO;
import com.coffee.system.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/product")
@AllArgsConstructor
public class AdminProductController {

    private final ProductService productService;

    // 1. 获取商品列表 (基础信息，包括下架商品)
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('pms:product:list')")
    public Result<List<Product>> list() {
        List<Product> list = productService.list();
        return Result.success(list);
    }

    // 2. 获取商品详情 (包含 SKU，用于编辑回显)
    @GetMapping("/detail/{id}")
//    @PreAuthorize("hasAuthority('pms:product:detail')")
    public Result<ProductDTO> detail(@PathVariable("id") Long id) {
        ProductDTO dto = productService.getDetail(id);
        return Result.success(dto);
    }

    // 3. 添加商品 (复杂结构)
    @PostMapping("/add")
    @PreAuthorize("hasAuthority('pms:product:add')")
    public Result<String> add(@RequestBody ProductDTO productDTO) {
        boolean success = productService.create(productDTO);
        return success ? Result.success("添加成功") : Result.failed("添加失败");
    }

    // 4. 修改商品
    @PostMapping("/update")
    @PreAuthorize("hasAuthority('pms:product:update')")
    public Result<String> update(@RequestBody ProductDTO productDTO) {
        boolean success = productService.update(productDTO);
        return success ? Result.success("修改成功") : Result.failed("修改失败");
    }

    // 5. 修改上下架状态
    @PostMapping("/updateStatus")
    @PreAuthorize("hasAuthority('pms:product:status')")
    public Result<String> updateStatus(@RequestBody Product product) {
        // 只需要传 id 和 status
        boolean success = productService.updateById(product);
        return success ? Result.success("状态更新成功") : Result.failed("状态更新失败");
    }

    // 6. 删除商品
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('pms:product:delete')")
    public Result<String> delete(@PathVariable("id") Long id) {
        // 逻辑删除商品，通常也应该级联删除 SKU，这里简化处理
        boolean success = productService.removeById(id);
        return success ? Result.success("删除成功") : Result.failed("删除失败");
    }
}