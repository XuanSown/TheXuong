<template>
  <div class="orders-manager">
    <div class="main-container">
      <!-- Toolbar & Filters Section -->
      <div class="toolbar-section">
        <div class="toolbar-content">
          <!-- Search -->
          <div class="search-container">
            <svg
              class="search-icon"
              width="18"
              height="18"
              viewBox="0 0 18 18"
              fill="none"
            >
              <circle
                cx="8"
                cy="8"
                r="6"
                stroke="#5E5F5C"
                stroke-width="1.66667"
              />
              <path
                d="M12.5 12.5L16.5 16.5"
                stroke="#5E5F5C"
                stroke-width="1.66667"
                stroke-linecap="round"
              />
            </svg>
            <input
              v-model="searchQuery"
              type="text"
              class="search-input"
              placeholder="Tìm mã đơn hoặc khách hàng..."
              @input="onSearch"
            >
          </div>

          <!-- Filter Buttons -->
          <div class="filter-buttons">
            <button
              v-for="filter in filters"
              :key="filter.value"
              class="filter-btn"
              :class="{ active: statusFilter === filter.value }"
              @click="applyFilter(filter.value)"
            >
              {{ filter.label }}
            </button>
          </div>
        </div>
      </div>

      <!-- Order Table -->
      <div class="order-table-container">
        <div class="table-wrapper">
          <!-- Table Header -->
          <div class="table-header">
            <div
              class="header-cell"
              style="width: 10%;"
            >
              MÃ ĐƠN
            </div>
            <div
              class="header-cell"
              style="width: 16%;"
            >
              KHÁCH HÀNG
            </div>
            <div
              class="header-cell"
              style="width: 12%;"
            >
              SĐT
            </div>
            <div
              class="header-cell"
              style="width: 15%;"
            >
              NGÀY ĐẶT
            </div>
            <div
              class="header-cell"
              style="width: 13%;"
            >
              THANH TOÁN
            </div>
            <div
              class="header-cell"
              style="width: 13%;"
            >
              TỔNG TIỀN
            </div>
            <div
              class="header-cell"
              style="width: 13%;"
            >
              TRẠNG THÁI
            </div>
            <div
              class="header-cell"
              style="width: 8%; justify-content: flex-end;"
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
              v-else-if="orders.length === 0"
              class="empty-cell"
            >
              Không có đơn hàng nào
            </div>
            <div v-else>
              <div
                v-for="order in paginatedOrders"
                :key="order.id"
                class="table-row"
              >
                <div
                  class="cell"
                  style="width: 10%;"
                >
                  <span class="order-id">{{ order.id }}</span>
                </div>
                <div
                  class="cell"
                  style="width: 16%;"
                >
                  <span class="customer-name">{{ order.customerName }}</span>
                </div>
                <div
                  class="cell"
                  style="width: 12%;"
                >
                  <span class="phone-number">{{ order.phone }}</span>
                </div>
                <div
                  class="cell"
                  style="width: 15%;"
                >
                  <span class="date">{{ order.date }}</span>
                </div>
                <div
                  class="cell"
                  style="width: 13%;"
                >
                  <span class="payment-method">{{ order.paymentMethod }}</span>
                </div>
                <div
                  class="cell"
                  style="width: 13%;"
                >
                  <span class="total-price">{{ formatPrice(order.total) }}</span>
                </div>
                <div
                  class="cell"
                  style="width: 13%;"
                >
                  <span
                    class="status-badge"
                    :class="getStatusClass(order.status)"
                  >
                    {{ getStatusLabel(order.status) }}
                  </span>
                </div>
                <div
                  class="cell"
                  style="width: 8%; justify-content: flex-end;"
                >
                  <div class="action-buttons">
                    <button
                      class="action-btn view-btn"
                      title="Xem chi tiết"
                      @click="viewOrderDetails(order.id)"
                    >
                      <svg
                        width="18"
                        height="18"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="#9CA3AF"
                        stroke-width="2"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                      >
                        <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                        <circle
                          cx="12"
                          cy="12"
                          r="3"
                        />
                      </svg>
                    </button>
                    <button
                      class="action-btn edit-btn"
                      title="Chỉnh sửa trạng thái"
                      @click="editOrderStatus(order)"
                    >
                      <svg
                        width="18"
                        height="18"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="#9CA3AF"
                        stroke-width="2"
                        stroke-linecap="round"
                        stroke-linejoin="round"
                      >
                        <path d="M11 4H4a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2v-7" />
                        <path d="M18.5 2.5a2.121 2.121 0 0 1 3 3L12 15l-4 1 1-4 9.5-9.5z" />
                      </svg>
                    </button>
                  </div>
                </div>
              </div>
            </div>
          </div> <!-- table-body -->
        </div> <!-- table-wrapper -->
        <!-- Pagination -->
        <div class="pagination-section">
          <div class="pagination-info">
            Hiển thị {{ pageNumber === 0 ? 1 : pageNumber * size + 1 }} - {{ Math.min((pageNumber + 1) * size, totalElements) }} của {{ totalElements }} kết quả
          </div>
          <div class="pagination-controls">
            <button
              class="page-btn prev-btn"
              :disabled="currentPage === 1"
              @click="changePage(pageNumber + 1)"
            >
              <svg
                width="20"
                height="20"
                viewBox="0 0 20 20"
                fill="none"
              >
                <path
                  d="M12.5 5L7.5 10L12.5 15"
                  stroke="currentColor"
                  stroke-width="1.66667"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </button>

            <button
              v-for="page in visiblePages"
              :key="page"
              class="page-btn page-number"
              :class="{ active: currentPage === page }"
              @click="changePage(Number(page))"
            >
              {{ page === '...' ? '...' : page }}
            </button>

            <button
              class="page-btn next-btn"
              :disabled="currentPage === totalPages"
              @click="changePage(pageNumber + 2)"
            >
              <svg
                width="20"
                height="20"
                viewBox="0 0 20 20"
                fill="none"
              >
                <path
                  d="M7.5 5L12.5 10L7.5 15"
                  stroke="currentColor"
                  stroke-width="1.66667"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                />
              </svg>
            </button>
          </div>
        </div>
      </div> <!-- order-table-container -->

      <!-- View Details Modal -->
      <div
        v-if="showViewModal"
        class="modal-overlay"
        @click.self="showViewModal = false"
      >
        <div class="confirm-dialog view-dialog">
          <div class="confirm-header">
            <h3>Chi tiết đơn hàng #{{ viewingOrder?.id }}</h3>
          </div>
          <div
            class="confirm-body"
            style="max-height: 400px; overflow-y: auto;"
          >
            <div
              v-if="viewingOrder"
              class="order-details-content"
            >
              <p><strong>Khách hàng:</strong> {{ viewingOrder.fullName }}</p>
              <p><strong>SĐT:</strong> {{ viewingOrder.phoneNumber }}</p>
              <p><strong>Địa chỉ:</strong> {{ viewingOrder.address }}</p>
              <p><strong>Ngày đặt:</strong> {{ new Date(viewingOrder.createdAt).toLocaleString('vi-VN') }}</p>
              <p style="margin-bottom: 16px;">
                <strong>Tổng thanh toán:</strong> <span style="color: #d32f2f; font-weight: bold;">{{ formatPrice(viewingOrder.totalMoney) }}</span>
              </p>
            
              <h4 style="font-weight: 700; text-transform: uppercase; margin-bottom: 8px;">
                Sản phẩm:
              </h4>
              <div
                class="items-list"
                style="display: flex; flex-direction: column; gap: 10px;"
              >
                <div
                  v-for="item in viewingOrder.items"
                  :key="item.id"
                  style="display: flex; gap: 16px; border-bottom: 1px solid #eee; padding-bottom: 8px; align-items: center;"
                >
                  <img
                    v-if="item.imageUrl"
                    :src="item.imageUrl"
                    style="width: 50px; height: 50px; object-fit: cover; border-radius: 4px;"
                  >
                  <div style="flex: 1;">
                    <p style="font-weight: 600; margin: 0;">
                      {{ item.productName }}
                    </p>
                    <p style="font-size: 13px; color: #666; margin: 4px 0 0;">
                      Size: {{ item.size }} | SL: {{ item.quantity }}
                    </p>
                  </div>
                  <div style="font-weight: 600;">
                    {{ formatPrice(item.totalPrice) }}
                  </div>
                </div>
              </div>
            </div>
            <div
              v-else
              style="text-align: center; padding: 20px;"
            >
              Đang tải...
            </div>
          </div>
          <div
            class="confirm-actions"
            style="margin-top: 16px;"
          >
            <button
              class="btn-secondary"
              @click="showViewModal = false"
            >
              ĐÓNG
            </button>
          </div>
        </div>
      </div>

      <!-- Status Edit Modal -->
      <div
        v-if="showStatusModal"
        class="modal-overlay"
        @click.self="cancelStatusChange"
      >
        <div class="confirm-dialog">
          <div class="confirm-header">
            <span class="confirm-icon">&#9998;</span>
            <h3>Chỉnh sửa trạng thái đơn hàng</h3>
          </div>
          <div class="confirm-body">
            <p class="mb-4">
              Đơn #{{ editingOrder?.id }} — {{ editingOrder?.customerName }}
            </p>
            <div class="form-group">
              <label>Trạng thái mới</label>
              <select
                v-model="newStatus"
                class="form-input"
              >
                <option value="PENDING">
                  Đã đặt (PENDING)
                </option>
                <option value="CONFIRMED">
                  Đã xác nhận (CONFIRMED)
                </option>
                <option value="SHIPPING">
                  Đang giao (SHIPPING)
                </option>
                <option value="DELIVERED">
                  Đã giao (DELIVERED)
                </option>
                <option value="COMPLETED">
                  Hoàn thành (COMPLETED)
                </option>
                <option value="CANCEL_REQUESTED">
                  Yêu cầu hủy (CANCEL_REQUESTED)
                </option>
                <option value="CANCELLED">
                  Đã hủy (CANCELLED)
                </option>
                <option value="REFUNDED">
                  Hoàn tiền (REFUNDED)
                </option>
              </select>
            </div>
          </div>
          <div class="confirm-actions">
            <button
              class="btn-secondary"
              @click="cancelStatusChange"
            >
              HỦY
            </button>
            <button
              class="btn-confirm primary"
              @click="submitStatusChange"
            >
              CẬP NHẬT
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useToast } from 'vue-toastification'
import adminService from '@/services/admin.service'
import orderService from '@/services/order.service'

