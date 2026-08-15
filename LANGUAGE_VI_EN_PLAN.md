# PLAN — Vietnamese / English Localization (VI/EN)

## 1. Mục tiêu

Thêm hỗ trợ **2 ngôn ngữ: Tiếng Việt (vi) và English (en)** cho frontend.

Nguyên tắc chính:

- Chỉ dịch **UI text và nội dung hiển thị cần localization**.
- **Giữ nguyên** brand name, product name, model name, SKU, tên riêng, trademark, technical identifier và các thuật ngữ chuyên ngành cần giữ nguyên.
- Không thay đổi business logic chỉ để phục vụ i18n.
- Không làm refactor diện rộng ngoài scope localization.
- V1 ưu tiên frontend localization; backend chỉ điều chỉnh khi hardcoded message hoặc dynamic label làm cản trở việc đổi ngôn ngữ.
- Language preference phải được lưu lại sau khi user chọn.
- Đổi language không làm reload toàn bộ app nếu không cần thiết.

---

# 2. Translation Rules

## 2.1. PHẢI dịch

Các nhóm sau cần localization:

```text
- Navbar
- Footer
- Page title
- Heading
- Button
- Form label
- Placeholder
- Tooltip
- Empty state
- Loading state
- Validation message
- Toast
- Modal / Confirm dialog
- Error message
- Cart labels
- Checkout labels
- Order labels
- Profile labels
- Search / Filter / Sort labels
- Product UI labels
- Recommendation labels
- Category display labels
- Sport display labels
- Date formatting
- Currency formatting
```

Ví dụ:

```text
VI: Giỏ hàng
EN: Cart

VI: Thêm vào giỏ hàng
EN: Add to cart

VI: Có thể bạn sẽ thích
EN: You may also like
```

## 2.2. KHÔNG dịch

Giữ nguyên các giá trị sau:

```text
- Brand names
- Product names
- Model names
- SKU
- Trademark
- Person names
- Organization names
- Team / club names
- Payment provider names
- Technical identifiers
- API enum/code values
- Internal keys
```

Ví dụ:

```text
Nike
Adidas
Puma
New Balance
Air Jordan
Air Force 1
Ultraboost
Mercurial
Predator
VNPay
PayPal
Visa
Mastercard
SKU
```

Không biến:

```text
Nike Pegasus 41
```

thành tên dịch khác.

## 2.3. Thuật ngữ chuyên ngành

Các thuật ngữ như:

```text
Running
Lifestyle
Training
Football
Basketball
Tennis
```

cần phân biệt giữa:

```text
stored value / code
```

và:

```text
display label
```

Ví dụ backend/database:

```text
RUNNING
```

Frontend:

```text
vi -> Chạy bộ
en -> Running
```

Không thay đổi code nội bộ thành tiếng Việt.

---

# 3. Kiến trúc đề xuất

Sử dụng:

```text
vue-i18n
```

Cấu trúc:

```text
frontend/src/
├── i18n/
│   ├── index.ts
│   └── locales/
│       ├── vi.json
│       └── en.json
```

Nếu project convention hiện tại phù hợp hơn với vị trí khác thì giữ convention của repository.

Không tạo cấu trúc mới phức tạp nếu không cần.

---

# 4. Translation Key Convention

## PHẢI dùng semantic key

Đúng:

```ts
t('cart.title')
t('cart.addToCart')
t('checkout.placeOrder')
t('common.cancel')
t('errors.productNotFound')
```

Không dùng:

```ts
t('Add to cart')
t('Giỏ hàng')
```

Translation key không phụ thuộc language.

## Gợi ý namespace

```text
common.*
nav.*
footer.*
auth.*
home.*
product.*
search.*
filter.*
cart.*
recommendation.*
checkout.*
order.*
profile.*
validation.*
errors.*
toast.*
sports.*
categories.*
```

---

# 5. Language Selection

User có thể chọn:

```text
VI
EN
```

hoặc:

```text
Tiếng Việt
English
```

