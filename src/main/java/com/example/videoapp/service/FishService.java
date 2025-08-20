package com.example.videoapp.service;

import com.example.videoapp.DTO.NotificationMessageDTO;
import com.example.videoapp.config.RabbitMQConfig;
import com.example.videoapp.entity.FishTransaction;
import com.example.videoapp.entity.User;
import com.example.videoapp.entity.Video;
import com.example.videoapp.mapper.FishTransactionMapper;
import com.example.videoapp.mapper.UserMapper;
import com.example.videoapp.mapper.VideoMapper;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

/**
 * 鱼币服务，处理用户鱼币相关的业务逻辑
 */
@Service
public class FishService {

    @Autowired
    private UserMapper userMapper;
    
    @Autowired
    private VideoMapper videoMapper;
    
    @Autowired
    private FishTransactionMapper fishTransactionMapper;
    
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Autowired
    private RedissonClient redissonClient;

    /**
     * 用户给视频投喂小鱼（点赞）
     * 
     * @param userId 用户ID
     * @param videoId 视频ID
     */
    @Transactional
    @CacheEvict(value = "video:detail", key = "#videoId")
    public void feedFishToVideo(Long userId, Long videoId) {
        // 创建分布式锁的键
        final String lockKey = "lock:user:feed:" + userId;
        RLock lock = redissonClient.getLock(lockKey);

        try {
            // 尝试获取锁，最多等待10秒，锁自动释放时间为10秒
            boolean isLocked = lock.tryLock(10, 10, TimeUnit.SECONDS);
            if (isLocked) {
                try {
                    // 检查用户是否存在
                    User user = userMapper.findById(userId);
                    if (user == null) { 
                        throw new RuntimeException("操作失败，用户不存在！"); 
                    }
                    
                    // 检查用户鱼币余额
                    if (user.getFishBalance() < 1) { 
                        throw new RuntimeException("小鱼余额不足！"); 
                    }

                    // 创建投喂交易记录
                    FishTransaction transaction = new FishTransaction();
                    transaction.setUserId(userId);
                    transaction.setVideoId(videoId);
                    transaction.setAmount(1);
                    transaction.setCreatedAt(LocalDateTime.now());
                    
                    try {
                        fishTransactionMapper.insert(transaction);
                    } catch (Exception e) {
                        throw new RuntimeException("您已经给这个视频喂过鱼了！");
                    }

                    // 更新用户鱼币余额和视频获得的鱼币数量
                    int newBalance = user.getFishBalance() - 1;
                    userMapper.updateUserFish(user.getId(), newBalance, user.getLastDailyClaim());
                    videoMapper.incrementFishCount(videoId);

                    // 发送通知给视频作者
                    Video video = videoMapper.findById(videoId);
                    if (video != null) {
                        NotificationMessageDTO notificationMessageDTO = new NotificationMessageDTO(
                                "NEW_FISH", 
                                userId, 
                                video.getUserId(), 
                                videoId,
                                user.getNickname(), 
                                video.getTitle());
                                
                        rabbitTemplate.convertAndSend(
                                RabbitMQConfig.NOTIFICATION_EXCHANGE_NAME,
                                RabbitMQConfig.NOTIFICATION_ROUTING_KEY, 
                                notificationMessageDTO);
                    }
                } finally {
                    // 确保锁被释放
                    lock.unlock();
                }
            } else {
                throw new RuntimeException("操作频繁，请稍后再试！");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("获取锁期间被中断", e);
        }
    }
}