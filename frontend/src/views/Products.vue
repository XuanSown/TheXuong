<template>
  <div class="min-h-screen bg-[#F9F9F9]">
    <main class="w-full max-w-[1280px] mx-auto px-4 pt-[120px] pb-8">
      <!-- Page Header -->
      <section class="bg-white border-y border-[#CFC4C6] py-24 mb-8">
        <div class="max-w-[1152px] mx-auto">
          <h1 class="font-geist text-[64px] leading-[77px] text-[#1A1C1C] tracking-[-1.28px] uppercase">
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
            <div class="border-t border-[rgba(207,196,197,0.3)] mb-6"></div>

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
          <div v-if="activeSport || activeBrand" class="mb-6 flex items-center gap-2">
            <span class="text-sm text-[#5E5F5C]">Đang lọc:</span>
            <div v-if="activeSport" class="flex items-center gap-2">
              <span class="px-3 py-1 bg-[#E5E7EB] text-sm text-[#1A1C1C] rounded">
                Thể thao: {{ getSportLabel(activeSport) }}
                <button @click="clearSport" class="ml-2 hover:text-red-500">&times;</button>
              </span>
            </div>
            <div v-if="activeBrand" class="flex items-center gap-2">
              <span class="px-3 py-1 bg-[#E5E7EB] text-sm text-[#1A1C1C] rounded">
                Thương hiệu: {{ getBrandLabel(activeBrand) }}
                <button @click="clearBrand" class="ml-2 hover:text-red-500">&times;</button>
              </span>
            </div>
            <button
              v-if="activeSport || activeBrand"
              @click="clearAllFilters"
              class="text-sm text-[#5E5F5C] hover:text-black underline"
            >
              Xóa tất cả
            </button>
          </div>

          <div class="relative">
            <!-- Row 1 -->
            <div class="flex justify-between mb-8" v-if="shouldShowProduct(1)">
              <div class="w-[270px] flex flex-col gap-6">
                <div class="w-[270px] h-[270px] bg-[#EEEEEE] border border-[#CFC4C6]">
                  <img v-if="product1Image" :src="product1Image" :alt="product1Name" class="w-full h-full object-cover" />
                </div>
                <div class="flex flex-col gap-2">
                  <p class="text-[10px] uppercase tracking-[1px] text-[#5E5F5C]">{{ product1Sport }}</p>
                  <h3 class="font-gelasio text-lg font-bold text-[#1A1C1C] leading-[29px]">
                    {{ product1Name }}
                  </h3>
                  <p class="font-geist text-base font-semibold text-black leading-[24px]">
                    {{ product1Price }}
                  </p>
                </div>
              </div>

              <div class="w-[270px] flex flex-col gap-6" v-if="shouldShowProduct(2)">
                <div class="w-[270px] h-[270px] bg-[#EEEEEE] border border-[#CFC4C6]">
                  <img v-if="product2Image" :src="product2Image" :alt="product2Name" class="w-full h-full object-cover" />
                </div>
                <div class="flex flex-col gap-2">
                  <p class="text-[10px] uppercase tracking-[1px] text-[#5E5F5C]">{{ product2Sport }}</p>
                  <h3 class="font-gelasio text-lg font-bold text-[#1A1C1C] leading-[29px]">
                    {{ product2Name }}
                  </h3>
                  <p class="font-geist text-base font-semibold text-black leading-[24px]">
                    {{ product2Price }}
                  </p>
                </div>
              </div>

              <div class="w-[270px] flex flex-col gap-6" v-if="shouldShowProduct(3)">
                <div class="w-[270px] h-[270px] bg-[#EEEEEE] border border-[#CFC4C6]">
                  <img v-if="product3Image" :src="product3Image" :alt="product3Name" class="w-full h-full object-cover" />
                </div>
                <div class="flex flex-col gap-2">
                  <p class="text-[10px] uppercase tracking-[1px] text-[#5E5F5C]">{{ product3Sport }}</p>
                  <h3 class="font-gelasio text-lg font-bold text-[#1A1C1C] leading-[29px]">
                    {{ product3Name }}
                  </h3>
                  <p class="font-geist text-base font-semibold text-black leading-[24px]">
                    {{ product3Price }}
                  </p>
                </div>
              </div>

              <div class="w-[270px] flex flex-col gap-6" v-if="shouldShowProduct(4)">
                <div class="w-[270px] h-[270px] bg-[#EEEEEE] border border-[#CFC4C6]">
                  <img v-if="product4Image" :src="product4Image" :alt="product4Name" class="w-full h-full object-cover" />
                </div>
                <div class="flex flex-col gap-2">
                  <p class="text-[10px] uppercase tracking-[1px] text-[#5E5F5C]">{{ product4Sport }}</p>
                  <h3 class="font-gelasio text-lg font-bold text-[#1A1C1C] leading-[29px]">
                    {{ product4Name }}
                  </h3>
                  <p class="font-geist text-base font-semibold text-black leading-[24px]">
                    {{ product4Price }}
                  </p>
                </div>
              </div>
            </div>

            <!-- Row 2 -->
            <div class="flex justify-between mb-8" v-if="hasSecondRow">
              <div class="w-[270px] flex flex-col gap-6" v-if="shouldShowProduct(5)">
                <div class="w-[270px] h-[270px] bg-[#EEEEEE] border border-[#CFC4C6]">
                  <img v-if="product5Image" :src="product5Image" :alt="product5Name" class="w-full h-full object-cover" />
                </div>
                <div class="flex flex-col gap-2">
                  <p class="text-[10px] uppercase tracking-[1px] text-[#5E5F5C]">{{ product5Sport }}</p>
                  <h3 class="font-gelasio text-lg font-bold text-[#1A1C1C] leading-[29px]">
                    {{ product5Name }}
                  </h3>
                  <p class="font-geist text-base font-semibold text-black leading-[24px]">
                    {{ product5Price }}
                  </p>
                </div>
              </div>

              <div class="w-[270px] flex flex-col gap-6" v-if="shouldShowProduct(6)">
                <div class="w-[270px] h-[270px] bg-[#EEEEEE] border border-[#CFC4C6]">
                  <img v-if="product6Image" :src="product6Image" :alt="product6Name" class="w-full h-full object-cover" />
                </div>
                <div class="flex flex-col gap-2">
                  <p class="text-[10px] uppercase tracking-[1px] text-[#5E5F5C]">{{ product6Sport }}</p>
                  <h3 class="font-gelasio text-lg font-bold text-[#1A1C1C] leading-[29px]">
                    {{ product6Name }}
                  </h3>
                  <p class="font-geist text-base font-semibold text-black leading-[24px]">
                    {{ product6Price }}
                  </p>
                </div>
              </div>

              <div class="w-[270px] flex flex-col gap-6" v-if="shouldShowProduct(7)">
                <div class="w-[270px] h-[270px] bg-[#EEEEEE] border border-[#CFC4C6]">
                  <img v-if="product7Image" :src="product7Image" :alt="product7Name" class="w-full h-full object-cover" />
                </div>
                <div class="flex flex-col gap-2">
                  <p class="text-[10px] uppercase tracking-[1px] text-[#5E5F5C]">{{ product7Sport }}</p>
                  <h3 class="font-gelasio text-lg font-bold text-[#1A1C1C] leading-[29px]">
                    {{ product7Name }}
                  </h3>
                  <p class="font-geist text-base font-semibold text-black leading-[24px]">
                    {{ product7Price }}
                  </p>
                </div>
              </div>

              <div class="w-[270px] flex flex-col gap-6" v-if="shouldShowProduct(8)">
                <div class="w-[270px] h-[270px] bg-[#EEEEEE] border border-[#CFC4C6]">
                  <img v-if="product8Image" :src="product8Image" :alt="product8Name" class="w-full h-full object-cover" />
                </div>
                <div class="flex flex-col gap-2">
                  <p class="text-[10px] uppercase tracking-[1px] text-[#5E5F5C]">{{ product8Sport }}</p>
                  <h3 class="font-gelasio text-lg font-bold text-[#1A1C1C] leading-[29px]">
                    {{ product8Name }}
                  </h3>
                  <p class="font-geist text-base font-semibold text-black leading-[24px]">
                    {{ product8Price }}
                  </p>
                </div>
              </div>
            </div>
          </div>

          <!-- No Results Message -->
          <div v-if="hasNoResults" class="text-center py-16">
            <p class="font-gelasio text-xl text-[#5E5F5C] mb-4">Không tìm thấy sản phẩm phù hợp</p>
            <button
              @click="clearAllFilters"
              class="px-6 py-3 bg-black text-white text-sm font-semibold uppercase tracking-[1.2px] rounded-sm hover:bg-gray-900 transition-colors"
            >
              Xóa bộ lọc
            </button>
          </div>

          <!-- Pagination -->
          <div v-if="!hasNoResults && hasProducts" class="flex justify-center items-center gap-2 mt-16">
            <button class="px-4 py-2 border border-[#CFC4C6] rounded-sm text-sm uppercase tracking-[1.8px] text-[#1A1C1C] hover:bg-black hover:text-white transition-colors">
              PREV
            </button>
            <button class="px-4 py-2 bg-black text-white text-sm uppercase tracking-[1.8px] rounded-sm">
              1
            </button>
            <button class="px-5 py-2 border border-[#CFC4C6] rounded-sm text-sm uppercase tracking-[1.8px] text-[#1A1C1C] hover:bg-black hover:text-white transition-colors">
              NEXT
            </button>
          </div>
        </section>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

