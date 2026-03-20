# 社区老年助餐服务系统 - 开发任务清单

## 当前技术基线
- 前端：`sky-admin-vue`，Vue 2 + TypeScript + Element UI + Vuex
- 后端：`sky-take-out`，Spring Boot + MyBatis XML + JWT + Interceptor
- 权限方案：继续沿用现有 JWT + Interceptor，不引入 Spring Security
- 当前角色口径：`ADMIN / OPERATOR / VOLUNTEER / FAMILY`

## Phase 1：数据库与基础结构
- [x] 提供社区助餐基础表变更脚本
  - [x] `community_meal_update.sql`
  - [x] 包含 `dining_point`、`elderly`、`volunteer_stats`
  - [x] 包含 `orders`、`dish`、`user`、`employee` 的社区化字段扩展
- [x] 提供登录与角色权限补丁脚本
  - [x] `login_role_auth_update.sql`
  - [x] `employee.role`
  - [x] `employee.dining_point_id`
  - [x] `employee/user` 明文 `123456` 转 `md5`
- [ ] 在本地数据库实际执行并校验以上脚本
- [x] 后端实体类已补齐核心字段
  - [x] `Employee.role`
  - [x] `Employee.diningPointId`
  - [x] `User.role`
  - [x] 订单、菜品、助餐点相关社区字段已进入代码模型

## Phase 2：登录、角色与权限骨架
- [x] 员工端支持 `ADMIN / OPERATOR` 登录
- [x] C 端支持 `FAMILY / VOLUNTEER` 角色识别
- [x] 员工 token 携带 `empId / role / diningPointId / username / name`
- [x] 用户 token 携带 `userId / role / username / name`
- [x] `BaseContext` 已扩展为统一认证上下文
  - [x] `currentId`
  - [x] `currentRole`
  - [x] `currentDiningPointId`
- [x] `JwtTokenAdminInterceptor` 已完成最小可用 URI 级拦截
  - [x] `ADMIN` 放行全部员工端接口
  - [x] `OPERATOR` 仅允许 `/admin/order/**`、`/admin/shop/**`
  - [x] 补充放行 `/admin/employee/logout`
- [x] `JwtTokenUserInterceptor` 已写入用户上下文并在请求结束后清理
- [ ] 基于 `currentDiningPointId` 的业务数据范围限制尚未系统接入
  - [ ] 例如 `OPERATOR` 查询订单时仅看自己助餐点

## Phase 3：前端登录态、菜单与员工管理
- [x] 开发代理已分流
  - [x] `/api/user -> http://localhost:8080`
  - [x] `/api -> http://localhost:8080/admin`
- [x] 前端统一通过请求头 `token` 发送 JWT
- [x] 登录成功后优先使用后端返回 `role`
- [x] `token / role / user_info` 已持久化到 cookie
- [x] 刷新页面后可从 cookie 恢复登录态和角色
- [x] 路由守卫按 `meta.roles` 做拦截
- [x] 默认首页已按角色跳转
  - [x] `ADMIN -> /dashboard`
  - [x] `OPERATOR -> /order`
  - [x] `VOLUNTEER -> /volunteer-tasks`
  - [x] `FAMILY -> /family-order`
- [x] 侧边栏已按当前角色过滤可见菜单
- [x] 员工新增/编辑页面已补 `role` 和 `diningPointId`
- [x] 员工列表页已补 `role` 和 `diningPointId` 展示
- [ ] `VOLUNTEER / FAMILY` 的完整业务菜单与接口联调还未全部收尾

## Phase 4：社区助餐业务页面现状
- [x] 助餐点管理页面已存在
- [x] 老人档案管理页面已存在
- [x] 志愿者任务页面已存在
- [x] 家属点餐页与历史订单页已接入当前 Vue 2 工程
- [x] 家属菜单已指向新的点餐/历史订单页面
- [x] 旧订单后台页面编译兼容已恢复
- [ ] 点餐页仍缺完整结算链路
  - [ ] 地址选择
  - [ ] 备注填写
  - [ ] 支付方式
  - [ ] 真正提交订单
