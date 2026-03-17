# 社区老年助餐服务系统 - 数据库设计方案

基于苍穹外卖原有数据库架构进行扩展与改造，以满足社区助餐业务需求。

## 1. 核心实体变更概述

### 1.1 用户体系重构
原系统使用 `employee` (管理端) 和 `user` (C端微信用户)。
新系统调整如下：
- **管理人员 (`employee`)**:
  - **社区管理员 (`ADMIN`)**: 拥有全局权限。
  - **助餐点操作员 (`OPERATOR`)**: 仅拥有特定助餐点权限。
  - **新增字段**: `dining_point_id` (关联助餐点，`ADMIN` 为空，`OPERATOR` 必填)。

- **服务对象与执行者 (`user`)**:
  - **老人/家属端账号 (`FAMILY`)**: 负责下单、代订。
  - **志愿者 (`VOLUNTEER`)**: 负责配送。
  - **改造点**: 原 `user` 表仅用于微信登录，现需增加账号密码登录支持及角色标识。
  - **新增字段**: `password` (加密密码), `username` (登录名), `role` (枚举: `FAMILY`, `VOLUNTEER`), `phone` (联系电话), `status` (启用/禁用)。

- **老人档案 (`elderly`) - 新增表**:
  - 为了支持“家属代订”及“一人绑定多老人”，将老人信息从 `user` 表独立出来。
  - **字段**: `id`, `user_id` (关联家属/本人账号), `name`, `gender`, `id_card`, `phone`, `address`, `health_info` (健康状况), `special_needs` (特殊需求), `grid_code` (所属网格/片区).

### 1.2 助餐点 (`dining_point`) - 新增表
作为服务的物理节点，负责餐品制作与发放。
- **字段**: `id`, `name`, `address`, `contact_phone`, `operating_hours`, `status` (营业中/休息), `image`.

### 1.3 志愿者服务记录 (`volunteer_stats`) - 新增表
记录志愿者的服务贡献。
- **字段**: `id`, `user_id` (志愿者), `total_orders` (累计单量), `total_hours` (累计时长), `rating` (评分), `level` (等级).

## 2. 详细表结构变更

### 2.1 employee (员工表 - 修改)
| 字段名 | 类型 | 说明 | 变更 |
| :--- | :--- | :--- | :--- |
| `dining_point_id` | bigint | 所属助餐点ID | **新增**，`ADMIN` 为空，`OPERATOR` 必填 |

### 2.2 user (C端用户表 - 修改)
| 字段名 | 类型 | 说明 | 变更 |
| :--- | :--- | :--- | :--- |
| `username` | varchar(32) | 登录账号 | **新增**，唯一 |
| `password` | varchar(64) | 登录密码 | **新增**，BCrypt加密 |
| `role` | varchar(20) | 角色 | **新增** (FAMILY, VOLUNTEER) |
| `name` | varchar(32) | 真实姓名 | **新增** |
| `phone` | varchar(11) | 手机号 | 原有，需设为索引 |

### 2.3 elderly (老人档案表 - 新增)
| 字段名 | 类型 | 说明 | 备注 |
| :--- | :--- | :--- | :--- |
| `id` | bigint | 主键 | 自增 |
| `user_id` | bigint | 关联用户ID | 家属或本人账号ID |
| `name` | varchar(32) | 老人姓名 | |
| `gender` | char(1) | 性别 | |
| `age` | int | 年龄 | |
| `phone` | varchar(11) | 联系电话 | |
| `address` | varchar(255)| 详细居住地址 | |
| `grid_code` | varchar(50) | 所属网格/片区 | 用于派单匹配 |
| `health_info` | text | 健康状况 | 慢性病、过敏源等 |
| `special_needs` | varchar(255)| 特殊需求 | 如：需敲门大声、行动不便 |
| `create_time` | datetime | 创建时间 | |

### 2.4 dining_point (助餐点表 - 新增)
| 字段名 | 类型 | 说明 | 备注 |
| :--- | :--- | :--- | :--- |
| `id` | bigint | 主键 | 自增 |
| `name` | varchar(50) | 助餐点名称 | |
| `address` | varchar(255)| 地址 | |
| `contact_phone` | varchar(20)| 联系电话 | |
| `status` | int | 状态 | 1:营业 0:休息 |
| `grid_coverage` | varchar(255)| 服务网格范围 | JSON数组，存储覆盖的片区代码 |

### 2.5 dish (菜品表 - 修改)
| 字段名 | 类型 | 说明 | 变更 |
| :--- | :--- | :--- | :--- |
| `dining_point_id` | bigint | 所属助餐点ID | **新增**，逻辑外键 |
| `nutrition_tags` | varchar(255)| 营养标签 | **新增**，如：低糖,低脂,易以此嚼 |
| `suitability` | varchar(255)| 适宜人群 | **新增** |

### 2.6 orders (订单表 - 修改)
| 字段名 | 类型 | 说明 | 变更 |
| :--- | :--- | :--- | :--- |
| `elder_id` | bigint | 用餐老人ID | **新增**，关联 elderly 表 |
| `volunteer_id` | bigint | 配送志愿者ID | **新增**，关联 user 表 |
| `dining_point_id` | bigint | 出餐助餐点ID | **新增**，关联 dining_point 表 |
| `subsidy_amount` | decimal | 补贴金额 | **新增** |
| `personal_pay` | decimal | 自付金额 | **新增** |
| `expected_time` | datetime | 期望送达时间 | **新增** |
| `status` | int | 订单状态 | 修改枚举定义 (见下方状态机) |

## 3. 业务状态机 (Order Status)

- `1` **PENDING_PAYMENT (待支付)**: 用户下单后，计算补贴与自付金额。
- `2` **TO_BE_SCHEDULED (待调度)**: 支付完成（或全额补贴），等待管理员/系统派单。
- `3` **CONFIRMED (已接单/制作中)**: 志愿者已接单（或指派），助餐点开始制作。
- `4` **MEAL_READY (待取餐)**: 助餐点标记出餐完成。
- `5` **DELIVERING (配送中)**: 志愿者扫码/确认取餐。
- `6` **COMPLETED (已完成)**: 志愿者送达并确认。
- `7` **CANCELLED (已取消)**: 用户或管理员取消。

## 4. 权限与角色模型

| 角色 | 表来源 | 关键权限 | 前端视图 |
| :--- | :--- | :--- | :--- |
| **社区管理员 (`ADMIN`)** | employee | 全局管理、人员审核、派单调度 | Admin Dashboard |
| **助餐点操作员 (`OPERATOR`)** | employee | 本点订单管理、出餐核销 | Operator View |
| **志愿者 (`VOLUNTEER`)** | user (role=`VOLUNTEER`) | 任务列表、接单/送达 | Volunteer View |
| **老人/家属端 (`FAMILY`)**| user (role=`FAMILY`) | 菜品浏览、下单、支付、评价 | Family View |
