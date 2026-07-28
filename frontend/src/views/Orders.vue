<template>
  <div class="min-h-screen">
    <main class="w-full max-w-[1280px] mx-auto px-4 pb-8">
      <div class="w-[1152px] mx-auto">
        <!-- Header Section -->
        <header class="flex flex-col gap-[48px] mb-16">
          <!-- Breadcrumb/Back Link -->
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
              LỊCH SỬ ĐƠN HÀNG
            </h1>
          </div>
        </header>

        <!-- Filter Tabs -->
        <div class="flex gap-8 border-b border-[#CFC4C5] mb-6">
          <button
            v-for="tab in tabs"
            :key="tab.value"
            class="pb-3 text-[14px] font-medium transition-colors relative uppercase tracking-[0.5px]"
            :class="activeTab === tab.value ? 'text-black' : 'text-[#848484] hover:text-black'"
            @click="activeTab = tab.value"
          >
            {{ tab.label }}
            <div
              v-if="activeTab === tab.value"
              class="absolute bottom-[-1px] left-0 w-full h-[2px] bg-black"
            />
          </button>
        </div>

        <!-- Orders Card -->
        <div
          class="relative bg-white border border-[rgba(0,0,0,0.05)] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] rounded-xl overflow-hidden"
        >
          <!-- Table Header -->
          <div class="flex border-b border-[#CFC4C5] bg-[#F3F3F3]">
            <div class="w-[143.36px] h-[44px] flex items-center px-8">
              <span class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.8px] text-[#5E5F5C]">MÃ
                ĐH</span>
            </div>
            <div class="w-[173.5px] h-[44px] flex items-center px-6">
              <span class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.8px] text-[#5E5F5C]">NGÀY
                ĐẶT</span>
            </div>
            <div class="w-[317.3px] h-[44px] flex items-center px-6">
              <span class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.8px] text-[#5E5F5C]">ĐỊA CHỈ
                NHẬN HÀNG</span>
            </div>
            <div class="w-[186.61px] h-[44px] flex items-center px-6">
              <span class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.8px] text-[#5E5F5C]">TỔNG
                TIỀN</span>
            </div>
            <div class="w-[194.73px] h-[44px] flex items-center px-6">
              <span class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.8px] text-[#5E5F5C]">TRẠNG
                THÁI</span>
            </div>
            <div class="w-[183.5px] h-[44px] flex items-center justify-end px-8">
              <span
                class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.8px] text-[#5E5F5C] text-right"
              >CHI
                TIẾT</span>
            </div>
          </div>

          <!-- Loading State -->
          <div
            v-if="orderStore.loading"
            class="flex items-center justify-center py-16"
          >
            <div class="w-8 h-8 border-4 border-black border-t-transparent rounded-full animate-spin" />
          </div>

          <!-- Error State -->
          <div
            v-else-if="orderStore.error"
            class="flex flex-col items-center justify-center py-16"
          >
            <p class="font-gelasio text-lg text-red-500 mb-4">
              {{ orderStore.error }}
            </p>
            <button
              class="px-6 py-2 bg-black text-white text-sm rounded hover:bg-gray-900"
              @click="orderStore.fetchOrders()"
            >
              Thử lại
            </button>
          </div>

          <!-- Empty State -->
          <div
            v-else-if="orders.length === 0"
            class="flex flex-col items-center justify-center py-16"
          >
            <p class="font-gelasio text-lg text-[#5E5F5C] mb-4">
              Bạn chưa có đơn hàng nào
            </p>
            <router-link
              to="/products"
              class="px-6 py-2 bg-black text-white text-sm rounded hover:bg-gray-900"
            >
              Mua sắm ngay
            </router-link>
          </div>

          <!-- Orders List -->
          <div
            v-else
            class="max-h-[400px] overflow-y-auto"
          >
            <div
              v-for="order in filteredOrders"
              :key="order.id"
              class="flex border-b border-[#CFC4C5] hover:bg-gray-50 transition-colors"
            >
              <!-- Order ID -->
              <div class="w-[143.36px] h-[110px] flex items-center px-8">
                <span class="font-geist text-[16px] text-black">#{{ order.id }}</span>
              </div>
              <!-- Date -->
              <div class="w-[173.5px] h-[110px] flex items-center px-6">
                <span class="font-geist text-[16px] text-[#5E5F5C]">{{ formatDate(order.createdAt) }}</span>
              </div>
              <!-- Address -->
              <div class="w-[317.3px] h-[110px] flex flex-col justify-center px-6">
                <span class="font-geist text-[16px] text-[#1A1C1C] mb-1">{{ order.fullName }}</span>
                <span class="font-geist text-[14px] text-[#5E5F5C]">{{ order.phoneNumber }}</span>
                <span
                  class="font-geist text-[14px] text-[#5E5F5C] truncate"
                  :title="order.address"
                >{{ order.address
                }}</span>
              </div>
              <!-- Total -->
              <div class="w-[186.61px] h-[110px] flex items-center px-6">
                <span class="font-geist text-[16px] text-black">{{ formatPrice(order.totalMoney) }}</span>
              </div>
              <!-- Status -->
              <div class="w-[194.73px] h-[110px] flex items-center px-6">
                <span
                  :class="['inline-flex items-center gap-2 px-3 py-1 rounded-full font-geist text-[12px] uppercase tracking-[0.6px]',
                           getStatusBadgeClass(order.status)]"
                >
                  {{ getStatusLabel(order.status) }}
                </span>
              </div>
              <!-- Detail Button -->
              <div class="w-[183.5px] h-[110px] flex items-center justify-end px-8">
                <button
                  class="inline-flex items-center gap-2 px-4 py-2 border border-black hover:bg-black hover:text-white transition-colors"
                  @click="viewOrderDetail(order.id)"
                >
                  <svg
                    class="w-[14px] h-[14px]"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path d="M5 12h14M12 5l7 7-7 7" />
                  </svg>
                  <span class="font-geist text-[11px]">Xem</span>
                </button>
              </div>
            </div>
          </div>
        </div>

        <!-- View All Button -->
        <div class="flex justify-center py-6 bg-[#F3F3F3] border-t border-[#CFC4C5]">
          <button
            class="font-gelasio text-[12px] font-semibold leading-[12px] tracking-[1.8px] text-[#5E5F5C] hover:text-black transition-colors"
          >
            Xem tất cả lịch sử
          </button>
        </div>
      </div>
    </main>

    <!-- Footer -->
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useOrderStore } from '@/stores/order.store'

