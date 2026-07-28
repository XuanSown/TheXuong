<template>
  <div class="min-h-screen bg-[#F9F9F9] flex items-center justify-center py-12 px-4">
    <div class="w-full max-w-[520px]">
      <div class="bg-white border border-[rgba(207,196,197,0.3)] rounded-xl shadow-[0px_25px_50px_-12px_rgba(0,0,0,0.25)] p-[55px_56px_56px]">
        <div class="flex flex-col gap-10">
          <div class="flex flex-col items-center gap-0">
            <div class="w-[82px] h-[75px] bg-[url('@/assets/logo.png')] bg-contain bg-no-repeat bg-center" />
            <div class="mt-1 flex flex-col items-center">
              <p class="font-geist text-base text-[#4C4546] leading-[26px]">
                Tạo tài khoản thành viên mới
              </p>
            </div>
          </div>

          <form
            class="flex flex-col gap-6"
            @submit.prevent="onSubmit"
          >
            <BaseInput
              v-model="fullName"
              type="text"
              placeholder="Họ và tên"
              :error="fullNameError"
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
                  <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                  <circle
                    cx="12"
                    cy="7"
                    r="4"
                  />
                </svg>
              </template>
            </BaseInput>

            <BaseInput
              v-model="email"
              type="email"
              placeholder="Email của bạn"
              :error="emailError"
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
                  /><path d="M22 4L12 13L2 4" />
                </svg>
              </template>
            </BaseInput>

            <BaseInput
              v-model="password"
              type="password"
              placeholder="Mật khẩu"
              :error="passwordError"
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
                    x="3"
                    y="11"
                    width="18"
                    height="11"
                    rx="2"
                  /><path d="M7 11V7a5 5 0 0 1 10 0v4" />
                </svg>
              </template>
            </BaseInput>

            <BaseInput
              v-model="confirmPassword"
              type="password"
              placeholder="Nhập lại mật khẩu"
              :error="confirmPasswordError"
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
                    x="3"
                    y="11"
                    width="18"
                    height="11"
                    rx="2"
                  /><path d="M7 11V7a5 5 0 0 1 10 0v4" />
                </svg>
              </template>
            </BaseInput>

            <BaseButton
              type="submit"
              full-width
              :loading="isSubmitting"
            >
              ĐĂNG KÝ NGAY
            </BaseButton>
          </form>

          <div class="flex justify-center items-center gap-2">
            <span class="font-gelasio text-base text-[#4C4546]">Đã có tài khoản?</span>
            <router-link
              to="/login"
              class="font-gelasio text-base font-bold text-black hover:underline"
            >
              Đăng nhập ngay
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useForm, useField } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import { registerSchema } from '@/utils/validators'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'

const router = useRouter()
const authStore = useAuthStore()

// Validation setup
const { handleSubmit, isSubmitting } = useForm({
  validationSchema: toTypedSchema(registerSchema)
})

const { value: fullName, errorMessage: fullNameError } = useField<string>('fullName')
const { value: email, errorMessage: emailError } = useField<string>('email')
const { value: password, errorMessage: passwordError } = useField<string>('password')
const { value: confirmPassword, errorMessage: confirmPasswordError } = useField<string>('confirmPassword')

const onSubmit = handleSubmit(async (values) => {
  try {
    await authStore.register({
      fullName: values.fullName,
      email: values.email,
      password: values.password,
      confirmPassword: values.confirmPassword
    })
    router.push({ name: 'login', query: { registered: 'success' } })
  } catch (error: any) {
    console.error('Registration failed:', error)
    // Removed alert, handled by global toast
  }
})
</script>

<style scoped>
</style>
