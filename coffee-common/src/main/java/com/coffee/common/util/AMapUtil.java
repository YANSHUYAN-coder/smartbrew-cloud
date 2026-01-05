package com.coffee.common.util;

import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 高德地图工具类
 * 用于地理编码、距离计算等
 */
@Slf4j
@Component
public class AMapUtil {

    @Value("${amap.key:f5ca3cdf9f9acf90ac8fa849446a0f15}")
    private String amapKey;

    private static final String DISTANCE_URL = "https://restapi.amap.com/v3/distance";
    private static final String REGEO_URL = "https://restapi.amap.com/v3/geocode/regeo";
    private static final String GEO_URL = "https://restapi.amap.com/v3/geocode/geo";
    private static final String WALKING_DIRECTION_URL = "https://restapi.amap.com/v3/direction/walking";

    /**
     * 逆地理编码：根据坐标获取详细地址组件（省市区）
     * @param location "lng,lat"
     * @return 包含省市区的 JSON 对象
     */
    public JSONObject getAddressComponent(String location) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("key", amapKey);
            paramMap.put("location", location);
            paramMap.put("extensions", "base"); // 只获取基础地址信息

            String result = HttpUtil.get(REGEO_URL, paramMap);
            JSONObject jsonObject = JSONUtil.parseObj(result);

            if ("1".equals(jsonObject.getStr("status"))) {
                return jsonObject.getJSONObject("regcodes") != null ? 
                        null : jsonObject.getJSONObject("regeocode").getJSONObject("addressComponent");
            }
        } catch (Exception e) {
            log.error("高德地图解析地址失败", e);
        }
        return null;
    }

    /**
     * 计算两个坐标点之间的骑行距离（米）
     * @param origins 起点坐标 "lng,lat"
     * @param destination 终点坐标 "lng,lat"
     * @return 距离（米），出错返回 -1
     */
    public int getDistance(String origins, String destination) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("key", amapKey);
            paramMap.put("origins", origins);
            paramMap.put("destination", destination);
            paramMap.put("type", 3); // 3: 步行距离，适合外卖/短距离场景

            String result = HttpUtil.get(DISTANCE_URL, paramMap);
            JSONObject jsonObject = JSONUtil.parseObj(result);

            if ("1".equals(jsonObject.getStr("status"))) {
                return jsonObject.getJSONArray("results")
                        .getJSONObject(0)
                        .getInt("distance");
            } else {
                log.error("高德地图距离计算失败: {}", jsonObject.getStr("info"));
            }
        } catch (Exception e) {
            log.error("调用高德地图 API 异常", e);
        }
        return -1;
    }

    /**
     * 地理编码：根据地址获取经纬度坐标
     * @param address 完整地址，例如："广东省深圳市南山区科技园中区科兴科学园B栋301"
     * @param city 城市（可选），例如："深圳市"，用于提高匹配精度
     * @return 坐标字符串 "lng,lat"，失败返回 null
     */
    public String geocode(String address, String city) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("key", amapKey);
            paramMap.put("address", address);
            if (city != null && !city.isEmpty()) {
                paramMap.put("city", city);
            }

            String result = HttpUtil.get(GEO_URL, paramMap);
            JSONObject jsonObject = JSONUtil.parseObj(result);

            if ("1".equals(jsonObject.getStr("status"))) {
                JSONObject geocodes = jsonObject.getJSONArray("geocodes").getJSONObject(0);
                String location = geocodes.getStr("location");
                if (location != null && !location.isEmpty()) {
                    return location; // 返回 "lng,lat" 格式
                }
            } else {
                log.warn("高德地图地理编码失败: {} - {}", jsonObject.getStr("info"), address);
            }
        } catch (Exception e) {
            log.error("调用高德地图地理编码 API 异常: {}", address, e);
        }
        return null;
    }

    /**
     * 步行路线规划：获取从起点到终点的真实路线坐标点
     * @param origin 起点坐标 "lng,lat"
     * @param destination 终点坐标 "lng,lat"
     * @return 路线坐标点列表，每个元素为 "lng,lat" 格式，失败返回 null
     */
    public java.util.List<String> getWalkingRoute(String origin, String destination) {
        try {
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("key", amapKey);
            paramMap.put("origin", origin);
            paramMap.put("destination", destination);
            paramMap.put("extensions", "base"); // base: 返回基础路线信息，all: 返回详细信息

            String result = HttpUtil.get(WALKING_DIRECTION_URL, paramMap);
            log.debug("高德地图路线规划API返回: {}", result);
            JSONObject jsonObject = JSONUtil.parseObj(result);

            if ("1".equals(jsonObject.getStr("status"))) {
                JSONObject route = jsonObject.getJSONObject("route");
                if (route != null) {
                    cn.hutool.json.JSONArray paths = route.getJSONArray("paths");
                    if (paths != null && paths.size() > 0) {
                        // 获取第一条路径（通常是最优路径）
                        JSONObject path = paths.getJSONObject(0);
                        // 步行路线规划返回的是 steps 数组，每个 step 包含 polyline
                        cn.hutool.json.JSONArray steps = path.getJSONArray("steps");
                        java.util.List<String> points = new java.util.ArrayList<>();
                        
                        if (steps != null && !steps.isEmpty()) {
                            for (int i = 0; i < steps.size(); i++) {
                                JSONObject step = steps.getJSONObject(i);
                                String polyline = step.getStr("polyline");
                                if (polyline != null && !polyline.isEmpty()) {
                                    String[] pointArray = polyline.split(";");
                                    for (String point : pointArray) {
                                        if (point != null && !point.trim().isEmpty()) {
                                            points.add(point.trim());
                                        }
                                    }
                                }
                            }
                        }
                        
                        if (!points.isEmpty()) {
                            return points;
                        }
                    } else {
                        log.warn("高德地图路线规划返回空路径，起点: {}, 终点: {}", origin, destination);
                    }
                } else {
                    log.warn("高德地图路线规划返回空route对象，起点: {}, 终点: {}", origin, destination);
                }
            } else {
                String info = jsonObject.getStr("info");
                String infocode = jsonObject.getStr("infocode");
                log.warn("高德地图路线规划失败: {} (错误码: {}) - 从 {} 到 {}", info, infocode, origin, destination);
            }
        } catch (Exception e) {
            log.error("调用高德地图路线规划 API 异常: 从 {} 到 {}", origin, destination, e);
        }
        return null;
    }
}

