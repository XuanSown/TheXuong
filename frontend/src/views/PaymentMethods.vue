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
          THANH TOÁN
        </p>
        <h1 class="font-geist font-bold text-white leading-[0.95] mb-6">
          <span
            class="block text-5xl sm:text-7xl md:text-8xl hero-anim hero-reveal"
            style="letter-spacing:-1.28px;animation-delay:.25s"
          >PHƯƠNG THỨC</span>
        </h1>
        <p
          class="font-geist text-base sm:text-lg text-white/70 leading-[29px] max-w-[640px] mx-auto hero-anim hero-fade"
          style="animation-delay:.5s"
        >
          Đa dạng phương thức thanh toán — an toàn, nhanh chóng và tiện lợi cho mọi đơn hàng.
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
            Thanh toán dễ dàng
          </h2>
        </div>
        <div
          class="relative"
          data-reveal
        >
          <div class="liquid-glass relative rounded-2xl border border-white/60 bg-white/50 p-8 md:p-10 shadow-[0_8px_32px_rgba(26,28,28,0.12)]">
            <span class="glass-sheen pointer-events-none" />
            <p class="font-geist text-base text-[#1A1C1C] leading-[29px] mb-5">
              THE XUONG SPORT hỗ trợ nhiều phương thức thanh toán phổ biến, giúp bạn dễ dàng hoàn tất đơn hàng một cách nhanh chóng và an toàn.
            </p>
            <p class="font-geist text-base text-[#5E5F5C] leading-[29px] mb-5">
              Tất cả giao dịch đều được mã hóa SSL/TLS và xử lý qua các cổng thanh toán uy tín, đạt chuẩn bảo mật quốc tế PCI DSS.
            </p>
            <p class="font-geist text-base text-[#5E5F5C] leading-[29px]">
              Mọi thắc mắc về thanh toán, vui lòng liên hệ đội ngũ hỗ trợ để được giải đáp nhanh nhất.
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- PAYMENT METHODS -->
    <section
      ref="methodsRef"
      class="w-full max-w-[1100px] mx-auto px-4 py-16 md:py-24"
    >
      <div
        class="text-center mb-12 md:mb-16"
        data-reveal
      >
        <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
          PHƯƠNG THỨC
        </p>
        <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
          Các hình thức thanh toán
        </h2>
      </div>
      <div class="relative">
        <div
          v-for="(method, i) in paymentMethods"
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
                {{ method.title }}
              </h3>
              <p class="font-geist text-base text-[#5E5F5C] leading-[26px] max-w-[640px] mb-4">
                {{ method.desc }}
              </p>
              <div class="flex flex-wrap gap-2">
                <span
                  v-for="(tag, j) in method.tags"
                  :key="j"
                  class="font-geist text-[11px] uppercase tracking-[1px] px-3 py-1.5 rounded-full bg-black/5 text-[#5E5F5C]"
                >{{ tag }}</span>
              </div>
            </div>
          </div>
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
          ƯU ĐIỂM
        </p>
        <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
          Tại sao chọn chúng tôi
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

    <!-- SECURITY -->
    <section class="w-full relative bg-black text-white py-24 md:py-32 overflow-hidden">
      <div class="absolute inset-0 hero-glow pointer-events-none opacity-60" />
      <div class="relative max-w-[1280px] mx-auto px-4">
        <div
          class="text-center mb-12 md:mb-16"
          data-reveal
        >
          <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-white/60 mb-4">
            AN TOÀN
          </p>
          <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] text-white">
            Bảo mật thanh toán
          </h2>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
          <div
            v-for="(s, i) in security"
            :key="i"
            data-reveal
            class="liquid-glass relative bg-white/10 border border-white/20 rounded-2xl p-8 hover:bg-white/20 hover:-translate-y-1 transition-all duration-500"
          >
            <span class="glass-sheen pointer-events-none" />
            <h3 class="font-geist text-[20px] leading-[26px] text-white mb-3">
              {{ s.title }}
            </h3>
            <p class="font-geist text-sm text-white/70 leading-[24px]">
              {{ s.desc }}
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- FAQ -->
    <section class="w-full max-w-[1280px] mx-auto px-4 py-24 md:py-32">
      <div
        class="text-center mb-12 md:mb-16"
        data-reveal
      >
        <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
          HỎI ĐÁP
        </p>
        <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
          Câu hỏi thường gặp
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
                  Câu hỏi
                </th>
                <th class="font-geist text-[13px] uppercase tracking-[1.2px] text-[#5E5F5C] py-4 px-3 text-left">
                  Trả lời
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(q, i) in faqs"
                :key="i"
                class="border-b border-[#CFC4C6]/50 hover:bg-white/40 transition-colors"
              >
                <td class="font-geist py-4 px-3 text-[15px] font-semibold text-[#1A1C1C]">
                  {{ q.question }}
                </td>
                <td class="font-geist py-4 px-3 text-sm text-[#5E5F5C]">
                  {{ q.answer }}
                </td>
              </tr>
            </tbody>
          </table>
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
          Sẵn sàng mua sắm?
        </h2>
        <p class="font-geist text-base text-white/70 leading-[29px] mb-10 max-w-[560px] mx-auto">
          Khám phá bộ sưu tập đồ thể thao chính hãng và thanh toán dễ dàng với phương thức yêu thích của bạn.
        </p>
        <div class="flex flex-col sm:flex-row gap-4 justify-center">
          <router-link
            to="/products"
            class="liquid-btn px-10 py-5 bg-white/10 text-white text-[12px] font-semibold uppercase tracking-[1.8px] leading-[12px] rounded-full hover:bg-white/20 transition-colors"
          >
            MUA SẮM NGAY
          </router-link>
          <router-link
            to="/about"
            class="liquid-btn px-10 py-5 bg-white/10 text-white text-[12px] font-semibold uppercase tracking-[1.8px] leading-[12px] rounded-full hover:bg-white/20 transition-colors"
          >
            LIÊN HỆ HỖ TRỢ
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
const methodsRef = ref<HTMLElement | null>(null)
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
useScrollReveal(methodsRef)

