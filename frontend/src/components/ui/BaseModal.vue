<template>
  <Teleport to="body">
<<<<<<< HEAD
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
=======
    <Transition name="modal">
      <div
        v-if="modelValue"
        class="modal-mask"
        @click="handleMaskClick"
      >
        <div class="modal-wrapper">
          <div
            class="modal-container"
            @click.stop
          >
            <!-- Header -->
            <div class="modal-header">
              <slot name="header">
                <h3 class="modal-title">
                  {{ title }}
                </h3>
              </slot>
              <button
                class="modal-close"
                @click="close"
              >
                <svg
                  width="24"
                  height="24"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path
                    d="M18 6L6 18M6 6l12 12"
                    stroke-linecap="round"
                    stroke-linejoin="round"
                  />
                </svg>
              </button>
            </div>

            <!-- Body -->
            <div class="modal-body">
              <slot />
            </div>

            <!-- Footer -->
            <div
              v-if="$slots.footer"
              class="modal-footer"
            >
              <slot name="footer" />
            </div>
          </div>
        </div>
      </div>
    </Transition>
>>>>>>> fb265d4 (restore: code lost to ponytail full (2026-07-27) from stash@{0})
  </Teleport>
</template>

<script setup lang="ts">
<<<<<<< HEAD
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
=======
import { watch, onMounted, onUnmounted } from 'vue'

const props = defineProps({
  modelValue: {
    type: Boolean,
    required: true
  },
  title: {
    type: String,
    default: ''
  },
  closeOnMaskClick: {
    type: Boolean,
    default: true
  }
})

const emit = defineEmits(['update:modelValue', 'close'])

const close = () => {
  emit('update:modelValue', false)
  emit('close')
}

const handleMaskClick = () => {
  if (props.closeOnMaskClick) {
    close()
  }
}

// Handle Escape key
const handleKeydown = (e: KeyboardEvent) => {
  if (e.key === 'Escape' && props.modelValue) {
    close()
  }
}

// Lock body scroll when modal is open
watch(() => props.modelValue, (isOpen) => {
  if (isOpen) {
    document.body.style.overflow = 'hidden'
  } else {
    document.body.style.overflow = ''
  }
})

onMounted(() => {
  document.addEventListener('keydown', handleKeydown)
  if (props.modelValue) {
    document.body.style.overflow = 'hidden'
  }
})

onUnmounted(() => {
  document.removeEventListener('keydown', handleKeydown)
  document.body.style.overflow = ''
})
</script>

<style scoped>
.modal-mask {
  position: fixed;
  z-index: 9998;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.5);
  display: flex;
  transition: opacity 0.3s ease;
}

.modal-wrapper {
  margin: auto;
  padding: 20px;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.modal-container {
  width: 100%;
  max-width: 500px;
  background-color: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  max-height: 90vh;
}

.modal-header {
  padding: 20px 24px;
  border-bottom: 1px solid #E5E7EB;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.modal-title {
  margin: 0;
  font-family: 'Geist', sans-serif;
  font-size: 18px;
  font-weight: 600;
  color: #111827;
}

.modal-close {
  background: transparent;
  border: none;
  cursor: pointer;
  color: #6B7280;
  padding: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 4px;
  transition: background-color 0.2s;
}

.modal-close:hover {
  background-color: #F3F4F6;
  color: #111827;
}

.modal-body {
  padding: 24px;
  overflow-y: auto;
  font-family: 'Geist', sans-serif;
}

.modal-footer {
  padding: 16px 24px;
  border-top: 1px solid #E5E7EB;
  display: flex;
  justify-content: flex-end;
  gap: 12px;
}

/* Transitions */
.modal-enter-from {
  opacity: 0;
}

.modal-leave-to {
  opacity: 0;
}

.modal-enter-from .modal-container,
.modal-leave-to .modal-container {
  transform: scale(0.95) translateY(-20px);
}
</style>
>>>>>>> fb265d4 (restore: code lost to ponytail full (2026-07-27) from stash@{0})
