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

      
      <!-- Right Side -->
      <div class="flex items-center gap-[16px] absolute right-[24px] top-1/2 transform -translate-y-1/2">
        <!-- Search Trigger Button -->
        <button
          class="relative w-[180px] h-[40px] bg-[#F5F5F5] hover:bg-[#EAEAEA] rounded-full flex items-center px-4 transition-colors group"
          @click="openSearch"
        >
          <svg
            class="w-5 h-5 text-[#111] group-hover:text-black transition-colors"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <circle cx="11" cy="11" r="8" />
            <line x1="21" y1="21" x2="16.65" y2="16.65" />
          </svg>
          <span class="ml-3 font-geist text-[15px] font-medium text-[#707072] group-hover:text-black transition-colors whitespace-nowrap">Tìm kiếm</span>
        </button>

        <!-- Favorite Link -->
        <router-link
          v-if="isAuthenticated"
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
        <div v-if="isAuthenticated" class="relative group">
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
          
          <!-- Dropdown Menu Container -->
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
              <router-link
                v-if="authStore.isAdmin"
                to="/admin"
                class="block px-4 py-3 text-[14px] font-geist text-blue-600 font-bold hover:bg-[#F9F9F9] hover:text-blue-800 transition-colors border-t border-[#F0F0F0]"
              >
                Trang Quản Trị
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
            <circle cx="9" cy="21" r="1" />
            <circle cx="20" cy="21" r="1" />
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
          v-if="isAuthenticated"
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
            <line x1="21" y1="12" x2="9" y2="12" />
          </svg>
        </button>

        <!-- Login Button for guests -->
        <router-link
          v-if="!authStore.isAuthenticated"
          :to="{ path: '/login', query: $route.path !== '/login' && $route.path !== '/register' ? { redirect: $route.fullPath } : undefined }"
          class="flex items-center justify-center w-[114px] h-[40px] bg-black rounded-full hover:bg-[#333333] transition-colors"
        >
          <span class="font-geist text-xs font-semibold text-white leading-[14.88px] tracking-[1.2px]">
            ĐĂNG NHẬP
          </span>
        </router-link>
      </div>
    </nav>


    <!-- Search Overlay -->
    <transition
      enter-active-class="transition duration-300 ease-out"
      enter-from-class="transform -translate-y-4 opacity-0"
      enter-to-class="transform translate-y-0 opacity-100"
      leave-active-class="transition duration-200 ease-in"
      leave-from-class="transform translate-y-0 opacity-100"
      leave-to-class="transform -translate-y-4 opacity-0"
    >
      <div
        v-if="isSearchOpen"
        class="fixed inset-0 z-50 bg-white min-h-[400px] h-fit pb-12 shadow-xl"
      >
        <div class="w-[1278px] max-w-[95vw] mx-auto pt-6 px-6 relative">
          <!-- Overlay Header (Logo, Input, Cancel) -->
          <div class="flex items-center justify-between gap-8 h-[64px]">
            <div class="w-[74px] h-[64px] bg-[url('@/assets/logo.png')] bg-contain bg-no-repeat bg-center" />
            
            <div class="flex-1 max-w-[640px] relative">
              <input
                ref="searchInputRef"
                v-model="searchQuery"
                type="text"
                placeholder="Tìm kiếm"
                class="w-full h-[48px] bg-[#F5F5F5] hover:bg-[#EAEAEA] rounded-full pl-[56px] pr-4 text-base font-geist text-[#111] outline-none transition-colors"
                @keyup.enter="handleSearch"
              />
              <svg
                class="absolute left-4 top-1/2 -translate-y-1/2 w-6 h-6 text-[#111]"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
                stroke-linecap="round"
                stroke-linejoin="round"
              >
                <circle cx="11" cy="11" r="8" />
                <line x1="21" y1="21" x2="16.65" y2="16.65" />
              </svg>
            </div>

            <button
              class="font-geist text-base font-medium text-[#111] hover:text-[#707072] transition-colors whitespace-nowrap"
              @click="closeSearch"
            >
              Hủy
            </button>
          </div>

          <!-- Overlay Content (Popular & Recent) -->
          <div class="max-w-[640px] mx-auto mt-12 flex flex-col gap-10 pl-[86px] pr-[120px]">
            
            <!-- Popular Search Terms -->
            <div>
              <h3 class="font-geist text-base text-[#707072] mb-4">Từ khóa phổ biến</h3>
              <div class="flex flex-wrap gap-2">
                <button
                  v-for="term in popularSearches"
                  :key="term"
                  class="h-[34px] px-5 bg-[#F5F5F5] hover:bg-[#EAEAEA] rounded-full font-geist text-sm text-[#111] transition-colors whitespace-nowrap"
                  @click="submitSearchTerm(term)"
                >
                  {{ term }}
                </button>
              </div>
            </div>

            <!-- Recent Searches -->
            <div v-if="recentSearches.length > 0">
              <h3 class="font-geist text-base text-[#707072] mb-4">Tìm kiếm gần đây</h3>
              <div class="flex flex-col gap-1">
                <div
                  v-for="(term, index) in recentSearches"
                  :key="index"
                  class="flex items-center justify-between group"
                >
                  <button
                    class="flex-1 text-left py-2 font-geist text-lg font-medium text-[#111] hover:text-[#707072] transition-colors"
                    @click="submitSearchTerm(term)"
                  >
                    {{ term }}
                  </button>
                  <button
                    class="p-2 opacity-0 group-hover:opacity-100 transition-opacity text-[#111] hover:text-[#707072]"
                    @click="removeRecentSearch(index)"
                  >
                    <svg class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <path d="M18 6L6 18M6 6l12 12" />
                    </svg>
                  </button>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </transition>

    <!-- Backdrop for overlay -->
    <div
      v-if="isSearchOpen"
      class="fixed inset-0 z-40 bg-black/20 backdrop-blur-sm"
      @click="closeSearch"
    />
  </header>
