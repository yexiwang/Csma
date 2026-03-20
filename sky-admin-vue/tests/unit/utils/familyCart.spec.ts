import {
  buildDishQuantityMap,
  calculateCartItemSubtotal,
  calculateOrderDishAmount,
  createEmptyCartSummary,
  formatAmount,
  normalizeCartSummary
} from '@/utils/familyCart'
import { ShoppingCartItem } from '@/api/shoppingCart'

describe('Utils:familyCart', () => {
  it('creates an empty summary with safe defaults', () => {
    expect(createEmptyCartSummary()).toEqual({
      elderId: undefined,
      diningPointId: undefined,
      totalCount: 0,
      dishAmount: 0,
      deliveryFee: 0,
      tablewareFee: 0,
      subsidyAmount: 0,
      payAmount: 0,
      effectiveTablewareNumber: 0
    })
  })

  it('normalizes backend summary payload consistently', () => {
    expect(normalizeCartSummary({
      elderId: '12',
      diningPointId: '7',
      totalCount: 3,
      dishAmount: 25,
      deliveryFee: 0,
      tablewareFee: 2,
      subsidyAmount: 0,
      payAmount: 27,
      effectiveTablewareNumber: 2
    })).toEqual({
      elderId: 12,
      diningPointId: 7,
      totalCount: 3,
      dishAmount: 25,
      deliveryFee: 0,
      tablewareFee: 2,
      subsidyAmount: 0,
      payAmount: 27,
      effectiveTablewareNumber: 2
    })
  })

  it('calculates subtotals, total dish amount and quantity map', () => {
    const items: ShoppingCartItem[] = [
      {
        id: 1,
        dishId: 101,
        name: '清蒸鱼',
        image: '',
        amount: 12.5,
        number: 2
      },
      {
        id: 2,
        dishId: 202,
        name: '米饭',
        image: '',
        amount: 2,
        number: 3
      }
    ]

    expect(calculateCartItemSubtotal(items[0])).toBe(25)
    expect(calculateOrderDishAmount(items)).toBe(31)
    expect(buildDishQuantityMap(items)).toEqual({
      101: 2,
      202: 3
    })
  })

  it('handles decimal formatting consistently', () => {
    expect(formatAmount(29.97)).toBe('29.97')
    expect(formatAmount('29.971')).toBe('29.97')
  })
})
