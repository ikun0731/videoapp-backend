package com.example.videoapp.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    // Spring Boot Starter Mail 自动为我们配置好的邮件发送器
    @Autowired
    private JavaMailSender mailSender;

    // 从 application.properties 中获取发件人邮箱地址
    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * 发送一封简单的文本邮件
     * @param to 收件人地址
     * @param subject 邮件主题
     * @param content 邮件正文
     */
    @Async // 【核心】将这个方法标记为异步。调用它会立刻返回，而邮件的发送将在后台线程中完成。
    public void sendSimpleMail(String to, String subject, String content) {
        long startTime = System.currentTimeMillis();
        System.out.println("开始发送邮件...");
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail); // 设置发件人
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message); // 执行发送

            long endTime = System.currentTimeMillis();
            System.out.println("邮件发送成功到 " + to + "，耗时: " + (endTime - startTime) + "ms");
        } catch (Exception e) {
            // 在真实项目中，这里应该使用更完善的日志框架
            System.err.println("发送邮件到 " + to + " 时发生错误");
            e.printStackTrace();
        }
    }
}