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
