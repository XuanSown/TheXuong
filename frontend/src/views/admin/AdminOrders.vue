<template>
  <div class="orders-manager">
    <div class="main-container">
      <!-- Toolbar & Filters Section -->
      <div class="toolbar-section">
        <div class="toolbar-content">
          <!-- Search -->
          <div class="search-container">
            <svg class="search-icon" width="18" height="18" viewBox="0 0 18 18" fill="none">
              <circle cx="8" cy="8" r="6" stroke="#5E5F5C" stroke-width="1.66667"/>
              <path d="M12.5 12.5L16.5 16.5" stroke="#5E5F5C" stroke-width="1.66667" stroke-linecap="round"/>
            </svg>
            <input
              type="text"
              v-model="searchQuery"
              class="search-input"
              placeholder="Tìm mã đơn hoặc khách hàng..."
            />
          </div>

          <!-- Filter Buttons -->
          <div class="filter-buttons">
            <button
              class="filter-btn"
              :class="{ active: statusFilter === 'all' }"
              @click="statusFilter = 'all'"
            >
              TẤT CẢ
            </button>
            <button
              class="filter-btn"
              :class="{ active: statusFilter === 'Đã đặt' }"
              @click="statusFilter = 'Đã đặt'"
            >
              ĐÃ ĐẶT
            </button>
            <button
              class="filter-btn"
              :class="{ active: statusFilter === 'Đang giao' }"
              @click="statusFilter = 'Đang giao'"
            >
              ĐANG GIAO
            </button>
            <button
              class="filter-btn"
              :class="{ active: statusFilter === 'Đã giao' }"
              @click="statusFilter = 'Đã giao'"
            >
              ĐÃ GIAO
            </button>
            <button
              class="filter-btn"
              :class="{ active: statusFilter === 'Đã hủy' }"
              @click="statusFilter = 'Đã hủy'"
            >
              ĐÃ HỦY
            </button>
          </div>
        </div>
      </div>

      <!-- Order Table -->
      <div class="order-table-container">
        <div class="table-wrapper">
          <!-- Table Header -->
          <div class="table-header">
            <div class="header-cell" style="width: 114.94px;">MÃ ĐƠN</div>
            <div class="header-cell" style="width: 143.05px;">KHÁCH HÀNG</div>
            <div class="header-cell" style="width: 134.88px;">SĐT</div>
            <div class="header-cell" style="width: 164.73px;">NGÀY ĐẶT</div>
            <div class="header-cell" style="width: 130.23px;">TỔNG TIỀN</div>
            <div class="header-cell" style="width: 126.11px;">TRẠNG THÁI</div>
            <div class="header-cell" style="width: 132px; text-align: right;">HÀNH ĐỘNG</div>
          </div>

          <!-- Table Body -->
          <div class="table-body">
            <div v-for="order in paginatedOrders" :key="order.id" class="table-row">
              <div class="cell" style="width: 114.94px;">
                <span class="order-id">{{ order.id }}</span>
              </div>
              <div class="cell" style="width: 143.05px;">
                <span class="customer-name">{{ order.customerName }}</span>
              </div>
              <div class="cell" style="width: 134.88px;">
                <span class="phone-number">{{ order.phone }}</span>
              </div>
              <div class="cell" style="width: 164.73px;">
                <span class="date">{{ order.date }}</span>
              </div>
              <div class="cell" style="width: 130.23px;">
                <span class="total-price">{{ formatPrice(order.total) }}</span>
              </div>
              <div class="cell" style="width: 126.11px;">
                <span
                  class="status-badge"
                  :class="getStatusClass(order.status)"
                >
                  {{ order.status }}
                </span>
              </div>
              <div class="cell" style="width: 132px; text-align: right;">
                <div class="action-buttons">
                  <button class="action-btn view-btn" title="Xem chi tiết">
                    <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                      <path d="M10 5C10 10 15 15 15 15C15 15 10 20 10 20C10 20 5 15 5 15C5 15 10 10 10 5Z" stroke="#9CA3AF" stroke-width="1.66667" stroke-linejoin="round"/>
                      <circle cx="10" cy="10" r="3" stroke="#9CA3AF" stroke-width="1.66667"/>
                    </svg>
                  </button>
                  <button class="action-btn edit-btn" title="Chỉnh sửa" @click="editOrder(order.id)">
                    <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                      <path d="M17.5 3.75L16.25 2.5M14.58 5.42L15.75 6.59C16.63 7.47 17.08 8.82 16.82 10.25C16.56 11.68 15.68 12.91 14.55 13.54L5.24 22.85C4.75 23.34 4.02 23.44 3.41 23.17C2.8 22.9 2.46 22.09 2.59 21.46L3.76 15.05" stroke="#9CA3AF" stroke-width="1.66667" stroke-linecap="round" stroke-linejoin="round"/>
                    </svg>
                  </button>
                  <button class="action-btn delete-btn" title="Xóa" @click="deleteOrder(order.id)">
                    <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                      <path d="M4 4.375L16 16.25M4 16.25L16 4.375" stroke="#9CA3AF" stroke-width="1.66667" stroke-linecap="round"/>
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- Pagination -->
        <div class="pagination-section">
          <div class="pagination-info">
            Hiển thị {{ startIndex + 1 }} - {{ Math.min(endIndex, filteredOrders.length) }} của {{ filteredOrders.length }} kết quả
          </div>
          <div class="pagination-controls">
            <button
              class="page-btn prev-btn"
              :disabled="currentPage === 1"
              @click="currentPage--"
            >
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M12.5 5L7.5 10L12.5 15" stroke="currentColor" stroke-width="1.66667" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>

            <button
              v-for="page in visiblePages"
              :key="page"
              class="page-btn page-number"
              :class="{ active: currentPage === page }"
              @click="currentPage = page"
            >
              {{ page === '...' ? '...' : page }}
            </button>

            <button
              class="page-btn next-btn"
              :disabled="currentPage === totalPages"
              @click="currentPage++"
            >
              <svg width="20" height="20" viewBox="0 0 20 20" fill="none">
                <path d="M7.5 5L12.5 10L7.5 15" stroke="currentColor" stroke-width="1.66667" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()

