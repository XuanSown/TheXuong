<template>
  <div
    ref="rootRef"
    class="relative min-h-screen bg-transparent text-[#1A1C1C]"
  >
    <!-- HERO -->
    <section
      ref="heroRef"
      class="relative w-full overflow-hidden bg-black text-white flex flex-col items-center justify-center"
      :style="{ height: '55dvh' }"
      @mousemove="onHeroMove"
    >
      <div class="absolute inset-0 z-0 hero-glow pointer-events-none" />
      <div class="absolute inset-0 z-0 hero-aurora pointer-events-none" />
      <div class="relative z-10 flex flex-col items-center text-center px-5 max-w-[900px]">
        <p
          class="font-geist text-[12px] uppercase tracking-[1.8px] text-white/60 mb-6 hero-anim hero-fade"
          style="animation-delay:.1s"
        >
          {{ t('tracking.eyebrow') }}
        </p>
        <h1 class="font-geist font-bold text-white leading-[0.95] mb-6">
          <span
            class="block text-5xl sm:text-7xl md:text-8xl hero-anim hero-reveal"
            style="letter-spacing:-1.28px;animation-delay:.25s"
          >{{ t('tracking.title1') }}</span>
          <span
            class="block text-4xl sm:text-5xl md:text-6xl -mt-1 hero-anim hero-reveal text-white/80"
            style="letter-spacing:-0.64px;animation-delay:.42s"
          >{{ t('tracking.title2') }}</span>
        </h1>
        <p
          class="font-geist text-base sm:text-lg text-white/70 leading-[29px] max-w-[560px] mx-auto hero-anim hero-fade"
          style="animation-delay:.6s"
        >
          {{ t('tracking.description') }}
        </p>
      </div>
    </section>

    <!-- TRACKING FORM -->
    <section class="w-full max-w-[680px] mx-auto px-4 -mt-16 relative z-10">
      <div
        class="liquid-glass rounded-2xl border border-white/60 bg-white/60 p-8 md:p-10 shadow-[0_8px_32px_rgba(26,28,28,0.15)]"
        data-reveal
      >
        <span class="glass-sheen pointer-events-none" />
        <form
          class="space-y-5"
          @submit.prevent="handleTrack"
        >
          <div>
            <label class="font-geist text-[13px] font-medium uppercase tracking-[1.2px] text-[#5E5F5C] mb-2 block">{{ t('tracking.orderCodeLabel') }}</label>
            <input
              v-model="trackingCode"
              type="text"
              :placeholder="t('tracking.orderCodePlaceholder')"
              class="w-full h-[52px] px-5 bg-white/70 border border-[#CFC4C6] rounded-xl font-geist text-[16px] text-[#1A1C1C] placeholder:text-[#CFC4C6] focus:outline-none focus:border-black transition-colors"
            >
          </div>
          <div>
            <label class="font-geist text-[13px] font-medium uppercase tracking-[1.2px] text-[#5E5F5C] mb-2 block">{{ t('tracking.contactLabel') }}</label>
            <input
              v-model="contact"
              type="text"
              :placeholder="t('tracking.contactPlaceholder')"
              class="w-full h-[52px] px-5 bg-white/70 border border-[#CFC4C6] rounded-xl font-geist text-[16px] text-[#1A1C1C] placeholder:text-[#CFC4C6] focus:outline-none focus:border-black transition-colors"
            >
          </div>
          <button
            type="submit"
            :disabled="loading || !trackingCode || !contact"
            class="w-full h-[52px] bg-black text-white font-geist text-[13px] font-semibold uppercase tracking-[1.8px] rounded-xl hover:bg-[#1A1C1C] transition-colors disabled:opacity-40 disabled:cursor-not-allowed flex items-center justify-center gap-3"
          >
            <svg
              v-if="loading"
              class="w-5 h-5 animate-spin"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
            >
              <circle
                cx="12"
                cy="12"
                r="10"
                stroke-opacity="0.3"
              />
              <path d="M12 2a10 10 0 0 1 10 10" />
            </svg>
            <span>{{ loading ? t('tracking.searching') : t('tracking.submit') }}</span>
          </button>
        </form>
      </div>
    </section>

    <!-- ERROR STATE -->
    <section
      v-if="error"
      class="w-full max-w-[680px] mx-auto px-4 py-12"
    >
      <div
        class="liquid-glass rounded-2xl border border-red-200/60 bg-red-50/60 p-8 text-center"
        data-reveal
      >
        <span class="glass-sheen pointer-events-none" />
        <div class="w-14 h-14 rounded-full bg-red-100 flex items-center justify-center mx-auto mb-5">
          <svg
            class="w-7 h-7 text-red-500"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          >
            <circle
              cx="12"
              cy="12"
              r="10"
            /><line
              x1="15"
              y1="9"
              x2="9"
              y2="15"
            /><line
              x1="9"
              y1="9"
              x2="15"
              y2="15"
            />
          </svg>
        </div>
        <h3 class="font-geist text-[20px] text-[#1A1C1C] mb-2">
          {{ t('tracking.notFound') }}
        </h3>
        <p class="font-geist text-sm text-[#5E5F5C] leading-[24px] mb-6">
          {{ error }}
        </p>
        <button
          class="px-6 py-3 border border-black rounded-lg font-geist text-[12px] font-semibold uppercase tracking-[1.2px] hover:bg-black hover:text-white transition-colors"
          @click="resetForm"
        >
          {{ t('tracking.retry') }}
        </button>
      </div>
    </section>

    <!-- RESULT -->
    <section
      v-if="order"
      class="w-full max-w-[1100px] mx-auto px-4 py-12 md:py-20 space-y-8"
    >
      <!-- Order Header -->
      <div
        class="liquid-glass rounded-2xl border border-white/60 bg-white/55 p-8 md:p-10 shadow-[0_8px_32px_rgba(26,28,28,0.10)]"
        data-reveal
      >
        <span class="glass-sheen pointer-events-none" />
        <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-5">
          <div>
            <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-2">
              {{ t('tracking.orderCode') }}
            </p>
            <h2 class="font-geist text-[28px] md:text-[32px] tracking-[-0.32px] text-[#1A1C1C]">
              #{{ order.id }}
            </h2>
          </div>
          <span :class="['inline-flex items-center gap-2 px-5 py-3 rounded-full font-geist text-[13px] font-semibold uppercase tracking-[1.2px]', getStatusBadgeClass(order.status)]">
            <span class="w-2 h-2 rounded-full bg-current" />
            {{ getStatusLabel(order.status) }}
          </span>
        </div>
        <div class="grid grid-cols-2 md:grid-cols-4 gap-6 mt-8 pt-8 border-t border-[#CFC4C6]/50">
          <div>
            <p class="font-geist text-[11px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-1">
              {{ t('tracking.placedOn') }}
            </p>
            <p class="font-geist text-[15px] text-[#1A1C1C]">
              {{ formatDate(order.createdAt) }}
            </p>
          </div>
          <div>
            <p class="font-geist text-[11px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-1">
              {{ t('tracking.payment') }}
            </p>
            <p class="font-geist text-[15px] text-[#1A1C1C]">
              {{ order.paymentMethod }}
            </p>
          </div>
          <div>
            <p class="font-geist text-[11px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-1">
              {{ t('tracking.total') }}
            </p>
            <p class="font-geist text-[15px] font-semibold text-[#1A1C1C]">
              {{ formatPrice(order.total || order.totalMoney || 0) }}
            </p>
          </div>
          <div>
            <p class="font-geist text-[11px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-1">
              {{ t('tracking.paymentStatus') }}
            </p>
            <p class="font-geist text-[15px] text-[#1A1C1C]">
              {{ order.paymentStatus === 'PAID' ? t('tracking.paid') : t('tracking.unpaid') }}
            </p>
          </div>
        </div>
      </div>

      <!-- Timeline -->
      <div
        class="liquid-glass rounded-2xl border border-white/60 bg-white/55 p-8 md:p-10 shadow-[0_8px_32px_rgba(26,28,28,0.10)]"
        data-reveal
      >
        <span class="glass-sheen pointer-events-none" />
        <h3 class="font-geist text-[20px] text-[#1A1C1C] mb-8">
          {{ t('tracking.timeline') }}
        </h3>
        <div class="relative pl-8">
          <div class="absolute left-[11px] top-2 bottom-2 w-px bg-[#CFC4C6]" />
          <div
            v-for="(step, i) in timelineSteps"
            :key="i"
            class="relative pb-8 last:pb-0"
          >
            <div
              :class="[
                'absolute left-[-21px] top-1 w-[22px] h-[22px] rounded-full border-2 flex items-center justify-center transition-all',
                step.active
                  ? 'bg-black border-black'
                  : 'bg-white border-[#CFC4C6]'
              ]"
            >
              <svg
                v-if="step.active"
                class="w-3 h-3 text-white"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="3"
                stroke-linecap="round"
              >
                <polyline points="20 6 9 17 4 12" />
              </svg>
            </div>
            <div class="ml-4">
              <p :class="['font-geist text-[15px] font-semibold', step.active ? 'text-[#1A1C1C]' : 'text-[#CFC4C6]']">
                {{ step.label }}
              </p>
              <p
                v-if="step.time"
                class="font-geist text-[13px] text-[#5E5F5C] mt-1"
              >
                {{ step.time }}
              </p>
            </div>
          </div>
        </div>
      </div>

      <!-- Shipping Info + Items -->
      <div class="grid md:grid-cols-2 gap-5">
        <!-- Shipping -->
        <div
          class="liquid-glass rounded-2xl border border-white/60 bg-white/55 p-8 shadow-[0_8px_32px_rgba(26,28,28,0.10)]"
          data-reveal
        >
          <span class="glass-sheen pointer-events-none" />
          <h3 class="font-geist text-[20px] text-[#1A1C1C] mb-6">
            {{ t('tracking.shippingInfo') }}
          </h3>
          <div class="space-y-4">
            <div>
              <p class="font-geist text-[11px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-1">
                {{ t('tracking.recipient') }}
              </p>
              <p class="font-geist text-[15px] text-[#1A1C1C]">
                {{ order.fullName || order.shippingName }}
              </p>
            </div>
            <div>
              <p class="font-geist text-[11px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-1">
                {{ t('tracking.phone') }}
              </p>
              <p class="font-geist text-[15px] text-[#1A1C1C]">
                {{ order.phoneNumber || order.shippingPhone }}
              </p>
            </div>
            <div>
              <p class="font-geist text-[11px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-1">
                {{ t('tracking.address') }}
              </p>
              <p class="font-geist text-[15px] text-[#1A1C1C] leading-[24px]">
                {{ order.address || order.shippingAddress }}
              </p>
            </div>
            <div v-if="order.note">
              <p class="font-geist text-[11px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-1">
                {{ t('order.note') }}
              </p>
              <p class="font-geist text-[15px] text-[#5E5F5C] leading-[24px]">
                {{ order.note }}
              </p>
            </div>
          </div>
        </div>

        <!-- Items -->
        <div
          class="liquid-glass rounded-2xl border border-white/60 bg-white/55 p-8 shadow-[0_8px_32px_rgba(26,28,28,0.10)]"
          data-reveal
        >
          <span class="glass-sheen pointer-events-none" />
          <h3 class="font-geist text-[20px] text-[#1A1C1C] mb-6">
            {{ t('tracking.products', { count: order.items?.length || 0 }) }}
          </h3>
          <div class="space-y-4">
            <div
              v-for="(item, i) in order.items"
              :key="i"
              class="flex items-center gap-4 pb-4 border-b border-[#CFC4C6]/30 last:border-0 last:pb-0"
            >
              <div class="w-[60px] h-[60px] rounded-lg bg-[#F4F5F7] flex-shrink-0 overflow-hidden">
                <img
                  v-if="item.imageUrl"
                  :src="item.imageUrl"
                  :alt="item.productName"
                  class="w-full h-full object-cover"
                >
                <div
                  v-else
                  class="w-full h-full flex items-center justify-center text-[#CFC4C6]"
                >
                  <svg
                    class="w-6 h-6"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="1.5"
                  >
                    <rect
                      x="3"
                      y="3"
                      width="18"
                      height="18"
                      rx="2"
                    /><circle
                      cx="8.5"
                      cy="8.5"
                      r="1.5"
                    /><path d="m21 15-5-5L5 21" />
                  </svg>
                </div>
              </div>
              <div class="flex-1 min-w-0">
                <p class="font-geist text-[14px] text-[#1A1C1C] truncate">
                  {{ item.productName }}
                </p>
                <p class="font-geist text-[12px] text-[#5E5F5C]">
                  {{ t('tracking.itemSize', { size: item.size, qty: item.quantity }) }}
                </p>
              </div>
              <p class="font-geist text-[14px] font-semibold text-[#1A1C1C] flex-shrink-0">
                {{ formatPrice(item.price * item.quantity) }}
              </p>
            </div>
          </div>
          <div class="pt-4 mt-4 border-t border-[#CFC4C6] space-y-2">
            <div class="flex justify-between">
              <span class="font-geist text-[13px] text-[#5E5F5C]">{{ t('common.subtotal') }}</span>
              <span class="font-geist text-[14px] text-[#1A1C1C]">{{ formatPrice(order.subtotal || 0) }}</span>
            </div>
            <div class="flex justify-between">
              <span class="font-geist text-[13px] text-[#5E5F5C]">{{ t('cart.shippingFee') }}</span>
              <span class="font-geist text-[14px] text-[#1A1C1C]">{{ formatPrice(order.shippingFee || 0) }}</span>
            </div>
            <div class="flex justify-between pt-2 border-t border-[#CFC4C6]/30">
              <span class="font-geist text-[15px] font-semibold text-[#1A1C1C]">{{ t('common.total') }}</span>
              <span class="font-geist text-[15px] font-semibold text-[#1A1C1C]">{{ formatPrice(order.total || order.totalMoney || 0) }}</span>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA -->
    <section
      v-if="!order && !error"
      class="w-full max-w-[680px] mx-auto px-4 py-16 text-center"
      data-reveal
    >
      <p class="font-geist text-sm text-[#5E5F5C] leading-[26px] mb-6 whitespace-pre-line">
        {{ t('tracking.helpText') }}
      </p>
      <router-link
        to="/contact"
        class="inline-flex items-center gap-2 px-6 py-3 border border-black rounded-lg font-geist text-[12px] font-semibold uppercase tracking-[1.2px] hover:bg-black hover:text-white transition-colors"
      >
        {{ t('tracking.contact') }}
      </router-link>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onUnmounted } from 'vue'
