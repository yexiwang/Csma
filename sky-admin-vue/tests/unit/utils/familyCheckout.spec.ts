import {
  formatDateTime,
  getDefaultEstimatedDeliveryTime,
  hasCheckoutDraftContent,
  isEstimatedDeliveryTimeValid,
  normalizeCheckoutDraft,
  normalizeCheckoutRemark,
  parseEstimatedDeliveryTime
} from '@/utils/familyCheckout'

describe('Utils:familyCheckout', () => {
  it('normalizes draft fields safely', () => {
    expect(normalizeCheckoutDraft(null)).toEqual({
      elderId: undefined,
      remark: '',
      estimatedDeliveryTime: ''
    })

    expect(normalizeCheckoutDraft({
      elderId: '12' as any,
      remark: '少盐',
      estimatedDeliveryTime: '2026-03-18 18:00:00'
    })).toEqual({
      elderId: 12,
      remark: '少盐',
      estimatedDeliveryTime: '2026-03-18 18:00:00'
    })
  })

  it('detects whether a checkout draft has meaningful content', () => {
    expect(hasCheckoutDraftContent({
      elderId: undefined,
      remark: '',
      estimatedDeliveryTime: ''
    })).toBe(false)

    expect(hasCheckoutDraftContent({
      elderId: 1,
      remark: '',
      estimatedDeliveryTime: ''
    })).toBe(true)
  })

  it('formats and validates estimated delivery time', () => {
    const baseDate = new Date(2026, 2, 18, 10, 0, 0)
    const defaultTime = getDefaultEstimatedDeliveryTime(baseDate)

    expect(defaultTime).toBe('2026-03-18 10:30:00')
    expect(formatDateTime(baseDate)).toBe('2026-03-18 10:00:00')
    expect(parseEstimatedDeliveryTime(defaultTime)?.getTime()).toBe(new Date(2026, 2, 18, 10, 30, 0).getTime())
    expect(isEstimatedDeliveryTimeValid(defaultTime, baseDate)).toBe(true)
    expect(isEstimatedDeliveryTimeValid('2026-03-18 09:59:59', baseDate)).toBe(false)
    expect(isEstimatedDeliveryTimeValid('invalid-time', baseDate)).toBe(false)
  })

  it('trims checkout remark before submit', () => {
    expect(normalizeCheckoutRemark('  门口放餐  ')).toBe('门口放餐')
    expect(normalizeCheckoutRemark(undefined)).toBe('')
  })
})
