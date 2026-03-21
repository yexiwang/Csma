# 社区老年助餐服务系统规格说明

## 1. 项目定位

本项目基于现有 `Sky Take Out` 代码进行改造，目标是从“商用外卖后台”调整为“社区老年助餐服务平台”。

当前代码中的核心角色已经统一为：

- `ADMIN`：社区管理员
- `OPERATOR`：助餐点操作员
- `VOLUNTEER`：志愿者
- `FAMILY`：老人家属/代订餐用户

系统当前的业务主线是：

1. `FAMILY` 浏览餐品并下单
2. 订单进入社区调度
3. `ADMIN` 统一查看和调度订单
4. `OPERATOR` 处理所属助餐点订单并出餐
5. `VOLUNTEER` 领取并完成配送任务

## 2. 当前实际技术栈

### 2.1 前端

- 框架：Vue 2.6
- 语言：TypeScript 3.9
- 构建：Vue CLI 3
- UI：Element UI
- 状态管理：Vuex
- 组件风格：`vue-property-decorator` class-based 组件
- 路由：`vue-router`，按 `meta.roles` 做角色控制

说明：

- 当前前端不是 `Vite + Pinia + Element Plus` 架构。
- 用户点餐与历史订单页面已经接入现有后台管理前端，而不是单独新建一套 C 端工程。

### 2.2 后端

- 框架：Spring Boot
- 持久层：MyBatis XML
- 数据库：MySQL
- 缓存：Redis
- 认证鉴权：JWT + Interceptor
- 统一返回：`Result<T>`

说明：

- 当前后端继续沿用现有 `JWT + Interceptor` 方案。
- 本项目当前没有引入 Spring Security。

## 3. 登录与权限现状

### 3.1 Token 约定

- 前后端统一使用请求头 `token`
- 员工端 token 当前包含：
  - `empId`
  - `role`
  - `diningPointId`
  - `username`
  - `name`
- C 端用户 token 当前包含：
  - `userId`
  - `role`
  - `username`
  - `name`

### 3.2 前端登录态

当前代码已经支持以下能力：

- 登录成功后优先使用后端返回的 `role`
- `token`、`role`、`user_info` 会写入 cookie
- 页面刷新后可从 cookie 恢复登录态和角色信息
- 路由守卫继续按 `meta.roles` 控制访问

### 3.3 后端基础权限

当前后端已经完成“够用”的员工端 URI 级权限控制：

- `ADMIN`：允许访问全部员工端接口
- `OPERATOR`：当前仅放行：
  - `/admin/order/**`
  - `/admin/shop/**`
  - `/admin/employee/logout`

说明：

- 这轮只完成了基础越权拦截骨架。
- “按助餐点做数据范围过滤”目前还没有全面落地。

## 4. 当前路由与角色入口

### 4.1 管理端与作业端

- `ADMIN`
  - `/dashboard`
  - `/statistics`
  - `/order`
  - `/diningPoint`
  - `/elderly`
  - `/familyProfile`
  - `/setmeal`
  - `/dish`
  - `/category`
  - `/employee`
- `OPERATOR`
  - `/order`
- `VOLUNTEER`
  - `/volunteer-overview`
  - `/volunteer-tasks`

### 4.2 家属端

- `FAMILY`
  - `/family-order`
  - `/family-history`

说明：

- 家属点餐页没有继续占用公共 `/order` 路径。
- 这样可以避免与后台订单调度页 `/order` 冲突。
- 志愿者当前默认入口为 `/volunteer-overview`。
- `个人概览` 与 `我的任务` 已拆为两个独立页面组件，不再复用同一个工作台组件。

## 5. 当前已落地的业务模块

### 5.1 组织与基础数据

当前代码中已经有以下核心模块或实体：

- 助餐点管理 `DiningPoint`
- 老人档案管理 `Elderly`
- 家属档案管理 `FamilyProfile`
- 员工管理 `Employee`
- 用户管理 `User`
- 订单主表与订单明细

其中角色字段已按当前实现对齐为：

