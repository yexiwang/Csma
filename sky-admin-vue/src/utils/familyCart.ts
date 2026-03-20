import { ShoppingCartItem } from '@/api/shoppingCart'

export interface CartSummary {
  totalCount: number
  dishAmount: number
  deliveryFee: number
  subsidyAmount: number
  payAmount: number
}

export type DishQuantityMap = Record<number, number>

const DEFAULT_CART_SUMMARY: CartSummary = {
  totalCount: 0,
  dishAmount: 0,
  deliveryFee: 0,
  subsidyAmount: 0,
  payAmount: 0
}

function toSafeNumber(value: number | string | undefined | null) {
  const nextValue = Number(value)
  return Number.isFinite(nextValue) ? nextValue : 0
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

export function calculateCartSummary(
  items: ShoppingCartItem[],
  options?: Partial<Pick<CartSummary, 'deliveryFee' | 'subsidyAmount'>>
): CartSummary {
  if (!Array.isArray(items) || items.length === 0) {
    const deliveryFee = fromCent(toCent(options && options.deliveryFee))
    const subsidyAmount = fromCent(toCent(options && options.subsidyAmount))
    return {
      ...DEFAULT_CART_SUMMARY,
      deliveryFee,
      subsidyAmount,
      payAmount: fromCent(toCent(deliveryFee) - toCent(subsidyAmount))
    }
  }

  const totalCount = items.reduce((sum, item) => sum + Math.max(0, Number(item.number || 0)), 0)
  const dishAmountInCent = items.reduce((sum, item) => sum + toCent(item.amount) * Math.max(0, Number(item.number || 0)), 0)
  const deliveryFee = fromCent(toCent(options && options.deliveryFee))
  const subsidyAmount = fromCent(toCent(options && options.subsidyAmount))
  const payAmountInCent = dishAmountInCent + toCent(deliveryFee) - toCent(subsidyAmount)

  return {
    totalCount,
    dishAmount: fromCent(dishAmountInCent),
    deliveryFee,
    subsidyAmount,
    payAmount: fromCent(payAmountInCent)
  }
}
