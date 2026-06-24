<template>
  <div class="users-manager">
    <!-- Sidebar Navigation (embedded for standalone, hidden when using AdminLayout) -->
    <nav class="sidebar">
      <div class="logo-section">
        <img src="@/assets/logo.png" alt="Logo" class="logo" />
      </div>
      <nav class="nav-menu">
        <router-link to="/admin/users" class="nav-item active">
          <span class="nav-icon">👥</span>
          <span class="nav-text">Quản Lý Người Dùng</span>
        </router-link>
        <router-link to="/admin/orders" class="nav-item">
          <span class="nav-icon">📦</span>
          <span class="nav-text">Quản Lý Đơn Hàng</span>
        </router-link>
        <router-link to="/admin/products" class="nav-item">
          <span class="nav-icon">🏷️</span>
          <span class="nav-text">Quản Lý Sản Phẩm</span>
        </router-link>
        <router-link to="/admin/statistics" class="nav-item">
          <span class="nav-icon">📊</span>
          <span class="nav-text">Quản Lý Thống Kê</span>
        </router-link>
      </nav>
      <div class="footer-menu">
        <router-link to="/help" class="nav-item">
          <span class="nav-icon">❓</span>
          <span class="nav-text">HELP CENTER</span>
        </router-link>
        <button @click="handleLogout" class="nav-item logout-btn">
          <span class="nav-icon">🚪</span>
          <span class="nav-text">LOGOUT</span>
        </button>
      </div>
    </nav>

    <!-- Main Content -->
    <main class="main-content">
      <!-- User List Section -->
      <section class="user-list">
        <!-- Header -->
        <div class="list-header">
          <div class="header-info">
            <h2>Danh sách người dùng</h2>
            <p class="user-count">Tổng cộng {{ totalUsers }} thành viên</p>
          </div>
          <div class="search-container">
            <input
              v-model="searchQuery"
              type="text"
              placeholder="Tìm theo email hoặc tên..."
              class="search-input"
            />
            <div class="search-icon">🔍</div>
          </div>
        </div>

        <!-- Table -->
        <div class="table-container">
          <table class="users-table">
            <thead>
              <tr>
                <th class="col-checkbox">
                  <input type="checkbox" v-model="selectAll" @change="toggleSelectAll" />
                </th>
                <th class="col-email">EMAIL / HỌ TÊN</th>
                <th class="col-role">QUYỀN</th>
                <th class="col-status">TRẠNG THÁI</th>
                <th class="col-provider">PROVIDER</th>
                <th class="col-actions">THAO TÁC</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="user in paginatedUsers" :key="user.id">
                <td class="col-checkbox">
                  <input type="checkbox" v-model="selectedUsers" :value="user.id" />
                </td>
                <td class="col-email">
                  <div class="user-info">
                    <span class="user-id">{{ user.idDisplay }}</span>
                    <div class="user-details">
                      <span class="user-email">{{ user.email }}</span>
                      <span class="user-name">{{ user.fullName }}</span>
                    </div>
                  </div>
                </td>
                <td class="col-role">
                  <span :class="['role-badge', user.roleClass]">
                    {{ user.role }}
                  </span>
                </td>
                <td class="col-status">
                  <span :class="['status-toggle', user.statusToggle]">
                    <input type="checkbox" :checked="user.isActive" disabled />
                    <div class="toggle-track">
                      <div class="toggle-thumb"></div>
                    </div>
                  </span>
                </td>
                <td class="col-provider">
                  <span class="provider-badge" :class="user.providerClass">
                    {{ user.provider }}
                  </span>
                </td>
                <td class="col-actions">
                  <div class="action-buttons">
                    <button class="action-btn edit-btn" @click="editUser(user)">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor">
                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                      </svg>
                    </button>
                    <button class="action-btn delete-btn" @click="deleteUser(user)">
                      <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor">
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
          <span class="showing-text">Showing 1 to 10 of {{ totalUsers }} users</span>
          <div class="pagination-controls">
            <button class="page-btn" :disabled="currentPage === 1" @click="currentPage--">
              Previous
            </button>
            <button
              v-for="page in visiblePages"
              :key="page"
              class="page-btn"
              :class="{ active: currentPage === page }"
              @click="currentPage = page"
            >
              {{ page }}
            </button>
            <button
              class="page-btn"
              :disabled="currentPage === totalPages"
              @click="currentPage++"
            >
              Next
            </button>
          </div>
        </div>
      </section>

      <!-- Add User Form -->
      <section class="add-user-form">
        <div class="form-header">
          <h2>Thêm người dùng mới</h2>
          <p class="form-subtitle">Điền thông tin để đăng ký tài khoản mới</p>
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
              />
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
              />
            </div>

            <!-- Full Name -->
            <div class="form-group">
              <label>FULL NAME</label>
              <input
                v-model="formData.fullName"
                type="text"
                placeholder="Họ và tên"
                class="form-input"
                required
              />
            </div>

            <!-- Password -->
            <div class="form-group">
              <label>PASSWORD</label>
              <input
                v-model="formData.password"
                type="password"
                placeholder="••••••••"
                class="form-input"
                required
              />
            </div>
          </div>

          <!-- Role Selection -->
          <div class="form-group">
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
            <button type="submit" class="submit-btn">
              LƯU
            </button>
            <button type="button" class="reset-btn" @click="resetForm">
              LÀM MỚI
            </button>
          </div>
        </form>
      </section>
    </main>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