import { useReveal } from '@/composables/useReveal'
import { orderService } from '@/services/order.service'
import type { Order } from '@/types'
import { useI18n } from 'vue-i18n'
import { formatCurrency, formatDate as formatDateByLocale } from '@/utils/formatters'

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

const trackingCode = ref('')
const contact = ref('')
const loading = ref(false)
const error = ref('')
const order = ref<Order | null>(null)

const statusOrder = ['PENDING', 'CONFIRMED', 'SHIPPING', 'DELIVERED', 'COMPLETED']

const timelineSteps = computed(() => {
  const steps = [
    { label: t('tracking.stepPlaced'), key: 'PENDING' },
    { label: t('tracking.stepConfirmed'), key: 'CONFIRMED' },
    { label: t('tracking.stepShipping'), key: 'SHIPPING' },
    { label: t('tracking.stepDelivered'), key: 'DELIVERED' },
    { label: t('tracking.stepCompleted'), key: 'COMPLETED' }
  ]
  const currentIdx = order.value
    ? statusOrder.indexOf(order.value.status)
    : -1
  const isCancelled = order.value?.status === 'CANCELLED'
  return steps.map((s, i) => ({
    label: s.label,
    active: isCancelled ? false : i <= currentIdx,
    time: i <= currentIdx ? formatDate(order.value?.createdAt || '') : undefined
  }))
})

