<template>
  <div class="products-manager">
    <!-- Container -->
    <div class="products-container">
      <!-- Header Section -->
      <div class="header-section">
        <div class="header-left">
          <h1 class="heading-1">
            DANH SÁCH SẢN PHẨM
          </h1>
          <p class="subtitle">
            Quản lý và theo dõi các sản phẩm trên hệ thống THE XUONG Sport.
          </p>
        </div>
        <button
          class="btn-add"
          @click="goToCreate"
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
          THÊM SẢN PHẨM MỚI
        </button>
      </div>

      <!-- Filters Section -->
      <div class="filters-section">
        <div class="search-container">
          <svg
            class="search-icon"
            width="20"
            height="20"
            viewBox="0 0 20 20"
            fill="none"
          >
            <circle
              cx="8.5"
              cy="8.5"
              r="6.5"
              stroke="#6B7280"
              stroke-width="1.66667"
            />
            <path
              d="M13.5 13.5L17.5 17.5"
              stroke="#6B7280"
              stroke-width="1.66667"
              stroke-linecap="round"
            />
          </svg>
          <input
            v-model="searchQuery"
            type="text"
            class="search-input"
            placeholder="Tìm kiếm sản phẩm..."
            @keyup.enter="onSearch"
          >
        </div>
      </div>

      <!-- Table Info & Pagination Top -->
      <div class="table-info-top">
        <span
          v-if="!isLoading"
          class="showing-text"
        >
          Hiển thị {{ (currentPage - 1) * itemsPerPage + 1 }} - {{ Math.min(currentPage * itemsPerPage, totalProducts) }} của {{ totalProducts }} sản phẩm
        </span>
        <span
          v-else
          class="showing-text"
        >Đang tải...</span>
        <div class="pagination">
          <button
            class="btn-page"
            :disabled="currentPage === 1 || isLoading"
            @click="currentPage--"
          >
            ‹
          </button>
          <button
            v-for="page in visiblePages"
            :key="page"
            class="btn-page"
            :class="{ active: currentPage === page }"
            :disabled="isLoading || page === '...'"
            @click="currentPage = Number(page)"
          >
            {{ page }}
          </button>
          <button
            class="btn-page"
            :disabled="currentPage === totalPages || isLoading"
            @click="currentPage++"
          >
            ›
          </button>
        </div>
      </div>

      <!-- Product Table -->
      <div class="product-table">
        <!-- Table Header -->
        <div class="table-header">
          <div class="header-cell cell-product">
            SẢN PHẨM
          </div>
          <div class="header-cell cell-category">
            DANH MỤC
          </div>
          <div class="header-cell cell-price">
            GIÁ NIÊM YẾT
          </div>
          <div class="header-cell cell-status">
            TRẠNG THÁI
          </div>
          <div class="header-cell cell-actions">
            THAO TÁC
          </div>
        </div>

        <!-- Table Body -->
        <div class="table-body">
          <div
            v-if="isLoading"
            class="loading-cell"
          >
            Đang tải dữ liệu...
          </div>
          <div
            v-else-if="allProducts.length === 0"
            class="empty-cell"
          >
            Không tìm thấy sản phẩm nào
          </div>
          <div
            v-for="product in allProducts"
            :key="product.id"
            class="table-row"
          >
            <div class="cell cell-product">
              <div class="product-info">
                <div class="product-image">
                  <div
                    v-if="product.image"
                    class="image-content"
                  >
                    <img
                      :src="product.image"
                      :alt="product.name"
                      loading="lazy"
                    >
                  </div>
                  <div
                    v-else
                    class="image-placeholder"
                  />
                </div>
                <div class="product-details">
                  <span class="product-name">{{ product.name }}</span>
                  <span class="product-id">ID: {{ product.id }}</span>
                </div>
              </div>
            </div>
            <div class="cell cell-category">
              {{ product.category || 'N/A' }}
            </div>
            <div class="cell cell-price">
              {{ formatPrice(product.price) }}
            </div>
            <div class="cell cell-status">
              <span
                class="status-badge"
                :class="product.active === false ? 'out-of-stock' : 'in-stock'"
              >
                {{ product.active === false ? 'ĐÃ ẨN' : 'HIỂN THỊ' }}
              </span>
            </div>
            <div class="cell cell-actions">
              <button
                class="btn-toggle-active"
                :class="product.active === false ? 'btn-show' : 'btn-hide'"
                @click="toggleActive(product.id, product.active)"
              >
                {{ product.active === false ? 'HIỆN' : 'ẨN' }}
              </button>
              <button
                class="btn-edit"
                @click="editProduct(product.id)"
              >
                SỬA
              </button>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- Liquid Glass Modal Overlay -->
    <Teleport to="body">
      <Transition name="modal-glass">
        <div
          v-if="showCreateModal"
          class="glass-overlay"
          @click.self="closeModal"
          @keydown.esc="closeModal"
        >
          <div class="glass-modal-container">
            <!-- Glass shimmer edge -->
            <div class="glass-edge-glow" />

            <!-- Close Button -->
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

            <!-- Modal scrollable content -->
            <div class="glass-modal-body">
              <AdminProductEdit
                :is-modal="true"
                @close="closeModal"
                @saved="onProductSaved"
              />
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, watch, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { useToast } from 'vue-toastification'
import adminService from '@/services/admin.service'
import AdminProductEdit from '@/views/admin/AdminProductEdit.vue'

