<template>
  <div>
    <el-scrollbar wrap-class="scrollbar-wrapper">
      <el-menu :default-openeds="defOpen"
               :default-active="defAct"
               :collapse="isCollapse"
               :background-color="variables.menuBg"
               :text-color="variables.menuText"
               :active-text-color="variables.menuActiveText"
               :unique-opened="false"
               :collapse-transition="false"
               mode="vertical"
      >
        <sidebar-item v-for="route in routes"
                      :key="route.path"
                      :item="route"
                      :base-path="route.path"
                      :is-collapse="isCollapse"
        />
        <!-- <div class="sub-menu">
          <div class="avatarName">
            {{ name }}
          </div>
          <div class="img">
            <img
              src="./../../../assets/icons/btn_close@2x.png"
              class="outLogin"
              alt="退出"
              @click="logout"
            />
          </div>
        </div> -->
      </el-menu>
    </el-scrollbar>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator'
import { AppModule } from '@/store/modules/app'
import { UserModule } from '@/store/modules/user'
import SidebarItem from './SidebarItem.vue'
import variables from '@/styles/_variables.scss'
import { getSidebarStatus, setSidebarStatus } from '@/utils/cookies'
import Cookies from 'js-cookie'
@Component({
  name: 'SideBar',
  components: {
    SidebarItem
  }
})
export default class extends Vue {
  private restKey: number = 0
  get name() {
    return (UserModule.userInfo as any).name
      ? (UserModule.userInfo as any).name
      : JSON.parse(Cookies.get('user_info') as any).name
  }
  get defOpen() {
    const currentPath = this.$route.path
    const matchedRoute = this.routes.find((route: any) => {
      const routePath = route.path.startsWith('/') ? route.path : `/${route.path}`
      return currentPath === routePath
    })
    return [matchedRoute ? matchedRoute.path : '/']
  }

  get defAct() {
    let path = this.$route.path
    return path
  }

  get sidebar() {
    return AppModule.sidebar
  }

  get roles() {
    return UserModule.roles
  }

  get currentRole() {
    return this.roles[0] || Cookies.get('role') || ''
  }

  get routes() {
    const routes = JSON.parse(
      JSON.stringify([...(this.$router as any).options.routes])
    )
    const menu = routes.find((item: any) => item.path === '/')
    if (!menu || !menu.children) {
      return []
    }

    const visibleRoutes = menu.children
      .filter((route: any) => this.isVisibleMenuRoute(route))
      .map((route: any) => this.cloneVisibleRoute(route))

    const seen = new Set()
    return visibleRoutes.filter((route: any) => {
      const routePath = route.path.startsWith('/') ? route.path : `/${route.path}`
      if (seen.has(routePath)) {
        return false
      }
      seen.add(routePath)
      return true
    })
  }

  get variables() {
    return variables
  }

  get isCollapse() {
    return !this.sidebar.opened
  }
  private async logout() {
    this.$store.dispatch('LogOut').then(() => {
      // location.href = '/'
      this.$router.replace({ path: '/login' })
    })
    // this.$router.push(`/login?redirect=${this.$route.fullPath}`)
  }

  private isVisibleMenuRoute(route: any) {
    if (!route) {
      return false
    }

    const meta = route.meta || {}
    if (meta.hidden || meta.notNeedAuth) {
      return false
    }

    if (!meta.roles || meta.roles.length === 0) {
      return true
    }

    return meta.roles.includes(this.currentRole)
  }

  private cloneVisibleRoute(route: any) {
    const nextRoute = { ...route }
    if (Array.isArray(route.children)) {
      nextRoute.children = route.children
        .filter((child: any) => this.isVisibleMenuRoute(child))
        .map((child: any) => this.cloneVisibleRoute(child))
    }
    return nextRoute
  }
}
</script>

<style lang="scss" scoped>
.el-scrollbar {
  height: 100%;
  background-color: $menuBg;
}

.el-menu {
  border: none;
  min-height: calc(100vh - 32px);
  width: 100% !important;
  padding: 24px 15px 0;
}
</style>