const handleTrack = async () => {
  if (!trackingCode.value || !contact.value) return
  loading.value = true
  error.value = ''
  order.value = null
  try {
    const code = trackingCode.value.replace('#', '').replace(/[^0-9]/g, '')
    const id = parseInt(code)
    if (isNaN(id)) {
      error.value = t('tracking.invalidCode')
      return
    }
    const result = await orderService.getOrder(id)
    order.value = result
  } catch {
    error.value = t('tracking.notFoundDetail')
  } finally {
    loading.value = false
  }
}

const resetForm = () => {
  trackingCode.value = ''
  contact.value = ''
  error.value = ''
  order.value = null
}

const getStatusBadgeClass = (status: string) => {
  switch (status) {
    case 'PENDING': return 'bg-[#FEF9C3] text-[#854D0E]'
    case 'CONFIRMED': return 'bg-[#E0E7FF] text-[#3730A3]'
    case 'SHIPPING': return 'bg-blue-100 text-blue-700'
    case 'DELIVERED': return 'bg-[#DCFCE7] text-[#166534]'
    case 'COMPLETED': return 'bg-[#D1FAE5] text-[#065F46]'
    case 'CANCELLED': return 'bg-red-100 text-red-600'
    case 'REFUNDED': return 'bg-[#F3E8FF] text-[#6B21A8]'
    default: return 'bg-gray-100 text-gray-600'
  }
}

