<template>
  <button
    v-show="visible"
    type="button"
    aria-label="Back to top"
    class="fixed bottom-6 right-6 z-50 flex h-11 w-11 items-center justify-center rounded-full bg-black text-white shadow-lg transition-colors hover:bg-gray-900"
    @click="scrollTop"
  >
    <svg class="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
      <path d="M18 15l-6-6-6 6" />
    </svg>
  </button>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'

const visible = ref(false)
const onScroll = () => { visible.value = window.scrollY > 300 }
const scrollTop = () => window.scrollTo({ top: 0, behavior: 'smooth' })

onMounted(() => window.addEventListener('scroll', onScroll, { passive: true }))
onBeforeUnmount(() => window.removeEventListener('scroll', onScroll))
</script>