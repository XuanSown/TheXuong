<template>
  <div class="checkout-page min-h-screen">
    <main class="w-full max-w-[1280px] mx-auto px-4 pb-8">
      <div class="w-[1152px] mx-auto">
        <!-- Header Section -->
        <header class="flex flex-col gap-[48px] mb-16">
          <!-- Breadcrumb -->
          <div class="flex items-center gap-3">
            <router-link
              to="/"
              class="flex items-center gap-2 text-[#5E5F5C] hover:text-black transition-colors"
            >
              <svg
                class="w-[13.33px] h-[13.33px]"
                viewBox="0 0 13 13"
                fill="currentColor"
              >
                <path
                  d="M6.5 1L1 6.5l5.5 5.5M1 6.5L6.5 12"
                  stroke="currentColor"
                  stroke-width="1.5"
                  fill="none"
                />
              </svg>
              <span class="font-gelasio text-base">Quay lại trang chủ</span>
            </router-link>
          </div>

          <!-- Page Title -->
          <div class="flex items-center gap-4">
            <svg
              class="w-[18px] h-[18px]"
              viewBox="0 0 18 18"
              fill="currentColor"
            >
              <rect
                width="18"
                height="18"
                fill="currentColor"
              />
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

            <form @submit.prevent="onSubmit">
              <!-- Full Name -->
              <div class="mb-6">
                <label
                  class="block font-geist text-[12px] font-semibold leading-[12px] tracking-[1.2px] uppercase text-[#4C4546] opacity-80 mb-2"
                >
                  HỌ VÀ TÊN
                </label>
                <BaseInput
                  v-model="fullName"
                  type="text"
                  placeholder="Nhập họ tên"
                  :error="fullNameError"
                  class="!h-[50px]"
                />
              </div>

              <!-- Phone Number -->
              <div class="mb-6">
                <label
                  class="block font-geist text-[12px] font-semibold leading-[12px] tracking-[1.2px] uppercase text-[#4C4546] opacity-80 mb-2"
                >
                  SỐ ĐIỆN THOẠI
                </label>
                <BaseInput
                  v-model="phoneNumber"
                  type="tel"
                  placeholder="Nhập số điện thoại"
                  :error="phoneNumberError"
                  class="!h-[50px]"
                />
              </div>

              <!-- Address Picker -->
              <div class="mb-6">
                <label class="block font-geist text-[12px] font-semibold leading-[12px] tracking-[1.2px] uppercase text-[#4C4546] opacity-80 mb-2">
                  ĐỊA CHỈ GIAO HÀNG
                </label>
                <div
                  v-if="addressStore.hasAddresses"
                  class="flex flex-col gap-2 mb-3"
                >
                  <label
                    v-for="a in addressStore.addresses"
                    :key="a.id"
                    class="flex gap-2 items-start p-3 border rounded-lg cursor-pointer"
                    :class="selectedAddressId === a.id ? 'border-black bg-gray-50' : 'border-[#CFC4C5]'"
                  >
                    <input
                      v-model="selectedAddressId"
                      type="radio"
                      :value="a.id"
                      @change="selectAddress(a)"
                    >
                    <div class="flex-1">
                      <div class="font-medium text-[14px]">
                        {{ a.recipientName }} · {{ a.recipientPhone }}
                        <span
                          v-if="a.isDefault"
                          class="ml-2 text-[10px] bg-black text-white px-1 rounded"
                        >Mặc định</span>
                        <span
                          v-if="a.label"
                          class="ml-2 text-[10px] text-gray-500"
                        >{{ a.label }}</span>
                      </div>
                      <div class="text-sm text-gray-600">
                        {{ formatAddress({ streetDetail: a.streetDetail, wardCode: a.wardCode, districtCode: a.districtCode, provinceCode: a.provinceCode }) }}
                      </div>
                    </div>
                    <button
                      type="button"
                      class="text-xs underline"
                      @click="editAtCheckout(a)"
                    >Sửa</button>
                  </label>
                </div>
                <button
                  type="button"
                  class="text-sm underline self-start mb-3"
                  @click="openAddressModal"
                >
                  + Thêm địa chỉ mới
                </button>
                <textarea
                  v-model="address"
                  placeholder="Nhập địa chỉ chi tiết"
                  rows="3"
                  class="w-full h-[98px] bg-white border border-[#CFC4C5] rounded-lg px-4 py-3 font-gelasio text-[16px] text-[#1A1C1C] outline-none focus:border-black transition-colors resize-none overflow-y-auto"
                  :class="{ 'border-red-500 focus:border-red-500': addressError }"
                />
                <span
                  v-if="addressError"
                  class="text-red-500 text-sm mt-1"
                >{{ addressError }}</span>
              </div>

              <!-- Address Modal -->
              <BaseModal
                v-model="showAddressModal"
                :title="editingAddress ? 'Sửa địa chỉ' : 'Thêm địa chỉ'"
              >
                <AddressForm
                  :model-value="editingAddress || undefined"
                  @submit="onSubmitAddressAtCheckout"
                  @cancel="showAddressModal = false"
                />
              </BaseModal>

              <!-- Payment Method -->
              <div class="flex flex-col gap-2 mb-6">
                <label
                  class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.2px] uppercase text-[#4C4546] opacity-80"
                >
                  PHƯƠNG THỨC THANH TOÁN
                </label>
                <div class="flex gap-4">
                  <label class="flex items-center gap-2 cursor-pointer">
                    <input
                      v-model="paymentMethod"
                      type="radio"
                      value="COD"
                      class="accent-black"
                    >
                    <span class="font-gelasio text-[14px] text-[#4C4546]">Thanh toán khi nhận hàng (COD)</span>
                  </label>
                  <label class="flex items-center gap-2 cursor-pointer">
                    <input
                      v-model="paymentMethod"
                      type="radio"
                      value="VNPAY"
                      class="accent-black"
                    >
                    <span class="font-gelasio text-[14px] text-[#4C4546]">Chuyển khoản (VNPay)</span>
                  </label>
                </div>
                <span
                  v-if="paymentMethodError"
                  class="text-red-500 text-sm mt-1"
                >{{ paymentMethodError }}</span>
              </div>

              <!-- Note (optional) -->
              <div class="flex flex-col gap-2">
                <label
                  class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.2px] uppercase text-[#4C4546] opacity-80"
                >
                  GHI CHÚ (TÙY CHỌN)
                </label>
                <textarea
                  v-model="note"
                  rows="2"
                  placeholder="Ghi chú cho đơn hàng..."
                  class="w-full h-[60px] bg-white border border-[#CFC4C5] rounded-lg px-4 py-3 font-gelasio text-[16px] text-[#1A1C1C] outline-none focus:border-black transition-colors resize-none overflow-y-auto"
                />
              </div>
            </form>
          </div>

          <!-- Right: Order Summary -->
          <div class="bg-white border border-[#EEEEEE] shadow-[0px_8px_30px_rgba(0,0,0,0.04)] rounded-xl p-8 h-fit">
            <h2 class="font-geist text-[16px] font-bold leading-[24px] tracking-[0.8px] uppercase text-[#4C4546] mb-6">
              Tóm tắt đơn hàng
            </h2>

            <!-- Cart Items -->
            <div
              v-if="cartItems.length > 0"
              class="space-y-4 mb-6"
            >
              <div
                v-for="item in cartItems"
                :key="`${item.id}-${item.variantId}-${item.size}`"
                class="flex gap-4 pb-4 border-b border-[#E8E8E8]"
              >
                <img
                  :src="item.productImage || '/placeholder.jpg'"
                  alt=""
                  class="w-20 h-20 object-cover rounded-lg"
                  loading="lazy"
                >
                <div class="flex-1">
                  <h3 class="font-gelasio text-[14px] text-[#1A1C1C] leading-[20px] line-clamp-2">
                    {{ item.productName }}
                  </h3>
                  <p class="font-gelasio text-[12px] text-[#848484]">
                    Size: {{ item.size }}
                  </p>
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

            <div
              v-else
              class="text-center py-8 text-[#848484]"
            >
              <p>Giỏ hàng trống</p>
              <router-link
                to="/products"
                class="text-blue-600 hover:underline"
              >
                Tiếp tục mua sắm
              </router-link>
            </div>

            <!-- Voucher Section -->
            <div
              v-if="cartItems.length > 0"
              class="mt-6 mb-2"
            >
              <label
                class="block font-geist text-[12px] font-semibold leading-[12px] tracking-[1.2px] uppercase text-[#4C4546] opacity-80 mb-2"
              >
                MÃ GIẢM GIÁ
              </label>
              <div class="flex gap-2">
                <input
                  v-model="voucherCode"
                  type="text"
                  placeholder="Nhập mã giảm giá..."
                  class="flex-1 h-[48px] px-4 border border-[#EEEEEE] rounded-lg focus:outline-none focus:border-black font-gelasio"
                  :disabled="!!appliedVoucher"
                >
                <BaseButton
                  v-if="!appliedVoucher"
                  :disabled="!voucherCode || isApplyingVoucher"
                  :loading="isApplyingVoucher"
                  class="!w-auto !px-6"
                  @click="applyVoucher"
                >
                  ÁP DỤNG
                </BaseButton>
                <BaseButton
                  v-else
                  class="!w-auto !px-6 !bg-red-500 hover:!bg-red-600"
                  @click="removeVoucher"
                >
                  XÓA
                </BaseButton>
              </div>
              <p
                v-if="voucherMessage"
                :class="voucherError ? 'text-red-500' : 'text-green-500'"
                class="text-sm mt-2 font-gelasio"
              >
                {{ voucherMessage }}
              </p>

              <!-- My Vouchers List -->
              <div
                v-if="myVouchers.length > 0 && !appliedVoucher"
                class="mt-4"
              >
                <button
                  type="button"
                  class="font-gelasio text-[14px] text-blue-600 hover:underline font-semibold flex items-center gap-1"
                  @click="showVoucherList = !showVoucherList"
                >
                  <svg
                    class="w-4 h-4 transition-transform"
                    :class="{ 'rotate-180': showVoucherList }"
                    fill="none"
                    stroke="currentColor"
                    viewBox="0 0 24 24"
                  >
                    <path
                      stroke-linecap="round"
                      stroke-linejoin="round"
                      stroke-width="2"
                      d="M19 9l-7 7-7-7"
                    />
                  </svg>
                  Chọn voucher của bạn ({{ myVouchers.length }})
                </button>

                <div
                  v-if="showVoucherList"
                  class="flex flex-col gap-2 mt-3 bg-gray-50 p-4 rounded-lg border border-[#EEEEEE]"
                >
                  <p class="text-xs text-gray-500 mb-1">
                    Click vào mã để tự động điền và áp dụng
                  </p>
                  <div class="flex flex-wrap gap-2">
                    <button
                      v-for="uv in myVouchers"
                      :key="uv.id"
                      type="button"
                      class="px-4 py-2 border border-[#CCCCCC] bg-white rounded text-[14px] font-geist font-bold hover:border-black hover:bg-black hover:text-white transition-colors tracking-wider"
                      @click="applyMyVoucher(uv.code)"
                    >
                      {{ uv.code }}
                    </button>
                  </div>
                </div>
              </div>
            </div>

            <!-- Points Section -->
            <div
              v-if="currentPoints > 0"
              class="mt-6 mb-2"
            >
              <label
                class="block font-geist text-[12px] font-semibold leading-[12px] tracking-[1.2px] uppercase text-[#4C4546] opacity-80 mb-2"
              >
                SỬ DỤNG ĐIỂM THƯỞNG
              </label>
              <p class="text-sm font-gelasio text-[#4C4546] mb-2">
                Bạn đang có <strong>{{ formatPrice(currentPoints) }}</strong> (1 điểm = 1đ)
              </p>
              <div class="flex gap-2">
                <input
                  v-model.number="pointsToUse"
                  type="number"
                  min="0"
                  :max="currentPoints"
                  placeholder="Nhập số điểm..."
                  class="flex-1 h-[48px] px-4 border border-[#EEEEEE] rounded-lg focus:outline-none focus:border-black font-gelasio"
                >
                <BaseButton
                  class="!w-auto !px-4 !bg-[#f1c40f] hover:!bg-[#f39c12] !text-black"
                  @click="applyAllPoints"
                >
                  DÙNG TẤT CẢ
                </BaseButton>
              </div>
              <p
                v-if="pointsError"
                class="text-red-500 text-sm mt-2 font-gelasio"
              >
                {{ pointsError }}
              </p>
            </div>

            <!-- Summary -->
            <div
              v-if="cartItems.length > 0"
              class="space-y-3 pt-4 border-t border-[#E8E8E8]"
            >
              <div class="flex justify-between">
                <span class="font-gelasio text-[14px] text-[#4C4546]">Tạm tính</span>
                <span class="font-gelasio text-[14px] text-black">{{ formatPrice(cartTotal) }}</span>
              </div>
              <div class="flex justify-between">
                <span class="font-gelasio text-[14px] text-[#4C4546]">Phí vận chuyển</span>
                <span class="font-gelasio text-[14px] text-black">{{ formatPrice(shippingFee) }}</span>
              </div>
              <div
                v-if="tierDiscountAmount > 0"
                class="flex justify-between text-green-600"
              >
                <span class="font-gelasio text-[14px]">Giảm giá hạng TV ({{ autoDiscountPercent }}%)</span>
                <span class="font-gelasio text-[14px]">-{{ formatPrice(tierDiscountAmount) }}</span>
              </div>
              <div
                v-if="appliedVoucher"
                class="flex justify-between text-green-600"
              >
                <span class="font-gelasio text-[14px]">Mã giảm giá ({{ appliedVoucher.code }})</span>
                <span class="font-gelasio text-[14px]">-{{ formatPrice(appliedVoucher.discountAmount) }}</span>
              </div>
              <div
                v-if="pointsToUse > 0"
                class="flex justify-between text-green-600"
              >
                <span class="font-gelasio text-[14px]">Trừ điểm thưởng</span>
                <span class="font-gelasio text-[14px]">-{{ formatPrice(pointsToUse) }}</span>
              </div>
              <div class="flex justify-between text-lg font-bold">
                <span class="font-gelasio text-[16px] text-black">Tổng cộng</span>
                <span class="font-gelasio text-[16px] text-black">{{ formatPrice(finalTotal) }}</span>
              </div>
            </div>

            <!-- Place Order Button -->
            <BaseButton
              v-if="cartItems.length > 0"
              :disabled="cartItems.length === 0"
              :loading="isSubmitting"
              class="w-full mt-6 !h-[56px]"
              @click="onSubmit"
            >
              ĐẶT HÀNG
            </BaseButton>
          </div>
        </div>
      </div>
    </main>

    <!-- Footer -->
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useCartStore } from '@/stores/cart.store'
import { useAuthStore } from '@/stores/auth.store'
import { useRouter } from 'vue-router'
import { useForm, useField } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import { checkoutSchema } from '@/utils/validators'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import orderService from '@/services/order.service'
import loyaltyService from '@/services/loyaltyService'
import { useAddressStore } from '@/stores/address.store'
import { formatAddress } from '@/utils/vn-regions'
import AddressForm from '@/components/address/AddressForm.vue'
import BaseModal from '@/components/ui/BaseModal.vue'
import { useToast } from 'vue-toastification'
import type { Address } from '@/types'

