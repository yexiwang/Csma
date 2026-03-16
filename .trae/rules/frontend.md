# 外卖系统前端开发规范文档

本文档基于现有外卖系统前端代码库 (https://github.com/yexiwang/project-sky-admin-vue-ts) 制定，旨在统一开发规范、提高代码质量、降低维护成本。新项目初始化及现有项目迭代请严格遵循本规范。

## 1. 技术栈与环境

- **框架**: Vue.js 2.6.x
- **语言**: TypeScript 3.x
- **UI 组件库**: Element UI 2.x
- **构建工具**: Vue CLI 3 / Webpack 4
- **状态管理**: Vuex 3.x + `vuex-module-decorators`
- **路由**: Vue Router 3.x
- **CSS 预处理**: SCSS (Sass)
- **HTTP 客户端**: Axios
- **代码风格**: ESLint + Prettier

### 1.1 开发环境要求
- Node.js >= 12.0.0
- npm >= 6.0.0 或 yarn >= 1.22.0
- 编辑器: VS Code (推荐安装 Vetur, ESLint, Prettier 插件)

---

## 2. 目录结构规范

```
src/
├── api/                # API 接口定义 (按业务模块拆分)
├── assets/             # 静态资源 (图片, 图标, 字体)
├── components/         # 公共组件 (非业务相关)
├── icons/              # SVG 图标
├── layout/             # 布局组件 (Sidebar, Navbar, AppMain)
├── router/             # 路由配置
├── store/              # Vuex 状态管理
│   ├── modules/        # 状态模块 (user, app 等)
│   └── index.ts        # Store 入口
├── styles/             # 全局样式
│   ├── _variables.scss # 变量定义
│   ├── _mixins.scss    # 混入定义
│   └── index.scss      # 全局样式入口
├── utils/              # 工具函数 (request, cookies, validate)
├── views/              # 页面组件 (按路由划分)
├── App.vue             # 根组件
├── main.ts             # 入口文件
├── permission.ts       # 权限控制 (路由守卫)
└── shims-vue.d.ts      # Vue 类型声明
```

---

## 3. 组件开发规范

### 3.1 组件定义
本项目强制使用 **Class-based Component** 风格，依赖 `vue-property-decorator` 和 `vue-class-component`。

**示例：**
```typescript
<template>
  <div class="example-component">
    <span>{{ title }}</span>
    <button @click="handleClick">Click Me</button>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator'

@Component({
  name: 'ExampleComponent',
  components: { /* 注册子组件 */ }
})
export default class ExampleComponent extends Vue {
  // Props 定义
  @Prop({ default: '' }) private title!: string
  @Prop({ required: true }) private list!: any[]

  // Data (直接定义类属性)
  private count: number = 0

  // Computed (使用 get 访问器)
  get doubleCount() {
    return this.count * 2
  }

  // Methods (直接定义类方法)
  private handleClick() {
    this.count++
    this.$emit('update', this.count)
  }
  
  // Lifecycle Hooks
  private created() {
    console.log('Component created')
  }
}
</script>

<style lang="scss" scoped>
.example-component {
  // 样式定义
}
</style>
```

### 3.2 命名约定
- **文件名**: 使用 `PascalCase` (如 `HeadLable/index.vue` 或 `MyComponent.vue`)。
- **组件名**: 在 `@Component` 装饰器中显式声明 `name`，使用 `PascalCase`。
- **Props**: 使用 `camelCase`。
- **Events**: 使用 `kebab-case` (如 `@click-item`)。

### 3.3 常用业务组件接口
- **HeadLable**: 顶部标题栏
  - Props: `title` (String), `goback` (Boolean), `butList` (Boolean)
  - Usage: `<HeadLable title="菜品管理" :goback="true" />`
- **ImageUpload**: 图片上传组件
  - Props: `propImageUrl` (String)
  - Events: `imageChange` (url: string)

---

## 4. 样式规范 (SCSS)

### 4.1 预处理器与变量
- 使用 SCSS 作为 CSS 预处理器。
- 全局变量文件: `src/styles/_variables.scss`。
- 全局 Mixins: `src/styles/_mixins.scss`。
- **自动注入**: 项目已配置 `style-resources-loader`，无需在每个组件中 `@import` 变量文件。

### 4.2 常用变量表
| 变量名 | 颜色值/用途 | 说明 |
| :--- | :--- | :--- |
| `$mine` | `#FFC200` | 主色调 (黄色) |
| `$blue` | `#2892E5` | 蓝色 (链接/信息) |
| `$green` | `#1DC779` | 绿色 (成功) |
| `$pink` | `#F56C6C` | 红色 (危险/错误) |
| `$gray-1` | `#20232A` | 深灰 (主要文本) |
| `$gray-5` | `#F3F4F7` | 背景灰 |
| `$sideBarWidth` | `190px` | 侧边栏宽度 |

### 4.3 移动端适配与布局
- **布局策略**: 响应式布局 + 弹性盒 (Flexbox)。
- **断点**: 利用 `DeviceType` (Mobile/Desktop) 在 `layout/index.vue` 中切换 class (`mobile`, `hideSidebar`)。
- **推荐**: 对于移动端专有页面，建议引入 `postcss-pxtorem` 将 px 转换为 rem，基准值设置为 37.5 或 75。

---

## 5. API 请求与错误处理

### 5.1 请求封装 (`src/utils/request.ts`)
- 基于 Axios 实例。
- **Base URL**: `process.env.VUE_APP_BASE_API` (通过 `.env` 文件配置)。
- **Timeout**: 600000ms。

### 5.2 拦截器逻辑
1.  **Request 拦截器**:
    - 自动注入 Token: `config.headers['token'] = UserModule.token`。
    - GET 请求参数序列化: 支持对象嵌套参数的扁平化处理。
    - **防重复提交**: 计算请求 Key，如果请求处于 Pending 状态则自动 Cancel。
2.  **Response 拦截器**:
    - **401 未授权**: 跳转至 `/login`。
    - **URL 清洗**: 移除 `/api` 前缀。
    - **业务错误**: 根据 `response.data.code` 判断 (1 为成功)。

### 5.3 接口定义规范
在 `src/api` 目录下按模块创建文件 (如 `employee.ts`, `dish.ts`)。

```typescript
import request from '@/utils/request'

// 建议定义 Interface 而不是使用 any
export interface ILoginData {
  username: string;
  password: string;
}

export const login = (data: ILoginData) =>
  request({
    url: '/employee/login',
    method: 'post',
    data
  })
```

---

## 6. 状态管理 (Vuex)

### 6.1 模块化
使用 `vuex-module-decorators` 实现强类型的 Vuex 模块。
所有模块置于 `src/store/modules/` 下。

### 6.2 User 模块示例 (`src/store/modules/user.ts`)
```typescript
@Module({ dynamic: true, store, name: 'user' })
class User extends VuexModule implements IUserState {
  public token = getToken() || ''
  
  @Mutation
  private SET_TOKEN(token: string) {
    this.token = token
  }
  
  @Action
  public async Login(userInfo: { username: string, password: string }) {
    // 调用 API 并 commit mutation
  }
}
```

---

## 7. 路由与权限

### 7.1 路由配置 (`src/router.ts`)
- 使用路由懒加载: `component: () => import('@/views/dashboard/index.vue')`。
- **Meta 字段**:
  - `title`: 页面标题
  - `icon`: 菜单图标 (对应 `src/icons/svg`)
  - `hidden`: 是否在侧边栏隐藏
  - `notNeedAuth`: 是否不需要登录即可访问

### 7.2 权限控制 (`src/permission.ts`)
- 全局 `beforeEach` 守卫。
- 检查 `Cookies.get('token')`。
- 有 Token -> 放行 (或跳转 Dashboard)。
- 无 Token -> 检查 `to.meta.notNeedAuth`，否则跳转 `/login`。

---

## 8. TypeScript 类型规范

- **Strict Mode**: 建议在 `tsconfig.json` 中逐步开启 `strict: true`，目前项目为 `strict: false`。
- **Any 使用**: 严禁滥用 `any`，应为 API 响应、Props、组件 Data 定义明确的 Interface。
- **类型定义文件**: 全局类型定义在 `src/types` 或根目录 `*.d.ts` 中。

---

## 9. 测试与构建

### 9.1 测试
- **Unit Test**: Jest (`npm run test:unit`)。目录: `tests/unit`。
- **E2E Test**: Cypress (`npm run test:e2e`)。
- **覆盖率要求**: 核心业务组件测试覆盖率应 >= 80%。

### 9.2 构建与 CI/CD
- **构建命令**:
  - 开发: `npm run serve`
  - 生产: `npm run build`
  - UAT: `npm run build:uat`
- **CI 流程** (`.gitlab-ci.yml`):
  - 触发: `master` 分支合并。
  - Stage 1: `install` (缓存 `node_modules`)。
  - Stage 2: `deploy` (构建并拷贝至 Nginx 目录)。

---
