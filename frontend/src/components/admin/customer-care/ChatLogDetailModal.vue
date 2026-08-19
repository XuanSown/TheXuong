<template>
  <div
    v-if="modelValue && log"
    class="modal-overlay"
    @click.self="close"
  >
    <div class="modal-content">
      <div class="modal-header">
        <h2>Chi Tiết Chat Log #{{ log.id }}</h2>
        <button
          class="btn-close"
          @click="close"
        >
          &times;
        </button>
      </div>
      <div class="modal-body">
        <div class="info-grid">
          <div class="info-item">
            <label>KHÁCH HÀNG</label>
            <span>{{ log.userName || '—' }}</span>
          </div>
          <div class="info-item">
            <label>CHAT ID</label>
            <span class="break-all">{{ log.chatId }}</span>
          </div>
          <div class="info-item">
            <label>INTENT</label>
            <span>{{ log.intent || '—' }}</span>
          </div>
          <div class="info-item wide">
            <label>CREATED AT</label>
            <span>{{ formatDate(log.createdAt) }}</span>
          </div>
        </div>

        <div class="message-section">
          <label>USER MESSAGE</label>
          <div class="message-box user-box">
            {{ log.userMessage }}
          </div>
        </div>

        <div class="message-section">
          <label>BOT REPLY</label>
          <div class="message-box bot-box">
            {{ log.botReply }}
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { AdminChatLog } from '@/types/customerCare'

defineProps<{
  modelValue: boolean
  log: AdminChatLog | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
}>()

const close = () => {
  emit('update:modelValue', false)
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return new Intl.DateTimeFormat('vi-VN', {
    hour: '2-digit', minute: '2-digit', second: '2-digit',
    day: '2-digit', month: '2-digit', year: 'numeric',
  }).format(d)
}
</script>

<style scoped>
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
  width: 720px;
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

.info-item label,
.message-section label {
  display: block;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1px;
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

.break-all {
  word-break: break-all;
}

.message-section {
  margin-bottom: 16px;
}

.message-box {
  padding: 14px 16px;
  border-radius: 8px;
  font-family: 'Geist', sans-serif;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
}

.user-box {
  background: #F3F3F4;
  border: 1px solid #E8E8E8;
  color: #000000;
}

.bot-box {
  background: #E6F4EA;
  border: 1px solid #CEEAD6;
  color: #137333;
}
</style>
