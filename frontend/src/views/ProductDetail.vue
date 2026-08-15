<template>
  <div class="min-h-screen">
    <main class="w-full max-w-[1280px] mx-auto px-4 pb-8">
      <!-- Breadcrumbs -->
      <nav class="w-[1152px] mx-auto mb-8 flex items-center gap-2">
        <router-link
          to="/"
          class="font-geist text-[12px] font-semibold uppercase tracking-[1.8px] text-[#646562] hover:text-black transition-colors"
        >
          {{ t('common.home') }}
        </router-link>
        <svg
          class="w-[4.32px] h-[7px]"
          viewBox="0 0 4 7"
          fill="currentColor"
        >
          <path
            d="M0 3.5L4 0V7L0 3.5Z"
            fill="black"
          />
        </svg>
        <router-link
          to="/products"
          class="font-geist text-[12px] font-semibold uppercase tracking-[1.8px] text-[#646562] hover:text-black transition-colors"
        >
          {{ t('nav.products') }}
        </router-link>
        <svg
          class="w-[4.32px] h-[7px]"
          viewBox="0 0 4 7"
          fill="currentColor"
        >
          <path
            d="M0 3.5L4 0V7L0 3.5Z"
            fill="black"
          />
        </svg>
        <span class="font-geist text-[12px] font-semibold uppercase tracking-[1.8px] text-black">
          {{ productName }}
        </span>
      </nav>

      <!-- Product Detail Section -->
      <section
        v-if="product"
        class="w-[1152px] mx-auto bg-white mb-8"
      >
        <div class="flex gap-8 p-8">
          <!-- Left: Image Gallery -->
          <div class="flex gap-4">
            <!-- Thumbnail Column -->
            <div class="flex flex-col gap-4">
              <div
                v-for="(img, idx) in product.images"
                :key="idx"
                class="w-[80px] h-[80px] bg-[#F3F3F4] border flex items-center justify-center cursor-pointer transition-colors"
                :class="mainImage === img ? 'border-black' : 'border-[rgba(207,196,197,0.3)] hover:border-black'"
                @click="mainImage = img; zoomLevel = 1"
              >
                <img
                  :src="img"
                  :alt="product.name"
                  class="w-[78px] h-[78px] object-cover"
                >
              </div>
            </div>

            <!-- Main Image -->
            <div
              class="relative overflow-hidden w-[556px] h-[556px] bg-[#F3F3F4] border border-[rgba(207,196,197,0.3)] flex items-center justify-center"
            >
              <img
                :src="mainImage"
                :alt="product.name"
                class="w-[554px] h-[554px] object-cover transition-transform duration-300 origin-center"
                :style="{ transform: `scale(${zoomLevel})` }"
              >

              <!-- Zoom Controls -->
              <div class="absolute right-4 bottom-4 flex gap-2">
                <button
                  class="w-10 h-10 bg-white/80 backdrop-blur-[4px] rounded-full flex items-center justify-center shadow hover:bg-white transition-colors"
                  :aria-label="t('product.zoomIn')"
                  @click="handleZoomIn"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke-width="1.5"
                    stroke="currentColor"
                    class="w-5 h-5 text-black"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607zM10.5 7.5v6m3-3h-6"
                    />
                  </svg>
                </button>
                <button
                  class="w-10 h-10 bg-white/80 backdrop-blur-[4px] rounded-full flex items-center justify-center shadow hover:bg-white transition-colors"
                  :aria-label="t('product.zoomOut')"
                  @click="handleZoomOut"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke-width="1.5"
                    stroke="currentColor"
                    class="w-5 h-5 text-black"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      d="M21 21l-5.197-5.197m0 0A7.5 7.5 0 105.196 5.196a7.5 7.5 0 0010.607 10.607zM13.5 10.5h-6"
                    />
                  </svg>
                </button>
              </div>
            </div>
          </div>

          <!-- Right: Product Info -->
          <div class="flex-1 flex flex-col gap-8 max-w-[452px]">
            <!-- Header -->
            <div class="flex flex-col gap-[15px]">
              <div class="flex items-center gap-3">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke-width="1.5"
                  stroke="currentColor"
                  class="w-4 h-4 text-[#4C4546]"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="M2.036 12.322a1.012 1.012 0 010-.639C3.423 7.51 7.36 4.5 12 4.5c4.638 0 8.573 3.007 9.963 7.178.07.207.07.431 0 .639C20.577 16.49 16.64 19.5 12 19.5c-4.638 0-8.573-3.007-9.963-7.178z"
                  />
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"
                  />
                </svg>
                <span class="font-geist text-base text-[#4C4546]">{{ t('product.views', { count: product.viewCount }) }}</span>
              </div>

              <h1 class="font-geist text-[32px] font-semibold leading-[38px] tracking-[-0.32px] text-black">
                {{ product.name }}
              </h1>

              <div class="flex items-center gap-4">
                <span class="font-inter text-[32px] font-semibold leading-[38px] text-black">{{
                  formatPrice(product.price) }}</span>
                <button
                  class="w-10 h-10 flex items-center justify-center rounded-full hover:bg-gray-100 transition-all active:scale-90"
                  :aria-label="t('product.toggleWishlist')"
                  @click="handleToggleFavorite"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    :fill="product && favoriteStore.isFavorite(product.id) ? 'currentColor' : 'none'"
                    viewBox="0 0 24 24" 
                    :stroke-width="product && favoriteStore.isFavorite(product.id) ? '0' : '1.5'" 
                    stroke="currentColor" 
                    class="w-6 h-6 transition-colors"
                    :class="product && favoriteStore.isFavorite(product.id) ? 'text-[#FF4A4A]' : 'text-[#4C4546]'"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      d="M21 8.25c0-2.485-2.099-4.5-4.688-4.5-1.935 0-3.597 1.126-4.312 2.733-.715-1.607-2.377-2.733-4.313-2.733C5.1 3.75 3 5.765 3 8.25c0 7.22 9 12 9 12s9-4.78 9-12z"
                    />
                  </svg>
                </button>
              </div>

              <hr class="border-[#CFC4C5]">

              <p class="font-gelasio text-base text-[#4C4546] leading-[26px]">
                {{ product.description }}
              </p>
            </div>

            <!-- Size Selector -->
            <div class="flex flex-col gap-4">
              <div class="flex items-center gap-2">
                <span class="font-geist text-[12px] font-bold uppercase tracking-[1.2px] text-black">
                  {{ t('product.selectSize') }}
                </span>
              </div>

              <div class="flex gap-2">
                <button
                  v-for="size in product.sizes"
                  :key="size.id"
                  :class="[
                    'w-[81px] h-[48px] border flex items-center justify-center font-geist text-base text-[#1A1C1C] transition-colors',
                    selectedSize === size.name
                      ? 'border-black bg-black text-white hover:bg-gray-900'
                      : 'border-[#7E7576] hover:border-black hover:bg-black hover:text-white'
                  ]"
                  @click="selectedSize = size.name"
                >
                  {{ size.name }}
                </button>
              </div>
              <p
                v-if="!selectedSize"
                class="text-red-500 text-sm"
              >
                {{ t('product.pleaseSelectSize') }}
              </p>
            </div>

            <!-- Quantity Selector -->
            <div class="flex flex-col gap-4">
              <div class="flex items-center gap-2">
                <span class="font-geist text-[12px] font-bold uppercase tracking-[1.2px] text-black">
                  {{ t('common.quantity') }}:
                </span>
              </div>
              <div class="flex items-center gap-4">
                <div class="flex items-center border border-[rgba(207,196,197,0.3)] w-[98px] h-[34px]">
                  <button
                    class="w-8 h-8 flex items-center justify-center border-r border-[rgba(207,196,197,0.3)] hover:bg-gray-50 transition-colors"
                    @click="quantity > 1 && (quantity--)"
                  >
                    <span
                      class="text-[#5E5F5C] font-inter text-sm"
                      style="font-weight: 600; letter-spacing: 0.7px; text-transform: uppercase;"
                    >-</span>
                  </button>
                  <span class="flex-1 text-center font-inter text-base text-[#1A1C1C]">{{ quantity }}</span>
                  <button
                    class="w-8 h-8 flex items-center justify-center border-l border-[rgba(207,196,197,0.3)] hover:bg-gray-50 transition-colors"
                    @click="quantity++"
                  >
                    <span
                      class="text-[#5E5F5C] font-inter text-sm"
                      style="font-weight: 600; letter-spacing: 0.7px; text-transform: uppercase;"
                    >+</span>
                  </button>
                </div>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex flex-col gap-3">
              <button
                class="w-full h-[56px] bg-white border-2 border-black text-black font-geist text-base flex items-center justify-center hover:bg-black hover:text-white transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="isAdding || !selectedSize"
                @click="handleAddToCart"
              >
                <span v-if="!isAdding">{{ t('cart.addToCart') }}</span>
                <span
                  v-else
                  class="flex items-center gap-2"
                >
                  <svg
                    class="animate-spin w-5 h-5"
                    viewBox="0 0 24 24"
                    fill="none"
                  >
                    <circle
                      class="opacity-25"
                      cx="12"
                      cy="12"
                      r="10"
                      stroke="currentColor"
                      stroke-width="4"
                    />
                    <path
                      class="opacity-75"
                      fill="currentColor"
                      d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                    />
                  </svg>
                  {{ t('product.adding') }}
                </span>
              </button>
              <button
                class="w-full h-[56px] bg-black text-white font-geist text-base flex items-center justify-center hover:bg-gray-900 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="!selectedSize"
                @click="handleBuyNow"
              >
                {{ t('product.buyNow') }}
              </button>
            </div>

            <!-- Trust Badges -->
            <div class="w-full bg-[#F3F3F4] p-4 flex justify-between items-center">
              <!-- 30-day returns -->
              <div class="flex flex-col items-center gap-3 flex-1">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke-width="1.5"
                  stroke="currentColor"
                  class="w-6 h-6 text-black"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="M16.023 9.348h4.992v-.001M2.985 19.644v-4.992m0 0h4.992m-4.993 0l3.181 3.183a8.25 8.25 0 0013.803-3.7M4.031 9.865a8.25 8.25 0 0113.803-3.7l3.181 3.182m0-4.991v4.99"
                  />
                </svg>
                <span class="font-geist text-[10px] leading-[12px] text-center text-[#1A1C1C] whitespace-pre-line">
                  {{ t('product.returns30d') }}
                </span>
              </div>

              <!-- Warranty -->
              <div class="flex flex-col items-center gap-3 flex-1">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke-width="1.5"
                  stroke="currentColor"
                  class="w-6 h-6 text-black"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="M9 12.75L11.25 15 15 9.75m-3-7.036A11.959 11.959 0 013.598 6 11.99 11.99 0 003 9.749c0 5.592 3.824 10.29 9 11.623 5.176-1.332 9-6.03 9-11.622 0-1.31-.21-2.571-.598-3.751h-.152c-3.196 0-6.1-1.248-8.25-3.285z"
                  />
                </svg>
                <span class="font-geist text-[10px] leading-[12px] text-center text-[#1A1C1C] whitespace-pre-line">
                  {{ t('product.genuineWarranty') }}
                </span>
              </div>

              <!-- Free Shipping -->
              <div class="flex flex-col items-center gap-3 flex-1">
                <svg
                  xmlns="http://www.w3.org/2000/svg"
                  fill="none"
                  viewBox="0 0 24 24"
                  stroke-width="1.5"
                  stroke="currentColor"
                  class="w-6 h-6 text-black"
                >
                  <path
                    stroke-linecap="round"
                    stroke-linejoin="round"
                    d="M8.25 18.75a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h6m-9 0H3.375a1.125 1.125 0 01-1.125-1.125V14.25m17.25 4.5a1.5 1.5 0 01-3 0m3 0a1.5 1.5 0 00-3 0m3 0h1.125c.621 0 1.129-.504 1.09-1.124a17.902 17.902 0 00-3.213-9.193 2.056 2.056 0 00-1.58-.86H14.25M16.5 18.75h-2.25m0-11.177v-.958c0-.568-.422-1.048-.987-1.106a48.554 48.554 0 00-10.026 0 1.106 1.106 0 00-.987 1.106v7.635m12-6.677v6.677m0 4.5v-4.5m0 0h-12"
                  />
                </svg>
                <span class="font-geist text-[10px] leading-[12px] text-center text-[#1A1C1C] whitespace-pre-line">
                  {{ t('product.freeShipping') }}
                </span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Loading State -->
      <div
        v-else-if="loading"
        class="w-[1152px] mx-auto bg-white mb-8 p-8 flex gap-8"
      >
        <div class="flex gap-4">
          <div class="flex flex-col gap-4">
            <BaseSkeleton
              v-for="i in 3"
              :key="i"
              type="image"
              class="w-[80px] h-[80px]"
            />
          </div>
          <BaseSkeleton
            type="image"
            class="w-[556px] h-[556px]"
          />
        </div>
        <div class="flex-1 flex flex-col gap-8 max-w-[452px]">
          <div class="flex flex-col gap-[15px]">
            <BaseSkeleton
              type="text"
              class="w-1/4"
            />
            <BaseSkeleton
              type="title"
              class="w-3/4"
            />
            <BaseSkeleton
              type="title"
              class="w-1/3 h-10"
            />
            <BaseSkeleton
              type="text"
              class="w-full h-24"
            />
          </div>
          <BaseSkeleton
            type="text"
            class="w-full h-12"
          />
          <BaseSkeleton
            type="text"
            class="w-full h-12"
          />
          <BaseSkeleton
            type="text"
            class="w-full h-32"
          />
        </div>
      </div>

      <!-- Not Found -->
      <div
        v-else
        class="text-center py-16"
      >
        <p class="font-gelasio text-xl text-[#5E5F5C] mb-8">
          {{ t('errors.productNotFound') }}
        </p>
        <router-link
          to="/products"
          class="text-black hover:underline"
        >
          {{ t('product.backToList') }}
        </router-link>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart.store'
