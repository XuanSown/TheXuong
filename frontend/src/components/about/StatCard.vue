<template>
  <div
    ref="elRef"
    class="flex flex-col items-start gap-2"
  >
    <span
      class="font-geist text-[56px] sm:text-[64px] leading-[64px] font-normal tracking-[-1.28px] text-current"
    >{{ display }}{{ suffix }}</span>
    <span
      class="font-geist text-[12px] uppercase tracking-[1.8px] text-current opacity-60"
    >{{ label }}</span>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import { useCountUp } from '@/composables/useCountUp'

const props = withDefaults(defineProps<{
  target: number
  label: string
  suffix?: string
  duration?: number
}>(), {
  suffix: '',
  duration: 2000,
})

const elRef = ref<HTMLElement | null>(null)
const { current } = useCountUp(elRef, { target: props.target, duration: props.duration })
const display = computed(() => Math.round(current.value))
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Geist:wght@300;400;500;600;700&display=swap') layer(fonts);

.font-geist {
  font-family: 'Geist', sans-serif;
}
</style>