- 员工端：`ADMIN / OPERATOR`
- 用户端：`FAMILY / VOLUNTEER`

### 5.2 订单调度主链路

当前代码中，订单调度相关后台页面与接口仍保留并可继续使用，包含：

- 订单列表查询
- 订单详情查询
- 管理员调度
- 助餐点开始制作
- 助餐点分配志愿者
- 助餐点标记出餐
- 助餐点确认取餐交接
- 管理端取消订单
- 志愿者配送完成

### 5.3 家属端点餐与历史订单

当前代码已经接入以下页面：

- `src/views/user-order/index.vue`
- `src/views/order-history/index.vue`
- `src/views/family-address/index.vue`
- `src/views/family-address/form.vue`

其中已落地能力包括：

- 菜品分类加载
- 菜品列表加载
- 按选中老人和所属助餐点加载可售菜品
- 后端购物车列表加载
- 后端购物车金额汇总
- 购物车加购、减购、整项删除、清空
- 购物车抽屉、底部栏、结算弹层共用真实金额
- 结算弹层中的地址选择、备注、预计送达、餐具模式
- 地址列表、地址选择、新增地址、编辑地址
- 默认地址自动选中与首个地址自动设为默认地址
- 历史订单分页查询
- 历史订单详情查看
- 历史订单金额拆分展示
- 取消订单
- 再来一单
- 催单
- 继续支付

说明：

- 当前 `/family-order` 的数量和金额已经改为“后端真相 + 前端单一状态源”：
  - 购物车条目来自 `/user/shoppingCart/list`
  - 金额汇总来自 `/user/shoppingCart/summary`
- 页面不再本地手算总价；购物车抽屉、底部栏、结算弹层统一读取同一份汇总结果。
- 结算草稿当前通过 `sessionStorage` 持久化 `remark`、`estimatedDeliveryTime`、`tablewareStatus`、`tablewareNumber`、`selectedAddressId`。
- 仓库中保留的 `Pinia` 相关草稿文件没有接入当前 Vue 2 主流程。
- 当前支付链路仍需特别注意：
  - FAMILY 下单后仍会立即调用 `PUT /user/order/payment`
  - 前端当前会对 `/user/order/submit` 和 `/user/order/payment` 的 `code` 做业务成功校验；若后端返回 `code = 0`，页面会直接展示真实错误，不再误报“订单创建成功”
  - 前端当前仍直接依赖 `/user/order/submit` 返回有效的 `data.id` 和 `data.orderNumber`
  - 如果后端返回体缺少 `id` 或 `orderNumber`，前端会提示“订单创建成功，但未返回有效订单信息”
- `/user/order/payment` 只允许处理 `status = 1` 的待支付订单；如果传入旧订单号、重复支付或订单已推进到其它状态，后端会返回“订单状态错误”
- 当前没有再通过历史订单反查、详情回查等前端兜底方式绕过这个问题

### 5.4 志愿者端工作台

当前代码已经接入以下页面：

- `src/views/volunteer/overview.vue`
- `src/views/volunteer/index.vue`

其中已落地能力包括：

- 左侧菜单新增 `个人概览` 与 `我的任务`
- 志愿者默认登录入口为 `个人概览`
- `个人概览` 页面展示：
  - 志愿者姓名
  - 当前服务状态
  - 累计服务单量
  - 累计服务时长
  - 综合评分
  - 等级
- `我的任务` 页面展示并处理：
  - `待取餐 / 配送中 / 已完成` 三个固定状态页签
  - 任务分页查询
  - 确认取餐
  - 完成配送
- 当前任务页 tab 与订单状态值映射固定为：
  - `待取餐 -> status = 4`
  - `配送中 -> status = 5`
  - `已完成 -> status = 6`
- 志愿者任务状态切换已按当前前端工作台体验收口：
  - `待取餐 -> 确认取餐成功 -> 自动切到配送中并刷新`
  - `配送中 -> 完成配送成功 -> 自动切到已完成并刷新`
