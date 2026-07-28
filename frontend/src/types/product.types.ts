export interface Product {
  id: number
  name: string
  price: number
  description?: string
  imageUrl: string
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
  size: string // numeric for shoes, text for clothing
  quantity: number
}

export interface ProductListResponse {
  content: Product[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}
