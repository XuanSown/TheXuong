<template>
  <div class="tiers-manager">
    <div class="main-container">
      <!-- Toolbar Section -->
      <div class="toolbar-section">
        <div class="toolbar-content">
          <div class="header-left">
            <h1 class="heading-1">
              QUẢN LÝ CẤP BẬC KHÁCH HÀNG
            </h1>
            <p class="subtitle">
              Thiết lập và quản lý các hạng khách hàng, điều kiện thăng hạng và đặc quyền.
            </p>
          </div>
          <button
            class="btn-add"
            @click="openCreateModal"
          >
            <svg
              width="16"
              height="16"
              viewBox="0 0 16 16"
              fill="none"
            >
              <path
                d="M8 3V13M3 8H13"
                stroke="white"
                stroke-width="2"
                stroke-linecap="round"
              />
            </svg>
            THÊM CẤP BẬC MỚI
          </button>
        </div>
      </div>

      <!-- Table Container -->
      <div class="tier-table-container">
        <div class="table-wrapper">
          <!-- Table Header -->
          <div class="table-header">
            <div
              class="header-cell"
              style="width: 15%;"
            >
              MÃ HẠNG
            </div>
            <div
              class="header-cell"
              style="width: 20%;"
            >
              TÊN HẠNG
            </div>
            <div
              class="header-cell"
              style="width: 20%;"
            >
              ĐIỀU KIỆN (TIỀN / ĐIỂM)
            </div>
            <div
              class="header-cell"
              style="width: 15%;"
            >
              GIẢM GIÁ TỰ ĐỘNG
            </div>
            <div
              class="header-cell"
              style="width: 20%;"
            >
              QUYỀN LỢI KHÁC
            </div>
            <div
              class="header-cell"
              style="width: 10%; justify-content: flex-end;"
            >
              HÀNH ĐỘNG
            </div>
          </div>

          <!-- Table Body -->
          <div class="table-body">
            <div
              v-if="isLoading"
              class="loading-cell"
            >
              Đang tải...
            </div>
            <div
              v-else-if="tiers.length === 0"
              class="empty-cell"
            >
              Chưa có hạng nào
            </div>
            <div v-else>
              <div
                v-for="tier in tiers"
                :key="tier.id"
                class="table-row"
              >
                <div
                  class="cell"
                  style="width: 15%;"
                >
                  <span class="badge">{{ tier.code }}</span>
                </div>
                <div
                  class="cell"
                  style="width: 20%;"
                >
                  <span class="tier-name">{{ tier.name }}</span>
                </div>
                <div
                  class="cell"
                  style="width: 20%;"
                >
                  <span class="conditions">
                    {{ formatCurrency(tier.minTotalSpent) }} / {{ formatPoints(tier.minTotalPoints) }}
                  </span>
                </div>
                <div
                  class="cell"
                  style="width: 15%;"
                >
                  <span class="discount">{{ tier.autoDiscountPercent || 0 }}%</span>
                </div>
                <div
                  class="cell"
                  style="width: 20%;"
                >
                  <div class="benefits-list">
                    <span
                      v-if="tier.rewardVoucherId"
                      class="reward-badge"
                    >Tặng Voucher #{{ tier.rewardVoucherId }}</span>
                    <ul
                      v-if="getParsedBenefits(tier.benefits).length"
                      class="benefits-ul"
                    >
                      <li
                        v-for="(b, i) in getParsedBenefits(tier.benefits)"
                        :key="i"
                      >
                        {{ b }}
                      </li>
                    </ul>
                  </div>
                </div>
                <div
                  class="cell"
                  style="width: 10%; justify-content: flex-end;"
                >
                  <div class="action-buttons">
                    <button
                      class="action-btn edit-btn"
                      title="Sửa"
                      @click="openEditModal(tier)"
                    >
                      SỬA
                    </button>
                    <button
                      class="action-btn delete-btn"
                      title="Xóa"
                      @click="handleDelete(tier)"
                    >
                      XÓA
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Modal -->
    <Teleport to="body">
      <Transition name="modal-glass">
        <div
          v-if="showModal"
          class="glass-overlay"
          @click.self="closeModal"
          @keydown.esc="closeModal"
        >
          <div class="glass-modal-container">
            <div class="glass-edge-glow" />
            <button
              class="glass-close-btn"
              aria-label="Đóng"
              @click="closeModal"
            >
              <svg
                width="20"
                height="20"
                viewBox="0 0 20 20"
                fill="none"
              >
                <path
                  d="M5 5L15 15M15 5L5 15"
                  stroke="currentColor"
                  stroke-width="1.8"
                  stroke-linecap="round"
                />
              </svg>
            </button>
            <div class="glass-modal-body">
              <h2 class="modal-title">
                {{ isEdit ? 'SỬA HẠNG KHÁCH HÀNG' : 'THÊM HẠNG KHÁCH HÀNG' }}
              </h2>
              <form
                class="tier-form"
                @submit.prevent="saveTier"
              >
                <div class="form-group">
                  <label>Mã Hạng (VD: VIP, THUONG) *</label>
                  <input
                    v-model="formData.code"
                    type="text"
                    class="form-input"
                    required
                    :disabled="isEdit"
                  >
                </div>
                <div class="form-group">
                  <label>Tên Hạng (VD: Khách Hàng VIP) *</label>
                  <input
                    v-model="formData.name"
                    type="text"
                    class="form-input"
                    required
                  >
                </div>
                <div class="form-row">
                  <div class="form-group">
                    <label>Chi tiêu tối thiểu (VNĐ) *</label>
                    <input
                      v-model.number="formData.minTotalSpent"
                      type="number"
                      min="0"
                      class="form-input"
                      required
                    >
                  </div>
                  <div class="form-group">
                    <label>Điểm tích lũy tối thiểu *</label>
                    <input
                      v-model.number="formData.minTotalPoints"
                      type="number"
                      min="0"
                      class="form-input"
                      required
                    >
                  </div>
                </div>
                <div class="form-row">
                  <div class="form-group">
                    <label>Giảm giá tự động (%)</label>
                    <input
                      v-model.number="formData.autoDiscountPercent"
                      type="number"
                      min="0"
                      max="100"
                      step="0.1"
                      class="form-input"
                    >
                  </div>
                  <div class="form-group">
                    <label>Tặng Voucher ID (khi thăng hạng)</label>
                    <input
                      v-model.number="formData.rewardVoucherId"
                      type="number"
                      min="1"
                      placeholder="Nhập ID Voucher"
                      class="form-input"
                    >
                  </div>
                </div>
                <div class="form-group">
                  <label>Quyền lợi khác (mỗi quyền lợi một dòng)</label>
                  <textarea
                    v-model="benefitsInput"
                    rows="4"
                    class="form-input"
                    placeholder="Ưu đãi sinh nhật&#10;Miễn phí vận chuyển"
                  />
                </div>
                <div class="form-actions">
                  <button
                    type="button"
                    class="btn-cancel"
                    @click="closeModal"
                  >
                    HỦY
                  </button>
                  <button
                    type="submit"
                    class="btn-submit"
                    :disabled="isSaving"
                  >
                    LƯU
                  </button>
                </div>
              </form>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { useToast } from 'vue-toastification'
