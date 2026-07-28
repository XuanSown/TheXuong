export interface CartItem {
<<<<<<< HEAD
  id: number | string
  variantId: number
  productId: number
  productName: string
  productImage: string
=======
  id: number
  productId: number
  productName: string
  productImage: string
  variantId: number
>>>>>>> fb265d4 (restore: code lost to ponytail full (2026-07-27) from stash@{0})
  size: string
  quantity: number
  price: number
  subtotal: number
}
<<<<<<< HEAD
export interface Cart { items: CartItem[] }
=======

export interface Cart {
  items: CartItem[]
  totalItems: number
  totalPrice: number
}
>>>>>>> fb265d4 (restore: code lost to ponytail full (2026-07-27) from stash@{0})
