<template>
  <div class="order-history-container">
    <div class="header">
      <h1>订单历史</h1>
    </div>

    <div class="tabs">
      <div
        v-for="tab in tabs"
        :key="String(tab.value)"
        :class="['tab-item', { active: currentStatus === tab.value }]"
        @click="changeStatus(tab.value)"
      >
        {{ tab.label }}
      </div>
    </div>

    <div v-loading="loading" class="order-list">
      <order-item
        v-for="order in orders"
        :key="order.id"
        :order="order"
        :class="['history-order-item', { 'is-created': isNewlyCreatedOrder(order.id) }]"
        @click="viewOrderDetail(order.id)"
      >
        <template #actions>
          <div class="action-buttons">
            <el-button
              v-if="order.status === ORDER_STATUS.PENDING_PAYMENT"
              size="mini"
              type="success"
              :loading="payingOrderId === order.id"
              @click.stop="handleContinuePayment(order)"
            >
              继续支付
            </el-button>
            <el-button
              v-if="order.status === ORDER_STATUS.PENDING_PAYMENT || order.status === ORDER_STATUS.TO_BE_SCHEDULED"
              size="mini"
              type="danger"
              @click.stop="handleCancel(order.id)"
            >
              取消订单
            </el-button>
            <el-button
              size="mini"
              type="primary"
              :loading="repeatingOrderId === order.id"
              @click.stop="handleRepetition(order.id)"
            >
              再来一单
            </el-button>
            <el-button
              v-if="canReminder(order.status)"
              size="mini"
              type="warning"
              @click.stop="handleReminder(order.id)"
            >
              催单
            </el-button>
            <el-button
              v-if="canReview(order)"
              size="mini"
              type="primary"
              @click.stop="openCreateReviewDialog(order)"
            >
              评价
            </el-button>
            <el-button
              v-else-if="canViewReview(order)"
              size="mini"
              @click.stop="openViewReviewDialog(order)"
            >
              查看评价
            </el-button>
          </div>
        </template>
      </order-item>

      <el-empty
        v-if="!loading && orders.length === 0"
        description="暂无订单"
      />
    </div>

    <el-pagination
      v-if="total > 0"
      class="pagination"
      :page-size="pageSize"
      :current-page="page"
      :total="total"
      layout="prev, pager, next"
      @current-change="handlePageChange"
    />

    <el-dialog
      title="订单详情"
      :visible.sync="detailVisible"
      width="760px"
      @close="handleDetailClose"
    >
      <div v-loading="detailLoading" class="detail-dialog">
        <div v-if="detailData" class="detail-body">
          <div class="detail-summary">
            <div class="detail-row">
              <span class="label">订单号</span>
              <span>{{ detailData.number || detailData.orderNumber || '--' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">订单状态</span>
              <el-tag :type="getOrderStatusTag(detailData.status)" size="mini">
                {{ getOrderStatusText(detailData.status) }}
              </el-tag>
            </div>
            <div class="detail-row">
              <span class="label">联系人</span>
              <span>{{ detailData.consignee || '--' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">联系电话</span>
              <span>{{ detailData.phone || '--' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">老人</span>
              <span>{{ getDisplayElderName(detailData) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">助餐点</span>
              <span>{{ getDisplayDiningPointName(detailData) }}</span>
            </div>
            <div class="detail-row full">
              <span class="label">配送地址</span>
              <span>{{ detailData.address || '--' }}</span>
            </div>
            <div class="detail-row full">
              <span class="label">备注</span>
              <span>{{ detailData.remark || '--' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">下单时间</span>
              <span>{{ formatTime(detailData.orderTime) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">支付完成时间</span>
              <span>{{ formatTime(detailData.checkoutTime) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">预计送达时间</span>
              <span>{{ formatTime(detailData.estimatedDeliveryTime || detailData.expectedTime) }}</span>
            </div>
            <div class="detail-row">
              <span class="label">实际送达/完成</span>
              <span>{{ formatTime(detailData.deliveryTime) }}</span>
            </div>
          </div>

          <div class="detail-section">
            <div class="section-title">
              菜品明细
            </div>
            <el-table
              :data="detailItems"
              border
              size="mini"
              empty-text="暂无菜品明细"
            >
              <el-table-column label="菜品" min-width="220">
                <template slot-scope="{ row }">
                  <div class="detail-dish-cell">
                    <img
                      v-if="row.dishImage || row.image"
                      :src="row.dishImage || row.image"
                      class="detail-dish-image"
                    >
                    <div class="detail-dish-copy">
                      <div class="name">
                        {{ row.dishName || row.name || '菜品' }}
                      </div>
                      <div v-if="row.dishFlavor" class="flavor">
                        {{ row.dishFlavor }}
                      </div>
                    </div>
                  </div>
                </template>
              </el-table-column>
              <el-table-column label="单价" width="110">
                <template slot-scope="{ row }">
                  ￥{{ formatPrice(row.amount) }}
                </template>
              </el-table-column>
              <el-table-column prop="number" label="数量" width="90" />
              <el-table-column label="小计" width="120">
                <template slot-scope="{ row }">
                  ￥{{ formatPrice(calculateDetailLineAmount(row)) }}
                </template>
              </el-table-column>
            </el-table>
          </div>

          <div class="detail-amount-list">
            <div class="detail-amount-row">
              <span>菜品金额</span>
              <span>￥{{ formatPrice(detailDishAmount) }}</span>
            </div>
            <div class="detail-amount-row">
              <span>配送费</span>
              <span>￥{{ formatPrice(detailData.deliveryFee) }}</span>
            </div>
            <div class="detail-amount-row">
              <span>餐具费</span>
              <span>￥{{ formatPrice(detailData.tablewareFee) }}</span>
            </div>
            <div class="detail-amount-row">
              <span>补贴金额</span>
              <span>-￥{{ formatPrice(detailData.subsidyAmount) }}</span>
            </div>
            <div class="detail-amount-row">
              <span>餐具说明</span>
              <span>{{ detailTablewareDescription }}</span>
            </div>
          </div>

          <div class="detail-total">
            <span>实付金额</span>
            <strong>￥{{ formatPrice(detailData.amount) }}</strong>
          </div>
        </div>

        <el-empty
          v-else-if="!detailLoading"
          description="订单详情为空"
        />
      </div>

      <div slot="footer" class="detail-footer">
        <el-button
          v-if="detailData && detailData.status === ORDER_STATUS.PENDING_PAYMENT"
          type="success"
          :loading="payingOrderId === detailData.id"
          @click="handleContinuePayment(detailData)"
        >
          继续支付
        </el-button>
        <el-button
          v-if="detailData && (detailData.status === ORDER_STATUS.PENDING_PAYMENT || detailData.status === ORDER_STATUS.TO_BE_SCHEDULED)"
          type="danger"
          @click="handleCancel(detailData.id)"
        >
          取消订单
        </el-button>
        <el-button
          v-if="detailData"
          type="primary"
          :loading="repeatingOrderId === detailData.id"
          @click="handleRepetition(detailData.id)"
        >
          再来一单
        </el-button>
        <el-button
          v-if="detailData && canReminder(detailData.status)"
          type="warning"
          @click="handleReminder(detailData.id)"
        >
          催单
        </el-button>
        <el-button
          v-if="detailData && canReview(detailData)"
          type="primary"
          @click="openCreateReviewDialog(detailData)"
        >
          评价
        </el-button>
        <el-button
          v-else-if="detailData && canViewReview(detailData)"
          @click="openViewReviewDialog(detailData)"
        >
          查看评价
        </el-button>
        <el-button @click="detailVisible = false">
          关闭
        </el-button>
      </div>
    </el-dialog>

    <el-dialog
      :title="reviewDialogTitle"
      :visible.sync="reviewDialogVisible"
      width="520px"
      append-to-body
      destroy-on-close
      :close-on-click-modal="!reviewSubmitting"
      :show-close="!reviewSubmitting"
      @close="handleReviewDialogClose"
    >
      <div v-loading="reviewLoading" class="review-dialog">
        <div v-if="reviewDialogOrder" class="review-order-brief">
          <div class="review-brief-row">
            <span class="label">订单号</span>
            <span>{{ reviewDialogOrder.number || reviewDialogOrder.orderNumber || '--' }}</span>
          </div>
          <div class="review-brief-row">
            <span class="label">服务老人</span>
            <span>{{ getDisplayElderName(reviewDialogOrder) }}</span>
          </div>
        </div>

        <div v-if="reviewDialogMode === 'view'" class="review-content">
          <div v-if="reviewData" class="review-view">
            <div class="review-score-row">
              <span class="label">评分</span>
              <div class="review-score-value">
                <el-rate :value="reviewData.score" disabled />
                <span class="review-score-text">{{ reviewData.score }} 分</span>
              </div>
            </div>
            <div class="review-text-block">
              <div class="label">
                评价内容
              </div>
              <div class="review-text">
                {{ reviewData.content || '用户未填写文字评价' }}
              </div>
            </div>
            <div class="review-time">
              评价时间：{{ formatTime(reviewData.createTime) }}
            </div>
          </div>
          <el-empty
            v-else-if="!reviewLoading"
            description="暂无评价内容"
          />
        </div>

        <div v-else class="review-content">
          <div class="review-score-row">
            <span class="label">评分</span>
            <div class="review-score-value">
              <el-rate v-model="reviewForm.score" />
              <span class="review-score-text">{{ reviewForm.score }} 分</span>
            </div>
          </div>
          <div class="review-text-block">
            <div class="label">
              文字评价
            </div>
            <el-input
              v-model="reviewForm.content"
              type="textarea"
              :rows="4"
              maxlength="255"
              placeholder="可选，简要描述本次服务体验"
            />
          </div>
        </div>
      </div>

      <div slot="footer" class="review-footer">
        <el-button :disabled="reviewSubmitting" @click="reviewDialogVisible = false">
          {{ reviewDialogMode === 'view' ? '关闭' : '取消' }}
        </el-button>
        <el-button
          v-if="reviewDialogMode === 'create'"
          type="primary"
          :loading="reviewSubmitting"
          @click="submitReview"
        >
          提交评价
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { Component, Vue } from 'vue-property-decorator'
import {
  cancelOrder,
  getOrderReview,
  getHistoryOrders,
  getOrderDetail,
  Order,
  OrderDetail,
  OrderReview,
  OrderReviewSubmitParams,
  paymentOrder,
  repetitionOrder,
  reminderOrder,
  submitOrderReview
} from '@/api/order'
import { ORDER_STATUS, getOrderStatusTag, getOrderStatusText } from '@/constants/order'
import OrderItem from '@/components/order/orderItem.vue'

interface TabOption {
  label: string
  value: number | null
}

type ReviewDialogMode = 'create' | 'view'

@Component({
  name: 'OrderHistory',
  components: {
    OrderItem
  }
})
export default class OrderHistory extends Vue {
  private readonly ORDER_STATUS = ORDER_STATUS
  private tabs: TabOption[] = [
    { label: '全部', value: null },
    { label: '待支付', value: ORDER_STATUS.PENDING_PAYMENT },
    { label: '待调度', value: ORDER_STATUS.TO_BE_SCHEDULED },
    { label: '制作中', value: ORDER_STATUS.PREPARING },
    { label: '待取餐', value: ORDER_STATUS.MEAL_READY },
    { label: '配送中', value: ORDER_STATUS.DELIVERY_IN_PROGRESS },
    { label: '已完成', value: ORDER_STATUS.COMPLETED },
    { label: '已取消', value: ORDER_STATUS.CANCELLED }
  ]

  private currentStatus: number | null = null
  private orders: Order[] = []
  private loading = false
  private page = 1
  private pageSize = 10
  private total = 0
  private payingOrderId: number | null = null
  private repeatingOrderId: number | null = null
  private highlightOrderId: number | null = null
  private createdOrderId: number | null = null
  private detailVisible = false
  private detailLoading = false
  private detailData: Order | null = null
  private reviewDialogVisible = false
  private reviewDialogMode: ReviewDialogMode = 'create'
  private reviewLoading = false
  private reviewSubmitting = false
  private reviewDialogOrder: Order | null = null
  private reviewData: OrderReview | null = null
  private reviewForm: OrderReviewSubmitParams = this.createDefaultReviewForm()

  created() {
    this.createdOrderId = this.parseOrderId(this.$route.query.createdOrderId)
    if (this.createdOrderId) {
      this.currentStatus = null
      this.page = 1
    }
    this.loadOrders()
  }

  get detailItems(): OrderDetail[] {
    if (!this.detailData) {
      return []
    }
    return this.detailData.orderDetailList || this.detailData.orderDetails || []
  }

  get detailDishAmount() {
    return this.detailItems.reduce((sum, item) => sum + this.calculateDetailLineAmount(item), 0)
  }

  get detailTablewareDescription() {
    if (!this.detailData) {
      return '--'
    }

    const effectiveTablewareNumber = this.detailItems.reduce((sum, item) => sum + Number(item.number || 0), 0)
    if (Number(this.detailData.tablewareStatus) === 1) {
      return `按餐量提供（${effectiveTablewareNumber} 份）`
    }
    if (Number(this.detailData.tablewareNumber || 0) > 0) {
      return `自定义 ${Number(this.detailData.tablewareNumber)} 份`
    }
    return '不需要餐具'
  }

  get reviewDialogTitle() {
    return this.reviewDialogMode === 'view' ? '查看评价' : '评价订单'
  }

  private createDefaultReviewForm(): OrderReviewSubmitParams {
    return {
      orderId: 0,
      score: 5,
      content: ''
    }
  }

  private parseOrderId(value: any) {
    if (Array.isArray(value)) {
      value = value[0]
    }

    const parsed = Number(value)
    return Number.isFinite(parsed) && parsed > 0 ? parsed : null
  }

  private getResponseData(res: any) {
    return res && res.data && res.data.data !== undefined ? res.data.data : res.data
  }

  private getOrderStatusText(status: number) {
    return getOrderStatusText(status)
  }

  private getOrderStatusTag(status: number) {
    return getOrderStatusTag(status)
  }

  private isNewlyCreatedOrder(orderId?: number) {
    return Number(orderId) === this.highlightOrderId
  }

  private calculateDetailLineAmount(detail: OrderDetail) {
    return Number(detail.amount || 0) * Number(detail.number || 0)
  }

  private formatPrice(price?: number) {
    return Number(price || 0).toFixed(2)
  }

  private getDisplayElderName(order: Order) {
    return order.elderName || '历史订单未记录服务老人'
  }

  private getDisplayDiningPointName(order: Order) {
    return order.diningPointName || '历史订单未记录助餐点'
  }

  private formatTime(time?: string) {
    if (!time) {
      return '--'
    }

    const date = new Date(time)
    if (Number.isNaN(date.getTime())) {
      return time
    }

    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hour = String(date.getHours()).padStart(2, '0')
    const minute = String(date.getMinutes()).padStart(2, '0')
    return `${year}-${month}-${day} ${hour}:${minute}`
  }

  private clearCreatedOrderQuery() {
    if (!this.$route.query.createdOrderId) {
      return
    }

    const nextQuery: Record<string, any> = { ...this.$route.query }
    delete nextQuery.createdOrderId

    this.$router.replace({
      path: this.$route.path,
      query: nextQuery
    }).catch(() => undefined)
  }

  private syncCreatedOrderHighlight() {
    if (!this.createdOrderId) {
      return
    }

    const matchedOrder = this.orders.find((item) => Number(item.id) === this.createdOrderId)
    this.highlightOrderId = matchedOrder ? Number(matchedOrder.id) : null
    this.clearCreatedOrderQuery()
    this.createdOrderId = null
  }

  private async loadOrders() {
    this.loading = true
    try {
      const params: { page: number; pageSize: number; status?: number } = {
        page: this.page,
        pageSize: this.pageSize
      }

      if (this.currentStatus !== null) {
        params.status = this.currentStatus
      }

      const res = await getHistoryOrders(params)
      const pageData = this.getResponseData(res) || {}
      this.orders = Array.isArray(pageData.records) ? pageData.records : []
      this.total = Number(pageData.total || 0)
      this.syncCreatedOrderHighlight()
    } catch (error) {
      this.orders = []
      this.total = 0
      this.$message.error(this.resolveErrorMessage(error, '订单历史加载失败，请重试'))
      this.clearCreatedOrderQuery()
      this.createdOrderId = null
    } finally {
      this.loading = false
    }
  }

  private changeStatus(status: number | null) {
    this.currentStatus = status
    this.page = 1
    this.loadOrders()
  }

  private handlePageChange(page: number) {
    this.page = page
    this.loadOrders()
  }

  private async viewOrderDetail(orderId: number) {
    this.detailVisible = true
    this.detailLoading = true
    this.detailData = null

    try {
      const res = await getOrderDetail(orderId)
      const data = this.getResponseData(res)
      this.detailData = data || null
    } catch (error) {
      this.detailVisible = false
      this.$message.error(this.resolveErrorMessage(error, '订单详情加载失败，请重试'))
    } finally {
      this.detailLoading = false
    }
  }

  private handleDetailClose() {
    this.detailLoading = false
    this.detailData = null
  }

  private async refreshDetailIfNeeded(orderId: number) {
    if (!this.detailVisible || !this.detailData || Number(this.detailData.id) !== Number(orderId)) {
      return
    }

    this.detailLoading = true
    try {
      const res = await getOrderDetail(orderId)
      const data = this.getResponseData(res)
      this.detailData = data || null
    } catch (_error) {
      this.detailVisible = false
      this.detailData = null
    } finally {
      this.detailLoading = false
    }
  }

  private async handleContinuePayment(order: Order) {
    const orderNumber = order.number || order.orderNumber
    if (!orderNumber) {
      this.$message.warning('当前订单缺少订单号，无法继续支付')
      return
    }

    this.payingOrderId = Number(order.id)
    try {
      await paymentOrder({
        orderNumber,
        payMethod: 1
      })
      this.$message.success('支付确认成功，订单已进入待调度')
      await this.loadOrders()
      await this.refreshDetailIfNeeded(Number(order.id))
    } catch (error) {
      const rawPaymentMessage = this.resolveErrorMessage(error, '')
      const paymentErrorMessage = this.resolvePaymentErrorMessage(error)
      if (rawPaymentMessage.includes('订单状态错误')) {
        await this.loadOrders()
        await this.refreshDetailIfNeeded(Number(order.id))
        this.$message.warning(paymentErrorMessage)
      } else if (paymentErrorMessage.includes('助餐点') && paymentErrorMessage.includes('休息')) {
        this.$message.warning(paymentErrorMessage)
      } else {
        this.$message.error(paymentErrorMessage)
      }
    } finally {
      this.payingOrderId = null
    }
  }

  private async handleCancel(orderId: number) {
    try {
      await this.$confirm('确认取消这笔订单吗？', '提示', { type: 'warning' })
      await cancelOrder(orderId)
      this.$message.success('订单已取消')
      await this.loadOrders()
      await this.refreshDetailIfNeeded(orderId)
    } catch (error) {
      if (error !== 'cancel' && error !== 'close') {
        this.$message.error(this.resolveErrorMessage(error, '取消订单失败，请重试'))
      }
    }
  }

  private async handleRepetition(orderId: number) {
    if (this.repeatingOrderId) {
      return
    }

    this.repeatingOrderId = orderId
    try {
      await repetitionOrder(orderId)
      await this.$router.push({
        path: '/family-order',
        query: {
          resumeCheckout: '1',
          repeatedOrderId: String(orderId)
        }
      })
    } catch (error) {
      this.$message.error(this.resolveErrorMessage(error, '再来一单失败，请重试'))
    } finally {
      this.repeatingOrderId = null
    }
  }

  private async handleReminder(orderId: number) {
    try {
      await reminderOrder(orderId)
      this.$message.success('催单成功')
    } catch (error) {
      this.$message.error(this.resolveErrorMessage(error, '催单失败，请重试'))
    }
  }

  private canReview(order: Order) {
    return order.status === ORDER_STATUS.COMPLETED && !order.reviewed
  }

  private canViewReview(order: Order) {
    return order.status === ORDER_STATUS.COMPLETED && !!order.reviewed
  }

  private openCreateReviewDialog(order: Order) {
    this.reviewDialogMode = 'create'
    this.reviewDialogOrder = order
    this.reviewData = null
    this.reviewLoading = false
    this.reviewSubmitting = false
    this.reviewForm = {
      orderId: Number(order.id),
      score: 5,
      content: ''
    }
    this.reviewDialogVisible = true
  }

  private async openViewReviewDialog(order: Order) {
    this.reviewDialogMode = 'view'
    this.reviewDialogOrder = order
    this.reviewData = null
    this.reviewLoading = true
    this.reviewSubmitting = false
    this.reviewForm = this.createDefaultReviewForm()
    this.reviewDialogVisible = true

    try {
      this.reviewData = await getOrderReview(Number(order.id))
      if (!this.reviewData) {
        this.reviewDialogVisible = false
        await this.loadOrders()
        await this.refreshDetailIfNeeded(Number(order.id))
        this.$message.warning('当前订单暂无评价内容')
      }
    } catch (error) {
      this.reviewDialogVisible = false
      this.$message.error(this.resolveErrorMessage(error, '获取评价失败，请重试'))
    } finally {
      this.reviewLoading = false
    }
  }

  private async submitReview() {
    if (!this.reviewDialogOrder) {
      return
    }
    if (!this.reviewForm.score || this.reviewForm.score < 1 || this.reviewForm.score > 5) {
      this.$message.warning('请先选择 1~5 分评分')
      return
    }

    this.reviewSubmitting = true
    try {
      await submitOrderReview({
        orderId: Number(this.reviewDialogOrder.id),
        score: this.reviewForm.score,
        content: this.reviewForm.content ? this.reviewForm.content.trim() : ''
      })
      this.$message.success('评价提交成功')
      const orderId = Number(this.reviewDialogOrder.id)
      this.closeReviewDialog()
      await this.loadOrders()
      await this.refreshDetailIfNeeded(orderId)
    } catch (error) {
      this.$message.error(this.resolveErrorMessage(error, '提交评价失败，请重试'))
    } finally {
      this.reviewSubmitting = false
    }
  }

  private handleReviewDialogClose() {
    if (this.reviewSubmitting) {
      return
    }
    this.closeReviewDialog()
  }

  private closeReviewDialog() {
    this.reviewDialogVisible = false
    this.reviewLoading = false
    this.reviewSubmitting = false
    this.reviewDialogOrder = null
    this.reviewData = null
    this.reviewForm = this.createDefaultReviewForm()
  }

  private canReminder(status: number) {
    const remindableStatusList: number[] = [
      ORDER_STATUS.TO_BE_SCHEDULED,
      ORDER_STATUS.PREPARING,
      ORDER_STATUS.MEAL_READY,
      ORDER_STATUS.DELIVERY_IN_PROGRESS
    ]
    return remindableStatusList.includes(status)
  }

  private resolveErrorMessage(error: any, fallbackMessage: string) {
    const responseMessage = error && error.response && error.response.data && error.response.data.msg
    const directMessage = error && error.message
    return responseMessage || directMessage || fallbackMessage
  }

  private resolvePaymentErrorMessage(error: any) {
    const message = this.resolveErrorMessage(error, '')
    if (message.includes('订单状态错误')) {
      return '订单状态已更新，请刷新查看'
    }
    return message || '继续支付失败，请重试'
  }
}
</script>

<style lang="scss" scoped>
.order-history-container {
  min-height: 100vh;
  background: #f5f7fa;

  .header {
    background: linear-gradient(135deg, #f8b500, #ffd86f);
    padding: 24px;
    text-align: center;
    color: #3c2f00;
  }

  .tabs {
    display: flex;
    gap: 10px;
    padding: 12px;
    background: #fff;
    overflow-x: auto;
  }

  .tab-item {
    padding: 10px 18px;
    border-radius: 999px;
    cursor: pointer;
    white-space: nowrap;
    color: #606266;

    &.active {
      background: #f8b500;
      color: #fff;
      font-weight: 600;
    }
  }

  .order-list {
    padding: 12px;
  }

  .action-buttons {
    display: flex;
    gap: 8px;
    flex-wrap: wrap;
  }

  .pagination {
    padding: 0 0 24px;
    text-align: center;
  }
}

.history-order-item.is-created {
  box-shadow: 0 0 0 2px rgba(248, 181, 0, 0.55), 0 12px 24px rgba(248, 181, 0, 0.18);
}

.detail-dialog {
  min-height: 220px;
}

.detail-summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
  padding: 16px;
  border-radius: 12px;
  background: #fff9e8;
}

.detail-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  color: #303133;
  line-height: 1.6;

  &.full {
    grid-column: 1 / -1;
  }

  .label {
    flex-shrink: 0;
    color: #909399;
  }
}

.detail-section {
  margin-top: 18px;
}

.section-title {
  margin-bottom: 12px;
  color: #303133;
  font-size: 15px;
  font-weight: 600;
}

.detail-dish-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.detail-dish-image {
  width: 44px;
  height: 44px;
  border-radius: 8px;
  object-fit: cover;
}

.detail-dish-copy {
  .name {
    color: #303133;
  }

  .flavor {
    margin-top: 4px;
    color: #909399;
    font-size: 12px;
  }
}

.detail-total {
  margin-top: 18px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 12px;
  color: #303133;

  strong {
    font-size: 18px;
    color: #f56c6c;
  }
}

.detail-amount-list {
  margin-top: 18px;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 14px 16px;
  border-radius: 12px;
  background: #fff9e8;
}

.detail-amount-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  color: #606266;
}

.detail-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

.review-order-brief,
.review-content {
  padding: 16px;
  border-radius: 12px;
  background: #fff9e8;
}

.review-order-brief {
  margin-bottom: 14px;
}

.review-brief-row,
.review-score-row {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  color: #303133;
  line-height: 1.6;
}

.review-brief-row + .review-brief-row,
.review-text-block,
.review-time {
  margin-top: 12px;
}

.review-score-value {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.review-score-text,
.review-time {
  color: #909399;
  font-size: 12px;
}

.review-text-block {
  .label {
    margin-bottom: 8px;
    color: #909399;
  }
}

.review-text {
  color: #303133;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.review-footer {
  display: flex;
  justify-content: flex-end;
  gap: 8px;
  flex-wrap: wrap;
}

@media (max-width: 768px) {
  .detail-summary {
    grid-template-columns: 1fr;
  }

  .review-brief-row,
  .review-score-row,
  .review-score-value {
    flex-direction: column;
    align-items: flex-start;
    gap: 6px;
  }
}
</style>
