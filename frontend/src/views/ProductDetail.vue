<template>
  <div class="min-h-screen bg-[#F9F9F9]">
    <main class="w-full max-w-[1280px] mx-auto px-4 pt-[120px] pb-8">
      <!-- Breadcrumbs -->
      <nav class="w-[1152px] mx-auto mb-8 flex items-center gap-2">
        <router-link to="/" class="font-geist text-[12px] font-semibold uppercase tracking-[1.8px] text-[#646562] hover:text-black transition-colors">
          TRANG CHỦ
        </router-link>
        <svg class="w-[4.32px] h-[7px]" viewBox="0 0 4 7" fill="currentColor">
          <path d="M0 3.5L4 0V7L0 3.5Z" fill="black"/>
        </svg>
        <router-link to="/products" class="font-geist text-[12px] font-semibold uppercase tracking-[1.8px] text-[#646562] hover:text-black transition-colors">
          SẢN PHẨM
        </router-link>
        <svg class="w-[4.32px] h-[7px]" viewBox="0 0 4 7" fill="currentColor">
          <path d="M0 3.5L4 0V7L0 3.5Z" fill="black"/>
        </svg>
        <span class="font-geist text-[12px] font-semibold uppercase tracking-[1.8px] text-black">
          MŨ TRUCKER STADIUM
        </span>
      </nav>

      <!-- Product Detail Section -->
      <section class="w-[1152px] mx-auto bg-white mb-8">
        <div class="flex gap-8 p-8">
          <!-- Left: Image Gallery -->
          <div class="flex gap-4">
            <!-- Thumbnail Column -->
            <div class="flex flex-col gap-4">
              <div class="w-[80px] h-[80px] bg-[#F3F3F4] border border-[rgba(207,196,197,0.3)] flex items-center justify-center">
                <div class="w-[78px] h-[78px] bg-gray-300" />
              </div>
              <div class="w-[80px] h-[80px] bg-[#F3F3F4] border border-[rgba(207,196,197,0.3)] flex items-center justify-center">
                <div class="w-[78px] h-[78px] bg-gray-300" />
              </div>
              <div class="w-[80px] h-[80px] bg-[#F3F3F4] border border-[rgba(207,196,197,0.3)] flex items-center justify-center">
                <div class="w-[78px] h-[78px] bg-gray-300" />
              </div>
            </div>

            <!-- Main Image -->
            <div class="relative w-[556px] h-[556px] bg-[#F3F3F4] border border-[rgba(207,196,197,0.3)] flex items-center justify-center">
              <div class="w-[554px] h-[554px] bg-gray-300" />

              <!-- Zoom Controls -->
              <div class="absolute right-4 bottom-4 flex gap-2">
                <button class="w-10 h-10 bg-white/80 backdrop-blur-[4px] rounded-full flex items-center justify-center shadow hover:bg-white transition-colors" aria-label="Zoom in">
                  <svg class="w-[7.4px] h-[12px]" viewBox="0 0 8 12" fill="currentColor">
                    <path d="M4 2V0M6 4H4M4 8C4 8 2 10 2 12H6C6 10 4 8 4 8Z" stroke="black" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </button>
                <button class="w-10 h-10 bg-white/80 backdrop-blur-[4px] rounded-full flex items-center justify-center shadow hover:bg-white transition-colors" aria-label="Zoom out">
                  <svg class="w-[7.4px] h-[12px]" viewBox="0 0 8 12" fill="currentColor">
                    <path d="M2 6H6M4 2C4 2 6 4 6 6C6 8 4 10 4 10" stroke="black" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>

          <!-- Right: Product Info -->
          <div class="flex-1 flex flex-col gap-8 max-w-[452px]">
            <!-- Header -->
            <div class="flex flex-col gap-[15px]">
              <div class="flex items-center gap-3">
                <div class="w-[16.5px] h-[11.25px] bg-[#4C4546]" />
                <span class="font-geist text-base text-[#4C4546]">2 lượt xem</span>
              </div>

              <h1 class="font-geist text-[32px] font-semibold leading-[38px] tracking-[-0.32px] text-black">
                MŨ TRUCKER STADIUM
              </h1>

              <div class="flex items-center gap-4">
                <span class="font-inter text-[32px] font-semibold leading-[38px] text-black">450.000 đ</span>
                <button class="w-[23.33px] h-[21.41px] bg-black flex items-center justify-center rounded-full" aria-label="Add to wishlist">
                  <svg class="w-3 h-2" viewBox="0 0 12 10" fill="white">
                    <path d="M6 1C4.5 4 2 6.5 2 9C2 10.5 3 11.5 4.5 11.5C5 11.5 5.5 11.4 6 11.3C6.5 11.4 7 11.5 7.5 11.5C9 11.5 10 10.5 10 9C10 6.5 7.5 4 6 1Z"/>
                  </svg>
                </button>
              </div>

              <hr class="border-[#CFC4C5]" />

              <p class="font-gelasio text-base text-[#4C4546] leading-[26px]">
                Mũ Trucker Stadium là phụ kiện không thể thiếu, hoàn hảo cho phong cách thư giãn hằng ngày. Thiết kế dáng trucker thời thượng, chiếc mũ lưỡi trai này là sự kết hợp hoàn hảo với tủ đồ thể thao của bạn.
              </p>
            </div>

            <!-- Size Selector -->
            <div class="flex flex-col gap-4">
              <div class="flex items-center gap-2">
                <span class="font-geist text-[12px] font-bold uppercase tracking-[1.2px] text-black">
                  CHỌN KÍCH Cỡ:
                </span>
              </div>

              <div class="flex gap-2">
                <button class="w-[81.53px] h-[48px] border border-black flex items-center justify-center font-geist text-base text-[#1A1C1C] hover:bg-black hover:text-white transition-colors">
                  S
                </button>
                <button class="w-[81.35px] h-[48px] border border-[#7E7576] flex items-center justify-center font-geist text-base text-[#1A1C1C] hover:border-black hover:bg-black hover:text-white transition-colors">
                  M
                </button>
                <button class="w-[81.75px] h-[48px] border border-[#7E7576] flex items-center justify-center font-geist text-base text-[#1A1C1C] hover:border-black hover:bg-black hover:text-white transition-colors">
                  L
                </button>
                <button class="w-[79.83px] h-[48px] border border-[#7E7576] flex items-center justify-center font-geist text-base text-[#1A1C1C] hover:border-black hover:bg-black hover:text-white transition-colors">
                  XL
                </button>
                <button class="w-[79.34px] h-[48px] border border-[#7E7576] flex items-center justify-center font-geist text-base text-[#1A1C1C] hover:border-black hover:bg-black hover:text-white transition-colors">
                  FreeSize
                </button>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex flex-col gap-3">
              <button class="w-full h-[56px] bg-black text-white font-geist text-base flex items-center justify-center hover:bg-gray-900 transition-colors">
                MUA NGAY
              </button>
              <button class="w-full h-[56px] bg-white border-2 border-black text-black font-geist text-base flex items-center justify-center hover:bg-black hover:text-white transition-colors">
                VUI LÒNG CHỌN SIZE
              </button>
            </div>

            <!-- Trust Badges -->
            <div class="w-full bg-[#F3F3F4] p-4 flex justify-between items-center">
              <!-- 30-day returns -->
              <div class="flex flex-col items-center gap-3 flex-1">
                <div class="w-[18px] h-[18px] bg-black" />
                <span class="font-geist text-[10px] leading-[12px] text-center text-[#1A1C1C]">
                  ĐỔI TRẢ<br>TRONG 30 NGÀY
                </span>
              </div>

              <!-- Warranty -->
              <div class="flex flex-col items-center gap-3 flex-1">
                <div class="w-[16px] h-[20px] bg-black" />
                <span class="font-geist text-[10px] leading-[12px] text-center text-[#1A1C1C]">
                  BẢO HÀNH<br>CHÍNH HÃNG
                </span>
              </div>

              <!-- Free Shipping -->
              <div class="flex flex-col items-center gap-3 flex-1">
                <div class="w-[22px] h-[16px] bg-black" />
                <span class="font-geist text-[10px] leading-[12px] text-center text-[#1A1C1C]">
                  FREESHIP<br>TOÀN QUỐC
                </span>
              </div>
            </div>
          </div>
        </div>
      </section>
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

.font-inter {
  font-family: 'Inter', sans-serif;
}
</style>
