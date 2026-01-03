package com.coffee.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.coffee.system.domain.entity.OmsStore;

public interface OmsStoreService extends IService<OmsStore> {
    /**
     * 获取当前默认门店信息（本系统暂定单店模式）
     */
    OmsStore getDefaultStore();

    /**
     * 获取附近的门店列表（按距离排序）
     */
    java.util.List<OmsStore> listNearby(Double longitude, Double latitude);
}

