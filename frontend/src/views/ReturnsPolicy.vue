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
          {{ t('returnsPolicy.heroEyebrow') }}
        </p>
        <h1 class="font-geist font-bold text-white leading-[0.95] mb-6">
          <span
            class="block text-5xl sm:text-7xl md:text-8xl hero-anim hero-reveal"
            style="letter-spacing:-1.28px;animation-delay:.25s"
          >{{ t('returnsPolicy.heroTitle') }}</span>
        </h1>
        <p
          class="font-geist text-base sm:text-lg text-white/70 leading-[29px] max-w-[640px] mx-auto hero-anim hero-fade"
          style="animation-delay:.5s"
        >
          {{ t('returnsPolicy.heroDesc') }}
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
            {{ t('returnsPolicy.overviewEyebrow') }}
          </p>
          <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
            {{ t('returnsPolicy.overviewTitle') }}
          </h2>
        </div>
        <div
          class="relative"
          data-reveal
        >
          <div class="liquid-glass relative rounded-2xl border border-white/60 bg-white/50 p-8 md:p-10 shadow-[0_8px_32px_rgba(26,28,28,0.12)]">
            <span class="glass-sheen pointer-events-none" />
            <i18n-t
              keypath="returnsPolicy.overviewP1"
              tag="p"
              class="font-geist text-base text-[#1A1C1C] leading-[29px] mb-5"
            >
              <template #days>
                <strong>{{ t('returnsPolicy.overviewDays') }}</strong>
              </template>
            </i18n-t>
            <p class="font-geist text-base text-[#5E5F5C] leading-[29px] mb-5">
              {{ t('returnsPolicy.overviewP2') }}
            </p>
            <i18n-t
              keypath="returnsPolicy.overviewP3"
              tag="p"
              class="font-geist text-base text-[#5E5F5C] leading-[29px]"
            >
              <template #days>
                <strong>{{ t('returnsPolicy.overviewRefundDays') }}</strong>
              </template>
            </i18n-t>
          </div>
        </div>
      </div>
    </section>

    <!-- PROCESS STEPS -->
    <section
      ref="processRef"
      class="w-full max-w-[1100px] mx-auto px-4 py-16 md:py-24"
    >
      <div
        class="text-center mb-12 md:mb-16"
        data-reveal
      >
        <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
          {{ t('returnsPolicy.processEyebrow') }}
        </p>
        <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
          {{ t('returnsPolicy.processTitle') }}
        </h2>
      </div>
      <div class="relative">
        <div
          v-for="(step, i) in processSteps"
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
                {{ step.title }}
              </h3>
              <p class="font-geist text-base text-[#5E5F5C] leading-[26px] max-w-[640px]">
                {{ step.desc }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CONDITIONS BENTO -->
    <section class="w-full max-w-[1280px] mx-auto px-4 py-16 md:py-24">
      <div
        class="text-center mb-12 md:mb-16"
        data-reveal
      >
        <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
          {{ t('returnsPolicy.conditionsEyebrow') }}
        </p>
        <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
          {{ t('returnsPolicy.conditionsTitle') }}
        </h2>
      </div>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
        <div
          v-for="(cond, i) in conditions"
          :key="i"
          data-reveal
          class="liquid-glass bg-white/55 border border-white/60 rounded-2xl p-8 md:p-10 shadow-[0_8px_32px_rgba(26,28,28,0.08)] hover:bg-white/75 hover:-translate-y-1 transition-all duration-500"
        >
          <span class="glass-sheen pointer-events-none" />
          <div class="w-10 h-10 rounded-full bg-black/5 flex items-center justify-center mb-5">
            <span class="text-lg">{{ cond.icon }}</span>
          </div>
          <h3 class="font-geist text-[22px] leading-[28px] tracking-[-0.22px] text-[#1A1C1C] mb-3">
            {{ cond.title }}
          </h3>
          <p class="font-geist text-sm text-[#5E5F5C] leading-[24px]">
            {{ cond.desc }}
          </p>
        </div>
      </div>
    </section>

    <!-- EXCLUSIONS -->
    <section class="w-full relative bg-black text-white py-24 md:py-32 overflow-hidden">
      <div class="absolute inset-0 hero-glow pointer-events-none opacity-60" />
      <div class="relative max-w-[1280px] mx-auto px-4">
        <div
          class="text-center mb-12 md:mb-16"
          data-reveal
        >
          <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-white/60 mb-4">
            {{ t('returnsPolicy.exclusionsEyebrow') }}
          </p>
          <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] text-white">
            {{ t('returnsPolicy.exclusionsTitle') }}
          </h2>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
          <div
            v-for="(ex, i) in exclusions"
            :key="i"
            data-reveal
            class="liquid-glass relative bg-white/10 border border-white/20 rounded-2xl p-8 hover:bg-white/20 hover:-translate-y-1 transition-all duration-500"
          >
            <span class="glass-sheen pointer-events-none" />
            <h3 class="font-geist text-[20px] leading-[26px] text-white mb-3">
              {{ ex.title }}
            </h3>
            <p class="font-geist text-sm text-white/70 leading-[24px]">
              {{ ex.desc }}
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- REFUND TIMELINE -->
    <section class="w-full max-w-[1280px] mx-auto px-4 py-24 md:py-32">
      <div
        class="text-center mb-12 md:mb-16"
        data-reveal
      >
        <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
          {{ t('returnsPolicy.refundEyebrow') }}
        </p>
        <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
          {{ t('returnsPolicy.refundTitle') }}
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
                  {{ t('returnsPolicy.refundThMethod') }}
                </th>
                <th class="font-geist text-[13px] uppercase tracking-[1.2px] text-[#5E5F5C] py-4 px-3 text-left">
                  {{ t('returnsPolicy.refundThTime') }}
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(row, i) in refundTimeline"
                :key="i"
                class="border-b border-[#CFC4C6]/50 hover:bg-white/40 transition-colors"
              >
                <td class="font-geist py-4 px-3 text-[15px] font-semibold text-[#1A1C1C]">
                  {{ row.method }}
                </td>
                <td class="font-geist py-4 px-3 text-sm text-[#5E5F5C]">
                  {{ row.duration }}
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
          {{ t('returnsPolicy.ctaTitle') }}
        </h2>
        <p class="font-geist text-base text-white/70 leading-[29px] mb-10 max-w-[560px] mx-auto">
          {{ t('returnsPolicy.ctaDesc') }}
        </p>
        <div class="flex flex-col sm:flex-row gap-4 justify-center">
          <router-link
            to="/contact"
            class="liquid-btn px-10 py-5 bg-white/10 text-white text-[12px] font-semibold uppercase tracking-[1.8px] leading-[12px] rounded-full hover:bg-white/20 transition-colors"
          >
            {{ t('returnsPolicy.ctaContact') }}
          </router-link>
          <router-link
            to="/orders"
            class="liquid-btn px-10 py-5 bg-white/10 text-white text-[12px] font-semibold uppercase tracking-[1.8px] leading-[12px] rounded-full hover:bg-white/20 transition-colors"
          >
            {{ t('returnsPolicy.ctaViewOrders') }}
          </router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { useI18n } from 'vue-i18n'
