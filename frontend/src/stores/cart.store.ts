import { defineStore } from 'pinia'
import type { Cart, CartItem } from '@/types'
import api from '@/services/api'

export const useCartStore = defineStore('cart', {
  state: () => ({
    cart: null as Cart | null,
    loading: false
  }),

  getters: {
    totalItems: (state) => state.cart?.totalItems || 0,
    totalPrice: (state) => state.cart?.totalPrice || 0,
    items: (state) => state.cart?.items || []
  },

  actions: {
    async fetchCart() {
      this.loading = true
      try {
        const cart = await api.getCart()
        this.cart = cart
      } finally {
        this.loading = false
      }
    },

    async addItem(variantId: number, quantity: number = 1) {
      const item = await api.addCartItem({ variantId, quantity })
      this.cart = item // API returns updated cart
    },

    async updateItem(itemId: number, quantity: number) {
      const cart = await api.updateCartItem(itemId, quantity)
      this.cart = cart
    },

    async removeItem(itemId: number) {
      await api.removeCartItem(itemId)
      if (this.cart) {
        this.cart.items = this.cart.items.filter(item => item.id !== itemId)
        this.cart.totalItems = this.cart.items.reduce((sum, item) => sum + item.quantity, 0)
        this.cart.totalPrice = this.cart.items.reduce((sum, item) => sum + item.subtotal, 0)
      }
    },

    clearCart() {
      this.cart = null
    }
  }
})
