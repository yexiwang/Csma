export interface CheckoutDraft {
  elderId?: number
  diningPointId?: number
  remark: string
  estimatedDeliveryTime: string
}

export const FAMILY_CHECKOUT_DRAFT_STORAGE_KEY = 'familyCheckoutDraft'
export const DEFAULT_DELIVERY_LEAD_MINUTES = 30

function toPositiveNumber(value: unknown) {
  const nextValue = Number(value)
  return Number.isFinite(nextValue) && nextValue > 0 ? nextValue : undefined
}

export function normalizeCheckoutDraft(draft?: Partial<CheckoutDraft> | null): CheckoutDraft {
  return {
    elderId: toPositiveNumber(draft && draft.elderId),
    diningPointId: toPositiveNumber(draft && draft.diningPointId),
    remark: typeof (draft && draft.remark) === 'string' ? (draft as CheckoutDraft).remark : '',
    estimatedDeliveryTime: typeof (draft && draft.estimatedDeliveryTime) === 'string'
      ? (draft as CheckoutDraft).estimatedDeliveryTime
      : ''
  }
}

export function hasCheckoutDraftContent(draft: CheckoutDraft) {
  return Boolean(draft.elderId || draft.diningPointId || draft.remark || draft.estimatedDeliveryTime)
}

export function normalizeCheckoutRemark(value?: string | null) {
  return typeof value === 'string' ? value.trim() : ''
}

export function formatDateTime(date: Date) {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hour = String(date.getHours()).padStart(2, '0')
  const minute = String(date.getMinutes()).padStart(2, '0')
  const second = String(date.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hour}:${minute}:${second}`
}

export function getDefaultEstimatedDeliveryTime(baseDate = new Date(), leadMinutes = DEFAULT_DELIVERY_LEAD_MINUTES) {
  return formatDateTime(new Date(baseDate.getTime() + leadMinutes * 60 * 1000))
}

export function parseEstimatedDeliveryTime(value?: string | null) {
  if (!value) {
    return null
  }

  const matched = value
    .trim()
    .match(/^(\d{4})-(\d{2})-(\d{2})[ T](\d{2}):(\d{2})(?::(\d{2}))?$/)

  if (!matched) {
    return null
  }

  const [, year, month, day, hour, minute, second = '00'] = matched
  const date = new Date(
    Number(year),
    Number(month) - 1,
    Number(day),
    Number(hour),
    Number(minute),
    Number(second)
  )

  return Number.isNaN(date.getTime()) ? null : date
}

export function isEstimatedDeliveryTimeValid(value?: string | null, now = new Date()) {
  const parsedDate = parseEstimatedDeliveryTime(value)
  return Boolean(parsedDate && parsedDate.getTime() >= now.getTime())
}
