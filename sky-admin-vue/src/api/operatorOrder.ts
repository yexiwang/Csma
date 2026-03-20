import request from '@/utils/request'
import { OrderDetail } from '@/api/order'

export interface ApiResult<T> {
  code: number
  msg?: string
  data: T
}

export interface OperatorOrderQueryParams {
  page: number
  pageSize: number
  view: string
  number?: string
  phone?: string
  beginTime?: string
  endTime?: string
}

export interface OperatorOrderOverview {
  pendingPrepare: number
  preparing: number
  pendingAssignment: number
  mealReady: number
  delivering: number
  completed: number
}

export interface OperatorVolunteerOption {
  id: number
  name: string
  phone?: string
  status?: number
  totalOrders?: number
  currentTaskCount?: number
}

export interface OperatorOrderItem {
  id: number
  number: string
  status: number
  amount: number
  orderTime?: string
  expectedTime?: string
  estimatedDeliveryTime?: string
  deliveryTime?: string
  address?: string
  remark?: string
  volunteerId?: number
  volunteerName?: string
  volunteerPhone?: string
  volunteerStatus?: number
  diningPointName?: string
  elderName?: string
  elderPhone?: string
  elderAddress?: string
  elderSpecialNeeds?: string
  handoverStatus?: string
  orderDetailList?: OrderDetail[]
}

export interface OperatorOrderPage {
  total: number
  records: OperatorOrderItem[]
}

export interface OperatorAssignVolunteerPayload {
  orderId: number
  volunteerId: number
}

function unwrapResult<T>(response: { data: ApiResult<T> }) {
  const result = response.data
  if (!result || result.code !== 1) {
    throw new Error((result && result.msg) || 'Request failed')
  }
  return result.data
}

export const getOperatorOrderBoard = async (params: OperatorOrderQueryParams): Promise<OperatorOrderPage> => {
  const response = await request({
    url: '/order/operator/board',
    method: 'get',
    params
  })
  return unwrapResult<OperatorOrderPage>(response)
}

export const getOperatorOrderOverview = async (): Promise<OperatorOrderOverview> => {
  const response = await request({
    url: '/order/operator/overview',
    method: 'get'
  })
  return unwrapResult<OperatorOrderOverview>(response)
}

export const getOperatorOrderDetail = async (id: number): Promise<OperatorOrderItem> => {
  const response = await request({
    url: `/order/operator/details/${id}`,
    method: 'get'
  })
  return unwrapResult<OperatorOrderItem>(response)
}

export const getOperatorVolunteerOptions = async (): Promise<OperatorVolunteerOption[]> => {
  const response = await request({
    url: '/order/operator/volunteers',
    method: 'get'
  })
  return unwrapResult<OperatorVolunteerOption[]>(response)
}

export const startOperatorPreparing = async (id: number): Promise<void> => {
  const response = await request({
    url: `/order/operator/startPreparing/${id}`,
    method: 'put'
  })
  unwrapResult<void>(response)
}

export const assignOperatorVolunteer = async (data: OperatorAssignVolunteerPayload): Promise<void> => {
  const response = await request({
    url: '/order/operator/assignVolunteer',
    method: 'put',
    data
  })
  unwrapResult<void>(response)
}

export const markOperatorMealReady = async (id: number): Promise<void> => {
  const response = await request({
    url: `/order/operator/mealReady/${id}`,
    method: 'put'
  })
  unwrapResult<void>(response)
}

export const confirmOperatorPickup = async (id: number): Promise<void> => {
  const response = await request({
    url: `/order/operator/pickup/${id}`,
    method: 'put'
  })
  unwrapResult<void>(response)
}
