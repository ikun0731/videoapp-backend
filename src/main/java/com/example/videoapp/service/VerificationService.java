package com.example.videoapp.service;

import org.redisson.api.RBucket;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务，负责生成、存储和验证邮箱验证码
 */
@Service
public class VerificationService {

    @Autowired
    private RedissonClient redissonClient;

    /**
     * 验证码在Redis中的Key前缀
     */
    private static final String VERIFICATION_CODE_KEY_PREFIX = "verification:code:";
    
    /**
     * 验证码有效期（分钟）
     */
    private static final int CODE_EXPIRATION_MINUTES = 5;

    /**
     * 生成、存储并返回一个6位数的验证码
     * 
     * @param email 用户的邮箱地址，将作为Key的一部分
     * @return 生成的6位数验证码
     */
    public String generateAndStoreCode(String email) {
        // 生成一个100000到999999之间的随机6位数
        String code = String.valueOf(100000 + new Random().nextInt(900000));

        // 构建在Redis中存储的Key
        String key = VERIFICATION_CODE_KEY_PREFIX + email;

        // 使用Redisson获取一个RBucket对象，它代表Redis中的一个String类型的键值对
        RBucket<String> bucket = redissonClient.getBucket(key);

        // 将验证码存入Redis，并设置有效期
        bucket.set(code, CODE_EXPIRATION_MINUTES, TimeUnit.MINUTES);

        System.out.println("为邮箱 " + email + " 生成的验证码是: " + code + " (有效期" + CODE_EXPIRATION_MINUTES + "分钟)");

        return code;
    }

    /**
     * 校验用户提交的验证码是否正确
     * 
     * @param email 用户的邮箱地址
     * @param code 用户提交的验证码
     * @return 验证码正确返回true，错误或已过期返回false
     */
    public boolean verifyCode(String email, String code) {
        if (email == null || code == null) {
            return false;
        }
        
        String key = VERIFICATION_CODE_KEY_PREFIX + email;
        RBucket<String> bucket = redissonClient.getBucket(key);

        // 从Redis中获取存储的验证码
        String storedCode = bucket.get();

        // 检查验证码是否存在且与用户提交的一致（忽略大小写）
        if (storedCode != null && storedCode.equalsIgnoreCase(code)) {
            // 验证成功后，立即删除该验证码，防止被重复使用
            bucket.delete();
            return true;
        }

        return false;
    }
}