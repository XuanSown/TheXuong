<template>
  <div
    ref="rootRef"
    class="relative min-h-screen bg-transparent text-[#1A1C1C]"
  >
    <!-- HERO -->
    <section
      ref="heroRef"
      class="relative w-full overflow-hidden bg-black text-white flex flex-col items-center justify-center"
      :style="{ height: '70dvh' }"
      @mousemove="onHeroMove"
    >
      <div class="absolute inset-0 z-0 hero-glow pointer-events-none" />
      <div class="absolute inset-0 z-0 hero-aurora pointer-events-none" />
      <div class="relative z-10 flex flex-col items-center text-center px-5 max-w-[900px]">
        <p
          class="font-geist text-[12px] uppercase tracking-[1.8px] text-white/60 mb-6 hero-anim hero-fade"
          style="animation-delay:.1s"
        >
          HƯỚNG DẪN
        </p>
        <h1 class="font-geist font-bold text-white leading-[0.95] mb-6">
          <span
            class="block text-5xl sm:text-7xl md:text-8xl hero-anim hero-reveal"
            style="letter-spacing:-1.28px;animation-delay:.25s"
          >CHỌN SIZE</span>
          <span
            class="block text-4xl sm:text-5xl md:text-6xl -mt-1 hero-anim hero-reveal text-white/80"
            style="letter-spacing:-0.64px;animation-delay:.42s"
          >CHUẨN XÁC</span>
        </h1>
        <p
          class="font-geist text-base sm:text-lg text-white/70 leading-[29px] max-w-[640px] mx-auto mb-10 hero-anim hero-fade"
          style="animation-delay:.6s"
        >
          Hướng dẫn chi tiết cách đo cơ thể và bảng size chuẩn cho từng loại trang phục, giày dép.
        </p>
        <a
          href="#measuring-guide"
          class="liquid-btn px-10 py-5 bg-white/10 text-white text-[12px] font-semibold uppercase tracking-[1.8px] leading-[12px] rounded-full hover:bg-white/20 transition-colors hero-anim hero-fade"
          style="animation-delay:.75s"
        >XEM HƯỚNG DẪN</a>
      </div>
      <div
        class="absolute bottom-6 left-1/2 -translate-x-1/2 z-10 hero-anim hero-fade"
        style="animation-delay:1s"
      >
        <span class="block w-px h-10 bg-white/40 scroll-indicator" />
      </div>
    </section>

    <!-- MEASURING GUIDE -->
    <section
      id="measuring-guide"
      class="w-full max-w-[1280px] mx-auto px-4 py-24 md:py-32"
    >
      <div class="grid md:grid-cols-2 gap-12 md:gap-20">
        <div
          class="md:sticky md:top-28 self-start"
          data-reveal
        >
          <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
            BƯỚC 1
          </p>
          <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
            Đo cơ thể
          </h2>
          <p class="font-geist text-base text-[#5E5F5C] leading-[29px] mt-6">
            Sử dụng thước dây mềm để đo các vòng cơ thể. Đảm bảo thước không quá chặt hoặc quá lỏng.
          </p>
        </div>
        <div class="space-y-5">
          <div
            v-for="(step, i) in measuringSteps"
            :key="i"
            data-reveal
            class="liquid-glass relative rounded-2xl border border-white/60 bg-white/50 p-8 shadow-[0_8px_32px_rgba(26,28,28,0.12)] hover:bg-white/75 hover:-translate-y-1 transition-all duration-500"
          >
            <span class="glass-sheen pointer-events-none" />
            <div class="flex items-start gap-5">
              <span class="font-jetbrains text-[14px] text-[#5E5F5C] pt-2">0{{ i + 1 }}</span>
              <div class="flex-1">
                <h3 class="font-geist text-[22px] leading-[28px] tracking-[-0.22px] text-[#1A1C1C] mb-3">
                  {{ step.title }}
                </h3>
                <p class="font-geist text-sm text-[#5E5F5C] leading-[24px]">
                  {{ step.desc }}
                </p>
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- SIZE CHARTS -->
    <section
      ref="chartsRef"
      class="w-full max-w-[1280px] mx-auto px-4 py-16 md:py-24"
    >
      <div
        class="text-center mb-12 md:mb-16"
        data-reveal
      >
        <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
          BƯỚC 2
        </p>
        <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
          Bảng size
        </h2>
      </div>

      <!-- Size Chart Tabs -->
      <div
        class="flex justify-center gap-3 mb-10"
        data-reveal
      >
        <button
          v-for="tab in tabs"
          :key="tab.id"
          :class="[
            'px-6 py-3 rounded-full text-[13px] font-semibold uppercase tracking-[1.2px] transition-all duration-300',
            activeTab === tab.id
              ? 'bg-black text-white shadow-lg'
              : 'bg-white/50 text-[#5E5F5C] border border-white/60 hover:bg-white/75'
          ]"
          @click="activeTab = tab.id"
        >
          {{ tab.label }}
        </button>
      </div>

      <!-- Active Chart -->
      <div
        class="liquid-glass relative rounded-2xl border border-white/60 bg-white/50 p-6 md:p-10 shadow-[0_8px_32px_rgba(26,28,28,0.12)]"
        data-reveal
      >
        <span class="glass-sheen pointer-events-none" />
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead>
              <tr class="border-b border-[#CFC4C6]">
                <th
                  v-for="col in currentChart.columns"
                  :key="col"
                  class="font-geist text-[13px] uppercase tracking-[1.2px] text-[#5E5F5C] py-4 px-3 text-left"
                >
                  {{ col }}
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(row, i) in currentChart.rows"
                :key="i"
                class="border-b border-[#CFC4C6]/50 hover:bg-white/40 transition-colors"
              >
                <td
                  v-for="(cell, j) in row"
                  :key="j"
                  :class="[
                    'font-geist py-4 px-3',
                    j === 0 ? 'text-[15px] font-semibold text-[#1A1C1C]' : 'text-sm text-[#5E5F5C]'
                  ]"
                >
                  {{ cell }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
        <p class="font-geist text-xs text-[#5E5F5C] mt-6 italic">
          * Số liệu có thể chênh lệch 1-2cm tùy theo từng thương hiệu
        </p>
      </div>
    </section>

    <!-- TIPS -->
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
            Mẹo chọn size
          </h2>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
          <div
            v-for="(tip, i) in tips"
            :key="i"
            data-reveal
            class="liquid-glass relative bg-white/10 border border-white/20 rounded-2xl p-8 hover:bg-white/20 hover:-translate-y-1 transition-all duration-500"
          >
            <span class="glass-sheen pointer-events-none" />
            <div class="text-3xl mb-4">
              {{ tip.icon }}
            </div>
            <h3 class="font-geist text-[20px] leading-[26px] text-white mb-3">
              {{ tip.title }}
            </h3>
            <p class="font-geist text-sm text-white/70 leading-[24px]">
              {{ tip.desc }}
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA -->
    <section
      class="w-full max-w-[900px] mx-auto px-4 py-24 md:py-32 text-center"
      data-reveal
    >
      <h2 class="font-geist text-[32px] sm:text-[40px] md:text-[48px] leading-[56px] tracking-[-0.96px] text-[#1A1C1C] mb-6">
        Vẫn chưa chắc chắn về size?
      </h2>
      <p class="font-geist text-base text-[#5E5F5C] leading-[29px] mb-10 max-w-[560px] mx-auto">
        Đội ngũ hỗ trợ của chúng tôi sẵn sàng tư vấn size phù hợp nhất cho bạn.
      </p>
      <div class="flex flex-col sm:flex-row gap-4 justify-center">
        <router-link
          to="/contact"
          class="liquid-btn px-8 py-4 bg-black text-white text-[12px] font-semibold uppercase tracking-[1.8px] leading-[12px] rounded-full hover:bg-[#1A1C1C] transition-colors border border-black/20"
        >
          LIÊN HỆ TƯ VẤN
        </router-link>
        <router-link
          to="/products"
          class="liquid-btn px-8 py-4 bg-white/50 text-[#1A1C1C] text-[12px] font-semibold uppercase tracking-[1.8px] leading-[12px] rounded-full hover:bg-white/75 transition-colors border border-white/60"
        >
          XEM SẢN PHẨM
        </router-link>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { useReveal } from '@/composables/useReveal'

const rootRef = ref<HTMLElement | null>(null)
const heroRef = ref<HTMLElement | null>(null)
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

onUnmounted(() => {
  if (glowRaf) cancelAnimationFrame(glowRaf)
})

const measuringSteps = [
  {
    title: 'Vòng ngực',
    desc: 'Đo quanh phần đầy nhất của ngực, giữ thước ngang với mặt đất. Không siết chặt.'
  },
  {
    title: 'Vòng eo',
    desc: 'Đo quanh phần hẹp nhất của eo, thường nằm trên rốn khoảng 2-3cm.'
  },
  {
    title: 'Vòng hông',
    desc: 'Đo quanh phần rộng nhất của hông và mông, giữ thước song song với mặt đất.'
  },
  {
    title: 'Chiều cao',
    desc: 'Đứng thẳng, không mang giày, đo từ đỉnh đầu đến gót chân.'
  },
  {
    title: 'Chiều dài chân',
    desc: 'Đo từ háng xuống gót chân để chọn size quần phù hợp.'
  }
]

const tabs = [
  { id: 'tops', label: 'Áo' },
  { id: 'bottoms', label: 'Quần' },
  { id: 'shoes', label: 'Giày' }
]

const activeTab = ref('tops')

const sizeCharts = {
  tops: {
    columns: ['Size', 'Ngực (cm)', 'Eo (cm)', 'Chiều cao (cm)'],
    rows: [
      ['XS', '80-84', '64-68', '160-165'],
      ['S', '85-89', '69-73', '165-170'],
      ['M', '90-94', '74-78', '170-175'],
      ['L', '95-99', '79-83', '175-180'],
      ['XL', '100-104', '84-88', '180-185'],
      ['2XL', '105-109', '89-93', '185-190']
    ]
  },
  bottoms: {
    columns: ['Size', 'Eo (cm)', 'Hông (cm)', 'Dài chân (cm)'],
    rows: [
      ['28', '70-72', '88-90', '76-78'],
      ['30', '74-76', '92-94', '78-80'],
      ['32', '78-80', '96-98', '80-82'],
      ['34', '82-84', '100-102', '82-84'],
      ['36', '86-88', '104-106', '84-86'],
      ['38', '90-92', '108-110', '86-88']
    ]
  },
  shoes: {
    columns: ['Size VN', 'Size US', 'Size EU', 'Chiều dài chân (cm)'],
    rows: [
      ['39', '7', '39', '24.5'],
      ['40', '8', '40', '25.0'],
      ['41', '9', '41', '25.5'],
      ['42', '10', '42', '26.0'],
      ['43', '11', '43', '26.5'],
      ['44', '12', '44', '27.0']
    ]
  }
}

const currentChart = computed(() => sizeCharts[activeTab.value as keyof typeof sizeCharts])

const tips = [
  {
    icon: '📏',
    title: 'Đo vào buổi sáng',
    desc: 'Cơ thể có xu hướng nhỏ hơn vào buổi sáng sau khi ngủ dậy.'
  },
  {
    icon: '👕',
    title: 'Mặc đồ mỏng khi đo',
    desc: 'Đo trên lớp quần áo mỏng để có số liệu chính xác nhất.'
  },
  {
    icon: '📐',
    title: 'Giữ thước song song',
    desc: 'Đảm bảo thước đo luôn song song với mặt đất khi đo vòng.'
  },
  {
    icon: '🤔',
    title: 'Ở giữa 2 size?',
    desc: 'Nếu số đo nằm giữa 2 size, hãy chọn size lớn hơn để thoải mái.'
  },
  {
    icon: '👟',
    title: 'Đo cả 2 chân',
    desc: 'Chân trái và phải có thể khác nhau, hãy dùng số đo của chân lớn hơn.'
  },
  {
    icon: '📞',
    title: 'Cần hỗ trợ?',
    desc: 'Liên hệ với chúng tôi để được tư vấn size chính xác cho từng thương hiệu.'
  }
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
  [data-reveal] { opacity: 1; transform: none; filter: none; transition: none; }
}
</style>
