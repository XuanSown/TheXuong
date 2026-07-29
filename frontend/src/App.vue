<template>
  <div id="app">
    <AppLoader
      v-if="!appReady"
      @exited="appReady = true"
    />

    <LayoutProvider>
      <router-view v-slot="{ Component }">
        <transition
          name="fade"
          mode="out-in"
        >
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
import { ref, onMounted, watch } from 'vue'
import { useAuthStore } from '@/stores/auth.store'
import { useCartStore } from '@/stores/cart.store'
import LayoutProvider from '@/components/layout/LayoutProvider.vue'
import BackToTop from '@/components/ui/BackToTop.vue'
import TelegramChatButton from '@/components/ui/TelegramChatButton.vue'
import AppLoader from '@/components/AppLoader.vue'

const authStore = useAuthStore()
const cartStore = useCartStore()
const appReady = ref(false)

onMounted(async () => {
  if (authStore.isAuthenticated) {
    await cartStore.fetchCart().catch(console.error)
  }
})

watch(
  () => authStore.isAuthenticated,
  async (newVal, oldVal) => {
    if (newVal && !oldVal) {
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
