/**
 * Voucher System Types
 * Batch 2: Voucher Catalog Management
 */

// ============================================================
// Voucher Catalog (Vouchers table)
// ============================================================
export interface Voucher {
  id: number
  code: string
  discountAmount: number
  requiredPoints: number
  minOrderAmount: number
  applicableCategoryIds: number[] | null
  applicableProductIds: number[] | null
  vipOnly: boolean
  status: VoucherStatus
  expiresAt: string | null // ISO date string or null
  createdAt: string
  updatedAt: string
  createdBy: string | null
  updatedBy: string | null
  claimedCount: number // computed from UserVouchers
}

export type VoucherStatus = 'ACTIVE' | 'LOCKED' | 'EXPIRED'

export interface VoucherCreateRequest {
  code?: string // optional: if null, auto-generate
  discountAmount: number
  requiredPoints: number
  minOrderAmount: number
  applicableCategoryIds?: number[] | null
  applicableProductIds?: number[] | null
  vipOnly: boolean
  status: VoucherStatus
  expiresAt?: string | null
}

export interface VoucherUpdateRequest {
  discountAmount?: number
  requiredPoints?: number
  minOrderAmount?: number
  applicableCategoryIds?: number[] | null
  applicableProductIds?: number[] | null
  vipOnly?: boolean
  status?: VoucherStatus
  expiresAt?: string | null
  adminNote?: string // required for certain status changes
}

export interface VoucherResponse {
  id: number
  code: string
  discountAmount: number
  requiredPoints: number
  minOrderAmount: number
  applicableCategoryIds: number[] | null
  applicableProductIds: number[] | null
  vipOnly: boolean
  status: VoucherStatus
  expiresAt: string | null
  createdAt: string
  updatedAt: string
  createdBy: string | null
  updatedBy: string | null
  claimedCount: number
}

export interface VoucherListResponse {
  vouchers: VoucherResponse[]
  total: number
  page: number
  size: number
}

// ============================================================
// Bulk Operations
// ============================================================
export type BulkAction = 'LOCK' | 'UNLOCK' | 'DELETE' | 'SET_VIP'

export interface BulkVoucherRequest {
  ids: number[]
  action: BulkAction
  value?: boolean // for SET_VIP action
  adminNote?: string
}

export interface BulkResult {
  id: number
  success: boolean
  error?: string
}

export interface BulkVoucherResponse {
  totalRequested: number
  successCount: number
  failureCount: number
  failures: BulkResult[]
}

// ============================================================
// Supporting Types
// ============================================================
export interface Category {
  id: number
  name: string
  description?: string
  parentId?: number | null
  createdAt?: string
  updatedAt?: string
}

export interface Product {
  id: number
  name: string
  sku: string
  price: number
  categoryId?: number
  status?: 'ACTIVE' | 'INACTIVE' | 'OUT_OF_STOCK'
  // ... other fields as needed
}

export interface VoucherStats {
  totalVouchers: number
  activeVouchers: number
  lockedVouchers: number
  expiredVouchers: number
  totalClaimed: number
  vipVouchers: number
  byStatus: Record<VoucherStatus, number>
  byVip: Record<string, number> // { "false": 6, "true": 1 }
}

// ============================================================
// Audit Log (VoucherAuditLog table)
// ============================================================
export type AuditAction = 'CREATE' | 'UPDATE' | 'DELETE' | 'LOCK' | 'UNLOCK'

export interface VoucherAuditLog {
  id: number
  voucherId: number
  adminId: string
  action: AuditAction
  oldValues: Record<string, unknown> | null
  newValues: Record<string, unknown> | null
  changedFields: string[] | null
  note: string | null
  createdAt: string
}

// ============================================================
// Form State
// ============================================================
export interface VoucherFormData {
  code: string
  discountAmount: number | null
  requiredPoints: number | null
  minOrderAmount: number
  applicableCategoryIds: number[]
  applicableProductIds: number[]
  vipOnly: boolean
  status: VoucherStatus
  expiresAt: string | null // YYYY-MM-DD format for input[type="date"]
}

export const DEFAULT_VOUCHER_FORM_DATA: VoucherFormData = {
  code: '',
  discountAmount: null,
  requiredPoints: null,
  minOrderAmount: 0,
  applicableCategoryIds: [],
  applicableProductIds: [],
  vipOnly: false,
  status: 'ACTIVE',
  expiresAt: null
}

export const VALID_DISCOUNT_AMOUNTS = [
  10000,   // 10k
  20000,   // 20k
  50000,   // 50k
  100000,  // 100k
  200000,  // 200k
  500000   // 500k
]

export const DISCOUNT_AMOUNT_LABELS = [
  '10,000 đ',
  '20,000 đ',
  '50,000 đ',
  '100,000 đ',
  '200,000 đ',
  '500,000 đ'
]
