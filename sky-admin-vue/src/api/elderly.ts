import request from '@/utils/request'

export interface ElderlyPageParams {
  page: number
  pageSize: number
  name?: string
}

export interface ElderlyFormData {
  id?: number
  userId?: number
  familyProfileId?: number
  familyName?: string
  familyPhone?: string
  familyUsername?: string
  name?: string
  gender?: string
  age?: number
  phone?: string
  address?: string
  diningPointId?: number
  diningPointName?: string
  gridCode?: string
  healthInfo?: string
  specialNeeds?: string
  idCard?: string
  image?: string
}

export const getElderlyPage = (params: ElderlyPageParams) => {
  return request({
    url: '/elderly/page',
    method: 'get',
    params
  })
}

export const addElderly = (data: ElderlyFormData) => {
  return request({
    url: '/elderly',
    method: 'post',
    data
  })
}

export const editElderly = (data: ElderlyFormData) => {
  return request({
    url: '/elderly',
    method: 'put',
    data
  })
}

export const deleteElderly = (id: number) => {
  return request({
    url: `/elderly/${id}`,
    method: 'delete'
  })
}

export const queryElderlyById = (id: number) => {
  return request({
    url: `/elderly/${id}`,
    method: 'get'
  })
}
