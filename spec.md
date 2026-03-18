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
  - `/setmeal`
  - `/dish`
  - `/category`
  - `/employee`
- `OPERATOR`
  - `/order`
- `VOLUNTEER`
  - `/volunteer-tasks`

### 4.2 家属端

- `FAMILY`
  - `/family-order`
  - `/family-history`

说明：

- 家属点餐页没有继续占用公共 `/order` 路径。
- 这样可以避免与后台订单调度页 `/order` 冲突。

## 5. 当前已落地的业务模块

### 5.1 组织与基础数据

当前代码中已经有以下核心模块或实体：

- 助餐点管理 `DiningPoint`
- 老人档案管理 `Elderly`
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
- 接单/拒单
- 取消订单
- 配送中
- 完成订单

### 5.3 家属端点餐与历史订单

当前代码已经接入以下页面：

- `src/views/user-order/index.vue`
- `src/views/order-history/index.vue`

其中已落地能力包括：

- 菜品分类加载
- 菜品列表加载
- 本地购物车数量维护
- 历史订单分页查询
- 历史订单详情查看
- 取消订单
- 再来一单
- 催单

说明：

- 当前购物车主要通过页面本地状态和 `localStorage` 维护。
- `Pinia` 草稿 store 文件仍在仓库中保留，但还没有正式接入当前 Vue 2 工程。

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

## 8. 当前未完成事项

以下内容仍属于后续开发任务，而不是当前代码已完整实现的能力：

- 家属端完整结算流程
- 老人选择与代下单完整表单
- 补贴计算展示闭环
- 志愿者完整任务流程联调
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
