import { defineStore } from 'pinia'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: []
  }),
  
  getters: {
    totalCount: (state) => {
      return state.items.reduce((sum, item) => sum + item.quantity, 0)
    },
    totalPrice: (state) => {
      return state.items.reduce((sum, item) => sum + item.price * item.quantity, 0)
    }
  },
  
  actions: {
    addToCart(product) {
      const existItem = this.items.find(item => item.id === product.id)
      if (existItem) {
        existItem.quantity += 1
      } else {
        this.items.push({
          ...product,
          quantity: 1
        })
      }
    },
    
    updateQuantity(id, delta) {
      const item = this.items.find(item => item.id === id)
      if (item) {
        item.quantity = Math.max(0, item.quantity + delta)
        if (item.quantity === 0) {
          this.items = this.items.filter(item => item.id !== id)
        }
      }
    },
    
    removeItem(id) {
      this.items = this.items.filter(item => item.id !== id)
    },
    
    clearCart() {
      this.items = []
    }
  }
})

