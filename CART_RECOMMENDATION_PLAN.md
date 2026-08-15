# PLAN — Cart Recommendation ("Có thể bạn sẽ thích")

## 1. Mục tiêu

Phát triển mục **"Có thể bạn sẽ thích"** bên trong trang giỏ hàng.

Yêu cầu chính:

- Đề xuất sản phẩm liên quan đến các sản phẩm đang có trong giỏ.
- Không đề xuất sản phẩm đã có trong giỏ.
- Không đề xuất sản phẩm inactive hoặc hết hàng.
- Recommendation không được làm chậm hoặc làm lỗi luồng Cart/Checkout.
- V1 dùng rule-based scoring, **không dùng AI/ML**.
- Tận dụng `ProductCard.vue` hiện có để hiển thị sản phẩm.
- Chỉ gọi lại recommendation khi **tập productId trong giỏ thay đổi**, không gọi lại khi chỉ tăng/giảm quantity.

---

## 2. Phạm vi V1

### Input

Danh sách `productId` hiện có trong cart.

Ví dụ:

```json
{
  "productIds": [12, 16, 21],
  "limit": 8
}
```

### Output

Danh sách tối đa 8 sản phẩm recommendation dạng lightweight DTO.

Ví dụ:

```json
[
  {
    "id": 25,
    "name": "Nike Pegasus",
    "price": 2400000,
    "imageUrl": "...",
    "sport": "Running",
    "brand": "Nike",
    "category": "Shoes"
  }
]
```

### Ranking V1

Ưu tiên:

```text
Cùng sport       +4
Cùng category    +3
Cùng brand       +2
Giá tương đồng   +1
Độ phổ biến      +1
```

Thứ tự quan trọng:

```text
sport > category > brand > price > popularity
```

Sau khi tính điểm:

```text
Candidates
  -> loại productId đang có trong cart
  -> loại inactive
  -> loại hết hàng
  -> loại duplicate
  -> sort score DESC
  -> LIMIT 8
```

---

# 3. Thứ tự triển khai

## PHASE 1 — Stabilize Cart

**Mục tiêu:** sửa các vấn đề hiện tại của Cart trước khi thêm recommendation.

### Task 1.1 — Fix CartItem ID / Variant ID

Kiểm tra luồng authenticated cart.

Backend:

```text
PUT    /api/v1/cart/items/{id}
DELETE /api/v1/cart/items/{id}
```

`{id}` là **CartItem.id**.

Hiện tại `Cart.vue` đang có khả năng truyền `item.variantId` khi update/remove.

Cần sửa để:

- Authenticated cart: update/remove bằng `CartItem.id`.
- Guest cart: localStorage vẫn có thể update/remove bằng `variantId`.
- Không thay đổi contract backend nếu không cần thiết.

### Task 1.2 — Chuẩn hóa cart item data

Đảm bảo cart item luôn phân biệt rõ:

```text
id        = CartItem ID
productId = Product ID
variantId = ProductVariant ID
```

Guest cart cần lưu `productId` đầy đủ để dùng làm input cho recommendation.

### Task 1.3 — Cleanup

Xóa:

- debug `console.log`
- `watchEffect` chỉ dùng debug
- unreachable return
- code thừa liên quan Cart

### Acceptance Criteria — Phase 1

- Update quantity authenticated cart hoạt động đúng.
- Remove authenticated cart hoạt động đúng.
- Guest cart vẫn hoạt động.
- `productId` có sẵn để tạo recommendation request.
- Không có regression trong Cart/Checkout.

---

## PHASE 2 — Backend Recommendation API

**Mục tiêu:** tạo API recommendation độc lập, nhẹ và dễ mở rộng.

### Task 2.1 — Tạo DTO riêng

Tạo DTO dạng:

```text
RecommendationProductDto
```

Chỉ trả các field ProductCard cần, ví dụ:

```text
id
name
price
imageUrl
sport
brand
category
```

Có thể thêm:

```text
inStock
```

nếu frontend thực sự cần.

**Không dùng full `ProductDto` cho recommendation.**

Lý do:

`ProductRestController.toProductDto()` hiện load thêm images và variants cho từng product, dễ gây nhiều query khi trả 6–8 recommendation cards.

### Task 2.2 — Tạo request DTO

Ví dụ:

```text
CartRecommendationRequest
- List<Long> productIds
- Integer limit
```

Validation:

- `productIds` null/empty -> trả danh sách rỗng.
- Remove duplicate product IDs.
- Limit mặc định = 8.
- Có giới hạn max, ví dụ 12 hoặc 20.