onUnmounted(() => {
  if (glowRaf) cancelAnimationFrame(glowRaf)
})

const paymentMethods = [
  {
    title: 'Thanh toán khi nhận hàng (COD)',
    desc: 'Thanh toán tiền mặt trực tiếp cho nhân viên giao hàng khi nhận sản phẩm. Kiểm tra hàng trước khi thanh toán, an tâm tuyệt đối.',
    tags: ['Tiền mặt', 'Kiểm tra hàng', 'Toàn quốc']
  },
  {
    title: 'Ví điện tử',
    desc: 'Thanh toán nhanh chóng qua MoMo, ZaloPay, ShopeePay. Quét mã QR hoặc xác nhận trong ứng dụng, giao dịch hoàn tất trong vài giây.',
    tags: ['MoMo', 'ZaloPay', 'ShopeePay', 'QR Code']
  },
  {
    title: 'Thẻ ngân hàng nội địa',
    desc: 'Thanh toán qua thẻ ATM của các ngân hàng Việt Nam thông qua cổng thanh toán Napas. Hỗ trợ hơn 40 ngân hàng trong nước.',
    tags: ['Napas', 'ATM', '40+ ngân hàng']
  },
  {
    title: 'Thẻ quốc tế',
    desc: 'Chấp nhận thẻ Visa, Mastercard, JCB. Giao dịch được mã hóa SSL và xác thực 3D-Secure để bảo vệ thông tin thẻ.',
    tags: ['Visa', 'Mastercard', 'JCB', '3D-Secure']
  },
  {
    title: 'Chuyển khoản ngân hàng',
    desc: 'Chuyển khoản trực tiếp qua Internet Banking hoặc tại quầy. Hệ thống tự động xác nhận khi nhận được thanh toán.',
    tags: ['Internet Banking', 'QR chuyển khoản', 'Tự động']
  }
]

