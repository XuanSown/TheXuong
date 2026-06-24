<template>
  <div class="products-manager">
    <!-- Container -->
    <div class="container">
      <!-- Header Section -->
      <div class="header-section">
        <div class="header-left">
          <h1 class="heading-1">DANH SÁCH SẢN PHẨM</h1>
          <p class="subtitle">Quản lý và theo dõi các sản phẩm trên hệ thống THE XUONG Sport.</p>
        </div>
        <button class="btn-add">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none">
            <path d="M8 3V13M3 8H13" stroke="white" stroke-width="2" stroke-linecap="round"/>
          </svg>
          THÊM SẢN PHẨM MỚI
        </button>
      </div>

      <!-- Filters Section -->
      <div class="filters-section">
        <div class="search-container">
          <svg class="search-icon" width="20" height="20" viewBox="0 0 20 20" fill="none">
            <circle cx="8.5" cy="8.5" r="6.5" stroke="#6B7280" stroke-width="1.66667"/>
            <path d="M13.5 13.5L17.5 17.5" stroke="#6B7280" stroke-width="1.66667" stroke-linecap="round"/>
          </svg>
          <input
            type="text"
            v-model="searchQuery"
            class="search-input"
            placeholder="Tìm kiếm sản phẩm..."
          />
        </div>
        <div class="filter-select">
          <svg class="filter-icon" width="21" height="21" viewBox="0 0 21 21" fill="none">
            <rect x="2.5" y="2.5" width="16" height="16" rx="2.5" stroke="#6B7280" stroke-width="1.575"/>
            <path d="M7.5 7.5V14.5M10.5 7.5V14.5M13.5 7.5V14.5" stroke="#6B7280" stroke-width="1.575" stroke-linecap="round"/>
          </svg>
          <span class="filter-text">Tất cả trạng thái</span>
        </div>
      </div>

      <!-- Table Info & Pagination Top -->
      <div class="table-info-top">
        <span class="showing-text">Hiển thị 1 - 5 của 128 sản phẩm</span>
        <div class="pagination">
          <button class="btn-page" :disabled="currentPage === 1" @click="currentPage--">
            ‹
          </button>
          <button
            v-for="page in visiblePages"
            :key="page"
            class="btn-page"
            :class="{ active: currentPage === page }"
            @click="currentPage = page"
          >
            {{ page === '...' ? '...' : page }}
          </button>
          <button class="btn-page" :disabled="currentPage === totalPages" @click="currentPage++">
            ›
          </button>
        </div>
      </div>

      <!-- Product Table -->
      <div class="product-table">
        <!-- Table Header -->
        <div class="table-header">
          <div class="header-cell cell-product">SẢN PHẨM</div>
          <div class="header-cell cell-category">DANH MỤC</div>
          <div class="header-cell cell-price">GIÁ NIÊM YẾT</div>
          <div class="header-cell cell-status">TRẠNG THÁI</div>
          <div class="header-cell cell-actions">THAO TÁC</div>
        </div>

        <!-- Table Body -->
        <div class="table-body">
          <div v-for="product in paginatedProducts" :key="product.id" class="table-row">
            <div class="cell cell-product">
              <div class="product-info">
                <div class="product-image">
                  <div v-if="product.image" class="image-content">
                    <img :src="product.image" :alt="product.name" />
                  </div>
                  <div v-else class="image-placeholder"></div>
                </div>
                <div class="product-details">
                  <span class="product-name">{{ product.name }}</span>
                  <span class="product-id">ID: {{ product.id }}</span>
                </div>
              </div>
            </div>
            <div class="cell cell-category">{{ product.category }}</div>
            <div class="cell cell-price">{{ formatPrice(product.price) }}</div>
            <div class="cell cell-status">
              <span
                class="status-badge"
                :class="product.status === 'IN STOCK' ? 'in-stock' : 'out-of-stock'"
              >
                {{ product.status }}
              </span>
            </div>
            <div class="cell cell-actions">
              <button class="btn-edit">SỬA</button>
              <button class="btn-delete">XÓA</button>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'

