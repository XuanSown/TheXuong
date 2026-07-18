<template>
  <div class="product-edit-container">
    <div class="form-container">
      <!-- Form Header -->
      <div class="form-header">
        <h1 class="form-title">
          {{ isEditMode ? 'SỬA SẢN PHẨM' : 'THÊM SẢN PHẨM MỚI' }}
        </h1>
      </div>

      <!-- Form Content -->
      <div class="form-content">
        <!-- Basic Info Section -->
        <div class="basic-info-section">
          <!-- Product Name -->
          <div class="field-group">
            <label class="field-label">TÊN SẢN PHẨM</label>
            <input
              v-model="productForm.name"
              type="text"
              class="field-input"
              placeholder="Nhập tên sản phẩm thể thao..."
            >
          </div>

          <!-- Price and Image Upload (2 columns) -->
          <div class="field-row">
            <div class="field-group half">
              <label class="field-label">GIÁ (VNĐ)</label>
              <input
                v-model="productForm.price"
                type="text"
                class="field-input"
                placeholder="500.000"
              >
            </div>
            <div class="field-group half">
              <label class="field-label">HÌNH ẢNH SẢN PHẨM</label>
              <div
                class="image-gallery"
                :class="{ 'is-dragging': isDragging }"
                @dragover.prevent="handleDragOver"
                @dragleave.prevent="handleDragLeave"
                @drop.prevent="handleDrop"
              >
                <Transition name="fade">
                  <div
                    v-if="isDragging"
                    class="drop-overlay"
                  >
                    <svg
                      class="drop-icon"
                      width="40"
                      height="40"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="1.5"
                      stroke-linecap="round"
                      stroke-linejoin="round"
                    >
                      <path d="M21 15v4a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2v-4" />
                      <polyline points="17 8 12 3 7 8" />
                      <line
                        x1="12"
                        y1="3"
                        x2="12"
                        y2="15"
                      />
                    </svg>
                    <span class="drop-text">Thả ảnh vào đây</span>
                  </div>
                </Transition>

                <!-- Image previews -->
                <div
                  v-for="(preview, idx) in imagePreviews"
                  :key="idx"
                  class="image-gallery-item"
                  :class="{ 'is-primary': idx === 0 }"
                >
                  <img
                    :src="preview"
                    class="gallery-image"
                  >
                  <span
                    v-if="idx === 0"
                    class="primary-badge"
                  >CHÍNH</span>
                  <button
                    type="button"
                    class="gallery-remove-btn"
                    title="Xóa ảnh"
                    @click="removeImage(idx)"
                  >
                    <svg
                      width="12"
                      height="12"
                      viewBox="0 0 12 12"
                      fill="none"
                    >
                      <path
                        d="M2.5 2.5L9.5 9.5M9.5 2.5L2.5 9.5"
                        stroke="white"
                        stroke-width="1.5"
                        stroke-linecap="round"
                      />
                    </svg>
                  </button>
                </div>

                <!-- Add button -->
                <button
                  v-if="imageFiles.length < 5"
                  type="button"
                  class="image-add-btn"
                  @click="triggerFileInput"
                >
                  <svg
                    width="24"
                    height="24"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="#94A3B8"
                    stroke-width="1.5"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <line
                      x1="12"
                      y1="5"
                      x2="12"
                      y2="19"
                    />
                    <line
                      x1="5"
                      y1="12"
                      x2="19"
                      y2="12"
                    />
                  </svg>
                  <span class="add-btn-text">Thêm ảnh</span>
                </button>
              </div>
              <p class="image-hint">
                Kéo thả ảnh hoặc click để chọn từ máy (tối đa 5 ảnh)
              </p>
              <input
                ref="fileInputRef"
                type="file"
                accept="image/*"
                multiple
                class="hidden-file-input"
                @change="handleFileSelect"
              >
            </div>
          </div>

          <!-- Category and Brand (2 columns) -->
          <div class="field-row">
            <div class="field-group half">
              <label class="field-label">DANH MỤC</label>
              <div class="select-wrapper">
                <select
                  v-model="productForm.category"
                  class="field-select"
                >
                  <option
                    value=""
                    disabled
                  >
                    Chọn danh mục
                  </option>
                  <option value="Áo">
                    Áo
                  </option>
                  <option value="Quần">
                    Quần
                  </option>
                  <option value="Giày">
                    Giày
                  </option>
                  <option value="Phụ kiện">
                    Phụ kiện
                  </option>
                  <option value="Balo">
                    Balo
                  </option>
                  <option value="Khác">
                    Khác
                  </option>
                </select>
                <svg
                  class="select-arrow"
                  width="21"
                  height="21"
                  viewBox="0 0 21 21"
                  fill="none"
                >
                  <path
                    d="M6.5 8.25L10.5 12.25L14.5 8.25"
                    stroke="#6B7280"
                    stroke-width="1.575"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </div>
            </div>
            <div class="field-group half">
              <label class="field-label">THƯƠNG HIỆU</label>
              <div class="select-wrapper">
                <select
                  v-model="productForm.brand"
                  class="field-select"
                >
                  <option
                    value=""
                    disabled
                  >
                    Chọn thương hiệu
                  </option>
                  <option value="Nike">
                    Nike
                  </option>
                  <option value="Adidas">
                    Adidas
                  </option>
                  <option value="Puma">
                    Puma
                  </option>
                  <option value="Li-Ning">
                    Li-Ning
                  </option>
                  <option value="ASICS">
                    ASICS
                  </option>
                  <option value="Fila">
                    Fila
                  </option>
                  <option value="Decathlon">
                    Decathlon
                  </option>
                  <option value="Mizuno">
                    Mizuno
                  </option>
                  <option value="Yonex">
                    Yonex
                  </option>
                  <option value="Khác">
                    Khác
                  </option>
                </select>
                <svg
                  class="select-arrow"
                  width="21"
                  height="21"
                  viewBox="0 0 21 21"
                  fill="none"
                >
                  <path
                    d="M6.5 8.25L10.5 12.25L14.5 8.25"
                    stroke="#6B7280"
                    stroke-width="1.575"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </div>
            </div>
          </div>

          <!-- Sport -->
          <div class="field-group">
            <label class="field-label">BỘ MÔN THỂ THAO</label>
            <div class="select-wrapper">
              <select
                v-model="productForm.sport"
                class="field-select"
              >
                <option
                  value=""
                  disabled
                >
                  Chọn bộ môn
                </option>
                <option value="Bóng đá">
                  Bóng đá
                </option>
                <option value="Chạy bộ">
                  Chạy bộ
                </option>
                <option value="Cầu lông & Pickleball">
                  Cầu lông & Pickleball
                </option>
                <option value="Cầu lông">
                  Cầu lông
                </option>
                <option value="Đua xe">
                  Đua xe
                </option>
                <option value="Bóng rổ">
                  Bóng rổ
                </option>
                <option value="Khác">
                  Khác
                </option>
              </select>
              <svg
                class="select-arrow"
                width="21"
                height="21"
                viewBox="0 0 21 21"
                fill="none"
              >
                <path
                  d="M6.5 8.25L10.5 12.25L14.5 8.25"
                  stroke="#6B7280"
                  stroke-width="1.575"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </div>
          </div>
        </div>

        <!-- Separator -->
        <div class="separator" />

        <!-- Size Management Section -->
        <div class="size-management-section">
          <div class="section-header">
            <span class="section-icon">📦</span>
            <h2 class="section-title">
              Cập nhật Số lượng theo Size
            </h2>
          </div>
          <p class="section-description">
            Chỉ cần điền số lượng cho những size mà bạn muốn bán. Để trống nếu size đó không kinh doanh.
          </p>

          <!-- Loading state -->
          <div
            v-if="isLoadingSizes"
            class="size-loading"
          >
            <div class="size-spinner" />
            <span>Đang tải danh sách size...</span>
          </div>

          <!-- Size error state -->
          <div
            v-else-if="sizeLoadError"
            class="size-error"
          >
            <span>{{ sizeLoadError }}</span>
            <button
              type="button"
              class="size-retry-btn"
              @click="fetchSizes"
            >
              Thử lại
            </button>
          </div>

          <!-- Size cards -->
          <div
            v-else-if="availableSizes.length > 0"
            class="size-grid"
          >
            <div
              v-for="size in availableSizes"
              :key="size"
              class="size-card"
            >
              <div class="size-header">
                {{ size }}
              </div>
              <input
                v-model.number="productForm.sizes[size]"
                type="number"
                class="size-input"
                placeholder="Số lượng"
                min="0"
              >
            </div>
          </div>

          <!-- No category selected -->
          <div
            v-else
            class="size-empty"
          >
            <span>Vui lòng chọn danh mục sản phẩm để hiển thị các size phù hợp.</span>
          </div>
        </div>

        <!-- Separator -->
        <div class="separator" />

        <!-- Description Section -->
        <div class="description-section">
          <label class="field-label">MÔ TẢ SẢN PHẨM</label>
          <textarea
            v-model="productForm.description"
            class="description-textarea"
            placeholder="Mô tả chi tiết về chất liệu, tính năng, và thông số kỹ thuật..."
          />
        </div>

        <!-- Action Buttons -->
        <div class="action-buttons">
          <button
            class="btn-cancel"
            @click="handleCancel"
          >
            HỦY
          </button>
          <button
            class="btn-save"
            :disabled="isSubmitting"
            @click="handleSave"
          >
            <svg
              v-if="!isSubmitting"
              width="16"
              height="16"
              viewBox="0 0 16 16"
              fill="none"
            >
              <path
                d="M13.5 4.5L6 12L2.5 8.5"
                stroke="white"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              />
            </svg>
            {{ isSubmitting ? 'ĐANG LƯU...' : 'LƯU SẢN PHẨM' }}
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import adminService from '@/services/admin.service'
import http from '@/services/http'

