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

### 第四轮已完成
- 志愿者左侧菜单已拆分为 `个人概览` 和 `我的任务` 两个独立入口。
- 志愿者默认登录入口已调整为 `/volunteer-overview`。
- 志愿者 `个人概览` 页面已落地，展示：
  - 志愿者姓名
  - 当前服务状态
  - 累计服务单量
  - 累计服务时长
  - 综合评分
  - 等级
- 后端已新增 `GET /user/volunteer/overview`，基于当前登录志愿者身份返回概览数据，前端不传 `volunteerId`。
- 志愿者 `我的任务` 页面已收口为固定三态页签：
  - `待取餐`
  - `配送中`
  - `已完成`
- 当前任务页 tab 与订单状态值映射固定为：
  - `待取餐 -> status = 4`
  - `配送中 -> status = 5`
  - `已完成 -> status = 6`
- 志愿者任务流转已优化为：
  - `确认取餐成功 -> 自动切到配送中并刷新目标列表`
  - `完成配送成功 -> 自动切到已完成并刷新目标列表`
- 志愿者 `完成配送` 当前使用轻量确认弹窗：
  - 展示订单号、服务老人、收餐地址
  - 确认成功后先关闭弹窗，再自动跳到 `已完成`
  - 自动跳转过程中不再出现二次确认弹窗
  - 本轮不引入签收码、图片上传、签名等复杂签收能力

### 第五轮第一个 Prompt 已完成
- 已新增订单评价表 `order_review`，用于承接 FAMILY 对已完成订单的真实评分数据。
- FAMILY 历史订单页已接入评价入口：
  - `status = 6 && 未评价` 显示 `评价`
  - `status = 6 && 已评价` 显示 `查看评价`
- 订单评价当前只支持：
  - 星级评分 `1~5`
  - 可选文字评价
- 当前评价规则已收口为：
  - 只有 `FAMILY` 可以评价
  - 只有已完成订单可以评价
  - 当前登录 FAMILY 只能评价自己的订单
  - 一个订单只能评价一次
- 未主动评分的已完成订单不会落评价记录，也不会计入志愿者综合评分。
- 后端已新增：
  - `POST /user/order/review`
  - `GET /user/order/review/{orderId}`
- 志愿者概览中的 `综合评分` 已切换为真实评价聚合结果：
  - `rating = avg(score)`
  - 保留 1 位小数
  - 无评价时显示 `--`
- 现有 `volunteer_stats.rating` 旧默认 `5.0` 已在增量 SQL 中清空为 `NULL`，并同步修正新建志愿者默认评分，避免继续出现假评分。
- 这轮落地时已验证：
  - 若未执行 `order_review_rating_update.sql`，历史订单页会因 `order_review` 表不存在而报 `500`
  - 因此切换环境时必须先补执行本轮增量 SQL

### 第五轮第二个 Prompt 已完成
- 志愿者等级制度已补全，继续复用 `volunteer_stats.level` 和 `volunteer_stats.total_orders`。
- 当前等级规则已固定为：
  - `Level 1：0 ~ 9 单`
  - `Level 2：10 ~ 29 单`
  - `Level 3：30 ~ 59 单`
  - `Level 4：60 ~ 99 单`
  - `Level 5：100 单及以上`
- 等级只按累计完成单量计算，不与评分、异常单、服务时长强绑定。
- 订单完成主链路已补齐 `totalOrders / level` 自动回写：
  - `ADMIN` 完成订单会触发回写
  - `VOLUNTEER` 完成配送也会触发同一套回写
- 当前 `volunteer_stats.level` 继续保留为结果字段，不做删字段处理：
  - 订单完成时会回写
  - 评分刷新时也会顺带校正
- 志愿者概览接口当前会基于有效累计单量兜底计算等级：
  - `effectiveTotalOrders = max(volunteer_stats.total_orders, orders.status = 6 的已完成订单数)`
  - `level = 按 effectiveTotalOrders 套用固定等级规则`
- 志愿者概览页本轮继续保持简洁，只显示 `Lv.X`，不增加差距文案和复杂等级体系。