const cartStore = useCartStore()
const authStore = useAuthStore()
const router = useRouter()

const cartItems = computed(() => cartStore.displayItems)
const cartTotal = computed(() => cartStore.totalPrice)
const shippingFee = ref(0)
const total = computed(() => cartTotal.value + shippingFee.value)

// Voucher state
const voucherCode = ref('')
const isApplyingVoucher = ref(false)
const appliedVoucher = ref<{ code: string, discountAmount: number } | null>(null)
const voucherMessage = ref('')
const voucherError = ref(false)
const myVouchers = ref<any[]>([])
const showVoucherList = ref(false)

const autoDiscountPercent = ref(0)
const tierDiscountAmount = ref(0)
const currentPoints = ref(0)
const pointsToUse = ref<number>(0)
const pointsError = ref('')

const addressStore = useAddressStore()
const checkoutToast = useToast()
const selectedAddressId = ref<number | null>(null)
const showAddressModal = ref(false)
const editingAddress = ref<Address | null>(null)

const finalTotal = computed(() => {
  let amt = total.value
  if (appliedVoucher.value) {
    amt -= appliedVoucher.value.discountAmount
  }
  if (tierDiscountAmount.value > 0) {
    amt -= tierDiscountAmount.value
  }
  if (pointsToUse.value > 0) {
    amt -= pointsToUse.value
  }
  return amt > 0 ? amt : 0
})

