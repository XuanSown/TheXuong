import axios, { AxiosInstance } from 'axios'
import { useToast } from 'vue-toastification'

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1'

export const LOCKED_REDIRECT_PATH = '/login?locked=1'

export function shouldRedirectToLogin(status: number | undefined, pathname: string): boolean {
  return status === 423 && pathname !== '/login'
}

const client: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true
})

client.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    if (shouldRedirectToLogin(status, window.location.pathname)) {
      const msg = error.response?.data?.error || error.response?.data?.message ||
        'Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.'
      useToast().error(msg)
      window.location.href = LOCKED_REDIRECT_PATH
      return Promise.reject(error)
    }
    if (status === 401) {
      if (error.config?.url && !error.config.url.includes('/auth/user') && window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    if (status === 403) {
      // Review dùng 403 cho lỗi nghiệp vụ (chưa mua, không phải chủ review) → không redirect, để component hiện toast.
      const url = error.config?.url || ''
      if (!url.startsWith('/reviews')) window.location.href = '/'
    }
    return Promise.reject(error)
  }
)

const http = {
  get: (url: string, config?: any) => client.get(url, config),
  post: (url: string, data?: any, config?: any) => client.post(url, data, config),
  put: (url: string, data?: any, config?: any) => client.put(url, data, config),
  patch: (url: string, data?: any, config?: any) => client.patch(url, data, config),
  delete: (url: string, config?: any) => client.delete(url, config)
}

export default http
