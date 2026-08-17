import http from './http'
import type { ApiResponse } from '@/types/common.types'
import type {
  AdminChatLog,
  AdminChatMemorySummary,
  AdminConversationDetail,
  AdminFaq,
  AdminFaqRequest,
  CustomerCareOverview,
  LogFilters,
  PageData,
} from '@/types/customerCare'

export const customerCareAdminService = {
  async getOverview(): Promise<ApiResponse<CustomerCareOverview>> {
    const { data } = await http.get('/admin/customer-care/overview')
    return data
  },

  async getFaqs(params?: { keyword?: string; topic?: string; page?: number; size?: number }): Promise<ApiResponse<PageData<AdminFaq>>> {
    const { data } = await http.get('/admin/customer-care/faqs', { params })
    return data
  },

  async createFaq(payload: AdminFaqRequest): Promise<ApiResponse<AdminFaq>> {
    const { data } = await http.post('/admin/customer-care/faqs', payload)
    return data
  },

  async updateFaq(id: number, payload: AdminFaqRequest): Promise<ApiResponse<AdminFaq>> {
    const { data } = await http.put(`/admin/customer-care/faqs/${id}`, payload)
    return data
  },

  async deleteFaq(id: number): Promise<ApiResponse<null>> {
    const { data } = await http.delete(`/admin/customer-care/faqs/${id}`)
    return data
  },

  async getConversations(params?: { keyword?: string; page?: number; size?: number }): Promise<ApiResponse<PageData<AdminChatMemorySummary>>> {
    const { data } = await http.get('/admin/customer-care/conversations', { params })
    return data
  },

  async getConversationDetail(chatId: string): Promise<ApiResponse<AdminConversationDetail>> {
    const { data } = await http.get(`/admin/customer-care/conversations/${chatId}`)
    return data
  },

  async resetMemory(chatId: string): Promise<ApiResponse<null>> {
    const { data } = await http.delete(`/admin/customer-care/conversations/${chatId}`)
    return data
  },

  async getLogs(params?: LogFilters & { page?: number; size?: number; sort?: string }): Promise<ApiResponse<PageData<AdminChatLog>>> {
    const { data } = await http.get('/admin/customer-care/logs', { params })
    return data
  },
}
