<template>
  <section class="dashboard-admin">
    <!-- Key Metrics Section -->
    <section class="metrics-section">
      <!-- Revenue Card -->
      <div class="metric-card">
        <div class="metric-header">
          <span class="metric-label">DOANH THU</span>
          <button class="filter-btn">
            <div class="filter-icon"></div>
            <span>LỌC</span>
          </button>
        </div>
        <div class="metric-value">
          <h2>1.250.000.000 đ</h2>
          <p class="metric-period">THEO KHOẢNG THỜI GIAN ĐÃ CHỌN</p>
        </div>
        <div class="metric-change positive">
          <div class="change-icon up"></div>
          <span>+12%</span>
        </div>
      </div>

      <!-- Orders Card -->
      <div class="metric-card">
        <div class="metric-header">
          <span class="metric-label">ĐƠN HÀNG</span>
          <button class="filter-btn">
            <div class="filter-icon"></div>
            <span>LỌC</span>
          </button>
        </div>
        <div class="metric-value">
          <h2>850</h2>
          <p class="metric-period">THEO KHOẢNG THỜI GIAN ĐÃ CHỌN</p>
        </div>
        <div class="metric-change positive">
          <div class="change-icon up"></div>
          <span>+5.2%</span>
        </div>
      </div>

      <!-- Customers Card -->
      <div class="metric-card">
        <div class="metric-header">
          <span class="metric-label">KHÁCH HÀNG</span>
          <button class="filter-btn">
            <div class="filter-icon"></div>
            <span>LỌC</span>
          </button>
        </div>
        <div class="metric-value">
          <h2>420</h2>
          <p class="metric-period">THEO KHOẢNG THỜI GIAN ĐÃ CHỌN</p>
        </div>
        <div class="metric-change positive">
          <div class="change-icon up"></div>
          <span>+8.4%</span>
        </div>
      </div>

      <!-- Products Card -->
      <div class="metric-card">
        <div class="metric-header">
          <span class="metric-label">SẢN PHẨM</span>
        </div>
        <div class="metric-value">
          <h2>128</h2>
          <p class="metric-period">ACTIVE</p>
        </div>
      </div>
    </section>

    <!-- Orders Chart Section -->
    <section class="chart-section">
      <div class="section-header">
        <h3>Biểu đồ đơn hàng theo năm</h3>
        <button class="year-selector">
          <span>Năm 2024</span>
          <div class="dropdown-icon"></div>
        </button>
      </div>
      <div class="chart-container">
        <div class="chart-placeholder">
          <img src="" alt="Orders Chart" />
        </div>
      </div>
    </section>

    <!-- Top Selling Products & Order Status -->
    <section class="insights-section">
      <!-- Top Selling Products -->
      <div class="top-products">
        <div class="section-header">
          <h3>Top sản phẩm bán chạy</h3>
          <router-link to="/admin/products" class="view-all">Xem tất cả</router-link>
        </div>
        <div class="products-list">
          <div v-for="product in topProducts" :key="product.id" class="product-item">
            <div class="product-image">
              <img :src="product.image" :alt="product.name" />
            </div>
            <div class="product-info">
              <h4>{{ product.name }}</h4>
              <p class="product-category">{{ product.category }}</p>
            </div>
            <div class="product-sales">
              <span class="sales-count">{{ product.sold }}</span>
              <span class="sales-label">ĐÃ BÁN</span>
            </div>
          </div>
        </div>
      </div>

      <!-- Order Status Distribution -->
      <div class="order-status">
        <h3>Đơn hàng theo trạng thái</h3>
        <div class="pie-chart-container">
          <div class="pie-chart">
            <div class="pie-segment total">
              <span class="pie-value">850</span>
              <span class="pie-label">TỔNG ĐƠN</span>
            </div>
          </div>
          <div class="status-legend">
            <div class="status-item">
              <div class="status-color completed"></div>
              <div class="status-info">
                <span class="status-percent">Hoàn thành (72%)</span>
                <span class="status-count">612 đơn</span>
              </div>
            </div>
            <div class="status-item">
              <div class="status-color delivering"></div>
              <div class="status-info">
                <span class="status-percent">Đang giao (18%)</span>
                <span class="status-count">153 đơn</span>
              </div>
            </div>
            <div class="status-item">
              <div class="status-color pending"></div>
              <div class="status-info">
                <span class="status-percent">Chờ xử lý (7%)</span>
                <span class="status-count">60 đơn</span>
              </div>
            </div>
            <div class="status-item">
              <div class="status-color cancelled"></div>
              <div class="status-info">
                <span class="status-percent">Đã hủy (3%)</span>
                <span class="status-count">25 đơn</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Low Stock & Top Customers -->
    <section class="analytics-section">
      <!-- Low Stock Inventory -->
      <div class="low-stock">
        <h3>Sản phẩm tồn kho thấp</h3>
        <div class="table-container">
          <table class="inventory-table">
            <thead>
              <tr>
                <th>SẢN PHẨM</th>
                <th>SKU</th>
                <th>CÒN LẠI</th>
                <th>TRẠNG THÁI</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="item in lowStockItems" :key="item.sku">
                <td>{{ item.product }}</td>
                <td>{{ item.sku }}</td>
                <td class="stock-count">{{ item.remaining }} units</td>
                <td>
                  <span :class="['status-badge', item.statusClass]">
                    {{ item.status }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>

      <!-- Top Customers -->
      <div class="top-customers">
        <h3>Top khách hàng</h3>
        <div class="customers-list">
          <div v-for="customer in topCustomers" :key="customer.id" class="customer-item">
            <div class="customer-avatar">
              <img :src="customer.avatar" :alt="customer.name" />
            </div>
            <div class="customer-info">
              <h4>{{ customer.name }}</h4>
              <p class="customer-orders">{{ customer.orders }} đơn hàng</p>
            </div>
            <div class="customer-spent">
              <span class="spent-amount">{{ customer.spent }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Additional Analytics -->
    <section class="additional-analytics">
      <!-- Inventory Breakdown -->
      <div class="inventory-breakdown">
        <h3>Trạng thái tồn kho</h3>
        <div class="progress-bars">
          <div class="progress-item">
            <div class="progress-label">
              <span>Sportswear / Shoes</span>
              <span>82% Tồn kho</span>
            </div>
            <div class="progress-bar">
              <div class="progress-fill" style="width: 82%"></div>
            </div>
          </div>
          <div class="progress-item">
            <div class="progress-label">
              <span>Equipment</span>
              <span>42% Tồn kho</span>
            </div>
            <div class="progress-bar">
              <div class="progress-fill" style="width: 42%"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- Product Reviews -->
      <div class="product-reviews">
        <h3>Top đánh giá sản phẩm</h3>
        <div class="reviews-list">
          <div class="review-item">
            <div class="rating-stars">★★★★★</div>
            <p class="review-text">"Chất lượng vải tuyệt vời, form dáng rất chuẩn."</p>
            <p class="review-author">— Adidas Premium Tee bởi Anh Khoa</p>
          </div>
          <div class="review-item">
            <div class="rating-stars">★★★★★</div>
            <p class="review-text">"Giày rất nhẹ và êm, hỗ trợ chạy bộ tốt."</p>
            <p class="review-author">— Nike Air Zoom bởi Minh Hạnh</p>
          </div>
        </div>
      </div>
    </section>
  </section>
</template>

<script setup>
import { ref } from 'vue'

// Mock data
const topProducts = ref([
  {
    id: 1,
    name: 'Nike Air Zoom Performance',
    category: 'SPORTSWEAR / SHOES',
    sold: 312,
    image: ''
  },
  {
    id: 2,
    name: 'Adidas Premium Training Tee',
    category: 'APPAREL / TOPS',
    sold: 245,
    image: ''
  },
  {
    id: 3,
    name: 'Pro Tech Compression',
    category: 'APPAREL / BOTTOMS',
    sold: 198,
    image: ''
  }
])

const lowStockItems = ref([
  {
    product: 'Elite Comp Jacket (Black)',
    sku: 'TX-JK-042',
    remaining: 2,
    status: 'KHẨN CẤP',
    statusClass: 'urgent'
  },
  {
    product: 'Pro Running Leggings (S)',
    sku: 'TX-LG-88',
    remaining: 5,
    status: 'CẢNH BÁO',
    statusClass: 'warning'
  },
  {
    product: 'Ultra Mesh Gloves (M)',
    sku: 'TX-GL-09',
    remaining: 8,
    status: 'CẢNH BÁO',
    statusClass: 'warning'
  }
])

const topCustomers = ref([
  {
    id: 1,
    name: 'Trần Văn An',
    orders: 24,
    spent: '18.5M đ',
    avatar: ''
  },
  {
    id: 2,
    name: 'Lê Thu Thảo',
    orders: 18,
    spent: '14.2M đ',
    avatar: ''
  },
  {
    id: 3,
    name: 'Nguyễn Minh Quân',
    orders: 15,
    spent: '11.0M đ',
    avatar: ''
  }
])
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

.filter-btn {
  display: flex;
  align-items: center;
  gap: 4px;
  background: none;
  border: none;
  cursor: pointer;
  color: #5E5F5C;
  font-family: 'Geist', sans-serif;
  font-size: 10px;
  font-weight: 700;
}

.filter-icon {
  width: 10.5px;
  height: 7px;
  background: #5E5F5C;
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

.metric-change {
  position: absolute;
  bottom: 24px;
  left: 24px;
  display: flex;
  align-items: center;
  gap: 4px;
}

.metric-change.positive .change-icon.up {
  width: 11.67px;
  height: 7px;
  background: #16A34A;
}

.metric-change span {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 700;
  color: #16A34A;
}

/* Chart Section */
.chart-section {
  background: #FFFFFF;
  border: 1px solid #000000;
  padding: 32px;
  margin-bottom: 30px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 40px;
}

.section-header h3 {
  font-family: 'Gelasio', serif;
  font-size: 24px;
  font-weight: 700;
  color: #1A1C1C;
  margin: 0;
}

.year-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  background: none;
  border: 1px solid #000000;
  padding: 8px 24px;
  cursor: pointer;
  font-family: 'Inter', sans-serif;
  font-size: 12px;
  font-weight: 600;
  color: #000000;
}

.dropdown-icon {
  width: 7px;
  height: 4.32px;
  background: #000000;
}

.chart-container {
  width: 100%;
  height: 384px;
}

.chart-placeholder {
  width: 100%;
  height: 100%;
  background: #F3F3F4;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* Insights Section */
.insights-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 30px;
  margin-bottom: 30px;
}

.top-products,
.order-status,
.low-stock,
.top-customers,
.inventory-breakdown,
.product-reviews {
  background: #FFFFFF;
  border: 1px solid #E8E8E8;
}

.top-products {
  padding: 32px;
}

.top-products .section-header {
  margin-bottom: 32px;
}

.view-all {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
  color: #5E5F5C;
  text-decoration: underline;
}

.products-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.product-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.product-image {
  width: 64px;
  height: 64px;
  background: #F3F3F4;
  display: flex;
  align-items: center;
  justify-content: center;
}

.product-image img {
  max-width: 100%;
  max-height: 100%;
}

.product-info {
  flex: 1;
}

.product-info h4 {
  font-family: 'Geist', sans-serif;
  font-size: 16px;
  font-weight: 700;
  color: #000000;
  margin: 0 0 0 0;
  line-height: 24px;
}

.product-category {
  font-family: 'Geist', sans-serif;
  font-size: 10px;
  color: #848484;
  margin: 0;
  text-transform: uppercase;
}

.product-sales {
  text-align: right;
}

.sales-count {
  display: block;
  font-family: 'Geist', sans-serif;
  font-size: 16px;
  font-weight: 700;
  color: #000000;
  line-height: 24px;
}

.sales-label {
  font-family: 'Geist', sans-serif;
  font-size: 10px;
  color: #848484;
  text-transform: uppercase;
}

/* Order Status */
.order-status {
  padding: 32px;
  position: relative;
}

.order-status h3 {
  position: absolute;
  top: 33px;
  left: 33px;
  font-family: 'Gelasio', serif;
  font-size: 24px;
  font-weight: 700;
  color: #1A1C1C;
}

.pie-chart-container {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 32px;
  padding-top: 80px;
}

.pie-chart {
  width: 192px;
  height: 224px;
  position: relative;
  background: conic-gradient(
    #000000 0% 72%,
    #5E5F5C 72% 90%,
    #DADADA 90% 97%,
    #BA1A1A 97% 100%
  );
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.pie-segment.total {
  position: absolute;
  text-align: center;
}

.pie-value {
  display: block;
  font-size: 28px;
  font-weight: 700;
  color: #1A1C1C;
  line-height: 42px;
}

.pie-label {
  font-family: 'Geist', sans-serif;
  font-size: 10px;
  color: #1A1C1C;
}

.status-legend {
  width: 141px;
}

.status-item {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
}

.status-item:last-child {
  margin-bottom: 0;
}

.status-color {
  width: 13.58px;
  height: 16px;
}

.status-color.completed {
  background: #000000;
}

.status-color.delivering {
  background: #5E5F5C;
}

.status-color.pending {
  background: #DADADA;
}

.status-color.cancelled {
  background: #BA1A1A;
}

.status-info {
  display: flex;
  flex-direction: column;
}

.status-percent {
  font-family: 'Geist', sans-serif;
  font-size: 16px;
  font-weight: 700;
  color: #1A1C1C;
  line-height: 24px;
}

.status-count {
  font-family: 'Geist Mono', monospace;
  font-size: 12px;
  color: #5E5F5C;
  line-height: 16px;
}

/* Analytics Section */
.analytics-section {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 30px;
  margin-bottom: 30px;
}

.low-stock {
  grid-column: span 2;
  padding: 32px;
}

.low-stock h3 {
  font-family: 'Gelasio', serif;
  font-size: 24px;
  font-weight: 700;
  color: #1A1C1C;
  margin: 0 0 32px 0;
}

.table-container {
  overflow-x: auto;
}

.inventory-table {
  width: 100%;
  border-collapse: collapse;
}

.inventory-table th {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1.8px;
  color: #1A1C1C;
  text-align: left;
  padding: 15.5px 1px 17.5px;
  border-bottom: 2px solid #000000;
}

.inventory-table td {
  font-family: 'Geist', sans-serif;
  padding: 16.5px 1px;
  border-top: 1px solid #F3F3F4;
}

.inventory-table tr:first-child td {
  border-top: none;
}

.stock-count {
  font-weight: 700;
  color: #BA1A1A;
}

.status-badge {
  display: inline-block;
  padding: 4px 8px;
  font-size: 10px;
  font-weight: 700;
  text-transform: uppercase;
}

.status-badge.urgent {
  background: #FFDAD6;
  color: #93000A;
}

.status-badge.warning {
  background: #FFDAD6;
  color: #93000A;
}

/* Top Customers */
.top-customers {
  padding: 32px;
}

.top-customers h3 {
  font-family: 'Gelasio', serif;
  font-size: 24px;
  font-weight: 700;
  color: #1A1C1C;
  margin: 0 0 32px 0;
}

.customers-list {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.customer-item {
  display: flex;
  align-items: center;
  gap: 16px;
}

.customer-avatar {
  width: 48px;
  height: 48px;
  border: 1px solid #000000;
  border-radius: 9999px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 4px;
}

.customer-avatar img {
  width: 38px;
  height: 38px;
  border-radius: 9999px;
  object-fit: cover;
}

.customer-info {
  flex: 1;
}

.customer-info h4 {
  font-family: 'Gelasio', serif;
  font-size: 16px;
  font-weight: 700;
  color: #1A1C1C;
  margin: 0 0 0 0;
  line-height: 24px;
}

.customer-orders {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #5E5F5C;
  margin: 0;
}

.customer-spent {
  text-align: right;
}

.spent-amount {
  font-family: 'Geist', sans-serif;
  font-size: 16px;
  font-weight: 700;
  color: #1A1C1C;
}

/* Additional Analytics */
.additional-analytics {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 30px;
}

.inventory-breakdown {
  padding: 32px;
}

.inventory-breakdown h3 {
  font-family: 'Gelasio', serif;
  font-size: 24px;
  font-weight: 700;
  color: #1A1C1C;
  margin: 0 0 32px 0;
}

.progress-bars {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.progress-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.progress-label {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.progress-label span:first-child {
  font-family: 'Geist', sans-serif;
  font-size: 16px;
  font-weight: 700;
  color: #1A1C1C;
}

.progress-label span:last-child {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
  color: #5E5F5C;
  letter-spacing: 1.8px;
}

.progress-bar {
  width: 100%;
  height: 8px;
  background: #F3F3F4;
  position: relative;
}

.progress-fill {
  height: 100%;
  background: #000000;
  position: absolute;
  left: 0;
  top: 0;
}

.product-reviews {
  padding: 32px;
}

.product-reviews h3 {
  font-family: 'Gelasio', serif;
  font-size: 24px;
  font-weight: 700;
  color: #1A1C1C;
  margin: 0 0 32px 0;
}

.reviews-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.review-item {
  padding-left: 16px;
  border-left: 4px solid #000000;
}

.rating-stars {
  color: #000000;
  font-size: 14px;
  margin-bottom: 8px;
}

.review-text {
  font-family: 'Gelasio', serif;
  font-size: 16px;
  font-weight: 700;
  color: #1A1C1C;
  margin: 0 0 8px 0;
  line-height: 24px;
}

.review-author {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #5E5F5C;
  margin: 0;
}
</style>