// Search and filter state
const searchQuery = ref('')
const statusFilter = ref('all')
const currentPage = ref(1)
const itemsPerPage = 4

// Mock order data
const orders = ref([
  {
    id: '#TX-1005',
    customerName: 'Nguyễn Văn A',
    phone: '0901234567',
    date: '24/10/2023 14:30',
    total: 2500000,
    status: 'Đã đặt'
  },
  {
    id: '#TX-1004',
    customerName: 'Trần Thị B',
    phone: '0987654321',
    date: '23/10/2023 09:15',
    total: 850000,
    status: 'Đang giao'
  },
  {
    id: '#TX-1003',
    customerName: 'Lê Văn C',
    phone: '0912345678',
    date: '22/10/2023 16:45',
    total: 1200000,
    status: 'Đã giao'
  },
  {
    id: '#TX-1002',
    customerName: 'Phạm Thị D',
    phone: '0933445566',
    date: '21/10/2023 10:00',
    total: 3100000,
    status: 'Đã hủy'
  },
  {
    id: '#TX-1001',
    customerName: 'Hoàng Văn E',
    phone: '0977889900',
    date: '20/10/2023 08:20',
    total: 1800000,
    status: 'Đã đặt'
  },
  {
    id: '#TX-1000',
    customerName: 'Vũ Thị F',
    phone: '0966554433',
    date: '19/10/2023 15:45',
    total: 4200000,
    status: 'Đang giao'
  },
  {
    id: '#TX-999',
    customerName: 'Đỗ Văn G',
    phone: '0944333221',
    date: '18/10/2023 11:30',
    total: 980000,
    status: 'Đã giao'
  },
  {
    id: '#TX-998',
    customerName: 'Mai Thị H',
    phone: '0922113344',
    date: '17/10/2023 14:15',
    total: 5600000,
    status: 'Đã đặt'
  }
])

// Computed: Filtered orders
const filteredOrders = computed(() => {
  let result = orders.value

  // Filter by search query
  if (searchQuery.value.trim()) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(
      o =>
        o.id.toLowerCase().includes(query) ||
        o.customerName.toLowerCase().includes(query) ||
        o.phone.includes(query)
    )
  }

  // Filter by status
  if (statusFilter.value !== 'all') {
    result = result.filter(o => o.status === statusFilter.value)
  }

  return result
})