const searchQuery = ref('')
const statusFilter = ref('all')
const currentPage = ref(1) // 1-based for display

const filters = [
  { label: 'TẤT CẢ', value: 'all' },
  { label: 'ĐÃ ĐẶT', value: 'PENDING' },
  { label: 'ĐÃ XÁC NHẬN', value: 'CONFIRMED' },
  { label: 'ĐANG GIAO', value: 'SHIPPING' },
  { label: 'ĐÃ GIAO', value: 'DELIVERED' },
  { label: 'HOÀN THÀNH', value: 'COMPLETED' },
  { label: 'YÊU CẦU HỦY', value: 'CANCEL_REQUESTED' },
  { label: 'ĐÃ HỦY', value: 'CANCELLED' },
  { label: 'HOÀN TIỀN', value: 'REFUNDED' }
]

const applyFilter = (status: string) => {
  statusFilter.value = status
  currentPage.value = 1
  fetchOrders()
}
const itemsPerPage = 10
const size = computed(() => itemsPerPage)
const isLoading = ref(false)
const orders = ref<any[]>([])
const totalElements = ref(0)
const totalPages = ref(1)
const pageNumber = ref(0) // 0-based from backend
const toast = useToast()

// View modal state
const showViewModal = ref(false)
const viewingOrder = ref<any>(null)

