import Vue from 'vue'
import Router from 'vue-router'
import 'normalize.css'
import ElementUI from 'element-ui'
import SvgIcon from 'vue-svgicon'
import VueAreaLinkage from 'vue-area-linkage'
import moment from 'moment'
import '@/styles/element-variables.scss'
import '@/styles/index.scss'
import '@/styles/home.scss'
import 'vue-area-linkage/dist/index.css'

import * as echarts from 'echarts'
// 瑞吉外卖样式表
import '@/styles/newRJWMsystem.scss'
import '@/styles/icon/iconfont.css'
import App from '@/App.vue'
import store from '@/store'
import router from '@/router'
import '@/icons/components'
import '@/permission'
import { checkProcessEnv } from '@/utils/common'

Vue.use(ElementUI)
Vue.use(VueAreaLinkage)
Vue.use(SvgIcon, {
  'tagName': 'svg-icon',
  'defaultWidth': '1em',
  'defaultHeight': '1em'
})

Vue.config.productionTip = false
Vue.prototype.moment = moment
Vue.prototype.$checkProcessEnv = checkProcessEnv
const routerPush = Router.prototype.push
Router.prototype.push = function push(location) {
 return routerPush.call(this, location).catch(error => error)
}
Vue.prototype.$echarts = echarts

// The project uses hash routing. If the browser opens a plain path such as
// /login directly, normalize it to /#/login before Vue Router starts.
const { hash, origin, pathname, search } = window.location
const basePath = (process.env.BASE_URL || '/').replace(/\/$/, '')
const normalizedBasePath = basePath === '' ? '/' : basePath
const appRoot = normalizedBasePath.endsWith('/') ? normalizedBasePath : `${normalizedBasePath}/`
if (!hash && pathname !== normalizedBasePath) {
  const routePath = basePath && pathname.startsWith(basePath)
    ? pathname.slice(basePath.length) || '/'
    : pathname
  window.location.replace(`${origin}${appRoot}#${routePath}${search}`)
}

new Vue({
  router,
  store,
  'render': (h) => h(App)
}).$mount('#app')
