import http from './http'
import type { Cart } from '@/types'

export const cartService = {
  async getCart(): Promise<Cart> {
    return (await http.get('/cart')).data
  },

  async addCartItem(data: { variantId: number; quantity: number }): Promise<any> {
    return (await http.post('/cart/items', data)).data
  },

  async updateCartItem(id: number, quantity: number): Promise<any> {
    return (await http.put(`/cart/items/${id}`, { quantity })).data
  },

  async removeCartItem(id: number): Promise<Cart> {
    return (await http.delete(`/cart/items/${id}`)).data
  }
}

export default cartService
