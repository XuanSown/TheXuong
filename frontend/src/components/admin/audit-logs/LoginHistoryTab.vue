<template>
  <div class="login-history-tab">
    <section class="logs-card">
      <div class="list-header">
        <div class="header-info">
          <h2>LỊCH SỬ ĐĂNG NHẬP</h2>
          <p class="logs-count">
            {{ totalElements }} bản ghi
          </p>
        </div>
        <button
          class="btn-secondary"
          :disabled="isLoading"
          @click="reload"
        >
          <span>↻</span> TẢI LẠI
        </button>
      </div>

      <div class="toolbar">
        <input
          v-model="filters.email"
          type="text"
          class="search-input"
          placeholder="Tìm email..."
          @keyup.enter="applyFilters"
        >
        <select
          v-model="filters.provider"
          class="select-input"
        >
          <option value="">
            TẤT CẢ NGUỒN
          </option>
          <option value="LOCAL">
            LOCAL
          </option>
          <option value="GOOGLE">
            GOOGLE
          </option>
        </select>
        <select
          v-model="filters.success"
          class="select-input"
        >
          <option value="">
            TẤT CẢ KẾT QUẢ
          </option>
          <option value="true">
            THÀNH CÔNG
          </option>
          <option value="false">
            THẤT BẠI
          </option>
        </select>
        <input
          v-model="filters.from"
          type="date"
          class="date-input"
          title="Từ ngày"
        >
        <input
          v-model="filters.to"
          type="date"
          class="date-input"
          title="Đến ngày"
        >
        <button
          class="btn-secondary"
          @click="applyFilters"
        >
          LỌC
        </button>
      </div>

      <div class="table-container">
        <table class="logs-table">
          <thead>
            <tr>
              <th class="col-time">
                THỜI GIAN
              </th>
              <th class="col-email">
                EMAIL
              </th>
              <th class="col-ip">
                IP
              </th>
              <th class="col-provider">
                NGUỒN
              </th>
              <th class="col-result">
                KẾT QUẢ
              </th>
              <th class="col-reason">
                LÝ DO
              </th>
              <th class="col-action">
                THAO TÁC
              </th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoading">
              <td
                colspan="7"
                class="loading-cell"
              >
                Đang tải dữ liệu...
              </td>
            </tr>
            <tr v-else-if="logs.length === 0">
              <td
                colspan="7"
                class="empty-cell"
              >
                Không có lịch sử đăng nhập nào
              </td>
            </tr>
            <tr
              v-for="log in logs"
              v-else
              :key="log.id"
            >
              <td class="col-time">
                <span class="time-text">{{ formatDate(log.createdAt) }}</span>
              </td>
              <td class="col-email">
                <span
                  class="email-text"
                  :title="log.email"
                >{{ log.email }}</span>
              </td>
              <td class="col-ip">
                <span
                  class="ip-text"
                  :title="log.ipAddress || ''"
                >{{ log.ipAddress || '—' }}</span>
              </td>
              <td class="col-provider">
                <span
                  class="provider-badge"
                  :class="log.provider === 'GOOGLE' ? 'badge-google' : 'badge-local'"
                >
                  {{ log.provider }}
                </span>
              </td>
              <td class="col-result">
                <span
                  class="result-badge"
                  :class="log.success ? 'badge-success' : 'badge-fail'"
                >
                  {{ log.success ? 'Thành công' : 'Thất bại' }}
                </span>
              </td>
              <td class="col-reason">
                <span
                  class="reason-text"
                  :title="log.failureReason || ''"
                >{{ log.failureReason || '—' }}</span>
              </td>
              <td class="col-action">
                <button
                  v-if="log.userId"
                  class="btn-lock"
                  :class="isUserActive(log.userId) ? 'lock' : 'unlock'"
                  :disabled="isLocking"
                  @click="toggleLock(log)"
                >
                  {{ isUserActive(log.userId) ? 'KHOÁ TÀI KHOẢN' : 'MỞ KHOÁ' }}
                </button>
                <span
                  v-else
                  class="action-none"
                >—</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div
        v-if="totalPages > 1"
        class="pagination"
      >
        <button
          class="btn-secondary"
          :disabled="page === 0 || isLoading"
          @click="goToPage(page - 1)"
        >
          ‹ TRƯỚC
        </button>
        <span class="page-info">Trang {{ page + 1 }} / {{ totalPages }}</span>
        <button
          class="btn-secondary"
          :disabled="page >= totalPages - 1 || isLoading"
          @click="goToPage(page + 1)"
        >
          SAU ›
        </button>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useToast } from 'vue-toastification'
import { loginHistoryAdminService } from '@/services/loginHistoryAdmin.service'
import { adminService } from '@/services/admin.service'
import type { AdminLoginHistory } from '@/types/loginHistory'

const toast = useToast()

const logs = ref<AdminLoginHistory[]>([])
const isLoading = ref(false)
const isLocking = ref(false)
const page = ref(0)
const size = ref(20)
const totalElements = ref(0)
const totalPages = ref(1)

const filters = reactive({
  email: '',
  provider: '',
  success: '',
  from: '',
  to: '',
})

const activeById = ref<Record<number, boolean>>({})

const loadUserActiveMap = async () => {
  try {
    const users = await adminService.getUsers()
    const map: Record<number, boolean> = {}
    for (const u of users as any[]) {
      map[u.id] = u.active !== false
    }
    activeById.value = map
  } catch {
    // Không chặn luồng chính nếu không tải được danh sách user
  }
}

const isUserActive = (userId: number) => activeById.value[userId] !== false

