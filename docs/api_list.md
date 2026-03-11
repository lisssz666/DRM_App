# DRM Sprayer API 接口清单

## 设备管理接口

### 1. 获取设备信息
- **URL**: GET /api/device/getDeviceInfo
- **描述**: 获取单个设备的详细信息
- **参数**:
  - `deviceId`: 设备唯一标识（通过请求参数传递）
- **返回**: 设备详细信息（Device 实体）

### 2. 设备控制
- **URL**: POST /api/device/controlDevice
- **描述**: 控制设备的开关状态
- **参数**:
  - `deviceId`: 设备唯一标识（通过请求参数传递）
  - `isOn`: 设备状态（true=开 启, false=关闭）
- **返回**: 操作结果

### 3. 修改精油名称
- **URL**: POST /api/device/updateOilName
- **描述**: 更新设备精油名称
- **参数**:
  - `deviceId`: 设备唯一标识（通过请求参数传递）
  - `oilName`: 新的精油名称
- **返回**: 更新后的设备信息

### 4. 更新精油量
- **URL**: POST /api/device/updateOilLevel
- **描述**: 更新设备精油剩余量
- **参数**:
  - `deviceId`: 设备唯一标识（通过请求参数传递）
  - `oilLevel`: 精油剩余量（百分比）
- **返回**: 更新后的设备信息

### 5. 锁定设备
- **URL**: POST /api/device/lockDevice
- **描述**: 锁定设备防止误操作
- **参数**:
  - `deviceId`: 设备唯一标识（通过请求参数传递）
- **返回**: 操作结果

### 6. 解锁设备
- **URL**: POST /api/device/unlockDevice
- **描述**: 解锁设备
- **参数**:
  - `deviceId`: 设备唯一标识（通过请求参数传递）
- **返回**: 操作结果

### 7. 风扇开关控制
- **URL**: POST /api/device/controlFan
- **描述**: 控制风扇开关
- **参数**:
  - `deviceId`: 设备唯一标识（通过请求参数传递）
  - `isOn`: 风扇状态（true=开启, false=关闭）
- **返回**: 操作结果

### 8. 设置风扇速度
- **URL**: POST /api/device/setFanSpeed
- **描述**: 设置风扇速度
- **参数**:
  - `deviceId`: 设备唯一标识（通过请求参数传递）
  - `speed`: 风扇速度（0-100）
- **返回**: 操作结果

---

## 用户管理接口

### 1. 用户登录
- **URL**: POST /api/user/login
- **描述**: 用户登录（支持密码登录和验证码登录）
- **参数**: 
  - 登录信息（通过请求体传递，包含email/phone、password/code、loginType）
- **返回**: 登录成功的用户信息

### 2. 用户注册
- **URL**: POST /api/user/register
- **描述**: 用户注册
- **参数**: 
  - 注册信息（通过请求体传递，包含email、phone、password、confirmPassword、code）
- **返回**: 注册成功的用户信息

### 3. 发送验证码
- **URL**: POST /api/user/sendCode
- **描述**: 发送验证码（支持邮箱和手机号，用于登录、注册或忘记密码）
- **参数**: 
  - email: 邮箱（可选）
  - phone: 手机号（可选）
  - type: 用途类型（login/register/forgot）
- **返回**: 操作结果

### 4. 验证验证码
- **URL**: POST /api/user/verifyCode
- **描述**: 验证验证码
- **参数**: 
  - email: 邮箱（可选）
  - phone: 手机号（可选）
  - code: 验证码
  - type: 用途类型（login/register/forgot）
- **返回**: 验证结果

### 5. 忘记密码
- **URL**: POST /api/user/forgotPassword
- **描述**: 忘记密码，重置密码
- **参数**: 
  - email: 邮箱
  - code: 验证码
  - newPassword: 新密码
  - confirmPassword: 确认新密码
- **返回**: 操作结果

### 6. 检查邮箱是否已存在
- **URL**: GET /api/user/checkEmail
- **描述**: 检查邮箱是否已存在
- **参数**: 
  - email: 邮箱

  
- **返回**: 邮箱是否已存在

### 7. 检查手机号是否已存在
- **URL**: GET /api/user/checkPhone
- **描述**: 检查手机号是否已存在
- **参数**: 
  - phone: 手机号
- **返回**: 手机号是否已存在

---

## 工作模式接口

### 1. 获取设备工作模式列表
- **URL**: GET /api/working-mode/getWorkingModes
- **描述**: 获取指定设备的所有工作模式
- **参数**:
  - `deviceId`: 设备唯一标识（通过请求参数传递）
- **返回**: 工作模式列表

### 2. 获取启用的工作模式
- **URL**: GET /api/working-mode/getEnabledWorkingModes
- **描述**: 获取设备的所有启用状态的工作模式
- **参数**:
  - `deviceId`: 设备唯一标识（通过请求参数传递）
- **返回**: 启用工作模式列表

### 3. 添加新的工作模式
- **URL**: POST /api/working-mode/addWorkingMode
- **描述**: 为设备添加新的工作模式
- **参数**:
  - 工作模式详细信息（通过请求体）
- **返回**: 新创建的工作模式

### 4. 更新工作模式
- **URL**: PUT /api/working-mode/updateWorkingMode
- **描述**: 更新指定 ID 的工作模式
- **参数**:
  - `id`: 工作模式 ID（通过请求参数传递）
  - 工作模式详细信息（通过请求体）
- **返回**: 更新后的工作模式

### 5. 删除工作模式
- **URL**: DELETE /api/working-mode/deleteWorkingMode
- **描述**: 删除指定 ID 的工作模式
- **参数**:
  - `id`: 工作模式 ID（通过请求参数传递）
- **返回**: 操作结果

### 6. 获取工作模式详情
- **URL**: GET /api/working-mode/getWorkingModeById
- **描述**: 获取单个工作模式的详细信息
- **参数**:
  - `id`: 工作模式 ID（通过请求参数传递）
- **返回**: 工作模式详情

---