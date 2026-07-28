import { type Ref, onMounted, onUnmounted } from 'vue'

export interface UseScrollRevealOptions {
  scale?: number
  dim?: number
  selector?: string
}

export function useScrollReveal(
  containerRef: Ref<HTMLElement | null>,
  options: UseScrollRevealOptions = {},
) {
  const { scale = 0.05, dim = 0.35, selector = '[data-stack-card]' } = options
  let rafId: number | null = null
  let ticking = false

  const update = () => {
    ticking = false
    const container = containerRef.value
    if (!container) return
    const cards = Array.from(container.querySelectorAll<HTMLElement>(selector))
    for (let i = 0; i < cards.length; i++) {
      const card = cards[i]
      if (i === cards.length - 1) {
        card.style.transform = ''
        card.style.opacity = '1'
        continue
      }
      const next = cards[i + 1]
      const curTop = card.getBoundingClientRect().top
      const nextTop = next.getBoundingClientRect().top
      const height = card.offsetHeight || 1
      const distance = Math.max(nextTop - curTop, 0)
      const progress = Math.max(0, Math.min(1, 1 - distance / height))
      card.style.transform = `scale(${1 - progress * scale})`
      card.style.opacity = String(1 - progress * dim)
    }
  }

  const onScroll = () => {
    if (ticking) return
    ticking = true
    rafId = requestAnimationFrame(update)
  }

  onMounted(() => {
    if (window.matchMedia('(prefers-reduced-motion: reduce)').matches) return
    window.addEventListener('scroll', onScroll, { passive: true })
    window.addEventListener('resize', onScroll)
    update()
  })

  onUnmounted(() => {
    window.removeEventListener('scroll', onScroll)
    window.removeEventListener('resize', onScroll)
    if (rafId) cancelAnimationFrame(rafId)
  })
}
