<template>
  <div>
    <!-- Customer routes: show Navbar + Footer -->
    <template v-if="layout === 'customer'">
      <Navbar />
      <main class="min-h-screen">
        <slot />
      </main>
      <Footer />
    </template>

    <!-- Admin routes: show AdminLayout -->
    <template v-else-if="layout === 'admin'">
      <AdminLayout>
        <slot />
      </AdminLayout>
    </template>

    <!-- Auth routes: minimal layout (no navbar/footer) -->
    <template v-else>
      <main class="min-h-screen">
        <slot />
      </main>
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import Navbar from '@/components/layout/Navbar.vue'
import Footer from '@/components/layout/Footer.vue'
import AdminLayout from '@/components/layout/AdminLayout.vue'

const route = useRoute()

const layout = computed(() => {
  if (route.path.startsWith('/admin')) {
    return 'admin'
  }
  if (['/login', '/register', '/forgot-password'].includes(route.path)) {
    return 'auth'
  }
  return 'customer'
})
</script>
