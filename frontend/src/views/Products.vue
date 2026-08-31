<template>
  <div class="min-h-screen bg-[#F9F9F9]">
    <main class="w-full max-w-[1280px] mx-auto px-4 pt-[10px] pb-8">
      <!-- Page Header -->
      <section class="bg-white border-y border-[#CFC4C6] py-16 mb-8">
        <div class="w-[1152px] mx-auto">
          <h1 class="font-geist text-[56px] leading-[20px] font-bold text-[#1A1C1C] tracking-[-1.12px] uppercase">
            {{ t('products.pageTitle') }}
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
                {{ t('common.sport') }}
              </h3>
              <ul class="flex flex-col gap-3">
                <li>
                  <router-link
                    :to="getFilterUrl('sport', null)"
                    :class="['text-sm', activeSport === '' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    {{ t('common.all') }}
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="getFilterUrl('sport', 'football')"
                    :class="['text-sm', activeSport === 'football' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    {{ t('sports.football') }}
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="getFilterUrl('sport', 'badminton')"
                    :class="['text-sm', activeSport === 'badminton' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    {{ t('sports.badminton') }}
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="getFilterUrl('sport', 'running')"
                    :class="['text-sm', activeSport === 'running' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    {{ t('sports.running') }}
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="getFilterUrl('sport', 'basketball')"
                    :class="['text-sm', activeSport === 'basketball' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    {{ t('sports.basketball') }}
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="getFilterUrl('sport', 'pickleball')"
                    :class="['text-sm', activeSport === 'pickleball' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    {{ t('sports.pickleball') }}
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="getFilterUrl('sport', 'racing')"
                    :class="['text-sm', activeSport === 'racing' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    {{ t('sports.racing') }}
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="getFilterUrl('sport', 'gym')"
                    :class="['text-sm', activeSport === 'gym' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    {{ t('sports.gym') }}
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="getFilterUrl('sport', 'other')"
                    :class="['text-sm', activeSport === 'other' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    {{ t('common.other') }}
                  </router-link>
                </li>
              </ul>
            </div>

            <!-- Divider -->
            <div class="border-t border-[rgba(207,196,197,0.3)] mb-6" />

            <!-- Brand Filter Section -->
            <div>
              <h3 class="font-geist text-sm font-semibold uppercase tracking-[1.2px] text-[#1A1C1C] mb-4">
                {{ t('common.brand') }}
              </h3>
              <ul class="flex flex-col gap-3">
                <li>
                  <router-link
                    :to="getFilterUrl('brand', null)"
                    :class="['text-sm', activeBrand === '' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    {{ t('common.all') }}
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="getFilterUrl('brand', 'nike')"
                    :class="['text-sm', activeBrand === 'nike' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    Nike
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="getFilterUrl('brand', 'adidas')"
                    :class="['text-sm', activeBrand === 'adidas' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    Adidas
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="getFilterUrl('brand', 'lining')"
                    :class="['text-sm', activeBrand === 'lining' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    Li-Ning
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="getFilterUrl('brand', 'puma')"
                    :class="['text-sm', activeBrand === 'puma' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    Puma
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="getFilterUrl('brand', 'mizuno')"
                    :class="['text-sm', activeBrand === 'mizuno' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    Mizuno
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="getFilterUrl('brand', 'yonex')"
                    :class="['text-sm', activeBrand === 'yonex' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    Yonex
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="getFilterUrl('brand', 'asics')"
                    :class="['text-sm', activeBrand === 'asics' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    ASICS
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="getFilterUrl('brand', 'fila')"
                    :class="['text-sm', activeBrand === 'fila' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    Fila
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="getFilterUrl('brand', 'decathlon')"
                    :class="['text-sm', activeBrand === 'decathlon' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    Decathlon
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="getFilterUrl('brand', 'other')"
                    :class="['text-sm', activeBrand === 'other' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    {{ t('common.other') }}
                  </router-link>
                </li>
              </ul>
            </div>

            <!-- Price Filter Section -->
            <div class="mb-6 mt-6">
              <h3 class="font-geist text-sm font-semibold uppercase tracking-[1.2px] text-[#1A1C1C] mb-4">
                {{ t('products.priceRange') }}
              </h3>
              <ul class="flex flex-col gap-3">
                <li>
                  <router-link
                    :to="getFilterUrl('price', null)"
                    :class="['text-sm', !activeMinPrice && !activeMaxPrice ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    {{ t('common.all') }}
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="getFilterUrl('price', '<1m')"
                    :class="['text-sm', !activeMinPrice && activeMaxPrice === '1000000' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    {{ t('products.under', { price: formatCurrency(1000000) }) }}
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="getFilterUrl('price', '1m-2m')"
                    :class="['text-sm', activeMinPrice === '1000000' && activeMaxPrice === '2000000' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    {{ t('products.priceRangeLabel', { from: formatCurrency(1000000), to: formatCurrency(2000000) }) }}
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="getFilterUrl('price', '2m-5m')"
                    :class="['text-sm', activeMinPrice === '2000000' && activeMaxPrice === '5000000' ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    {{ t('products.priceRangeLabel', { from: formatCurrency(2000000), to: formatCurrency(5000000) }) }}
                  </router-link>
                </li>
                <li>
                  <router-link
                    :to="getFilterUrl('price', '>5m')"
                    :class="['text-sm', activeMinPrice === '5000000' && !activeMaxPrice ? 'text-black font-semibold' : 'text-[#5E5F5C] hover:text-black']"
                  >
                    {{ t('products.above', { price: formatCurrency(5000000) }) }}
                  </router-link>
                </li>
              </ul>
            </div>

            <!-- Divider -->
            <div class="border-t border-[rgba(207,196,197,0.3)] mb-6" />

            <!-- Size Filter Section -->
            <div>
              <h3 class="font-geist text-sm font-semibold uppercase tracking-[1.2px] text-[#1A1C1C] mb-4">
                {{ t('products.size') }}
              </h3>

              <!-- Shoes -->
              <div class="mb-4">
                <span class="text-xs text-[#5E5F5C] uppercase font-semibold mb-2 block">{{ t('products.shoes') }}</span>
                <div class="grid grid-cols-3 gap-2">
                  <router-link
                    v-for="size in ['36', '37', '38', '39', '40', '41', '42', '43', '44', '45']"
                    :key="size"
                    :to="getFilterUrl('shoeSize', size)"
                    :class="[
                      'h-10 border rounded flex items-center justify-center text-sm transition-colors',
                      activeShoeSize === size ? 'border-black bg-black text-white font-semibold' : 'border-[#CFC4C6] text-[#1A1C1C] hover:border-black'
                    ]"
                  >
                    {{ size }}
                  </router-link>
                </div>
              </div>

              <!-- Clothing -->
              <div class="mb-4">
                <span class="text-xs text-[#5E5F5C] uppercase font-semibold mb-2 block">{{ t('products.clothing') }}</span>
                <div class="grid grid-cols-3 gap-2">
                  <router-link
                    v-for="size in ['S', 'M', 'L', 'XL', 'XXL', 'XXXL']"
                    :key="size"
                    :to="getFilterUrl('shoeSize', size)"
                    :class="[
                      'h-10 border rounded flex items-center justify-center text-sm transition-colors',
                      activeShoeSize === size ? 'border-black bg-black text-white font-semibold' : 'border-[#CFC4C6] text-[#1A1C1C] hover:border-black'
                    ]"
                  >
                    {{ size }}
                  </router-link>
                </div>
              </div>

              <!-- Accessories -->
              <div>
                <span class="text-xs text-[#5E5F5C] uppercase font-semibold mb-2 block">{{ t('products.accessories') }}</span>
                <div class="grid grid-cols-3 gap-2">
                  <router-link
                    v-for="size in ['FreeSize']"
                    :key="size"
                    :to="getFilterUrl('shoeSize', size)"
                    :class="[
                      'h-10 border rounded flex items-center justify-center text-sm transition-colors col-span-3',
                      activeShoeSize === size ? 'border-black bg-black text-white font-semibold' : 'border-[#CFC4C6] text-[#1A1C1C] hover:border-black'
                    ]"
                  >
                    {{ size }}
                  </router-link>
                </div>
              </div>
            </div>
          </div>
        </aside>

        <!-- Product Grid -->
        <section class="flex-1">
          <!-- Active Filters Display -->
          <div
            v-if="activeSport || activeBrand || activeKeyword || activeMinPrice || activeMaxPrice || activeShoeSize"
            class="mb-6 flex items-center gap-2"
          >
            <span class="text-sm text-[#5E5F5C]">{{ t('products.filteringBy') }}</span>
            <div
              v-if="activeKeyword"
              class="flex items-center gap-2"
            >
              <span class="px-3 py-1 bg-[#E5E7EB] text-sm text-[#1A1C1C] rounded flex items-center">
                {{ t('products.keyword', { keyword: activeKeyword }) }}
                <button
                  class="ml-2 hover:text-red-500"
                  @click="clearKeyword"
                >&times;</button>
              </span>
            </div>
            <div
              v-if="activeSport"
              class="flex items-center gap-2"
            >
              <span class="px-3 py-1 bg-[#E5E7EB] text-sm text-[#1A1C1C] rounded flex items-center">
                {{ t('products.sport', { sport: getSportLabel(activeSport) }) }}
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
                {{ t('products.brand', { brand: getBrandLabel(activeBrand) }) }}
                <button
                  class="ml-2 hover:text-red-500"
                  @click="clearBrand"
                >&times;</button>
              </span>
            </div>
            <div
              v-if="activePriceLabel"
              class="flex items-center gap-2"
            >
              <span class="px-3 py-1 bg-[#E5E7EB] text-sm text-[#1A1C1C] rounded flex items-center">
                {{ t('products.price', { price: activePriceLabel }) }}
                <button
                  class="ml-2 hover:text-red-500"
                  @click="clearPrice"
                >&times;</button>
              </span>
            </div>
            <div
              v-if="activeShoeSize"
              class="flex items-center gap-2"
            >
              <span class="px-3 py-1 bg-[#E5E7EB] text-sm text-[#1A1C1C] rounded flex items-center">
                {{ t('products.sizeLabel', { size: activeShoeSize }) }}
                <button
                  class="ml-2 hover:text-red-500"
                  @click="clearShoeSize"
                >&times;</button>
              </span>
            </div>

            <button
              v-if="activeSport || activeBrand || activeKeyword || activeMinPrice || activeMaxPrice || activeShoeSize"
              class="text-sm text-[#5E5F5C] hover:text-black underline"
              @click="clearAllFilters"
            >
              {{ t('products.clearAll') }}
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
                {{ t('products.noProductsFound') }}
              </p>
              <button
                class="px-6 py-3 bg-black text-white text-sm font-semibold uppercase tracking-[1.2px] rounded-sm hover:bg-gray-900 transition-colors"
                @click="clearAllFilters"
              >
                {{ t('products.clearFilters') }}
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
              {{ t('common.previous') }}
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
              {{ t('common.next') }}
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
import { useI18n } from 'vue-i18n'
import { formatCurrency } from '@/utils/formatters'
import { sportTranslationPath } from '@/i18n/labels'

