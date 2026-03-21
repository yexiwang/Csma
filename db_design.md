# 社区老年助餐服务系统数据库设计说明

## 1. 文档目的

本文档以当前仓库实际代码与已落地 SQL 为准，说明社区助餐场景下的核心表结构、金额字段、订单状态语义，以及当前需要特别注意的支付链路问题。

当前文档重点覆盖：

- 老人、家属、助餐点、菜品、套餐、订单的核心关系
- 第二轮 FAMILY 真实结算金额字段
- 助餐点休息态的数据库语义
- 定时任务与统计口径
- 当前支付功能的已知问题

## 2. 当前核心关系

### 2.1 角色来源
- `employee.role`：`ADMIN / OPERATOR`
- `user.role`：`FAMILY / VOLUNTEER`

### 2.2 老人与家属
- 当前老人底层仍通过 `elderly.user_id -> user.id` 绑定 `FAMILY`
- `family_profile` 是家属档案业务表，不替代老人底层绑定字段

### 2.3 老人与助餐点
- `elderly.dining_point_id` 表示老人所属助餐点
- 订单归属链路固定为：
  - `elderId -> elderly.dining_point_id -> orders.dining_point_id`
- 菜品、套餐只承担供给合法性和助餐点归属校验

## 3. 核心表现状

### 3.1 user

当前实际关键字段：

| 字段 | 说明 |
| :--- | :--- |
| `id` | 用户主键 |
| `username` | 账号 |
| `password` | 当前兼容 MD5 |
| `role` | `FAMILY / VOLUNTEER` |
| `status` | 启停状态 |
| `name` | 姓名 |
| `phone` | 电话 |

说明：
- `role='FAMILY'` 的 `user.name / user.phone` 也是家属档案页的主数据来源。

### 3.2 employee

当前实际关键字段：

| 字段 | 说明 |
| :--- | :--- |
| `id` | 员工主键 |
| `username` | 员工账号 |
| `password` | 当前兼容 MD5 |
| `role` | `ADMIN / OPERATOR` |
| `dining_point_id` | `OPERATOR` 所属助餐点 |
| `status` | 启停状态 |

说明：
- `ADMIN` 当前不要求绑定助餐点。
- `OPERATOR` 必须绑定一个有效助餐点。

### 3.3 family_profile

当前已收口为家属档案业务表：

| 字段 | 说明 |
| :--- | :--- |
| `id` | 档案主键 |
| `user_id` | 关联 `FAMILY` 用户 |
| `remark` | 备注 |
| `status` | `1 启用 / 0 停用` |
| `is_deleted` | 逻辑删除标记 |

说明：
- 不再保存 `name`、`phone` 字段。
- 家属姓名、电话统一来自 `user` 表。

### 3.4 dining_point

当前实际关键字段：

| 字段 | 说明 |
| :--- | :--- |
| `id` | 助餐点主键 |
| `name` | 助餐点名称 |
| `address` | 地址 |
| `contact_phone` | 联系电话 |
| `operating_hours` | 营业时间 |
| `status` | `1 营业 / 0 休息` |

说明：
- `status=0` 表示休息态，不允许生成新单。

### 3.5 elderly

当前实际关键字段：

| 字段 | 说明 |
| :--- | :--- |
| `id` | 老人主键 |
| `user_id` | 关联 `FAMILY` 用户 |
| `name` | 老人姓名 |
| `phone` | 联系电话 |
| `address` | 地址 |
| `dining_point_id` | 所属助餐点 |
| `special_needs` | 特殊需求 |
| `is_deleted` | 逻辑删除标记 |

说明：
- 当前管理员端已补齐“所属助餐点”的前后端读写链路。

### 3.6 dish

当前实际关键字段：

| 字段 | 说明 |
| :--- | :--- |
| `id` | 菜品主键 |
| `name` | 菜品名称 |
| `category_id` | 分类 |
| `price` | 单价 |
| `status` | `1 起售 / 0 停售` |
| `dining_point_id` | 所属助餐点 |

