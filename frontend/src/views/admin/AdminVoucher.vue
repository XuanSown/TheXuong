<template>
  <div class="admin-voucher-page">
    <!-- Header Section -->
    <div class="header-section">
      <h1 class="page-title">
        QUẢN LÝ VOUCHER
      </h1>
      <button
        class="btn-add"
        @click="openCreateModal"
      >
        THÊM VOUCHER MỚI
      </button>
    </div>

    <!-- Search & Filters -->
    <div class="filters-section">
      <div class="search-box">
        <input
          v-model="searchQuery"
          type="text"
          placeholder="Tìm theo mã voucher (TX-XXXXXX)..."
          class="search-input"
        >
      </div>

      <div class="filter-buttons">
        <button
          v-for="status in statusOptions"
          :key="status.value"
          :class="['filter-btn', { active: statusFilter === status.value }]"
          @click="statusFilter = status.value"
        >
          {{ status.label }}
        </button>
      </div>

      <div class="vip-filter">
        <label class="checkbox-label">
          <input
            v-model="vipOnlyFilter"
            type="checkbox"
          >
          Chỉ VIP
        </label>
      </div>
    </div>

    <!-- Bulk Actions Bar (hiển thị khi có selection) -->
    <div
      v-if="selectedIds.length > 0"
      class="bulk-actions-bar"
    >
      <span class="selection-info">
        Đã chọn {{ selectedIds.length }} voucher
      </span>
      <div class="bulk-buttons">
        <button
          class="btn-bulk btn-lock"
          @click="handleBulkLock"
        >
          LOCK 🔒
        </button>
        <button
          class="btn-bulk btn-unlock"
          @click="handleBulkUnlock"
        >
          UNLOCK 🔓
        </button>
        <button
          class="btn-bulk btn-delete"
          @click="handleBulkDelete"
        >
          DELETE 🗑️
        </button>
        <button
          class="btn-bulk btn-vip"
          @click="handleBulkSetVip"
        >
          SET VIP ⭐
        </button>
      </div>
    </div>

    <!-- Data Table -->
    <div class="table-container">
      <table class="voucher-table">
        <thead>
          <tr>
            <th class="col-checkbox">
              <input
                v-model="allSelected"
                type="checkbox"
                :disabled="vouchers.length === 0"
                @change="toggleSelectAll"
              >
            </th>
            <th
              v-for="col in columns"
              :key="col.key"
              :class="['sortable', { active: sortBy === col.key }]"
              @click="col.sortable !== false ? handleSort(col.key) : null"
            >
              {{ col.label }}
              <span
                v-if="sortBy === col.key"
                class="sort-icon"
              >
                {{ sortOrder === 'asc' ? '▲' : '▼' }}
              </span>
            </th>
            <th>HÀNH ĐỘNG</th>
          </tr>
        </thead>
        <tbody>
          <tr v-if="isLoading">
            <td
              :colspan="columns.length + 2"
              class="loading-cell"
            >
              Đang tải...
            </td>
          </tr>
          <tr v-else-if="vouchers.length === 0">
            <td
              :colspan="columns.length + 2"
              class="empty-cell"
            >
              <div class="empty-state">
                <div class="empty-icon">
                  📦
                </div>
                <div class="empty-text">
                  Chưa có voucher nào
                </div>
                <div class="empty-hint">
                  Nhấn "THÊM VOUCHER MỚI" để tạo
                </div>
              </div>
            </td>
          </tr>
          <tr
            v-for="voucher in vouchers"
            v-else
            :key="voucher.id"
          >
            <td class="col-checkbox">
              <input
                v-model="selectedIds"
                :value="voucher.id"
                type="checkbox"
                :disabled="voucher.status === 'EXPIRED'"
              >
            </td>
            <td class="col-code">
              <span class="mono">{{ voucher.code }}</span>
            </td>
            <td class="col-amount">
              {{ formatCurrency(voucher.discountAmount) }}
            </td>
            <td class="col-points">
              {{ voucher.requiredPoints }}
            </td>
            <td class="col-min-order">
              {{ formatCurrency(voucher.minOrderAmount) }}
            </td>
            <td class="col-vip">
              <span
                v-if="voucher.vipOnly"
                class="badge vip-badge"
              >VIP</span>
              <span
                v-else
                class="badge normal-badge"
              >Không</span>
            </td>
            <td class="col-status">
              <span :class="['status-badge', getStatusClass(voucher.status)]">
                {{ getStatusLabel(voucher.status) }}
              </span>
            </td>
            <td class="col-actions">
              <button
                class="btn-action btn-edit"
                @click="openEditModal(voucher)"
              >
                SỬA
              </button>
              <button
                class="btn-action btn-delete"
                @click="handleDelete(voucher)"
              >
                XÓA
              </button>
            </td>
          </tr>
        </tbody>
      </table>
    </div>

    <!-- Pagination -->
    <div
      v-if="total > 0"
      class="pagination"
    >
      <span class="page-info">
        Hiển thị {{ startIndex }}-{{ endIndex }} của {{ total }} kết quả
      </span>
      <div class="page-buttons">
        <button
          :disabled="currentPage === 1"
          class="btn-page"
          @click="changePage(currentPage - 1)"
        >
          &lt;
        </button>
        <button
          v-for="page in visiblePages"
          :key="page"
          :class="['btn-page', { active: currentPage === page }]"
          @click="changePage(page)"
        >
          {{ page }}
        </button>
        <button
          :disabled="currentPage === totalPages"
          class="btn-page"
          @click="changePage(currentPage + 1)"
        >
          &gt;
        </button>
      </div>
    </div>

    <!-- Voucher Modal (Create/Edit) -->
    <div
      v-if="showModal"
      class="modal-overlay"
      @click.self="closeModal"
    >
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ modalMode === 'create' ? 'THÊM VOUCHER MỚI' : 'CHỈNH SỬA VOUCHER' }}</h2>
          <button
            class="btn-close"
            @click="closeModal"
          >
            &times;
          </button>
        </div>

        <div class="modal-body">
          <form @submit.prevent="handleSubmit">
            <!-- Code Field -->
            <div class="form-group">
              <label>Mã voucher <span class="required">*</span></label>
              <div class="input-with-hint">
                <input
                  v-model="formData.code"
                  type="text"
                  placeholder="TX-XXXXXX"
                  :class="{ error: errors.code }"
                  @blur="validateCode"
                >
                <button
                  type="button"
                  class="btn-generate"
                  title="Tự động tạo mã"
                  @click="generateCode"
                >
                  🎲
                </button>
              </div>
              <div
                v-if="errors.code"
                class="error-message"
              >
                {{ errors.code }}
              </div>
              <div class="form-hint">
                Format: TX-XXXXXX (chữ hoa, không O/I/L)
              </div>
            </div>

            <!-- Discount Amount & Required Points -->
            <div class="form-row">
              <div class="form-group">
                <label>Mệnh giá giảm (đồng) <span class="required">*</span></label>
                <select
                  v-model="formData.discountAmount"
                  :class="{ error: errors.discountAmount }"
                  @change="onDiscountChange"
                >
                  <option
                    :value="null"
                    disabled
                  >
                    Chọn mệnh giá
                  </option>
                  <option
                    v-for="(label, idx) in discountLabels"
                    :key="discountAmounts[idx]"
                    :value="discountAmounts[idx]"
                  >
                    {{ label }}
                  </option>
                </select>
                <div
                  v-if="errors.discountAmount"
                  class="error-message"
                >
                  {{ errors.discountAmount }}
                </div>
              </div>

              <div class="form-group">
                <label>Điểm cần <span class="required">*</span></label>
                <input
                  v-model="formData.requiredPoints"
                  type="number"
                  min="1"
                  max="50"
                  :class="{ error: errors.requiredPoints }"
                  @blur="validatePoints"
                >
                <div
                  v-if="errors.requiredPoints"
                  class="error-message"
                >
                  {{ errors.requiredPoints }}
                </div>
                <div class="form-hint">
                  Tự động: {{ formData.discountAmount ? Math.floor(formData.discountAmount / 10000) : 0 }} điểm
                </div>
              </div>
            </div>

            <!-- Min Order Amount -->
            <div class="form-group">
              <label>Min order amount (đồng)</label>
              <input
                v-model.number="formData.minOrderAmount"
                type="number"
                min="0"
                :class="{ error: errors.minOrderAmount }"
                @blur="validateMinOrder"
              >
              <div
                v-if="errors.minOrderAmount"
                class="error-message"
              >
                {{ errors.minOrderAmount }}
              </div>
              <div class="form-hint">
                Điều kiện đơn tối thiểu để áp dụng voucher
              </div>
            </div>

            <!-- VIP Only Toggle -->
            <div class="form-group checkbox-group">
              <label>
                <input
                  v-model="formData.vipOnly"
                  type="checkbox"
                >
                Chỉ dành cho VIP
              </label>
            </div>

            <!-- Status Select -->
            <div class="form-group">
              <label>Trạng thái <span class="required">*</span></label>
              <select
                v-model="formData.status"
                :class="{ error: errors.status }"
              >
                <option value="ACTIVE">
                  ACTIVE
                </option>
                <option value="LOCKED">
                  LOCKED
                </option>
                <option value="EXPIRED">
                  EXPIRED
                </option>
              </select>
              <div
                v-if="errors.status"
                class="error-message"
              >
                {{ errors.status }}
              </div>
            </div>

            <!-- Expires At Date Picker -->
            <div class="form-group">
              <label>Ngày hết hạn (catalog)</label>
              <input
                v-model="formData.expiresAt"
                type="date"
                :min="today"
              >
              <div class="form-hint">
                Để trống nếu vĩnh viễn
              </div>
            </div>

            <!-- Applicable Categories (Multi-select) -->
            <div class="form-group">
              <label>Danh mục áp dụng</label>
              <div class="multi-select-container">
                <select
                  v-model="formData.applicableCategoryIds"
                  multiple
                  class="multi-select"
                  size="5"
                >
                  <option
                    v-for="cat in categories"
                    :key="cat.id"
                    :value="cat.id"
                  >
                    {{ cat.name }}
                  </option>
                </select>
                <div
                  v-if="formData.applicableCategoryIds.length > 0"
                  class="selected-chips"
                >
                  <span
                    v-for="catId in formData.applicableCategoryIds"
                    :key="catId"
                    class="chip"
                  >
                    {{ getCategoryName(catId) }}
                    <button
                      type="button"
                      class="chip-remove"
                      @click="removeCategory(catId)"
                    >&times;</button>
                  </span>
                </div>
              </div>
              <div class="form-hint">
                Giữ Ctrl/Cmd để chọn nhiều. Để trống = áp dụng tất cả
              </div>
            </div>

            <!-- Applicable Products (Multi-select) -->
            <div class="form-group">
              <label>Sản phẩm áp dụng</label>
              <div class="multi-select-container">
                <select
                  v-model="formData.applicableProductIds"
                  multiple
                  class="multi-select"
                  size="5"
                >
                  <option
                    v-for="prod in products"
                    :key="prod.id"
                    :value="prod.id"
                  >
                    {{ prod.name }} ({{ prod.sku }})
                  </option>
                </select>
                <div
                  v-if="formData.applicableProductIds.length > 0"
                  class="selected-chips"
                >
                  <span
                    v-for="prodId in formData.applicableProductIds"
                    :key="prodId"
                    class="chip"
                  >
                    {{ getProductName(prodId) }}
                    <button
                      type="button"
                      class="chip-remove"
                      @click="removeProduct(prodId)"
                    >&times;</button>
                  </span>
                </div>
              </div>
              <div class="form-hint">
                Để trống = áp dụng tất cả sản phẩm
              </div>
            </div>

            <!-- Form Actions -->
            <div class="form-actions">
              <button
                type="button"
                class="btn-secondary"
                @click="closeModal"
              >
                HỦY
              </button>
              <button
                type="submit"
                :disabled="isSubmitting"
                class="btn-primary"
              >
                {{ isSubmitting ? 'ĐANG LƯU...' : 'LƯU' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Confirmation Dialog -->
    <div
      v-if="showConfirm"
      class="modal-overlay"
      @click.self="cancelConfirm"
    >
      <div class="confirm-dialog">
        <div class="confirm-header">
          <span class="confirm-icon">⚠️</span>
          <h3>{{ confirmTitle }}</h3>
        </div>
        <div class="confirm-body">
          {{ confirmMessage }}
        </div>
        <div class="confirm-actions">
          <button
            class="btn-secondary"
            @click="cancelConfirm"
          >
            HỦY
          </button>
          <button
            class="btn-confirm"
            :class="confirmType"
            @click="confirmAction"
          >
            {{ confirmButtonText }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useToast } from 'vue-toastification'
import voucherService from '@/services/voucherService'
import type { VoucherResponse, Category, Product } from '@/types/voucher'

const toast = useToast()

// ============================================================
// Constants
// ============================================================
const discountAmounts = [10000, 20000, 50000, 100000, 200000, 500000]
const discountLabels = [
  '10,000 đ',
  '20,000 đ',
  '50,000 đ',
  '100,000 đ',
  '200,000 đ',
  '500,000 đ'
]

const statusOptions = [
  { label: 'TẤT CẢ', value: 'all' },
  { label: 'ACTIVE', value: 'ACTIVE' },
  { label: 'LOCKED', value: 'LOCKED' },
  { label: 'EXPIRED', value: 'EXPIRED' }
]

const columns = [
  { key: 'code', label: 'MÃ VOUCHER', sortable: true },
  { key: 'discountAmount', label: 'MỆNG GIÁ', sortable: true },
  { key: 'requiredPoints', label: 'ĐIỂM CẦN', sortable: true },
  { key: 'minOrderAmount', label: 'MIN ORDER', sortable: true },
  { key: 'vipOnly', label: 'VIP ONLY', sortable: true },
  { key: 'status', label: 'TRẠNG THÁI', sortable: true }
]

// ============================================================
// Table State
// ============================================================
const vouchers = ref<VoucherResponse[]>([])
const total = ref(0)
const currentPage = ref(1)
const itemsPerPage = ref(5)
const searchQuery = ref('')
const statusFilter = ref('all')
const vipOnlyFilter = ref(false)
const selectedIds = ref<number[]>([])
const sortBy = ref('code')
const sortOrder = ref('asc')
const isLoading = ref(false)

// ============================================================
// Modal State
// ============================================================
const showModal = ref(false)
const modalMode = ref('create')
const currentVoucherId = ref<number | null>(null)
const formData = ref<any>({
  code: '',
  discountAmount: null,
  requiredPoints: null,
  minOrderAmount: 0,
  applicableCategoryIds: [],
  applicableProductIds: [],
  vipOnly: false,
  status: 'ACTIVE',
  expiresAt: null
})
const errors = ref<any>({})
const isSubmitting = ref(false)

// Supporting data for selects
const categories = ref<Category[]>([])
const products = ref<Product[]>([])

// ============================================================
// Confirmation Dialog State
// ============================================================
const showConfirm = ref(false)
const confirmActionCallback = ref<(() => void) | null>(null)
const confirmTitle = ref('')
const confirmMessage = ref('')
const confirmButtonText = ref('')
const confirmType = ref<'danger' | 'warning' | 'primary'>('primary')

// ============================================================
// Computed Properties
// ============================================================
const allSelected = computed({
  get: () => {
    return vouchers.value.length > 0 &&
      selectedIds.value.length === vouchers.value.length
  },
  set: (value) => {
    selectedIds.value = value ? vouchers.value.map(v => v.id) : []
  }
})

const totalPages = computed(() =>
  Math.ceil(total.value / itemsPerPage.value)
)

const startIndex = computed(() =>
  (currentPage.value - 1) * itemsPerPage.value + 1
)

const endIndex = computed(() =>
  Math.min(currentPage.value * itemsPerPage.value, total.value)
)

const visiblePages = computed(() => {
  const pages = []
  const maxVisible = 5
  let start = Math.max(1, currentPage.value - Math.floor(maxVisible / 2))
  let end = Math.min(totalPages.value, start + maxVisible - 1)

  if (end - start + 1 < maxVisible) {
    start = Math.max(1, end - maxVisible + 1)
  }

  for (let i = start; i <= end; i++) {
    pages.push(i)
  }
  return pages
})

const today = computed(() => {
  return new Date().toISOString().split('T')[0]
})

// ============================================================
// Data Fetching
// ============================================================
const fetchVouchers = async () => {
  isLoading.value = true
  try {
    const params = {
      page: currentPage.value,
      size: itemsPerPage.value,
      search: searchQuery.value || undefined,
      status: statusFilter.value === 'all' ? undefined : statusFilter.value,
      vipOnly: vipOnlyFilter.value || undefined
    }

    const response = await voucherService.getVouchers(params)
    vouchers.value = response.vouchers
    total.value = response.total
    selectedIds.value = [] // Reset selection on page change
  } catch (error) {
    console.error('Failed to fetch vouchers:', error)
    toast.error('Tai danh sach voucher that bai')
  } finally {
    isLoading.value = false
  }
}

const fetchCategories = async () => {
  try {
    categories.value = await voucherService.getAllCategories()
  } catch (error) {
    console.error('Failed to fetch categories:', error)
  toast.error('Tai danh sach danh muc that bai')
  }
}

const fetchProducts = async () => {
  try {
    products.value = await voucherService.getAllProducts()
  } catch (error) {
    console.error('Failed to fetch products:', error)
  toast.error('Tai danh sach san pham that bai')
  }
}

// ============================================================
// Sorting & Filtering
// ============================================================
const handleSort = (column: any) => {
  if (sortBy.value === column) {
    sortOrder.value = sortOrder.value === 'asc' ? 'desc' : 'asc'
  } else {
    sortBy.value = column as typeof sortBy.value
    sortOrder.value = 'asc'
  }
  // TODO: Implement server-side sorting or client-side
  // For now, just refetch
  fetchVouchers()
}

const changePage = (page: any) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  fetchVouchers()
}

// Reset to page 1 when filters change
watch([searchQuery, statusFilter, vipOnlyFilter], () => {
  currentPage.value = 1
  fetchVouchers()
})

// ============================================================
// Form Helpers
// ============================================================
const generateCode = () => {
  const allowedChars = 'ABCDEFGHJKMNPQRSTUVWXYZ23456789'
  let code = 'TX-'
  for (let i = 0; i < 6; i++) {
    code += allowedChars[Math.floor(Math.random() * allowedChars.length)]
  }
  formData.value.code = code
}

const onDiscountChange = () => {
  if (formData.value.discountAmount) {
    const expectedPoints = Math.floor(formData.value.discountAmount / 10000)
    if (formData.value.requiredPoints !== expectedPoints) {
      formData.value.requiredPoints = expectedPoints
    }
  }
}

const validateCode = () => {
  const code = formData.value.code?.trim()
  if (!code) {
    errors.value.code = 'Mã voucher là bắt buộc'
    return false
  }
  if (!voucherService.isValidCodeFormat(code)) {
    errors.value.code = 'Mã phải định dạng TX-XXXXXX (chữ hoa, không 0/O/1/I/L)'
    return false
  }
  delete errors.value.code
  return true
}

const validatePoints = () => {
  const points = formData.value.requiredPoints
  if (!points) {
    errors.value.requiredPoints = 'Điểm cần là bắt buộc'
    return false
  }
  if (points < 1 || points > 50) {
    errors.value.requiredPoints = 'Điểm cần phải từ 1-50'
    return false
  }
  if (formData.value.discountAmount && points !== Math.floor(formData.value.discountAmount / 10000)) {
    errors.value.requiredPoints = `Điểm cần phải là ${Math.floor(formData.value.discountAmount / 10000)} cho mệnh giá này`
    return false
  }
  delete errors.value.requiredPoints
  return true
}

const validateMinOrder = () => {
  const minOrder = formData.value.minOrderAmount || 0
  const discount = formData.value.discountAmount || 0
  if (minOrder < discount) {
    errors.value.minOrderAmount = 'Min order phải ≥ mệnh giá giảm'
    return false
  }
  delete errors.value.minOrderAmount
  return true
}

const validateForm = () => {
  const validators = [
    validateCode,
    validatePoints,
    validateMinOrder
  ]
  return validators.every(fn => fn())
}

const getCategoryName = (id: any) => {
  const cat = categories.value.find(c => c.id === id)
  return cat ? cat.name : String(id)
}

const getProductName = (id: any) => {
  const prod = products.value.find(p => p.id === id)
  return prod ? `${prod.name} (${prod.sku})` : String(id)
}

const removeCategory = (id: any) => {
  formData.value.applicableCategoryIds = formData.value.applicableCategoryIds.filter((catId: any) => catId !== id)
}

const removeProduct = (id: any) => {
  formData.value.applicableProductIds = formData.value.applicableProductIds.filter((prodId: any) => prodId !== id)
}

// ============================================================
// CRUD Operations
// ============================================================
const openCreateModal = () => {
  resetForm()
  modalMode.value = 'create'
  currentVoucherId.value = null
  showModal.value = true
}

const openEditModal = (voucher: VoucherResponse) => {
  resetForm()
  modalMode.value = 'edit'
  currentVoucherId.value = voucher.id
  formData.value = {
    code: voucher.code,
    discountAmount: voucher.discountAmount,
    requiredPoints: voucher.requiredPoints,
    minOrderAmount: voucher.minOrderAmount,
    applicableCategoryIds: voucher.applicableCategoryIds || [],
    applicableProductIds: voucher.applicableProductIds || [],
    vipOnly: voucher.vipOnly,
    status: voucher.status,
    expiresAt: voucher.expiresAt ? voucher.expiresAt.split('T')[0] : null
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  resetForm()
}

const resetForm = () => {
  formData.value = {
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
  errors.value = {}
}

const handleSubmit = async () => {
  if (!validateForm()) {
    return
  }

  isSubmitting.value = true
  try {
    const payload = {
      code: formData.value.code || undefined,
      discountAmount: formData.value.discountAmount,
      requiredPoints: formData.value.requiredPoints,
      minOrderAmount: formData.value.minOrderAmount,
      applicableCategoryIds: formData.value.applicableCategoryIds.length > 0
        ? formData.value.applicableCategoryIds
        : null,
      applicableProductIds: formData.value.applicableProductIds.length > 0
        ? formData.value.applicableProductIds
        : null,
      vipOnly: formData.value.vipOnly,
      status: formData.value.status,
      expiresAt: formData.value.expiresAt || null
    }

    if (modalMode.value === 'create') {
      await voucherService.createVoucher(payload)
    } else {
      await voucherService.updateVoucher(currentVoucherId.value!, payload)
    }

    closeModal()
    fetchVouchers()
  } catch (error) {
    console.error('Failed to save voucher:', error)
  } finally {
    isSubmitting.value = false
  }
}

// ============================================================
// Delete & Bulk Actions
// ============================================================


const showConfirmation = (
  title: string,
  message: string,
  action: () => void,
  buttonText = 'XÁC NHẬN',
  type: 'danger' | 'warning' | 'primary' = 'primary'
) => {
  confirmTitle.value = title
  confirmMessage.value = message
  confirmActionCallback.value = action
  confirmButtonText.value = buttonText
  confirmType.value = type
  showConfirm.value = true
}

const confirmAction = () => {
  if (confirmActionCallback.value) {
    confirmActionCallback.value()
  }
  cancelConfirm()
}

const cancelConfirm = () => {
  showConfirm.value = false
  confirmActionCallback.value = null
}

const handleDelete = (voucher: VoucherResponse) => {
  showConfirmation(
    'XÓA VOUCHER',
    `Bạn có chắc chuyển voucher "${voucher.code}" sang EXPIRED? Hành động này không thể hoàn tác.`,
    async () => {
      try {
        await voucherService.deleteVoucher(voucher.id)
        fetchVouchers()
      } catch (error) {
        console.error('Failed to delete voucher:', error)
      }
    },
    'CHUYỂN EXPIRED',
    'danger'
  )
}

const handleBulkLock = () => {
  showConfirmation(
    'LOCK VOUCHERS',
    `Bạn có chắc khóa ${selectedIds.value.length} voucher đã chọn?`,
    async () => {
      try {
        await voucherService.bulkAction({
          ids: selectedIds.value,
          action: 'LOCK'
        })
        selectedIds.value = []
        fetchVouchers()
      } catch (error) {
        console.error('Failed to bulk lock:', error)
      }
    }
  )
}

const handleBulkUnlock = () => {
  showConfirmation(
    'UNLOCK VOUCHERS',
    `Bạn có chắc mở khóa ${selectedIds.value.length} voucher đã chọn?`,
    async () => {
      try {
        await voucherService.bulkAction({
          ids: selectedIds.value,
          action: 'UNLOCK'
        })
        selectedIds.value = []
        fetchVouchers()
      } catch (error) {
        console.error('Failed to bulk unlock:', error)
      }
    }
  )
}

const handleBulkDelete = () => {
  showConfirmation(
    'DELETE VOUCHERS',
    `Bạn có chắc chuyển ${selectedIds.value.length} voucher sang EXPIRED? Hành động này không thể hoàn tác.`,
    async () => {
      try {
        await voucherService.bulkAction({
          ids: selectedIds.value,
          action: 'DELETE'
        })
        selectedIds.value = []
        fetchVouchers()
      } catch (error) {
        console.error('Failed to bulk delete:', error)
      }
    },
    'CHUYỂN EXPIRED',
    'danger'
  )
}

const handleBulkSetVip = () => {
  showConfirmation(
    'SET VIP',
    `Bạn có chắc đặt VIP flag = true cho ${selectedIds.value.length} voucher?`,
    async () => {
      try {
        await voucherService.bulkAction({
          ids: selectedIds.value,
          action: 'SET_VIP',
          value: true
        })
        selectedIds.value = []
        fetchVouchers()
      } catch (error) {
        console.error('Failed to bulk set VIP:', error)
      }
    },
    'XÁC NHẬN',
    'warning'
  )
}

const toggleSelectAll = () => {
  // Computed property handles this automatically
}

// ============================================================
// Display Helpers
// ============================================================
const formatCurrency = (value: any) => {
  return new Intl.NumberFormat('vi-VN').format(value) + ' đ'
}

const getStatusLabel = (status: 'ACTIVE' | 'LOCKED' | 'EXPIRED') => {
  const labels = {
    ACTIVE: 'Hoạt động',
    LOCKED: 'Đã khóa',
    EXPIRED: 'Hết hạn'
  }
  return labels[status]
}

const getStatusClass = (status: 'ACTIVE' | 'LOCKED' | 'EXPIRED') => {
  const classes = {
    ACTIVE: 'status-active',
    LOCKED: 'status-locked',
    EXPIRED: 'status-expired'
  }
  return classes[status]
}

// ============================================================
// Lifecycle
// ============================================================
onMounted(() => {
  fetchVouchers()
  fetchCategories()
  fetchProducts()
})
</script>

<style scoped>
/* ============================================================
   Admin Voucher Page — Tailwind-inspired CSS
   Follows pattern from AdminUsers.vue & AdminProducts.vue
   ============================================================ */

.admin-voucher-page {
  max-width: 1280px;
  margin: 0 auto;
  padding: 20px;
  font-family: 'Geist', sans-serif;
}

/* Header Section */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 24px;
  font-weight: 700;
  color: #000000;
  margin: 0;
}

.btn-add {
  background: #000000;
  color: #FFFFFF;
  border: none;
  padding: 12px 24px;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1.8px;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-add:hover {
  background: #333333;
}

/* Filters Section */
.filters-section {
  display: flex;
  gap: 16px;
  margin-bottom: 24px;
  align-items: center;
  flex-wrap: wrap;
}

.search-box {
  flex: 1;
  min-width: 300px;
}

.search-input {
  width: 100%;
  padding: 10px 16px;
  border: 1px solid #000000;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
}

.search-input:focus {
  border-color: #666666;
}

.filter-buttons {
  display: flex;
  gap: 8px;
}

.filter-btn {
  padding: 8px 16px;
  border: 1px solid #000000;
  background: #FFFFFF;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.filter-btn.active {
  background: #000000;
  color: #FFFFFF;
}

.filter-btn:hover:not(.active) {
  background: #F9F9F9;
}

.vip-filter {
  display: flex;
  align-items: center;
}

.checkbox-label {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  cursor: pointer;
}

/* Bulk Actions Bar */
.bulk-actions-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  background: #F9F9F9;
  padding: 12px 16px;
  margin-bottom: 16px;
  border: 1px solid #E8E8E8;
}

.selection-info {
  font-weight: 600;
  color: #000000;
}

.bulk-buttons {
  display: flex;
  gap: 8px;
}

.btn-bulk {
  padding: 8px 16px;
  border: none;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.btn-bulk:hover {
  opacity: 0.8;
}

.btn-lock {
  background: #FEF3C7;
  color: #92400E;
}

.btn-unlock {
  background: #DCFCE7;
  color: #166534;
}

.btn-delete {
  background: #FEE2E2;
  color: #991B1B;
}

.btn-vip {
  background: #F3E8FF;
  color: #6B21A8;
}

/* Table Container */
.table-container {
  border: 1px solid #000000;
  margin-bottom: 24px;
  overflow-x: auto;
}

.voucher-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 14px;
}

.voucher-table th,
.voucher-table td {
  padding: 12px 16px;
  text-align: left;
  border-bottom: 1px solid #E8E8E8;
}

.voucher-table th {
  background: #F9F9F9;
  font-weight: 600;
  color: #000000;
}

.voucher-table th.sortable {
  cursor: pointer;
  user-select: none;
}

.voucher-table th.sortable:hover {
  background: #EEEEEE;
}

.sort-icon {
  margin-left: 4px;
  font-size: 12px;
}

.col-checkbox {
  width: 48px;
  text-align: center;
}

.col-code {
  width: 150px;
}

.col-code .mono {
  font-family: 'Geist', sans-serif;
  font-size: 13px;
}

.col-amount {
  width: 120px;
}

.col-points {
  width: 100px;
}

.col-min-order {
  width: 120px;
}

.col-vip {
  width: 80px;
  text-align: center;
}

.col-status {
  width: 120px;
}

.col-actions {
  width: 150px;
  text-align: center;
}

/* Badges */
.badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.vip-badge {
  background: #F3E8FF;
  color: #6B21A8;
}

.normal-badge {
  background: #F9F9F9;
  color: #666666;
}

.status-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
}

.status-active {
  background: #DCFCE7;
  color: #166534;
}

.status-locked {
  background: #FEF3C7;
  color: #92400E;
}

.status-expired {
  background: #FEE2E2;
  color: #991B1B;
}

/* Action Buttons */
.btn-action {
  padding: 6px 12px;
  margin: 0 4px;
  border: 1px solid #000000;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-edit {
  background: #FFFFFF;
  color: #000000;
}

.btn-edit:hover {
  background: #000000;
  color: #FFFFFF;
}

.btn-delete {
  background: #FFFFFF;
  color: #DC2626;
  border-color: #DC2626;
}

.btn-delete:hover {
  background: #DC2626;
  color: #FFFFFF;
}

/* Table States */
.loading-cell,
.empty-cell {
  text-align: center;
  padding: 60px 20px !important;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
}

.empty-icon {
  font-size: 48px;
}

.empty-text {
  font-size: 16px;
  font-weight: 600;
  color: #000000;
}

.empty-hint {
  font-size: 14px;
  color: #666666;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 0;
}

.page-info {
  font-size: 14px;
  color: #666666;
}

.page-buttons {
  display: flex;
  gap: 4px;
}

.btn-page {
  min-width: 40px;
  padding: 8px 12px;
  border: 1px solid #E8E8E8;
  background: #FFFFFF;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-page:hover:not(:disabled) {
  border-color: #000000;
}

.btn-page.active {
  background: #000000;
  color: #FFFFFF;
  border-color: #000000;
}

.btn-page:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

/* Modal Overlay */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-content {
  background: #FFFFFF;
  border: 1px solid #000000;
  width: 90%;
  max-width: 700px;
  max-height: 90vh;
  overflow-y: auto;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #E8E8E8;
}

.modal-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.btn-close {
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: #666666;
  line-height: 1;
}

.btn-close:hover {
  color: #000000;
}

.modal-body {
  padding: 24px;
}

/* Form Styles */
.form-group {
  margin-bottom: 20px;
}

.form-group label {
  display: block;
  margin-bottom: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #000000;
}

.required {
  color: #DC2626;
}

.form-group input[type="text"],
.form-group input[type="number"],
.form-group input[type="date"],
.form-group select {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #000000;
  font-size: 14px;
  outline: none;
  transition: border-color 0.2s;
  box-sizing: border-box;
}

.form-group input:focus,
.form-group select:focus {
  border-color: #666666;
}

.form-group input.error,
.form-group select.error {
  border-color: #DC2626;
}

.error-message {
  color: #DC2626;
  font-size: 12px;
  margin-top: 4px;
}

.form-hint {
  color: #666666;
  font-size: 12px;
  margin-top: 4px;
}

.input-with-hint {
  display: flex;
  gap: 8px;
}

.input-with-hint input {
  flex: 1;
}

.btn-generate {
  padding: 10px 16px;
  border: 1px solid #000000;
  background: #FFFFFF;
  cursor: pointer;
  font-size: 16px;
}

.btn-generate:hover {
  background: #F9F9F9;
}

/* Form Row (2 columns) */
.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

/* Checkbox Group */
.checkbox-group label {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.checkbox-group input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
}

/* Multi-select */
.multi-select-container {
  position: relative;
}

.multi-select {
  width: 100%;
  height: 120px;
  padding: 8px;
  border: 1px solid #000000;
  font-size: 14px;
  outline: none;
}

.multi-select:focus {
  border-color: #666666;
}

.selected-chips {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 8px;
}

.chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: #F9F9F9;
  border: 1px solid #E8E8E8;
  border-radius: 16px;
  font-size: 12px;
}

