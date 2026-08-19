# Design: Tính năng Review sản phẩm

- **Ngày:** 2026-08-20
- **Loại:** Feature mới
- **Files chạm:** `ReviewRepository.java` (mới), `ReviewService.java` (mới), `ReviewRestController.java` (mới), `ReviewDto` (mới), `ReviewSummaryDto` (mới), `ReviewRequest` (mới), `ProductDetail.vue`, `ProductReviews.vue` (mới), `StarRating.vue` (mới), `vi.json`, `en.json`, `dbTheXuong.sql` (không đổi schema), tests

## Hiện trạng

- Bảng `Reviews` đã tồn tại trong `dbTheXuong.sql`: `id, user_id, product_id, rating INT CHECK (1-5), comment NVARCHAR(MAX), created_at DATETIME2`, có `UNIQUE (user_id, product_id)` và `IX_Reviews_productId`.
- Entity `Review.java` đã có, khớp schema.
- **Chưa có** `ReviewRepository`, `ReviewService`, `ReviewController` — feature chưa được implement.
- Rate limit đã reserve sẵn `/api/v1/reviews` → `RateLimitPlan.USER_REVIEW` (`RateLimitInterceptor.java:77`).
- `ProductDetail.vue` chưa có section đánh giá.
- i18n VI/EN đã setup đầy đủ (`frontend/src/i18n/locales/vi.json`, `en.json`).
- Phân quyền: `Users.role` là String (`CUSTOMER`/`ADMIN`/`BOTH`), authority = chính giá trị role (`SimpleGrantedAuthority(role)` — KHÔNG có prefix `ROLE_`), method security `@EnableMethodSecurity` đã bật → dùng `hasAnyAuthority('ADMIN','BOTH')`.
- `OrderDetail.productId` là cột Long thuần → dễ query kiểm tra "đã mua".

## Quyết định chính (đã chốt với user)

| # | Quyết định |
|---|---|
| 1 | Chỉ khách **đã mua** (đơn `COMPLETED` chứa sản phẩm đó) mới review được → badge "Đã mua hàng" |
| 2 | 1 user tối đa 1 review/sản phẩm (giữ UNIQUE constraint) |
| 3 | User được **sửa** review của mình, **không được xóa** |
| 4 | ADMIN/BOTH được **sửa và xóa** mọi review — **KHÔNG có tính năng reply** (đã bỏ) |
| 5 | Mặc định hiển thị **2 review mới nhất**, nút mở rộng xem tất cả |
| 6 | Nội dung review giới hạn hiển thị **150 ký tự**, quá thì cắt + "Xem thêm" |
| 7 | i18n **VI/EN** đầy đủ cho toàn bộ tính năng |
| 8 | Style **monochrome đen** (theme website): sao đen, badge đen, không màu vàng/xanh |

## Không thay đổi DB

Bảng `Reviews` giữ nguyên 100% — không migration, không cột mới (đã bỏ phương án reply self-reference). `dbTheXuong.sql` chỉ cần kiểm tra là đã có bảng (đã có).

## Backend

### `ReviewRepository` (mới)

```java
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);
    boolean existsByUserIdAndProductId(Long userId, Long productId);
    Optional<Review> findByIdAndUserId(Long id, Long userId);
    @Query("SELECT COUNT(r) FROM Review r WHERE r.product.id = :productId")
    long countByProductId(@Param("productId") Long productId);
    @Query("SELECT COALESCE(AVG(r.rating), 0) FROM Review r WHERE r.product.id = :productId")
    double avgRatingByProductId(@Param("productId") Long productId);
    @Query("SELECT r.rating, COUNT(r) FROM Review r WHERE r.product.id = :productId GROUP BY r.rating")
    List<Object[]> ratingDistribution(@Param("productId") Long productId);
}
```

Entity `Review` giữ nguyên, không có cột `parent_id`.

### `ReviewService` (mới)

- `getProductReviews(productId)`: trả `ReviewSummaryDto` (averageRating làm tròn 1 số lẻ, totalCount, distribution 5→1 map) + `List<ReviewDto>` (mới nhất trước).
- `createReview(user, productId, rating, comment)`:
  - Kiểm tra product tồn tại (404)
  - Kiểm tra đã mua: `OrderDetail` có `productId` khớp, thuộc order của user, `Order.status = COMPLETED` (403 nếu chưa)
  - Kiểm tra chưa review (409 nếu đã có)
- `updateReview(user, reviewId, rating, comment)`:
  - Chủ review (user.id == review.user.id) **hoặc** role ADMIN/BOTH
  - Người khác → 403
- `deleteReview(user, reviewId)`: chỉ ADMIN/BOTH (403 ngược lại). Hard delete.
- Kiểm tra quyền admin: `hasAnyAuthority('ADMIN','BOTH')` — làm 1 helper `isAdmin(User)`.

### `ReviewRestController` (mới, `/api/v1/reviews`)

| Method | Endpoint | Quyền | Mô tả |
|---|---|---|---|
| GET | `/product/{productId}` | Public | Summary + toàn bộ danh sách review (không phân trang — YAGNI) |
| POST | `` | Authenticated | Tạo review. Body: `{rating, comment}` |
| PUT | `/{id}` | Chủ review hoặc ADMIN/BOTH | Sửa review. Body: `{rating, comment}` |
| DELETE | `/{id}` | ADMIN/BOTH | Xóa review |

