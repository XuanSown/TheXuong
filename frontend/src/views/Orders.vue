<template>
  <div class="min-h-screen bg-[#F3F3F3]">
    <main class="w-full max-w-[1280px] mx-auto px-4 pt-[120px] pb-8">
      <div class="w-[1152px] mx-auto">
        <!-- Header Section -->
        <header class="flex flex-col gap-[48px] mb-16">
          <!-- Breadcrumb/Back Link -->
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
              LỊCH SỬ ĐƠN HÀNG
            </h1>
          </div>
        </header>

        <!-- Orders Card -->
        <div class="relative bg-white border border-[rgba(0,0,0,0.05)] shadow-[0px_1px_2px_rgba(0,0,0,0.05)] rounded-xl overflow-hidden">
          <!-- Table Header -->
          <div class="flex border-b border-[#CFC4C5] bg-[#F3F3F3]">
            <div class="w-[143.36px] h-[44px] flex items-center px-8">
              <span class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.8px] text-[#5E5F5C]">MÃ ĐH</span>
            </div>
            <div class="w-[173.5px] h-[44px] flex items-center px-6">
              <span class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.8px] text-[#5E5F5C]">NGÀY ĐẶT</span>
            </div>
            <div class="w-[317.3px] h-[44px] flex items-center px-6">
              <span class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.8px] text-[#5E5F5C]">ĐỊA CHỈ NHẬN HÀNG</span>
            </div>
            <div class="w-[186.61px] h-[44px] flex items-center px-6">
              <span class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.8px] text-[#5E5F5C]">TỔNG TIỀN</span>
            </div>
            <div class="w-[194.73px] h-[44px] flex items-center px-6">
              <span class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.8px] text-[#5E5F5C]">TRẠNG THÁI</span>
            </div>
            <div class="w-[183.5px] h-[44px] flex items-center justify-end px-8">
              <span class="font-geist text-[12px] font-semibold leading-[12px] tracking-[1.8px] text-[#5E5F5C] text-right">CHI TIẾT</span>
            </div>
          </div>

          <!-- Orders List -->
          <div class="max-h-[400px] overflow-y-auto">
            <!-- Order Row 1 - Processing -->
            <div class="flex border-b border-[#CFC4C5] hover:bg-gray-50 transition-colors">
              <div class="w-[143.36px] h-[110px] flex items-center px-8">
                <span class="font-geist text-[16px] text-black">#001</span>
              </div>
              <div class="w-[173.5px] h-[110px] flex items-center px-6">
                <span class="font-geist text-[16px] text-[#5E5F5C]">Hôm nay</span>
              </div>
              <div class="w-[317.3px] h-[61px] flex flex-col justify-center px-6">
                <span class="font-geist text-[16px] text-[#1A1C1C] mb-1">Hải</span>
                <span class="font-geist text-[14px] text-[#5E5F5C]">0775182044</span>
                <span class="font-geist text-[14px] text-[#5E5F5C]">111</span>
              </div>
              <div class="w-[186.61px] h-[110px] flex items-center px-6">
                <div class="flex flex-col items-start gap-1">
                  <span class="font-geist text-[16px] text-black">1.850.000đ</span>
                </div>
              </div>
              <div class="w-[194.73px] h-[109px] flex items-center px-6">
                <div class="inline-flex items-center gap-2 px-3 py-1 bg-[#FEF9C3] rounded-full">
                  <span class="font-geist text-[12px] text-[#854D0E] uppercase tracking-[0.6px]">ĐANG XỬ LÝ</span>
                </div>
              </div>
              <div class="w-[183.5px] h-[103px] flex items-center justify-end px-8">
                <button class="inline-flex items-center gap-2 px-4 py-2 border border-black hover:bg-black hover:text-white transition-colors">
                  <svg class="w-[12.83px] h-[8.75px]" viewBox="0 0 13 9" fill="currentColor">
                    <path d="M5 1L1 8h2v2h2V8h2L5 1z" fill="currentColor"/>
                  </svg>
                  <span class="font-geist text-[11px]">Xem</span>
                </button>
              </div>
            </div>

            <!-- Order Row 2 - Delivered -->
            <div class="flex border-b border-[#CFC4C5] hover:bg-gray-50 transition-colors">
              <div class="w-[143.36px] h-[109.5px] flex items-center px-8">
                <span class="font-geist text-[16px] text-black">#098</span>
              </div>
              <div class="w-[173.5px] h-[109.5px] flex items-center px-6">
                <span class="font-geist text-[16px] text-[#5E5F5C]">20/10/2023</span>
              </div>
              <div class="w-[317.3px] h-[61px] flex flex-col justify-center px-6">
                <span class="font-geist text-[16px] text-[#1A1C1C] mb-1">Hải</span>
                <span class="font-geist text-[14px] text-[#5E5F5C]">0775182044</span>
                <span class="font-geist text-[14px] text-[#5E5F5C]">Lầu 5, Bitexco Financial Tower</span>
              </div>
              <div class="w-[186.61px] h-[109.5px] flex items-center px-6">
                <span class="font-geist text-[16px] text-black">4.500.000đ</span>
              </div>
              <div class="w-[194.73px] h-[108.5px] flex items-center px-6">
                <div class="inline-flex items-center gap-2 px-3 py-1 bg-[#DCFCE7] rounded-full">
                  <span class="font-geist text-[12px] text-[#166534] uppercase tracking-[0.6px]">ĐÃ GIAO</span>
                </div>
              </div>
              <div class="w-[183.5px] h-[102.5px] flex items-center justify-end px-8">
                <button class="inline-flex items-center gap-2 px-4 py-2 border border-black hover:bg-black hover:text-white transition-colors">
                  <svg class="w-[12.83px] h-[8.75px]" viewBox="0 0 13 9" fill="currentColor">
                    <path d="M5 1L1 8h2v2h2V8h2L5 1z" fill="currentColor"/>
                  </svg>
                  <span class="font-geist text-[11px]">Xem</span>
                </button>
              </div>
            </div>

            <!-- Order Row 3 -->
            <div class="flex border-b border-[#CFC4C5] hover:bg-gray-50 transition-colors">
              <div class="w-[143.36px] h-[110px] flex items-center px-8">
                <span class="font-geist text-[16px] text-black">#097</span>
              </div>
              <div class="w-[173.5px] h-[110px] flex items-center px-6">
                <span class="font-geist text-[16px] text-[#5E5F5C]">19/10/2023</span>
              </div>
              <div class="w-[317.3px] h-[61px] flex flex-col justify-center px-6">
                <span class="font-geist text-[16px] text-[#1A1C1C] mb-1">Hải</span>
                <span class="font-geist text-[14px] text-[#5E5F5C]">0775182044</span>
                <span class="font-geist text-[14px] text-[#5E5F5C]">123 Nguyễn Văn Linh, Quận 7</span>
              </div>
              <div class="w-[186.61px] h-[110px] flex items-center px-6">
                <span class="font-geist text-[16px] text-black">2.100.000đ</span>
              </div>
              <div class="w-[194.73px] h-[110px] flex items-center px-6">
                <div class="inline-flex items-center gap-2 px-3 py-1 bg-[#FEF9C3] rounded-full">
                  <span class="font-geist text-[12px] text-[#854D0E] uppercase tracking-[0.6px]">ĐANG XỬ LÝ</span>
                </div>
              </div>
              <div class="w-[183.5px] h-[110px] flex items-center justify-end px-8">
                <button class="inline-flex items-center gap-2 px-4 py-2 border border-black hover:bg-black hover:text-white transition-colors">
                  <svg class="w-[12.83px] h-[8.75px]" viewBox="0 0 13 9" fill="currentColor">
                    <path d="M5 1L1 8h2v2h2V8h2L5 1z" fill="currentColor"/>
                  </svg>
                  <span class="font-geist text-[11px]">Xem</span>
                </button>
              </div>
            </div>

            <!-- Order Row 4 - Cancelled -->
            <div class="flex border-b border-[#CFC4C5] hover:bg-gray-50 transition-colors">
              <div class="w-[143.36px] h-[110px] flex items-center px-8">
                <span class="font-geist text-[16px] text-black">#096</span>
              </div>
              <div class="w-[173.5px] h-[110px] flex items-center px-6">
                <span class="font-geist text-[16px] text-[#5E5F5C]">18/10/2023</span>
              </div>
              <div class="w-[317.3px] h-[61px] flex flex-col justify-center px-6">
                <span class="font-geist text-[16px] text-[#1A1C1C] mb-1">Hải</span>
                <span class="font-geist text-[14px] text-[#5E5F5C]">0775182044</span>
                <span class="font-geist text-[14px] text-[#5E5F5C]">123 Đường số 10, Quận 4</span>
              </div>
              <div class="w-[186.61px] h-[110px] flex items-center px-6">
                <span class="font-geist text-[16px] text-black">3.250.000đ</span>
              </div>
              <div class="w-[194.73px] h-[110px] flex items-center px-6">
                <div class="inline-flex items-center gap-2 px-3 py-1 bg-red-100 rounded-full">
                  <span class="font-geist text-[12px] text-red-600 uppercase tracking-[0.6px]">ĐÃ HỦY</span>
                </div>
              </div>
              <div class="w-[183.5px] h-[110px] flex items-center justify-end px-8">
                <button class="inline-flex items-center gap-2 px-4 py-2 border border-black hover:bg-black hover:text-white transition-colors">
                  <svg class="w-[12.83px] h-[8.75px]" viewBox="0 0 13 9" fill="currentColor">
                    <path d="M5 1L1 8h2v2h2V8h2L5 1z" fill="currentColor"/>
                  </svg>
                  <span class="font-geist text-[11px]">Xem</span>
                </button>
              </div>
            </div>

            <!-- Order Row 5 -->
            <div class="flex border-b border-[#CFC4C5] hover:bg-gray-50 transition-colors">
              <div class="w-[143.36px] h-[110px] flex items-center px-8">
                <span class="font-geist text-[16px] text-black">#095</span>
              </div>
              <div class="w-[173.5px] h-[110px] flex items-center px-6">
                <span class="font-geist text-[16px] text-[#5E5F5C]">15/10/2023</span>
              </div>
              <div class="w-[317.3px] h-[61px] flex flex-col justify-center px-6">
                <span class="font-geist text-[16px] text-[#1A1C1C] mb-1">Hải</span>
                <span class="font-geist text-[14px] text-[#5E5F5C]">0775182044</span>
                <span class="font-geist text-[14px] text-[#5E5F5C]">222 Võ Văn Ngân, Bình Thạnh</span>
              </div>
              <div class="w-[186.61px] h-[110px] flex items-center px-6">
                <span class="font-geist text-[16px] text-black">890.000đ</span>
              </div>
              <div class="w-[194.73px] h-[110px] flex items-center px-6">
                <div class="inline-flex items-center gap-2 px-3 py-1 bg-[#DCFCE7] rounded-full">
                  <span class="font-geist text-[12px] text-[#166534] uppercase tracking-[0.6px]">ĐÃ GIAO</span>
                </div>
              </div>
              <div class="w-[183.5px] h-[110px] flex items-center justify-end px-8">
                <button class="inline-flex items-center gap-2 px-4 py-2 border border-black hover:bg-black hover:text-white transition-colors">
                  <svg class="w-[12.83px] h-[8.75px]" viewBox="0 0 13 9" fill="currentColor">
                    <path d="M5 1L1 8h2v2h2V8h2L5 1z" fill="currentColor"/>
                  </svg>
                  <span class="font-geist text-[11px]">Xem</span>
                </button>
              </div>
            </div>
          </div>

          <!-- View All Button -->
          <div class="flex justify-center py-6 bg-[#F3F3F3] border-t border-[#CFC4C5]">
            <button class="font-gelasio text-[12px] font-semibold leading-[12px] tracking-[1.8px] text-[#5E5F5C] hover:text-black transition-colors">
              Xem tất cả lịch sử
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
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useOrderStore } from '@/stores/order.store'

