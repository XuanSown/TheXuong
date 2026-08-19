<template>
  <div class="chat-log-management">
    <section class="logs-card">
      <div class="list-header">
        <div class="header-info">
          <h2>LỊCH SỬ TƯ VẤN CHATBOT</h2>
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
          v-model="filters.keyword"
          type="text"
          class="search-input"
          placeholder="Tìm khách hàng, chat ID, nội dung tin nhắn..."
          @keyup.enter="applyFilters"
        >
        <input
          v-model="filters.intent"
          type="text"
          class="intent-input"
          placeholder="Intent (VD: stock, faq)..."
          @keyup.enter="applyFilters"
        >
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
              <th class="col-user">
                KHÁCH HÀNG
              </th>
              <th class="col-chatid">
                CHAT ID
              </th>
              <th class="col-intent">
                INTENT
              </th>
              <th class="col-message">
                TIN NHẮN
              </th>
              <th class="col-reply">
                PHẢN HỒI
              </th>
              <th class="col-detail">
                CHI TIẾT
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
                Không tìm thấy log nào
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
              <td class="col-user">
                <span
                  class="user-text"
                  :title="log.userName || ''"
                >{{ log.userName || '—' }}</span>
              </td>
              <td class="col-chatid">
                <span
                  class="chatid-text"
                  :title="log.chatId"
                >{{ log.chatId }}</span>
              </td>
              <td class="col-intent">
                <span
                  v-if="log.intent"
                  class="intent-badge"
                  :class="getIntentClass(log.intent)"
                >{{ log.intent }}</span>
                <span
                  v-else
                  class="intent-none"
                >—</span>
              </td>
              <td class="col-message">
                <span
                  class="truncate message-text"
                  :title="log.userMessage"
                >{{ log.userMessage }}</span>
              </td>
              <td class="col-reply">
                <span
                  class="truncate reply-text"
                  :title="log.botReply"
                >{{ log.botReply }}</span>
              </td>
              <td class="col-detail">
                <button
                  class="btn-detail"
                  @click="openDetail(log)"
                >
                  Xem
                </button>
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

    <ChatLogDetailModal
      v-model="showDetailModal"
      :log="selectedLog"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useToast } from 'vue-toastification'
import { customerCareAdminService } from '@/services/customerCareAdmin.service'
import type { AdminChatLog } from '@/types/customerCare'
import ChatLogDetailModal from './ChatLogDetailModal.vue'

const toast = useToast()

const logs = ref<AdminChatLog[]>([])
const isLoading = ref(false)
const page = ref(0)
const size = ref(20)
const totalElements = ref(0)
const totalPages = ref(1)

const filters = reactive({
  keyword: '',
  intent: '',
  from: '',
  to: '',
})

const showDetailModal = ref(false)
const selectedLog = ref<AdminChatLog | null>(null)

const loadLogs = async () => {
  isLoading.value = true
  try {
    const res = await customerCareAdminService.getLogs({
      keyword: filters.keyword || undefined,
      intent: filters.intent || undefined,
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
    toast.error('Lỗi khi tải danh sách chat logs')
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

const openDetail = (log: AdminChatLog) => {
  selectedLog.value = log
  showDetailModal.value = true
}

const getIntentClass = (intent: string) => {
  const key = intent.toLowerCase()
  if (key === 'stock') return 'badge-stock'
  if (key === 'faq') return 'badge-faq'
  if (key === 'order_tracking' || key === 'order') return 'badge-order'
  if (key === 'price') return 'badge-price'
  if (key === 'rate_limited') return 'badge-limited'
  if (key === 'greeting') return 'badge-greeting'
  return 'badge-default'
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return new Intl.DateTimeFormat('vi-VN', {
    hour: '2-digit', minute: '2-digit', second: '2-digit',
    day: '2-digit', month: '2-digit', year: 'numeric',
  }).format(d)
}

onMounted(loadLogs)
</script>

<style scoped>
.chat-log-management {
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
  min-width: 220px;
  padding: 8px 12px;
  border: 1px solid #E8E8E8;
  border-radius: 6px;
  font-family: 'Geist', sans-serif;
  font-size: 13px;
  color: #000000;
}

.intent-input {
  width: 180px;
  padding: 8px 12px;
  border: 1px solid #E8E8E8;
  border-radius: 6px;
  font-family: 'Geist', sans-serif;
  font-size: 13px;
  color: #000000;
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

.col-time { width: 150px; }
.col-user { width: 130px; }
.col-chatid { width: 110px; }
.col-intent { width: 130px; }
.col-message { width: auto; }
.col-reply { width: auto; }
.col-detail { width: 90px; }

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

.user-text {
  font-family: 'Geist', sans-serif;
  font-size: 13px;
  font-weight: 600;
  color: #000000;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.chatid-text {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #4C4546;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.intent-badge {
  display: inline-block;
  max-width: 100%;
  padding: 4px 10px;
  border-radius: 9999px;
  font-family: 'Geist', sans-serif;
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.badge-stock { background: #DBEAFE; color: #1E40AF; }
.badge-faq { background: #E6F4EA; color: #137333; }
.badge-order { background: #D1FAE5; color: #065F46; }
.badge-price { background: #FEF3C7; color: #92400E; }
.badge-limited { background: #FCE8E6; color: #C5221F; }
.badge-greeting { background: #E0E7FF; color: #3730A3; }
.badge-default { background: #F3F3F4; color: #4C4546; }

.intent-none {
  color: #B0B0B0;
  font-size: 13px;
}

.truncate {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.message-text {
  font-family: 'Geist', sans-serif;
  font-size: 13px;
  color: #000000;
  max-width: 280px;
}

.reply-text {
  font-family: 'Geist', sans-serif;
  font-size: 13px;
  color: #5E5F5C;
  max-width: 280px;
}

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
