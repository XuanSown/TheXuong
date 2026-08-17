import http from './http'
import type { ApiResponse } from '@/types/common.types'
import type { AdminLoginHistory, LoginHistoryFilters } from '@/types/loginHistory'
import type { PageData } from '@/types/customerCare'

export const loginHistoryAdminService = {
  async getHistory(params?: LoginHistoryFilters): Promise<ApiResponse<PageData<AdminLoginHistory>>> {
    const { data } = await http.get('/admin/login-history', { params })
    return data
  },
}
