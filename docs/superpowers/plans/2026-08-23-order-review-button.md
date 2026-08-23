# Nút "Đánh giá sản phẩm" trên trang đơn hàng — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Sau khi xác nhận "Đã nhận được hàng" (đơn `COMPLETED`), mỗi sản phẩm trong trang chi tiết đơn có nút dẫn đến mục đánh giá của sản phẩm đó.

**Architecture:** Frontend-only. OrderDetail.vue thêm nút review per item khi status `COMPLETED`, check trạng thái đã-review qua API `GET /reviews/product/{id}` song song, navigate sang `/product-detail/:id?review=1`; ProductDetail.vue thêm anchor `id="reviews"` + auto-scroll khi có query. Spec: `docs/superpowers/specs/2026-08-23-order-review-button-design.md`.

**Tech Stack:** Vue 3 + TypeScript + Tailwind (arbitrary values), vue-router 4, vue-i18n, Vitest + @vue/test-utils.

## Global Constraints

- KHÔNG đổi bất kỳ file backend nào.
- Không thêm dependency mới.
- Rule icon 2 trang đang làm: màu chính **đen** (`text-black`/`bg-black`); xám nhạt CHỈ cho trạng thái phụ (sao rỗng, placeholder ảnh, hover).
- Nhãn i18n dùng đúng key: `order.reviewProduct` = `"Đánh giá sản phẩm"` (vi) / `"Review product"` (en); `order.reviewed` = `"Đã đánh giá"` (vi) / `"Reviewed"` (en).
- Đường dẫn điều hướng chính xác: `/product-detail/${productId}` kèm `query: { review: '1' }`.
- KHÔNG chạy `npm run lint` (script có `--fix`, sẽ sửa tràn file ngoài scope). Chỉ dùng `npx eslint <file...>` scoped.
- Type-check có sẵn 2 lỗi pre-existing KHÔNG được sửa và không được tạo lỗi mới: `AdminProducts.vue` TS6133, `AdminUsers.vue` TS2353.
- Star path SVG copy nguyên văn từ `frontend/src/components/ui/StarRating.vue` (hằng `STAR_PATH`).
- Commit theo từng task, message tiếng Anh kiểu conventional (`feat:`/`test:`/`style:`).

---

### Task 1: Nút review trên OrderDetail + i18n + test

**Files:**
- Modify: `frontend/src/views/OrderDetail.vue`
- Modify: `frontend/src/i18n/locales/vi.json` (nhóm `"order"`, sau dòng `"receivedError"`)
- Modify: `frontend/src/i18n/locales/en.json` (nhóm `"order"`, sau dòng `"receivedError"`)
- Test: `frontend/src/views/__tests__/OrderDetail.spec.ts` (tạo mới)

**Interfaces:**
- Consumes: `reviewService.getProductReviews(productId: number): Promise<ReviewListResponse>` (named export từ `@/services/review.service`); `ReviewListResponse.reviews[].isMine: boolean` từ `@/types/review.types`.
- Produces: trong OrderDetail.vue — `goToReview(productId: number)` điều hướng; state `reviewedProductIds: Ref<Set<number>>`. Task 2 KHÔNG phụ thuộc task này.

- [ ] **Step 1: Thêm i18n keys**

Trong `frontend/src/i18n/locales/vi.json`, nhóm `"order"` — sau dòng `"receivedError": "Đã xảy ra lỗi khi xác nhận nhận hàng"` (dòng ~390, nhớ thêm dấu phẩy vào cuối dòng đó):

```json
    "reviewProduct": "Đánh giá sản phẩm",
    "reviewed": "Đã đánh giá"
```

Trong `frontend/src/i18n/locales/en.json` cùng vị trí tương đối (sau `"receivedError": "An error occurred while confirming receipt"`):

```json
    "reviewProduct": "Review product",
    "reviewed": "Reviewed"
```

- [ ] **Step 2: Viết test thất bại — `frontend/src/views/__tests__/OrderDetail.spec.ts`**

