<template>
  <div class="min-h-screen bg-[#F9F9F9]">
    <main class="w-full max-w-[1280px] mx-auto px-4 pt-[120px] pb-8">
      <!-- Breadcrumbs -->
      <nav class="w-[1152px] mx-auto mb-8 flex items-center gap-2">
        <router-link to="/" class="font-geist text-[12px] font-semibold uppercase tracking-[1.8px] text-[#646562] hover:text-black transition-colors">
          TRANG CHỦ
        </router-link>
        <svg class="w-[4.32px] h-[7px]" viewBox="0 0 4 7" fill="currentColor">
          <path d="M0 3.5L4 0V7L0 3.5Z" fill="black"/>
        </svg>
        <router-link to="/products" class="font-geist text-[12px] font-semibold uppercase tracking-[1.8px] text-[#646562] hover:text-black transition-colors">
          SẢN PHẨM
        </router-link>
        <svg class="w-[4.32px] h-[7px]" viewBox="0 0 4 7" fill="currentColor">
          <path d="M0 3.5L4 0V7L0 3.5Z" fill="black"/>
        </svg>
        <span class="font-geist text-[12px] font-semibold uppercase tracking-[1.8px] text-black">
          {{ productName }}
        </span>
      </nav>

      <!-- Product Detail Section -->
      <section class="w-[1152px] mx-auto bg-white mb-8" v-if="product">
        <div class="flex gap-8 p-8">
          <!-- Left: Image Gallery -->
          <div class="flex gap-4">
            <!-- Thumbnail Column -->
            <div class="flex flex-col gap-4">
              <div v-for="(img, idx) in product.images" :key="idx" class="w-[80px] h-[80px] bg-[#F3F3F4] border border-[rgba(207,196,197,0.3)] flex items-center justify-center">
                <img :src="img" :alt="product.name" class="w-[78px] h-[78px] object-cover" />
              </div>
            </div>

            <!-- Main Image -->
            <div class="relative w-[556px] h-[556px] bg-[#F3F3F4] border border-[rgba(207,196,197,0.3)] flex items-center justify-center">
              <img :src="product.imageUrl" :alt="product.name" class="w-[554px] h-[554px] object-cover" />

              <!-- Zoom Controls -->
              <div class="absolute right-4 bottom-4 flex gap-2">
                <button class="w-10 h-10 bg-white/80 backdrop-blur-[4px] rounded-full flex items-center justify-center shadow hover:bg-white transition-colors" aria-label="Zoom in">
                  <svg class="w-[7.4px] h-[12px]" viewBox="0 0 8 12" fill="currentColor">
                    <path d="M4 2V0M6 4H4M4 8C4 8 2 10 2 12H6C6 10 4 8 4 8Z" stroke="black" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </button>
                <button class="w-10 h-10 bg-white/80 backdrop-blur-[4px] rounded-full flex items-center justify-center shadow hover:bg-white transition-colors" aria-label="Zoom out">
                  <svg class="w-[7.4px] h-[12px]" viewBox="0 0 8 12" fill="currentColor">
                    <path d="M2 6H6M4 2C4 2 6 4 6 6C6 8 4 10 4 10" stroke="black" stroke-width="1.5" stroke-linecap="round"/>
                  </svg>
                </button>
              </div>
            </div>
          </div>

          <!-- Right: Product Info -->
          <div class="flex-1 flex flex-col gap-8 max-w-[452px]">
            <!-- Header -->
            <div class="flex flex-col gap-[15px]">
              <div class="flex items-center gap-3">
                <div class="w-[16.5px] h-[11.25px] bg-[#4C4546]" />
                <span class="font-geist text-base text-[#4C4546]">{{ product.viewCount }} lượt xem</span>
              </div>

              <h1 class="font-geist text-[32px] font-semibold leading-[38px] tracking-[-0.32px] text-black">
                {{ product.name }}
              </h1>

              <div class="flex items-center gap-4">
                <span class="font-inter text-[32px] font-semibold leading-[38px] text-black">{{ formatPrice(product.price) }}</span>
                <button class="w-[23.33px] h-[21.41px] bg-black flex items-center justify-center rounded-full" aria-label="Add to wishlist">
                  <svg class="w-3 h-2" viewBox="0 0 12 10" fill="white">
                    <path d="M6 1C4.5 4 2 6.5 2 9C2 10.5 3 11.5 4.5 11.5C5 11.5 5.5 11.4 6 11.3C6.5 11.4 7 11.5 7.5 11.5C9 11.5 10 10.5 10 9C10 6.5 7.5 4 6 1Z"/>
                  </svg>
                </button>
              </div>

              <hr class="border-[#CFC4C5]" />

              <p class="font-gelasio text-base text-[#4C4546] leading-[26px]">
                {{ product.description }}
              </p>
            </div>

            <!-- Size Selector -->
            <div class="flex flex-col gap-4">
              <div class="flex items-center gap-2">
                <span class="font-geist text-[12px] font-bold uppercase tracking-[1.2px] text-black">
                  CHỌN KÍCH Cỡ:
                </span>
              </div>

              <div class="flex gap-2">
                <button
                  v-for="size in product.sizes"
                  :key="size.id"
                  @click="selectedSize = size.size"
                  :class="[
                    'w-[81px] h-[48px] border flex items-center justify-center font-geist text-base text-[#1A1C1C] transition-colors',
                    selectedSize === size.size
                      ? 'border-black bg-black text-white hover:bg-gray-900'
                      : 'border-[#7E7576] hover:border-black hover:bg-black hover:text-white'
                  ]"
                >
                  {{ size.size }}
                </button>
              </div>
              <p v-if="!selectedSize" class="text-red-500 text-sm">Vui lòng chọn kích cỡ</p>
            </div>

            <!-- Quantity Selector -->
            <div class="flex flex-col gap-4">
              <div class="flex items-center gap-2">
                <span class="font-geist text-[12px] font-bold uppercase tracking-[1.2px] text-black">
                  SỐ LƯỢNG:
                </span>
              </div>
              <div class="flex items-center gap-4">
                <div class="flex items-center border border-[rgba(207,196,197,0.3)] w-[98px] h-[34px]">
                  <button
                    @click="quantity > 1 && (quantity--)"
                    class="w-8 h-8 flex items-center justify-center border-r border-[rgba(207,196,197,0.3)] hover:bg-gray-50 transition-colors"
                  >
                    <span class="text-[#5E5F5C] font-inter text-sm" style="font-weight: 600; letter-spacing: 0.7px; text-transform: uppercase;">-</span>
                  </button>
                  <span class="flex-1 text-center font-inter text-base text-[#1A1C1C]">{{ quantity }}</span>
                  <button
                    @click="quantity++"
                    class="w-8 h-8 flex items-center justify-center border-l border-[rgba(207,196,197,0.3)] hover:bg-gray-50 transition-colors"
                  >
                    <span class="text-[#5E5F5C] font-inter text-sm" style="font-weight: 600; letter-spacing: 0.7px; text-transform: uppercase;">+</span>
                  </button>
                </div>
              </div>
            </div>

            <!-- Action Buttons -->
            <div class="flex flex-col gap-3">
              <button
                @click="handleAddToCart"
                class="w-full h-[56px] bg-white border-2 border-black text-black font-geist text-base flex items-center justify-center hover:bg-black hover:text-white transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="isAdding || !selectedSize"
              >
                <span v-if="!isAdding">THÊM VÀO GIỎ HÀNG</span>
                <span v-else class="flex items-center gap-2">
                  <svg class="animate-spin w-5 h-5" viewBox="0 0 24 24" fill="none">
                    <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                    <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                  </svg>
                  Đang thêm...
                </span>
              </button>
              <button
                @click="handleBuyNow"
                class="w-full h-[56px] bg-black text-white font-geist text-base flex items-center justify-center hover:bg-gray-900 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                :disabled="!selectedSize"
              >
                MUA NGAY
              </button>
            </div>

            <!-- Trust Badges -->
            <div class="w-full bg-[#F3F3F4] p-4 flex justify-between items-center">
              <!-- 30-day returns -->
              <div class="flex flex-col items-center gap-3 flex-1">
                <div class="w-[18px] h-[18px] bg-black" />
                <span class="font-geist text-[10px] leading-[12px] text-center text-[#1A1C1C]">
                  ĐỔI TRẢ<br>TRONG 30 NGÀY
                </span>
              </div>

              <!-- Warranty -->
              <div class="flex flex-col items-center gap-3 flex-1">
                <div class="w-[16px] h-[20px] bg-black" />
                <span class="font-geist text-[10px] leading-[12px] text-center text-[#1A1C1C]">
                  BẢO HÀNH<br>CHÍNH HÃNG
                </span>
              </div>

              <!-- Free Shipping -->
              <div class="flex flex-col items-center gap-3 flex-1">
                <div class="w-[22px] h-[16px] bg-black" />
                <span class="font-geist text-[10px] leading-[12px] text-center text-[#1A1C1C]">
                  FREESHIP<br>TOÀN QUỐC
                </span>
              </div>
            </div>
          </div>
        </div>
      </section>

      <!-- Loading State -->
      <div v-else-if="loading" class="text-center py-16">
        <p class="font-gelasio text-xl text-[#5E5F5C]">Đang tải sản phẩm...</p>
      </div>

      <!-- Not Found -->
      <div v-else class="text-center py-16">
        <p class="font-gelasio text-xl text-[#5E5F5C] mb-8">Không tìm thấy sản phẩm</p>
        <router-link to="/products" class="text-black hover:underline">Quay lại danh sách sản phẩm</router-link>
      </div>
    </main>

    <!-- Footer -->
    <Footer />
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart.store'
import { useAuthStore } from '@/stores/auth.store'
import Footer from '@/components/layout/Footer.vue'

