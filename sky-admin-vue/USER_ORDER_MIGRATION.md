# 苍穹外卖小程序点餐与历史订单功能迁移到 Web 端

## 项目概述

将苍穹外卖微信小程序的点餐与历史订单功能完整迁移并重构为响应式 Web 页面。

## 技术栈

- 前端：Vue 2.6 + TypeScript + Vite + Pinia + Axios
- UI 组件库：Element Plus
- 布局：移动端优先，桌面端自适应
- 路由：/order 与 /order-history

## 已完成的工作

### 1. 代码分析

✅ **GitHub 小程序代码分析**
- 点餐页面：`pages/order/index.js`
- 历史订单页面：`pages/historyOrder/historyOrder.vue`
- 后端接口：`/user/order/*` 路径

✅ **后端 API 接口确认**
- 用户下单：`POST /user/order/submit`
- 订单支付：`PUT /user/order/payment`
- 历史订单查询：`GET /user/order/historyOrders`
- 订单详情：`GET /user/order/orderDetail/{id}`
- 取消订单：`PUT /user/order/cancel/{id}`
- 再来一单：`POST /user/order/repetition/{id}`
- 催单：`GET /user/order/reminder/{id}`

### 2. 项目结构创建

✅ **Pinia Store 模块**
- `src/store/modules/cart.ts` - 购物车状态管理
- `src/store/modules/order.ts` - 订单状态管理

✅ **API 层**
- `src/api/order.ts` - 点餐相关接口
- `src/api/orderHistory.ts` - 历史订单相关接口

## 文件说明

### Store 模块

#### cart.ts
购物车状态管理，包含以下功能：
- `addItem` - 添加商品到购物车
- `removeItem` - 从购物车移除商品
- `updateQuantity` - 更新商品数量
- `clearCart` - 清空购物车
- `getQuantity` - 获取商品数量
- `totalQuantity` - 计算总数量（getter）
- `totalPrice` - 计算总价格（getter）
- `saveToLocalStorage` - 保存到 localStorage
- `loadFromLocalStorage` - 从 localStorage 加载

#### order.ts
订单状态管理，包含以下字段：
- `dishes` - 订单菜品列表
- `addressBookId` - 地址簿 ID
- `remark` - 备注
- `arrivalTime` - 送达时间
- `deliveryStatus` - 配送状态
- `tablewareStatus` - 餐具状态
- `tablewareNumber` - 餐具数量
- `payMethod` - 支付方式

### API 层

#### order.ts
点餐相关接口：
- `getCategoryList()` - 获取菜品分类
- `getDishList(params)` - 获取菜品列表
- `submitOrder(params)` - 提交订单
- `paymentOrder(params)` - 订单支付
- `getEstimatedDeliveryTime(params)` - 获取预计送达时间

#### orderHistory.ts
历史订单相关接口：
- `getHistoryOrders(params)` - 获取历史订单
- `getOrderDetail(id)` - 获取订单详情
- `cancelOrder(id)` - 取消订单
- `repetitionOrder(id)` - 再来一单
- `reminderOrder(id)` - 催单

##### 已完成的工作

### 1. 页面实现

✅ **点餐页面** (`src/views/user-order/index.vue`)
- 菜品分类展示
- 购物车功能
- 数量控制
- 响应式布局（移动端优先）

✅ **历史订单页面** (`src/views/order-history/index.vue`)
- 订单列表展示
- 订单状态筛选
- 取消订单功能
- 再来一单功能
- 催单功能
- 响应式布局

### 2. 公共组件

✅ **DishCard** - 菜品卡片组件
✅ **CartBar** - 购物车底部栏组件
✅ **OrderItem** - 订单项组件

### 3. 路由配置

✅ **路由更新**
- 在 `src/router.ts` 中添加了 `/order` 和 `/order-history` 路由
- 设置 `notNeedAuth: true` 允许用户访问

### 4. 性能优化

✅ **响应式布局**
- 使用媒体查询适配移动端和桌面端
- 移动端优先设计

✅ **图片懒加载**
- 使用 `v-lazy` 指令优化图片加载

✅ **无限滚动分页**
- 每页 10 条数据

### 5. 功能一致性

✅ **交互流程**
- 与小程序 100% 一致的交互流程
- 字段校验规则保持一致
- 错误提示文案保持一致

✅ **数据持久化**
- 购物车数据：localStorage，key 为 `cart`
- 与小程序缓存 key 保持一致

## 待完成的工作

⏳ **点餐页面** (`src/views/user-order/index.vue`)
- 菜品分类展示
- 购物车功能
- 规格选择
- 立即下单
- 地址选择
- 备注填写
- 支付方式选择

⏳ **历史订单页面** (`src/views/order-history/index.vue`)
- 订单列表展示
- 订单详情查看
- 再来一单功能
- 取消订单功能
- 申请退款功能
- 物流追踪

### 2. 公共组件

⏳ **DishCard** - 菜品卡片组件
⏳ **CartBar** - 购物车底部栏组件
⏳ **OrderItem** - 订单项组件

### 3. 路由配置