```ts
import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount, flushPromises } from '@vue/test-utils'
import OrderDetail from '@/views/OrderDetail.vue'

const pushMock = vi.fn()

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: pushMock }),
  useRoute: () => ({ params: { id: '1' }, query: {} })
}))
vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key: string) => key })
}))
vi.mock('@/utils/apiError', () => ({
  getApiErrorMessage: (_e: unknown, key: string) => key
}))

let currentOrder: any = null
const fetchOrderById = vi.fn()
vi.mock('@/stores/order.store', () => ({
  useOrderStore: () => ({
    get currentOrder() {
      return currentOrder
    },
    fetchOrderById
  })
}))

const getProductReviews = vi.fn()
vi.mock('@/services/review.service', () => ({
  reviewService: {
    getProductReviews: (...args: unknown[]) => getProductReviews(...args)
  }
}))

const baseItem = {
  productId: 10,
  productName: 'Giày chạy bộ',
  variantId: 1,
  size: '42',
  quantity: 1,
  price: 100000,
  subtotal: 100000,
  imageUrl: ''
}

const baseOrder = (overrides: Record<string, unknown>) => ({
  id: 1,
  status: 'COMPLETED',
  items: [{ ...baseItem }],
  createdAt: '2026-08-23T10:00:00Z',
  fullName: 'Nguyen Van A',
  phoneNumber: '0900000000',
  address: 'Ha Noi',
  note: '',
  paymentMethod: 'cod',
  subtotal: 100000,
  total: 100000,
  ...overrides
})

const countLabel = (wrapper: ReturnType<typeof mount>, label: string) =>
  wrapper.text().split(label).length - 1

const mountView = async () => {
  const wrapper = mount(OrderDetail)
  await flushPromises()
  return wrapper
}

beforeEach(() => {
  vi.clearAllMocks()
  currentOrder = null
})

describe('OrderDetail review buttons', () => {
  it('renders no review button when order is DELIVERED', async () => {
    currentOrder = baseOrder({ status: 'DELIVERED' })
    const wrapper = await mountView()
    expect(countLabel(wrapper, 'order.reviewProduct')).toBe(0)
    expect(countLabel(wrapper, 'order.reviewed')).toBe(0)
    expect(getProductReviews).not.toHaveBeenCalled()
  })

  it('renders one review button per item when COMPLETED', async () => {
    currentOrder = baseOrder({
      items: [{ ...baseItem }, { ...baseItem, productId: 11 }]
    })
    getProductReviews.mockResolvedValue({ summary: {}, reviews: [] })
    const wrapper = await mountView()
    expect(getProductReviews).toHaveBeenCalledTimes(2)
    expect(getProductReviews).toHaveBeenCalledWith(10)
    expect(getProductReviews).toHaveBeenCalledWith(11)
    expect(countLabel(wrapper, 'order.reviewProduct')).toBe(2)
  })

  it('switches label to Reviewed for products already reviewed by me', async () => {
    currentOrder = baseOrder({
      items: [{ ...baseItem }, { ...baseItem, productId: 11 }]
    })
    getProductReviews.mockImplementation((pid: number) =>
      Promise.resolve(
        pid === 10
          ? { summary: {}, reviews: [{ isMine: true }] }
          : { summary: {}, reviews: [] }
      )
    )
    const wrapper = await mountView()
    expect(countLabel(wrapper, 'order.reviewed')).toBe(1)
    expect(countLabel(wrapper, 'order.reviewProduct')).toBe(1)
  })

  it('navigates to product detail with review=1 on click', async () => {
    currentOrder = baseOrder({})
    getProductReviews.mockResolvedValue({ summary: {}, reviews: [] })
    const wrapper = await mountView()
    const btn = wrapper
      .findAll('button')
      .find((b) => b.text().includes('order.reviewProduct'))
    expect(btn).toBeDefined()
    await btn!.trigger('click')
    expect(pushMock).toHaveBeenCalledWith({
      path: '/product-detail/10',
      query: { review: '1' }
    })
  })

  it('falls back to Review button when the check API fails', async () => {
    currentOrder = baseOrder({})
    getProductReviews.mockRejectedValue(new Error('network'))
    const wrapper = await mountView()
    expect(countLabel(wrapper, 'order.reviewed')).toBe(0)
    expect(countLabel(wrapper, 'order.reviewProduct')).toBe(1)
  })

  it('uses black primary color for breadcrumb link', async () => {
    currentOrder = baseOrder({})
    getProductReviews.mockResolvedValue({ summary: {}, reviews: [] })
    const wrapper = await mountView()
    const backLink = wrapper.find('a[href="/orders"]')
    expect(backLink.classes()).toContain('text-black')
    expect(backLink.classes()).not.toContain('text-[#666666]')
  })
})
```

- [ ] **Step 3: Chạy test để thấy FAIL**