说明：
- `dish.dining_point_id` 已是正式供给字段。
- 管理端菜品新增/编辑必须提交 `diningPointId`。

### 3.7 setmeal

当前实际关键字段：

| 字段 | 说明 |
| :--- | :--- |
| `id` | 套餐主键 |
| `name` | 套餐名称 |
| `category_id` | 分类 |
| `price` | 套餐价格 |
| `status` | `1 起售 / 0 停售` |
| `dining_point_id` | 所属助餐点 |

说明：
- `setmeal.dining_point_id` 已成为正式业务字段。
- 管理端套餐新增/编辑必须提交 `diningPointId`。
- 套餐内菜品必须与套餐所属助餐点一致。

### 3.8 shopping_cart

当前实际关键字段：

| 字段 | 说明 |
| :--- | :--- |
| `id` | 购物车项主键 |
| `user_id` | 当前 FAMILY 用户 |
| `elder_id` | 当前服务老人 |
| `dish_id` | 菜品 ID |
| `number` | 数量 |
| `amount` | 单价 |

说明：
- `shopping_cart.elder_id` 已持久化。
- 当前同一 `FAMILY` 用户同一时刻只允许存在一个老人的购物车。

### 3.9 orders

当前实际关键字段：

| 字段 | 说明 |
| :--- | :--- |
| `id` | 订单主键 |
| `number` | 订单号，当前支付接口使用 |
| `user_id` | 下单 FAMILY 用户 |
| `elder_id` | 老人 ID |
| `dining_point_id` | 订单所属助餐点 |
| `volunteer_id` | 志愿者 ID |
| `status` | 订单状态 |
| `pay_status` | 支付状态 |
| `amount` | 订单总金额，当前等于 FAMILY `payAmount` |
| `personal_pay` | 实付金额 |
| `subsidy_amount` | 补贴金额，当前固定 `0` |
| `delivery_fee` | 配送费，当前固定 `0` |
| `tableware_fee` | 餐具费 |
| `pack_amount` | 当前 FAMILY 主流程固定 `0` |
| `expected_time` | 期望送达时间 |
| `estimated_delivery_time` | FAMILY 提交的预计送达时间 |
| `delivery_status` | 配送时间选择状态，当前 FAMILY 主流程固定写 `0` |
| `order_time` | 下单时间 |
| `checkout_time` | 支付完成时间 |
| `cancel_time` | 取消时间 |
| `cancel_reason` | 取消原因 |

说明：
- `delivery_fee`、`tableware_fee` 已进入当前真实结算与历史订单金额展示。
- FAMILY 主流程当前金额口径为：
  - `amount = payAmount`
  - `personal_pay = payAmount`
  - `subsidy_amount = 0`
  - `pack_amount = 0`
- FAMILY 下单当前固定提交 `delivery_status = 0`；后端也会在 `OrdersSubmitDTO.deliveryStatus` 为空时默认补 `0`，避免因数据库非空约束导致事务回滚。

### 3.10 volunteer_stats

当前代码中的志愿者概览数据会使用 `volunteer_stats` 作为统计来源之一。

当前实际会被前后端读取的关键字段：

| 字段 | 说明 |
| :--- | :--- |
| `user_id` | 关联 `VOLUNTEER` 用户 |
| `total_orders` | 累计服务单量 |
| `total_hours` | 累计服务时长 |
| `rating` | 综合评分 |
| `level` | 等级 |

说明：

- 当前用户端概览接口为 `GET /user/volunteer/overview`。
- 接口基于当前登录用户身份获取数据，前端不传 `volunteerId`。
- 概览接口当前数据来源为：
  - `user.name / user.status`
  - `volunteer_stats.total_hours / rating / level`
  - `max(volunteer_stats.total_orders, orders.status = 6 的已完成订单数)`
- 之所以对 `total_orders` 做兼容，是因为当前统计表未完全随每次履约自动回写；文档和展示应按当前代码口径理解。
- 若 `volunteer_stats` 记录缺失，当前前端会以 `0 / --` 做兜底展示，不影响任务页继续使用。

