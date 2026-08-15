<template>
  <div class="flex flex-col items-center justify-center min-h-screen py-12 px-4 sm:px-6 lg:px-8 bg-[#F9F9F9]">
    <div class="max-w-md w-full space-y-8 text-center">
      <h1 class="text-9xl font-extrabold text-black tracking-widest">
        404
      </h1>
      <div class="mt-4">
        <p class="font-gelasio text-2xl text-[#5E5F5C] mb-2">
          {{ t('notFound.title') }}
        </p>
        <p class="font-gelasio text-base text-[#5E5F5C] mb-8">
          {{ t('notFound.desc') }}
        </p>
      </div>
      <router-link
        to="/"
        class="inline-flex items-center gap-2 px-8 py-3 bg-black text-white font-geist text-sm uppercase tracking-wider hover:bg-gray-900 transition-colors"
      >
        <svg
          class="w-4 h-4"
          viewBox="0 0 16 16"
          fill="currentColor"
        >
          <path d="M8 1L1 7l7 6 7-6-7-7zM8 8L1 14l7-6 7 6-7-6z" />
        </svg>
        {{ t('notFound.backHome') }}
      </router-link>
    </div>
  </div>
</template>

<script setup lang="ts">
import { useI18n } from 'vue-i18n'

const { t } = useI18n()
</script>

<style scoped>
</style>
