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
              class="flex items-center gap-2 text-[#666666] hover:text-black transition-colors"
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
              <span class="font-gelasio text-[14px]">Quay lại lịch sử đơn hàng</span>
            </router-link>
          </div>

          <!-- Page Title -->
          <div class="flex items-center justify-between">
            <div class="flex flex-col gap-2">
              <h1 class="font-geist text-[30px] font-bold leading-[36px] tracking-[-0.75px] text-[#111111]">
                Chi tiết đơn hàng #{{ orderId }}
              </h1>
              <span class="font-geist text-[14px] text-[#666666]">
                Ngày đặt: {{ formatDate(order?.createdAt || '') }}
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
                {{ isCancelling ? 'Đang xử lý...' : 'Yêu cầu hủy đơn' }}
              </button>
              <button
                v-if="order.status === 'DELIVERED'"
                class="px-4 py-1.5 bg-black text-white rounded font-geist text-[12px] font-medium hover:bg-gray-900 transition-colors"
                :disabled="isConfirmingReceived"
                @click="confirmReceivedOrder"
              >
                {{ isConfirmingReceived ? 'Đang xử lý...' : 'Đã nhận được hàng' }}
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
                  THÔNG TIN NGƯỜI NHẬN
                </h2>
                <button
                  v-if="order?.status === 'PENDING' && !isEditing"
                  class="text-blue-600 text-[14px] font-medium hover:underline"
                  @click="startEdit"
                >
                  Sửa
                </button>
              </div>
              <div
                v-if="order && !isEditing"
                class="flex flex-col gap-4"
              >
                <div class="flex flex-col gap-1">
                  <span class="font-geist text-[14px] text-[#666666]">Họ và tên</span>
                  <span class="font-geist text-[14px] font-medium text-[#111111]">{{ order.fullName }}</span>
                </div>
                <div class="flex flex-col gap-1">
                  <span class="font-geist text-[14px] text-[#666666]">Số điện thoại</span>
                  <span class="font-geist text-[14px] font-medium text-[#111111]">{{ order.phoneNumber }}</span>
                </div>
                <div class="flex flex-col gap-1">
                  <span class="font-geist text-[14px] text-[#666666]">Địa chỉ giao hàng</span>
                  <span class="font-geist text-[14px] font-medium text-[#111111] leading-[23px]">
                    {{ order.address }}
                  </span>
                </div>
                <div
                  v-if="order.note"
                  class="flex flex-col gap-1 pt-2 border-t border-[#F0F0F0]"
                >
                  <span class="font-geist text-[14px] text-[#666666]">Ghi chú</span>
                  <span class="font-geist text-[14px] font-medium text-[#111111]">{{ order.note }}</span>
                </div>
              </div>

              <!-- Edit Mode -->
              <div
                v-if="order && isEditing"
                class="flex flex-col gap-4"
              >
                <div class="flex flex-col gap-1">
                  <label class="text-[12px] text-[#666666]">Số điện thoại</label>
                  <input
                    v-model="editForm.phoneNumber"
                    class="border border-[#E0E0E0] px-3 py-2 rounded-md w-full text-[14px] font-medium text-[#111111] outline-none focus:border-black"
                    placeholder="Nhập số điện thoại mới"
                  >
                </div>
                <div class="flex flex-col gap-1">
                  <label class="text-[12px] text-[#666666]">Địa chỉ giao hàng</label>
                  <textarea
                    v-model="editForm.address"
                    class="border border-[#E0E0E0] px-3 py-2 rounded-md w-full text-[14px] font-medium text-[#111111] outline-none focus:border-black resize-none"
                    rows="3"
                    placeholder="Nhập địa chỉ mới"
                  />
                </div>
                <div class="flex gap-3 justify-end mt-2">
                  <button
                    class="px-4 py-2 border border-black text-black text-[14px] font-medium rounded-md hover:bg-black hover:text-white transition-colors"
                    :disabled="isSaving"
                    @click="isEditing = false"
                  >
                    Hủy
                  </button>
                  <button
                    class="px-4 py-2 bg-black text-white text-[14px] font-medium rounded-md hover:bg-gray-900 transition-colors flex items-center justify-center min-w-[80px]"
                    :disabled="isSaving"
                    @click="saveShippingInfo"
                  >
                    <span v-if="!isSaving">Lưu</span>
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
                PHƯƠNG THỨC THANH TOÁN
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
                SẢN PHẨM ĐÃ MUA
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
                          Size: {{ item.size }} | SL: {{ item.quantity }}
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
                  <span class="font-geist text-[14px] text-[#666666]">Tạm tính</span>
                  <span class="font-geist text-[14px] font-medium text-[#111111]">{{ formatPrice(order.subtotal) }}</span>
                </div>
                <div class="flex justify-between items-center">
                  <span class="font-geist text-[14px] text-[#666666]">Phí vận chuyển</span>
                  <span class="font-geist text-[14px] font-medium text-[#111111]">Miễn phí</span>
                </div>
                <div class="border-t border-[#F0F0F0] pt-4">
                  <div class="flex justify-between items-center">
                    <span class="font-geist text-[14px] font-bold leading-[20px] tracking-[0.7px] uppercase text-[#111111]">
                      TỔNG THANH TOÁN
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
import { useRoute } from 'vue-router'
import { useOrderStore } from '@/stores/order.store'
import orderService from '@/services/order.service'