const props = defineProps({
  isModal: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['close', 'saved'])

const route = useRoute()
const router = useRouter()

// Determine if we're in edit mode based on URL params
const isEditMode = computed(() => !!route.params.id)

// Form data structure
const productForm = ref<Record<string, any>>({
  name: '',
  price: '',
  imageUrl: '',
  category: '',
  brand: '',
  sport: '',
  sizes: {} as Record<string, number>,
  description: ''
})

// ── Size catalog state ──────────────────────────────────────────
const availableSizes = ref<string[]>([])
const isLoadingSizes = ref(false)
const sizeLoadError = ref('')

const CATEGORY_TO_SIZE_TYPE: Record<string, string> = {
  'Áo': 'CLOTHING',
  'Quần': 'CLOTHING',
  'Giày': 'SHOES',
  'Phụ kiện': 'ACCESSORIES',
  'Balo': 'ACCESSORIES',
  'Khác': 'CUSTOM'
}

function getSizeTypeForCategory(category: string): string | undefined {
  return CATEGORY_TO_SIZE_TYPE[category]
}

async function fetchSizes() {
  const category = productForm.value.category
  if (!category) {
    availableSizes.value = []
    return
  }

  const sizeType = getSizeTypeForCategory(category)
  isLoadingSizes.value = true
  sizeLoadError.value = ''

  try {
    const response: any = await adminService.getSizeCatalog(sizeType)
    // Expect response shape: { data: [{ sizeName: 'S', sizeType: 'CLOTHING', ... }, ...] }
    // or a plain array. Handle both.
    const items: any[] = Array.isArray(response) ? response : (response?.data ?? response?.content ?? [])
    availableSizes.value = items
      .map((item: any) => item.sizeName ?? item.name ?? item)
      .filter((s: any) => typeof s === 'string' && s.trim().length > 0)
  } catch (error) {
    console.error('Failed to fetch size catalog:', error)
    sizeLoadError.value = 'Không thể tải danh sách size. Vui lòng thử lại.'
    availableSizes.value = []
  } finally {
    isLoadingSizes.value = false
  }
}

// Fetch sizes whenever the category changes
watch(
  () => productForm.value.category,
  (newCategory, oldCategory) => {
    if (newCategory && newCategory !== oldCategory) {
      productForm.value.sizes = {}
      fetchSizes()
    }
  }
)

// ── Image upload state ──────────────────────────────────────────
const imageFiles = ref<File[]>([])
const imagePreviews = ref<string[]>([])
const isDragging = ref(false)
const fileInputRef = ref<HTMLInputElement | null>(null)

const loadProductData = async (productId: number) => {
  try {
    const existingProduct = await adminService.getProduct(productId)
    if (existingProduct) {
      productForm.value = { 
        ...existingProduct,
        sizes: existingProduct.sizeQuantities || {}
      }
      if (existingProduct.images && existingProduct.images.length > 0) {
        imagePreviews.value = [...existingProduct.images]
      } else if (existingProduct.imageUrl) {
        imagePreviews.value = [existingProduct.imageUrl]
      } else {
        imagePreviews.value = []
      }
      
      // Fetch sizes for the existing product's category
      await fetchSizes()
    }
  } catch (error) {
    console.error('Failed to load product for editing:', error)
  }
}

onMounted(async () => {
  if (isEditMode.value && route.params.id) {
    const productId = parseInt(route.params.id as string)
    await loadProductData(productId)
  }
})

// Watch for route changes to reload product if needed
watch(
  () => route.params.id,
  async (newId) => {
    if (isEditMode.value && newId) {
      const productId = parseInt(newId as string)
      await loadProductData(productId)
    }
  }
)

// Image Upload Handlers
const triggerFileInput = () => {
  fileInputRef.value?.click()
}

const handleFileSelect = (event: any) => {
  const files = Array.from(event.target.files || []) as File[]
  addImageFiles(files)
  event.target.value = ''
}

const handleDragOver = (event: any) => {
  event.preventDefault()
  event.stopPropagation()
  isDragging.value = true
}

const handleDragLeave = (event: any) => {
  event.preventDefault()
  event.stopPropagation()
  isDragging.value = false
}

const handleDrop = (event: any) => {
  event.preventDefault()
  event.stopPropagation()
  isDragging.value = false
  const files = Array.from(event.dataTransfer?.files || []) as File[]
  const imageFilesOnly = files.filter((f: File) => f.type.startsWith('image/'))
  if (imageFilesOnly.length === 0) {
    alert('Vui lòng chỉ kéo thả file ảnh!')
    return
  }
  addImageFiles(imageFilesOnly)
}

const addImageFiles = (newFiles: File[]) => {
  const remainingSlots = 5 - imageFiles.value.length
  if (remainingSlots <= 0) return
  const filesToAdd = newFiles.slice(0, remainingSlots)
  for (const file of filesToAdd) {
    imageFiles.value.push(file)
    const reader = new FileReader()
    reader.onload = (e) => {
      if (e.target?.result) {
        imagePreviews.value.push(e.target.result as string)
      }
    }
    reader.readAsDataURL(file)
  }
  if (imageFiles.value.length > 0) {
    productForm.value.imageUrl = ''
  }
}

const removeImage = (index: any) => {
  imageFiles.value.splice(index, 1)
  imagePreviews.value.splice(index, 1)
}

// Handle save
const isSubmitting = ref(false)

const handleSave = async () => {
  // Validate form
  if (!productForm.value.name.trim()) {
    alert('Vui lòng nhập tên sản phẩm')
    return
  }
  if (!productForm.value.price || parseFloat(productForm.value.price.toString().replace(/\D/g, '')) <= 0) {
    alert('Vui lòng nhập giá sản phẩm hợp lệ')
    return
  }
  if (!productForm.value.category) {
    alert('Vui lòng chọn danh mục')
    return
  }

  isSubmitting.value = true

  try {
    const formData = new FormData()
    formData.append('name', productForm.value.name.trim())
    formData.append('description', productForm.value.description || '')
    formData.append('price', String(parseFloat(productForm.value.price.toString().replace(/\D/g, '')) || 0))
    formData.append('sport', productForm.value.sport || '')
    formData.append('brand', productForm.value.brand || '')
    formData.append('category', productForm.value.category || '')

    const sizesObj: Record<string, any> = {}
    for (const [size, qty] of Object.entries(productForm.value.sizes as Record<string, any>)) {
      if (qty !== null && qty > 0) {
        sizesObj[size] = qty
      }
    }
    formData.append('sizeQuantities', JSON.stringify(sizesObj))

    if (imageFiles.value.length > 0) {
      imageFiles.value.forEach(file => {
        formData.append('files', file)
      })
    }

    if (isEditMode.value && route.params.id) {
      await http.put(`/admin/products/${route.params.id}`, formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    } else {
      await http.post('/admin/products', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      })
    }

    if (props.isModal) {
      emit('saved')
      emit('close')
    } else {
      router.push('/admin/products')
    }
  } catch (error: any) {
    console.error('Failed to save product:', error)
    alert('Lưu sản phẩm thất bại: ' + (error.response?.data?.error || error.message))
  } finally {
    isSubmitting.value = false
  }
}

// Handle cancel
const handleCancel = () => {
  if (props.isModal) {
    emit('close')
  } else {
    router.push('/admin/products')
  }
}
</script>

<style scoped>
.product-edit-container {
  width: 896px;
  min-height: 981px;
  background: #FFFFFF;
  box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.05);
  border-radius: 30px;
  margin: 0 auto;
}