import { useAuthStore } from '@/stores/auth.store'
import { useFavoriteStore } from '@/stores/favorite.store'
import BaseSkeleton from '@/components/ui/BaseSkeleton.vue'
import { productService } from '@/services/product.service'
import { useToast } from 'vue-toastification'
import { useI18n } from 'vue-i18n'
import { formatCurrency } from '@/utils/formatters'

const { t } = useI18n()

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const authStore = useAuthStore()
const favoriteStore = useFavoriteStore()
const toast = useToast()

const product = ref<any>(null)
const loading = ref(true)
const selectedSize = ref<string | null>(null)
const quantity = ref(1)
const isAdding = ref(false)

const mainImage = ref<string>('')
const zoomLevel = ref<number>(1)

const handleZoomIn = () => {
  if (zoomLevel.value < 2.5) zoomLevel.value += 0.5
}
const handleZoomOut = () => {
  if (zoomLevel.value > 1) zoomLevel.value -= 0.5
}

// Get product ID from route
const productId = computed(() => Number(route.params.id))

const productName = computed(() => product.value?.name || t('product.genericName'))

const formatPrice = (price: number): string => {
  return formatCurrency(price)
}

const handleToggleFavorite = () => {
  if (product.value) {
    favoriteStore.toggleFavorite(product.value)
    if (favoriteStore.isFavorite(product.value.id)) {
      toast.success(t('toast.addedToFavorite'))
    } else {
      toast.info(t('toast.removedFromFavorite'))
    }
  }
}