</template>

<script setup lang="ts">
import { computed, ref, watch, nextTick, onMounted } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import { useCartStore } from '@/stores/cart.store'
import { useRouter, useRoute } from 'vue-router'
import { useToast } from 'vue-toastification'

const authStore = useAuthStore()
const cartStore = useCartStore()
const router = useRouter()
const route = useRoute()
const toast = useToast()

const searchQuery = ref(route.query.keyword as string || '')
const isSearchOpen = ref(false)
const searchInputRef = ref<HTMLInputElement | null>(null)
const recentSearches = ref<string[]>([])

const popularSearches = [
  'Nike Air Force 1',
  'Jordan 1',
  'Adidas Ultraboost',
  'Giày chạy bộ',
  'Giày bóng đá',
  'Balo thể thao'
]

onMounted(() => {
  const saved = localStorage.getItem('recent_searches')
  if (saved) {
    try {
      recentSearches.value = JSON.parse(saved)
    } catch {
      recentSearches.value = []
    }
  }
})

const saveRecentSearch = (term: string) => {
  const arr = [...recentSearches.value]
  const idx = arr.indexOf(term)
  if (idx > -1) arr.splice(idx, 1)
  arr.unshift(term)
  if (arr.length > 5) arr.pop()
  recentSearches.value = arr
  localStorage.setItem('recent_searches', JSON.stringify(arr))
}

const removeRecentSearch = (index: number) => {
  recentSearches.value.splice(index, 1)
  localStorage.setItem('recent_searches', JSON.stringify(recentSearches.value))
}

const openSearch = () => {
  isSearchOpen.value = true
  searchQuery.value = route.query.keyword as string || ''
  nextTick(() => {
    searchInputRef.value?.focus()
  })
}

const closeSearch = () => {
  isSearchOpen.value = false
}

const submitSearchTerm = (term: string) => {
  searchQuery.value = term
  handleSearch()
}

const handleSearch = () => {
  if (searchQuery.value.trim()) {
    const term = searchQuery.value.trim()
    
    // Check for special characters often used in SQL injection
    const invalidCharsRegex = /['";=%*/\\]/
    if (invalidCharsRegex.test(term)) {
      toast.error('Từ khóa không được chứa ký tự đặc biệt')
      return
    }

    saveRecentSearch(term)
    router.push({ path: '/products', query: { ...route.query, keyword: term } })
  } else {
    // If empty, remove keyword from query
    const query = { ...route.query }
    delete query.keyword
    router.push({ path: '/products', query })
  }
  closeSearch()
}

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
