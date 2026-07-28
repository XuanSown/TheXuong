<template>
  <div class="min-h-screen bg-[#F9F9F9] flex items-center justify-center py-12 px-4">
    <div class="w-full max-w-[480px]">
      <!-- Forgot Password Card -->
      <div class="bg-white border border-[rgba(207,196,197,0.3)] rounded-xl shadow-[0px_25px_50px_-12px_rgba(0,0,0,0.25)] p-12">
        <div class="flex flex-col gap-8">
          <!-- Logo & Brand -->
          <div class="flex flex-col items-center gap-0">
            <div class="w-[82px] h-[75px] bg-[url('@/assets/logo.png')] bg-contain bg-no-repeat bg-center" />
            <div class="mt-1 flex flex-col items-center">
              <p class="font-geist text-base text-[#4C4546] leading-[26px]">
                Quên mật khẩu
              </p>
            </div>
          </div>

          <!-- Info Text -->
          <p class="font-gelasio text-sm text-[#7E7576] leading-relaxed text-center">
            Nhập email đã đăng ký để nhận link đặt lại mật khẩu. Link có hiệu lực trong <strong class="text-black">2 giờ</strong>.
          </p>

          <!-- Success Message -->
          <div
            v-if="successMsg"
            class="bg-[#E2F0E7] border border-[#C3E6CB] rounded-lg p-4 flex items-center gap-3"
          >
            <svg
              class="w-5 h-5 text-[#2D5A3F] flex-shrink-0"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <path d="M22 11.08V12a10 10 0 1 1-5.93-9.14" />
              <polyline points="22 4 12 14.01 9 11.01" />
            </svg>
            <span class="font-gelasio text-sm font-semibold text-[#2D5A3F]">
              {{ successMsg }}
            </span>
          </div>

          <!-- Error Message -->
          <div
            v-if="errorMsg"
            class="bg-[#FDE8E8] border border-[#F5C6CB] rounded-lg p-4 flex items-center gap-3"
          >
            <svg
              class="w-5 h-5 text-[#721C24] flex-shrink-0"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <circle
                cx="12"
                cy="12"
                r="10"
              />
              <line
                x1="15"
                y1="9"
                x2="9"
                y2="15"
              />
              <line
                x1="9"
                y1="9"
                x2="15"
                y2="15"
              />
            </svg>
            <span class="font-gelasio text-sm font-semibold text-[#721C24]">
              {{ errorMsg }}
            </span>
          </div>

          <!-- Forgot Password Form -->
          <form
            v-if="!successMsg"
            class="flex flex-col gap-5"
            @submit.prevent="onSubmit"
          >
            <!-- Email Input -->
            <BaseInput
              v-model="email"
              type="email"
              placeholder="Nhập email của bạn"
              :error="emailError"
              class="!h-[59.59px]"
            >
              <template #prefix>
                <svg
                  class="w-5 h-5 text-[#7E7576]"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                  stroke-linecap="round"
                  stroke-linejoin="round"
                >
                  <rect
                    x="2"
                    y="4"
                    width="20"
                    height="16"
                    rx="2"
                  />
                  <path d="M22 4L12 13L2 4" />
                </svg>
              </template>
            </BaseInput>

            <!-- Submit Button -->
            <BaseButton
              type="submit"
              :loading="isSubmitting"
              class="w-full !h-[56px]"
            >
              Gửi link đặt lại mật khẩu
            </BaseButton>
          </form>

          <!-- Back to Login Link -->
          <div class="flex justify-center">
            <router-link
              to="/login"
              class="font-gelasio text-base text-[#7E7576] hover:text-black transition-colors"
            >
              ← Quay lại đăng nhập
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import authService from '@/services/auth.service'
import { useForm, useField } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import { forgotPasswordSchema } from '@/utils/validators'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'

const successMsg = ref('')
const errorMsg = ref('')

const { handleSubmit, isSubmitting } = useForm({
  validationSchema: toTypedSchema(forgotPasswordSchema)
})

const { value: email, errorMessage: emailError } = useField<string>('email')

const onSubmit = handleSubmit(async (values) => {
  successMsg.value = ''
  errorMsg.value = ''
  try {
    const res = await authService.forgotPassword(values.email)
    successMsg.value = res.message || 'Đã gửi link đặt lại mật khẩu vào email của bạn!'
  } catch (error: any) {
    errorMsg.value = error.response?.data?.message || 'Có lỗi xảy ra, vui lòng thử lại'
  }
})
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
