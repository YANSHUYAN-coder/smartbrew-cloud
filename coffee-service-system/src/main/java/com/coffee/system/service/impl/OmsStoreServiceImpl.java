package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.system.domain.entity.OmsStore;
import com.coffee.system.mapper.OmsStoreMapper;
import com.coffee.system.service.OmsStoreService;
import org.springframework.stereotype.Service;

@Service
public class OmsStoreServiceImpl extends ServiceImpl<OmsStoreMapper, OmsStore> implements OmsStoreService {

    @Override
    public OmsStore getDefaultStore() {
        // 简单实现：查询第一条记录作为默认门店
        return this.lambdaQuery().orderByAsc(OmsStore::getId).last("LIMIT 1").one();
    }
}

