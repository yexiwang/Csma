<template>
  <div class="operator-order-page">
    <div class="summary-grid">
      <el-card
        v-for="card in summaryCards"
        :key="card.key"
        shadow="never"
        :class="['summary-card', { active: currentView === card.view }]"
        @click.native="changeView(card.view)"
      >
        <div class="summary-label">{{ card.label }}</div>
        <div class="summary-value">{{ overview[card.key] || 0 }}</div>
      </el-card>
    </div>

    <div class="board-panel">
      <div class="panel-header">
        <div class="title-wrap">
          <h2>{{ currentViewLabel }}</h2>
          <p>仅展示当前助餐点订单，按状态推进制作与配送协同。</p>
        </div>
        <div class="tabs">
          <div
            v-for="tab in viewTabs"
            :key="tab.value"
            :class="['tab-item', { active: currentView === tab.value }]"
            @click="changeView(tab.value)"
          >
            {{ tab.label }}
          </div>
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
          placeholder="联系电话"
          style="width: 180px"
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

      <el-row v-loading="loading" :gutter="20">
        <el-col
          v-for="item in tableData"
          :key="item.id"
          :xs="24"
          :sm="12"
          :xl="8"
          class="card-col"
        >
          <el-card shadow="hover" class="order-card">
            <div class="card-head">
              <div>
                <div class="order-number">订单号 {{ item.number }}</div>
                <div class="elder-line">{{ getDisplayElderName(item) }}</div>
              </div>
              <div class="tag-group">
                <el-tag :type="getOrderStatusTag(item.status)" size="mini">
                  {{ getOrderStatusText(item.status) }}
                </el-tag>
                <el-tag size="mini" type="info">
                  {{ item.handoverStatus || '待处理' }}
                </el-tag>
              </div>
            </div>

            <div class="card-body">
              <div class="info-row">
                <span class="label">配送地址</span>
                <span class="value address">{{ item.address || '--' }}</span>
              </div>
              <div class="info-row">
                <span class="label">菜品数量 / 金额</span>
                <span class="value">{{ getDishCount(item) }} 份 / ￥{{ Number(item.amount || 0).toFixed(2) }}</span>
              </div>
              <div class="info-row">
                <span class="label">下单时间</span>
                <span class="value">{{ item.orderTime || '--' }}</span>
              </div>
              <div class="info-row">
                <span class="label">期望送达</span>
                <span class="value">{{ item.expectedTime || item.estimatedDeliveryTime || '--' }}</span>
              </div>
              <div class="info-row">
                <span class="label">志愿者</span>
                <span class="value">{{ getVolunteerText(item) }}</span>
              </div>
            </div>

            <div class="card-actions">
              <el-button type="text" @click="openDetail(item.id)">查看详情</el-button>
              <el-button
                v-if="canStartPreparing(item)"
                type="text"
                @click="handleStartPreparing(item)"
              >
                开始制作
              </el-button>
              <el-button
                v-if="canAssignVolunteer(item)"
                type="text"
                @click="openAssignDialog(item)"
              >
                分配志愿者
              </el-button>
              <el-button
                v-if="canMarkMealReady(item)"
                type="text"
                @click="handleMealReady(item)"
              >
                标记出餐完成
              </el-button>
              <el-button
                v-if="canConfirmPickup(item)"
                type="text"
                @click="handlePickup(item)"
              >
                确认已取餐
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-empty
        v-if="!loading && tableData.length === 0"
        description="当前视图暂无订单"
      />

      <el-pagination
        v-if="total > 0"
        class="page-list"
        :page-sizes="[6, 12, 18, 24]"
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
      :visible.sync="assignVisible"
      width="760px"
    >
      <div v-if="assignOrder" class="assign-header">
        <div><strong>订单号：</strong>{{ assignOrder.number }}</div>
        <div><strong>老人：</strong>{{ getDisplayElderName(assignOrder) }}</div>
        <div><strong>当前状态：</strong>{{ getOrderStatusText(assignOrder.status) }}</div>
      </div>
      <el-table
        v-loading="volunteersLoading"
        :data="volunteers"
        border
        max-height="360"
      >
        <el-table-column label="选择" width="70" align="center">
          <template slot-scope="scope">
            <el-radio v-model="assignForm.volunteerId" :label="scope.row.id">
              &nbsp;
            </el-radio>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" min-width="120" />
        <el-table-column prop="phone" label="手机号" min-width="140" />
        <el-table-column label="状态" width="100">
          <template slot-scope="scope">
            <el-tag :type="scope.row.status === 1 ? 'success' : 'info'" size="mini">
              {{ scope.row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalOrders" label="累计服务单量" width="120" />
        <el-table-column prop="currentTaskCount" label="当前任务数" width="110" />
      </el-table>
      <div slot="footer">
        <el-button @click="assignVisible = false">取消</el-button>
        <el-button type="primary" :loading="assignSubmitting" @click="submitAssign">
          确认分配
        </el-button>
      </div>
    </el-dialog>

    <el-dialog
      title="订单详情"
      :visible.sync="detailVisible"
      width="760px"
    >
      <div v-if="detailData" class="detail-panel">
        <div class="detail-grid">
          <div><strong>订单号：</strong>{{ detailData.number }}</div>
          <div><strong>状态：</strong>{{ getOrderStatusText(detailData.status) }}</div>
          <div><strong>老人姓名：</strong>{{ getDisplayElderName(detailData) }}</div>
          <div><strong>老人电话：</strong>{{ detailData.elderPhone || '--' }}</div>
          <div><strong>志愿者：</strong>{{ detailData.volunteerName || '--' }}</div>
          <div><strong>志愿者电话：</strong>{{ detailData.volunteerPhone || '--' }}</div>
          <div><strong>交接状态：</strong>{{ detailData.handoverStatus || '--' }}</div>
          <div><strong>下单时间：</strong>{{ detailData.orderTime || '--' }}</div>
          <div><strong>期望送达：</strong>{{ detailData.expectedTime || detailData.estimatedDeliveryTime || '--' }}</div>
          <div><strong>送达时间：</strong>{{ detailData.deliveryTime || '--' }}</div>
          <div class="full"><strong>配送地址：</strong>{{ detailData.address || detailData.elderAddress || '--' }}</div>
          <div class="full"><strong>特殊需求：</strong>{{ detailData.elderSpecialNeeds || '--' }}</div>
          <div class="full"><strong>备注：</strong>{{ detailData.remark || '--' }}</div>
        </div>

        <el-table :data="detailData.orderDetailList || []" border size="mini">
          <el-table-column prop="name" label="菜品" min-width="180" />
          <el-table-column prop="number" label="数量" width="90" />
          <el-table-column label="金额" width="110">
            <template slot-scope="scope">
              ￥{{ Number(scope.row.amount || 0).toFixed(2) }}
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
  assignOperatorVolunteer,
  confirmOperatorPickup,
  getOperatorOrderBoard,
  getOperatorOrderDetail,
  getOperatorOrderOverview,
  getOperatorVolunteerOptions,
  markOperatorMealReady,
  OperatorOrderItem,
  OperatorOrderOverview,
  OperatorVolunteerOption,
  startOperatorPreparing
} from '@/api/operatorOrder'
import { ORDER_STATUS, getOrderStatusTag, getOrderStatusText } from '@/constants/order'

interface ViewTab {
  label: string
  value: string
  key: keyof OperatorOrderOverview
}

@Component({
  name: 'OperatorOrderCenter'
})
export default class OperatorOrderCenter extends Vue {
  private readonly ORDER_STATUS = ORDER_STATUS
  private readonly viewTabs: ViewTab[] = [
    { label: '待调度', value: 'PENDING_PREPARE', key: 'pendingPrepare' },
    { label: '制作中', value: 'PREPARING', key: 'preparing' },
    { label: '待分配志愿者', value: 'PENDING_ASSIGNMENT', key: 'pendingAssignment' },
    { label: '待取餐', value: 'MEAL_READY', key: 'mealReady' },
    { label: '配送中', value: 'DELIVERING', key: 'delivering' },
    { label: '已完成', value: 'COMPLETED', key: 'completed' }
  ]

  private loading = false
  private volunteersLoading = false
  private assignSubmitting = false
  private detailVisible = false
  private assignVisible = false
  private tableData: OperatorOrderItem[] = []
  private volunteers: OperatorVolunteerOption[] = []
  private total = 0
  private page = 1
  private pageSize = 6
  private currentView = 'PENDING_PREPARE'
  private dateRange: string[] = []
  private detailData: OperatorOrderItem | null = null
  private assignOrder: OperatorOrderItem | null = null
  private query = {
    number: '',
    phone: ''
  }

  private overview: OperatorOrderOverview = {
    pendingPrepare: 0,
    preparing: 0,
    pendingAssignment: 0,
    mealReady: 0,
    delivering: 0,
    completed: 0
  }

  private assignForm = {
    volunteerId: undefined as number | undefined
  }

  get summaryCards() {
    return this.viewTabs
  }

  get currentViewLabel() {
    const current = this.viewTabs.find((item) => item.value === this.currentView)
    return current ? current.label : '订单执行'
  }

  created() {
    this.loadData()
  }

  private getOrderStatusText(status: number) {
    return getOrderStatusText(status)
  }

  private getOrderStatusTag(status: number) {
    return getOrderStatusTag(status)
  }

  private async loadData() {
    this.loading = true
    try {
      const [boardData, overviewData] = await Promise.all([
        getOperatorOrderBoard({
          page: this.page,
          pageSize: this.pageSize,
          view: this.currentView,
          number: this.query.number || undefined,
          phone: this.query.phone || undefined,
          beginTime: this.dateRange[0] || undefined,
          endTime: this.dateRange[1] || undefined
        }),
        getOperatorOrderOverview()
      ])
      this.tableData = boardData.records || []
      this.total = boardData.total || 0
      this.overview = overviewData
    } catch (error) {
      this.$message.error((error as Error).message || '获取订单数据失败')
    } finally {
      this.loading = false
    }
  }

  private changeView(view: string) {
    if (this.currentView === view) {
      return
    }
    this.currentView = view
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

  private getDishCount(item: OperatorOrderItem) {
    const orderDetails = item.orderDetailList || []
    return orderDetails.reduce((sum, detail) => sum + Number(detail.number || 0), 0)
  }

  private getVolunteerText(item: OperatorOrderItem) {
    if (!item.volunteerName) {
      return '未分配志愿者'
    }
    return item.volunteerPhone ? `${item.volunteerName}（${item.volunteerPhone}）` : item.volunteerName
  }

  private getDisplayElderName(item: OperatorOrderItem | null) {
    return item && item.elderName ? item.elderName : '历史订单未记录服务老人'
  }

  private canStartPreparing(item: OperatorOrderItem) {
    return item.status === ORDER_STATUS.TO_BE_SCHEDULED
  }

  private canAssignVolunteer(item: OperatorOrderItem) {
    return item.status === ORDER_STATUS.TO_BE_SCHEDULED
      || item.status === ORDER_STATUS.PREPARING
      || item.status === ORDER_STATUS.MEAL_READY
  }

  private canMarkMealReady(item: OperatorOrderItem) {
    return item.status === ORDER_STATUS.PREPARING
  }

  private canConfirmPickup(item: OperatorOrderItem) {
    return item.status === ORDER_STATUS.MEAL_READY && !!item.volunteerId
  }

  private async openDetail(id: number) {
    try {
      this.detailData = await getOperatorOrderDetail(id)
      this.detailVisible = true
    } catch (error) {
      this.$message.error((error as Error).message || '获取订单详情失败')
    }
  }

  private async openAssignDialog(item: OperatorOrderItem) {
    this.assignOrder = item
    this.assignForm.volunteerId = item.volunteerId
    this.assignVisible = true
    this.volunteersLoading = true
    try {
      this.volunteers = await getOperatorVolunteerOptions()
    } catch (error) {
      this.$message.error((error as Error).message || '获取志愿者列表失败')
      this.assignVisible = false
    } finally {
      this.volunteersLoading = false
    }
  }

  private async submitAssign() {
    if (!this.assignOrder) {
      return
    }
    if (!this.assignForm.volunteerId) {
      this.$message.warning('请选择志愿者')
      return
    }
    this.assignSubmitting = true
    try {
      await assignOperatorVolunteer({
        orderId: this.assignOrder.id,
        volunteerId: this.assignForm.volunteerId
      })
      this.$message.success('志愿者分配成功')
      this.assignVisible = false
      this.loadData()
    } catch (error) {
      this.$message.error((error as Error).message || '志愿者分配失败')
    } finally {
      this.assignSubmitting = false
    }
  }

  private async handleStartPreparing(item: OperatorOrderItem) {
    try {
      await this.$confirm('确认将该订单流转为制作中？', '提示', { type: 'warning' })
      await startOperatorPreparing(item.id)
      this.$message.success('订单已进入制作中')
      this.loadData()
    } catch (error) {
      if (error !== 'cancel' && error !== 'close') {
        this.$message.error((error as Error).message || '操作失败')
      }
    }
  }

  private async handleMealReady(item: OperatorOrderItem) {
    try {
      await this.$confirm('确认该订单已出餐完成？', '提示', { type: 'warning' })
      await markOperatorMealReady(item.id)
      this.$message.success('订单已进入待取餐')
      this.loadData()
    } catch (error) {
      if (error !== 'cancel' && error !== 'close') {
        this.$message.error((error as Error).message || '操作失败')
      }
    }
  }

  private async handlePickup(item: OperatorOrderItem) {
    try {
      await this.$confirm('确认志愿者已完成取餐交接？', '提示', { type: 'warning' })
      await confirmOperatorPickup(item.id)
      this.$message.success('订单已进入配送中')
      this.loadData()
    } catch (error) {
      if (error !== 'cancel' && error !== 'close') {
        this.$message.error((error as Error).message || '操作失败')
      }
    }
  }
}
</script>

<style lang="scss" scoped>
.operator-order-page {
  padding: 24px;
}

.summary-grid {
  display: grid;
  grid-template-columns: repeat(6, minmax(0, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}

.summary-card {
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;

  &.active {
    border-color: #f8b500;
    box-shadow: 0 10px 20px rgba(248, 181, 0, 0.12);
  }
}

.summary-label {
  color: #909399;
  font-size: 13px;
}

.summary-value {
  margin-top: 8px;
  font-size: 30px;
  font-weight: 700;
  color: #303133;
}

.board-panel {
  background: #fff;
  border-radius: 12px;
  padding: 20px;
}

.panel-header {
  display: flex;
  justify-content: space-between;
  gap: 16px;
  align-items: flex-start;
  margin-bottom: 20px;
}

.title-wrap h2 {
  margin: 0;
  font-size: 22px;
  color: #303133;
}

.title-wrap p {
  margin: 8px 0 0;
  color: #909399;
  font-size: 13px;
}

.tabs {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  justify-content: flex-end;
}

.tab-item {
  padding: 8px 14px;
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
  margin-bottom: 20px;
}

.card-col {
  margin-bottom: 20px;
}

.order-card {
  min-height: 300px;
}

.card-head {
  display: flex;
  justify-content: space-between;
  gap: 12px;
  margin-bottom: 14px;
}

.order-number {
  font-size: 15px;
  font-weight: 600;
  color: #303133;
}

.elder-line {
  margin-top: 6px;
  color: #909399;
}

.tag-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
  align-items: flex-end;
}

.card-body {
  min-height: 160px;
}

.info-row {
  display: flex;
  gap: 12px;
  margin-bottom: 10px;
}

.label {
  width: 92px;
  color: #909399;
  flex-shrink: 0;
}

.value {
  color: #303133;
  line-height: 1.5;
}

.address {
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}

.card-actions {
  display: flex;
  gap: 14px;
  flex-wrap: wrap;
  padding-top: 8px;
  border-top: 1px solid #f0f2f5;
}

.page-list {
  margin-top: 12px;
  text-align: center;
}

.assign-header {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
  margin-bottom: 16px;
  color: #606266;
}

.detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
  margin-bottom: 16px;
}

.full {
  grid-column: 1 / -1;
}

@media (max-width: 1400px) {
  .summary-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .summary-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .panel-header {
    flex-direction: column;
  }

  .assign-header,
  .detail-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 640px) {
  .summary-grid {
    grid-template-columns: 1fr;
  }
}
</style>
