package com.cgnpc.drm.vo;

import lombok.Data;

/**
 * 统一API响应包装类
 */
@Data
public class ResponseVO<T> {
    
    /**
     * 响应状态码
     */
    private Integer code;
    
    /**
     * 响应消息
     */
    private String message;
    
    /**
     * 响应数据
     */
    private T data;
    
    /**
     * 构造方法私有化
     */
    private ResponseVO() {
    }
    
    /**
     * 成功响应
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ResponseVO<T> success() {
        ResponseVO<T> response = new ResponseVO<>();
        response.setCode(200);
        response.setMessage("请求成功");
        return response;
    }
    
    /**
     * 成功响应带数据
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ResponseVO<T> success(T data) {
        ResponseVO<T> response = new ResponseVO<>();
        response.setCode(200);
        response.setMessage("请求成功");
        response.setData(data);
        return response;
    }
    
    /**
     * 成功响应带自定义消息
     * @param message 自定义消息
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ResponseVO<T> success(String message) {
        ResponseVO<T> response = new ResponseVO<>();
        response.setCode(200);
        response.setMessage(message);
        return response;
    }
    
    /**
     * 成功响应带自定义消息和数据
     * @param message 自定义消息
     * @param data 响应数据
     * @param <T> 数据类型
     * @return 成功响应
     */
    public static <T> ResponseVO<T> success(String message, T data) {
        ResponseVO<T> response = new ResponseVO<>();
        response.setCode(200);
        response.setMessage(message);
        response.setData(data);
        return response;
    }
    
    /**
     * 错误响应
     * @param code 错误状态码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误响应
     */
    public static <T> ResponseVO<T> error(int code, String message) {
        ResponseVO<T> response = new ResponseVO<>();
        response.setCode(code);
        response.setMessage(message);
        return response;
    }
    
    /**
     * 错误响应
     * @param message 错误消息
     * @param <T> 数据类型
     * @return 错误响应
     */
    public static <T> ResponseVO<T> error(String message) {
        ResponseVO<T> response = new ResponseVO<>();
        response.setCode(500);
        response.setMessage(message);
        return response;
    }
}