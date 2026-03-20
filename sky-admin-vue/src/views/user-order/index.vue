<template>
  <div class="family-order-page">
    <div class="page-header">
      <div>
        <h1>点餐</h1>
        <p>先确定当前服务老人，再在其默认助餐点范围内选择菜品并下单。</p>
      </div>
      <el-button type="text" @click="$router.push('/family-history')">
        查看历史订单
      </el-button>
    </div>

    <div class="service-panel">
      <div class="service-panel-main">
        <div class="panel-copy">
          <div class="panel-label">当前服务老人</div>
          <div class="panel-value">
            {{ selectedElderly ? selectedElderly.name : '未选择' }}
          </div>
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
        ></el-empty>
      </div>

      <div class="dish-panel">
        <div class="dish-panel-header">
          <div>
            <div class="panel-title">{{ currentCategoryName }}</div>
            <div class="dish-panel-subtitle">
              {{ selectedDiningPointName || '未绑定助餐点' }}
            </div>
          </div>
          <div class="dish-panel-meta">
            共 {{ allAvailableDishes.length }} 道可售菜品
          </div>
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
        ></el-empty>
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
      @clear="handleClearCart"
    />

    <el-dialog
      title="确认订单"
      :visible.sync="checkoutVisible"
      width="720px"
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
            <div class="address-card-body">
              {{ formatFullAddress(selectedAddress) }}
            </div>
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
                <span>&yen;{{ formatAmount(calculateCartItemSubtotal(item)) }}</span>
              </div>
            </div>
          </div>
        </div>

        <div class="checkout-section">
          <div class="section-title">配送要求</div>
          <el-form label-width="100px">
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
          <div class="section-title">费用明细</div>
          <div class="checkout-amount-list">
            <div class="amount-row">
              <span>菜品金额</span>
              <span>&yen;{{ formatAmount(cartSummary.dishAmount) }}</span>
            </div>
            <div class="amount-row">
              <span>配送费</span>
              <span>&yen;{{ formatAmount(cartSummary.deliveryFee) }}</span>
            </div>
            <div class="amount-row">
              <span>补贴金额</span>
              <span>-&yen;{{ formatAmount(cartSummary.subsidyAmount) }}</span>
            </div>
            <div class="amount-row total">
              <span>待支付</span>
              <span>&yen;{{ formatAmount(cartSummary.payAmount) }}</span>
            </div>
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
          提交并支付
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
  subShoppingCart
} from '@/api/shoppingCart'
import { resolveSelectedAddress, formatFullAddress } from '@/utils/familyAddress'
import {
  DEFAULT_DELIVERY_LEAD_MINUTES,
  FAMILY_CHECKOUT_DRAFT_STORAGE_KEY,
  CheckoutDraft,
  getDefaultEstimatedDeliveryTime,
  hasCheckoutDraftContent,
  isEstimatedDeliveryTimeValid,
  normalizeCheckoutDraft,
  normalizeCheckoutRemark
} from '@/utils/familyCheckout'
import {
  CartSummary,
  DishQuantityMap,
  buildDishQuantityMap,
  calculateCartItemSubtotal,
  calculateCartSummary,
  formatAmount,
  normalizeShoppingCartItems
} from '@/utils/familyCart'

type PageEmptyState =
  | ''
  | 'NO_ELDERLY'
  | 'SELECT_ELDERLY'
  | 'ELDERLY_NO_DINING_POINT'
  | 'CART_MISMATCH'

interface CheckoutFormState {
  elderId?: number
  diningPointId?: number
  remark: string
  estimatedDeliveryTime: string
}

