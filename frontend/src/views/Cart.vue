<template>
  <div class="min-h-screen bg-[#F9F9F9]">
    <main class="w-full max-w-[1280px] mx-auto px-4 pt-[120px] pb-8">
      <div class="w-full max-w-[1152px] mx-auto">
        <!-- Header Section -->
        <header class="flex flex-col gap-[15px] mb-16">
          <h1 class="font-geist text-[64px] font-normal leading-[70px] tracking-[-1.28px] text-black">
            {{ t('cart.title') }}
          </h1>
          <p
            v-if="!authStore.isAuthenticated"
            class="font-gelasio text-base text-[#5E5F5C]"
          >
            <i18n-t
              keypath="cart.guestNotice"
              tag="span"
            >
              <template #login>
                <router-link
                  to="/login?redirect=/cart"
                  class="text-black font-semibold hover:underline"
                >
                  {{ t('cart.login') }}
                </router-link>
              </template>
            </i18n-t>
          </p>
        </header>

        <!-- Cart Content -->
        <div
          v-if="cartItems.length > 0"
          class="flex flex-col lg:flex-row gap-8 mb-16"
        >
          <!-- Cart Items List -->
          <div class="flex-1 flex flex-col gap-8">
            <div
              v-for="item in cartItems"
              :key="item.variantId"
              class="border-b border-[rgba(207,196,197,0.3)] pb-8"
            >
              <div class="flex gap-6">
                <!-- Product Image -->
                <div class="w-[160px] h-[160px] bg-[#F3F3F4] flex-shrink-0">
                  <img
                    v-if="item.productImage"
                    :src="item.productImage"
                    :alt="item.productName"
                    class="w-full h-full object-cover"
                    loading="lazy"
                  >
                  <div
                    v-else
                    class="w-full h-full bg-gray-200 flex items-center justify-center"
                  >
                    <span class="text-gray-400 text-sm">{{ t('cart.noImage') }}</span>
                  </div>
                </div>

                <!-- Product Info -->
                <div class="flex-1 flex flex-col justify-between">
                  <div class="flex flex-col gap-[5.22px]">
                    <h3 class="font-geist text-2xl font-normal leading-[38px] tracking-[-0.32px] text-black">
                      {{ item.productName }}
                    </h3>
                    <p
                      v-if="item.size"
                      class="font-geist text-sm text-[#5E5F5C]"
                    >
                      {{ t('cart.itemSize', { size: item.size }) }}
                    </p>
                    <p v-if="item.stockQuantity !== undefined && item.stockQuantity <= 0" class="text-red-500 text-xs font-semibold mt-1">
                      ⚠️ Size này hiện đã hết hàng. Vui lòng xóa khỏi giỏ.
                    </p>
                    <p v-else-if="item.stockQuantity !== undefined && item.quantity > item.stockQuantity" class="text-orange-500 text-xs font-semibold mt-1">
                      ⚠️ Kho chỉ còn {{ item.stockQuantity }} sản phẩm. Vui lòng giảm số lượng.
                    </p>
                  </div>

                  <div class="flex justify-between items-end">
                    <!-- Quantity Controls -->
                    <div class="flex items-center gap-4">
                      <div class="flex items-center border border-[rgba(207,196,197,0.3)] w-[98px] h-[34px]">
                        <button
                          class="w-8 h-8 flex items-center justify-center border-r border-[rgba(207,196,197,0.3)] hover:bg-gray-50 transition-colors"
                          :disabled="isUpdating"
                          @click="decreaseQuantity(item)"
                        >
                          <span
                            class="text-[#5E5F5C] font-inter text-sm"
                            style="font-weight: 600; letter-spacing: 0.7px; text-transform: uppercase;"
                          >-</span>
                        </button>
                        <span class="flex-1 text-center font-inter text-base text-[#1A1C1C]">{{ item.quantity }}</span>
                        <button
                          class="w-8 h-8 flex items-center justify-center border-l border-[rgba(207,196,197,0.3)] hover:bg-gray-50 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                          :disabled="isUpdating || (item.stockQuantity !== undefined && item.quantity >= item.stockQuantity)"
                          @click="increaseQuantity(item)"
                        >
                          <span
                            class="text-[#5E5F5C] font-inter text-sm"
                            style="font-weight: 600; letter-spacing: 0.7px; text-transform: uppercase;"
                          >+</span>
                        </button>
                      </div>
                    </div>

                    <!-- Price -->
                    <div class="text-right">
                      <p class="font-geist text-2xl text-black">
                        {{ formatPrice(item.subtotal) }}
                      </p>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Remove Link -->
              <div class="mt-4 flex justify-end">
                <button
                  class="flex items-center gap-2 text-[#5E5F5C] hover:text-red-500 transition-colors"
                  :disabled="isUpdating"
                  @click="removeItem(cartItemKey(item))"
                >
                  <svg
                    xmlns="http://www.w3.org/2000/svg"
                    class="w-4 h-4"
                    fill="none"
                    viewBox="0 0 24 24"
                    stroke="currentColor"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                    />
                  </svg>
                  <span class="font-gelasio text-base">{{ t('cart.remove') }}</span>
                </button>
              </div>
            </div>
          </div>

          <!-- Order Summary Sidebar -->
          <div class="w-full lg:w-[466px] flex-shrink-0">
            <div class="bg-[#F3F3F4] border border-[rgba(207,196,197,0.2)] p-8 flex flex-col gap-6">
              <h2 class="font-geist text-2xl font-semibold leading-[38px] text-[#1A1C1C]">
                {{ t('common.subtotal') }}
              </h2>

              <div class="flex flex-col gap-4">
                <!-- Subtotal -->
                <div class="flex justify-between items-center">
                  <span class="font-geist text-base text-[#5E5F5C]">{{ t('common.subtotal') }} ({{ t('cart.itemCount', { count: cartStore.totalItems }) }})</span>
                  <span class="font-geist text-base text-black">{{ formatPrice(cartStore.totalPrice) }}</span>
                </div>

                <!-- Shipping -->
                <div class="flex justify-between items-center">
                  <span class="font-geist text-base text-[#5E5F5C]">{{ t('cart.shippingFee') }}</span>
                  <span class="font-gelasio text-base text-black">{{ t('cart.free') }}</span>
                </div>

                <!-- Divider -->
                <div class="border-t border-[rgba(207,196,197,0.3)] pt-4">
                  <div class="flex justify-between items-center">
                    <span class="font-geist text-2xl font-semibold leading-[38px] tracking-[-0.32px] text-[#1A1C1C]">{{ t('common.total') }}</span>
                    <span class="font-geist text-2xl text-black">{{ formatPrice(cartStore.totalPrice) }}</span>
                  </div>
                </div>
              </div>

              <!-- Checkout Button -->
              <p v-if="hasStockIssues" class="text-red-500 text-xs text-center mb-2 font-semibold">
                ⚠️ Giỏ hàng có sản phẩm hết hàng hoặc vượt tồn kho. Vui lòng cập nhật trước khi thanh toán.
              </p>
              <button
                class="w-full h-[56px] bg-black border-2 border-black text-white font-geist text-base flex items-center justify-center hover:bg-gray-900 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="isUpdating || hasStockIssues"
                @click="handleCheckout"
              >
                <span v-if="!isUpdating">{{ t('cart.checkout') }}</span>
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
                  {{ t('cart.processing') }}
                </span>
              </button>

              <!-- Security Message -->
              <div class="flex items-center justify-center gap-4 text-[#5E5F5C] text-sm">
                <svg
                  class="w-4 h-3"
                  viewBox="0 0 16 21"
                  fill="currentColor"
                >
                  <path d="M8 1C4.5 4 2 6.5 2 9C2 10.5 3 11.5 4.5 11.5C5 11.5 5.5 11.4 6 11.3C6.5 11.4 7 11.5 7.5 11.5C9 11.5 10 10.5 10 9C10 6.5 7.5 4 6 1Z" />
                </svg>
                <span>{{ t('cart.securePayment') }}</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty Cart Message -->
        <div
          v-else
          class="text-center py-16"
        >
          <p class="font-gelasio text-xl text-[#5E5F5C] mb-8">
            {{ t('cart.empty') }}
          </p>
          <router-link
            to="/products"
            class="inline-block w-[200px] h-[56px] bg-black text-white font-geist text-base flex items-center justify-center hover:bg-gray-900 transition-colors"
          >
            {{ t('cart.continueShopping') }}
          </router-link>
        </div>

        <!-- Recommendation Section -->
        <section
          v-if="showRecommendations"
          class="mb-16"
        >
          <h2 class="font-geist text-2xl font-semibold leading-[38px] text-[#1A1C1C] mb-8">
            {{ t('recommendation.title') }}
          </h2>

          <!-- Loading Skeleton -->
          <div
            v-if="recommendationLoading"
            class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-x-6 gap-y-12"
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

          <!-- Recommendation Cards -->
          <div
            v-else
            class="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-x-6 gap-y-12"
          >
            <ProductCard
              v-for="product in recommendations"
              :key="product.id"
              :product="product"
            />
          </div>
        </section>
      </div>
    </main>

    <!-- Footer -->
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useCartStore } from '@/stores/cart.store'
import { productService } from '@/services/product.service'
import type { RecommendationProduct } from '@/types'
import ProductCard from '@/components/ui/ProductCard.vue'
import BaseSkeleton from '@/components/ui/BaseSkeleton.vue'
import { useI18n } from 'vue-i18n'
import { formatCurrency } from '@/utils/formatters'
import { useToast } from 'vue-toastification'

