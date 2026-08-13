<template>
  <div class="audit-logs-manager">
    <main class="main-content">
      <section class="logs-list">
        <div class="list-header">
          <div class="header-info">
            <h2>Lịch Sử Hệ Thống (Audit Logs)</h2>
            <p class="logs-count">
              Tổng cộng {{ logs.length }} bản ghi
            </p>
          </div>
          <button class="btn-secondary" @click="loadLogs" :disabled="isLoading">
            <span class="icon">↻</span> TẢI LẠI
          </button>
        </div>

        <div class="table-container">
          <table class="logs-table">
            <thead>
              <tr>
                <th class="col-time">THỜI GIAN</th>
                <th class="col-admin">NGƯỜI THỰC HIỆN</th>
                <th class="col-module">MODULE</th>
                <th class="col-action">HÀNH ĐỘNG</th>
                <th class="col-target">ĐỐI TƯỢNG (ID)</th>
                <th class="col-details">CHI TIẾT</th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="isLoading">
                <td colspan="6" class="loading-cell">Đang tải dữ liệu...</td>
              </tr>
              <tr v-else-if="logs.length === 0">
                <td colspan="6" class="empty-cell">Không có lịch sử nào được ghi nhận</td>
              </tr>
              <tr
                v-else
                v-for="log in logs"
                :key="log.id"
              >
                <td class="col-time">
                  <span class="time">{{ formatDate(log.createdAt) }}</span>
                </td>
                <td class="col-admin">
                  <span class="admin-id">{{ log.adminId }}</span>
                </td>
                <td class="col-module">
                  <span class="badge" :class="getModuleClass(log.module)">
                    {{ log.module }}
                  </span>
                </td>
                <td class="col-action">
                  <span class="badge" :class="getActionClass(log.action)">
                    {{ log.action }}
                  </span>
                </td>
                <td class="col-target">
                  <span class="target-id">#{{ log.targetId }}</span>
                </td>
                <td class="col-details">
                  <button class="btn-detail" @click="openDetailModal(log)">
                    Xem chi tiết
                  </button>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </main>

    <!-- Detail Modal -->
    <div v-if="showModal && selectedLog" class="modal-overlay" @click.self="closeModal">
      <div class="modal-content">
        <div class="modal-header">
          <h2>Chi Tiết Thay Đổi</h2>
          <button class="btn-close" @click="closeModal">&times;</button>
        </div>
        <div class="modal-body">
          <div class="info-grid">
            <div class="info-item">
              <label>THỜI GIAN</label>
              <span>{{ formatDate(selectedLog.createdAt) }}</span>
            </div>
            <div class="info-item">
              <label>NGƯỜI THỰC HIỆN</label>
              <span>{{ selectedLog.adminId }}</span>
            </div>
            <div class="info-item">
              <label>MODULE</label>
              <span class="badge" :class="getModuleClass(selectedLog.module)">{{ selectedLog.module }}</span>
            </div>
            <div class="info-item">
              <label>HÀNH ĐỘNG</label>
              <span class="badge" :class="getActionClass(selectedLog.action)">{{ selectedLog.action }}</span>
            </div>
            <div class="info-item">
              <label>ĐỐI TƯỢNG</label>
              <span>{{ selectedLog.module }} #{{ selectedLog.targetId }}</span>
            </div>
          </div>
          
          <div v-if="selectedLog.changedFields" class="fields-section">
            <label>CÁC TRƯỜNG BỊ ẢNH HƯỞNG</label>
            <div class="changed-fields">
              {{ selectedLog.changedFields }}
            </div>
          </div>

          <div class="diff-section" v-if="selectedLog.oldValues || selectedLog.newValues">
            <div class="diff-col old-val">
              <label>DỮ LIỆU CŨ</label>
              <pre>{{ formatJson(selectedLog.oldValues) }}</pre>
            </div>
            <div class="diff-col new-val">
              <label>DỮ LIỆU MỚI</label>
              <pre>{{ formatJson(selectedLog.newValues) }}</pre>
            </div>
          </div>

          <div v-if="selectedLog.note" class="note-section">
            <label>GHI CHÚ</label>
            <p>{{ selectedLog.note }}</p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useToast } from 'vue-toastification'
import { adminAuditService, type SystemAuditLog } from '@/services/adminAudit.service'

const toast = useToast()
const logs = ref<SystemAuditLog[]>([])
const isLoading = ref(false)

const showModal = ref(false)
const selectedLog = ref<SystemAuditLog | null>(null)

