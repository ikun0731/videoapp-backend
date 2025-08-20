package com.example.videoapp.service;

import com.example.videoapp.config.RabbitMQConfig;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

/**
 * 视频处理服务，负责异步处理视频相关任务
 */
@Service
public class VideoProcessingService {

    /**
     * 处理视频异步任务
     * 使用RabbitListener注解监听指定的消息队列
     * 
     * @param videoId 视频ID，由消息队列传入
     */
    @RabbitListener(queues = RabbitMQConfig.VIDEO_QUEUE_NAME)
    public void handleVideoProcess(Long videoId) {
        System.out.println("======================================================");
        System.out.println("【消费者】收到新的视频处理任务！视频ID: " + videoId);
        System.out.println("======================================================");

        // 模拟耗时的视频处理任务
        try {
            // 模拟视频转码
            System.out.println("--> 正在进行视频转码 (模拟耗时 5 秒)...");
            Thread.sleep(5000);
            System.out.println("--> 视频转码完成。");

            // 模拟视频截图
            System.out.println("--> 正在进行视频截图 (模拟耗时 2 秒)...");
            Thread.sleep(2000);
            System.out.println("--> 视频截图完成。");

            // 这里可以添加实际的视频处理逻辑，如更新数据库中的视频状态
            System.out.println("所有处理任务完成，视频ID: " + videoId);

        } catch (InterruptedException e) {
            System.err.println("视频处理任务被中断，视频ID: " + videoId);
            // 恢复中断状态
            Thread.currentThread().interrupt();
        }
    }
}