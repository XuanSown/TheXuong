<template>
  <div class="min-h-screen bg-[#F9F9F9]">
    <main class="w-full max-w-[1280px] mx-auto px-4 pt-[120px] pb-8">
      <div class="w-[1152px] mx-auto">
        <!-- Header Section -->
        <header class="flex flex-col gap-[15px] mb-16">
          <h1 class="font-geist text-[64px] font-normal leading-[70px] tracking-[-1.28px] text-black">
            GIỎ HÀNG
          </h1>
          <p v-if="cartStore.isGuestCart" class="font-gelasio text-base text-[#5E5F5C]">
            Bạn đang mua sắm với tư cách khách. Vui lòng <router-link to="/login?redirect=/cart" class="text-black font-semibold hover:underline">đăng nhập</router-link> để hoàn tất đơn hàng.
          </p>
        </header>

        <!-- Cart Content -->
        <div v-if="cartItems.length > 0" class="flex gap-8 mb-16">
          <!-- Cart Items List -->
          <div class="flex-1 flex flex-col gap-8">
            <div v-for="item in cartItems" :key="item.variantId" class="border-b border-[rgba(207,196,197,0.3)] pb-8">
              <div class="flex gap-6">
                <!-- Product Image -->
                <div class="w-[160px] h-[160px] bg-[#F3F3F4] flex-shrink-0">
                  <img
                    v-if="item.productImage"
                    :src="item.productImage"
                    :alt="item.productName"
                    class="w-full h-full object-cover"
                    loading="lazy"
                  />
                  <div v-else class="w-full h-full bg-gray-200 flex items-center justify-center">
                    <span class="text-gray-400 text-sm">No image</span>
                  </div>
                </div>

                <!-- Product Info -->
                <div class="flex-1 flex flex-col justify-between">
                  <div class="flex flex-col gap-[5.22px]">
                    <h3 class="font-geist text-2xl font-normal leading-[38px] tracking-[-0.32px] text-black">
                      {{ item.productName }}
                    </h3>
                    <p v-if="item.size" class="font-geist text-sm text-[#5E5F5C]">
                      Size: {{ item.size }}
                    </p>
                  </div>

                  <div class="flex justify-between items-end">
                    <!-- Quantity Controls -->
                    <div class="flex items-center gap-4">
                      <div class="flex items-center border border-[rgba(207,196,197,0.3)] w-[98px] h-[34px]">
                        <button
                          @click="decreaseQuantity(item)"
                          class="w-8 h-8 flex items-center justify-center border-r border-[rgba(207,196,197,0.3)] hover:bg-gray-50 transition-colors"
                          :disabled="isUpdating"
                        >
                          <span class="text-[#5E5F5C] font-inter text-sm" style="font-weight: 600; letter-spacing: 0.7px; text-transform: uppercase;">-</span>
                        </button>
                        <span class="flex-1 text-center font-inter text-base text-[#1A1C1C]">{{ item.quantity }}</span>
                        <button
                          @click="increaseQuantity(item)"
                          class="w-8 h-8 flex items-center justify-center border-l border-[rgba(207,196,197,0.3)] hover:bg-gray-50 transition-colors"
                          :disabled="isUpdating"
                        >
                          <span class="text-[#5E5F5C] font-inter text-sm" style="font-weight: 600; letter-spacing: 0.7px; text-transform: uppercase;">+</span>
                        </button>
                      </div>
                    </div>

                    <!-- Price -->
                    <div class="text-right">
                      <p class="font-geist text-2xl text-black">{{ formatPrice(item.subtotal) }}</p>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Remove Link -->
              <div class="mt-4 flex justify-end">
                <button
                  @click="removeItem(item.variantId)"
                  class="flex items-center gap-2 text-[#5E5F5C] hover:text-red-500 transition-colors"
                  :disabled="isUpdating"
                >
                  <svg xmlns="http://www.w3.org/2000/svg" class="w-4 h-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                  </svg>
                  <span class="font-gelasio text-base">Xóa khỏi giỏ</span>
                </button>
              </div>
            </div>
          </div>

          <!-- Order Summary Sidebar -->
          <div class="w-[466px] flex-shrink-0">
            <div class="bg-[#F3F3F4] border border-[rgba(207,196,197,0.2)] p-8 flex flex-col gap-6">
              <h2 class="font-geist text-2xl font-semibold leading-[38px] text-[#1A1C1C]">
                Tạm tính
              </h2>

              <div class="flex flex-col gap-4">
                <!-- Subtotal -->
                <div class="flex justify-between items-center">
                  <span class="font-geist text-base text-[#5E5F5C]">Tạm tính ({{ cartStore.totalItems }} sản phẩm)</span>
                  <span class="font-geist text-base text-black">{{ formatPrice(cartStore.totalPrice) }}</span>
                </div>

                <!-- Shipping -->
                <div class="flex justify-between items-center">
                  <span class="font-geist text-base text-[#5E5F5C]">Phí vận chuyển</span>
                  <span class="font-gelasio text-base text-black">Miễn phí</span>
                </div>

                <!-- Divider -->
                <div class="border-t border-[rgba(207,196,197,0.3)] pt-4">
                  <div class="flex justify-between items-center">
                    <span class="font-geist text-2xl font-semibold leading-[38px] tracking-[-0.32px] text-[#1A1C1C]">Tổng cộng</span>
                    <span class="font-geist text-2xl text-black">{{ formatPrice(cartStore.totalPrice) }}</span>
                  </div>
                </div>
              </div>

              <!-- Checkout Button -->
              <button
                @click="handleCheckout"
                class="w-full h-[56px] bg-black border-2 border-black text-white font-geist text-base flex items-center justify-center hover:bg-gray-900 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="isUpdating"
              >
                <span v-if="!isUpdating">THANH TOÁN NGAY</span>
                <span v-else class="flex items-center gap-2">
                  <svg class="animate-spin w-5 h-5" viewBox="0 0 24 24" fill="none">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                  </svg>
                  Đang xử lý...
                </span>
              </button>

              <!-- Security Message -->
              <div class="flex items-center justify-center gap-4 text-[#5E5F5C] text-sm">
                <svg class="w-4 h-3" viewBox="0 0 16 21" fill="currentColor">
                  <path d="M8 1C4.5 4 2 6.5 2 9C2 10.5 3 11.5 4.5 11.5C5 11.5 5.5 11.4 6 11.3C6.5 11.4 7 11.5 7.5 11.5C9 11.5 10 10.5 10 9C10 6.5 7.5 4 6 1Z"/>
                </svg>
                <span>Thanh toán an toàn & bảo mật</span>
              </div>
            </div>
          </div>
        </div>

        <!-- Empty Cart Message -->
        <div v-else class="text-center py-16">
          <p class="font-gelasio text-xl text-[#5E5F5C] mb-8">Giỏ hàng của bạn đang trống</p>
          <router-link
            to="/products"
            class="inline-block w-[200px] h-[56px] bg-black text-white font-geist text-base flex items-center justify-center hover:bg-gray-900 transition-colors"
          >
            TIẾP TỤC MUA SẮM
          </router-link>
        </div>
      </div>
    </main>

    <!-- Footer -->
    
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useCartStore } from '@/stores/cart.store'


