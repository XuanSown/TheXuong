<template>
  <div class="users-manager">
    <!-- Main Content -->
    <main class="main-content">
      <!-- User List Section -->
      <section class="user-list">
        <!-- Header -->
        <div class="list-header">
          <div class="header-info">
            <h2>Danh sách người dùng</h2>
            <p class="user-count">
              Tổng cộng {{ totalUsers }} thành viên
            </p>
          </div>
          <div class="search-container">
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Tìm theo email hoặc tên..."
              class="search-input"
              @input="onSearch"
            >
            <div class="search-icon">
              <svg
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <circle
                  cx="11"
                  cy="11"
                  r="8"
                />
                <line
                  x1="21"
                  y1="21"
                  x2="16.65"
                  y2="16.65"
                />
              </svg>
            </div>
          </div>
        </div>

        <!-- Table -->
        <div class="table-container">
          <table class="users-table">
            <thead>
              <tr>
                <th class="col-id">
                  ID
                </th>
                <th class="col-email">
                  EMAIL / HO TEN
                </th>
                <th class="col-role">
                  QUYEN
                </th>
                <th class="col-tier">
                  HANG
                </th>
                <th class="col-status">
                  TRANG THAI
                </th>
                <th class="col-provider">
                  PROVIDER
                </th>
                <th class="col-actions">
                  THAO TAC
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="isLoading">
                <td
                  colspan="6"
                  class="loading-cell"
                >
                  Đang tải dữ liệu...
                </td>
              </tr>
              <tr v-else-if="paginatedUsers.length === 0">
                <td
                  colspan="6"
                  class="empty-cell"
                >
                  Không tìm thấy người dùng nào
                </td>
              </tr>
              <tr
                v-for="user in paginatedUsers"
                :key="user.id"
              >
                <td class="col-id">
                  {{ user.idDisplay }}
                </td>
                <td class="col-email">
                  <div class="user-info">
                    <div class="user-details">
                      <span class="user-email">{{ user.email }}</span>
                      <span class="user-name">{{ user.fullName }}</span>
                    </div>
                  </div>
                </td>
                <td class="col-role">
                  <span
                    class="role-badge"
                    :class="user.roleClass"
                    @click="cycleRole(user)"
                  >
                    {{ user.role }}
                  </span>
                </td>
                <td class="col-tier">
                  <div class="tier-info">
                    <span class="badge">{{ user.tierCode || 'THUONG' }}</span>
                    <button
                      class="action-btn loyalty-btn"
                      title="Chi tiet hang"
                      @click="openLoyaltyModal(user)"
                    >
                      ⭐
                    </button>
                  </div>
                </td>
                <td class="col-status">
                  <span
                    class="status-toggle"
                    :class="{ active: user.isActive }"
                    @click="toggleUserActive(user)"
                  >
                    <div class="toggle-track">
                      <div class="toggle-thumb" />
                    </div>
                  </span>
                </td>
                <td class="col-provider">
                  <span
                    class="provider-badge"
                    :class="user.providerClass"
                  >
                    {{ user.provider }}
                  </span>
                </td>
                <td class="col-actions">
                  <div class="action-buttons">
                    <button
                      class="action-btn edit-btn"
                      title="Sua"
                      @click="editUser(user)"
                    >
                      <svg
                        width="16"
                        height="16"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                      >
                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                      </svg>
                    </button>
                    <button
                      class="action-btn delete-btn"
                      title="Xoa"
                      @click="deleteUser(user)"
                    >
                      <svg
                        width="16"
                        height="16"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                      >
                        <polyline points="3 6 5 6 21 6" />
                        <path d="M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6m3 0V4a2 2 0 0 1 2-2h4a2 2 0 0 1 2 2v2" />
                      </svg>
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <!-- Pagination -->
        <div class="pagination">
          <span
            v-if="!isLoading"
            class="showing-text"
          >
            Hiển thị {{ startIndex + 1 }} đến {{ Math.min(endIndex, filteredUsers.length) }} của {{ totalUsers }} người
            dùng
          </span>
          <div class="pagination-controls">
            <button
              class="page-btn"
              :disabled="currentPage === 1 || isLoading"
              @click="currentPage--"
            >
              Trước
            </button>
            <button
              v-for="page in visiblePages"
              :key="page"
              class="page-btn"
              :class="{ active: currentPage === page }"
              :disabled="isLoading"
              @click="currentPage = page"
            >
              {{ page }}
            </button>
            <button
              class="page-btn"
              :disabled="currentPage === totalPages || isLoading"
              @click="currentPage++"
            >
              Sau
            </button>
          </div>
        </div>
      </section>

      <!-- Add User Form -->
      <section class="add-user-form">
        <div class="form-header">
          <h2>Thêm người dùng mới</h2>
          <p class="form-subtitle">
            Điền thông tin để đăng ký tài khoản mới
          </p>
        </div>
        <form @submit.prevent="handleSubmit">
          <div class="form-grid">
            <!-- Email -->
            <div class="form-group">
              <label>EMAIL</label>
              <input
                v-model="formData.email"
                type="email"
                placeholder="example@thexuong.com"
                class="form-input"
                required
              >
            </div>

            <!-- Username -->
            <div class="form-group">
              <label>USERNAME</label>
              <input
                v-model="formData.username"
                type="text"
                placeholder="username123"
                class="form-input"
                required
              >
            </div>

            <!-- Full Name -->
            <div class="form-group">
              <label>FULL NAME</label>
              <input
                v-model="formData.fullName"
                type="text"
                placeholder="Ho va ten"
                class="form-input"
                required
              >
            </div>

            <!-- Password -->
            <div class="form-group">
              <label>PASSWORD</label>
              <input
                v-model="formData.password"
                type="password"
                placeholder="........"
                class="form-input"
                required
                minlength="8"
              >
            </div>
          </div>

          <!-- Role Selection -->
          <div class="form-group role-group">
            <label>QUYỀN NGƯỜI DÙNG</label>
            <div class="role-options">
              <button
                type="button"
                class="role-btn"
                :class="{ active: formData.role === 'CUSTOMER' }"
                @click="formData.role = 'CUSTOMER'"
              >
                CUSTOMER
              </button>
              <button
                type="button"
                class="role-btn"
                :class="{ active: formData.role === 'ADMIN' }"
                @click="formData.role = 'ADMIN'"
              >
                ADMIN
              </button>
              <button
                type="button"
                class="role-btn"
                :class="{ active: formData.role === 'BOTH' }"
                @click="formData.role = 'BOTH'"
              >
                BOTH
              </button>
            </div>
          </div>

          <!-- Form Actions -->
          <div class="form-actions">
            <button
              type="submit"
              class="submit-btn"
              :disabled="isSubmitting"
            >
              {{ isSubmitting ? 'ĐANG LƯU...' : 'LƯU' }}
            </button>
            <button
              type="button"
              class="reset-btn"
              @click="resetForm"
            >
              LÀM MỚI
            </button>
          </div>
        </form>
      </section>
    </main>

    <!-- Edit User Modal -->
    <div
      v-if="showEditModal"
      class="modal-overlay"
      @click.self="closeEditModal"
    >
      <div class="modal-content">
        <div class="modal-header">
          <h2>Sửa người dùng</h2>
          <button
            class="btn-close"
            @click="closeEditModal"
          >
            &times;
          </button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitEdit">
            <div class="form-group">
              <label>EMAIL</label>
              <input
                v-model="editForm.email"
                type="email"
                class="form-input"
                disabled
              >
            </div>
            <div class="form-group">
              <label>HO VA TEN</label>
              <input
                v-model="editForm.fullName"
                type="text"
                class="form-input"
                required
              >
            </div>
            <div class="form-group">
              <label>QUYEN</label>
              <select
                v-model="editForm.role"
                class="form-input"
              >
                <option value="CUSTOMER">
                  CUSTOMER
                </option>
                <option value="ADMIN">
                  ADMIN
                </option>
                <option value="BOTH">
                  BOTH
                </option>
              </select>
            </div>
            <div class="form-group">
              <label>TRẠNG THÁI</label>
              <select
                v-model="editForm.active"
                class="form-input"
              >
                <option value="true">
                  Hoạt động
                </option>
                <option value="false">
                  Khóa
                </option>
              </select>
            </div>
            <div
              v-if="editingUser?.provider === 'LOCAL'"
              class="form-group"
            >
              <label>MẬT KHẨU MỚI (Để trống nếu không đổi)</label>
              <input
                v-model="editForm.password"
                type="password"
                class="form-input"
                placeholder="Nhập mật khẩu mới..."
                minlength="8"
              >
            </div>
            <div
              v-if="editingUser?.provider === 'LOCAL' && editForm.password"
              class="form-group"
            >
              <label>XÁC NHẬN MẬT KHẨU</label>
              <input
                v-model="editForm.confirmPassword"
                type="password"
                class="form-input"
                placeholder="Nhập lại mật khẩu mới..."
                minlength="8"
              >
            </div>
            <div class="form-actions">
              <button
                type="button"
                class="btn-secondary"
                @click="closeEditModal"
              >
                HUY
              </button>
              <button
                type="submit"
                :disabled="isEditSubmitting"
                class="btn-primary"
              >
                {{ isEditSubmitting ? 'ĐANG LƯU...' : 'LƯU' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>

    <!-- Loyalty Progress & Adjustment Modal -->
    <div
      v-if="showLoyaltyModal"
      class="modal-overlay"
      @click.self="closeLoyaltyModal"
    >
      <div class="modal-content loyalty-modal">
        <div class="modal-header">
          <h2>Chi Tiết Khách Hàng Thân Thiết</h2>
          <button
            class="btn-close"
            @click="closeLoyaltyModal"
          >
            &times;
          </button>
        </div>
        <div
          v-if="loyaltyData"
          class="modal-body"
        >
          <div class="loyalty-stats">
            <div class="stat-card">
              <span class="stat-label">Hạng hiện tại</span>
              <span class="stat-value">{{ loyaltyData.currentTierName }} ({{ loyaltyData.currentTierCode }})</span>
            </div>
            <div class="stat-card">
              <span class="stat-label">Điểm hiện tại</span>
              <span class="stat-value">{{ loyaltyData.currentPoints }} điểm</span>
            </div>
          </div>
          
          <div
            v-if="loyaltyData.nextTierCode"
            class="progress-section"
          >
            <h3>Tiến trình lên hạng: {{ loyaltyData.nextTierName }}</h3>
            <p>
              Cần thêm: 
              <strong>{{ formatCurrency(loyaltyData.spentRemainingToNextTier) }}</strong> chi tiêu
              hoặc <strong>{{ loyaltyData.pointsRemainingToNextTier }}</strong> điểm
            </p>
          </div>
          <div
            v-else
            class="progress-section"
          >
            <h3>Khách hàng đã đạt hạng cao nhất!</h3>
          </div>

          <hr class="divider">

          <div class="adjust-points-section">
            <h3 style="margin-bottom: 16px;">
              Cộng/Trừ Điểm Thủ Công
            </h3>
            <form
              style="padding: 0;"
              @submit.prevent="submitAdjustPoints"
            >
              <div
                class="form-row loyalty-form-row"
                style="display: flex; gap: 16px; align-items: flex-start; margin-bottom: 16px;"
              >
                <div
                  class="form-group"
                  style="flex: 1;"
                >
                  <label>Số điểm (nhập số âm để trừ)</label>
                  <input
                    v-model.number="adjustForm.points"
                    type="number"
                    required
                    class="form-input"
                  >
                </div>
                <div
                  class="form-group"
                  style="flex: 2;"
                >
                  <label>Lý do (bắt buộc)</label>
                  <input
                    v-model="adjustForm.note"
                    type="text"
                    required
                    placeholder="VD: Đền bù đơn hàng..."
                    class="form-input"
                  >
                </div>
              </div>
              <div
                class="form-actions"
                style="margin-top: 16px;"
              >
                <button
                  type="submit"
                  class="btn-primary"
                  :disabled="isAdjusting"
                  style="width: 100%;"
                >
                  {{ isAdjusting ? 'Đang xử lý...' : 'Thực Hiện' }}
                </button>
              </div>
            </form>
          </div>

          <hr class="divider">

          <div class="adjust-tier-section">
            <h3 style="margin-bottom: 16px;">
              Điều Chỉnh Hạng Thủ Công
            </h3>
            <form
              style="padding: 0;"
              @submit.prevent="submitUpdateTier"
            >
              <div
                class="form-row loyalty-form-row"
                style="display: flex; gap: 16px; align-items: flex-start; margin-bottom: 16px;"
              >
                <div
                  class="form-group"
                  style="flex: 1;"
                >
                  <label>Hạng Mới</label>
                  <select
                    v-model="tierForm.newTierCode"
                    required
                    class="form-input"
                  >
                    <option
                      value=""
                      disabled
                    >
                      -- Chọn Hạng --
                    </option>
                    <option value="THUONG">
                      Khách hàng thường (THUONG)
                    </option>
                    <option value="VIP">
                      Khách hàng VIP (VIP)
                    </option>
                  </select>
                </div>
                <div
                  class="form-group"
                  style="flex: 2;"
                >
                  <label>Lý do (bắt buộc)</label>
                  <input
                    v-model="tierForm.note"
                    type="text"
                    required
                    placeholder="VD: Đặc cách thăng hạng..."
                    class="form-input"
                  >
                </div>
              </div>
              <div
                class="form-actions"
                style="margin-top: 16px;"
              >
                <button
                  type="submit"
                  class="btn-primary"
                  :disabled="isUpdatingTier"
                  style="width: 100%; background: #333;"
                >
                  {{ isUpdatingTier ? 'Đang xử lý...' : 'Cập Nhật Hạng' }}
                </button>
              </div>
            </form>
          </div>
        </div>
        <div
          v-else
          class="modal-body"
        >
          <p>Đang tải dữ liệu...</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useToast } from 'vue-toastification'
import adminService from '@/services/admin.service'
import { loyaltyAdminService, type UserLoyaltyProgress } from '@/services/loyaltyAdminService'

const toast = useToast()
const searchQuery = ref('')
const currentPage = ref(1)
const itemsPerPage = 10
const isLoading = ref(false)
const isSubmitting = ref(false)
const isEditSubmitting = ref(false)
const allUsers = ref<any[]>([])
const currentUserId = ref<number | null>(null)

// Form for adding new user
const formData = ref({
  email: '',
  username: '',
  fullName: '',
  password: '',
  role: 'CUSTOMER' as 'CUSTOMER' | 'ADMIN' | 'BOTH'
})

// Edit modal state
const showEditModal = ref(false)
const editingUser = ref<any>(null)
const editForm = ref({
  email: '',
  fullName: '',
  role: 'CUSTOMER',
  active: 'true',
  password: '',
  confirmPassword: ''
})

// Loyalty Modal State
const showLoyaltyModal = ref(false)
const loyaltyData = ref<UserLoyaltyProgress | null>(null)
const loyaltyUser = ref<any>(null)
const adjustForm = ref({ points: 0, note: '' })
const tierForm = ref({ newTierCode: '', note: '' })
const isAdjusting = ref(false)
const isUpdatingTier = ref(false)

const formatCurrency = (val: number | undefined) => {
  if (val === undefined || val === null) return '0 đ'
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val)
}

