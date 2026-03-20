# 社区老年助餐服务系统任务清单

## 当前技术基线
- 前端：`sky-admin-vue`，Vue 2 + TypeScript + Element UI
- 后端：`sky-take-out`，Spring Boot + MyBatis XML + JWT + Interceptor
- 权限方案：继续沿用 `JWT + Interceptor + BaseContext`
- 当前角色：`ADMIN / OPERATOR / VOLUNTEER / FAMILY`

## 当前已完成里程碑

### 第一轮已完成
- 家属档案管理已落地，`family_profile` 作为家属档案业务表使用。
- 老人已显式关联家属，底层仍通过 `elderly.user_id -> user.id` 绑定 `FAMILY`。
- 老人“所属助餐点”字段已落地，管理员端可维护。
- 登录、角色菜单、路由守卫、基础越权拦截已按当前四角色收口。

### 第二轮已完成
- `dish.dining_point_id`、`setmeal.dining_point_id` 已纳入正式供给体系。
- 管理端菜品新增/编辑必须提交 `diningPointId`，列表已展示所属助餐点。
- 管理端套餐新增/编辑必须提交 `diningPointId`，选菜已按当前助餐点过滤。
- FAMILY 点餐页已按“先选老人，再按老人所属助餐点加载菜品”运行。
- FAMILY 购物车已切到“后端真相 + 前端单一状态”：
  - `/user/shoppingCart/list` 负责条目
  - `/user/shoppingCart/summary` 负责金额汇总
- FAMILY 结算页已接入真实金额字段：
  - `dishAmount`
  - `deliveryFee`
  - `tablewareFee`
  - `subsidyAmount`
  - `payAmount`
- 地址选择、默认地址、首个地址自动默认、回跳恢复结算已落地。
- 下单成功后后端会清空购物车，并跳转 `/family-history?createdOrderId=...`。
- 历史订单详情已展示菜品金额、配送费、餐具费、补贴金额、实付金额。

### 第三轮已完成
- 助餐点休息态已阻断新单链路：
  - 不允许可售查询
  - 不允许加入购物车
  - 不允许提交订单
  - 不允许再来一单
  - 不允许待支付订单继续支付推进
- 既有 `2/3/4/5` 状态订单允许继续履约，不做批量改单。
- 定时任务已收口为：
  - 保留支付超时自动取消
  - 移除“配送中超时自动完成”
  - 新增超时未履约日志识别，不改状态
- 统计口径已统一为当前 `1-7` 语义。
- ADMIN dashboard、ADMIN/OPERATOR 订单相关前端状态文案已按新语义收口。
- FAMILY 模拟支付链路已做最小修复：
  - `payment(orderNumber)` 统一调用 `paySuccess(orderNumber)`
  - `paySuccess(orderNumber)` 统一更新 `status=2`、`pay_status=1`、`checkout_time`
  - 历史订单“继续支付”与支付回调复用同一套状态更新逻辑
- FAMILY 下单提交链路已补齐业务失败识别：
  - `/user/order/submit` 返回 `code=0` 时，前端直接提示真实错误，不再误报“订单创建成功”
  - 下单会显式提交 `deliveryStatus=0`
  - 后端在落库前也会默认补 `deliveryStatus=0`，避免因 `delivery_status` 非空约束导致事务回滚

## 当前仍待继续

### 业务能力
- FAMILY 套餐购物车、套餐下单、套餐再来一单、套餐结算链路。
- 售后、退款、支付补偿与异常对账闭环。
- 异常订单页面化、通知与人工处理流。
- 志愿者完整任务流程联调。
- 全角色越权回归与跨助餐点数据隔离复核。

### 支付链路
- `/user/order/submit -> /user/order/payment` 的主链路已恢复到可用状态。
- `payment(orderNumber)`、历史订单继续支付、`/notify/paySuccess` 当前都复用 `paySuccess(orderNumber)`。
- `/user/order/payment` 仍然只允许处理 `status = 1` 的待支付订单。
- 如果传入旧订单号、重复支付，或该订单已被推进到非待支付状态，后端会返回：
  - `订单状态错误`
- 当前仍需继续稳定的是 `/user/order/submit` 的返回契约：
  - 前端仍要求返回 `data.id`
  - 前端仍要求返回 `data.orderNumber`
  - 如果后端未来缺少其中任一字段，前端仍会提示“订单创建成功，但未返回有效订单信息”
- 后续支付链路的继续优化方向仍是：后端稳定返回 `OrderSubmitVO(id, orderNumber, orderAmount, orderTime)`，而不是继续扩大前端兜底。

## 当前固定规则

### 老人、订单与助餐点
- 老人决定订单归属助餐点。
- 订单归属链路固定为：`elderId -> elderly.dining_point_id -> orders.dining_point_id`
- 菜品/套餐只承担供给合法性与助餐点归属校验。

### 助餐点休息态
- 对新单：
  - 禁止加入购物车
  - 禁止提交订单
  - 禁止作为可售菜单来源
- 对已有订单：
  - `1 待支付`：允许保留，按支付超时规则自动取消
  - `2 待调度`：允许继续
  - `3 制作中`：允许继续
  - `4 待取餐`：允许继续
  - `5 配送中`：允许继续
  - `6 已完成 / 7 已取消`：不处理

### FAMILY 金额模型
- `deliveryFee = 0`
- `tablewareFee = 1 元 / 份`
- `subsidyAmount = 0`
- `payAmount = dishAmount + deliveryFee + tablewareFee - subsidyAmount`
- FAMILY 主流程中 `packAmount = 0`

## 文档同步说明
- `spec.md`：记录当前业务规格、前端页面规则、支付链路问题。
- `db_design.md`：记录当前表结构、金额字段、订单状态与支付链路边界。
- 本文件用于标识“当前已完成 / 当前仍待继续”，以后续真实代码状态为准。
