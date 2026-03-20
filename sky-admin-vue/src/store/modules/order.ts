import { defineStore } from 'pinia'

interface OrderDish {
  id: number
  name: string
  price: number
  image: string
  quantity: number
}

export const useOrderStore = defineStore('order', {
  state: () => ({
    dishes: [] as OrderDish[],
    addressBookId: null as number | null,
    remark: '',
    arrivalTime: '',
    deliveryStatus: 1,
    tablewareStatus: 0,
    tablewareNumber: 0,
    payMethod: 1
  }),
  actions: {
    setDishes(state, newDishes: OrderDish[]) {
      state.dishes = newDishes
    },
    setAddressBookId(state, id: number) {
      state.addressBookId = id
    },
    setRemark(state, newRemark: string) {
      state.remark = newRemark
    },
    setArrivalTime(state, time: string) {
      state.arrivalTime = time
    },
    setDeliveryStatus(state, status: number) {
      state.deliveryStatus = status
    },
    setTablewareStatus(state, status: number) {
      state.tablewareStatus = status
    },
    setTablewareNumber(state, num: number) {
      state.tablewareNumber = num
    },
    setPayMethod(state, method: number) {
      state.payMethod = method
    },
    clearOrder(state) {
      state.dishes = []
      state.addressBookId = null
      state.remark = ''
      state.arrivalTime = ''
      state.deliveryStatus = 1
      state.tablewareStatus = 0
      state.tablewareNumber = 0
      state.payMethod = 1
    }
  }
})