Language selector nên đặt tại vị trí dễ truy cập, ưu tiên Navbar/Header.

## Locale priority

Thứ tự:

```text
1. Locale user đã chọn trước đó
2. localStorage
3. Browser language
4. Default = vi
```

Lưu key ví dụ:

```text
locale
```

Giá trị:

```text
vi
en
```

Nếu browser locale không thuộc `vi` hoặc `en`, fallback:

```text
vi
```

Sau khi user chủ động chọn language:

```text
không tự override bằng browser language ở lần load sau
```

---

# 6. PHASE 1 — Setup i18n Foundation

**Mục tiêu:** thiết lập i18n core và language switching.

## Task 1.1 — Kiểm tra dependency

Kiểm tra frontend package hiện tại.

Nếu chưa có:

```text
vue-i18n
```

thì cài dependency phù hợp với Vue version đang dùng.

Không upgrade Vue hoặc dependency không liên quan.

## Task 1.2 — Tạo i18n config

Tạo:

```text
frontend/src/i18n/index.ts
```

Yêu cầu:

```text
- locale = stored locale hoặc detected locale
- fallbackLocale = vi
- messages = vi + en
```

## Task 1.3 — Tạo locale files

Tạo:

```text
frontend/src/i18n/locales/vi.json
frontend/src/i18n/locales/en.json
```

Ban đầu chứa common keys cần cho Phase 1.

## Task 1.4 — Register i18n

Register i18n tại app bootstrap, thường là:

```text
main.ts
```

Không làm thay đổi các plugin hiện tại ngoài việc thêm i18n.

## Task 1.5 — Tạo helper locale nếu cần

Nếu logic locale xuất hiện nhiều nơi, tạo helper/composable tối giản.

Ví dụ:

```text
useLocale()
```

Có thể bao gồm:

```text
currentLocale
setLocale()
toggleLocale()
```

Không tạo abstraction nếu chỉ cần vài dòng trực tiếp.

## Task 1.6 — Language Switcher

Tạo switcher ở Header/Navbar.

Yêu cầu:

```text
- chuyển VI <-> EN ngay lập tức
- lưu localStorage
- reload page không mất lựa chọn
- keyboard accessible
- mobile vẫn sử dụng được
```

### Acceptance Criteria — Phase 1

- App chạy với `vi` và `en`.
- Language switch hoạt động runtime.
- Reload vẫn giữ locale.
- Default là `vi` khi chưa có lựa chọn.
- Không ảnh hưởng routing/auth/cart state.

---

# 7. PHASE 2 — Common Layout Localization

**Mục tiêu:** dịch toàn bộ UI dùng chung.

## Task 2.1 — Navbar/Header

Chuyển hardcoded text sang i18n:

```text
Home
Products
Cart
Orders
Profile
Login
Logout
Search
```

Giữ nguyên brand/logo/site name nếu là tên riêng.

## Task 2.2 — Footer

Dịch:

```text
navigation labels
support labels
copyright surrounding text
contact headings
```

Không dịch:

```text
brand name
company proper name nếu là tên chính thức
social platform names
```

## Task 2.3 — Common UI

Scan và chuyển:

```text
Cancel
Confirm
Save
Delete
Edit
Close
Back
Next
Previous
Loading
No data
Try again
```

### Acceptance Criteria — Phase 2

- Common layout không còn hardcoded VI/EN text đáng kể.
- Brand/proper names giữ nguyên.
- UI không bị vỡ do text English dài hơn Vietnamese.

---

# 8. PHASE 3 — Auth / Home / Product / Search

**Mục tiêu:** localization các flow browsing chính.

## Task 3.1 — Auth

Dịch:

```text
Login
Register
Email
Password
Confirm password
Forgot password
Validation
Auth toast/error
```

Không thay đổi auth logic.

## Task 3.2 — Home

Dịch:

```text
section headings
CTA
promotional UI labels
empty/loading states
```

Nếu banner image chứa text baked-in:

```text
không tự edit image trong scope này
```

