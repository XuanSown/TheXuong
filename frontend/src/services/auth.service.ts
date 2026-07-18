import http from './http'
import type { User } from '@/types'

export const authService = {
  async login(credentials: { email: string; password: string }): Promise<User> {
    return (await http.post('/auth/login', credentials)).data.user
  },

  async logout(): Promise<void> {
    await http.post('/auth/logout')
  },

  async getCurrentUser(): Promise<User> {
    return (await http.get('/auth/user')).data.user
  },

  async register(data: {
    fullName: string
    email: string
    password: string
    confirmPassword: string
  }): Promise<{ message: string }> {
    return (await http.post('/auth/register', data)).data
  },

  async forgotPassword(email: string): Promise<{ message: string }> {
    return (await http.post('/auth/forgot-password', { email })).data
  },

  async updateProfile(data: {
    fullName?: string
    phoneNumber?: string
    password?: string
  }): Promise<{ user: User }> {
    return (await http.put('/auth/profile', data)).data
  },

  async changePassword(data: {
    currentPassword: string
    newPassword: string
    confirmPassword?: string
  }): Promise<any> {
    return (await http.put('/auth/password', data)).data
  },

  async resetPassword(data: {
    token: string
    password: string
    confirmPassword: string
  }): Promise<any> {
    return (await http.post('/auth/reset-password', data)).data
  }
}

export default authService
