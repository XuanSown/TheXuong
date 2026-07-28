<template>
  <div
    ref="rootRef"
    class="relative min-h-screen bg-transparent text-[#1A1C1C]"
  >
    <!-- HERO -->
    <section
      ref="heroRef"
      class="relative w-full overflow-hidden bg-black text-white flex flex-col items-center justify-center"
      :style="{ height: '60dvh' }"
      @mousemove="onHeroMove"
    >
      <div class="absolute inset-0 z-0 hero-glow pointer-events-none" />
      <div class="absolute inset-0 z-0 hero-aurora pointer-events-none" />
      <div class="relative z-10 flex flex-col items-center text-center px-5 max-w-[900px]">
        <p
          class="font-geist text-[12px] uppercase tracking-[1.8px] text-white/60 mb-6 hero-anim hero-fade"
          style="animation-delay:.1s"
        >
          CHÍNH SÁCH
        </p>
        <h1 class="font-geist font-bold text-white leading-[0.95] mb-6">
          <span
            class="block text-5xl sm:text-7xl md:text-8xl hero-anim hero-reveal"
            style="letter-spacing:-1.28px;animation-delay:.25s"
          >VẬN CHUYỂN</span>
        </h1>
        <p
          class="font-geist text-base sm:text-lg text-white/70 leading-[29px] max-w-[640px] mx-auto hero-anim hero-fade"
          style="animation-delay:.5s"
        >
          Giao hàng toàn quốc — nhanh chóng, an toàn và minh bạch từ kho đến tay bạn.
        </p>
      </div>
      <div
        class="absolute bottom-6 left-1/2 -translate-x-1/2 z-10 hero-anim hero-fade"
        style="animation-delay:.8s"
      >
        <span class="block w-px h-10 bg-white/40 scroll-indicator" />
      </div>
    </section>

    <!-- OVERVIEW -->
    <section class="w-full max-w-[1280px] mx-auto px-4 py-24 md:py-32">
      <div class="grid md:grid-cols-2 gap-12 md:gap-20">
        <div
          class="md:sticky md:top-28 self-start"
          data-reveal
        >
          <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
            TỔNG QUAN
          </p>
          <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
            Chính sách vận chuyển
          </h2>
        </div>
        <div
          class="relative"
          data-reveal
        >
          <div class="liquid-glass relative rounded-2xl border border-white/60 bg-white/50 p-8 md:p-10 shadow-[0_8px_32px_rgba(26,28,28,0.12)]">
            <span class="glass-sheen pointer-events-none" />
            <p class="font-geist text-base text-[#1A1C1C] leading-[29px] mb-5">
              THE XUONG SPORT hợp tác với các đơn vị vận chuyển uy tín để đảm bảo đơn hàng được giao đến bạn nhanh nhất và an toàn nhất.
            </p>
            <p class="font-geist text-base text-[#5E5F5C] leading-[29px] mb-5">
              Hỗ trợ giao hàng toàn quốc với nhiều lựa chọn tốc độ. Miễn phí vận chuyển cho đơn hàng từ <strong>500.000đ</strong>.
            </p>
            <p class="font-geist text-base text-[#5E5F5C] leading-[29px]">
              Mọi đơn hàng đều được đóng gói cẩn thận và có mã theo dõi để bạn dễ dàng kiểm tra hành trình.
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- SHIPPING OPTIONS -->
    <section
      ref="optionsRef"
      class="w-full max-w-[1100px] mx-auto px-4 py-16 md:py-24"
    >
      <div
        class="text-center mb-12 md:mb-16"
        data-reveal
      >
        <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
          LỰA CHỌN
        </p>
        <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
          Hình thức giao hàng
        </h2>
      </div>
      <div class="relative">
        <div
          v-for="(opt, i) in shippingOptions"
          :key="i"
          data-stack-card
          data-reveal
          class="stack-card liquid-glass relative bg-white/55 border border-white/60 rounded-2xl p-8 md:p-12 shadow-[0_8px_32px_rgba(26,28,28,0.10)]"
          :style="{ zIndex: i }"
        >
          <span class="glass-sheen pointer-events-none" />
          <div class="flex items-start gap-6 md:gap-10">
            <span class="font-jetbrains text-[14px] text-[#5E5F5C] pt-2">0{{ i + 1 }}</span>
            <div class="flex-1">
              <h3 class="font-geist text-[28px] md:text-[32px] leading-[38px] tracking-[-0.32px] text-[#1A1C1C] mb-4">
                {{ opt.title }}
              </h3>
              <p class="font-geist text-base text-[#5E5F5C] leading-[26px] max-w-[640px] mb-4">
                {{ opt.desc }}
              </p>
              <div class="flex flex-wrap gap-2">
                <span
                  v-for="(tag, j) in opt.tags"
                  :key="j"
                  class="font-geist text-[11px] uppercase tracking-[1px] px-3 py-1.5 rounded-full bg-black/5 text-[#5E5F5C]"
                >{{ tag }}</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- DELIVERY TIME & FEE TABLE -->
    <section class="w-full max-w-[1280px] mx-auto px-4 py-24 md:py-32">
      <div
        class="text-center mb-12 md:mb-16"
        data-reveal
      >
        <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
          BIỂU PHÍ
        </p>
        <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
          Thời gian & phí giao hàng
        </h2>
      </div>
      <div
        class="liquid-glass relative rounded-2xl border border-white/60 bg-white/50 p-6 md:p-10 shadow-[0_8px_32px_rgba(26,28,28,0.12)]"
        data-reveal
      >
        <span class="glass-sheen pointer-events-none" />
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead>
              <tr class="border-b border-[#CFC4C6]">
                <th class="font-geist text-[13px] uppercase tracking-[1.2px] text-[#5E5F5C] py-4 px-3 text-left">
                  Khu vực
                </th>
                <th class="font-geist text-[13px] uppercase tracking-[1.2px] text-[#5E5F5C] py-4 px-3 text-left">
                  Thời gian
                </th>
                <th class="font-geist text-[13px] uppercase tracking-[1.2px] text-[#5E5F5C] py-4 px-3 text-left">
                  Phí tiêu chuẩn
                </th>
                <th class="font-geist text-[13px] uppercase tracking-[1.2px] text-[#5E5F5C] py-4 px-3 text-left">
                  Giao nhanh
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(row, i) in deliveryTable"
                :key="i"
                class="border-b border-[#CFC4C6]/50 hover:bg-white/40 transition-colors"
              >
                <td class="font-geist py-4 px-3 text-[15px] font-semibold text-[#1A1C1C]">
                  {{ row.region }}
                </td>
                <td class="font-geist py-4 px-3 text-sm text-[#5E5F5C]">
                  {{ row.time }}
                </td>
                <td class="font-geist py-4 px-3 text-sm text-[#5E5F5C]">
                  {{ row.fee }}
                </td>
                <td class="font-geist py-4 px-3 text-sm text-[#5E5F5C]">
                  {{ row.express }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>

    <!-- FEATURES BENTO -->
    <section class="w-full max-w-[1280px] mx-auto px-4 py-16 md:py-24">
      <div
        class="text-center mb-12 md:mb-16"
        data-reveal
      >
        <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
          CAM KẾT
        </p>
        <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
          Đóng gói & giao nhận
        </h2>
      </div>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
        <div
          v-for="(f, i) in features"
          :key="i"
          data-reveal
          class="liquid-glass bg-white/55 border border-white/60 rounded-2xl p-8 md:p-10 shadow-[0_8px_32px_rgba(26,28,28,0.08)] hover:bg-white/75 hover:-translate-y-1 transition-all duration-500"
        >
          <span class="glass-sheen pointer-events-none" />
          <div class="w-10 h-10 rounded-full bg-black/5 flex items-center justify-center mb-5">
            <span class="text-lg">{{ f.icon }}</span>
          </div>
          <h3 class="font-geist text-[22px] leading-[28px] tracking-[-0.22px] text-[#1A1C1C] mb-3">
            {{ f.title }}
          </h3>
          <p class="font-geist text-sm text-[#5E5F5C] leading-[24px]">
            {{ f.desc }}
          </p>
        </div>
      </div>
    </section>

    <!-- NOTES / EXCLUSIONS -->
    <section class="w-full relative bg-black text-white py-24 md:py-32 overflow-hidden">
      <div class="absolute inset-0 hero-glow pointer-events-none opacity-60" />
      <div class="relative max-w-[1280px] mx-auto px-4">
        <div
          class="text-center mb-12 md:mb-16"
          data-reveal
        >
          <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-white/60 mb-4">
            LƯU Ý
          </p>
          <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] text-white">
            Điều cần biết
          </h2>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
          <div
            v-for="(n, i) in notes"
            :key="i"
            data-reveal
            class="liquid-glass relative bg-white/10 border border-white/20 rounded-2xl p-8 hover:bg-white/20 hover:-translate-y-1 transition-all duration-500"
          >
            <span class="glass-sheen pointer-events-none" />
            <h3 class="font-geist text-[20px] leading-[26px] text-white mb-3">
              {{ n.title }}
            </h3>
            <p class="font-geist text-sm text-white/70 leading-[24px]">
              {{ n.desc }}
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- TRACKING -->
    <section class="w-full max-w-[1280px] mx-auto px-4 py-24 md:py-32">
      <div class="grid md:grid-cols-2 gap-12 md:gap-20">
        <div
          class="md:sticky md:top-28 self-start"
          data-reveal
        >
          <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
            THEO DÕI
          </p>
          <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
            Kiểm tra hành trình
          </h2>
        </div>
        <div
          class="relative"
          data-reveal
        >
          <div class="liquid-glass relative rounded-2xl border border-white/60 bg-white/50 p-8 md:p-10 shadow-[0_8px_32px_rgba(26,28,28,0.12)]">
            <span class="glass-sheen pointer-events-none" />
            <p class="font-geist text-base text-[#1A1C1C] leading-[29px] mb-5">
              Sau khi đơn hàng được giao cho đơn vị vận chuyển, bạn sẽ nhận được email và thông báo với <strong>mã vận đơn</strong> để theo dõi hành trình.
            </p>
            <p class="font-geist text-base text-[#5E5F5C] leading-[29px] mb-5">
              Đăng nhập tài khoản và vào mục "Đơn hàng" để xem trạng thái giao hàng real-time, vị trí kiện hàng và thời gian dự kiến nhận.
            </p>
            <p class="font-geist text-base text-[#5E5F5C] leading-[29px]">
              Nếu đơn hàng không cập nhật trạng thái trong 48 giờ, vui lòng liên hệ hỗ trợ để được kiểm tra.
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA -->
    <section class="w-full relative bg-black text-white py-28 md:py-40 overflow-hidden">
      <div class="absolute inset-0 hero-glow pointer-events-none opacity-70" />
      <div
        class="relative max-w-[900px] mx-auto px-4 text-center"
        data-reveal
      >
        <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] text-white mb-8">
          Cần hỗ trợ vận chuyển?
        </h2>
        <p class="font-geist text-base text-white/70 leading-[29px] mb-10 max-w-[560px] mx-auto">
          Đội ngũ chăm sóc khách hàng sẵn sàng hỗ trợ bạn theo dõi và xử lý mọi vấn đề giao hàng.
        </p>
        <div class="flex flex-col sm:flex-row gap-4 justify-center">
          <router-link
            to="/about"
            class="liquid-btn px-10 py-5 bg-white/10 text-white text-[12px] font-semibold uppercase tracking-[1.8px] leading-[12px] rounded-full hover:bg-white/20 transition-colors"
          >
            LIÊN HỆ NGAY
          </router-link>
          <router-link
            to="/orders"
            class="liquid-btn px-10 py-5 bg-white/10 text-white text-[12px] font-semibold uppercase tracking-[1.8px] leading-[12px] rounded-full hover:bg-white/20 transition-colors"
          >
            XEM ĐƠN HÀNG
          </router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { useReveal } from '@/composables/useReveal'
