<template>
  <div class="min-h-screen flex">
    <!-- Blank Layout: no Navbar, no Footer (for login/register) -->
    <div v-if="layout === 'blank'" class="w-full">
      <slot />
    </div>

    <!-- Admin Layout: Sidebar only (no Navbar/Footer) -->
    <div v-else-if="layout === 'admin'" class="w-full flex">
      <slot />
    </div>

    <!-- Customer Layout: Navbar + Main + Footer -->
    <div v-else class="flex flex-col w-full">
      <Navbar />
      <main class="flex-1 pt-[120px]">
        <slot />
      </main>
      <Footer />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Navbar from './Navbar.vue'
import Footer from './Footer.vue'

const route = useRoute()

const layout = computed(() => {
  return route.meta.layout as 'admin' | 'customer' | 'blank' || 'customer'
})
</script>
