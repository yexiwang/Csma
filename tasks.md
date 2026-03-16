# 社区老年助餐服务系统 - 开发任务清单 (Tasks)

## Phase 1: 数据库与后端基础改造 (Backend Foundation)
- [ ] **数据库变更脚本 (SQL)**
  - [ ] 创建 `dining_point` 表 (助餐点)。
  - [ ] 创建 `elderly` 表 (老人档案)。
  - [ ] 创建 `volunteer_stats` 表 (志愿者服务记录)。
  - [ ] 修改 `employee` 表 (增加 `dining_point_id`)。
  - [ ] 修改 `user` 表 (增加 `username`, `password`, `role`, `name`)。
  - [ ] 修改 `orders` 表 (增加 `elder_id`, `volunteer_id`, `dining_point_id`, `subsidy_amount`, `expected_time`)。
  - [ ] 修改 `dish` 表 (增加 `dining_point_id`, `nutrition_tags`)。

- [ ] **后端实体类更新 (POJO)**
  - [ ] 创建/更新 Entity 类 (`DiningPoint`, `Elderly`, `VolunteerStats`).
  - [ ] 更新 DTO/VO 类 (`UserLoginDTO`, `OrderSubmitDTO`, `ElderlyDTO`).
  - [ ] 配置 MyBatis Mapper XML 文件。

- [ ] **基础 CRUD 接口**
  - [ ] `DiningPointController`: 增删改查助餐点。
  - [ ] `ElderlyController`: 老人档案管理 (增删改查)。
  - [ ] `UserController`: 用户注册/登录 (支持多角色)。

## Phase 2: 核心业务逻辑实现 (Core Logic)
- [ ] **订单调度模块**
  - [ ] `OrderService.submitOrder`: 下单逻辑 (校验老人信息、计算补贴)。
  - [ ] `OrderService.dispatchOrder`: 派单逻辑 (管理员指派志愿者)。
  - [ ] `OrderService.completeOrder`: 志愿者确认送达逻辑 (更新状态、记录服务时长)。

- [ ] **助餐点作业模块**
  - [ ] `OrderService.queryByPoint`: 查询本点订单。
  - [ ] `OrderService.markMealReady`: 标记出餐。

- [ ] **权限控制 (Security)**
  - [ ] 配置 Spring Security 拦截器，区分 `/admin/**` (管理员), `/operator/**` (操作员), `/volunteer/**` (志愿者), `/family/**` (家属)。
  - [ ] 实现 JWT Token 生成与解析，包含 Role 信息。

## Phase 3: 前端页面开发 (Frontend - Vue 2)
- [ ] **项目初始化**
  - [ ] 基于 `vue-element-admin` 或 Sky Admin 初始化项目。
  - [ ] 配置动态路由 (`permission.js`)，根据 Role 加载不同菜单。

- [ ] **公共组件**
  - [ ] 封装适老化文本组件 (`BigText`).
  - [ ] 封装高对比度主题配置。

- [ ] **管理端视图 (Admin)**
  - [ ] 助餐点管理页面。
  - [ ] 老人档案管理页面。
  - [ ] 订单调度中心 (待调度订单列表 + 志愿者选择弹窗)。

- [ ] **操作员视图 (Operator)**
  - [ ] 简易订单列表 (待制作/待取餐)。
  - [ ] 出餐确认按钮。

- [ ] **志愿者视图 (Volunteer)**
  - [ ] 任务看板 (待取餐/配送中)。
  - [ ] 任务详情页 (显示地址、老人需求)。

- [ ] **家属/老人视图 (Family)**
  - [ ] 首页 (菜品浏览 + 营养标签筛选)。
  - [ ] 下单页 (选择老人、地址、时间)。
  - [ ] 订单列表页。

## Phase 4: 联调与测试 (Integration & Testing)
- [ ] **全流程联调**
  - [ ] 下单 -> 派单 -> 制作 -> 取餐 -> 送达。
  - [ ] 验证数据一致性 (数据库状态)。

- [ ] **适老化验收**
  - [ ] 检查字体大小是否符合标准 (≥16px)。
  - [ ] 检查颜色对比度。

- [ ] **文档完善**
  - [ ] API 接口文档 (Swagger)。
  - [ ] 用户操作手册。