.form-container {
  display: flex;
  flex-direction: column;
}

/* Form Header */
.form-header {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 16px 24px;
  width: 896px;
  height: 60px;
  background: #0F172A;
  border-radius: 30px 30px 0 0;
}

.form-title {
  width: 848px;
  height: 28px;
  margin: 0;
  font-family: 'Inter', sans-serif;
  font-weight: 600;
  font-size: 18px;
  line-height: 28px;
  color: #FFFFFF;
}

/* Form Content */
.form-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 10px 32px 32px;
  gap: 10px;
  width: 896px;
  min-height: 921px;
}

/* Basic Info Section */
.basic-info-section {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 16px;
  width: 832px;
}

.field-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  width: 832px;
}

.field-row {
  display: flex;
  gap: 16px;
  width: 832px;
}

.field-group.half {
  flex: 1;
  width: auto;
}

.field-label {
  display: flex;
  align-items: center;
  width: 100%;
  height: 18px;
  font-family: 'Inter', sans-serif;
  font-weight: 600;
  font-size: 12px;
  line-height: 18px;
  letter-spacing: 0.3px;
  text-transform: uppercase;
  color: #94A3B8;
}

.field-input {
  display: flex;
  align-items: center;
  width: 100%;
  height: 46px;
  padding: 13px 14px 14px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 4px;
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  color: #475569;
  box-sizing: border-box;
}