// Computed: Pagination
const totalPages = computed(() => Math.max(1, Math.ceil(filteredOrders.value.length / itemsPerPage)))

const startIndex = computed(() => (currentPage.value - 1) * itemsPerPage)
const endIndex = computed(() => startIndex.value + itemsPerPage)

const paginatedOrders = computed(() => {
  return filteredOrders.value.slice(startIndex.value, endIndex.value)
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

// Get status badge class
const getStatusClass = (status) => {
  switch (status) {
    case 'Đã đặt':
      return 'status-placed'
    case 'Đang giao':
      return 'status-delivering'
    case 'Đã giao':
      return 'status-delivered'
    case 'Đã hủy':
      return 'status-cancelled'
    default:
      return ''
  }
}

// Format price
const formatPrice = (price) => {
  return new Intl.NumberFormat('vi-VN').format(price) + ' đ'
}

// Action handlers
const editOrder = (orderId) => {
  console.log('Edit order:', orderId)
  // Navigate to edit page or open modal
}

const deleteOrder = (orderId) => {
  if (confirm('Bạn có chắc chắn muốn xóa đơn hàng này?')) {
    console.log('Delete order:', orderId)
    // Implement delete logic
  }
}
</script>

<style scoped>
.orders-manager {
  width: 1280px;
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
  position: absolute;
  left: 267px;
  right: 16px;
  top: 13px;
  height: 131px;
  background: #FFFFFF;
  border: 1px solid #E2E2E2;
  box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.05);
  border-radius: 12px;
  padding: 24px;
}

.toolbar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 947px;
  height: 73px;
}

/* Search */
.search-container {
  position: relative;
  width: 373.25px;
  height: 50.59px;
}

.search-input {
  width: 100%;
  height: 100%;
  padding: 14px 16px 15.59px 48px;
  background: #F3F3F3;
  border: none;
  border-radius: 9999px;
  font-family: 'Geist', sans-serif;
  font-size: 16px;
  color: #6B7280;
  box-sizing: border-box;
}

.search-input::placeholder {
  color: #6B7280;
}

.search-input:focus {
  outline: none;
  box-shadow: 0 0 0 2px rgba(0, 0, 0, 0.1);
}

.search-icon {
  position: absolute;
  left: 16px;
  top: 50%;
  transform: translateY(-50%);
  width: 18px;
  height: 18px;
}

/* Filter Buttons */
.filter-buttons {
  display: flex;
  gap: 8px;
  width: 448.75px;
  height: 73px;
  position: relative;
}

.filter-btn {
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 16px;
  height: 33px;
  background: #EEEEEE;
  border: none;
  border-radius: 8px;
  cursor: pointer;
  font-family: 'Gelasio', serif;
  font-size: 11px;
  font-weight: 400;
  line-height: 16px;
  letter-spacing: 1.1px;
  text-transform: uppercase;
  color: #1A1C1C;
  transition: all 0.2s;
}

.filter-btn:hover {
  background: #E5E7EB;
}

.filter-btn.active {
  background: #000000;
  color: #FFFFFF;
}

.filter-btn:nth-child(1) { left: 0px; top: 0px; width: 76px; }
.filter-btn:nth-child(2) { left: 87.11px; top: 0px; width: 79px; }
.filter-btn:nth-child(3) { left: 175.25px; top: 0px; width: 104px; }
.filter-btn:nth-child(4) { left: 287.2px; top: 0px; width: 85px; }
.filter-btn:nth-child(5) { left: 0px; top: 40.5px; width: 80px; }

/* Order Table */
.order-table-container {
  position: absolute;
  left: 267px;
  right: 26px;
  top: 163px;
  bottom: 163px;
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.05);
  border-radius: 16px;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.table-wrapper {
  flex: 1;
  overflow-y: auto;
}

.table-header {
  display: flex;
  width: 985px;
  min-width: 934px;
  height: 64.5px;
  background: #F9FAFB;
  border-bottom: 1px solid #E5E7EB;
  position: sticky;
  top: 0;
  z-index: 1;
}