const getSelectedVariant = computed(() => {
  if (!product.value || !selectedSize.value) return null
  return product.value.sizes.find((s: any) => s.name === selectedSize.value)
})

const handleAddToCart = async () => {
  if (!selectedSize.value || !product.value) return

  isAdding.value = true
  try {
    const variant = getSelectedVariant.value
    await cartStore.addItem(variant.id, quantity.value, {
      variantId: variant.id,
      productId: product.value.id,
      productName: product.value.name,
      productImage: product.value.imageUrl,
      size: selectedSize.value,
      price: product.value.price
    }, authStore.isAuthenticated)
    toast.success(t('toast.addedToCart'))
  } catch (error) {
    console.error('Failed to add to cart:', error)
    toast.error(t('toast.addToCartFailed'))
  } finally {
    isAdding.value = false
  }
}

const handleBuyNow = async () => {
  if (!selectedSize.value || !product.value) return

  isAdding.value = true
  try {
    const variant = getSelectedVariant.value
    await cartStore.addItem(variant.id, quantity.value, {
      variantId: variant.id,
      productId: product.value.id,
      productName: product.value.name,
      productImage: product.value.imageUrl,
      size: selectedSize.value,
      price: product.value.price
    }, authStore.isAuthenticated)

    if (!authStore.isAuthenticated) {
      authStore.setRedirectPath('/checkout')
      router.push('/login')
    } else {
      router.push('/checkout')
    }
  } catch (error) {
    console.error('Failed to add to cart:', error)
    toast.error(t('toast.addToCartFailed'))
  } finally {
    isAdding.value = false
  }
}

onMounted(async () => {
  if (!productId.value) {
    loading.value = false
    return
  }

  loading.value = true
  try {
    const productData = await productService.getProduct(productId.value)

    // API now returns real images array from ProductImage table
    if (productData && !productData.images && productData.imageUrl) {
      productData.images = [productData.imageUrl]
    }

    product.value = productData
    mainImage.value = productData.imageUrl
  } catch (error) {
    console.error('Failed to fetch product:', error)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.font-inter {
  font-family: 'Geist', sans-serif;
}
</style>