.field-input::placeholder {
  color: #94A3B8;
}

.field-input:focus {
  outline: none;
  border-color: #0F172A;
}

/* Select */
.select-wrapper {
  position: relative;
  width: 100%;
}

.field-select {
  display: flex;
  align-items: center;
  width: 100%;
  height: 46px;
  padding: 10px 14px;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 4px;
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  color: #475569;
  appearance: none;
  cursor: pointer;
  box-sizing: border-box;
}

.field-select:focus {
  outline: none;
  border-color: #0F172A;
}

.select-arrow {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  pointer-events: none;
}

/* Size Management Section */
.size-management-section {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 4px;
  width: 832px;
}

.section-header {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 8px;
  width: 832px;
  height: 24px;
}

.section-icon {
  width: 16px;
  height: 24px;
  font-size: 16px;
  color: #334155;
}

.section-title {
  width: 197px;
  height: 20px;
  margin: 0;
  font-family: 'Geist', sans-serif;
  font-weight: 700;
  font-size: 14px;
  line-height: 20px;
  color: #1F2937;
}

.section-description {
  width: 832px;
  height: 16px;
  margin: 0;
  font-family: 'Inter', sans-serif;
  font-style: italic;
  font-weight: 400;
  font-size: 12px;
  line-height: 16px;
  color: #6B7280;
}

