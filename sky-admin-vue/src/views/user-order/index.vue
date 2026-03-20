<template>
  <div class="family-order-page">
    <div class="page-header">
      <div>
        <h1>点餐</h1>
        <p>先确定当前服务老人，再在其所属助餐点范围内选择菜品并下单。</p>
      </div>
      <el-button type="text" @click="$router.push('/family-history')">
        查看历史订单
      </el-button>
    </div>

    <div class="service-panel">
      <div class="service-panel-main">
        <div class="panel-copy">
          <div class="panel-label">当前服务老人</div>
          <div class="panel-value">{{ selectedElderly ? selectedElderly.name : '未选择' }}</div>
          <div class="panel-hint">
            {{ selectedDiningPointName ? `助餐点：${selectedDiningPointName}` : '请选择已绑定助餐点的老人后再点餐' }}
          </div>
        </div>

        <div class="panel-actions">
          <el-select
            v-if="elderlyOptions.length > 0"
            :value="selectedElderId"
            placeholder="请选择服务老人"
            clearable
            filterable
            class="elderly-select"
            @change="handleElderChange"
          >
            <el-option
              v-for="elderly in elderlyOptions"
              :key="elderly.id"
              :label="buildElderlyOptionLabel(elderly)"
              :value="elderly.id"
            />
          </el-select>

          <el-button type="primary" plain @click="goToAddressBook">
            地址管理
          </el-button>
        </div>
      </div>

      <el-alert
        v-if="cartContextHint"
        :title="cartContextHint"
        type="warning"
        show-icon
        :closable="false"
      />

      <el-alert
        v-if="isDiningPointResting"
        title="当前所属助餐点已休息，暂不可点餐或提交订单。"
        type="error"
        show-icon
        :closable="false"
      />
    </div>

    <div v-if="showOrderingContent" class="page-body" v-loading="pageLoading || dishLoading">
      <div class="category-panel">
        <div class="panel-title">分类</div>
        <div
          v-for="category in visibleCategories"
          :key="category.id"
          :class="['category-item', { active: currentCategory === category.id }]"
          @click="selectCategory(category.id)"
        >
          {{ category.name }}
        </div>

        <el-empty
          v-if="!pageLoading && visibleCategories.length === 0"
          description="当前助餐点暂无可用分类"
          :image-size="80"
        />
      </div>

      <div class="dish-panel">
        <div class="dish-panel-header">
          <div>
            <div class="panel-title">{{ currentCategoryName }}</div>
            <div class="dish-panel-subtitle">{{ selectedDiningPointName || '未绑定助餐点' }}</div>
          </div>
          <div class="dish-panel-meta">共 {{ allAvailableDishes.length }} 道可售菜品</div>
        </div>

        <div v-if="currentDishes.length > 0" class="dish-grid">
          <dish-card
            v-for="dish in currentDishes"
            :key="dish.id"
            :dish="dish"
          >
            <template #actions>
              <div class="quantity-control">
                <el-button
                  size="mini"
                  icon="el-icon-minus"
                  :disabled="getCartQuantity(dish.id) === 0 || cartSyncing"
                  @click="decreaseQuantity(dish)"
                />
                <span class="quantity">{{ getCartQuantity(dish.id) }}</span>
                <el-button
                  size="mini"
                  icon="el-icon-plus"
                  :disabled="cartSyncing || !canAddDish"
                  @click="increaseQuantity(dish)"
                />
              </div>
            </template>
          </dish-card>
        </div>

        <el-empty
          v-else-if="!pageLoading"
          :description="dishEmptyDescription"
        />
      </div>
    </div>

    <div v-else class="empty-state-wrapper" v-loading="pageLoading">
      <el-empty :description="pageEmptyDescription">
        <el-button
          v-if="pageEmptyState === 'NO_ELDERLY'"
          type="primary"
          @click="goToAdminHint"
        >
          已了解
        </el-button>
        <el-button
          v-if="pageEmptyState === 'CART_MISMATCH'"
          type="primary"
          :loading="cartSyncing"
          @click="handleResolveCartMismatch"
        >
          清空购物车并重新选择老人
        </el-button>
      </el-empty>
    </div>

    <cart-bar
      v-if="cartItems.length > 0"
      :summary="cartSummary"
      :loading="submitting"
      :checkout-disabled="checkoutDisabled"
      @checkout="openCheckout"
      @open-cart="cartDrawerVisible = true"
    />

    <cart-drawer
      :visible="cartDrawerVisible"
      :items="cartItems"
      :summary="cartSummary"
      :syncing="cartSyncing"
      @close="cartDrawerVisible = false"
      @increase="increaseQuantityByCartItem"
      @decrease="decreaseQuantityByCartItem"
      @remove="removeCartItem"
      @clear="handleClearCart"
    />

    <el-dialog
      title="确认订单"
      :visible.sync="checkoutVisible"
      width="760px"
      append-to-body
      @close="handleCheckoutClose"
    >
      <div class="checkout-dialog">
        <div class="checkout-section">
          <div class="section-title">服务对象</div>
          <div class="service-summary-card">
            <div class="service-summary-row">
              <span class="label">老人</span>
              <span>{{ selectedElderly ? selectedElderly.name : '--' }}</span>
            </div>
            <div class="service-summary-row">
              <span class="label">联系电话</span>
              <span>{{ selectedElderly && selectedElderly.phone ? selectedElderly.phone : '--' }}</span>
            </div>
            <div class="service-summary-row">
              <span class="label">助餐点</span>
              <span>{{ selectedDiningPointName || '--' }}</span>
            </div>
            <div class="service-summary-row full">
              <span class="label">老人地址</span>
              <span>{{ selectedElderly && selectedElderly.address ? selectedElderly.address : '--' }}</span>
            </div>
          </div>
        </div>

        <div class="checkout-section">
          <div class="section-title section-title-row">
            <span>配送地址</span>
            <el-button type="text" @click="goToAddressBook('checkout')">
              选择/管理地址
            </el-button>
          </div>

          <div v-if="selectedAddress" class="address-card">
            <div class="address-card-header">
              <span>{{ selectedAddress.consignee }}</span>
              <span>{{ selectedAddress.phone }}</span>
            </div>
            <div class="address-card-body">{{ formatFullAddress(selectedAddress) }}</div>
          </div>

          <el-empty
            v-else
            description="请先选择配送地址"
            :image-size="80"
          >
            <el-button type="primary" plain @click="goToAddressBook('checkout')">
              去选择地址
            </el-button>
          </el-empty>
        </div>

        <div class="checkout-section">
          <div class="section-title">菜品明细</div>
          <div class="checkout-order-list">
            <div
              v-for="item in cartItems"
              :key="getCartItemKey(item)"
              class="checkout-order-item"
            >
              <div class="checkout-order-main">
                <div class="checkout-order-name">{{ item.name }}</div>
                <div v-if="item.dishFlavor" class="checkout-order-meta">{{ item.dishFlavor }}</div>
              </div>
              <div class="checkout-order-side">
                <span>x{{ item.number }}</span>
                <span>￥{{ formatAmount(calculateCartItemSubtotal(item)) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="checkout-section">
          <div class="section-title">配送要求</div>
          <el-form label-width="110px">
            <el-form-item label="预计送达">
              <el-date-picker
                v-model="checkoutForm.estimatedDeliveryTime"
                type="datetime"
                format="yyyy-MM-dd HH:mm"
                value-format="yyyy-MM-dd HH:mm:ss"
                placeholder="请选择预计送达时间"
                :picker-options="deliveryTimePickerOptions"
                style="width: 100%"
              />
            </el-form-item>
            <el-form-item label="备注">
              <el-input
                v-model.trim="checkoutForm.remark"
                type="textarea"
                :rows="3"
                maxlength="120"
                show-word-limit
                placeholder="如口味要求、忌口说明、送达提醒等"
              />
            </el-form-item>
          </el-form>
        </div>

        <div class="checkout-section">
          <div class="section-title">餐具</div>
          <div class="tableware-card">
            <el-radio-group :value="tablewareMode" @input="handleTablewareModeChange">
              <el-radio-button label="none">不需要</el-radio-button>
              <el-radio-button label="auto">按餐量提供</el-radio-button>
              <el-radio-button label="custom">自定义数量</el-radio-button>
            </el-radio-group>

            <div v-if="tablewareMode === 'custom'" class="tableware-custom-row">
              <span>餐具数量</span>
              <el-input-number
                v-model="checkoutForm.tablewareNumber"
                :min="1"
                :max="99"
                controls-position="right"
                @change="handleCustomTablewareNumberChange"
              />
            </div>

            <div class="tableware-hint">{{ tablewareHintText }}</div>
          </div>
        </div>

        <div class="checkout-section">
          <div class="section-title">费用明细</div>
          <div class="checkout-amount-list">
            <div class="amount-row"><span>菜品金额</span><span>￥{{ formatAmount(cartSummary.dishAmount) }}</span></div>
            <div class="amount-row"><span>配送费</span><span>￥{{ formatAmount(cartSummary.deliveryFee) }}</span></div>
            <div class="amount-row"><span>餐具费</span><span>￥{{ formatAmount(cartSummary.tablewareFee) }}</span></div>
            <div class="amount-row"><span>补贴金额</span><span>-￥{{ formatAmount(cartSummary.subsidyAmount) }}</span></div>
            <div class="amount-row total"><span>待支付</span><span>￥{{ formatAmount(cartSummary.payAmount) }}</span></div>
          </div>
        </div>
      </div>

      <div slot="footer">
        <el-button @click="checkoutVisible = false">
          取消
        </el-button>
        <el-button
          type="primary"
          :loading="submitting"
          :disabled="checkoutSubmitDisabled"
          @click="submitCheckout"
        >
          提交订单
        </el-button>
      </div>
    </el-dialog>
  </div>
</template>

<script lang="ts">
import { Component, Vue, Watch } from 'vue-property-decorator'
import DishCard from '@/components/order/DishCard.vue'
import CartBar from '@/components/order/cartBar.vue'
import CartDrawer from '@/components/order/CartDrawer.vue'
import { AddressBook, getAddressBookList } from '@/api/addressBook'
import { Category, Dish, Elderly, getFamilyCategoryList, getFamilyDishList, getMyElderlyList } from '@/api/family'
import { OrderSubmitResponse, paymentOrder, submitOrder } from '@/api/order'
import {
  ShoppingCartItem,
  addShoppingCart,
  cleanShoppingCart,
  getShoppingCartList,
  getShoppingCartSummary,
  removeShoppingCart,
  subShoppingCart
} from '@/api/shoppingCart'
import { resolveSelectedAddress, formatFullAddress } from '@/utils/familyAddress'
import {
  buildTablewarePayload,
  CheckoutDraft,
  DEFAULT_DELIVERY_LEAD_MINUTES,
  FAMILY_CHECKOUT_DRAFT_STORAGE_KEY,
  getDefaultEstimatedDeliveryTime,
  hasCheckoutDraftContent,
  isEstimatedDeliveryTimeValid,
  normalizeCheckoutDraft,
  normalizeCheckoutRemark,
  resolveTablewareMode,
  TABLEWARE_STATUS_AUTO,
  TablewareMode
} from '@/utils/familyCheckout'
import {
  CartSummary,
  DishQuantityMap,
  buildDishQuantityMap,
  calculateCartItemSubtotal,
  createEmptyCartSummary,
  formatAmount,
  normalizeCartSummary,
  normalizeShoppingCartItems
} from '@/utils/familyCart'

type PageEmptyState = '' | 'NO_ELDERLY' | 'SELECT_ELDERLY' | 'ELDERLY_NO_DINING_POINT' | 'CART_MISMATCH'

interface CheckoutFormState {
  elderId?: number
  diningPointId?: number
  remark: string
  estimatedDeliveryTime: string
  tablewareStatus: number
  tablewareNumber: number
}

@Component({
  name: 'UserOrder',
  components: { DishCard, CartBar, CartDrawer }
})
export default class UserOrder extends Vue {
  private categories: Category[] = []
  private elderlyOptions: Elderly[] = []
  private selectedElderly: Elderly | null = null
  private allAvailableDishes: Dish[] = []
  private visibleCategories: Category[] = []
  private categoryDishMap: Record<number, Dish[]> = {}
  private currentCategory: number | null = null
  private currentDishes: Dish[] = []
  private cartItems: ShoppingCartItem[] = []
  private cartSummary: CartSummary = createEmptyCartSummary()
  private addressList: AddressBook[] = []
  private selectedAddressId: number | null = null
  private pageEmptyState: PageEmptyState = ''
  private pageLoading = false
  private dishLoading = false
  private cartSyncing = false
  private checkoutVisible = false
  private cartDrawerVisible = false
  private submitting = false
  private suppressDraftSync = false

  private checkoutForm: CheckoutFormState = this.createDefaultCheckoutForm()

  private readonly deliveryTimePickerOptions = {
    disabledDate: (time: Date) => time.getTime() < Date.now() - 60 * 1000
  }

  private formatAmount = formatAmount
  private formatFullAddress = formatFullAddress
  private calculateCartItemSubtotal = calculateCartItemSubtotal

  created() {
    this.initializePage()
  }

  @Watch('checkoutForm.remark')
  private onRemarkChanged() {
    this.persistCheckoutDraft()
  }

  @Watch('checkoutForm.estimatedDeliveryTime')
  private onEstimatedDeliveryTimeChanged() {
    this.persistCheckoutDraft()
  }

  @Watch('selectedAddressId')
  private onSelectedAddressIdChanged() {
    this.persistCheckoutDraft()
  }

  @Watch('$route')
  private async onRouteChanged() {
    await this.loadAddresses(false)
    this.handleResumeCheckoutRequest()
  }

  get selectedElderId() {
    return this.selectedElderly ? Number(this.selectedElderly.id) : undefined
  }

  get selectedDiningPointId() {
    return this.selectedElderly && this.selectedElderly.diningPointId ? Number(this.selectedElderly.diningPointId) : null
  }

  get selectedDiningPointName() {
    return this.selectedElderly && this.selectedElderly.diningPointName ? this.selectedElderly.diningPointName : ''
  }

  get isDiningPointResting() {
    if (!this.selectedElderly || this.selectedElderly.diningPointStatus === undefined || this.selectedElderly.diningPointStatus === null) {
      return false
    }
    return Number(this.selectedElderly.diningPointStatus) !== 1
  }

  get currentCategoryName() {
    const currentCategory = this.visibleCategories.find((item) => Number(item.id) === this.currentCategory)
    return currentCategory ? currentCategory.name : '当前菜品'
  }

  get dishQuantityMap(): DishQuantityMap {
    return buildDishQuantityMap(this.cartItems)
  }

  get cartElderId() {
    const elderIds = this.getUniqueDefinedNumbers(this.cartItems.map((item) => item.elderId))
    return elderIds.length === 1 ? elderIds[0] : null
  }

  get hasCartElderMismatch() {
    if (this.cartItems.length === 0) {
      return false
    }
    const elderIds = this.getUniqueDefinedNumbers(this.cartItems.map((item) => item.elderId))
    return elderIds.length !== 1 || this.cartItems.some((item) => item.elderId === undefined || item.elderId === null)
  }

  get cartDiningPointId() {
    const diningPointIds = this.getUniqueDefinedNumbers(this.cartItems.map((item) => item.diningPointId))
    return diningPointIds.length === 1 ? diningPointIds[0] : null
  }

  get hasCartDiningPointMismatch() {
    if (this.cartItems.length === 0) {
      return false
    }
    const diningPointIds = this.getUniqueDefinedNumbers(this.cartItems.map((item) => item.diningPointId))
    return diningPointIds.length !== 1 || this.cartItems.some((item) => item.diningPointId === undefined || item.diningPointId === null)
  }

  get hasCartContextMismatch() {
    return this.hasCartElderMismatch || this.hasCartDiningPointMismatch
  }

  get cartMatchesSelectedElderly() {
    if (this.cartItems.length === 0) {
      return true
    }
    if (!this.selectedElderly || !this.selectedDiningPointId) {
      return false
    }
    return this.cartElderId === this.selectedElderId && this.cartDiningPointId === this.selectedDiningPointId
  }

  get cartContextHint() {
    if (this.pageEmptyState === 'CART_MISMATCH') {
      return '当前购物车与当前服务老人不一致，或包含历史未绑定老人的数据，请先清空购物车后重新选择老人。'
    }
    if (this.cartItems.length > 0 && this.selectedElderId && this.cartElderId && this.cartElderId !== this.selectedElderId) {
      return '当前购物车已绑定其他老人，切换服务老人前将先清空购物车。'
    }
    return ''
  }

  get canAddDish() {
    return Boolean(this.selectedElderly && this.selectedDiningPointId && !this.isDiningPointResting && !this.hasCartContextMismatch && this.cartMatchesSelectedElderly)
  }

  get showOrderingContent() {
    return this.pageEmptyState === ''
  }

  get checkoutDisabled() {
    return this.cartItems.length === 0 || !this.selectedElderly || !this.selectedDiningPointId || this.isDiningPointResting || this.pageEmptyState === 'CART_MISMATCH' || !this.cartMatchesSelectedElderly
  }

  get checkoutSubmitDisabled() {
    return this.submitting || !this.selectedAddress || this.checkoutDisabled
  }

  get selectedAddress() {
    if (!this.selectedAddressId) {
      return null
    }
    return this.addressList.find((item) => Number(item.id) === this.selectedAddressId) || null
  }

  get tablewareMode(): TablewareMode {
    return resolveTablewareMode(this.checkoutForm.tablewareStatus, this.checkoutForm.tablewareNumber)
  }

  get tablewareHintText() {
    if (this.tablewareMode === 'auto') {
      return `按餐量提供，当前将按 ${this.cartSummary.effectiveTablewareNumber} 份计费。`
    }
    if (this.tablewareMode === 'custom') {
      return `自定义餐具数量，当前按 ${this.checkoutForm.tablewareNumber || 0} 份计费。`
    }
    return '不需要餐具，当前餐具费为 0 元。'
  }

  get pageEmptyDescription() {
    switch (this.pageEmptyState) {
      case 'NO_ELDERLY':
        return '请先新增或关联老人档案'
      case 'SELECT_ELDERLY':
        return '请先选择服务老人'
      case 'ELDERLY_NO_DINING_POINT':
        return '当前老人未绑定助餐点，无法下单'
      case 'CART_MISMATCH':
        return '购物车与当前服务老人不一致'
      default:
        return '暂无可展示内容'
    }
  }

  get dishEmptyDescription() {
    return this.selectedDiningPointName ? `${this.selectedDiningPointName} 暂无可售菜品` : '当前助餐点暂无可售菜品'
  }

  private createDefaultCheckoutForm(partial: Partial<CheckoutFormState> = {}): CheckoutFormState {
    return {
      elderId: partial.elderId,
      diningPointId: partial.diningPointId,
      remark: partial.remark || '',
      estimatedDeliveryTime: partial.estimatedDeliveryTime || getDefaultEstimatedDeliveryTime(new Date(), DEFAULT_DELIVERY_LEAD_MINUTES),
      tablewareStatus: partial.tablewareStatus === TABLEWARE_STATUS_AUTO ? TABLEWARE_STATUS_AUTO : 0,
      tablewareNumber: Math.max(0, Number(partial.tablewareNumber || 0))
    }
  }

  private getUniqueDefinedNumbers(values: Array<number | string | undefined | null>) {
    return Array.from(
      new Set(
        values
          .map((value) => (value !== undefined && value !== null ? Number(value) : null))
          .filter((value): value is number => value !== null && Number.isFinite(value))
      )
    )
  }

  private parseAddressId(value: any) {
    if (Array.isArray(value)) {
      value = value[0]
    }
    const parsed = Number(value)
    return Number.isFinite(parsed) && parsed > 0 ? parsed : null
  }

  private getCartItemKey(item: ShoppingCartItem) {
    return item.id || `${item.elderId || 'unknown'}-${item.dishId || 'dish'}-${item.dishFlavor || 'default'}`
  }

  private requireCartActionElderId() {
    if (!this.selectedElderly) {
      this.$message.warning('请先选择服务老人')
      return null
    }
    if (!this.selectedDiningPointId) {
      this.$message.warning('当前老人未绑定助餐点，无法点餐')
      return null
    }
    if (this.isDiningPointResting) {
      this.$message.warning('当前所属助餐点已休息，暂不可点餐')
      return null
    }
    if (this.hasCartContextMismatch || !this.cartMatchesSelectedElderly) {
      this.$message.warning('当前购物车与服务老人不一致，请先清空购物车后重试')
      return null
    }
    return Number(this.selectedElderly.id)
  }

  private async initializePage() {
    this.pageLoading = true
    try {
      await Promise.all([
        this.loadCategories(false),
        this.loadCart(false),
        this.loadElderly(false)
      ])
      await this.syncFamilyOrderingContext()
      await this.loadAddresses(false)
      await this.refreshCartSummaryForCurrentContext(false)
      this.handleResumeCheckoutRequest()
    } finally {
      this.pageLoading = false
    }
  }

  private async loadCategories(showError = true) {
    try {
      const response = await getFamilyCategoryList()
      const payload = this.extractPayload<Category[]>(response)
      this.categories = Array.isArray(payload)
        ? payload.filter((item) => item && (item.type === 1 || item.type === undefined))
        : []
    } catch (error) {
      this.categories = []
      if (showError) {
        this.$message.error(this.resolveErrorMessage(error, '分类加载失败，请稍后重试'))
      }
    }
  }

  private async loadElderly(showError = true) {
    try {
      const response = await getMyElderlyList()
      const payload = this.extractPayload<Elderly[]>(response)
      this.elderlyOptions = Array.isArray(payload) ? payload : []
    } catch (error) {
      this.elderlyOptions = []
      if (showError) {
        this.$message.error(this.resolveErrorMessage(error, '老人档案加载失败，请稍后重试'))
      }
    }
  }

  private async loadCart(showError = true) {
    try {
      const response = await getShoppingCartList()
      const payload = this.extractPayload<any[]>(response)
      this.cartItems = normalizeShoppingCartItems(payload)
    } catch (error) {
      this.cartItems = []
      if (showError) {
        this.$message.error(this.resolveErrorMessage(error, '购物车加载失败，请稍后重试'))
      }
    }
  }

  private async loadAddresses(showError = true) {
    try {
      const response = await getAddressBookList()
      const payload = this.extractPayload<AddressBook[]>(response)
      this.addressList = Array.isArray(payload) ? payload : []

      const routeSelectedAddressId = this.parseAddressId(this.$route.query.selectedAddressId)
      const draft = this.getCheckoutDraft()
      const preferredDraftAddressId = this.isDraftCompatible(draft) ? draft.selectedAddressId : undefined
      const selectedAddress = resolveSelectedAddress(this.addressList, {
        preferredAddressId: routeSelectedAddressId || preferredDraftAddressId,
        currentSelectedAddressId: this.selectedAddressId
      })
      this.selectedAddressId = selectedAddress ? Number(selectedAddress.id) : null
    } catch (error) {
      this.addressList = []
      this.selectedAddressId = null
      if (showError) {
        this.$message.error(this.resolveErrorMessage(error, '地址加载失败，请稍后重试'))
      }
    }
  }

  private async refreshCartState(preserveCurrentCategory = true) {
    await this.loadCart()
    await this.syncFamilyOrderingContext(preserveCurrentCategory)
    await this.refreshCartSummaryForCurrentContext()
  }

  private async refreshCartSummaryForCurrentContext(showError = true) {
    const fallbackSummary = createEmptyCartSummary({
      elderId: this.selectedElderId,
      diningPointId: this.selectedDiningPointId || undefined
    })

    if (!this.selectedElderId || !this.selectedDiningPointId || this.pageEmptyState !== '') {
      this.cartSummary = fallbackSummary
      return
    }

    try {
      const response = await getShoppingCartSummary({
        elderId: Number(this.selectedElderId),
        tablewareStatus: this.checkoutForm.tablewareStatus,
        tablewareNumber: this.checkoutForm.tablewareNumber
      })
      this.cartSummary = normalizeCartSummary(this.extractPayload(response), fallbackSummary)
    } catch (error) {
      this.cartSummary = fallbackSummary
      if (showError) {
        this.$message.error(this.resolveErrorMessage(error, '结算金额加载失败，请稍后重试'))
      }
    }
  }

  private async syncFamilyOrderingContext(preserveCurrentCategory = true) {
    if (this.elderlyOptions.length === 0) {
      this.selectedElderly = null
      this.pageEmptyState = 'NO_ELDERLY'
      this.resetDishData()
      this.syncCheckoutContextWithSelectedElderly(true)
      return
    }

    if (this.hasCartContextMismatch) {
      this.selectedElderly = null
      this.pageEmptyState = 'CART_MISMATCH'
      this.resetDishData()
      this.syncCheckoutContextWithSelectedElderly(true)
      return
    }

    let nextElderly: Elderly | null = null
    if (this.cartItems.length > 0) {
      nextElderly = this.cartElderId
        ? this.elderlyOptions.find((item) => Number(item.id) === Number(this.cartElderId)) || null
        : null

      if (!nextElderly) {
        this.selectedElderly = null
        this.pageEmptyState = 'CART_MISMATCH'
        this.resetDishData()
        this.syncCheckoutContextWithSelectedElderly(true)
        return
      }
    } else {
      const draft = this.getCheckoutDraft()
      const draftElderly = draft.elderId
        ? this.elderlyOptions.find((item) => Number(item.id) === Number(draft.elderId)) || null
        : null
      const currentSelected = this.selectedElderId
        ? this.elderlyOptions.find((item) => Number(item.id) === Number(this.selectedElderId)) || null
        : null

      if (currentSelected) {
        nextElderly = currentSelected
      } else if (draftElderly) {
        nextElderly = draftElderly
      } else if (this.elderlyOptions.length === 1) {
        nextElderly = this.elderlyOptions[0]
      }
    }

    if (!nextElderly) {
      this.selectedElderly = null
      this.pageEmptyState = 'SELECT_ELDERLY'
      this.resetDishData()
      this.syncCheckoutContextWithSelectedElderly(true)
      return
    }

    await this.applySelectedElderly(nextElderly, preserveCurrentCategory)
  }

  private async applySelectedElderly(elderly: Elderly | null, preserveCurrentCategory = true) {
    const previousElderId = this.checkoutForm.elderId
    const previousDiningPointId = this.checkoutForm.diningPointId
    this.selectedElderly = elderly

    const nextElderId = elderly ? Number(elderly.id) : undefined
    const nextDiningPointId = elderly && elderly.diningPointId ? Number(elderly.diningPointId) : undefined
    const contextChanged = previousElderId !== nextElderId || previousDiningPointId !== nextDiningPointId

    if (!elderly) {
      this.pageEmptyState = this.elderlyOptions.length === 0 ? 'NO_ELDERLY' : 'SELECT_ELDERLY'
      this.resetDishData()
      this.syncCheckoutContextWithSelectedElderly(contextChanged)
      return
    }

    if (!elderly.diningPointId) {
      this.pageEmptyState = 'ELDERLY_NO_DINING_POINT'
      this.resetDishData()
      this.syncCheckoutContextWithSelectedElderly(contextChanged)
      return
    }

    if (this.hasCartContextMismatch || (this.cartItems.length > 0 && (
      this.cartElderId !== Number(elderly.id) ||
      this.cartDiningPointId !== Number(elderly.diningPointId)
    ))) {
      this.pageEmptyState = 'CART_MISMATCH'
      this.resetDishData()
      this.syncCheckoutContextWithSelectedElderly(contextChanged)
      return
    }

    this.pageEmptyState = ''
    this.syncCheckoutContextWithSelectedElderly(contextChanged)
    await this.loadDishesForDiningPoint(Number(elderly.diningPointId), preserveCurrentCategory)
  }

  private syncCheckoutContextWithSelectedElderly(forceReset = false) {
    const nextElderId = this.selectedElderId
    const nextDiningPointId = this.selectedDiningPointId || undefined
    const currentContextChanged = this.checkoutForm.elderId !== nextElderId || this.checkoutForm.diningPointId !== nextDiningPointId

    if (!forceReset && !currentContextChanged) {
      this.checkoutForm = { ...this.checkoutForm, elderId: nextElderId, diningPointId: nextDiningPointId }
      this.persistCheckoutDraft()
      return
    }

    const draft = this.getCheckoutDraft()
    const canRestoreDraft = this.matchesCheckoutContext(draft.elderId, draft.diningPointId, nextElderId, nextDiningPointId)
    const routeSelectedAddressId = this.parseAddressId(this.$route.query.selectedAddressId)

    this.suppressDraftSync = true
    this.checkoutForm = this.createDefaultCheckoutForm({
      elderId: nextElderId,
      diningPointId: nextDiningPointId,
      remark: canRestoreDraft ? normalizeCheckoutRemark(draft.remark) : '',
      estimatedDeliveryTime: canRestoreDraft && draft.estimatedDeliveryTime
        ? draft.estimatedDeliveryTime
        : getDefaultEstimatedDeliveryTime(new Date(), DEFAULT_DELIVERY_LEAD_MINUTES),
      tablewareStatus: canRestoreDraft ? draft.tablewareStatus : 0,
      tablewareNumber: canRestoreDraft ? draft.tablewareNumber : 0
    })

    if (!routeSelectedAddressId && canRestoreDraft && draft.selectedAddressId) {
      this.selectedAddressId = draft.selectedAddressId
    }

    this.$nextTick(() => {
      this.suppressDraftSync = false
      this.persistCheckoutDraft()
    })
  }

  private matchesCheckoutContext(draftElderId?: number, draftDiningPointId?: number, elderId?: number, diningPointId?: number) {
    return Boolean(draftElderId && draftDiningPointId && elderId && diningPointId && Number(draftElderId) === Number(elderId) && Number(draftDiningPointId) === Number(diningPointId))
  }

  private isDraftCompatible(draft: CheckoutDraft) {
    return this.matchesCheckoutContext(draft.elderId, draft.diningPointId, this.selectedElderId, this.selectedDiningPointId || undefined)
  }

  private async loadDishesForDiningPoint(diningPointId: number, preserveCurrentCategory = true) {
    this.dishLoading = true
    try {
      const response = await getFamilyDishList({ diningPointId })
      const payload = this.extractPayload<Dish[]>(response)
      this.applyDishList(Array.isArray(payload) ? payload : [], preserveCurrentCategory)
    } catch (error) {
      this.resetDishData()
      this.$message.error(this.resolveErrorMessage(error, '菜品加载失败，请稍后重试'))
    } finally {
      this.dishLoading = false
    }
  }

  private applyDishList(dishes: Dish[], preserveCurrentCategory = true) {
    this.allAvailableDishes = dishes
    const categoryMap: Record<number, Dish[]> = {}
    const visibleCategories: Category[] = []
    const knownCategoryIds = new Set<number>()
    const categoryIdsInDishList = Array.from(new Set(dishes.map((dish) => this.resolveDishCategoryId(dish)).filter((id): id is number => typeof id === 'number')))

    this.categories
      .filter((category) => categoryIdsInDishList.includes(Number(category.id)))
      .forEach((category) => {
        const categoryId = Number(category.id)
        knownCategoryIds.add(categoryId)
        visibleCategories.push({ id: categoryId, name: category.name, type: category.type })
      })

    dishes.forEach((dish) => {
      const categoryId = this.resolveDishCategoryId(dish)
      if (!categoryMap[categoryId]) {
        categoryMap[categoryId] = []
      }
      categoryMap[categoryId].push(dish)
      if (!knownCategoryIds.has(categoryId)) {
        knownCategoryIds.add(categoryId)
        visibleCategories.push({ id: categoryId, name: dish.categoryName || '未分类' })
      }
    })

    this.visibleCategories = visibleCategories
    this.categoryDishMap = categoryMap
    const nextCategoryId = preserveCurrentCategory && this.currentCategory && this.categoryDishMap[this.currentCategory]
      ? this.currentCategory
      : (visibleCategories[0] ? Number(visibleCategories[0].id) : null)
    this.currentCategory = nextCategoryId
    this.currentDishes = nextCategoryId !== null && this.categoryDishMap[nextCategoryId] ? this.categoryDishMap[nextCategoryId] : []
  }

  private resolveDishCategoryId(dish: Dish) {
    return dish.categoryId !== undefined && dish.categoryId !== null ? Number(dish.categoryId) : -1
  }

  private resetDishData() {
    this.allAvailableDishes = []
    this.visibleCategories = []
    this.categoryDishMap = {}
    this.currentCategory = null
    this.currentDishes = []
  }

  private selectCategory(categoryId: number) {
    if (this.currentCategory === categoryId) {
      return
    }
    this.currentCategory = categoryId
    this.currentDishes = this.categoryDishMap[categoryId] || []
  }

  private getCartQuantity(dishId: number) {
    return this.dishQuantityMap[dishId] || 0
  }

  private async increaseQuantity(dish: Dish) {
    if (!this.ensureDishCanBeAdded()) {
      return
    }
    const elderId = this.requireCartActionElderId()
    if (!elderId) {
      return
    }
    this.cartSyncing = true
    try {
      await addShoppingCart({ elderId, dishId: dish.id })
      await this.refreshCartState()
    } catch (error) {
      this.$message.error(this.resolveErrorMessage(error, '加入购物车失败，请稍后重试'))
    } finally {
      this.cartSyncing = false
    }
  }

  private async increaseQuantityByCartItem(item: ShoppingCartItem) {
    if (item.dishId === undefined || item.dishId === null) {
      return
    }
    const elderId = item.elderId || this.requireCartActionElderId()
    if (!elderId) {
      return
    }
    this.cartSyncing = true
    try {
      await addShoppingCart({ elderId, dishId: item.dishId, dishFlavor: item.dishFlavor })
      await this.refreshCartState()
    } catch (error) {
      this.$message.error(this.resolveErrorMessage(error, '购物车更新失败，请稍后重试'))
    } finally {
      this.cartSyncing = false
    }
  }

  private async decreaseQuantity(dish: Dish) {
    const elderId = this.requireCartActionElderId()
    if (!elderId) {
      return
    }
    this.cartSyncing = true
    try {
      await subShoppingCart({ elderId, dishId: dish.id })
      await this.refreshCartState()
    } catch (error) {
      this.$message.error(this.resolveErrorMessage(error, '减少菜品失败，请稍后重试'))
    } finally {
      this.cartSyncing = false
    }
  }

  private async decreaseQuantityByCartItem(item: ShoppingCartItem) {
    if (item.dishId === undefined || item.dishId === null) {
      return
    }
    const elderId = item.elderId || this.requireCartActionElderId()
    if (!elderId) {
      return
    }
    this.cartSyncing = true
    try {
      await subShoppingCart({ elderId, dishId: item.dishId, dishFlavor: item.dishFlavor })
      await this.refreshCartState()
    } catch (error) {
      this.$message.error(this.resolveErrorMessage(error, '购物车更新失败，请稍后重试'))
    } finally {
      this.cartSyncing = false
    }
  }

  private async removeCartItem(item: ShoppingCartItem) {
    if (item.dishId === undefined || item.dishId === null) {
      return
    }
    const elderId = item.elderId || this.requireCartActionElderId()
    if (!elderId) {
      return
    }
    this.cartSyncing = true
    try {
      await removeShoppingCart({ elderId, dishId: item.dishId, dishFlavor: item.dishFlavor })
      await this.refreshCartState()
    } catch (error) {
      this.$message.error(this.resolveErrorMessage(error, '删除菜品失败，请稍后重试'))
    } finally {
      this.cartSyncing = false
    }
  }

  private ensureDishCanBeAdded() {
    if (!this.selectedElderly) {
      this.$message.warning('请先选择服务老人')
      return false
    }
    if (!this.selectedDiningPointId) {
      this.$message.warning('当前老人未绑定助餐点，无法点餐')
      return false
    }
    if (this.isDiningPointResting) {
      this.$message.warning('当前所属助餐点已休息，暂不可点餐')
      return false
    }
    if (this.pageEmptyState === 'CART_MISMATCH' || this.hasCartContextMismatch || !this.cartMatchesSelectedElderly) {
      this.$message.warning('请先清空购物车并重新选择老人')
      return false
    }
    return true
  }

  private async handleElderChange(value: number | string | undefined) {
    const nextElderId = value !== undefined && value !== null && value !== '' ? Number(value) : null
    if (!nextElderId) {
      if (this.cartItems.length > 0) {
        try {
          await this.$confirm('切换老人将清空当前购物车，是否继续？', '提示', {
            type: 'warning',
            confirmButtonText: '继续切换',
            cancelButtonText: '取消'
          })
        } catch (_error) {
          return
        }
        const cleaned = await this.cleanCartOnly()
        if (!cleaned) {
          return
        }
      }

      this.selectedElderly = null
      this.pageEmptyState = 'SELECT_ELDERLY'
      this.resetDishData()
      this.syncCheckoutContextWithSelectedElderly(true)
      await this.refreshCartSummaryForCurrentContext(false)
      return
    }

    const nextElderly = this.elderlyOptions.find((item) => Number(item.id) === nextElderId)
    if (nextElderly) {
      await this.switchSelectedElderly(nextElderly)
    }
  }

  private async switchSelectedElderly(nextElderly: Elderly) {
    if (this.selectedElderId && Number(this.selectedElderId) === Number(nextElderly.id)) {
      return
    }
    if (this.cartItems.length > 0) {
      try {
        await this.$confirm('切换老人将清空当前购物车，是否继续？', '提示', {
          type: 'warning',
          confirmButtonText: '继续切换',
          cancelButtonText: '取消'
        })
      } catch (_error) {
        return
      }
      const cleaned = await this.cleanCartOnly()
      if (!cleaned) {
        return
      }
    }

    await this.applySelectedElderly(nextElderly, false)
    await this.refreshCartSummaryForCurrentContext(false)
  }

  private async cleanCartOnly() {
    this.cartSyncing = true
    try {
      await cleanShoppingCart()
      this.cartItems = []
      this.cartSummary = createEmptyCartSummary({
        elderId: this.selectedElderId,
        diningPointId: this.selectedDiningPointId || undefined
      })
      this.cartDrawerVisible = false
      this.checkoutVisible = false
      return true
    } catch (error) {
      this.$message.error(this.resolveErrorMessage(error, '清空购物车失败，请稍后重试'))
      return false
    } finally {
      this.cartSyncing = false
    }
  }

  private async handleResolveCartMismatch() {
    const cleaned = await this.cleanCartOnly()
    if (!cleaned) {
      return
    }
    await this.syncFamilyOrderingContext(false)
    await this.refreshCartSummaryForCurrentContext(false)
    this.$message.success('购物车已清空，请重新选择服务老人')
  }

  private async handleClearCart() {
    if (this.cartItems.length === 0 || this.cartSyncing) {
      return
    }
    try {
      await this.$confirm('确认清空当前购物车吗？', '提示', { type: 'warning' })
    } catch (_error) {
      return
    }
    const cleaned = await this.cleanCartOnly()
    if (!cleaned) {
      return
    }
    await this.syncFamilyOrderingContext()
    await this.refreshCartSummaryForCurrentContext(false)
    this.$message.success('购物车已清空')
  }

  private openCheckout() {
    if (this.checkoutDisabled) {
      if (!this.selectedElderly) {
        this.$message.warning('请先选择服务老人')
      } else if (!this.selectedDiningPointId) {
        this.$message.warning('当前老人未绑定助餐点，无法下单')
      } else if (this.isDiningPointResting) {
        this.$message.warning('当前所属助餐点已休息，暂不可下单')
      } else if (this.hasCartContextMismatch || !this.cartMatchesSelectedElderly) {
        this.$message.warning('当前购物车与服务老人不一致，请先清空购物车后重试')
      } else {
        this.$message.warning('请先选择菜品')
      }
      return
    }
    if (!this.selectedAddress) {
      this.$message.warning('请先选择配送地址')
    }
    this.checkoutVisible = true
  }

  private handleCheckoutClose() {
    this.persistCheckoutDraft()
  }

  private async handleTablewareModeChange(mode: TablewareMode) {
    const payload = buildTablewarePayload(mode, this.checkoutForm.tablewareNumber)
    this.checkoutForm = {
      ...this.checkoutForm,
      tablewareStatus: payload.tablewareStatus,
      tablewareNumber: payload.tablewareNumber
    }
    this.persistCheckoutDraft()
    await this.refreshCartSummaryForCurrentContext()
  }

  private async handleCustomTablewareNumberChange(value: number | string) {
    if (this.tablewareMode !== 'custom') {
      return
    }
    this.checkoutForm = {
      ...this.checkoutForm,
      tablewareStatus: 0,
      tablewareNumber: Math.max(1, Number(value || 1))
    }
    this.persistCheckoutDraft()
    await this.refreshCartSummaryForCurrentContext()
  }

  private validateCheckoutContext() {
    if (!this.selectedElderly) {
      this.$message.warning('请先选择服务老人')
      return false
    }
    if (!this.selectedDiningPointId) {
      this.$message.warning('当前老人未绑定助餐点，无法下单')
      return false
    }
    if (this.isDiningPointResting) {
      this.$message.warning('当前所属助餐点已休息，暂不可下单')
      return false
    }
    if (this.cartItems.length === 0) {
      this.$message.warning('请先选择菜品')
      return false
    }
    if (this.hasCartContextMismatch || !this.cartMatchesSelectedElderly) {
      this.$message.warning('当前购物车与服务老人不一致，请先清空购物车后重试')
      return false
    }
    if (!this.selectedAddress) {
      this.$message.warning('请选择配送地址')
      return false
    }
    if (this.checkoutForm.estimatedDeliveryTime && !isEstimatedDeliveryTimeValid(this.checkoutForm.estimatedDeliveryTime)) {
      this.$message.warning('预计送达时间不能早于当前时间')
      return false
    }
    if (this.tablewareMode === 'custom' && Number(this.checkoutForm.tablewareNumber) <= 0) {
      this.$message.warning('自定义餐具数量必须大于 0')
      return false
    }
    return true
  }

  private async submitCheckout() {
    if (!this.validateCheckoutContext()) {
      return
    }
    this.submitting = true
    try {
      const submitResponse = await submitOrder({
        elderId: Number(this.selectedElderly && this.selectedElderly.id),
        addressBookId: Number(this.selectedAddress && this.selectedAddress.id),
        payMethod: 1,
        remark: normalizeCheckoutRemark(this.checkoutForm.remark),
        estimatedDeliveryTime: this.checkoutForm.estimatedDeliveryTime || undefined,
        deliveryStatus: 0,
        tablewareStatus: this.checkoutForm.tablewareStatus,
        tablewareNumber: this.checkoutForm.tablewareNumber,
        dishAmount: this.cartSummary.dishAmount,
        deliveryFee: this.cartSummary.deliveryFee,
        tablewareFee: this.cartSummary.tablewareFee,
        subsidyAmount: this.cartSummary.subsidyAmount,
        payAmount: this.cartSummary.payAmount
      })
      const orderData = this.extractPayload<OrderSubmitResponse>(submitResponse) || {} as OrderSubmitResponse
      const createdOrderId = Number(orderData.id)
      const orderNumber = orderData.orderNumber ? String(orderData.orderNumber) : ''

      if (!createdOrderId || !orderNumber) {
        throw new Error('订单创建成功，但未返回有效订单信息')
      }

      let paymentFailed = false
      let paymentFailedMessage = ''
      try {
        await paymentOrder({ orderNumber, payMethod: 1 })
      } catch (error) {
        paymentFailed = true
        paymentFailedMessage = this.resolvePaymentFollowupMessage(error)
      }

      this.cartItems = []
      this.cartSummary = createEmptyCartSummary({
        elderId: this.selectedElderId,
        diningPointId: this.selectedDiningPointId || undefined
      })
      this.checkoutVisible = false
      this.cartDrawerVisible = false
      this.clearCheckoutDraft()
      this.syncCheckoutContextWithSelectedElderly(true)

      if (paymentFailed) {
        this.$message.warning(paymentFailedMessage || '订单已创建，可在历史订单继续支付')
      } else {
        this.$message.success('下单成功')
      }

      await this.$router.push({
        path: '/family-history',
        query: { createdOrderId: String(createdOrderId) }
      })
    } catch (error) {
      this.$message.error(this.resolveErrorMessage(error, '下单失败，请稍后重试'))
    } finally {
      this.submitting = false
    }
  }

  private handleResumeCheckoutRequest() {
    if (this.$route.query.resumeCheckout !== '1') {
      return
    }
    if (!this.selectedElderly || this.checkoutDisabled) {
      return
    }
    this.checkoutVisible = true
    this.$router.replace({
      path: this.$route.path,
      query: {
        ...this.$route.query,
        resumeCheckout: undefined
      }
    }).catch(() => undefined)
  }

  private buildElderlyOptionLabel(elderly: Elderly) {
    const diningPointName = elderly.diningPointName || '未绑定助餐点'
    return `${elderly.name} / ${diningPointName}`
  }

  private goToAddressBook(mode: 'list' | 'checkout' = 'list') {
    const query: Record<string, string> = {}
    if (mode === 'checkout') {
      query.mode = 'checkout'
    }
    if (this.selectedAddressId) {
      query.selectedAddressId = String(this.selectedAddressId)
    }
    this.$router.push({ path: '/family-addresses', query })
  }

  private goToAdminHint() {
    this.$message.info('请联系管理员先新增或关联老人档案')
  }

  private getCheckoutDraft() {
    try {
      const rawDraft = sessionStorage.getItem(FAMILY_CHECKOUT_DRAFT_STORAGE_KEY)
      return normalizeCheckoutDraft(rawDraft ? JSON.parse(rawDraft) : null)
    } catch (_error) {
      return normalizeCheckoutDraft(null)
    }
  }

  private persistCheckoutDraft() {
    if (this.suppressDraftSync) {
      return
    }
    const draft: CheckoutDraft = normalizeCheckoutDraft({
      elderId: this.selectedElderId,
      diningPointId: this.selectedDiningPointId || undefined,
      selectedAddressId: this.selectedAddressId || undefined,
      remark: normalizeCheckoutRemark(this.checkoutForm.remark),
      estimatedDeliveryTime: this.checkoutForm.estimatedDeliveryTime,
      tablewareStatus: this.checkoutForm.tablewareStatus,
      tablewareNumber: this.checkoutForm.tablewareNumber
    })
    if (hasCheckoutDraftContent(draft)) {
      sessionStorage.setItem(FAMILY_CHECKOUT_DRAFT_STORAGE_KEY, JSON.stringify(draft))
      return
    }
    sessionStorage.removeItem(FAMILY_CHECKOUT_DRAFT_STORAGE_KEY)
  }

  private clearCheckoutDraft() {
    sessionStorage.removeItem(FAMILY_CHECKOUT_DRAFT_STORAGE_KEY)
  }

  private extractPayload<T>(response: any): T {
    if (response && response.data && response.data.data !== undefined) {
      return response.data.data as T
    }
    if (response && response.data !== undefined) {
      return response.data as T
    }
    return response as T
  }

  private resolveErrorMessage(error: any, fallbackMessage: string) {
    const responseMessage = error && error.response && error.response.data && error.response.data.msg
    const directMessage = error && error.message
    return responseMessage || directMessage || fallbackMessage
  }

  private resolvePaymentFollowupMessage(error: any) {
    const message = this.resolveErrorMessage(error, '')
    if (message.includes('助餐点') && message.includes('休息')) {
      return '订单已创建，但当前助餐点已休息，暂不可继续支付，请联系管理员处理'
    }
    if (message.includes('订单状态错误')) {
      return '订单状态已更新，请前往历史订单刷新查看'
    }
    return '订单已创建，可在历史订单继续支付'
  }
}
</script>

<style lang="scss" scoped>
.family-order-page {
  min-height: calc(100vh - 84px);
  padding: 24px 24px 112px;
  background:
    radial-gradient(circle at top left, rgba(255, 194, 0, 0.16), transparent 28%),
    linear-gradient(180deg, #fffdf7 0%, #f7f9fc 100%);
}

.page-header,
.service-panel-main,
.dish-panel-header,
.section-title-row,
.address-card-header,
.checkout-order-item {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.page-header {
  margin-bottom: 20px;

  h1 {
    margin: 0;
    font-size: 30px;
    color: #303133;
  }

  p {
    margin: 8px 0 0;
    color: #606266;
    font-size: 14px;
  }
}

.service-panel,
.category-panel,
.dish-panel,
.empty-state-wrapper,
.checkout-dialog {
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.08);
}

.service-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 20px;
  padding: 18px 20px;
}

.panel-label {
  color: #909399;
  font-size: 13px;
}

.panel-value {
  margin-top: 6px;
  color: #303133;
  font-size: 22px;
  font-weight: 600;
}

.panel-hint,
.dish-panel-subtitle,
.dish-panel-meta,
.tableware-hint {
  margin-top: 8px;
  color: #909399;
  font-size: 13px;
}

.panel-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.elderly-select {
  width: 320px;
}

.page-body {
  display: grid;
  grid-template-columns: 220px minmax(0, 1fr);
  gap: 20px;
}

.category-panel {
  padding: 18px 16px;
}

.panel-title,
.section-title {
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.section-title {
  margin-bottom: 14px;
  font-size: 16px;
}

.category-item {
  margin-top: 12px;
  padding: 12px 14px;
  border-radius: 12px;
  color: #606266;
  cursor: pointer;

  &.active {
    background: #f8b500;
    color: #fff;
    font-weight: 600;
  }
}

.dish-panel {
  padding: 20px;
}

.dish-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.quantity-control,
.tableware-custom-row,
.checkout-order-side {
  display: flex;
  align-items: center;
  gap: 12px;
}

.quantity {
  min-width: 20px;
  text-align: center;
}

.empty-state-wrapper {
  min-height: 360px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 32px;
}

.checkout-dialog {
  box-shadow: none;
}

.checkout-section {
  padding: 18px 22px;
  border-bottom: 1px solid #f2f6fc;
}

.checkout-section:last-child {
  border-bottom: 0;
}

.service-summary-card,
.address-card,
.tableware-card {
  padding: 16px;
  border-radius: 14px;
  background: #fff9e8;
}

.service-summary-card {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
}

.service-summary-row {
  display: flex;
  align-items: flex-start;
  gap: 10px;

  &.full {
    grid-column: 1 / -1;
  }

  .label {
    color: #909399;
    flex-shrink: 0;
  }
}

.address-card-body {
  margin-top: 8px;
  color: #606266;
  line-height: 1.7;
}

.checkout-order-list,
.checkout-amount-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.checkout-order-item {
  padding: 12px 0;
  border-bottom: 1px solid #f2f6fc;
}

.checkout-order-item:last-child {
  border-bottom: 0;
}

.checkout-order-name {
  color: #303133;
  font-weight: 600;
}

.checkout-order-meta {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
}

.amount-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #606266;

  &.total {
    color: #303133;
    font-size: 16px;
    font-weight: 700;
  }
}

@media (max-width: 960px) {
  .page-body {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .family-order-page {
    padding: 16px 16px 108px;
  }

  .page-header,
  .service-panel-main,
  .dish-panel-header,
  .section-title-row,
  .address-card-header,
  .checkout-order-item {
    flex-direction: column;
    align-items: flex-start;
  }

  .panel-actions {
    width: 100%;
    flex-direction: column;
    align-items: stretch;
  }

  .elderly-select {
    width: 100%;
  }

  .service-summary-card {
    grid-template-columns: 1fr;
  }

  .dish-grid {
    grid-template-columns: 1fr;
  }

  .checkout-order-side {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
