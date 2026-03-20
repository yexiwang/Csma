# 社区老年助餐服务系统数据库设计说明

## 1. 文档目的

本文档以当前仓库的实际代码与 SQL 脚本为准，说明社区助餐改造后的数据库结构、角色模型和落地注意事项。

当前应优先参考的脚本有：

- `community_meal_update.sql`
- `login_role_auth_update.sql`
- `family_profile_master_data_update.sql`
- `family_profile_drop_name_phone_columns.sql`

说明：

- `family_profile_name_cleanup.sql`
- `family_profile_phone_cleanup.sql`

这两份脚本属于中间过渡脚本，用于把历史姓名、电话回填到 `user` 表。
如果库已经完成最终删列，不应重复执行。

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
- 对于 `role='FAMILY'` 的用户，`user.name` 与 `user.phone` 现在也是家属档案页的主数据来源。

### 4.2.1 family_profile

当前 `family_profile` 已从“家属姓名/电话资料表”收口为“家属档案业务表”。

当前关键字段：

| 字段 | 类型 | 说明 |
| :--- | :--- | :--- |
| `id` | bigint | 主键 |
| `user_id` | bigint | 关联的 `FAMILY` 用户 ID，唯一 |
| `remark` | varchar(255) | 家属档案备注 |
| `status` | int | 档案状态，`1` 启用，`0` 停用 |
| `create_time` | datetime | 创建时间 |
| `update_time` | datetime | 更新时间 |
| `create_user` | bigint | 创建人 |
| `update_user` | bigint | 更新人 |
| `is_deleted` | int | 逻辑删除标记，`0` 未删除，`1` 已删除 |

说明：

- `family_profile` 当前不再存储 `name`、`phone` 字段。
- 家属姓名统一从 `user.name` 联查返回。
- 家属电话统一从 `user.phone` 联查返回。
- `family_profile` 的作用是承载家属档案状态、备注、逻辑删除和与 `FAMILY` 账号的一对一绑定关系。
- `family_profile` 不替代 `elderly.user_id`，老人底层仍继续绑定 `user.id`。
- 老人可绑定的家属必须同时满足：`family_profile.status = 1`、`family_profile.is_deleted = 0`、`user.status = 1`、`user.role = 'FAMILY'`。
- 若家属档案仍被未删除老人引用，则不允许删除，只允许停用。

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
| `user_id` | bigint | 关联的 `FAMILY` 用户 ID |
| `name` | varchar(32) | 老人姓名 |
| `gender` | char(1) | 性别 |
| `age` | int | 年龄 |
| `phone` | varchar(11) | 联系电话 |
| `address` | varchar(255) | 地址 |
| `dining_point_id` | bigint | 所属助餐点 |
| `grid_code` | varchar(50) | 网格/片区编码 |
| `health_info` | text | 健康情况 |
| `special_needs` | varchar(255) | 特殊需求 |
| `id_card` | varchar(18) | 身份证号 |
| `image` | varchar(255) | 照片 |
| `create_time` | datetime | 创建时间 |
| `update_time` | datetime | 更新时间 |
| `is_deleted` | int | 逻辑删除标记 |

说明：

- 当前老人归属仍继续沿用 `elderly.user_id -> user.id`。
- 管理员端保存老人时，必须绑定一个有效且启用的家属档案所对应的 `FAMILY` 用户。
- 当前阶段不引入 `family_profile_id` 作为老人底层关系字段。
- 若当前已绑定家属已停用或当前不可选，历史老人记录仍可展示，但不能再作为新的绑定候选。

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

## 10. 2026-03 最新规则补充

以下内容覆盖本文档中与当前实现不一致的旧描述。

### 10.1 老人与助餐点绑定

- `elderly.dining_point_id` 当前已经是正式业务字段
- 其业务含义为：该老人默认由哪个助餐点提供助餐服务

### 10.2 订单归属规则

当前订单归属助餐点链路为：

`elderId -> elderly.dining_point_id -> orders.dining_point_id`

这意味着：

- 订单归属助餐点不再由购物车菜品反推
- 菜品的 `dining_point_id` 只负责一致性校验
- 助餐点休息时禁止产生新单

### 10.3 购物车规则

- `shopping_cart` 当前已新增持久化字段 `elder_id`
- 同一 `FAMILY` 用户同一时刻只允许存在一个老人的购物车
- `GET /user/shoppingCart/list` 当前返回：
  - 持久化字段 `elderId`
  - 派生字段 `diningPointId`

### 10.4 OPERATOR 数据范围

- `orders.dining_point_id` 不仅决定订单归属，也决定 `OPERATOR` 的数据范围
- 当角色为 `OPERATOR` 时，订单查询与状态操作都必须满足：
  - `orders.dining_point_id = currentDiningPointId`

### 10.5 管理员端老人档案字段落地

当前仓库已经完成管理员端“老人档案 -> 所属助餐点”字段落地，数据库与接口约定如下：

- 字段：`elderly.dining_point_id`
- 推荐注释：`所属助餐点ID/默认助餐点ID`
- 业务含义：表示该老人默认由哪个助餐点提供助餐服务
- 推荐索引：`idx_elderly_dining_point_id`

增量 SQL 已单独沉淀在仓库根目录：

- `elderly_dining_point_admin_update.sql`

管理员端当前读写方式：

- 新增/编辑老人时，`dining_point_id` 由管理端表单显式提交
- 分页/详情查询通过 `elderly LEFT JOIN dining_point` 直接返回 `diningPointName`
- 管理端不再需要拿到 `diningPointId` 后自行循环查名称

当前管理端校验约定：

- `dining_point_id` 必填
- 目标助餐点必须存在
- 目标助餐点必须为启用状态
- 若为历史遗留空值数据，可查询、可展示，但不能在保存时继续以空值提交
