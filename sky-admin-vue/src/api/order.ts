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
  personalPay?: number
  subsidyAmount?: number
  volunteerName?: string
  diningPointName?: string
  elderName?: string
  address?: string
  consignee?: string
  phone?: string
  orderDetails?: OrderDetail[]
  orderDetailList?: OrderDetail[]
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
  amount?: number
}

export interface OrderSubmitResponse {
  id: number
  orderNumber?: string
  orderAmount?: number
  orderTime?: string
}

export interface OrderPaymentParams {
  orderNumber: string
  payMethod: number
}

export const submitOrder = (data: OrderSubmitParams) =>
  request({
    url: '/user/order/submit',
    method: 'post',
    data
  })

export const paymentOrder = (data: OrderPaymentParams) =>
  request({
    url: '/user/order/payment',
    method: 'put',
    data
  })

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