const fetchUsers = async () => {
  isLoading.value = true
  try {
    const items = await adminService.getUsers()
    if (Array.isArray(items)) {
      allUsers.value = items.map((u: any) => ({
        id: u.id,
        idDisplay: '#' + String(u.id).padStart(3, '0'),
        email: u.email || '',
        fullName: u.fullName || '',
        role: u.role || 'CUSTOMER',
        roleClass: getRoleClass(u.role),
        isActive: u.active !== false,
        provider: u.provider || 'LOCAL',
        providerClass: (u.provider || 'LOCAL').toLowerCase(),
        tierCode: u.tierCode || 'THUONG'
      }))
    }
  } catch (error: any) {
    console.error('Failed to fetch users:', error)
    toast.error('Loi khi tai danh sach nguoi dung')
    allUsers.value = []
  } finally {
    isLoading.value = false
  }
}

const getRoleClass = (role: any) => {
  switch (role) {
    case 'ADMIN': return 'admin'
    case 'BOTH': return 'both'
    default: return 'customer'
  }
}

const cycleRole = async (user: any) => {
  if (user.provider === 'GOOGLE') return
  const roles = ['CUSTOMER', 'ADMIN', 'BOTH']
  const currentIdx = roles.indexOf(user.role)
  const nextRole = roles[(currentIdx + 1) % roles.length]

  try {
    await adminService.updateUser(user.id, { role: nextRole })
    user.role = nextRole
    user.roleClass = getRoleClass(nextRole)
    toast.success('Doi quyen thanh cong')
  } catch (error: any) {
    toast.error('Doi quyen that bai: ' + (error.response?.data?.message || error.response?.data?.error || error.message))
  }
}