### Task 2.3 — Tạo endpoint

Đề xuất:

```http
POST /api/v1/products/recommendations/cart
```

Body:

```json
{
  "productIds": [12, 16, 21],
  "limit": 8
}
```

Lý do dùng POST:

- Guest cart và authenticated cart dùng chung API.
- Không phụ thuộc server-side cart.
- Tránh query string dài khi cart có nhiều sản phẩm.

### Acceptance Criteria — Phase 2

- Endpoint nhận danh sách productId.
- Empty cart trả `[]`.
- API không phụ thuộc việc user đã login.
- Response chỉ chứa lightweight DTO.
- API không ảnh hưởng API Product hiện tại.

---

## PHASE 3 — Recommendation Engine V1

**Mục tiêu:** triển khai recommendation rule-based.

### Task 3.1 — Tạo `RecommendationService`

Luồng:

```text
productIds từ cart
    ↓
load thông tin sản phẩm trong cart
    ↓
tìm candidate products
    ↓
filter
    ↓
score
    ↓
sort
    ↓
top N
    ↓
RecommendationProductDto
```

### Task 3.2 — Candidate filtering

Candidate bắt buộc:

- `active = true`
- Không thuộc `productIds` trong cart.
- Có ít nhất một `ProductVariant` còn hàng.
- Không duplicate.

Nên filter càng nhiều càng tốt tại DB.

Đặc biệt stock nên dùng kiểu:

```sql
EXISTS (
  SELECT 1
  FROM ProductVariant ...
  WHERE product_id = ...
  AND quantity > 0
)
```

Tránh load toàn bộ variants chỉ để kiểm tra stock.

### Task 3.3 — Scoring

Một product candidate có thể được so với nhiều product trong cart.

Gợi ý:

```text
sameSport       = +4
sameCategory    = +3
sameBrand       = +2
similarPrice    = +1
popularity      = +1
```

Không cộng điểm duplicate vô hạn nếu nhiều cart items giống nhau.

Ưu tiên dựa trên **unique productId**.

### Task 3.4 — Price similarity

V1 chỉ cần rule đơn giản.

Ví dụ:

```text
candidate price nằm trong khoảng ±30% giá tham chiếu -> +1
```

Không cần thuật toán phức tạp.

### Task 3.5 — Fallback

Nếu sản phẩm liên quan không đủ `limit`:

1. Same sport/category.
2. Same brand.
3. Popular/best-selling products còn hàng.
4. New products còn hàng.

Vẫn phải loại product đang trong cart.

### Acceptance Criteria — Phase 3

- Recommendation liên quan hợp lý.
- Không trả sản phẩm đang nằm trong cart.
- Không trả inactive/out-of-stock.
- Không duplicate.
- Tối đa theo `limit`.
- Kết quả deterministic khi score giống nhau bằng tie-break rõ ràng, ví dụ popularity/viewCount/id.

---

## PHASE 4 — Frontend Integration

**Mục tiêu:** kết nối API với Cart.

### Task 4.1 — Product service

Trong:

```text
frontend/src/services/product.service.ts
```

Thêm:

```ts
getCartRecommendations(productIds: number[], limit = 8)
```

### Task 4.2 — Recommendation state trong Cart

Trong `Cart.vue`, tạo state riêng:

```text
recommendations
recommendationLoading
recommendationError
```

Recommendation phải load độc lập với Cart.

Nếu recommendation lỗi:

- Cart vẫn render.
- Checkout vẫn hoạt động.
- Không hiển thị lỗi phá UX.
- Có thể ẩn section recommendation.

### Task 4.3 — Chỉ refetch khi product set thay đổi

Tạo key từ unique product IDs:

```ts
const recommendationKey = computed(() =>
  [...new Set(cartItems.value.map(item => item.productId))]
    .filter(Boolean)
    .sort((a, b) => a - b)
    .join(',')
)
```

Watch `recommendationKey`.

**Không watch quantity.**

Không gọi lại API khi user chỉ:

```text
+ quantity
- quantity
```

Chỉ gọi lại khi:

```text
add product mới
remove product
load cart
merge guest cart làm thay đổi product set
```

### Task 4.4 — Render UI

Section:

```text
Có thể bạn sẽ thích
```

đặt **bên dưới toàn bộ khu vực Cart + Order Summary**.

Tận dụng:

```text
frontend/src/components/ui/ProductCard.vue
```

Không làm quick-add trong V1 vì product có variant/size.