const route = useRoute()
const router = useRouter()
const cartStore = useCartStore()
const authStore = useAuthStore()

const product = ref<any>(null)
const loading = ref(true)
const selectedSize = ref<string | null>(null)
const quantity = ref(1)
const isAdding = ref(false)
const successMessage = ref('')

// Get product ID from route
const productId = computed(() => route.params.id)

// Mock product data - replace with actual API call
const mockProduct = {
  id: 1,
  name: 'MŨ TRUCKER STADIUM',
  price: 450000,
  imageUrl: 'https://via.placeholder.com/556x556?text=Product+Image',
  images: [
    'https://via.placeholder.com/80x80?text=Thumb1',
    'https://via.placeholder.com/80x80?text=Thumb2',
    'https://via.placeholder.com/80x80?text=Thumb3'
  ],
  description: 'Mũ Trucker Stadium là phụ kiện không thể thiếu, hoàn hảo cho phong cách thư giãn hằng ngày. Thiết kế dáng trucker thời thượng, chiếc mũ lưỡi trai này là sự kết hợp hoàn hảo với tủ đồ thể thao của bạn.',
  viewCount: 2,
  sizes: [
    { id: 1, size: 'S', quantity: 10 },
    { id: 2, size: 'M', quantity: 15 },
    { id: 3, size: 'L', quantity: 20 },
    { id: 4, size: 'XL', quantity: 5 },
    { id: 5, size: 'FreeSize', quantity: 8 }
  ]
}