import { useScrollReveal } from '@/composables/useScrollReveal'

const rootRef = ref<HTMLElement | null>(null)
const heroRef = ref<HTMLElement | null>(null)
const optionsRef = ref<HTMLElement | null>(null)
let glowRaf: number | null = null

const onHeroMove = (e: MouseEvent) => {
  if (glowRaf) return
  glowRaf = requestAnimationFrame(() => {
    const hero = heroRef.value
    if (hero) {
      const rect = hero.getBoundingClientRect()
      hero.style.setProperty('--mx', (e.clientX - rect.left) + 'px')
      hero.style.setProperty('--my', (e.clientY - rect.top) + 'px')
    }
    glowRaf = null
  })
}

useReveal(rootRef)
useScrollReveal(optionsRef)

onUnmounted(() => {
  if (glowRaf) cancelAnimationFrame(glowRaf)
})

const shippingOptions = [
  {
    title: 'Giao hàng tiêu chuẩn',
    desc: 'Phương thức phổ biến với chi phí hợp lý. Phù hợp cho đơn hàng không cần gấp, giao trong thời gian dự kiến.',
    tags: ['Toàn quốc', 'Tiết kiệm', '2-5 ngày']
  },
  {
    title: 'Giao hàng nhanh',
    desc: 'Ưu tiên xử lý và vận chuyển nhanh hơn. Phù hợp khi cần nhận hàng sớm, đặc biệt trong nội thành.',
    tags: ['Ưu tiên', 'Nội thành', '1-2 ngày']
  },
  {
    title: 'Giao hàng hỏa tốc',
    desc: 'Giao trong ngày cho khu vực nội thành TP.HCM và Hà Nội. Nhận hàng chỉ sau vài giờ đặt đơn.',
    tags: ['Trong ngày', 'HCM & HN', '2-4 giờ']
  },
  {
    title: 'Giao hàng COD',
    desc: 'Thanh toán khi nhận hàng. Kiểm tra sản phẩm trước khi thanh toán cho nhân viên giao hàng.',
    tags: ['Thanh toán khi nhận', 'Kiểm tra hàng', 'Toàn quốc']
  }
]

