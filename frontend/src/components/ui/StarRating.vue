<template>
  <div
    class="relative inline-block shrink-0"
    :class="sizeClass"
    role="img"
    :aria-label="`${modelValue} / 5`"
  >
    <div class="flex h-full w-full">
      <svg
        v-for="i in 5"
        :key="i"
        viewBox="0 0 24 24"
        class="h-full w-full text-[#E5E7EB]"
        fill="currentColor"
      >
        <path :d="STAR_PATH" />
      </svg>
    </div>
    <div
      class="absolute inset-y-0 left-0 overflow-hidden"
      :style="{ width: fillPercent }"
    >
      <div
        class="flex h-full"
        :class="sizeClass"
      >
        <svg
          v-for="i in 5"
          :key="i"
          viewBox="0 0 24 24"
          class="h-full w-full shrink-0 text-black"
          fill="currentColor"
        >
          <path :d="STAR_PATH" />
        </svg>
      </div>
    </div>
    <template v-if="interactive">
      <button
        v-for="i in 5"
        :key="'hit-' + i"
        type="button"
        class="absolute inset-y-0 cursor-pointer bg-transparent"
        :style="{ left: ((i - 1) * 20) + '%', width: '20%' }"
        :aria-label="`${i} ${t('review.star')}`"
        @mouseenter="hover = i"
        @mouseleave="hover = 0"
        @click="emit('update:modelValue', i)"
      />
    </template>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from 'vue'
import { useI18n } from 'vue-i18n'

const STAR_PATH =
  'M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.285 5.385a.562.562 0 01-.84.61l-4.725-2.885a.563.563 0 00-.586 0L6.982 20.54a.562.562 0 01-.84-.61l1.285-5.386a.562.562 0 00-.182-.557l-4.204-3.602a.563.563 0 01.321-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z'

const { t } = useI18n()

const props = withDefaults(
  defineProps<{
    modelValue?: number
    interactive?: boolean
    size?: 'sm' | 'md'
  }>(),
  { modelValue: 0, interactive: false, size: 'md' }
)

const emit = defineEmits<{ (e: 'update:modelValue', value: number): void }>()

const hover = ref(0)
const shown = computed(() =>
  props.interactive && hover.value > 0 ? hover.value : props.modelValue
)
const fillPercent = computed(
  () => `${(Math.min(Math.max(shown.value, 0), 5) / 5) * 100}%`
)
const sizeClass = computed(() =>
  props.size === 'sm' ? 'h-4 w-[80px]' : 'h-6 w-[120px]'
)
</script>
