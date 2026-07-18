import axios, { AxiosInstance } from 'axios'

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1'

const client: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true
})

client.interceptors.request.use((config) => {
  if (['POST', 'PUT', 'PATCH', 'DELETE'].includes(config.method?.toUpperCase() || '')) {
    const meta = document.querySelector('meta[name="_csrf"]')
    const token = meta?.getAttribute('content') || localStorage.getItem('csrf_token')
    if (token) config.headers['X-CSRF-TOKEN'] = token
  }
  return config
})

client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      if (error.config?.url && !error.config.url.includes('/auth/user') && window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    if (error.response?.status === 403) window.location.href = '/'
    return Promise.reject(error)
  }
)

const http = {
  get: (url: string, config?: any) => client.get(url, config),
  post: (url: string, data?: any, config?: any) => client.post(url, data, config),
  put: (url: string, data?: any, config?: any) => client.put(url, data, config),
  patch: (url: string, data?: any, config?: any) => client.patch(url, data, config),
  delete: (url: string, config?: any) => client.delete(url, config),
  setCsrfToken: (token: string) => localStorage.setItem('csrf_token', token),
  clearCsrfToken: () => localStorage.removeItem('csrf_token')
}

export default http