/* Size loading / error / empty states */
.size-loading {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 832px;
  padding: 24px 0;
  font-family: 'Inter', sans-serif;
  font-size: 13px;
  color: #64748B;
}

.size-spinner {
  width: 18px;
  height: 18px;
  border: 2px solid #E2E8F0;
  border-top-color: #0F172A;
  border-radius: 50%;
  animation: spin 0.7s linear infinite;
}

@keyframes spin {
  to { transform: rotate(360deg); }
}

.size-error {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 832px;
  padding: 16px 0;
  font-family: 'Inter', sans-serif;
  font-size: 13px;
  color: #EF4444;
}

.size-retry-btn {
  padding: 4px 14px;
  background: #0F172A;
  color: #FFF;
  border: none;
  border-radius: 4px;
  font-family: 'Inter', sans-serif;
  font-size: 12px;
  cursor: pointer;
}

.size-retry-btn:hover {
  background: #1E293B;
}

.size-empty {
  display: flex;
  align-items: center;
  width: 832px;
  padding: 24px 0;
  font-family: 'Inter', sans-serif;
  font-size: 13px;
  font-style: italic;
  color: #94A3B8;
}

/* Size grid */
.size-grid {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  width: 832px;
  margin-top: 16px;
}

.size-card {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 0 8px;
  gap: 8px;
  width: 126.67px;
  height: 80px;
  background: #FFFFFF;
  border: 1px solid #E2E8F0;
  border-radius: 4px;
}

.size-header {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 126.67px;
  height: 38px;
  background: #F8FAFC;
  border-bottom: 1px solid #E2E8F0;
  font-family: 'Inter', sans-serif;
  font-weight: 700;
  font-size: 14px;
  line-height: 21px;
  color: #334155;
}

