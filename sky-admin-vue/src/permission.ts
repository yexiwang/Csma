import router from './router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import Cookies from 'js-cookie'
import { removeRole, removeStoreId, removeToken, removeUserInfo } from '@/utils/cookies'

NProgress.configure({ showSpinner: false })

const VALID_ROLES = ['ADMIN', 'OPERATOR', 'VOLUNTEER', 'FAMILY']

function getDefaultRouteByRole(role: string) {
  if (role === 'ADMIN') return '/dashboard'
  if (role === 'OPERATOR') return '/order'
  if (role === 'VOLUNTEER') return '/volunteer-overview'
  if (role === 'FAMILY') return '/family-order'
  return '/login'
}

function clearAuthCookies() {
  removeToken()
  removeRole()
  removeUserInfo()
  removeStoreId()
}

router.beforeEach(async (to: any, _from: any, next: any) => {
  NProgress.start()

  const token = Cookies.get('token')
  const role = Cookies.get('role') || ''
  const hasValidRole = VALID_ROLES.includes(role)

  if (token) {
    if (!hasValidRole) {
      clearAuthCookies()
      if (to.path === '/login') {
        next()
      } else {
        next('/login')
        NProgress.done()
      }
      return
    }

    if (to.path === '/login') {
      next(getDefaultRouteByRole(role))
      NProgress.done()
      return
    }

    if (to.meta.roles && to.meta.roles.length > 0) {
      if (role && to.meta.roles.includes(role)) {
        next()
      } else {
        next(getDefaultRouteByRole(role))
        NProgress.done()
      }
    } else {
      next()
    }
  } else if (!to.meta.notNeedAuth) {
    next('/login')
    NProgress.done()
  } else {
    next()
  }
})

router.afterEach((to: any) => {
  NProgress.done()
  document.title = to.meta.title
})