const route = useRoute()
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
    alert('Vui lòng nhập đầy đủ thông tin')
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
    alert(error?.response?.data?.error || 'Đã xảy ra lỗi khi cập nhật thông tin')
  } finally {
    isSaving.value = false
  }
}

const confirmCancelOrder = async () => {
  if (!order.value) return
  if (!confirm('Bạn có chắc chắn muốn yêu cầu hủy đơn hàng này không?')) {
    return
  }

  try {
    isCancelling.value = true
    await orderService.cancelOrder(orderId.value)
    alert('Yêu cầu hủy đơn hàng đã được gửi thành công.')
    await orderStore.fetchOrderById(orderId.value)
  } catch (error: any) {
    console.error('Failed to cancel order:', error)
    alert(error?.response?.data?.error || 'Đã xảy ra lỗi khi hủy đơn hàng')
  } finally {
    isCancelling.value = false
  }
}

const isConfirmingReceived = ref(false)

const confirmReceivedOrder = async () => {
  if (!order.value) return
  if (!confirm('Bạn xác nhận đã nhận được hàng? (Đơn hàng sẽ chuyển sang trạng thái Hoàn thành và bạn sẽ được cộng điểm)')) {
    return
  }

  try {
    isConfirmingReceived.value = true
    await orderService.confirmReceived(orderId.value)
    alert('Cảm ơn bạn đã xác nhận nhận hàng! Đơn hàng đã hoàn tất.')
    await orderStore.fetchOrderById(orderId.value)
  } catch (error: any) {
    console.error('Failed to confirm received:', error)
    alert(error?.response?.data?.error || 'Đã xảy ra lỗi khi xác nhận nhận hàng')
  } finally {
    isConfirmingReceived.value = false
  }
}

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    'processing': 'ĐANG XỬ LÝ',
    'PENDING': 'CHỜ XỬ LÝ',
    'CONFIRMED': 'ĐÃ XÁC NHẬN',
    'SHIPPING': 'ĐANG VẬN CHUYỂN',
    'DELIVERED': 'ĐÃ GIAO',
    'COMPLETED': 'HOÀN THÀNH',
    'CANCELLED': 'ĐÃ HỦY',
    'CANCEL_REQUESTED': 'YÊU CẦU HỦY',
    'REFUNDED': 'ĐÃ HOÀN TIỀN',
    'refunded': 'ĐÃ HOÀN TIỀN'
  }
  return labels[status] || status.toUpperCase()
}

const getPaymentMethodLabel = (method: string) => {
  const labels: Record<string, string> = {
    'cod': 'Thanh toán khi nhận hàng (COD)',
    'vnpay': 'Thanh toán qua VNPay',
    'bank_transfer': 'Chuyển khoản ngân hàng',
    'momo': 'Thanh toán qua Momo'
  }
  return labels[method] || method
}

const formatDate = (dateString: string) => {
  if (!dateString) return 'N/A'
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  })
}

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
    minimumFractionDigits: 0,
    maximumFractionDigits: 0
  }).format(price).replace('₫', 'đ')
}

onMounted(async () => {
  if (!orderId.value) {
    return
  }

  try {
    await orderStore.fetchOrderById(orderId.value)
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