- `完成配送` 当前使用页面内轻量确认弹窗：
  - 展示订单号、服务老人、收餐地址
  - 成功后先关闭弹窗，再切换到 `已完成`
  - 自动跳转过程中不再出现二次确认弹窗
  - 本轮不引入签收码、图片上传、签名等复杂签收能力

当前志愿者端接口包括：

- `GET /user/volunteer/overview`
- `GET /user/volunteer/orders`
- `PUT /user/volunteer/pickup/{id}`
- `PUT /user/volunteer/complete/{id}`

说明：

- 志愿者概览接口基于当前登录用户身份获取数据，前端不传 `volunteerId`。
- 概览加载失败时，当前前端会显示默认值，不影响任务页继续使用。

## 6. 当前订单状态约定

结合当前前后端页面与接口，现阶段建议统一按以下状态理解：

1. 待支付
2. 待调度
3. 制作中
4. 待取餐
5. 配送中
6. 已完成
7. 已取消

说明：

- 后续如果数据库枚举或接口状态码继续细化，应优先与实际代码同步。

## 7. 开发与联调注意事项

### 7.1 前端代理

当前开发代理已经拆分为：

- `/api/user` -> `http://localhost:8080`
- `/api` -> `http://localhost:8080/admin`

目的：

- 避免 `/user/**` 被错误代理到 `/admin/user/**`

### 7.2 SQL 执行

当前项目涉及至少两类数据库变更：

- 社区助餐相关结构变更
- 登录与角色权限骨架补充变更

需要重点注意：

- 仓库内已有 `login_role_auth_update.sql`
- 如果之前已经执行过旧版社区改造 SQL，`employee.dining_point_id` 可能已存在
- 此时再次执行新增 SQL 时，需要跳过重复的 `ALTER TABLE employee ADD COLUMN dining_point_id ...`

### 7.3 隐私与越权

涉及老人、家属、志愿者数据时，后续开发需要重点关注：

- 地址、电话等敏感信息脱敏
- 志愿者与家属越权访问
- 员工端跨助餐点访问
- 订单状态并发修改
- 志愿者统计数据的自动回写与长期准确性校验

## 8. 当前未完成事项

以下内容仍属于后续开发任务，而不是当前代码已完整实现的能力：

- 套餐加入购物车、套餐提交订单、套餐再来一单、套餐结算链路
- 历史订单更完整的售后、配送轨迹与签收凭证展示
- 真实支付回调失败后的退款、补偿与异常对账闭环
- 提交后支付链路的返回契约稳定化：
  - `/user/order/submit` 稳定返回 `OrderSubmitVO(id, orderNumber, orderAmount, orderTime)`
  - `/submit -> /payment` 串联时序、重复点击、旧订单号误用的联调收口
- 补贴政策、优惠分摊与 `subsidyAmount` 的真实业务启用
- 异常订单的页面化看板、通知与人工处理流
- 志愿者完整任务流程联调与统计自动回写
- 操作员按所属助餐点的数据隔离
- 真实环境下全角色联调测试
- 提交前的配置脱敏与测试材料整理

## 9. 文档使用说明

本说明文档以“当前仓库实际代码状态”为准。

后续如果继续改动以下任一内容，应同步更新本文档：

- 角色命名
- 登录返回结构
- token claims
- 路由入口
- 点餐与订单页面路径
- 权限拦截规则

## 10. 2026-03 最新规则补丁

以下内容覆盖本文档中与当前代码不一致的旧描述。

### 10.1 老人作为连接点

- 当前主规则是：`订单归属助餐点由老人决定，菜品助餐点只做合法性校验`
- `elderly.dining_point_id` 是订单归属助餐点的业务来源
- `orders.dining_point_id` 直接继承该老人绑定的助餐点
- `dish.dining_point_id` 仍然保留，但只负责校验菜品是否合法属于当前老人对应助餐点
- 当前阶段规则固定为：老人决定订单归属助餐点，菜品/套餐只允许属于该助餐点，助餐点休息时禁止产生新单