const router = useRouter()
const authStore = useAuthStore()
const cartStore = useCartStore()

const isUpdating = ref(false)

// Use displayItems which works for both authenticated and guest users
const cartItems = computed(() => cartStore.displayItems)

const formatPrice = (price: number): string => {
  return new Intl.NumberFormat('vi-VN').format(price) + ' đ'
}

const increaseQuantity = async (item: any) => {
  isUpdating.value = true
  try {
    await cartStore.updateItem(item.variantId, item.quantity + 1)
  } finally {
    isUpdating.value = false
  }
}

const decreaseQuantity = async (item: any) => {
  isUpdating.value = true
  try {
    if (item.quantity <= 1) {
      await removeItem(item.variantId)
    } else {
      await cartStore.updateItem(item.variantId, item.quantity - 1)
    }
  } finally {
    isUpdating.value = false
  }
}

const removeItem = async (variantId: number) => {
  isUpdating.value = true
  try {
    await cartStore.removeItem(variantId)
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
@import url('https://fonts.googleapis.com/css2?family=Gelasio:wght@400;500;600;700&family=Inter:wght@400;500;600;700&display=swap') layer(fonts);

.font-geist {
  font-family: 'Geist', sans-serif;
}

.font-gelasio {
  font-family: 'Geist', sans-serif;
}

.font-inter {
  font-family: 'Geist', sans-serif;
}
</style>
