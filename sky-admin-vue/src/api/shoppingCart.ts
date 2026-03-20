import request from '@/utils/request'

export interface ShoppingCartItem {
  id: number
  elderId?: number
  dishId?: number
  setmealId?: number
  dishFlavor?: string
  name: string
  image: string
  amount: number
  number: number
  diningPointId?: number
  createTime?: string
}

export interface ShoppingCartSummary {
  elderId?: number
  diningPointId?: number
  totalCount: number
  dishAmount: number
  deliveryFee: number
  tablewareFee: number
  subsidyAmount: number
  payAmount: number
  effectiveTablewareNumber: number
}

export interface ShoppingCartPayload {
  elderId: number
  dishId?: number
  setmealId?: number
  dishFlavor?: string
}

export interface ShoppingCartSummaryParams {
  elderId: number
  tablewareStatus?: number
  tablewareNumber?: number
}

export const addShoppingCart = (data: ShoppingCartPayload) =>
  request({
    url: '/user/shoppingCart/add',
    method: 'post',
    data
  })

export const subShoppingCart = (data: ShoppingCartPayload) =>
  request({
    url: '/user/shoppingCart/sub',
    method: 'post',
    data
  })

export const removeShoppingCart = (data: ShoppingCartPayload) =>
  request({
    url: '/user/shoppingCart/remove',
    method: 'post',
    data
  })

export const getShoppingCartList = () =>
  request({
    url: '/user/shoppingCart/list',
    method: 'get'
  })

export const getShoppingCartSummary = (params: ShoppingCartSummaryParams) =>
  request({
    url: '/user/shoppingCart/summary',
    method: 'get',
    params
  })

export const cleanShoppingCart = () =>
  request({
    url: '/user/shoppingCart/clean',
    method: 'delete'
  })