### 第五轮第三个 Prompt 已完成
- 志愿者 `个人概览` 页已新增轻量导出能力。
- 后端已新增：
  - `GET /user/volunteer/overview/export`
- 导出当前直接复用 `GET /user/volunteer/overview` 的数据源，不单独维护报表查询逻辑。
- 当前导出格式为轻量 Excel 单表，导出内容包括：
  - 志愿者姓名
  - 当前服务状态
  - 累计服务单量
  - 累计服务时长
  - 综合评分
  - 等级
  - 导出时间
- 本轮不新增复杂模板、筛选条件和批量导出能力。

### 第五轮第四个 Prompt 已完成
- WebSocket 来单提醒与催单提醒已改为定向推送给对应助餐点的 `OPERATOR`，不再按原始苍穹外卖方式广播给管理端。
- 当前 WebSocket 连接路径已收口为：
  - `/ws/{empId}/{role}/{diningPointId}`
- 当前推送对象过滤规则固定为：
  - `role = OPERATOR`
  - `diningPointId = orders.dining_point_id`
- FAMILY 支付成功后会向对应助餐点在线 `OPERATOR` 推送 `type = 1` 的来单提醒。
- FAMILY 催单后会向对应助餐点在线 `OPERATOR` 推送 `type = 2` 的催单提醒。
- FAMILY 当前只允许对以下状态催单：
  - `2 待调度`
  - `3 制作中`
  - `4 待取餐`
  - `5 配送中`
- 以下状态当前不允许催单：
  - `1 待支付`
  - `6 已完成`
  - `7 已取消`
- 同一订单当前增加了 60 秒基础催单限流，超出会提示“请勿频繁催单，60秒后再试”。
- WebSocket 消费当前已从全局 Navbar 收回到 `OPERATOR` 的 `/order` 页面：
  - 只有 `OPERATOR` 订单页会接收提醒
  - 只有 `OPERATOR` 订单页会播放声音
- 当前声音约定为：
  - `preview.mp3`：来单提醒
  - `reminder.mp3`：催单提醒
- 当前 WebSocket 连接身份仍依赖 URL 参数，不是完整的服务端鉴权绑定连接；这属于现有基础设施的结构性弱点，但本轮未重写这一层。

### 第五轮管理员端查询增强已完成
- 员工管理页已新增轻量筛选：
  - 员工姓名
  - 员工角色：`全部 / 管理员 / 操作员`
  - 所属助餐点：`全部 / 具体助餐点`
- 助餐点管理页已新增状态下拉筛选：
  - `全部 / 营业中 / 休息中`
- 老人档案页已新增所属助餐点筛选：
  - `全部助餐点 / 具体助餐点`
- 当前对应分页接口已同步接收并处理查询参数：
  - `/admin/employee/page`：`name / role / diningPointId`
  - `/admin/diningPoint/page`：`name / status`
  - `/admin/elderly/page`：`name / diningPointId`
- 本轮保持最小改动：
  - 不改新增/编辑表单主体结构
  - 不新建复杂查询面板
  - 页面样式继续沿用原有查询栏布局

## 当前仍待继续

### 业务能力
- FAMILY 套餐购物车、套餐下单、套餐再来一单、套餐结算链路。
- 售后、退款、支付补偿与异常对账闭环。
- 异常订单页面化、通知与人工处理流。
- 志愿者累计服务时长 `totalHours` 自动回写与长期准确性校验。
- 全角色越权回归与跨助餐点数据隔离复核。
- 订单评价内容治理与后续运营规则补充。
- WebSocket 握手鉴权与消息持久化机制当前仍未建设，现阶段仍以轻量实时提醒为主。

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
- 当前志愿者概览与任务页拆分、菜单入口、概览接口与状态流转规则也应同步以实际代码为准。
- 当前订单评价表、评价接口、历史订单评价入口与志愿者评分来源也应同步以实际代码为准。
- 本文件用于标识“当前已完成 / 当前仍待继续”，以后续真实代码状态为准。