const toggleUserActive = async (user: any) => {
  try {
    await adminService.toggleUserActive(user.id)
    user.isActive = !user.isActive
    toast.success('Cap nhat trang thai thanh cong')
  } catch (error: any) {
    toast.error('Cap nhat trang thai that bai: ' + (error.response?.data?.message || error.response?.data?.error || error.message))
  }
}

const editUser = (user: any) => {
  editingUser.value = user
  editForm.value = {
    email: user.email,
    fullName: user.fullName,
    role: user.role,
    active: user.isActive ? 'true' : 'false',
    password: '',
    confirmPassword: ''
  }
  showEditModal.value = true
}

const closeEditModal = () => {
  showEditModal.value = false
  editingUser.value = null
}

const submitEdit = async () => {
  if (!editingUser.value) return
  if (editForm.value.password && editForm.value.password !== editForm.value.confirmPassword) {
    toast.error('Mật khẩu xác nhận không khớp!')
    return
  }
  isEditSubmitting.value = true
  try {
    const isActiveBoolean = editForm.value.active === 'true'
    const payload: any = {
      fullName: editForm.value.fullName,
      role: editForm.value.role,
      active: isActiveBoolean
    }
    if (editForm.value.password) {
      payload.password = editForm.value.password
    }
    await adminService.updateUser(editingUser.value.id, payload)
    // Update local data
    editingUser.value.fullName = editForm.value.fullName
    editingUser.value.role = editForm.value.role
    editingUser.value.roleClass = getRoleClass(editForm.value.role)
    editingUser.value.isActive = isActiveBoolean
    toast.success('Cap nhat nguoi dung thanh cong')
    closeEditModal()
  } catch (error: any) {
    toast.error('Cap nhat that bai: ' + (error.response?.data?.message || error.response?.data?.error || error.message))
  } finally {
    isEditSubmitting.value = false
  }
}

