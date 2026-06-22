<template>
  <div class="min-h-screen flex items-center justify-center bg-gray-100">
    <div class="text-center">
      <h1 class="text-9xl font-bold text-gray-300">404</h1>
      <p class="text-2xl text-gray-600 mt-4">Không tìm thấy trang</p>
      <p class="text-gray-500 mt-2">Trang bạn đang tìm kiếm không tồn tại.</p>
      <router-link
        to="/"
        class="inline-block mt-6 px-6 py-3 bg-primary-500 text-white rounded-md hover:bg-primary-600 transition-base"
      >
        Về trang chủ
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
</script>