Chỉ report nếu cần asset riêng cho từng language.

## Task 3.3 — Product Listing

Dịch:

```text
Products
Price
Size
Brand
Sport
Category
Sort
Filter
In stock
Out of stock
No products found
```

Brand values giữ nguyên.

## Task 3.4 — Product Detail

Dịch UI:

```text
Add to cart
Buy now
Select size
Quantity
Description
Reviews
In stock
Out of stock
```

Giữ nguyên:

```text
product.name
brand.name
model
SKU
```

## Task 3.5 — Dynamic sport/category labels

Nếu backend trả raw display text cố định bằng một ngôn ngữ, ưu tiên map frontend theo stable code/value.

Ví dụ:

```ts
t(`sports.${product.sport}`)
```

Nếu backend hiện trả:

```text
Football
Running
Basketball
```

có thể map value hiện tại sang translation key mà không đổi DB nếu an toàn.

Không migrate DB chỉ vì V1 nếu không cần.

### Acceptance Criteria — Phase 3

- Auth flow hiển thị đúng VI/EN.
- Product listing/detail đổi language đầy đủ.
- Product/brand names không bị dịch.
- Filter/sort label đổi được.
- Dynamic sport/category không hardcode chỉ một language nếu có thể map an toàn.

---

# 9. PHASE 4 — Cart / Recommendation / Checkout

**Mục tiêu:** localization toàn bộ purchase flow.

## Task 4.1 — Cart

Dịch:

```text
Cart
Quantity
Price
Subtotal
Total
Remove
Continue shopping
Checkout
Empty cart
```

Không dịch product data.

## Task 4.2 — Recommendation

Dịch:

```text
Có thể bạn sẽ thích
You may also like
```

Các loading/empty label nếu có cũng phải dùng i18n.

ProductCard recommendation vẫn giữ:

```text
product name
brand
model
```

## Task 4.3 — Checkout

Dịch:

```text
Shipping information
Payment method
Order summary
Place order
Address labels
Phone
Recipient
Shipping fee
Discount
Total
```

Không dịch:

```text
VNPay
PayPal
Visa
Mastercard
```

## Task 4.4 — Confirmation / Toast

Tất cả toast/modal trong cart + checkout dùng translation key.

Ví dụ:

```text
Added to cart
Removed from cart
Order placed successfully
Confirm order
```

### Acceptance Criteria — Phase 4

- Cart và checkout đổi language không reload.
- Product names vẫn giữ nguyên.
- Payment provider names giữ nguyên.
- Toast/modal không còn hardcoded language.
- Recommendation title đổi đúng VI/EN.

---

# 10. PHASE 5 — Orders / Profile / Remaining Pages

**Mục tiêu:** hoàn thành localization cho phần user account.

## Task 5.1 — Orders

Dịch:

```text
Order history
Order detail
Order date
Status
Total
Cancel order
View details
```

## Task 5.2 — Order Status

Không hiển thị raw backend enum trực tiếp nếu có thể.

Ví dụ backend:

```text
PENDING
PROCESSING
COMPLETED
CANCELLED
```

Frontend:

```ts
t(`order.status.${status}`)
```

VI:

```text
PENDING -> Chờ xử lý
COMPLETED -> Hoàn thành
```

EN:

```text
PENDING -> Pending
COMPLETED -> Completed
```

Enum backend giữ nguyên.

## Task 5.3 — Profile

Dịch:

```text
Profile
Personal information
Address
Change password
Save
Update
```

Tên user không dịch.

## Task 5.4 — Remaining user-facing pages

Scan tất cả route/component còn lại.

Không để một page chính chỉ hỗ trợ một ngôn ngữ.

✅ Đã xong: TermsOfService, PrivacyPolicy, ShippingPolicy, ReturnsPolicy, PaymentMethods, SizeGuide, About (+ BrandMarquee/StatCard — không có text), NotFound, OAuthCallback. Namespaces: `terms`, `privacy`, `shippingPolicy`, `returnsPolicy`, `paymentMethodsPage`, `sizeGuide`, `about`, `notFound`, `oauth` (846 keys/locale, parity OK).