const applyAllPoints = () => {
  let maxUsable = total.value
  if (appliedVoucher.value) maxUsable -= appliedVoucher.value.discountAmount
  if (tierDiscountAmount.value > 0) maxUsable -= tierDiscountAmount.value
  
  if (maxUsable <= 0) {
    pointsToUse.value = 0
    return
  }
  
  pointsToUse.value = Math.min(currentPoints.value, maxUsable)
}

// Validation setup
const { handleSubmit, isSubmitting, setValues } = useForm({
  validationSchema: toTypedSchema(checkoutSchema),
  initialValues: {
    paymentMethod: 'COD'
  }
})

const { value: fullName, errorMessage: fullNameError } = useField<string>('fullName')
const { value: phoneNumber, errorMessage: phoneNumberError } = useField<string>('phoneNumber')
const { value: address, errorMessage: addressError } = useField<string>('address')
const { value: paymentMethod, errorMessage: paymentMethodError } = useField<'COD' | 'VNPAY'>('paymentMethod')
const { value: note } = useField<string>('note')

const loadCheckoutData = async () => {
  if (!authStore.isAuthenticated) return
  try {
    const response = await orderService.getCheckoutData()
    if (response) {
      autoDiscountPercent.value = response.autoDiscountPercent || 0
      tierDiscountAmount.value = response.tierDiscountAmount || 0
      currentPoints.value = response.currentPoints || 0
      // We can also load vouchers from here instead of getMyVouchers if we want, but let's keep getMyVouchers logic as is or use this.
      if (response.availableVouchers) {
        myVouchers.value = response.availableVouchers
      }
    }
  } catch (error) {
    console.error('Failed to load checkout data:', error)
  }
}

