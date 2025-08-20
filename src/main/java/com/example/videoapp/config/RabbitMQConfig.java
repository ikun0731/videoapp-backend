package com.example.videoapp.config;

import jakarta.annotation.PostConstruct;
import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * RabbitMQ配置类，负责声明和配置消息队列相关的基础设施
 */
@Configuration
public class RabbitMQConfig {

    /**
     * 视频处理相关的交换机、队列和路由键名称
     */
    public static final String VIDEO_EXCHANGE_NAME = "video.exchange";
    public static final String VIDEO_QUEUE_NAME = "video.process.queue";
    public static final String VIDEO_ROUTING_KEY = "video.process";

    /**
     * 通知服务相关的交换机、队列和路由键名称
     */
    public static final String NOTIFICATION_EXCHANGE_NAME = "notification.exchange";
    public static final String NOTIFICATION_QUEUE_NAME = "notification.queue";
    public static final String NOTIFICATION_ROUTING_KEY = "notification.new";

    /**
     * Spring AMQP的管理工具
     */
    @Autowired
    private AmqpAdmin amqpAdmin;

    /**
     * 在Bean初始化后自动声明RabbitMQ的交换机、队列和绑定关系
     */
    @PostConstruct
    public void declareInfrastructure() {
        // 声明视频处理相关的基础设施
        DirectExchange videoExchange = new DirectExchange(VIDEO_EXCHANGE_NAME);
        Queue videoQueue = new Queue(VIDEO_QUEUE_NAME, true);
        Binding videoBinding = BindingBuilder.bind(videoQueue).to(videoExchange).with(VIDEO_ROUTING_KEY);
        amqpAdmin.declareExchange(videoExchange);
        amqpAdmin.declareQueue(videoQueue);
        amqpAdmin.declareBinding(videoBinding);

        // 声明通知服务相关的基础设施
        DirectExchange notificationExchange = new DirectExchange(NOTIFICATION_EXCHANGE_NAME);
        Queue notificationQueue = new Queue(NOTIFICATION_QUEUE_NAME, true);
        Binding notificationBinding = BindingBuilder.bind(notificationQueue).to(notificationExchange).with(NOTIFICATION_ROUTING_KEY);
        amqpAdmin.declareExchange(notificationExchange);
        amqpAdmin.declareQueue(notificationQueue);
        amqpAdmin.declareBinding(notificationBinding);
    }
    
    /**
     * 配置消息转换器，用于对象和JSON之间的转换
     * 
     * @return JSON消息转换器
     */
    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}