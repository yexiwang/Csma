type OrderTagType = '' | 'success' | 'info' | 'warning' | 'danger'

export const ORDER_STATUS = {
  PENDING_PAYMENT: 1,
  TO_BE_SCHEDULED: 2,
  PREPARING: 3,
  MEAL_READY: 4,
  DELIVERY_IN_PROGRESS: 5,
  COMPLETED: 6,
  CANCELLED: 7
} as const

export const ORDER_STATUS_TEXT: Record<number, string> = {
  [ORDER_STATUS.PENDING_PAYMENT]: '待支付',
  [ORDER_STATUS.TO_BE_SCHEDULED]: '待制作',
  [ORDER_STATUS.PREPARING]: '制作中',
  [ORDER_STATUS.MEAL_READY]: '待取餐',
  [ORDER_STATUS.DELIVERY_IN_PROGRESS]: '配送中',
  [ORDER_STATUS.COMPLETED]: '已完成',
  [ORDER_STATUS.CANCELLED]: '已取消'
}

export const ORDER_STATUS_TAG: Record<number, OrderTagType> = {
  [ORDER_STATUS.PENDING_PAYMENT]: 'info',
  [ORDER_STATUS.TO_BE_SCHEDULED]: 'warning',
  [ORDER_STATUS.PREPARING]: 'warning',
  [ORDER_STATUS.MEAL_READY]: 'warning',
  [ORDER_STATUS.DELIVERY_IN_PROGRESS]: 'warning',
  [ORDER_STATUS.COMPLETED]: 'success',
  [ORDER_STATUS.CANCELLED]: 'info'
}

export const getOrderStatusText = (status: number) => ORDER_STATUS_TEXT[status] || '未知状态'

export const getOrderStatusTag = (status: number) => ORDER_STATUS_TAG[status] || 'info'
