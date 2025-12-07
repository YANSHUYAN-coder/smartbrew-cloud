package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.system.domain.Product;
import com.coffee.system.mapper.ProductMapper;
import com.coffee.system.service.ProductService;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {
    // 业务逻辑写在这里
}