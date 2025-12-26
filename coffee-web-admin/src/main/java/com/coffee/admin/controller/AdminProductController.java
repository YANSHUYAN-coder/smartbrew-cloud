package com.coffee.admin.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.coffee.common.dto.ProductSearchParam;
import com.coffee.common.result.Result;
import com.coffee.common.util.MinioUtil;
import com.coffee.system.domain.entity.Product;
import com.coffee.system.domain.dto.ProductDTO;
import com.coffee.system.service.ProductService;
import lombok.AllArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/admin/product")
@AllArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final MinioUtil minioUtil;

    // 1. 获取商品列表 (基础信息，包括下架商品) - 分页查询，支持筛选
    @GetMapping("/list")
    @PreAuthorize("hasAuthority('pms:product:list')")
    public Result<Page<Product>> list(@ModelAttribute ProductSearchParam searchParam) {
        // 如果参数为空，创建默认的 PageParam
        if (searchParam == null) {
            searchParam = new ProductSearchParam();
        }
        Page<Product> page = productService.getList(searchParam);
        return Result.success(page);
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
        // 建议增加参数校验
        if (product.getId() == null || product.getStatus() == null) {
            return Result.failed("参数错误");
        }
        // 只需要传 id 和 status
        boolean success = productService.updateStatus(product.getId(), product.getStatus());
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

    // 7. 上传商品图片
    @PostMapping("/upload/image")
    // @PreAuthorize("hasAnyAuthority('pms:product:add', 'pms:product:update')")
    public Result<String> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.failed("请选择要上传的文件");
        }
        try {
            String imageUrl = minioUtil.uploadProductImage(file);
            return Result.success(imageUrl);
        } catch (Exception e) {
            return Result.failed("图片上传失败: " + e.getMessage());
        }
    }
    
    // 搜索接口已移至 coffee-api 模块的 ProductController，路径：/api/product/search
}