const productName = computed(() => product.value?.name || 'Sản phẩm')

const formatPrice = (price: number): string => {
  return new Intl.NumberFormat('vi-VN').format(price) + 'đ'
}

const getSelectedVariant = computed(() => {
  if (!product.value || !selectedSize.value) return null
  return product.value.sizes.find((s: any) => s.size === selectedSize.value)
})

const handleAddToCart = async () => {
  if (!selectedSize.value || !product.value) return

  isAdding.value = true
  try {
    const variant = getSelectedVariant.value
    await cartStore.addItem(variant.id, quantity.value, {
      variantId: variant.id,
      productName: product.value.name,
      productImage: product.value.imageUrl,
      size: selectedSize.value,
      price: product.value.price
    }, authStore.isAuthenticated)
    successMessage.value = 'Đã thêm vào giỏ hàng!'
    setTimeout(() => {
      successMessage.value = ''
    }, 3000)
  } catch (error) {
    console.error('Failed to add to cart:', error)
    alert('Không thể thêm vào giỏ hàng. Vui lòng thử lại.')
  } finally {
    isAdding.value = false
  }
}

const handleBuyNow = async () => {
  if (!selectedSize.value || !product.value) return

  // Add to cart first
  isAdding.value = true
  try {
    const variant = getSelectedVariant.value
    await cartStore.addItem(variant.id, quantity.value, {
      variantId: variant.id,
      productName: product.value.name,
      productImage: product.value.imageUrl,
      size: selectedSize.value,
      price: product.value.price
    }, authStore.isAuthenticated)

    // Check if authenticated, if not redirect to login
    if (!authStore.isAuthenticated) {
      authStore.setRedirectPath('/checkout')
      router.push('/login')
    } else {
      router.push('/checkout')
    }
  } catch (error) {
    console.error('Failed to add to cart:', error)
    alert('Không thể thêm vào giỏ hàng. Vui lòng thử lại.')
  } finally {
    isAdding.value = false
  }
}

onMounted(async () => {
  loading.value = true
  try {
    // In real app, fetch from API using productId
    // const productData = await api.getProduct(productId.value)
    // product.value = productData

    // Using mock data for now
    await new Promise(resolve => setTimeout(resolve, 500)) // Simulate API delay
    product.value = mockProduct
  } catch (error) {
    console.error('Failed to fetch product:', error)
  } finally {
    loading.value = false
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
</style>
