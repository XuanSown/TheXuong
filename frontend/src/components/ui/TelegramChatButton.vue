<template>
  <Teleport to="body">
    <Transition name="chat-fade">
      <div
        v-show="visible && hasPosition"
        class="fixed z-[9999] select-none touch-none snap-transition"
        :class="{ 'no-transition': isDragging }"
        :style="{ left: position.x + 'px', top: position.y + 'px' }"
        @mousedown="startDrag"
        @touchstart.passive="startDrag"
      >
        <!-- Chat Button (Draggable) -->
        <div
          class="cursor-pointer flex items-center justify-center w-[60px] h-[60px] rounded-full bg-[#0088cc] hover:bg-[#0077b5] shadow-lg hover:shadow-xl transition-colors duration-200 group relative"
          title="Chat với tư vấn viên"
          @click="handleClick"
        >
          <!-- Telegram SVG Icon -->
          <svg
            class="w-8 h-8 text-white group-hover:scale-110 transition-transform"
            viewBox="0 0 24 24"
            fill="currentColor"
          >
            <path d="M11.944 0A12 12 0 0 0 0 12a12 12 0 0 0 12 12 12 12 0 0 0 12-12A12 12 0 0 0 12 0a12 12 0 0 0-.056 0zm4.962 7.224c.1-.002.321.023.465.14a.506.506 0 0 1 .171.325c.016.093.036.306.02.472-.18 1.898-.962 6.502-1.36 8.627-.168.9-.499 1.201-.82 1.23-.696.065-1.225-.46-1.9-.902-1.056-.693-1.653-1.124-2.678-1.8-1.185-.78-.417-1.21.258-1.91.177-.184 3.247-2.977 3.307-3.23.007-.032.014-.15-.056-.212s-.174-.041-.249-.024c-.106.024-1.793 1.14-5.061 3.345-.479.33-.913.49-1.302.48-.428-.008-1.252-.241-1.865-.44-.752-.245-1.349-.374-1.297-.789.027-.216.325-.437.893-.663 3.498-1.524 5.83-2.529 6.998-3.014 3.332-1.386 4.025-1.627 4.476-1.635z" />
          </svg>
        </div>

        <!-- Close / Hide Button -->
        <button
          class="absolute -top-1 -right-1 w-5 h-5 bg-gray-500 hover:bg-gray-600 text-white rounded-full flex items-center justify-center text-xs leading-none shadow transition-colors z-10"
          aria-label="Ẩn nút chat"
          title="Ẩn"
          @click.stop="hide"
        >
          ×
        </button>

        <!-- Tooltip -->
        <div class="absolute bottom-full left-1/2 -translate-x-1/2 mb-2 px-3 py-1.5 bg-gray-800 text-white text-xs rounded-lg whitespace-nowrap opacity-0 group-hover:opacity-100 transition-opacity pointer-events-none shadow-lg">
          Chat tư vấn
          <div class="absolute top-full left-1/2 -translate-x-1/2 -mt-1 border-4 border-transparent border-t-gray-800" />
        </div>
      </div>
    </Transition>

    <!-- Show Button (when hidden) -->
    <Transition name="chat-fade">
      <button
        v-show="!visible && hasPosition"
        class="fixed z-[9999] flex items-center justify-center w-10 h-10 rounded-full bg-[#0088cc] hover:bg-[#0077b5] shadow-md hover:shadow-lg transition-colors duration-200 snap-transition"
        :style="{ left: (position.x + 10) + 'px', top: (position.y + 10) + 'px' }"
        aria-label="Hiện nút chat Telegram"
        title="Chat với tư vấn viên"
        @click="show"
      >
        <svg
          class="w-5 h-5 text-white"
          viewBox="0 0 24 24"
          fill="currentColor"
        >
          <path d="M11.944 0A12 12 0 0 0 0 12a12 12 0 0 0 12 12 12 12 0 0 0 12-12A12 12 0 0 0 12 0a12 12 0 0 0-.056 0zm4.962 7.224c.1-.002.321.023.465.14a.506.506 0 0 1 .171.325c.016.093.036.306.02.472-.18 1.898-.962 6.502-1.36 8.627-.168.9-.499 1.201-.82 1.23-.696.065-1.225-.46-1.9-.902-1.056-.693-1.653-1.124-2.678-1.8-1.185-.78-.417-1.21.258-1.91.177-.184 3.247-2.977 3.307-3.23.007-.032.014-.15-.056-.212s-.174-.041-.249-.024c-.106.024-1.793 1.14-5.061 3.345-.479.33-.913.49-1.302.48-.428-.008-1.252-.241-1.865-.44-.752-.245-1.349-.374-1.297-.789.027-.216.325-.437.893-.663 3.498-1.524 5.83-2.529 6.998-3.014 3.332-1.386 4.025-1.627 4.476-1.635z" />
        </svg>
      </button>
    </Transition>
  </Teleport>
