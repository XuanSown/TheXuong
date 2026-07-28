<template>
  <div
    ref="rootRef"
    class="relative min-h-screen bg-transparent text-[#1A1C1C]"
  >
    <!-- Spiral Vortex (silver pearl) background -->
    <canvas
      id="bg-light"
      ref="vortexRef"
      class="fixed inset-0 w-full h-full -z-20 bg-[#F4F5F7] transition-opacity duration-500"
      :style="{ opacity: vortexOpacity }"
      aria-hidden="true"
    />
    <div
      class="fixed inset-0 -z-10 bg-[rgba(249,249,249,0.16)] pointer-events-none transition-opacity duration-500"
      :style="{ opacity: vortexOpacity }"
    />
    <!-- 1. HERO -->
    <section
      ref="heroRef"
      class="relative w-full overflow-hidden bg-black text-white flex flex-col items-center justify-center"
      :style="{ height: '90dvh' }"
      @mousemove="onHeroMove"
    >
      <div class="absolute inset-0 z-0 hero-glow pointer-events-none" />
      <div
        class="absolute inset-0 z-0 hero-aurora pointer-events-none"
      />
      <div class="relative z-10 flex flex-col items-center text-center px-5 max-w-[900px]">
        <p
          class="font-geist text-[12px] uppercase tracking-[1.8px] text-white/60 mb-6 hero-anim hero-fade"
          style="animation-delay:.1s"
        >
          VỀ CHÚNG TÔI
        </p>
        <h1 class="font-lobster font-normal text-white leading-[0.95] mb-6">
          <span
            class="block font-lobster text-5xl sm:text-7xl md:text-8xl hero-anim hero-reveal"
            style="letter-spacing:0;animation-delay:.25s"
          >THE XUONG</span>
          <span
            class="block font-lobster text-5xl sm:text-7xl md:text-8xl -mt-1 hero-anim hero-reveal"
            style="letter-spacing:0;animation-delay:.42s"
          >SPORT</span>
        </h1>
        <p
          class="font-geist text-base sm:text-lg text-white/70 leading-[29px] max-w-[640px] mx-auto mb-10 hero-anim hero-fade"
          style="animation-delay:.6s"
        >
          Sàn thương mại điện tử đồ thể thao chính hãng — đồng hành cùng vận động viên và người yêu thể thao Việt Nam.
        </p>
        <router-link
          to="/products"
          class="liquid-btn px-10 py-5 bg-white/10 text-white text-[12px] font-semibold uppercase tracking-[1.8px] leading-[12px] rounded-full hover:bg-white/20 transition-colors hero-anim hero-fade"
          style="animation-delay:.75s"
        >
          KHÁM PHÁ SẢN PHẨM
        </router-link>
      </div>
      <div
        class="absolute bottom-6 left-1/2 -translate-x-1/2 z-10 hero-anim hero-fade"
        style="animation-delay:1s"
      >
        <span class="block w-px h-10 bg-white/40 scroll-indicator" />
      </div>
    </section>

    <!-- 2. BRAND MARQUEE -->
    <BrandMarquee />

    <!-- 3. STORY / MISSION -->
    <section class="w-full max-w-[1280px] mx-auto px-4 py-24 md:py-32">
      <div class="grid md:grid-cols-2 gap-12 md:gap-20">
        <div
          class="md:sticky md:top-28 self-start"
          data-reveal
        >
          <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
            SỨ MỆNH
          </p>
          <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
            Câu chuyện của chúng tôi
          </h2>
        </div>
        <div
          class="relative"
          data-reveal
        >
          <div class="liquid-glass relative rounded-2xl border border-white/60 bg-white/50 p-8 md:p-10 shadow-[0_8px_32px_rgba(26,28,28,0.12)]">
            <span class="glass-sheen pointer-events-none" />
            <p class="font-geist text-base text-[#1A1C1C] leading-[29px] mb-5">
              THE XUONG SPORT ra đời với mong muốn biến việc sở hữu đồ thể thao chính hãng trở nên dễ dàng và đáng tin cậy đối với mọi vận động viên và người yêu thể thao tại Việt Nam.
            </p>
            <p class="font-geist text-base text-[#5E5F5C] leading-[29px] mb-5">
              Chúng tôi tin rằng thiết bị phù hợp không chỉ nâng cao thành tích mà còn nuôi dưỡng đam mê. Mỗi sản phẩm trên sàn đều được tuyển chọn kỹ lưỡng từ những thương hiệu hàng đầu thế giới.
            </p>
            <p class="font-geist text-base text-[#5E5F5C] leading-[29px]">
              Tầm nhìn của chúng tôi là trở thành điểm đến uy tín nhất cho đồ thể thao chính hãng, nơi niềm tin của khách hàng được đặt lên hàng đầu.
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- 4. SCROLL-STACKED COMMITMENT CARDS -->
    <section
      ref="stackRef"
      class="w-full max-w-[1100px] mx-auto px-4 py-16 md:py-24"
    >
      <div
        class="text-center mb-12 md:mb-16"
        data-reveal
      >
        <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
          CAM KẾT
        </p>
        <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
          Bốn trụ cột của chúng tôi
        </h2>
      </div>
      <div class="relative">
        <div
          v-for="(card, i) in commitments"
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
                {{ card.title }}
              </h3>
              <p class="font-geist text-base text-[#5E5F5C] leading-[26px] max-w-[640px]">
                {{ card.copy }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- 5. CORE VALUES BENTO -->
    <section class="w-full max-w-[1280px] mx-auto px-4 py-16 md:py-24">
      <div
        class="text-center mb-12 md:mb-16"
        data-reveal
      >
        <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
          GIÁ TRỊ CỐT LÕI
        </p>
        <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
          Điều chúng tôi tin
        </h2>
      </div>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
        <div
          v-for="(v, i) in values"
          :key="i"
          data-reveal
          class="liquid-glass bg-white/55 border border-white/60 rounded-2xl p-8 md:p-10 shadow-[0_8px_32px_rgba(26,28,28,0.08)] hover:bg-white/75 hover:-translate-y-1 transition-all duration-500"
        >
          <span class="glass-sheen pointer-events-none" />
          <h3 class="font-geist text-[22px] leading-[28px] tracking-[-0.22px] text-[#1A1C1C] mb-3">
            {{ v.title }}
          </h3>
          <p class="font-geist text-sm text-[#5E5F5C] leading-[24px]">
            {{ v.copy }}
          </p>
        </div>
      </div>
    </section>

    <!-- 6. STATS COUNT-UP -->
    <section class="w-full relative bg-black text-white py-24 md:py-32 overflow-hidden">
      <div class="absolute inset-0 hero-glow pointer-events-none opacity-60" />
      <div class="relative max-w-[1280px] mx-auto px-4 grid grid-cols-2 lg:grid-cols-4 gap-10 md:gap-16">
        <StatCard
          v-for="(s, i) in stats"
          :key="i"
          :target="s.value"
          :suffix="s.suffix"
          :label="s.label"
        />
      </div>
    </section>

    <!-- 7. TESTIMONIALS -->
    <section class="w-full max-w-[1280px] mx-auto px-4 py-24 md:py-32">
      <div
        class="text-center mb-12 md:mb-16"
        data-reveal
      >
        <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
          KHÁCH HÀNG
        </p>
        <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
          Khách hàng nói gì
        </h2>
      </div>
      <div class="grid grid-cols-1 md:grid-cols-3 gap-5">
        <figure
          v-for="(t, i) in testimonials"
          :key="i"
          data-reveal
          class="liquid-glass relative bg-white/55 border border-white/60 rounded-2xl p-8 md:p-10 shadow-[0_8px_32px_rgba(26,28,28,0.08)] hover:bg-white/75 hover:-translate-y-1 transition-all duration-500 flex flex-col"
        >
          <span class="glass-sheen pointer-events-none" />
          <span class="font-jetbrains italic text-[64px] leading-[1] text-[#1A1C1C] opacity-20 mb-2">&ldquo;</span>
          <blockquote class="font-geist text-base text-[#1A1C1C] leading-[26px] mb-8 flex-1">
            {{ t.quote }}
          </blockquote>
          <figcaption class="border-t border-[#CFC4C6] pt-5">
            <p class="font-geist text-[15px] text-[#1A1C1C] mb-1">
              {{ t.name }}
            </p>
            <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C]">
              {{ t.role }}
            </p>
          </figcaption>
        </figure>
      </div>
    </section>

    <!-- 8. FINAL CTA -->
    <section class="w-full relative bg-black text-white py-28 md:py-40 overflow-hidden">
      <div class="absolute inset-0 hero-glow pointer-events-none opacity-70" />
      <div
        class="relative max-w-[900px] mx-auto px-4 text-center"
        data-reveal
      >
        <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] text-white mb-8">
          Sẵn sàng nâng tầm trải nghiệm thể thao?
        </h2>
        <router-link
          to="/products"
          class="liquid-btn px-10 py-5 bg-white/10 text-white text-[12px] font-semibold uppercase tracking-[1.8px] leading-[12px] rounded-full hover:bg-white/20 transition-colors"
        >
          KHÁM PHÁ SẢN PHẨM
        </router-link>
      </div>
    </section>
    <!-- Sentinel: fades vortex out when footer enters viewport -->
    <div
      ref="footerSentinel"
      class="h-px w-full"
      aria-hidden="true"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import BrandMarquee from '@/components/about/BrandMarquee.vue'
