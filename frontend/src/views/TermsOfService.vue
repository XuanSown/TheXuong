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
          PHÁP LÝ
        </p>
        <h1 class="font-geist font-bold text-white leading-[0.95] mb-6">
          <span
            class="block text-4xl sm:text-6xl md:text-7xl hero-anim hero-reveal"
            style="letter-spacing:-1.28px;animation-delay:.25s"
          >ĐIỀU KHOẢN</span>
          <span
            class="block text-4xl sm:text-6xl md:text-7xl -mt-1 hero-anim hero-reveal text-white/80"
            style="letter-spacing:-0.64px;animation-delay:.42s"
          >DỊCH VỤ</span>
        </h1>
        <p
          class="font-geist text-base sm:text-lg text-white/70 leading-[29px] max-w-[640px] mx-auto hero-anim hero-fade"
          style="animation-delay:.6s"
        >
          Quy định và điều khoản sử dụng dịch vụ tại THE XUONG SPORT — cập nhật lần cuối: tháng 7/2025.
        </p>
      </div>
      <div
        class="absolute bottom-6 left-1/2 -translate-x-1/2 z-10 hero-anim hero-fade"
        style="animation-delay:.8s"
      >
        <span class="block w-px h-10 bg-white/40 scroll-indicator" />
      </div>
    </section>

    <!-- TABLE OF CONTENTS -->
    <section class="w-full max-w-[1280px] mx-auto px-4 py-24 md:py-32">
      <div class="grid md:grid-cols-[1fr_2fr] gap-12 md:gap-20">
        <div
          class="md:sticky md:top-28 self-start"
          data-reveal
        >
          <p class="font-geist text-[12px] uppercase tracking-[1.8px] text-[#5E5F5C] mb-4">
            MỤC LỤC
          </p>
          <h2 class="font-geist text-[32px] sm:text-[40px] leading-[48px] tracking-[-0.64px] text-[#1A1C1C]">
            Nội dung chính
          </h2>
        </div>
        <nav
          class="space-y-3"
          data-reveal
        >
          <a
            v-for="(section, i) in tocItems"
            :key="i"
            :href="'#section-' + i"
            class="liquid-glass block rounded-xl border border-white/60 bg-white/50 p-5 hover:bg-white/75 hover:-translate-y-0.5 transition-all duration-300 group"
          >
            <span class="glass-sheen pointer-events-none" />
            <div class="flex items-center gap-4">
              <span class="font-jetbrains text-[13px] text-[#5E5F5C] group-hover:text-[#1A1C1C] transition-colors">0{{ i + 1 }}</span>
              <span class="font-geist text-[16px] text-[#1A1C1C] group-hover:translate-x-1 transition-transform duration-300">{{ section }}</span>
            </div>
          </a>
        </nav>
      </div>
    </section>

    <!-- SECTIONS -->
    <section class="w-full max-w-[1280px] mx-auto px-4 pb-16 md:pb-24">
      <div class="space-y-20 md:space-y-28">
        <div
          v-for="(section, i) in sections"
          :id="'section-' + i"
          :key="i"
          class="grid md:grid-cols-[1fr_2fr] gap-10 md:gap-20 scroll-mt-28"
        >
          <div
            class="md:sticky md:top-28 self-start"
            data-reveal
          >
            <span class="font-jetbrains text-[13px] text-[#5E5F5C] mb-2 block">0{{ i + 1 }}</span>
            <h3 class="font-geist text-[28px] sm:text-[36px] leading-[42px] tracking-[-0.36px] text-[#1A1C1C]">
              {{ section.title }}
            </h3>
          </div>
          <div class="space-y-5">
            <div
              v-for="(block, j) in section.blocks"
              :key="j"
              data-reveal
              class="liquid-glass relative rounded-2xl border border-white/60 bg-white/50 p-8 shadow-[0_8px_32px_rgba(26,28,28,0.10)]"
            >
              <span class="glass-sheen pointer-events-none" />
              <h4
                v-if="block.subtitle"
                class="font-geist text-[18px] leading-[26px] text-[#1A1C1C] mb-3"
              >
                {{ block.subtitle }}
              </h4>
              <p class="font-geist text-base text-[#5E5F5C] leading-[29px]">
                {{ block.content }}
              </p>
              <ul
                v-if="block.list"
                class="mt-4 space-y-2"
              >
                <li
                  v-for="(item, k) in block.list"
                  :key="k"
                  class="font-geist text-sm text-[#5E5F5C] leading-[26px] pl-5 relative before:content-[''] before:absolute before:left-0 before:top-[10px] before:w-[6px] before:h-[6px] before:rounded-full before:bg-[#CFC4C6]"
                >
                  {{ item }}
                </li>
              </ul>
            </div>
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
          Có thắc mắc về điều khoản?
        </h2>
        <p class="font-geist text-base text-white/70 leading-[29px] mb-10 max-w-[560px] mx-auto">
          Liên hệ với chúng tôi để được giải đáp mọi thắc mắc liên quan đến điều khoản dịch vụ.
        </p>
        <div class="flex flex-col sm:flex-row gap-4 justify-center">
          <router-link
            to="/contact"
            class="liquid-btn px-10 py-5 bg-white/10 text-white text-[12px] font-semibold uppercase tracking-[1.8px] leading-[12px] rounded-full hover:bg-white/20 transition-colors"
          >
            LIÊN HỆ NGAY
          </router-link>
          <router-link
            to="/policy/privacy"
            class="liquid-btn px-10 py-5 bg-white/10 text-white text-[12px] font-semibold uppercase tracking-[1.8px] leading-[12px] rounded-full hover:bg-white/20 transition-colors"
          >
            CHÍNH SÁCH BẢO MẬT
          </router-link>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup lang="ts">
