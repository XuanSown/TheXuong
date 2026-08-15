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
          {{ t('terms.heroEyebrow') }}
        </p>
        <h1 class="font-geist font-bold text-white leading-[0.95] mb-6">
          <span
            class="block text-4xl sm:text-6xl md:text-7xl hero-anim hero-reveal"
            style="letter-spacing:-1.28px;animation-delay:.25s"
          >{{ t('terms.heroTitle1') }}</span>
          <span
            class="block text-4xl sm:text-6xl md:text-7xl -mt-1 hero-anim hero-reveal text-white/80"
            style="letter-spacing:-0.64px;animation-delay:.42s"
          >{{ t('terms.heroTitle2') }}</span>
        </h1>
        <p
          class="font-geist text-base sm:text-lg text-white/70 leading-[29px] max-w-[640px] mx-auto hero-anim hero-fade"
          style="animation-delay:.6s"
        >
          {{ t('terms.heroDesc') }}
        </p>
      </div>
      <div
        class="absolute bottom-6 left-1/2 -translate-x-1/2 z-10 hero-anim hero-fade"
        style="animation-delay:.8s"
      >
        <span class="block w-px h-10 bg-white/40 scroll-indicator" />
      </div>
    </section>

    <!-- TABLE OF CONTENTS -->
    <section class="w-full max-w-[1280px] mx-auto px-4 py-24 md:py-32">
      <div class="grid md:grid-cols-[1fr_2fr] gap-12 md:gap-20">
        <div
          class="md:sticky md:top-28 self-start"
          data-reveal
        >
          <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
            {{ t('terms.tocEyebrow') }}
          </p>
          <h2 class="font-geist text-[32px] sm:text-[40px] leading-[48px] tracking-[-0.64px] text-[#1A1C1C]">
            {{ t('terms.tocTitle') }}
          </h2>
        </div>
        <nav
          class="space-y-3"
          data-reveal
        >
          <a
            v-for="(section, i) in tocItems"
            :key="i"
            :href="'#section-' + i"
            class="liquid-glass block rounded-xl border border-white/60 bg-white/50 p-5 hover:bg-white/75 hover:-translate-y-0.5 transition-all duration-300 group"
          >
            <span class="glass-sheen pointer-events-none" />
            <div class="flex items-center gap-4">
              <span class="font-jetbrains text-[13px] text-[#5E5F5C] group-hover:text-[#1A1C1C] transition-colors">0{{ i + 1 }}</span>
              <span class="font-geist text-[16px] text-[#1A1C1C] group-hover:translate-x-1 transition-transform duration-300">{{ section }}</span>
            </div>
          </a>
        </nav>
      </div>
    </section>

    <!-- SECTIONS -->
    <section class="w-full max-w-[1280px] mx-auto px-4 pb-16 md:pb-24">
      <div class="space-y-20 md:space-y-28">
        <div
          v-for="(section, i) in sections"
          :id="'section-' + i"
          :key="i"
          class="grid md:grid-cols-[1fr_2fr] gap-10 md:gap-20 scroll-mt-28"
        >
          <div
            class="md:sticky md:top-28 self-start"
            data-reveal
          >
            <span class="font-jetbrains text-[13px] text-[#5E5F5C] mb-2 block">0{{ i + 1 }}</span>
            <h3 class="font-geist text-[28px] sm:text-[36px] leading-[42px] tracking-[-0.36px] text-[#1A1C1C]">
              {{ section.title }}
            </h3>
          </div>
          <div class="space-y-5">
            <div
              v-for="(block, j) in section.blocks"
              :key="j"
              data-reveal
              class="liquid-glass relative rounded-2xl border border-white/60 bg-white/50 p-8 shadow-[0_8px_32px_rgba(26,28,28,0.10)]"
            >
              <span class="glass-sheen pointer-events-none" />
              <h4
                v-if="block.subtitle"
                class="font-geist text-[18px] leading-[26px] text-[#1A1C1C] mb-3"
              >
                {{ block.subtitle }}
              </h4>
              <p class="font-geist text-base text-[#5E5F5C] leading-[29px]">
                {{ block.content }}
              </p>
              <ul
                v-if="block.list"
                class="mt-4 space-y-2"
              >
                <li
                  v-for="(item, k) in block.list"
                  :key="k"
                  class="font-geist text-sm text-[#5E5F5C] leading-[26px] pl-5 relative before:content-[''] before:absolute before:left-0 before:top-[10px] before:w-[6px] before:h-[6px] before:rounded-full before:bg-[#CFC4C6]"
                >
                  {{ item }}
                </li>
              </ul>
            </div>
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
          {{ t('terms.ctaTitle') }}
        </h2>
        <p class="font-geist text-base text-white/70 leading-[29px] mb-10 max-w-[560px] mx-auto">
          {{ t('terms.ctaDesc') }}
        </p>
        <div class="flex flex-col sm:flex-row gap-4 justify-center">
          <router-link
            to="/contact"
            class="liquid-btn px-10 py-5 bg-white/10 text-white text-[12px] font-semibold uppercase tracking-[1.8px] leading-[12px] rounded-full hover:bg-white/20 transition-colors"
          >
            {{ t('terms.ctaContact') }}
          </router-link>
          <router-link
            to="/policy/privacy"
            class="liquid-btn px-10 py-5 bg-white/10 text-white text-[12px] font-semibold uppercase tracking-[1.8px] leading-[12px] rounded-full hover:bg-white/20 transition-colors"
          >
            {{ t('terms.ctaPrivacy') }}
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

