<template>
  <div class="flex flex-col gap-3">
    <div class="aspect-square w-full overflow-hidden rounded-lg bg-gray-100">
      <img :src="product.imageUrl" :alt="product.name" class="h-full w-full object-cover" />
    </div>
    <div class="flex flex-col gap-1">
      <span class="font-geist text-xs uppercase tracking-wider text-[#7E7576]">{{ product.brand }}</span>
      <h3 class="font-gelasio text-base text-black">{{ product.name }}</h3>
      <p class="font-gelasio text-sm font-semibold text-black">{{ formatPrice(product.price) }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import type { Product } from '@/types'

defineProps<{ product: Product }>()

const formatPrice = (n: number) => new Intl.NumberFormat('vi-VN').format(n) + ' đ'
</script>