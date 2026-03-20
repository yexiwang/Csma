import request from '@/utils/request'

export interface AddressBook {
  id: number
  userId?: number
  consignee: string
  phone: string
  sex?: string | number
  provinceCode?: string
  provinceName?: string
  cityCode?: string
  cityName?: string
  districtCode?: string
  districtName?: string
  detail: string
  label?: string
  isDefault?: number
}

export interface AddressBookPayload {
  id?: number
  consignee: string
  phone: string
  sex?: string | number
  provinceCode?: string
  provinceName?: string
  cityCode?: string
  cityName?: string
  districtCode?: string
  districtName?: string
  detail: string
  label?: string
  isDefault?: number
}

export const getAddressBookList = () =>
  request({
    url: '/user/addressBook/list',
    method: 'get'
  })

export const getDefaultAddressBook = () =>
  request({
    url: '/user/addressBook/default',
    method: 'get'
  })

export const getAddressBookById = (id: number) =>
  request({
    url: `/user/addressBook/${id}`,
    method: 'get'
  })

export const createAddressBook = (data: AddressBookPayload) =>
  request({
    url: '/user/addressBook',
    method: 'post',
    data
  })

export const updateAddressBook = (data: AddressBookPayload) =>
  request({
    url: '/user/addressBook',
    method: 'put',
    data
  })

export const deleteAddressBook = (id: number) =>
  request({
    url: '/user/addressBook',
    method: 'delete',
    params: { id }
  })

export const setDefaultAddressBook = (id: number) =>
  request({
    url: '/user/addressBook/default',
    method: 'put',
    data: { id }
  })
