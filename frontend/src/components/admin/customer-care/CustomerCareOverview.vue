<template>
  <div class="overview-tab">
    <div
      v-if="isLoading"
      class="loading-state"
    >
      <div
        v-for="i in 4"
        :key="i"
        class="skeleton-card"
      />
    </div>

    <div
      v-else-if="loadError"
      class="error-state"
    >
      <p>{{ loadError }}</p>
      <button
        class="btn-secondary"
        @click="loadOverview"
      >
        THỬ LẠI
      </button>
    </div>

    <template v-else>
      <div class="cards-grid">
        <div class="stat-card">
          <span class="stat-label">TỔNG FAQ</span>
          <span class="stat-value">{{ overview?.totalFaqs ?? 0 }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">TỔNG HỘI THOẠI</span>
          <span class="stat-value">{{ overview?.totalConversations ?? 0 }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">TIN NHẮN HÔM NAY</span>
          <span class="stat-value">{{ overview?.todayMessages ?? 0 }}</span>
        </div>
        <div class="stat-card">
          <span class="stat-label">INTENT NHIỀU NHẤT</span>
          <span class="stat-value intent-value">{{ overview?.topIntent || '—' }}</span>
        </div>
      </div>

      <section class="recent-card">
        <div class="recent-header">
          <h3>HOẠT ĐỘNG GẦN ĐÂY</h3>
        </div>
        <div
          v-if="recentLoading"
          class="recent-state"
        >
          Đang tải...
        </div>
        <div
          v-else-if="recentLogs.length === 0"
          class="recent-state"
        >
          Chưa có hoạt động nào
        </div>
        <table
          v-else
          class="recent-table"
        >
          <thead>
            <tr>
              <th class="r-col-time">
                THỜI GIAN
              </th>
              <th class="r-col-user">
                KHÁCH HÀNG
              </th>
              <th class="r-col-chatid">
                CHAT ID
              </th>
              <th class="r-col-intent">
                INTENT
              </th>
              <th class="r-col-message">
                TIN NHẮN
              </th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="log in recentLogs"
              :key="log.id"
            >
              <td class="r-col-time">
                <span class="recent-time">{{ formatDate(log.createdAt) }}</span>
              </td>
              <td class="r-col-user">
                <span class="recent-user">{{ log.userName || '—' }}</span>
              </td>
              <td class="r-col-chatid">
                <span
                  class="recent-chatid"
                  :title="log.chatId"
                >{{ log.chatId }}</span>
              </td>
              <td class="r-col-intent">
                <span
                  v-if="log.intent"
                  class="intent-badge"
                >{{ log.intent }}</span>
                <span v-else>—</span>
              </td>
              <td class="r-col-message">
                <span
                  class="recent-message"
                  :title="log.userMessage"
                >{{ log.userMessage }}</span>
              </td>
            </tr>
          </tbody>
        </table>
      </section>
    </template>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { customerCareAdminService } from '@/services/customerCareAdmin.service'
import type { AdminChatLog, CustomerCareOverview } from '@/types/customerCare'

const overview = ref<CustomerCareOverview | null>(null)
const isLoading = ref(false)
const loadError = ref('')

const recentLogs = ref<AdminChatLog[]>([])
const recentLoading = ref(false)

const loadOverview = async () => {
  isLoading.value = true
  loadError.value = ''
  try {
    const res = await customerCareAdminService.getOverview()
    overview.value = res.data ?? null
  } catch {
    loadError.value = 'Không thể tải dữ liệu tổng quan. Vui lòng thử lại.'
  } finally {
    isLoading.value = false
  }
}

const loadRecentLogs = async () => {
  recentLoading.value = true
  try {
    const res = await customerCareAdminService.getLogs({ page: 0, size: 5 })
    if (res.success && res.data) {
      recentLogs.value = res.data.content
    }
  } catch {
    recentLogs.value = []
  } finally {
    recentLoading.value = false
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return new Intl.DateTimeFormat('vi-VN', {
    hour: '2-digit', minute: '2-digit',
    day: '2-digit', month: '2-digit',
  }).format(d)
}

onMounted(() => {
  loadOverview()
  loadRecentLogs()
})
</script>

<style scoped>
.loading-state {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.skeleton-card {
  height: 110px;
  background: #FFFFFF;
  border: 1px solid #E8E8E8;
  border-radius: 12px;
  animation: pulse 1.2s ease-in-out infinite;
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}

.error-state {
  padding: 48px 24px;
  text-align: center;
  color: #6B7280;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  background: #FFFFFF;
  border: 1px solid #E8E8E8;
  border-radius: 12px;
}

.error-state p {
  margin: 0 0 12px 0;
  color: #C5221F;
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
}

.cards-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-bottom: 16px;
}

@media (max-width: 1000px) {
  .cards-grid,
  .loading-state {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 640px) {
  .cards-grid,
  .loading-state {
    grid-template-columns: 1fr;
  }
}

.stat-card {
  background: #FFFFFF;
  border: 1px solid #E8E8E8;
  border-radius: 12px;
  box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.05);
  padding: 20px 24px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.stat-label {
  font-family: 'Geist', sans-serif;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1.2px;
  color: #848484;
  text-transform: uppercase;
}

.stat-value {
  font-family: 'Geist', sans-serif;
  font-size: 28px;
  font-weight: 700;
  color: #000000;
}

.intent-value {
  font-size: 20px;
  text-transform: uppercase;
  word-break: break-all;
}

.recent-card {
  background: #FFFFFF;
  border: 1px solid #E8E8E8;
  border-radius: 12px;
  box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.05);
}

.recent-header {
  padding: 16px 20px;
  border-bottom: 1px solid #E8E8E8;
}

.recent-header h3 {
  margin: 0;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #000000;
}

.recent-state {
  padding: 32px 16px;
  text-align: center;
  color: #6B7280;
  font-family: 'Geist', sans-serif;
  font-size: 13px;
}

.recent-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.recent-table th {
  font-family: 'Geist', sans-serif;
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  color: #4C4546;
  text-align: left;
  padding: 10px 16px;
  background: #F3F3F4;
  border-bottom: 1px solid #E8E8E8;
}

.recent-table td {
  padding: 10px 16px;
  border-top: 1px solid #E8E8E8;
}

.r-col-time { width: 120px; }
.r-col-user { width: 140px; }
.r-col-chatid { width: 110px; }
.r-col-intent { width: 140px; }

.recent-time,
.recent-user,
.recent-chatid,
.recent-message {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #4C4546;
  display: block;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.recent-user {
  font-weight: 600;
  color: #000000;
}

.intent-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 9999px;
  background: #F3F3F4;
  color: #4C4546;
  font-family: 'Geist', sans-serif;
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
