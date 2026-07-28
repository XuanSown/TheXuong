<<<<<<< HEAD
export interface ApiResponse<T = unknown> { data?: T; message?: string; success?: boolean }
export interface PageParams { page?: number; size?: number; sort?: string }
=======
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
>>>>>>> fb265d4 (restore: code lost to ponytail full (2026-07-27) from stash@{0})