- `ReviewRequest`: `rating` 1-5 (`@Min(1) @Max(5)`), `comment` tối đa 1000 ký tự (`@Size(max = 1000)`), comment **tùy chọn** (review chỉ sao vẫn hợp lệ).
- `ReviewDto`: `id, rating, comment, createdAt, authorName, verifiedBuyer` (luôn true vì chỉ khách đã mua review được), `isMine` (user đang xem có phải chủ review không), `canModerate` (user đang xem có role ADMIN/BOTH không — để frontend quyết định hiện nút Sửa/Xóa). Với GET public: nếu request **chưa đăng nhập**, `isMine = canModerate = false`; nếu đã đăng nhập, tính theo user hiện tại.
- Rate limit `USER_REVIEW` đã được interceptor áp dụng tự động cho `/api/v1/reviews`.

### Kiểm tra "đã mua" — query

```java
// OrderDetailRepository (thêm method)
@Query("""
    SELECT COUNT(d) > 0 FROM OrderDetail d
    WHERE d.productId = :productId
      AND d.order.user.id = :userId
      AND d.order.status = com.example.thexuong.entity.OrderStatus.COMPLETED
""")
boolean hasCompletedPurchase(@Param("userId") Long userId, @Param("productId") Long productId);
```

## Frontend

### `StarRating.vue` (mới, component UI nhỏ)

- Display mode: vẽ 5 sao SVG đen (đầy = đen đặc, trống = chỉ viền đen), hỗ trợ hiển thị nửa sao.
- Interactive mode (cho form đánh giá): click chọn 1-5 sao, hover preview.
- Props: `modelValue`, `interactive`, `size`.

### `ProductReviews.vue` (mới, nhúng vào `ProductDetail.vue`)

Nằm dưới phần thông tin sản phẩm, full width của section. Gồm:

1. **Header tóm tắt:**
   - Số trung bình lớn (VD: 4.6), dãy sao đen, "(N đánh giá)"
   - Phân bố sao: 5★→1★, mỗi dòng: "5" + thanh ngang (width = %) + số lượng. Tất cả màu đen/xám.

2. **Danh sách review:**
   - Hiện 2 mới nhất; nút "Xem tất cả N đánh giá" / "Thu gọn" toggle.
   - Card review: avatar tròn (chữ cái đầu fullName), tên user, badge "Đã mua hàng" (đen), sao đen, thời gian tương đối ("3 ngày trước"/"2 weeks ago", tooltip = ngày đầy đủ).
   - Nội dung: cắt 150 ký tự + "Xem thêm"/"Thu gọn" (chỉ khi vượt 150).
   - Review chỉ sao không chữ vẫn hiển thị.

3. **Form đánh giá (user đã đăng nhập):**
   - Chưa mua → text "Bạn cần mua sản phẩm để đánh giá".
   - Đã mua, chưa review → StarRating interactive + textarea (max 1000, đếm ký tự còn lại) + nút Gửi.
   - Đã review → hiện review của mình + nút "Sửa đánh giá" (mở form đã điền sẵn, submit = PUT).

4. **Hành động admin (chỉ khi `canModerate`):** nút Sửa (modal form) + nút Xóa (modal xác nhận dùng `BaseModal` có sẵn).

### `ProductDetail.vue` (sửa)

- Fetch product có sẵn; thêm fetch review summary+list qua service mới.
- Nhúng `<ProductReviews :product-id="product.id" />` bên dưới section sản phẩm.

### `review.service.ts` (mới)

- `getProductReviews(productId)`, `createReview(productId, {rating, comment})`, `updateReview(id, {rating, comment})`, `deleteReview(id)` — dùng `http.ts` có sẵn.

### i18n

Thêm namespace `review.*` vào cả `vi.json` và `en.json`:
`reviews (N đánh giá), average, rating, writeReview, editReview, deleteReview, confirmDelete, reviewSubmitted, reviewUpdated, reviewDeleted, needToPurchase, verifiedBuyer, seeAllReviews, hideReviews, seeMore, seeLess, yourReview, submit, update, charactersLeft, star, stars, deleteConfirmMessage, noReviews, errorNotOwner, errorAlreadyReviewed`

### Màu sắc (theme đen)

- Sao: đen đặc khi đầy, viền đen khi trống.
- Badge "Đã mua hàng": viền đen + icon ✓ đen + chữ đen.
- Thời gian/giá trị text: `text-[#1A1C1C]` / xám `text-[#5E5F5C]` (đúng palette đang dùng trong ProductDetail).
- Không dùng vàng/xanh/đỏ cho badge.

## Xử lý lỗi (HTTP status)

| Tình huống | Status |
|---|---|
| Sản phẩm/review không tồn tại | 404 |
| Chưa đăng nhập khi tạo review | 401 |
| Chưa mua (hoặc đơn chưa COMPLETED) | 403 |
| Đã review sản phẩm này rồi | 409 |
| User thường sửa review không phải của mình | 403 |
| User thường xóa review | 403 |
| Rating ngoài 1-5 / comment > 1000 ký tự | 400 |

## Testing

- Backend `ReviewServiceTest` (JUnit, theo style `RecommendationServiceTest` có sẵn — Mockito + JUnit):
  - create: chưa mua → 403; đã mua COMPLETED → tạo thành công; review 2 lần → 409
  - update: chủ review sửa được; user khác → 403; admin sửa review bất kỳ được
  - delete: user thường → 403; admin → xóa thành công
- Frontend vitest: `truncateText` (150 ký tự + dấu "…"), format thời gian tương đối VI/EN, vài case `StarRating` nếu cần.

## Scope check

- Không làm: phân trang review, reply, review nhiều lần, trang admin quản lý review, soft delete review, ảnh/video trong review.