const router = useRouter()
const toast = useToast()
const searchQuery = ref('')
const currentPage = ref(1)
const itemsPerPage = 10
const isLoading = ref(false)
const allProducts = ref<any[]>([])
const totalProducts = ref(0)
const totalPages = ref(1)
const showCreateModal = ref(false)

// Fetch products from API
const fetchProducts = async () => {
  isLoading.value = true
  try {
    const params: any = {
      page: currentPage.value - 1,
      size: itemsPerPage,
      sort: 'newest'
    }
    if (searchQuery.value.trim()) {
      params.keyword = searchQuery.value.trim()
    }
    const data = await adminService.getProducts(params)
    const items = data.content || data || []
    allProducts.value = items.map((p: any) => ({
      id: p.id,
      name: p.name,
      category: p.category || p.sport || 'N/A',
      price: p.price || p.minPrice || 0,
      image: p.imageUrl || p.image || '',
      active: p.active !== false
    }))
    totalProducts.value = data.totalElements || items.length
    totalPages.value = data.totalPages || Math.max(1, Math.ceil(totalProducts.value / itemsPerPage))
  } catch (error) {
    console.error('Failed to fetch products:', error)
    allProducts.value = []
    totalProducts.value = 0
    totalPages.value = 1
  } finally {
    isLoading.value = false
  }
}

const onSearch = () => {
  currentPage.value = 1
  fetchProducts()
}

const goToCreate = () => {
  showCreateModal.value = true
}

const closeModal = () => {
  showCreateModal.value = false
}

const onProductSaved = () => {
  toast.success('Thêm sản phẩm thành công!')
  fetchProducts()
}

// Lock body scroll when modal is open
watch(showCreateModal, (isOpen) => {
  document.body.style.overflow = isOpen ? 'hidden' : ''
})

// ESC key handler
const handleEscKey = (e: KeyboardEvent) => {
  if (e.key === 'Escape' && showCreateModal.value) {
    closeModal()
  }
}

const editProduct = (id: any) => {
  router.push(`/admin/products/${id}/edit`)
}

const deleteProduct = async (id: any) => {
  if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm này?')) return
  try {
    await adminService.deleteProduct(id)
    allProducts.value = allProducts.value.filter(p => p.id !== id)
    totalProducts.value--
    toast.success('Xóa sản phẩm thành công!')
  } catch (error: any) {
    toast.error('Xóa sản phẩm thất bại: ' + (error.response?.data?.error || error.message))
  }
}

const toggleActive = async (id: any, _currentStatus?: any) => {
  try {
    const res = await adminService.toggleProductActive(id)
    toast.success(res.message || 'Cập nhật trạng thái thành công!')
    
    // Update local state
    const product = allProducts.value.find(p => p.id === id)
    if (product) {
      product.active = !product.active
    }
  } catch (error: any) {
    toast.error('Cập nhật trạng thái thất bại: ' + (error.response?.data?.error || error.message))
  }
}

onMounted(() => {
  fetchProducts()
  document.addEventListener('keydown', handleEscKey)
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleEscKey)
  document.body.style.overflow = ''
})

watch(currentPage, () => {
  fetchProducts()
})