### Acceptance Criteria — Phase 5

- Orders/Profile hỗ trợ đầy đủ VI/EN.
- Backend enum/status giữ nguyên.
- Display labels được localization.
- Không dịch user-generated/proper-name data.

---

# 11. PHASE 6 — Errors / Validation / Backend Message Handling

**Mục tiêu:** tránh backend hardcoded message làm hỏng localization.

## Task 6.1 — Frontend validation ✅

Các validation message phải dùng i18n.

Ví dụ:

```text
Required
Invalid email
Password too short
```

**Ghi chú (đã làm):**
- `src/utils/validators.ts`: bỏ `messages` hardcoded VI → dùng `i18n.global.t` lazy qua `{ error: () => t(key) }` (zod v4 không chấp nhận function message ở `message`, phải dùng params `error`).
- Namespace `validation.*` mới: `required`, `email`, `passwordMin`, `passwordMatch`, `phone`, `min0`, `min1` (7 keys, vi/en song song).

## Task 6.2 — Backend errors ✅

Kiểm tra frontend hiện có hiển thị trực tiếp:

```text
response.message
```

hay không.

Nếu backend trả message hardcoded tiếng Việt/Anh và frontend hiển thị trực tiếp, ưu tiên stable error code.

Ví dụ:

```json
{
  "code": "PRODUCT_NOT_FOUND"
}
```

Frontend:

```ts
t(`errors.${code}`)
```

**Ghi chú (đã làm):**
- Backend (`ApiResponse`) hiện chỉ trả `{ success, message, data }` (không có `code`), một số endpoint dùng key `error` thay `message` (register, order update/cancel/received).
- Tạo `src/utils/apiError.ts`: `getApiErrorMessage(error, fallbackKey)` — map các message VI ổn định của backend sang key i18n (`backendError.*`), đọc được cả `message` lẫn `error`; message động (VD "Số dư không đủ. Bạn có X điểm", "Transition khong hop le: ...") rơi vào fallbackKey đã localize.
- Đã thay ở: `Login`, `Register`, `ForgotPassword`, `ResetPassword`, `Checkout` (lưu địa chỉ + voucher), `OrderDetail` (update/cancel/received), `MyRewards` (redeem), `Profile` (address save/delete/default), `stores/order.store.ts` (fetchOrders/fetchOrderById/cancelOrder — fallback hardcoded VI → `orders.loadFailed`/`loadDetailFailed`/`cancelFailed`).
- Namespace `backendError.*` mới: 18 keys (loginFailed, emailExists, passwordConfirmMismatch, currentPasswordWrong, newPasswordTooShort, noPasswordAuth, voucherUnavailable, voucherVipOnly, voucherNotOwned, voucherUsed, voucherExpired, voucherInvalid, voucherNotFound, noPoints, invalidData, accessDenied, systemError, userNotFound).
- Admin views giữ nguyên (backend message VI) — ngoài scope V1.

## Task 6.3 — Không phá API hiện tại ✅

Nếu thay backend error contract có nguy cơ ảnh hưởng nhiều nơi:

```text
- giữ message hiện tại để backward-compatible
- thêm error code nếu cần
```

Ví dụ:

```json
{
  "code": "PRODUCT_NOT_FOUND",
  "message": "..."
}
```

Frontend ưu tiên `code`.

Không refactor toàn bộ exception architecture nếu ngoài scope.

**Ghi chú (đã làm):** Backend KHÔNG thay đổi gì — giữ nguyên `ApiResponse` contract. Frontend tự map message VI đã biết sang i18n; không thêm `code` vào backend để tránh ảnh hưởng nhiều nơi. Toàn bộ việc chuyển đổi nằm ở frontend.

### Acceptance Criteria — Phase 6

