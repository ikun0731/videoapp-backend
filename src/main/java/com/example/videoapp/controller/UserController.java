package com.example.videoapp.controller;

import com.example.videoapp.DTO.*;
import com.example.videoapp.common.Result;
import com.example.videoapp.entity.User;
import com.example.videoapp.entity.Video;
import com.example.videoapp.service.EmailService;
import com.example.videoapp.service.UserService;
import com.example.videoapp.service.VerificationService;
import com.example.videoapp.service.VideoService;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("api/users")
public class UserController {
    @Autowired
    private UserService userService;
    
    @Autowired
    private VideoService videoService;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private VerificationService verificationService;
    
    /**
     * 注册新用户
     * 
     * @param registerRequest 注册请求对象
     * @return 注册成功的用户信息
     */
    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterRequest registerRequest) {
        return Result.success(userService.register(registerRequest));
    }
    
    /**
     * 用户登录
     * 
     * @param loginRequest 登录请求对象
     * @return 包含JWT令牌的登录响应
     */
    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setToken(userService.login(loginRequest.getUsername(), loginRequest.getPassword()));
        return Result.success(loginResponse);
    }
    
    /**
     * 获取当前登录用户的资料
     * 
     * @param authentication 认证对象
     * @return 用户资料
     */
    @GetMapping("/me")
    public Result<UserProfileResponse> getUserInfo(Authentication authentication) {
        return Result.success(userService.getUserInfoByUsername(authentication.getName()));
    }
    
    /**
     * 根据用户名获取用户资料
     * 
     * @param username 用户名
     * @return 用户资料
     */
    @GetMapping("/{username}")
    public Result<UserProfileResponse> getUserInfoByUsername(@PathVariable String username) {
        return Result.success(userService.getUserInfoByUsername(username));
    }
    
    /**
     * 搜索用户
     * 
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页大小
     * @return 用户分页列表
     */
    @GetMapping("/search")
    public Result<PageInfo<User>> searchUsers(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(userService.searchUsers(keyword, page, size));
    }
    
    /**
     * 更新当前登录用户的资料
     * 
     * @param authentication 认证对象
     * @param updateUserProfileRequest 更新资料请求
     * @return 更新后的用户资料
     */
    @PatchMapping("/me")
    public Result<UserProfileResponse> updateUserInfo(
            Authentication authentication, 
            @RequestBody UpdateUserProfileRequest updateUserProfileRequest) {
        return Result.success(userService.updateUserInfo(authentication.getName(), updateUserProfileRequest));
    }
    
    /**
     * 获取指定用户的视频列表
     * 
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页大小
     * @return 视频分页列表
     */
    @GetMapping("/{userId}/videos")
    public Result<PageInfo<Video>> getUserVideos(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return Result.success(videoService.findVideosByUserIdPaginated(userId, page, size));
    }
    
    /**
     * 更新当前登录用户的头像
     * 
     * @param authentication 认证对象
     * @param avatarFile 头像文件
     * @return 新头像的URL
     */
    @PostMapping("/me/avatar")
    public Result<String> updateAvatar(
            Authentication authentication, 
            @RequestParam("avatar") MultipartFile avatarFile) {
        String newAvatarUrl = userService.updateAvatar(authentication.getName(), avatarFile);
        return Result.success(newAvatarUrl);
    }
    
    /**
     * 修改当前登录用户的密码
     * 
     * @param authentication 认证对象
     * @param request 修改密码请求
     * @return 操作结果
     */
    @PostMapping("/me/password")
    public Result<Void> changePassword(
            Authentication authentication, 
            @RequestBody ChangePasswordRequest request) {
        userService.changePassword(
                authentication.getName(),
                request.getOldPassword(),
                request.getNewPassword()
        );
        return Result.success();
    }
    
    /**
     * 领取每日奖励
     * 
     * @param authentication 认证对象
     * @return 操作结果
     */
    @PostMapping("/me/claim-daily")
    public Result<Void> claimDaily(Authentication authentication) {
        userService.claimDailyFish(authentication.getName());
        return Result.success();
    }
    
    /**
     * 发送测试邮件
     * 
     * @return 测试结果
     */
    @GetMapping("/test-email")
    public Result<String> testEmail() {
        String to = "1654800149@qq.com";
        String subject = "测试邮件 from 渔鱼网";
        String content = "你好！如果你能收到这封邮件，说明我们的 Spring Boot 邮件发送功能已成功配置！";

        emailService.sendSimpleMail(to, subject, content);

        return Result.success("测试邮件已异步发送，请在几秒钟后检查你的收件箱！");
    }
    
    /**
     * 发送注册验证码
     * 
     * @param payload 包含邮箱地址的请求体
     * @return 操作结果
     */
    @PostMapping("/send-verification-code")
    public Result<Void> sendVerificationCode(@RequestBody Map<String, String> payload) {
        String email = payload.get("email");
        if (email == null || email.isBlank()) {
            return Result.error(400, "邮箱地址不能为空");
        }

        if (userService.isEmailRegistered(email)) {
            return Result.error(400, "该邮箱已被注册，请直接登录或找回密码。");
        }

        String code = verificationService.generateAndStoreCode(email);
        String subject = "【渔鱼网】您的注册验证码";
        String content = "您好！\n\n您的注册验证码是：" + code + "，5分钟内有效。请勿泄露给他人。\n\n祝您在渔鱼网玩得愉快！";
        emailService.sendSimpleMail(email, subject, content);

        return Result.success();
    }
}
