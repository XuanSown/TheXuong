import { defineStore } from 'pinia'
import type { Cart, CartItem } from '@/types'
import cartService from '@/services/cart.service'
import i18n from '@/i18n'

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
    guestItems: (localStorage.getItem('guest_cart_items') ? JSON.parse(localStorage.getItem('guest_cart_items')!) : []) as GuestCartItem[],
    cart: null as Cart | null,
    loading: false
  }),

  getters: {
    totalItems: (state) => {
      if (state.cart?.items) {
        return state.cart.items.reduce((sum, item) => sum + item.quantity, 0)
      }
      // Fallback to guest cart from localStorage
      return state.guestItems.reduce((sum, item) => sum + item.quantity, 0)
    },
    totalPrice: (state) => {
      if (state.cart?.items) {
        return state.cart.items.reduce((sum, item) => sum + item.subtotal, 0)
      }
      // Fallback to guest cart from localStorage
      return state.guestItems.reduce((sum, item) => sum + (item.price || 0) * item.quantity, 0)
    },
    items: (state) => state.cart?.items || [],
    // Get display items (from server or guest localStorage)
    displayItems: (state): Array<Omit<CartItem, 'id'> & { id: number | string; variantId: number }> => {
      if (state.cart?.items) {
        return state.cart.items
      }
      // Return guest items
      return state.guestItems.map((item, index) => ({
        id: `guest-${index}`,
        variantId: item.variantId,
        productId: item.productId || 0,
        productName: item.productName || i18n.global.t('product.genericName'),
        productImage: item.productImage || '',
        size: item.size || '',
        quantity: item.quantity,
        price: item.price || 0,
        subtotal: (item.price || 0) * item.quantity
      }))
    },
    isGuestCart: (state) => state.cart === null
  },

  actions: {
    // Fetch cart from server (for authenticated users)
    async fetchCart() {
      this.loading = true
      try {
        const cart = await cartService.getCart()
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
          const cart = await cartService.addCartItem({ variantId, quantity })
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
        const cart = await cartService.updateCartItem(itemId, quantity)
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
        await cartService.removeCartItem(itemId)
        await this.fetchCart()
      } else {
        // Guest: remove from localStorage
        this.removeFromGuestCart(itemId)
      }
    },

    clearCart() {
      this.cart = null
      this.guestItems = []
      localStorage.removeItem(GUEST_CART_KEY)
    },

    // Merge guest cart (from localStorage) into server cart after login
    async mergeGuestCart() {
      const guestItems = [...this.guestItems];
      if (!guestItems || guestItems.length === 0) return;
      try {
        // Fetch current server cart
        await this.fetchCart()

        let hasError = false;
        // Add each guest item to server cart
        for (const guestItem of guestItems) {
          try {
            await cartService.addCartItem({
              variantId: guestItem.variantId,
              quantity: guestItem.quantity
            })
          } catch (error) {
            console.error('Failed to merge guest item:', error)
            hasError = true;
          }
        }

        // Refetch cart to get merged result
        await this.fetchCart()
        
        // Clear guest cart only if all merges succeeded
        if (!hasError) {
          this.guestItems = []
          localStorage.removeItem(GUEST_CART_KEY)
        } else {
          console.warn('Guest cart not cleared due to merge errors.')
        }
      } catch (error) {
        console.error('Failed to merge guest cart:', error)
      }
    },

    // Guest cart localStorage operations
    addToGuestCart(variantId: number, quantity: number, productInfo?: Partial<GuestCartItem>) {
      const existingIndex = this.guestItems.findIndex(item => item.variantId === variantId)
      if (existingIndex >= 0) {
        this.guestItems[existingIndex].quantity += quantity
      } else {
        this.guestItems.push({
          variantId,
          quantity,
          ...productInfo
        })
      }

      localStorage.setItem(GUEST_CART_KEY, JSON.stringify(this.guestItems))
    },

    updateGuestCartItem(variantId: number, quantity: number) {
      const index = this.guestItems.findIndex(item => item.variantId === variantId)
      if (index >= 0) {
        if (quantity <= 0) {
          this.guestItems.splice(index, 1)
        } else {
          this.guestItems[index].quantity = quantity
        }
        localStorage.setItem(GUEST_CART_KEY, JSON.stringify(this.guestItems))
      }
    },

    removeFromGuestCart(variantId: number) {
      this.guestItems = this.guestItems.filter(item => item.variantId !== variantId)
      localStorage.setItem(GUEST_CART_KEY, JSON.stringify(this.guestItems))
    },

    // Get guest cart items (for display before login)
    getGuestCartItems(): GuestCartItem[] {
      return this.guestItems
    }
  }
})
