<template>
  <div class="cart-bar">
    <button class="cart-trigger" type="button" @click="handleOpenCart">
      <div class="cart-icon">
        <i class="el-icon-shopping-cart-2"></i>
        <span v-if="summary.totalCount > 0" class="cart-badge">{{ summary.totalCount }}</span>
      </div>
      <div class="cart-info">
        <span class="total-price">待支付：￥{{ formatAmount(summary.payAmount) }}</span>
        <span class="item-count">
          <template v-if="summary.totalCount > 0">
            共 {{ summary.totalCount }} 份，菜品 ￥{{ formatAmount(summary.dishAmount) }} / 配送 ￥{{ formatAmount(summary.deliveryFee) }} / 餐具 ￥{{ formatAmount(summary.tablewareFee) }}
          </template>
          <template v-else>
            购物车为空，请先选择商品
          </template>
        </span>
      </div>
    </button>

    <el-button
      type="primary"
      :loading="loading"
      :disabled="checkoutDisabled"
      @click="handleCheckout"
    >
      去结算
    </el-button>
  </div>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator'
import { CartSummary, formatAmount } from '@/utils/familyCart'

@Component({
  name: 'CartBar'
})
export default class CartBar extends Vue {
  @Prop({
    default: () => ({
      totalCount: 0,
      dishAmount: 0,
      deliveryFee: 0,
      tablewareFee: 0,
      subsidyAmount: 0,
      payAmount: 0,
      effectiveTablewareNumber: 0
    })
  }) private readonly summary!: CartSummary
  @Prop({ default: false }) private readonly loading!: boolean
  @Prop({ default: false }) private readonly checkoutDisabled!: boolean

  private formatAmount = formatAmount

  private handleCheckout() {
    this.$emit('checkout')
  }

  private handleOpenCart() {
    this.$emit('open-cart')
  }
}
</script>

<style lang="scss" scoped>
.cart-bar {
  position: fixed;
  left: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 16px 20px calc(16px + env(safe-area-inset-bottom));
  background: #fff;
  box-shadow: 0 -4px 18px rgba(15, 23, 42, 0.08);
  z-index: 3002;
}

.cart-trigger {
  display: flex;
  align-items: center;
  gap: 14px;
  flex: 1;
  min-width: 0;
  padding: 0;
  border: 0;
  background: none;
  text-align: left;
  cursor: pointer;
}

.cart-icon {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 46px;
  height: 46px;
  border-radius: 50%;
  background: #fff4d4;
  color: #d48806;
  font-size: 22px;
}

.cart-badge {
  position: absolute;
  top: -4px;
  right: -4px;
  min-width: 20px;
  height: 20px;
  padding: 0 6px;
  border-radius: 999px;
  background: #f56c6c;
  color: #fff;
  font-size: 12px;
  line-height: 20px;
  text-align: center;
}

.cart-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
  min-width: 0;
}

.item-count {
  color: #909399;
  font-size: 13px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.total-price {
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

@media (max-width: 768px) {
  .cart-bar {
    gap: 12px;
    padding-left: 16px;
    padding-right: 16px;
  }
}
</style>

<style lang="scss">
.app-wrapper:not(.mobile) .cart-bar {
  left: $sideBarWidth;
}

.app-wrapper.hideSidebar:not(.mobile) .cart-bar {
  left: 80px;
}

.app-wrapper.mobile .cart-bar {
  left: 0;
}
</style>
