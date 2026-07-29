export interface ApiResponse<T> {
  success: boolean
  data?: T
  message?: string
  errors?: Record<string, string[]>
}

export interface PageParams {
  page: number
  size: number
  keyword?: string
  sport?: string
  brand?: string
  sort?: 'newest' | 'price_asc' | 'price_desc'
}