Click ProductCard -> Product Detail để user chọn size.

### Task 4.5 — Responsive

Desktop:

```text
4 cards / row
```

Tablet:

```text
2–3 cards / row
```

Mobile:

```text
horizontal scroll hoặc 2 cards / row
```

Đồng thời tránh width cứng gây overflow trong Cart.

Ưu tiên:

```text
w-full
max-w-[1152px]
```

thay vì chỉ:

```text
w-[1152px]
```

### Acceptance Criteria — Phase 4

- Cart có section recommendation.
- Tối đa 8 products.
- ProductCard click đúng product detail.
- Quantity change không gọi lại recommendation API.
- Add/remove product có gọi lại.
- API recommendation fail không ảnh hưởng cart.
- Responsive không overflow.

---

## PHASE 5 — Performance & Query Review

**Mục tiêu:** đảm bảo tính năng không tạo N+1 hoặc query quá nặng.

### Task 5.1 — Kiểm tra SQL/query count

Không reuse logic mapping full `ProductDto` hiện tại nếu nó gây:

```text
1 query products
+ N query images
+ N query variants
```

Recommendation endpoint cần query gọn.

### Task 5.2 — Lightweight query/projection

Ưu tiên một trong:

1. JPA projection.
2. Custom DTO query.
3. Entity query nhưng batch fetch dữ liệu cần thiết.

Không fetch collections không cần dùng.

### Task 5.3 — Limit candidates

Không score toàn bộ database nếu không cần.

Có thể lấy candidate pool giới hạn dựa trên:

```text
sport/category/brand
```

sau đó score.

### Task 5.4 — Cache

**Không thêm cache ở V1 nếu chưa có vấn đề performance thực tế.**

Chỉ cân nhắc cache khi:

- Query recommendation chậm.
- Traffic đủ lớn.
- Profiling chứng minh cần cache.

### Acceptance Criteria — Phase 5

- Không có Hibernate pagination warning mới.
- Không JOIN FETCH collection + pagination.
- Không N+1 nghiêm trọng.
- Recommendation response nhanh và ổn định.
- Không làm chậm cart rendering.

---

# 4. Test Plan

## Backend

Test ít nhất:

```text
1. productIds = []
2. một product trong cart
3. nhiều product trong cart
4. duplicate productIds
5. product inactive
6. candidate inactive
7. candidate hết stock
8. candidate đã có trong cart
9. limit = 8
10. recommendation không đủ 8
11. invalid productId
12. nhiều candidate cùng score
```

## Frontend

Test:

```text
1. Guest cart
2. Authenticated cart
3. Empty cart
4. Cart 1 product
5. Cart nhiều products
6. Increase quantity
7. Decrease quantity
8. Remove product
9. Add product
10. Recommendation API error
11. Loading state
12. Desktop
13. Tablet
14. Mobile
```

### Quan trọng

Dùng Network tab xác nhận:

```text
quantity 1 -> 2
```

**không tạo request recommendation mới.**

---

# 5. Không làm trong V1

Không triển khai các phần sau trừ khi thực sự cần để hoàn thành V1:

- AI/ML recommendation.
- Vector database.
- Embedding.
- Personalized recommendation theo user profile.
- Redis chỉ để cache recommendation.
- Background job.
- Quick-add trực tiếp từ ProductCard.
- Tracking/analytics phức tạp.
- Schema mới chỉ để phục vụ recommendation V1.

Giữ implementation đơn giản và đúng kiến trúc hiện tại.

---

# 6. PHASE 6 — V2: Co-purchase Recommendation

**Chỉ thực hiện sau khi V1 hoàn thành và ổn định.**

Có thể tận dụng `OrderDetail` hiện tại.

Ý tưởng:

```text
Cart có Product A
    ↓
tìm các COMPLETED orders từng chứa Product A
    ↓
lấy các Product B khác trong cùng order
    ↓
đếm số lần A và B được mua cùng nhau
    ↓
coPurchaseScore
```

Chỉ dùng đơn hàng thành công/completed.

Không dùng:

```text
cancelled
pending
failed
```

Có thể kết hợp score:

```text
coPurchase      * 5
sameSport       * 4
sameCategory    * 3
sameBrand       * 2
priceSimilarity * 1
popularity      * 1
```

V2 không được làm thay đổi API contract frontend nếu không cần.

---

# 7. Thứ tự file dự kiến thay đổi

Backend có thể cần:

