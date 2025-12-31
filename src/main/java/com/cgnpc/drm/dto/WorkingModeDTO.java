package com.cgnpc.drm.dto;

import lombok.Data;
import javax.validation.constraints.NotNull;

@Data
public class WorkingModeDTO {
    /**
     * 工作模式ID
     */
    private Long id;
    
    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    private String deviceId;
    
    /**
     * 模式名称
     */
    private String modeName;
    
    /**
     * 运行星期：格式为逗号分隔的数字，如"1,2,3"表示周一到周三
     */
    private String weekDays;
    
    /**
     * 开始时间：格式为HH:mm，如"08:30"
     */
    private String startTime;
    
    /**
     * 结束时间：格式为HH:mm，如"17:30"
     */
    private String endTime;
    
    /**
     * 运行时间：单位为分钟
     */
    private Integer runTime;
    
    /**
     * 停止时间：单位为分钟
     */
    private Integer stopTime;
    
    /**
     * 是否默认模式：true-是，false-否
     */
    private Boolean isDefault;
    
    /**
     * 模式状态：true-启用，false-禁用
     */
    private Boolean status;
}