const loadLogs = async () => {
  isLoading.value = true
  try {
    const res = await loginHistoryAdminService.getHistory({
      email: filters.email || undefined,
      provider: filters.provider || undefined,
      success: filters.success === '' ? undefined : filters.success === 'true',
      from: filters.from || undefined,
      to: filters.to || undefined,
      page: page.value,
      size: size.value,
    })
    if (res.success && res.data) {
      logs.value = res.data.content
      totalElements.value = res.data.totalElements
      totalPages.value = Math.max(1, res.data.totalPages)
    }
  } catch {
    toast.error('Lỗi khi tải lịch sử đăng nhập')
  } finally {
    isLoading.value = false
  }
}

const applyFilters = () => {
  page.value = 0
  loadLogs()
}

const reload = () => {
  loadLogs()
}

const goToPage = (target: number) => {
  if (target < 0 || target >= totalPages.value) return
  page.value = target
  loadLogs()
}

const toggleLock = async (log: AdminLoginHistory) => {
  if (!log.userId) return
  const currentlyActive = isUserActive(log.userId)
  const action = currentlyActive ? 'khoá' : 'mở khoá'
  if (!window.confirm(`Bạn có chắc muốn ${action} tài khoản ${log.email}?`)) return
  isLocking.value = true
  try {
    const res = await adminService.toggleUserActive(log.userId)
    if (typeof res?.data?.active === 'boolean') {
      activeById.value[log.userId] = res.data.active
    }
    toast.success(res?.message || `Đã ${action} tài khoản thành công`)
  } catch {
    toast.error(`Không thể ${action} tài khoản`)
  } finally {
    isLocking.value = false
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return new Intl.DateTimeFormat('vi-VN', {
    hour: '2-digit', minute: '2-digit', second: '2-digit',
    day: '2-digit', month: '2-digit', year: 'numeric',
  }).format(d)
}

onMounted(() => {
  loadUserActiveMap()
  loadLogs()
})
</script>

<style scoped>
.login-history-tab {
  width: 100%;
}

.logs-card {
  background: #FFFFFF;
  border: 1px solid #E8E8E8;
  box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.05);
  border-radius: 12px;
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #E8E8E8;
}

.list-header h2 {
  font-family: 'Geist', sans-serif;
  font-size: 20px;
  font-weight: 600;
  color: #000000;
  margin: 0 0 4px 0;
}

.logs-count {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #848484;
  margin: 0;
}

.btn-secondary {
  background: #F3F3F4;
  color: #4C4546;
  border: 1px solid #E8E8E8;
  padding: 8px 16px;
  border-radius: 6px;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
  display: inline-flex;
  align-items: center;
  gap: 6px;
}

.btn-secondary:hover:not(:disabled) {
  background: #E8E8E8;
}

.btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.toolbar {
  display: flex;
  gap: 10px;
  padding: 16px 24px;
  border-bottom: 1px solid #E8E8E8;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 200px;
  padding: 8px 12px;
  border: 1px solid #E8E8E8;
  border-radius: 6px;
  font-family: 'Geist', sans-serif;
  font-size: 13px;
  color: #000000;
}

.select-input {
  width: 160px;
  padding: 8px 12px;
  border: 1px solid #E8E8E8;
  border-radius: 6px;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #000000;
  background: #FFFFFF;
}

.date-input {
  width: 150px;
  padding: 7px 10px;
  border: 1px solid #E8E8E8;
  border-radius: 6px;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #000000;
}

.table-container {
  overflow-x: auto;
}

.logs-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.logs-table th {
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

.logs-table td {
  padding: 12px 16px;
  border-top: 1px solid #E8E8E8;
  vertical-align: middle;
}

.col-time { width: 170px; }
.col-email { width: 200px; }
.col-ip { width: 130px; }
.col-provider { width: 110px; }
.col-result { width: 130px; }
.col-reason { width: auto; }
.col-action { width: 160px; }

.loading-cell,
.empty-cell {
  padding: 32px 16px;
  text-align: center;
  color: #6B7280;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
}

.time-text {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #5E5F5C;
  white-space: nowrap;
}

.email-text {
  font-family: 'Geist', sans-serif;
  font-size: 13px;
  font-weight: 600;
  color: #000000;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.ip-text {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #4C4546;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.provider-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 9999px;
  font-family: 'Geist', sans-serif;
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
}

.badge-local { background: #F3F3F4; color: #4C4546; }
.badge-google { background: #DBEAFE; color: #1E40AF; }

.result-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 9999px;
  font-family: 'Geist', sans-serif;
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
}

.badge-success { background: #E6F4EA; color: #137333; }
.badge-fail { background: #FCE8E6; color: #C5221F; }

.reason-text {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #C5221F;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.btn-lock {
  border: 1px solid #E8E8E8;
  border-radius: 6px;
  padding: 6px 12px;
  font-family: 'Geist', sans-serif;
  font-size: 11px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}

.btn-lock.lock {
  background: #FCE8E6;
  color: #C5221F;
  border-color: #F5C6C3;
}

.btn-lock.lock:hover:not(:disabled) {
  background: #FADCDA;
}

.btn-lock.unlock {
  background: #E6F4EA;
  color: #137333;
  border-color: #C4E8CF;
}

.btn-lock.unlock:hover:not(:disabled) {
  background: #D8EEDF;
}

.btn-lock:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.action-none {
  color: #B0B0B0;
  font-size: 13px;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 16px;
  padding: 16px 24px;
  border-top: 1px solid #E8E8E8;
}

.page-info {
  font-family: 'Geist', sans-serif;
  font-size: 13px;
  color: #4C4546;
}
</style>
