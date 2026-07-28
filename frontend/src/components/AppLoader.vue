<template>
  <div
    class="app-loader"
    :class="{ 'app-loader--exit': exitActive }"
    aria-hidden="true"
  >
    <div class="app-loader__aurora">
      <div class="app-loader__aurora-layer app-loader__aurora-layer--1" />
      <div class="app-loader__aurora-layer app-loader__aurora-layer--2" />
      <div class="app-loader__aurora-layer app-loader__aurora-layer--3" />
    </div>

    <div class="app-loader__stage">
      <img
        :src="logoUrl"
        alt=""
        class="app-loader__logo"
      >
      <h1 class="app-loader__title">
        The Xưởng
      </h1>
      <p class="app-loader__subtitle">
        Sport
      </p>
      <div class="app-loader__shimmer">
        <div />
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import logoUrl from '@/assets/logo.png'

const emit = defineEmits<{ exited: [] }>()
const router = useRouter()
const exitActive = ref(false)

const sleep = (ms: number) => new Promise<void>(r => setTimeout(r, ms))

onMounted(async () => {
  await Promise.all([router.isReady(), sleep(1200)])
  exitActive.value = true
  setTimeout(() => emit('exited'), 650)
})
</script>

<style scoped>
.app-loader {
  position: fixed;
  inset: 0;
  z-index: 9999;
  background: #0A0A0A;
  overflow: hidden;
  transition: opacity 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.app-loader--exit {
  opacity: 0;
}

.app-loader__aurora {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.app-loader__aurora-layer {
  position: absolute;
  border-radius: 50%;
  filter: blur(20px);
  animation: loaderAuroraDrift 14s ease-in-out infinite alternate;
  transition: transform 0.6s cubic-bezier(0.16, 1, 0.3, 1),
              filter 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.app-loader__aurora-layer--1 {
  width: 60vw;
  height: 60vw;
  top: -10%;
  left: -10%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.18) 0%, transparent 70%);
}

.app-loader__aurora-layer--2 {
  width: 55vw;
  height: 55vw;
  bottom: -15%;
  right: -10%;
  background: radial-gradient(circle, rgba(255, 255, 255, 0.12) 0%, transparent 70%);
  animation-delay: -5s;
}

.app-loader__aurora-layer--3 {
  width: 45vw;
  height: 45vw;
  top: 30%;
  left: 50%;
  transform: translateX(-50%);
  background: radial-gradient(circle, rgba(255, 255, 255, 0.08) 0%, transparent 70%);
  animation-delay: -9s;
}

@keyframes loaderAuroraDrift {
  0% { transform: translate(0, 0) scale(1); }
  50% { transform: translate(4%, -3%) scale(1.08); }
  100% { transform: translate(-3%, 4%) scale(0.95); }
}

.app-loader__stage {
  position: relative;
  z-index: 10;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 100vh;
  padding: 0 1.5rem;
  transition: transform 0.6s cubic-bezier(0.16, 1, 0.3, 1),
              opacity 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.app-loader__logo {
  width: 120px;
  height: auto;
  margin-bottom: 2rem;
  animation: loaderPulse 1.8s ease-in-out infinite;
  transition: transform 0.6s cubic-bezier(0.16, 1, 0.3, 1),
              filter 0.6s cubic-bezier(0.16, 1, 0.3, 1),
              opacity 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes loaderPulse {
  0%, 100% { transform: scale(1); filter: drop-shadow(0 0 0 rgba(255, 255, 255, 0)); }
  50% { transform: scale(1.08); filter: drop-shadow(0 0 24px rgba(255, 255, 255, 0.35)); }
}

.app-loader__title {
  font-family: 'Dancing Script', cursive;
  font-weight: 700;
  font-size: clamp(3rem, 6vw, 4.5rem);
  color: #fff;
  letter-spacing: 0.02em;
  margin: 0;
  opacity: 0;
  transform: translateY(20px);
  filter: blur(8px);
  animation: loaderRevealUp 0.9s cubic-bezier(0.16, 1, 0.3, 1) 0.3s forwards;
  transition: transform 0.6s cubic-bezier(0.16, 1, 0.3, 1),
              opacity 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.app-loader__subtitle {
  font-family: 'JetBrains Mono', monospace;
  font-size: 10px;
  text-transform: uppercase;
  letter-spacing: 0.4em;
  color: rgba(255, 255, 255, 0.5);
  margin: 0.5rem 0 0;
  opacity: 0;
  transform: translateY(20px);
  filter: blur(8px);
  animation: loaderRevealUp 0.9s cubic-bezier(0.16, 1, 0.3, 1) 0.5s forwards;
  transition: transform 0.6s cubic-bezier(0.16, 1, 0.3, 1),
              opacity 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

@keyframes loaderRevealUp {
  to { opacity: 1; transform: translateY(0); filter: blur(0); }
}

.app-loader__shimmer {
  position: relative;
  width: 8rem;
  height: 1px;
  background: rgba(255, 255, 255, 0.2);
  margin-top: 2rem;
  overflow: hidden;
  opacity: 0;
  animation: loaderRevealUp 0.9s cubic-bezier(0.16, 1, 0.3, 1) 0.7s forwards;
  transition: transform 0.6s cubic-bezier(0.16, 1, 0.3, 1),
              opacity 0.6s cubic-bezier(0.16, 1, 0.3, 1);
}

.app-loader__shimmer > div {
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.6), transparent);
  animation: loaderShimmerSweep 1.4s linear infinite;
}

@keyframes loaderShimmerSweep {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

/* === EXIT === */
.app-loader--exit .app-loader__aurora-layer {
  animation: none !important;
  transform: scale(1.15);
  filter: blur(40px);
}

.app-loader--exit .app-loader__logo,
.app-loader--exit .app-loader__title,
.app-loader--exit .app-loader__subtitle,
.app-loader--exit .app-loader__shimmer,
.app-loader--exit .app-loader__shimmer > div {
  animation: none !important;
}

.app-loader--exit .app-loader__logo {
  transform: scale(1.4);
  filter: blur(14px);
  opacity: 0;
}

.app-loader--exit .app-loader__title,
.app-loader--exit .app-loader__subtitle,
.app-loader--exit .app-loader__shimmer {
  transform: translateY(-24px);
  opacity: 0;
}

/* === MOBILE === */
@media (max-width: 768px) {
  .app-loader__aurora-layer { filter: blur(12px); }
  .app-loader__aurora-layer--3 { display: none; }
}

/* === REDUCED MOTION === */
@media (prefers-reduced-motion: reduce) {
  .app-loader__aurora-layer,
  .app-loader__logo,
  .app-loader__shimmer > div {
    animation: none !important;
  }
  .app-loader__title,
  .app-loader__subtitle,
  .app-loader__shimmer {
    opacity: 1 !important;
    transform: none !important;
    filter: none !important;
    animation: none !important;
  }
  .app-loader,
  .app-loader__stage,
  .app-loader__logo,
  .app-loader__aurora-layer,
  .app-loader__title,
  .app-loader__subtitle,
  .app-loader__shimmer {
    transition-duration: 0.3s !important;
  }
}
</style>
