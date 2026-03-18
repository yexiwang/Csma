# 社区老年助餐服务系统数据库设计说明

## 1. 文档目的

本文档以当前仓库的实际代码与 SQL 脚本为准，说明社区助餐改造后的数据库结构、角色模型和落地注意事项。

当前应优先参考的脚本有：

- `community_meal_update.sql`
- `login_role_auth_update.sql`

## 2. 当前数据库改造基线

项目仍基于原 `sky_take_out` 数据库扩展，不是完全重建新库。

目前数据库改造主要分为两部分：

### 2.1 社区助餐业务结构扩展

由 `community_meal_update.sql` 提供，主要包括：

- 新增 `dining_point`
- 新增 `elderly`
- 新增 `volunteer_stats`
- 修改 `employee`
- 修改 `user`
- 修改 `orders`
- 修改 `dish`

### 2.2 登录与角色权限补充

由 `login_role_auth_update.sql` 提供，主要包括：

- 为 `employee` 增加 `role`
- 统一初始化员工角色
- 统一补齐用户角色默认值
- 将明文 `123456` 兼容更新为 `MD5('123456')`

注意：

- `employee.dining_point_id` 在 `community_meal_update.sql` 中已经出现。
- `login_role_auth_update.sql` 中对该字段的 `ALTER TABLE` 已经注释掉，避免重复执行时报错。

## 3. 当前角色模型

### 3.1 员工端角色

来源表：`employee`

- `ADMIN`
- `OPERATOR`

当前代码约束：

- `ADMIN` 的 `dining_point_id` 应为空
- `OPERATOR` 必须绑定 `dining_point_id`

### 3.2 用户端角色

来源表：`user`

- `FAMILY`
- `VOLUNTEER`

当前代码中：

- 用户登录如果角色为空，会默认补成 `FAMILY`
- 微信登录自动注册的用户也会默认写入 `FAMILY`

## 4. 核心表结构现状

### 4.1 employee

当前代码实际使用的关键字段：

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | bigint | 主键 |
| `username` | varchar | 登录账号 |
| `name` | varchar | 姓名 |
| `password` | varchar | 登录密码，当前兼容 MD5 |
| `phone` | varchar | 手机号 |
| `sex` | varchar | 性别 |
| `id_number` | varchar | 身份证号 |
| `status` | int | 状态 |
| `role` | varchar(20) | 角色，`ADMIN / OPERATOR` |
| `dining_point_id` | bigint | 所属助餐点，操作员必填 |
| `create_time` | datetime | 创建时间 |
| `update_time` | datetime | 更新时间 |
| `create_user` | bigint | 创建人 |
| `update_user` | bigint | 更新人 |

### 4.2 user

当前代码实际使用的关键字段：

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | bigint | 主键 |
| `openid` | varchar | 微信标识 |
| `username` | varchar(32) | 账号登录用户名 |
| `password` | varchar(64) | 登录密码，当前兼容 MD5 |
| `role` | varchar(20) | 角色，`FAMILY / VOLUNTEER` |
| `status` | int | 状态 |
| `name` | varchar(32) | 姓名 |
| `phone` | varchar(11) | 手机号 |
| `sex` | varchar | 性别 |
| `id_number` | varchar | 身份证号 |
| `avatar` | varchar | 头像 |
| `create_time` | datetime | 注册时间 |

说明：

- 当前仓库里的登录实现仍按 MD5 校验密码，不是 BCrypt。
- 后续如果要整体升级密码方案，应作为单独改造任务处理。

### 4.3 dining_point

由 `community_meal_update.sql` 新增。

当前关键字段：

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | bigint | 主键 |
| `name` | varchar(50) | 助餐点名称 |
| `address` | varchar(255) | 地址 |
| `contact_phone` | varchar(20) | 联系电话 |
| `operating_hours` | varchar(50) | 营业时间 |
| `status` | int | 状态，`1` 营业，`0` 休息 |
| `image` | varchar(255) | 图片 |
| `grid_coverage` | json | 服务片区范围 |
| `create_time` | datetime | 创建时间 |
| `update_time` | datetime | 更新时间 |
| `create_user` | bigint | 创建人 |
| `update_user` | bigint | 更新人 |

