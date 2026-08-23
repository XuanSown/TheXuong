<template>
  <div class="min-h-screen bg-white">
    <main class="w-full max-w-[1280px] mx-auto px-4 pt-[120px] pb-8">
      <div class="w-[1152px] mx-auto">
        <!-- Header Section -->
        <header class="flex flex-col gap-[48px] mb-16">
          <!-- Breadcrumb -->
          <div class="flex items-center gap-3">
            <router-link
              to="/orders"
              class="flex items-center gap-2 text-black hover:text-[#5E5F5C] transition-colors"
            >
              <svg
                class="w-[9.33px] h-[9.33px]"
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
              <span class="font-gelasio text-[14px]">{{ t('order.backToHistory') }}</span>
            </router-link>
          </div>

          <!-- Page Title -->
          <div class="flex items-center justify-between">
            <div class="flex flex-col gap-2">
              <h1 class="font-geist text-[30px] font-bold leading-[36px] tracking-[-0.75px] text-[#111111]">
                {{ t('order.detail', { id: orderId }) }}
              </h1>
              <span class="font-geist text-[14px] text-[#666666]">
                {{ t('order.placedAt', { date: formatDate(order?.createdAt || '') }) }}
              </span>
            </div>
            <div
              v-if="order"
              class="flex items-center gap-3"
            >
              <button
                v-if="order.status === 'PENDING'"
                class="px-4 py-1.5 border border-red-500 text-red-500 rounded font-geist text-[12px] font-medium hover:bg-red-50 transition-colors"
                :disabled="isCancelling"
                @click="confirmCancelOrder"
              >
                {{ isCancelling ? t('order.processing') : t('order.cancelRequest') }}
              </button>
              <button
                v-if="order.status === 'DELIVERED'"
                class="px-4 py-1.5 bg-black text-white rounded font-geist text-[12px] font-medium hover:bg-gray-900 transition-colors"
                :disabled="isConfirmingReceived"
                @click="confirmReceivedOrder"
              >
                {{ isConfirmingReceived ? t('order.processing') : t('order.receivedGoods') }}
              </button>
              <div class="px-3 py-1 bg-[#FEF3C3] rounded">
                <span class="font-geist text-[12px] font-semibold text-[#92400E] uppercase tracking-[0.6px]">
                  {{ getStatusLabel(order.status) }}
                </span>
              </div>
            </div>
          </div>
        </header>

        <!-- Order Content -->
        <section class="flex gap-6 relative">
          <!-- Left Column: Shipping & Payment Info -->
          <div class="flex flex-col gap-6 w-[357.33px]">
            <!-- Shipping Info Card -->
            <div class="bg-[#F9F9F9] border border-[#F0F0F0] rounded-lg p-6">
              <div class="flex justify-between items-center mb-4">
                <h2 class="font-geist text-[14px] font-bold leading-[20px] tracking-[0.7px] uppercase text-[#111111]">
                  {{ t('order.recipientInfo') }}
                </h2>
                <button
                  v-if="order?.status === 'PENDING' && !isEditing"
                  class="text-blue-600 text-[14px] font-medium hover:underline"
                  @click="startEdit"
                >
                  {{ t('common.edit') }}
                </button>
              </div>
              <div
                v-if="order && !isEditing"
                class="flex flex-col gap-4"
              >
                <div class="flex flex-col gap-1">
                  <span class="font-geist text-[14px] text-[#666666]">{{ t('order.fullName') }}</span>
                  <span class="font-geist text-[14px] font-medium text-[#111111]">{{ order.fullName }}</span>
                </div>
                <div class="flex flex-col gap-1">
                  <span class="font-geist text-[14px] text-[#666666]">{{ t('order.phone') }}</span>
                  <span class="font-geist text-[14px] font-medium text-[#111111]">{{ order.phoneNumber }}</span>
                </div>
                <div class="flex flex-col gap-1">
                  <span class="font-geist text-[14px] text-[#666666]">{{ t('order.shippingAddress') }}</span>
                  <span class="font-geist text-[14px] font-medium text-[#111111] leading-[23px]">
                    {{ order.address }}
                  </span>
                </div>
                <div
                  v-if="order.note"
                  class="flex flex-col gap-1 pt-2 border-t border-[#F0F0F0]"
                >
                  <span class="font-geist text-[14px] text-[#666666]">{{ t('order.note') }}</span>
                  <span class="font-geist text-[14px] font-medium text-[#111111]">{{ order.note }}</span>
                </div>
              </div>

              <!-- Edit Mode -->
              <div
                v-if="order && isEditing"
                class="flex flex-col gap-4"
              >
                <div class="flex flex-col gap-1">
                  <label class="text-[12px] text-[#666666]">{{ t('order.phone') }}</label>
                  <input
                    v-model="editForm.phoneNumber"
                    class="border border-[#E0E0E0] px-3 py-2 rounded-md w-full text-[14px] font-medium text-[#111111] outline-none focus:border-black"
                    :placeholder="t('order.newPhonePlaceholder')"
                  >
                </div>
                <div class="flex flex-col gap-1">
                  <label class="text-[12px] text-[#666666]">{{ t('order.shippingAddress') }}</label>
                  <textarea
                    v-model="editForm.address"
                    class="border border-[#E0E0E0] px-3 py-2 rounded-md w-full text-[14px] font-medium text-[#111111] outline-none focus:border-black resize-none"
                    rows="3"
                    :placeholder="t('order.newAddressPlaceholder')"
                  />
                </div>
                <div class="flex gap-3 justify-end mt-2">
                  <button
                    class="px-4 py-2 border border-black text-black text-[14px] font-medium rounded-md hover:bg-black hover:text-white transition-colors"
                    :disabled="isSaving"
                    @click="isEditing = false"
                  >
                    {{ t('order.cancel') }}
                  </button>
                  <button
                    class="px-4 py-2 bg-black text-white text-[14px] font-medium rounded-md hover:bg-gray-900 transition-colors flex items-center justify-center min-w-[80px]"
                    :disabled="isSaving"
                    @click="saveShippingInfo"
                  >
                    <span v-if="!isSaving">{{ t('order.save') }}</span>
                    <div
                      v-else
                      class="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"
                    />
                  </button>
                </div>
              </div>
            </div>

            <!-- Payment Info Card -->
            <div class="bg-[#F9F9F9] border border-[#F0F0F0] rounded-lg p-6">
              <h2 class="font-geist text-[14px] font-bold leading-[20px] tracking-[0.7px] uppercase text-[#111111] mb-4">
                {{ t('order.paymentMethod') }}
              </h2>
              <div v-if="order">
                <span class="font-geist text-[14px] font-medium text-[#111111]">
                  {{ getPaymentMethodLabel(order.paymentMethod) }}
                </span>
              </div>
            </div>
          </div>

          <!-- Right Column: Product List & Totals -->
          <div class="flex-1 border border-[#F0F0F0] rounded-lg overflow-hidden">
            <!-- Header -->
            <div class="bg-[#F9F9F9] border-b border-[#F0F0F0] px-6 py-6">
              <h2 class="font-geist text-[14px] font-bold leading-[20px] tracking-[0.7px] uppercase text-[#111111]">
                {{ t('order.purchasedProducts') }}
              </h2>
            </div>

            <!-- Items -->
            <div class="p-6">
              <div
                v-if="order"
                class="flex flex-col gap-6"
              >
                <div
                  v-for="(item, index) in order.items"
                  :key="index"
                  class="flex gap-6"
                  :class="{ 'border-t border-[#F0F0F0] pt-6': index > 0 }"
                >
                  <!-- Product Image -->
                  <div class="w-[96px] h-[96px] bg-[#F9F9F9] rounded flex-shrink-0 flex items-center justify-center overflow-hidden">
                    <img
                      v-if="item.imageUrl"
                      :src="item.imageUrl"
                      :alt="item.productName"
                      class="w-full h-full object-cover"
                    >
                    <svg
                      v-else
                      class="w-10 h-10 text-[#CFC4C5]"
                      viewBox="0 0 24 24"
                      fill="none"
                      stroke="currentColor"
                      stroke-width="2"
                    >
                      <rect
                        x="3"
                        y="3"
                        width="18"
                        height="18"
                        rx="2"
                        ry="2"
                      />
                      <circle
                        cx="8.5"
                        cy="8.5"
                        r="1.5"
                      />
                      <polyline points="21 15 16 10 5 21" />
                    </svg>
                  </div>

                  <!-- Product Info -->
                  <div class="flex-1 flex flex-col justify-between">
                    <div class="flex justify-between items-start">
                      <div class="flex flex-col gap-1">
                        <h3 class="font-geist text-[16px] font-bold leading-[24px] text-[#111111]">
                          {{ item.productName }}
                        </h3>
                        <p class="font-geist text-[14px] text-[#666666]">
                          {{ t('order.itemSizeQty', { size: item.size, qty: item.quantity }) }}
                        </p>
                      </div>
                      <div class="flex flex-col items-end gap-1">
                        <span class="font-geist text-[16px] font-medium text-[#111111]">
                          {{ formatPrice(item.price) }}
                        </span>
                      </div>
                    </div>
                    <div class="flex justify-end mt-2">
                      <span class="font-geist text-[14px] font-bold text-[#111111]">
                        {{ formatPrice(item.price * item.quantity) }}
                      </span>
                    </div>
                    <div
                      v-if="showReviewButtons"
                      class="flex justify-end mt-3"
                    >
                      <button
                        v-if="reviewedProductIds.has(item.productId)"
                        class="flex items-center gap-2 px-4 py-1.5 border border-black text-black rounded font-geist text-[12px] font-medium hover:bg-gray-50 transition-colors"
                        @click="goToReview(item.productId)"
                      >
                        <svg
                          class="w-[14px] h-[14px]"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          stroke-width="2"
                        >
                          <polyline points="20 6 9 17 4 12" />
                        </svg>
                        {{ t('order.reviewed') }}
                      </button>
                      <button
                        v-else
                        class="flex items-center gap-2 px-4 py-1.5 bg-black text-white rounded font-geist text-[12px] font-medium hover:bg-gray-900 transition-colors"
                        @click="goToReview(item.productId)"
                      >
                        <svg
                          class="w-[14px] h-[14px]"
                          viewBox="0 0 24 24"
                          fill="currentColor"
                        >
                          <path d="M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.285 5.385a.562.562 0 01-.84.61l-4.725-2.885a.563.563 0 00-.586 0L6.982 20.54a.562.562 0 01-.84-.61l1.285-5.386a.562.562 0 00-.182-.557l-4.204-3.602a.563.563 0 01.321-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z" />
                        </svg>
                        {{ t('order.reviewProduct') }}
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>

            <!-- Totals -->
            <div class="bg-[#F9F9F9] border-t border-[#F0F0F0] p-6">
              <div
                v-if="order"
                class="flex flex-col gap-4"
              >
                <div class="flex justify-between items-center">
                  <span class="font-geist text-[14px] text-[#666666]">{{ t('common.subtotal') }}</span>
                  <span class="font-geist text-[14px] font-medium text-[#111111]">{{ formatPrice(order.subtotal) }}</span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="font-geist text-[14px] text-[#666666]">{{ t('cart.shippingFee') }}</span>
                  <span class="font-geist text-[14px] font-medium text-[#111111]">{{ t('cart.free') }}</span>
                </div>
                <div class="border-t border-[#F0F0F0] pt-4">
                  <div class="flex justify-between items-center">
                    <span class="font-geist text-[14px] font-bold leading-[20px] tracking-[0.7px] uppercase text-[#111111]">
                      {{ t('order.totalPay') }}
                    </span>
                    <span class="font-geist text-[20px] font-bold text-[#111111]">{{ formatPrice(order.totalMoney || order.total || 0) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </main>

    <!-- Footer -->
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useOrderStore } from '@/stores/order.store'
import orderService from '@/services/order.service'
import { reviewService } from '@/services/review.service'
import { useI18n } from 'vue-i18n'
import { formatCurrency, formatDate as formatDateByLocale } from '@/utils/formatters'
import { getApiErrorMessage } from '@/utils/apiError'

const { t } = useI18n()

const route = useRoute()
const router = useRouter()
const orderStore = useOrderStore()

const orderId = computed(() => route.params.id as string)

const order = computed(() => orderStore.currentOrder)

const isEditing = ref(false)
const isSaving = ref(false)
const isCancelling = ref(false)
const editForm = ref({
  phoneNumber: '',
  address: ''
})

const reviewedProductIds = ref<Set<number>>(new Set())

const showReviewButtons = computed(() => order.value?.status === 'COMPLETED')

const loadReviewedProducts = async () => {
  if (!order.value || !showReviewButtons.value) return
  const results = await Promise.allSettled(
    order.value.items.map(async (item) => ({
      productId: item.productId,
      reviewed: (await reviewService.getProductReviews(item.productId)).reviews.some((r) => r.isMine)
    }))
  )
  const next = new Set<number>()
  for (const r of results) {
    if (r.status === 'fulfilled' && r.value.reviewed) {
      next.add(r.value.productId)
    }
  }
  reviewedProductIds.value = next
}

const goToReview = (productId: number) => {
  router.push({ path: `/product-detail/${productId}`, query: { review: '1' } })
}

const startEdit = () => {
  if (order.value) {
    editForm.value = {
      phoneNumber: order.value.phoneNumber || '',
      address: order.value.address || ''
    }
    isEditing.value = true
  }
}

const saveShippingInfo = async () => {
  if (!order.value) return
  if (!editForm.value.phoneNumber.trim() || !editForm.value.address.trim()) {
    alert(t('order.fillInfo'))
    return
  }
  
  try {
    isSaving.value = true
    await orderService.updateOrderInfo(orderId.value, editForm.value)
    // Refetch the order to get the updated info
    await orderStore.fetchOrderById(orderId.value)
    isEditing.value = false
  } catch (error: any) {
    console.error('Failed to update info', error)
    alert(getApiErrorMessage(error, 'order.updateError'))
  } finally {
    isSaving.value = false
  }
}

const confirmCancelOrder = async () => {
  if (!order.value) return
  if (!confirm(t('order.cancelConfirm'))) {
    return
  }

  try {
    isCancelling.value = true
    await orderService.cancelOrder(orderId.value)
    alert(t('order.cancelSent'))
    await orderStore.fetchOrderById(orderId.value)
  } catch (error: any) {
    console.error('Failed to cancel order:', error)
    alert(getApiErrorMessage(error, 'order.cancelError'))
  } finally {
    isCancelling.value = false
  }
}

const isConfirmingReceived = ref(false)

const confirmReceivedOrder = async () => {
  if (!order.value) return
  if (!confirm(t('order.receivedConfirm'))) {
    return
  }

  try {
    isConfirmingReceived.value = true
    await orderService.confirmReceived(orderId.value)
    alert(t('order.receivedThanks'))
    await orderStore.fetchOrderById(orderId.value)
    await loadReviewedProducts()
  } catch (error: any) {
    console.error('Failed to confirm received:', error)
    alert(getApiErrorMessage(error, 'order.receivedError'))
  } finally {
    isConfirmingReceived.value = false
  }
}

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    'processing': t('orderStatus.processing'),
    'PENDING': t('orderStatus.pending'),
    'CONFIRMED': t('orderStatus.confirmed'),
    'SHIPPING': t('orderStatus.shipping'),
    'DELIVERED': t('orderStatus.delivered'),
    'COMPLETED': t('orderStatus.completed'),
    'CANCELLED': t('orderStatus.cancelled'),
    'CANCEL_REQUESTED': t('orderStatus.cancelRequested'),
    'REFUNDED': t('orderStatus.refunded'),
    'refunded': t('orderStatus.refunded')
  }
  return labels[status] || status.toUpperCase()
}

const getPaymentMethodLabel = (method: string) => {
  const labels: Record<string, string> = {
    'cod': t('paymentMethod.cod'),
    'vnpay': t('paymentMethod.vnpay'),
    'bank_transfer': t('paymentMethod.bankTransfer'),
    'momo': t('paymentMethod.momo')
  }
  return labels[method] || method
}

const formatDate = (dateString: string) => {
  if (!dateString) return 'N/A'
  return formatDateByLocale(dateString, {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatPrice = (price: number) => {
  return formatCurrency(price)
}

onMounted(async () => {
  if (!orderId.value) {
    return
  }

  try {
    await orderStore.fetchOrderById(orderId.value)
    await loadReviewedProducts()
  } catch (error) {
    console.error('Failed to fetch order:', error)
  }
})
</script>

<style scoped>
.font-inter {
  font-family: 'Geist', sans-serif;
}

/* Custom scrollbar */
.overflow-y-auto::-webkit-scrollbar {
  width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: transparent;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #CFC4C5;
  border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #A1A0A0;
}
</style>
