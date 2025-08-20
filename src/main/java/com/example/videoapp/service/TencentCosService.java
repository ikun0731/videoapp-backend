package com.example.videoapp.service;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class TencentCosService {

    // 1. 从配置文件中注入我们的密钥和存储桶信息
    @Value("${tencent.cos.secretId}")
    private String secretId;
    @Value("${tencent.cos.secretKey}")
    private String secretKey;
    @Value("${tencent.cos.bucketName}")
    private String bucketName;
    @Value("${tencent.cos.region}")
    private String region;

    // 2. 声明一个 COS 客户端对象
    private COSClient cosClient;

    // 3. @PostConstruct 注解能让这个方法在 Service 对象创建后，自动执行一次，用于初始化
    @PostConstruct
    public void init() {
        // a. 创建身份验证信息
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        // b. 设置存储桶所在的地域
        ClientConfig clientConfig = new ClientConfig(new Region(region));
        // c. 生成 COS 客户端
        this.cosClient = new COSClient(cred, clientConfig);
        //System.out.println("Tencent COS Client initialized successfully.");
    }

    /**
     * 核心方法：上传文件到腾讯云 COS
     * @param file 用户上传的文件
     * @param directory 在存储桶中要保存的目录 (例如 "videos/", "covers/", "avatars/")
     * @return 返回文件上传后在云上的完整访问 URL
     */
    public String uploadFile(MultipartFile file, String directory) {
        try {
            // a. 为文件生成一个唯一的名称，避免重名覆盖
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String uniqueFileName = UUID.randomUUID().toString() + extension;
            String key = directory + uniqueFileName; // 最终在COS中的完整路径，例如 "videos/uuid-xxx.mp4"

            // b. 设置对象的元数据，比如文件大小和类型
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(file.getSize());
            objectMetadata.setContentType(file.getContentType());

            // c. 创建上传请求
            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    bucketName,
                    key,
                    file.getInputStream(),
                    objectMetadata
            );

            // d. 执行上传
            cosClient.putObject(putObjectRequest);

            // e. 拼接并返回文件的公网访问 URL
            // 格式: https://<BucketName-APPID>.cos.<Region>.myqcloud.com/<Key>
            // BucketName 中可能已经包含了 APPID，具体格式请在腾讯云控制台的“存储桶列表”页确认
            return "https://" + bucketName + ".cos." + region + ".myqcloud.com/" + key;

        } catch (IOException | CosClientException e) {
            // 在实际项目中，这里应该记录更详细的错误日志
            e.printStackTrace();
            throw new RuntimeException("文件上传到腾讯云COS失败", e);
        }
    }

    /**
     * 【新增】从腾讯云 COS 删除一个文件
     * @param fileUrl 文件的完整公网访问 URL
     */
    public void deleteFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        final String cosDomain = ".myqcloud.com/";
        int domainIndex = fileUrl.indexOf(cosDomain);

        if (domainIndex == -1) {
            System.err.println("无法解析的 COS URL 格式: " + fileUrl);
            return;
        }

        String key = fileUrl.substring(domainIndex + cosDomain.length());

        try {
            cosClient.deleteObject(bucketName, key);
            System.out.println("成功删除 COS 文件: " + key);
        } catch (CosClientException e) {
            System.err.println("删除 COS 文件失败: " + key);
            e.printStackTrace();
        }
    }

    // 4. @PreDestroy 注解能让这个方法在应用关闭前，自动执行一次，用于清理资源
    @PreDestroy
    public void destroy() {
        if (cosClient != null) {
            cosClient.shutdown();
            System.out.println("Tencent COS Client shut down.");
        }
    }
}