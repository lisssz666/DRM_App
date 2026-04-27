package com.cgnpc.drm.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

/**
 * 反馈问题DTO
 */
@Data
public class FeedbackDTO {

    /**
     * 问题和反馈内容
     */
    @NotBlank(message = "反馈内容不能为空")
    @Size(max = 200, message = "反馈内容不能超过200个字符")
    private String content;

    /**
     * 联系方式
     */
    @Size(max = 100, message = "联系方式不能超过100个字符")
    private String contact;
}
