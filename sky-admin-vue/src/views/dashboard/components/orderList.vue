<template>
  <div class="container homecon">
    <div class="header">
      <h2 class="homeTitle">
        订单看板
      </h2>
      <el-button type="primary" size="mini" @click="$router.push('/order')">
        前往订单调度
      </el-button>
    </div>

    <div class="tab-row">
      <div
        v-for="tab in tabList"
        :key="tab.value"
        :class="['tab-item', { active: currentStatus === tab.value }]"
        @click="changeStatus(tab.value)"
      >
        <el-badge :value="tab.count" :hidden="!tab.count">
          {{ tab.label }}
        </el-badge>
      </div>
    </div>

    <el-table
      v-loading="loading"
      :data="tableData"
      stripe
      border
      class="tableBox"
    >
      <el-table-column prop="number" label="订单号" min-width="170" />
      <el-table-column prop="orderDishes" label="订单菜品" min-width="220" show-overflow-tooltip />
      <el-table-column prop="address" label="配送地址" min-width="220" show-overflow-tooltip />
      <el-table-column prop="diningPointName" label="助餐点" min-width="120" />
      <el-table-column prop="volunteerName" label="志愿者" min-width="110">
        <template slot-scope="{ row }">
          <span>{{ row.volunteerName || '--' }}</span>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="110">
        <template slot-scope="{ row }">
          <el-tag :type="getOrderStatusTag(row.status)">
            {{ getOrderStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="orderTime" label="下单时间" min-width="170" />
      <el-table-column label="操作" width="150" fixed="right">
        <template slot-scope="{ row }">
          <el-button type="text" @click="goToOrder(row.status)">
            去处理
          </el-button>
          <el-button type="text" @click="showDetail(row.id)">
            查看
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-empty
      v-if="!loading && tableData.length === 0"
      description="当前状态没有订单"
    />

    <el-pagination
      v-if="total > 0"
      class="pageList"
      :page-sizes="[5, 10, 20]"
      :page-size="pageSize"
      :current-page="page"
      layout="total, sizes, prev, pager, next"
      :total="total"
      @size-change="handleSizeChange"
      @current-change="handleCurrentChange"
    />

    <el-dialog title="订单详情" :visible.sync="detailVisible" width="640px">
      <div v-if="detailData" class="detail-box">
        <p><strong>订单号：</strong>{{ detailData.number }}</p>
        <p><strong>状态：</strong>{{ getOrderStatusText(detailData.status) }}</p>
        <p><strong>收货人：</strong>{{ detailData.consignee || '--' }}</p>
        <p><strong>地址：</strong>{{ detailData.address || '--' }}</p>
        <el-table :data="detailData.orderDetailList || []" border size="mini">
          <el-table-column prop="name" label="菜品" min-width="180" />
          <el-table-column prop="number" label="数量" width="80" />
          <el-table-column label="金额" width="100">
            <template slot-scope="{ row }">
              ￥{{ Number(row.amount || 0).toFixed(2) }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator'
import { getAdminOrderDetail, getAdminOrderPage } from '@/api/adminOrder'
import { ORDER_STATUS, getOrderStatusTag, getOrderStatusText } from '@/constants/order'

@Component({
  name: 'DashboardOrderList'
})
export default class DashboardOrderList extends Vue {
  @Prop({ default: () => ({}) }) private readonly orderStatics!: any

  private currentStatus: number = ORDER_STATUS.TO_BE_SCHEDULED
  private loading = false
  private page = 1
  private pageSize = 5
  private total = 0
  private tableData: any[] = []
  private detailVisible = false
  private detailData: any = null

  get tabList() {
    return [
      {
        label: '待调度',
        value: ORDER_STATUS.TO_BE_SCHEDULED,
        count: this.orderStatics.toBeConfirmed || 0
      },
      {
        label: '制作中',
        value: ORDER_STATUS.PREPARING,
        count: this.orderStatics.confirmed || 0
      },
      {
        label: '待取餐',
        value: ORDER_STATUS.MEAL_READY,
        count: this.orderStatics.deliveryInProgress || 0
      }
    ]
  }

  created() {
    this.loadOrders()
  }

  private getOrderStatusText(status: number) {
    return getOrderStatusText(status)
  }

  private getOrderStatusTag(status: number) {
    return getOrderStatusTag(status)
  }

  private async loadOrders() {
    this.loading = true
    try {
      const res = await getAdminOrderPage({
        page: this.page,
        pageSize: this.pageSize,
        status: this.currentStatus
      })
      const pageData = res.data && res.data.data ? res.data.data : res.data
      this.tableData = pageData && pageData.records ? pageData.records : []
      this.total = pageData && pageData.total ? pageData.total : 0
      this.$emit('getOrderListBy3Status')
    } finally {
      this.loading = false
    }
  }

  private changeStatus(status: number) {
    this.currentStatus = status
    this.page = 1
    this.loadOrders()
  }

  private goToOrder(status: number) {
    this.$router.push({ path: '/order', query: { status: String(status) } })
  }

  private async showDetail(id: number) {
    const res = await getAdminOrderDetail(id)
    this.detailData = res.data && res.data.data ? res.data.data : res.data
    this.detailVisible = true
  }

  private handleSizeChange(pageSize: number) {
    this.pageSize = pageSize
    this.page = 1
    this.loadOrders()
  }

  private handleCurrentChange(page: number) {
    this.page = page
    this.loadOrders()
  }
}
</script>

<style lang="scss" scoped>
.header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}

.tab-row {
  display: flex;
  gap: 10px;
  margin-bottom: 16px;
}

.tab-item {
  padding: 8px 14px;
  border-radius: 999px;
  background: #f4f4f5;
  cursor: pointer;

  &.active {
    background: #2f8f83;
    color: #fff;
  }
}

.pageList {
  margin-top: 16px;
  text-align: center;
}

.detail-box p {
  margin: 0 0 12px;
}
</style>
