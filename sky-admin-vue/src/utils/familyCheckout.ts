export interface CheckoutDraft {
  elderId?: number
  diningPointId?: number
  selectedAddressId?: number
  remark: string
  estimatedDeliveryTime: string
  tablewareStatus: number
  tablewareNumber: number
}

export type TablewareMode = 'none' | 'auto' | 'custom'

export const FAMILY_CHECKOUT_DRAFT_STORAGE_KEY = 'familyCheckoutDraft'
export const DEFAULT_DELIVERY_LEAD_MINUTES = 30
export const TABLEWARE_STATUS_AUTO = 1
export const TABLEWARE_STATUS_MANUAL = 0

function toPositiveNumber(value: unknown) {
  const nextValue = Number(value)
  return Number.isFinite(nextValue) && nextValue > 0 ? nextValue : undefined
}

function toNonNegativeNumber(value: unknown) {
  const nextValue = Number(value)
  return Number.isFinite(nextValue) && nextValue >= 0 ? Math.floor(nextValue) : 0
}

export function resolveTablewareMode(tablewareStatus?: number | null, tablewareNumber?: number | null): TablewareMode {
  if (Number(tablewareStatus) === TABLEWARE_STATUS_AUTO) {
    return 'auto'
  }
  if (toNonNegativeNumber(tablewareNumber) > 0) {
    return 'custom'
  }
  return 'none'
}

export function buildTablewarePayload(mode: TablewareMode, customTablewareNumber?: number | null) {
  switch (mode) {
    case 'auto':
      return {
        tablewareStatus: TABLEWARE_STATUS_AUTO,
        tablewareNumber: 0
      }
    case 'custom':
      return {
        tablewareStatus: TABLEWARE_STATUS_MANUAL,
        tablewareNumber: Math.max(1, toNonNegativeNumber(customTablewareNumber))
      }
    default:
      return {
        tablewareStatus: TABLEWARE_STATUS_MANUAL,
        tablewareNumber: 0
      }
  }
}

export function normalizeCheckoutDraft(draft?: Partial<CheckoutDraft> | null): CheckoutDraft {
  const tablewareStatus = Number(draft && draft.tablewareStatus) === TABLEWARE_STATUS_AUTO
    ? TABLEWARE_STATUS_AUTO
    : TABLEWARE_STATUS_MANUAL

  return {
    elderId: toPositiveNumber(draft && draft.elderId),
    diningPointId: toPositiveNumber(draft && draft.diningPointId),
    selectedAddressId: toPositiveNumber(draft && draft.selectedAddressId),
    remark: typeof (draft && draft.remark) === 'string' ? (draft as CheckoutDraft).remark : '',
    estimatedDeliveryTime: typeof (draft && draft.estimatedDeliveryTime) === 'string'
      ? (draft as CheckoutDraft).estimatedDeliveryTime
      : '',
    tablewareStatus,
    tablewareNumber: toNonNegativeNumber(draft && draft.tablewareNumber)
  }
}

export function hasCheckoutDraftContent(draft: CheckoutDraft) {
  return Boolean(
    draft.elderId ||
    draft.diningPointId ||
    draft.selectedAddressId ||
    draft.remark ||
    draft.estimatedDeliveryTime ||
    draft.tablewareStatus === TABLEWARE_STATUS_AUTO ||
    draft.tablewareNumber > 0
  )
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
