import Vue from 'vue'
import Router from 'vue-router'
import Layout from '@/layout/index.vue'

Vue.use(Router)

const router = new Router({
  scrollBehavior: (_to, _from, savedPosition) => {
    if (savedPosition) {
      return savedPosition
    }
    return { x: 0, y: 0 }
  },
  base: process.env.BASE_URL,
  routes: [
    {
      path: '/login',
      component: () => import(/* webpackChunkName: "login" */ '@/views/login/index.vue'),
      meta: { title: '社区老年助餐服务系统', hidden: true, notNeedAuth: true }
    },
    {
      path: '/404',
      component: () => import(/* webpackChunkName: "404" */ '@/views/404.vue'),
      meta: { title: '社区老年助餐服务系统', hidden: true, notNeedAuth: true }
    },
    {
      path: '/',
      component: Layout,
      redirect: '/dashboard',
      children: [
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import(/* webpackChunkName: "dashboard" */ '@/views/dashboard/index.vue'),
          meta: {
            title: '工作台',
            icon: 'dashboard',
            affix: true,
            roles: ['ADMIN']
          }
        },
        {
          path: '/statistics',
          component: () => import(/* webpackChunkName: "shopTable" */ '@/views/statistics/index.vue'),
          meta: {
            title: '数据统计',
            icon: 'icon-statistics',
            roles: ['ADMIN']
          }
        },
        {
          path: 'order',
          component: () => import(/* webpackChunkName: "shopTable" */ '@/views/orderDetails/index.vue'),
          meta: {
            title: '订单执行',
            icon: 'icon-order',
            roles: ['ADMIN', 'OPERATOR']
          }
        },
        {
          path: 'diningPoint',
          component: () => import(/* webpackChunkName: "shopTable" */ '@/views/diningPoint/index.vue'),
          meta: {
            title: '助餐点管理',
            icon: 'icon-shop',
            roles: ['ADMIN']
          }
        },
        {
          path: 'elderly',
          component: () => import(/* webpackChunkName: "shopTable" */ '@/views/elderly/index.vue'),
          meta: {
            title: '老人档案',
            icon: 'icon-user',
            roles: ['ADMIN']
          }
        },
        {
          path: 'setmeal',
          component: () => import(/* webpackChunkName: "shopTable" */ '@/views/setmeal/index.vue'),
          meta: {
            title: '套餐管理',
            icon: 'icon-combo',
            roles: ['ADMIN']
          }
        },
        {
          path: 'dish',
          component: () => import(/* webpackChunkName: "shopTable" */ '@/views/dish/index.vue'),
          meta: {
            title: '菜品管理',
            icon: 'icon-dish',
            roles: ['ADMIN']
          }
        },
        {
          path: '/dish/add',
          component: () => import(/* webpackChunkName: "shopTable" */ '@/views/dish/addDishtype.vue'),
          meta: {
            title: '添加菜品',
            hidden: true,
            roles: ['ADMIN']
          }
        },
        {
          path: 'category',
          component: () => import(/* webpackChunkName: "shopTable" */ '@/views/category/index.vue'),
          meta: {
            title: '分类管理',
            icon: 'icon-category',
            roles: ['ADMIN']
          }
        },
        {
          path: 'employee',
          component: () => import(/* webpackChunkName: "shopTable" */ '@/views/employee/index.vue'),
          meta: {
            title: '员工管理',
            icon: 'icon-employee',
            roles: ['ADMIN']
          }
        },
        {
          path: 'volunteerManage',
          component: () => import(/* webpackChunkName: "volunteer-manage" */ '@/views/volunteerManage/index.vue'),
          meta: {
            title: '志愿者管理',
            icon: 'icon-user',
            roles: ['ADMIN']
          }
        },
        {
          path: '/employee/add',
          component: () => import(/* webpackChunkName: "dashboard" */ '@/views/employee/addEmployee.vue'),
          meta: {
            title: '添加员工',
            hidden: true,
            roles: ['ADMIN']
          }
        },
        {
          path: '/setmeal/add',
          component: () => import(/* webpackChunkName: "shopTable" */ '@/views/setmeal/addSetmeal.vue'),
          meta: {
            title: '添加套餐',
            hidden: true,
            roles: ['ADMIN']
          }
        },
        {
          path: 'volunteer-tasks',
          component: () => import(/* webpackChunkName: "volunteer" */ '@/views/volunteer/index.vue'),
          meta: {
            title: '我的任务',
            icon: 'icon-order',
            roles: ['VOLUNTEER']
          }
        },
        {
          path: 'family-order',
          component: () => import(/* webpackChunkName: "user-order" */ '@/views/user-order/index.vue'),
          meta: {
            title: '点餐',
            icon: 'icon-dish',
            roles: ['FAMILY']
          }
        },
        {
          path: 'family-addresses',
          component: () => import(/* webpackChunkName: "family-address" */ '@/views/family-address/index.vue'),
          meta: {
            title: '鍦板潃绠＄悊',
            hidden: true,
            roles: ['FAMILY']
          }
        },
        {
          path: 'family-addresses/new',
          component: () => import(/* webpackChunkName: "family-address" */ '@/views/family-address/form.vue'),
          meta: {
            title: '鏂板鍦板潃',
            hidden: true,
            roles: ['FAMILY']
          }
        },
        {
          path: 'family-addresses/:id/edit',
          component: () => import(/* webpackChunkName: "family-address" */ '@/views/family-address/form.vue'),
          meta: {
            title: '淇敼鍦板潃',
            hidden: true,
            roles: ['FAMILY']
          }
        },
        {
          path: 'family-history',
          component: () => import(/* webpackChunkName: "order-history" */ '@/views/order-history/index.vue'),
          meta: {
            title: '历史订单',
            icon: 'icon-order',
            roles: ['FAMILY']
          }
        }
      ]
    },
    {
      path: '*',
      redirect: '/404',
      meta: { hidden: true }
    }
  ]
})

export default router
