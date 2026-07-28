<template>
<<<<<<< HEAD
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
=======
  <div :class="['skeleton', typeClass, animationClass]" />
</template>

<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps({
  type: {
    type: String,
    default: 'text', // text, title, avatar, image, card
  },
  animation: {
    type: String,
    default: 'pulse', // pulse, wave
  }
})

const typeClass = computed(() => {
  switch (props.type) {
    case 'text': return 'h-4 w-full rounded'
    case 'title': return 'h-8 w-3/4 rounded'
    case 'avatar': return 'h-12 w-12 rounded-full'
    case 'image': return 'w-full aspect-square rounded-sm'
    case 'card': return 'w-full h-full'
    default: return 'h-4 w-full rounded'
  }
})

const animationClass = computed(() => {
  return props.animation === 'pulse' ? 'animate-pulse bg-gray-200' : 'animate-pulse bg-gray-200'
})
</script>

<style scoped>
/* Wave animation could be added here if needed, sticking to Tailwind's default pulse for simplicity */
</style>
>>>>>>> fb265d4 (restore: code lost to ponytail full (2026-07-27) from stash@{0})
