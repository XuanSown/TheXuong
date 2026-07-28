import { defineStore } from 'pinia'
import type { Product } from '@/types'
<<<<<<< HEAD
import favoriteService from '@/services/favorite.service'
=======

const STORAGE_KEY = 'favorite_products'
>>>>>>> fb265d4 (restore: code lost to ponytail full (2026-07-27) from stash@{0})

export const useFavoriteStore = defineStore('favorite', {
  state: () => ({
    items: [] as Product[],
<<<<<<< HEAD
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
=======
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
>>>>>>> fb265d4 (restore: code lost to ponytail full (2026-07-27) from stash@{0})
        this.items.splice(index, 1)
      } else {
        this.items.push(product)
      }
<<<<<<< HEAD
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
=======
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
>>>>>>> fb265d4 (restore: code lost to ponytail full (2026-07-27) from stash@{0})
