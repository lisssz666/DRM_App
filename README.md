# DRM Sprayer Management System

## 项目简介

DRM喷雾机管理系统是一个用于控制、存储和查看喷香机设备的后端服务系统。系统采用Spring Boot框架，提供完整的RESTful API接口，支持设备参数控制、工作模式管理、精油信息管理等功能。

## 功能特性

- **设备管理**: 实时监控和控制设备状态
- **精油管理**: 支持修改精油名称和查看剩余量
- **工作模式**: 灵活配置设备工作模式和时间
- **风扇控制**: 支持风扇开关和速度调节
- **设备锁定**: 防止误操作

## 技术栈

- **框架**: Spring Boot 2.7.x
- **ORM**: Spring Data JPA + MyBatis Plus
- **数据库**: MySQL 8.0+
- **构建工具**: Maven
- **编程语言**: Java 8+

## 项目结构

```
DRM_App/
├── src/
│   ├── main/
│   │   ├── java/com/cgnpc/drm/
│   │   │   ├── controller/       # API控制器
│   │   │   ├── entity/          # 数据库实体
│   │   │   ├── repository/      # 数据访问层
│   │   │   ├── service/         # 业务逻辑层
│   │   │   │   └── impl/        # 业务实现
│   │   │   └── Dto/             # 数据传输对象
│   │   └── resources/
│   │       ├── application.yml  # 配置文件
│   │       └── mapper/          # MyBatis映射文件
│   └── test/                    # 测试文件
├── database/                    # 数据库脚本
├── docs/                        # 文档
├── pom.xml                      # Maven配置
└── README.md
```

## 快速开始

### 1. 环境要求

- JDK 1.8+
- MySQL 8.0+
- Maven 3.6+

### 2. 数据库配置

```sql
-- 创建数据库
CREATE DATABASE drm_sprayer;

-- 执行初始化脚本
source /path/to/database/init.sql
```

### 3. 配置文件修改

编辑 `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/drm_sprayer?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=GMT%2B8
    username: [your-username]
    password: [your-password]
```

### 4. 运行项目

```bash
mvn spring-boot:run
```

或使用IDE运行 `DrmSprayerApplication` 类

## API 文档

完整的接口清单请查看 `docs/api_list.md`

## 数据库表结构

- `device`: 设备基本信息表
- `working_mode`: 工作模式配置表

详细表结构请查看 `database/init.sql`

## 许可证

内部项目使用