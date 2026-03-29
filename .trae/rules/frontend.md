---
alwaysApply: false
description: 
---
# 社区助餐项目前端开发规则

## 1. 当前项目技术基线

本项目当前前端以实际仓库代码为准，技术栈如下：

- Vue 2.6
- TypeScript 3.9
- Vue CLI 3 / Webpack 4
- Element UI
- Vuex + `vuex-module-decorators`
- Vue Router 3
- SCSS
- Axios
- `vue-property-decorator` class-based 组件

后续开发默认遵循这套约束。

### 1.1 当前不作为主方案的技术

除非明确发起整套迁移，否则不要默认引入以下写法作为主流程实现：

- Vue 3 Composition API
- `<script setup>`
- Element Plus
- Vite
- Pinia

说明：

- 仓库里保留了 Pinia 草稿文件，但当前页面没有正式接入。
- 新功能应优先接到现有 Vue 2 + Vuex 工程上。

## 2. 目录与职责

推荐继续遵循当前目录分工：

- `src/api`：按业务模块拆分接口
- `src/views`：页面级组件
- `src/components`：复用组件
- `src/store/modules`：Vuex 模块
- `src/layout`：主布局
- `src/utils/request.ts`：统一请求封装
- `src/permission.ts`：路由守卫
- `src/router.ts`：路由配置

## 3. 组件与页面开发约束

### 3.1 默认组件写法

默认使用 class-based 组件风格：

- `vue-property-decorator`
- `@Component`
- `export default class Xxx extends Vue`

组件应显式声明 `name`。

### 3.2 命名建议

- 页面和复用组件文件优先保持现有工程风格
- 组件 `name` 使用 PascalCase
- props 使用 camelCase
- 自定义事件使用 kebab-case

### 3.3 样式约束

- 默认使用 `lang="scss"`
- 页面样式优先 `scoped`
- 公共变量与 mixin 继续沿用 `src/styles/_variables.scss`、`src/styles/_mixins.scss`
- 视觉风格优先延续现有管理端样式，不额外引入新的设计体系

## 4. 路由与菜单规则

### 4.1 角色控制

当前路由与菜单按 `meta.roles` 控制。

角色口径统一为：

- `ADMIN`
- `OPERATOR`
- `VOLUNTEER`
- `FAMILY`

新增页面时：

- 无角色限制的页面可不写 `meta.roles`
- 有角色限制的页面必须显式声明 `meta.roles`

### 4.2 默认首页

当前未授权跳转逻辑应保持一致：

- `ADMIN` -> `/dashboard`
- `OPERATOR` -> `/order`
- `VOLUNTEER` -> `/volunteer-tasks`
- `FAMILY` -> `/family-order`

### 4.3 路径冲突约束

当前后台订单调度页使用：

- `/order`

当前家属端点餐与历史订单页使用：

- `/family-order`
- `/family-history`

后续新增家属端页面时，不要再复用公共 `/order`，避免和后台订单调度入口冲突。

## 5. 登录态与权限态

### 5.1 当前存储方式

当前用户状态由 Vuex `user` 模块统一管理，并通过 cookie 恢复。

关键键名约定：

- `token`
- `role`
- `user_info`

说明：

- `user_info` 统一存 JSON 字符串
- 不要再混用 `userInfo` / `user_info`

### 5.2 登录逻辑

登录成功后：

- 优先使用后端返回的 `role`
- 再写入 Vuex
- 再写入 cookie

刷新页面后：

- 从 cookie 恢复 `token / role / user_info`

## 6. 请求封装与代理规则

### 6.1 请求头

当前前后端统一使用：

- `token`

新接口不要再区分 admin/user 两套 token header。

### 6.2 开发代理

当前开发代理规则为：

- `/api/user` -> `http://localhost:8080`
- `/api` -> `http://localhost:8080/admin`

并且必须保证：

- `/api/user` 优先匹配

### 6.3 接口文件约束

接口统一写在 `src/api` 下，按领域拆分。

当前要特别注意：

- `src/api/order.ts` 仍承担旧后台订单接口兼容职责
- 如果继续拆分家属端点餐接口，不能把后台 `dashboard`、`orderDetails`、`volunteer` 等旧页面导入打断

## 7. 家属端点餐页面开发注意事项

当前已经落地的页面：

- `src/views/user-order/index.vue`
- `src/views/order-history/index.vue`

这两页目前运行在现有后台主布局里，不是独立 C 端项目。

后续完善时应注意：

- 优先在现有页面上增量迭代
- 不要直接改回 Vue 3 风格
- 购物车当前以页面本地状态和 `localStorage` 为主
- `Pinia` 草稿 store 目前不是正式依赖

## 8. 类型与代码质量

- 能定义接口时不要直接使用 `any`
- 新增 API 返回结构应补接口类型
- 保持现有 ESLint 规则通过
- 不要引入与当前 TS 3.9 不兼容的语法

## 9. 构建与验证

当前常用命令：

- 开发：`npm run serve`
- 构建：`npm run build`
- 单测：`npm run test:unit`
- E2E：`npm run test:e2e`

提交前至少保证：

- 前端能正常启动
- `npm run build` 通过
- 角色菜单与路由不冲突

## 10. 文档维护要求

如果以下内容发生变化，需要同步更新本规则文档：

- 技术栈
- 路由入口
- 登录态存储方式
- 请求代理规则
- 角色命名
- 点餐页接入方式

## 11. 2026-03 最新前端规则补充

以下内容覆盖文档中与当前实现不一致的旧描述。

### 11.1 FAMILY 点餐页

- `/family-order` 当前必须先确定服务老人
- 当前菜品列表按 `selectedElderly.diningPointId` 加载
- 不再默认展示所有助餐点菜品

### 11.2 FAMILY 购物车

- 购物车当前已经严格绑定老人
- `add/sub` 请求必须携带 `elderId`
- 同一 `FAMILY` 用户同一时刻只允许存在一个老人的购物车
- 不再保留“同助餐点老人可共用购物车”的前端逻辑

### 11.3 FAMILY 结算页

- 结算弹窗中的老人信息当前为只读展示
- 提交订单时固定使用当前页面选中的老人
- 当购物车非空时，老人恢复优先以后端购物车返回为准

### 11.4 `/order` 路由

- `/order` 当前由 `ADMIN` 和 `OPERATOR` 共用
- 页面内部按角色分视图
- `OPERATOR` 使用本站点订单执行中心，不再作为缩小版管理员页面
