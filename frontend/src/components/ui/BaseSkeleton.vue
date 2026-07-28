<template>
  <div :class="['animate-pulse rounded bg-gray-200', sizeMap[type ?? 'text']]" />
</template>

<script setup lang="ts">
defineProps<{ type?: 'image' | 'text' | 'title' }>()

const sizeMap: Record<'image' | 'text' | 'title', string> = {
  image: 'aspect-square w-full',
  text: 'h-3 w-full',
  title: 'h-5 w-full'
}
</script>