<template>
  <div id="app">
    <LayoutProvider>
      <router-view />
    </LayoutProvider>
  </div>
</template>

<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import { useCartStore } from '@/stores/cart.store'
import LayoutProvider from '@/components/layout/LayoutProvider.vue'

const authStore = useAuthStore()
const cartStore = useCartStore()

onMounted(async () => {
  // Fetch user info if token exists
  await authStore.fetchUser().catch(() => {
    // Silently fail if no valid token
  })

  // Fetch cart if user is authenticated
  if (authStore.isAuthenticated) {
    await cartStore.fetchCart().catch(console.error)
  }
})

// Watch for login to merge guest cart
watch(
  () => authStore.isAuthenticated,
  async (newVal, oldVal) => {
    if (newVal && !oldVal) {
      // User just logged in - merge guest cart
      await cartStore.mergeGuestCart()
    }
  }
)
</script>
