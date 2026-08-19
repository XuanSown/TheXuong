<template>
  <div
    v-if="modelValue"
    class="modal-overlay"
    @click.self="close"
  >
    <div class="modal-content">
      <div class="modal-header">
        <h2>{{ faq ? 'Sửa FAQ' : 'Thêm FAQ mới' }}</h2>
        <button
          class="btn-close"
          @click="close"
        >
          &times;
        </button>
      </div>
      <div class="modal-body">
        <form @submit.prevent="submitForm">
          <div class="form-group">
            <label>CHỦ ĐỀ</label>
            <input
              v-model="form.topic"
              type="text"
              required
              class="form-input"
              placeholder="VD: Giao hàng"
            >
          </div>

          <div class="form-group">
            <label>TỪ KHÓA NHẬN DIỆN</label>
            <input
              v-model="form.questionKeywords"
              type="text"
              required
              class="form-input"
              placeholder="ship, giao hàng, phí ship, bao lâu"
            >
            <p class="form-hint">
              Phân cách từ khóa bằng dấu phẩy. Chatbot sử dụng các từ khóa này để nhận diện nội dung câu hỏi.
            </p>
          </div>

          <div class="form-group">
            <label>CÂU TRẢ LỜI</label>
            <textarea
              v-model="form.answer"
              required
              rows="6"
              class="form-input form-textarea"
              placeholder="Nội dung chatbot sẽ trả lời khách..."
            />
          </div>

          <div class="form-actions">
            <button
              type="button"
              class="btn-secondary"
              @click="close"
            >
              HỦY
            </button>
            <button
              type="submit"
              class="btn-primary"
            >
              LƯU LẠI
            </button>
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, watch } from 'vue'
import type { AdminFaq, AdminFaqRequest } from '@/types/customerCare'

const props = defineProps<{
  modelValue: boolean
  faq: AdminFaq | null
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: boolean): void
  (e: 'save', payload: AdminFaqRequest): void
}>()

const form = ref<AdminFaqRequest>({
  topic: '',
  questionKeywords: '',
  answer: '',
})

watch(
  () => props.modelValue,
  (visible) => {
    if (visible) {
      form.value = props.faq
        ? {
            topic: props.faq.topic,
            questionKeywords: props.faq.questionKeywords,
            answer: props.faq.answer,
          }
        : { topic: '', questionKeywords: '', answer: '' }
    }
  }
)

const close = () => {
  emit('update:modelValue', false)
}

const submitForm = () => {
  emit('save', {
    topic: form.value.topic.trim(),
    questionKeywords: form.value.questionKeywords.trim(),
    answer: form.value.answer.trim(),
  })
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
  width: 560px;
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

.form-group {
  margin-bottom: 16px;
}

.form-group label {
  display: block;
  font-size: 10px;
  font-weight: 700;
  letter-spacing: 1px;
  color: #848484;
  margin-bottom: 6px;
  font-family: 'Geist', sans-serif;
}

.form-input {
  width: 100%;
  box-sizing: border-box;
  padding: 10px 12px;
  border: 1px solid #E8E8E8;
  border-radius: 6px;
  font-family: 'Geist', sans-serif;
  font-size: 13px;
  color: #000000;
}

.form-input:focus {
  outline: none;
  border-color: #000000;
}

.form-textarea {
  resize: vertical;
  min-height: 120px;
}

.form-hint {
  margin: 6px 0 0 0;
  font-family: 'Geist', sans-serif;
  font-size: 11px;
  color: #848484;
  line-height: 1.4;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 8px;
}

.btn-primary {
  background: #000000;
  color: #FFFFFF;
  border: none;
  padding: 10px 18px;
  border-radius: 6px;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
  transition: opacity 0.2s;
}

.btn-primary:hover:not(:disabled) {
  opacity: 0.85;
}

.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background: #F3F3F4;
  color: #4C4546;
  border: 1px solid #E8E8E8;
  padding: 10px 18px;
  border-radius: 6px;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
  cursor: pointer;
}
</style>