// Search and pagination
const searchQuery = ref('')
const currentPage = ref(1)
const itemsPerPage = 10
const selectAll = ref(false)
const selectedUsers = ref([])

// Form data
const formData = ref({
  email: '',
  username: '',
  fullName: '',
  password: '',
  role: 'CUSTOMER'
})

// Mock users data
const users = ref([
  {
    id: 1,
    idDisplay: '#001',
    email: 'admin@thexuong.com',
    fullName: 'Quản Trị Viên',
    role: 'ADMIN',
    roleClass: 'admin',
    isActive: true,
    provider: 'LOCAL',
    providerClass: 'local'
  },
  {
    id: 2,
    idDisplay: '#002',
    email: 'khachhang@thexuong.com',
    fullName: 'Khách Hàng VIP',
    role: 'CUSTOMER',
    roleClass: 'customer',
    isActive: true,
    provider: 'GOOGLE',
    providerClass: 'google'
  },
  {
    id: 3,
    idDisplay: '#003',
    email: 'both@thexuong.com',
    fullName: 'Người Dùng Test',
    role: 'BOTH',
    roleClass: 'both',
    isActive: true,
    provider: 'LOCAL',
    providerClass: 'local'
  }
])

// Computed
const totalUsers = computed(() => users.value.length)

const filteredUsers = computed(() => {
  if (!searchQuery.value) return users.value
  const query = searchQuery.value.toLowerCase()
  return users.value.filter(
    user =>
      user.email.toLowerCase().includes(query) ||
      user.fullName.toLowerCase().includes(query)
  )
})

const totalPages = computed(() =>
  Math.ceil(filteredUsers.value.length / itemsPerPage)
)

const paginatedUsers = computed(() => {
  const start = (currentPage.value - 1) * itemsPerPage
  const end = start + itemsPerPage
  return filteredUsers.value.slice(start, end)
})

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

// Methods
const toggleSelectAll = () => {
  if (selectAll.value) {
    selectedUsers.value = paginatedUsers.value.map(user => user.id)
  } else {
    selectedUsers.value = []
  }
}

const editUser = (user) => {
  // Implement edit functionality
  console.log('Edit user:', user)
}

const deleteUser = (user) => {
  // Implement delete functionality
  if (confirm(`Are you sure you want to delete ${user.email}?`)) {
    users.value = users.value.filter(u => u.id !== user.id)
  }
}