const deliveryTable = [
  { region: 'Nội thành (HCM, Hà Nội)', time: '1-2 ngày', fee: '25.000đ', express: '45.000đ (trong ngày)' },
  { region: 'Ngoại thành', time: '2-3 ngày', fee: '30.000đ', express: '55.000đ (1 ngày)' },
  { region: 'Tỉnh / Thành phố khác', time: '3-5 ngày', fee: '35.000đ', express: '65.000đ (2-3 ngày)' },
  { region: 'Vùng sâu, vùng xa', time: '5-7 ngày', fee: '45.000đ', express: 'Không hỗ trợ' },
  { region: 'Đơn từ 500.000đ', time: 'Theo khu vực', fee: 'Miễn phí', express: 'Giảm 50%' }
]

const features = [
  {
    icon: '📦',
    title: 'Đóng gói cẩn thận',
    desc: 'Mọi sản phẩm được đóng gói chống sốc, bảo vệ nguyên vẹn từ kho đến tay khách hàng.'
  },
  {
    icon: '🔍',
    title: 'Mã vận đơn theo dõi',
    desc: 'Nhận mã tracking ngay sau khi đơn hàng được giao cho đối tác vận chuyển.'
  },
  {
    icon: '📍',
    title: 'Cập nhật real-time',
    desc: 'Theo dõi vị trí và trạng thái đơn hàng trực tiếp trên website hoặc ứng dụng.'
  },
  {
    icon: '📞',
    title: 'Liên hệ trước khi giao',
    desc: 'Nhân viên giao hàng sẽ gọi xác nhận trước khi đến địa chỉ giao hàng.'
  },
  {
    icon: '🔄',
    title: 'Giao lại miễn phí',
    desc: 'Nếu vắng nhà, đơn hàng được giao lại tối đa 3 lần hoàn toàn miễn phí.'
  },
  {
    icon: '✅',
    title: 'Kiểm tra khi nhận',
    desc: 'Với COD, bạn được kiểm tra sản phẩm trước khi thanh toán cho shipper.'
  }
]

