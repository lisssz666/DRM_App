package com.cgnpc.drm.service;

import com.cgnpc.drm.entity.Group;
import com.cgnpc.drm.entity.DeviceGroup;
import java.util.List;

public interface GroupService {
    /**
     * 创建分组
     * @param group 分组信息
     * @return 创建的分组
     */
    Group createGroup(Group group);

    /**
     * 更新分组
     * @param id 分组ID
     * @param group 分组信息
     * @return 更新后的分组
     */
    Group updateGroup(Long id, Group group);

    /**
     * 删除分组
     * @param id 分组ID
     * @param userId 用户ID
     * @return 删除结果
     */
    boolean deleteGroup(Long id, Long userId);

    /**
     * 获取用户的所有分组
     * @param userId 用户ID
     * @return 分组列表
     */
    List<Group> getGroupsByUserId(Long userId);

    /**
     * 获取分组详情
     * @param id 分组ID
     * @param userId 用户ID
     * @return 分组信息
     */
    Group getGroupById(Long id, Long userId);

    /**
     * 添加设备到分组
     * @param deviceId 设备ID
     * @param groupId 分组ID
     * @param userId 用户ID
     * @return 关联信息
     */
    DeviceGroup addDeviceToGroup(String deviceId, Long groupId, Long userId);

    /**
     * 从分组移除设备
     * @param deviceId 设备ID
     * @param userId 用户ID
     * @return 移除结果
     */
    boolean removeDeviceFromGroup(String deviceId, Long userId);

    /**
     * 移动设备到其他分组
     * @param deviceId 设备ID
     * @param groupId 新分组ID
     * @param userId 用户ID
     * @return 关联信息
     */
    DeviceGroup moveDeviceToGroup(String deviceId, Long groupId, Long userId);

    /**
     * 获取分组内的设备
     * @param groupId 分组ID（如果为null，则返回用户的所有设备）
     * @param userId 用户ID
     * @return 设备列表
     */
    List<String> getDevicesInGroup(Long groupId, Long userId);

    /**
     * 初始化用户的默认分组（All分组）
     * @param userId 用户ID
     */
    void initializeDefaultGroup(Long userId);
}