const deleteUser = async (user: any) => {
  if (user.id === currentUserId.value) {
    toast.error('Khong the xoa tai khoan cua chinh minh')
    return
  }
  if (!confirm(`Ban co chac muon xoa ${user.email}? Hanh dong nay khong the hoan tac.`)) return
  try {
    await adminService.deleteUser(user.id)
    toast.success('Xoa nguoi dung thanh cong')
    // Remove from local list
    const idx = allUsers.value.findIndex(u => u.id === user.id)
    if (idx >= 0) allUsers.value.splice(idx, 1)
  } catch (error: any) {
    toast.error('Xoa that bai: ' + (error.response?.data?.message || error.response?.data?.error || error.message))
  }
}

const handleSubmit = async () => {
  isSubmitting.value = true
  try {
    await adminService.createUser({
      email: formData.value.email,
      username: formData.value.username,
      fullName: formData.value.fullName,
      password: formData.value.password,
      role: formData.value.role
    })
    toast.success('Tao nguoi dung thanh cong!')
    resetForm()
    fetchUsers()
  } catch (error: any) {
    toast.error('Tao nguoi dung that bai: ' + (error.response?.data?.message || error.response?.data?.error || error.message))
  } finally {
    isSubmitting.value = false
  }
}

const resetForm = () => {
  formData.value = {
    email: '',
    username: '',
    fullName: '',
    password: '',
    role: 'CUSTOMER'
  }
}

