<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth.store'
import { useToast } from 'vue-toastification'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const toast = useToast()

onMounted(async () => {
  const token = route.query.token as string | undefined
  if (!token) {
    toast.error('Đăng nhập Google thất bại')
    router.replace('/login')
    return
  }

  localStorage.setItem('access_token', token)
  try {
    await authStore.fetchUser()
    toast.success('Đăng nhập thành công')
    const redirect = (route.query.redirect as string) || '/'
    router.replace(redirect)
  } catch {
    localStorage.removeItem('access_token')
    router.replace('/login')
  }
})
</script>

<template>
  <div class="flex items-center justify-center min-h-screen">
    <p class="text-gray-500">Đang xử lý đăng nhập...</p>
  </div>
</template>
