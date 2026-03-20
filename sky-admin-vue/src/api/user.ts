import request from '@/utils/request'

export interface VolunteerOption {
  id: number
  name: string
  username?: string
  phone?: string
  role?: string
  status?: number
}

export const editPassword = (data: any) =>
  request({
    url: '/employee/editPassword',
    method: 'put',
    data
  })

export const getStatus = () =>
  request({
    url: '/shop/status',
    method: 'get'
  })

export const setStatus = (status: number) =>
  request({
    url: `/shop/${status}`,
    method: 'put',
    data: status
  })

export const getVolunteerOptions = () =>
  request({
    url: '/volunteer/list',
    method: 'get'
  })
