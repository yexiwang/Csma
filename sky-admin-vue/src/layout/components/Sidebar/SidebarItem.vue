<template>
  <div>
    <!-- <div
      v-if=" !item.meta || !item.meta.hidden "
      :class="['menu-wrapper', isCollapse ? 'simple-mode' : 'full-mode', {'first-level': isFirstLevel}]"
    > -->
    <div
      v-if="!item.meta || !item.meta.hidden"
      :class="['menu-wrapper', 'full-mode', { 'first-level': isFirstLevel }]"
    >
      <template v-if="theOnlyOneChild && !theOnlyOneChild.children">
        <sidebar-item-link
          v-if="theOnlyOneChild.meta"
          :to="resolvePath(theOnlyOneChild.path)"
        >
          <el-menu-item
            :index="resolvePath(theOnlyOneChild.path)"
            :class="{ 'submenu-title-noDropdown': isFirstLevel }"
          >
            <!-- <i v-if="theOnlyOneChild.meta.title==='工作台'" class="iconfont icon img-icon-sel" /> -->
            <!-- <svg-icon v-if="theOnlyOneChild.meta.title==='工作台'" name="dashboard" width="20" height="20"></svg-icon> -->
            <svg-icon
              v-if="theOnlyOneChild.meta.svgIcon"
              :name="theOnlyOneChild.meta.svgIcon"
              width="28"
              height="28"
              class="menu-svg-icon"
            />
            <i
              v-else-if="theOnlyOneChild.meta.icon"
              class="iconfont"
              :class="theOnlyOneChild.meta.icon"
            />
            <span v-if="theOnlyOneChild.meta.title" slot="title">{{
              theOnlyOneChild.meta.title
            }}</span>
          </el-menu-item>
        </sidebar-item-link>
      </template>
      <el-submenu v-else :index="resolvePath(item.path)" popper-append-to-body>
        <template slot="title">
          <svg-icon
            v-if="item.meta && item.meta.svgIcon"
            :name="item.meta.svgIcon"
            width="28"
            height="28"
            class="menu-svg-icon"
          />
          <i
            v-else-if="item.meta && item.meta.icon"
            class="iconfont"
            :class="item.meta.icon"
          />
          <span v-if="item.meta && item.meta.title" slot="title">{{
            item.meta.title
          }}</span>
        </template>
        <template v-if="item.children">
          <sidebar-item
            v-for="child in item.children"
            :key="child.path"
            :item="child"
            :is-collapse="isCollapse"
            :is-first-level="false"
            :base-path="resolvePath(child.path)"
            class="nest-menu"
          />
        </template>
      </el-submenu>
    </div>
  </div>
</template>

<script lang="ts">
import path from 'path'
import { Component, Prop, Vue } from 'vue-property-decorator'
import { UserModule } from '@/store/modules/user'
import { RouteConfig } from 'vue-router'
import { isExternal } from '@/utils/validate'
import SidebarItemLink from './SidebarItemLink.vue'
import Cookies from 'js-cookie'

@Component({
  name: 'SidebarItem',
  components: {
    SidebarItemLink,
  },
})
export default class extends Vue {
  @Prop({ required: true }) private item!: RouteConfig
  @Prop({ default: false }) private isCollapse!: boolean
  @Prop({ default: true }) private isFirstLevel!: boolean
  @Prop({ default: '' }) private basePath!: string

  get showingChildNumber() {
    return this.visibleChildren.length
  }

  get roles() {
    return UserModule.roles
  }

  get currentRole() {
    return this.roles[0] || Cookies.get('role') || ''
  }

  get visibleChildren() {
    if (!this.item.children) {
      return []
    }

    return this.item.children.filter((item) => this.isRouteVisible(item))
  }

  get theOnlyOneChild() {
    if (this.showingChildNumber > 0) {
      return null
    }
    if (this.visibleChildren.length > 0) {
      for (const child of this.visibleChildren) {
        return child
      }
    }
    // If there is no children, return itself with path removed,
    // because this.basePath already conatins item's path information
    return { ...this.item, path: '' }
  }

  private resolvePath(routePath: string) {
    if (isExternal(routePath)) {
      return routePath
    }
    if (isExternal(this.basePath)) {
      return this.basePath
    }
    return path.resolve(this.basePath, routePath)
  }

  private isRouteVisible(route: RouteConfig) {
    if (!route) {
      return false
    }

    const meta: any = route.meta || {}
    if (meta.hidden || meta.notNeedAuth) {
      return false
    }

    if (!meta.roles || meta.roles.length === 0) {
      return true
    }

    return meta.roles.includes(this.currentRole)
  }
}
</script>

<style lang="scss" scoped>
.menu-svg-icon {
  width: 28px !important;
  height: 28px !important;
  margin-left: -4px;
  margin-right: 4px !important;
  vertical-align: -0.85em;
}
</style>
