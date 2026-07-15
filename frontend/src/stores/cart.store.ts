import { defineStore } from 'pinia'
import type { Cart, CartItem } from '@/types'
import api from '@/services/api'

const GUEST_CART_KEY = 'guest_cart_items'

interface GuestCartItem {
  variantId: number
  quantity: number
  productId?: number
  productName?: string
  productImage?: string
  size?: string
  price?: number
}

export const useCartStore = defineStore('cart', {
  state: () => ({
    cart: null as Cart | null,
    loading: false
  }),

  getters: {
    totalItems: (state) => {
      if (state.cart?.items) {
        return state.cart.items.reduce((sum, item) => sum + item.quantity, 0)
      }
      // Fallback to guest cart from localStorage
      const guestItems = localStorage.getItem(GUEST_CART_KEY)
      if (guestItems) {
        try {
          const items = JSON.parse(guestItems) as GuestCartItem[]
          return items.reduce((sum, item) => sum + item.quantity, 0)
        } catch {
          return 0
        }
      }
      return 0
    },
    totalPrice: (state) => {
      if (state.cart?.items) {
        return state.cart.items.reduce((sum, item) => sum + item.subtotal, 0)
      }
      // Fallback to guest cart from localStorage
      const guestItems = localStorage.getItem(GUEST_CART_KEY)
      if (guestItems) {
        try {
          const items = JSON.parse(guestItems) as GuestCartItem[]
          return items.reduce((sum, item) => sum + (item.price || 0) * item.quantity, 0)
        } catch {
          return 0
        }
      }
      return 0
    },
    items: (state) => state.cart?.items || [],
    // Get display items (from server or guest localStorage)
    displayItems: (state): Array<Omit<CartItem, 'id'> & { id: number | string; variantId: number }> => {
      if (state.cart?.items) {
        return state.cart.items
      }
      // Return guest items with computed id based on variantId
      const guestItemsStr = localStorage.getItem(GUEST_CART_KEY)
      if (!guestItemsStr) return []
      try {
        const guestItems = JSON.parse(guestItemsStr) as GuestCartItem[]
        return guestItems.map((item, index) => ({
          id: `guest-${index}`,
          variantId: item.variantId,
          productId: item.productId || 0,
          productName: item.productName || 'Sản phẩm',
          productImage: item.productImage || '',
          size: item.size || '',
          quantity: item.quantity,
          price: item.price || 0,
          subtotal: (item.price || 0) * item.quantity
        }))
      } catch {
        return []
      }
    },
    isGuestCart: (state) => state.cart === null
  },

  actions: {
    // Fetch cart from server (for authenticated users)
    async fetchCart() {
      this.loading = true
      try {
        const cart = await api.getCart()
        this.cart = cart
        // Clear guest cart after successful server fetch
        localStorage.removeItem(GUEST_CART_KEY)
      } finally {
        this.loading = false
      }
    },

    // Add item - works for both authenticated and guest users
    async addItem(variantId: number, quantity: number = 1, productInfo?: Partial<GuestCartItem>, isAuthenticated?: boolean) {
      // If explicitly authenticated or cart exists (server cart loaded), use API
      if (isAuthenticated || this.cart !== null) {
        try {
          const cart = await api.addCartItem({ variantId, quantity })
          this.cart = cart
        } catch (error) {
          // If API fails (e.g., token expired), fallback to guest cart
          console.error('Add to API failed, falling back to guest cart:', error)
          this.addToGuestCart(variantId, quantity, productInfo)
        }
      } else {
        // Guest: save to localStorage
        this.addToGuestCart(variantId, quantity, productInfo)
      }
    },

    // Update quantity
    async updateItem(itemId: number, quantity: number) {
      if (this.cart) {
        // Authenticated
        const cart = await api.updateCartItem(itemId, quantity)
        this.cart = cart
      } else {
        // Guest: update localStorage
        this.updateGuestCartItem(itemId, quantity)
      }
    },

    // Remove item
    async removeItem(itemId: number) {
      if (this.cart) {
        // Authenticated
        await api.removeCartItem(itemId)
        await this.fetchCart()
      } else {
        // Guest: remove from localStorage
        this.removeFromGuestCart(itemId)
      }
    },

    clearCart() {
      this.cart = null
      localStorage.removeItem(GUEST_CART_KEY)
    },

    // Merge guest cart (from localStorage) into server cart after login
    async mergeGuestCart() {
      const guestItemsStr = localStorage.getItem(GUEST_CART_KEY)
      if (!guestItemsStr) return

      try {
        const guestItems: GuestCartItem[] = JSON.parse(guestItemsStr)
        if (guestItems.length === 0) return

        // Fetch current server cart
        await this.fetchCart()

        // Add each guest item to server cart
        for (const guestItem of guestItems) {
          try {
            await api.addCartItem({
              variantId: guestItem.variantId,
              quantity: guestItem.quantity
            })
          } catch (error) {
            console.error('Failed to merge guest item:', error)
          }
        }

        // Refetch cart to get merged result
        await this.fetchCart()
        // Clear guest cart after successful merge
        localStorage.removeItem(GUEST_CART_KEY)
      } catch (error) {
        console.error('Failed to merge guest cart:', error)
      }
    },

    // Guest cart localStorage operations
    addToGuestCart(variantId: number, quantity: number, productInfo?: Partial<GuestCartItem>) {
      const guestItemsStr = localStorage.getItem(GUEST_CART_KEY)
      let guestItems: GuestCartItem[] = guestItemsStr ? JSON.parse(guestItemsStr) : []

      // Check if variant already exists
      const existingIndex = guestItems.findIndex(item => item.variantId === variantId)
      if (existingIndex >= 0) {
        guestItems[existingIndex].quantity += quantity
      } else {
        guestItems.push({
          variantId,
          quantity,
          ...productInfo
        })
      }

      localStorage.setItem(GUEST_CART_KEY, JSON.stringify(guestItems))
    },

    updateGuestCartItem(variantId: number, quantity: number) {
      const guestItemsStr = localStorage.getItem(GUEST_CART_KEY)
      if (!guestItemsStr) return

      const guestItems: GuestCartItem[] = JSON.parse(guestItemsStr)
      const index = guestItems.findIndex(item => item.variantId === variantId)

      if (index >= 0) {
        if (quantity <= 0) {
          guestItems.splice(index, 1)
        } else {
          guestItems[index].quantity = quantity
        }
        localStorage.setItem(GUEST_CART_KEY, JSON.stringify(guestItems))
      }
    },

    removeFromGuestCart(variantId: number) {
      const guestItemsStr = localStorage.getItem(GUEST_CART_KEY)
      if (!guestItemsStr) return

      const guestItems: GuestCartItem[] = JSON.parse(guestItemsStr)
      const filtered = guestItems.filter(item => item.variantId !== variantId)
      localStorage.setItem(GUEST_CART_KEY, JSON.stringify(filtered))
    },

    // Get guest cart items (for display before login)
    getGuestCartItems(): GuestCartItem[] {
      const guestItemsStr = localStorage.getItem(GUEST_CART_KEY)
      if (!guestItemsStr) return []
      try {
        return JSON.parse(guestItemsStr) as GuestCartItem[]
      } catch {
        return []
      }
    }
  }
})