Run (từ thư mục `frontend`): `npx vitest run src/views/__tests__/OrderDetail.spec.ts`
Expected: FAIL — nút không tồn tại, `getProductReviews` chưa được gọi (các assertion `countLabel(...) > 0` và `toHaveBeenCalledTimes(2)` fail).

- [ ] **Step 4: Implement OrderDetail.vue**

4a. Script — thêm import (giữ các import hiện có):

```ts
import { useRouter } from 'vue-router'
import { reviewService } from '@/services/review.service'
```

Sửa dòng `const route = useRoute()` hiện có thành:

```ts
const route = useRoute()
const router = useRouter()
```

4b. Script — thêm state + logic sau khối `editForm` (trước `startEdit`):

```ts
const reviewedProductIds = ref<Set<number>>(new Set())

const showReviewButtons = computed(() => order.value?.status === 'COMPLETED')

const loadReviewedProducts = async () => {
  if (!order.value || !showReviewButtons.value) return
  const results = await Promise.allSettled(
    order.value.items.map(async (item) => ({
      productId: item.productId,
      reviewed: (await reviewService.getProductReviews(item.productId)).reviews.some((r) => r.isMine)
    }))
  )
  const next = new Set<number>()
  for (const r of results) {
    if (r.status === 'fulfilled' && r.value.reviewed) {
      next.add(r.value.productId)
    }
  }
  reviewedProductIds.value = next
}

const goToReview = (productId: number) => {
  router.push({ path: `/product-detail/${productId}`, query: { review: '1' } })
}
```

4c. Script — trong `onMounted`, sau `await orderStore.fetchOrderById(orderId.value)` thêm `await loadReviewedProducts()`; kết quả:

```ts
  try {
    await orderStore.fetchOrderById(orderId.value)
    await loadReviewedProducts()
  } catch (error) {
    console.error('Failed to fetch order:', error)
  }
```

4d. Script — trong `confirmReceivedOrder`, sau `await orderStore.fetchOrderById(orderId.value)` thêm `await loadReviewedProducts()`:

```ts
    await orderService.confirmReceived(orderId.value)
    alert(t('order.receivedThanks'))
    await orderStore.fetchOrderById(orderId.value)
    await loadReviewedProducts()
```

4e. Template — mũi tên breadcrumb (khối `<router-link to="/orders">`, dòng ~11): đổi class từ `text-[#666666] hover:text-black` thành:

```html
              class="flex items-center gap-2 text-black hover:text-[#5E5F5C] transition-colors"
```

4f. Template — trong vòng lặp item, bên trong `<div class="flex-1 flex flex-col justify-between">`, SAU khối `<div class="flex justify-end mt-2">...</div>` (line total), thêm:

```html
                    <div
                      v-if="showReviewButtons"
                      class="flex justify-end mt-3"
                    >
                      <button
                        v-if="reviewedProductIds.has(item.productId)"
                        class="flex items-center gap-2 px-4 py-1.5 border border-black text-black rounded font-geist text-[12px] font-medium hover:bg-gray-50 transition-colors"
                        @click="goToReview(item.productId)"
                      >
                        <svg
                          class="w-[14px] h-[14px]"
                          viewBox="0 0 24 24"
                          fill="none"
                          stroke="currentColor"
                          stroke-width="2"
                        >
                          <polyline points="20 6 9 17 4 12" />
                        </svg>
                        {{ t('order.reviewed') }}
                      </button>
                      <button
                        v-else
                        class="flex items-center gap-2 px-4 py-1.5 bg-black text-white rounded font-geist text-[12px] font-medium hover:bg-gray-900 transition-colors"
                        @click="goToReview(item.productId)"
                      >
                        <svg
                          class="w-[14px] h-[14px]"
                          viewBox="0 0 24 24"
                          fill="currentColor"
                        >
                          <path d="M11.48 3.499a.562.562 0 011.04 0l2.125 5.111a.563.563 0 00.475.345l5.518.442c.499.04.701.663.321.988l-4.204 3.602a.563.563 0 00-.182.557l1.285 5.385a.562.562 0 01-.84.61l-4.725-2.885a.562.562 0 00-.586 0L6.982 20.54a.562.562 0 01-.84-.61l1.285-5.386a.562.562 0 00-.182-.557l-4.204-3.602a.563.563 0 01.321-.988l5.518-.442a.563.563 0 00.475-.345L11.48 3.5z" />
                        </svg>
                        {{ t('order.reviewProduct') }}
                      </button>
                    </div>
```

- [ ] **Step 5: Chạy test để thấy PASS**

