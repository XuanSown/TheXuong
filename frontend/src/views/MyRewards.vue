<template>
  <div class="my-rewards-page min-h-screen">
    <main class="w-full max-w-[1280px] mx-auto px-4 pb-8">
      <div class="w-[1152px] mx-auto">
        <!-- Header -->
        <header class="mb-10 text-center">
          <h1 class="font-geist text-[32px] font-bold text-black mb-2">
            ĐIỂM & VOUCHER
          </h1>
          <p class="font-gelasio text-lg text-[#5E5F5C]">
            Số điểm hiện tại của bạn: <span
              class="font-bold text-black text-2xl"
            >{{ currentPoints }}</span>
          </p>
        </header>

        <!-- Tabs -->
        <div class="flex justify-center gap-8 mb-8 border-b border-[#E8E8E8]">
          <button
            class="pb-4 font-geist text-[16px] font-bold tracking-[1px] uppercase transition-colors"
            :class="activeTab === 'catalog' ? 'text-black border-b-2 border-black' : 'text-[#848484] hover:text-black'"
            @click="activeTab = 'catalog'"
          >
            Đổi Voucher
          </button>
          <button
            class="pb-4 font-geist text-[16px] font-bold tracking-[1px] uppercase transition-colors"
            :class="activeTab === 'my-vouchers' ? 'text-black border-b-2 border-black' : 'text-[#848484] hover:text-black'"
            @click="activeTab = 'my-vouchers'"
          >
            Voucher của tôi
          </button>
        </div>

        <!-- Content -->
        <div class="bg-white rounded-xl shadow-[0_8px_30px_rgba(0,0,0,0.04)] p-8">
          <!-- Catalog Tab -->
          <div v-if="activeTab === 'catalog'">
            <div
              v-if="isLoadingCatalog"
              class="text-center py-10"
            >
              Đang tải danh mục...
            </div>
            <div
              v-else-if="catalog.length === 0"
              class="text-center py-10 text-[#848484]"
            >
              Hiện tại không có voucher nào để đổi.
            </div>
            <div
              v-else
              class="grid grid-cols-2 gap-6"
            >
              <div
                v-for="item in catalog"
                :key="item.id"
                class="border border-[#EEEEEE] rounded-lg p-6 flex flex-col justify-between hover:border-black transition-colors"
              >
                <div>
                  <div class="flex justify-between items-start mb-4">
                    <span class="bg-black text-white text-xs font-bold px-3 py-1 rounded">Giảm {{
                      formatPrice(item.discountAmount) }}</span>
                    <span
                      v-if="item.vipOnly"
                      class="bg-yellow-400 text-black text-xs font-bold px-3 py-1 rounded"
                    >VIP</span>
                  </div>
                  <h3 class="font-geist font-bold text-lg mb-2">
                    Voucher {{ formatPrice(item.discountAmount) }}
                  </h3>
                  <p class="font-gelasio text-sm text-[#5E5F5C] mb-1">
                    Đơn tối thiểu: {{ item.minOrderAmount > 0 ? formatPrice(item.minOrderAmount) : 'Không yêu cầu' }}
                  </p>
                  <p class="font-gelasio text-sm text-[#5E5F5C] mb-4">
                    Điểm cần để đổi: <strong class="text-black">{{ item.requiredPoints }} điểm</strong>
                  </p>
                </div>
                <BaseButton
                  :disabled="currentPoints < item.requiredPoints || isRedeeming"
                  :loading="isRedeeming && redeemingId === item.id"
                  class="w-full !h-[44px]"
                  @click="handleRedeem(item)"
                >
                  {{ currentPoints >= item.requiredPoints ? 'Đổi ngay' : 'Không đủ điểm' }}
                </BaseButton>
              </div>
            </div>
          </div>

          <!-- My Vouchers Tab -->
          <div v-if="activeTab === 'my-vouchers'">
            <div class="flex gap-4 mb-6">
              <button
                v-for="status in ['ALL', 'UNUSED', 'USED', 'EXPIRED']"
                :key="status"
                class="px-4 py-2 rounded-full text-sm font-gelasio transition-colors"
                :class="voucherFilter === status ? 'bg-black text-white' : 'bg-[#F3F3F4] text-[#5E5F5C] hover:bg-[#E8E8E8]'"
                @click="voucherFilter = status"
              >
                {{ statusLabel(status) }}
              </button>
            </div>

            <div
              v-if="isLoadingMyVouchers"
              class="text-center py-10"
            >
              Đang tải voucher của bạn...
            </div>
            <div
              v-else-if="filteredMyVouchers.length === 0"
              class="text-center py-10 text-[#848484]"
            >
              Bạn chưa có voucher nào trong trạng thái này.
            </div>
            <div
              v-else
              class="grid grid-cols-2 gap-6"
            >
              <div
                v-for="uv in filteredMyVouchers"
                :key="uv.id"
                class="border rounded-lg p-6 flex flex-col justify-between"
                :class="uv.status === 'UNUSED' ? 'border-[#EEEEEE] bg-white' : 'border-[#F3F3F4] bg-[#F9F9F9] opacity-70'"
              >
                <div>
                  <div class="flex justify-between items-start mb-4">
                    <span class="font-geist font-bold text-xl tracking-wider">{{ uv.code }}</span>
                    <span
                      class="text-xs font-bold px-3 py-1 rounded"
                      :class="statusBadgeClass(uv.status)"
                    >
                      {{ statusLabel(uv.status) }}
                    </span>
                  </div>
                  <p class="font-gelasio text-sm text-[#5E5F5C] mb-1">
                    Ngày đổi: {{ formatDate(uv.issuedAt) }}
                  </p>
                  <p class="font-gelasio text-sm text-[#5E5F5C]">
                    Hạn dùng: {{ formatDate(uv.expiresAt) }}
                  </p>
                  <p
                    v-if="uv.status === 'USED'"
                    class="font-gelasio text-sm text-green-600 mt-2"
                  >
                    Đã dùng ngày: {{ formatDate(uv.usedAt) }}
                  </p>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import loyaltyService from '@/services/loyaltyService'

