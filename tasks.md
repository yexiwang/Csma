# 社区老年助餐服务系统 - 开发任务清单

## 当前技术基线

- 前端：`sky-admin-vue`，Vue 2 + TypeScript + Element UI + Vuex
- 后端：`sky-take-out`，Spring Boot + MyBatis XML + JWT + Interceptor
- 权限方案：继续沿用现有 JWT + Interceptor，不引入 Spring Security
- 当前角色口径：`ADMIN / OPERATOR / VOLUNTEER / FAMILY`

## 当前已落地里程碑

- 数据库与主数据
  - 已补齐社区助餐基础结构、角色补丁与家属档案增量 SQL
  - `family_profile` 已落地为家属档案业务表
  - `family_profile` 已删除 `name`、`phone` 字段，家属姓名/电话主数据统一来自 `user`
- 登录、鉴权与角色
  - 员工端已支持 `ADMIN / OPERATOR`
  - 用户端已支持 `FAMILY / VOLUNTEER`
  - JWT、`BaseContext`、路由守卫、菜单过滤已闭环
- 老人、家属与助餐点主链路
  - 管理员端已支持老人档案管理
  - 管理员端已支持家属档案管理 `/familyProfile`
  - 老人新增/编辑已要求显式选择关联家属
  - 老人新增/编辑已要求显式选择所属助餐点
- FAMILY 端基础链路
  - `/family-order` 已改成先选老人，再按老人所属助餐点加载菜品
  - `shopping_cart.elder_id` 已持久化
  - 购物车当前严格绑定老人
  - 结算页当前已能按老人真实提交订单
- OPERATOR 执行链路
  - `OPERATOR` 当前只能查看所属助餐点订单
  - 已支持开始制作、分配志愿者、标记出餐完成、确认志愿者取餐

## 当前固定规则

### 1. 老人与家属的底层绑定规则

- 当前阶段老人底层仍通过 `elderly.user_id -> user.id` 绑定 `FAMILY` 用户
- `family_profile` 主要作为管理员维护、启停、删除限制和展示入口
- 当前阶段不把老人底层关系字段改成 `family_profile_id`

### 2. 老人可绑定家属的有效性规则

管理员端“老人新增/编辑”时，家属必须同时满足以下条件，才允许作为可选项和可保存目标：

- `family_profile.status = 1`
- `family_profile.is_deleted = 0`
- `user.status = 1`
- `user.role = 'FAMILY'`

### 3. 家属档案停用与删除联动规则

- 家属档案若仍关联未删除老人，不允许删除，只允许停用
- 当前 `DELETE /admin/familyProfile` 为逻辑删除，不做物理删除
- 停用后的家属档案，不允许再作为老人新增/编辑时的可选项
- 已绑定该家属的历史老人记录仍允许展示
- 管理端编辑历史老人时，如当前绑定家属已停用或当前不可选，页面应保留回显并给出提示

### 4. 老人删除联动规则

- 老人删除优先走逻辑删除
- 若老人已有关联订单记录，不允许删除
- 老人删除不会自动改变家属档案状态

### 5. 订单归属与助餐点规则

- 老人决定订单归属助餐点
- 订单归属链路固定为：`elderId -> elderly.dining_point_id -> orders.dining_point_id`
- 菜品/套餐只允许属于该助餐点
- 助餐点休息时禁止产生新单

## 当前仅保留未完成项

### 数据与环境

- 在真实数据库执行并复核全部增量 SQL 与索引
- 清理历史脏数据并补录必要主数据
- 准备完整测试账号：`ADMIN / OPERATOR / FAMILY / VOLUNTEER`
- 清理配置中的敏感信息

### FAMILY 端与订单展示

- 地址选择、备注填写、支付方式等结算补充能力
- 历史订单更完整的详情展示
- 售后/退款类动作设计与落地
- 配送轨迹与签收展示
- 补贴金额、自付金额等展示优化

### 调度、执行与联调

- `ADMIN -> OPERATOR -> VOLUNTEER` 全流程联调回归
- `ADMIN` 指派志愿者的调度闭环补强
- `VOLUNTEER` 接单、配送、完成链路回归
- 订单状态流转与页面按钮状态逐项核对
- 全角色越权场景回归

### 统计、异常与展示优化

- 更细的异常工单与人工干预记录
- 更完整的统计口径与展示页
- 老人、家属、志愿者敏感信息脱敏方案
- 更严格的外键、索引与数据隔离梳理