.chip-remove {
  background: none;
  border: none;
  cursor: pointer;
  font-size: 16px;
  line-height: 1;
  color: #666666;
  padding: 0;
}

.chip-remove:hover {
  color: #DC2626;
}

/* Form Actions */
.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
  padding-top: 20px;
  border-top: 1px solid #E8E8E8;
}

.btn-primary {
  background: #000000;
  color: #FFFFFF;
  border: none;
  padding: 12px 32px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.btn-primary:hover:not(:disabled) {
  opacity: 0.8;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background: #FFFFFF;
  color: #000000;
  border: 1px solid #000000;
  padding: 12px 32px;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.btn-secondary:hover {
  background: #F9F9F9;
}

/* Confirmation Dialog */
.confirm-dialog {
  background: #FFFFFF;
  border: 1px solid #000000;
  width: 90%;
  max-width: 450px;
  padding: 24px;
}

.confirm-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 16px;
}

.confirm-icon {
  font-size: 28px;
}

.confirm-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 700;
}

.confirm-body {
  margin-bottom: 24px;
  color: #333333;
  line-height: 1.6;
}

.confirm-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.btn-confirm {
  padding: 10px 24px;
  border: none;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
}

.btn-confirm.danger {
  background: #DC2626;
  color: #FFFFFF;
}

.btn-confirm.warning {
  background: #F59E0B;
  color: #FFFFFF;
}

.btn-confirm.primary {
  background: #000000;
  color: #FFFFFF;
}

/* Responsive adjustments */
@media (max-width: 768px) {
  .filters-section {
    flex-direction: column;
    align-items: stretch;
  }

  .search-box {
    min-width: 100%;
  }

  .filter-buttons {
    flex-wrap: wrap;
  }

  .header-section {
    flex-direction: column;
    gap: 16px;
    text-align: center;
  }

  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
