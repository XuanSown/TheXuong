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
                {{ t('auth.welcomeBack') }}
              </p>
            </div>
          </div>

          <!-- Success Alert (shown after logout) -->
          <div
            v-if="showSuccessAlert"
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
              {{ t('auth.logoutSuccess') }}
            </span>
          </div>

          <!-- Register Success Alert -->
          <div
            v-if="showRegisterSuccess"
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
              {{ t('auth.registerSuccessLogin') }}
            </span>
          </div>

          <!-- Locked Account Alert -->
          <div
            v-if="showLockedAlert"
            class="bg-[#FDE8E8] border border-[#F5C6C6] rounded-lg p-4 flex items-center gap-3"
          >
            <svg
              class="w-5 h-5 text-[#9B1C1C] flex-shrink-0"
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
                x1="12"
                y1="8"
                x2="12"
                y2="12"
              />
              <line
                x1="12"
                y1="16"
                x2="12.01"
                y2="16"
              />
            </svg>
            <span class="font-gelasio text-sm font-semibold text-[#9B1C1C]">
              {{ t('auth.accountLocked') }}
            </span>
          </div>

          <!-- Login Form -->
          <form
            class="flex flex-col gap-5"
            @submit.prevent="onSubmit"
          >
            <!-- Email Input -->
            <BaseInput
              v-model="email"
              type="email"
              :placeholder="t('auth.emailPlaceholder')"
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
                  />
                  <path d="M22 4L12 13L2 4" />
                </svg>
              </template>
            </BaseInput>

            <!-- Password Input -->
            <BaseInput
              v-model="password"
              :type="showPassword ? 'text' : 'password'"
              :placeholder="t('auth.passwordPlaceholder')"
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
                  />
                  <path d="M7 11V7a5 5 0 0 1 10 0v4" />
                </svg>
              </template>
              <template #suffix>
                <button
                  type="button"
                  class="flex items-center justify-center text-[#7E7576] hover:text-black transition-colors"
                  :aria-label="t('auth.togglePasswordVisibility')"
                  @click="showPassword = !showPassword"
                >
                  <svg
                    v-if="showPassword"
                    class="w-5 h-5"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24" />
                    <line
                      x1="1"
                      y1="1"
                      x2="23"
                      y2="23"
                    />
                  </svg>
                  <svg
                    v-else
                    class="w-5 h-5"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  >
                    <path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z" />
                    <circle
                      cx="12"
                      cy="12"
                      r="3"
                    />
                  </svg>
                </button>
              </template>
            </BaseInput>

            <!-- Forgot Password Link -->
            <div class="flex justify-end">
              <router-link
                to="/forgot-password"
                class="font-gelasio text-sm font-semibold text-black hover:underline transition-colors"
              >
                {{ t('auth.forgotPassword') }}
              </router-link>
            </div>

            <!-- Submit Button -->
            <BaseButton
              type="submit"
              full-width
              :loading="isSubmitting"
            >
              {{ t('auth.login') }}
            </BaseButton>
          </form>

          <!-- Divider -->
          <div class="relative w-full h-[14.39px] flex items-center">
            <div class="absolute inset-0 border-t border-[#CFC4C5]" />
            <div class="absolute left-1/2 -translate-x-1/2 w-[67.14px] h-[14.39px] bg-white flex items-center justify-center">
              <span class="font-geist text-xs text-[#CFC4C5] leading-[14px]">{{ t('auth.or') }}</span>
            </div>
          </div>

          <!-- Google Login Button -->
          <button
            class="w-full h-[56px] border-2 border-[#CFC4C5] rounded-lg flex items-center justify-center gap-2 hover:border-black hover:bg-gray-50 transition-colors"
            @click="handleGoogleLogin"
          >
            <svg
              class="w-5 h-5"
              viewBox="0 0 24 24"
            >
              <path
                d="M22.56 12.25c0-.78-.07-1.53-.2-2.25H12v4.26h5.92a5.06 5.06 0 0 1-2.2 3.32v2.77h3.57c2.08-1.92 3.28-4.74 3.28-8.1z"
                fill="#4285F4"
              />
              <path
                d="M12 23c2.97 0 5.46-.98 7.28-2.66l-3.57-2.77c-.98.66-2.23 1.06-3.71 1.06-2.86 0-5.29-1.93-6.16-4.53H2.18v2.84C3.99 20.53 7.7 23 12 23z"
                fill="#34A853"
              />
              <path
                d="M5.84 14.09c-.22-.66-.35-1.36-.35-2.09s.13-1.43.35-2.09V7.07H2.18C1.43 8.55 1 10.22 1 12s.43 3.45 1.18 4.93l2.85-2.22.81-.62z"
                fill="#FBBC05"
              />
              <path
                d="M12 5.38c1.62 0 3.06.56 4.21 1.64l3.15-3.15C17.45 2.09 14.97 1 12 1 7.7 1 3.99 3.47 2.18 7.07l3.66 2.84c.87-2.6 3.3-4.53 6.16-4.53z"
                fill="#EA4335"
              />
            </svg>
            <span class="font-gelasio text-base text-[#1A1C1C]">{{ t('auth.loginWithGoogle') }}</span>
          </button>

          <!-- Register Link -->
          <div class="flex justify-center items-center gap-2">
            <span class="font-gelasio text-base text-[#4C4546]">{{ t('auth.noAccount') }}</span>
            <router-link
              to="/register"
              class="font-gelasio text-base font-bold text-black hover:underline transition-colors"
            >
              {{ t('auth.registerNow') }}
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
import { useForm, useField } from 'vee-validate'
import { toTypedSchema } from '@vee-validate/zod'
import { loginSchema } from '@/utils/validators'
import { getApiErrorMessage } from '@/utils/apiError'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import { useToast } from 'vue-toastification'
import { useI18n } from 'vue-i18n'

