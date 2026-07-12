import { defineStore } from 'pinia'
import type { User } from '@/types'
import api from '@/services/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null as User | null,
    isAuthenticated: false,
    isInitialized: false,
    roles: [] as string[],
    loading: false,
    redirectTo: null as string | null,
  }),
  getters: {
    isAdmin: (state) => state.roles.includes('ADMIN') || state.roles.includes('BOTH'),
    isCustomer: (state) => state.roles.includes('CUSTOMER') || state.roles.includes('BOTH'),
    hasRole: (state) => (role: string) => state.roles.includes(role),
  },
  actions: {
    setRedirectPath(path: string | null) {
      this.redirectTo = path
    },
    setUser(user: User) {
      this.user = user
      this.isAuthenticated = true
      const role = user.role || 'CUSTOMER'
      this.roles = [role.toUpperCase()]
    },
    async login(credentials: { email: string; password: string }) {
      this.loading = true
      try {
        const user = await api.login(credentials)
        this.setUser(user)
        return user
      } finally {
        this.loading = false
      }
    },
    async register(data: {
      fullName: string
      email: string
      password: string
      confirmPassword: string
    }) {
      this.loading = true
      try {
        await api.register(data)
      } finally {
        this.loading = false
      }
    },
    async logout() {
      try {
        await api.logout()
      } catch {
        // Continue clearing state even if logout API fails
      } finally {
        this.clear()
      }
    },
    async fetchUser() {
      this.loading = true
      try {
        const user = await api.getCurrentUser()
        this.setUser(user)
        return user
      } catch (error) {
        this.clear()
        throw error
      } finally {
        this.loading = false
        this.isInitialized = true
      }
    },
    async updateProfile(profileData: {
      fullName?: string
      phoneNumber?: string
      address?: string
      password?: string
    }) {
      this.loading = true
      try {
        const response = await api.updateProfile(profileData)
        if (response.user) {
          this.setUser(response.user)
        }
        return response
      } finally {
        this.loading = false
      }
    },
    clear() {
      this.user = null
      this.isAuthenticated = false
      this.isInitialized = true // Still initialized even if cleared
      this.roles = []
      this.redirectTo = null
      localStorage.removeItem('guest_cart_items')
    },
  },
})
