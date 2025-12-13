package com.coffee.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.coffee.common.dto.PageParam;
import com.coffee.system.domain.entity.UmsMemberLevel;

/**
 * 会员等级服务接口
 */
public interface UmsMemberLevelService extends IService<UmsMemberLevel> {
    /**
     * 分页查询会员等级列表
     * @param pageParam 分页参数
     * @param keyword 关键字（搜索等级名称）
     * @return 分页结果
     */
    Page<UmsMemberLevel> getList(PageParam pageParam, String keyword);
}