const { t } = useI18n()

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const toast = useToast()

// Validate redirect URL to prevent open redirect attacks
const isValidRedirect = (path: string): boolean => {
  // Only allow relative paths starting with /
  if (!path || !path.startsWith('/')) return false
  // Reject protocol-relative URLs like //evil.com
  if (path.startsWith('//')) return false
  return true
}

// Validation setup
const { handleSubmit, isSubmitting } = useForm({
validationSchema: toTypedSchema(loginSchema)
})

const { value: email, errorMessage: emailError } = useField<string>('email')
const { value: password, errorMessage: passwordError } = useField<string>('password')

const showPassword = ref(false)
const showSuccessAlert = ref(false)
const showRegisterSuccess = ref(false)
const showLockedAlert = ref(false)

onMounted(() => {
// Show success message if redirected from logout
if (route.query.logout === 'success') {
  showSuccessAlert.value = true
  // Auto-hide after 5 seconds
  setTimeout(() => {
  showSuccessAlert.value = false
  }, 5000)
}

// Show success message if redirected from register
if (route.query.registered === 'success') {
  showRegisterSuccess.value = true
  setTimeout(() => {
  showRegisterSuccess.value = false
  }, 5000)
}

// Show locked account alert if redirected after being kicked
if (route.query.locked === '1') {
  showLockedAlert.value = true
  setTimeout(() => {
    showLockedAlert.value = false
  }, 8000)
}

// Store redirect path if present (validate to prevent open redirect)
const redirectPath = route.query.redirect as string | undefined
if (redirectPath && isValidRedirect(redirectPath)) {
  authStore.setRedirectPath(redirectPath)
}
})

const onSubmit = handleSubmit(async (values) => {
try {
  await authStore.login({
  email: values.email,
  password: values.password
  })

  // Redirect based on role
  const redirectTo = authStore.redirectTo
  authStore.setRedirectPath(null)

  if (redirectTo && isValidRedirect(redirectTo)) {
  router.push(redirectTo)
  } else if (authStore.isAdmin) {
  router.push('/admin')
  } else {
  router.push('/')
  }
} catch (error: any) {
  console.error('Login failed:', error)
  toast.error(getApiErrorMessage(error, 'auth.loginFailed'))
}
})

const handleGoogleLogin = () => {
// Store the redirect target in sessionStorage before OAuth flow
const redirectTarget = authStore.redirectTo || '/cart'
sessionStorage.setItem('oauth_redirect_target', redirectTarget)

// Clear redirect in store
authStore.setRedirectPath(null)

// Redirect to backend OAuth2 endpoint (no extra query params needed)
window.location.href = '/oauth2/authorization/google'
}
</script>

<style scoped>
</style>
