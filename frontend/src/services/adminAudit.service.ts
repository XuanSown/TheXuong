import http from './http'

export interface SystemAuditLog {
  id: number
  adminId: string
  module: string
  action: string
  targetId: string
  oldValues: string | null
  newValues: string | null
  changedFields: string | null
  note: string | null
  createdAt: string
}

export const adminAuditService = {
  async getAuditLogs() {
    const { data } = await http.get('/admin/audit-logs')
    return data
  }
}
