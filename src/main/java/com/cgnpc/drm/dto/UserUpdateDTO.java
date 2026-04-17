package com.cgnpc.drm.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 用户信息更新DTO
 */
@Data
public class UserUpdateDTO {
    /**
     * 用户昵称
     */
    private String nickname;

    /**
     * 用户头像URL
     */
    private String avatar;

    /**
     * 用户头像文件
     */
    private MultipartFile avatarFile;
}