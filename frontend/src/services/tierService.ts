import http from './http'

export interface Tier {
  id?: number
  code: string
  name: string
  minTotalSpent: number
  minTotalPoints: number
  benefits?: string
  bonusPercentage?: number
  autoDiscountPercent?: number
  rewardVoucherId?: number | null
  createdAt?: string
}

export const tierService = {
  async getAllTiers() {
    const { data } = await http.get('/admin/tiers')
    return data
  },
  
  async createTier(tier: Tier) {
    const { data } = await http.post('/admin/tiers', tier)
    return data
  },
  
  async updateTier(id: number, tier: Tier) {
    const { data } = await http.put(`/admin/tiers/${id}`, tier)
    return data
  },
  
  async deleteTier(id: number) {
    const { data } = await http.delete(`/admin/tiers/${id}`)
    return data
  }
}