import { useReveal } from '@/composables/useReveal'
import { useScrollReveal } from '@/composables/useScrollReveal'

const { t } = useI18n()

const rootRef = ref<HTMLElement | null>(null)
const heroRef = ref<HTMLElement | null>(null)
const processRef = ref<HTMLElement | null>(null)
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
useScrollReveal(processRef)

onUnmounted(() => {
  if (glowRaf) cancelAnimationFrame(glowRaf)
})

const processSteps = computed(() => [
  { title: t('returnsPolicy.step1Title'), desc: t('returnsPolicy.step1Desc') },
  { title: t('returnsPolicy.step2Title'), desc: t('returnsPolicy.step2Desc') },
  { title: t('returnsPolicy.step3Title'), desc: t('returnsPolicy.step3Desc') },
  { title: t('returnsPolicy.step4Title'), desc: t('returnsPolicy.step4Desc') },
  { title: t('returnsPolicy.step5Title'), desc: t('returnsPolicy.step5Desc') }
])

const conditions = computed(() => [
  { icon: '📦', title: t('returnsPolicy.condition1Title'), desc: t('returnsPolicy.condition1Desc') },
  { icon: '🏷️', title: t('returnsPolicy.condition2Title'), desc: t('returnsPolicy.condition2Desc') },
  { icon: '📅', title: t('returnsPolicy.condition3Title'), desc: t('returnsPolicy.condition3Desc') },
  { icon: '🧾', title: t('returnsPolicy.condition4Title'), desc: t('returnsPolicy.condition4Desc') },
  { icon: '✅', title: t('returnsPolicy.condition5Title'), desc: t('returnsPolicy.condition5Desc') },
  { icon: '🔍', title: t('returnsPolicy.condition6Title'), desc: t('returnsPolicy.condition6Desc') }
])

const exclusions = computed(() => [
  { title: t('returnsPolicy.exclusion1Title'), desc: t('returnsPolicy.exclusion1Desc') },
  { title: t('returnsPolicy.exclusion2Title'), desc: t('returnsPolicy.exclusion2Desc') },
  { title: t('returnsPolicy.exclusion3Title'), desc: t('returnsPolicy.exclusion3Desc') },
  { title: t('returnsPolicy.exclusion4Title'), desc: t('returnsPolicy.exclusion4Desc') }
])

const refundTimeline = computed(() => [
  { method: t('returnsPolicy.refundRow1Method'), duration: t('returnsPolicy.refundRow1Time') },
  { method: t('returnsPolicy.refundRow2Method'), duration: t('returnsPolicy.refundRow2Time') },
  { method: t('returnsPolicy.refundRow3Method'), duration: t('returnsPolicy.refundRow3Time') },
  { method: t('returnsPolicy.refundRow4Method'), duration: t('returnsPolicy.refundRow4Time') }
])
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
