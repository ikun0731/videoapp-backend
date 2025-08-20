package com.example.videoapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.spring.cache.RedissonSpringCacheManager;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redisson配置类，配置Redisson的缓存管理器
 */
@Configuration
@EnableCaching
public class RedissonConfig {

    /**
     * 创建并配置RedissonSpringCacheManager
     * 
     * @param redissonClient Redisson客户端
     * @return 配置好的缓存管理器
     */
    @Bean
    public CacheManager cacheManager(RedissonClient redissonClient) {
        // 创建Jackson的ObjectMapper，支持Java 8时间类型和类型信息保留
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL
        );
        
        // 创建JSON编码器
        JsonJacksonCodec codec = new JsonJacksonCodec(objectMapper);

        // 创建Redisson缓存管理器
        RedissonSpringCacheManager cacheManager = new RedissonSpringCacheManager(redissonClient);

        // 设置JSON编码器
        cacheManager.setCodec(codec);

        return cacheManager;
    }
}