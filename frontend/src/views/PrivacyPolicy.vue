<template>
  <div
    ref="rootRef"
    class="relative min-h-screen bg-transparent text-[#1A1C1C]"
  >
    <!-- HERO -->
    <section
      ref="heroRef"
      class="relative w-full overflow-hidden bg-black text-white flex flex-col items-center justify-center"
      :style="{ height: '60dvh' }"
      @mousemove="onHeroMove"
    >
      <div class="absolute inset-0 z-0 hero-glow pointer-events-none" />
      <div class="absolute inset-0 z-0 hero-aurora pointer-events-none" />
      <div class="relative z-10 flex flex-col items-center text-center px-5 max-w-[900px]">
        <p
          class="font-geist text-[12px] uppercase tracking-[1.8px] text-white/60 mb-6 hero-anim hero-fade"
          style="animation-delay:.1s"
        >
          CHÍNH SÁCH
        </p>
        <h1 class="font-geist font-bold text-white leading-[0.95] mb-6">
          <span
            class="block text-5xl sm:text-7xl md:text-8xl hero-anim hero-reveal"
            style="letter-spacing:-1.28px;animation-delay:.25s"
          >BẢO MẬT</span>
        </h1>
        <p
          class="font-geist text-base sm:text-lg text-white/70 leading-[29px] max-w-[640px] mx-auto hero-anim hero-fade"
          style="animation-delay:.5s"
        >
          Cam kết bảo vệ dữ liệu cá nhân của bạn — minh bạch, an toàn và tuân thủ pháp luật.
        </p>
      </div>
      <div
        class="absolute bottom-6 left-1/2 -translate-x-1/2 z-10 hero-anim hero-fade"
        style="animation-delay:.8s"
      >
        <span class="block w-px h-10 bg-white/40 scroll-indicator" />
      </div>
    </section>

    <!-- OVERVIEW -->
    <section class="w-full max-w-[1280px] mx-auto px-4 py-24 md:py-32">
      <div class="grid md:grid-cols-2 gap-12 md:gap-20">
        <div
          class="md:sticky md:top-28 self-start"
          data-reveal
        >
          <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
            TỔNG QUAN
          </p>
          <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
            Chính sách bảo mật
          </h2>
        </div>
        <div
          class="relative"
          data-reveal
        >
          <div class="liquid-glass relative rounded-2xl border border-white/60 bg-white/50 p-8 md:p-10 shadow-[0_8px_32px_rgba(26,28,28,0.12)]">
            <span class="glass-sheen pointer-events-none" />
            <p class="font-geist text-base text-[#1A1C1C] leading-[29px] mb-5">
              THE XUONG SPORT cam kết bảo vệ quyền riêng tư và dữ liệu cá nhân của khách hàng. Chính sách này giải thích cách chúng tôi thu thập, sử dụng và bảo vệ thông tin của bạn.
            </p>
            <p class="font-geist text-base text-[#5E5F5C] leading-[29px] mb-5">
              Bằng việc sử dụng dịch vụ của chúng tôi, bạn đồng ý với các điều khoản được mô tả trong chính sách này. Chúng tôi tuân thủ đầy đủ các quy định của pháp luật Việt Nam về bảo vệ dữ liệu cá nhân.
            </p>
            <p class="font-geist text-base text-[#5E5F5C] leading-[29px]">
              Chính sách được cập nhật lần cuối: <strong>01/01/2025</strong>
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- DATA COLLECTION -->
    <section
      ref="collectionRef"
      class="w-full max-w-[1100px] mx-auto px-4 py-16 md:py-24"
    >
      <div
        class="text-center mb-12 md:mb-16"
        data-reveal
      >
        <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
          THU THẬP
        </p>
        <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
          Thông tin chúng tôi thu thập
        </h2>
      </div>
      <div class="relative">
        <div
          v-for="(item, i) in dataCollection"
          :key="i"
          data-stack-card
          data-reveal
          class="stack-card liquid-glass relative bg-white/55 border border-white/60 rounded-2xl p-8 md:p-12 shadow-[0_8px_32px_rgba(26,28,28,0.10)]"
          :style="{ zIndex: i }"
        >
          <span class="glass-sheen pointer-events-none" />
          <div class="flex items-start gap-6 md:gap-10">
            <span class="font-jetbrains text-[14px] text-[#5E5F5C] pt-2">0{{ i + 1 }}</span>
            <div class="flex-1">
              <h3 class="font-geist text-[28px] md:text-[32px] leading-[38px] tracking-[-0.32px] text-[#1A1C1C] mb-4">
                {{ item.title }}
              </h3>
              <p class="font-geist text-base text-[#5E5F5C] leading-[26px] max-w-[640px]">
                {{ item.desc }}
              </p>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- PURPOSE BENTO -->
    <section class="w-full max-w-[1280px] mx-auto px-4 py-16 md:py-24">
      <div
        class="text-center mb-12 md:mb-16"
        data-reveal
      >
        <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
          MỤC ĐÍCH
        </p>
        <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
          Cách chúng tôi sử dụng
        </h2>
      </div>
      <div class="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-5">
        <div
          v-for="(p, i) in purposes"
          :key="i"
          data-reveal
          class="liquid-glass bg-white/55 border border-white/60 rounded-2xl p-8 md:p-10 shadow-[0_8px_32px_rgba(26,28,28,0.08)] hover:bg-white/75 hover:-translate-y-1 transition-all duration-500"
        >
          <span class="glass-sheen pointer-events-none" />
          <div class="w-10 h-10 rounded-full bg-black/5 flex items-center justify-center mb-5">
            <span class="text-lg">{{ p.icon }}</span>
          </div>
          <h3 class="font-geist text-[22px] leading-[28px] tracking-[-0.22px] text-[#1A1C1C] mb-3">
            {{ p.title }}
          </h3>
          <p class="font-geist text-sm text-[#5E5F5C] leading-[24px]">
            {{ p.desc }}
          </p>
        </div>
      </div>
    </section>

    <!-- SECURITY MEASURES -->
    <section class="w-full relative bg-black text-white py-24 md:py-32 overflow-hidden">
      <div class="absolute inset-0 hero-glow pointer-events-none opacity-60" />
      <div class="relative max-w-[1280px] mx-auto px-4">
        <div
          class="text-center mb-12 md:mb-16"
          data-reveal
        >
          <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-white/60 mb-4">
            BẢO MẬT
          </p>
          <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] text-white">
            Biện pháp bảo vệ
          </h2>
        </div>
        <div class="grid grid-cols-1 md:grid-cols-2 gap-5">
          <div
            v-for="(m, i) in securityMeasures"
            :key="i"
            data-reveal
            class="liquid-glass relative bg-white/10 border border-white/20 rounded-2xl p-8 hover:bg-white/20 hover:-translate-y-1 transition-all duration-500"
          >
            <span class="glass-sheen pointer-events-none" />
            <h3 class="font-geist text-[20px] leading-[26px] text-white mb-3">
              {{ m.title }}
            </h3>
            <p class="font-geist text-sm text-white/70 leading-[24px]">
              {{ m.desc }}
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- USER RIGHTS -->
    <section class="w-full max-w-[1280px] mx-auto px-4 py-24 md:py-32">
      <div
        class="text-center mb-12 md:mb-16"
        data-reveal
      >
        <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
          QUYỀN LỢI
        </p>
        <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
          Quyền của bạn
        </h2>
      </div>
      <div
        class="liquid-glass relative rounded-2xl border border-white/60 bg-white/50 p-6 md:p-10 shadow-[0_8px_32px_rgba(26,28,28,0.12)]"
        data-reveal
      >
        <span class="glass-sheen pointer-events-none" />
        <div class="overflow-x-auto">
          <table class="w-full">
            <thead>
              <tr class="border-b border-[#CFC4C6]">
                <th class="font-geist text-[13px] uppercase tracking-[1.2px] text-[#5E5F5C] py-4 px-3 text-left">
                  Quyền
                </th>
                <th class="font-geist text-[13px] uppercase tracking-[1.2px] text-[#5E5F5C] py-4 px-3 text-left">
                  Mô tả
                </th>
              </tr>
            </thead>
            <tbody>
              <tr
                v-for="(r, i) in userRights"
                :key="i"
                class="border-b border-[#CFC4C6]/50 hover:bg-white/40 transition-colors"
              >
                <td class="font-geist py-4 px-3 text-[15px] font-semibold text-[#1A1C1C]">
                  {{ r.right }}
                </td>
                <td class="font-geist py-4 px-3 text-sm text-[#5E5F5C]">
                  {{ r.desc }}
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </section>

    <!-- COOKIES -->
    <section class="w-full max-w-[1280px] mx-auto px-4 py-16 md:py-24">
      <div class="grid md:grid-cols-2 gap-12 md:gap-20">
        <div
          class="md:sticky md:top-28 self-start"
          data-reveal
        >
          <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
            COOKIES
          </p>
          <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] uppercase text-[#1A1C1C]">
            Cookie & theo dõi
          </h2>
        </div>
        <div
          class="relative"
          data-reveal
        >
          <div class="liquid-glass relative rounded-2xl border border-white/60 bg-white/50 p-8 md:p-10 shadow-[0_8px_32px_rgba(26,28,28,0.12)]">
            <span class="glass-sheen pointer-events-none" />
            <p class="font-geist text-base text-[#1A1C1C] leading-[29px] mb-5">
              Chúng tôi sử dụng cookie để cải thiện trải nghiệm của bạn trên website. Cookie là các tệp nhỏ được lưu trên trình duyệt của bạn.
            </p>
            <p class="font-geist text-base text-[#5E5F5C] leading-[29px] mb-5">
              <strong>Cookie cần thiết:</strong> Giúp website hoạt động đúng cách (đăng nhập, giỏ hàng, bảo mật).
            </p>
            <p class="font-geist text-base text-[#5E5F5C] leading-[29px] mb-5">
              <strong>Cookie phân tích:</strong> Giúp chúng tôi hiểu cách bạn sử dụng website để cải thiện dịch vụ.
            </p>
            <p class="font-geist text-base text-[#5E5F5C] leading-[29px]">
              Bạn có thể tắt cookie trong cài đặt trình duyệt, nhưng một số tính năng có thể không hoạt động đúng.
            </p>
          </div>
        </div>
      </div>
    </section>

    <!-- CTA -->
    <section class="w-full relative bg-black text-white py-28 md:py-40 overflow-hidden">
      <div class="absolute inset-0 hero-glow pointer-events-none opacity-70" />
      <div
        class="relative max-w-[900px] mx-auto px-4 text-center"
        data-reveal
      >
        <h2 class="font-geist text-[40px] sm:text-[56px] md:text-[64px] leading-[70px] tracking-[-1.28px] text-white mb-8">
          Có thắc mắc về bảo mật?
        </h2>
        <p class="font-geist text-base text-white/70 leading-[29px] mb-10 max-w-[560px] mx-auto">
          Đội ngũ của chúng tôi sẵn sàng giải đáp mọi thắc mắc về chính sách bảo mật và quyền riêng tư của bạn.
        </p>
        <div class="flex flex-col sm:flex-row gap-4 justify-center">
          <router-link
            to="/about"
            class="liquid-btn px-10 py-5 bg-white/10 text-white text-[12px] font-semibold uppercase tracking-[1.8px] leading-[12px] rounded-full hover:bg-white/20 transition-colors"
          >
            LIÊN HỆ NGAY
          </router-link>
          <router-link
            to="/policy/returns"
            class="liquid-btn px-10 py-5 bg-white/10 text-white text-[12px] font-semibold uppercase tracking-[1.8px] leading-[12px] rounded-full hover:bg-white/20 transition-colors"
          >
            CHÍNH SÁCH ĐỔI TRẢ
          </router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { useReveal } from '@/composables/useReveal'
import { useScrollReveal } from '@/composables/useScrollReveal'

const rootRef = ref<HTMLElement | null>(null)
const heroRef = ref<HTMLElement | null>(null)
const collectionRef = ref<HTMLElement | null>(null)
let glowRaf: number | null = null

const onHeroMove = (e: MouseEvent) => {
  if (glowRaf) return
  glowRaf = requestAnimationFrame(() => {
    const hero = heroRef.value
    if (hero) {
      const rect = hero.getBoundingClientRect()
      hero.style.setProperty('--mx', (e.clientX - rect.left) + 'px')
      hero.style.setProperty('--my', (e.clientY - rect.top) + 'px')
    }
    glowRaf = null
  })
}

useReveal(rootRef)
useScrollReveal(collectionRef)

onUnmounted(() => {
  if (glowRaf) cancelAnimationFrame(glowRaf)
})

const dataCollection = [
  {
    title: 'Thông tin cá nhân',
    desc: 'Họ tên, email, số điện thoại, địa chỉ giao hàng khi bạn đăng ký tài khoản hoặc đặt hàng.'
  },
  {
    title: 'Thông tin đơn hàng',
    desc: 'Lịch sử mua hàng, phương thức thanh toán, địa chỉ giao nhận để xử lý và theo dõi đơn hàng.'
  },
  {
    title: 'Dữ liệu thiết bị',
    desc: 'Địa chỉ IP, loại trình duyệt, thiết bị sử dụng để cải thiện trải nghiệm và bảo mật tài khoản.'
  },
  {
    title: 'Dữ liệu sử dụng',
    desc: 'Các trang đã xem, thời gian truy cập, tương tác trên website để phân tích và tối ưu hóa dịch vụ.'
  }
]

const purposes = [
  {
    icon: '📦',
    title: 'Xử lý đơn hàng',
    desc: 'Xác nhận, đóng gói, giao hàng và xử lý các yêu cầu đổi trả, hoàn tiền.'
  },
  {
    icon: '👤',
    title: 'Quản lý tài khoản',
    desc: 'Duy trì tài khoản, lịch sử đơn hàng, điểm tích lũy và ưu đãi cá nhân hóa.'
  },
  {
    icon: '📧',
    title: 'Thông báo & hỗ trợ',
    desc: 'Gửi thông tin đơn hàng, cập nhật trạng thái, và phản hồi yêu cầu hỗ trợ.'
  },
  {
    icon: '📊',
    title: 'Phân tích & cải thiện',
    desc: 'Hiểu hành vi người dùng để tối ưu giao diện, tính năng và trải nghiệm mua sắm.'
  },
  {
    icon: '🔒',
    title: 'Bảo mật & an toàn',
    desc: 'Phát hiện và ngăn chặn gian lận, bảo vệ tài khoản và dữ liệu khách hàng.'
  },
  {
    icon: '📢',
    title: 'Marketing (có sự đồng ý)',
    desc: 'Gửi thông tin khuyến mãi, sản phẩm mới khi bạn đã đồng ý nhận thông tin từ chúng tôi.'
  }
]

const securityMeasures = [
  {
    title: 'Mã hóa SSL/TLS',
    desc: 'Mọi dữ liệu truyền tải giữa trình duyệt và máy chủ được mã hóa bằng giao thức SSL/TLS 256-bit.'
  },
  {
    title: 'Bảo vệ thanh toán',
    desc: 'Thông tin thanh toán được xử lý qua cổng thanh toán đạt chuẩn PCI DSS, không lưu trữ trên hệ thống.'
  },
  {
    title: 'Kiểm soát truy cập',
    desc: 'Chỉ nhân viên được ủy quyền mới có quyền truy cập dữ liệu khách hàng, với ghi nhật ký đầy đủ.'
  },
  {
    title: 'Sao lưu & phục hồi',
    desc: 'Dữ liệu được sao lưu định kỳ và có kế hoạch phục hồi khi xảy ra sự cố.'
  }
]

const userRights = [
  { right: 'Truy cập', desc: 'Xem thông tin cá nhân mà chúng tôi đang lưu trữ về bạn.' },
  { right: 'Chỉnh sửa', desc: 'Yêu cầu cập nhật, sửa đổi thông tin không chính xác hoặc đã thay đổi.' },
  { right: 'Xóa', desc: 'Yêu cầu xóa dữ liệu cá nhân, trừ khi pháp luật yêu cầu lưu giữ.' },
  { right: 'Phản đối', desc: 'Từ chối việc xử lý dữ liệu cho mục đích marketing trực tiếp.' },
  { right: 'Rút lại đồng ý', desc: 'Hủy bỏ sự đồng ý đã cấp trước đó bất kỳ lúc nào.' },
  { right: 'Khiếu nại', desc: 'Gửi khiếu nại đến cơ quan có thẩm quyền nếu cho rằng quyền bị vi phạm.' }
]
</script>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Geist:wght@300;400;500;600;700&family=JetBrains+Mono:ital,wght@0,400;0,500;0,600;1,400;1,500;1,600&display=swap') layer(fonts);

.font-geist {
  font-family: 'Geist', sans-serif;
}

.font-jetbrains {
  font-family: 'JetBrains Mono', monospace;
}

/* ---- Liquid glass ---- */
.liquid-glass {
  -webkit-backdrop-filter: blur(18px) saturate(160%);
  backdrop-filter: blur(18px) saturate(160%);
  background-image: linear-gradient(135deg, rgba(255, 255, 255, 0.55), rgba(255, 255, 255, 0.18));
  position: relative;
  overflow: hidden;
}

.glass-sheen {
  position: absolute;
  inset: 0;
  border-radius: inherit;
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.55) 0%, transparent 42%),
    radial-gradient(120% 80% at 50% -20%, rgba(255, 255, 255, 0.35), transparent 60%);
  pointer-events: none;
  opacity: 0.9;
}

