/**
 * Voucher Service — API client for admin voucher management
 * Batch 2: Voucher Catalog CRUD + Bulk Operations
 */

import api from './api'
import type {
  VoucherResponse,
  VoucherListResponse,
  VoucherCreateRequest,
  VoucherUpdateRequest,
  BulkVoucherRequest,
  BulkVoucherResponse,
  VoucherStats,
  Category,
  Product
} from '@/types/voucher'

export const voucherService = {
  // ============================================================
  // CRUD Operations
  // ============================================================

  /**
   * Get paginated list of vouchers with filters
   */
  async getVouchers(params: {
    page?: number
    size?: number
    search?: string
    status?: string | 'all'
    vipOnly?: boolean
    minPoints?: number
    maxPoints?: number
  }): Promise<VoucherListResponse> {
    const response = await api.get('/admin/loyalty/vouchers', { params })
    return response.data?.data || response.data
  },

  /**
   * Get single voucher by ID
   */
  async getVoucher(id: number): Promise<VoucherResponse> {
    const response = await api.get(`/admin/loyalty/vouchers/${id}`)
    return response.data?.data || response.data
  },

  /**
   * Create new voucher
   * If code is empty/null, backend will auto-generate
   */
  async createVoucher(data: VoucherCreateRequest): Promise<VoucherResponse> {
    const response = await api.post('/admin/loyalty/vouchers', data)
    return response.data?.data || response.data
  },

  /**
   * Update existing voucher (partial update)
   */
  async updateVoucher(
    id: number,
    data: VoucherUpdateRequest
  ): Promise<VoucherResponse> {
    const response = await api.put(`/admin/loyalty/vouchers/${id}`, data)
    return response.data?.data || response.data
  },

  /**
   * Delete voucher (soft delete → status = EXPIRED)
   * Fails if voucher has claimed UserVouchers (business rule)
   */
  async deleteVoucher(id: number): Promise<void> {
    await api.delete(`/admin/loyalty/vouchers/${id}`)
  },

  // ============================================================
  // Bulk Operations
  // ============================================================

  /**
   * Bulk lock/unlock/delete/set-vip multiple vouchers
   */
  async bulkAction(data: BulkVoucherRequest): Promise<BulkVoucherResponse> {
    const response = await api.post('/admin/loyalty/vouchers/bulk', data)
    return response.data?.data || response.data
  },

  // ============================================================
  // Statistics & Supporting Data
  // ============================================================

  /**
   * Get voucher statistics (optional endpoint)
   */
  async getVoucherStats(): Promise<VoucherStats> {
    const response = await api.get('/admin/loyalty/vouchers/stats')
    return response.data?.data || response.data
  },

  /**
   * Get all categories for multi-select (load once on modal open)
   */
  async getAllCategories(): Promise<Category[]> {
    const response = await api.get('/categories', { params: { all: true } })
    return response.data.content || response.data
  },

  /**
   * Search products (for multi-select)
   */
  async searchProducts(query: string): Promise<Product[]> {
    const response = await api.get('/products', { params: { keyword: query, size: 100 } })
    return response.data.content || response.data
  },

  /**
   * Get all products (if Option A: load all on modal open)
   */
  async getAllProducts(): Promise<Product[]> {
    const response = await api.get('/products', { params: { size: 100 } })
    return response.data.content || response.data
  },

  // ============================================================
  // Validation Helpers (Client-side)
  // ============================================================

  /**
   * Validate code format: TX-[A-Z0-9]{6} (exclude 0,O,1,I,L)
   */
  isValidCodeFormat(code: string): boolean {
    const regex = /^TX-[A-HJ-NP-RT-Z0-9]{6}$/i
    return regex.test(code)
  },

  /**
   * Validate discount amount is in allowed list
   */
  isValidDiscountAmount(amount: number): boolean {
    return [10000, 20000, 50000, 100000, 200000, 500000].includes(amount)
  },

  /**
   * Validate points consistency: requiredPoints = discountAmount / 10000
   */
  validatePointsConsistency(
    discountAmount: number,
    requiredPoints: number
  ): boolean {
    return requiredPoints === Math.floor(discountAmount / 10000)
  },

  /**
   * Format discount amount for display
   */
  formatDiscount(amount: number): string {
    return new Intl.NumberFormat('vi-VN').format(amount) + ' đ'
  },

  /**
   * Format status badge label
   */
  getStatusLabel(status: string): string {
    const labels: Record<string, string> = {
      ACTIVE: 'Hoạt động',
      LOCKED: 'Đã khóa',
      EXPIRED: 'Hết hạn'
    }
    return labels[status] || status
  }
}

export default voucherService