const { t } = useI18n()

const route = useRoute()
const router = useRouter()

// State
const products = ref<Product[]>([])
const isLoading = ref(true)
const currentPage = ref(0)
const totalPages = ref(1)

// Active filters from query params
const activeSport = computed(() => {
  const sport = route.query.sport as string
  return (!sport || sport.toLowerCase() === 'all') ? '' : sport
})
const activeBrand = computed(() => {
  const brand = route.query.brand as string
  return (!brand || brand.toLowerCase() === 'all') ? '' : brand
})
const activeKeyword = computed(() => (route.query.keyword as string) || '')
const activeMinPrice = computed(() => route.query.minPrice as string || '')
const activeMaxPrice = computed(() => route.query.maxPrice as string || '')
const activeShoeSize = computed(() => route.query.shoeSize as string || '')

const getPriceLabel = (min: string, max: string) => {
  if (!min && max === '1000000') return t('products.under', { price: formatCurrency(1000000) })
  if (min === '1000000' && max === '2000000') return t('products.priceRangeLabel', { from: formatCurrency(1000000), to: formatCurrency(2000000) })
  if (min === '2000000' && max === '5000000') return t('products.priceRangeLabel', { from: formatCurrency(2000000), to: formatCurrency(5000000) })
  if (min === '5000000' && !max) return t('products.above', { price: formatCurrency(5000000) })
  return ''
}
const activePriceLabel = computed(() => getPriceLabel(activeMinPrice.value, activeMaxPrice.value))