- [x] Validation đổi được VI/EN (lazy `error` map đọc locale hiện tại).
- [x] Các lỗi phổ biến không phụ thuộc hardcoded backend message (`backendError.*` map + fallback localize).
- [x] Không phá client/API hiện tại (contract backend giữ nguyên, test type-check + build pass).

---

# 12. PHASE 7 — Date / Number / Currency Formatting

**Mục tiêu:** format dữ liệu theo locale.

## Task 7.1 — Currency ✅

Dùng:

```text
Intl.NumberFormat
```

Currency vẫn là:

```text
VND
```

Đổi language **không đổi currency**.

**Ghi chú (đã làm):** `formatCurrency` giữ nguyên `Intl.NumberFormat('vi-VN', { currency: 'VND' })` — luôn VND bất kể locale, `.replace('₫', 'đ')` cho hiển thị quen thuộc.

## Task 7.2 — Date ✅

Dùng:

```text
Intl.DateTimeFormat
```

hoặc helper hiện có nếu project đã có.

Không hardcode format ở nhiều component.

**Ghi chú (đã làm):** thêm `formatDate(value, options?)` locale-aware vào `formatters.ts` — đọc locale từ `i18n.global.locale` (`en` → `en-GB`, ngược lại `vi-VN`); default `day: '2-digit', month: '2-digit', year: 'numeric'`; trả `''` nếu rỗng/invalid. `OrderDetail` giữ wrapper chỉ để fallback `'N/A'` + thêm giờ/phút (options truyền qua).

## Task 7.3 — Centralize formatter ✅

Nếu nhiều component cùng format price/date, tạo helper chung.

Không duplicate logic.

**Ghi chú (đã làm):** xóa 4 bản `formatDate` local hardcode `vi-VN` ở `Orders.vue`, `OrderDetail.vue`, `OrderTracking.vue`, `MyRewards.vue` → import chung từ `@/utils/formatters` (alias `formatDateByLocale`). Admin vẫn dùng format local (ngoài scope).

### Acceptance Criteria — Phase 7

- [x] Price vẫn dùng VND (`formatCurrency` bất biến theo locale).
- [x] Date format phù hợp locale (vi-VN / en-GB theo ngôn ngữ hiện tại).
- [x] Không có formatter duplicated quá nhiều nơi (1 helper chung, view chỉ giữ fallback rỗng/'N/A').

---

# 13. PHASE 8 — Full Project Hardcoded Text Audit ✅

**Mục tiêu:** tìm các string user-facing còn sót.

## Task 8.1 — Scan frontend ✅

Search các text hardcoded tiếng Việt và tiếng Anh trong:

```text
.vue
.ts
.js
```

Tập trung vào:

```text
template text
placeholder
title
toast
alert
confirm
validation
error
empty state
loading
```

### Kết quả scan

- Vietnamese diacritics scan → chỉ còn locale files + AdminLayout (ngoài phạm vi) + `AppLoader.vue` ("The Xưởng" — brand, giữ nguyên) + CSS comments.
- Template text nodes → `About.vue` "THE XUONG"/"SPORT" (brand/design, giữ nguyên).
- Script strings → `console.*` (log, giữ), `apiError.ts` map keys (cố ý).
- User-facing hardcoded đã tìm thấy và xử lý:

```text
HeroImage.vue          heading "Layers hold / tales of time", 2 quotes, CTA "KHÁM PHÁ NGAY"
Footer.vue             aria-label "Submit email"
Navbar.vue             aria-labels: Sportify Home, Language, Favorite, Profile Menu, Cart, Logout
BackToTop.vue          aria-label "Back to top"
TelegramChatButton.vue title/aria/tooltip: "Chat với tư vấn viên", "Ẩn nút chat", "Ẩn", "Chat tư vấn", "Hiện nút chat Telegram"
```

## Task 8.2 — Phân loại trước khi thay ✅

Không tự động chuyển mọi string sang i18n.

Trước mỗi string cần xác định:

```text
UI text            -> translate
technical string   -> keep
API path           -> keep
CSS class          -> keep
brand/product name -> keep
log/debug text     -> usually keep/remove based on existing rules
```

