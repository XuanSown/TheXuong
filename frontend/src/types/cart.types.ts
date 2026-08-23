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
  stockQuantity?: number
}

export interface Cart {
  items: CartItem[]
  totalItems: number
  totalPrice: number
}
