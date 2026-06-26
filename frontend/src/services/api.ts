import axios, { AxiosInstance, AxiosError } from 'axios'
import type { User } from '@/types'

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1'

class ApiService {
  private client: AxiosInstance

  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json'
      },
      withCredentials: true // important: send cookies (JSESSIONID)
    })

    // Request interceptor - add CSRF token
    this.client.interceptors.request.use(
      (config) => {
        // Only add CSRF for mutating requests
        if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(config.method?.toUpperCase() || '')) {
          const token = this.getCsrfToken()
          if (token) {
            config.headers['X-CSRF-TOKEN'] = token
          }
        }
        return config
      },
      (error) => Promise.reject(error)
    )

    // Response interceptor - handle auth errors
    this.client.interceptors.response.use(
      (response) => response,
      async (error: AxiosError) => {
        if (error.response?.status === 401) {
          // Unauthorized - redirect to login
          window.location.href = '/login'
          return Promise.reject(error)
        }
        if (error.response?.status === 403) {
          // Forbidden - redirect to home or show 403 page
          window.location.href = '/'
          return Promise.reject(error)
        }
        return Promise.reject(error)
      }
    )
  }

  private getCsrfToken(): string | null {
    // Try to get from meta tag (injected by Spring Boot)
    const meta = document.querySelector('meta[name="_csrf"]')
    if (meta) {
      return meta.getAttribute('content') || null
    }
    // Or from localStorage if stored
    return localStorage.getItem('csrf_token')
  }

  setCsrfToken(token: string) {
    localStorage.setItem('csrf_token', token)
  }

  clearCsrfToken() {
    localStorage.removeItem('csrf_token')
  }

  // Auth APIs
  async login(credentials: { email: string; password: string }): Promise<User> {
    const response = await this.client.post<{ user: User }>('/auth/login', credentials)
    return response.data.user
  }

  async logout(): Promise<void> {
    await this.client.post('/auth/logout')
  }

  async getCurrentUser(): Promise<User> {
    const response = await this.client.get<{ user: User }>('/auth/user')
    return response.data.user
  }

  async register(data: {
    fullName: string
    email: string
    password: string
    confirmPassword: string
  }): Promise<{ message: string }> {
    return (await this.client.post('/auth/register', data)).data
  }

  async forgotPassword(email: string): Promise<{ message: string }> {
    return (await this.client.post('/auth/forgot-password', { email })).data
  }

  // Products APIs
  async getProducts(params: {
    page?: number
    size?: number
    keyword?: string
    sport?: string
    brand?: string
    sort?: 'newest' | 'price_asc' | 'price_desc'
  } = {}): Promise<{
    content: any[]
    totalElements: number
    totalPages: number
    size: number
    number: number
  }> {
    return (await this.client.get('/products', { params })).data
  }

  async getProduct(id: number): Promise<any> {
    return (await this.client.get(`/products/${id}`)).data
  }

  async getNewProducts(limit: number = 8): Promise<any[]> {
    return (await this.client.get('/products/new', { params: { limit } })).data
  }

  async getSports(): Promise<string[]> {
    return (await this.client.get('/categories/sports')).data
  }

  async getBrands(): Promise<string[]> {
    return (await this.client.get('/categories/brands')).data
  }

  // Cart APIs
  async getCart(): Promise<any> {
    return (await this.client.get('/cart')).data
  }

  async addCartItem(data: { variantId: number; quantity: number }): Promise<any> {
    return (await this.client.post('/cart/items', data)).data
  }

  async updateCartItem(id: number, quantity: number): Promise<any> {
    return (await this.client.put(`/cart/items/${id}`, { quantity })).data
  }

  async removeCartItem(id: number): Promise<void> {
    await this.client.delete(`/cart/items/${id}`)
  }

  // Order APIs
  async getOrders(): Promise<any[]> {
    return (await this.client.get('/orders')).data
  }

  async getOrder(id: number): Promise<any> {
    return (await this.client.get(`/orders/${id}`)).data
  }

  async createOrder(): Promise<{ orderId: number; vnpayUrl: string }> {
    return (await this.client.post('/orders/place')).data
  }

  async cancelOrder(id: number): Promise<void> {
    await this.client.post(`/orders/${id}/cancel`)
  }

  // Checkout APIs
  async createCheckout(): Promise<{ vnpayUrl: string }> {
    return (await this.client.post('/checkout/create')).data
  }

  async confirmPayment(orderId: number): Promise<void> {
    await this.client.post(`/checkout/confirm`, { orderId })
  }

  // Profile APIs
  async getProfile(): Promise<any> {
    return (await this.client.get('/auth/profile')).data
  }

  async updateProfile(data: {
    fullName?: string
    phoneNumber?: string
    address?: string
  }): Promise<any> {
    return (await this.client.put('/auth/profile', data)).data
  }

  // Admin APIs
  async getAdminUsers(): Promise<any[]> {
    return (await this.client.get('/admin/users')).data
  }

  async toggleUserActive(id: number): Promise<any> {
    return (await this.client.patch(`/admin/users/${id}/toggle-active`)).data
  }

  async getAdminProducts(params?: {
    page?: number
    size?: number
    keyword?: string
  }): Promise<any> {
    return (await this.client.get('/admin/products', { params })).data
  }

  async createAdminProduct(data: FormData): Promise<any> {
    return (await this.client.post('/admin/products', data, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })).data
  }

  async updateAdminProduct(id: number, data: FormData): Promise<any> {
    return (await this.client.put(`/admin/products/${id}`, data, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })).data
  }

  async deleteAdminProduct(id: number): Promise<void> {
    await this.client.delete(`/admin/products/${id}`)
  }

  async getAdminOrders(params?: {
    status?: string
    page?: number
    size?: number
  }): Promise<any> {
    return (await this.client.get('/admin/orders', { params })).data
  }

  async updateOrderStatus(id: number, status: string): Promise<any> {
    return (await this.client.patch(`/admin/orders/${id}/status`, { status })).data
  }

  async getStatistics(): Promise<any> {
    return (await this.client.get('/admin/statistics')).data
  }
}

export const api = new ApiService()
export default api
