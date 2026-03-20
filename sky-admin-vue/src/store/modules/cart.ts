import { defineStore } from 'pinia'

interface CartItem {
  id: number
  name: string
  price: number
  image: string
  quantity: number
}

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [] as CartItem[]
  }),
  getters: {
    totalQuantity: (state) => {
      return state.items.reduce((sum: number, item: CartItem) => sum + item.quantity, 0)
    },
    totalPrice: (state) => {
      return state.items.reduce((sum: number, item: CartItem) => sum + (item.price * item.quantity), 0)
    }
  },
  actions: {
    addItem(state, item: CartItem) {
      const existingItem = state.items.find((i: CartItem) => i.id === item.id)
      if (existingItem) {
        existingItem.quantity += item.quantity
      } else {
        state.items.push({ ...item })
      }
      this.saveToLocalStorage()
    },
    removeItem(state, id: number) {
      const index = state.items.findIndex((i: CartItem) => i.id === id)
      if (index > -1) {
        state.items.splice(index, 1)
        this.saveToLocalStorage()
      }
    },
    updateQuantity(state, payload: { id: number; quantity: number }) {
      const item = state.items.find((i: CartItem) => i.id === payload.id)
      if (item) {
        if (payload.quantity <= 0) {
          this.removeItem(state, payload.id)
        } else {
          item.quantity = payload.quantity
          this.saveToLocalStorage()
        }
      }
    },
    clearCart(state) {
      state.items = []
      this.saveToLocalStorage()
    },
    getQuantity(state, id: number): number {
      const item = state.items.find((i: CartItem) => i.id === id)
      return item ? item.quantity : 0
    },
    saveToLocalStorage(state) {
      localStorage.setItem('cart', JSON.stringify(state.items))
    },
    loadFromLocalStorage(state) {
      const saved = localStorage.getItem('cart')
      if (saved) {
        try {
          state.items = JSON.parse(saved)
        } catch (e) {
          console.error('加载购物车失败:', e)
        }
      }
    }
  }
})
