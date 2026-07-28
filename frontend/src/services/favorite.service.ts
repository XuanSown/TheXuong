// ponytail: favorite endpoints unconfirmed against backend
import http from './http'
import type { Product } from '@/types'

export const favoriteService = {
  async getFavorites(): Promise<Product[]> {
    return (await http.get('/favorites')).data
  },

  async addFavorite(productId: number): Promise<void> {
    await http.post(`/favorites/${productId}`)
  },

  async removeFavorite(productId: number): Promise<void> {
    await http.delete(`/favorites/${productId}`)
  }
}

export default favoriteService