<template>
  <div class="order-item" @click="handleClick">
    <div class="order-header">
      <span class="order-number">订单号：{{ order.number || order.orderNumber || '--' }}</span>
      <span :class="['status', getStatusClass(order.status)]">
        {{ getOrderStatusText(order.status) }}
      </span>
    </div>

    <div class="order-meta">
      <span>老人：{{ getDisplayElderName(order) }}</span>
      <span>助餐点：{{ getDisplayDiningPointName(order) }}</span>
    </div>

    <div class="order-content">
      <div v-if="orderDetails.length > 0" class="dish-list">
        <div
          v-for="detail in orderDetails"
          :key="`${detail.id || detail.dishId || detail.name}-${detail.dishFlavor || ''}-${detail.number}`"
          class="dish-item"
        >
          <img
            v-if="detail.dishImage || detail.image"
            :src="detail.dishImage || detail.image"
            class="dish-image"
            loading="lazy"
          >
          <div class="dish-info">
            <span class="dish-name">{{ detail.dishName || detail.name || '菜品' }}</span>
            <span v-if="detail.dishFlavor" class="dish-flavor">{{ detail.dishFlavor }}</span>
            <span class="dish-quantity">x{{ detail.number }}</span>
          </div>
        </div>
      </div>
      <div v-else class="dish-summary">
        {{ order.orderDishes || '暂无菜品摘要' }}
      </div>
    </div>

    <div class="order-footer">
      <span class="order-time">{{ formatTime(order.orderTime) }}</span>
      <span class="order-amount">￥{{ formatPrice(order.amount) }}</span>
    </div>

    <div class="order-actions">
      <slot name="actions" :order="order" />
    </div>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator'
import { Order, OrderDetail } from '@/api/order'
import { ORDER_STATUS, getOrderStatusText } from '@/constants/order'

@Component({
  name: 'OrderItem'
})
export default class OrderItem extends Vue {
  @Prop({ required: true }) private readonly order!: Order

  get orderDetails(): OrderDetail[] {
    return this.order.orderDetailList || this.order.orderDetails || []
  }

  private handleClick() {
    this.$emit('click', this.order)
  }

  private getOrderStatusText(status: number) {
    return getOrderStatusText(status)
  }

  private getStatusClass(status: number) {
    const classMap: Record<number, string> = {
      [ORDER_STATUS.PENDING_PAYMENT]: 'pending',
      [ORDER_STATUS.TO_BE_SCHEDULED]: 'scheduled',
      [ORDER_STATUS.PREPARING]: 'preparing',
      [ORDER_STATUS.MEAL_READY]: 'pickup',
      [ORDER_STATUS.DELIVERY_IN_PROGRESS]: 'delivering',
      [ORDER_STATUS.COMPLETED]: 'completed',
      [ORDER_STATUS.CANCELLED]: 'cancelled'
    }
    return classMap[status] || ''
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

  private formatPrice(price?: number) {
    return Number(price || 0).toFixed(2)
  }

  private getDisplayElderName(order: Order) {
    return order.elderName || '历史订单未记录服务老人'
  }

  private getDisplayDiningPointName(order: Order) {
    return order.diningPointName || '历史订单未记录助餐点'
  }
}
</script>

<style lang="scss" scoped>
.order-item {
  background: #fff;
  border-radius: 10px;
  margin-bottom: 12px;
  padding: 16px;
  box-shadow: 0 2px 8px rgba(15, 23, 42, 0.08);
  cursor: pointer;
  transition: box-shadow 0.2s ease;

  &:hover {
    box-shadow: 0 6px 18px rgba(15, 23, 42, 0.12);
  }

  .order-header,
  .order-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
  }

  .order-header {
    margin-bottom: 8px;
    padding-bottom: 12px;
    border-bottom: 1px solid #eef2f7;
  }

  .order-number {
    color: #606266;
  }

  .order-meta {
    display: flex;
    flex-wrap: wrap;
    gap: 16px;
    margin-bottom: 12px;
    color: #909399;
    font-size: 13px;
  }

  .status {
    padding: 4px 12px;
    border-radius: 999px;
    color: #fff;
    font-size: 12px;

    &.pending {
      background: #909399;
    }

    &.scheduled {
      background: #e6a23c;
    }

    &.preparing {
      background: #409eff;
    }

    &.pickup {
      background: #67c23a;
    }

    &.delivering {
      background: #409eff;
    }

    &.completed {
      background: #67c23a;
    }

    &.cancelled {
      background: #909399;
    }
  }

  .dish-list {
    display: flex;
    flex-wrap: wrap;
    gap: 10px;
  }

  .dish-item {
    display: flex;
    align-items: center;
    gap: 8px;
    padding: 8px;
    border-radius: 8px;
    background: #f8fafc;
  }

  .dish-image {
    width: 44px;
    height: 44px;
    border-radius: 6px;
    object-fit: cover;
  }

  .dish-info {
    display: flex;
    flex-direction: column;
    gap: 2px;
  }

  .dish-name {
    color: #303133;
    font-weight: 500;
  }

  .dish-flavor,
  .dish-quantity,
  .dish-summary,
  .order-time,
  .order-amount {
    color: #909399;
    font-size: 12px;
  }

  .order-content {
    margin-bottom: 12px;
  }

  .order-actions {
    margin-top: 12px;
  }
}
</style>
