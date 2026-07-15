<template>
  <div id="app">
    <LayoutProvider>
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </LayoutProvider>

    <!-- Global Components -->
    <BackToTop />
    <TelegramChatButton />
  </div>
</template>

<script setup lang="ts">
import { onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import { useCartStore } from '@/stores/cart.store'
import LayoutProvider from '@/components/layout/LayoutProvider.vue'
import BackToTop from '@/components/ui/BackToTop.vue'
import TelegramChatButton from '@/components/ui/TelegramChatButton.vue'

const authStore = useAuthStore()
const cartStore = useCartStore()

onMounted(async () => {
  // Router guard now handles auth initialization before mount.
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

<style>
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>
