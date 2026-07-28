<template>
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
  </div>
</template>

<script setup lang="ts">
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