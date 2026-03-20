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

export interface ShoppingCartPayload {
  elderId: number
  dishId?: number
  setmealId?: number
  dishFlavor?: string
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

export const getShoppingCartList = () =>
  request({
    url: '/user/shoppingCart/list',
    method: 'get'
  })

export const cleanShoppingCart = () =>
  request({
    url: '/user/shoppingCart/clean',
    method: 'delete'
  })