Run (từ thư mục `frontend`): `npx vitest run src/views/__tests__/OrderDetail.spec.ts`
Expected: PASS 6/6.

- [ ] **Step 6: Chạy toàn bộ suite frontend để chắc không regress**

Run (từ thư mục `frontend`): `npm run test`
Expected: tất cả pass (baseline 58 test + 6 test mới).

- [ ] **Step 7: Commit**

```bash
git add frontend/src/views/OrderDetail.vue frontend/src/i18n/locales/vi.json frontend/src/i18n/locales/en.json frontend/src/views/__tests__/OrderDetail.spec.ts
git commit -m "feat: add per-item review buttons on completed orders"
```

---

### Task 2: Anchor reviews + auto-scroll trên ProductDetail

**Files:**
- Modify: `frontend/src/views/ProductDetail.vue` (template ~dòng 379-383; script import dòng ~458; `onMounted` dòng ~592-614)

**Interfaces:**
- Consumes: URL query `?review=1` do Task 1 tạo khi điều hướng.
- Produces: phần tử DOM `id="reviews"` bọc quanh `<ProductReviews>` (đích scroll).

- [ ] **Step 1: Bọc ProductReviews bằng anchor div**

Thay khối hiện tại:

```html
      <!-- Product Reviews -->
      <ProductReviews
        v-if="product"
        :product-id="product.id"
      />
```

bằng (chuỗi `v-if` → `v-else-if="loading"` ở khối Loading State phía dưới VẪN hoạt động vì wrapper là sibling kế tiếp):

```html
      <!-- Product Reviews -->
      <div
        v-if="product"
        id="reviews"
        class="scroll-mt-[120px]"
      >
        <ProductReviews :product-id="product.id" />
      </div>
```

- [ ] **Step 2: Thêm nextTick vào import vue**

Dòng ~458: `import { ref, computed, watch, onMounted } from 'vue'` →

```ts
import { ref, computed, watch, onMounted, nextTick } from 'vue'
```

- [ ] **Step 3: Auto-scroll cuối onMounted**

Thêm vào CUỐI thân `onMounted` (sau khối `finally { loading.value = false }`, vẫn trong ngoặc nhọn của onMounted):

```ts
  if (route.query.review === '1') {
    await nextTick()
    document.getElementById('reviews')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }
```

- [ ] **Step 4: Kiểm tra compile + suite**

Run (từ thư mục `frontend`): `npm run test && npm run type-check`
Expected: toàn bộ test pass; type-check chỉ còn đúng 2 lỗi pre-existing (`AdminProducts.vue` TS6133, `AdminUsers.vue` TS2353) — không thêm lỗi nào.

- [ ] **Step 5: Commit**

```bash
git add frontend/src/views/ProductDetail.vue
git commit -m "feat: auto-scroll to reviews section via review=1 query"
```

---

### Task 3: Verification + smoke checklist

**Files:**
- Không sửa file nào (chỉ chạy verify). Nếu phát hiện bug → quay lại task tương ứng sửa.

- [ ] **Step 1: Suite đầy đủ**

Run (từ thư mục `frontend`): `npm run test`
Expected: tất cả pass (64+ tests).

Run (từ thư mục `frontend`): `npm run type-check`
Expected: CHỈ 2 lỗi pre-existing (AdminProducts TS6133, AdminUsers TS2353).

Run (từ thư mục gốc repo): `npx eslint "frontend/src/views/OrderDetail.vue" "frontend/src/views/ProductDetail.vue" "frontend/src/views/__tests__/OrderDetail.spec.ts"`
Expected: 0 errors (warnings format SVG nếu có thì fix bằng `npx eslint <file> --fix` scoped rồi commit `style:` riêng).

- [ ] **Step 2: Smoke test thủ công (cần app chạy + DB)** — bàn giao user:

1. Mở đơn `DELIVERED` → không có nút review.
2. Bấm "Đã nhận được hàng" → confirm → đơn `COMPLETED` → nút "Đánh giá sản phẩm" xuất hiện dưới từng SP.
3. Bấm nút → sang trang chi tiết SP, trang tự cuộn xuống mục đánh giá.
4. Viết review, quay lại đơn → nút đổi thành "Đã đánh giá".
5. Bấm "Đã đánh giá" → vẫn sang trang SP (thấy review cũ + nút sửa).
6. Đơn COMPLETED cũ (trước khi có feature) → cũng hiện nút đúng trạng thái.
