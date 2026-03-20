import request from '@/utils/request'

export interface AdminOrderQueryParams {
  page: number
  pageSize: number
  status?: number
  number?: string
  phone?: string
  beginTime?: string
  endTime?: string
}

export interface AdminOrderActionPayload {
  id: number
  cancelReason?: string
  rejectionReason?: string
}

export interface OrderDispatchPayload {
  id: number
  volunteerId: number
}

export interface VolunteerOption {
  id: number
  name: string
  username?: string
  phone?: string
  role?: string
  status?: number
}

export const getAdminOrderPage = (params: AdminOrderQueryParams) =>
  request({
    url: '/order/conditionSearch',
    method: 'get',
    params
  })

export const getAdminOrderStatistics = () =>
  request({
    url: '/order/statistics',
    method: 'get'
  })

export const getAdminOrderDetail = (id: number) =>
  request({
    url: `/order/details/${id}`,
    method: 'get'
  })

export const dispatchOrder = (data: OrderDispatchPayload) =>
  request({
    url: '/order/confirm',
    method: 'put',
    data
  })

export const getDispatchVolunteerOptions = () =>
  request({
    url: '/volunteer/list',
    method: 'get'
  })

export const rejectAdminOrder = (data: AdminOrderActionPayload) =>
  request({
    url: '/order/rejection',
    method: 'put',
    data
  })

export const cancelAdminOrder = (data: AdminOrderActionPayload) =>
  request({
    url: '/order/cancel',
    method: 'put',
    data
  })

export const markMealReady = (id: number) =>
  request({
    url: `/order/mealReady/${id}`,
    method: 'put'
  })
