<template>
  <operator-order-center v-if="isOperator" />
  <div v-else class="order-page">
    <div class="stats-grid">
      <el-card shadow="never" class="stat-card">
        <div class="stat-label">待制作</div>
        <div class="stat-value">{{ statistics.toBeConfirmed || 0 }}</div>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <div class="stat-label">制作中</div>
        <div class="stat-value">{{ statistics.confirmed || 0 }}</div>
      </el-card>
      <el-card shadow="never" class="stat-card">
        <div class="stat-label">待取餐</div>
        <div class="stat-value">{{ statistics.deliveryInProgress || 0 }}</div>
      </el-card>
    </div>

    <div class="panel">
      <div class="tabs">
        <div
          v-for="tab in statusTabs"
          :key="tab.value"
          :class="['tab-item', { active: currentStatus === tab.value }]"
          @click="changeStatus(tab.value)"
        >
          {{ tab.label }}
        </div>
      </div>

      <div class="toolbar">
        <el-input
          v-model="query.number"
          clearable
          placeholder="订单号"
          style="width: 180px"
          @clear="handleQuery"
          @keyup.enter.native="handleQuery"
        />
        <el-input
          v-model="query.phone"
          clearable
          placeholder="手机号"
          style="width: 160px"
          @clear="handleQuery"
          @keyup.enter.native="handleQuery"
        />
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          clearable
          value-format="yyyy-MM-dd HH:mm:ss"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          style="width: 320px"
          @change="handleQuery"
        />
        <el-button type="primary" @click="handleQuery">查询</el-button>
        <el-button @click="resetQuery">重置</el-button>
      </div>

      <el-table
        v-loading="loading"
        :data="tableData"
        stripe
        border
      >
        <el-table-column prop="number" label="订单号" min-width="170" />
        <el-table-column label="状态" width="110">
          <template slot-scope="{ row }">
            <el-tag :type="getOrderStatusTag(row.status)">
              {{ getOrderStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="consignee" label="收货人" min-width="100" />
        <el-table-column prop="phone" label="手机号" min-width="120" />
        <el-table-column prop="address" label="地址" min-width="220" show-overflow-tooltip />
        <el-table-column label="助餐点" min-width="120">
          <template slot-scope="{ row }">
            <span>{{ getDisplayDiningPointName(row) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="volunteerName" label="志愿者" min-width="110">
          <template slot-scope="{ row }">
            <span>{{ row.volunteerName || '--' }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="orderTime" label="下单时间" min-width="170" />
        <el-table-column prop="amount" label="金额" width="100">
          <template slot-scope="{ row }">
            ￥{{ Number(row.amount || 0).toFixed(2) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="260" fixed="right">
          <template slot-scope="{ row }">
            <el-button type="text" @click="openDetail(row.id)">查看</el-button>
            <el-button
              v-if="isAdmin && row.status === ORDER_STATUS.TO_BE_SCHEDULED"
              type="text"
              @click="openDispatch(row)"
            >
              分配志愿者
            </el-button>
            <el-button
              v-if="canMarkMealReady(row)"
              type="text"
              @click="handleMealReady(row)"
            >
              标记出餐完成
            </el-button>
            <el-button
              v-if="canReject(row)"
              type="text"
              class="danger-text"
              @click="handleReject(row)"
            >
              拒单
            </el-button>
            <el-button
              v-if="canCancel(row)"
              type="text"
              class="danger-text"
              @click="handleCancel(row)"
            >
              取消
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <el-pagination
        v-if="total > 0"
        class="pageList"
        :page-sizes="[10, 20, 30, 40]"
        :page-size="pageSize"
        :current-page="page"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>

    <el-dialog
      title="分配志愿者"
      :visible.sync="dispatchVisible"
      width="420px"
    >
      <el-form label-width="90px">
        <el-form-item label="订单号">
          <span>{{ dispatchRow.number || '--' }}</span>
        </el-form-item>
        <el-form-item label="志愿者">
          <el-select v-model="dispatchForm.volunteerId" placeholder="请选择志愿者" style="width: 100%">
            <el-option
              v-for="item in volunteers"
              :key="item.id"
              :label="formatVolunteerLabel(item)"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <div slot="footer">
        <el-button @click="dispatchVisible = false">取消</el-button>
        <el-button type="primary" :loading="dispatching" @click="submitDispatch">
          确认分配
        </el-button>
      </div>
    </el-dialog>

    <el-dialog
      title="订单详情"
      :visible.sync="detailVisible"
      width="720px"
    >
      <div v-if="detailData" class="detail-box">
        <div class="detail-grid">
          <div><strong>订单号：</strong>{{ detailData.number }}</div>
          <div><strong>状态：</strong>{{ getOrderStatusText(detailData.status) }}</div>
          <div><strong>收货人：</strong>{{ detailData.consignee || '--' }}</div>
          <div><strong>手机号：</strong>{{ detailData.phone || '--' }}</div>
          <div><strong>助餐点：</strong>{{ getDisplayDiningPointName(detailData) }}</div>
          <div><strong>志愿者：</strong>{{ detailData.volunteerName || '--' }}</div>
          <div><strong>老人：</strong>{{ getDisplayElderName(detailData) }}</div>
          <div><strong>下单时间：</strong>{{ detailData.orderTime || '--' }}</div>
          <div class="full"><strong>地址：</strong>{{ detailData.address || '--' }}</div>
          <div class="full"><strong>备注：</strong>{{ detailData.remark || '--' }}</div>
        </div>

        <el-table :data="detailData.orderDetailList || []" border size="mini">
          <el-table-column prop="name" label="菜品" min-width="180" />
          <el-table-column prop="number" label="数量" width="90" />
          <el-table-column label="金额" width="110">
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
import { Component, Vue } from 'vue-property-decorator'
import {
  cancelAdminOrder,
  dispatchOrder,
  getAdminOrderDetail,
  getAdminOrderPage,
  getAdminOrderStatistics,
  markMealReady,
  rejectAdminOrder
} from '@/api/adminOrder'
import { AvailableVolunteerOption, getAvailableVolunteerList } from '@/api/adminVolunteer'
import { ORDER_STATUS, getOrderStatusTag, getOrderStatusText } from '@/constants/order'
import { UserModule } from '@/store/modules/user'
import OperatorOrderCenter from './components/OperatorOrderCenter.vue'

interface StatusTab {
  label: string
  value: number | null
}

@Component({
  name: 'OrderDispatch',
  components: {
    OperatorOrderCenter
  }
})
export default class OrderDispatch extends Vue {
  private readonly ORDER_STATUS = ORDER_STATUS
  private statistics: any = {}
  private loading = false
  private tableData: any[] = []
  private total = 0
  private page = 1
  private pageSize = 10
  private currentStatus: number | null = null
  private dateRange: string[] = []
  private query = {
    number: '',
    phone: ''
  }
  private detailVisible = false
  private detailData: any = null
  private dispatchVisible = false
  private dispatching = false
  private volunteers: AvailableVolunteerOption[] = []
  private dispatchRow: any = {}
  private dispatchForm = {
    id: 0,
    volunteerId: undefined as number | undefined
  }

  private statusTabs: StatusTab[] = [
    { label: '全部', value: null },
    { label: '待制作', value: ORDER_STATUS.TO_BE_SCHEDULED },
    { label: '制作中', value: ORDER_STATUS.PREPARING },
    { label: '待取餐', value: ORDER_STATUS.MEAL_READY },
    { label: '配送中', value: ORDER_STATUS.DELIVERY_IN_PROGRESS },
    { label: '已完成', value: ORDER_STATUS.COMPLETED },
    { label: '已取消', value: ORDER_STATUS.CANCELLED }
  ]

  get isAdmin() {
    return UserModule.roles.includes('ADMIN')
  }

  get isOperator() {
    return UserModule.roles.includes('OPERATOR')
  }

  created() {
    if (this.isOperator) {
      return
    }
    const routeStatus = this.$route.query.status
    if (routeStatus) {
      this.currentStatus = Number(routeStatus)
    }
    this.loadData()
  }

  private getOrderStatusText(status: number) {
    return getOrderStatusText(status)
  }

  private getDisplayElderName(order: any) {
    return order && order.elderName ? order.elderName : '历史订单未记录服务老人'
  }

  private getDisplayDiningPointName(order: any) {
    return order && order.diningPointName ? order.diningPointName : '历史订单未记录助餐点'
  }

  private getOrderStatusTag(status: number) {
    return getOrderStatusTag(status)
  }

  private async loadData() {
    this.loading = true
    try {
      const [pageRes, statRes] = await Promise.all([
        getAdminOrderPage({
          page: this.page,
          pageSize: this.pageSize,
          status: this.currentStatus === null ? undefined : this.currentStatus,
          number: this.query.number || undefined,
          phone: this.query.phone || undefined,
          beginTime: this.dateRange[0] || undefined,
          endTime: this.dateRange[1] || undefined
        }),
        getAdminOrderStatistics()
      ])

      const pageData = pageRes.data && pageRes.data.data ? pageRes.data.data : pageRes.data
      this.tableData = pageData && pageData.records ? pageData.records : []
      this.total = pageData && pageData.total ? pageData.total : 0
      this.statistics = statRes.data && statRes.data.data ? statRes.data.data : {}
    } finally {
      this.loading = false
    }
  }

  private changeStatus(status: number | null) {
    this.currentStatus = status
    this.page = 1
    this.loadData()
  }

  private handleQuery() {
    this.page = 1
    this.loadData()
  }

  private resetQuery() {
    this.query.number = ''
    this.query.phone = ''
    this.dateRange = []
    this.handleQuery()
  }

  private handleSizeChange(pageSize: number) {
    this.pageSize = pageSize
    this.page = 1
    this.loadData()
  }

  private handleCurrentChange(page: number) {
    this.page = page
    this.loadData()
  }

  private async openDetail(id: number) {
    const res = await getAdminOrderDetail(id)
    this.detailData = res.data && res.data.data ? res.data.data : res.data
    this.detailVisible = true
  }

  private async openDispatch(row: any) {
    this.dispatchRow = row
    this.dispatchForm = {
      id: row.id,
      volunteerId: row.volunteerId
    }
    try {
      if (this.volunteers.length === 0) {
        const res = await getAvailableVolunteerList()
        if (!res || !res.data || String(res.data.code) !== '1') {
          throw new Error((res && res.data && res.data.msg) || '获取志愿者列表失败')
        }
        const data = res.data && res.data.data ? res.data.data : res.data
        this.volunteers = Array.isArray(data) ? data : []
      }
      this.dispatchVisible = true
    } catch (error) {
      this.$message.error((error as Error).message || '获取志愿者列表失败')
    }
  }

  private async submitDispatch() {
    if (!this.dispatchForm.volunteerId) {
      this.$message.warning('请选择志愿者')
      return
    }
    this.dispatching = true
    try {
      await dispatchOrder({
        id: this.dispatchForm.id,
        volunteerId: this.dispatchForm.volunteerId
      })
      this.$message.success('分配成功')
      this.dispatchVisible = false
      this.loadData()
    } finally {
      this.dispatching = false
    }
  }

  private formatVolunteerLabel(item: AvailableVolunteerOption) {
    if (item.phone) {
      return `${item.name}（${item.phone}）`
    }
    return item.name || `志愿者#${item.id}`
  }

  private async handleMealReady(row: any) {
    try {
      await this.$confirm('确认该订单已经出餐完成？', '提示', { type: 'warning' })
      await markMealReady(row.id)
      this.$message.success('订单已进入待取餐')
      this.loadData()
    } catch (error) {
      if (error !== 'cancel' && error !== 'close') {
        throw error
      }
    }
  }

  private async handleReject(row: any) {
    try {
      const promptResult: any = await this.$prompt('请输入拒单原因', '拒单', {
        inputPlaceholder: '例如助餐点无法制作'
      })
      const value = promptResult && promptResult.value ? promptResult.value : ''
      await rejectAdminOrder({
        id: row.id,
        rejectionReason: value || '管理员拒单'
      })
      this.$message.success('已拒单')
      this.loadData()
    } catch (error) {
      if (error !== 'cancel' && error !== 'close') {
        throw error
      }
    }
  }

  private async handleCancel(row: any) {
    try {
      const promptResult: any = await this.$prompt('请输入取消原因', '取消订单', {
        inputPlaceholder: '例如用户电话取消'
      })
      const value = promptResult && promptResult.value ? promptResult.value : ''
      await cancelAdminOrder({
        id: row.id,
        cancelReason: value || '管理员取消'
      })
      this.$message.success('订单已取消')
      this.loadData()
    } catch (error) {
      if (error !== 'cancel' && error !== 'close') {
        throw error
      }
    }
  }

  private canMarkMealReady(row: any) {
    return this.isOperator && row.status === ORDER_STATUS.PREPARING
  }

  private canReject(row: any) {
    return this.isAdmin && row.status === ORDER_STATUS.TO_BE_SCHEDULED
  }

  private canCancel(row: any) {
    return [ORDER_STATUS.TO_BE_SCHEDULED, ORDER_STATUS.PREPARING, ORDER_STATUS.MEAL_READY, ORDER_STATUS.DELIVERY_IN_PROGRESS].includes(row.status)
  }
}
</script>

<style lang="scss" scoped>
.order-page {
  padding: 24px;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.stat-card {
  border-radius: 10px;
}

.stat-label {
  color: #909399;
  font-size: 14px;
}

.stat-value {
  margin-top: 8px;
  font-size: 28px;
  font-weight: 700;
  color: #303133;
}

.panel {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}

.tabs {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.tab-item {
  padding: 8px 16px;
  border-radius: 999px;
  background: #f4f4f5;
  color: #606266;
  cursor: pointer;

  &.active {
    background: #f8b500;
    color: #fff;
    font-weight: 600;
  }
}

.toolbar {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.pageList {
  margin-top: 16px;
  text-align: center;
}

.detail-box {
  .detail-grid {
    display: grid;
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 12px 16px;
    margin-bottom: 16px;
  }

  .full {
    grid-column: 1 / -1;
  }
}

.danger-text {
  color: #f56c6c;
}

@media (max-width: 900px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
}
</style>
