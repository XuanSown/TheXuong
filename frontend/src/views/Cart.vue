<template>
  <div class="min-h-screen bg-[#F9F9F9]">
    <main class="w-full max-w-[1280px] mx-auto px-4 pt-[120px] pb-8">
      <div class="w-[1152px] mx-auto">
        <!-- Header Section -->
        <header class="flex flex-col gap-[15px] mb-16">
          <h1 class="font-geist text-[64px] font-normal leading-[70px] tracking-[-1.28px] text-black">
            GIỎ HÀNG
          </h1>
        </header>

        <!-- Cart Content -->
        <div class="flex gap-8 mb-16">
          <!-- Cart Items List -->
          <div class="flex-1 flex flex-col gap-8">
            <!-- Item 1 -->
            <div class="border-b border-[rgba(207,196,197,0.3)] pb-8">
              <div class="flex gap-6">
                <!-- Product Image -->
                <div class="w-[160px] h-[160px] bg-[#F3F3F4] flex-shrink-0">
                  <div class="w-full h-full bg-gray-200" />
                </div>

                <!-- Product Info -->
                <div class="flex-1 flex flex-col justify-between">
                  <div class="flex flex-col gap-[5.22px]">
                    <h3 class="font-geist text-2xl font-normal leading-[38px] tracking-[-0.32px] text-black">
                      ÁO TRAINING ADIDAS PREMIUM
                    </h3>
                    <p class="font-geist text-sm text-[#5E5F5C]">
                      Size: M | Màu: Đen
                    </p>
                  </div>

                  <div class="flex justify-between items-end">
                    <!-- Quantity Controls -->
                    <div class="flex items-center gap-4">
                      <div class="flex items-center border border-[rgba(207,196,197,0.3)] w-[98px] h-[34px]">
                        <button class="w-8 h-8 flex items-center justify-center border-r border-[rgba(207,196,197,0.3)]">
                          <span class="text-[#5E5F5C] font-inter text-sm" style="font-weight: 600; letter-spacing: 0.7px; text-transform: uppercase;">-</span>
                        </button>
                        <span class="flex-1 text-center font-inter text-base text-[#1A1C1C]">1</span>
                        <button class="w-8 h-8 flex items-center justify-center border-l border-[rgba(207,196,197,0.3)]">
                          <span class="text-[#5E5F5C] font-inter text-sm" style="font-weight: 600; letter-spacing: 0.7px; text-transform: uppercase;">+</span>
                        </button>
                      </div>
                    </div>

                    <!-- Price -->
                    <div class="text-right">
                      <p class="font-geist text-2xl text-black">850.000đ</p>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Remove Link -->
              <div class="mt-4 flex justify-end">
                <button class="flex items-center gap-2 text-black hover:opacity-70 transition-opacity">
                  <svg class="w-[9.33px] h-[9.33px]" viewBox="0 0 9 9" fill="currentColor">
                    <rect width="9" height="9" fill="currentColor"/>
                  </svg>
                  <span class="font-gelasio text-base">Tiếp tục mua sắm</span>
                </button>
              </div>
            </div>

            <!-- Item 2 -->
            <div class="border-b border-[rgba(207,196,197,0.3)] pb-8">
              <div class="flex gap-6">
                <div class="w-[160px] h-[160px] bg-[#F3F3F4] flex-shrink-0">
                  <div class="w-full h-full bg-gray-200" />
                </div>
                <div class="flex-1 flex flex-col justify-between">
                  <div class="flex flex-col gap-[5.22px]">
                    <h3 class="font-geist text-2xl font-normal leading-[38px] tracking-[-0.32px] text-black">
                      MŨ TRUCKER STADIUM
                    </h3>
                    <p class="font-geist text-sm text-[#5E5F5C]">
                      Size: L | Màu: Trắng
                    </p>
                  </div>

                  <div class="flex justify-between items-end">
                    <div class="flex items-center gap-4">
                      <div class="flex items-center border border-[rgba(207,196,197,0.3)] w-[98px] h-[34px]">
                        <button class="w-8 h-8 flex items-center justify-center border-r border-[rgba(207,196,197,0.3)]">
                          <span class="text-[#5E5F5C] font-inter text-sm" style="font-weight: 600; letter-spacing: 0.7px; text-transform: uppercase;">-</span>
                        </button>
                        <span class="flex-1 text-center font-inter text-base text-[#1A1C1C]">2</span>
                        <button class="w-8 h-8 flex items-center justify-center border-l border-[rgba(207,196,197,0.3)]">
                          <span class="text-[#5E5F5C] font-inter text-sm" style="font-weight: 600; letter-spacing: 0.7px; text-transform: uppercase;">+</span>
                        </button>
                      </div>
                    </div>
                    <div class="text-right">
                      <p class="font-geist text-2xl text-black">1.000.000đ</p>
                    </div>
                  </div>
                </div>
              </div>

              <div class="mt-4 flex justify-end">
                <button class="flex items-center gap-2 text-black hover:opacity-70 transition-opacity">
                  <svg class="w-[9.33px] h-[9.33px]" viewBox="0 0 9 9" fill="currentColor">
                    <rect width="9" height="9" fill="currentColor"/>
                  </svg>
                  <span class="font-gelasio text-base">Tiếp tục mua sắm</span>
                </button>
              </div>
            </div>
          </div>

          <!-- Order Summary Sidebar -->
          <div class="w-[466px] flex-shrink-0">
            <div class="bg-[#F3F3F4] border border-[rgba(207,196,197,0.2)] p-8 flex flex-col gap-6">
              <h2 class="font-geist text-2xl font-semibold leading-[38px] text-[#1A1C1C]">
                Tạm tính
              </h2>

              <div class="flex flex-col gap-4">
                <!-- Subtotal -->
                <div class="flex justify-between items-center">
                  <span class="font-geist text-base text-[#5E5F5C]">Tạm tính (2 sản phẩm)</span>
                  <span class="font-geist text-base text-black">1.850.000đ</span>
                </div>

                <!-- Shipping -->
                <div class="flex justify-between items-center">
                  <span class="font-geist text-base text-[#5E5F5C]">Phí vận chuyển</span>
                  <span class="font-gelasio text-base text-black">Miễn phí</span>
                </div>

                <!-- Divider -->
                <div class="border-t border-[rgba(207,196,197,0.3)] pt-4">
                  <div class="flex justify-between items-center">
                    <span class="font-geist text-2xl font-semibold leading-[38px] tracking-[-0.32px] text-[#1A1C1C]">Tổng cộng</span>
                    <span class="font-geist text-2xl text-black">1.850.000đ</span>
                  </div>
                </div>
              </div>

              <!-- Checkout Button -->
              <button class="w-full h-[56px] bg-black border-2 border-black text-white font-geist text-base flex items-center justify-center hover:bg-gray-900 transition-colors">
                THANH TOÁN NGAY
              </button>

              <!-- Security Message -->
              <div class="flex items-center justify-center gap-4 text-[#5E5F5C] text-sm">
                <svg class="w-4 h-3" viewBox="0 0 16 21" fill="currentColor">
                  <path d="M8 1C4.5 4 2 6.5 2 9C2 10.5 3 11.5 4.5 11.5C5 11.5 5.5 11.4 6 11.3C6.5 11.4 7 11.5 7.5 11.5C9 11.5 10 10.5 10 9C10 6.5 7.5 4 6 1Z"/>
                </svg>
                <span>Thanh toán an toàn & bảo mật</span>
              </div>
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

.font-inter {
  font-family: 'Inter', sans-serif;
}
</style>
