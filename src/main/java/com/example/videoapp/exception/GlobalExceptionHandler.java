package com.example.videoapp.exception;

import com.example.videoapp.DTO.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 全局异常处理器，处理应用中抛出的所有未捕获异常
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    /**
     * 处理所有运行时异常
     * 
     * @param ex 捕获到的运行时异常
     * @return 包含错误信息的响应实体
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        ErrorResponse error = new ErrorResponse();
        error.setStatus(401);
        error.setMessage(ex.getMessage());
        error.setTimestamp(System.currentTimeMillis());
        return new ResponseEntity<>(error, HttpStatus.UNAUTHORIZED);
    }
}