const loadLogs = async () => {
  isLoading.value = true
  try {
    const res = await adminAuditService.getAuditLogs()
    if (res.success) {
      logs.value = res.data
    }
  } catch (error) {
    toast.error('Lỗi khi tải lịch sử hệ thống')
  } finally {
    isLoading.value = false
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return new Intl.DateTimeFormat('vi-VN', {
    hour: '2-digit', minute: '2-digit', second: '2-digit',
    day: '2-digit', month: '2-digit', year: 'numeric'
  }).format(d)
}

const getActionClass = (action: string) => {
  if (action === 'CREATE') return 'badge-create'
  if (action === 'UPDATE') return 'badge-update'
  if (action === 'DELETE') return 'badge-delete'
  return 'badge-default'
}

const getModuleClass = (module: string) => {
  switch (module?.toUpperCase()) {
    case 'VOUCHER': return 'badge-voucher'
    case 'PRODUCT': return 'badge-product'
    case 'TIER': return 'badge-tier'
    case 'USER': return 'badge-user'
    case 'ORDER': return 'badge-order'
    default: return 'badge-default'
  }
}

const formatJson = (val: string | null) => {
  if (!val) return 'Không có dữ liệu'
  try {
    return JSON.stringify(JSON.parse(val), null, 2)
  } catch {
    return val
  }
}

const openDetailModal = (log: SystemAuditLog) => {
  selectedLog.value = log
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  selectedLog.value = null
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.audit-logs-manager {
  display: flex;
  min-height: 100vh;
  background: #F9F9F9;
}

.main-content {
  padding: 32px 24px;
  width: 100%;
  box-sizing: border-box;
}

.logs-list {
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
  display: flex;
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

.table-container {
  overflow-x: auto;
}

.logs-table {
  width: 100%;
  border-collapse: collapse;
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

.loading-cell,
.empty-cell {
  padding: 32px 16px;
  text-align: center;
  color: #6B7280;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
}

.col-time { width: 150px; }
.col-admin { width: 180px; }
.col-module { width: 100px; }
.col-action { width: 100px; }
.col-target { width: 120px; }
.col-details { width: 120px; text-align: right; }

.time {
  font-family: 'Geist', sans-serif;
  font-size: 13px;
  color: #5E5F5C;
}

.admin-id {
  font-family: 'Geist', sans-serif;
  font-size: 13px;
  font-weight: 600;
  color: #000000;
}

.target-id {
  font-family: 'Geist', sans-serif;
  font-size: 13px;
  color: #4C4546;
}

.badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 9999px;
  font-family: 'Geist', sans-serif;
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
}
.badge-create { background: #E6F4EA; color: #137333; }
.badge-update { background: #E8F0FE; color: #1967D2; }
.badge-delete { background: #FCE8E6; color: #C5221F; }

/* Module Badges */
.badge-voucher { background: #FEF3C7; color: #92400E; }
.badge-product { background: #DBEAFE; color: #1E40AF; }
.badge-tier { background: #FFEDD5; color: #C2410C; }
.badge-user { background: #E0E7FF; color: #3730A3; }
.badge-order { background: #D1FAE5; color: #065F46; }
.badge-default { background: #F3F3F4; color: #4C4546; }

.btn-detail {
  background: none;
  border: 1px solid #E8E8E8;
  border-radius: 4px;
  padding: 6px 12px;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
  color: #000000;
  cursor: pointer;
  transition: all 0.2s;
}
.btn-detail:hover {
  background: #F3F3F4;
}

/* Modal Styles */
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
  border: 1px solid #E8E8E8;
  border-radius: 12px;
  width: 800px;
  max-width: 90vw;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
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
  font-weight: 600;
  color: #000000;
  font-family: 'Geist', sans-serif;
}

.btn-close {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #666;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 16px;
  margin-bottom: 24px;
  padding-bottom: 24px;
  border-bottom: 1px solid #E8E8E8;
}

.info-item label {
  display: block;
  font-size: 10px;
  font-weight: 700;
  color: #848484;
  margin-bottom: 4px;
  font-family: 'Geist', sans-serif;
}

.info-item span {
  font-size: 14px;
  font-weight: 500;
  color: #000000;
  font-family: 'Geist', sans-serif;
}

.fields-section {
  margin-bottom: 24px;
}

.fields-section label {
  display: block;
  font-size: 10px;
  font-weight: 700;
  color: #848484;
  margin-bottom: 8px;
  font-family: 'Geist', sans-serif;
}

.changed-fields {
  background: #F3F3F4;
  padding: 8px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-family: monospace;
  color: #991B1B;
}

.diff-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

.diff-col label {
  display: block;
  font-size: 10px;
  font-weight: 700;
  margin-bottom: 8px;
  font-family: 'Geist', sans-serif;
}

.old-val label { color: #C5221F; }
.new-val label { color: #137333; }

.diff-col pre {
  margin: 0;
  padding: 16px;
  background: #F8F9FA;
  border: 1px solid #E8E8E8;
  border-radius: 6px;
  font-family: monospace;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 300px;
  overflow-y: auto;
}

.old-val pre { background: #FCE8E6; border-color: #FAD2CF; }
.new-val pre { background: #E6F4EA; border-color: #CEEAD6; }

.note-section label {
  display: block;
  font-size: 10px;
  font-weight: 700;
  color: #848484;
  margin-bottom: 8px;
  font-family: 'Geist', sans-serif;
}
.note-section p {
  margin: 0;
  font-size: 14px;
  color: #4C4546;
  font-family: 'Geist', sans-serif;
  line-height: 1.5;
}
</style>
