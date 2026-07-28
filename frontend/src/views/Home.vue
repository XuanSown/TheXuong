<template>
  <div class="min-h-screen bg-[#F9F9F9]">
    <!-- Main Content -->
    <main class="w-full max-w-[1280px] mx-auto px-4 py-[30px]">
      <!-- Hero Section -->
      <section class="w-full flex flex-col items-center justify-center gap-[31px] mb-8">
        <!-- Heading -->
        <div class="text-center">
          <h1 class="font-geist text-[64px] leading-[70px] font-normal text-black tracking-[-1.28px] mb-2">
            THE XUONG SPORT
          </h1>
          <p class="font-gelasio text-lg text-[#5E5F5C] leading-[29px] max-w-[672px] mx-auto">
            Nâng tầm trải nghiệm sản phẩm thể thao
          </p>
        </div>

        <!-- CTA Buttons -->
        <div class="flex items-center gap-4">
          <router-link
            to="/products"
            class="px-10 py-5 bg-black text-white text-[12px] font-semibold uppercase tracking-[1.8px] leading-[12px] rounded-sm hover:bg-gray-900 transition-colors"
          >
            KHÁM PHÁ NGAY
          </router-link>
          <router-link
            to="/about"
            class="px-10 py-5 border border-black text-black text-[12px] font-semibold uppercase tracking-[1.8px] leading-[12px] rounded-sm hover:bg-black hover:text-white transition-colors"
          >
            VỀ CHÚNG TÔI
          </router-link>
        </div>

        <!-- Hero Image -->
        <HeroImage />
      </section>

      <!-- Trust Brands Marquee -->
      <section
        class="w-[1250px] mx-auto h-[50px] bg-white border border-[#CFC4C6] mb-[30px] flex items-center overflow-hidden"
      >
        <div class="relative flex overflow-x-hidden w-full">
          <div class="py-12 animate-marquee whitespace-nowrap flex items-center justify-around min-w-full gap-[120px]">
            <span class="font-geist text-lg text-[#1A1C1C] opacity-80">NIKE</span>
            <span class="font-geist text-lg font-bold text-[#1A1C1C] opacity-80">ADIDAS</span>
            <span class="font-geist text-lg font-bold text-[#1A1C1C] opacity-80">LI-NING</span>
            <span class="font-geist text-lg font-bold text-[#1A1C1C] opacity-80">PUMA</span>
          </div>
          <div
            class="absolute top-0 py-12 animate-marquee2 whitespace-nowrap flex items-center justify-around min-w-full gap-[120px]"
          >
            <span class="font-geist text-lg text-[#1A1C1C] opacity-80">NIKE</span>
            <span class="font-geist text-lg font-bold text-[#1A1C1C] opacity-80">ADIDAS</span>
            <span class="font-geist text-lg font-bold text-[#1A1C1C] opacity-80">LI-NING</span>
            <span class="font-geist text-lg font-bold text-[#1A1C1C] opacity-80">PUMA</span>
          </div>
        </div>
      </section>

      <!-- Bento Grid Features -->
      <section class="w-[1250px] h-[824px] relative mb-[30px]">
        <!-- Left Top: Technology -->
        <div class="absolute left-0 right-[392px] top-0 h-[400px] bg-[#F3F3F4] border border-[#CFC4C6] p-[48px]">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="32"
            height="32"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
            stroke-linecap="round"
            stroke-linejoin="round"
            class="mb-[49px] mt-4 text-[#1A1C1C]"
          >
            <path
              d="m12 3-1.912 5.813a2 2 0 0 1-1.275 1.275L3 12l5.813 1.912a2 2 0 0 1 1.275 1.275L12 21l1.912-5.813a2 2 0 0 1 1.275-1.275L21 12l-5.813-1.912a2 2 0 0 1-1.275-1.275L12 3Z"
            />
            <path d="M5 3v4" />
            <path d="M3 5h4" />
            <path d="M19 3v4" />
            <path d="M17 5h4" />
          </svg>
          <h3
            class="absolute left-[49px] right-[49px] top-[219.83px] font-gelasio text-[32px] leading-[38px] text-[#1A1C1C] tracking-[-0.32px]"
          >
            CÔNG NGHỆ VƯỢT TRỘI
          </h3>
          <p
            class="absolute left-[49px] right-[263px] bottom-[49px] max-w-[448px] font-gelasio text-base text-[#5E5F5C] leading-[26px]"
          >
            Tất cả sản phẩm tại TheXuong đều được tuyển chọn kỹ lưỡng, đảm bảo tích hợp những công nghệ hỗ trợ vận động
            tiên tiến nhất hiện nay.
          </p>
        </div>

        <!-- Right: Authentic Guarantee -->
        <div class="absolute left-[784px] right-0 top-0 h-[400px] bg-black p-[48px]">
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="28"
            height="28"
            viewBox="0 0 24 24"
            fill="white"
            stroke="white"
            stroke-width="1.5"
            stroke-linecap="round"
            stroke-linejoin="round"
            class="mb-6"
          >
            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" />
            <path
              d="m9 12 2 2 4-4"
              stroke="black"
              fill="none"
              stroke-width="2"
            />
          </svg>
          <div class="flex flex-col gap-4">
            <h3 class="font-gelasio text-[28px] leading-[34px] text-white tracking-[-0.32px] whitespace-nowrap">
              CAM KẾT CHÍNH HÃNG
            </h3>
            <div class="relative w-[261.59px] min-h-[107.39px] h-auto bg-[#0E0E0E] p-6">
              <p class="font-gelasio text-base text-[rgba(255,255,255,0.6)] leading-[26px]">
                Chúng tôi cam kết 100% sản phẩm là hàng chính hãng, phát hiện hàng giả đền bù gấp 10 lần.
              </p>
            </div>
          </div>
        </div>

        <!-- Left Bottom: Reputation -->
        <div class="absolute left-0 right-[784px] top-[424px] h-[400px] bg-[#F3F3F4] border border-[#CFC4C6] p-[48px]">
          <svg
            viewBox="0 0 24 24"
            width="28"
            height="28"
            stroke="currentColor"
            stroke-width="1.5"
            fill="none"
            stroke-linecap="round"
            stroke-linejoin="round"
            class="mb-4 text-[#1A1C1C]"
          >
            <circle
              cx="12"
              cy="5"
              r="2"
            />
            <path d="m10 6.5-6 14.5" />
            <path d="m14 6.5 6 14.5" />
            <path d="M8 15h8" />
          </svg>
          <h3 class="font-gelasio text-[32px] leading-[38px] text-[#1A1C1C] tracking-[-0.32px] mb-4">
            UY TÍN
          </h3>
          <p class="font-gelasio text-base text-black leading-[26px] max-w-[270px]">
            THEXUONG tự tin với chất lượng sản phẩm.
          </p>
        </div>

        <!-- Right Bottom: Care -->
        <div
          class="absolute left-[392px] right-0 top-[424px] h-[400px] border border-[#CFC4C6] p-0 overflow-hidden bg-[url('@/assets/hero_image1.jpeg')] bg-cover bg-center grayscale"
        >
          <div class="absolute inset-0 bg-white/60" />
          <div class="absolute bottom-0 left-0 right-0 p-12">
            <h3 class="font-gelasio text-[32px] leading-[38px] text-[#1A1C1C] tracking-[-0.32px] mb-4">
              CHĂM SÓC
            </h3>
            <p class="font-gelasio text-base text-black leading-[26px] max-w-[384px]">
              THEXUONG luôn luôn hỗ trợ khi khách hàng cần chúng tôi.
            </p>
          </div>
        </div>
      </section>

      <!-- New Products Section -->
      <section class="w-[1121px] mx-auto mb-[30px]">
        <div class="text-center mb-16">
          <p class="font-gelasio text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
            BẢN SẮC THỂ THAO
          </p>
          <h2 class="font-geist text-[64px] leading-[77px] text-[#1A1C1C] tracking-[-1.28px] uppercase">
            SẢN PHẨM MỚI
          </h2>
        </div>

        <!-- Product Grid -->
        <div class="relative min-h-[300px]">
          <!-- Skeleton Loading State -->
          <div
            v-if="isLoading"
            class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5"
          >
            <div
              v-for="i in 4"
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
            v-else-if="newProducts.length > 0"
            class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5"
          >
            <ProductCard
              v-for="product in newProducts.slice(0, 4)"
              :key="product.id"
              :product="product"
            />
          </div>

          <!-- No Data State -->
          <div
            v-else
            class="flex justify-center items-center h-full py-12"
          >
            <p class="text-gray-500 font-gelasio">
              Chưa có sản phẩm mới nào.
            </p>
          </div>

          <!-- View All Button -->
          <div class="flex justify-center mt-10">
            <router-link
              to="/products"
              class="px-10 py-3 bg-black text-white text-[12px] font-semibold uppercase tracking-[1.8px] leading-[12px] rounded-sm hover:bg-gray-900 transition-colors"
            >
              XEM TẤT CẢ
            </router-link>
          </div>
        </div>
      </section>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import HeroImage from '@/components/HeroImage.vue'
import ProductCard from '@/components/ui/ProductCard.vue'
import BaseSkeleton from '@/components/ui/BaseSkeleton.vue'
import { productService } from '@/services/product.service'
import type { Product } from '@/types'

const newProducts = ref<Product[]>([])
const isLoading = ref(true)

const fetchNewProducts = async () => {
  isLoading.value = true
  try {
    const products = await productService.getNewProducts(4)
    newProducts.value = products || []
  } catch (error) {
    console.error('Failed to fetch new products:', error)
  } finally {
    isLoading.value = false
  }
}

onMounted(() => {
  fetchNewProducts()
})
</script>

<style scoped>
/* Marquee animation */
@keyframes marquee {
  0% {
    transform: translateX(0);
  }

  100% {
    transform: translateX(-100%);
  }
}

@keyframes marquee2 {
  0% {
    transform: translateX(100%);
  }

  100% {
    transform: translateX(0);
  }
}

.animate-marquee {
  animation: marquee 25s linear infinite;
}

.animate-marquee2 {
  animation: marquee2 25s linear infinite;
}
</style>