### 10.2 FAMILY 当前链路

当前 `FAMILY` 端已经不是“先看全部菜、结算时再选老人”，而是：

1. 先确定服务老人
2. 按老人绑定助餐点加载菜品
3. 购物车严格绑定该老人
4. 结算页只读展示当前老人
5. 提交订单时稳定携带 `elderId`

### 10.3 购物车规则

- `shopping_cart.elder_id` 当前已经持久化
- 同一 `FAMILY` 用户同一时刻只允许存在一个老人的购物车
- 切换到其他老人时，如果购物车非空，必须先清空购物车
- “同助餐点老人可共用购物车”已经不是当前规则

### 10.4 OPERATOR 当前定位

- `OPERATOR` 当前已从“只能看待制作/待取餐”升级为“本站点订单执行与配送协调中心”
- 只能查看和处理自己助餐点的订单
- 负责开始制作、分配志愿者、标记出餐、确认取餐交接

### 10.5 角色协作主链

当前系统应按以下链路理解：

`FAMILY 下单 -> 对应助餐点处理 -> 志愿者取餐配送`

### 10.6 管理员端老人档案中的所属助餐点

本轮已补齐管理员端“老人档案 -> 所属助餐点”的前后端读写链路，作为“老人决定订单归属助餐点”规则的基础数据入口。

- 统一文案：`所属助餐点`
- 业务含义：表示该老人默认由哪个助餐点提供助餐服务
- 管理员新增老人时必须选择所属助餐点
- 管理员编辑老人时必须保留或改为一个有效的所属助餐点
- 历史空值数据在列表页显示为 `未绑定`，但一旦进入新增/编辑保存流程，仍按必填校验处理

当前管理员端已落地的接口约定：

- `POST /admin/elderly`：接收 `ElderlyDTO`
- `PUT /admin/elderly`：接收 `ElderlyDTO`
- `GET /admin/elderly/page`：返回 `ElderlyVO` 分页结果，包含 `diningPointId`、`diningPointName`
- `GET /admin/elderly/{id}`：返回 `ElderlyVO` 详情，包含 `diningPointId`、`diningPointName`
- `GET /admin/diningPoint/list?status=1`：返回管理端老人表单使用的助餐点下拉数据

当前管理员端页面行为：

- 老人列表页直接展示所属助餐点名称，不再由前端本地反查
- 老人新增/编辑表单中的所属助餐点使用下拉选择
- 下拉数据只加载启用中的助餐点
- 若提交不存在或已停用的助餐点，后端会拒绝保存

本轮范围说明：

- 只补齐管理员端老人档案的数据维护入口
- 不改 `FAMILY` 点餐主流程、购物车、结算页和订单提交主逻辑

### 10.7 管理员端家属档案与 FAMILY 主数据

本轮已补齐管理员端“家属档案管理”前后端闭环，并完成家属主数据收口。

当前管理员端已落地的入口：

- 菜单路由：`/familyProfile`
- 页面能力：分页、筛选、新增、编辑、启停、删除
- 新增家属档案时支持两种方式：
  - 绑定已有 `FAMILY` 账号
  - 同步创建新的 `FAMILY` 账号并建档

当前管理员端已落地的接口约定：

- `GET /admin/familyProfile/page`
- `GET /admin/familyProfile/{id}`
- `POST /admin/familyProfile`
- `PUT /admin/familyProfile`
- `POST /admin/familyProfile/status/{status}?id=`
- `DELETE /admin/familyProfile?id=`
- `GET /admin/familyProfile/options`
- `GET /admin/familyProfile/familyUsers`

当前主数据规则：

- `family_profile` 只保留档案层字段：
  - `user_id`
  - `remark`
  - `status`
  - `create_time / update_time`
  - `create_user / update_user`
  - `is_deleted`
- 家属姓名统一来源于 `user.name`
- 家属联系电话统一来源于 `user.phone`
- `family_profile` 表中原有的 `name`、`phone` 字段已经删除
- 老人可绑定的家属必须同时满足：
  - `family_profile.status = 1`
  - `family_profile.is_deleted = 0`
  - `user.status = 1`
  - `user.role = 'FAMILY'`

