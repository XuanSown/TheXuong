import { describe, it, expect, vi, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useCartStore } from '@/stores/cart.store'
import cartService from '@/services/cart.service'

vi.mock('@/services/cart.service', () => ({
  default: {
    getCart: vi.fn(),
    addCartItem: vi.fn(),
    updateCartItem: vi.fn(),
    removeCartItem: vi.fn()
  }
}))

describe('cart store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  it('fetchCart loads cart from cartService and sets cart state', async () => {
    const mockCart = {
      id: 1,
      items: [
        {
          id: 10,
          productId: 100,
          productName: 'Shoe A',
          productImage: 'img.jpg',
          variantId: 5,
          size: '42',
          quantity: 2,
          price: 500000,
          subtotal: 1000000
        }
      ],
      total: 1000000,
      itemCount: 1
    }
    vi.mocked(cartService.getCart).mockResolvedValue(mockCart as any)

    const cartStore = useCartStore()
    await cartStore.fetchCart()

    expect(cartService.getCart).toHaveBeenCalledTimes(1)
    expect(cartStore.cart).toEqual(mockCart)
    expect(cartStore.totalItems).toBe(2)
  })

  it('mergeGuestCart fetches cart even when guestItems is empty', async () => {
    const mockCart = {
      id: 1,
      items: [
        {
          id: 10,
          productId: 100,
          productName: 'Shoe A',
          productImage: 'img.jpg',
          variantId: 5,
          size: '42',
          quantity: 1,
          price: 500000,
          subtotal: 500000
        }
      ],
      total: 500000,
      itemCount: 1
    }
    vi.mocked(cartService.getCart).mockResolvedValue(mockCart as any)

    const cartStore = useCartStore()
    expect(cartStore.guestItems.length).toBe(0)

    await cartStore.mergeGuestCart()

    expect(cartService.getCart).toHaveBeenCalledTimes(1)
    expect(cartStore.cart).toEqual(mockCart)
  })

  it('mergeGuestCart merges items and refreshes cart when guestItems exist', async () => {
    const mockCart = {
      id: 1,
      items: [],
      total: 0,
      itemCount: 0
    }
    vi.mocked(cartService.getCart).mockResolvedValue(mockCart as any)
    vi.mocked(cartService.addCartItem).mockResolvedValue({} as any)

    const cartStore = useCartStore()
    cartStore.guestItems = [
      { variantId: 5, quantity: 2, price: 100 }
    ]

    await cartStore.mergeGuestCart()

    expect(cartService.addCartItem).toHaveBeenCalledWith({ variantId: 5, quantity: 2 })
    expect(cartService.getCart).toHaveBeenCalled()
    expect(cartStore.guestItems).toEqual([])
  })

  it('clearCart resets both cart and guestItems', () => {
    const cartStore = useCartStore()
    cartStore.cart = { id: 1, items: [], total: 0, itemCount: 0 } as any
    cartStore.guestItems = [{ variantId: 1, quantity: 1 }]

    cartStore.clearCart()

    expect(cartStore.cart).toBeNull()
    expect(cartStore.guestItems).toEqual([])
  })
})
