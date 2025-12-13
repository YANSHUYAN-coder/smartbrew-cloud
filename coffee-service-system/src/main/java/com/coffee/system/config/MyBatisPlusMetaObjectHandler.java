package com.coffee.system.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis Plus 自动填充处理器
 * 用于自动填充创建时间和更新时间
 */
@Component
public class MyBatisPlusMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时的填充策略
     * 当执行 insert 操作时，自动填充 createTime 和 updateTime
     * 
     * strictInsertFill 方法：只有当字段值为 null 时才填充
     * - 如果前端没有传入这些字段，字段值为 null，会被填充
     * - 如果前端传入了 null，也会被填充
     * - 如果前端传入了具体时间，则使用前端传入的值（不会覆盖）
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        // 只在字段值为 null 时填充，如果已有值则不覆盖
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, now);
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, now);
    }

    /**
     * 更新时的填充策略
     * 当执行 update 操作时，自动填充 updateTime
     * 
     * 注意：对于更新操作，我们希望在每次更新时都更新 updateTime
     * 但 strictUpdateFill 只在字段为 null 时填充，所以使用 setFieldValByName 强制更新
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        LocalDateTime now = LocalDateTime.now();
        // 对于 updateTime，每次更新都应该设置为当前时间
        // 使用 setFieldValByName 确保无论字段是否有值都会被更新
        this.setFieldValByName("updateTime", now, metaObject);
    }
}