// Filter and Paginate logic is now handled by the server
// Total pages is fetched from server

// Visible page numbers (show max 5 pages)
const visiblePages = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value

  if (total <= 5) {
    for (let i = 1; i <= total; i++) pages.push(i)
  } else {
    if (current <= 3) {
      for (let i = 1; i <= 4; i++) pages.push(i)
      pages.push('...', total)
    } else if (current >= total - 2) {
      pages.push(1, '...')
      for (let i = total - 3; i <= total; i++) pages.push(i)
    } else {
      pages.push(1, '...')
      for (let i = current - 1; i <= current + 1; i++) pages.push(i)
      pages.push('...', total)
    }
  }
  return pages
})

// Format price
const formatPrice = (price: any) => {
  return new Intl.NumberFormat('vi-VN').format(price) + ' đ'
}
</script>

<style scoped>
.products-manager {
  width: 100%;
  min-height: 1024px;
  background: #FFFFFF;
  border-width: 0px 1px;
  border-style: solid;
  border-color: #E5E7EB;
  box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.05);
  margin: 0 auto;
}

.products-container {
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 100%;
  margin: 0 auto;
  box-sizing: border-box;
}

/* Header Section */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
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
  border-radius: 0;
  cursor: pointer;
  color: #FFFFFF;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  font-weight: 400;
  line-height: 20px;
  text-align: center;
  box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.05);
}

.btn-add:hover {
  background: #1a1a1a;
}

/* Filters Section */
.filters-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.search-container {
  position: relative;
  width: 448px;
  max-width: 448px;
  height: 40px;
}

.search-input {
  width: 100%;
  height: 100%;
  padding: 10px 12px 10px 40px;
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  border-radius: 6px;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  color: #6B7280;
  box-sizing: border-box;
}

.search-input::placeholder {
  color: #6B7280;
}

.search-input:focus {
  outline: none;
  border-color: #000000;
}

.search-icon {
  position: absolute;
  left: 12px;
  top: 50%;
  transform: translateY(-50%);
  width: 20px;
  height: 20px;
  z-index: 1;
}

/* Table Info & Pagination Top */
.table-info-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
  height: 38px;
}

.showing-text {
  height: 20px;
  font-family: 'Geist', sans-serif;
  font-weight: 400;
  font-size: 14px;
  line-height: 20px;
  color: #6B7280;
}

.pagination {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 4px;
  height: 30px;
}

.btn-page {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 4px 12px;
  min-width: 31px;
  height: 30px;
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  font-weight: 400;
  line-height: 20px;
  color: #000000;
  cursor: pointer;
  box-sizing: border-box;
}

.btn-page:hover:not(:disabled) {
  background: #F9FAFB;
}

.btn-page.active {
  background: #000000;
  border: 1px solid #000000;
  color: #FFFFFF;
}

.btn-page:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

/* Product Table */
.product-table {
  position: relative;
  top: 0;
  left: 0;
  right: 0;
  min-height: 354px;
  background: #FFFFFF;
  border: 1px solid rgba(207, 196, 197, 0.3);
  overflow: hidden;
}

.table-header {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 61px;
  border-bottom: 1px solid rgba(207, 196, 197, 0.5);
}

.header-cell {
  display: flex;
  align-items: center;
  padding: 0 24px;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
  line-height: 12px;
  letter-spacing: 0.6px;
  text-transform: uppercase;
  color: #5E5F5C;
  box-sizing: border-box;
}

.cell-product {
  flex: 1;
  min-width: 300px;
}

.cell-category {
  flex: 0 0 160px;
}

.cell-price {
  flex: 0 0 180px;
}

.cell-status {
  flex: 0 0 160px;
  justify-content: center;
}

.cell-actions {
  flex: 0 0 260px;
  justify-content: center;
  gap: 12px;
}

.table-body {
  display: flex;
  flex-direction: column;
  min-height: 293.5px;
  overflow-y: auto;
}

.table-row {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  width: 100%;
  min-height: 98px;
  border-top: 1px solid rgba(207, 196, 197, 0.2);
  box-sizing: border-box;
}

.table-row:hover {
  background: #F9FAFB;
}

.cell {
  display: flex;
  flex-direction: row;
  align-items: center;
  padding: 0 24px;
  box-sizing: border-box;
}

