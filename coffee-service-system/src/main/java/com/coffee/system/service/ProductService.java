package com.coffee.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coffee.common.dto.PageParam;
import com.coffee.common.dto.ProductSearchParam;
import com.coffee.common.vo.MenuVO;
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
    
    /**
     * 搜索商品（通用方法，支持关键词、状态、分类筛选）
     * @param searchParam 搜索参数
     * @return 分页结果
     */
    Page<Product> search(ProductSearchParam searchParam);

    /* 用户端*/


    /**
     * 获取商品菜单列表 (按分类分组，仅限上架商品)
     * @return 旧格式：按分类名称分组的Map
     */
    Map<String, List<Product>> getMenu();

    /**
     * 获取商品菜单列表（新格式，前端直接使用）
     * @return 包含分类列表和商品列表的VO
     */
    MenuVO getMenuVO();

    ProductDetailVO getMenuDetail(Long id);
}