const notes = [
  {
    title: 'Thời gian xử lý đơn',
    desc: 'Đơn đặt trước 15h được xử lý và giao cho vận chuyển trong ngày. Đơn sau 15h được xử lý vào ngày hôm sau.'
  },
  {
    title: 'Thay đổi địa chỉ giao hàng',
    desc: 'Có thể thay đổi địa chỉ trước khi đơn được giao cho vận chuyển. Sau đó, vui lòng liên hệ hỗ trợ.'
  },
  {
    title: 'Đơn hàng bị thất lạc',
    desc: 'Nếu đơn không nhận được sau 7 ngày so với thời gian dự kiến, liên hệ ngay để được xử lý và đền bù.'
  },
  {
    title: 'Hàng dễ vỡ & cồng kềnh',
    desc: 'Một số sản phẩm đặc biệt có thể áp dụng phí đóng gói bổ sung. Thông tin được hiển thị rõ khi đặt hàng.'
  }
]
</script>

<style scoped>
/* ---- Liquid glass ---- */
.liquid-glass {
  -webkit-backdrop-filter: blur(18px) saturate(160%);
  backdrop-filter: blur(18px) saturate(160%);
  background-image: linear-gradient(135deg, rgba(255, 255, 255, 0.55), rgba(255, 255, 255, 0.18));
  position: relative;
  overflow: hidden;
}

