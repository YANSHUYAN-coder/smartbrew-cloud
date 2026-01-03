package com.coffee.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.coffee.system.domain.entity.OmsStore;
import com.coffee.system.mapper.OmsStoreMapper;
import com.coffee.system.service.OmsStoreService;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OmsStoreServiceImpl extends ServiceImpl<OmsStoreMapper, OmsStore> implements OmsStoreService {

    @Override
    public OmsStore getDefaultStore() {
        // 简单实现：查询第一条记录作为默认门店
        return this.lambdaQuery().orderByAsc(OmsStore::getId).last("LIMIT 1").one();
    }

    @Override
    public List<OmsStore> listNearby(Double longitude, Double latitude) {
        List<OmsStore> allStores = this.list();
        
        // 如果没有经纬度，按 ID 排序返回
        if (longitude == null || latitude == null) {
            return allStores;
        }

        // 使用球面距离公式进行排序（Haversine formula 简化版）
        return allStores.stream()
                .sorted(Comparator.comparingDouble(store -> 
                    calculateDistance(latitude, longitude, store.getLatitude(), store.getLongitude())
                ))
                .collect(Collectors.toList());
    }

    /**
     * 计算两点间的球面距离（单位：公里）
     */
    private double calculateDistance(double lat1, double lng1, double lat2, double lng2) {
        double radLat1 = Math.toRadians(lat1);
        double radLat2 = Math.toRadians(lat2);
        double a = radLat1 - radLat2;
        double b = Math.toRadians(lng1) - Math.toRadians(lng2);
        double s = 2 * Math.asin(Math.sqrt(Math.pow(Math.sin(a / 2), 2)
                + Math.cos(radLat1) * Math.cos(radLat2) * Math.pow(Math.sin(b / 2), 2)));
        s = s * 6378.137; // 地球半径 公里
        return s;
    }
}

