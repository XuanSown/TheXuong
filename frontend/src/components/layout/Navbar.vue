<template>
  <header class="bg-white shadow-sm sticky top-0 z-50">
    <div class="container-custom">
      <div class="flex items-center justify-between h-16 md:h-20">
        <!-- Logo -->
        <router-link to="/" class="flex items-center flex-shrink-0">
          <span class="font-brand text-xl md:text-2xl text-primary-500">SPORTIFY</span>
        </router-link>

        <!-- Desktop Navigation -->
        <nav class="hidden md:flex items-center space-x-8">
          <router-link
            to="/products"
            class="text-gray-700 hover:text-primary-500 font-medium transition-base"
          >
            Sản phẩm
          </router-link>

          <!-- Sport Dropdown -->
          <div class="relative group">
            <button
              class="flex items-center text-gray-700 hover:text-primary-500 font-medium transition-base"
            >
              Thể thao
              <svg class="w-4 h-4 ml-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
              </svg>
            </button>
            <!-- Dropdown menu -->
            <div class="absolute left-0 mt-2 w-48 bg-white rounded-md shadow-lg py-2 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200">
              <router-link
                v-for="sport in sports"
                :key="sport"
                :to="`/products?sport=${encodeURIComponent(sport)}`"
                class="block px-4 py-2 text-gray-700 hover:bg-primary-50 hover:text-primary-500"
              >
                {{ sport }}
              </router-link>
            </div>
          </div>

          <!-- Brand Dropdown -->
          <div class="relative group">
            <button
              class="flex items-center text-gray-700 hover:text-primary-500 font-medium transition-base"
            >
              Thương hiệu
              <svg class="w-4 h-4 ml-1" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
              </svg>
            </button>
            <div class="absolute left-0 mt-2 w-48 bg-white rounded-md shadow-lg py-2 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200">
              <router-link
                v-for="brand in brands"
                :key="brand"
                :to="`/products?brand=${encodeURIComponent(brand)}`"
                class="block px-4 py-2 text-gray-700 hover:bg-primary-50 hover:text-primary-500"
              >
                {{ brand }}
              </router-link>
            </div>
          </div>
        </nav>

        <!-- Right side: Cart & User -->
        <div class="flex items-center space-x-4">
          <!-- Cart -->
          <router-link
            to="/cart"
            class="relative p-2 text-gray-700 hover:text-primary-500 transition-base"
          >
            <svg class="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
            </svg>
            <span
              v-if="cartCount > 0"
              class="absolute -top-1 -right-1 bg-primary-500 text-white text-xs font-bold rounded-full h-5 w-5 flex items-center justify-center"
            >
              {{ cartCount }}
            </span>
          </router-link>

          <!-- User Menu (Desktop) -->
          <div v-if="isAuthenticated" class="hidden md:block relative group">
            <button class="flex items-center space-x-2 text-gray-700 hover:text-primary-500 transition-base">
              <svg class="w-6 h-6" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd" />
              </svg>
              <span class="text-sm font-medium">{{ user?.fullName || user?.username }}</span>
              <svg class="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                <path fill-rule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clip-rule="evenodd" />
              </svg>
            </button>
            <div class="absolute right-0 mt-2 w-48 bg-white rounded-md shadow-lg py-2 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200">
              <router-link
                to="/profile"
                class="block px-4 py-2 text-gray-700 hover:bg-primary-50 hover:text-primary-500"
              >
                Tài khoản
              </router-link>
              <router-link
                to="/orders"
                class="block px-4 py-2 text-gray-700 hover:bg-primary-50 hover:text-primary-500"
              >
                Đơn hàng
              </router-link>
              <!-- Admin link -->
              <router-link
                v-if="isAdmin"
                to="/admin/products"
                class="block px-4 py-2 text-gray-700 hover:bg-primary-50 hover:text-primary-500"
              >
                Quản trị
              </router-link>
              <hr class="my-2 border-gray-200">
              <button
                @click="handleLogout"
                class="block w-full text-left px-4 py-2 text-gray-700 hover:bg-red-50 hover:text-red-500"
              >
                Đăng xuất
              </button>
            </div>
          </div>

          <!-- Login/Register (Desktop) -->
          <div v-else class="hidden md:flex items-center space-x-2">
            <router-link
              to="/login"
              class="px-4 py-2 text-gray-700 hover:text-primary-500 font-medium transition-base"
            >
              Đăng nhập
            </router-link>
            <router-link
              to="/register"
              class="px-4 py-2 bg-primary-500 text-white rounded-md hover:bg-primary-600 font-medium transition-base"
            >
              Đăng ký
            </router-link>
          </div>

          <!-- Mobile menu button -->
          <button
            @click="mobileMenuOpen = !mobileMenuOpen"
            class="md:hidden p-2 text-gray-700 hover:text-primary-500"
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
    </div>

    <!-- Mobile Menu -->
    <div v-if="mobileMenuOpen" class="md:hidden bg-white border-t border-gray-200">
      <div class="container-custom py-4 space-y-4">
        <!-- Mobile nav links -->
        <router-link
          to="/products"
          class="block text-gray-700 hover:text-primary-500 font-medium py-2"
          @click="mobileMenuOpen = false"
        >
          Sản phẩm
        </router-link>

        <!-- Mobile Sport dropdown -->
        <div class="border-t border-gray-200 pt-4">
          <p class="text-sm font-semibold text-gray-500 uppercase mb-2">Thể thao</p>
          <div class="grid grid-cols-2 gap-2">
            <router-link
              v-for="sport in sports"
              :key="sport"
              :to="`/products?sport=${encodeURIComponent(sport)}`"
              class="text-gray-700 hover:text-primary-500 py-1"
              @click="mobileMenuOpen = false"
            >
              {{ sport }}
            </router-link>
          </div>
        </div>

        <!-- Mobile Brand dropdown -->
        <div class="border-t border-gray-200 pt-4">
          <p class="text-sm font-semibold text-gray-500 uppercase mb-2">Thương hiệu</p>
          <div class="grid grid-cols-2 gap-2">
            <router-link
              v-for="brand in brands"
              :key="brand"
              :to="`/products?brand=${encodeURIComponent(brand)}`"
              class="text-gray-700 hover:text-primary-500 py-1"
              @click="mobileMenuOpen = false"
            >
              {{ brand }}
            </router-link>
          </div>
        </div>

        <!-- Mobile Auth -->
        <div v-if="isAuthenticated" class="border-t border-gray-200 pt-4 space-y-2">
          <router-link
            to="/profile"
            class="block text-gray-700 hover:text-primary-500 py-2"
            @click="mobileMenuOpen = false"
          >
            Tài khoản
          </router-link>
          <router-link
            to="/orders"
            class="block text-gray-700 hover:text-primary-500 py-2"
            @click="mobileMenuOpen = false"
          >
            Đơn hàng
          </router-link>
          <router-link
            v-if="isAdmin"
            to="/admin/products"
            class="block text-gray-700 hover:text-primary-500 py-2"
            @click="mobileMenuOpen = false"
          >
            Quản trị
          </router-link>
          <button
            @click="handleLogout"
            class="block w-full text-left text-red-500 hover:text-red-600 py-2"
          >
            Đăng xuất
          </button>
        </div>

        <div v-else class="border-t border-gray-200 pt-4 flex flex-col space-y-2">
          <router-link
            to="/login"
            class="block text-center py-2 text-gray-700 border border-gray-300 rounded-md hover:bg-gray-50"
            @click="mobileMenuOpen = false"
          >
            Đăng nhập
          </router-link>
          <router-link
            to="/register"
            class="block text-center py-2 bg-primary-500 text-white rounded-md hover:bg-primary-600"
            @click="mobileMenuOpen = false"
          >
            Đăng ký
          </router-link>
        </div>
      </div>
    </div>
  </header>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useCartStore } from '@/stores/cart.store'

const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()

const mobileMenuOpen = ref(false)

const user = computed(() => authStore.user)
const isAuthenticated = computed(() => authStore.isAuthenticated)
const isAdmin = computed(() => authStore.isAdmin)
const cartCount = computed(() => cartStore.totalItems)

// Mock data - sẽ thay bằng API
const sports = ref([
  'Bóng đá',
  'Bóng rổ',
  'Tennis',
  'Chạy bộ',
  'Bơi lội',
  'Gym & Fitness'
])

const brands = ref([
  'Nike',
  'Adidas',
  'Puma',
  'New Balance',
  'Under Armour',
  'Asics'
])

onMounted(async () => {
  await authStore.fetchUser()
  if (isAuthenticated.value) {
    await cartStore.fetchCart()
  }
})

const handleLogout = async () => {
  mobileMenuOpen.value = false
  await authStore.logout()
}
</script>