const features = [
  {
    icon: '⚡',
    title: 'Xử lý tức thì',
    desc: 'Giao dịch được xác nhận trong vài giây, đơn hàng được xử lý ngay sau khi thanh toán thành công.'
  },
  {
    icon: '🔒',
    title: 'Bảo mật tuyệt đối',
    desc: 'Mã hóa SSL/TLS, cổng thanh toán đạt chuẩn PCI DSS, không lưu trữ thông tin thẻ.'
  },
  {
    icon: '🌍',
    title: 'Đa dạng lựa chọn',
    desc: 'Từ tiền mặt đến ví điện tử, thẻ ngân hàng — phù hợp với mọi nhu cầu.'
  },
  {
    icon: '📱',
    title: 'Thanh toán di động',
    desc: 'Quét mã QR, xác nhận vân tay, thanh toán một chạm trên điện thoại.'
  },
  {
    icon: '✅',
    title: 'Kiểm tra trước khi trả',
    desc: 'Với COD, bạn có thể kiểm tra sản phẩm trước khi thanh toán cho shipper.'
  },
  {
    icon: '💬',
    title: 'Hỗ trợ 24/7',
    desc: 'Đội ngũ hỗ trợ sẵn sàng giải đáp mọi thắc mắc về thanh toán bất cứ lúc nào.'
  }
]

const security = [
  {
    title: 'Mã hóa SSL/TLS 256-bit',
    desc: 'Mọi dữ liệu thanh toán được truyền tải qua kết nối mã hóa, ngăn chặn đánh cắp thông tin.'
  },
  {
    title: 'Cổng thanh toán PCI DSS',
    desc: 'Hợp tác với các cổng thanh toán đạt chuẩn bảo mật quốc tế, đảm bảo an toàn giao dịch.'
  },
  {
    title: 'Xác thực 3D-Secure',
    desc: 'Lớp bảo mật bổ sung cho thẻ quốc tế, yêu cầu xác nhận từ ngân hàng phát hành.'
  },
  {
    title: 'Không lưu trữ thông tin thẻ',
    desc: 'Thông tin thẻ chỉ được xử lý bởi cổng thanh toán, không lưu trên hệ thống của chúng tôi.'
  }
]

const faqs = [
  { question: 'Thanh toán thất bại phải làm sao?', answer: 'Kiểm tra số dư, hạn mức thẻ hoặc thử phương thức khác. Liên hệ hỗ trợ nếu vẫn gặp lỗi.' },
  { question: 'COD có mất phí không?', answer: 'Không phát sinh phụ phí. Bạn chỉ thanh toán đúng giá trị đơn hàng khi nhận sản phẩm.' },
  { question: 'Ví điện tử nào được hỗ trợ?', answer: 'Hiện hỗ trợ MoMo, ZaloPay và ShopeePay. Các ví khác sẽ được cập nhật trong tương lai.' },
  { question: 'Thẻ quốc tế có an toàn không?', answer: 'Có. Giao dịch được bảo vệ bởi 3D-Secure và không lưu thông tin thẻ trên hệ thống.' },
  { question: 'Chuyển khoản bao lâu xác nhận?', answer: 'Thông thường trong 5-15 phút. Nếu quá 1 giờ chưa nhận được xác nhận, vui lòng liên hệ hỗ trợ.' },
  { question: 'Có trả góp không?', answer: 'Hiện chưa hỗ trợ trả góp. Tính năng sẽ được triển khai trong thời gian tới.' }
]
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Geist:wght@300;400;500;600;700&family=JetBrains+Mono:ital,wght@0,400;0,500;0,600;1,400;1,500;1,600&display=swap') layer(fonts);

.font-geist {
  font-family: 'Geist', sans-serif;
}

.font-jetbrains {
  font-family: 'JetBrains Mono', monospace;
}

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
