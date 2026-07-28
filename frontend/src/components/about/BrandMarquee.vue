<template>
  <section
    class="w-full bg-white border-y border-[#CFC4C6] overflow-hidden
      [mask-image:linear-gradient(to_right,transparent,black_8%,black_92%,transparent)]
      [-webkit-mask-image:linear-gradient(to_right,transparent,black_8%,black_92%,transparent)]"
  >
    <div class="relative flex w-full">
      <div
        class="flex animate-marquee whitespace-nowrap items-center min-w-full gap-[120px] py-6"
      >
        <span
          v-for="(brand, i) in brands"
          :key="`a-${i}`"
          class="font-geist text-lg font-semibold text-[#1A1C1C] opacity-80"
        >{{ brand }}</span>
      </div>
      <div
        class="flex animate-marquee2 absolute top-0 whitespace-nowrap items-center min-w-full gap-[120px] py-6"
        aria-hidden="true"
      >
        <span
          v-for="(brand, i) in brands"
          :key="`b-${i}`"
          class="font-geist text-lg font-semibold text-[#1A1C1C] opacity-80"
        >{{ brand }}</span>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
const brands = ['NIKE', 'ADIDAS', 'PUMA', 'LI-NING', 'NEW BALANCE', 'ASICS']
</script>

<style scoped>
@keyframes marquee {
  0% { transform: translateX(0); }
  100% { transform: translateX(-100%); }
}

@keyframes marquee2 {
  0% { transform: translateX(100%); }
  100% { transform: translateX(0); }
}

.animate-marquee { animation: marquee 30s linear infinite; }
.animate-marquee2 { animation: marquee2 30s linear infinite; }

@media (prefers-reduced-motion: reduce) {
  .animate-marquee,
  .animate-marquee2 { animation: none; }
}
</style>