.loading-cell, .empty-cell {
  padding: 48px 24px;
  text-align: center;
  color: #6B7280;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
}

.cell .product-info {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16px;
  padding: 0px 24px;
}

.product-image {
  width: 64px;
  height: 64px;
  background: #F3F3F4;
  flex: none;
  overflow: hidden;
}

.product-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-placeholder {
  width: 64px;
  height: 64px;
  background: #E5E7EB;
}

.product-details {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  padding: 0px;
}

.product-name {
  font-family: 'Geist', sans-serif;
  font-weight: 700;
  font-size: 16px;
  line-height: 21px;
  color: #000000;
}

.product-id {
  font-family: 'Geist', sans-serif;
  font-weight: 400;
  font-size: 12px;
  line-height: 16px;
  color: #5E5F5C;
}

.cell-category {
  font-family: 'Geist', sans-serif;
  font-weight: 400;
  font-size: 16px;
  line-height: 21px;
  color: #5E5F5C;
}

.cell-price {
  font-family: 'Geist', sans-serif;
  font-weight: 600;
  font-size: 16px;
  line-height: 21px;
  color: #000000;
}

/* Status Badge */
.status-badge {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  padding: 4px 12px;
  width: 80px;
  height: 23px;
  border-radius: 9999px;
  font-family: 'Geist', sans-serif;
  font-size: 10px;
  font-weight: 700;
  line-height: 13px;
  letter-spacing: 1px;
  text-transform: uppercase;
}

.status-badge.in-stock {
  background: rgba(185, 226, 246, 0.2);
  border: 1px solid rgba(0, 123, 255, 0.3);
  color: #007BFF;
}

.status-badge.out-of-stock {
  background: rgba(207, 196, 197, 0.2);
  border: 1px solid rgba(126, 117, 118, 0.3);
  color: #5E5F5C;
}

/* Action Buttons */

.btn-edit {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 8px 16px;
  width: 65px;
  height: 30px;
  border: 1px solid #7E7576;
  background: transparent;
  cursor: pointer;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
  line-height: 12px;
  letter-spacing: 1.8px;
  text-transform: uppercase;
  color: #1A1C1C;
  box-sizing: border-box;
}

.btn-edit:hover {
  background: #F9FAFB;
}

.btn-toggle-active {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 8px 16px;
  width: 65px;
  height: 30px;
  border-radius: 4px;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
  line-height: 12px;
  letter-spacing: 1.8px;
  text-transform: uppercase;
  cursor: pointer;
  transition: all 0.2s ease;
}

.btn-show {
  background: #FFFFFF;
  border: 1px solid rgba(16, 185, 129, 0.5); /* Emerald 500 */
  color: #10B981;
}

.btn-show:hover {
  background: #ECFDF5;
}

.btn-hide {
  background: #FFFFFF;
  border: 1px solid rgba(245, 158, 11, 0.5); /* Amber 500 */
  color: #F59E0B;
}

.btn-hide:hover {
  background: #FFFBEB;
}

.btn-delete {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 8px 16px;
  width: 63px;
  height: 30px;
  border: 1px solid rgba(186, 26, 26, 0.4);
  background: transparent;
  cursor: pointer;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
  line-height: 12px;
  letter-spacing: 1.8px;
  text-transform: uppercase;
  color: #BA1A1A;
  box-sizing: border-box;
}

.btn-delete:hover {
  background: rgba(186, 26, 26, 0.05);
}

/* Scrollbar styling */
.table-body::-webkit-scrollbar {
  width: 8px;
}

.table-body::-webkit-scrollbar-track {
  background: #F3F3F4;
}

.table-body::-webkit-scrollbar-thumb {
  background: #D1D5DB;
  border-radius: 4px;
}

.table-body::-webkit-scrollbar-thumb:hover {
  background: #9CA3AF;
}

/* ═══════════════════════════════════════════════════════
   LIQUID GLASS MODAL (global — Teleported to body)
   ═══════════════════════════════════════════════════════ */

/* Overlay backdrop */
:global(.glass-overlay) {
  position: fixed;
  inset: 0;
  z-index: 9999;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(15, 23, 42, 0.35);
  backdrop-filter: blur(16px) saturate(140%);
  -webkit-backdrop-filter: blur(16px) saturate(140%);
  padding: 24px;
}