</template>

<script setup lang="ts">
import { ref, onMounted, onBeforeUnmount } from 'vue'

const STORAGE_KEY = 'telegram-chat-btn-state'

const visible = ref(true)
const hasPosition = ref(false)
const position = ref({ x: 0, y: 0 })

const isDragging = ref(false)
let dragStartPos = { x: 0, y: 0 }
let dragOffset = { x: 0, y: 0 }
let hasMoved = false

onMounted(() => {
  const saved = localStorage.getItem(STORAGE_KEY)
  if (saved) {
    try {
      const state = JSON.parse(saved)
      visible.value = state.visible ?? true
      if (state.position) {
        position.value = {
          x: Math.min(Math.max(20, state.position.x), window.innerWidth - 80),
          y: Math.min(Math.max(20, state.position.y), window.innerHeight - 80),
        }
      } else {
        position.value = { x: window.innerWidth - 80, y: window.innerHeight - 80 }
      }
    } catch {
      position.value = { x: window.innerWidth - 80, y: window.innerHeight - 80 }
    }
  } else {
    position.value = { x: window.innerWidth - 80, y: window.innerHeight - 80 }
  }
  
  hasPosition.value = true
  window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', handleResize)
})

function handleResize() {
  if (!isDragging.value && hasPosition.value) {
    snapToEdge()
  }
}

function saveState() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify({ 
    visible: visible.value,
    position: position.value
  }))
}

function hide() {
  visible.value = false
  saveState()
}

function show() {
  visible.value = true
  saveState()
}

function handleClick() {
  if (!hasMoved) {
    window.open('https://t.me/thexuongsport_n8nchatbot', '_blank')
  }
}

function startDrag(e: MouseEvent | TouchEvent) {
  if ((e.target as HTMLElement).closest('button')) return;
  
  isDragging.value = true
  hasMoved = false
  
  const clientX = e instanceof MouseEvent ? e.clientX : e.touches[0].clientX
  const clientY = e instanceof MouseEvent ? e.clientY : e.touches[0].clientY
  
  dragStartPos = { x: clientX, y: clientY }
  dragOffset = {
    x: clientX - position.value.x,
    y: clientY - position.value.y,
  }

  document.addEventListener('mousemove', onMove, { passive: false })
  document.addEventListener('touchmove', onMove, { passive: false })
  document.addEventListener('mouseup', onEnd)
  document.addEventListener('touchend', onEnd)
}

function onMove(e: MouseEvent | TouchEvent) {
  if (!isDragging.value) return
  
  const clientX = e instanceof MouseEvent ? e.clientX : e.touches[0].clientX
  const clientY = e instanceof MouseEvent ? e.clientY : e.touches[0].clientY
  
  if (Math.abs(clientX - dragStartPos.x) > 5 || Math.abs(clientY - dragStartPos.y) > 5) {
    hasMoved = true
    if (e.cancelable) e.preventDefault() // prevent scroll on touch devices
  }
  
  position.value = {
    x: clientX - dragOffset.x,
    y: clientY - dragOffset.y,
  }
}

function onEnd() {
  if (isDragging.value) {
    isDragging.value = false
    snapToEdge()
  }
  
  document.removeEventListener('mousemove', onMove)
  document.removeEventListener('touchmove', onMove)
  document.removeEventListener('mouseup', onEnd)
  document.removeEventListener('touchend', onEnd)
}

function snapToEdge() {
  const midX = window.innerWidth / 2
  const buttonCenter = position.value.x + 30
  
  let targetX = 20
  if (buttonCenter > midX) {
    targetX = window.innerWidth - 80
  }
  
  let targetY = Math.max(20, Math.min(position.value.y, window.innerHeight - 80))
  
  position.value = { x: targetX, y: targetY }
  saveState()
}
</script>

<style scoped>
.chat-fade-enter-active,
.chat-fade-leave-active {
  transition: opacity 0.3s ease, transform 0.3s ease;
  transform-origin: center;
}
.chat-fade-enter-from,
.chat-fade-leave-to {
  opacity: 0;
  transform: scale(0.8);
}

.snap-transition {
  transition: left 0.3s cubic-bezier(0.2, 0.8, 0.2, 1), top 0.3s cubic-bezier(0.2, 0.8, 0.2, 1);
}
.no-transition {
  transition: none !important;
}
</style>