import { ref, onUnmounted } from 'vue'
import { useReveal } from '@/composables/useReveal'

const rootRef = ref<HTMLElement | null>(null)
const heroRef = ref<HTMLElement | null>(null)
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

onUnmounted(() => {
  if (glowRaf) cancelAnimationFrame(glowRaf)
})

const tocItems = [
  'Giới thiệu chung',
  'Tài khoản người dùng',
  'Sản phẩm & đặt hàng',
  'Thanh toán & giao hàng',
  'Đổi trả & hoàn tiền',
  'Bảo mật thông tin',
  'Sở hữu trí tuệ',
  'Giải quyết tranh chấp',
  'Thay đổi điều khoản'
]

interface Block {
  subtitle?: string
  content: string
  list?: string[]
}

const sections: { title: string; blocks: Block[] }[] = [
  {
    title: 'Giới thiệu chung',
    blocks: [
      {
        content: 'Chào mừng bạn đến với THE XUONG SPORT — sàn thương mại điện tử chuyên cung cấp đồ thể thao chính hãng. Bằng việc truy cập và sử dụng website, bạn đồng ý tuân thủ các điều khoản và điều kiện dưới đây.'
      },
      {
        subtitle: 'Phạm vi áp dụng',
        content: 'Các điều khoản này áp dụng cho mọi hoạt động trên website thexuong.xuansown.id.vn, bao gồm duyệt sản phẩm, đặt hàng, thanh toán và sử dụng tài khoản.'
      }
    ]
  },
  {
    title: 'Tài khoản người dùng',
    blocks: [
      {
        subtitle: 'Đăng ký & bảo mật',
        content: 'Bạn chịu trách nhiệm cung cấp thông tin chính xác khi đăng ký tài khoản và bảo mật mật khẩu của mình.',
        list: [
          'Mỗi cá nhân chỉ được phép tạo một tài khoản duy nhất',
          'Bạn chịu trách nhiệm cho mọi hoạt động diễn ra dưới tài khoản của mình',
          'Thông báo ngay cho chúng tôi nếu phát hiện tài khoản bị truy cập trái phép'
        ]
      },
      {
        subtitle: 'Quyền hạn của THE XUONG',
        content: 'Chúng tôi có quyền tạm khóa hoặc xóa tài khoản nếu phát hiện hành vi gian lận, vi phạm điều khoản hoặc gây ảnh hưởng đến người dùng khác.'
      }
    ]
  },
  {
    title: 'Sản phẩm & đặt hàng',
    blocks: [
      {
        subtitle: 'Thông tin sản phẩm',
        content: 'THE XUONG SPORT cam kết cung cấp thông tin sản phẩm chính xác bao gồm mô tả, hình ảnh, giá bán và tình trạng hàng. Tuy nhiên, màu sắc thực tế có thể khác biệt nhẹ do thiết bị hiển thị.'
      },
      {
        subtitle: 'Xác nhận đơn hàng',
        content: 'Đơn hàng được xác nhận khi bạn nhận được email xác nhận từ hệ thống. Chúng tôi có quyền từ chối đơn hàng nếu phát hiện dấu hiệu gian lận hoặc thông tin không chính xác.'
      }
    ]
  },
  {
    title: 'Thanh toán & giao hàng',
    blocks: [
      {
        subtitle: 'Phương thức thanh toán',
        content: 'Chúng tôi hỗ trợ nhiều phương thức thanh toán bao gồm thẻ tín dụng/ghi nợ, chuyển khoản ngân hàng, ví điện tử và thanh toán khi nhận hàng (COD).',
        list: [
          'Giá hiển thị trên website đã bao gồm VAT',
          'Phí vận chuyển được tính riêng và hiển thị trước khi thanh toán',
          'Giao dịch được mã hóa SSL để đảm bảo an toàn'
        ]
      },
      {
        subtitle: 'Giao hàng',
        content: 'Thời gian giao hàng từ 2-7 ngày làm việc tùy theo khu vực. THE XUONG SPORT không chịu trách nhiệm cho các trường hợp chậm giao do yếu tố bất khả kháng như thiên tai, dịch bệnh.'
      }
    ]
  },
  {
    title: 'Đổi trả & hoàn tiền',
    blocks: [
      {
        content: 'Chính sách đổi trả được quy định chi tiết tại trang Chính sách đổi trả. Tóm tắt:',
        list: [
          'Đổi trả trong vòng 7 ngày kể từ ngày nhận hàng',
          'Sản phẩm phải còn nguyên tem mác, chưa qua sử dụng',
          'Hoàn tiền được xử lý trong 3-5 ngày làm việc',
          'Phí vận chuyển đổi trả do khách hàng chi trả (trừ lỗi nhà sản xuất)'
        ]
      }
    ]
  },
  {
    title: 'Bảo mật thông tin',
    blocks: [
      {
        subtitle: 'Thu thập & sử dụng',
        content: 'Chúng tôi thu thập thông tin cá nhân (tên, email, số điện thoại, địa chỉ) chỉ để phục vụ xử lý đơn hàng và cải thiện trải nghiệm người dùng. Thông tin không được chia sẻ cho bên thứ ba trừ khi có yêu cầu pháp lý.'
      },
      {
        subtitle: 'Bảo vệ dữ liệu',
        content: 'Mọi giao dịch và dữ liệu cá nhân được bảo vệ bằng mã hóa SSL. Chúng tôi tuân thủ các tiêu chuẩn bảo mật ngành để đảm bảo an toàn thông tin khách hàng.'
      }
    ]
  },
  {
    title: 'Sở hữu trí tuệ',
    blocks: [
      {
        content: 'Toàn bộ nội dung trên website bao gồm văn bản, hình ảnh, logo, thiết kế và mã nguồn thuộc quyền sở hữu của THE XUONG SPORT hoặc các đối tác cấp phép.',
        list: [
          'Không được sao chép, phân phối hoặc sử dụng nội dung khi chưa có sự đồng ý bằng văn bản',
          'Tên thương hiệu và logo của các đối tác (Nike, Adidas, Li-Ning, Puma) thuộc sở hữu của respective chủ sở hữu',
          'Vi phạm sở hữu trí tuệ sẽ bị xử lý theo quy định pháp luật'
        ]
      }
    ]
  },
  {
    title: 'Giải quyết tranh chấp',
    blocks: [
      {
        content: 'Mọi tranh chấp phát sinh từ việc sử dụng dịch vụ sẽ được giải quyết thông qua thương lượng trực tiếp giữa hai bên. Nếu không đạt được thỏa thuận, tranh chấp sẽ được đưa ra Tòa án có thẩm quyền tại Việt Nam.'
      },
      {
        subtitle: 'Khiếu nại',
        content: 'Để gửi khiếu nại, vui lòng liên hệ qua email hoặc trang liên hệ với mô tả chi tiết về vấn đề. Chúng tôi cam kết phản hồi trong vòng 48 giờ làm việc.'
      }
    ]
  },
  {
    title: 'Thay đổi điều khoản',
    blocks: [
      {
        content: 'THE XUONG SPORT có quyền thay đổi, bổ sung điều khoản bất kỳ lúc nào mà không cần thông báo trước. Phiên bản mới nhất sẽ được cập nhật trên trang này. Việc tiếp tục sử dụng dịch vụ sau khi thay đổi được coi là chấp nhận điều khoản mới.'
      }
    ]
  }
]
</script>

<style scoped>
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
  [data-reveal] { opacity: 1; transform: none; filter: none; transition: none; }
}
</style>