// Search and pagination state
const searchQuery = ref('')
const currentPage = ref(1)
const itemsPerPage = 5

// Mock product data
const products = ref([
  {
    id: 1,
    name: 'Giày Đá Bóng Nike Air Zoom',
    category: 'Footwear',
    price: 2450000,
    status: 'IN STOCK',
    image: ''
  },
  {
    id: 2,
    name: 'Mũ Trucker Stadium',
    category: 'Accessories',
    price: 450000,
    status: 'OUT OF STOCK',
    image: ''
  },
  {
    id: 3,
    name: 'Áo Tập Pro Combat',
    category: 'Apparel',
    price: 890000,
    status: 'IN STOCK',
    image: ''
  },
  {
    id: 4,
    name: 'Bóng Đá Size 5',
    category: 'Equipment',
    price: 320000,
    status: 'IN STOCK',
    image: ''
  },
  {
    id: 5,
    name: 'Tất Chuyên Gôn Nike',
    category: 'Accessories',
    price: 89000,
    status: 'IN STOCK',
    image: ''
  },
  {
    id: 6,
    name: 'Giày Tennis Adidas',
    category: 'Footwear',
    price: 2100000,
    status: 'OUT OF STOCK',
    image: ''
  },
  {
    id: 7,
    name: 'Quần Short Training',
    category: 'Apparel',
    price: 350000,
    status: 'IN STOCK',
    image: ''
  },
  {
    id: 8,
    name: 'Balo Thể Thao',
    category: 'Accessories',
    price: 580000,
    status: 'IN STOCK',
    image: ''
  }
])

// Computed: Filtered products based on search
const filteredProducts = computed(() => {
  if (!searchQuery.value.trim()) {
    return products.value
  }
  const query = searchQuery.value.toLowerCase()
  return products.value.filter(
    p =>
      p.name.toLowerCase().includes(query) ||
      p.category.toLowerCase().includes(query) ||
      p.id.toString().includes(query)
  )
})

// Computed: Pagination
const totalPages = computed(() => Math.max(1, Math.ceil(filteredProducts.value.length / itemsPerPage)))

const startIndex = computed(() => (currentPage.value - 1) * itemsPerPage)
const endIndex = computed(() => startIndex.value + itemsPerPage)

const paginatedProducts = computed(() => {
  return filteredProducts.value.slice(startIndex.value, endIndex.value)
})

// Visible page numbers (show max 5 pages)
const visiblePages = computed(() => {
  const pages = []
  const total = totalPages.value
  const current = currentPage.value

  if (total <= 5) {
    for (let i = 1; i <= total; i++) {
      pages.push(i)
    }
  } else {
    if (current <= 3) {
      for (let i = 1; i <= 4; i++) {
        pages.push(i)
      }
      pages.push('...', total)
    } else if (current >= total - 2) {
      pages.push(1, '...')
      for (let i = total - 3; i <= total; i++) {
        pages.push(i)
      }
    } else {
      pages.push(1, '...')
      for (let i = current - 1; i <= current + 1; i++) {
        pages.push(i)
      }
      pages.push('...', total)
    }
  }
  return pages
})

// Watch for search changes to reset pagination
const handleSearch = () => {
  currentPage.value = 1
}

// Format price
const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN').format(price) + ' đ'
}
</script>

<style scoped>
.products-manager {
  width: 1280px;
  min-height: 1024px;
  background: linear-gradient(0deg, #FFFFFF, #FFFFFF), linear-gradient(0deg, #F9FAFB, #F9FAFB), #FFFFFF;
  border-width: 0px 1px;
  border-style: solid;
  border-color: #E5E7EB;
  box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.05);
  margin: 0 auto;
}

.container {
  padding: 32px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  width: 1022px;
  height: 254px;
  margin: 0 auto;
  isolation: isolate;
}

/* Header Section */
.header-section {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 320.81px;
  width: 958px;
  height: 64px;
  z-index: 0;
}