```text
src/main/java/.../dto/RecommendationProductDto.java
src/main/java/.../dto/CartRecommendationRequest.java
src/main/java/.../service/RecommendationService.java
src/main/java/.../repository/ProductRepository.java
src/main/java/.../controller/ProductRestController.java
```

Có thể cần repository khác nếu query stock/candidate tách riêng.

Frontend:

```text
frontend/src/services/product.service.ts
frontend/src/views/Cart.vue
```

Có thể tái sử dụng:

```text
frontend/src/components/ui/ProductCard.vue
```

Chỉ sửa `ProductCard.vue` nếu thật sự cần.

Ngoài ra Phase 1 có thể sửa:

```text
frontend/src/stores/cart.store.ts
frontend/src/services/cart.service.ts
```

---

# 8. Execution Checklist

Opencode phải làm đúng thứ tự dưới đây.

```text
[ ] Phase 1 — Stabilize Cart
    [ ] Fix authenticated CartItem.id vs variantId
    [ ] Verify guest cart
    [ ] Ensure productId exists
    [ ] Cleanup debug code

[ ] Phase 2 — Backend API
    [ ] RecommendationProductDto
    [ ] CartRecommendationRequest
    [ ] POST /products/recommendations/cart

[ ] Phase 3 — Recommendation Engine
    [ ] Candidate query
    [ ] Active filter
    [ ] Stock filter
    [ ] Exclude cart products
    [ ] Scoring
    [ ] Ranking
    [ ] Limit
    [ ] Fallback

[ ] Phase 4 — Frontend
    [ ] product.service API
    [ ] Recommendation state
    [ ] recommendationKey
    [ ] Render ProductCard
    [ ] Loading/fallback
    [ ] Responsive

[ ] Phase 5 — Performance
    [ ] Review query count
    [ ] Check N+1
    [ ] Check Hibernate warnings
    [ ] Verify quantity does not refetch

[ ] Tests
    [ ] Backend tests
    [ ] Frontend/manual tests
    [ ] Guest cart
    [ ] Authenticated cart
    [ ] Mobile/desktop
```

---

# 9. Progress Report Format

Sau **mỗi Phase**, Opencode phải báo cáo tiến độ trước khi chuyển phase tiếp theo.

Dùng format:

```md
## Progress Report — Phase X

### Status
COMPLETED | PARTIAL | BLOCKED

### Completed
- ...
- ...

### Files changed
- `path/to/file`
- `path/to/file`

### Key implementation
- ...

### Tests performed
- ...
- ...

### Issues / Risks
- None
hoặc
- ...

### Remaining
- ...
```

Nếu gặp bug/blocker, **không tự mở rộng scope lớn**.

Báo rõ:

```text
BLOCKED
Reason:
Affected task:
Suggested fix:
```

---

# 10. Final Report

Khi hoàn thành toàn bộ V1, báo cáo:

```md
# Final Report — Cart Recommendation V1

## Result
COMPLETED / PARTIAL

## Implemented
- ...

## Files changed
- ...

## API
- Method:
- Endpoint:
- Request:
- Response:

## Recommendation logic
- ...

## Performance considerations
- ...

## Tests
- ...

## Known limitations
- ...

## Suggested V2
- Co-purchase recommendation using completed orders.
```

---

# 11. Definition of Done

Feature chỉ được xem là hoàn thành khi:

- Cart update/remove hoạt động đúng cho cả guest và authenticated user.
- Recommendation API hoạt động.
- Recommendation dựa trên products hiện tại trong cart.
- Không đề xuất item đã có trong cart.
- Không đề xuất inactive/out-of-stock.
- UI hiển thị bằng ProductCard.
- Quantity change không refetch recommendation.
- Recommendation failure không ảnh hưởng checkout.
- Không tạo N+1 nghiêm trọng.
- Không xuất hiện Hibernate warning mới liên quan pagination/fetch.
- Responsive hoạt động.
- Test các flow chính thành công.
- Có Final Report.

---

## Nguyên tắc triển khai

**Ưu tiên sửa đúng và đơn giản hơn over-engineering.**

Nếu kiến trúc thực tế trong repository khác với plan:

1. Kiểm tra code hiện tại.
2. Giữ behavior hiện tại nếu hợp lý.
3. Điều chỉnh implementation cho phù hợp repository.
4. Không đổi public API không liên quan.
5. Không refactor diện rộng ngoài scope.
6. Báo rõ thay đổi so với plan trong Progress Report.

Thực hiện lần lượt từng phase và luôn báo cáo tiến độ.
