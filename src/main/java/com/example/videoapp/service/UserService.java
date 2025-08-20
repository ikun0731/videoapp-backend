package com.example.videoapp.service;

import com.example.videoapp.DTO.RegisterRequest;
import com.example.videoapp.DTO.UpdateUserProfileRequest;
import com.example.videoapp.DTO.UserProfileResponse;
import com.example.videoapp.JwtUtil;
import com.example.videoapp.entity.User;
import com.example.videoapp.mapper.UserMapper;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private JwtUtil jwtUtil;
    
    @Autowired
    private TencentCosService tencentCosService;
    
    @Autowired
    private VerificationService verificationService;
    
    /**
     * 注册新用户
     * 
     * @param registerRequest 包含用户注册信息的请求对象
     * @return 注册成功的用户对象
     */
    public User register(RegisterRequest registerRequest) {
        // 校验验证码
        boolean isCodeValid = verificationService.verifyCode(registerRequest.getEmail(), registerRequest.getVerificationCode());
        if (!isCodeValid) {
            throw new RuntimeException("验证码错误或已过期");
        }

        // 校验用户名是否已被注册
        User existingUser = userMapper.findByUsername(registerRequest.getUsername());
        if (existingUser != null) {
            throw new RuntimeException("用户名 '" + registerRequest.getUsername() + "' 已被注册");
        }
        
        // 校验邮箱是否已被注册
        User userByEmail = userMapper.findByEmail(registerRequest.getEmail());
        if (userByEmail != null) {
            throw new RuntimeException("该邮箱已被注册");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setNickname(registerRequest.getUsername()); // 默认昵称等于用户名
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setCreatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());
        user.setFishBalance(0);
        user.setLastDailyClaim(null);

        userMapper.insert(user);
        return user;
    }

    /**
     * 用户登录
     * 
     * @param username 用户名
     * @param rawPassword 原始密码（未加密）
     * @return JWT令牌
     */
    public String login(String username, String rawPassword) {
        User userInDb = userMapper.findByUsername(username);
        if (userInDb != null && passwordEncoder.matches(rawPassword, userInDb.getPassword())) {
            return jwtUtil.generateToken(userInDb.getUsername());
        }
        throw new RuntimeException("用户名或密码错误");
    }

    /**
     * 根据用户名获取用户资料
     * 
     * @param username 用户名
     * @return 用户资料响应对象
     */
    public UserProfileResponse getUserInfoByUsername(String username) {
        User userInDb = userMapper.findByUsername(username);
        if (userInDb == null) {
            throw new RuntimeException("用户 '" + username + "' 不存在");
        }
        
        UserProfileResponse userProfileResponse = new UserProfileResponse();
        userProfileResponse.setId(userInDb.getId());
        userProfileResponse.setUsername(userInDb.getUsername());
        userProfileResponse.setNickname(userInDb.getNickname());
        userProfileResponse.setEmail(userInDb.getEmail());
        userProfileResponse.setCreatedAt(userInDb.getCreatedAt());
        userProfileResponse.setAvatarUrl(userInDb.getAvatarUrl());
        userProfileResponse.setFishBalance(userInDb.getFishBalance());

        LocalDate today = LocalDate.now();
        userProfileResponse.setCanClaimDaily(userInDb.getLastDailyClaim() == null || !userInDb.getLastDailyClaim().isEqual(today));

        return userProfileResponse;
    }

    /**
     * 根据昵称获取用户资料
     * 
     * @param nickname 用户昵称
     * @return 用户资料响应对象
     */
    public UserProfileResponse getUserInfoByNickname(String nickname) {
        User userInDb = userMapper.findByNickname(nickname);
        if (userInDb == null) {
            throw new RuntimeException("昵称为 '" + nickname + "' 的用户不存在");
        }
        return getUserInfoByUsername(userInDb.getUsername());
    }

    /**
     * 更新用户资料
     * 
     * @param username 用户名
     * @param updateUserProfileRequest 包含更新信息的请求对象
     * @return 更新后的用户资料响应对象
     */
    public UserProfileResponse updateUserInfo(String username, UpdateUserProfileRequest updateUserProfileRequest) {
        User userInDb = userMapper.findByUsername(username);
        if (userInDb == null) {
            throw new RuntimeException("找不到用户 " + username);
        }
        
        if (updateUserProfileRequest.getNickname() != null) {
            userInDb.setNickname(updateUserProfileRequest.getNickname());
        }
        
        if (updateUserProfileRequest.getEmail() != null) {
            userInDb.setEmail(updateUserProfileRequest.getEmail());
        }
        
        userInDb.setUpdatedAt(LocalDateTime.now());
        userMapper.updateProfile(userInDb);
        
        return getUserInfoByUsername(username);
    }

    /**
     * 更新用户头像
     * 
     * @param username 用户名
     * @param avatarFile 头像文件
     * @return 新头像的URL
     */
    public String updateAvatar(String username, MultipartFile avatarFile) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        String oldAvatarUrl = user.getAvatarUrl();
        String newAvatarUrl = tencentCosService.uploadFile(avatarFile, "avatars/");

        user.setAvatarUrl(newAvatarUrl);
        userMapper.updateProfile(user);

        // 在数据库更新成功后，删除旧的文件
        if (oldAvatarUrl != null && !oldAvatarUrl.isEmpty()) {
            tencentCosService.deleteFile(oldAvatarUrl);
        }

        return newAvatarUrl;
    }

    /**
     * 修改用户密码
     * 
     * @param username 用户名
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    public void changePassword(String username, String oldPassword, String newPassword) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("旧密码错误！");
        }
        
        String newHashedPassword = passwordEncoder.encode(newPassword);
        user.setPassword(newHashedPassword);
        userMapper.updateProfile(user);
    }

    /**
     * 根据关键词搜索用户
     * 
     * @param keyword 搜索关键词
     * @param page 页码
     * @param size 每页大小
     * @return 分页用户列表
     */
    public PageInfo<User> searchUsers(String keyword, int page, int size) {
        PageHelper.startPage(page, size);
        List<User> users = userMapper.searchByNickName(keyword);
        return new PageInfo<>(users);
    }

    /**
     * 领取每日鱼币奖励
     * 
     * @param username 用户名
     */
    public void claimDailyFish(String username) {
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        
        LocalDate today = LocalDate.now();
        if (user.getLastDailyClaim() != null && user.getLastDailyClaim().isEqual(today)) {
            throw new RuntimeException("今天已经领取过了，请明天再来！");
        }
        
        // 每日奖励10鱼币
        int dailyRewardAmount = 10;
        int newBalance = user.getFishBalance() + dailyRewardAmount;
        userMapper.updateUserFish(user.getId(), newBalance, today);
    }
    
    /**
     * 检查邮箱是否已被注册
     * 
     * @param email 邮箱地址
     * @return 如果已注册返回true，否则返回false
     */
    public boolean isEmailRegistered(String email) {
        return userMapper.findByEmail(email) != null;
    }
}