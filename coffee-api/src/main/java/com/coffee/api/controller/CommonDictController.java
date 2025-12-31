package com.coffee.api.controller;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.coffee.common.dict.GenderStatus;
import com.coffee.common.dict.OrderStatus;
import com.coffee.common.result.Result;
import com.coffee.common.util.AMapUtil;
import com.coffee.system.domain.entity.OmsStore;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通用字典接口
 * 聚合了：
 * 1. 静态枚举字典 (订单状态、性别) -> 代码维护，高性能
 * 2. 动态数据库字典 (省市区) -> 对接高德地图 API，内存缓存
 */
@Slf4j
@RestController
@RequestMapping("/common/dict")
@Tag(name = "通用字典接口", description = "提供静态枚举字典、动态数据库字典")
public class CommonDictController {

    @Autowired
    private AMapUtil aMapUtil;

    // 注入高德地图 Key (默认使用你提供的值)
    @Value("${amap.key}")
    private String amapKey;

    /**
     * 3. 逆地理编码
     * 根据经纬度获取省市区信息
     */
    @GetMapping("/regeo")
    @Operation(summary = "逆地理编码", description = "根据经纬度获取省市区信息")
    public Result<JSONObject> regeo(@RequestParam String location) {
        JSONObject addressComponent = aMapUtil.getAddressComponent(location);
        return addressComponent != null ? Result.success(addressComponent) : Result.failed("解析地址失败");
    }

    @Autowired
    private com.coffee.system.service.OmsStoreService storeService;

    /**
     * 4. 计算用户与门店的距离
     * @param userLocation 用户当前经纬度 "lng,lat"
     */
    @GetMapping("/distance")
    @Operation(summary = "计算距离", description = "计算用户当前位置与门店的距离")
    public Result<Map<String, Object>> getStoreDistance(@RequestParam String userLocation, @RequestParam(required = false) Long storeId) {
        // 获取门店信息
        OmsStore store;
        if (storeId != null) {
            store = storeService.getById(storeId);
        } else {
            store = storeService.getDefaultStore();
        }
        
        if (store == null) return Result.failed("未找到门店信息");

        String storeLocation = store.getLongitude() + "," + store.getLatitude();
        int distance = aMapUtil.getDistance(storeLocation, userLocation);
        
        Map<String, Object> result = new HashMap<>();
        result.put("distance", distance); // 米
        // 格式化展示文字
        if (distance < 1000) {
            result.put("distanceText", distance + "m");
        } else {
            result.put("distanceText", String.format("%.1fkm", distance / 1000.0));
        }
        
        return Result.success(result);
    }

    // 简单的内存缓存，防止频繁调用高德接口导致 QPS 超限
    // 只有重启服务器后，第一次请求会去调高德，之后都读内存，速度极快
    private static List<Map<String, Object>> REGION_CACHE = null;

    /**
     * 1. 获取指定类型的枚举字典
     * 用于前端下拉框展示
     * @param type 字典类型: orderStatus, gender
     */
    @GetMapping("/enum/{type}")
    @Operation(summary = "获取枚举字典", description = "获取枚举字典")
    public Result<List<Map<String, Object>>> getEnumDict(@PathVariable("type") String type) {
        List<Map<String, Object>> list = new ArrayList<>();

        switch (type) {
            case "orderStatus":
                // 自动遍历枚举类
                for (OrderStatus status : OrderStatus.values()) {
                    list.add(createDict(status.getCode(), status.getDesc()));
                }
                break;

            case "gender":
                // 性别
                for (GenderStatus status : GenderStatus.values()) {
                    list.add(createDict(status.getCode(), status.getDesc()));
                }
                break;

            default:
                return Result.failed("不支持的字典类型: " + type);
        }

        return Result.success(list);
    }

    /**
     * 2. 获取全国省市区列表 (对接高德地图 API)
     * 结构：省 -> 市 -> 区
     */
    @GetMapping("/regions")
    @Operation(summary = "获取全国省市区列表", description = "获取全国省市区列表")
    public Result<List<Map<String, Object>>> getRegions() {
        // 1. 检查缓存，如果有数据直接返回，不调 API
        if (REGION_CACHE != null && !REGION_CACHE.isEmpty()) {
            return Result.success(REGION_CACHE);
        }

        // 2. 缓存为空，调用高德 API
        // subdistrict=3 表示获取到区县级 (0:国家, 1:省, 2:市, 3:区县)
        String url = String.format("https://restapi.amap.com/v3/config/district?keywords=&subdistrict=3&key=%s", amapKey);

        try {
            log.info("正在从高德地图同步行政区划数据...");
            String responseBody = HttpUtil.get(url);

            // 3. 解析 JSON
            JSONObject json = JSONUtil.parseObj(responseBody);
            if (!"1".equals(json.getStr("status"))) {
                log.error("高德API请求失败:Info: {}, Infocode: {}", json.getStr("info"), json.getStr("infocode"));
                return Result.failed("获取行政区划失败: " + json.getStr("info"));
            }

            // 4. 数据转换
            // 高德返回的根节点 districts[0] 是 "中华人民共和国"，我们需要它下面的 districts (各省)
            JSONArray districts = json.getJSONArray("districts");
            if (districts != null && !districts.isEmpty()) {
                JSONObject country = districts.getJSONObject(0);
                JSONArray provinces = country.getJSONArray("districts");

                // 递归转换结构为前端级联选择器需要的格式 (value/label/children)
                REGION_CACHE = convertAmapData(provinces);

                log.info("高德地图数据同步完成，已写入本地缓存，共加载 {} 个省级行政区", REGION_CACHE.size());
                return Result.success(REGION_CACHE);
            }

            return Result.success(new ArrayList<>());

        } catch (Exception e) {
            log.error("调用高德接口异常", e);
            return Result.failed("获取数据异常: " + e.getMessage());
        }
    }

    // --- 辅助方法 ---

    /**
     * 递归转换高德数据结构
     * 高德字段: adcode(编码), name(名称), districts(子级)
     * 前端字段: value, label, children
     */
    private List<Map<String, Object>> convertAmapData(JSONArray amapDistricts) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (amapDistricts == null || amapDistricts.isEmpty()) {
            return result;
        }

        for (int i = 0; i < amapDistricts.size(); i++) {
            JSONObject item = amapDistricts.getJSONObject(i);
            Map<String, Object> map = new HashMap<>();

            // 核心转换逻辑
            map.put("value", item.getStr("adcode")); // 例如: 110000
            map.put("label", item.getStr("name"));   // 例如: 北京市

            // 递归处理子级
            JSONArray children = item.getJSONArray("districts");
            if (children != null && !children.isEmpty()) {
                map.put("children", convertAmapData(children));
            }

            result.add(map);
        }
        return result;
    }

    private Map<String, Object> createDict(Object value, String label) {
        Map<String, Object> map = new HashMap<>();
        map.put("value", value);
        map.put("label", label);
        return map;
    }
}