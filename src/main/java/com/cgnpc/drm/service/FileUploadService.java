package com.cgnpc.drm.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传服务接口
 */
public interface FileUploadService {
    /**
     * 上传用户头像
     * @param file 头像文件
     * @return 头像文件的访问URL
     */
    String uploadAvatar(MultipartFile file);

    /**
     * 删除文件
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    boolean deleteFile(String filePath);
}