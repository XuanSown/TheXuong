import http from './http'

export const loyaltyService = {
  /**
   * Lấy số dư điểm hiện tại
   */
  async getPoints(): Promise<number> {
    const response = await http.get('/loyalty/points')
    return response.data.data.currentPoints
  },

  /**
   * Lấy lịch sử giao dịch điểm
   */
  async getHistory(): Promise<any[]> {
    const response = await http.get('/loyalty/history')
    return response.data.data
  },

  /**
   * Lấy danh sách voucher người dùng đang sở hữu
   */
  async getMyVouchers(status?: 'UNUSED' | 'USED' | 'EXPIRED'): Promise<any[]> {
    const response = await http.get('/my-vouchers', { params: { status } })
    return response.data.data
  },

  /**
   * Lấy danh mục voucher có thể đổi
   */
  async getCatalog(): Promise<any[]> {
    const response = await http.get('/loyalty/catalog')
    return response.data.data
  },

  /**
   * Đổi voucher bằng điểm
   */
  async redeemVoucher(voucherId: number): Promise<any> {
    const response = await http.post('/loyalty/redeem', { voucherId })
    return response.data.data
  },

  /**
   * Validate mã giảm giá tại checkout
   */
  async validateVoucher(code: string, total: number): Promise<{ code: string; discountAmount: number }> {
    const response = await http.get('/loyalty/validate-voucher', { params: { code, total } })
    return response.data.data
  }
}

export default loyaltyService
