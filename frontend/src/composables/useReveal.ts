import { type Ref, onMounted, onUnmounted } from 'vue'

export interface UseRevealOptions {
  selector?: string
  threshold?: number
  once?: boolean
}

// ponytail: single IntersectionObserver per container, lightweight reveal-on-scroll
export function useReveal(
  containerRef: Ref<HTMLElement | null>,
  options: UseRevealOptions = {},
) {
  const { selector = '[data-reveal]', threshold = 0.15, once = true } = options
  let observer: IntersectionObserver | null = null
  const reduce = typeof window !== 'undefined'
    && window.matchMedia('(prefers-reduced-motion: reduce)').matches

  onMounted(() => {
    const container = containerRef.value
    if (!container) return
    const items = Array.from(container.querySelectorAll<HTMLElement>(selector))

    if (reduce || typeof IntersectionObserver === 'undefined') {
      items.forEach((el) => el.classList.add('is-revealed'))
      return
    }

    observer = new IntersectionObserver((entries) => {
      for (const entry of entries) {
        if (entry.isIntersecting) {
          entry.target.classList.add('is-revealed')
          if (once) observer?.unobserve(entry.target)
        } else if (!once) {
          entry.target.classList.remove('is-revealed')
        }
      }
    }, { threshold, rootMargin: '0px 0px -8% 0px' })

    items.forEach((el) => observer?.observe(el))
  })

  onUnmounted(() => observer?.disconnect())
}
