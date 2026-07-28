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