const viewOrderDetails = async (orderId: number) => {
  showViewModal.value = true
  viewingOrder.value = null
  try {
    const res = await orderService.getOrder(orderId)
    viewingOrder.value = res
  } catch (err) {
    toast.error('Không thể lấy chi tiết đơn hàng')
    showViewModal.value = false
  }
}

// Status edit modal state
const showStatusModal = ref(false)
const editingOrder = ref<any>(null)
const newStatus = ref('')

const STATUS_LABELS: Record<string, string> = {
  PENDING: 'Đã đặt',
  CONFIRMED: 'Đã xác nhận',
  SHIPPING: 'Đang giao',
  DELIVERED: 'Đã giao',
  COMPLETED: 'Hoàn thành',
  CANCEL_REQUESTED: 'Yêu cầu hủy',
  CANCELLED: 'Đã hủy',
  REFUNDED: 'Hoàn tiền'
}

const STATUS_CLASSES: Record<string, string> = {
  PENDING: 'status-pending',
  CONFIRMED: 'status-confirmed',
  SHIPPING: 'status-delivering',
  DELIVERED: 'status-delivered',
  COMPLETED: 'status-completed',
  CANCEL_REQUESTED: 'status-cancel-requested',
  CANCELLED: 'status-cancelled',
  REFUNDED: 'status-refunded'
}