// Loyalty Modal Methods
const openLoyaltyModal = async (user: any) => {
  loyaltyUser.value = user
  loyaltyData.value = null
  adjustForm.value = { points: 0, note: '' }
  tierForm.value = { newTierCode: '', note: '' }
  showLoyaltyModal.value = true
  
  try {
    const res = await loyaltyAdminService.getLoyaltyProgress(user.id)
    if (res.success) {
      loyaltyData.value = res.data
    }
  } catch (error) {
    toast.error('Lỗi khi tải thông tin loyalty')
  }
}

const closeLoyaltyModal = () => {
  showLoyaltyModal.value = false
  loyaltyUser.value = null
}

const submitAdjustPoints = async () => {
  if (!loyaltyUser.value) return
  if (adjustForm.value.points === 0) {
    toast.error('Số điểm điều chỉnh phải khác 0')
    return
  }
  isAdjusting.value = true
  try {
    await loyaltyAdminService.adjustPoints(loyaltyUser.value.id, adjustForm.value.points, adjustForm.value.note)
    toast.success('Điều chỉnh điểm thành công')
    // Refresh data
    const res = await loyaltyAdminService.getLoyaltyProgress(loyaltyUser.value.id)
    if (res.success) {
      loyaltyData.value = res.data
    }
    fetchUsers() // To update tierCode in the main list if changed
    adjustForm.value = { points: 0, note: '' }
  } catch (error: any) {
    toast.error('Lỗi khi điều chỉnh điểm: ' + (error.response?.data?.message || ''))
  } finally {
    isAdjusting.value = false
  }
}

