import http from './http'
import type { Review, ReviewListResponse } from '@/types/review.types'

export const reviewService = {
  async getProductReviews(productId: number): Promise<ReviewListResponse> {
    return (await http.get(`/reviews/product/${productId}`)).data
  },

  async createReview(productId: number, payload: { rating: number; comment?: string }): Promise<Review> {
    return (await http.post('/reviews', { productId, ...payload })).data
  },

  async updateReview(id: number, payload: { rating: number; comment?: string }): Promise<Review> {
    return (await http.put(`/reviews/${id}`, payload)).data
  },

  async deleteReview(id: number): Promise<void> {
    await http.delete(`/reviews/${id}`)
  }
}
