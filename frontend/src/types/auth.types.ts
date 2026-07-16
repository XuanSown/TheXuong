export interface Address {
  id: number
  label?: string
  recipientName: string
  recipientPhone: string
  provinceCode: string
  districtCode: string
  wardCode: string
  streetDetail?: string
  latitude?: number
  longitude?: number
  isDefault: boolean
}

export interface User {
  id: number
  username: string
  email: string
  fullName: string
  phone?: string
  role: string // 'CUSTOMER' | 'ADMIN' | 'BOTH'
  roles: string[] // derived in store: [role]
  enabled: boolean
  createdAt: string
  phoneNumber?: string
  provider?: string
  addresses?: Address[]
}