const submitUpdateTier = async () => {
  if (!loyaltyUser.value) return
  if (!tierForm.value.newTierCode) {
    toast.error('Vui lòng chọn hạng mới')
    return
  }
  if (tierForm.value.newTierCode === loyaltyData.value?.currentTierCode) {
    toast.error('Người dùng đang ở hạng này rồi')
    return
  }
  isUpdatingTier.value = true
  try {
    await loyaltyAdminService.updateTier(loyaltyUser.value.id, tierForm.value.newTierCode, tierForm.value.note)
    toast.success('Cập nhật hạng thành công')
    // Refresh data
    const res = await loyaltyAdminService.getLoyaltyProgress(loyaltyUser.value.id)
    if (res.success) {
      loyaltyData.value = res.data
    }
    fetchUsers() // To update tierCode in the main list
    tierForm.value = { newTierCode: '', note: '' }
  } catch (error: any) {
    toast.error('Lỗi khi cập nhật hạng: ' + (error.response?.data?.message || error.response?.data?.error || error.message))
  } finally {
    isUpdatingTier.value = false
  }
}

onMounted(() => {
  fetchUsers()
  // Get current user ID from session
  const stored = localStorage.getItem('auth')
  if (stored) {
    try {
      const auth = JSON.parse(stored)
      currentUserId.value = auth.user?.id || null
    } catch { /* ignore invalid JSON */ }
  }
})

// Computed
const totalUsers = computed(() => allUsers.value.length)

const filteredUsers = computed(() => {
  if (!searchQuery.value) return allUsers.value
  const query = searchQuery.value.toLowerCase()
  return allUsers.value.filter(
    user => user.email.toLowerCase().includes(query) || user.fullName.toLowerCase().includes(query)
  )
})

const totalPages = computed(() => Math.ceil(filteredUsers.value.length / itemsPerPage))

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  return filteredUsers.value.slice(start, end)
})

const startIndex = computed(() => (currentPage.value - 1) * itemsPerPage)
const endIndex = computed(() => startIndex.value + itemsPerPage)

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

const onSearch = () => {
  currentPage.value = 1
}
</script>

<style scoped>
.users-manager {
  display: flex;
  min-height: 100vh;
  background: #F9F9F9;
}

/* Main Content */
.main-content {
  padding: 32px 24px;
  width: 100%;
  box-sizing: border-box;
}

/* User List Section */
.user-list {
  background: #FFFFFF;
  border: 1px solid #E8E8E8;
  box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.05);
  border-radius: 12px;
  margin-bottom: 24px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  background: #FFFFFF;
  border-radius: 12px 12px 0 0;
  border-bottom: 1px solid #E8E8E8;
}

.list-header h2 {
  font-family: 'Geist', sans-serif;
  font-size: 20px;
  font-weight: 600;
  color: #000000;
  margin: 0 0 4px 0;
  line-height: 28px;
}

.user-count {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #848484;
  margin: 0;
}

.search-container {
  position: relative;
  width: 240px;
}

.search-input {
  width: 100%;
  padding: 8px 14px 9px 36px;
  background: #F9F9F9;
  border: 1px solid #E8E8E8;
  border-radius: 9999px;
  color: #000000;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  outline: none;
  transition: all 0.2s;
  box-sizing: border-box;
}

.search-input::placeholder {
  color: #848484;
}

.search-input:focus {
  background: #FFFFFF;
  border-color: #000000;
}

.search-icon {
  position: absolute;
  left: 10px;
  top: 50%;
  transform: translateY(-50%);
  width: 16px;
  height: 16px;
  color: #848484;
}

.search-icon svg {
  width: 100%;
  height: 100%;
}

.table-container {
  overflow-x: auto;
}

.users-table {
  width: 100%;
  border-collapse: collapse;
}

.users-table th {
  font-family: 'Geist', sans-serif;
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  color: #4C4546;
  text-align: left;
  padding: 12px 16px;
  background: #F3F3F4;
  border-bottom: 1px solid #E8E8E8;
}

