<template>
  <header class="fixed top-4 left-1/2 transform -translate-x-1/2 z-50 w-[1278px] max-w-[95vw]">
    <nav
      class="relative bg-white/80 backdrop-blur-[6px] border-2 border-white rounded-2xl px-[24px] py-[36px] shadow-lg"
    >
      <!-- Logo - Center -->
      <router-link
        to="/"
        class="absolute left-1/2 top-[-16px] transform -translate-x-1/2 w-[74px] h-[64px] bg-[url('@/assets/logo.png')] bg-contain bg-no-repeat bg-center z-10"
        aria-label="Sportify Home"
      />

      <!-- Left Navigation -->
      <div class="flex items-center gap-[62px] absolute left-[24px] top-[36px]">
        <!-- Sản phẩm Link -->
        <router-link
          to="/products"
          class="relative group w-[107px] h-[31.59px] flex items-center border-b-2 border-transparent hover:border-black transition-colors"
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
        >
          <span
            class="font-geist text-base text-[#5E5F5C] leading-[26px]"
          >
            THỂ THAO
          </span>
        </router-link>
      </div>

      <!-- Right Side: Icons for logged in users -->
      <div v-if="isAuthenticated" class="flex items-center gap-[16px] absolute right-[24px] top-[31px]">
        <!-- Favorite Link -->
        <router-link
          to="/favorite"
          class="w-[36px] h-[34px] flex items-center justify-center hover:opacity-70 transition-opacity"
          aria-label="Favorite"
        >
          <svg class="w-[20px] h-[18px] text-[#5E5F5C]" viewBox="0 0 16 15" fill="currentColor">
            <path d="M8 1C4.5 4 2 6.5 2 9C2 10.5 3 11.5 4.5 11.5C5 11.5 5.5 11.4 6 11.3C6.5 11.4 7 11.5 7.5 11.5C9 11.5 10 10.5 10 9C10 6.5 7.5 4 6 1Z"/>
          </svg>
        </router-link>

        <!-- User Link -->
        <router-link
          to="/profile"
          class="w-[34px] h-[34px] flex items-center justify-center hover:opacity-70 transition-opacity"
          aria-label="Profile"
        >
          <svg class="w-[20px] h-[20px] text-[#5E5F5C]" viewBox="0 0 24 24" fill="currentColor">
            <path d="M12 12c2.21 0 4-1.79 4-4s-1.79-4-4-4-4 1.79-4 4 1.79 4 4 4zm0 2c-2.67 0-8 1.34-8 4v2h16v-2c0-2.66-5.33-4-8-4z"/>
          </svg>
        </router-link>

        <!-- Cart Link -->
        <router-link
          to="/cart"
          class="w-[36px] h-[34px] flex items-center justify-center hover:opacity-70 transition-opacity"
          aria-label="Cart"
        >
          <svg class="w-[19.98px] h-[20px] text-[#5E5F5C]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M2 3h6l4 6-4 6-6-4H2v-3z"/>
            <path d="M22 3h-6l-4 6 4 6 6-4h2v-3z"/>
          </svg>
        </router-link>

        <!-- Logout Button -->
        <button
          @click="handleLogout"
          class="flex items-center justify-center w-[34px] h-[34px] rounded-full border border-[#CFC4C6] hover:border-black transition-colors"
          aria-label="Logout"
        >
          <svg class="w-[16px] h-[16px] text-[#5E5F5C]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
          </svg>
        </button>
      </div>

      <!-- Right Side: Login Button for guests -->
      <div v-else class="flex items-center gap-[16px] absolute right-[24px] top-[31px]">
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
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

const router = useRouter()
const authStore = useAuthStore()

const isAuthenticated = computed(() => authStore.isAuthenticated)

const handleLogout = async () => {
  await authStore.logout()
  router.push('/')
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&display=swap') layer(fonts);

.font-geist {
  font-family: 'Inter', 'Geist', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}
</style>
