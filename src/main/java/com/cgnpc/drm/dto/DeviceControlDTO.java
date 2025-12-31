package com.cgnpc.drm.dto;

import lombok.Data;

@Data
public class DeviceControlDTO {
    /**
     * 风扇状态：true-开启，false-关闭
     */
    private Boolean fanStatus;
    
    /**
     * 设备状态：true-开启，false-关闭
     */
    private Boolean deviceStatus;
    
    /**
     * 锁定状态：true-锁定，false-解锁
     */
    private Boolean lockStatus;
    
    /**
     * 灯光状态：true-开启，false-关闭
     */
    private Boolean lightStatus;
    
    /**
     * 风扇速度：0-100之间的数值
     */
    private Integer fanSpeed;
    
    /**
     * 精油名称
     */
    private String essentialOilName;
    
    /**
     * 精油量：0-100之间的数值
     */
    private Integer essentialOilLevel;
    
    /**
     * 当前工作模式ID
     */
    private Long currentModeId;
}