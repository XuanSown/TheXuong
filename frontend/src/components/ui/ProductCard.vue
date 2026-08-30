<template>
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
        v-if="product.stockQuantity !== undefined && product.stockQuantity <= 0"
        class="absolute inset-0 bg-black/40 flex items-center justify-center"
      >
        <span class="bg-red-500 text-white text-xs font-bold px-3 py-1 rounded">HẾT HÀNG</span>
      </div>
      <div
        v-else-if="!product.imageUrl"
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
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useI18n } from 'vue-i18n'
import { formatCurrency } from '@/utils/formatters'
import { sportTranslationPath } from '@/i18n/labels'

interface ProductCardProduct {
  id: number
  name: string
  price: number
  imageUrl: string
  sport: string
  stockQuantity?: number
}

const props = defineProps<{
  product: ProductCardProduct
}>()

const router = useRouter()
const { t } = useI18n()

const goToDetail = () => {
  router.push(`/product-detail/${props.product.id}`)
}

const formattedPrice = computed(() => {
  return formatCurrency(props.product.price)
})

const sportLabel = computed(() => {
  return t(sportTranslationPath(props.product.sport)).toUpperCase()
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
