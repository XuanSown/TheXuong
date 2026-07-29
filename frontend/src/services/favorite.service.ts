import type { Product } from '@/types'

// ponytail: favorites are client-side localStorage only — backend has no favorite endpoint
// (no Favorite entity/controller/path in the Java source). Persist full Product objects so
// Favorite.vue can render without re-fetching. When backend adds /favorites, replace these
// with http calls mirroring cart.service.ts.

const FAVORITES_KEY = 'favorite_items'

function read(): Product[] {
  try {
    return JSON.parse(localStorage.getItem(FAVORITES_KEY) || '[]') as Product[]
  } catch {
    return []
  }
}

function write(items: Product[]): void {
  try {
    localStorage.setItem(FAVORITES_KEY, JSON.stringify(items))
  } catch {
    // storage full or disabled — no-op
  }
}

export const favoriteService = {
  getFavorites(): Promise<Product[]> {
    return Promise.resolve(read())
  },

  addFavorite(product: Product): Promise<void> {
    const items = read()
    if (!items.some(p => p.id === product.id)) items.push(product)
    write(items)
    return Promise.resolve()
  },

  removeFavorite(productId: number): Promise<void> {
    write(read().filter(p => p.id !== productId))
    return Promise.resolve()
  }
}

export default favoriteService