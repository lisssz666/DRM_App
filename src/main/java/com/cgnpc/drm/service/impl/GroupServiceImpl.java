package com.cgnpc.drm.service.impl;

import com.cgnpc.drm.entity.Group;
import com.cgnpc.drm.entity.DeviceGroup;
import com.cgnpc.drm.entity.Device;
import com.cgnpc.drm.repository.GroupRepository;
import com.cgnpc.drm.repository.DeviceGroupRepository;
import com.cgnpc.drm.repository.DeviceRepository;
import com.cgnpc.drm.service.GroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class GroupServiceImpl implements GroupService {

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private DeviceGroupRepository deviceGroupRepository;

    @Autowired
    private DeviceRepository deviceRepository;

    @Override
    public Group createGroup(Group group) {
        return groupRepository.save(group);
    }

    @Override
    public Group updateGroup(Long id, Group group) {
        Group existingGroup = groupRepository.findByIdAndUserId(id, group.getUserId());
        if (existingGroup == null) {
            throw new RuntimeException("Group does not exist or not owned by current user.");
        }
        existingGroup.setGroupName(group.getGroupName());
        existingGroup.setDescription(group.getDescription());
        return groupRepository.save(existingGroup);
    }

    @Override
    public boolean deleteGroup(Long id, Long userId) {
        Group group = groupRepository.findByIdAndUserId(id, userId);
        if (group == null) {
            throw new RuntimeException("Group does not exist or not owned by current user.");
        }
        // 批量删除设备关联
        deviceGroupRepository.deleteByGroupIdAndUserId(id, userId);
        // 删除分组
        groupRepository.delete(group);
        return true;
    }

    @Override
    public List<Group> getGroupsByUserId(Long userId) {
        List<Group> groups = groupRepository.findByUserId(userId);
        
        // 检查是否已存在默认分组
        boolean hasDefaultGroup = groups.stream().anyMatch(g -> g.getIsDefault() != null && g.getIsDefault());
        
        // 如果不存在默认分组，则创建
        if (!hasDefaultGroup) {
            initializeDefaultGroup(userId);
            groups = groupRepository.findByUserId(userId);
        }
        
        return groups;
    }

    @Override
    public Group getGroupById(Long id, Long userId) {
        Group group = groupRepository.findByIdAndUserId(id, userId);
        if (group == null) {
            throw new RuntimeException("Group does not exist or not owned by current user.");
        }
        return group;
    }

    @Override
    public DeviceGroup addDeviceToGroup(String deviceId, Long groupId, Long userId) {
        // 检查分组是否存在且属于当前用户
        Group group = groupRepository.findByIdAndUserId(groupId, userId);
        if (group == null) {
            throw new RuntimeException("Group does not exist or not owned by current user.");
        }
        // 先移除设备的现有分组关联
        DeviceGroup existingRelation = deviceGroupRepository.findByDeviceIdAndUserId(deviceId, userId);
        if (existingRelation != null) {
            deviceGroupRepository.delete(existingRelation);
        }
        // 创建新的关联关系
        DeviceGroup deviceGroup = new DeviceGroup();
        deviceGroup.setDeviceId(deviceId);
        deviceGroup.setGroupId(groupId);
        deviceGroup.setUserId(userId);
        return deviceGroupRepository.save(deviceGroup);
    }

    @Override
    public boolean removeDeviceFromGroup(String deviceId, Long userId) {
        deviceGroupRepository.deleteByDeviceIdAndUserId(deviceId, userId);
        return true;
    }

    @Override
    public DeviceGroup moveDeviceToGroup(String deviceId, Long groupId, Long userId) {
        // 检查分组是否存在且属于当前用户
        Group group = groupRepository.findByIdAndUserId(groupId, userId);
        if (group == null) {
            throw new RuntimeException("Group does not exist or not owned by current user.");
        }
        // 先移除设备的现有分组关联
        deviceGroupRepository.deleteByDeviceIdAndUserId(deviceId, userId);
        // 创建新的关联关系
        DeviceGroup deviceGroup = new DeviceGroup();
        deviceGroup.setDeviceId(deviceId);
        deviceGroup.setGroupId(groupId);
        deviceGroup.setUserId(userId);
        return deviceGroupRepository.save(deviceGroup);
    }

    @Override
    public List<String> getDevicesInGroup(Long groupId, Long userId) {
        // 如果groupId为null，返回用户的所有设备
        if (groupId == null) {
            List<Device> devices = deviceRepository.findByUserId(userId);
            return devices.stream()
                    .map(Device::getDeviceId)
                    .collect(Collectors.toList());
        }
        
        // 检查是否为默认分组（All分组）
        Group group = groupRepository.findByIdAndUserId(groupId, userId);
        if (group != null && group.getIsDefault() != null && group.getIsDefault()) {
            // 如果是默认分组，返回用户的所有设备
            List<Device> devices = deviceRepository.findByUserId(userId);
            return devices.stream()
                    .map(Device::getDeviceId)
                    .collect(Collectors.toList());
        }
        
        // 否则返回指定分组内的设备
        List<DeviceGroup> deviceGroups = deviceGroupRepository.findByGroupIdAndUserId(groupId, userId);
        return deviceGroups.stream()
                .map(DeviceGroup::getDeviceId)
                .collect(Collectors.toList());
    }

    @Override
    public void initializeDefaultGroup(Long userId) {
        // 检查是否已存在默认分组
        List<Group> existingGroups = groupRepository.findByUserId(userId);
        boolean hasDefaultGroup = existingGroups.stream()
                .anyMatch(g -> g.getIsDefault() != null && g.getIsDefault());
        
        // 如果不存在，则创建默认分组
        if (!hasDefaultGroup) {
            Group defaultGroup = new Group();
            defaultGroup.setGroupName("All");
            defaultGroup.setUserId(userId);
            defaultGroup.setIsDefault(true);
            defaultGroup.setDescription("All devices");
            groupRepository.save(defaultGroup);
        }
    }
}