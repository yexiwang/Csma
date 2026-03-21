import request from '@/utils/request'

export interface OrderDetail {
  id?: number
  dishId?: number
  setmealId?: number
  dishFlavor?: string
  dishName?: string
  dishImage?: string
  name?: string
  image?: string
  number: number
  amount: number
}

export interface AddressBook {
  id: number
  consignee: string
  phone: string
  sex?: number | string
  provinceName?: string
  cityName?: string
  districtName?: string
  detail: string
  label?: string
  isDefault?: number
}

export interface Order {
  id: number
  orderNumber?: string
  number?: string
  orderDishes?: string
  status: number
  amount: number
  orderTime: string
  payStatus?: number
  estimatedDeliveryTime?: string
  expectedTime?: string
  checkoutTime?: string
  deliveryStatus?: number
  deliveryTime?: string
  cancelTime?: string
  remark?: string
  tablewareStatus?: number
  tablewareNumber?: number
  packAmount?: number
  deliveryFee?: number
  tablewareFee?: number
  personalPay?: number
  subsidyAmount?: number
  volunteerName?: string
  diningPointName?: string
  elderName?: string
  address?: string
  consignee?: string
  phone?: string
  reviewed?: boolean
  orderDetails?: OrderDetail[]
  orderDetailList?: OrderDetail[]
}

export interface OrderReview {
  orderId: number
  score: number
  content?: string
  createTime?: string
}

export interface OrderReviewSubmitParams {
  orderId: number
  score: number
  content?: string
}

export interface OrderSubmitParams {
  elderId: number
  addressBookId: number
  payMethod: number
  remark?: string
  estimatedDeliveryTime?: string
  deliveryStatus?: number
  tablewareStatus?: number
  tablewareNumber?: number
  packAmount?: number
  dishAmount?: number
  deliveryFee?: number
  tablewareFee?: number
  subsidyAmount?: number
  payAmount?: number
  amount?: number
}

export interface OrderSubmitResponse {
  id: number
  orderNumber: string
  orderAmount: number
  orderTime: string
}

export interface OrderPaymentParams {
  orderNumber: string
  payMethod: number
}

const ensureBusinessSuccess = (response: any, fallbackMessage: string) => {
  const code = response && response.data && response.data.code
  if (code === 1) {
    return response
  }

  const error: any = new Error((response && response.data && response.data.msg) || fallbackMessage)
  error.response = response
  throw error
}

const unwrapBusinessData = <T>(response: any, fallbackMessage: string): T => {
  const safeResponse = ensureBusinessSuccess(response, fallbackMessage)
  return safeResponse && safeResponse.data ? safeResponse.data.data : undefined
}

export const submitOrder = (data: OrderSubmitParams) =>
  request({
    url: '/user/order/submit',
    method: 'post',
    data
  }).then((response) => ensureBusinessSuccess(response, '下单失败，请稍后重试'))

export const paymentOrder = (data: OrderPaymentParams) =>
  request({
    url: '/user/order/payment',
    method: 'put',
    data
  }).then((response) => ensureBusinessSuccess(response, '支付失败，请重试'))

export const getHistoryOrders = (params: { page: number; pageSize: number; status?: number }) =>
  request({
    url: '/user/order/historyOrders',
    method: 'get',
    params
  })

export const getOrderDetail = (id: number) =>
  request({
    url: `/user/order/orderDetail/${id}`,
    method: 'get'
  })

export const cancelOrder = (id: number) =>
  request({
    url: `/user/order/cancel/${id}`,
    method: 'put'
  })

export const repetitionOrder = (id: number) =>
  request({
    url: `/user/order/repetition/${id}`,
    method: 'post'
  })

export const reminderOrder = (id: number) =>
  request({
    url: `/user/order/reminder/${id}`,
    method: 'get'
  })

export const submitOrderReview = (data: OrderReviewSubmitParams) =>
  request({
    url: '/user/order/review',
    method: 'post',
    data
  }).then((response) => ensureBusinessSuccess(response, '提交评价失败，请稍后重试'))

export const getOrderReview = (orderId: number): Promise<OrderReview | null> =>
  request({
    url: `/user/order/review/${orderId}`,
    method: 'get'
  }).then((response) => unwrapBusinessData<OrderReview | null>(response, '获取评价失败，请稍后重试'))