import StatCard from '@/components/about/StatCard.vue'
import { useScrollReveal } from '@/composables/useScrollReveal'
import { useReveal } from '@/composables/useReveal'
import { useSpiralVortex } from '@/composables/useSpiralVortex'

const rootRef = ref<HTMLElement | null>(null)
const heroRef = ref<HTMLElement | null>(null)
const vortexRef = ref<HTMLCanvasElement | null>(null)
const footerSentinel = ref<HTMLElement | null>(null)
const vortexOpacity = ref(1)
let glowRaf: number | null = null
let footerObserver: IntersectionObserver | null = null

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

const stackRef = ref<HTMLElement | null>(null)
useScrollReveal(stackRef)
useReveal(rootRef)
useSpiralVortex(vortexRef)

onMounted(() => {
  const sentinel = footerSentinel.value
  if (!sentinel || typeof IntersectionObserver === 'undefined') return
  footerObserver = new IntersectionObserver(
    (entries) => {
      for (const e of entries) {
        vortexOpacity.value = e.isIntersecting ? 0 : 1
      }
    },
    { rootMargin: '0px 0px 60% 0px' },
  )
  footerObserver.observe(sentinel)
})

onUnmounted(() => {
  footerObserver?.disconnect()
  if (glowRaf) cancelAnimationFrame(glowRaf)
})