onMounted(async () => {
  // Check if cart is empty
  if (cartItems.value.length === 0) {
    router.push('/cart')
    return
  }

  // Auto-fill from user profile
  if (authStore.user) {
    setValues({
      fullName: authStore.user.fullName || '',
      phoneNumber: authStore.user.phone || '',
      address: '',
      paymentMethod: 'COD'
    })
  }

  // Load addresses and prefill default
  await addressStore.fetch()
  const def = addressStore.defaultAddress
  if (def) selectAddress(def)

  loadCheckoutData()
})

function selectAddress(a: Address) {
  selectedAddressId.value = a.id
  setValues({
    fullName: a.recipientName,
    phoneNumber: a.recipientPhone,
    address: formatAddress({ streetDetail: a.streetDetail, wardCode: a.wardCode, districtCode: a.districtCode, provinceCode: a.provinceCode })
  })
}
const openAddressModal = () => { editingAddress.value = null; showAddressModal.value = true }
const editAtCheckout = (a: Address) => { editingAddress.value = a; showAddressModal.value = true }
const onSubmitAddressAtCheckout = async (data: any) => {
  try {
    const saved: Address = editingAddress.value
      ? await addressStore.update(editingAddress.value.id, data)
      : await addressStore.create(data)
    showAddressModal.value = false
    selectAddress(saved)
    checkoutToast.success('Đã lưu địa chỉ')
  } catch (e: any) { checkoutToast.error(e.response?.data?.message || 'Không thể lưu địa chỉ') }
}

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price)
}