与老人档案的关系规则：

- 老人底层仍通过 `elderly.user_id` 绑定 `FAMILY` 用户
- `family_profile` 主要作为管理员维护、启停、删除限制和展示入口，不替代底层关系字段
- 当前阶段不把老人底层关系字段改成 `family_profile_id`
- 老人新增/编辑时必须显式选择关联家属
- 老人列表与详情当前已返回：
  - `familyName`
  - `familyUsername`
  - `familyPhone`
  - `familyProfileId`
- 家属档案若仍关联老人，不允许删除，只允许停用
- 停用后的家属档案不允许继续作为老人新增/编辑时的可选项
- 已绑定该家属的历史老人记录仍允许展示，但应提示“关联家属已停用”或“当前不可选”
- 老人删除只做自身逻辑删除，不自动改变家属档案状态

### 10.8 2026-03 第二轮后端规则收口

本轮只处理“数据库与后端主规则收口”，不改前端页面，不扩套餐加入购物车、套餐下单、套餐再来一单、套餐结算链路。

当前补齐后的后端规则如下：

- `setmeal.dining_point_id` 成为正式业务字段，套餐必须显式归属一个助餐点
- 套餐内菜品必须全部属于同一个助餐点，且必须与套餐自身 `dining_point_id` 一致
- `dish.dining_point_id`、`setmeal.dining_point_id` 在管理端新增/编辑时都按必填处理
- 用户端 `GET /user/dish/list`、`GET /user/setmeal/list` 必须传 `diningPointId`
- 用户端可售查询不再返回全平台数据，只返回指定助餐点下 `status = 1` 的菜品/套餐
- `dining_point.status` 本轮统一按 `1 = 营业`、`0 = 休息` 解释
- 助餐点休息时：
  - 不允许用户端加载可售菜品/套餐
  - 不允许新加入购物车
  - 不允许提交新订单
  - 不允许“再来一单”回写购物车
- 订单创建前统一校验：
  - `elderId` 必须存在且属于当前 `FAMILY`
  - `elderly.dining_point_id` 必须存在
  - 目标助餐点必须营业
  - 购物车内所有菜品必须属于该老人当前助餐点
- `orders.dining_point_id` 始终取自 `elderly.dining_point_id`，不再从购物车内容反推
- 缓存键中，用户端套餐缓存已按 `diningPointId + categoryId` 组合；管理端修改菜品、套餐、助餐点时会统一清理用户可售缓存

本轮新增的增量 SQL：

- `setmeal_dining_point_supply_update.sql`

该脚本负责：

- 安全新增 `setmeal.dining_point_id`
- 创建 `idx_setmeal_dining_point_id`
- 按“套餐内有效菜品唯一助餐点”回填历史数据
- 输出未能唯一回填的历史套餐诊断结果

### 10.9 2026-03 第二轮 FAMILY 购物车、结算、地址与金额闭环

本轮只处理 `FAMILY` 端下单主链路，不改 `ADMIN / OPERATOR / VOLUNTEER` 页面，不扩套餐购物车和套餐下单。

当前 `FAMILY` 端的真实规则如下：

- 页面主入口：
  - `/family-order`
  - `/family-history`
  - `/family-addresses`
- `GET /user/shoppingCart/summary` 成为 `FAMILY` 金额真相，统一返回：
  - `elderId`
  - `diningPointId`
  - `totalCount`
  - `dishAmount`
  - `deliveryFee`
  - `tablewareFee`
  - `subsidyAmount`
  - `payAmount`
  - `effectiveTablewareNumber`
- `POST /user/shoppingCart/remove` 补齐了购物车整项删除能力。
- `OrdersSubmitDTO` 与前端 `submitOrder` 当前稳定携带：
  - `elderId`
  - `addressBookId`
  - `remark`
  - `estimatedDeliveryTime`
  - `deliveryStatus`
  - `tablewareStatus`
  - `tablewareNumber`
  - `dishAmount`
  - `deliveryFee`
  - `tablewareFee`
  - `subsidyAmount`
  - `payAmount`
