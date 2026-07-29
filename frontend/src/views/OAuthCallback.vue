<template>
  <div class="min-h-screen bg-[#F9F9F9] flex items-center justify-center">
    <div class="text-center">
      <div class="w-16 h-16 border-4 border-black border-t-transparent rounded-full animate-spin mx-auto mb-6" />
      <h1 class="font-geist text-2xl text-black mb-2">
        ĐANG HOÀN TẤT ĐĂNG NHẬP
      </h1>
      <p class="font-gelasio text-[#5E5F5C]">
        Vui lòng chờ một chút...
      </p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'

const router = useRouter()
const authStore = useAuthStore()

onMounted(async () => {
  try {
    // Fetch the current user to establish auth state
    await authStore.fetchUser()

    // Get the stored redirect target from sessionStorage
    const redirectTarget = sessionStorage.getItem('oauth_redirect_target')
    sessionStorage.removeItem('oauth_redirect_target')

    // Redirect to the appropriate page
    if (redirectTarget) {
      router.push(redirectTarget)
    } else {
      router.push('/')
    }
  } catch (error) {
    console.error('OAuth callback error:', error)
    // On error, redirect to login page
    router.push('/login?error=oauth')
  }
})
</script>

<style scoped>
@keyframes spin {
  to { transform: rotate(360deg); }
}
.animate-spin {
  animation: spin 1s linear infinite;
}
</style>