// Fetch orders from API (backend handles filtering + pagination)
const fetchOrders = async () => {
  isLoading.value = true
  try {
    const params: any = { page: currentPage.value - 1, size: itemsPerPage }
    if (statusFilter.value !== 'all') {
      params.status = statusFilter.value
    }
    if (searchQuery.value.trim()) {
      params.keyword = searchQuery.value.trim()
    }
    const response = await adminService.getOrders(params)
    const data = response
    const items = data.content || data || []
    orders.value = items.map((o: any) => ({
      id: o.id || 0,
      customerName: o.fullName || 'N/A',
      phone: o.phoneNumber || '',
      date: o.createdAt ? new Date(o.createdAt).toLocaleString('vi-VN') : '',
      paymentMethod: getPaymentMethodLabel(o.paymentMethod || 'COD'),
      total: o.totalMoney || 0,
      status: o.status || '',
      raw: o
    }))
    totalElements.value = (data.totalElements as number) || items.length
    totalPages.value = (data.totalPages as number) || 1
    pageNumber.value = (data.number as number) || 0
    currentPage.value = pageNumber.value + 1
  } catch (error) {
    console.error('Failed to fetch orders:', error)
    toast.error('Không tải được danh sách đơn hàng')
    orders.value = []
  } finally {
    isLoading.value = false
  }
}

const onSearch = () => {
  currentPage.value = 1
  fetchOrders()
}

onMounted(() => {
  fetchOrders()
})

// Backend already returns the correct page
const paginatedOrders = computed(() => orders.value)