import { tierService, type Tier } from '@/services/tierService'

const toast = useToast()
const tiers = ref<Tier[]>([])
const isLoading = ref(false)

const showModal = ref(false)
const isEdit = ref(false)
const isSaving = ref(false)
const formData = ref<Tier>({
  code: '',
  name: '',
  minTotalSpent: 0,
  minTotalPoints: 0,
  benefits: '',
  autoDiscountPercent: 0,
  rewardVoucherId: null
})
const benefitsInput = ref('')

const formatCurrency = (val: number | undefined) => {
  if (val === undefined || val === null) return '0 đ'
  return new Intl.NumberFormat('vi-VN').format(val) + ' đ'
}

const formatPoints = (val: number | undefined) => {
  if (val === undefined || val === null) return '0 điểm'
  return new Intl.NumberFormat('vi-VN').format(val) + ' điểm'
}

const getParsedBenefits = (benefits: string | undefined): string[] => {
  if (!benefits) return []
  try {
    const parsed = JSON.parse(benefits)
    if (Array.isArray(parsed)) return parsed
    return []
  } catch (e) {
    return [benefits]
  }
}

const loadTiers = async () => {
  isLoading.value = true
  try {
    const res = await tierService.getAllTiers()
    if (res.success) {
      tiers.value = res.data
    }
  } catch (error) {
    toast.error('Lỗi khi tải danh sách hạng')
  } finally {
    isLoading.value = false
  }
}

