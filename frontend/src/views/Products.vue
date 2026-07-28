<template>
  <div class="min-h-screen bg-[#F9F9F9]">
    <main class="w-full max-w-[1280px] mx-auto px-4 pt-[10px] pb-8">
      <!-- Page Header -->
      <section class="bg-white border-y border-[#CFC4C6] py-16 mb-8">
        <div class="w-[1152px] mx-auto">
          <h1 class="font-geist text-[56px] leading-[20px] font-bold text-[#1A1C1C] tracking-[-1.12px] uppercase">
            DANH SÁCH SẢN PHẨM
          </h1>
        </div>
      </section>

      <!-- Main Content with Sidebar -->
      <section class="w-[1152px] mx-auto relative flex gap-8">
        <!-- Filter Sidebar -->
        <aside class="w-[264px] flex-shrink-0">
          <div class="bg-white border border-[rgba(207,196,197,0.3)] p-6 sticky top-[140px]">
            <!-- Sports Filter Section -->
            <div class="mb-8">
              <h3 class="font-geist text-sm font-semibold uppercase tracking-[1.2px] text-[#1A1C1C] mb-4">
                Thể thao
              </h3>
              <ul class="flex flex-col gap-3">
                <li>
                  <router-link
                    to="/products"
                    :class="['text-sm', activeSport === '' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    Tất cả
                  </router-link>
                </li>
                <li>
                  <router-link
                    to="/products?sport=football"
                    :class="['text-sm', activeSport === 'football' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    Bóng đá
                  </router-link>
                </li>
                <li>
                  <router-link
                    to="/products?sport=badminton"
                    :class="['text-sm', activeSport === 'badminton' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    Cầu lông
                  </router-link>
                </li>
                <li>
                  <router-link
                    to="/products?sport=running"
                    :class="['text-sm', activeSport === 'running' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    Chạy bộ
                  </router-link>
                </li>
                <li>
                  <router-link
                    to="/products?sport=other"
                    :class="['text-sm', activeSport === 'other' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    Khác
                  </router-link>
                </li>
              </ul>
            </div>

            <!-- Divider -->
            <div class="border-t border-[rgba(207,196,197,0.3)] mb-6" />

            <!-- Brand Filter Section -->
            <div>
              <h3 class="font-geist text-sm font-semibold uppercase tracking-[1.2px] text-[#1A1C1C] mb-4">
                Thương hiệu
              </h3>
              <ul class="flex flex-col gap-3">
                <li>
                  <router-link
                    to="/products"
                    :class="['text-sm', activeBrand === '' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    Tất cả
                  </router-link>
                </li>
                <li>
                  <router-link
                    to="/products?brand=nike"
                    :class="['text-sm', activeBrand === 'nike' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    Nike
                  </router-link>
                </li>
                <li>
                  <router-link
                    to="/products?brand=adidas"
                    :class="['text-sm', activeBrand === 'adidas' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    Adidas
                  </router-link>
                </li>
                <li>
                  <router-link
                    to="/products?brand=lining"
                    :class="['text-sm', activeBrand === 'lining' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    Li-Ning
                  </router-link>
                </li>
                <li>
                  <router-link
                    to="/products?brand=puma"
                    :class="['text-sm', activeBrand === 'puma' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    Puma
                  </router-link>
                </li>
                <li>
                  <router-link
                    to="/products?brand=other"
                    :class="['text-sm', activeBrand === 'other' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    Khác
                  </router-link>
                </li>
              </ul>
            </div>
          </div>
        </aside>

        <!-- Product Grid -->
        <section class="flex-1">
          <!-- Active Filters Display -->
          <div
            v-if="activeSport || activeBrand"
            class="mb-6 flex items-center gap-2"
          >
            <span class="text-sm text-[#5E5F5C]">Đang lọc:</span>
            <div
              v-if="activeSport"
              class="flex items-center gap-2"
            >
              <span class="px-3 py-1 bg-[#E5E7EB] text-sm text-[#1A1C1C] rounded flex items-center">
                Thể thao: {{ getSportLabel(activeSport) }}
                <button
                  class="ml-2 hover:text-red-500"
                  @click="clearSport"
                >&times;</button>
              </span>
            </div>
            <div
              v-if="activeBrand"
              class="flex items-center gap-2"
            >
              <span class="px-3 py-1 bg-[#E5E7EB] text-sm text-[#1A1C1C] rounded flex items-center">
                Thương hiệu: {{ getBrandLabel(activeBrand) }}
                <button
                  class="ml-2 hover:text-red-500"
                  @click="clearBrand"
                >&times;</button>
              </span>
            </div>
            <button
              v-if="activeSport || activeBrand"
              class="text-sm text-[#5E5F5C] hover:text-black underline"
              @click="clearAllFilters"
            >
              Xóa tất cả
            </button>
          </div>

          <div class="relative min-h-[400px]">
            <!-- Skeleton Loading State -->
            <div
              v-if="isLoading"
              class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-x-6 gap-y-12 mb-8"
            >
              <div
                v-for="i in 6"
                :key="i"
                class="w-full flex flex-col gap-4"
              >
                <BaseSkeleton type="image" />
                <div class="flex flex-col gap-2">
                  <BaseSkeleton
                    type="text"
                    class="w-1/4"
                  />
                  <BaseSkeleton
                    type="title"
                    class="w-3/4"
                  />
                  <BaseSkeleton
                    type="text"
                    class="w-1/3"
                  />
                </div>
              </div>
            </div>

            <!-- Product List -->
            <div
              v-else-if="products.length > 0"
              class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-x-6 gap-y-12 mb-8"
            >
              <ProductCard
                v-for="product in products"
                :key="product.id"
                :product="product"
              />
            </div>

            <!-- No Results Message -->
            <div
              v-else
              class="absolute inset-0 flex flex-col items-center justify-center py-16"
            >
              <p class="font-gelasio text-xl text-[#5E5F5C] mb-4">
                Không tìm thấy sản phẩm phù hợp
              </p>
              <button
                class="px-6 py-3 bg-black text-white text-sm font-semibold uppercase tracking-[1.2px] rounded-sm hover:bg-gray-900 transition-colors"
                @click="clearAllFilters"
              >
                Xóa bộ lọc
              </button>
            </div>
          </div>

          <!-- Pagination -->
          <div
            v-if="totalPages > 1 && !isLoading"
            class="flex justify-center items-center gap-2 mt-16"
          >
            <button
              :disabled="currentPage === 0"
              class="px-4 py-2 border border-[#CFC4C6] rounded-sm text-sm uppercase tracking-[1.8px] text-[#1A1C1C] hover:bg-black hover:text-white transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              @click="changePage(currentPage - 1)"
            >
              PREV
            </button>
            <button
              v-for="page in totalPages"
              :key="page" 
              :class="['px-4 py-2 text-sm uppercase tracking-[1.8px] rounded-sm', currentPage === page - 1 ? 'bg-black text-white' : 'text-[#1A1C1C] border border-transparent hover:bg-gray-100']"
              @click="changePage(page - 1)"
            >
              {{ page }}
            </button>
            <button
              :disabled="currentPage >= totalPages - 1"
              class="px-5 py-2 border border-[#CFC4C6] rounded-sm text-sm uppercase tracking-[1.8px] text-[#1A1C1C] hover:bg-black hover:text-white transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
              @click="changePage(currentPage + 1)"
            >
              NEXT
            </button>
          </div>
        </section>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { productService } from '@/services/product.service'