### 4.4 elderly

由 `community_meal_update.sql` 新增。

当前关键字段：

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | bigint | 主键 |
| `user_id` | bigint | 关联家属/账号 |
| `name` | varchar(32) | 老人姓名 |
| `gender` | char(1) | 性别 |
| `age` | int | 年龄 |
| `phone` | varchar(11) | 联系电话 |
| `address` | varchar(255) | 地址 |
| `grid_code` | varchar(50) | 网格/片区编码 |
| `health_info` | text | 健康情况 |
| `special_needs` | varchar(255) | 特殊需求 |
| `id_card` | varchar(18) | 身份证号 |
| `image` | varchar(255) | 照片 |
| `create_time` | datetime | 创建时间 |
| `update_time` | datetime | 更新时间 |

### 4.5 volunteer_stats

由 `community_meal_update.sql` 新增。

当前关键字段：

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | bigint | 主键 |
| `user_id` | bigint | 志愿者用户 ID |
| `total_orders` | int | 累计服务单量 |
| `total_hours` | decimal(10,1) | 累计服务时长 |
| `rating` | decimal(2,1) | 评分 |
| `level` | int | 等级 |
| `create_time` | datetime | 创建时间 |
| `update_time` | datetime | 更新时间 |

### 4.6 orders

在原订单表基础上，社区助餐改造新增的关键字段有：

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `elder_id` | bigint | 用餐老人 ID |
| `volunteer_id` | bigint | 配送志愿者 ID |
| `dining_point_id` | bigint | 出餐助餐点 ID |
| `subsidy_amount` | decimal(10,2) | 补贴金额 |
| `personal_pay` | decimal(10,2) | 自付金额 |
| `expected_time` | datetime | 期望送达时间 |

说明：

- 当前代码里订单调度、志愿者配送、家属历史订单都已经依赖这些扩展字段。
- 但“按助餐点做严格数据隔离”还没有在所有查询中完全落地。

### 4.7 dish

在原菜品表基础上，社区助餐改造新增的关键字段有：

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `dining_point_id` | bigint | 所属助餐点 |
| `nutrition_tags` | varchar(255) | 营养标签 |
| `suitability` | varchar(255) | 适宜人群 |

## 5. 当前订单状态约定

结合当前前后端页面与接口，现阶段建议按以下状态理解：

| 状态值 | 状态含义 |
| :--- | :--- |
| `1` | 待支付 |
| `2` | 待调度 |
| `3` | 制作中 |
| `4` | 待取餐 |
| `5` | 配送中 |
| `6` | 已完成 |
| `7` | 已取消 |

说明：

- 文档中的状态命名应优先和当前代码、页面文案保持一致。

## 6. 当前登录与权限相关字段约定

### 6.1 员工登录

员工 token 当前会写入：

- `empId`
- `role`
- `diningPointId`
- `username`
- `name`

### 6.2 用户登录

用户 token 当前会写入：

- `userId`
- `role`
- `username`
- `name`

### 6.3 请求头

当前前后端统一使用请求头：

- `token`

## 7. SQL 执行建议

建议执行顺序：

1. 基础 `sky_take_out` 库结构
2. `community_meal_update.sql`
3. `login_role_auth_update.sql`

执行时需要注意：

- 若数据库已执行过旧版社区改造 SQL，请先检查 `employee.dining_point_id` 是否已存在
- 如果已存在，不要重复执行同名 `ALTER TABLE`
- 若库中仍有明文 `123456`，再执行登录权限补充 SQL 完成兼容更新

## 8. 当前未完全落地的数据库层事项

以下内容仍属于后续优化范围：

- 更严格的外键与索引梳理
- 助餐点范围内的数据隔离查询
- 老人隐私字段脱敏方案
- 更完整的补贴计算与审计字段
- 配送轨迹、签收凭证等扩展表

## 9. 维护说明

后续如果以下任一内容发生变化，应同步更新本文档：

- 角色字段
- 订单扩展字段
- 登录 token claims
- SQL 初始化脚本
- 订单状态定义