.users-table td {
  padding: 12px 16px;
  border-top: 1px solid #E8E8E8;
  vertical-align: middle;
}

.users-table tr:first-child td {
  border-top: none;
}

.loading-cell,
.empty-cell {
  padding: 32px 16px;
  text-align: center;
  color: #6B7280;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
}

.col-id {
  width: 60px;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  color: #5E5F5C;
}

.col-email {
  width: 320px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-details {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-email {
  font-family: 'Geist', sans-serif;
  font-size: 16px;
  font-weight: 400;
  color: #000000;
}

.user-name {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #848484;
}

/* Role Badge */
.role-badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 9999px;
  font-family: 'Geist', sans-serif;
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  cursor: pointer;
  transition: all 0.2s;
  border: none;
}

.role-badge.customer {
  background: #DBEAFE;
  color: #1E40AF;
}

.role-badge.admin {
  background: #FEE2E2;
  color: #991B1B;
}

.role-badge.both {
  background: #000000;
  color: #FFFFFF;
}

.role-badge:hover {
  opacity: 0.8;
}

.col-role {
  width: 130px;
}

.col-tier {
  width: 140px;
}

.tier-info {
  display: flex;
  align-items: center;
  gap: 8px;
}

.badge {
  background: #f1c40f;
  color: #fff;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 11px;
  font-weight: bold;
}

.loyalty-btn {
  background: transparent;
  border: 1px solid #f1c40f;
  color: #f1c40f;
  width: 28px;
  height: 28px;
}
.loyalty-btn:hover {
  background: #f1c40f;
  color: white;
}

/* Status Toggle */
.col-status {
  width: 144px;
}

.status-toggle {
  display: inline-flex;
  align-items: center;
  cursor: pointer;
}

.toggle-track {
  width: 45px;
  height: 25px;
  background: #CCCCCC;
  border-radius: 24px;
  position: relative;
  transition: background 0.2s;
}

.status-toggle.active .toggle-track {
  background: #000000;
}

.toggle-thumb {
  position: absolute;
  width: 18px;
  height: 18px;
  background: #FFFFFF;
  border-radius: 9px;
  top: 3px;
  left: 3px;
  transition: transform 0.2s;
}

.status-toggle.active .toggle-thumb {
  transform: translateX(20px);
}

/* Provider Badge */
.col-provider {
  width: 128px;
}

.provider-badge {
  display: inline-block;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
  text-transform: uppercase;
}

.provider-badge.local {
  color: #5E5F5C;
}

.provider-badge.google {
  color: #5E5F5C;
}

/* Actions */
.col-actions {
  width: 161px;
  text-align: right;
}

.action-buttons {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

.action-btn {
  width: 32px;
  height: 32px;
  border-radius: 9999px;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}

.edit-btn {
  background: transparent;
  border: 1px solid #E2E2E2;
  color: #5E5F5C;
}

.edit-btn:hover {
  background: #F3F3F4;
  border-color: #000000;
}

.delete-btn {
  background: transparent;
  border: 1px solid #E2E2E2;
  color: #5E5F5C;
}

.delete-btn:hover {
  background: #FFDAD6;
  border-color: #93000A;
  color: #93000A;
}

/* Loyalty Modal Styles */
.loyalty-modal {
  width: 600px;
  max-width: 90vw;
}

.loyalty-stats {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
}

.stat-card {
  flex: 1;
  background: #f8f9fa;
  padding: 15px;
  border-radius: 8px;
  border: 1px solid #e9ecef;
  display: flex;
  flex-direction: column;
}

.stat-label {
  font-size: 12px;
  color: #6c757d;
  text-transform: uppercase;
}

.stat-value {
  font-size: 18px;
  font-weight: bold;
  color: #2c3e50;
  margin-top: 5px;
}

.progress-section {
  background: #e3f2fd;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.progress-section h3 {
  margin: 0 0 10px 0;
  font-size: 16px;
  color: #1976d2;
}

.divider {
  border: none;
  border-top: 1px solid #eee;
  margin: 20px 0;
}

.adjust-points-section h3 {
  font-size: 16px;
  margin-bottom: 15px;
}

.flex-2 {
  flex: 2;
}

/* Pagination */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  border-top: 1px solid #E8E8E8;
}

.showing-text {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #848484;
}

.pagination-controls {
  display: flex;
  gap: 6px;
}

.page-btn {
  min-width: 32px;
  height: 32px;
  padding: 6px 12px;
  border: 1px solid #E2E2E2;
  border-radius: 4px;
  background: #FFFFFF;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 400;
  color: #1A1C1C;
  cursor: pointer;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  border-color: #000000;
}

.page-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.page-btn.active {
  background: #000000;
  color: #FFFFFF;
  border-color: #000000;
}

/* Add User Form */
.add-user-form {
  background: #FFFFFF;
  border: 1px solid #E8E8E8;
  box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.05);
  border-radius: 12px;
  padding-bottom: 24px;
}

