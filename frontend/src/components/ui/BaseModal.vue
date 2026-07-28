<template>
  <Teleport to="body">
    <div v-if="modelValue" class="fixed inset-0 z-50 flex items-center justify-center p-4">
      <div class="absolute inset-0 bg-black/50" @click="close" />
      <div class="relative flex max-h-[90vh] w-full max-w-lg flex-col overflow-auto rounded-xl bg-white p-6 shadow-xl">
        <div class="mb-4 flex items-center justify-between">
          <h3 v-if="title" class="text-lg font-semibold">{{ title }}</h3>
          <span v-else />
          <button type="button" aria-label="Close" class="text-gray-500 hover:text-black" @click="close">×</button>
        </div>
        <slot />
      </div>
    </div>
  </Teleport>
</template>

<script setup lang="ts">
import { watch, onBeforeUnmount } from 'vue'

const props = defineProps<{ modelValue: boolean; title?: string }>()
const emit = defineEmits<{ 'update:modelValue': [value: boolean] }>()

const close = () => emit('update:modelValue', false)
const onKey = (e: KeyboardEvent) => { if (e.key === 'Escape') close() }

watch(() => props.modelValue, (v) => {
  if (v) window.addEventListener('keydown', onKey)
  else window.removeEventListener('keydown', onKey)
})
onBeforeUnmount(() => window.removeEventListener('keydown', onKey))
</script>