const currentPoints = ref(0)
const catalog = ref<any[]>([])
const myVouchers = ref<any[]>([])
const activeTab = ref<'catalog' | 'my-vouchers'>('catalog')
const voucherFilter = ref('ALL')

const isLoadingCatalog = ref(false)
const isLoadingMyVouchers = ref(false)
const isRedeeming = ref(false)
const redeemingId = ref<number | null>(null)

onMounted(async () => {
  await loadPoints()
  await loadCatalog()
  await loadMyVouchers()
})

const loadPoints = async () => {
  try {
    currentPoints.value = await loyaltyService.getPoints()
  } catch (error) {
    console.error('Failed to load points', error)
  }
}

const loadCatalog = async () => {
  isLoadingCatalog.value = true
  try {
    catalog.value = await loyaltyService.getCatalog()
  } catch (error) {
    console.error('Failed to load catalog', error)
  } finally {
    isLoadingCatalog.value = false
  }
}

const loadMyVouchers = async () => {
  isLoadingMyVouchers.value = true
  try {
    myVouchers.value = await loyaltyService.getMyVouchers()
  } catch (error) {
    console.error('Failed to load my vouchers', error)
  } finally {
    isLoadingMyVouchers.value = false
  }
}

const filteredMyVouchers = computed(() => {
  if (voucherFilter.value === 'ALL') return myVouchers.value
  return myVouchers.value.filter(v => v.status === voucherFilter.value)
})

const handleRedeem = async (item: any) => {
  if (!confirm(`Bạn có chắc chắn muốn đổi ${item.requiredPoints} điểm lấy voucher này?`)) return

  isRedeeming.value = true
  redeemingId.value = item.id
  try {
    await loyaltyService.redeemVoucher(item.id)
    alert('Đổi voucher thành công! Hãy kiểm tra trong tab Voucher của tôi.')
    await loadPoints()
    await loadMyVouchers()
    activeTab.value = 'my-vouchers'
    voucherFilter.value = 'UNUSED'
  } catch (error: any) {
    alert(error.response?.data?.message || 'Có lỗi xảy ra khi đổi voucher.')
  } finally {
    isRedeeming.value = false
    redeemingId.value = null
  }
}

const formatPrice = (price: number) => {
  return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price)
}

const formatDate = (dateStr: string) => {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('vi-VN')
}

const statusLabel = (status: string) => {
  switch (status) {
    case 'ALL': return 'Tất cả'
    case 'UNUSED': return 'Chưa dùng'
    case 'USED': return 'Đã dùng'
    case 'EXPIRED': return 'Hết hạn'
    default: return status
  }
}

const statusBadgeClass = (status: string) => {
  switch (status) {
    case 'UNUSED': return 'bg-green-100 text-green-700'
    case 'USED': return 'bg-gray-200 text-gray-700'
    case 'EXPIRED': return 'bg-red-100 text-red-700'
    default: return 'bg-gray-100'
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
</style>