.form-header {
  padding: 16px 24px;
  background: #FFFFFF;
  border-radius: 12px 12px 0 0;
  border-bottom: 1px solid #E8E8E8;
}

.form-header h2 {
  font-family: 'Geist', sans-serif;
  font-size: 20px;
  font-weight: 600;
  color: #000000;
  margin: 0 0 4px 0;
  line-height: 28px;
}

.form-subtitle {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #848484;
  margin: 0;
}

form {
  padding: 24px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  margin-bottom: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-group label {
  font-family: 'Geist', sans-serif;
  font-size: 11px;
  font-weight: 400;
  text-transform: uppercase;
  color: #4C4546;
}

.form-input {
  padding: 10px 12px;
  background: #F3F3F4;
  border: 1px solid transparent;
  border-radius: 8px;
  font-family: 'Geist', sans-serif;
  font-size: 16px;
  color: #6B7280;
  outline: none;
  transition: all 0.2s;
  box-sizing: border-box;
}

.form-input:focus {
  background: #FFFFFF;
  border-color: #000000;
}

.form-input::placeholder {
  color: #9CA3AF;
}

.form-input:disabled {
  opacity: 0.6;
  cursor: not-allowed;
}

.role-group {
  margin-bottom: 24px;
}

.role-options {
  display: flex;
  gap: 8px;
}

.role-btn {
  padding: 6px 14px;
  border: 1px solid #E2E2E2;
  border-radius: 9999px;
  background: #FFFFFF;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 400;
  color: #1A1C1C;
  cursor: pointer;
  transition: all 0.2s;
}

.role-btn:hover {
  border-color: #000000;
}

.role-btn.active {
  background: #000000;
  color: #FFFFFF;
  border-color: #000000;
}

.form-actions {
  display: flex;
  gap: 12px;
}

.submit-btn {
  padding: 12px 0;
  width: 180px;
  background: #000000;
  border: none;
  border-radius: 8px;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1.8px;
  text-transform: uppercase;
  color: #FFFFFF;
  cursor: pointer;
  transition: all 0.2s;
}

.submit-btn:hover:not(:disabled) {
  background: #333333;
}

.submit-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.reset-btn {
  padding: 12px 0;
  width: 180px;
  background: transparent;
  border: 2px solid #000000;
  border-radius: 8px;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 400;
  letter-spacing: 1.8px;
  text-transform: uppercase;
  color: #000000;
  cursor: pointer;
  transition: all 0.2s;
}

.reset-btn:hover {
  background: #F3F3F4;
}

/* Edit Modal */
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
  max-width: 500px;
  border-radius: 12px;
  overflow: hidden;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #E8E8E8;
  background: #FFFFFF;
}

.modal-header h2 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #000000;
}

.btn-close {
  background: none;
  border: none;
  font-size: 28px;
  cursor: pointer;
  color: #000000;
  line-height: 1;
}

.btn-close:hover {
  color: #666666;
}

.modal-body {
  padding: 24px;
}

.modal-body .form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 16px;
  margin-top: 32px;
}

.btn-primary {
  padding: 12px 24px;
  width: auto;
  min-width: 140px;
  background: #000000;
  border: none;
  border-radius: 8px;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1.8px;
  text-transform: uppercase;
  color: #FFFFFF;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-primary:hover:not(:disabled) {
  background: #333333;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  padding: 12px 24px;
  width: auto;
  min-width: 140px;
  background: transparent;
  border: 2px solid #000000;
  border-radius: 8px;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 400;
  letter-spacing: 1.8px;
  text-transform: uppercase;
  color: #000000;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-secondary:hover {
  background: #F3F3F4;
}
</style>