const getFilterUrl = (key: string, value: string | null) => {
  const query = { ...route.query }
  
  // Toggle logic for standard keys (sport, brand, shoeSize)
  if (key !== 'price' && value !== null && query[key] === value) {
    value = null
  }

  if (value === null) {
    delete query[key]
  } else {
    query[key] = value
  }

  if (key === 'price') {
    const isCurrentPrice = 
      (value === '<1m' && !query.minPrice && query.maxPrice === '1000000') ||
      (value === '1m-2m' && query.minPrice === '1000000' && query.maxPrice === '2000000') ||
      (value === '2m-5m' && query.minPrice === '2000000' && query.maxPrice === '5000000') ||
      (value === '>5m' && query.minPrice === '5000000' && !query.maxPrice)

    delete query.minPrice
    delete query.maxPrice
    delete query.price

    if (!isCurrentPrice && value !== null) {
      if (value === '<1m') { query.maxPrice = '1000000' }
      if (value === '1m-2m') { query.minPrice = '1000000'; query.maxPrice = '2000000' }
      if (value === '2m-5m') { query.minPrice = '2000000'; query.maxPrice = '5000000' }
      if (value === '>5m') { query.minPrice = '5000000' }
    }
  }
  return { path: '/products', query }
}

// ponytail: single source of truth - 1 map cho cả API (title case) và UI label (uppercase).
const SPORT_NAME: Record<string, string> = {
  football: 'Bóng đá',
  badminton: 'Cầu lông',
  running: 'Chạy bộ',
  basketball: 'Bóng rổ',
  pickleball: 'Cầu lông & Pickleball',
  racing: 'Đua xe',
  gym: 'Gym',
  other: 'Khác'
}