import type { OrderStatus } from '@/types'

const router = useRouter()
const authStore = useAuthStore()
const orderStore = useOrderStore()

const tabs = [
  { label: 'Tất cả', value: 'ALL' },
  { label: 'Chờ xử lý', value: 'PENDING' },
  { label: 'Đang vận chuyển', value: 'SHIPPING' },
  { label: 'Đã giao', value: 'DELIVERED' },
  { label: 'Hoàn thành', value: 'COMPLETED' },
  { label: 'Đã hủy', value: 'CANCELLED' }
]
const activeTab = ref('ALL')

onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.push('/login?redirect=/orders')
    return
  }
  await orderStore.fetchOrders()
})

const orders = computed(() => orderStore.orders)

const filteredOrders = computed<any[]>(() => {
  if (activeTab.value === 'ALL') return orders.value
  return orders.value.filter(o => o.status === activeTab.value)
})

const getStatusBadgeClass = (status: OrderStatus | string) => {
  switch (status) {
    case 'PENDING': return 'bg-[#FEF9C3] text-[#854D0E]'
    case 'CONFIRMED': return 'bg-[#E0E7FF] text-[#3730A3]'
    case 'CANCEL_REQUESTED': return 'bg-[#FFEDD5] text-[#C2410C]'
    case 'SHIPPING': return 'bg-blue-100 text-blue-600'
    case 'DELIVERED': return 'bg-[#DCFCE7] text-[#166534]'
    case 'COMPLETED': return 'bg-[#D1FAE5] text-[#065F46]'
    case 'CANCELLED': return 'bg-red-100 text-red-600'
    case 'REFUNDED': return 'bg-[#F3E8FF] text-[#6B21A8]'
    default: return 'bg-gray-100 text-gray-600'
  }
}

const getStatusLabel = (status: OrderStatus | string) => {
  switch (status) {
    case 'PENDING': return 'ĐANG XỬ LÝ'
    case 'CONFIRMED': return 'ĐÃ XÁC NHẬN'
    case 'CANCEL_REQUESTED': return 'YÊU CẦU HỦY'
    case 'SHIPPING': return 'ĐANG VẬN CHUYỂN'
    case 'DELIVERED': return 'ĐÃ GIAO'
    case 'COMPLETED': return 'HOÀN THÀNH'
    case 'CANCELLED': return 'ĐÃ HỦY'
    case 'REFUNDED': return 'ĐÃ HOÀN TIỀN'
    default: return status.toUpperCase()
  }
}

const formatDate = (dateString: string) => {
  const date = new Date(dateString)
  return date.toLocaleDateString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric'
  })
}

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN').format(price) + ' đ'
}

const viewOrderDetail = (orderId: number) => {
  router.push(`/order/${orderId}`)
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

/* Custom scrollbar */
.overflow-y-auto::-webkit-scrollbar {
  width: 6px;
}

.overflow-y-auto::-webkit-scrollbar-track {
  background: #f1f1f1;
}

.overflow-y-auto::-webkit-scrollbar-thumb {
  background: #CFC4C5;
  border-radius: 3px;
}

.overflow-y-auto::-webkit-scrollbar-thumb:hover {
  background: #A1A0A0;
}
</style>
