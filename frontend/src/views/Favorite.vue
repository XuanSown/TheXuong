<template>
  <div class="min-h-screen bg-[#F9F9F9]">
    <main class="w-full max-w-[1280px] mx-auto px-4 pt-[120px] pb-8">
      <div class="w-[1152px] mx-auto">
        <!-- Header Section -->
        <header class="flex flex-col gap-[15px] mb-16">
          <h1 class="font-geist text-[64px] font-normal leading-[70px] tracking-[-1.28px] text-black">
            {{ t('favorite.title') }}
          </h1>
          <p
            v-if="!authStore.isAuthenticated"
            class="font-gelasio text-base text-[#5E5F5C]"
          >
            <i18n-t
              keypath="favorite.guestNotice"
              tag="span"
            >
              <template #login>
                <router-link
                  to="/login?redirect=/favorite"
                  class="text-black font-semibold hover:underline"
                >
                  {{ t('favorite.login') }}
                </router-link>
              </template>
            </i18n-t>
          </p>
        </header>

        <!-- Favorites Content -->
        <div
          v-if="favoriteStore.loading"
          class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5"
        >
          <div
            v-for="i in 4"
            :key="i"
            class="w-full flex flex-col gap-4"
          >
            <BaseSkeleton type="image" />
            <div class="flex flex-col gap-2">
              <BaseSkeleton
                type="text"
                class="w-1/4"
              />
              <BaseSkeleton
                type="title"
                class="w-3/4"
              />
              <BaseSkeleton
                type="text"
                class="w-1/3"
              />
            </div>
          </div>
        </div>

        <!-- Favorites Grid -->
        <div
          v-else-if="favoriteItems.length > 0"
          class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-5"
        >
          <div
            v-for="product in favoriteItems"
            :key="product.id"
            class="group relative"
          >
            <!-- Product Card -->
            <div class="bg-white border border-[rgba(207,196,197,0.3)] rounded-lg overflow-hidden hover:shadow-lg transition-shadow">
              <!-- Image -->
              <div class="relative w-full aspect-square bg-[#F3F3F4] overflow-hidden">
                <router-link
                  :to="`/product-detail/${product.id}`"
                  class="block w-full h-full"
                >
                  <img
                    :src="product.imageUrl"
                    :alt="product.name"
                    class="w-full h-full object-cover group-hover:scale-105 transition-transform duration-300"
                    loading="lazy"
                  >
                </router-link>
                <!-- Remove from favorite button -->
                <button
                  class="absolute top-3 right-3 w-8 h-8 bg-white rounded-full flex items-center justify-center shadow-md hover:bg-red-50 transition-colors z-10"
                  :aria-label="t('product.toggleWishlist')"
                  @click="handleToggleFavorite(product)"
                >
                  <svg
                    class="w-4 h-4 text-red-500"
                    viewBox="0 0 24 24"
                    fill="currentColor"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
                  </svg>
                </button>
              </div>
              <!-- Info -->
              <div class="p-4 flex flex-col gap-2">
                <p class="font-geist text-xs text-[#5E5F5C] uppercase tracking-wider">
                  {{ product.brand || t('common.other') }}
                </p>
                <router-link
                  :to="`/product-detail/${product.id}`"
                  class="hover:underline"
                >
                  <h3 class="font-geist text-base text-black leading-tight line-clamp-2">
                    {{ product.name }}
                  </h3>
                </router-link>
                <p class="font-geist text-lg font-semibold text-black">
                  {{ formatPrice(product.price) }}
                </p>
                <button
                  class="w-full h-[40px] bg-black text-white font-geist text-xs uppercase tracking-wider hover:bg-gray-900 transition-colors mt-2"
                  @click="addToCart(product)"
                >
                  {{ t('favorite.addToCart') }}
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty State -->
        <div
          v-else
          class="flex flex-col items-center justify-center py-20"
        >
          <svg
            class="w-24 h-24 text-[#CFC4C5] mb-6"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
            stroke-linecap="round"
            stroke-linejoin="round"
          >
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z" />
          </svg>
          <p class="font-gelasio text-xl text-[#5E5F5C] mb-4">
            {{ t('favorite.empty') }}
          </p>
          <router-link
            to="/products"
            class="px-8 py-3 bg-black text-white font-geist text-sm uppercase tracking-wider hover:bg-gray-900 transition-colors"
          >
            {{ t('favorite.explore') }}
          </router-link>
        </div>
      </div>
    </main>

  <!-- Footer -->
  </div>
</template>

<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useFavoriteStore } from '@/stores/favorite.store'
import { useCartStore } from '@/stores/cart.store'
import { useI18n } from 'vue-i18n'
import { formatCurrency } from '@/utils/formatters'

import BaseSkeleton from '@/components/ui/BaseSkeleton.vue'
import type { Product } from '@/types'

const { t } = useI18n()

const router = useRouter()
const authStore = useAuthStore()
const favoriteStore = useFavoriteStore()
const cartStore = useCartStore()

const favoriteItems = computed(() => favoriteStore.items)

onMounted(() => {
  favoriteStore.fetchFavorites()
})

const formatPrice = (price: number): string => {
  return formatCurrency(price)
}

const handleToggleFavorite = (product: Product) => {
  favoriteStore.toggleFavorite(product)
}

const addToCart = (product: Product) => {
  // Find first variant with available size
  const firstSize = product.sizes?.[0]
  if (firstSize) {
    cartStore.addItem(firstSize.id, 1, {
    productId: product.id,
    productName: product.name,
    productImage: product.imageUrl,
    size: firstSize.size,
    price: product.price,
    }, authStore.isAuthenticated)
  }
  router.push('/cart')
}
</script>

<style scoped>
.line-clamp-2 {
display: -webkit-box;
-webkit-line-clamp: 2;
-webkit-box-orient: vertical;
overflow: hidden;
}
</style>