const router = useRouter()
const authStore = useAuthStore()
const orderStore = useOrderStore()

onMounted(async () => {
  if (!authStore.isAuthenticated) {
    router.push('/login?redirect=/orders')
    return
  }

  await orderStore.fetchOrders()
})

const orders = computed(() => orderStore.orders)

const getStatusBadgeClass = (status: string) => {
  switch (status) {
    case 'processing':
      return 'bg-[#FEF9C3] text-[#854D0E]'
    case 'delivered':
      return 'bg-[#DCFCE7] text-[#166534]'
    case 'cancelled':
      return 'bg-red-100 text-red-600'
    case 'shipped':
      return 'bg-blue-100 text-blue-600'
    default:
      return 'bg-gray-100 text-gray-600'
  }
}

const getStatusLabel = (status: string) => {
  switch (status) {
    case 'processing':
      return 'ĐANG XỬ LÝ'
    case 'delivered':
      return 'ĐÃ GIAO'
    case 'cancelled':
      return 'ĐÃ HỦY'
    case 'shipped':
      return 'ĐANG VẬN CHUYỂN'
    default:
      return status.toUpperCase()
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
  return new Intl.NumberFormat('vi-VN').format(price) + 'đ'
}

const viewOrderDetail = (orderId: string) => {
  router.push(`/order/${orderId}`)
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

/* Row hover effect */
tr:hover {
  background-color: rgba(0, 0, 0, 0.02);
}
</style>
