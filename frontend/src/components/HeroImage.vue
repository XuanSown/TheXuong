<template>
  <section
    class="relative w-full overflow-hidden bg-black"
    :style="{ height: '100dvh' }"
  >
    <!-- Base Image Layer -->
    <div
      class="absolute inset-0 bg-center bg-cover bg-no-repeat z-10 hero-zoom"
      :style="{ backgroundImage: 'url(' + bgImage1 + ')' }"
    />

    <!-- Spotlight Reveal Layer -->
    <div class="absolute inset-0 z-30 pointer-events-none">
      <canvas
        ref="canvasRef"
        class="absolute opacity-0 pointer-events-none"
      />
      <div
        class="absolute inset-0 bg-center bg-cover bg-no-repeat pointer-events-none"
        :style="({
          backgroundImage: 'url(' + bgImage2 + ')',
          maskImage: maskUrl ? 'url(' + maskUrl + ')' : 'none',
          webkitMaskImage: maskUrl ? 'url(' + maskUrl + ')' : 'none',
          maskSize: '100% 100%',
          webkitMaskSize: '100% 100%',
          maskRepeat: 'no-repeat',
          webkitMaskRepeat: 'no-repeat',
        } as any)"
      />
    </div>

    <!-- Hero Heading -->
    <h1
      class="absolute top-[14%] left-0 right-0 flex flex-col items-center text-center px-5 pointer-events-none z-50 text-white leading-[0.95]"
    >
      <span
        class="block font-jetbrains italic font-normal text-5xl sm:text-7xl md:text-8xl hero-anim hero-reveal"
        :style="{ letterSpacing: '-0.05em', animationDelay: '0.25s' }"
      >
        Layers hold
      </span>
      <span
        class="block font-normal text-5xl sm:text-7xl md:text-8xl -mt-1 hero-anim hero-reveal"
        :style="{ letterSpacing: '-0.08em', animationDelay: '0.42s' }"
      >
        tales of time
      </span>
    </h1>

    <!-- Bottom Left Details -->
    <div
      class="hidden sm:block absolute bottom-14 left-10 md:left-14 max-w-[260px] z-50 hero-anim hero-fade"
      :style="{ animationDelay: '0.7s' }"
    >
      <p class="text-sm text-white/80 leading-relaxed">
        — "Sports are not just about winning; they are about pushing your limits, embracing the sweat, and discovering
        the champion hidden inside you. Every drop of sweat brings you closer to your goals."
      </p>
    </div>

    <!-- Bottom Right Details & CTA -->
    <div
      class="absolute bottom-10 sm:bottom-24 left-5 right-5 sm:left-auto sm:right-10 md:right-14 max-w-full sm:max-w-[260px] flex flex-col items-start gap-4 sm:gap-5 z-50 hero-anim hero-fade"
      :style="{ animationDelay: '0.85s' }"
    >
      <p class="text-xs sm:text-sm text-white/80 leading-relaxed">
        — "Your body can stand almost anything; it is your mind that you have to convince. Train hard, stay focused, and
        let your passion speak louder than your excuses today."
      </p>
      <router-link
        to="/products"
        class="px-8 py-3 bg-white text-black text-xs font-semibold uppercase tracking-[1.8px] leading-[12px] rounded-sm hover:bg-gray-200 transition-colors"
      >
        KHÁM PHÁ NGAY
      </router-link>
    </div>
  </section>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import heroImage1 from '@/assets/hero_image1.jpeg'
import heroImage2 from '@/assets/hero_image2.jpeg'

withDefaults(defineProps<{
  bgImage1?: string
  bgImage2?: string
}>(), {
  bgImage1: () => heroImage1,
  bgImage2: () => heroImage2,
})

const canvasRef = ref<HTMLCanvasElement | null>(null)
const maskUrl = ref('')
const SPOTLIGHT_R = 260

const drawMask = () => {
  const canvas = canvasRef.value
  if (!canvas) return
  const ctx = canvas.getContext('2d', { willReadFrequently: true })
  if (!ctx) return

  ctx.clearRect(0, 0, canvas.width, canvas.height)

  if (smoothPos.x !== -999 && smoothPos.y !== -999) {
    const gradient = ctx.createRadialGradient(
      smoothPos.x, smoothPos.y, 0,
      smoothPos.x, smoothPos.y, SPOTLIGHT_R
    )
    gradient.addColorStop(0, 'rgba(255,255,255,1)')
    gradient.addColorStop(0.4, 'rgba(255,255,255,1)')
    gradient.addColorStop(0.6, 'rgba(255,255,255,0.75)')
    gradient.addColorStop(0.75, 'rgba(255,255,255,0.4)')
    gradient.addColorStop(0.88, 'rgba(255,255,255,0.12)')
    gradient.addColorStop(1, 'rgba(255,255,255,0)')
    ctx.fillStyle = gradient
    ctx.beginPath()
    ctx.arc(smoothPos.x, smoothPos.y, SPOTLIGHT_R, 0, Math.PI * 2)
    ctx.fill()
  }

  maskUrl.value = canvas.toDataURL('image/png')
}

const mouse = { x: -999, y: -999 }
const smoothPos = { x: -999, y: -999 }
let rafRef: number | null = null

const onMouseMove = (e: MouseEvent) => {
  if (mouse.x === -999) {
    smoothPos.x = e.clientX
    smoothPos.y = e.clientY
  }
  mouse.x = e.clientX
  mouse.y = e.clientY
}

const loop = () => {
  if (mouse.x !== -999) {
    smoothPos.x += (mouse.x - smoothPos.x) * 0.1
    smoothPos.y += (mouse.y - smoothPos.y) * 0.1
    drawMask()
  }
  rafRef = requestAnimationFrame(loop)
}

const resizeCanvas = () => {
  const canvas = canvasRef.value
  if (canvas) {
    canvas.width = window.innerWidth
    canvas.height = window.innerHeight
    drawMask()
  }
}

onMounted(() => {
  resizeCanvas()
  window.addEventListener('resize', resizeCanvas)
  window.addEventListener('mousemove', onMouseMove)
  rafRef = requestAnimationFrame(loop)
})

onUnmounted(() => {
  window.removeEventListener('resize', resizeCanvas)
  window.removeEventListener('mousemove', onMouseMove)
  if (rafRef) cancelAnimationFrame(rafRef)
})
</script>

<style scoped>
@keyframes heroReveal {
  0% {
    opacity: 0;
    transform: translateY(28px);
    filter: blur(12px);
  }

  100% {
    opacity: 1;
    transform: translateY(0);
    filter: blur(0);
  }
}

@keyframes heroFadeUp {
  0% {
    opacity: 0;
    transform: translateY(20px);
  }

  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes heroZoom {
  0% {
    transform: scale(1.12);
  }

  100% {
    transform: scale(1);
  }
}

.hero-anim {
  opacity: 0;
  animation-fill-mode: forwards;
  animation-timing-function: cubic-bezier(0.16, 1, 0.3, 1);
}

.hero-reveal {
  animation-name: heroReveal;
  animation-duration: 1.1s;
}

.hero-fade {
  animation-name: heroFadeUp;
  animation-duration: 1s;
}

.hero-zoom {
  animation: heroZoom 1.8s cubic-bezier(0.16, 1, 0.3, 1) forwards;
}

@media (prefers-reduced-motion: reduce) {

  .hero-anim,
  .hero-zoom {
    animation: none;
    opacity: 1;
  }
}
</style>
