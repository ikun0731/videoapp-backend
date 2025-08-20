package com.example.videoapp.DTO;

import lombok.Data;

/**
 * 错误响应DTO，用于返回错误信息
 */
@Data
public class ErrorResponse {
    /**
     * HTTP状态码，例如401表示未授权
     */
    private int status;
    
    /**
     * 具体的错误信息，例如"用户名或密码错误"
     */
    private String message;
    
    /**
     * 错误发生时的时间戳
     */
    private long timestamp;
}