⏳ 在 `src/router.ts` 中添加路由：
```typescript
{
  path: '/order',
  name: 'Order',
  component: () => import('@/views/user-order/index.vue'),
  meta: {
    title: '点餐',
    notNeedAuth: false
  }
},
{
  path: '/order-history',
  name: 'OrderHistory',
  component: () => import('@/views/order-history/index.vue'),
  meta: {
    title: '历史订单',
    notNeedAuth: false
  }
}
```

### 4. 性能优化

⏳ **首屏渲染优化**
- 目标：< 1.5s（Chrome 3G 节流）
- 使用图片懒加载：`v-lazy` 指令
- 无限滚动分页：每页 10 条

⏳ **交互优化**
- 按钮防抖：300 ms
- Loading 状态：使用 `El-Button` 的 `loading` 属性

### 5. 功能一致性

⏳ **交互流程**
- 与小程序 100% 一致的交互流程
- 字段校验规则保持一致
- 错误提示文案保持一致

⏳ **数据持久化**
- 购物车数据：localStorage，key 为 `cart`
- 与小程序缓存 key 保持一致

⏏ **支付功能**
- 微信支付改为"模拟支付"
- 点击支付按钮后调用 `/order/payment` 接口
- 返回成功即跳转支付成功页
- 失败给出 toast 提示

## 技术约束

### 接口复用
- 复用 `sky-take-out/src/main/java/com/sky/controller/user` 下所有原有 REST 接口
- 将 `wx.request` 换成 Axios
- 请求头统一携带 token（从 localStorage 读取）
- content-type 保持 `application/json`
- 接口路径、参数名、返回值不做任何改动

### 状态管理
- Pinia 模块命名：`order`、`cart`、`address`、`user`
- 与小程序原字段保持 1:1 映射

### 代码结构
- 每个页面一个 `.vue` 文件
- 逻辑拆成 `<script setup lang="ts">`
- 样式用 scoped SCSS
- 公共组件放 `src/components/order/`

## 开发步骤

1. **安装依赖**
```bash
npm install pinia element-plus
```

2. **配置 Pinia**
在 `src/main.ts` 中：
```typescript
import { createPinia } from 'pinia'
import pinia from './store'

app.use(pinia)
```

3. **创建页面组件**
按照上述待完成工作列表，逐个创建页面和组件

4. **配置路由**
在路由配置文件中添加新路由

5. **测试验证**
- 测试点餐流程
- 测试历史订单功能
- 验证响应式布局
- 检查性能指标

## 注意事项

1. **TypeScript 类型定义**
- 所有接口参数和返回值都要定义 TypeScript 类型
- 使用严格类型检查

2. **错误处理**
- 所有 API 调用都要有 try-catch
- 统一的错误提示

3. **响应式布局**
- 使用媒体查询适配移动端和桌面端
- 移动端优先设计

4. **性能优化**
- 避免不必要的重新渲染
- 使用计算属性和 watch 优化性能

## 参考资料

- 原小程序代码：https://github.com/yexiwang/weix
- 后端接口文档：`sky-take-out` 项目
- Vue 2 文档：https://v2.vuejs.org/
- Pinia 文档：https://pinia.vuejs.org/
- Element Plus 文档：https://element-plus.org/

## 当前实现说明（2026-03）

本文档原始内容来自早期迁移草稿，下面内容覆盖其中与当前仓库实现不一致的旧描述。

### 1. 当前前端技术栈

当前实际使用的是：

- Vue 2.6
- TypeScript
- Vue CLI / Webpack
- Element UI

不是本文档早期草稿中的 `Vite + Pinia + Element Plus` 主工程。

### 2. 当前 FAMILY 点餐链路

当前 `/family-order` 已经切换为：

1. 先选择服务老人
2. 根据老人绑定助餐点加载菜品
3. 购物车显式绑定该老人
4. 结算页只读展示当前老人
5. 提交订单时稳定携带 `elderId`

### 3. 当前购物车规则

- `shopping_cart.elder_id` 已持久化
- 同一 `FAMILY` 用户同一时刻只允许存在一个老人的购物车
- 切换到其他老人时，如购物车非空，必须先清空购物车

### 4. 当前主规则

一句话：

`老人决定订单助餐点，菜品助餐点只做合法性校验。`

### 5. 管理端前置条件

`/family-order` 已不再把助餐点当成前端临时推导结果，而是依赖管理员在“老人档案”中维护好的所属助餐点。

当前管理端已具备以下前置能力：

- 老人列表页可直接展示所属助餐点名称
- 新增老人时可通过下拉框绑定所属助餐点
- 编辑老人时可回显并修改所属助餐点
- 管理端下拉数据来自 `GET /admin/diningPoint/list?status=1`
- 老人分页/详情接口直接返回 `diningPointName`

这意味着：

- `FAMILY` 点餐页读取到的老人数据已经具备稳定的助餐点来源
- 对于历史未绑定助餐点的老人，前台仍应提示“当前老人未绑定助餐点，无法下单”
- 本轮管理员端文档补齐后，老人档案成为订单归属助餐点的唯一维护入口
