import { defineStore } from 'pinia'
import type { User } from '@/types'
import api from '@/services/api'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null as User | null,
    isAuthenticated: false,
    roles: [] as string[],
    loading: false
  }),

  getters: {
    isAdmin: (state) => state.roles.includes('ADMIN') || state.roles.includes('BOTH'),
    isCustomer: (state) => state.roles.includes('CUSTOMER'),
    hasRole: (state) => (role: string) => state.roles.includes(role)
  },

  actions: {
    async login(email: string, password: string) {
      this.loading = true
      try {
        const user = await api.login({ email, password })
        this.setUser(user)
        return user
      } finally {
        this.loading = false
      }
    },

    async logout() {
      try {
        await api.logout()
      } finally {
        this.clear()
        window.location.href = '/'
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
      }
    },

    setUser(user: User) {
      this.user = user
      this.isAuthenticated = true
      this.roles = user.roles || []
    },

    clear() {
      this.user = null
      this.isAuthenticated = false
      this.roles = []
      api.clearCsrfToken()
    }
  }
})
