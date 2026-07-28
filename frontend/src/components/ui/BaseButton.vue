<template>
  <button
<<<<<<< HEAD
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
=======
    :class="[
      'base-btn',
      `variant-${variant}`,
      { 'is-full-width': fullWidth },
      { 'is-loading': loading },
      { 'is-disabled': disabled }
    ]"
    :disabled="disabled || loading"
    :type="type"
    @click="$emit('click', $event)"
  >
    <div
      v-if="loading"
      class="spinner"
    />
    <span
      class="btn-content"
      :class="{ 'opacity-0': loading }"
    >
      <slot name="icon-left" />
      <slot>{{ label }}</slot>
      <slot name="icon-right" />
    </span>
>>>>>>> fb265d4 (restore: code lost to ponytail full (2026-07-27) from stash@{0})
  </button>
</template>

<script setup lang="ts">
<<<<<<< HEAD
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
=======

export interface Props {
  variant?: 'primary' | 'secondary' | 'danger' | 'outline' | 'text'
  type?: 'button' | 'submit' | 'reset'
  label?: string
  fullWidth?: boolean
  loading?: boolean
  disabled?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  variant: 'primary',
  type: 'button',
  fullWidth: false,
  loading: false,
  disabled: false
})

defineEmits(['click'])
</script>

<style scoped>
.base-btn {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  padding: 10px 24px;
  height: 40px;
  border-radius: 0px;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  font-weight: 500;
  line-height: 20px;
  text-transform: uppercase;
  letter-spacing: 0.5px;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  position: relative;
  overflow: hidden;
  box-sizing: border-box;
}

.is-full-width {
  width: 100%;
}

.is-disabled {
  cursor: not-allowed;
  opacity: 0.6;
}

.btn-content {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 100%;
  justify-content: center;
}

.opacity-0 {
  opacity: 0;
}

/* Variants */
.variant-primary {
  background: #000000;
  color: #FFFFFF;
  border: 1px solid #000000;
}
.variant-primary:hover:not(.is-disabled) {
  background: #1a1a1a;
}

.variant-secondary {
  background: #F3F4F6;
  color: #111827;
  border: 1px solid #E5E7EB;
}
.variant-secondary:hover:not(.is-disabled) {
  background: #E5E7EB;
}

.variant-danger {
  background: transparent;
  color: #BA1A1A;
  border: 1px solid rgba(186, 26, 26, 0.4);
}
.variant-danger:hover:not(.is-disabled) {
  background: rgba(186, 26, 26, 0.05);
}

.variant-outline {
  background: transparent;
  color: #1A1C1C;
  border: 1px solid #7E7576;
}
.variant-outline:hover:not(.is-disabled) {
  background: #F9FAFB;
}

.variant-text {
  background: transparent;
  color: #000000;
  border: none;
  padding: 10px 16px;
}
.variant-text:hover:not(.is-disabled) {
  background: #F3F4F6;
}

/* Spinner */
.spinner {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 16px;
  height: 16px;
  border: 2px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top-color: currentColor;
  animation: spin 0.8s linear infinite;
}

@keyframes spin {
  to { transform: translate(-50%, -50%) rotate(360deg); }
}
</style>
>>>>>>> fb265d4 (restore: code lost to ponytail full (2026-07-27) from stash@{0})
