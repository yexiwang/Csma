<template>
  <div class="dashboard-container">
    <div class="container">
      <el-timeline>
        <el-timeline-item timestamp="2023-10-01" placement="top">
          <el-card>
            <h4>宫保鸡丁套餐</h4>
            <p>李大爷 已收货 2023-10-01 12:30</p>
          </el-card>
        </el-timeline-item>
        <el-timeline-item timestamp="2023-09-28" placement="top">
          <el-card>
            <h4>清蒸鲈鱼</h4>
            <p>李大爷 已收货 2023-09-28 12:15</p>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'

@Component({
  name: 'FamilyHistory'
})
export default class extends Vue {}
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 30px;
}
</style>