// Active filters from query params
const activeSport = computed(() => (route.query.sport as string) || '')
const activeBrand = computed(() => (route.query.brand as string) || '')

// Clear filter functions
const clearSport = () => {
  window.location.href = '/products' + (activeBrand.value ? `?brand=${activeBrand.value}` : '')
}

const clearBrand = () => {
  window.location.href = '/products' + (activeSport.value ? `?sport=${activeSport.value}` : '')
}

const clearAllFilters = () => {
  window.location.href = '/products'
}

// Product data with sport and brand attributes
const products = [
  {
    id: 1,
    name: 'Giày Đá Bóng Nike Air Zoom',
    sport: 'football',
    brand: 'nike',
    price: '2.450.000 đ',
    image: 'https://via.placeholder.com/270x270?text=Nike+Football'
  },
  {
    id: 2,
    name: 'Vợt Cầu Lông Yonex Astrox 88',
    sport: 'badminton',
    brand: 'yonex',
    price: '3.950.000 đ',
    image: 'https://via.placeholder.com/270x270?text=Yonex+Badminton'
  },
  {
    id: 3,
    name: 'Giày Chạy Bộ Asics GT-2000',
    sport: 'running',
    brand: 'asics',
    price: '2.850.000 đ',
    image: 'https://via.placeholder.com/270x270?text=Asics+Running'
  },
  {
    id: 4,
    name: 'Vợt Cầu Lông Yonex Nanoflare 1000Z',
    sport: 'badminton',
    brand: 'yonex',
    price: '4.250.000 đ',
    image: 'https://via.placeholder.com/270x270?text=Yonex+Nanoflare'
  },
  {
    id: 5,
    name: 'Vợt Cầu Lông Yonex Nanoflare',
    sport: 'badminton',
    brand: 'yonex',
    price: '2.750.000 đ',
    image: 'https://via.placeholder.com/270x270?text=Yonex+Nanoflare'
  },
  {
    id: 6,
    name: 'Giày Chạy Bộ Asics GT-2000',
    sport: 'running',
    brand: 'asics',
    price: '2.850.000 đ',
    image: 'https://via.placeholder.com/270x270?text=Asics+GT2000'
  },
  {
    id: 7,
    name: 'Vợt Cầu Lông Yonex Nanoflare 1000Z',
    sport: 'badminton',
    brand: 'yonex',
    price: '4.250.000 đ',
    image: 'https://via.placeholder.com/270x270?text=Yonex+1000Z'
  },
  {
    id: 8,
    name: 'Giày Chạy Bộ Asics Novablast 4',
    sport: 'running',
    brand: 'asics',
    price: '3.150.000 đ',
    image: 'https://via.placeholder.com/270x270?text=Asics+Novablast'
  },
  // Other sport products
  {
    id: 9,
    name: 'Áo Training Adidas Premium',
    sport: 'other',
    brand: 'adidas',
    price: '850.000 đ',
    image: 'https://via.placeholder.com/270x270?text=Adidas+Apparel'
  },
  {
    id: 10,
    name: 'Quần Short Nike Dri-FIT',
    sport: 'other',
    brand: 'nike',
    price: '650.000 đ',
    image: 'https://via.placeholder.com/270x270?text=Nike+Shorts'
  },
  // Other brand products
  {
    id: 11,
    name: 'Giày Basketball Puma Clyde',
    sport: 'basketball',
    brand: 'other',
    price: '3.200.000 đ',
    image: 'https://via.placeholder.com/270x270?text=Puma+Basketball'
  },
  {
    id: 12,
    name: 'Bộ tập Gym Li-Ning',
    sport: 'other',
    brand: 'lining',
    price: '1.200.000 đ',
    image: 'https://via.placeholder.com/270x270?text=Li-Ning+Gym'
  }
]

