import { defineStore } from 'pinia'
import type { Product } from '@/types'

const STORAGE_KEY = 'favorite_products'

export const useFavoriteStore = defineStore('favorite', {
  state: () => ({
    items: [] as Product[],
    loading: false,
  }),
  getters: {
    count: (state) => state.items.length,
    productIds: (state) => state.items.map(p => p.id),
  },
  actions: {
    loadFromStorage() {
      try {
        const stored = localStorage.getItem(STORAGE_KEY)
        if (stored) {
          this.items = JSON.parse(stored)
        }
      } catch {
        this.items = []
      }
    },
    saveToStorage() {
      localStorage.setItem(STORAGE_KEY, JSON.stringify(this.items))
    },
    toggleFavorite(product: Product) {
      const index = this.items.findIndex(p => p.id === product.id)
      if (index >= 0) {
        this.items.splice(index, 1)
      } else {
        this.items.push(product)
      }
      this.saveToStorage()
    },
    isFavorite(productId: number): boolean {
      return this.items.some(p => p.id === productId)
    },
    async fetchFavorites() {
      this.loading = true
      try {
        this.loadFromStorage()
      } finally {
        this.loading = false
      }
    },
  },
})
