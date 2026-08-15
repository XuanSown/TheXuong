export type { User, Address } from './auth.types'
export type { Cart, CartItem } from './cart.types'
export type { Order, OrderItem } from './order.types'
export { OrderStatus } from './order.types'
export type { ApiResponse, PageParams } from './common.types'

export interface Product {
  id: number
  name: string
  price: number
  description?: string
  imageUrl: string // ảnh chính (images[0])
  images?: string[] // danh sách tất cả ảnh (1-5 ảnh)
  sport: string
  category: string
  brand: string
  viewCount: number
  sizes: ProductSize[]
  createdAt: string
  updatedAt: string
}

export interface ProductSize {
  id: number
  size: string
  quantity: number
}

export interface ProductListResponse {
  content: Product[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export interface RecommendationProduct {
  id: number
  name: string
  price: number
  imageUrl: string
  sport: string
  brand: string
  category: string
}
