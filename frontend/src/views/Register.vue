<template>
  <div class="min-h-screen bg-[#F9F9F9] flex items-center justify-center py-12 px-4">
    <div class="w-full max-w-[520px]">
      <!-- Register Card -->
      <div class="bg-white border border-[rgba(207,196,197,0.3)] rounded-xl shadow-[0px_25px_50px_-12px_rgba(0,0,0,0.25)] p-[55px_56px_56px]">
        <div class="flex flex-col gap-10">
          <!-- Logo & Brand -->
          <div class="flex flex-col items-center gap-0">
            <div class="w-[82px] h-[75px] bg-[url('@/assets/logo.png')] bg-contain bg-no-repeat bg-center" />
            <div class="mt-1 flex flex-col items-center">
              <p class="font-geist text-base text-[#4C4546] leading-[26px]">
                Tạo tài khoản thành viên mới
              </p>
            </div>
          </div>

          <!-- Register Form -->
          <form @submit.prevent="handleRegister" class="flex flex-col gap-6">
            <!-- Email Input -->
            <div class="relative">
              <div class="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-4 bg-[#7E7576]" />
              <input
                v-model="email"
                type="email"
                placeholder="Email của bạn"
                class="w-full h-[60.59px] bg-[#F3F3F3] rounded-lg pl-12 pr-4 font-gelasio text-base text-[rgba(126,117,118,0.6)] placeholder:text-[rgba(126,117,118,0.6)] outline-none focus:ring-2 focus:ring-black transition-all"
                required
              />
            </div>

            <!-- Password Input -->
            <div class="relative">
              <div class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-[21px] bg-[#7E7576]" />
              <input
                v-model="password"
                type="password"
                placeholder="Mật khẩu"
                class="w-full h-[60.59px] bg-[#F3F3F3] rounded-lg pl-12 pr-4 font-gelasio text-base text-[rgba(126,117,118,0.6)] placeholder:text-[rgba(126,117,118,0.6)] outline-none focus:ring-2 focus:ring-black transition-all"
                required
                minlength="6"
              />
            </div>

            <!-- Confirm Password Input -->
            <div class="relative">
              <div class="absolute left-4 top-1/2 -translate-y-1/2 w-4 h-[21px] bg-[#7E7576]" />
              <input
                v-model="confirmPassword"
                type="password"
                placeholder="Nhập lại mật khẩu"
                class="w-full h-[60.59px] bg-[#F3F3F3] rounded-lg pl-12 pr-4 font-gelasio text-base text-[rgba(126,117,118,0.6)] placeholder:text-[rgba(126,117,118,0.6)] outline-none focus:ring-2 focus:ring-black transition-all"
                required
              />
            </div>

            <!-- Submit Button -->
            <button
              type="submit"
              :disabled="isLoading"
              class="w-full h-[56px] bg-black text-white font-gelasio text-base flex items-center justify-center rounded-lg hover:bg-gray-900 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
            >
              <span v-if="!isLoading">ĐĂNG KÝ NGAY</span>
              <span v-else class="flex items-center gap-2">
                <svg class="animate-spin w-5 h-5" viewBox="0 0 24 24" fill="none">
                  <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
                  <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z" />
                </svg>
                Đang đăng ký...
              </span>
            </button>
          </form>

          <!-- Footer Link -->
          <div class="flex justify-center items-center gap-2 relative h-[26px]">
            <span class="font-gelasio text-base text-[#4C4546]">Đã có tài khoản?</span>
            <router-link to="/login" class="font-gelasio text-base font-bold text-black hover:underline transition-colors">
              Đăng nhập ngay
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

const router = useRouter()
const authStore = useAuthStore()

const email = ref('')
const password = ref('')
const confirmPassword = ref('')
const isLoading = ref(false)

const handleRegister = async () => {
  if (password.value !== confirmPassword.value) {
    alert('Mật khẩu nhập lại không khớp!')
    return
  }

  isLoading.value = true
  try {
    await authStore.register({
      username: email.value.split('@')[0],
      email: email.value,
      password: password.value
    })
    router.push('/')
  } catch (error: any) {
    console.error('Registration failed:', error)
    alert('Đăng ký thất bại. Vui lòng thử lại.')
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
</style>
