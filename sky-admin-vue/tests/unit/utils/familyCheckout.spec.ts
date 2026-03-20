import {
  buildTablewarePayload,
  formatDateTime,
  getDefaultEstimatedDeliveryTime,
  hasCheckoutDraftContent,
  isEstimatedDeliveryTimeValid,
  normalizeCheckoutDraft,
  normalizeCheckoutRemark,
  parseEstimatedDeliveryTime,
  resolveTablewareMode,
  TABLEWARE_STATUS_AUTO,
  TABLEWARE_STATUS_MANUAL
} from '@/utils/familyCheckout'

describe('Utils:familyCheckout', () => {
  it('normalizes draft fields safely', () => {
    expect(normalizeCheckoutDraft(null)).toEqual({
      elderId: undefined,
      diningPointId: undefined,
      selectedAddressId: undefined,
      remark: '',
      estimatedDeliveryTime: '',
      tablewareStatus: TABLEWARE_STATUS_MANUAL,
      tablewareNumber: 0
    })

    expect(normalizeCheckoutDraft({
      elderId: '12' as any,
      diningPointId: '8' as any,
      selectedAddressId: '3' as any,
      remark: '少盐',
      estimatedDeliveryTime: '2026-03-18 18:00:00',
      tablewareStatus: 1,
      tablewareNumber: '2' as any
    })).toEqual({
      elderId: 12,
      diningPointId: 8,
      selectedAddressId: 3,
      remark: '少盐',
      estimatedDeliveryTime: '2026-03-18 18:00:00',
      tablewareStatus: TABLEWARE_STATUS_AUTO,
      tablewareNumber: 2
    })
  })

  it('detects whether a checkout draft has meaningful content', () => {
    expect(hasCheckoutDraftContent({
      elderId: undefined,
      diningPointId: undefined,
      selectedAddressId: undefined,
      remark: '',
      estimatedDeliveryTime: '',
      tablewareStatus: TABLEWARE_STATUS_MANUAL,
      tablewareNumber: 0
    })).toBe(false)

    expect(hasCheckoutDraftContent({
      elderId: 1,
      diningPointId: 2,
      selectedAddressId: 3,
      remark: '',
      estimatedDeliveryTime: '',
      tablewareStatus: TABLEWARE_STATUS_MANUAL,
      tablewareNumber: 0
    })).toBe(true)
  })

  it('resolves tableware mode and payload mapping', () => {
    expect(resolveTablewareMode(TABLEWARE_STATUS_MANUAL, 0)).toBe('none')
    expect(resolveTablewareMode(TABLEWARE_STATUS_AUTO, 0)).toBe('auto')
    expect(resolveTablewareMode(TABLEWARE_STATUS_MANUAL, 4)).toBe('custom')

    expect(buildTablewarePayload('none')).toEqual({
      tablewareStatus: TABLEWARE_STATUS_MANUAL,
      tablewareNumber: 0
    })
    expect(buildTablewarePayload('auto')).toEqual({
      tablewareStatus: TABLEWARE_STATUS_AUTO,
      tablewareNumber: 0
    })
    expect(buildTablewarePayload('custom', 5)).toEqual({
      tablewareStatus: TABLEWARE_STATUS_MANUAL,
      tablewareNumber: 5
    })
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
