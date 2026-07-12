import axios, { AxiosInstance, AxiosError } from 'axios'
import { useToast } from 'vue-toastification'

export class HttpClient {
  protected client: AxiosInstance

  constructor() {
    const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1'
    this.client = axios.create({
      baseURL: API_BASE_URL,
      headers: { 'Content-Type': 'application/json' },
      withCredentials: false
    })

    this.client.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('access_token')
        if (token) config.headers['Authorization'] = `Bearer ${token}`
        return config
      },
      (error) => Promise.reject(error)
    )

    this.client.interceptors.response.use(
      (response) => response,
      async (error: AxiosError) => {
        if (error.response?.status === 401) {
          const url = error.config?.url || ''
          if (!url.includes('/auth/user') && !url.includes('/auth/login')
              && !url.includes('/auth/forgot-password') && !url.includes('/auth/reset-password')) {
            localStorage.removeItem('access_token')
            if (window.location.pathname !== '/login') {
              window.location.href = `/login?redirect=${encodeURIComponent(window.location.pathname + window.location.search)}`
            }
          }
        }
        if (error.response?.status === 403) {
          if (window.location.pathname !== '/') window.location.href = '/'
        }

        // Global toast notification for API errors
        const url = error.config?.url || ''
        const isLoginApi = url.includes('/auth/login')
        
        if (error.response?.status !== 401 || isLoginApi) {
          const errorMessage = (error.response?.data as any)?.error
            || (error.response?.data as any)?.message
            || error.message
            || 'Có lỗi xảy ra khi kết nối tới máy chủ'
          useToast().error(errorMessage)
        }

        return Promise.reject(error)
      }
    )
  }

  // Expose HTTP methods for backward compatibility and raw usage
  get<T = any>(...args: Parameters<AxiosInstance['get']>) { return this.client.get<T>(...args) }
  post<T = any>(...args: Parameters<AxiosInstance['post']>) { return this.client.post<T>(...args) }
  put<T = any>(...args: Parameters<AxiosInstance['put']>) { return this.client.put<T>(...args) }
  patch<T = any>(...args: Parameters<AxiosInstance['patch']>) { return this.client.patch<T>(...args) }
  delete<T = any>(...args: Parameters<AxiosInstance['delete']>) { return this.client.delete<T>(...args) }
}

export const http = new HttpClient()
export default http