const applyVoucher = async () => {
  if (!voucherCode.value) return
  isApplyingVoucher.value = true
  voucherMessage.value = ''
  voucherError.value = false

  try {
    const data = await loyaltyService.validateVoucher(voucherCode.value.trim(), total.value)

    if (data) {
      appliedVoucher.value = {
        code: data.code,
        discountAmount: data.discountAmount
      }
      voucherMessage.value = 'Áp dụng mã giảm giá thành công!'
    }
  } catch (error: any) {
    console.error('Failed to apply voucher:', error)
    voucherError.value = true
    voucherMessage.value = error.response?.data?.message || 'Mã giảm giá không hợp lệ hoặc đã hết hạn.'
  } finally {
    isApplyingVoucher.value = false
  }
}

const applyMyVoucher = (code: string) => {
  voucherCode.value = code
  applyVoucher()
}

const removeVoucher = () => {
  appliedVoucher.value = null
  voucherCode.value = ''
  voucherMessage.value = ''
  voucherError.value = false
}

const onSubmit = handleSubmit(async (values) => {
  if (cartItems.value.length === 0) {
    return
  }

  if (!authStore.isAuthenticated) {
    router.push('/login')
    return
  }

  if (pointsToUse.value > currentPoints.value) {
    pointsError.value = 'Số điểm sử dụng vượt quá số điểm hiện có.'
    return
  }
  pointsError.value = ''

  try {
    const orderData = {
      fullName: values.fullName,
      phoneNumber: values.phoneNumber,
      address: values.address,
      paymentMethod: values.paymentMethod,
      note: values.note,
      voucherCode: appliedVoucher.value?.code || null,
      pointsToUse: pointsToUse.value > 0 ? pointsToUse.value : null
    }

    const response = await orderService.createOrder(orderData)

    // Clear cart after successful order
    cartStore.clearCart()

    if (response.order.paymentUrl) {
      window.location.href = response.order.paymentUrl
    } else {
      router.push(`/order/${response.order.id}`)
    }

  } catch (error: any) {
    console.error('Failed to place order:', error)
    checkoutToast.error('Đặt hàng thất bại. Vui lòng thử lại.')
  }
})
</script>

<style scoped>
.font-inter {
  font-family: 'Geist', sans-serif;
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