@Component({
  name: 'UserOrder',
  components: {
    DishCard,
    CartBar,
    CartDrawer
  }
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

  private checkoutForm: CheckoutFormState = {
    elderId: undefined,
    diningPointId: undefined,
    remark: '',
    estimatedDeliveryTime: getDefaultEstimatedDeliveryTime(new Date(), DEFAULT_DELIVERY_LEAD_MINUTES)
  }

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

  get selectedElderId() {
    return this.selectedElderly ? Number(this.selectedElderly.id) : undefined
  }

  get selectedDiningPointId() {
    return this.selectedElderly && this.selectedElderly.diningPointId
      ? Number(this.selectedElderly.diningPointId)
      : null
  }

  get selectedDiningPointName() {
    return this.selectedElderly && this.selectedElderly.diningPointName
      ? this.selectedElderly.diningPointName
      : ''
  }

  get currentCategoryName() {
    const currentCategory = this.visibleCategories.find((item) => Number(item.id) === this.currentCategory)
    return currentCategory ? currentCategory.name : '当前菜品'
  }

  get dishQuantityMap(): DishQuantityMap {
    return buildDishQuantityMap(this.cartItems)
  }

  get cartSummary(): CartSummary {
    return calculateCartSummary(this.cartItems)
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
    return Boolean(
      this.selectedElderly &&
      this.selectedDiningPointId &&
      !this.hasCartContextMismatch &&
      this.cartMatchesSelectedElderly
    )
  }

  get showOrderingContent() {
    return this.pageEmptyState === ''
  }

  get checkoutDisabled() {
    return this.cartItems.length === 0 ||
      !this.selectedElderly ||
      !this.selectedDiningPointId ||
      this.pageEmptyState === 'CART_MISMATCH' ||
      !this.cartMatchesSelectedElderly
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
    if (this.selectedDiningPointName) {
      return `${this.selectedDiningPointName} 暂无可售菜品`
    }
    return '当前助餐点暂无可售菜品'
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
        this.loadCategories(),
        this.loadAddresses(),
        this.loadCart(),
        this.loadElderly()
      ])
      await this.syncFamilyOrderingContext()
      this.handleResumeCheckoutRequest()
    } finally {
      this.pageLoading = false
    }
  }

  private async loadCategories() {
    try {
      const response = await getFamilyCategoryList()
      const payload = this.extractPayload<Category[]>(response)
      this.categories = Array.isArray(payload)
        ? payload.filter((item) => item && (item.type === 1 || item.type === undefined))
        : []
    } catch (error) {
      this.categories = []
      this.$message.error(this.resolveErrorMessage(error, '分类加载失败，请稍后重试'))
    }
  }

  private async loadElderly() {
    try {
      const response = await getMyElderlyList()
      const payload = this.extractPayload<Elderly[]>(response)
      this.elderlyOptions = Array.isArray(payload) ? payload : []
    } catch (error) {
      this.elderlyOptions = []
      this.$message.error(this.resolveErrorMessage(error, '老人档案加载失败，请稍后重试'))
    }
  }

  private async loadCart() {
    try {
      const response = await getShoppingCartList()
      const payload = this.extractPayload<any[]>(response)
      this.cartItems = normalizeShoppingCartItems(payload)
    } catch (error) {
      this.cartItems = []
      this.$message.error(this.resolveErrorMessage(error, '购物车加载失败，请稍后重试'))
    }
  }

  private async loadAddresses() {
    try {
      const response = await getAddressBookList()
      const payload = this.extractPayload<AddressBook[]>(response)
      this.addressList = Array.isArray(payload) ? payload : []
      const preferredAddressId = Array.isArray(this.$route.query.selectedAddressId)
        ? this.$route.query.selectedAddressId[0]
        : this.$route.query.selectedAddressId
      const selectedAddress = resolveSelectedAddress(this.addressList, {
        preferredAddressId,
        currentSelectedAddressId: this.selectedAddressId
      })
      this.selectedAddressId = selectedAddress ? Number(selectedAddress.id) : null
    } catch (error) {
      this.addressList = []
      this.selectedAddressId = null
      this.$message.error(this.resolveErrorMessage(error, '地址加载失败，请稍后重试'))
    }
  }

  private async syncFamilyOrderingContext() {
    if (this.elderlyOptions.length === 0) {
      this.selectedElderly = null
      this.pageEmptyState = 'NO_ELDERLY'
      this.resetDishData()
      this.syncCheckoutContextWithSelectedElderly()
      return
    }

    if (this.hasCartContextMismatch) {
      this.selectedElderly = null
      this.pageEmptyState = 'CART_MISMATCH'
      this.resetDishData()
      this.syncCheckoutContextWithSelectedElderly()
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
        this.syncCheckoutContextWithSelectedElderly()
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

      if (draftElderly) {
        nextElderly = draftElderly
      } else if (currentSelected) {
        nextElderly = currentSelected
      } else if (this.elderlyOptions.length === 1) {
        nextElderly = this.elderlyOptions[0]
      }
    }

    if (!nextElderly) {
      this.selectedElderly = null
      this.pageEmptyState = 'SELECT_ELDERLY'
      this.resetDishData()
      this.syncCheckoutContextWithSelectedElderly()
      return
    }

    await this.applySelectedElderly(nextElderly, false)
  }

  private async applySelectedElderly(elderly: Elderly | null, preserveCurrentCategory = true) {
    this.selectedElderly = elderly
    this.syncCheckoutContextWithSelectedElderly()

    if (!elderly) {
      this.pageEmptyState = this.elderlyOptions.length === 0 ? 'NO_ELDERLY' : 'SELECT_ELDERLY'
      this.resetDishData()
      return
    }

    if (!elderly.diningPointId) {
      this.pageEmptyState = 'ELDERLY_NO_DINING_POINT'
      this.resetDishData()
      return
    }

    if (this.hasCartContextMismatch) {
      this.pageEmptyState = 'CART_MISMATCH'
      this.resetDishData()
      return
    }

    if (
      this.cartItems.length > 0 &&
      (
        this.cartElderId !== Number(elderly.id) ||
        this.cartDiningPointId !== Number(elderly.diningPointId)
      )
    ) {
      this.pageEmptyState = 'CART_MISMATCH'
      this.resetDishData()
      return
    }

    this.pageEmptyState = ''
    await this.loadDishesForDiningPoint(Number(elderly.diningPointId), preserveCurrentCategory)
  }

  private async loadDishesForDiningPoint(diningPointId: number, preserveCurrentCategory = true) {
    this.dishLoading = true
    try {
      const response = await getFamilyDishList({ diningPointId })
      const payload = this.extractPayload<Dish[]>(response)
      const dishes = Array.isArray(payload) ? payload : []
      this.applyDishList(dishes, preserveCurrentCategory)
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

    const categoryIdsInDishList = Array.from(
      new Set(
        dishes
          .map((dish) => this.resolveDishCategoryId(dish))
          .filter((id): id is number => typeof id === 'number')
      )
    )

    this.categories
      .filter((category) => categoryIdsInDishList.includes(Number(category.id)))
      .forEach((category) => {
        const categoryId = Number(category.id)
        knownCategoryIds.add(categoryId)
        visibleCategories.push({
          id: categoryId,
          name: category.name,
          type: category.type
        })
      })

    dishes.forEach((dish) => {
      const categoryId = this.resolveDishCategoryId(dish)
      if (!categoryMap[categoryId]) {
        categoryMap[categoryId] = []
      }
      categoryMap[categoryId].push(dish)

      if (!knownCategoryIds.has(categoryId)) {
        knownCategoryIds.add(categoryId)
        visibleCategories.push({
          id: categoryId,
          name: dish.categoryName || '未分类'
        })
      }
    })

    this.visibleCategories = visibleCategories
    this.categoryDishMap = categoryMap

    const nextCategoryId = preserveCurrentCategory && this.currentCategory && this.categoryDishMap[this.currentCategory]
      ? this.currentCategory
      : (visibleCategories[0] ? Number(visibleCategories[0].id) : null)

    this.currentCategory = nextCategoryId
    this.currentDishes = nextCategoryId !== null && this.categoryDishMap[nextCategoryId]
      ? this.categoryDishMap[nextCategoryId]
      : []
  }

  private resolveDishCategoryId(dish: Dish) {
    return dish.categoryId !== undefined && dish.categoryId !== null
      ? Number(dish.categoryId)
      : -1
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
      await this.loadCart()
      await this.syncFamilyOrderingContext()
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

    const targetDish = this.allAvailableDishes.find((dish) => Number(dish.id) === Number(item.dishId))
    if (targetDish) {
      await this.increaseQuantity(targetDish)
      return
    }

    const elderId = item.elderId || this.requireCartActionElderId()
    if (!elderId) {
      return
    }

    this.cartSyncing = true
    try {
      await addShoppingCart({
        elderId,
        dishId: item.dishId,
        dishFlavor: item.dishFlavor
      })
      await this.loadCart()
      await this.syncFamilyOrderingContext()
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
      await this.loadCart()
      await this.syncFamilyOrderingContext()
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
      await subShoppingCart({
        elderId,
        dishId: item.dishId,
        dishFlavor: item.dishFlavor
      })
      await this.loadCart()
      await this.syncFamilyOrderingContext()
    } catch (error) {
      this.$message.error(this.resolveErrorMessage(error, '购物车更新失败，请稍后重试'))
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
        } catch (error) {
          return
        }

        const cleaned = await this.cleanCartForContextSwitch()
        if (!cleaned) {
          return
        }
      }

      this.selectedElderly = null
      this.pageEmptyState = 'SELECT_ELDERLY'
      this.resetDishData()
      this.syncCheckoutContextWithSelectedElderly()
      return
    }

    const nextElderly = this.elderlyOptions.find((item) => Number(item.id) === nextElderId)
    if (!nextElderly) {
      return
    }

    await this.switchSelectedElderly(nextElderly)
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
      } catch (error) {
        return
      }

      const cleaned = await this.cleanCartForContextSwitch()
      if (!cleaned) {
        return
      }
    }

    await this.applySelectedElderly(nextElderly, false)
  }

  private async cleanCartForContextSwitch() {
    this.cartSyncing = true
    try {
      await cleanShoppingCart()
      this.cartItems = []
      this.cartDrawerVisible = false
      this.checkoutVisible = false
      this.resetCheckoutFormForCurrentContext()
      return true
    } catch (error) {
      this.$message.error(this.resolveErrorMessage(error, '清空购物车失败，请稍后重试'))
      return false
    } finally {
      this.cartSyncing = false
    }
  }

  private async handleResolveCartMismatch() {
    const cleaned = await this.cleanCartForContextSwitch()
    if (!cleaned) {
      return
    }

    await this.syncFamilyOrderingContext()
    this.$message.success('购物车已清空，请重新选择服务老人')
  }

  private async handleClearCart() {
    if (this.cartItems.length === 0 || this.cartSyncing) {
      return
    }

    try {
      await this.$confirm('确认清空当前购物车吗？', '提示', {
        type: 'warning'
      })
    } catch (error) {
      return
    }

    const cleaned = await this.cleanCartForContextSwitch()
    if (cleaned) {
      this.$message.success('购物车已清空')
      await this.syncFamilyOrderingContext()
    }
  }

  private openCheckout() {
    if (this.checkoutDisabled) {
      if (!this.selectedElderly) {
        this.$message.warning('请先选择服务老人')
      } else if (!this.selectedDiningPointId) {
        this.$message.warning('当前老人未绑定助餐点，无法下单')
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

  private validateCheckoutContext() {
    if (!this.selectedElderly) {
      this.$message.warning('请先选择服务老人')
      return false
    }

    if (!this.selectedDiningPointId) {
      this.$message.warning('当前老人未绑定助餐点，无法下单')
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

    if (this.cartElderId !== Number(this.selectedElderly.id)) {
      this.$message.warning('当前购物车未绑定当前服务老人，请刷新后重试')
      return false
    }

    if (this.cartDiningPointId !== Number(this.selectedDiningPointId)) {
      this.$message.warning('当前购物车与老人对应助餐点不一致，请先清空购物车后重试')
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

    return true
  }

  private async submitCheckout() {
    if (!this.validateCheckoutContext()) {
      return
    }

    this.submitting = true
    try {
      const submitResponse = await submitOrder({
        elderId: Number(this.selectedElderly.id),
        addressBookId: Number(this.selectedAddress.id),
        payMethod: 1,
        remark: normalizeCheckoutRemark(this.checkoutForm.remark),
        estimatedDeliveryTime: this.checkoutForm.estimatedDeliveryTime || undefined
      })
      const orderData = this.extractPayload<OrderSubmitResponse>(submitResponse)

      if (!orderData || !orderData.orderNumber) {
        throw new Error('订单创建成功，但未返回有效订单号')
      }

      await paymentOrder({
        orderNumber: orderData.orderNumber,
        payMethod: 1
      })

      this.$message.success('下单成功')
      this.checkoutVisible = false
      await this.loadCart()
      this.resetCheckoutFormForCurrentContext()
      await this.syncFamilyOrderingContext()
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

    this.$router.push({
      path: '/family-addresses',
      query
    })
  }

  private goToAdminHint() {
    this.$message.info('请联系管理员先新增或关联老人档案')
  }

  private getCheckoutDraft() {
    try {
      const rawDraft = sessionStorage.getItem(FAMILY_CHECKOUT_DRAFT_STORAGE_KEY)
      return normalizeCheckoutDraft(rawDraft ? JSON.parse(rawDraft) : null)
    } catch (error) {
      return normalizeCheckoutDraft(null)
    }
  }

  private persistCheckoutDraft() {
    if (this.suppressDraftSync) {
      return
    }

    const draft: CheckoutDraft = normalizeCheckoutDraft({
      elderId: this.selectedElderly ? this.selectedElderly.id : undefined,
      diningPointId: this.selectedDiningPointId || undefined,
      remark: normalizeCheckoutRemark(this.checkoutForm.remark),
      estimatedDeliveryTime: this.checkoutForm.estimatedDeliveryTime
    })

    if (hasCheckoutDraftContent(draft)) {
      sessionStorage.setItem(FAMILY_CHECKOUT_DRAFT_STORAGE_KEY, JSON.stringify(draft))
      return
    }

    sessionStorage.removeItem(FAMILY_CHECKOUT_DRAFT_STORAGE_KEY)
  }

  private syncCheckoutContextWithSelectedElderly() {
    this.checkoutForm = {
      ...this.checkoutForm,
      elderId: this.selectedElderly ? Number(this.selectedElderly.id) : undefined,
      diningPointId: this.selectedDiningPointId || undefined,
      estimatedDeliveryTime: this.checkoutForm.estimatedDeliveryTime || getDefaultEstimatedDeliveryTime(new Date(), DEFAULT_DELIVERY_LEAD_MINUTES)
    }
    this.persistCheckoutDraft()
  }

  private resetCheckoutFormForCurrentContext() {
    this.suppressDraftSync = true
    this.checkoutForm = {
      elderId: this.selectedElderly ? Number(this.selectedElderly.id) : undefined,
      diningPointId: this.selectedDiningPointId || undefined,
      remark: '',
      estimatedDeliveryTime: getDefaultEstimatedDeliveryTime(new Date(), DEFAULT_DELIVERY_LEAD_MINUTES)
    }
    this.$nextTick(() => {
      this.suppressDraftSync = false
      this.persistCheckoutDraft()
    })
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
    const response = error && error.response && error.response.data
    return response && response.msg ? response.msg : fallbackMessage
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

.page-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
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

.service-panel {
  display: flex;
  flex-direction: column;
  gap: 14px;
  margin-bottom: 20px;
  padding: 18px 20px;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.08);
}

.service-panel-main {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
}

.panel-copy {
  min-width: 0;
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

.panel-hint {
  margin-top: 8px;
  color: #606266;
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
  align-items: start;
}

.category-panel,
.dish-panel,
.checkout-dialog {
  background: #fff;
  border-radius: 18px;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.08);
}

.category-panel {
  padding: 18px 16px;
}

.panel-title {
  color: #303133;
  font-size: 18px;
  font-weight: 600;
}

.category-item {
  margin-top: 12px;
  padding: 12px 14px;
  border-radius: 12px;
  background: #f7f9fc;
  color: #606266;
  cursor: pointer;
  transition: all 0.2s ease;

  &:hover {
    background: #fff4cf;
    color: #8a5a00;
  }

  &.active {
    background: #ffc200;
    color: #603c00;
    font-weight: 600;
  }
}

.dish-panel {
  padding: 22px;
}

.dish-panel-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 18px;
}

.dish-panel-subtitle,
.dish-panel-meta {
  margin-top: 8px;
  color: #909399;
  font-size: 13px;
}

.dish-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
}

.quantity-control {
  display: flex;
  align-items: center;
  gap: 10px;
}

.quantity {
  min-width: 20px;
  text-align: center;
  color: #303133;
  font-weight: 600;
}

.empty-state-wrapper {
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 360px;
  border-radius: 18px;
  background: #fff;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.08);
}

.checkout-dialog {
  padding: 4px;
  box-shadow: none;
}

.checkout-section + .checkout-section {
  margin-top: 22px;
}

.section-title {
  margin-bottom: 12px;
  color: #303133;
  font-size: 16px;
  font-weight: 600;
}

.section-title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.service-summary-card,
.address-card,
.checkout-order-list,
.checkout-amount-list {
  padding: 16px;
  border-radius: 14px;
  background: #f8fafc;
}

.checkout-order-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;

  & + .checkout-order-item {
    margin-top: 12px;
    padding-top: 12px;
    border-top: 1px solid #e5e7eb;
  }
}

.checkout-order-main {
  min-width: 0;
}

.checkout-order-name {
  color: #303133;
  font-size: 14px;
  font-weight: 600;
}

.checkout-order-meta {
  margin-top: 6px;
  color: #909399;
  font-size: 12px;
}

.checkout-order-side {
  display: flex;
  align-items: center;
  gap: 16px;
  color: #303133;
  font-size: 14px;
  white-space: nowrap;
}

.service-summary-row,
.amount-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  color: #303133;
  font-size: 14px;

  & + .service-summary-row,
  & + .amount-row {
    margin-top: 12px;
  }

  &.full {
    align-items: flex-start;
  }

  .label {
    min-width: 72px;
    color: #909399;
  }
}

.amount-row.total {
  padding-top: 12px;
  border-top: 1px solid #e5e7eb;
  color: #111827;
  font-size: 16px;
  font-weight: 600;
}

.address-card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  color: #303133;
  font-weight: 600;
}

.address-card-body {
  margin-top: 10px;
  color: #606266;
  line-height: 1.6;
}

@media (max-width: 1200px) {
  .page-body {
    grid-template-columns: 180px minmax(0, 1fr);
  }

  .elderly-select {
    width: 260px;
  }
}

@media (max-width: 900px) {
  .family-order-page {
    padding-left: 16px;
    padding-right: 16px;
  }

  .page-body {
    grid-template-columns: 1fr;
  }

  .service-panel-main,
  .dish-panel-header,
  .section-title-row {
    flex-direction: column;
    align-items: stretch;
  }

  .elderly-select {
    width: 100%;
  }
}

@media (max-width: 768px) {
  .family-order-page {
    padding-top: 16px;
  }

  .page-header {
    flex-direction: column;
  }

  .dish-grid {
    grid-template-columns: 1fr;
  }
}
</style>




