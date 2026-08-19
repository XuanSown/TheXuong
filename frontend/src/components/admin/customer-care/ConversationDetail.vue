<template>
  <div class="conversation-detail">
    <div class="detail-header">
      <div class="detail-info">
        <span class="detail-label">CHAT ID</span>
        <span
          class="detail-chatid"
          :title="chatId"
        >{{ chatId }}</span>
        <span
          v-if="detail?.updatedAt"
          class="detail-time"
        >{{ formatDate(detail.updatedAt) }}</span>
      </div>
      <button
        class="btn-danger"
        :disabled="isResetting"
        @click="confirmReset"
      >
        {{ isResetting ? 'ĐANG XÓA...' : 'XÓA BỘ NHỚ' }}
      </button>
    </div>

    <div
      v-if="isLoading"
      class="detail-state"
    >
      Đang tải hội thoại...
    </div>

    <div
      v-else-if="loadError"
      class="detail-state error-text"
    >
      {{ loadError }}
    </div>

    <div
      v-else
      class="messages-scroll"
    >
      <div
        v-if="detail?.parseError"
        class="parse-warning"
      >
        ⚠ Không thể đọc dữ liệu bộ nhớ (JSON không hợp lệ).
      </div>

      <div
        v-if="!detail || detail.messages.length === 0"
        class="detail-state"
      >
        Không có tin nhắn nào trong bộ nhớ
      </div>

      <div
        v-for="(msg, index) in detail?.messages || []"
        :key="index"
        class="message-row"
        :class="msg.role === 'user' ? 'row-user' : 'row-assistant'"
      >
        <div
          class="message-bubble"
          :class="msg.role === 'user' ? 'bubble-user' : 'bubble-assistant'"
        >
          <span class="bubble-role">{{ msg.role === 'user' ? 'KHÁCH' : 'BOT' }}</span>
          <p class="bubble-content">
            {{ msg.content }}
          </p>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import { useToast } from 'vue-toastification'
import { customerCareAdminService } from '@/services/customerCareAdmin.service'
import type { AdminConversationDetail } from '@/types/customerCare'

const props = defineProps<{
  chatId: string
}>()

const emit = defineEmits<{
  (e: 'reset', chatId: string): void
}>()

const toast = useToast()

const detail = ref<AdminConversationDetail | null>(null)
const isLoading = ref(false)
const loadError = ref('')
const isResetting = ref(false)

const loadDetail = async () => {
  isLoading.value = true
  loadError.value = ''
  try {
    const res = await customerCareAdminService.getConversationDetail(props.chatId)
    detail.value = res.data ?? null
  } catch {
    loadError.value = 'Không thể tải chi tiết hội thoại.'
  } finally {
    isLoading.value = false
  }
}

watch(() => props.chatId, loadDetail, { immediate: true })

const confirmReset = async () => {
  if (!confirm(`Bạn có chắc chắn muốn xóa bộ nhớ của chat ${props.chatId}?\nLịch sử Chat Logs sẽ được giữ nguyên.`)) return
  isResetting.value = true
  try {
    const res = await customerCareAdminService.resetMemory(props.chatId)
    if (res.success) {
      toast.success('Đã xóa bộ nhớ hội thoại')
      emit('reset', props.chatId)
    }
  } catch (error: any) {
    toast.error(error.response?.data?.message || 'Lỗi khi xóa bộ nhớ')
  } finally {
    isResetting.value = false
  }
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return new Intl.DateTimeFormat('vi-VN', {
    hour: '2-digit', minute: '2-digit',
    day: '2-digit', month: '2-digit', year: 'numeric',
  }).format(d)
}
</script>

<style scoped>
.conversation-detail {
  display: flex;
  flex-direction: column;
  min-height: 480px;
  max-height: 660px;
}

.detail-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  padding: 16px 20px;
  border-bottom: 1px solid #E8E8E8;
  flex-wrap: wrap;
}

.detail-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  min-width: 0;
}

.detail-label {
  font-family: 'Geist', sans-serif;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #848484;
}

.detail-chatid {
  font-family: 'Geist', sans-serif;
  font-size: 15px;
  font-weight: 700;
  color: #000000;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-time {
  font-family: 'Geist', sans-serif;
  font-size: 11px;
  color: #848484;
}

.btn-danger {
  background: #C5221F;
  color: #FFFFFF;
  border: none;
  padding: 10px 16px;
  border-radius: 6px;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 700;
  cursor: pointer;
  transition: opacity 0.2s;
  flex-shrink: 0;
}

.btn-danger:hover:not(:disabled) {
  opacity: 0.85;
}

.btn-danger:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.detail-state {
  padding: 48px 24px;
  text-align: center;
  color: #6B7280;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
}

.error-text {
  color: #C5221F;
}

.parse-warning {
  margin: 12px 16px;
  padding: 10px 14px;
  background: #FEF3C7;
  border: 1px solid #FDE68A;
  border-radius: 8px;
  color: #92400E;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
}

.messages-scroll {
  flex: 1;
  overflow-y: auto;
  padding: 16px 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.message-row {
  display: flex;
}

.row-user {
  justify-content: flex-end;
}

.row-assistant {
  justify-content: flex-start;
}

.message-bubble {
  max-width: 75%;
  border-radius: 12px;
  padding: 10px 14px;
}

.bubble-user {
  background: #000000;
}

.bubble-user .bubble-role {
  color: #B0B0B0;
}

.bubble-user .bubble-content {
  color: #FFFFFF;
}

.bubble-assistant {
  background: #F3F3F4;
  border: 1px solid #E8E8E8;
}

.bubble-assistant .bubble-role {
  color: #848484;
}

.bubble-assistant .bubble-content {
  color: #000000;
}

.bubble-role {
  display: block;
  font-family: 'Geist', sans-serif;
  font-size: 9px;
  font-weight: 700;
  letter-spacing: 1px;
  margin-bottom: 2px;
}

.bubble-content {
  margin: 0;
  font-family: 'Geist', sans-serif;
  font-size: 13px;
  line-height: 1.5;
  white-space: pre-wrap;
  word-break: break-word;
}
</style>