const openCreateModal = () => {
  isEdit.value = false
  formData.value = {
    code: '',
    name: '',
    minTotalSpent: 0,
    minTotalPoints: 0,
    benefits: '',
    autoDiscountPercent: 0,
    rewardVoucherId: null
  }
  benefitsInput.value = ''
  showModal.value = true
}

const openEditModal = (tier: Tier) => {
  isEdit.value = true
  formData.value = { ...tier }
  const parsed = getParsedBenefits(tier.benefits)
  benefitsInput.value = parsed.join('\n')
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const saveTier = async () => {
  isSaving.value = true
  try {
    const payload = { ...formData.value }
    if (!payload.rewardVoucherId) payload.rewardVoucherId = null
    
    // Parse benefits back to JSON string
    const lines = benefitsInput.value.split('\n').map(l => l.trim()).filter(l => l.length > 0)
    payload.benefits = JSON.stringify(lines)
    
    if (isEdit.value && payload.id) {
      await tierService.updateTier(payload.id, payload)
      toast.success('Cập nhật hạng thành công')
    } else {
      await tierService.createTier(payload)
      toast.success('Tạo hạng thành công')
    }
    closeModal()
    loadTiers()
  } catch (error: any) {
    toast.error(error.response?.data?.message || 'Lỗi khi lưu hạng')
  } finally {
    isSaving.value = false
  }
}

const handleDelete = async (tier: Tier) => {
  if (confirm(`Bạn có chắc muốn xóa hạng ${tier.name}?`)) {
    try {
      await tierService.deleteTier(tier.id!)
      toast.success('Xóa hạng thành công')
      loadTiers()
    } catch (error: any) {
      toast.error('Lỗi khi xóa hạng')
    }
  }
}

watch(showModal, (isOpen) => {
  document.body.style.overflow = isOpen ? 'hidden' : ''
})

onMounted(() => {
  loadTiers()
})
</script>

<style scoped>
.tiers-manager {
  width: 100%;
  min-height: 1120px;
  background: #F9F9F9;
  display: flex;
  flex-direction: column;
}

.main-container {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 0;
}

/* Toolbar Section */
.toolbar-section {
  background: #FFFFFF;
  border: 1px solid #E2E2E2;
  box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.05);
  border-radius: 12px;
  padding: 24px;
  margin-bottom: 10px;
}

.toolbar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.heading-1 {
  font-family: 'Geist', sans-serif;
  font-weight: 400;
  font-size: 30px;
  line-height: 36px;
  letter-spacing: -0.75px;
  text-transform: uppercase;
  color: #111827;
  margin: 0;
}

.subtitle {
  font-family: 'Geist', sans-serif;
  font-weight: 400;
  font-size: 14px;
  line-height: 20px;
  color: #6B7280;
  margin: 0;
}

.btn-add {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 24px;
  height: 40px;
  background: #000000;
  border: none;
  cursor: pointer;
  color: #FFFFFF;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  font-weight: 400;
  line-height: 20px;
  box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.05);
}

.btn-add:hover {
  background: #1a1a1a;
}

/* Table */
.tier-table-container {
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.05);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-height: 400px;
}

.table-wrapper {
  flex: 1;
  overflow-y: auto;
}

.table-header {
  display: flex;
  width: 100%;
  height: 64.5px;
  background: #F9FAFB;
  border-bottom: 1px solid #E5E7EB;
}

.header-cell {
  display: flex;
  align-items: center;
  padding: 23.5px 24px 25px;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 0.6px;
  text-transform: uppercase;
  color: #6B7280;
  box-sizing: border-box;
}

.table-body {
  display: flex;
  flex-direction: column;
}

.table-row {
  display: flex;
  width: 100%;
  min-height: 57px;
  border-top: 1px solid #F3F4F6;
  box-sizing: border-box;
}

.table-row:hover {
  background: #F9FAFB;
}

.cell {
  display: flex;
  align-items: center;
  padding: 18px 24px 19px;
  box-sizing: border-box;
}

