package com.cgnpc.drm.service.impl;

import com.cgnpc.drm.service.FileUploadService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

/**
 * 文件上传服务实现类
 */
@Service
public class FileUploadServiceImpl implements FileUploadService {

    private static final Logger logger = LoggerFactory.getLogger(FileUploadServiceImpl.class);

    @Value("${file.upload.path:/opt/drm-sprayer/uploads}")
    private String uploadPath;

    @Value("${file.upload.url-prefix:/uploads}")
    private String urlPrefix;

    @Value("${file.upload.base-url:http://127.0.0.1}")
    private String baseUrl;

    @Value("${server.port:8080}")
    private String serverPort;

    @Value("${file.upload.max-size:5242880}")
    private long maxFileSize;

    @Override
    public String uploadAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File cannot be empty.");
        }

        // 检查文件大小
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("File size exceeds the limit (max 5MB).");
        }

        // 检查文件类型
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new RuntimeException("Only image files are allowed.");
        }

        try {
            // 创建上传目录
            String datePath = new SimpleDateFormat("yyyy/MM/dd").format(new Date());
            String avatarPath = uploadPath + "/avatars/" + datePath;
            Path path = Paths.get(avatarPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }

            // 生成文件名
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + extension;

            // 保存文件
            String filePath = avatarPath + "/" + filename;
            file.transferTo(new File(filePath));

            // 生成访问URL
            String fileUrl = baseUrl + ":" + serverPort + urlPrefix + "/avatars/" + datePath + "/" + filename;

            logger.info("Avatar uploaded successfully: {}", fileUrl);
            return fileUrl;
        } catch (IOException e) {
            logger.error("Failed to upload avatar: {}", e.getMessage());
            throw new RuntimeException("Failed to upload avatar: " + e.getMessage());
        }
    }

    @Override
    public boolean deleteFile(String filePath) {
        try {
            if (filePath == null || filePath.isEmpty()) {
                return false;
            }

            // 从URL中提取文件路径
            String relativePath = filePath.substring(filePath.indexOf(urlPrefix) + urlPrefix.length());
            String fullPath = uploadPath + relativePath;

            File file = new File(fullPath);
            if (file.exists()) {
                boolean deleted = file.delete();
                if (deleted) {
                    logger.info("File deleted successfully: {}", fullPath);
                } else {
                    logger.warn("Failed to delete file: {}", fullPath);
                }
                return deleted;
            } else {
                logger.warn("File does not exist: {}", fullPath);
                return false;
            }
        } catch (Exception e) {
            logger.error("Failed to delete file: {}", e.getMessage());
            return false;
        }
    }
}