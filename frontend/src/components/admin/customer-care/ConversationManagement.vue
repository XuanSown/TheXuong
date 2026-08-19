<template>
  <div class="conversation-management">
    <div class="conversation-layout">
      <section class="conversation-list-panel">
        <div class="panel-header">
          <h3>HỘI THOẠI</h3>
          <p class="panel-count">
            {{ totalElements }} hội thoại
          </p>
        </div>

        <div class="search-bar">
          <input
            v-model="keyword"
            type="text"
            class="search-input"
            placeholder="Tìm kiếm Chat ID..."
            @keyup.enter="applySearch"
          >
          <button
            class="btn-secondary"
            @click="applySearch"
          >
            TÌM
          </button>
        </div>

        <div class="list-scroll">
          <div
            v-if="isLoading"
            class="state-cell"
          >
            Đang tải dữ liệu...
          </div>
          <div
            v-else-if="conversations.length === 0"
            class="state-cell"
          >
            Không có hội thoại nào
          </div>
          <button
            v-for="conv in conversations"
            v-else
            :key="conv.chatId"
            class="conv-item"
            :class="{ active: selectedChatId === conv.chatId }"
            @click="selectConversation(conv.chatId)"
          >
            <div class="conv-top">
              <span
                class="conv-chatid"
                :title="conv.chatId"
              >{{ conv.chatId }}</span>
              <span class="conv-count">{{ conv.messageCount }} lượt</span>
            </div>
            <p
              class="conv-last"
              :title="conv.lastMessage || ''"
            >
              {{ conv.lastMessage || 'Chưa có tin nhắn' }}
            </p>
            <span class="conv-time">{{ formatDate(conv.updatedAt) }}</span>
          </button>
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

      <section class="conversation-detail-panel">
        <ConversationDetail
          v-if="selectedChatId"
          :chat-id="selectedChatId"
          @reset="handleReset"
        />
        <div
          v-else
          class="detail-empty"
        >
          Chọn một hội thoại bên trái để xem chi tiết
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useToast } from 'vue-toastification'
import { customerCareAdminService } from '@/services/customerCareAdmin.service'
import type { AdminChatMemorySummary } from '@/types/customerCare'
import ConversationDetail from './ConversationDetail.vue'

const toast = useToast()

const conversations = ref<AdminChatMemorySummary[]>([])
const isLoading = ref(false)
const keyword = ref('')
const page = ref(0)
const size = ref(20)
const totalElements = ref(0)
const totalPages = ref(1)
const selectedChatId = ref<string | null>(null)

const loadConversations = async () => {
  isLoading.value = true
  try {
    const res = await customerCareAdminService.getConversations({
      keyword: keyword.value || undefined,
      page: page.value,
      size: size.value,
    })
    if (res.success && res.data) {
      conversations.value = res.data.content
      totalElements.value = res.data.totalElements
      totalPages.value = Math.max(1, res.data.totalPages)
    }
  } catch {
    toast.error('Lỗi khi tải danh sách hội thoại')
  } finally {
    isLoading.value = false
  }
}

const applySearch = () => {
  page.value = 0
  selectedChatId.value = null
  loadConversations()
}

const goToPage = (target: number) => {
  if (target < 0 || target >= totalPages.value) return
  page.value = target
  loadConversations()
}

const selectConversation = (chatId: string) => {
  selectedChatId.value = chatId
}

const handleReset = () => {
  selectedChatId.value = null
  loadConversations()
}

const formatDate = (dateStr: string | null) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return new Intl.DateTimeFormat('vi-VN', {
    hour: '2-digit', minute: '2-digit',
    day: '2-digit', month: '2-digit', year: 'numeric',
  }).format(d)
}

onMounted(loadConversations)
</script>

<style scoped>
.conversation-layout {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 16px;
  align-items: start;
}

@media (max-width: 900px) {
  .conversation-layout {
    grid-template-columns: 1fr;
  }
}

.conversation-list-panel,
.conversation-detail-panel {
  background: #FFFFFF;
  border: 1px solid #E8E8E8;
  border-radius: 12px;
  box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.05);
}

.panel-header {
  padding: 16px 20px;
  border-bottom: 1px solid #E8E8E8;
}

.panel-header h3 {
  margin: 0 0 2px 0;
  font-family: 'Geist', sans-serif;
  font-size: 16px;
  font-weight: 700;
  color: #000000;
}

.panel-count {
  margin: 0;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #848484;
}

.search-bar {
  display: flex;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid #E8E8E8;
}

.search-input {
  flex: 1;
  padding: 8px 12px;
  border: 1px solid #E8E8E8;
  border-radius: 6px;
  font-family: 'Geist', sans-serif;
  font-size: 13px;
  color: #000000;
}

.btn-secondary {
  background: #F3F3F4;
  color: #4C4546;
  border: 1px solid #E8E8E8;
  padding: 8px 14px;
  border-radius: 6px;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}

.btn-secondary:hover:not(:disabled) {
  background: #E8E8E8;
}

.btn-secondary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.list-scroll {
  max-height: 560px;
  overflow-y: auto;
}

.state-cell {
  padding: 32px 16px;
  text-align: center;
  color: #6B7280;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
}

.conv-item {
  display: block;
  width: 100%;
  text-align: left;
  background: #FFFFFF;
  border: none;
  border-bottom: 1px solid #E8E8E8;
  padding: 12px 16px;
  cursor: pointer;
  transition: background 0.15s;
}

.conv-item:hover {
  background: #F9F9F9;
}

.conv-item.active {
  background: #000000;
}

.conv-item.active .conv-chatid,
.conv-item.active .conv-count,
.conv-item.active .conv-last,
.conv-item.active .conv-time {
  color: #FFFFFF;
}

.conv-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 4px;
}

.conv-chatid {
  font-family: 'Geist', sans-serif;
  font-size: 13px;
  font-weight: 700;
  color: #000000;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conv-count {
  font-family: 'Geist', sans-serif;
  font-size: 11px;
  color: #848484;
  flex-shrink: 0;
  margin-left: 8px;
}

.conv-last {
  margin: 0 0 4px 0;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #4C4546;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.conv-time {
  font-family: 'Geist', sans-serif;
  font-size: 11px;
  color: #848484;
}

.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  padding: 12px 16px;
  border-top: 1px solid #E8E8E8;
}

.page-info {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #4C4546;
}

.detail-empty {
  padding: 80px 24px;
  text-align: center;
  color: #6B7280;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
}
</style>