// Filter logic
const filteredProducts = computed(() => {
  return products.filter(p => {
    const matchSport = !activeSport.value || p.sport === activeSport.value
    const matchBrand = !activeBrand.value || p.brand === activeBrand.value
    return matchSport && matchBrand
  })
})

const hasProducts = computed(() => filteredProducts.value.length > 0)
const hasNoResults = computed(() => !hasProducts.value && (activeSport.value || activeBrand.value))

// Product getters with dynamic content based on filtered results
const product1 = computed(() => filteredProducts.value[0])
const product2 = computed(() => filteredProducts.value[1])
const product3 = computed(() => filteredProducts.value[2])
const product4 = computed(() => filteredProducts.value[3])
const product5 = computed(() => filteredProducts.value[4])
const product6 = computed(() => filteredProducts.value[5])
const product7 = computed(() => filteredProducts.value[6])
const product8 = computed(() => filteredProducts.value[7])

const product1Name = computed(() => product1.value?.name || '')
const product1Image = computed(() => product1.value?.image || '')
const product1Sport = computed(() => getSportLabel(product1.value?.sport || ''))
const product1Price = computed(() => product1.value?.price || '')

const product2Name = computed(() => product2.value?.name || '')
const product2Image = computed(() => product2.value?.image || '')
const product2Sport = computed(() => getSportLabel(product2.value?.sport || ''))
const product2Price = computed(() => product2.value?.price || '')