.header-left {
  display: flex;
  flex-direction: column;
  gap: 8px;
  width: 426px;
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
  width: 216px;
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
  padding-top: 16px;
  gap: 318px;
  width: 958px;
  height: 56px;
  z-index: 1;
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

.filter-select {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  position: relative;
  padding: 8px 40px 8px 12px;
  width: 192px;
  height: 38px;
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  border-radius: 6px;
  isolation: isolate;
  cursor: pointer;
}

.filter-icon {
  position: absolute;
  left: 9px;
  top: 50%;
  transform: translateY(-50%);
  width: 21px;
  height: 21px;
}

.filter-text {
  margin-left: 24px;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  font-weight: 400;
  line-height: 20px;
  color: #000000;
}

/* Table Info & Pagination Top */
.table-info-top {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 8px;
  width: 958px;
  height: 38px;
  z-index: 2;
}

.showing-text {
  width: 198px;
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
  width: 180px;
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
  position: absolute;
  top: 258px;
  left: 0;
  right: 0;
  height: 354px;
  background: #FFFFFF;
  border: 1px solid rgba(207, 196, 197, 0.3);
  overflow: hidden;
  z-index: 3;
}

.table-header {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: flex-start;
  width: 712px;
  height: 61px;
  border-bottom: 1px solid rgba(207, 196, 197, 0.5);
  margin: -1px 0px;
}

.header-cell {
  display: flex;
  align-items: center;
  padding: 24px;
  font-family: 'Gelasio', serif;
  font-size: 12px;
  font-weight: 600;
  line-height: 12px;
  letter-spacing: 0.6px;
  text-transform: uppercase;
  color: #5E5F5C;
}

.cell-product {
  flex: 0 0 405.67px;
  margin: 0px -30px;
}

.cell-category {
  flex: 0 0 165.02px;
  margin: 0px -30px;
}

.cell-price {
  flex: 0 0 168.53px;
  margin: 0px -30px;
}

.cell-status {
  flex: 0 0 188.64px;
  margin: 0px -30px;
  justify-content: center;
}

.cell-actions {
  flex: 0 0 222.14px;
  justify-content: flex-end;
}

.table-body {
  display: flex;
  flex-direction: column;
  position: absolute;
  left: 154px;
  right: 156px;
  top: 61px;
  bottom: 0;
  height: 293.5px;
  overflow-y: auto;
}

.table-row {
  display: flex;
  flex-direction: row;
  justify-content: center;
  align-items: center;
  width: 712px;
  min-height: 98px;
  border-top: 1px solid rgba(207, 196, 197, 0.2);
  margin: -1px 0px;
  box-sizing: border-box;
}

.table-row:hover {
  background: #F9FAFB;
}

.cell {
  display: flex;
  flex-direction: row;
  align-items: center;
  box-sizing: border-box;
}

.cell .product-info {
  display: flex;
  flex-direction: row;
  align-items: center;
  gap: 16px;
  padding: 0px 24px;
  width: 357.67px;
  margin: 0px -30px;
}

.product-image {
  width: 64px;
  height: 64px;
  background: #F3F3F4;
  flex: none;
  order: 0;
  flex-grow: 0;
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
  width: 215px;
  height: 37px;
}

.product-name {
  width: 215px;
  height: 21px;
  font-family: 'Geist', sans-serif;
  font-weight: 700;
  font-size: 16px;
  line-height: 21px;
  color: #000000;
}

.product-id {
  width: 215px;
  height: 16px;
  font-family: 'Geist', sans-serif;
  font-weight: 400;
  font-size: 12px;
  line-height: 16px;
  color: #5E5F5C;
}

.cell-category {
  padding: 38.5px 24px 38.5px 48px;
  width: 189.02px;
  margin: 0px -30px;
  font-family: 'Geist', sans-serif;
  font-weight: 400;
  font-size: 16px;
  line-height: 21px;
  color: #5E5F5C;
}

.cell-price {
  padding: 38.5px 24px;
  width: 168.53px;
  margin: 0px -30px;
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
.cell-actions {
  display: flex;
  flex-direction: row;
  justify-content: flex-end;
  align-items: flex-start;
  padding: 0px 0px 0px 24px;
  gap: 12px;
  width: 198.14px;
  height: 30px;
}

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
</style>
