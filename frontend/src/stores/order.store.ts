import { defineStore } from 'pinia'
import type { Order } from '@/types'
import orderService from '@/services/order.service'

export const useOrderStore = defineStore('order', {
  state: () => ({
    orders: [] as Order[],
    currentOrder: null as Order | null,
    loading: false,
    error: null as string | null
  }),
  actions: {
    async fetchOrders(): Promise<void> {
      this.loading = true
      this.error = null
      try {
        this.orders = await orderService.getOrders()
      } catch (e: any) {
        this.error = e?.message || 'Không tải được đơn hàng'
      } finally {
        this.loading = false
      }
    },
    async fetchOrderById(id: number | string): Promise<void> {
      this.loading = true
      try {
        this.currentOrder = await orderService.getOrder(id)
      } finally {
        this.loading = false
      }
    }
  }
})