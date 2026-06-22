<template>
  <div id="app">
    <LayoutProvider>
      <router-view />
    </LayoutProvider>
  </div>
</template>

<script setup lang="ts">
import LayoutProvider from '@/components/layout/LayoutProvider.vue'
</script>