.size-input {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 102.67px;
  height: 24px;
  padding: 4px 0 5px;
  border: none;
  background: transparent;
  font-family: 'Inter', sans-serif;
  font-size: 12px;
  color: #94A3B8;
  box-sizing: border-box;
}

.size-input:focus {
  outline: none;
}

.size-input::placeholder {
  color: #94A3B8;
}

/* Separator */
.separator {
  width: 832px;
  height: 1px;
  border-top: 1px solid #F3F4F6;
}

/* Description Section */
.description-section {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 8px;
  width: 832px;
}

.description-textarea {
  display: flex;
  align-items: flex-start;
  width: 832px;
  height: 87px;
  padding: 10px 14px 0;
  background: #F8FAFC;
  border: 1px solid #E2E8F0;
  border-radius: 4px;
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  color: #475569;
  resize: vertical;
  box-sizing: border-box;
}

.description-textarea::placeholder {
  color: #94A3B8;
}

.description-textarea:focus {
  outline: none;
  border-color: #0F172A;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  flex-direction: row;
  justify-content: flex-end;
  align-items: center;
  gap: 16px;
  width: 832px;
  padding-top: 16px;
}

.btn-cancel {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 10px 32px;
  width: 99px;
  height: 46px;
  border: 1px solid #F87171;
  border-radius: 2px;
  background: transparent;
  cursor: pointer;
  font-family: 'Geist', sans-serif;
  font-size: 16px;
  font-weight: 500;
  line-height: 24px;
  color: #EF4444;
  box-sizing: border-box;
}

.btn-cancel:hover {
  background: rgba(239, 68, 68, 0.05);
}

.btn-save {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 24px;
  width: 193px;
  height: 44px;
  background: #0F172A;
  border: none;
  border-radius: 2px;
  cursor: pointer;
  font-family: 'Geist', sans-serif;
  font-size: 16px;
  font-weight: 500;
  line-height: 24px;
  color: #FFFFFF;
  box-sizing: border-box;
}

.btn-save:hover {
  background: #1E293B;
}

/* ── Drag & Drop Image Gallery ────────────────────────────── */
.image-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(100px, 1fr));
  gap: 12px;
  width: 100%;
  position: relative;
  border-radius: 8px;
  transition: all 0.2s ease;
  min-height: 80px;
  padding: 4px;
}

.image-gallery.is-dragging {
  border: 2px dashed #0F172A;
  background: rgba(15, 23, 42, 0.03);
}

.drop-overlay {
  position: absolute;
  inset: 0;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  background: rgba(15, 23, 42, 0.06);
  border: 2px dashed #0F172A;
  border-radius: 8px;
  pointer-events: none;
}

.drop-icon {
  color: #0F172A;
}

.drop-text {
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: #0F172A;
  letter-spacing: 0.3px;
}

.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from, .fade-leave-to {
  opacity: 0;
}

.image-gallery-item {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid #E2E8F0;
  background: #F8FAFC;
}

.image-gallery-item.is-primary {
  border-color: #0F172A;
  border-width: 2px;
}

.gallery-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.primary-badge {
  position: absolute;
  bottom: 4px;
  left: 4px;
  background: #0F172A;
  color: white;
  font-family: 'Geist', sans-serif;
  font-size: 9px;
  font-weight: 700;
  padding: 2px 6px;
  border-radius: 3px;
  letter-spacing: 0.5px;
}

.gallery-remove-btn {
  position: absolute;
  top: 4px;
  right: 4px;
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: rgba(239, 68, 68, 0.9);
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0;
  opacity: 0;
  transition: opacity 0.2s;
}

.image-gallery-item:hover .gallery-remove-btn {
  opacity: 1;
}

.gallery-remove-btn:hover {
  background: #EF4444;
}

.image-add-btn {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  width: 100%;
  aspect-ratio: 1;
  border: 2px dashed #E2E8F0;
  border-radius: 8px;
  background: #F8FAFC;
  cursor: pointer;
  transition: all 0.2s ease;
}

.image-add-btn:hover {
  border-color: #94A3B8;
  background: #F1F5F9;
}

.add-btn-text {
  font-family: 'Geist', sans-serif;
  font-size: 11px;
  color: #94A3B8;
}

.image-hint {
  font-family: 'Geist', sans-serif;
  font-size: 11px;
  color: #94A3B8;
  margin-top: 4px;
}

.hidden-file-input {
  display: none;
}
</style>
