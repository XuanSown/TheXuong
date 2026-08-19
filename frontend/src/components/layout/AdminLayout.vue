<template>
  <div class="admin-layout">
    <!-- Sidebar Navigation -->
    <aside class="sidebar">
      <!-- Logo Section -->
      <div class="logo-section">
        <img
          src="@/assets/logo.png"
          alt="Logo"
          class="logo"
        >
      </div>

      <!-- Navigation Menu -->
      <nav class="nav-menu">
        <router-link
          to="/admin/users"
          class="nav-item"
          :class="{ active: String($route.name).startsWith('admin-users') }"
        >
          <span class="nav-icon">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            ><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2" /><circle
              cx="9"
              cy="7"
              r="4"
            /><path d="M23 21v-2a4 4 0 0 0-3-3.87" /><path d="M16 3.13a4 4 0 0 1 0 7.75" /></svg>
          </span>
          <span class="nav-text">Quản Lý Người Dùng</span>
        </router-link>
        <router-link
          to="/admin/orders"
          class="nav-item"
          :class="{ active: String($route.name).startsWith('admin-orders') }"
        >
          <span class="nav-icon">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            ><path d="m7.5 4.27 9 5.15" /><path d="M21 8a2 2 0 0 0-1-1.73l-7-4a2 2 0 0 0-2 0l-7 4A2 2 0 0 0 3 8v8a2 2 0 0 0 1 1.73l7 4a2 2 0 0 0 2 0l7-4A2 2 0 0 0 21 16Z" /><path d="m3.3 7 8.7 5 8.7-5" /><path d="M12 22V12" /></svg>
          </span>
          <span class="nav-text">Quản Lý Đơn Hàng</span>
        </router-link>
        <router-link
          to="/admin/products"
          class="nav-item"
          :class="{ active: String($route.name).startsWith('admin-products') }"
        >
          <span class="nav-icon">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            ><path d="M20.59 13.41l-7.17 7.17a2 2 0 0 1-2.83 0L2 12V2h10l8.59 8.59a2 2 0 0 1 0 2.82z" /><line
              x1="7"
              y1="7"
              x2="7.01"
              y2="7"
            /></svg>
          </span>
          <span class="nav-text">Quản Lý Sản Phẩm</span>
        </router-link>
        <router-link
          to="/admin/loyalty/vouchers"
          class="nav-item"
          :class="{ active: String($route.name).startsWith('admin-vouchers') }"
        >
          <span class="nav-icon">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            ><path d="M2 9a3 3 0 0 1 0 6v2a2 2 0 0 0 2 2h16a2 2 0 0 0 2-2v-2a3 3 0 0 1 0-6V7a2 2 0 0 0-2-2H4a2 2 0 0 0-2 2Z" /><path d="M13 5v2" /><path d="M13 17v2" /><path d="M13 11v2" /></svg>
          </span>
          <span class="nav-text">Quản Lý Voucher</span>
        </router-link>
        <router-link
          to="/admin/statistics"
          class="nav-item"
          :class="{ active: String($route.name).startsWith('admin-statistics') }"
        >
          <span class="nav-icon">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            ><path d="M3 3v18h18" /><path d="M18 17V9" /><path d="M13 17V5" /><path d="M8 17v-3" /></svg>
          </span>
          <span class="nav-text">Quản Lý Thống Kê</span>
        </router-link>
        <router-link
          to="/admin/loyalty/tiers"
          class="nav-item"
          :class="{ active: String($route.name).startsWith('admin-tiers') }"
        >
          <span class="nav-icon">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            ><path d="M12 2l3.09 6.26L22 9.27l-5 4.87 1.18 6.88L12 17.77l-6.18 3.25L7 14.14 2 9.27l6.91-1.01L12 2z" /></svg>
          </span>
          <span class="nav-text">Quản Lý Cấp Bậc</span>
        </router-link>
        <router-link
          to="/admin/customer-care"
          class="nav-item"
          :class="{ active: String($route.name).startsWith('admin-customer-care') }"
        >
          <span class="nav-icon">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            ><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z" /><path d="M8 9h8" /><path d="M8 13h5" /></svg>
          </span>
          <span class="nav-text">Quản Lý CSKH</span>
        </router-link>
        <router-link
          to="/admin/audit-logs"
          class="nav-item"
          :class="{ active: String($route.name).startsWith('admin-audit-logs') }"
        >
          <span class="nav-icon">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            ><path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" /><polyline points="14 2 14 8 20 8" /><line
              x1="16"
              y1="13"
              x2="8"
              y2="13"
            /><line
              x1="16"
              y1="17"
              x2="8"
              y2="17"
            /><polyline points="10 9 9 9 8 9" /></svg>
          </span>
          <span class="nav-text">Lịch Sử Hệ Thống</span>
        </router-link>
      </nav>

      <!-- Footer Menu -->
      <div class="footer-menu">
        <router-link
          to="/help"
          class="nav-item"
        >
          <span class="nav-icon">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            ><circle
              cx="12"
              cy="12"
              r="10"
            /><path d="M9.09 9a3 3 0 0 1 5.83 1c0 2-3 3-3 3" /><line
              x1="12"
              y1="17"
              x2="12.01"
              y2="17"
            /></svg>
          </span>
          <span class="nav-text">HELP CENTER</span>
        </router-link>
        <button
          class="nav-item logout-btn"
          @click="handleLogout"
        >
          <span class="nav-icon">
            <svg
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            ><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" /><polyline points="16 17 21 12 16 7" /><line
              x1="21"
              y1="12"
              x2="9"
              y2="12"
            /></svg>
          </span>
          <span class="nav-text">LOGOUT</span>
        </button>
      </div>
    </aside>

    <!-- Main Content Area -->
    <main class="main-content">
      <slot />
    </main>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

