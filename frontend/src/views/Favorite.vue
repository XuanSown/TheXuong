<template>
  <div class="min-h-screen bg-[#F9F9F9]">
    <main class="w-full max-w-[1280px] mx-auto px-4 pt-[120px] pb-8">
      <div class="w-[1152px] mx-auto">
        <!-- Header Section -->
        <header class="flex flex-col gap-[15px] mb-16">
          <h1 class="font-geist text-[64px] font-normal leading-[70px] tracking-[-1.28px] text-black uppercase">
            SẢN PHẨM YÊU THÍCH
          </h1>
          <p class="font-gelasio text-base text-[#4C4546] max-w-[672px]">
            Lưu lại những món đồ bạn yêu thích nhất để dễ dàng tìm kiếm và mua sắm.
          </p>
        </header>

        <!-- Empty State -->
        <div class="relative w-[270px] h-[408.19px] border-2 border-dashed border-[#CFC4C5] rounded-lg flex flex-col items-center justify-center gap-4 mb-8">
          <div class="w-10 h-10 bg-[#CFC4C5] rounded-full flex items-center justify-center">
            <svg class="w-5 h-5 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
            </svg>
          </div>
          <span class="font-gelasio text-sm text-[#646562]">Tiếp tục mua sắm</span>
          <router-link to="/products" class="font-gelasio text-sm font-semibold text-black hover:underline">
            Thêm nhiều sản phẩm yêu thích hơn
          </router-link>
        </div>

        <!-- Product Grid -->
        <div class="grid grid-cols-4 gap-6 mb-16">
          <!-- Product Card 1 -->
          <div class="relative group">
            <div class="relative h-[270px] bg-[#F3F3F4] rounded-lg overflow-hidden mb-4">
              <div class="w-full h-full bg-gray-200" />
              <!-- Wishlist Heart Overlay -->
              <button class="absolute top-[17px] right-[17px] w-8 h-8 bg-black/10 backdrop-blur-[2px] rounded-full flex items-center justify-center hover:bg-black/20 transition-colors">
                <svg class="w-[16.67px] h-[15.29px]" viewBox="0 0 16 15" fill="black">
                  <path d="M8 1C4.5 4 2 6.5 2 9C2 10.5 3 11.5 4.5 11.5C5 11.5 5.5 11.4 6 11.3C6.5 11.4 7 11.5 7.5 11.5C9 11.5 10 10.5 10 9C10 6.5 7.5 4 6 1Z"/>
                </svg>
              </button>
            </div>
            <div class="flex flex-col gap-2">
              <h3 class="font-gelasio text-base font-semibold text-black uppercase leading-[26px] truncate">
                GIÀY ĐÁ BÓNG NIKE AIR ZOOM
              </h3>
              <p class="font-geist text-base text-black">3.250.000đ</p>
              <button class="w-full h-[36px] border border-black rounded flex items-center justify-center gap-2 hover:bg-black hover:text-white transition-colors">
                <svg class="w-[13.32px] h-[13.33px]" viewBox="0 0 13 13">
                  <rect width="13" height="13" fill="currentColor"/>
                </svg>
                <span class="font-geist text-[10px] text-[#1A1C1C]">THÊM VÀO GIỎ</span>
              </button>
            </div>
          </div>

          <!-- Product Card 2 -->
          <div class="relative group">
            <div class="relative h-[270px] bg-[#F3F3F4] rounded-lg overflow-hidden mb-4">
              <div class="w-full h-full bg-gray-200" />
              <button class="absolute top-[17px] right-[17px] w-8 h-8 bg-black/10 backdrop-blur-[2px] rounded-full flex items-center justify-center hover:bg-black/20 transition-colors">
                <svg class="w-[16.67px] h-[15.29px]" viewBox="0 0 16 15" fill="black">
                  <path d="M8 1C4.5 4 2 6.5 2 9C2 10.5 3 11.5 4.5 11.5C5 11.5 5.5 11.4 6 11.3C6.5 11.4 7 11.5 7.5 11.5C9 11.5 10 10.5 10 9C10 6.5 7.5 4 6 1Z"/>
                </svg>
              </button>
            </div>
            <div class="flex flex-col gap-2">
              <h3 class="font-gelasio text-base font-semibold text-black uppercase leading-[26px] truncate">
                ÁO TRAINING ADIDAS PREMIUM
              </h3>
              <p class="font-geist text-base text-black">890.000đ</p>
              <button class="w-full h-[36px] border border-black rounded flex items-center justify-center gap-2 hover:bg-black hover:text-white transition-colors">
                <svg class="w-[13.32px] h-[13.33px]" viewBox="0 0 13 13">
                  <rect width="13" height="13" fill="currentColor"/>
                </svg>
                <span class="font-geist text-[10px] text-[#1A1C1C]">THÊM VÀO GIỎ</span>
              </button>
            </div>
          </div>

          <!-- Product Card 3 -->
          <div class="relative group">
            <div class="relative h-[270px] bg-[#F3F3F4] rounded-lg overflow-hidden mb-4">
              <div class="w-full h-full bg-gray-200" />
              <button class="absolute top-[17px] right-[17px] w-8 h-8 bg-black/10 backdrop-blur-[2px] rounded-full flex items-center justify-center hover:bg-black/20 transition-colors">
                <svg class="w-[16.67px] h-[15.29px]" viewBox="0 0 16 15" fill="black">
                  <path d="M8 1C4.5 4 2 6.5 2 9C2 10.5 3 11.5 4.5 11.5C5 11.5 5.5 11.4 6 11.3C6.5 11.4 7 11.5 7.5 11.5C9 11.5 10 10.5 10 9C10 6.5 7.5 4 6 1Z"/>
                </svg>
              </button>
            </div>
            <div class="flex flex-col gap-2">
              <h3 class="font-gelasio text-base font-semibold text-black uppercase leading-[26px] truncate">
                VỢT CẦU LÔNG YONEX ASTROX
              </h3>
              <p class="font-geist text-base text-black">4.100.000đ</p>
              <button class="w-full h-[36px] border border-black rounded flex items-center justify-center gap-2 hover:bg-black hover:text-white transition-colors">
                <svg class="w-[13.32px] h-[13.33px]" viewBox="0 0 13 13">
                  <rect width="13" height="13" fill="currentColor"/>
                </svg>
                <span class="font-geist text-[10px] text-[#1A1C1C]">THÊM VÀO GIỎ</span>
              </button>
            </div>
          </div>

          <!-- Product Card 4 -->
          <div class="relative group">
            <div class="relative h-[270px] bg-[#F3F3F4] rounded-lg overflow-hidden mb-4">
              <div class="w-full h-full bg-gray-200" />
              <button class="absolute top-[17px] right-[17px] w-8 h-8 bg-black/10 backdrop-blur-[2px] rounded-full flex items-center justify-center hover:bg-black/20 transition-colors">
                <svg class="w-[16.67px] h-[15.29px]" viewBox="0 0 16 15" fill="black">
                  <path d="M8 1C4.5 4 2 6.5 2 9C2 10.5 3 11.5 4.5 11.5C5 11.5 5.5 11.4 6 11.3C6.5 11.4 7 11.5 7.5 11.5C9 11.5 10 10.5 10 9C10 6.5 7.5 4 6 1Z"/>
                </svg>
              </button>
            </div>
            <div class="flex flex-col gap-2">
              <h3 class="font-gelasio text-base font-semibold text-black uppercase leading-[26px] truncate">
                CLASSIC WHITE RUNNERS
              </h3>
              <p class="font-geist text-base text-black">2.100.000đ</p>
              <button class="w-full h-[36px] border border-black rounded flex items-center justify-center gap-2 hover:bg-black hover:text-white transition-colors">
                <svg class="w-[13.32px] h-[13.33px]" viewBox="0 0 13 13">
                  <rect width="13" height="13" fill="currentColor"/>
                </svg>
                <span class="font-geist text-[10px] text-[#1A1C1C]">THÊM VÀO GIỎ</span>
              </button>
            </div>
          </div>
        </div>
      </div>
    </main>

    <!-- Footer -->
    <Footer />
  </div>
</template>

<script setup lang="ts">
import Footer from '@/components/layout/Footer.vue'
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Gelasio:wght@400;500;600;700&family=Inter:wght@400;500;600;700&display=swap') layer(fonts);

.font-geist {
  font-family: 'Inter', 'Geist', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.font-gelasio {
  font-family: 'Gelasio', serif;
}
</style>
