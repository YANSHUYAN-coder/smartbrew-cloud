package com.coffee.common.result;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一 API 响应结构
 */
@Data
@NoArgsConstructor
public class Result<T> {
    private long code;
    private String message;
    private T data;

    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<T>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    public static <T> Result<T> failed(String message) {
        Result<T> result = new Result<T>();
        result.setCode(500);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> failed(long code, String message) {
        Result<T> result = new Result<T>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}