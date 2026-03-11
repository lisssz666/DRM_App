-- DRM Sprayer Database Initialization Script
-- 创建数据库
CREATE DATABASE IF NOT EXISTS drm_sprayer DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE drm_sprayer;

-- 创建设备表
CREATE TABLE IF NOT EXISTS `device` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` varchar(50) NOT NULL COMMENT '设备ID',
  `device_name` varchar(100) DEFAULT NULL COMMENT '设备名称',
  `essential_oil_name` varchar(100) DEFAULT NULL COMMENT '精油名称',
  `essential_oil_level` int(11) DEFAULT 100 COMMENT '精油量(0-100%)',
  `fan_status` tinyint(1) DEFAULT 0 COMMENT '风扇开关: 0=关, 1=开',
  `device_status` tinyint(1) DEFAULT 0 COMMENT '设备开关: 0=关, 1=开',
  `lock_status` tinyint(1) DEFAULT 0 COMMENT '锁定状态: 0=解锁, 1=锁定',
  `fan_speed` int(11) DEFAULT 50 COMMENT '风扇速度(0-100)',
  `current_mode_id` bigint(20) DEFAULT NULL COMMENT '当前工作模式ID',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备信息表';

-- 创建工作模式表
CREATE TABLE IF NOT EXISTS `working_mode` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `device_id` varchar(50) NOT NULL COMMENT '关联设备ID',
  `mode_name` varchar(100) DEFAULT NULL COMMENT '模式名称',
  `week_days` varchar(50) DEFAULT NULL COMMENT '工作周几，逗号分隔',
  `start_time` varchar(5) DEFAULT '00:00' COMMENT '开始时间HH:mm',
  `end_time` varchar(5) DEFAULT '23:59' COMMENT '结束时间HH:mm',
  `run_time` int(11) DEFAULT 300 COMMENT '运行时间(秒)',
  `stop_time` int(11) DEFAULT 900 COMMENT '停止时间(秒)',
  `is_default` tinyint(1) DEFAULT 0 COMMENT '是否默认模式',
  `status` tinyint(1) DEFAULT 1 COMMENT '模式状态: 0=禁用, 1=启用',
  `created_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  KEY `idx_device_id` (`device_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='工作模式表';

-- 创建用户表
CREATE TABLE IF NOT EXISTS `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `email` varchar(100) NOT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `password` varchar(255) NOT NULL COMMENT '密码（加密存储）',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `status` int(11) NOT NULL DEFAULT 1 COMMENT '用户状态：0-禁用，1-启用',
  `create_time` datetime NOT NULL COMMENT '创建时间',
  `update_time` datetime NOT NULL COMMENT '更新时间',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_email` (`email`),
  UNIQUE KEY `uk_phone` (`phone`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 初始化示例数据
INSERT INTO `device` (`device_id`, `device_name`, `essential_oil_name`, `essential_oil_level`, `fan_status`, `device_status`, `lock_status`, `fan_speed`) VALUES
('P205', '喷雾机设备', '柠檬精油', 80, 1, 1, 0, 50);

INSERT INTO `working_mode` (`device_id`, `mode_name`, `week_days`, `start_time`, `end_time`, `run_time`, `stop_time`, `is_default`, `status`) VALUES
('P205', 'Working Mode 1', 'Mon,Tue,Wed,Thu,Fri', '08:00', '22:00', 300, 900, 1, 1);