import request from '@/utils/request'

export interface Category {
  id: number
  name: string
  type?: number
}

export interface Dish {
  id: number
  name: string
  price: number
  image: string
  description?: string
  categoryId?: number
  categoryName?: string
  diningPointId?: number
}

export interface SetmealDish {
  id: number
  setmealId: number
  dishId: number
  name: string
  price: number
  copies: number
}

export interface SetmealDishItem {
  name: string
  copies: number
  image: string
  description: string
}

export interface Setmeal {
  id: number
  categoryId: number
  diningPointId: number
  name: string
  price: number
  status: number
  description?: string
  image: string
  categoryName?: string
  diningPointName?: string
  setmealDishes?: SetmealDish[]
}

export interface Elderly {
  id: number
  name: string
  address?: string
  phone?: string
  diningPointId?: number
  diningPointName?: string
  diningPointStatus?: number
}

export const getFamilyCategoryList = () =>
  request({
    url: '/user/category/list',
    method: 'get'
  })

export const getFamilyDishList = (params: { diningPointId: number; categoryId?: number }) =>
  request({
    url: '/user/dish/list',
    method: 'get',
    params
  })

export const getMyElderlyList = () =>
  request({
    url: '/user/elderly/list',
    method: 'get'
  })

export const getFamilySetmealList = (params: { categoryId?: number; diningPointId: number }) =>
  request({
    url: '/user/setmeal/list',
    method: 'get',
    params
  })

export const getSetmealDetail = (id: number) =>
  request({
    url: `/user/setmeal/dish/${id}`,
    method: 'get'
  })
