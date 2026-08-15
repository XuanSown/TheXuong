import { defineStore } from 'pinia'
import { type Order, OrderStatus } from '@/types'
import { orderService } from '@/services/order.service'
import { getApiErrorMessage } from '@/utils/apiError'

export const useOrderStore = defineStore('order', {
  state: () => ({
    orders: [] as Order[],
    currentOrder: null as Order | null,
    loading: false,
    error: null as string | null,
  }),
  actions: {
    async fetchOrders() {
      this.loading = true
      this.error = null
      try {
        this.orders = await orderService.getOrders()
      } catch (e: any) {
        this.error = getApiErrorMessage(e, 'orders.loadFailed')
        this.orders = []
      } finally {
        this.loading = false
      }
    },
    async fetchOrderById(id: string) {
      this.loading = true
      this.error = null
      try {
        this.currentOrder = await orderService.getOrder(Number(id))
        return this.currentOrder
      } catch (e: any) {
        this.error = getApiErrorMessage(e, 'orders.loadDetailFailed')
        this.currentOrder = null
        throw e
      } finally {
        this.loading = false
      }
    },
    async cancelOrder(id: number) {
      this.loading = true
      this.error = null
      try {
        await orderService.cancelOrder(id)
        // Optimistic update
        const order = this.orders.find(o => o.id === id)
        if (order) {
          order.status = OrderStatus.CANCELLED
        }
        if (this.currentOrder?.id === id) {
          this.currentOrder.status = OrderStatus.CANCELLED
        }
      } catch (e: any) {
        this.error = getApiErrorMessage(e, 'orders.cancelFailed')
        throw e
      } finally {
        this.loading = false
      }
    },
  },
})
