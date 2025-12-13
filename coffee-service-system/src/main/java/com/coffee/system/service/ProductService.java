package com.coffee.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coffee.common.dto.PageParam;
import com.coffee.common.vo.ProductDetailVO;
import com.coffee.system.domain.entity.Product;
import com.coffee.system.domain.dto.ProductDTO;

import java.util.List;
import java.util.Map;

public interface ProductService extends IService<Product> {
    /*管理端*/
    
    /**
     * 分页查询商品列表
     */
    Page<Product> getList(PageParam pageParam);
    
    /**
     * 创建商品（包含SKU）
     */
    boolean create(ProductDTO productDTO);

    /**
     * 修改商品（包含SKU）
     */
    boolean update(ProductDTO productDTO);

    /**
     * 获取商品详情（包含SKU）- 用于后台编辑回显
     */
    ProductDTO getDetail(Long id);

    /* 用户端*/


    /**
     * 获取商品菜单列表 (按分类分组，仅限上架商品)
     * @return
     */
    Map<String, List<Product>> getMenu();

    ProductDetailVO getMenuDetail(Long id);
}