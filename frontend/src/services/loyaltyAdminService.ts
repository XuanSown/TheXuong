import http from './http'

export interface UserLoyaltyProgress {
  userId: number
  currentTierCode: string
  currentTierName: string
  currentPoints: number
  totalSpent365Days: number
  totalPointsEarned365Days: number
  nextTierCode?: string
  nextTierName?: string
  minSpentNextTier?: number
  minPointsNextTier?: number
  spentRemainingToNextTier?: number
  pointsRemainingToNextTier?: number
}

export interface TierHistory {
  id: number
  userId: number
  oldTierCode: string
  newTierCode: string
  reason: string
  createdAt: string
}

export const loyaltyAdminService = {
  async getLoyaltyProgress(userId: number) {
    const { data } = await http.get(`/admin/loyalty/users/${userId}/progress`)
    return data
  },
  
  async getTierHistory(userId: number) {
    const { data } = await http.get(`/admin/loyalty/users/${userId}/history`)
    return data
  },
  
  async adjustPoints(userId: number, points: number, note: string) {
    const { data } = await http.post(`/admin/loyalty/users/${userId}/adjust-points`, { points, note })
    return data
  },

  async updateTier(userId: number, newTierCode: string, reason: string) {
    const { data } = await http.post(`/admin/loyalty/users/${userId}/update-tier`, { newTierCode, reason })
    return data
  }
}
