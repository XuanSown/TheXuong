<<<<<<< HEAD
export const OrderStatus = {
  PENDING: 'PENDING', CONFIRMED: 'CONFIRMED', CANCEL_REQUESTED: 'CANCEL_REQUESTED',
  SHIPPING: 'SHIPPING', DELIVERED: 'DELIVERED', COMPLETED: 'COMPLETED',
  CANCELLED: 'CANCELLED', REFUNDED: 'REFUNDED'
} as const
export type OrderStatus = typeof OrderStatus[keyof typeof OrderStatus]
export interface OrderItem {
  imageUrl?: string
  productName: string
  size: string
  quantity: number
  price: number
}
export interface Order {
  id: number
  createdAt: string
  fullName: string
  phoneNumber: string
  address: string
  totalMoney: number
  total?: number
  status: OrderStatus
  note?: string
  paymentMethod: string
  subtotal: number
  items: OrderItem[]
}
=======
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
  fullName?: string
  phoneNumber?: string
  address?: string
  note?: string
  totalMoney?: number
}

export interface OrderItem {
  productId: number
  productName: string
  variantId: number
  size: string
  quantity: number
  price: number
  subtotal: number
  imageUrl?: string
}

export enum OrderStatus {
  PENDING = 'PENDING',
  CONFIRMED = 'CONFIRMED',
  SHIPPING = 'SHIPPING',
  DELIVERED = 'DELIVERED',
  COMPLETED = 'COMPLETED',
  CANCELLED = 'CANCELLED',
  REFUNDED = 'REFUNDED'
}
>>>>>>> fb265d4 (restore: code lost to ponytail full (2026-07-27) from stash@{0})