const getStatusLabel = (status: string) => {
  const keyMap: Record<string, string> = {
    PENDING: 'orderStatus.pending',
    CONFIRMED: 'orderStatus.confirmed',
    SHIPPING: 'orderStatus.shipping',
    DELIVERED: 'orderStatus.delivered',
    COMPLETED: 'orderStatus.completed',
    CANCELLED: 'orderStatus.cancelled',
    REFUNDED: 'orderStatus.refunded'
  }
  return keyMap[status] ? t(keyMap[status]) : status
}

const formatDate = (dateString: string) => formatDateByLocale(dateString)

const formatPrice = (price: number) => {
  return formatCurrency(price)
}
</script>

<style scoped>
.liquid-glass {
  -webkit-backdrop-filter: blur(18px) saturate(160%);
  backdrop-filter: blur(18px) saturate(160%);
  background-image: linear-gradient(135deg, rgba(255,255,255,0.55), rgba(255,255,255,0.18));
  position: relative;
  overflow: hidden;
}

.glass-sheen {
  position: absolute; inset: 0; border-radius: inherit;
  background: linear-gradient(135deg, rgba(255,255,255,0.55) 0%, transparent 42%),
    radial-gradient(120% 80% at 50% -20%, rgba(255,255,255,0.35), transparent 60%);
  pointer-events: none; opacity: 0.9;
}

