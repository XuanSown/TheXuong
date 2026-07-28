<template>
<<<<<<< HEAD
  <div class="flex flex-col gap-3">
    <div class="aspect-square w-full overflow-hidden rounded-lg bg-gray-100">
      <img :src="product.imageUrl" :alt="product.name" class="h-full w-full object-cover" />
    </div>
    <div class="flex flex-col gap-1">
      <span class="font-geist text-xs uppercase tracking-wider text-[#7E7576]">{{ product.brand }}</span>
      <h3 class="font-gelasio text-base text-black">{{ product.name }}</h3>
      <p class="font-gelasio text-sm font-semibold text-black">{{ formatPrice(product.price) }}</p>
=======
  <div
    class="w-full flex flex-col gap-4 group cursor-pointer"
    @click="goToDetail"
  >
    <div class="w-full aspect-square bg-[#EEEEEE] border border-[#CFC4C6] overflow-hidden relative">
      <img
        v-if="product.imageUrl"
        :src="product.imageUrl"
        :alt="product.name"
        loading="lazy"
        class="w-full h-full object-cover transition-transform duration-500 group-hover:scale-105"
      >
      <div
        v-else
        class="w-full h-full flex items-center justify-center text-gray-400"
      >
        <svg
          class="w-12 h-12"
          fill="none"
          stroke="currentColor"
          viewBox="0 0 24 24"
        >
          <path
            stroke-linecap="round"
            stroke-linejoin="round"
            stroke-width="2"
            d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"
          />
        </svg>
      </div>
    </div>
    <div class="flex flex-col gap-2">
      <p class="text-[10px] uppercase tracking-[1px] text-[#5E5F5C]">
        {{ sportLabel }}
      </p>
      <h3 class="font-gelasio text-lg font-bold text-[#1A1C1C] leading-[29px] group-hover:underline decoration-1 underline-offset-4">
        {{ product.name }}
      </h3>
      <p class="font-geist text-base font-semibold text-black leading-[24px]">
        {{ formattedPrice }}
      </p>
>>>>>>> fb265d4 (restore: code lost to ponytail full (2026-07-27) from stash@{0})
    </div>
  </div>
</template>

<script setup lang="ts">
<<<<<<< HEAD
import type { Product } from '@/types'

defineProps<{ product: Product }>()

const formatPrice = (n: number) => new Intl.NumberFormat('vi-VN').format(n) + ' đ'
</script>
=======
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import type { Product } from '@/types'
import { formatCurrency } from '@/utils/formatters'

const props = defineProps<{
  product: Product
}>()

const router = useRouter()

const goToDetail = () => {
  router.push(`/product-detail/${props.product.id}`)
}

const formattedPrice = computed(() => {
  return formatCurrency(props.product.price)
})

const sportLabel = computed(() => {
  const sport = props.product.sport?.toLowerCase() || ''
  const labels: Record<string, string> = {
    football: 'BÓNG ĐÁ',
    badminton: 'CẦU LÔNG',
    running: 'CHẠY BỘ',
    basketball: 'BÓNG RỔ',
    other: 'KHÁC'
  }
  return labels[sport] || sport.toUpperCase()
})
</script>

<style scoped>
.font-geist {
  font-family: 'Geist', sans-serif;
}

.font-gelasio {
  font-family: 'Geist', sans-serif;
}
</style>
>>>>>>> fb265d4 (restore: code lost to ponytail full (2026-07-27) from stash@{0})
