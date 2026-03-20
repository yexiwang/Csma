<template>
  <div class="family-redirect">
    <el-empty description="家属下单页已迁移到新的 family-order 路由">
      <el-button type="primary" @click="$router.push('/family-order')">
        前往家属下单
      </el-button>
    </el-empty>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'

@Component({
  name: 'FamilyLegacyEntry'
})
export default class FamilyLegacyEntry extends Vue {}
</script>

<style lang="scss" scoped>
.family-redirect {
  padding: 32px;
  background: #fff;
  border-radius: 12px;
}
</style>
