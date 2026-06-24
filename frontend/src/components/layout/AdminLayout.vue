<template>
  <div class="admin-layout">
    <!-- Sidebar Navigation -->
    <aside class="sidebar">
      <!-- Logo Section -->
      <div class="logo-section">
        <img src="@/assets/logo.png" alt="Logo" class="logo" />
      </div>

      <!-- Navigation Menu -->
      <nav class="nav-menu">
        <router-link
          to="/admin/users"
          class="nav-item"
          :class="{ active: $route.name?.startsWith('admin-users') }"
        >
          <span class="nav-icon">👥</span>
          <span class="nav-text">Quản Lý Người Dùng</span>
        </router-link>
        <router-link
          to="/admin/orders"
          class="nav-item"
          :class="{ active: $route.name?.startsWith('admin-orders') }"
        >
          <span class="nav-icon">📦</span>
          <span class="nav-text">Quản Lý Đơn Hàng</span>
        </router-link>
        <router-link
          to="/admin/products"
          class="nav-item"
          :class="{ active: $route.name?.startsWith('admin-products') }"
        >
          <span class="nav-icon">🏷️</span>
          <span class="nav-text">Quản Lý Sản Phẩm</span>
        </router-link>
        <router-link
          to="/admin/loyalty/vouchers"
          class="nav-item"
          :class="{ active: $route.name?.startsWith('admin-vouchers') }"
        >
          <span class="nav-icon">🎟️</span>
          <span class="nav-text">Quản Lý Voucher</span>
        </router-link>
        <router-link
          to="/admin/statistics"
          class="nav-item active"
          :class="{ active: $route.name?.startsWith('admin-statistics') }"
        >
          <span class="nav-icon">📊</span>
          <span class="nav-text">Quản Lý Thống Kê</span>
        </router-link>
      </nav>

      <!-- Footer Menu -->
      <div class="footer-menu">
        <router-link to="/help" class="nav-item">
          <span class="nav-icon">❓</span>
          <span class="nav-text">HELP CENTER</span>
        </router-link>
        <button @click="handleLogout" class="nav-item logout-btn">
          <span class="nav-icon">🚪</span>
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

<script setup>
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

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
  background: #F9F9F9;
}

/* Sidebar */
.sidebar {
  width: 256px;
  background: #FFFFFF;
  border-right: 1px solid #000000;
  display: flex;
  flex-direction: column;
  padding-top: 140px;
  position: fixed;
  height: 100vh;
  top: 0;
  left: 0;
  z-index: 100;
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
}

.nav-item {
  display: flex;
  align-items: center;
  padding: 16px 24px;
  text-decoration: none;
  color: #5E5F5C;
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
  background: #F9F9F9;
}

.nav-item.active {
  background: #FFFFFF;
  border-left-color: #FFFFFF;
  color: #000000;
  font-weight: 700;
}

.nav-icon {
  width: 20px;
  height: 20px;
  margin-right: 16px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}

.nav-text {
  flex: 1;
}

.footer-menu {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  border-top: 1px solid #E8E8E8;
  padding: 16px 0;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.logout-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: #5E5F5C;
}

.logout-btn:hover {
  background: #F9F9F9;
}

/* Main Content */
.main-content {
  margin-left: 256px;
  padding: 120px 20px 20px;
  width: calc(100% - 256px);
  max-width: 1278px;
}
</style>
