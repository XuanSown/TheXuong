<template>
  <div class="checkout-page min-h-screen bg-[#F3F3F4]">
    <main class="w-full max-w-[1280px] mx-auto px-4 pt-[120px] pb-8">
      <div class="w-[1152px] mx-auto">
        <!-- Header Section -->
        <header class="flex flex-col gap-[48px] mb-16">
          <!-- Breadcrumb -->
          <div class="flex items-center gap-3">
            <router-link to="/" class="flex items-center gap-2 text-[#5E5F5C] hover:text-black transition-colors">
              <svg class="w-[13.33px] h-[13.33px]" viewBox="0 0 13 13" fill="currentColor">
                <path d="M6.5 1L1 6.5l5.5 5.5M1 6.5L6.5 12" stroke="currentColor" stroke-width="1.5" fill="none"/>
              </svg>
              <span class="font-gelasio text-base">Quay lại trang chủ</span>
            </router-link>
          </div>

          <!-- Page Title -->
          <div class="flex items-center gap-4">
            <svg class="w-[18px] h-[18px]" viewBox="0 0 18 18" fill="currentColor">
              <rect width="18" height="18" fill="currentColor"/>
            </svg>
            <h1 class="font-geist text-[20px] leading-[30px] text-black">
              THANH TOÁN
            </h1>
          </div>
        </header>

        <!-- Checkout Content -->
        <div class="grid grid-cols-2 gap-8">
          <!-- Left: Shipping Info -->
          <div class="bg-white border border-[#EEEEEE] shadow-[0px_8px_30px_rgba(0,0,0,0.04)] rounded-xl p-8">
            <h2 class="font-geist text-[16px] font-bold leading-[24px] tracking-[0.8px] uppercase text-[#4C4546] mb-6">
              Thông tin giao hàng
            </h2>

            <form @submit.prevent="handlePlaceOrder">
              <!-- Full Name -->
              <div class="flex flex-col gap-2 mb-6">
                <label class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.2px] uppercase text-[#4C4546] opacity-80">
                  HỌ VÀ TÊN
                </label>
                <input
                  v-model="shippingForm.fullName"
                  type="text"
                  required
                  placeholder="Nhập họ tên"
                  class="w-full h-[50px] bg-white border border-[#CFC4C5] rounded-lg px-4 font-gelasio text-[16px] text-[#1A1C1C] outline-none focus:border-black transition-colors"
                />
              </div>

              <!-- Phone Number -->
              <div class="flex flex-col gap-2 mb-6">
                <label class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.2px] uppercase text-[#4C4546] opacity-80">
                  SỐ ĐIỆN THOẠI
                </label>
                <input
                  v-model="shippingForm.phoneNumber"
                  type="tel"
                  required
                  placeholder="Nhập số điện thoại"
                  class="w-full h-[50px] bg-white border border-[#CFC4C5] rounded-lg px-4 font-gelasio text-[16px] text-[#1A1C1C] outline-none focus:border-black transition-colors"
                />
              </div>

              <!-- Address -->
              <div class="flex flex-col gap-2 mb-6">
                <label class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.2px] uppercase text-[#4C4546] opacity-80">
                  ĐỊA CHỈ NHẬN HÀNG
                </label>
                <textarea
                  v-model="shippingForm.address"
                  required
                  placeholder="Nhập địa chỉ chi tiết"
                  rows="3"
                  class="w-full h-[98px] bg-white border border-[#CFC4C5] rounded-lg px-4 py-3 font-gelasio text-[16px] text-[#1A1C1C] outline-none focus:border-black transition-colors resize-none overflow-y-auto"
                ></textarea>
              </div>

              <!-- Payment Method -->
              <div class="flex flex-col gap-2 mb-6">
                <label class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.2px] uppercase text-[#4C4546] opacity-80">
                  PHƯƠNG THỨC THANH TOÁN
                </label>
                <div class="flex gap-4">
                  <label class="flex items-center gap-2 cursor-pointer">
                    <input
                      v-model="shippingForm.paymentMethod"
                      type="radio"
                      value="COD"
                      class="accent-black"
                    />
                    <span class="font-gelasio text-[14px] text-[#4C4546]">Thanh toán khi nhận hàng (COD)</span>
                  </label>
                  <label class="flex items-center gap-2 cursor-pointer">
                    <input
                      v-model="shippingForm.paymentMethod"
                      type="radio"
                      value="VNPAY"
                      class="accent-black"
                    />
                    <span class="font-gelasio text-[14px] text-[#4C4546]">Chuyển khoản (VNPay)</span>
                  </label>
                </div>
              </div>

              <!-- Note (optional) -->
              <div class="flex flex-col gap-2">
                <label class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.2px] uppercase text-[#4C4546] opacity-80">
                  GHI CHÚ (TÙY CHỌN)
                </label>
                <textarea
                  v-model="shippingForm.note"
                  rows="2"
                  placeholder="Ghi chú cho đơn hàng..."
                  class="w-full h-[60px] bg-white border border-[#CFC4C5] rounded-lg px-4 py-3 font-gelasio text-[16px] text-[#1A1C1C] outline-none focus:border-black transition-colors resize-none overflow-y-auto"
                ></textarea>
              </div>
            </form>
          </div>

          <!-- Right: Order Summary -->
          <div class="bg-white border border-[#EEEEEE] shadow-[0px_8px_30px_rgba(0,0,0,0.04)] rounded-xl p-8 h-fit">
            <h2 class="font-geist text-[16px] font-bold leading-[24px] tracking-[0.8px] uppercase text-[#4C4546] mb-6">
              Tóm tắt đơn hàng
            </h2>

            <!-- Cart Items -->
            <div v-if="cartItems.length > 0" class="space-y-4 mb-6">
              <div v-for="item in cartItems" :key="`${item.id}-${item.variantId}-${item.size}`" class="flex gap-4 pb-4 border-b border-[#E8E8E8]">
                <img
                  :src="item.productImage || '/placeholder.jpg'"
                  alt=""
                  class="w-20 h-20 object-cover rounded-lg"
                />
                <div class="flex-1">
                  <h3 class="font-gelasio text-[14px] text-[#1A1C1C] leading-[20px] line-clamp-2">
                    {{ item.productName }}
                  </h3>
                  <p class="font-gelasio text-[12px] text-[#848484]">Size: {{ item.size }}</p>
                  <div class="flex justify-between items-center mt-2">
                    <span class="font-gelasio text-[14px] font-semibold text-black">
                      {{ formatPrice(item.price) }} x {{ item.quantity }}
                    </span>
                    <span class="font-gelasio text-[14px] font-bold text-black">
                      {{ formatPrice(item.price * item.quantity) }}
                    </span>
                  </div>
                </div>
              </div>
            </div>

            <div v-else class="text-center py-8 text-[#848484]">
              <p>Giỏ hàng trống</p>
              <router-link to="/products" class="text-blue-600 hover:underline">
                Tiếp tục mua sắm
              </router-link>
            </div>

            <!-- Summary -->
            <div v-if="cartItems.length > 0" class="space-y-3 pt-4 border-t border-[#E8E8E8]">
              <div class="flex justify-between">
                <span class="font-gelasio text-[14px] text-[#4C4546]">Tạm tính</span>
                <span class="font-gelasio text-[14px] text-black">{{ formatPrice(cartTotal) }}</span>
              </div>
              <div class="flex justify-between">
                <span class="font-gelasio text-[14px] text-[#4C4546]">Phí vận chuyển</span>
                <span class="font-gelasio text-[14px] text-black">{{ formatPrice(shippingFee) }}</span>
              </div>
              <div class="flex justify-between text-lg font-bold">
                <span class="font-gelasio text-[16px] text-black">Tổng cộng</span>
                <span class="font-gelasio text-[16px] text-black">{{ formatPrice(total) }}</span>
              </div>
            </div>

            <!-- Place Order Button -->
            <button
              v-if="cartItems.length > 0"
              @click="handlePlaceOrder"
              :disabled="isLoading || !isFormValid"
              class="w-full mt-6 py-4 bg-black text-white font-gelasio text-[16px] rounded-lg hover:bg-gray-900 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {{ isLoading ? 'Đang xử lý...' : 'ĐẶT HÀNG' }}
            </button>
          </div>
        </div>
      </div>
    </main>

    <!-- Footer -->
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { useCartStore } from '@/stores/cart.store'
import { useAuthStore } from '@/stores/auth.store'
import { useRouter } from 'vue-router'
import api from '@/services/api'

const cartStore = useCartStore()
const authStore = useAuthStore()
const router = useRouter()

const cartItems = computed(() => cartStore.displayItems)
const cartTotal = computed(() => cartStore.totalPrice)
const shippingFee = ref(0) // TODO: Calculate based on address
const total = computed(() => cartTotal.value + shippingFee.value)

const isLoading = ref(false)

const shippingForm = reactive({
  fullName: '',
  phoneNumber: '',
  address: '',
  paymentMethod: 'COD' as 'COD' | 'VNPAY',
  note: ''
})

const isFormValid = computed(() => {
  return shippingForm.fullName.trim() !== '' &&
         shippingForm.phoneNumber.trim() !== '' &&
         shippingForm.address.trim() !== '' &&
         cartItems.value.length > 0
})

onMounted(() => {
  // Auto-fill from user profile if logged in
  if (authStore.user) {
    shippingForm.fullName = authStore.user.fullName || ''
    shippingForm.phoneNumber = authStore.user.phone || ''
    shippingForm.address = authStore.user.address || ''
  }

  // Check if cart is empty
  if (cartItems.value.length === 0) {
    router.push('/cart')
  }
})

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price)
}

