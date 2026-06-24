<template>
  <div class="min-h-screen bg-[#F9F9F9] flex items-center justify-center py-12 px-4">
    <div class="w-full max-w-[480px]">
      <!-- Login Card -->
      <div class="bg-white border border-[rgba(207,196,197,0.3)] rounded-xl shadow-[0px_25px_50px_-12px_rgba(0,0,0,0.25)] p-12">
        <div class="flex flex-col gap-8">
          <!-- Logo & Brand -->
          <div class="flex flex-col items-center gap-0">
            <div class="w-[82px] h-[75px] bg-[url('@/assets/logo.png')] bg-contain bg-no-repeat bg-center" />
            <div class="mt-1 flex flex-col items-center">
              <p class="font-geist text-base text-[#4C4546] leading-[26px]">
                Welcome Back!
              </p>
            </div>
          </div>

          <!-- Success Alert (shown after logout) -->
          <div v-if="showSuccessAlert" class="bg-[#E2F0E7] border border-[#C3E6CB] rounded-lg p-4 flex items-center gap-3">
            <div class="w-[16.67px] h-[16.67px] bg-[#2D5A3F] rounded-full" />
            <span class="font-gelasio text-sm font-semibold text-[#2D5A3F]">
              Đăng xuất thành công!
            </span>
          </div>

          <!-- Login Form -->
          <form @submit.prevent="handleLogin" class="flex flex-col gap-5">
            <!-- Email Input -->
            <div class="relative">
              <div class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-4 bg-[#7E7576]" />
              <input
                v-model="email"
                type="email"
                placeholder="Email của bạn"
                class="w-full h-[59.59px] bg-[#F3F3F3] rounded-lg pl-12 pr-4 font-gelasio text-base text-[rgba(126,117,118,0.6)] placeholder:text-[rgba(126,117,118,0.6)] outline-none focus:ring-2 focus:ring-black transition-all"
                required
              />
            </div>

            <!-- Password Input -->
            <div class="relative">
              <div class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-[21px] bg-[#7E7576]" />
              <input
                v-model="password"
                :type="showPassword ? 'text' : 'password'"
                placeholder="Mật khẩu"
                class="w-full h-[59.59px] bg-[#F3F3F3] rounded-lg pl-12 pr-4 font-gelasio text-base text-[rgba(126,117,118,0.6)] placeholder:text-[rgba(126,117,118,0.6)] outline-none focus:ring-2 focus:ring-black transition-all"
                required
              />
              <button
                type="button"
                @click="showPassword = !showPassword"
                class="absolute right-4 top-1/2 -translate-y-1/2 w-5 h-5 flex items-center justify-center text-[#7E7576] hover:text-black transition-colors"
                aria-label="Toggle password visibility"
              >
                <svg v-if="showPassword" class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                  <line x1="1" y1="1" x2="23" y2="23" />
                </svg>
                <svg v-else class="w-5 h-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                  <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                  <circle cx="12" cy="12" r="3" />
                </svg>
              </button>
            </div>

            <!-- Forgot Password Link -->
            <div class="flex justify-end">
              <router-link to="/forgot-password" class="font-gelasio text-sm font-semibold text-black hover:underline transition-colors">
                Quên mật khẩu?
              </router-link>
            </div>

            <!-- Submit Button -->
            <button
              type="submit"
              :disabled="isLoading"
              class="w-full h-[56px] bg-black text-white font-gelasio text-base flex items-center justify-center rounded-lg hover:bg-gray-900 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="!isLoading">ĐĂNG NHẬP</span>
              <span v-else class="flex items-center gap-2">
                <svg class="animate-spin w-5 h-5" viewBox="0 0 24 24" fill="none">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                </svg>
                Đang đăng nhập...
              </span>
            </button>
          </form>

          <!-- Divider -->
          <div class="relative w-full h-[14.39px] flex items-center">
            <div class="absolute inset-0 border-t border-[#CFC4C5]" />
            <div class="absolute left-1/2 -translate-x-1/2 w-[67.14px] h-[14.39px] bg-white flex items-center justify-center">
              <span class="font-geist text-xs text-[#CFC4C5] leading-[14px]">HOẶC</span>
            </div>
          </div>

          <!-- Google Login Button -->
          <button
            @click="handleGoogleLogin"
            class="w-full h-[56px] border-2 border-[#CFC4C5] rounded-lg flex items-center justify-center gap-2 hover:border-black hover:bg-gray-50 transition-colors"
          >
            <div class="w-[21px] h-[21px] bg-[url('https://upload.wikimedia.org/wikipedia/commons/5/53/Google_%22G%22_Logo.svg')] bg-contain bg-no-repeat bg-center" />
            <span class="font-gelasio text-base text-[#1A1C1C]">Đăng nhập bằng Google</span>
          </button>

          <!-- Register Link -->
          <div class="flex justify-center items-center gap-2">
            <span class="font-gelasio text-base text-[#4C4546]">Chưa có tài khoản?</span>
            <router-link to="/register" class="font-gelasio text-base font-bold text-black hover:underline transition-colors">
              Đăng ký ngay
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const showPassword = ref(false)
const isLoading = ref(false)
const showSuccessAlert = ref(false)

onMounted(() => {
  // Show success message if redirected from logout
  if (route.query.logout === 'success') {
    showSuccessAlert.value = true
    // Auto-hide after 5 seconds
    setTimeout(() => {
      showSuccessAlert.value = false
    }, 5000)
  }
})

const handleLogin = async () => {
  isLoading.value = true
  try {
    await authStore.login({
      email: email.value,
      password: password.value
    })
    router.push('/')
  } catch (error: any) {
    console.error('Login failed:', error)
    alert('Đăng nhập thất bại. Vui lòng kiểm tra email và mật khẩu.')
  } finally {
    isLoading.value = false
  }
}

const handleGoogleLogin = () => {
  // TODO: Implement Google OAuth
  console.log('Google login clicked')
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
</style>