// Visible page numbers (show max 5 pages)
const visiblePages = computed(() => {
  const pages: (number | string)[] = []
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

// Navigate to a specific page (1-based)
const changePage = (page: number) => {
  if (page < 1 || page > totalPages.value) return
  currentPage.value = page
  fetchOrders()
}

const getStatusLabel = (status: string) => STATUS_LABELS[status] || status
const getStatusClass = (status: string) => STATUS_CLASSES[status] || ''

const getPaymentMethodLabel = (method: string) => {
  if (method === 'COD') return 'Tiền mặt'
  if (method === 'VNPAY') return 'VNPay'
  return method
}

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN').format(price) + ' d'
}

const editOrderStatus = (order: any) => {
  editingOrder.value = order
  newStatus.value = order.status
  showStatusModal.value = true
}

const submitStatusChange = async () => {
  if (!editingOrder.value || newStatus.value === editingOrder.value.status) {
    showStatusModal.value = false
    editingOrder.value = null
    return
  }
  const currentStatus = editingOrder.value.status
  editingOrder.value.status = newStatus.value
  showStatusModal.value = false
  try {
    await adminService.updateOrderStatus(editingOrder.value.id, newStatus.value)
    toast.success('Cập nhật trạng thái thành công')
    fetchOrders()
  } catch (err: any) {
    editingOrder.value.status = currentStatus
    toast.error('Cập nhật trạng thái thất bại: ' + (err.response?.data?.error || err.message))
  }
  editingOrder.value = null
}

const cancelStatusChange = () => {
  showStatusModal.value = false
  editingOrder.value = null
}
</script>
<style scoped>
.orders-manager {
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
position: relative;
left: 0;
right: 0;
top: 0;
height: auto;
min-height: 131px;
background: #FFFFFF;
border: 1px solid #E2E2E2;
box-shadow: 0px 1px 2px rgba(0, 0, 0, 0.05);
border-radius: 12px;
padding: 24px;
margin-bottom: 10px;
}

.toolbar-content {
display: flex;
flex-direction: column;
gap: 16px;
width: 100%;
height: auto;
}

/* Search */
.search-container {
position: relative;
width: 373.25px;
height: 40px;
}

.search-input {
width: 100%;
height: 100%;
padding: 10px 16px 10px 40px;
background: #F3F3F3;
border: none;
border-radius: 9999px;
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
flex-wrap: wrap;
gap: 8px;
width: 100%;
}

.filter-btn {
display: flex;
align-items: center;
justify-content: center;
padding: 8px 16px;
height: 33px;
background: #EEEEEE;
border: none;
border-radius: 8px;
cursor: pointer;
font-family: 'Geist', sans-serif;
font-size: 11px;
font-weight: 400;
line-height: 16px;
letter-spacing: 1.1px;
text-transform: uppercase;
color: #1A1C1C;
transition: all 0.2s;
white-space: nowrap;
}

.filter-btn:hover {
background: #E5E7EB;
}

.filter-btn.active {
background: #000000;
color: #FFFFFF;
}

/* Order Table */
.order-table-container {
position: relative;
left: 0;
right: 0;
top: 0;
bottom: auto;
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
width: 100%;
min-width: 934px;
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

.order-id {
font-family: 'Geist', sans-serif;
font-weight: 500;
font-size: 14px;
line-height: 20px;
color: #111827;
}

.customer-name {
font-family: 'Geist', sans-serif;
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

.payment-method {
font-family: 'Geist', sans-serif;
font-weight: 500;
font-size: 13px;
line-height: 20px;
color: #4B5563;
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

.status-pending {
background: #DBEAFE;
color: #1E40AF;
}

.status-confirmed {
background: #E0E7FF;
color: #3730A3;
}

.status-delivering {
background: #FEF9C3;
color: #854D0E;
}

.status-delivered {
background: #DCFCE7;
color: #166534;
}

.status-completed {
background: #D1FAE5;
color: #065F46;
}

.status-cancelled {
  background: #FEE2E2;
  color: #991B1B;
}

.status-cancel-requested {
  background: #FFEDD5;
  color: #C2410C;
}

.status-refunded {
background: #F3E8FF;
color: #6B21A8;
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

.action-btn:hover svg path,
.action-btn:hover svg circle {
  stroke: #111827;
}

/* Pagination */
.pagination-section {
display: flex;
justify-content: space-between;
align-items: center;
padding: 16px 24px;
width: 100%;
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
width: auto;
min-width: 242.98px;
height: 38px;
display: flex;
gap: 4px;
align-items: center;
}

.page-btn {
display: flex;
align-items: center;
justify-content: center;
background: #FFFFFF;
border: 1px solid #D1D5DB;
cursor: pointer;
font-family: 'Geist', sans-serif;
font-size: 14px;
font-weight: 500;
line-height: 20px;
color: #6B7280;
box-sizing: border-box;
transition: all 0.2s;
min-width: 38px;
height: 38px;
padding: 8px;
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
border-radius: 6px 0 0 6px;
}

.page-btn.next-btn {
border-radius: 0 6px 6px 0;
}

.page-btn.page-number {
border-radius: 4px;
padding: 7.5px 16px 8.5px;
}

/* Modal Overlay */
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

.confirm-dialog {
background: #FFFFFF;
border: 1px solid #000000;
width: 90%;
max-width: 450px;
padding: 24px;
}

.view-dialog {
max-width: 600px;
}

.confirm-header {
display: flex;
align-items: center;
gap: 12px;
margin-bottom: 16px;
}

.confirm-icon {
font-size: 28px;
}

.confirm-header h3 {
margin: 0;
font-size: 18px;
font-weight: 700;
}

.confirm-body {
margin-bottom: 24px;
color: #333333;
line-height: 1.6;
}

.confirm-actions {
display: flex;
justify-content: flex-end;
gap: 12px;
}

.btn-confirm {
padding: 10px 24px;
border: none;
font-size: 14px;
font-weight: 600;
cursor: pointer;
}

.btn-confirm.primary {
background: #000000;
color: #FFFFFF;
}

.btn-confirm.primary:hover {
background: #333333;
}

.btn-secondary {
background: #FFFFFF;
color: #000000;
border: 1px solid #000000;
padding: 10px 24px;
font-size: 14px;
font-weight: 600;
cursor: pointer;
}

.btn-secondary:hover {
background: #F9F9F9;
}

.form-group {
margin-bottom: 16px;
}

.form-group label {
display: block;
margin-bottom: 6px;
font-size: 14px;
font-weight: 600;
color: #000000;
}

.form-input {
width: 100%;
padding: 10px 12px;
border: 1px solid #000000;
font-size: 14px;
outline: none;
box-sizing: border-box;
}

.form-input:focus {
border-color: #666666;
}

.mb-4 {
margin-bottom: 16px;
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