- 后端会在提交时重新计算金额并和客户端金额做严格比对；金额不一致时会拒绝并提示刷新。
- 当前 FAMILY 主流程的金额规则固定为：
  - `dishAmount = sum(item.amount * item.number)`
  - `deliveryFee = 0`
  - `tablewareFee = effectiveTablewareNumber * 1`
  - `subsidyAmount = 0`
  - `payAmount = dishAmount + deliveryFee + tablewareFee - subsidyAmount`
- 餐具模式映射固定为：
  - 不需要：`tablewareStatus = 0`，`tablewareNumber = 0`
  - 按餐量提供：`tablewareStatus = 1`，数量以后端 `effectiveTablewareNumber = totalCount` 为准
  - 自定义：`tablewareStatus = 0`，`tablewareNumber > 0`
- 老人接口 `UserElderlyVO` 当前已返回 `diningPointStatus`，前端直接用它判断所属助餐点是否营业。
- 地址链路当前规则：
  - `/user/addressBook/default` 查不到默认地址时返回成功空值，不再作为异常
  - 地址列表按 `is_default desc, id desc` 排序
  - 用户首个地址自动设为默认地址
  - 地址页在 checkout 模式下返回 `/family-order?resumeCheckout=1&selectedAddressId=...`
- FAMILY 下单提交当前固定带 `deliveryStatus = 0`；后端在落库前也会为缺省值补 `deliveryStatus = 0`，避免 `orders.delivery_status` 非空约束导致整笔订单回滚。
- 下单成功后会在同一事务内清空后端购物车，并跳转 `/family-history?createdOrderId=...` 高亮新订单。
- 如果“提交成功但支付失败”，当前也按“订单已创建”处理：
  - 不保留购物车
  - 跳历史订单继续支付
  - 避免重复下单

### 10.10 2026-03 第三轮助餐点休息态、定时任务与统计收口

本轮只处理“助餐点休息态影响范围 + 订单定时任务 + 统计 SQL/页面口径统一”。

当前规则如下：

- 助餐点切为休息后，只阻断新单生成链路：
  - 不允许加载该点可售菜品/套餐
  - 不允许新加入购物车
  - 不允许提交新订单
  - 不允许“再来一单”回写购物车
  - 不允许待支付订单继续支付推进
- 助餐点切为休息后，不批量改已有订单：
  - `1 待支付`：保留原订单，不能继续支付，仍按支付超时规则自动取消
  - `2 待调度`：允许继续
  - `3 制作中`：允许继续
  - `4 待取餐`：允许继续
  - `5 配送中`：允许继续
  - `6 已完成 / 7 已取消`：不处理
- `payment()` 在把订单从 `待支付` 推进到 `待调度` 前，会再次校验订单所属助餐点是否营业。
- `paySuccess()` 也增加了同层保护，避免绕过前置支付接口直接把休息态订单推进到 `待调度`。
- 定时任务当前只保留合理逻辑：
  - 每分钟扫描待支付超时订单，超 15 分钟自动取消，取消原因写为“支付超时自动取消”
  - 不再保留“配送中超过 1 天自动完成”的旧外卖逻辑
  - 额外增加超时履约识别，只做日志/统计，不改订单状态
- 异常履约识别口径：
  - 优先使用 `estimated_delivery_time`
  - 为空时回退 `expected_time`
  - 两者都为空时，本轮不纳入异常识别
  - `3 制作中` 超时：制作中超时
  - `4 待取餐` 超时：待取餐超时
  - `5 配送中` 超时：配送中超时
- 统计口径统一按当前状态语义执行：
  - `1` 待支付
  - `2` 待调度
  - `3` 制作中
  - `4` 待取餐
  - `5` 配送中
  - `6` 已完成
  - `7` 已取消
