package com.example.videoapp.DTO;

import lombok.Data;

/**
 * 修改密码请求DTO
 */
@Data
public class ChangePasswordRequest {
    /**
     * 旧密码
     */
    private String oldPassword;
    
    /**
     * 新密码
     */
    private String newPassword;
}