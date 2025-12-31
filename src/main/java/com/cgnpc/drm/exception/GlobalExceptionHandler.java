package com.cgnpc.drm.exception;

import com.cgnpc.drm.vo.ResponseVO;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理运行时异常
     */
    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseVO<Object> handleRuntimeException(RuntimeException e) {
        return ResponseVO.error(500, e.getMessage());
    }
    
    /**
     * 处理参数缺失异常
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseVO<Object> handleMissingParams(MissingServletRequestParameterException e) {
        String message = "缺少必要参数: " + e.getParameterName();
        return ResponseVO.error(400, message);
    }
    
    /**
     * 处理请求体解析异常
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseVO<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException e) {
        return ResponseVO.error(400, "请求参数格式错误");
    }
    
    /**
     * 处理通用异常
     */
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ResponseVO<Object> handleException(Exception e) {
        return ResponseVO.error(500, "服务器内部错误");
    }
}