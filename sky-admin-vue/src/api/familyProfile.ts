import request from '@/utils/request'

export interface FamilyProfileItem {
  id: number
  userId: number
  username?: string
  name: string
  phone?: string
  remark?: string
  status: number
  elderlyCount?: number
  createTime?: string
  updateTime?: string
}

export interface FamilyProfileFormData {
  id?: number
  userId?: number
  createUser?: boolean
  username?: string
  password?: string
  name: string
  phone?: string
  remark?: string
  status: number
}

export interface FamilyProfilePageParams {
  page: number
  pageSize: number
  name?: string
  phone?: string
  status?: number
}

export interface FamilyProfileOption {
  id: number
  userId: number
  username?: string
  name?: string
  phone?: string
}

export interface FamilyUserOption {
  userId: number
  username?: string
  name?: string
  phone?: string
  status?: number
  bound?: boolean | number
  boundFamilyProfileId?: number
  boundFamilyProfileName?: string
}

export const getFamilyProfilePage = (params: FamilyProfilePageParams) =>
  request({
    url: '/familyProfile/page',
    method: 'get',
    params
  })

export const getFamilyProfileById = (id: number) =>
  request({
    url: `/familyProfile/${id}`,
    method: 'get'
  })

export const addFamilyProfile = (data: FamilyProfileFormData) =>
  request({
    url: '/familyProfile',
    method: 'post',
    data
  })

export const updateFamilyProfile = (data: FamilyProfileFormData) =>
  request({
    url: '/familyProfile',
    method: 'put',
    data
  })

export const updateFamilyProfileStatus = (id: number, status: number) =>
  request({
    url: `/familyProfile/status/${status}`,
    method: 'post',
    params: { id }
  })

export const deleteFamilyProfile = (id: number) =>
  request({
    url: '/familyProfile',
    method: 'delete',
    params: { id }
  })

export const getFamilyProfileOptions = () =>
  request({
    url: '/familyProfile/options',
    method: 'get'
  })

export const getFamilyUsers = () =>
  request({
    url: '/familyProfile/familyUsers',
    method: 'get'
  })
