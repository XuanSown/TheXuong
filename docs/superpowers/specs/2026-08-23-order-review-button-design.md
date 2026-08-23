# Spec: Nút "Đánh giá sản phẩm" trên trang chi tiết đơn hàng

- Ngày: 2026-08-23
- Phạm vi: Frontend only (không đổi backend)
- Trang liên quan: `frontend/src/views/OrderDetail.vue`, `frontend/src/views/ProductDetail.vue`, i18n locales

## 1. Vấn đề

Trang `/order/:id` có nút "Đã nhận được hàng" (hiện khi status `DELIVERED`). Sau khi bấm, đơn chuyển sang `COMPLETED` nhưng không có gì dẫn người dùng đến nơi đánh giá sản phẩm.

## 2. Yêu cầu đã chốt (qua clarifying questions)

1. **Đích điều hướng:** trang chi tiết sản phẩm `/product-detail/:productId`, tự cuộn xuống mục "Đánh giá" (form viết review đã có sẵn trong `ProductReviews.vue`).
2. **Vị trí nút:** 1 nút cho **mỗi sản phẩm** trong đơn (vì backend chỉ cho review 1 lần/sản phẩm/người dùng).
3. **Sản phẩm đã review:** check qua API và **đổi trạng thái nút** thành "Đã đánh giá" (nút vẫn bấm được để xem/sửa review cũ — logic `myReview` sẵn có ở ProductReviews sẽ hiển thị review của user kèm nút sửa).
4. **Rule màu icon (áp dụng cho 2 trang đang làm):** icon chính dùng **đen**, chỉ giữ xám nhạt cho **trạng thái phụ** (sao rỗng, placeholder ảnh, hover).

## 3. Thiết kế

### 3.1 OrderDetail.vue

**Hiển thị nút:**
- Khi `order.status === 'COMPLETED'`, mỗi item trong danh sách sản phẩm hiển thị nút dưới khối thông tin sản phẩm:
  - Chưa review: nút nền đen, chữ trắng, icon sao trắng — nhãn `t('order.reviewProduct')` = "Đánh giá sản phẩm".
  - Đã review: nút viền đen, chữ đen, icon check đen — nhãn `t('order.reviewed')` = "Đã đánh giá". Vẫn clickable.
- Trạng thái khác (`DELIVERED`, `PENDING`, ...) → không hiện nút.

**Check đã review:**
- Khi order load xong với status `COMPLETED`, gọi song song `Promise.all(reviewService.getProductReviews(item.productId))` cho từng item.
- Xây map `reviewedProductIds: Set<number>` từ flag `isMine` trong response.
- Request lỗi (network/401...) → coi như chưa review, vẫn hiện nút "Đánh giá sản phẩm" (không block UI).
- N items = N request song song (chấp nhận theo quyết định của user; đơn thực tế thường 1–5 SP).

**Điều hướng:**
```ts
router.push({ path: `/product-detail/${item.productId}`, query: { review: '1' } })
```

### 3.2 ProductDetail.vue

- Bọc `<ProductReviews>` trong wrapper div: `id="reviews"` + `scroll-mt-[120px]` (chừa chiều cao navbar fixed).
- `onMounted`: nếu `route.query.review === '1'` → sau khi product render xong (watch product / nextTick) → `document.getElementById('reviews')?.scrollIntoView({ behavior: 'smooth', block: 'start' })`.
- Không scroll nếu query thiếu hoặc sai giá trị.
- Query param chỉ dùng 1 lần; không xoá query khỏi URL (YAGNI).

### 3.3 Rule icon đen/trắng (2 trang)

| Vị trí | Hiện tại | Sửa thành |
|---|---|---|
| Mũi tên breadcrumb (OrderDetail) | `text-[#666666] hover:text-black` | `text-black hover:text-[#5E5F5C]` |
| Nút "Đánh giá sản phẩm" (mới) | — | nền đen `bg-black text-white`, icon sao trắng |
| Nút "Đã đánh giá" (mới) | — | viền đen `border border-black text-black`, icon check đen |
| Placeholder ảnh SP (OrderDetail) | `text-[#CFC4C5]` | giữ nguyên (trạng thái phụ hợp lệ) |
| StarRating sao đặc | `text-black` | giữ nguyên (đã đúng) |
| StarRating sao rỗng | `text-[#E5E7EB]` | giữ nguyên (trạng thái phụ hợp lệ) |
| Nút sửa/xóa review | `text-black underline` | giữ nguyên (đã đúng) |

### 3.4 i18n

Thêm key vào `vi.json` và `en.json` (nhóm `order`):
- `reviewProduct`: `"Đánh giá sản phẩm"` / `"Review product"`
- `reviewed`: `"Đã đánh giá"` / `"Reviewed"`

### 3.5 Không đổi

- Backend: không thêm/sửa endpoint nào (dùng lại `GET /reviews/product/{id}`).
- Logic confirm-received, alert "Cảm ơn", refetch order: giữ nguyên.
- Các trang khác: không đụng.

## 4. Testing

- File mới `frontend/src/views/__tests__/OrderDetail.spec.ts`:
  1. Order `COMPLETED` → render đúng số nút bằng số item, nhãn "Đánh giá sản phẩm".
  2. Item có `isMine=true` → nút đó nhãn "Đã đánh giá".
  3. Click nút → `router.push` tới `/product-detail/:id` kèm `query.review = '1'`.
  4. Order `DELIVERED` → không có nút review.
  5. API check lỗi → vẫn hiện nút "Đánh giá sản phẩm" (fallback).
- Test scroll ProductDetail: ưu tiên manual smoke test (jsdom không có layout engine thật); nếu viết thì assert `getElementById('reviews')` được gọi khi có query.

## 5. Rủi ro & lưu ý

- Timing scroll: product load async → phải scroll sau khi wrapper tồn tại trong DOM (dùng watch + nextTick).
- N request song song khi mở đơn COMPLETED nhiều SP — chấp nhận, có thể tối ưu sau bằng endpoint batch nếu cần.