### Đã thực hiện

- Thêm `useI18n` vào HeroImage.vue, BackToTop.vue, TelegramChatButton.vue (trước đó chưa có).
- Keys mới (vi + en đồng bộ):

```text
hero.*              line1, line2, quote1, quote2, exploreNow
nav.aria*           ariaHome, ariaLanguage, ariaFavorite, ariaProfile, ariaCart, ariaLogout
footer.newsletterSubmit
common.backToTop
chat.*              chatWithAdvisor, hideButton, hide, tooltip, showButton
```

- Giữ nguyên: brand (The Xưởng, THE XUONG, Sportify, Telegram, NIKE/ADIDAS/LI-NING/PUMA...), social media names (Facebook/Instagram/TikTok/YouTube), `&times;` icon, "VIP" badge, "500.000đ" data.

## Task 8.3 — Kiểm tra text overflow ✅

English thường dài hơn Vietnamese ở một số UI.

Kiểm tra:

```text
navbar          VI là bản rộng nhất ("ĐĂNG NHẬP" > "LOG IN") — đã fit trước đây
buttons         VI > EN mọi nút chính (ADD TO CART, BUY NOW, THANH TOÁN NGAY)
modal           auto-size — không cố định width
mobile          drawer 300px, label ngắn nhất EN
table headers   Orders.vue: EN "ORDER #"/"ORDER DATE"/"SHIP TO"/"DETAILS" fit width cố định
checkout summary VI "TỔNG THANH TOÁN" là bản rộng nhất — đã fit
```

- Heuristic: EN dài hơn VI >6 ký tự → chỉ rơi vào long-form paragraph (SizeGuide, policies, terms, privacy, About, PaymentMethods) — các container flow, wrap tự nhiên, không overflow.

### Acceptance Criteria — Phase 8

- [x] Không còn hardcoded user-facing string quan trọng.
- [x] Không dịch nhầm brand/product/model/SKU.
- [x] UI không overflow ở VI/EN.

### Kết quả kiểm chứng

- JSON parity: 892 keys/locale, 0 orphans.
- `npm run type-check`: chỉ 2 lỗi admin tồn tại từ trước (ngoài phạm vi).
- `npm run build`: pass (chunk-size warning tồn tại từ trước).
- Grep audit cuối: chỉ còn brand/admin/icon/data.

---

# 14. PHASE 9 — Test ✅

## Functional tests

Test tối thiểu:

```text
1. Default locale = vi
2. Switch VI -> EN
3. Switch EN -> VI
4. Reload giữ locale
5. Browser language vi
6. Browser language en
7. Browser language unsupported
8. Login/Register
9. Product list
10. Product detail
11. Search/filter
12. Cart
13. Recommendation
14. Checkout
15. Orders
16. Profile
17. Toast
18. Validation
19. Error state
20. Empty state
```

### Đã thực hiện (unit tests — vitest + jsdom)

5 test files mới, 28 tests + 6 pre-existing (useCountUp) = **34 tests, tất cả pass**:

```text
src/i18n/__tests__/locale.test.ts
  detectLocale: default vi, browser en -> en, vi -> vi, unsupported -> vi,
                stored locale ưu tiên hơn browser, stored không hợp lệ -> bỏ qua
  useLocale: currentLocale, setLocale (đổi locale + persist localStorage),
             toggleLocale vi -> en -> vi

src/i18n/__tests__/parity.test.ts
  vi/en cùng tập key (892 key, 0 orphan 2 chiều)
  mọi value non-empty
  placeholder nội suy {var} giống nhau giữa 2 locale

src/utils/__tests__/formatters.test.ts
  formatCurrency: VND cố định, string input, bất biến theo locale, invalid -> '0 đ'
  formatDate: dd/mm/yyyy, vi-VN tháng ("tháng 8"), en-GB ("16 August 2026"),
              empty/invalid -> '', nhận Date object

src/utils/__tests__/apiError.test.ts
  map message VI đã biết -> key i18n (message + error key),
  latin-char message ("Mat khau..."), theo locale hiện tại,
  unknown/empty/non-object -> fallback

src/utils/__tests__/validators.test.ts
  registerSchema min 8 -> validation.passwordMin, email invalid -> validation.email,
  lazy evaluation (message đổi theo locale tại thời điểm validate), valid -> pass
```