const commitments = [
  { title: 'Chính hãng 100%', copy: 'Mọi sản phẩm đều có nguồn gốc rõ ràng từ NIKE, ADIDAS, LI-NING, PUMA. Phát hiện hàng giả, đền bù gấp 10 lần.' },
  { title: 'Đa dạng môn thể thao', copy: 'Bóng đá, cầu lông, chạy bộ, bóng rổ… đầy đủ dụng cụ và trang phục cho mọi nhu cầu vận động.' },
  { title: 'Dịch vụ khách hàng tận tâm', copy: 'Hỗ trợ nhanh chóng, đổi trả linh hoạt, giao hàng toàn quốc — luôn đồng hành cùng bạn.' },
  { title: 'Cam kết giá & chất lượng', copy: 'Giá cạnh tranh, chất lượng đảm bảo, chính sách bảo hành minh bạch và rõ ràng.' },
]

const values = [
  { title: 'Tín nhiệm', copy: 'Uy tín là nền tảng mọi hoạt động — giữ lời hứa với khách hàng.' },
  { title: 'Đam mê', copy: 'Yêu thể thao và truyền tải nhiệt huyết ấy vào từng sản phẩm.' },
  { title: 'Khách hàng là trọng tâm', copy: 'Mọi quyết định đều xuất phát từ lợi ích và trải nghiệm của khách hàng.' },
  { title: 'Đổi mới', copy: 'Liên tục cập nhật sản phẩm và dịch vụ để vượt mong đợi.' },
  { title: 'Trách nhiệm', copy: 'Minh bạch trong nguồn hàng, giá cả và chính sách bảo hành.' },
  { title: 'Cộng đồng', copy: 'Góp phần xây dựng cộng đồng thể thao Việt Nam phát triển.' },
]

const stats = [
  { value: 10, suffix: 'K+', label: 'Khách hàng' },
  { value: 500, suffix: '+', label: 'Sản phẩm' },
  { value: 4, suffix: '', label: 'Môn thể thao' },
  { value: 100, suffix: '%', label: 'Chính hãng' },
]

const testimonials = [
  { quote: 'Mua giày chạy bộ tại TheXuong lần đầu mà nhận đúng đồ chính hãng, đóng gói kỹ. Chắc chắn sẽ quay lại.', name: 'Minh Anh', role: 'Vận động viên chạy bộ' },
  { quote: 'Cần dụng cụ cầu lông gấp, shop tư vấn nhiệt tình, giao đúng hẹn. Dịch vụ tận tâm đúng như cam kết.', name: 'Hoàng Nam', role: 'Cầu thủ cầu lông' },
  { quote: 'Sàn đồ thể thao đáng tin cậy. Mỗi sản phẩm đều rõ nguồn gốc, giá cạnh tranh so với nơi khác.', name: 'Thu Hà', role: 'Người yêu thể thao' },
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

/* ponytail: faux top-light sheen, cheap stand-in for real refraction */
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
    radial-gradient(40% 60% at 20% 20%, rgba(120, 120, 255, 0.18), transparent 70%),
    radial-gradient(45% 55% at 80% 30%, rgba(255, 120, 180, 0.14), transparent 70%),
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