const handlePlaceOrder = async () => {
  if (!isFormValid.value) {
    alert('Vui lòng điền đầy đủ thông tin giao hàng')
    return
  }

  if (cartItems.value.length === 0) {
    alert('Giỏ hàng trống')
    return
  }

  if (!authStore.isAuthenticated) {
    alert('Vui lòng đăng nhập để đặt hàng')
    router.push('/login')
    return
  }

  isLoading.value = true
  try {
    const orderData = {
      fullName: shippingForm.fullName,
      phoneNumber: shippingForm.phoneNumber,
      address: shippingForm.address,
      paymentMethod: shippingForm.paymentMethod
    }

    const response = await api.post('/api/v1/orders', orderData)

    // Clear cart after successful order
    cartStore.clearCart()

    alert('Đặt hàng thành công!')
    router.push(`/order/success/${response.data.order.id}`)

  } catch (error: any) {
    console.error('Failed to place order:', error)
    alert('Đặt hàng thất bại: ' + (error.response?.data?.error || error.response?.data?.message || 'Vui lòng thử lại'))
  } finally {
    isLoading.value = false
  }
}
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Gelasio:wght@400;500;600;700&family=Inter:wght@400;500;600;700&display=swap') layer(fonts);

.font-geist {
  font-family: 'Inter', 'Geist', -apple-system, BlinkMacSystemFont, 'Segoe UI', sans-serif;
}

.font-gelasio {
  font-family: 'Gelasio', serif;
}

.font-inter {
  font-family: 'Inter', sans-serif;
}

/* Custom scrollbar for textarea */
textarea::-webkit-scrollbar {
  width: 6px;
}

textarea::-webkit-scrollbar-track {
  background: transparent;
}

textarea::-webkit-scrollbar-thumb {
  background: rgba(0, 0, 0, 0.2);
  border-radius: 3px;
}

textarea::-webkit-scrollbar-thumb:hover {
  background: rgba(0, 0, 0, 0.3);
}
</style>