.glass-sheen {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.55) 0%, transparent 42%),
    radial-gradient(120% 80% at 50% -20%, rgba(255, 255, 255, 0.35), transparent 60%);
  pointer-events: none;
  opacity: 0.9;
}

.liquid-btn {
  -webkit-backdrop-filter: blur(12px) saturate(160%);
  backdrop-filter: blur(12px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 4px 24px rgba(255, 255, 255, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.25);
}

/* ---- Aurora on hero ---- */
.hero-aurora {
  background:
    radial-gradient(40% 60% at 20% 20%, rgba(255, 107, 53, 0.18), transparent 70%),
    radial-gradient(45% 55% at 80% 30%, rgba(14, 165, 233, 0.14), transparent 70%),
    radial-gradient(50% 60% at 50% 90%, rgba(120, 255, 220, 0.12), transparent 70%);
  filter: blur(20px);
  animation: auroraDrift 14s ease-in-out infinite alternate;
  transform: translate3d(0, 0, 0);
}

@keyframes auroraDrift {
  0% { transform: translate3d(-3%, -2%, 0) scale(1.05); }
  100% { transform: translate3d(3%, 2%, 0) scale(1.12); }
}

/* ---- Scroll reveal ---- */
[data-reveal] {
  opacity: 0;
  transform: translateY(34px);
  filter: blur(8px);
  transition: opacity 0.9s cubic-bezier(0.16, 1, 0.3, 1),
    transform 0.9s cubic-bezier(0.16, 1, 0.3, 1),
    filter 0.9s cubic-bezier(0.16, 1, 0.3, 1);
  will-change: opacity, transform, filter;
}

[data-reveal].is-revealed {
  opacity: 1;
  transform: translateY(0);
  filter: blur(0);
}

.hero-glow {
  background: radial-gradient(600px circle at var(--mx, 50%) var(--my, 50%), rgba(255, 255, 255, 0.12), transparent 60%);
}

.stack-card {
  position: sticky;
  top: 96px;
  margin-bottom: 24px;
}

@media (max-width: 768px) {
  .stack-card { top: 80px; }
}

@keyframes heroReveal {
  0% { opacity: 0; transform: translateY(28px); filter: blur(12px); }
  100% { opacity: 1; transform: translateY(0); filter: blur(0); }
}

@keyframes heroFadeUp {
  0% { opacity: 0; transform: translateY(20px); }
  100% { opacity: 1; transform: translateY(0); }
}

.hero-anim {
  opacity: 0;
  animation-fill-mode: forwards;
  animation-timing-function: cubic-bezier(0.16, 1, 0.3, 1);
}

.hero-reveal { animation-name: heroReveal; animation-duration: 1.1s; }
.hero-fade { animation-name: heroFadeUp; animation-duration: 1s; }

@keyframes scrollIndicator {
  0% { transform: scaleY(0); transform-origin: top; }
  50% { transform: scaleY(1); transform-origin: top; }
  100% { transform: scaleY(0); transform-origin: bottom; }
}

.scroll-indicator { animation: scrollIndicator 2s ease-in-out infinite; }

@media (prefers-reduced-motion: reduce) {
  .hero-anim,
  .scroll-indicator,
  .hero-aurora { animation: none; opacity: 1; }
  .hero-glow,
  .hero-aurora { display: none; }
  .stack-card { position: static; margin-bottom: 24px; }
  [data-reveal] { opacity: 1; transform: none; filter: none; transition: none; }
}
</style>
