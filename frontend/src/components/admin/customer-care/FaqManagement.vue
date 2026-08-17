<template>
  <div class="faq-management">
    <section class="faq-card">
      <div class="list-header">
        <div class="header-info">
          <h2>FAQ CHATBOT</h2>
          <p class="faq-count">{{ totalElements }} câu hỏi</p>
        </div>
        <button class="btn-primary" @click="openAddModal">
          + THÊM FAQ
        </button>
      </div>

      <div class="toolbar">
        <input
          v-model="keyword"
          type="text"
          class="search-input"
          placeholder="Tìm kiếm chủ đề, từ khóa, câu trả lời..."
          @keyup.enter="applyFilters"
        />
        <input
          v-model="topic"
          type="text"
          class="topic-input"
          placeholder="Lọc theo chủ đề..."
          @keyup.enter="applyFilters"
        />
        <button class="btn-secondary" @click="applyFilters">TÌM</button>
        <button class="btn-secondary" @click="reload" :disabled="isLoading">
          <span>↻</span> TẢI LẠI
        </button>
      </div>

      <div class="table-container">
        <table class="faq-table">
          <thead>
            <tr>
              <th class="col-id">ID</th>
              <th class="col-topic">CHỦ ĐỀ</th>
              <th class="col-keywords">TỪ KHÓA</th>
              <th class="col-answer">CÂU TRẢ LỜI</th>
              <th class="col-updated">CẬP NHẬT</th>
              <th class="col-actions">THAO TÁC</th>
            </tr>
          </thead>
          <tbody>
            <tr v-if="isLoading">
              <td colspan="6" class="loading-cell">Đang tải dữ liệu...</td>
            </tr>
            <tr v-else-if="faqs.length === 0">
              <td colspan="6" class="empty-cell">Không tìm thấy FAQ nào</td>
            </tr>
            <tr v-else v-for="faq in faqs" :key="faq.id">
              <td class="col-id">
                <span class="faq-id">#{{ faq.id }}</span>
              </td>
              <td class="col-topic">
                <span class="topic-badge">{{ faq.topic }}</span>
              </td>
              <td class="col-keywords">
                <span class="truncate keywords-text" :title="faq.questionKeywords">
                  {{ faq.questionKeywords }}
                </span>
              </td>
              <td class="col-answer">
                <span class="truncate answer-text" :title="faq.answer">
                  {{ faq.answer }}
                </span>
              </td>
              <td class="col-updated">
                <span class="updated-text">{{ formatDate(faq.updatedAt) }}</span>
              </td>
              <td class="col-actions">
                <div class="action-buttons">
                  <button class="action-btn edit-btn" title="Sửa" @click="openEditModal(faq)">✎</button>
                  <button class="action-btn delete-btn" title="Xóa" @click="deleteFaq(faq)">🗑</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>

      <div v-if="totalPages > 1" class="pagination">
        <button class="btn-secondary" :disabled="page === 0 || isLoading" @click="goToPage(page - 1)">
          ‹ TRƯỚC
        </button>
        <span class="page-info">Trang {{ page + 1 }} / {{ totalPages }}</span>
        <button class="btn-secondary" :disabled="page >= totalPages - 1 || isLoading" @click="goToPage(page + 1)">
          SAU ›
        </button>
      </div>
    </section>

    <FaqFormModal
      v-model="showModal"
      :faq="editingFaq"
      @save="handleSave"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useToast } from 'vue-toastification'
import { customerCareAdminService } from '@/services/customerCareAdmin.service'
import type { AdminFaq, AdminFaqRequest } from '@/types/customerCare'
import FaqFormModal from './FaqFormModal.vue'

const toast = useToast()

const faqs = ref<AdminFaq[]>([])
const isLoading = ref(false)
const keyword = ref('')
const topic = ref('')
const page = ref(0)
const size = ref(20)
const totalElements = ref(0)
const totalPages = ref(1)

const showModal = ref(false)
const editingFaq = ref<AdminFaq | null>(null)

