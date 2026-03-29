---
alwaysApply: false
description: 
---
# 社区助餐项目后端开发规则

## 1. 当前项目技术基线

本项目后端以当前仓库代码为准，技术栈如下：

- Spring Boot
- MyBatis XML
- MySQL
- Redis
- JWT + Interceptor
- Maven
- Lombok
- Swagger / Knife4j

后续开发默认遵循这套技术路线。

### 1.1 当前不采用的方案

除非明确发起整套改造，否则不要擅自引入：

- Spring Security
- JPA / Hibernate 作为主持久层
- 注解 SQL 替代现有 MyBatis XML 主方案

## 2. 模块与分层

当前项目模块划分为：

- `sky-common`
- `sky-pojo`
- `sky-server`

职责约定：

- `sky-common`：常量、工具类、上下文、统一返回、异常等
- `sky-pojo`：Entity、DTO、VO
- `sky-server`：Controller、Service、Mapper、配置、拦截器、任务等

## 3. 编码约束

### 3.1 Controller 层

- 只做参数接收、日志记录、调用 Service、返回 `Result<T>`
- 不要在 Controller 里堆大段业务逻辑
- 登录、分页、编辑等接口继续沿用现有路径风格

### 3.2 Service 层

- 负责业务逻辑、校验、事务边界
- 角色相关约束优先放在 Service 层处理
- 例如员工保存/更新时，继续保持：
  - `role` 为空默认 `ADMIN`
  - `OPERATOR` 必须绑定 `diningPointId`
  - `ADMIN` 自动清空 `diningPointId`

### 3.3 Mapper 层

- 优先使用 MyBatis XML
- 复杂 SQL 不要塞到注解里
- 数据库字段继续使用 `snake_case`
- Java 属性继续使用 `camelCase`

## 4. 统一返回与模型使用

### 4.1 返回结构

接口统一返回：

- `Result<T>`

不要新造一套和现有工程并行的响应格式。

### 4.2 模型分层

- `Entity`：数据库实体
- `DTO`：入参对象
- `VO`：出参对象

原则：

- 不直接把 `Entity` 当成所有接口出参
- 敏感字段优先通过 `VO` 控制
- 避免在 Controller / Service 中大量用 `Map` 代替 DTO/VO

## 5. 当前认证与权限规则

### 5.1 认证方式

当前继续沿用：

- JWT
- `JwtTokenAdminInterceptor`
- `JwtTokenUserInterceptor`

不要在本轮开发中替换成 Spring Security。

### 5.2 Token 约定

当前请求头统一使用：

- `token`

员工 token 当前包含：

- `empId`
- `role`
- `diningPointId`
- `username`
- `name`

用户 token 当前包含：

- `userId`
- `role`
- `username`
- `name`

### 5.3 上下文

当前统一上下文使用：

- `BaseContext`

当前至少包含：

- `currentId`
- `currentRole`
- `currentDiningPointId`

拦截器写入后必须在请求结束时清理 `ThreadLocal`。

## 6. 当前角色口径

### 6.1 员工端

来源表：`employee`

- `ADMIN`
- `OPERATOR`

### 6.2 用户端

来源表：`user`

- `FAMILY`
- `VOLUNTEER`

后续代码、SQL、文档统一按这套命名，不再使用旧的缩写或别名。

## 7. 当前基础权限骨架

员工端当前已落地的越权拦截思路是 URI 级控制。

规则现状：

- `ADMIN`：允许全部员工端接口
- `OPERATOR`：当前仅允许：
  - `/admin/order/**`
  - `/admin/shop/**`
  - `/admin/employee/logout`

说明：

- 本轮只维护这套“够用”的权限骨架
- 不要擅自扩成复杂注解权限框架
- 更细的助餐点数据隔离可以在后续查询层逐步补

## 8. 数据库与 SQL 约束

### 8.1 当前关键脚本

当前应优先参考：

- `community_meal_update.sql`
- `login_role_auth_update.sql`

### 8.2 密码兼容现状

当前登录代码仍使用 MD5 校验密码。

因此：

- 涉及 employee/user 初始化密码时，应与当前实现兼容
- 不要在未整体迁移前局部改成 BCrypt 或其他方案

### 8.3 社区助餐关键字段

后续涉及这些业务时，需优先沿用现有字段：

- `employee.role`
- `employee.dining_point_id`
- `user.role`
- `orders.elder_id`
- `orders.volunteer_id`
- `orders.dining_point_id`
- `orders.subsidy_amount`
- `orders.personal_pay`
- `orders.expected_time`
- `dish.dining_point_id`
- `dish.nutrition_tags`
- `dish.suitability`

## 9. 日志、异常与公共字段

- 使用 `@Slf4j`
- 关键业务操作保留日志
- 业务异常优先抛给全局异常处理
- 公共字段继续沿用现有 AOP 自动填充方案

## 10. 开发注意事项

### 10.1 不要打断现有订单链路

当前后台订单页、志愿者页、家属历史订单页已经共同依赖现有订单实体与接口。

因此：

- 修改订单相关接口时优先做兼容
- 不要为了新功能随手删掉旧接口出口

### 10.2 操作员功能开发

后续新增 `OPERATOR` 功能时，应优先考虑：

- 使用 `BaseContext.getCurrentDiningPointId()` 作为数据范围依据
- 查询结果只返回所属助餐点数据

### 10.3 敏感配置

配置文件中涉及数据库、OSS、微信等敏感信息时：

- 不要在新文档、日志或示例中再次扩散
- 正式提交前应尽量改为环境化配置

## 11. 构建与验证

当前常用命令：

- 编译：`mvn -q -DskipTests compile`
- 测试：按模块实际情况执行 Maven test

提交前至少保证：

- 后端能正常编译
- 登录接口不报错
- 角色 claim 不丢
- 基础越权拦截不失效

## 12. 文档维护要求

如果以下内容变化，需要同步更新本规则文档：

- 角色模型
- token claims
- 拦截器权限规则
- SQL 初始化脚本
- 密码方案
- 订单扩展字段

## 13. 2026-03 最新后端规则补充

以下内容覆盖文档中与当前实现不一致的旧描述。

### 13.1 老人驱动订单归属

- `elderly.dining_point_id` 是订单归属助餐点的业务来源
- `orders.dining_point_id` 由老人继承
- `dish.dining_point_id` 只做合法性校验
- 不允许回退为“从购物车菜品反推订单归属助餐点”

### 13.2 购物车规则

- `shopping_cart.elder_id` 当前已经持久化
- `ShoppingCartDTO`、`ShoppingCart`、购物车相关接口都应显式处理 `elderId`
- 同一 `FAMILY` 用户同一时刻只允许存在一个老人的购物车

### 13.3 `submitOrder` 规则

- `elderId` 为必填
- 提交订单时必须校验：
  - 老人存在
  - 老人属于当前 FAMILY
  - 老人已绑定助餐点
  - 购物车项属于当前老人
  - 菜品属于当前老人助餐点

### 13.4 OPERATOR 数据范围

- `OPERATOR` 查询和操作订单时，必须按 `orders.dining_point_id = currentDiningPointId` 收口
- 不允许只靠前端隐藏按钮控制 `OPERATOR` 越权
