<template>
  <div class="min-h-screen bg-white">
    <main class="w-full max-w-[1280px] mx-auto px-4 pt-[120px] pb-8">
      <div class="w-[1152px] mx-auto">
        <!-- Header Section -->
        <header class="flex flex-col gap-[48px] mb-16">
          <!-- Breadcrumb -->
          <div class="flex items-center gap-3">
            <router-link to="/orders" class="flex items-center gap-2 text-[#666666] hover:text-black transition-colors">
              <svg class="w-[9.33px] h-[9.33px]" viewBox="0 0 13 13" fill="currentColor">
                <path d="M6.5 1L1 6.5l5.5 5.5M1 6.5L6.5 12" stroke="currentColor" stroke-width="1.5" fill="none"/>
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
            <div v-if="order" class="px-3 py-1 bg-[#FEF3C3] rounded">
              <span class="font-geist text-[12px] font-semibold text-[#92400E] uppercase tracking-[0.6px]">
                {{ getStatusLabel(order.status) }}
              </span>
            </div>
          </div>
        </header>

        <!-- Order Content -->
        <section class="flex gap-6 relative">
          <!-- Left Column: Shipping & Payment Info -->
          <div class="flex flex-col gap-6 w-[357.33px]">
            <!-- Shipping Info Card -->
            <div class="bg-[#F9F9F9] border border-[#F0F0F0] rounded-lg p-6">
              <h2 class="font-geist text-[14px] font-bold leading-[20px] tracking-[0.7px] uppercase text-[#111111] mb-4">
                THÔNG TIN NGƯỜI NHẬN
              </h2>
              <div class="flex flex-col gap-4" v-if="order">
                <div class="flex flex-col gap-1">
                  <span class="font-geist text-[14px] text-[#666666]">Họ và tên</span>
                  <span class="font-geist text-[14px] font-medium text-[#111111]">{{ order.shipping.fullName }}</span>
                </div>
                <div class="flex flex-col gap-1">
                  <span class="font-geist text-[14px] text-[#666666]">Số điện thoại</span>
                  <span class="font-geist text-[14px] font-medium text-[#111111]">{{ order.shipping.phone }}</span>
                </div>
                <div class="flex flex-col gap-1">
                  <span class="font-geist text-[14px] text-[#666666]">Địa chỉ giao hàng</span>
                  <span class="font-geist text-[14px] font-medium text-[#111111] leading-[23px]">
                    {{ order.shipping.address }}, {{ order.shipping.province }}
                  </span>
                </div>
                <div v-if="order.shipping.note" class="flex flex-col gap-1 pt-2 border-t border-[#F0F0F0]">
                  <span class="font-geist text-[14px] text-[#666666]">Ghi chú</span>
                  <span class="font-geist text-[14px] font-medium text-[#111111]">{{ order.shipping.note }}</span>
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
              <div v-if="order" class="flex flex-col gap-6">
                <div v-for="(item, index) in order.items" :key="index" class="flex gap-6" :class="{ 'border-t border-[#F0F0F0] pt-6': index > 0 }">
                  <!-- Product Image -->
                  <div class="w-[96px] h-[96px] bg-[#F9F9F9] rounded flex-shrink-0 flex items-center justify-center">
                    <svg class="w-10 h-10 text-[#CFC4C5]" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                      <rect x="3" y="3" width="18" height="18" rx="2" ry="2"/>
                      <circle cx="8.5" cy="8.5" r="1.5"/>
                      <polyline points="21 15 16 10 5 21"/>
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
              <div v-if="order" class="flex flex-col gap-4">
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
                    <span class="font-geist text-[20px] font-bold text-[#111111]">{{ formatPrice(order.total) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </section>
      </div>
    </main>

    <!-- Footer -->
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { useOrderStore } from '@/stores/order.store'

const route = useRoute()
const orderStore = useOrderStore()

const orderId = computed(() => route.params.id as string)

const order = computed(() => orderStore.currentOrder)

const getStatusLabel = (status: string) => {
  const labels: Record<string, string> = {
    'processing': 'ĐANG XỬ LÝ',
    'confirmed': 'ĐÃ XÁC NHẬN',
    'shipped': 'ĐANG VẬN CHUYỂN',
    'delivered': 'ĐÃ GIAO',
    'cancelled': 'ĐÃ HỦY',
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
