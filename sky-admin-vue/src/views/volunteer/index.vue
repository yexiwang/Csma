<template>
  <div class="volunteer-page">
    <div class="toolbar">
      <span class="label">任务状态</span>
      <el-select v-model="status" clearable placeholder="全部" @change="handleQuery">
        <el-option label="待取餐" :value="ORDER_STATUS.MEAL_READY" />
        <el-option label="配送中" :value="ORDER_STATUS.DELIVERY_IN_PROGRESS" />
        <el-option label="已完成" :value="ORDER_STATUS.COMPLETED" />
      </el-select>
    </div>

    <el-row v-loading="loading" :gutter="20">
      <el-col
        v-for="item in tableData"
        :key="item.id"
        :xs="24"
        :sm="12"
        :lg="8"
        class="task-col"
      >
        <el-card shadow="hover" class="task-card">
          <div slot="header" class="task-header">
            <span>订单号 {{ item.number }}</span>
            <el-tag :type="getOrderStatusTag(item.status)">
              {{ getOrderStatusText(item.status) }}
            </el-tag>
          </div>

          <div class="task-content">
            <p><strong>助餐点：</strong>{{ item.diningPointName || '--' }}</p>
            <p><strong>联系人：</strong>{{ item.consignee || '--' }} {{ item.phone || '' }}</p>
            <p><strong>地址：</strong>{{ item.address || '--' }}</p>
            <p><strong>下单时间：</strong>{{ item.orderTime || '--' }}</p>
            <p v-if="item.remark"><strong>备注：</strong>{{ item.remark }}</p>
          </div>

          <div class="task-actions">
            <el-button
              v-if="item.status === ORDER_STATUS.MEAL_READY"
              type="primary"
              @click="handlePickup(item.id)"
            >
              确认取餐
            </el-button>
            <el-button
              v-if="item.status === ORDER_STATUS.DELIVERY_IN_PROGRESS"
              type="success"
              @click="handleComplete(item.id)"
            >
              确认送达
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-empty
      v-if="!loading && tableData.length === 0"
      description="当前没有任务"
    />

    <el-pagination
      v-if="total > 0"
      class="pageList"
      :page-sizes="[6, 12, 18]"
      :page-size="pageSize"
      :current-page="page"
      layout="total, sizes, prev, pager, next, jumper"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import {
  getVolunteerOrderPage,
  VolunteerTaskItem,
  volunteerConfirmComplete,
  volunteerConfirmPickup
} from '@/api/volunteer'
import { ORDER_STATUS, getOrderStatusTag, getOrderStatusText } from '@/constants/order'

@Component({
  name: 'VolunteerTask'
})
export default class VolunteerTask extends Vue {
  private readonly ORDER_STATUS = ORDER_STATUS
  private tableData: VolunteerTaskItem[] = []
  private loading = false
  private total = 0
  private page = 1
  private pageSize = 6
  private status: number | undefined = undefined

  created() {
    this.getList()
  }

  private getOrderStatusText(status: number) {
    return getOrderStatusText(status)
  }

  private getOrderStatusTag(status: number) {
    return getOrderStatusTag(status)
  }

  private async getList() {
    this.loading = true
    try {
      const pageData = await getVolunteerOrderPage({
        page: this.page,
        pageSize: this.pageSize,
        status: this.status
      })
      this.tableData = pageData.records || []
      this.total = pageData.total || 0
    } finally {
      this.loading = false
    }
  }

  private handleQuery() {
    this.page = 1
    this.getList()
  }

  private async handlePickup(id: number) {
    try {
      await this.$confirm('确认已经在助餐点取到餐品？', '提示', { type: 'warning' })
      await volunteerConfirmPickup(id)
      this.$message.success('已切换为配送中')
      this.getList()
    } catch (error) {
      if (error !== 'cancel' && error !== 'close') {
        throw error
      }
    }
  }

  private async handleComplete(id: number) {
    try {
      await this.$confirm('确认餐品已经送达？', '提示', { type: 'success' })
      await volunteerConfirmComplete(id)
      this.$message.success('任务已完成')
      this.getList()
    } catch (error) {
      if (error !== 'cancel' && error !== 'close') {
        throw error
      }
    }
  }

  private handleSizeChange(pageSize: number) {
    this.pageSize = pageSize
    this.page = 1
    this.getList()
  }

  private handleCurrentChange(page: number) {
    this.page = page
    this.getList()
  }
}
</script>

<style lang="scss" scoped>
.volunteer-page {
  padding: 24px;

  .toolbar {
    display: flex;
    align-items: center;
    gap: 12px;
    margin-bottom: 20px;
    background: #fff;
    padding: 16px 20px;
    border-radius: 8px;
  }

  .label {
    font-size: 14px;
    color: #606266;
  }

  .task-col {
    margin-bottom: 20px;
  }

  .task-card {
    min-height: 240px;
  }

  .task-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .task-content p {
    margin: 10px 0;
    color: #606266;
    line-height: 1.5;
  }

  .task-actions {
    margin-top: 16px;
    text-align: right;
  }

  .pageList {
    margin-top: 8px;
    text-align: center;
  }
}
</style>