- 销量 Top10、营业额、有效订单等统计统一只认 `status = 6` 的已完成订单。
- 为兼容已有前后端字段名，部分 VO 仍保留历史包袱，但当前语义已经固定为：
  - `OrderStatisticsVO.toBeConfirmed = 2 待调度`
  - `OrderStatisticsVO.confirmed = 3 制作中`
  - `OrderStatisticsVO.deliveryInProgress = 4 待取餐`
  - `OrderOverViewVO.waitingOrders = 2 待调度`
  - `OrderOverViewVO.deliveredOrders = 3 制作中`
  - `OrderOverViewVO.completedOrders = 6 已完成`
  - `OrderOverViewVO.cancelledOrders = 7 已取消`

### 10.11 2026-03 第二轮前端管理页收口

本轮只处理与第二轮规则直接相关的前端管理页、FAMILY 金额摘要和文档同步，不新增新的业务方向。

当前前端收口状态如下：

- 管理端菜品页面：
  - 新增/编辑表单必须提交 `diningPointId`
  - 助餐点下拉默认只展示营业中的助餐点
  - 编辑历史记录时，如果当前绑定的是休息中助餐点：
    - 当前旧值继续回显
    - 标签显示为 `名称（休息中）`
    - 其它可切换选项只包含营业中的助餐点
    - 不改该字段时允许保存其它字段
    - 改字段后只能切到营业中的助餐点
  - 列表页当前已展示所属助餐点名称
- 管理端套餐页面：
  - 新增/编辑表单必须提交 `diningPointId`
  - 套餐列表页展示所属助餐点名称
  - 套餐选菜弹层当前会带上 `diningPointId` 查询 `/admin/dish/list`
  - 分类切换、关键字搜索都只查询当前助餐点下的菜品
  - 切换套餐所属助餐点时，若已有已选菜品，前端会提示并清空已选菜品后再重新选择
- FAMILY 点餐页：
  - 当前仍只接菜品，不扩展套餐 UI
  - 所有 `/user/dish/list` 查询始终携带当前老人所属 `diningPointId`
  - 进入页面、刷新购物车、打开结算、提交订单时都会重新校验老人所属助餐点状态
  - 助餐点休息时，前端统一提示“当前所属助餐点已休息，暂不可点餐/下单”
- FAMILY 金额展示：
  - 购物车抽屉、底部结算栏、结算弹层、历史订单详情当前统一展示：
    - 菜品金额
    - 配送费
    - 餐具费
    - 实付金额
  - `subsidyAmount` 当前固定为 `0`，在详细视图继续展示为 `0`
  - 底部结算栏已经不再只显示总价，而是与其它页面保持同一金额口径的摘要展示

### 10.12 2026-03 当前支付链路问题说明

当前支付功能的状态应按以下方式理解：

- 后端模拟支付当前已按最小改动方式统一收口：
  - `payment(orderNumber)` 只做必要校验和模拟支付发起
  - 支付成功后的状态更新统一走 `paySuccess(orderNumber)`
  - 历史订单“继续支付”和 `/notify/paySuccess` 也复用 `paySuccess(orderNumber)`
- `paySuccess(orderNumber)` 当前统一负责：
  - 查询订单
  - 仅允许 `1 待支付` 订单推进
  - 将订单更新为 `2 待调度`
  - 将 `pay_status` 更新为 `1 已支付`
  - 写入 `checkout_time`
  - 触发现有 WebSocket 来单提醒
- 前端当前已经不再把 `code = 0` 的下单或支付响应误当成成功。
- 当前仍未完全收口的问题集中在 `/user/order/submit` 返回契约：
  - 前端依赖返回 `id + orderNumber`
  - 如果返回缺字段、字段结构不一致，前端仍无法安全继续支付
- 为避免误拿历史订单号、旧订单号或非当前订单信息，当前版本已经明确不再用前端回查历史订单的方式兜底支付。
- 因此，后续支付链路的继续优化方向仍应优先放在后端返回契约稳定化，而不是继续扩大前端兜底逻辑。
