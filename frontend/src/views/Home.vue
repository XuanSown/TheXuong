<template>
  <div class="min-h-screen bg-[#f9f9f9]">
    <!-- Hero Section -->
    <section class="relative w-full h-[500px] md:h-[600px] lg:h-[700px] overflow-hidden">
      <!-- Background Image with Gradient Overlay -->
      <div class="absolute inset-0">
        <img
          src="https://images.unsplash.com/photo-1571902943202-507ec2618e8f?w=1920&q=80"
          alt="The Xuong Sport"
          class="w-full h-full object-cover"
        />
        <div class="absolute inset-0 bg-gradient-to-r from-black/60 via-black/40 to-transparent"></div>
      </div>

      <!-- Content -->
      <div class="relative z-10 h-full flex items-center">
        <div class="container-custom">
          <div class="max-w-3xl">
            <h1 class="font-geist text-4xl md:text-5xl lg:text-6xl xl:text-7xl font-bold text-white mb-4 md:mb-6 leading-tight">
              THE XUONG<br>SPORT
            </h1>
            <p class="font-gelasio text-lg md:text-xl lg:text-2xl text-gray-200 mb-6 md:mb-8">
              Nâng tầm đam mê thể thao với những sản phẩm chất lượng cao cấp từ các thương hiệu hàng đầu thế giới
            </p>
            <div class="flex flex-wrap gap-4">
              <router-link
                to="/products"
                class="px-8 py-3 bg-primary-500 text-white font-semibold rounded-md hover:bg-primary-600 transition-all duration-200"
              >
                Khám phá ngay
              </router-link>
              <router-link
                to="/products"
                class="px-8 py-3 bg-white/10 backdrop-blur-sm text-white font-semibold rounded-md border border-white hover:bg-white hover:text-gray-900 transition-all duration-200"
              >
                Xem bộ sưu tập
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- Trust Brands Marquee -->
    <section class="py-8 bg-white border-y border-gray-200 overflow-hidden">
      <div class="marquee-container">
        <div class="marquee-content">
          <span class="brand-item font-bold">NIKE</span>
          <span class="brand-item font-bold">ADIDAS</span>
          <span class="brand-item font-bold">LI-NING</span>
          <span class="brand-item font-bold">PUMA</span>
          <span class="brand-item font-bold">NEW BALANCE</span>
          <span class="brand-item font-bold">UNDER ARMOUR</span>
          <span class="brand-item font-bold">ASICS</span>
          <span class="brand-item font-bold">MIZUNO</span>
          <!-- Duplicate for seamless loop -->
          <span class="brand-item font-bold">NIKE</span>
          <span class="brand-item font-bold">ADIDAS</span>
          <span class="brand-item font-bold">LI-NING</span>
          <span class="brand-item font-bold">PUMA</span>
          <span class="brand-item font-bold">NEW BALANCE</span>
          <span class="brand-item font-bold">UNDER ARMOUR</span>
          <span class="brand-item font-bold">ASICS</span>
          <span class="brand-item font-bold">MIZUNO</span>
        </div>
      </div>
    </section>

    <!-- Bento Grid Features -->
    <section class="py-16 md:py-24 bg-[#f9f9f9]">
      <div class="container-custom">
        <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
          <!-- Left Column - Large Feature -->
          <div class="bg-gradient-to-br from-primary-500 to-primary-700 rounded-3xl p-8 md:p-12 flex flex-col justify-between min-h-[400px]">
            <div>
              <h2 class="font-geist text-3xl md:text-4xl lg:text-5xl font-bold text-white mb-4">
                CÔNG NGHỆ<br>VƯỢT TRỘI
              </h2>
              <p class="font-gelasio text-lg text-white/90 leading-relaxed">
                Trải nghiệm công nghệ tiên tiến nhất trong từng sản phẩm. Chất liệu thông minh, thiết kế tối ưu, mang đến hiệu suất vượt trội cho vận động viên chuyên nghiệp và người đam mê thể thao.
              </p>
            </div>
            <div class="mt-8">
              <div class="flex items-center gap-2 text-white">
                <span class="text-6xl font-bold">30%</span>
                <span class="text-lg">công nghệ<br/>mới nhất</span>
              </div>
            </div>
          </div>

          <!-- Right Column - Top -->
          <div class="bg-white rounded-3xl p-8 md:p-12 border border-gray-200 min-h-[190px] lg:min-h-[194px]">
            <div class="flex flex-col h-full justify-between">
              <h3 class="font-geist text-2xl md:text-3xl font-bold text-[#1a1c1c]">
                CAM KẾT<br>CHÍNH HÃNG
              </h3>
              <p class="font-gelasio text-gray-600 text-sm md:text-base">
                100% sản phẩm chính hãng, có đầy đủ giấy tờ và chế độ bảo hành. Mua hàng tự tin, sử dụng an tâm.
              </p>
            </div>
          </div>

          <!-- Right Column - Bottom Left -->
          <div class="bg-white rounded-3xl p-8 md:p-12 border border-gray-200">
            <div class="flex flex-col h-full justify-between">
              <h3 class="font-geist text-2xl md:text-3xl font-bold text-[#1a1c1c]">
                UY TÍN
              </h3>
              <div class="flex items-center gap-2 mt-4">
                <div class="flex">
                  <svg class="w-6 h-6 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                  <svg class="w-6 h-6 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                  <svg class="w-6 h-6 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                  <svg class="w-6 h-6 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                  <svg class="w-6 h-6 text-yellow-400" fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z"/>
                  </svg>
                </div>
                <span class="text-gray-600">10.000+ khách hàng tin tưởng</span>
              </div>
            </div>
          </div>

          <!-- Right Column - Bottom Right -->
          <div class="bg-[#f2f2f2] rounded-3xl p-8 md:p-12 relative overflow-hidden min-h-[190px] lg:min-h-[194px]">
            <div class="relative z-10 flex flex-col h-full justify-between">
              <h3 class="font-geist text-2xl md:text-3xl font-bold text-[#1a1c1c]">
                CHĂM SÓC
              </h3>
              <p class="font-gelasio text-gray-600 text-sm md:text-base">
                Dịch vụ hậu mãi tận tâm, hỗ trợ 24/7 và chính sách đổi trả linh hoạt
              </p>
            </div>
            <!-- Background Image -->
            <div class="absolute right-0 bottom-0 w-32 h-32 md:w-48 md:h-48 bg-primary-100 rounded-full -mr-16 -mb-16"></div>
            <div class="absolute right-8 bottom-8 w-24 h-24 md:w-32 md:h-32 bg-primary-200 rounded-full -mr-12 -mb-12 opacity-50"></div>
          </div>
        </div>
      </div>
    </section>

    <!-- New Products Section -->
    <section class="py-12 md:py-16 lg:py-20 bg-white">
      <div class="container-custom">
        <div class="flex flex-col md:flex-row md:items-end justify-between mb-8 md:mb-12">
          <div>
            <h2 class="font-geist text-3xl md:text-4xl lg:text-5xl font-bold text-[#1a1c1c] mb-2">
              SẢN PHẨM MỚI
            </h2>
            <p class="font-gelasio text-gray-500">
              Khám phá những mẫu sản phẩm mới nhất của chúng tôi
            </p>
          </div>
          <router-link
            to="/products"
            class="mt-4 md:mt-0 inline-flex items-center text-primary-500 hover:text-primary-600 font-semibold"
          >
            Xem tất cả
            <svg class="w-5 h-5 ml-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 8l4 4m0 0l-4 4m4-4H3"/>
            </svg>
          </router-link>
        </div>

        <!-- Loading State -->
        <div v-if="loading" class="flex justify-center items-center py-20">
          <div class="animate-spin rounded-full h-12 w-12 border-4 border-primary-500 border-t-transparent"></div>
        </div>

        <!-- Products Grid -->
        <div v-else-if="products.length" class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4 md:gap-6 lg:gap-8">
          <div
            v-for="product in products"
            :key="product.id"
            class="group bg-white rounded-xl border border-gray-100 overflow-hidden hover:border-primary-200 hover:shadow-lg transition-all duration-300"
          >
            <router-link :to="`/products/${product.id}`" class="block">
              <!-- Image Container -->
              <div class="aspect-square relative overflow-hidden bg-gray-50">
                <img
                  :src="product.imageUrl || '/placeholder-product.png'"
                  :alt="product.name"
                  class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-500"
                  loading="lazy"
                />
                <!-- NEW Badge -->
                <div class="absolute top-3 left-3 bg-primary-500 text-white text-xs font-semibold px-3 py-1 rounded-full">
                  MỚI
                </div>
              </div>
              <!-- Product Info -->
              <div class="p-4">
                <h3 class="font-gelasio text-base font-semibold text-[#1a1c1c] line-clamp-2 min-h-[48px] mb-2 group-hover:text-primary-500 transition-colors">
                  {{ product.name }}
                </h3>
                <div class="flex items-center justify-between mt-2">
                  <span class="font-geist text-lg font-bold text-primary-500">
                    {{ formatPrice(product.price) }}
                  </span>
                  <span class="text-sm text-gray-400 line-through" v-if="product.originalPrice">
                    {{ formatPrice(product.originalPrice) }}
                  </span>
                </div>
              </div>
            </router-link>
          </div>
        </div>

        <!-- Empty State -->
        <div v-else class="text-center py-20">
          <div class="w-24 h-24 mx-auto mb-4 bg-gray-100 rounded-full flex items-center justify-center">
            <svg class="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.217a.75.75 0 00-.712 1.28l.288.474c.1.162.018.34-.104.472l-1.906 1.096A.75.75 0 0118 19.284V21a.75.75 0 01-.75.75H6a.75.75 0 01-.75-.75V21a.75.75 0 01.75-.75h4.5a.75.75 0 00.712-1.28l-.288-.474c-.1-.162-.018-.34.104-.472l1.906-1.096a.75.75 0 011.28-.712l2.25.75V21a.75.75 0 01.75.75h2.5a.75.75 0 01.75-.75V21a.75.75 0 01.75-.75h-2.5z"/>
            </svg>
          </div>
          <p class="text-gray-500 text-lg">Chưa có sản phẩm mới</p>
          <p class="text-gray-400 text-sm mt-2">Vui lòng quay lại sau</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useCartStore } from '@/stores/cart.store'
import type { Product } from '@/types'
import api from '@/services/api'

const cartStore = useCartStore()
const loading = ref(false)
const products = ref<Product[]>([])

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND'
  }).format(price)
}

onMounted(async () => {
  try {
    loading.value = true
    const data = await api.getNewProducts(8)
    products.value = data.data || data || data as any
  } catch (error) {
    console.error('Failed to fetch new products:', error)
    products.value = []
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
/* Marquee animation */
.marquee-container {
  width: 100%;
  overflow: hidden;
}

.marquee-content {
  display: flex;
  gap: 4rem;
  animation: marquee 25s linear infinite;
  white-space: nowrap;
}

.brand-item {
  font-size: 1.5rem;
  font-weight: 700;
  color: #5e5f5c;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

@keyframes marquee {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-50%);
  }
}

/* Smooth marquee */
.marquee-container:hover .marquee-content {
  animation-play-state: paused;
}

/* Responsive adjustments */
@media (max-width: 640px) {
  .brand-item {
    font-size: 1rem;
    gap: 2rem;
  }

  .marquee-content {
    gap: 2rem;
  }
}

@media (min-width: 1024px) {
  .brand-item {
    font-size: 1.75rem;
  }
}
</style>
