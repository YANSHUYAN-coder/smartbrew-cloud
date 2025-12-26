package com.coffee.common.cache;

import org.springframework.cache.interceptor.KeyGenerator;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 自定义缓存 Key 生成器
 * 用于生成更合理的缓存键，避免使用复杂的 SpEL 表达式
 */
@Component("cacheKeyGenerator")
public class CacheKeyGenerator implements KeyGenerator {

    @Override
    public Object generate(Object target, Method method, Object... params) {
        // 格式：类名:方法名:参数值
        String className = target.getClass().getSimpleName();
        String methodName = method.getName();
        
        // 将参数转换为字符串（处理 null）
        String paramStr = Arrays.stream(params)
                .map(param -> param == null ? "null" : param.toString())
                .collect(Collectors.joining(":"));
        
        return className + ":" + methodName + ":" + paramStr;
    }
}

