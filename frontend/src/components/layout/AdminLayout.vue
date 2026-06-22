<template>
  <div class="min-h-screen bg-gray-50 flex">
    <!-- Sidebar -->
    <aside class="hidden md:flex flex-col w-64 bg-gray-900 text-white min-h-screen fixed left-0 top-0 pt-20">
      <div class="flex-1 px-4 py-6 space-y-2">
        <router-link
          to="/admin/products"
          class="flex items-center px-4 py-3 text-gray-300 hover:bg-gray-800 hover:text-white rounded-lg transition-base"
          active-class="bg-gray-800 text-white"
        >
          <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
          </svg>
          Quản lý sản phẩm
        </router-link>

        <router-link
          to="/admin/orders"
          class="flex items-center px-4 py-3 text-gray-300 hover:bg-gray-800 hover:text-white rounded-lg transition-base"
          active-class="bg-gray-800 text-white"
        >
          <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2" />
          </svg>
          Quản lý đơn hàng
        </router-link>

        <router-link
          to="/admin/users"
          class="flex items-center px-4 py-3 text-gray-300 hover:bg-gray-800 hover:text-white rounded-lg transition-base"
          active-class="bg-gray-800 text-white"
        >
          <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
          </svg>
          Quản lý người dùng
        </router-link>

        <router-link
          to="/admin/statistics"
          class="flex items-center px-4 py-3 text-gray-300 hover:bg-gray-800 hover:text-white rounded-lg transition-base"
          active-class="bg-gray-800 text-white"
        >
          <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
          </svg>
          Thống kê
        </router-link>
      </div>

      <!-- Bottom section -->
      <div class="px-4 py-6 border-t border-gray-800">
        <router-link
          to="/"
          class="flex items-center text-gray-400 hover:text-white transition-base"
        >
          <svg class="w-5 h-5 mr-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 19l-7-7m0 0l7-7m-7 7h18" />
          </svg>
          Về trang chủ
        </router-link>
      </div>
    </aside>

    <!-- Mobile Header -->
    <div class="md:hidden fixed top-0 left-0 right-0 bg-gray-900 text-white z-50">
      <div class="flex items-center justify-between h-14 px-4">
        <span class="font-brand text-lg text-primary-500">SPORTIFY Admin</span>
        <button
          @click="mobileMenuOpen = !mobileMenuOpen"
          class="p-2"
          aria-label="Toggle menu"
        >
          <svg v-if="!mobileMenuOpen" class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 6h16M4 12h16M4 18h16" />
          </svg>
          <svg v-else class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>
    </div>

    <!-- Main Content -->
    <div class="flex-1 md:ml-64 pt-14 md:pt-0">
      <!-- Mobile Menu Overlay -->
      <div v-if="mobileMenuOpen" class="md:hidden fixed inset-0 bg-gray-900 bg-opacity-75 z-40" @click="mobileMenuOpen = false"></div>

      <!-- Mobile Menu -->
      <div v-if="mobileMenuOpen" class="md:hidden fixed inset-y-0 left-0 w-64 bg-gray-900 text-white z-50 pt-16">
        <div class="px-4 py-6 space-y-2">
          <router-link
            to="/admin/products"
            class="flex items-center px-4 py-3 text-gray-300 hover:bg-gray-800 rounded-lg"
            @click="mobileMenuOpen = false"
          >
            Quản lý sản phẩm
          </router-link>
          <router-link
            to="/admin/orders"
            class="flex items-center px-4 py-3 text-gray-300 hover:bg-gray-800 rounded-lg"
            @click="mobileMenuOpen = false"
          >
            Quản lý đơn hàng
          </router-link>
          <router-link
            to="/admin/users"
            class="flex items-center px-4 py-3 text-gray-300 hover:bg-gray-800 rounded-lg"
            @click="mobileMenuOpen = false"
          >
            Quản lý người dùng
          </router-link>
          <router-link
            to="/admin/statistics"
            class="flex items-center px-4 py-3 text-gray-300 hover:bg-gray-800 rounded-lg"
            @click="mobileMenuOpen = false"
          >
            Thống kê
          </router-link>
        </div>
      </div>

      <!-- Page Content -->
      <main class="p-4 md:p-8">
        <slot></slot>
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const mobileMenuOpen = ref(false)
</script>
