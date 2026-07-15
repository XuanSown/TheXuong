<template>
  <div class="flex flex-col items-center justify-center min-h-screen py-12 px-4 sm:px-6 lg:px-8 bg-[#F9F9F9]">
    <div class="max-w-md w-full space-y-8 text-center">
      <h1 class="text-9xl font-extrabold text-black tracking-widest">404</h1>
      <div class="mt-4">
        <p class="font-gelasio text-2xl text-[#5E5F5C] mb-2">Trang không tìm thấy</p>
        <p class="font-gelasio text-base text-[#5E5F5C] mb-8">
          Trang bạn đang tìm kiếm không tồn tại hoặc đã bị di chuyển.
        </p>
      </div>
      <router-link
        to="/"
        class="inline-flex items-center gap-2 px-8 py-3 bg-black text-white font-geist text-sm uppercase tracking-wider hover:bg-gray-900 transition-colors"
      >
        <svg class="w-4 h-4" viewBox="0 0 16 16" fill="currentColor">
          <path d="M8 1L1 7l7 6 7-6-7-7zM8 8L1 14l7-6 7 6-7-6z"/>
        </svg>
        Về trang chủ
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
// Not Found Page
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Gelasio:wght@400;500;600;700&family=Inter:wght@400;500;600;700&display=swap') layer(fonts);

.font-geist {
font-family: 'Geist', sans-serif;
}

.font-gelasio {
font-family: 'Geist', sans-serif;
}
</style>
