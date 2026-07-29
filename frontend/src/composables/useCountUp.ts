import { ref, onMounted, onUnmounted, type Ref } from 'vue'

export interface UseCountUpOptions {
  target: number
  duration?: number
}

export const easeOutExpo = (t: number): number =>
  t === 1 ? 1 : 1 - Math.pow(2, -10 * t)

export function useCountUp(elRef: Ref<HTMLElement | null>, options: UseCountUpOptions) {
  const { target, duration = 2000 } = options
  const current = ref(0)
  let rafId: number | null = null
  let observer: IntersectionObserver | null = null
  let started = false

  const animate = () => {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      current.value = target
      return
    }
    const start = performance.now()
    const step = (now: number) => {
      const progress = Math.min((now - start) / duration, 1)
      current.value = easeOutExpo(progress) * target
      if (progress < 1) {
        rafId = requestAnimationFrame(step)
      } else {
        current.value = target
      }
    }
    rafId = requestAnimationFrame(step)
  }

  const startAnim = () => {
    if (started) return
    started = true
    animate()
  }

  onMounted(() => {
    const el = elRef.value
    if (!el || typeof IntersectionObserver === 'undefined') {
      current.value = target
      return
    }
    observer = new IntersectionObserver(
      (entries) => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            startAnim()
            observer?.disconnect()
          }
        }
      },
      { threshold: 0.4 },
    )
    observer.observe(el)
  })

  onUnmounted(() => {
    observer?.disconnect()
    if (rafId) cancelAnimationFrame(rafId)
  })

  return { current }
}
