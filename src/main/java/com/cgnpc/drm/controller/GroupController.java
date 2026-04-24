package com.cgnpc.drm.controller;

import com.cgnpc.drm.entity.Group;
import com.cgnpc.drm.entity.DeviceGroup;
import com.cgnpc.drm.service.GroupService;
import com.cgnpc.drm.service.UserService;
import com.cgnpc.drm.vo.ResponseVO;
import com.cgnpc.drm.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/group")
public class GroupController {

    @Autowired
    private GroupService groupService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * 创建分组
     * POST /api/group/create
     */
    @PostMapping("/create")
    public ResponseVO<Group> createGroup(@RequestParam Map<String, String> params, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        try {
            Group group = new Group();
            group.setUserId(userId);
            group.setGroupName(params.get("groupName"));
            group.setDescription(params.get("description"));
            
            Group createdGroup = groupService.createGroup(group);
            return ResponseVO.success("分组创建成功", createdGroup);
        } catch (Exception e) {
            return ResponseVO.error(500, "分组创建失败: " + e.getMessage());
        }
    }

    /**
     * 更新分组
     * PUT /api/group/update
     */
    @PutMapping("/update")
    public ResponseVO<Group> updateGroup(@RequestParam Long id, @RequestParam Map<String, String> params, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        try {
            Group group = new Group();
            group.setUserId(userId);
            group.setGroupName(params.get("groupName"));
            group.setDescription(params.get("description"));
            
            Group updatedGroup = groupService.updateGroup(id, group);
            return ResponseVO.success("分组更新成功", updatedGroup);
        } catch (Exception e) {
            return ResponseVO.error(500, "分组更新失败: " + e.getMessage());
        }
    }

    /**
     * 删除分组
     * DELETE /api/group/delete
     */
    @DeleteMapping("/delete")
    public ResponseVO<Boolean> deleteGroup(@RequestParam Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        try {
            boolean result = groupService.deleteGroup(id, userId);
            return ResponseVO.success("分组删除成功", result);
        } catch (Exception e) {
            return ResponseVO.error(500, "分组删除失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的所有分组
     * GET /api/group/getGroups
     */
    @GetMapping("/getGroups")
    public ResponseVO<List<Group>> getGroups(HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        try {
            List<Group> groups = groupService.getGroupsByUserId(userId);
            return ResponseVO.success("获取分组列表成功", groups);
        } catch (Exception e) {
            return ResponseVO.error(500, "获取分组列表失败: " + e.getMessage());
        }
    }

    /**
     * 获取分组详情
     * GET /api/group/getGroup
     */
    @GetMapping("/getGroup")
    public ResponseVO<Group> getGroup(@RequestParam Long id, HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        try {
            Group group = groupService.getGroupById(id, userId);
            return ResponseVO.success("获取分组详情成功", group);
        } catch (Exception e) {
            return ResponseVO.error(500, "获取分组详情失败: " + e.getMessage());
        }
    }

    /**
     * 设备分组操作（添加、移除、移动）
     * POST /api/group/deviceOperation
     */
    @PostMapping("/deviceOperation")
    public ResponseVO<?> deviceOperation(
            @RequestParam String deviceId,
            @RequestParam String operation, // add:添加到分组, remove:从分组移除, move:移动到其他分组
            @RequestParam(required = false) Long groupId,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        try {
            switch (operation) {
                case "add":
                    if (groupId == null) {
                        return ResponseVO.error(400, "添加设备到分组时必须指定groupId");
                    }
                    DeviceGroup addedDevice = groupService.addDeviceToGroup(deviceId, groupId, userId);
                    return ResponseVO.success("设备添加到分组成功", addedDevice);
                case "remove":
                    boolean removed = groupService.removeDeviceFromGroup(deviceId, userId);
                    return ResponseVO.success("设备从分组移除成功", removed);
                case "move":
                    if (groupId == null) {
                        return ResponseVO.error(400, "移动设备到分组时必须指定groupId");
                    }
                    DeviceGroup movedDevice = groupService.moveDeviceToGroup(deviceId, groupId, userId);
                    return ResponseVO.success("设备移动到分组成功", movedDevice);
                default:
                    return ResponseVO.error(400, "不支持的操作类型，支持的操作：add, remove, move");
            }
        } catch (Exception e) {
            return ResponseVO.error(500, "操作失败: " + e.getMessage());
        }
    }

    /**
     * 获取分组内的设备
     * GET /api/group/getDevicesInGroup
     */
    @GetMapping("/getDevicesInGroup")
    public ResponseVO<List<String>> getDevicesInGroup(
            @RequestParam(required = false) Long groupId,
            HttpServletRequest request) {
        Long userId = getCurrentUserId(request);
        try {
            List<String> deviceIds = groupService.getDevicesInGroup(groupId, userId);
            return ResponseVO.success("获取分组内设备成功", deviceIds);
        } catch (Exception e) {
            return ResponseVO.error(500, "获取分组内设备失败: " + e.getMessage());
        }
    }

    // 从请求中获取当前用户ID
    private Long getCurrentUserId(HttpServletRequest request) {
        String token = request.getHeader("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
            // 从token中提取用户名（邮箱或手机号）
            String username = jwtUtil.extractUsername(token);
            // 根据用户名获取用户对象（先尝试邮箱，再尝试手机号）
            com.cgnpc.drm.entity.User user = null;
            if (username.contains("@")) {
                // 邮箱登录
                user = userService.findByEmail(username);
            } else {
                // 手机号登录
                user = userService.findByPhone(username);
            }
            if (user != null) {
                return user.getId();
            }
        }
        throw new RuntimeException("User not logged in");
    }
}