<template>
  <div class="min-h-screen bg-[#F9F9F9] flex items-center justify-center py-12 px-4">
    <div class="w-full max-w-[480px]">
      <!-- Reset Password Card -->
      <div class="bg-white border border-[rgba(207,196,197,0.3)] rounded-xl shadow-[0px_25px_50px_-12px_rgba(0,0,0,0.25)] p-12">
        <div class="flex flex-col gap-8">
          <!-- Logo & Brand -->
          <div class="flex flex-col items-center gap-0">
            <div class="w-[82px] h-[75px] bg-[url('@/assets/logo.png')] bg-contain bg-no-repeat bg-center" />
            <div class="mt-1 flex flex-col items-center">
              <p class="font-geist text-base text-[#4C4546] leading-[26px]">
                {{ t('auth.resetPasswordTitle') }}
              </p>
            </div>
          </div>

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

          <!-- Reset Password Form -->
          <form
            v-if="!successMsg"
            class="flex flex-col gap-5"
            @submit.prevent="onSubmit"
          >
            <BaseInput
              v-model="password"
              type="password"
              :placeholder="t('auth.newPasswordPlaceholder')"
              :error="passwordError"
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
                    x="3"
                    y="11"
                    width="18"
                    height="11"
                    rx="2"
                  />
                  <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                </svg>
              </template>
            </BaseInput>

            <BaseInput
              v-model="confirmPassword"
              type="password"
              :placeholder="t('auth.confirmNewPasswordPlaceholder')"
              :error="confirmPasswordError"
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
                    x="3"
                    y="11"
                    width="18"
                    height="11"
                    rx="2"
                  />
                  <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                </svg>
              </template>
            </BaseInput>

            <!-- Submit Button -->
            <BaseButton
              type="submit"
              :loading="isSubmitting"
              class="w-full !h-[56px]"
            >
              {{ t('auth.updatePassword') }}
            </BaseButton>
          </form>

          <!-- Back to Forgot Password Link -->
          <div class="flex justify-center">
            <router-link
              to="/forgot-password"
              class="font-gelasio text-base text-[#7E7576] hover:text-black transition-colors"
            >
              ← {{ t('auth.requestNewLink') }}
            </router-link>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import authService from '@/services/auth.service'
import { useForm, useField } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import { resetPasswordSchema } from '@/utils/validators'
import { getApiErrorMessage } from '@/utils/apiError'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const route = useRoute()
const router = useRouter()

const token = ref('')
const successMsg = ref('')
const errorMsg = ref('')

const { handleSubmit, isSubmitting } = useForm({
  validationSchema: toTypedSchema(resetPasswordSchema)
})

const { value: password, errorMessage: passwordError } = useField<string>('password')
const { value: confirmPassword, errorMessage: confirmPasswordError } = useField<string>('confirmPassword')

onMounted(() => {
  if (route.query.token) {
    token.value = route.query.token as string
  } else {
    errorMsg.value = t('auth.invalidToken')
  }
})

const onSubmit = handleSubmit(async (values) => {
  if (!token.value) {
    errorMsg.value = t('auth.missingToken')
    return
  }

  successMsg.value = ''
  errorMsg.value = ''
  
  try {
    const res = await authService.resetPassword({
      token: token.value,
      password: values.password,
      confirmPassword: values.confirmPassword
    })
    successMsg.value = res.message || t('auth.passwordResetSuccess')
    setTimeout(() => {
      router.push('/login')
    }, 2000)
  } catch (error: any) {
    errorMsg.value = getApiErrorMessage(error, 'errors.generic')
  }
})
</script>

<style scoped>
</style>
