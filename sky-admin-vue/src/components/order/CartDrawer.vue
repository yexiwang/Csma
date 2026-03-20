<template>
  <el-drawer
    :visible="visible"
    direction="btt"
    size="68%"
    :with-header="false"
    custom-class="family-cart-drawer"
    @close="handleClose"
  >
    <div class="cart-drawer">
      <div class="drawer-indicator"></div>

      <div class="drawer-header">
        <div>
          <h3 class="drawer-title">购物车</h3>
          <p class="drawer-subtitle">共 {{ summary.totalCount }} 份</p>
        </div>
        <el-button
          type="text"
          :disabled="syncing || items.length === 0"
          @click="handleClear"
        >
          清空购物车
        </el-button>
      </div>

      <div class="drawer-body" v-loading="syncing">
        <el-empty
          v-if="items.length === 0"
          description="购物车还是空的，先去选几份餐吧"
        />

        <div v-else class="cart-list">
          <div
            v-for="item in items"
            :key="getItemKey(item)"
            class="cart-item"
          >
            <div class="item-info">
              <div class="item-name">{{ item.name }}</div>
              <div v-if="item.dishFlavor" class="item-flavor">{{ item.dishFlavor }}</div>
              <el-button
                type="text"
                size="mini"
                class="item-remove"
                :disabled="syncing"
                @click="handleRemove(item)"
              >
                删除
              </el-button>
            </div>

            <div class="item-price">￥{{ formatAmount(calculateCartItemSubtotal(item)) }}</div>

            <div class="quantity-control">
              <el-button
                size="mini"
                icon="el-icon-minus"
                :disabled="syncing"
                @click="handleDecrease(item)"
              />
              <span class="quantity">{{ item.number }}</span>
              <el-button
                size="mini"
                icon="el-icon-plus"
                :disabled="syncing"
                @click="handleIncrease(item)"
              />
            </div>
          </div>
        </div>
      </div>

      <div class="drawer-footer">
        <div class="summary-row">
          <span>商品数量</span>
          <span>{{ summary.totalCount }} 份</span>
        </div>
        <div class="summary-row">
          <span>菜品金额</span>
          <span>￥{{ formatAmount(summary.dishAmount) }}</span>
        </div>
        <div class="summary-row">
          <span>配送费</span>
          <span>￥{{ formatAmount(summary.deliveryFee) }}</span>
        </div>
        <div class="summary-row">
          <span>餐具费</span>
          <span>￥{{ formatAmount(summary.tablewareFee) }}</span>
        </div>
        <div class="summary-row">
          <span>补贴金额</span>
          <span>-￥{{ formatAmount(summary.subsidyAmount) }}</span>
        </div>
        <div class="summary-row total">
          <span>待支付</span>
          <span>￥{{ formatAmount(summary.payAmount) }}</span>
        </div>
      </div>
    </div>
  </el-drawer>
</template>

<script lang="ts">
import { Component, Prop, Vue } from 'vue-property-decorator'
import { ShoppingCartItem } from '@/api/shoppingCart'
import { CartSummary, calculateCartItemSubtotal, formatAmount } from '@/utils/familyCart'

@Component({
  name: 'CartDrawer'
})
export default class CartDrawer extends Vue {
  @Prop({ default: false }) private readonly visible!: boolean
  @Prop({ default: () => [] }) private readonly items!: ShoppingCartItem[]
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
  @Prop({ default: false }) private readonly syncing!: boolean

  private calculateCartItemSubtotal = calculateCartItemSubtotal
  private formatAmount = formatAmount

  private getItemKey(item: ShoppingCartItem) {
    if (item.id) {
      return String(item.id)
    }
    return `${item.dishId || item.setmealId || item.name}-${item.dishFlavor || 'default'}`
  }

  private handleClose() {
    this.$emit('close')
  }

  private handleIncrease(item: ShoppingCartItem) {
    this.$emit('increase', item)
  }

  private handleDecrease(item: ShoppingCartItem) {
    this.$emit('decrease', item)
  }

  private handleRemove(item: ShoppingCartItem) {
    this.$emit('remove', item)
  }

  private handleClear() {
    this.$emit('clear')
  }
}
</script>

<style lang="scss" scoped>
.cart-drawer {
  display: flex;
  flex-direction: column;
  height: 100%;
  background: #fff;
}

.drawer-indicator {
  width: 48px;
  height: 4px;
  border-radius: 999px;
  background: #dcdfe6;
  margin: 12px auto 0;
}

.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 20px;
  border-bottom: 1px solid #ebeef5;
}

.drawer-title {
  margin: 0;
  font-size: 18px;
  color: #303133;
}

.drawer-subtitle {
  margin: 6px 0 0;
  font-size: 12px;
  color: #909399;
}

.drawer-body {
  flex: 1;
  overflow-y: auto;
  padding: 12px 20px 20px;
}

.cart-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.cart-item {
  display: grid;
  grid-template-columns: minmax(0, 1fr) auto auto;
  gap: 16px;
  align-items: center;
  padding: 14px 0;
  border-bottom: 1px solid #f2f6fc;
}

.item-info {
  min-width: 0;
}

.item-name {
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.item-flavor {
  margin-top: 4px;
  color: #909399;
  font-size: 12px;
}

.item-remove {
  padding: 0;
  margin-top: 6px;
}

.item-price {
  color: #f56c6c;
  font-size: 15px;
  font-weight: 600;
  white-space: nowrap;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 8px;
}

.quantity {
  min-width: 20px;
  text-align: center;
  color: #303133;
}

.drawer-footer {
  padding: 16px 20px calc(16px + env(safe-area-inset-bottom));
  border-top: 1px solid #ebeef5;
  background: #fffdf7;
}

.summary-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #606266;
  font-size: 14px;

  & + .summary-row {
    margin-top: 10px;
  }

  &.total {
    color: #303133;
    font-size: 16px;
    font-weight: 600;
  }
}

@media (max-width: 768px) {
  .drawer-header,
  .drawer-body,
  .drawer-footer {
    padding-left: 16px;
    padding-right: 16px;
  }

  .cart-item {
    grid-template-columns: minmax(0, 1fr);
    gap: 10px;
  }

  .item-price,
  .quantity-control {
    justify-content: space-between;
  }
}
</style>

<style lang="scss">
.app-wrapper:not(.mobile) .family-cart-drawer {
  left: $sideBarWidth !important;
  width: calc(100% - #{$sideBarWidth}) !important;
}

.app-wrapper.hideSidebar:not(.mobile) .family-cart-drawer {
  left: 80px !important;
  width: calc(100% - 80px) !important;
}

.app-wrapper.mobile .family-cart-drawer {
  left: 0 !important;
  width: 100% !important;
}
</style>