.liquid-btn {
  -webkit-backdrop-filter: blur(12px) saturate(160%);
  backdrop-filter: blur(12px) saturate(160%);
  border: 1px solid rgba(255, 255, 255, 0.4);
  box-shadow: 0 4px 24px rgba(255, 255, 255, 0.08), inset 0 1px 0 rgba(255, 255, 255, 0.25);
}

/* ---- Aurora on hero ---- */
.hero-aurora {
  background:
    radial-gradient(40% 60% at 20% 20%, rgba(255, 107, 53, 0.18), transparent 70%),
    radial-gradient(45% 55% at 80% 30%, rgba(14, 165, 233, 0.14), transparent 70%),
    radial-gradient(50% 60% at 50% 90%, rgba(120, 255, 220, 0.12), transparent 70%);
  filter: blur(20px);
  animation: auroraDrift 14s ease-in-out infinite alternate;
  transform: translate3d(0, 0, 0);
}

@keyframes auroraDrift {
  0% { transform: translate3d(-3%, -2%, 0) scale(1.05); }
  100% { transform: translate3d(3%, 2%, 0) scale(1.12); }
}

/* ---- Scroll reveal ---- */
[data-reveal] {
  opacity: 0;
  transform: translateY(34px);
  filter: blur(8px);
  transition: opacity 0.9s cubic-bezier(0.16, 1, 0.3, 1),
    transform 0.9s cubic-bezier(0.16, 1, 0.3, 1),
    filter 0.9s cubic-bezier(0.16, 1, 0.3, 1);
  will-change: opacity, transform, filter;
}

