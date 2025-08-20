package com.example.videoapp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/**
 * Redis缓存配置类，配置Redis的序列化方式和缓存策略
 */
@Configuration
@EnableCaching
public class RedisConfig {

    /**
     * 配置Redis缓存
     * 
     * @return Redis缓存配置
     */
    @Bean
    public RedisCacheConfiguration redisCacheConfiguration() {
        // 创建一个专门用于Redis的ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();

        // 添加JavaTimeModule模块，使ObjectMapper支持LocalDateTime等Java 8时间类型
        objectMapper.registerModule(new JavaTimeModule());

        // 启用默认的类型推断，在JSON中记录对象的类型信息，以便正确反序列化
        BasicPolymorphicTypeValidator typeValidator = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();
        objectMapper.activateDefaultTyping(typeValidator, ObjectMapper.DefaultTyping.NON_FINAL);

        // 使用定制的ObjectMapper创建JSON序列化器
        GenericJackson2JsonRedisSerializer redisSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);

        // 构建并返回Redis缓存配置
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))  // 设置缓存有效期为30分钟
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(redisSerializer))
                .disableCachingNullValues();  // 不缓存null值
    }
}