const loadFaqs = async () => {
  isLoading.value = true
  try {
    const res = await customerCareAdminService.getFaqs({
      keyword: keyword.value || undefined,
      topic: topic.value || undefined,
      page: page.value,
      size: size.value,
    })
    if (res.success && res.data) {
      faqs.value = res.data.content
      totalElements.value = res.data.totalElements
      totalPages.value = Math.max(1, res.data.totalPages)
    }
  } catch {
    toast.error('Lỗi khi tải danh sách FAQ')
  } finally {
    isLoading.value = false
  }
}

const applyFilters = () => {
  page.value = 0
  loadFaqs()
}

const reload = () => {
  loadFaqs()
}

const goToPage = (target: number) => {
  if (target < 0 || target >= totalPages.value) return
  page.value = target
  loadFaqs()
}

const openAddModal = () => {
  editingFaq.value = null
  showModal.value = true
}

const openEditModal = (faq: AdminFaq) => {
  editingFaq.value = faq
  showModal.value = true
}

const handleSave = async (payload: AdminFaqRequest) => {
  try {
    if (editingFaq.value) {
      const res = await customerCareAdminService.updateFaq(editingFaq.value.id, payload)
      if (res.success) {
        toast.success('Cập nhật FAQ thành công')
        showModal.value = false
        await loadFaqs()
      }
    } else {
      const res = await customerCareAdminService.createFaq(payload)
      if (res.success) {
        toast.success('Tạo FAQ thành công')
        showModal.value = false
        await loadFaqs()
      }
    }
  } catch (error: any) {
    toast.error(error.response?.data?.message || 'Lỗi khi lưu FAQ')
  }
}

const deleteFaq = async (faq: AdminFaq) => {
  if (!confirm(`Bạn có chắc chắn muốn xóa FAQ chủ đề "${faq.topic}"?`)) return
  try {
    const res = await customerCareAdminService.deleteFaq(faq.id)
    if (res.success) {
      toast.success('Xóa FAQ thành công')
      await loadFaqs()
    }
  } catch (error: any) {
    toast.error(error.response?.data?.message || 'Lỗi khi xóa FAQ')
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

onMounted(loadFaqs)
</script>

<style scoped>
.faq-management {
  width: 100%;
}

.faq-card {
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

.faq-count {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #848484;
  margin: 0;
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

.btn-primary:hover {
  opacity: 0.85;
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

.topic-input {
  width: 200px;
  padding: 8px 12px;
  border: 1px solid #E8E8E8;
  border-radius: 6px;
  font-family: 'Geist', sans-serif;
  font-size: 13px;
  color: #000000;
}

.table-container {
  overflow-x: auto;
}

.faq-table {
  width: 100%;
  border-collapse: collapse;
  table-layout: fixed;
}

.faq-table th {
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

.faq-table td {
  padding: 12px 16px;
  border-top: 1px solid #E8E8E8;
  vertical-align: middle;
}

.col-id { width: 70px; }
.col-topic { width: 140px; }
.col-keywords { width: 220px; }
.col-answer { width: auto; }
.col-updated { width: 150px; }
.col-actions { width: 110px; }

.loading-cell,
.empty-cell {
  padding: 32px 16px;
  text-align: center;
  color: #6B7280;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
}

.faq-id {
  font-family: 'Geist', sans-serif;
  font-size: 13px;
  font-weight: 600;
  color: #4C4546;
}

.topic-badge {
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

.truncate {
  display: block;
  max-width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.keywords-text {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #5E5F5C;
}

.answer-text {
  font-family: 'Geist', sans-serif;
  font-size: 13px;
  color: #000000;
  max-width: 420px;
}

.updated-text {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #5E5F5C;
  white-space: nowrap;
}

.action-buttons {
  display: flex;
  gap: 8px;
}

.action-btn {
  width: 30px;
  height: 30px;
  border: 1px solid #E8E8E8;
  border-radius: 4px;
  background: #FFFFFF;
  cursor: pointer;
  font-size: 13px;
  transition: all 0.2s;
  display: inline-flex;
  align-items: center;
  justify-content: center;
}

.edit-btn:hover {
  background: #E8F0FE;
  border-color: #1967D2;
}

.delete-btn:hover {
  background: #FCE8E6;
  border-color: #C5221F;
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
