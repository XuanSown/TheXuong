<template>
  <footer class="w-full bg-[#0F1115] text-white py-[30px]">
    <div class="max-w-[1280px] mx-auto px-4">
      <!-- Top Footer Section -->
      <div class="relative border-b border-white/10 pb-8 mb-8">
        <div class="grid grid-cols-1 md:grid-cols-4 gap-8">
          <!-- Company Info Column -->
          <div class="md:col-span-1 flex flex-col gap-5">
            <!-- Logo -->
            <div class="w-[72px] h-[70px] bg-[url('@/assets/logo_footer.png')] bg-contain bg-no-repeat bg-center" />

            <!-- Description -->
            <p
              class="text-[#9CA3AF] text-sm leading-[24px] font-normal"
              style="font-family: 'Geist', sans-serif;"
            >
              Nơi đam mê thể thao hội tụ. Chúng tôi cam kết mang đến những sản phẩm chất lượng nhất cho cộng đồng yêu thể thao Việt Nam.
            </p>

            <!-- Contact Info -->
            <div class="flex flex-col gap-3">
              <!-- Address -->
              <div class="flex items-center gap-3">
                <svg
                  class="w-4 h-4 text-white flex-shrink-0"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path d="M20 10c0 6-8 12-8 12s-8-6-8-12a8 8 0 0 1 16 0Z" />
                  <circle
                    cx="12"
                    cy="10"
                    r="3"
                  />
                </svg>
                <span
                  class="text-[#9CA3AF] text-sm leading-[20px]"
                  style="font-family: 'Geist', sans-serif;"
                >
                  Quận 12, TP. Hồ Chí Minh
                </span>
              </div>

              <!-- Email -->
              <div class="flex items-center gap-3">
                <svg
                  class="w-4 h-4 text-white flex-shrink-0"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <rect
                    width="20"
                    height="16"
                    x="2"
                    y="4"
                    rx="2"
                  />
                  <path d="m22 7-8.97 5.7a1.94 1.94 0 0 1-2.06 0L2 7" />
                </svg>
                <span
                  class="text-[#9CA3AF] text-sm leading-[20px]"
                  style="font-family: 'Nimbus Sans', sans-serif;"
                >
                  thexuong.sport@gmail.com
                </span>
              </div>

              <!-- Phone -->
              <div class="flex items-center gap-3">
                <svg
                  class="w-4 h-4 text-white flex-shrink-0"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path d="M22 16.92v3a2 2 0 0 1-2.18 2 19.79 19.79 0 0 1-8.63-3.07 19.5 19.5 0 0 1-6-6 19.79 19.79 0 0 1-3.07-8.67A2 2 0 0 1 4.11 2h3a2 2 0 0 1 2 1.72 12.84 12.84 0 0 0 .7 2.81 2 2 0 0 1-.45 2.11L8.09 9.91a16 16 0 0 0 6 6l1.27-1.27a2 2 0 0 1 2.11-.45 12.84 12.84 0 0 0 2.81.7A2 2 0 0 1 22 16.92z" />
                </svg>
                <span
                  class="text-[#9CA3AF] text-sm leading-[20px]"
                  style="font-family: 'Geist', sans-serif;"
                >
                  +84 909 123 456
                </span>
              </div>
            </div>
          </div>

          <!-- Customer Support Column -->
          <div class="md:col-span-1 flex flex-col gap-6">
            <h4
              class="text-[#D1D5DB] text-xs uppercase tracking-wider leading-[16px]"
              style="font-family: 'Geist', sans-serif;"
            >
              HỖ TRỢ KHÁCH HÀNG
            </h4>
            <div class="flex flex-col gap-4">
              <router-link
                to="/guide/size"
                class="text-[#9CA3AF] text-sm hover:text-white transition-colors"
              >
                Hướng dẫn chọn size
              </router-link>
              <router-link
                to="/policy/returns"
                class="text-[#9CA3AF] text-sm hover:text-white transition-colors"
              >
                Chính sách đổi trả
              </router-link>
              <router-link
                to="/payment-methods"
                class="text-[#9CA3AF] text-sm hover:text-white transition-colors"
              >
                Phương thức thanh toán
              </router-link>
              <router-link
                to="/order-tracking"
                class="text-[#9CA3AF] text-sm hover:text-white transition-colors"
              >
                Kiểm tra đơn hàng
              </router-link>
            </div>
          </div>

          <!-- Policies Column -->
          <div class="md:col-span-1 flex flex-col gap-6">
            <h4
              class="text-[#D1D5DB] text-xs uppercase tracking-wider leading-[16px]"
              style="font-family: 'Geist', sans-serif;"
            >
              ĐIỀU KHOẢN & CHÍNH SÁCH
            </h4>
            <div class="flex flex-col gap-4">
              <router-link
                to="/policy/privacy"
                class="text-[#9CA3AF] text-sm hover:text-white transition-colors"
              >
                Chính sách bảo mật
              </router-link>
              <router-link
                to="/terms-of-service"
                class="text-[#9CA3AF] text-sm hover:text-white transition-colors"
              >
                Điều khoản dịch vụ
              </router-link>
              <router-link
                to="/policy/shipping"
                class="text-[#9CA3AF] text-sm hover:text-white transition-colors"
              >
                Chính sách vận chuyển
              </router-link>
            </div>
          </div>

          <!-- Newsletter Column -->
          <div class="md:col-span-1 flex flex-col gap-4">
            <h4
              class="text-[#D1D5DB] text-xs uppercase tracking-wider leading-[16px]"
              style="font-family: 'Geist', sans-serif;"
            >
              ĐĂNG KÝ NHẬN ƯU ĐÃI
            </h4>

            <!-- Newsletter Form -->
            <div class="relative">
              <input
                type="email"
                placeholder="Email của bạn..."
                class="w-full bg-[#171A1F] border border-white/10 rounded-md px-4 py-3 text-sm text-white placeholder:text-[#6B7280] outline-none focus:border-white/30 transition-colors"
              >
              <button
                type="button"
                class="absolute right-0 top-0 bottom-0 w-12 flex items-center justify-center"
                aria-label="Submit email"
              >
                <svg
                  class="w-4 h-3"
                  viewBox="0 0 16 12"
                  fill="none"
                  xmlns="http://www.w3.org/2000/svg"
                >
                  <path
                    d="M1 6L15 6"
                    stroke="white"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                  <path
                    d="M9 2L15 6L9 10"
                    stroke="white"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </button>
            </div>

            <!-- Social Media Heading -->
            <h5
              class="text-[#6B7280] text-xs uppercase tracking-wider leading-[16px] mt-4"
              style="font-family: 'Geist', sans-serif;"
            >
              MẠNG XÃ HỘI
            </h5>

            <!-- Social Icons -->
            <div class="flex items-center gap-3">
              <a
                href="#"
                class="w-[30px] h-[30px] bg-white flex items-center justify-center rounded-sm hover:bg-gray-200 transition-colors"
                aria-label="Facebook"
              >
                <svg
                  class="w-5 h-5 text-[#0F1115]"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path d="M18 2h-3a5 5 0 0 0-5 5v3H7v4h3v8h4v-8h3l1-4h-4V7a1 1 0 0 1 1-1h3z" />
                </svg>
              </a>
              <a
                href="#"
                class="w-[30px] h-[30px] bg-white flex items-center justify-center rounded-sm hover:bg-gray-200 transition-colors"
                aria-label="Instagram"
              >
                <svg
                  class="w-5 h-5 text-[#0F1115]"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <rect
                    width="20"
                    height="20"
                    x="2"
                    y="2"
                    rx="5"
                    ry="5"
                  />
                  <path d="M16 11.37A4 4 0 1 1 12.63 8 4 4 0 0 1 16 11.37z" />
                  <line
                    x1="17.5"
                    x2="17.51"
                    y1="6.5"
                    y2="6.5"
                  />
                </svg>
              </a>
              <a
                href="#"
                class="w-[30px] h-[30px] bg-white flex items-center justify-center rounded-sm hover:bg-gray-200 transition-colors"
                aria-label="TikTok"
              >
                <svg
                  class="w-5 h-5 text-[#0F1115]"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path d="M9 12a4 4 0 1 0 4 4V4a5 5 0 0 0 5 5" />
                </svg>
              </a>
              <a
                href="#"
                class="w-[30px] h-[30px] bg-white flex items-center justify-center rounded-sm hover:bg-gray-200 transition-colors"
                aria-label="YouTube"
              >
                <svg
                  class="w-5 h-5 text-[#0F1115]"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <path d="M22.54 6.42a2.78 2.78 0 0 0-1.94-2C18.88 4 12 4 12 4s-6.88 0-8.6.46a2.78 2.78 0 0 0-1.94 2A29 29 0 0 0 1 11.75a29 29 0 0 0 .46 5.33A2.78 2.78 0 0 0 3.4 19c1.72.46 8.6.46 8.6.46s6.88 0 8.6-.46a2.78 2.78 0 0 0 1.94-2 29 29 0 0 0 .46-5.25 29 29 0 0 0-.46-5.33z" />
                  <polygon points="9.75 15.02 15.5 11.75 9.75 8.48 9.75 15.02" />
                </svg>
              </a>
            </div>
          </div>
        </div>
      </div>

      <!-- Bottom Footer -->
      <div class="flex flex-col md:flex-row justify-between items-center gap-4 pt-6">
        <div
          class="text-[#6B7280] text-xs leading-[16px]"
          style="font-family: 'Geist', sans-serif; letter-spacing: 1.1px; text-transform: uppercase;"
        >
          © 2026 THE XUONG SPORT. ALL RIGHTS RESERVED.
        </div>
        <div class="flex items-center gap-6 text-sm text-[#9CA3AF]">
          <router-link
            to="/about"
            class="hover:text-white transition-colors"
          >
            Về chúng tôi
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
  font-family: 'Geist', sans-serif;
}
</style>