const { t } = useI18n()

const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()
const toast = useToast()

const isUpdating = ref(false)

onMounted(async () => {
  if (authStore.isAuthenticated) {
    await cartStore.fetchCart().catch(console.error)
  }
})

// Use displayItems which works for both authenticated and guest users
const cartItems = computed(() => cartStore.displayItems)

const hasStockIssues = computed(() => {
  return cartItems.value.some((item: any) => {
    if (item.stockQuantity === undefined || item.stockQuantity === null) return false
    return item.stockQuantity <= 0 || item.quantity > item.stockQuantity
  })
})

// Recommendation state - load độc lập với Cart, fail không ảnh hưởng cart/checkout
const recommendations = ref<RecommendationProduct[]>([])
const recommendationLoading = ref(false)
const recommendationError = ref(false)

const showRecommendations = computed(() =>
  !recommendationError.value && (recommendationLoading.value || recommendations.value.length > 0)
)

// Key từ tập unique productId — quantity thay đổi không đổi key -> không refetch
const recommendationKey = computed(() =>
  [...new Set(cartItems.value.map(item => item.productId))]
    .filter(Boolean)
    .sort((a, b) => a - b)
    .join(',')
)

watch(recommendationKey, async (key) => {
  if (!key) {
    recommendations.value = []
    return
  }
  recommendationLoading.value = true
  recommendationError.value = false
  try {
    const ids = key.split(',').map(Number)
    recommendations.value = await productService.getCartRecommendations(ids, 8)
  } catch (error) {
    console.error('Failed to load recommendations:', error)
    recommendationError.value = true
    recommendations.value = []
  } finally {
    recommendationLoading.value = false
  }
}, { immediate: true })

