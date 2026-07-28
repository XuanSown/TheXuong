<template>
  <div class="dashboard-admin">
    <!-- Key Metrics Section -->
    <section class="metrics-section">
      <!-- Revenue Card -->
      <div class="metric-card">
        <div class="metric-header">
          <span class="metric-label">DOANH THU</span>
          <button
            class="filter-btn"
            @click="refreshData"
          >
            <span>LÀM MỚI</span>
          </button>
        </div>
        <div class="metric-value">
          <h2>{{ formatPrice(stats.totalRevenue) }}</h2>
          <p class="metric-period">
            THEO KHOẢNG THỜI GIAN ĐÃ CHỌN
          </p>
        </div>
      </div>

      <!-- Orders Card -->
      <div class="metric-card">
        <div class="metric-header">
          <span class="metric-label">ĐƠN HÀNG</span>
        </div>
        <div class="metric-value">
          <h2>{{ stats.totalOrders }}</h2>
          <p class="metric-period">
            TỔNG SỐ ĐƠN HÀNG
          </p>
        </div>
      </div>

      <!-- Customers Card -->
      <div class="metric-card">
        <div class="metric-header">
          <span class="metric-label">KHÁCH HÀNG</span>
        </div>
        <div class="metric-value">
          <h2>{{ stats.totalUsers }}</h2>
          <p class="metric-period">
            ĐÃ ĐĂNG KÝ
          </p>
        </div>
        <div class="metric-sub">
          <span>{{ stats.usersWithOrders }} có đơn hàng</span>
          <span>{{ stats.usersWithoutOrders }} chưa có đơn</span>
        </div>
      </div>

      <!-- Products Card -->
      <div class="metric-card">
        <div class="metric-header">
          <span class="metric-label">SẢN PHẨM</span>
        </div>
        <div class="metric-value">
          <h2>{{ stats.totalProducts }}</h2>
          <p class="metric-period">
            TRONG KHO
          </p>
        </div>
      </div>
    </section>

    <!-- Orders Chart Section -->
    <section class="chart-section">
      <div class="section-header">
        <h3>Doanh thu theo ngày</h3>
      </div>
      <div class="chart-container">
        <div
          v-if="isLoading"
          class="chart-placeholder"
        >
          Đang tải...
        </div>
        <div
          v-else-if="stats.revenueByDay.length === 0"
          class="chart-placeholder"
        >
          Chưa có dữ liệu
        </div>
        <table
          v-else
          class="revenue-table"
        >
          <thead>
            <tr>
              <th>Ngày</th>
              <th>Doanh thu</th>
            </tr>
          </thead>
          <tbody>
            <tr
              v-for="row in stats.revenueByDay"
              :key="row[0]"
            >
              <td>{{ row[0] }}</td>
              <td>{{ formatPrice(row[1]) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
    </section>

    <!-- Top Selling Products & Order Status -->
    <section class="insights-section">
      <!-- Top Selling Products -->
      <div class="top-products">
        <div class="section-header">
          <h3>Top sản phẩm bán chạy</h3>
        </div>
        <div
          v-if="isLoading"
          class="loading-text"
        >
          Đang tải...
        </div>
        <div
          v-else
          class="products-list"
        >
          <div
            v-for="product in stats.topSelling"
            :key="product[0]"
            class="product-item"
          >
            <div class="product-info">
              <h4>{{ product[0] }}</h4>
              <p class="product-category">
                Đã bán: {{ product[1] }} sản phẩm
              </p>
            </div>
            <div class="product-sales">
              <span class="sales-count">{{ formatPrice(product[2]) }}</span>
              <span class="sales-label">DOANH THU</span>
            </div>
          </div>
          <div
            v-if="stats.topSelling.length === 0"
            class="empty-text"
          >
            Chưa có dữ liệu
          </div>
        </div>
      </div>

      <!-- Order Status Distribution -->
      <div class="order-status">
        <h3>Đơn hàng theo trạng thái</h3>
        <div
          v-if="isLoading"
          class="loading-text"
        >
          Đang tải...
        </div>
        <div
          v-else
          class="status-list"
        >
          <div
            v-for="item in stats.orderStatusStats"
            :key="item[0]"
            class="status-item"
          >
            <div
              class="status-color"
              :class="getStatusColor(item[0])"
            />
            <div class="status-info">
              <span class="status-label">{{ getStatusLabel(item[0]) }}</span>
              <span class="status-count">{{ item[1] }} đơn</span>
            </div>
          </div>
          <div
            v-if="stats.orderStatusStats.length === 0"
            class="empty-text"
          >
            Chưa có dữ liệu
          </div>
        </div>
      </div>
    </section>

    <!-- Low Stock & Top Viewed -->
    <section class="analytics-section">
      <!-- Low Stock Inventory -->
      <div class="low-stock">
        <h3>Tồn kho thấp</h3>
        <div
          v-if="isLoading"
          class="loading-text"
        >
          Đang tải...
        </div>
        <div
          v-else
          class="table-container"
        >
          <table class="inventory-table">
            <thead>
              <tr>
                <th>SẢN PHẨM</th>
                <th>TỒN KHO</th>
                <th>TRẠNG THÁI</th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="item in stats.lowStock"
                :key="item[0]"
              >
                <td>{{ item[0] }}</td>
                <td>{{ item[1] }}</td>
                <td>
                  <span :class="['status-badge', item[1] <= 5 ? 'urgent' : 'warning']">
                    {{ item[1] <= 5 ? 'KHẨN CẤP' : 'CẢNH BÁO' }}
                  </span>
                </td>
              </tr>
              <tr v-if="stats.lowStock.length === 0">
                <td
                  colspan="3"
                  class="empty-text"
                >
                  Không có sản phẩm tồn kho thấp
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Top Viewed Products -->
      <div class="top-customers">
        <h3>Top sản phẩm xem nhiều</h3>
        <div
          v-if="isLoading"
          class="loading-text"
        >
          Đang tải...
        </div>
        <div
          v-else
          class="customers-list"
        >
          <div
            v-for="product in stats.topViewed"
            :key="product[0]"
            class="customer-item"
          >
            <div class="customer-info">
              <h4>{{ product[0] }}</h4>
              <p class="customer-orders">
                {{ product[1] }} lượt xem
              </p>
            </div>
          </div>
          <div
            v-if="stats.topViewed.length === 0"
            class="empty-text"
          >
            Chưa có dữ liệu
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import adminService from '@/services/admin.service'

const isLoading = ref(false)

const stats = reactive({
  totalRevenue: 0,
  totalOrders: 0,
  totalUsers: 0,
  usersWithOrders: 0,
  usersWithoutOrders: 0,
  totalProducts: 0,
  topSelling: [],
  slowSelling: [],
  revenueByDay: [],
  lowStock: [],
  topViewed: [],
  leastViewed: [],
  orderStatusStats: []
})

const fetchStatistics = async () => {
  isLoading.value = true
  try {
    const data = await adminService.getStatistics()

    stats.totalRevenue = data.revenueByDay?.reduce((sum, row) => sum + (Number(row[1]) || 0), 0) || 0
    stats.totalOrders = data.orderStatusStats?.reduce((sum, row) => sum + (Number(row[1]) || 0), 0) || 0
    stats.totalUsers = data.totalUsers || 0
    stats.usersWithOrders = data.usersWithOrders || 0
    stats.usersWithoutOrders = data.usersWithoutOrders || 0
    stats.totalProducts = data.inventory?.length || 0
    stats.topSelling = data.topSelling || []
    stats.slowSelling = data.slowSelling || []
    stats.revenueByDay = data.revenueByDay || []
    stats.lowStock = (data.inventory || []).filter((item) => Number(item[1]) <= 10)
    stats.topViewed = data.topViewed || []
    stats.leastViewed = data.leastViewed || []
    stats.orderStatusStats = data.orderStatusStats || []
  } catch (error) {
    console.error('Failed to fetch statistics:', error)
  } finally {
    isLoading.value = false
  }
}

const refreshData = () => {
  fetchStatistics()
}

const formatPrice = (value) => {
  return new Intl.NumberFormat('vi-VN').format(value) + ' đ'
}

const getStatusLabel = (status) => {
  const labels = {
    PENDING: 'Chờ xử lý',
    CONFIRMED: 'Đã xác nhận',
    SHIPPING: 'Đang giao',
    DELIVERED: 'Đã giao',
    COMPLETED: 'Hoàn thành',
    CANCELLED: 'Đã hủy',
    REFUNDED: 'Hoàn tiền'
  }
  return labels[status] || status
}

const getStatusColor = (status) => {
  const colors = {
    PENDING: 'status-pending',
    CONFIRMED: 'status-confirmed',
    SHIPPING: 'status-delivering',
    DELIVERED: 'status-delivered',
    COMPLETED: 'status-completed',
    CANCELLED: 'status-cancelled',
    REFUNDED: 'status-refunded'
  }
  return colors[status] || ''
}

onMounted(() => {
  fetchStatistics()
})
</script>

<style scoped>
.dashboard-admin {
  width: 100%;
  min-height: 100vh;
  background: #F9F9F9;
  padding: 120px 20px 20px;
}

/* Metrics Section */
.metrics-section {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-bottom: 30px;
}

.metric-card {
  background: #FFFFFF;
  border: 1px solid #E8E8E8;
  padding: 24px;
  position: relative;
  height: 175px;
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.metric-label {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1.8px;
  color: #848484;
}

.metric-value h2 {
  font-family: 'Geist', sans-serif;
  font-size: 20px;
  font-weight: 700;
  color: #000000;
  margin: 0 0 4px 0;
  line-height: 30px;
}

.metric-period {
  font-family: 'Geist', sans-serif;
  font-size: 10px;
  color: #5E5F5C;
  margin: 0;
  text-transform: uppercase;
}

.metric-sub {
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: 8px;
}

.metric-sub span {
  font-family: 'Geist', sans-serif;
  font-size: 11px;
  color: #6B7280;
}

/* Chart Section */
.chart-section {
  background: #FFFFFF;
  border: 1px solid #E8E8E8;
  padding: 32px;
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h3 {
  font-family: 'Geist', sans-serif;
  font-size: 20px;
  font-weight: 700;
  color: #1A1C1C;
  margin: 0;
}

.chart-container {
  width: 100%;
  min-height: 200px;
}

.chart-placeholder {
  width: 100%;
  min-height: 100px;
  background: #F3F3F4;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6B7280;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
}

.revenue-table {
  width: 100%;
  border-collapse: collapse;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
}

.revenue-table th {
  text-align: left;
  padding: 12px 16px;
  background: #F9FAFB;
  border-bottom: 1px solid #E5E7EB;
  font-weight: 600;
  color: #6B7280;
  text-transform: uppercase;
  font-size: 12px;
}

.revenue-table td {
  padding: 12px 16px;
  border-bottom: 1px solid #F3F4F6;
  color: #374151;
}

/* Insights Section */
.insights-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  margin-bottom: 30px;
}

.top-products, .order-status, .low-stock, .top-customers {
  background: #FFFFFF;
  border: 1px solid #E8E8E8;
  padding: 32px;
}

.top-products .section-header {
  margin-bottom: 24px;
}

.products-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.product-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  padding: 12px 0;
  border-bottom: 1px solid #F3F4F6;
}

.product-item:last-child {
  border-bottom: none;
}

.product-info {
  flex: 1;
}

.product-info h4 {
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  font-weight: 700;
  color: #000000;
  margin: 0 0 4px 0;
}

.product-category {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #848484;
  margin: 0;
}

.product-sales {
  text-align: right;
}

.sales-count {
  display: block;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  font-weight: 700;
  color: #000000;
}

.sales-label {
  font-family: 'Geist', sans-serif;
  font-size: 10px;
  color: #848484;
  text-transform: uppercase;
}

/* Order Status */
.order-status h3 {
  font-family: 'Geist', sans-serif;
  font-size: 20px;
  font-weight: 700;
  color: #1A1C1C;
  margin: 0 0 24px 0;
}

.status-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.status-color {
  width: 13px;
  height: 13px;
  border-radius: 3px;
  flex-shrink: 0;
}

.status-color.pending { background: #DBEAFE; }
.status-color.confirmed { background: #E0E7FF; }
.status-color.delivering { background: #FEF9C3; }
.status-color.delivered { background: #DCFCE7; }
.status-color.completed { background: #D1FAE5; }
.status-color.cancelled { background: #FEE2E2; }
.status-color.refunded { background: #F3E8FF; }

.status-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex: 1;
}

.status-label {
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  font-weight: 400;
  color: #374151;
}

.status-count {
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  font-weight: 600;
  color: #000000;
}

/* Analytics Section */
.analytics-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
}

.low-stock h3, .top-customers h3 {
  font-family: 'Geist', sans-serif;
  font-size: 20px;
  font-weight: 700;
  color: #1A1C1C;
  margin: 0 0 24px 0;
}

.table-container {
  overflow-x: auto;
}

.inventory-table {
  width: 100%;
  border-collapse: collapse;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
}

.inventory-table th {
  text-align: left;
  padding: 12px 16px;
  background: #F9FAFB;
  border-bottom: 1px solid #E5E7EB;
  font-weight: 600;
  color: #6B7280;
  text-transform: uppercase;
  font-size: 12px;
}

.inventory-table td {
  padding: 12px 16px;
  border-top: 1px solid #F3F4F6;
  color: #374151;
}

.status-badge {
  display: inline-block;
  padding: 4px 8px;
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
  border-radius: 9999px;
}

.status-badge.urgent {
  background: #FFDAD6;
  color: #93000A;
}

.status-badge.warning {
  background: #FEF9C3;
  color: #854D0E;
}

.customers-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.customer-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 0;
  border-bottom: 1px solid #F3F4F6;
}

.customer-item:last-child {
  border-bottom: none;
}

.customer-info {
  flex: 1;
}

.customer-info h4 {
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  font-weight: 700;
  color: #000000;
  margin: 0 0 4px 0;
}

.customer-orders {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #848484;
  margin: 0;
}

.loading-text, .empty-text {
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  color: #6B7280;
  padding: 24px 0;
  text-align: center;
}
</style>
