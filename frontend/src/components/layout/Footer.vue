<template>
  <footer class="w-full bg-[#0F1115] text-white py-[30px]">
    <div class="max-w-[1280px] mx-auto px-4">
      <!-- Top Footer Section -->
      <div class="relative border-b border-white/10 pb-8 mb-8">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
          <!-- Company Info Column -->
          <div class="md:col-span-1 flex flex-col gap-5">
            <!-- Logo -->
            <div class="w-[72px] h-[70px] bg-[url('@/assets/logo.png')] bg-contain bg-no-repeat bg-center" />

            <!-- Description -->
            <p class="text-[#9CA3AF] text-sm leading-[24px] font-normal" style="font-family: 'Gelasio', serif;">
              Nơi đam mê thể thao hội tụ. Chúng tôi cam kết mang đến những sản phẩm chất lượng nhất cho cộng đồng yêu thể thao Việt Nam.
            </p>

            <!-- Contact Info -->
            <div class="flex flex-col gap-3">
              <!-- Address -->
              <div class="flex items-center gap-3">
                <div class="w-[12px] h-[15px] bg-white flex-shrink-0" />
                <span class="text-[#9CA3AF] text-sm leading-[20px]" style="font-family: 'Geist', sans-serif;">
                  Quận 12, TP. Hồ Chí Minh
                </span>
              </div>

              <!-- Email -->
              <div class="flex items-center gap-3">
                <div class="w-[15px] h-[12px] bg-white flex-shrink-0" />
                <span class="text-[#9CA3AF] text-sm leading-[20px]" style="font-family: 'Nimbus Sans', sans-serif;">
                  thexuong.sport@gmail.com
                </span>
              </div>

              <!-- Phone -->
              <div class="flex items-center gap-3">
                <div class="w-[13.5px] h-[13.5px] bg-white flex-shrink-0 rounded-full" />
                <span class="text-[#9CA3AF] text-sm leading-[20px]" style="font-family: 'Geist', sans-serif;">
                  +84 909 123 456
                </span>
              </div>
            </div>
          </div>

          <!-- Customer Support Column -->
          <div class="md:col-span-1 flex flex-col gap-6">
            <h4 class="text-[#D1D5DB] text-xs uppercase tracking-wider leading-[16px]" style="font-family: 'Gelasio', serif;">
              HỖ TRỢ KHÁCH HÀNG
            </h4>
            <div class="flex flex-col gap-4">
              <router-link to="/guide/size" class="text-[#9CA3AF] text-sm hover:text-white transition-colors">
                Hướng dẫn chọn size
              </router-link>
              <router-link to="/policy/returns" class="text-[#9CA3AF] text-sm hover:text-white transition-colors">
                Chính sách đổi trả
              </router-link>
              <router-link to="/payment-methods" class="text-[#9CA3AF] text-sm hover:text-white transition-colors">
                Phương thức thanh toán
              </router-link>
              <router-link to="/order-tracking" class="text-[#9CA3AF] text-sm hover:text-white transition-colors">
                Kiểm tra đơn hàng
              </router-link>
            </div>
          </div>

          <!-- Policies Column -->
          <div class="md:col-span-1 flex flex-col gap-6">
            <h4 class="text-[#D1D5DB] text-xs uppercase tracking-wider leading-[16px]" style="font-family: 'Gelasio', serif;">
              ĐIỀU KHOẢN & CHÍNH SÁCH
            </h4>
            <div class="flex flex-col gap-4">
              <router-link to="/policy/privacy" class="text-[#9CA3AF] text-sm hover:text-white transition-colors">
                Chính sách bảo mật
              </router-link>
              <router-link to="/terms-of-service" class="text-[#9CA3AF] text-sm hover:text-white transition-colors">
                Điều khoản dịch vụ
              </router-link>
              <router-link to="/policy/shipping" class="text-[#9CA3AF] text-sm hover:text-white transition-colors">
                Chính sách vận chuyển
              </router-link>
              <router-link to="/agency-partnership" class="text-[#9CA3AF] text-sm hover:text-white transition-colors">
                Hợp tác đại lý
              </router-link>
            </div>
          </div>

          <!-- Newsletter Column -->
          <div class="md:col-span-1 flex flex-col gap-4">
            <h4 class="text-[#D1D5DB] text-xs uppercase tracking-wider leading-[16px]" style="font-family: 'Gelasio', serif;">
              ĐĂNG KÝ NHẬN ƯU ĐÃI
            </h4>

            <!-- Newsletter Form -->
            <div class="relative">
              <input
                type="email"
                placeholder="Email của bạn..."
                class="w-full bg-[#171A1F] border border-white/10 rounded-md px-4 py-3 text-sm text-white placeholder:text-[#6B7280] outline-none focus:border-white/30 transition-colors"
              />
              <button
                type="button"
                class="absolute right-0 top-0 bottom-0 w-12 flex items-center justify-center"
                aria-label="Submit email"
              >
                <svg class="w-4 h-3" viewBox="0 0 16 12" fill="none" xmlns="http://www.w3.org/2000/svg">
                  <path d="M1 6L15 6" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                  <path d="M9 2L15 6L9 10" stroke="white" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                </svg>
              </button>
            </div>

            <!-- Social Media Heading -->
            <h5 class="text-[#6B7280] text-xs uppercase tracking-wider leading-[16px] mt-4" style="font-family: 'Gelasio', serif;">
              MẠNG XÃ HỘI
            </h5>

            <!-- Social Icons -->
            <div class="flex items-center gap-3">
              <a href="#" class="w-[30px] h-[30px] bg-white flex items-center justify-center hover:bg-gray-200 transition-colors" aria-label="Facebook">
                <svg class="w-5 h-5 text-[#0F1115]" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.47h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.47h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/>
                </svg>
              </a>
              <a href="#" class="w-[30px] h-[30px] bg-white border border-white flex items-center justify-center hover:bg-white/90 transition-colors" aria-label="Instagram">
                <svg class="w-5 h-5 text-[#0F1115]" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12 2.163c3.204 0 3.584.012 4.85.07 3.252.148 4.771 1.691 4.919 4.919.058 1.265.069 1.645.069 4.849 0 3.205-.012 3.584-.069 4.849-.149 3.225-1.664 4.771-4.919 4.919-1.266.058-1.644.07-4.85.07-3.204 0-3.584-.012-4.849-.07-3.26-.149-4.771-1.699-4.919-4.92-.058-1.265-.07-1.644-.07-4.849 0-3.204.013-3.583.07-4.849.149-3.227 1.664-4.771 4.919-4.919 1.266-.057 1.645-.069 4.849-.069zm0-2.163c-3.259 0-3.667.014-4.947.072-4.358.2-6.78 2.618-6.98 6.98-.059 1.281-.073 1.689-.073 4.948 0 3.259.014 3.668.072 4.948.2 4.358 2.618 6.78 6.98 6.98 1.281.058 1.689.072 4.948.072 3.259 0 3.668-.014 4.948-.072 4.354-.2 6.782-2.618 6.979-6.98.059-1.28.073-1.689.073-4.948 0-3.259-.014-3.667-.072-4.947-.196-4.354-2.617-6.78-6.979-6.98-1.281-.059-1.69-.073-4.949-.073zm0 5.838c-3.403 0-6.162 2.759-6.162 6.162s2.759 6.163 6.162 6.163 6.162-2.759 6.162-6.163c0-3.403-2.759-6.162-6.162-6.162zm0 10.162c-2.209 0-4-1.79-4-4 0-2.209 1.791-4 4-4s4 1.791 4 4c0 2.21-1.791 4-4 4zm6.406-11.845c-.796 0-1.441.645-1.441 1.44s.645 1.44 1.441 1.44c.795 0 1.439-.645 1.439-1.44s-.644-1.44-1.439-1.44z"/>
                </svg>
              </a>
              <a href="#" class="w-[30px] h-[30px] bg-white flex items-center justify-center hover:bg-gray-200 transition-colors" aria-label="TikTok">
                <svg class="w-5 h-5 text-[#0F1115]" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M12.525.02c1.31-.02 2.61-.01 3.91-.02.08 1.53.63 3.09 1.75 4.17 1.12 1.11 2.7 1.62 4.24 1.79v4.03c-1.44-.05-2.89-.35-4.2-.97-.57-.26-1.1-.59-1.62-.93-.01 2.92.01 5.84-.02 8.75-.08 1.4-.54 2.79-1.35 3.94-1.31 1.92-3.58 3.17-5.91 3.21-1.43.08-2.86-.31-4.08-1.03-2.02-1.19-3.44-3.37-3.65-5.71-.02-.5-.03-1-.01-1.49.18-1.9 1.12-3.72 2.58-4.96 1.66-1.44 3.98-2.13 6.15-1.72.02 1.48-.04 2.96-.04 4.44-.99-.32-2.15-.23-3.02.37-.63.41-1.11 1.04-1.36 1.75-.21.51-.15 1.07-.14 1.61.24 1.64 1.82 3.02 3.5 2.87 1.12-.01 2.19-.66 2.77-1.61.19-.33.4-.67.41-1.06.1-1.79.06-3.57.07-5.36.01-4.03-.01-8.05.02-12.07z"/>
                </svg>
              </a>
              <a href="#" class="w-[30px] h-[30px] bg-white border border-white flex items-center justify-center hover:bg-white/90 transition-colors" aria-label="YouTube">
                <svg class="w-5 h-5 text-[#0F1115]" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M23.498 6.186a3.016 3.016 0 0 0-2.122-2.136C19.505 3.545 12 3.545 12 3.545s-7.505 0-9.377.505A3.017 3.017 0 0 0 .502 6.186C0 8.07 0 12 0 12s0 3.93.502 5.814a3.016 3.016 0 0 0 2.122 2.136c1.871.505 9.376.505 9.376.505s7.505 0 9.377-.505a3.015 3.015 0 0 0 2.122-2.136C24 15.93 24 12 24 12s0-3.93-.502-5.814zM9.545 15.568V8.432L15.818 12l-6.273 3.568z"/>
                </svg>
              </a>
            </div>
          </div>
        </div>
      </div>

      <!-- Bottom Footer -->
      <div class="flex flex-col md:flex-row justify-between items-center gap-4 pt-6">
        <div class="text-[#6B7280] text-xs leading-[16px]" style="font-family: 'Geist', sans-serif; letter-spacing: 1.1px; text-transform: uppercase;">
          © 2026 THE XUONG SPORT. ALL RIGHTS RESERVED.
        </div>
        <div class="flex items-center gap-6 text-sm text-[#9CA3AF]">
          <router-link to="/about" class="hover:text-white transition-colors">
            Về chúng tôi
          </router-link>
          <router-link to="/careers" class="hover:text-white transition-colors">
            Tuyển dụng
          </router-link>
          <router-link to="/contact" class="hover:text-white transition-colors">
            Liên hệ
          </router-link>
        </div>
      </div>
    </div>
  </footer>
</template>

<script setup lang="ts">
// No script logic needed for static footer
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Gelasio:wght@400;500&family=Inter:wght@400;500;600&display=swap');

.font-geist {
  font-family: 'Inter', 'Geist', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}
</style>