const { t } = useI18n()

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

const tocItems = computed(() => [
  t('terms.s1Title'),
  t('terms.s2Title'),
  t('terms.s3Title'),
  t('terms.s4Title'),
  t('terms.s5Title'),
  t('terms.s6Title'),
  t('terms.s7Title'),
  t('terms.s8Title'),
  t('terms.s9Title'),
])

interface Block {
  subtitle?: string
  content: string
  list?: string[]
}

const sections = computed<{ title: string; blocks: Block[] }[]>(() => [
  {
    title: t('terms.s1Title'),
    blocks: [
      { content: t('terms.s1b1') },
      { subtitle: t('terms.s1b2Sub'), content: t('terms.s1b2') }
    ]
  },
  {
    title: t('terms.s2Title'),
    blocks: [
      {
        subtitle: t('terms.s2b1Sub'),
        content: t('terms.s2b1'),
        list: [t('terms.s2b1l1'), t('terms.s2b1l2'), t('terms.s2b1l3')]
      },
      { subtitle: t('terms.s2b2Sub'), content: t('terms.s2b2') }
    ]
  },
  {
    title: t('terms.s3Title'),
    blocks: [
      { subtitle: t('terms.s3b1Sub'), content: t('terms.s3b1') },
      { subtitle: t('terms.s3b2Sub'), content: t('terms.s3b2') }
    ]
  },
  {
    title: t('terms.s4Title'),
    blocks: [
      {
        subtitle: t('terms.s4b1Sub'),
        content: t('terms.s4b1'),
        list: [t('terms.s4b1l1'), t('terms.s4b1l2'), t('terms.s4b1l3')]
      },
      { subtitle: t('terms.s4b2Sub'), content: t('terms.s4b2') }
    ]
  },
  {
    title: t('terms.s5Title'),
    blocks: [
      {
        content: t('terms.s5b1'),
        list: [t('terms.s5b1l1'), t('terms.s5b1l2'), t('terms.s5b1l3'), t('terms.s5b1l4')]
      }
    ]
  },
  {
    title: t('terms.s6Title'),
    blocks: [
      { subtitle: t('terms.s6b1Sub'), content: t('terms.s6b1') },
      { subtitle: t('terms.s6b2Sub'), content: t('terms.s6b2') }
    ]
  },
  {
    title: t('terms.s7Title'),
    blocks: [
      {
        content: t('terms.s7b1'),
        list: [t('terms.s7b1l1'), t('terms.s7b1l2'), t('terms.s7b1l3')]
      }
    ]
  },
  {
    title: t('terms.s8Title'),
    blocks: [
      { content: t('terms.s8b1') },
      { subtitle: t('terms.s8b2Sub'), content: t('terms.s8b2') }
    ]
  },
  {
    title: t('terms.s9Title'),
    blocks: [
      { content: t('terms.s9b1') }
    ]
  }
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
