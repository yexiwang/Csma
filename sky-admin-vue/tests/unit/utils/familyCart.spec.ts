import {
  buildDishQuantityMap,
  calculateCartItemSubtotal,
  calculateCartSummary,
  formatAmount
} from '@/utils/familyCart'
import { ShoppingCartItem } from '@/api/shoppingCart'

describe('Utils:familyCart', () => {
  it('returns empty summary with default fees', () => {
    expect(calculateCartSummary([])).toEqual({
      totalCount: 0,
      dishAmount: 0,
      deliveryFee: 0,
      subsidyAmount: 0,
      payAmount: 0
    })
  })

  it('calculates single item subtotal and summary', () => {
    const items: ShoppingCartItem[] = [
      {
        id: 1,
        dishId: 101,
        name: '清蒸鱼',
        image: '',
        amount: 12.5,
        number: 2
      }
    ]

    expect(calculateCartItemSubtotal(items[0])).toBe(25)
    expect(calculateCartSummary(items)).toEqual({
      totalCount: 2,
      dishAmount: 25,
      deliveryFee: 0,
      subsidyAmount: 0,
      payAmount: 25
    })
  })

  it('aggregates multiple items and dish quantities', () => {
    const items: ShoppingCartItem[] = [
      {
        id: 1,
        dishId: 101,
        name: '红烧肉',
        image: '',
        amount: 18.8,
        number: 1
      },
      {
        id: 2,
        dishId: 101,
        dishFlavor: '少盐',
        name: '红烧肉',
        image: '',
        amount: 18.8,
        number: 2
      },
      {
        id: 3,
        dishId: 202,
        name: '米饭',
        image: '',
        amount: 2,
        number: 3
      }
    ]

    expect(buildDishQuantityMap(items)).toEqual({
      101: 3,
      202: 3
    })
    expect(calculateCartSummary(items, { deliveryFee: 3, subsidyAmount: 2.5 })).toEqual({
      totalCount: 6,
      dishAmount: 62.4,
      deliveryFee: 3,
      subsidyAmount: 2.5,
      payAmount: 62.9
    })
  })

  it('handles decimal formatting consistently', () => {
    const items: ShoppingCartItem[] = [
      {
        id: 1,
        dishId: 99,
        name: '豆腐',
        image: '',
        amount: 9.99,
        number: 3
      }
    ]

    const summary = calculateCartSummary(items)
    expect(summary.dishAmount).toBe(29.97)
    expect(formatAmount(summary.payAmount)).toBe('29.97')
  })
})