- [ ] 历史订单页仍缺完整详情与后续动作闭环
  - [ ] 更完整的详情展示
  - [ ] 退款/售后类动作
  - [ ] 物流/配送轨迹

## Phase 5：订单调度与业务联调
- [ ] 下单 -> 调度 -> 制作 -> 取餐 -> 配送 -> 完成 全流程联调
- [ ] `ADMIN` 指派志愿者的调度闭环完善
- [ ] `OPERATOR` 制作中 / 待取餐 / 出餐确认链路完善
- [ ] `VOLUNTEER` 接单 / 配送 / 完成链路完善
- [ ] 订单状态流转与页面按钮状态逐项核对
- [ ] 数据范围与越权场景回归
  - [ ] `OPERATOR` 访问 `/admin/employee/page` 返回 403
  - [ ] `ADMIN` 访问员工管理返回 200
  - [ ] `FAMILY` 刷新后不丢角色
  - [ ] `VOLUNTEER` 刷新后仍可进入任务页

## Phase 6：提交前收尾
- [ ] 在真实数据库执行角色补丁 SQL
- [ ] 准备测试账号
  - [ ] `ADMIN`
  - [ ] `OPERATOR`
  - [ ] `FAMILY`
  - [ ] `VOLUNTEER`
- [ ] 清理配置中的敏感信息
- [ ] 补最终接口/功能验收记录
- [ ] 补答辩演示脚本和操作说明

## 当前阻塞与注意事项
- 当前代码里的登录、角色、菜单、基础权限骨架已经打通，但数据库必须先执行最新 SQL。
- `employee.dining_point_id` 如果之前已执行过 `community_meal_update.sql`，再执行 `login_role_auth_update.sql` 时要跳过重复加列。
- 本轮没有重构全部 `family/volunteer` 业务接口，也没有引入 Spring Security。
- 本轮重点已经完成到“登录成功、角色可识别、刷新不丢、菜单正确、员工端越权能拦住”。

## 2026-03 最新落地补充

以下内容为当前代码已落地能力，用于覆盖上文中仍保留的旧 TODO。

### 已落地：老人驱动订单归属

- [x] `elderly.dining_point_id` 已成为正式业务字段
- [x] 下单时由老人决定 `orders.dining_point_id`
- [x] 菜品助餐点只做合法性校验

### 已落地：FAMILY 点餐与购物车

- [x] `/family-order` 已改成先选老人，再按老人助餐点加载菜品
- [x] `shopping_cart.elder_id` 已持久化
- [x] 购物车当前严格绑定老人
- [x] 切换老人时，如购物车非空，必须先清空购物车
- [x] 结算页当前已只读展示老人并真实提交订单

### 已落地：OPERATOR 执行中心

- [x] `OPERATOR` 只能查看所属助餐点订单
- [x] 已支持开始制作
- [x] 已支持分配志愿者
- [x] 已支持标记出餐完成
- [x] 已支持确认志愿者取餐

### 已落地：管理员端家属档案与主数据收口

- [x] 管理员端已新增 `/familyProfile` 家属档案管理页面
- [x] 家属档案新增支持“绑定已有 FAMILY 账号”与“同步创建 FAMILY 账号并建档”
- [x] 老人档案新增/编辑已要求显式选择关联家属
- [x] 家属姓名主数据已统一收口到 `user.name`
- [x] 家属联系电话主数据已统一收口到 `user.phone`
- [x] `family_profile` 表已删除 `name`、`phone` 字段，仅保留档案层字段
- [x] 家属档案 VO 仍返回 `name`、`phone`，但来源改为 `user` 联查

### 仍待继续

- [ ] 历史脏数据清理与补录
- [ ] 全角色完整联调回归记录
- [ ] 更细的异常工单、统计与展示优化
