package com.example.videoapp.common;

import lombok.Data;

/**
 * 统一响应结果封装类
 * 
 * @param <T> 响应数据类型
 */
@Data
public class Result<T> {

    /**
     * 状态码
     */
    private Integer code;
    
    /**
     * 提示信息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;

    /**
     * 私有构造函数，防止直接实例化
     */
    private Result() {}

    /**
     * 创建成功响应，包含数据
     * 
     * @param <T> 数据类型
     * @param data 响应数据
     * @return 成功的响应结果
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(200);
        result.setMessage("操作成功");
        result.setData(data);
        return result;
    }

    /**
     * 创建成功响应，不包含数据
     * 
     * @param <T> 数据类型
     * @return 成功的响应结果
     */
    public static <T> Result<T> success() {
        return success(null);
    }

    /**
     * 创建错误响应
     * 
     * @param <T> 数据类型
     * @param code 错误状态码
     * @param message 错误提示信息
     * @return 错误的响应结果
     */
    public static <T> Result<T> error(Integer code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}