## 4. FAMILY 当前金额模型

当前固定规则：

- `dishAmount = sum(item.amount * item.number)`
- `deliveryFee = 0`
- `tablewareFee = effectiveTablewareNumber * 1`
- `subsidyAmount = 0`
- `payAmount = dishAmount + deliveryFee + tablewareFee - subsidyAmount`

页面与落库口径：

- 购物车抽屉、底部结算栏、结算弹层、历史订单详情统一按上面口径展示
- 订单落库时：
  - `amount = payAmount`
  - `personal_pay = payAmount`
  - `delivery_fee`、`tableware_fee` 持久化

## 5. 当前订单状态语义

统一按以下状态理解：

| 状态值 | 含义 |
| :--- | :--- |
| `1` | 待支付 |
| `2` | 待调度 |
| `3` | 制作中 |
| `4` | 待取餐 |
| `5` | 配送中 |
| `6` | 已完成 |
| `7` | 已取消 |

说明：
- 统计 SQL、ADMIN 概览、OPERATOR 看板、历史订单页都应按这一口径理解。
- `Top10`、营业额、有效订单统一按 `status = 6` 作为完成口径。

## 6. 助餐点休息态规则

### 6.1 新单阻断
- 助餐点切为休息后，不允许：
  - 作为可售菜品/套餐来源
  - 新加入购物车
  - 提交新订单
  - 再来一单回写购物车
  - 待支付订单继续支付推进

### 6.2 既有订单处理
- `1 待支付`：保留原单，按支付超时规则自动取消
- `2 待调度`：允许继续
- `3 制作中`：允许继续
- `4 待取餐`：允许继续
- `5 配送中`：允许继续
- `6 已完成 / 7 已取消`：不处理

## 7. 定时任务与统计口径

### 7.1 定时任务
- 保留：待支付超时自动取消
- 移除：配送中超时自动完成
- 新增：超时未履约日志识别，不直接改订单状态

### 7.2 统计口径
- 销量 Top10：只统计 `status = 6`
- 营业额：只统计 `status = 6`
- 工作台/概览页：统一按当前 `1-7` 语义

## 8. 当前支付链路问题说明

当前支付功能需要特别说明：

- `orders.number` 仍然是 FAMILY 支付接口使用的订单号。
- 后端模拟支付当前已统一为：
  - `payment(orderNumber)` 做 FAMILY 归属、待支付状态、助餐点营业等前置校验
  - `paySuccess(orderNumber)` 统一处理支付成功后的状态更新
- `paySuccess(orderNumber)` 当前固定执行：
  - `status = 2`
  - `pay_status = 1`
  - `checkout_time = now`
  - 触发现有 WebSocket 来单提醒
- 历史订单“继续支付”和 `/notify/paySuccess` 当前都复用同一套 `paySuccess(orderNumber)` 逻辑。
- 前端当前会对 `/user/order/submit` 和 `/user/order/payment` 的业务码做校验；若后端返回 `code = 0`，页面直接展示真实错误，不再误报“订单创建成功”。
- `/user/order/payment` 当前只允许处理 `status = 1` 的待支付订单。
- 如果传入旧订单号、重复触发支付，或该订单已经被推进到非待支付状态，后端返回：
  - `订单状态错误`
  这属于当前状态保护逻辑。

当前仍需继续关注的点主要在返回契约，而不是数据库字段本身：

- 前端当前仍依赖 `/user/order/submit` 返回：
  - `data.id`
  - `data.orderNumber`
- 如果后端未来返回体缺少 `id` 或 `orderNumber`，前端仍会提示：
  - `订单创建成功，但未返回有效订单信息`
- 因此后续应继续从 `/user/order/submit` 的返回契约稳定化入手，而不是继续扩大前端兜底。

## 9. 维护说明

后续如果继续调整以下任一内容，应同步更新本文档：

- 订单金额字段含义
- 订单状态枚举
- 助餐点休息态规则
- 订单支付链路
- 统计 SQL 口径