.hero-aurora {
  background:
    radial-gradient(40% 60% at 20% 20%, rgba(255,107,53,0.18), transparent 70%),
    radial-gradient(45% 55% at 80% 30%, rgba(14,165,233,0.14), transparent 70%),
    radial-gradient(50% 60% at 50% 90%, rgba(120,255,220,0.12), transparent 70%);
  filter: blur(20px);
  animation: auroraDrift 14s ease-in-out infinite alternate;
  transform: translate3d(0,0,0);
}

@keyframes auroraDrift {
  0% { transform: translate3d(-3%,-2%,0) scale(1.05); }
  100% { transform: translate3d(3%,2%,0) scale(1.12); }
}

[data-reveal] {
  opacity: 0; transform: translateY(34px); filter: blur(8px);
  transition: opacity 0.9s cubic-bezier(0.16,1,0.3,1),
    transform 0.9s cubic-bezier(0.16,1,0.3,1),
    filter 0.9s cubic-bezier(0.16,1,0.3,1);
  will-change: opacity, transform, filter;
}

[data-reveal].is-revealed { opacity: 1; transform: translateY(0); filter: blur(0); }

.hero-glow {
  background: radial-gradient(600px circle at var(--mx,50%) var(--my,50%), rgba(255,255,255,0.12), transparent 60%);
}

@keyframes heroReveal {
  0% { opacity: 0; transform: translateY(28px); filter: blur(12px); }
  100% { opacity: 1; transform: translateY(0); filter: blur(0); }
}

@keyframes heroFadeUp {
  0% { opacity: 0; transform: translateY(20px); }
  100% { opacity: 1; transform: translateY(0); }
}

.hero-anim { opacity: 0; animation-fill-mode: forwards; animation-timing-function: cubic-bezier(0.16,1,0.3,1); }
.hero-reveal { animation-name: heroReveal; animation-duration: 1.1s; }
.hero-fade { animation-name: heroFadeUp; animation-duration: 1s; }

@media (prefers-reduced-motion: reduce) {
  .hero-anim, .hero-aurora { animation: none; opacity: 1; }
  .hero-glow, .hero-aurora { display: none; }
  [data-reveal] { opacity: 1; transform: none; filter: none; transition: none; }
}
</style>
