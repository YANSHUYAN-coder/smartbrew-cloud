package com.coffee.system.service.impl;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.common.dto.PageParam;
import com.coffee.system.domain.entity.UmsMemberLevel;
import com.coffee.system.mapper.UmsMemberLevelMapper;
import com.coffee.system.service.UmsMemberLevelService;
import org.springframework.stereotype.Service;

/**
 * 会员等级服务实现
 */
@Service
public class UmsMemberLevelServiceImpl extends ServiceImpl<UmsMemberLevelMapper, UmsMemberLevel> implements UmsMemberLevelService {
    
    @Override
    public Page<UmsMemberLevel> getList(PageParam pageParam, String keyword) {
        Page<UmsMemberLevel> levelPage = new Page<>(pageParam.getPage(), pageParam.getPageSize());
        LambdaQueryWrapper<UmsMemberLevel> wrapper = new LambdaQueryWrapper<>();
        
        // 如果有关键字，搜索等级名称
        if (StrUtil.isNotBlank(keyword)) {
            wrapper.like(UmsMemberLevel::getName, keyword);
        }
        
        // 按成长值门槛升序排序（成长值越低的等级越靠前）
        wrapper.orderByAsc(UmsMemberLevel::getGrowthPoint);
        
        return this.page(levelPage, wrapper);
    }
}

