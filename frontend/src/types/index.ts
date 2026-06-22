// User types
export interface User {
  id: number
  username: string
  email: string
  fullName: string
  phone?: string
  address?: string
  roles: string[] // ['CUSTOMER'], ['ADMIN'], ['BOTH']
  enabled: boolean
  createdAt: string
}

// Product types
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

// Cart types
export interface CartItem {
  id: number
  productId: number
  productName: string
  productImage: string
  variantId: number
  size: string
  quantity: number
  price: number
  subtotal: number
}

export interface Cart {
  items: CartItem[]
  totalItems: number
  totalPrice: number
}

// Order types
export interface Order {
  id: number
  orderNumber: string
  userId: number
  items: OrderItem[]
  shippingAddress: string
  shippingPhone: string
  shippingName: string
  subtotal: number
  shippingFee: number
  total: number
  status: OrderStatus
  paymentMethod: string
  paymentStatus: string
  vnpayTransactionNo?: string
  createdAt: string
  updatedAt: string
}

export interface OrderItem {
  productId: number
  productName: string
  variantId: number
  size: string
  quantity: number
  price: number
  subtotal: number
}

export enum OrderStatus {
  PENDING = 'PENDING',
  PAID = 'PAID',
  SHIPPING = 'SHIPPING',
  DELIVERED = 'DELIVERED',
  CANCELLED = 'CANCELLED'
}

// API Response types
export interface ApiResponse<T> {
  success: boolean
  data?: T
  message?: string
  errors?: Record<string, string[]>
}

// Pagination
export interface PageParams {
  page: number
  size: number
  keyword?: string
  sport?: string
  brand?: string
  sort?: 'newest' | 'price_asc' | 'price_desc'
}
