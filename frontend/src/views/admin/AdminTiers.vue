<template>
  <div class="tiers-manager">
    <!-- Main Content -->
    <main class="main-content">
      <!-- Tier List Section -->
      <section class="tier-list">
        <!-- Header -->
        <div class="list-header">
          <div class="header-info">
            <h2>Danh sách cấp bậc</h2>
            <p class="tier-count">
              Tổng cộng {{ tiers.length }} cấp bậc
            </p>
          </div>
          <button
            class="btn-primary"
            @click="openAddModal"
          >
            + THÊM CẤP BẬC
          </button>
        </div>

        <!-- Table -->
        <div class="table-container">
          <table class="tiers-table">
            <thead>
              <tr>
                <th class="col-id">
                  MÃ HẠNG
                </th>
                <th class="col-name">
                  TÊN HẠNG
                </th>
                <th class="col-condition">
                  ĐIỀU KIỆN (TIỀN / ĐIỂM)
                </th>
                <th class="col-discount">
                  GIẢM GIÁ TỰ ĐỘNG
                </th>
                <th class="col-actions">
                  THAO TÁC
                </th>
              </tr>
            </thead>
            <tbody>
              <tr v-if="isLoading">
                <td
                  colspan="5"
                  class="loading-cell"
                >
                  Đang tải dữ liệu...
                </td>
              </tr>
              <tr v-else-if="tiers.length === 0">
                <td
                  colspan="5"
                  class="empty-cell"
                >
                  Không tìm thấy cấp bậc nào
                </td>
              </tr>
              <tr
                v-for="tier in tiers"
                v-else
                :key="tier.id"
              >
                <td class="col-id">
                  <span class="badge">{{ tier.code }}</span>
                </td>
                <td class="col-name">
                  <span class="tier-name">{{ tier.name }}</span>
                </td>
                <td class="col-condition">
                  <span class="conditions">
                    {{ formatCurrency(tier.minTotalSpent) }} / {{ formatPoints(tier.minTotalPoints) }}
                  </span>
                </td>
                <td class="col-discount">
                  <span class="discount">{{ tier.autoDiscountPercent || 0 }}%</span>
                </td>
                <td class="col-actions">
                  <div class="action-buttons">
                    <button
                      class="action-btn edit-btn"
                      title="Sửa"
                      @click="openEditModal(tier)"
                    >
                      ✎
                    </button>
                    <button
                      class="action-btn delete-btn"
                      title="Xóa"
                      @click="deleteTier(tier)"
                    >
                      🗑
                    </button>
                  </div>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </section>
    </main>

    <!-- Modal Form (Add/Edit) -->
    <div
      v-if="showModal"
      class="modal-overlay"
      @click.self="closeModal"
    >
      <div class="modal-content">
        <div class="modal-header">
          <h2>{{ isEditing ? 'Sửa cấp bậc' : 'Thêm cấp bậc' }}</h2>
          <button
            class="btn-close"
            @click="closeModal"
          >
            &times;
          </button>
        </div>
        <div class="modal-body">
          <form @submit.prevent="submitForm">
            <div class="form-group">
              <label>Mã Hạng (VD: VIP, VVIP)</label>
              <input
                v-model="form.code"
                type="text"
                required
                class="form-input"
                :disabled="isEditing && isBaseTier(form.code)"
              >
            </div>
            
            <div class="form-group">
              <label>Tên Hạng</label>
              <input
                v-model="form.name"
                type="text"
                required
                class="form-input"
              >
            </div>

            <div class="form-row">
              <div class="form-group half">
                <label>Số tiền tối thiểu (VNĐ)</label>
                <input
                  v-model.number="form.minTotalSpent"
                  type="number"
                  required
                  min="0"
                  class="form-input"
                >
              </div>
              <div class="form-group half">
                <label>Số điểm tối thiểu</label>
                <input
                  v-model.number="form.minTotalPoints"
                  type="number"
                  required
                  min="0"
                  class="form-input"
                >
              </div>
            </div>

            <div class="form-group">
              <label>Giảm giá tự động (%)</label>
              <input
                v-model.number="form.autoDiscountPercent"
                type="number"
                min="0"
                max="100"
                class="form-input"
              >
            </div>

            <div class="form-actions">
              <button
                type="button"
                class="btn-secondary"
                @click="closeModal"
              >
                HỦY
              </button>
              <button
                type="submit"
                class="btn-primary"
                :disabled="isSaving"
              >
                {{ isSaving ? 'ĐANG LƯU...' : 'LƯU LẠI' }}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useToast } from 'vue-toastification'
import { tierService, type Tier } from '@/services/tierService'

const toast = useToast()
const tiers = ref<Tier[]>([])
const isLoading = ref(false)

// Modal State
const showModal = ref(false)
const isEditing = ref(false)
const isSaving = ref(false)
const currentTierId = ref<number | null>(null)

const form = ref({
  code: '',
  name: '',
  minTotalSpent: 0,
  minTotalPoints: 0,
  autoDiscountPercent: 0
})