.header-cell {
  display: flex;
  align-items: center;
  padding: 23.5px 24px 25px;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
  line-height: 16px;
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
  width: 985px;
  min-width: 934px;
  height: 57px;
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

.order-id {
  font-family: 'Geist', sans-serif;
  font-weight: 500;
  font-size: 14px;
  line-height: 20px;
  color: #111827;
}

.customer-name {
  font-family: 'Inter', sans-serif;
  font-weight: 400;
  font-size: 14px;
  line-height: 20px;
  color: #374151;
}

.phone-number {
  font-family: 'Geist', sans-serif;
  font-weight: 400;
  font-size: 14px;
  line-height: 20px;
  color: #6B7280;
}

.date {
  font-family: 'Geist', sans-serif;
  font-weight: 400;
  font-size: 14px;
  line-height: 20px;
  color: #6B7280;
}

.total-price {
  font-family: 'Geist', sans-serif;
  font-weight: 500;
  font-size: 14px;
  line-height: 20px;
  color: #111827;
}

/* Status Badges */
.status-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 10px;
  border-radius: 9999px;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 500;
  line-height: 16px;
}

.status-placed {
  background: #DBEAFE;
  color: #1E40AF;
}

.status-delivering {
  background: #FEF9C3;
  color: #854D0E;
}

.status-delivered {
  background: #DCFCE7;
  color: #166534;
}

.status-cancelled {
  background: #FEE2E2;
  color: #991B1B;
}

/* Action Buttons */
.action-buttons {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 12px;
  margin-left: auto;
}

.action-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 20px;
  height: 20px;
  background: transparent;
  border: none;
  cursor: pointer;
  padding: 0;
}

.action-btn:hover svg path {
  stroke: #374151;
}

/* Pagination */
.pagination-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px 24px;
  width: 985px;
  height: 71px;
  background: #FFFFFF;
  border-top: 1px solid #E5E7EB;
  box-sizing: border-box;
}

.pagination-info {
  width: 198px;
  height: 20px;
  font-family: 'Geist', sans-serif;
  font-weight: 400;
  font-size: 14px;
  line-height: 20px;
  color: #374151;
}

.pagination-controls {
  position: relative;
  width: 242.98px;
  height: 38px;
  background: rgba(255, 255, 255, 0.002);
  box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.05);
  border-radius: 6px;
}

.page-btn {
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #FFFFFF;
  border: 1px solid #D1D5DB;
  cursor: pointer;
  font-family: 'Inter', sans-serif;
  font-size: 14px;
  font-weight: 500;
  line-height: 20px;
  color: #6B7280;
  box-sizing: border-box;
  transition: all 0.2s;
}

.page-btn:hover:not(:disabled) {
  background: #F9FAFB;
}

.page-btn.active {
  background: #000000;
  border-color: #000000;
  color: #FFFFFF;
}

.page-btn:disabled {
  cursor: not-allowed;
  opacity: 0.5;
}

.page-btn.prev-btn {
  left: 0;
  top: 0;
  bottom: 0;
  width: 38px;
  height: 38px;
  padding: 8px;
  border-radius: 6px 0 0 6px;
}

.page-btn.next-btn {
  left: 204.98px;
  top: 0;
  bottom: 0;
  width: 38px;
  height: 38px;
  padding: 8px;
  border-radius: 0 6px 6px 0;
}

.page-btn.page-number {
  height: 38px;
  padding: 7.5px 16px 8.5px;
}

.page-btn.page-number:nth-of-type(2) { left: 38px; width: 42.64px; }
.page-btn.page-number:nth-of-type(3) { left: 80.64px; width: 42.64px; }
.page-btn.page-number:nth-of-type(4) { left: 122.28px; width: 42.78px; }
.page-btn.page-number:nth-of-type(5) { left: 164.06px; width: 41.92px; }

.page-btn.page-number.active {
  background: #000000;
  border-color: #000000;
  color: #FFFFFF;
}

/* Scrollbar */
.table-wrapper::-webkit-scrollbar {
  width: 8px;
}

.table-wrapper::-webkit-scrollbar-track {
  background: #F3F3F4;
}

.table-wrapper::-webkit-scrollbar-thumb {
  background: #D1D5DB;
  border-radius: 4px;
}

.table-wrapper::-webkit-scrollbar-thumb:hover {
  background: #9CA3AF;
}
</style>
