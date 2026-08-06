import http from './http'
import type { ProductListResponse, Product } from '@/types'

export const productService = {
  async getProducts(params: {
    page?: number; size?: number; keyword?: string; sport?: string; brand?: string; 
    minPrice?: number; maxPrice?: number; shoeSize?: string;
    sort?: 'newest' | 'price_asc' | 'price_desc'
  } = {}): Promise<ProductListResponse> {
    return (await http.get('/products', { params })).data
  },

  async getProduct(id: number): Promise<Product> {
    return (await http.get(`/products/${id}`)).data
  },

  async getNewProducts(limit: number = 8): Promise<Product[]> {
    return (await http.get('/products/new', { params: { limit } })).data
  },

  async getSports(): Promise<string[]> {
    return (await http.get('/categories/sports')).data
  },

  async getBrands(): Promise<string[]> {
    return (await http.get('/categories/brands')).data
  }
}
