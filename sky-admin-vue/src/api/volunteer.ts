import request from '@/utils/request'

export interface ApiResult<T> {
  code: number
  msg?: string
  data: T
}

export interface VolunteerTaskItem {
  id: number
  number: string
  status: number
  elderName?: string
  diningPointName?: string
  consignee?: string
  phone?: string
  address?: string
  orderTime?: string
  remark?: string
}

export interface VolunteerTaskPage {
  total: number
  records: VolunteerTaskItem[]
}

export interface VolunteerTaskQuery {
  page: number
  pageSize: number
  status?: number
}

export interface VolunteerOverview {
  name?: string
  status?: number
  totalOrders?: number
  totalHours?: number | string
  rating?: number | string
  level?: number
}

function unwrapResult<T>(response: { data: ApiResult<T> }) {
  const result = response.data
  if (!result || result.code !== 1) {
    throw new Error((result && result.msg) || 'Request failed')
  }
  return result.data
}

export const getVolunteerOrderPage = async (params: VolunteerTaskQuery): Promise<VolunteerTaskPage> => {
  const response = await request({
    url: '/user/volunteer/orders',
    method: 'get',
    params
  })
  return unwrapResult<VolunteerTaskPage>(response)
}

export const getVolunteerOverview = async (): Promise<VolunteerOverview> => {
  const response = await request({
    url: '/user/volunteer/overview',
    method: 'get'
  })
  return unwrapResult<VolunteerOverview>(response)
}

export const exportVolunteerOverview = () =>
  request({
    url: '/user/volunteer/overview/export',
    method: 'get',
    responseType: 'blob'
  })

export const volunteerConfirmPickup = async (id: number): Promise<void> => {
  const response = await request({
    url: `/user/volunteer/pickup/${id}`,
    method: 'put'
  })
  unwrapResult<void>(response)
}

export const volunteerConfirmComplete = async (id: number): Promise<void> => {
  const response = await request({
    url: `/user/volunteer/complete/${id}`,
    method: 'put'
  })
  unwrapResult<void>(response)
}