.loading-cell, .empty-cell {
  padding: 48px 24px;
  text-align: center;
  color: #6B7280;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
}

.badge {
  background: #DBEAFE;
  color: #1E40AF;
  padding: 4px 10px;
  border-radius: 9999px;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
}

.tier-name {
  font-family: 'Geist', sans-serif;
  font-weight: 500;
  font-size: 14px;
  color: #111827;
}

.conditions {
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  color: #4B5563;
}

.discount {
  font-family: 'Geist', sans-serif;
  font-weight: 500;
  font-size: 14px;
  color: #059669; /* Emerald 600 */
}

.benefits-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.benefits-ul {
  margin: 0;
  padding-left: 16px;
  font-family: 'Geist', sans-serif;
  font-size: 13px;
  color: #6B7280;
}

.reward-badge {
  background: #FEF3C7;
  color: #D97706;
  padding: 2px 8px;
  border-radius: 4px;
  font-family: 'Geist', sans-serif;
  font-size: 11px;
  font-weight: 600;
  display: inline-block;
  width: max-content;
  margin-bottom: 4px;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  gap: 8px;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  height: 28px;
  border: 1px solid;
  border-radius: 4px;
  background: transparent;
  cursor: pointer;
  font-family: 'Geist', sans-serif;
  font-size: 11px;
  font-weight: 600;
  letter-spacing: 1.2px;
  text-transform: uppercase;
}

.edit-btn {
  border-color: #D1D5DB;
  color: #374151;
}

.edit-btn:hover {
  background: #F3F4F6;
}

.delete-btn {
  border-color: rgba(186, 26, 26, 0.4);
  color: #BA1A1A;
}

.delete-btn:hover {
  background: rgba(186, 26, 26, 0.05);
}

/* Glass Modal */
.glass-overlay {
  position: fixed;
  top: 0; left: 0; right: 0; bottom: 0;
  background: rgba(17, 24, 39, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 9999;
}

.glass-modal-container {
  position: relative;
  width: 500px;
  max-width: 90%;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1), 0 10px 10px -5px rgba(0, 0, 0, 0.04);
}

.glass-close-btn {
  position: absolute;
  top: 16px;
  right: 16px;
  background: transparent;
  border: none;
  cursor: pointer;
  color: #9CA3AF;
  padding: 4px;
  border-radius: 4px;
}

.glass-close-btn:hover {
  background: #F3F4F6;
  color: #4B5563;
}

.glass-modal-body {
  padding: 32px;
}

.modal-title {
  font-family: 'Geist', sans-serif;
  font-size: 20px;
  font-weight: 600;
  color: #111827;
  margin-top: 0;
  margin-bottom: 24px;
}

/* Form Styles */
.tier-form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.form-row {
  display: flex;
  gap: 16px;
}

.form-row .form-group {
  flex: 1;
}

.form-group label {
  font-family: 'Geist', sans-serif;
  font-size: 13px;
  font-weight: 500;
  color: #374151;
}

.form-input {
  width: 100%;
  padding: 8px 12px;
  background: #FFFFFF;
  border: 1px solid #D1D5DB;
  border-radius: 6px;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  color: #111827;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #000000;
  box-shadow: 0 0 0 1px #000000;
}

.form-input:disabled {
  background: #F3F4F6;
  color: #9CA3AF;
  cursor: not-allowed;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 8px;
}

.btn-cancel {
  padding: 8px 16px;
  background: #FFFFFF;
  border: 1px solid #D1D5DB;
  border-radius: 6px;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  font-weight: 500;
  color: #374151;
  cursor: pointer;
}

.btn-cancel:hover {
  background: #F9FAFB;
}

.btn-submit {
  padding: 8px 24px;
  background: #000000;
  border: 1px solid #000000;
  border-radius: 6px;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  font-weight: 500;
  color: #FFFFFF;
  cursor: pointer;
}

.btn-submit:hover:not(:disabled) {
  background: #1a1a1a;
}

.btn-submit:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

/* Modal transition */
.modal-glass-enter-active,
.modal-glass-leave-active {
  transition: opacity 0.3s ease;
}
.modal-glass-enter-from,
.modal-glass-leave-to {
  opacity: 0;
}
</style>
