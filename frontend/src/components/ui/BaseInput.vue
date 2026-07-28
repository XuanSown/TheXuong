<template>
<<<<<<< HEAD
  <div class="flex flex-col gap-2">
    <label v-if="label" class="text-xs uppercase text-[#4C4546]">
      {{ label }}
      <span v-if="required" class="text-red-500">*</span>
    </label>
    <div
      v-bind="$attrs"
      class="flex h-[50px] w-full items-center gap-2 rounded-lg border border-[#CFC4C5] px-3 transition-colors focus-within:border-black"
      :class="{ 'border-red-500 focus-within:border-red-500': error }"
    >
      <span v-if="$slots.prefix" class="flex items-center justify-center text-[#7E7576]">
        <slot name="prefix" />
      </span>
      <input
        :type="type"
        :value="modelValue"
        :placeholder="placeholder"
        :required="required"
        @input="onInput"
        class="w-full bg-transparent font-gelasio text-[16px] outline-none placeholder:text-[#9CA0AF]"
      />
      <span v-if="$slots.suffix" class="flex items-center justify-center text-[#7E7576]">
        <slot name="suffix" />
      </span>
    </div>
    <p v-if="error" class="text-sm text-red-500">{{ error }}</p>
=======
  <div class="base-input-wrapper">
    <label
      v-if="label"
      :for="id"
      class="input-label"
    >
      {{ label }} <span
        v-if="required"
        class="text-danger"
      >*</span>
    </label>
    
    <div class="input-container">
      <div
        v-if="$slots.prefix"
        class="input-prefix"
      >
        <slot name="prefix" />
      </div>
      
      <input
        :id="id"
        :type="type"
        :value="modelValue ?? ''"
        :placeholder="placeholder"
        :disabled="disabled"
        :required="required"
        :class="[
          'base-input',
          { 'has-prefix': $slots.prefix },
          { 'has-suffix': $slots.suffix },
          { 'is-invalid': !!error }
        ]"
        @input="$emit('update:modelValue', ($event.target as HTMLInputElement).value)"
        @blur="$emit('blur', $event)"
        @focus="$emit('focus', $event)"
      >
      
      <div
        v-if="$slots.suffix"
        class="input-suffix"
      >
        <slot name="suffix" />
      </div>
    </div>
    
    <span
      v-if="error"
      class="input-error"
    >{{ error }}</span>
    <span
      v-else-if="hint"
      class="input-hint"
    >{{ hint }}</span>
>>>>>>> fb265d4 (restore: code lost to ponytail full (2026-07-27) from stash@{0})
  </div>
</template>

<script setup lang="ts">
<<<<<<< HEAD
defineOptions({ inheritAttrs: false })

defineProps<{
  modelValue?: string
  type?: 'text' | 'email' | 'tel' | 'password'
  label?: string
  placeholder?: string
  error?: string
  required?: boolean
}>()

const emit = defineEmits<{ 'update:modelValue': [value: string] }>()

const onInput = (e: Event) => emit('update:modelValue', (e.target as HTMLInputElement).value)
</script>
=======
import { useId } from 'vue'

export interface Props {
  modelValue?: string | number | null
  label?: string
  type?: string
  placeholder?: string
  disabled?: boolean
  required?: boolean
  error?: string
  hint?: string
}

const props = withDefaults(defineProps<Props>(), {
  type: 'text',
  disabled: false,
  required: false
})

defineEmits(['update:modelValue', 'blur', 'focus'])

// Generate a unique ID if one isn't provided (requires Vue 3.5+)
// Fallback if useId is not available
const id = useId ? useId() : `input-${Math.random().toString(36).substring(2, 9)}`
</script>

<style scoped>
.base-input-wrapper {
  display: flex;
  flex-direction: column;
  gap: 6px;
  width: 100%;
}

.input-label {
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  font-weight: 500;
  color: #111827;
}

.text-danger {
  color: #BA1A1A;
}

.input-container {
  position: relative;
  display: flex;
  align-items: center;
  width: 100%;
}

.base-input {
  width: 100%;
  height: 40px;
  padding: 10px 12px;
  background: #FFFFFF;
  border: 1px solid #E5E7EB;
  border-radius: 6px;
  font-family: 'Geist', sans-serif;
  font-size: 14px;
  color: #111827;
  transition: all 0.2s ease;
  box-sizing: border-box;
}

.base-input::placeholder {
  color: #9CA3AF;
}

.base-input:focus {
  outline: none;
  border-color: #000000;
  box-shadow: 0 0 0 1px #000000;
}

.base-input:disabled {
  background: #F9FAFB;
  color: #9CA3AF;
  cursor: not-allowed;
}

.base-input.is-invalid {
  border-color: #BA1A1A;
}

.base-input.is-invalid:focus {
  box-shadow: 0 0 0 1px #BA1A1A;
}

.has-prefix {
  padding-left: 40px;
}

.has-suffix {
  padding-right: 40px;
}

.input-prefix, .input-suffix {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #6B7280;
}

.input-prefix {
  left: 12px;
}

.input-suffix {
  right: 12px;
}

.input-error {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #BA1A1A;
  margin-top: 2px;
}

.input-hint {
  font-family: 'Geist', sans-serif;
  font-size: 12px;
  color: #6B7280;
  margin-top: 2px;
}
</style>
>>>>>>> fb265d4 (restore: code lost to ponytail full (2026-07-27) from stash@{0})
