import { defineStore } from 'pinia'
import type { User } from '@/types'
import authService from '@/services/auth.service'
import http from '@/services/http'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null as User | null,
    isAuthenticated: false,
    isInitialized: false,
    roles: [] as string[],
    loading: false,
    redirectTo: null as string | null // Store redirect path after login
  }),

  getters: {
    isAdmin: (state) => state.roles.includes('ADMIN') || state.roles.includes('BOTH'),
    isCustomer: (state) => state.roles.includes('CUSTOMER'),
    hasRole: (state) => (role: string) => state.roles.includes(role)
  },

  actions: {
    setRedirectPath(path: string | null) {
      this.redirectTo = path
    },

    setUser(user: User) {
      this.user = user
      this.isAuthenticated = true
      this.roles = user.roles || []
    },

    async login(credentials: { email: string; password: string }) {
      this.loading = true
      try {
        const user = await authService.login({ email: credentials.email, password: credentials.password })
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
      return await authService.register(data)
    },

    async logout() {
      try {
        await authService.logout()
      } finally {
        this.clear()
        window.location.href = '/'
      }
    },

    async fetchUser() {
      this.loading = true
      try {
        const user = await authService.getCurrentUser()
        this.setUser(user)
        this.isInitialized = true
        return user
      } catch (error) {
        this.clear()
        this.isInitialized = true
        throw error
      } finally {
        this.loading = false
      }
    },

    async updateProfile(profileData: {
      fullName?: string
      phoneNumber?: string
      password?: string
    }) {
      this.loading = true
      try {
        const response = await authService.updateProfile(profileData)
        // Update local user data
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
      this.roles = []
      this.redirectTo = null
      http.clearCsrfToken()
      // Clear guest cart on logout
      localStorage.removeItem('guest_cart_items')
    }
  }
})
