import { describe, it, expect, vi, beforeEach } from 'vitest'
import { useAuthStore } from '@/stores/auth.store'

vi.mock('@/services/auth.service', () => ({
  default: {
    login: vi.fn(),
    logout: vi.fn(),
    getCurrentUser: vi.fn(),
    register: vi.fn(),
    forgotPassword: vi.fn(),
    updateProfile: vi.fn(),
    changePassword: vi.fn(),
    resetPassword: vi.fn()
  }
}))

describe('auth store role getters', () => {
  let store: ReturnType<typeof useAuthStore>

  beforeEach(() => {
    store = useAuthStore()
  })

  const setRole = (role: string) => {
    store.setUser({ id: 1, email: 'x@y.z', username: 'x', fullName: 'X', role } as any)
  }

  it('CUSTOMER is customer but not admin', () => {
    setRole('CUSTOMER')
    expect(store.isCustomer).toBe(true)
    expect(store.isAdmin).toBe(false)
  })

  it('ADMIN is admin but not customer', () => {
    setRole('ADMIN')
    expect(store.isAdmin).toBe(true)
    expect(store.isCustomer).toBe(false)
  })

  it('BOTH is both admin and customer (allows switching to customer UI)', () => {
    setRole('BOTH')
    expect(store.isAdmin).toBe(true)
    expect(store.isCustomer).toBe(true)
  })

  it('guard must NOT block BOTH from customer routes', () => {
    setRole('BOTH')
    const guardBlocksCustomerRoutes = store.isAuthenticated && store.isAdmin && !store.isCustomer
    expect(guardBlocksCustomerRoutes).toBe(false)
  })

  it('guard must block pure ADMIN from customer routes', () => {
    setRole('ADMIN')
    const guardBlocksCustomerRoutes = store.isAuthenticated && store.isAdmin && !store.isCustomer
    expect(guardBlocksCustomerRoutes).toBe(true)
  })
})