const BRAND_NAME: Record<string, string> = {
  nike: 'Nike',
  adidas: 'Adidas',
  lining: 'Li-Ning',
  puma: 'Puma',
  yonex: 'Yonex',
  asics: 'ASICS',
  mizuno: 'Mizuno',
  fila: 'Fila',
  decathlon: 'Decathlon',
  other: 'Khác'
}

const getApiSportName = (sport: string): string => SPORT_NAME[sport] || sport
const getApiBrandName = (brand: string): string => BRAND_NAME[brand] || brand
const getSportLabel = (sport: string): string => t(sportTranslationPath(sport)).toUpperCase()
const getBrandLabel = (brand: string): string => {
  if (brand === 'other') return t('common.other').toUpperCase()
  return (BRAND_NAME[brand] || brand).toUpperCase()
}

const fetchProducts = async () => {
  isLoading.value = true
  try {
    const response = await productService.getProducts({
      page: currentPage.value,
      size: 9,
      sport: getApiSportName(activeSport.value),
      brand: getApiBrandName(activeBrand.value),
      keyword: activeKeyword.value || undefined,
      minPrice: activeMinPrice.value ? Number(activeMinPrice.value) : undefined,
      maxPrice: activeMaxPrice.value ? Number(activeMaxPrice.value) : undefined,
      shoeSize: activeShoeSize.value || undefined
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
  () => [route.query.sport, route.query.brand, route.query.keyword, route.query.minPrice, route.query.maxPrice, route.query.shoeSize],
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
  const query = { ...route.query }
  delete query.brand
  router.push({ query })
}

const clearKeyword = () => {
  const query = { ...route.query }
  delete query.keyword
  router.push({ query })
}

const clearAllFilters = () => {
  router.push('/products')
}
const clearPrice = () => {
    const query = { ...route.query }
    delete query.minPrice
    delete query.maxPrice
    router.push({ path: '/products', query })
  }
  const clearShoeSize = () => {
    const query = { ...route.query }
    delete query.shoeSize
    router.push({ path: '/products', query })
  }
</script>

<style scoped>
</style>
