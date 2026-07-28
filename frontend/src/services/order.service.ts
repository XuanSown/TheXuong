import http from './http'
import type { Order } from '@/types'

export const orderService = {
  async getOrders(): Promise<Order[]> {
    return (await http.get('/orders')).data
  },

  async getOrder(id: number | string): Promise<Order> {
    return (await http.get(`/orders/${id}`)).data
  },

  async createOrder(data: {
    fullName: string
    phoneNumber: string
    address: string
    paymentMethod: string
    note?: string
    voucherCode?: string | null
    pointsToUse?: number | null
  }): Promise<{ order: { id: number; paymentUrl?: string } }> {
    return (await http.post('/orders', data)).data
  },

  async cancelOrder(id: number | string): Promise<void> {
    await http.post(`/orders/${id}/cancel`)
  },

  async updateOrderInfo(id: number | string, data: { phoneNumber: string; address: string }): Promise<any> {
    return (await http.put(`/orders/${id}/update-info`, data)).data
  },

  async confirmReceived(id: number | string): Promise<void> {
    await http.post(`/orders/${id}/confirm-received`)
  },

  async getCheckoutData(): Promise<{
    autoDiscountPercent?: number
    tierDiscountAmount?: number
    currentPoints?: number
    availableVouchers?: any[]
  }> {
    return (await http.get('/checkout')).data
  }
}

export default orderService
