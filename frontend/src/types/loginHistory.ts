export interface AdminLoginHistory {
  id: number
  userId: number | null
  email: string
  ipAddress: string | null
  userAgent: string | null
  provider: string
  success: boolean
  failureReason: string | null
  createdAt: string
}

export interface LoginHistoryFilters {
  email?: string
  provider?: string
  success?: boolean
  from?: string
  to?: string
  page?: number
  size?: number
  sort?: string
}