const product3Name = computed(() => product3.value?.name || '')
const product3Image = computed(() => product3.value?.image || '')
const product3Sport = computed(() => getSportLabel(product3.value?.sport || ''))
const product3Price = computed(() => product3.value?.price || '')

const product4Name = computed(() => product4.value?.name || '')
const product4Image = computed(() => product4.value?.image || '')
const product4Sport = computed(() => getSportLabel(product4.value?.sport || ''))
const product4Price = computed(() => product4.value?.price || '')

const product5Name = computed(() => product5.value?.name || '')
const product5Image = computed(() => product5.value?.image || '')
const product5Sport = computed(() => getSportLabel(product5.value?.sport || ''))
const product5Price = computed(() => product5.value?.price || '')

const product6Name = computed(() => product6.value?.name || '')
const product6Image = computed(() => product6.value?.image || '')
const product6Sport = computed(() => getSportLabel(product6.value?.sport || ''))
const product6Price = computed(() => product6.value?.price || '')

const product7Name = computed(() => product7.value?.name || '')
const product7Image = computed(() => product7.value?.image || '')
const product7Sport = computed(() => getSportLabel(product7.value?.sport || ''))
const product7Price = computed(() => product7.value?.price || '')

const product8Name = computed(() => product8.value?.name || '')
const product8Image = computed(() => product8.value?.image || '')
const product8Sport = computed(() => getSportLabel(product8.value?.sport || ''))
const product8Price = computed(() => product8.value?.price || '')

const hasSecondRow = computed(() => filteredProducts.value.length > 4)

const shouldShowProduct = (index: number) => {
  return filteredProducts.value.length >= index
}

const getSportLabel = (sport: string): string => {
  const labels: Record<string, string> = {
    football: 'BÓNG ĐÁ',
    badminton: 'CẦU LÔNG',
    running: 'CHẠY BỘ',
    basketball: 'BÓNG RỔ',
    other: 'KHÁC'
  }
  return labels[sport] || sport.toUpperCase()
}

const getBrandLabel = (brand: string): string => {
  const labels: Record<string, string> = {
    nike: 'Nike',
    adidas: 'Adidas',
    lining: 'Li-Ning',
    puma: 'Puma',
    yonex: 'Yonex',
    asics: 'Asics',
    other: 'Khác'
  }
  return labels[brand] || brand
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Gelasio:wght@400;500;600;700&family=Inter:wght@400;500;600;700&display=swap');

.font-geist {
  font-family: 'Inter', 'Geist', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.font-gelasio {
  font-family: 'Gelasio', serif;
}
</style>