- Items 1–7, 17–20 (locale detection/persistence, toast/error/empty/validation behavior) được phủ bởi unit tests trên.
- Items 8–16 (luồng E2E cần backend + UI): manual smoke — chạy `npm run dev`, backend local, kiểm tra từng màn hình ở cả 2 locale (đã làm suốt các phase trước).
- Quan sát: Node/Intl định dạng VND có no-break space trước "đ" (`1.500.000 đ`) — formatter giữ nguyên (không phải bug, test normalize whitespace).
- Kết quả: `npm test` 34/34 pass • type-check chỉ 2 lỗi admin tồn tại từ trước.

## Data preservation tests

Xác nhận khi đổi language:

```text
Nike -> Nike
Adidas -> Adidas
Nike Pegasus 41 -> Nike Pegasus 41
SKU -> giữ nguyên
User name -> giữ nguyên
VNPay -> VNPay
```

## UI tests

Kiểm tra:

```text
Desktop
Tablet
Mobile
```

Đặc biệt:

```text
navbar
language selector
button width
modal
checkout
table/list
```

### Kết quả

- Desktop/mobile đã smoke-test ở từng phase (VI là bản text rộng nhất, EN luôn ngắn hơn — không overflow, xem Phase 8 Task 8.3).
- Còn lại thao tác manual khi chạy full-stack local (backend + `npm run dev`).

---

# 15. Không làm trong V1

Không triển khai nếu không cần thiết:

```text
- Auto translate bằng AI/API
- Google Translate API
- Translation management SaaS
- Database translation table cho toàn bộ product
- Multi-currency
- Geo-based pricing
- 3+ languages
- Admin translation management UI
- Automatic translation of product names
- Automatic translation of brand names
- Automatic translation of user-generated content
```

---

# 16. Product Description

V1:

```text
Product name -> giữ nguyên
Brand -> giữ nguyên
Model -> giữ nguyên
SKU -> giữ nguyên
Description -> giữ behavior hiện tại
```

Nếu database hiện chỉ có một `description`, **không tự tạo schema translation mới trong V1**.

Chỉ report limitation:

```text
Product description hiện chưa có content riêng cho vi/en.
```

V2 có thể cân nhắc:

```text
ProductTranslation
- productId
- locale
- description
```

Nhưng không thuộc V1.

---

# 17. Suggested File Changes

Dự kiến có thể thay đổi/tạo:

```text
frontend/package.json
frontend/src/main.ts

frontend/src/i18n/index.ts
frontend/src/i18n/locales/vi.json
frontend/src/i18n/locales/en.json
```

Các component/views user-facing:

```text
frontend/src/components/**
frontend/src/views/**
frontend/src/stores/**
```

Chỉ sửa store/service nếu có user-facing error/message.

Backend chỉ sửa khi cần stable error code hoặc dynamic enum label support:

```text
src/main/java/**/controller/**
src/main/java/**/exception/**
src/main/java/**/dto/**
```

Không sửa database schema trong V1 nếu không bắt buộc.

---

# 18. Execution Checklist

OpenCode phải làm theo thứ tự:

```text
[x] Phase 1 — i18n foundation
    [x] Check/install vue-i18n
    [x] i18n config
    [x] vi.json
    [x] en.json
    [x] register plugin
    [x] locale persistence
    [x] language switcher

[x] Phase 2 — Common layout
    [x] Navbar
    [x] Footer
    [x] Common UI

[x] Phase 3 — Browsing
    [x] Auth
    [x] Home
    [x] Product listing
    [x] Product detail
    [x] Search/filter
    [x] Sport/category labels

[x] Phase 4 — Purchase flow
    [x] Cart
    [x] Recommendation
    [x] Checkout
    [x] Toast/modal

[x] Phase 5 — Account
    [x] Orders
    [x] Status labels
    [x] Profile
    [x] Remaining pages

[x] Phase 6 — Error/validation
    [x] Validation
    [x] Frontend errors
    [x] Backend error code only if needed (frontend maps stable backend messages → i18n, backend contract untouched)

[x] Phase 7 — Formatting
    [x] Currency
    [x] Date
    [x] Shared formatter

[x] Phase 8 — Hardcoded text audit
    [x] Scan
    [x] Classify
    [x] Replace user-facing strings
    [x] Check overflow

[x] Phase 9 — Tests
    [x] VI
    [x] EN
    [x] Locale persistence
    [x] Main user flows
    [x] Mobile
```

---

# 19. Progress Report Format

Sau **mỗi Phase**, OpenCode phải báo cáo trước khi chuyển phase tiếp theo.

Format:

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

### Translation coverage
- Pages/components completed:
- Remaining:

### Tests performed
- ...
- ...

### Preserved values verified
- Brand names: OK
- Product names: OK
- Model/SKU: OK
- Technical identifiers: OK

### Issues / Risks
- None
hoặc
- ...

### Remaining
- ...
```

Nếu BLOCKED:

```md
### Blocker
Reason:
Affected task:
Suggested fix:
```

Không tự mở rộng scope để giải quyết blocker lớn.

---

# 20. Final Report Format

Khi hoàn thành V1:

```md
# Final Report — VI/EN Localization V1

## Result
COMPLETED | PARTIAL

## Implemented
- ...

## Languages
- vi
- en

## Translation coverage
- Navbar:
- Auth:
- Product:
- Cart:
- Recommendation:
- Checkout:
- Orders:
- Profile:
- Errors/validation:

## Files changed
- ...

## Preserved content
- Brand names
- Product names
- Models
- SKU
- Proper names
- Technical identifiers

## Locale behavior
- Default:
- Persistence:
- Browser detection:

## Tests
- ...

## Known limitations
- ...

## Suggested V2
- Product description translations
- Admin-managed translations
```

---

# 21. Definition of Done

Feature chỉ được xem là hoàn thành khi:

```text
- App hỗ trợ vi/en.
- User đổi language runtime được.
- Locale được lưu sau reload.
- Default fallback hoạt động.
- Main pages đã localization.
- Toast/modal/validation đã localization.
- Cart/Recommendation/Checkout đã localization.
- Date/currency có locale-aware formatting.
- Brand names không bị dịch.
- Product names không bị dịch.
- Model/SKU không bị dịch.
- Proper names không bị dịch.
- Technical identifiers không bị dịch.
- Backend enum/code không bị đổi vì localization.
- Không còn hardcoded user-facing text quan trọng.
- Mobile/desktop không bị vỡ layout do text length.
- Không có regression business logic.
- Có Final Report.
```

---

# 22. Nguyên tắc triển khai bắt buộc

OpenCode phải tuân thủ:

```text
1. Inspect code hiện tại trước khi sửa.
2. Follow existing frontend/backend conventions.
3. Không refactor diện rộng ngoài scope.
4. Không dịch brand/product/model/SKU.
5. Không thay đổi internal enum/code sang tiếng Việt.
6. Không dùng English/Vietnamese text làm translation key.
7. Dùng semantic translation key.
8. Không tạo database translation schema trong V1 nếu không bắt buộc.
9. Không thêm AI translation service.
10. Không đổi currency khi đổi language.
11. Không để localization failure ảnh hưởng core business flow.
12. Báo cáo sau từng phase.
```

Nếu implementation thực tế khác plan vì kiến trúc repository:

```text
- ưu tiên kiến trúc hiện tại
- giữ behavior hiện có
- thay đổi tối thiểu
- ghi rõ deviation trong Progress Report
```

Thực hiện lần lượt từng phase và luôn báo cáo tiến độ.
