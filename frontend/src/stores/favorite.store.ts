import { defineStore } from 'pinia'
import type { Product } from '@/types'
import favoriteService from '@/services/favorite.service'

export const useFavoriteStore = defineStore('favorite', {
  state: () => ({
    items: [] as Product[],
    loading: false
  }),
  getters: {
    isFavorite: (state) => (productId: number) => state.items.some(p => p.id === productId)
  },
  actions: {
    async fetchFavorites(): Promise<void> {
      this.loading = true
      try {
        this.items = await favoriteService.getFavorites()
      } finally {
        this.loading = false
      }
    },
    async toggleFavorite(product: Product): Promise<void> {
      const index = this.items.findIndex(p => p.id === product.id)
      const wasPresent = index >= 0
      if (wasPresent) {
        this.items.splice(index, 1)
      } else {
        this.items.push(product)
      }
      try {
        if (wasPresent) {
          await favoriteService.removeFavorite(product.id)
        } else {
          await favoriteService.addFavorite(product)
        }
      } catch (e: any) {
        if (wasPresent) {
          this.items.splice(index, 0, product)
        } else {
          const i = this.items.findIndex(p => p.id === product.id)
          if (i >= 0) this.items.splice(i, 1)
        }
      }
    }
  }
})