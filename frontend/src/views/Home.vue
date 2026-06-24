<template>
  <div class="min-h-screen bg-[#F9F9F9]">
    <!-- Main Content -->
    <main class="w-full max-w-[1280px] mx-auto px-4 py-[30px]">
      <!-- Hero Section -->
      <section class="w-full h-[888px] flex flex-col items-center justify-center gap-[31px] mb-8">
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
            class="px-10 py-[27px] border border-black text-black text-[12px] font-semibold uppercase tracking-[1.8px] leading-[12px] rounded-sm hover:bg-black hover:text-white transition-colors"
          >
            VỀ CHÚNG TÔI
          </router-link>
        </div>

        <!-- Hero Image -->
        <div class="w-[1152px] h-[648px] bg-[#EEEEEE] border border-[#CFC4C6] rounded-lg relative overflow-hidden">
          <div class="absolute inset-0 bg-gradient-to-t from-[rgba(249,249,249,0.4)] to-transparent" />
          <!-- Placeholder for hero image -->
          <div class="w-full h-full flex items-center justify-center text-[#9CA3AF]">
            <span class="font-gelasio text-2xl">Hero Image</span>
          </div>
        </div>
      </section>

      <!-- Trust Brands Marquee -->
      <section class="py-[120px] px-0 bg-white border-y border-[#CFC4C6] mb-[30px]">
        <div class="overflow-hidden">
          <div class="flex animate-marquee gap-[208.52px] px-0" style="width: max-content;">
            <!-- Brand logos (duplicated for infinite scroll) -->
            <span class="font-geist text-2xl text-[#1A1C1C] opacity-80 whitespace-nowrap">NIKE</span>
            <span class="font-geist text-2xl font-bold text-[#1A1C1C] opacity-80 whitespace-nowrap">ADIDAS</span>
            <span class="font-geist text-2xl font-bold text-[#1A1C1C] opacity-80 whitespace-nowrap">LI-NING</span>
            <span class="font-geist text-2xl font-bold text-[#1A1C1C] opacity-80 whitespace-nowrap">PUMA</span>
            <span class="font-geist text-2xl text-[#1A1C1C] opacity-80 whitespace-nowrap">NIKE</span>
            <span class="font-geist text-2xl font-bold text-[#1A1C1C] opacity-80 whitespace-nowrap">ADIDAS</span>
            <span class="font-geist text-2xl font-bold text-[#1A1C1C] opacity-80 whitespace-nowrap">LI-NING</span>
            <!-- Duplicate set for seamless loop -->
            <span class="font-geist text-2xl text-[#1A1C1C] opacity-80 whitespace-nowrap">NIKE</span>
            <span class="font-geist text-2xl font-bold text-[#1A1C1C] opacity-80 whitespace-nowrap">ADIDAS</span>
            <span class="font-geist text-2xl font-bold text-[#1A1C1C] opacity-80 whitespace-nowrap">LI-NING</span>
            <span class="font-geist text-2xl font-bold text-[#1A1C1C] opacity-80 whitespace-nowrap">PUMA</span>
          </div>
        </div>
      </section>

      <!-- Bento Grid Features -->
      <section class="w-[1152px] h-[824px] relative mb-[30px]">
        <!-- Left Top: Technology -->
        <div class="absolute left-0 right-[392px] top-0 h-[400px] bg-[#F3F3F4] border border-[#CFC4C6] p-[48px]">
          <div class="w-[662px] h-[33px] bg-black mb-[49px] mt-4" />
          <h3 class="absolute left-[49px] right-[49px] top-[219.83px] font-gelasio text-[32px] leading-[38px] text-[#1A1C1C] tracking-[-0.32px]">
            CÔNG NGHỆ VƯỢT TRỘI
          </h3>
          <p class="absolute left-[49px] right-[263px] bottom-[49px] max-w-[448px] font-gelasio text-base text-[#5E5F5C] leading-[26px]">
            Tất cả sản phẩm tại TheXuong đều được tuyển chọn kỹ lưỡng, đảm bảo tích hợp những công nghệ hỗ trợ vận động tiên tiến nhất hiện nay.
          </p>
        </div>

        <!-- Right: Authentic Guarantee -->
        <div class="absolute left-[784px] right-0 top-0 h-[400px] bg-black p-[48px]">
          <div class="w-[272px] h-[30px] bg-white mb-6" />
          <div class="flex flex-col gap-4">
            <h3 class="font-gelasio text-[32px] leading-[38px] text-white tracking-[-0.32px]">
              CAM KẾT CHÍNH HÃNG
            </h3>
            <div class="relative w-[261.59px] h-[107.39px] bg-[#0E0E0E] p-6">
              <p class="font-gelasio text-lg text-[rgba(255,255,255,0.6)] leading-[29px]">
                Chúng tôi cam kết 100% sản phẩm là hàng chính hãng, phát hiện hàng giả đền bù gấp 10 lần.
              </p>
            </div>
          </div>
        </div>

        <!-- Left Bottom: Reputation -->
        <div class="absolute left-0 right-[784px] top-[424px] h-[400px] bg-[#F3F3F4] border border-[#CFC4C6] p-[48px]">
          <div class="w-[16.5px] h-[27px] bg-[#1A1C1C] mb-4" />
          <h3 class="font-gelasio text-[32px] leading-[38px] text-[#1A1C1C] tracking-[-0.32px] mb-4">
            UY TÍN
          </h3>
          <p class="font-gelasio text-base text-black leading-[26px] max-w-[270px]">
            THEXUONG tự tin với chất lượng sản phẩm.
          </p>
        </div>

        <!-- Right Bottom: Care -->
        <div class="absolute left-[392px] right-0 top-[424px] h-[400px] border border-[#CFC4C6] p-0 overflow-hidden">
          <div class="absolute inset-0 bg-white/50" />
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
        <div class="relative h-[411.3px]">
          <!-- Product 1 -->
          <div class="absolute left-0 right-[858.75px] top-0 h-[388.8px] flex flex-col gap-6">
            <div class="w-[262.25px] h-[270px] bg-[#EEEEEE] border border-[#CFC4C6]" />
            <div class="flex flex-col gap-2">
              <h4 class="font-gelasio text-lg text-[#1A1C1C] leading-[29px]">
                Giày Đá Bóng Nike Air Zoom
              </h4>
              <p class="font-geist text-base font-semibold text-black leading-[24px]">
                2.450.000 đ
              </p>
            </div>
          </div>

          <!-- Product 2 -->
          <div class="absolute left-[286.25px] right-[572.5px] top-0 h-[388.8px] flex flex-col gap-6">
            <div class="w-[262.25px] h-[270px] bg-[#EEEEEE] border border-[#CFC4C6]" />
            <div class="flex flex-col gap-2">
              <h4 class="font-gelasio text-lg font-bold text-[#1A1C1C] leading-[29px]">
                Vợt Cầu Lông Yonex Astrox
              </h4>
              <p class="font-geist text-base font-semibold text-black leading-[24px]">
                3.850.000 đ
              </p>
            </div>
          </div>

          <!-- Product 3 -->
          <div class="absolute left-[572.5px] right-[286.25px] top-0 h-[354.8px] flex flex-col gap-6">
            <div class="w-[262.25px] h-[270px] bg-[#EEEEEE] border border-[#CFC4C6]" />
            <div class="flex flex-col gap-2">
              <h4 class="font-gelasio text-lg font-bold text-[#1A1C1C] leading-[29px]">
                Áo Training Adidas Premium
              </h4>
              <p class="font-geist text-base font-semibold text-black leading-[24px]">
                850.000 đ
              </p>
            </div>
          </div>

          <!-- Product 4 -->
          <div class="absolute left-[858.75px] right-0 top-0 h-[388.8px] flex flex-col gap-6">
            <div class="w-[262.25px] h-[270px] bg-[#EEEEEE] border border-[#CFC4C6]" />
            <div class="flex flex-col gap-2">
              <h4 class="font-gelasio text-lg font-bold text-[#1A1C1C] leading-[29px]">
                Giày Chạy Bộ Asics Gel
              </h4>
              <p class="font-geist text-base font-semibold text-black leading-[24px]">
                3.200.000 đ
              </p>
            </div>
          </div>

          <!-- View All Button -->
          <div class="absolute left-1/2 top-[293.15px] transform -translate-x-1/2">
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
// No imports needed - LayoutProvider provides Navbar and Footer
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Gelasio:wght@400;500;600;700&family=Inter:wght@400;500;600;700&display=swap') layer(fonts);

.font-geist {
  font-family: 'Inter', 'Geist', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.font-gelasio {
  font-family: 'Gelasio', serif;
}

/* Marquee animation */
@keyframes marquee {
  0% {
    transform: translateX(0);
  }
  100% {
    transform: translateX(-50%);
  }
}

.animate-marquee {
  animation: marquee 30s linear infinite;
}
</style>