const router = useRouter()
const authStore = useAuthStore()

const handleLogout = async () => {
  await authStore.logout()
  router.push('/login')
}
</script>

<style scoped>
.admin-layout {
  display: flex;
  min-height: 100vh;
  width: 100%;
  background: #F9F9F9;
}

/* Sidebar */
.sidebar {
  width: 256px;
  background: #FFFFFF;
  display: flex;
  flex-direction: column;
  padding-top: 140px;
  position: fixed;
  height: 100vh;
  top: 0;
  left: 0;
  z-index: 100;
  border-right: 1px solid #E8E8E8;
}

.logo-section {
  position: absolute;
  top: 32px;
  left: 0;
  width: 100%;
  height: 92px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo {
  max-width: 150px;
  height: auto;
}

.nav-menu {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 0;
  flex: 1;
  overflow-y: auto;
}

/* Hide scrollbar for .nav-menu */
.nav-menu::-webkit-scrollbar {
  width: 4px;
}
.nav-menu::-webkit-scrollbar-thumb {
  background-color: #E8E8E8;
  border-radius: 4px;
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  text-decoration: none;
  color: black;
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  font-weight: 600;
  letter-spacing: 1.8px;
  transition: all 0.2s;
  border-left: 4px solid transparent;
  background: transparent;
  border-top: none;
  border-right: none;
  border-bottom: none;
  cursor: pointer;
  width: 100%;
  text-align: left;
}

.nav-item:hover {
  background: black;
  color: white;
}

.nav-item.active {
  background: black;
  border-left-color: #FFFFFF;
  color: #FFFFFF;
  font-weight: 700;
}

.nav-icon {
  width: 20px;
  height: 20px;
  margin-right: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #5E5F5C;
}

.nav-icon svg {
  width: 100%;
  height: 100%;
}

.nav-item:hover .nav-icon,
.nav-item.active .nav-icon {
  color: #FFFFFF;
}

.nav-text {
  flex: 1;
}

.footer-menu {
  border-top: 1px solid #E8E8E8;
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
  margin-top: auto;
}

.logout-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: black;
}

.logout-btn:hover {
  background: #000000;
  color: #FFFFFF;
}

/* Main Content */
.main-content {
  margin-left: 256px;
  padding: 32px 0px;
  width: calc(100% - 256px);
  box-sizing: border-box;
}
</style>