/* Modal Container — Liquid Glass */
:global(.glass-modal-container) {
  position: relative;
  width: 100%;
  max-width: 860px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
  border-radius: 20px;
  overflow: hidden;

  /* Multi-layer glass effect */
  background:
    linear-gradient(135deg, rgba(255,255,255,0.92) 0%, rgba(248,250,252,0.88) 50%, rgba(241,245,249,0.92) 100%);
  backdrop-filter: blur(40px) saturate(180%);
  -webkit-backdrop-filter: blur(40px) saturate(180%);

  /* Subtle glass border */
  border: 1px solid rgba(255, 255, 255, 0.45);

  /* Floating multi-layer shadow */
  box-shadow:
    0 0 0 1px rgba(15, 23, 42, 0.04),
    0 4px 6px -1px rgba(15, 23, 42, 0.06),
    0 12px 24px -4px rgba(15, 23, 42, 0.1),
    0 32px 64px -8px rgba(15, 23, 42, 0.14),
    inset 0 1px 1px rgba(255, 255, 255, 0.6);
}

/* Edge glow effect — subtle prismatic highlight */
:global(.glass-edge-glow) {
  position: absolute;
  inset: 0;
  border-radius: 20px;
  pointer-events: none;
  z-index: 1;
  background:
    linear-gradient(135deg,
      rgba(255,255,255,0.3) 0%,
      transparent 40%,
      transparent 60%,
      rgba(255,255,255,0.15) 100%
    );
  mask: linear-gradient(#fff 0 0) content-box, linear-gradient(#fff 0 0);
  mask-composite: exclude;
  -webkit-mask-composite: xor;
  padding: 1px;
}

/* Close button */
:global(.glass-close-btn) {
  position: absolute;
  top: 16px;
  right: 16px;
  z-index: 10;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 36px;
  height: 36px;
  border: none;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  color: #475569;
  cursor: pointer;
  transition: all 0.25s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow:
    0 1px 3px rgba(0, 0, 0, 0.08),
    inset 0 1px 1px rgba(255, 255, 255, 0.5);
}

:global(.glass-close-btn:hover) {
  background: rgba(239, 68, 68, 0.1);
  color: #EF4444;
  transform: rotate(90deg);
  box-shadow:
    0 2px 8px rgba(239, 68, 68, 0.15),
    inset 0 1px 1px rgba(255, 255, 255, 0.5);
}

/* Scrollable body */
:global(.glass-modal-body) {
  overflow-y: auto;
  overflow-x: hidden;
  max-height: 90vh;
  scrollbar-width: thin;
  scrollbar-color: rgba(148, 163, 184, 0.4) transparent;
}

:global(.glass-modal-body::-webkit-scrollbar) {
  width: 6px;
}

:global(.glass-modal-body::-webkit-scrollbar-track) {
  background: transparent;
}

:global(.glass-modal-body::-webkit-scrollbar-thumb) {
  background: rgba(148, 163, 184, 0.35);
  border-radius: 3px;
}

:global(.glass-modal-body::-webkit-scrollbar-thumb:hover) {
  background: rgba(148, 163, 184, 0.55);
}

/* ═══════════════════════════════════════════════════════
   MODAL TRANSITION ANIMATIONS (global — Teleported)
   ═══════════════════════════════════════════════════════ */

/* Enter */
:global(.modal-glass-enter-active) {
  transition: all 0.4s cubic-bezier(0.16, 1, 0.3, 1);
}

:global(.modal-glass-enter-active .glass-modal-container) {
  transition: all 0.45s cubic-bezier(0.16, 1, 0.3, 1);
}

:global(.modal-glass-enter-from) {
  opacity: 0;
}

:global(.modal-glass-enter-from .glass-modal-container) {
  opacity: 0;
  transform: scale(0.92) translateY(30px);
  filter: blur(4px);
}

/* Leave */
:global(.modal-glass-leave-active) {
  transition: all 0.3s cubic-bezier(0.4, 0, 1, 1);
}

:global(.modal-glass-leave-active .glass-modal-container) {
  transition: all 0.25s cubic-bezier(0.4, 0, 1, 1);
}

:global(.modal-glass-leave-to) {
  opacity: 0;
}

:global(.modal-glass-leave-to .glass-modal-container) {
  opacity: 0;
  transform: scale(0.95) translateY(16px);
  filter: blur(2px);
}
</style>
