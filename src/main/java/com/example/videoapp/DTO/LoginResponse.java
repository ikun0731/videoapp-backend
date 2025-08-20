package com.example.videoapp.DTO;

import lombok.Data;

/**
 * 登录响应DTO
 */
@Data
public class LoginResponse {
    /**
     * JWT令牌
     */
    private String token;
}
