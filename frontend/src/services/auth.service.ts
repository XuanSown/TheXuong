import http from './http'
import type { User } from '@/types'

export const authService = {
  async login(credentials: { email: string; password: string }): Promise<User> {
    const response = await http.post<{ accessToken: string; user: User }>('/auth/login', credentials)
    localStorage.setItem('access_token', response.data.accessToken)
    return response.data.user
  },

  async logout(): Promise<void> {
    try {
      await http.post('/auth/logout')
    } finally {
      localStorage.removeItem('access_token')
    }
  },

  async getCurrentUser(): Promise<User> {
    const response = await http.get<{ user: User }>('/auth/user')
    return response.data.user
  },

  async register(data: { fullName: string; email: string; password: string; confirmPassword: string }): Promise<{ message: string }> {
    return (await http.post('/auth/register', data)).data
  },

  async forgotPassword(email: string): Promise<{ message: string }> {
    return (await http.post('/auth/forgot-password', { email })).data
  },

  async resetPassword(data: { token: string; newPassword: string; confirmPassword: string }): Promise<{ message: string }> {
    return (await http.post('/auth/reset-password', data)).data
  },

  async getProfile(): Promise<any> {
    return (await http.get('/auth/profile')).data
  },

  async updateProfile(data: { fullName?: string; phoneNumber?: string; address?: string }): Promise<any> {
    return (await http.put('/auth/profile', data)).data
  },

  async changePassword(data: { currentPassword: string; newPassword: string; confirmPassword: string }): Promise<any> {
    return (await http.put('/auth/password', data)).data
  }
}