const formatCurrency = (val: number | undefined) => {
  if (val === undefined || val === null) return '0 đ'
  return new Intl.NumberFormat('vi-VN').format(val) + ' đ'
}

const formatPoints = (val: number | undefined) => {
  if (val === undefined || val === null) return '0 điểm'
  return new Intl.NumberFormat('vi-VN').format(val) + ' điểm'
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

const isBaseTier = (code: string) => {
  if (tiers.value.length === 0) return false
  return tiers.value[0].code === code
}

// Modal Actions
const openAddModal = () => {
  isEditing.value = false
  currentTierId.value = null
  form.value = {
    code: '',
    name: '',
    minTotalSpent: 0,
    minTotalPoints: 0,
    autoDiscountPercent: 0
  }
  showModal.value = true
}

const openEditModal = (tier: Tier) => {
  isEditing.value = true
  currentTierId.value = tier.id!
  form.value = {
    code: tier.code,
    name: tier.name,
    minTotalSpent: tier.minTotalSpent,
    minTotalPoints: tier.minTotalPoints,
    autoDiscountPercent: tier.autoDiscountPercent || 0
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
}

const submitForm = async () => {
  isSaving.value = true
  try {
    if (isEditing.value && currentTierId.value) {
      const res = await tierService.updateTier(currentTierId.value, form.value as Tier)
      if (res.success) {
        toast.success('Cập nhật cấp bậc thành công')
        await loadTiers()
        closeModal()
      }
    } else {
      const res = await tierService.createTier(form.value as Tier)
      if (res.success) {
        toast.success('Thêm cấp bậc thành công')
        await loadTiers()
        closeModal()
      }
    }
  } catch (error: any) {
    toast.error(error.response?.data?.message || 'Lỗi khi lưu cấp bậc')
  } finally {
    isSaving.value = false
  }
}

const deleteTier = async (tier: Tier) => {
  if (isBaseTier(tier.code)) {
    toast.error('Không thể xóa hạng thấp nhất (mặc định)')
    return
  }
  if (!confirm(`Bạn có chắc chắn muốn xóa hạng ${tier.name}?`)) return
  
  try {
    const res = await tierService.deleteTier(tier.id!)
    if (res.success) {
      toast.success('Xóa cấp bậc thành công')
      await loadTiers()
    }
  } catch (error: any) {
    toast.error(error.response?.data?.message || 'Lỗi khi xóa cấp bậc')
  }
}

onMounted(() => {
  loadTiers()
})
</script>

<style scoped>
.tiers-manager {
  display: flex;
  min-height: 100vh;
  background: #F9F9F9;
}

/* Main Content */
.main-content {
  padding: 32px 24px;
  width: 100%;
  box-sizing: border-box;
}

/* Tier List Section */
.tier-list {
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

.tier-count {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #848484;
  margin: 0;
}

.btn-primary {
  background: #000000;
  color: #FFFFFF;
  border: none;
  padding: 8px 16px;
  border-radius: 6px;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: opacity 0.2s;
}

.btn-primary:hover:not(:disabled) {
  opacity: 0.9;
}
.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-secondary {
  background: #F3F3F4;
  color: #4C4546;
  border: 1px solid #E8E8E8;
  padding: 8px 16px;
  border-radius: 6px;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-secondary:hover {
  background: #E8E8E8;
}

.table-container {
  overflow-x: auto;
}

.tiers-table {
  width: 100%;
  border-collapse: collapse;
}

.tiers-table th {
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

.tiers-table td {
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

.col-id { width: 120px; }
.col-name { width: 200px; }
.col-condition { width: 250px; }
.col-discount { width: 150px; }
.col-actions { width: 100px; text-align: center; }

.badge {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 9999px;
  background: #000000;
  color: #FFFFFF;
  font-family: 'Geist', sans-serif;
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
}

.tier-name {
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: #000000;
}

.conditions {
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  color: #5E5F5C;
}

.discount {
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: #991B1B;
}

.action-buttons {
  display: flex;
  gap: 8px;
  justify-content: center;
}

.action-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  font-size: 16px;
  color: #666;
  transition: all 0.2s;
}

.action-btn:hover {
  background: #f0f0f0;
  color: #000;
}

.delete-btn:hover {
  color: #991B1B;
  background: #FEE2E2;
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
  width: 500px;
  max-width: 90vw;
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
}

.form-group {
  margin-bottom: 20px;
}

.form-row {
  display: flex;
  gap: 16px;
}
.half { flex: 1; }

.form-group label {
  display: block;
  font-size: 12px;
  font-weight: 600;
  color: #4C4546;
  margin-bottom: 8px;
  font-family: 'Geist', sans-serif;
}

.form-input {
  width: 100%;
  padding: 10px 12px;
  border: 1px solid #E8E8E8;
  border-radius: 6px;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  box-sizing: border-box;
}

.form-input:focus {
  outline: none;
  border-color: #000000;
}

.form-input:disabled {
  background: #F3F3F4;
  cursor: not-allowed;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  margin-top: 32px;
}
</style>