import type { Product } from '@/types'
import ProductCard from '@/components/ui/ProductCard.vue'
import BaseSkeleton from '@/components/ui/BaseSkeleton.vue'

const route = useRoute()
const router = useRouter()

// State
const products = ref<Product[]>([])
const isLoading = ref(true)
const currentPage = ref(0)
const totalPages = ref(1)

// Active filters from query params
const activeSport = computed(() => (route.query.sport as string) || '')
const activeBrand = computed(() => (route.query.brand as string) || '')

// ponytail: single source of truth - 1 map cho cả API (title case) và UI label (uppercase).
const SPORT_NAME: Record<string, string> = {
  football: 'Bóng đá',
  badminton: 'Cầu lông',
  running: 'Chạy bộ',
  basketball: 'Bóng rổ',
  other: 'Khác'
}

const BRAND_NAME: Record<string, string> = {
  nike: 'Nike',
  adidas: 'Adidas',
  lining: 'Li-Ning',
  puma: 'Puma',
  yonex: 'Yonex',
  asics: 'Asics',
  mizuno: 'Mizuno',
  other: 'Khác'
}

const getApiSportName = (sport: string): string => SPORT_NAME[sport] || sport
const getApiBrandName = (brand: string): string => BRAND_NAME[brand] || brand
const getSportLabel = (sport: string): string => (SPORT_NAME[sport] || sport).toUpperCase()
const getBrandLabel = (brand: string): string => (BRAND_NAME[brand] || brand).toUpperCase()

const fetchProducts = async () => {
  isLoading.value = true
  try {
    const response = await productService.getProducts({
      page: currentPage.value,
      size: 9,
      sport: getApiSportName(activeSport.value),
      brand: getApiBrandName(activeBrand.value)
    })
    products.value = response.content || []
    totalPages.value = response.totalPages || 1
  } catch (error) {
    console.error('Failed to fetch products:', error)
  } finally {
    isLoading.value = false
  }
}

// Watchers
watch(
  () => [route.query.sport, route.query.brand],
  () => {
    currentPage.value = 0
    fetchProducts()
  }
)

onMounted(() => {
  fetchProducts()
})

const changePage = (page: number) => {
  if (page >= 0 && page < totalPages.value) {
    currentPage.value = page
    fetchProducts()
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }
}

// Clear filter functions
const clearSport = () => {
  router.push({ path: '/products', query: { ...route.query, sport: undefined } })
}

const clearBrand = () => {
  router.push({ path: '/products', query: { ...route.query, brand: undefined } })
}

const clearAllFilters = () => {
  router.push('/products')
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Gelasio:wght@400;500;600;700&family=Inter:wght@400;500;600;700&display=swap');

.font-geist {
  font-family: 'Geist', sans-serif;
}

.font-gelasio {
  font-family: 'Geist', sans-serif;
}
</style>
