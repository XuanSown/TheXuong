export interface CartItem {
  id: number | string
  productId: number
  productName: string
  productImage: string
  size: string
  quantity: number
  price: number
  subtotal: number
}
export interface Cart { items: CartItem[] }