const formatPrice = (price: number): string => {
  return formatCurrency(price)
}

const cartItemKey = (item: any): number => {
  return cartStore.isGuestCart ? item.variantId : item.id
}

const increaseQuantity = async (item: any) => {
  isUpdating.value = true
  try {
    await cartStore.updateItem(cartItemKey(item), item.quantity + 1)
  } catch (error: any) {
    const msg = error?.response?.data?.error || error?.response?.data?.message || t('toast.updateFailed', 'Không thể tăng số lượng')
    toast.error(msg)
  } finally {
    isUpdating.value = false
  }
}

const decreaseQuantity = async (item: any) => {
  isUpdating.value = true
  try {
    if (item.quantity <= 1) {
      await removeItem(cartItemKey(item))
    } else {
      await cartStore.updateItem(cartItemKey(item), item.quantity - 1)
    }
  } catch (error: any) {
    const msg = error?.response?.data?.error || error?.response?.data?.message || t('toast.updateFailed', 'Không thể giảm số lượng')
    toast.error(msg)
  } finally {
    isUpdating.value = false
  }
}

const removeItem = async (itemKey: number) => {
  isUpdating.value = true
  try {
    await cartStore.removeItem(itemKey)
  } finally {
    isUpdating.value = false
  }
}

const handleCheckout = () => {
  // Check if user is authenticated
  if (!authStore.isAuthenticated) {
    // Store current cart in localStorage (already done by cart store)
    // Redirect to login with redirect back to cart/checkout
    authStore.setRedirectPath('/cart') // Keep on cart page after login to review
    router.push('/login')
  } else {
    // User is authenticated, proceed to checkout
    router.push('/checkout')
  }
}
</script>

<style scoped>
.font-inter {
  font-family: 'Geist', sans-serif;
}
</style>
