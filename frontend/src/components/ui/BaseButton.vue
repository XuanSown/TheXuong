<template>
  <button
    :type="type"
    :disabled="disabled || loading"
    :class="[
      'inline-flex items-center justify-center gap-2 rounded-lg font-geist text-sm font-semibold uppercase tracking-[1.2px] transition-colors',
      fullWidth ? 'h-[56px] w-full' : 'h-[48px] px-6',
      variant === 'outline'
        ? 'border border-[#CFC4C5] text-[#1A1C1C] hover:bg-gray-100'
        : 'bg-black text-white hover:bg-gray-900',
      (disabled || loading) ? 'cursor-not-allowed opacity-60' : 'cursor-pointer'
    ]"
    @click="$emit('click', $event)"
  >
    <svg v-if="loading" class="h-4 w-4 animate-spin" viewBox="0 0 24 24" fill="none">
      <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" />
      <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z" />
    </svg>
    <slot v-if="$slots.default" />
    <template v-else>{{ label }}</template>
  </button>
</template>

<script setup lang="ts">
defineProps<{
  type?: 'submit' | 'button'
  variant?: 'primary' | 'outline'
  label?: string
  loading?: boolean
  fullWidth?: boolean
  disabled?: boolean
}>()

defineEmits<{ click: [e: MouseEvent] }>()
</script>