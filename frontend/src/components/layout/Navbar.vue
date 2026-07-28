<template>
  <header class="fixed top-4 left-1/2 transform -translate-x-1/2 z-50 w-[1278px] max-w-[95vw]">
    <nav
      class="relative bg-white backdrop-blur-[6px] border-2 border-white rounded-2xl px-[24px] py-[36px] shadow-lg"
    >
      <!-- Logo - Center -->
      <router-link
        to="/"
        class="absolute left-1/2 top-1/2 transform -translate-x-1/2 -translate-y-1/2 w-[74px] h-[64px] bg-[url('@/assets/logo.png')] bg-contain bg-no-repeat bg-center z-10"
        aria-label="Sportify Home"
      />

      <!-- Left Navigation -->
      <div class="flex items-center gap-[62px] absolute left-[24px] top-1/2 transform -translate-y-1/2">
        <!-- Sản phẩm Link -->
        <router-link
          to="/products"
          class="relative group w-[107px] h-[31.59px] flex items-center border-b-2 border-transparent hover:border-black transition-colors"
          active-class="border-black"
        >
          <span
            class="font-geist text-base text-[#5E5F5C] leading-[26px]"
          >
            SẢN PHẨM
          </span>
        </router-link>

        <!-- Thể thao Link -->
        <router-link
          to="/products?sport=all"
          class="relative group w-[81.28px] h-[31.59px] flex items-center border-b-2 border-transparent hover:border-black transition-colors"
          active-class="border-black"
        >
          <span
            class="font-geist text-base text-[#5E5F5C] leading-[26px]"
          >
            THỂ THAO
          </span>
        </router-link>

        <!-- Thương hiệu Link -->
        <router-link
          to="/products?brand=all"
          class="relative group w-[123px] h-[31.59px] flex items-center border-b-2 border-transparent hover:border-black transition-colors"
          active-class="border-black"
        >
          <span
            class="font-geist text-base text-[#5E5F5C] leading-[26px]"
          >
            THƯƠNG HIỆU
          </span>
        </router-link>
      </div>

      <!-- Right Side: Icons for logged in users -->
      <div
        v-if="isAuthenticated"
        class="flex items-center gap-[16px] absolute right-[24px] top-1/2 transform -translate-y-1/2"
      >
        <!-- Favorite Link -->
        <router-link
          to="/favorite"
          class="w-[36px] h-[34px] flex items-center justify-center hover:opacity-70 transition-opacity"
          aria-label="Favorite"
        >
          <svg
            class="w-[20px] h-[20px] text-[#5E5F5C]"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
          </svg>
        </router-link>

        <!-- User Dropdown -->
        <div class="relative group">
          <button
            class="w-[34px] h-[34px] flex items-center justify-center hover:opacity-70 transition-opacity"
            aria-label="Profile Menu"
          >
            <svg
              class="w-[20px] h-[20px] text-[#5E5F5C]"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
              <circle
                cx="12"
                cy="7"
                r="4"
              />
            </svg>
          </button>
          
          <!-- Dropdown Menu Container (bridges the hover gap) -->
          <div class="absolute right-0 top-full pt-2 w-[180px] opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-50">
            <div class="bg-white border border-[#F0F0F0] rounded-xl shadow-[0_4px_12px_rgba(0,0,0,0.05)] overflow-hidden">
              <router-link
                to="/profile"
                class="block px-4 py-3 text-[14px] font-geist text-[#5E5F5C] hover:bg-[#F9F9F9] hover:text-black transition-colors"
              >
                Hồ sơ của tôi
              </router-link>
              <router-link
                to="/orders"
                class="block px-4 py-3 text-[14px] font-geist text-[#5E5F5C] hover:bg-[#F9F9F9] hover:text-black transition-colors border-t border-[#F0F0F0]"
              >
                Lịch sử đơn hàng
              </router-link>
              <router-link
                to="/my-rewards"
                class="block px-4 py-3 text-[14px] font-geist text-[#5E5F5C] hover:bg-[#F9F9F9] hover:text-black transition-colors border-t border-[#F0F0F0]"
              >
                Điểm & Voucher
              </router-link>
            </div>
          </div>
        </div>

        <!-- Cart Link -->
        <router-link
          to="/cart"
          class="w-[36px] h-[34px] flex items-center justify-center hover:opacity-70 transition-opacity relative"
          aria-label="Cart"
        >
          <svg
            class="w-[20px] h-[20px] text-[#5E5F5C]"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <circle
              cx="9"
              cy="21"
              r="1"
            />
            <circle
              cx="20"
              cy="21"
              r="1"
            />
            <path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6" />
          </svg>
          <!-- Badge showing cart item count -->
          <span
            v-if="cartItemCount > 0"
            class="absolute -top-1 -right-1 bg-red-500 text-white text-[10px] font-bold min-w-[18px] h-[18px] flex items-center justify-center rounded-full px-1"
          >
            {{ cartItemCount > 99 ? '99+' : cartItemCount }}
          </span>
        </router-link>

        <!-- Logout Button -->
        <button
          class="flex items-center justify-center w-[34px] h-[34px] rounded-full border border-[#CFC4C6] hover:border-black transition-colors"
          aria-label="Logout"
          @click="handleLogout"
        >
          <svg
            class="w-[16px] h-[16px] text-[#5E5F5C]"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
          >
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4" />
            <polyline points="16 17 21 12 16 7" />
            <line
              x1="21"
              y1="12"
              x2="9"
              y2="12"
            />
          </svg>
        </button>
      </div>

      <!-- Right Side: Login Button for guests -->
      <div
        v-else
        class="flex items-center gap-[16px] absolute right-[24px] top-1/2 transform -translate-y-1/2"
      >
        <router-link
          to="/login"
          class="flex items-center justify-center w-[90px] h-[31px] bg-black rounded-sm"
        >
          <span class="text-white text-[10px] font-normal uppercase leading-[15px] tracking-wide">
            ĐĂNG NHẬP
          </span>
        </router-link>
      </div>
    </nav>
  </header>
</template>

<script setup lang="ts">
import { computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useCartStore } from '@/stores/cart.store'

const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()

const isAuthenticated = computed(() => authStore.isAuthenticated)
const cartItemCount = computed(() => cartStore.totalItems)

const handleLogout = async () => {
  await authStore.logout()
  router.push('/')
}

// Fetch cart when user logs in (auth state changes from false to true)
watch(isAuthenticated, (newVal, oldVal) => {
  if (newVal && !oldVal) {
    cartStore.fetchCart().catch(console.error)
  }
})
</script>

<style scoped>
</style>