[data-reveal].is-revealed {
  opacity: 1;
  transform: translateY(0);
  filter: blur(0);
}

.hero-glow {
  background: radial-gradient(600px circle at var(--mx, 50%) var(--my, 50%), rgba(255, 255, 255, 0.12), transparent 60%);
}

.stack-card {
  position: sticky;
  top: 96px;
  margin-bottom: 24px;
}

@media (max-width: 768px) {
  .stack-card { top: 80px; }
}

@keyframes heroReveal {
  0% { opacity: 0; transform: translateY(28px); filter: blur(12px); }
  100% { opacity: 1; transform: translateY(0); filter: blur(0); }
}

@keyframes heroFadeUp {
  0% { opacity: 0; transform: translateY(20px); }
  100% { opacity: 1; transform: translateY(0); }
}

.hero-anim {
  opacity: 0;
  animation-fill-mode: forwards;
  animation-timing-function: cubic-bezier(0.16, 1, 0.3, 1);
}

.hero-reveal { animation-name: heroReveal; animation-duration: 1.1s; }
.hero-fade { animation-name: heroFadeUp; animation-duration: 1s; }

@keyframes scrollIndicator {
  0% { transform: scaleY(0); transform-origin: top; }
  50% { transform: scaleY(1); transform-origin: top; }
  100% { transform: scaleY(0); transform-origin: bottom; }
}

.scroll-indicator { animation: scrollIndicator 2s ease-in-out infinite; }

@media (prefers-reduced-motion: reduce) {
  .hero-anim,
  .scroll-indicator,
  .hero-aurora { animation: none; opacity: 1; }
  .hero-glow,
  .hero-aurora { display: none; }
  .stack-card { position: static; margin-bottom: 24px; }
  [data-reveal] { opacity: 1; transform: none; filter: none; transition: none; }
}
</style>
