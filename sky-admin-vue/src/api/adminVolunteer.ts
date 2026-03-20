import request from '@/utils/request'

export interface VolunteerItem {
  id: number
  username: string
  name: string
  phone: string
  status: number
  createTime?: string
}

export interface VolunteerPageQuery {
  page: number
  pageSize: number
  name?: string
  phone?: string
  username?: string
  status?: number
}

export interface VolunteerFormPayload {
  id?: number
  username?: string
  password?: string
  name: string
  phone: string
  status: number
}

export interface VolunteerStatusPayload {
  id: number
  status: number
}

export interface AvailableVolunteerOption {
  id: number
  name: string
  phone?: string
}

export const getVolunteerPage = (params: VolunteerPageQuery) =>
  request({
    url: '/volunteer/page',
    method: 'get',
    params
  })

export const getVolunteerById = (id: number) =>
  request({
    url: `/volunteer/${id}`,
    method: 'get'
  })

export const addVolunteer = (data: VolunteerFormPayload) =>
  request({
    url: '/volunteer',
    method: 'post',
    data
  })

export const updateVolunteer = (data: VolunteerFormPayload) =>
  request({
    url: '/volunteer',
    method: 'put',
    data
  })

export const updateVolunteerStatus = ({ id, status }: VolunteerStatusPayload) =>
  request({
    url: `/volunteer/status/${status}`,
    method: 'post',
    params: { id }
  })

export const getAvailableVolunteerList = () =>
  request({
    url: '/volunteer/list',
    method: 'get'
  })
