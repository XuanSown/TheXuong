<template>
  <div class="product-edit-container">
    <div class="form-container">
      <!-- Form Header -->
      <div class="form-header">
        <h1 class="form-title">{{ isEditMode ? 'SỬA SẢN PHẨM' : 'THÊM SẢN PHẨM MỚI' }}</h1>
      </div>

      <!-- Form Content -->
      <div class="form-content">
        <!-- Basic Info Section -->
        <div class="basic-info-section">
          <!-- Product Name -->
          <div class="field-group">
            <label class="field-label">TÊN SẢN PHẨM</label>
            <input
              type="text"
              v-model="productForm.name"
              class="field-input"
              placeholder="Nhập tên sản phẩm thể thao..."
            />
          </div>

          <!-- Price and Image URL (2 columns) -->
          <div class="field-row">
            <div class="field-group half">
              <label class="field-label">GIÁ (VNĐ)</label>
              <input
                type="text"
                v-model="productForm.price"
                class="field-input"
                placeholder="500.000"
              />
            </div>
            <div class="field-group half">
              <label class="field-label">ĐƯỜNG DẪN HÌNH ẢNH (URL)</label>
              <input
                type="text"
                v-model="productForm.imageUrl"
                class="field-input"
                placeholder="https://example.com/image.jpg"
              />
            </div>
          </div>

          <!-- Category and Brand (2 columns) -->
          <div class="field-row">
            <div class="field-group half">
              <label class="field-label">DANH MỤC</label>
              <div class="select-wrapper">
                <select v-model="productForm.category" class="field-select">
                  <option value="" disabled>Chọn danh mục</option>
                  <option value="Footwear">Footwear</option>
                  <option value="Apparel">Apparel</option>
                  <option value="Accessories">Accessories</option>
                  <option value="Equipment">Equipment</option>
                </select>
                <svg class="select-arrow" width="21" height="21" viewBox="0 0 21 21" fill="none">
                  <path d="M6.5 8.25L10.5 12.25L14.5 8.25" stroke="#6B7280" stroke-width="1.575" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </div>
            </div>
            <div class="field-group half">
              <label class="field-label">THƯƠNG HIỆU</label>
              <input
                type="text"
                v-model="productForm.brand"
                class="field-input"
                placeholder="Nike, Adidas, Puma..."
              />
            </div>
          </div>

          <!-- Sport -->
          <div class="field-group">
            <label class="field-label">BỘ MÔN THỂ THAO</label>
            <input
              type="text"
              v-model="productForm.sport"
              class="field-input"
              placeholder="Bóng đá, Chạy bộ, Gym..."
            />
          </div>
        </div>

        <!-- Separator -->
        <div class="separator"></div>

        <!-- Size Management Section -->
        <div class="size-management-section">
          <div class="section-header">
            <span class="section-icon">📦</span>
            <h2 class="section-title">Cập nhật Số lượng theo Size</h2>
          </div>
          <p class="section-description">Chỉ cần điền số lượng cho những size mà bạn muốn bán. Để trống nếu size đó không kinh doanh.</p>

          <div class="size-grid">
            <!-- Size cards for each size -->
            <div v-for="(quantity, size) in productForm.sizes" :key="size" class="size-card">
              <div class="size-header">{{ size }}</div>
              <input
                type="number"
                v-model.number="productForm.sizes[size]"
                class="size-input"
                :placeholder="quantity !== null ? quantity : 'Số lượng'"
                min="0"
              />
            </div>
          </div>
        </div>

        <!-- Separator -->
        <div class="separator"></div>

        <!-- Description Section -->
        <div class="description-section">
          <label class="field-label">MÔ TẢ SẢN PHẨM</label>
          <textarea
            v-model="productForm.description"
            class="description-textarea"
            placeholder="Mô tả chi tiết về chất liệu, tính năng, và thông số kỹ thuật..."
          ></textarea>
        </div>

        <!-- Action Buttons -->
        <div class="action-buttons">
          <button class="btn-cancel" @click="handleCancel">HỦY</button>
          <button class="btn-save" @click="handleSave">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
              <path d="M13.5 4.5L6 12L2.5 8.5" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            LƯU SẢN PHẨM
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

// Determine if we're in edit mode
const isEditMode = computed(() => route.name === 'admin-product-edit')

// Form data structure
const productForm = ref({
  name: '',
  price: '',
  imageUrl: '',
  category: '',
  brand: '',
  sport: '',
  sizes: {
    'S': null,
    'M': null,
    'L': null,
    'XL': null,
    'XXL': null
  },
  description: ''
})

// Mock product data for editing
const mockProducts = {
  1: {
    name: 'Giày Đá Bóng Nike Air Zoom',
    price: '2450000',
    imageUrl: '',
    category: 'Footwear',
    brand: 'Nike',
    sport: 'Bóng đá',
    sizes: { 'S': 10, 'M': 25, 'L': 30, 'XL': 15, 'XXL': 5 },
    description: 'Giày đá bóng cao cấp với công nghệ Air Zoom, mang lại cảm giác thoải mái và hỗ trợ tối ưu trong thi đấu.'
  },
  2: {
    name: 'Mũ Trucker Stadium',
    price: '450000',
    imageUrl: '',
    category: 'Accessories',
    brand: 'Nike',
    sport: 'Bóng đá',
    sizes: { 'S': 50, 'M': 80, 'L': 60, 'XL': null, 'XXL': null },
    description: 'Mũ lưỡi trai thể thao thiết kế hiện đại, chất liệu cotton thoáng mát.'
  }
})

// Load product data if editing
onMounted(() => {
  if (isEditMode.value && route.params.id) {
    const productId = parseInt(route.params.id)
    const existingProduct = mockProducts[productId]
    if (existingProduct) {
      productForm.value = { ...existingProduct }
    }
  }
})

// Watch for route changes to reload product if needed
watch(
  () => route.params.id,
  (newId) => {
    if (isEditMode.value && newId) {
      const productId = parseInt(newId)
      const existingProduct = mockProducts[productId]
      if (existingProduct) {
        productForm.value = { ...existingProduct }
      }
    }
  }
)

// Format price for display
const formatPrice = (value) => {
  if (!value) return ''
  return new Intl.NumberFormat('vi-VN').format(value)
}

// Handle save
const handleSave = () => {
  // Validate form
  if (!productForm.value.name.trim()) {
    alert('Vui lòng nhập tên sản phẩm')
    return
  }
  if (!productForm.value.price || parseFloat(productForm.value.price) <= 0) {
    alert('Vui lòng nhập giá sản phẩm hợp lệ')
    return
  }
  if (!productForm.value.category) {
    alert('Vui lòng chọn danh mục')
    return
  }

  // Here you would typically send data to backend API
  const productData = {
    ...productForm.value,
    price: parseFloat(productForm.value.price.toString().replace(/\D/g, '')) || 0
  }
  console.log('Saving product:', productData)

  // Navigate back to products list
  router.push('/admin/products')
}

// Handle cancel
const handleCancel = () => {
  router.push('/admin/products')
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
</style>