const handleSubmit = () => {
  // Implement form submission
  console.log('Submit form:', formData.value)
  alert('User creation would be implemented here!')
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

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.users-manager {
  display: flex;
  min-height: 100vh;
  background: #F9F9F9;
}

/* Sidebar */
.sidebar {
  width: 256px;
  background: #FFFFFF;
  border-right: 1px solid #000000;
  display: flex;
  flex-direction: column;
  padding-top: 140px;
  position: fixed;
  height: 100vh;
}

.logo-section {
  position: absolute;
  top: 32px;
  left: 0;
  width: 100%;
  height: 92px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo {
  max-width: 150px;
  height: auto;
}

.nav-menu {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  text-decoration: none;
  color: #5E5F5C;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1.8px;
  cursor: pointer;
  transition: all 0.2s;
  border-left: 4px solid transparent;
  width: 100%;
  text-align: left;
  background: transparent;
  border-top: none;
  border-right: none;
  border-bottom: none;
}

.nav-item:hover {
  background: #F9F9F9;
}

.nav-item.active {
  background: #000000;
  color: #FFFFFF;
}

.nav-icon {
  width: 20px;
  height: 20px;
  margin-right: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.footer-menu {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  border-top: 1px solid #E8E8E8;
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.logout-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #5E5F5C;
  font-family: inherit;
}

.logout-btn:hover {
  background: #F9F9F9;
}

/* Main Content */
.main-content {
  margin-left: 256px;
  padding: 120px 20px 20px;
  width: calc(100% - 256px);
  max-width: 1280px;
}

/* User List Section */
.user-list {
  background: #FFFFFF;
  border: 1px solid #E8E8E8;
  box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.05);
  border-radius: 12px;
  margin-bottom: 30px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  background: #1C1B1B;
  border-radius: 12px 12px 0 0;
}

.list-header h2 {
  font-family: 'Geist', sans-serif;
  font-size: 20px;
  font-weight: 400;
  color: #FFFFFF;
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
  width: 256px;
}

.search-input {
  width: 100%;
  padding: 9px 16px 10px 40px;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 9999px;
  color: #FFFFFF;
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  outline: none;
  transition: all 0.2s;
}

.search-input::placeholder {
  color: #848484;
}

.search-input:focus {
  background: rgba(255, 255, 255, 0.15);
  border-color: rgba(255, 255, 255, 0.3);
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
  opacity: 0.7;
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
  padding: 16px 24px;
  background: #F3F3F4;
  border-bottom: 1px solid #E8E8E8;
}

.users-table td {
  padding: 16px 24px;
  border-top: 1px solid #E8E8E8;
  vertical-align: middle;
}

.users-table tr:first-child td {
  border-top: none;
}

.col-checkbox {
  width: 60px;
  text-align: center;
}

.col-email {
  width: 344px;
}

.col-role {
  width: 168px;
}

.col-status {
  width: 144px;
}

.col-provider {
  width: 128px;
}

.col-actions {
  width: 161px;
  text-align: right;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 24px;
}

.user-id {
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  color: #5E5F5C;
  width: 40px;
}

.user-details {
  display: flex;
  flex-direction: column;
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

.role-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 9999px;
  font-family: 'Geist', sans-serif;
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
}

.role-badge.admin {
  background: #FFDAD6;
  color: #93000A;
}

.role-badge.customer {
  background: #B9E2F6;
  color: #646562;
}

.role-badge.both {
  background: #000000;
  color: #FFFFFF;
}

.status-toggle {
  display: inline-flex;
  align-items: center;
  gap: 8px;
}

.status-toggle input {
  display: none;
}

.toggle-track {
  width: 45px;
  height: 25px;
  background: #CCCCCC;
  border-radius: 24px;
  position: relative;
  transition: background 0.2s;
}

.status-toggle[data-active="true"] .toggle-track {
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

.status-toggle[data-active="true"] .toggle-thumb {
  transform: translateX(20px);
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

/* Pagination */
.pagination {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 24px;
  border-top: 1px solid #E8E8E8;
}

.showing-text {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #848484;
}

.pagination-controls {
  display: flex;
  gap: 8px;
}

.page-btn {
  min-width: 34px;
  height: 34px;
  padding: 8px 16px;
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
  padding-bottom: 32px;
}

.form-header {
  padding: 24px;
  background: #1C1B1B;
  border-radius: 12px 12px 0 0;
}

.form-header h2 {
  font-family: 'Geist', sans-serif;
  font-size: 20px;
  font-weight: 400;
  color: #FFFFFF;
  margin: 0 0 4px 0;
  line-height: 28px;
}

.form-subtitle {
  font-family: 'Gelasio', sans-serif;
  font-size: 12px;
  color: #848484;
  margin: 0;
}

form {
  padding: 32px;
}

.form-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 10px;
  margin-bottom: 10px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-family: 'Geist', sans-serif;
  font-size: 11px;
  font-weight: 400;
  text-transform: uppercase;
  color: #4C4546;
}

.form-input {
  padding: 14px 12px;
  background: #F3F3F4;
  border: 1px solid transparent;
  border-radius: 8px;
  font-family: 'Geist', sans-serif;
  font-size: 16px;
  color: #6B7280;
  outline: none;
  transition: all 0.2s;
}

.form-input:focus {
  background: #FFFFFF;
  border-color: #000000;
}

.form-input::placeholder {
  color: #9CA3AF;
}

.role-options {
  display: flex;
  gap: 8px;
}

.role-btn {
  padding: 8px 16px;
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
  gap: 16px;
  margin-top: 32px;
}

.submit-btn {
  padding: 17.5px 0 18.5px;
  width: 213px;
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

.submit-btn:hover {
  background: #333333;
}

.reset-btn {
  padding: 16px 0;
  width: 217px;
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
</style>
