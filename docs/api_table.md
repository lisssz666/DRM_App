# DRM Sprayer API 接口表格

| 分类 | API名称 | 请求方式 | URL | 描述 | 参数 | 返回结果 |
| --- | --- | --- | --- | --- | --- | --- |
| 设备管理 | 获取设备信息 | GET | /api/device/getDeviceInfo | 获取单个设备的详细信息 | deviceId: 设备唯一标识（通过请求参数传递） | 设备详细信息（Device 实体） |
| 设备管理 | 设备控制 | POST | /api/device/controlDevice | 控制设备的开关状态 | deviceId: 设备唯一标识（通过请求参数传递）<br>isOn: 设备状态（true=开启, false=关闭） | 操作结果 |
| 设备管理 | 修改精油名称 | POST | /api/device/updateOilName | 更新设备精油名称 | deviceId: 设备唯一标识（通过请求参数传递）<br>oilName: 新的精油名称 | 更新后的设备信息 |
| 设备管理 | 更新精油量 | POST | /api/device/updateOilLevel | 更新设备精油剩余量 | deviceId: 设备唯一标识（通过请求参数传递）<br>oilLevel: 精油剩余量（百分比） | 更新后的设备信息 |
| 设备管理 | 锁定设备 | POST | /api/device/lockDevice | 锁定设备防止误操作 | deviceId: 设备唯一标识（通过请求参数传递） | 操作结果 |
| 设备管理 | 解锁设备 | POST | /api/device/unlockDevice | 解锁设备 | deviceId: 设备唯一标识（通过请求参数传递） | 操作结果 |
| 设备管理 | 风扇开关控制 | POST | /api/device/controlFan | 控制风扇开关 | deviceId: 设备唯一标识（通过请求参数传递）<br>isOn: 风扇状态（true=开启, false=关闭） | 操作结果 |
| 设备管理 | 设置风扇速度 | POST | /api/device/setFanSpeed | 设置风扇速度 | deviceId: 设备唯一标识（通过请求参数传递）<br>speed: 风扇速度（0-100） | 操作结果 |
| 工作模式 | 获取设备工作模式列表 | GET | /api/working-mode/getWorkingModes | 获取指定设备的所有工作模式 | deviceId: 设备唯一标识（通过请求参数传递） | 工作模式列表 |
| 工作模式 | 获取启用的工作模式 | GET | /api/working-mode/getEnabledWorkingModes | 获取设备的所有启用状态的工作模式 | deviceId: 设备唯一标识（通过请求参数传递） | 启用工作模式列表 |
| 工作模式 | 添加新的工作模式 | POST | /api/working-mode/addWorkingMode | 为设备添加新的工作模式 | 工作模式详细信息（通过请求体） | 新创建的工作模式 |
| 工作模式 | 更新工作模式 | PUT | /api/working-mode/updateWorkingMode | 更新指定 ID 的工作模式 | id: 工作模式 ID（通过请求参数传递）<br>工作模式详细信息（通过请求体） | 更新后的工作模式 |
| 工作模式 | 删除工作模式 | DELETE | /api/working-mode/deleteWorkingMode | 删除指定 ID 的工作模式 | id: 工作模式 ID（通过请求参数传递） | 操作结果 |
| 工作模式 | 获取工作模式详情 | GET | /api/working-mode/getWorkingModeById | 获取单个工作模式的详细信息 | id: 工作模式 ID（通过请求参数传递） | 工作模式详情 |