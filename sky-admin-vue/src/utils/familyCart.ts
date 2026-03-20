import { ShoppingCartItem } from '@/api/shoppingCart'

export interface CartSummary {
  elderId?: number
  diningPointId?: number
  totalCount: number
  dishAmount: number
  deliveryFee: number
  tablewareFee: number
  subsidyAmount: number
  payAmount: number
  effectiveTablewareNumber: number
}

export type DishQuantityMap = Record<number, number>

function toSafeNumber(value: number | string | undefined | null) {
  const nextValue = Number(value)
  return Number.isFinite(nextValue) ? nextValue : 0
}

function toOptionalPositiveNumber(value: number | string | undefined | null) {
  const nextValue = Number(value)
  return Number.isFinite(nextValue) && nextValue > 0 ? nextValue : undefined
}

export function toCent(value: number | string | undefined | null) {
  return Math.round(toSafeNumber(value) * 100)
}

export function fromCent(value: number) {
  return Number((value / 100).toFixed(2))
}

export function formatAmount(value: number | string | undefined | null) {
  return fromCent(toCent(value)).toFixed(2)
}

export function createEmptyCartSummary(partial: Partial<CartSummary> = {}): CartSummary {
  return {
    elderId: toOptionalPositiveNumber(partial.elderId),
    diningPointId: toOptionalPositiveNumber(partial.diningPointId),
    totalCount: Math.max(0, Number(partial.totalCount || 0)),
    dishAmount: fromCent(toCent(partial.dishAmount)),
    deliveryFee: fromCent(toCent(partial.deliveryFee)),
    tablewareFee: fromCent(toCent(partial.tablewareFee)),
    subsidyAmount: fromCent(toCent(partial.subsidyAmount)),
    payAmount: fromCent(toCent(partial.payAmount)),
    effectiveTablewareNumber: Math.max(0, Number(partial.effectiveTablewareNumber || 0))
  }
}

export function normalizeCartSummary(payload: unknown, fallback: Partial<CartSummary> = {}): CartSummary {
  if (!payload || typeof payload !== 'object') {
    return createEmptyCartSummary(fallback)
  }

  const raw = payload as any
  return createEmptyCartSummary({
    elderId: raw.elderId !== undefined ? raw.elderId : fallback.elderId,
    diningPointId: raw.diningPointId !== undefined ? raw.diningPointId : fallback.diningPointId,
    totalCount: raw.totalCount !== undefined ? raw.totalCount : fallback.totalCount,
    dishAmount: raw.dishAmount !== undefined ? raw.dishAmount : fallback.dishAmount,
    deliveryFee: raw.deliveryFee !== undefined ? raw.deliveryFee : fallback.deliveryFee,
    tablewareFee: raw.tablewareFee !== undefined ? raw.tablewareFee : fallback.tablewareFee,
    subsidyAmount: raw.subsidyAmount !== undefined ? raw.subsidyAmount : fallback.subsidyAmount,
    payAmount: raw.payAmount !== undefined ? raw.payAmount : fallback.payAmount,
    effectiveTablewareNumber: raw.effectiveTablewareNumber !== undefined
      ? raw.effectiveTablewareNumber
      : fallback.effectiveTablewareNumber
  })
}

export function normalizeShoppingCartItems(payload: unknown): ShoppingCartItem[] {
  if (!Array.isArray(payload)) {
    return []
  }

  return payload.map((rawItem: any) => ({
    id: Number(rawItem.id || 0),
    elderId: rawItem.elderId !== undefined && rawItem.elderId !== null ? Number(rawItem.elderId) : undefined,
    dishId: rawItem.dishId !== undefined && rawItem.dishId !== null ? Number(rawItem.dishId) : undefined,
    setmealId: rawItem.setmealId !== undefined && rawItem.setmealId !== null ? Number(rawItem.setmealId) : undefined,
    dishFlavor: rawItem.dishFlavor || undefined,
    name: rawItem.name || '',
    image: rawItem.image || '',
    amount: fromCent(toCent(rawItem.amount)),
    number: Math.max(0, Number(rawItem.number || 0)),
    diningPointId: rawItem.diningPointId !== undefined && rawItem.diningPointId !== null
      ? Number(rawItem.diningPointId)
      : undefined,
    createTime: rawItem.createTime || undefined
  }))
}

export function buildDishQuantityMap(items: ShoppingCartItem[]): DishQuantityMap {
  return items.reduce((quantityMap, item) => {
    if (item.dishId === undefined || item.dishId === null) {
      return quantityMap
    }

    const key = Number(item.dishId)
    quantityMap[key] = (quantityMap[key] || 0) + Math.max(0, Number(item.number || 0))
    return quantityMap
  }, {} as DishQuantityMap)
}

export function calculateCartItemSubtotal(item: Pick<ShoppingCartItem, 'amount' | 'number'>) {
  const amountInCent = toCent(item.amount)
  const count = Math.max(0, Number(item.number || 0))
  return fromCent(amountInCent * count)
}

export function calculateOrderDishAmount(items: Array<Pick<ShoppingCartItem, 'amount' | 'number'>>) {
  return fromCent(
    items.reduce((sum, item) => sum + toCent(item.amount) * Math.max(0, Number(item.number || 0)), 0)
  )
}
