# Migration Plan: Thymeleaf → Vue 3 + TypeScript + Tailwind CSS

## Context

**Project:** TheXuong - E-commerce sport apparel application
**Current stack:** Spring Boot 3.5.9 + Thymeleaf + Bootstrap 5 + Font Awesome
**Target stack:** Spring Boot (backend API) + Vue 3 (SPA) + TypeScript + Tailwind CSS
**Figma designs:** Design components and layouts provided by user via Figma

---

## Current State Analysis

### Technology Stack
- **Backend:** Spring Boot 3.5.9, Java 21, Gradle
- **Security:** Spring Security (session-based), OAuth2 Google
- **Database:** SQL Server + JPA/Hibernate
- **Frontend (current):**
  - Thymeleaf templates (server-side rendering)
  - Bootstrap 5.3 CSS + JS bundle
  - Font Awesome 6.5.1 icons
  - Custom CSS at `/css/style.css`
- **Build:** Single Gradle project

### Existing Pages (11 Thymeleaf templates)
| Page | Route | Purpose |
|------|-------|---------|
| index.html | `/`, `/index` | Homepage with hero carousel + featured products |
| products.html | `/products` | Product catalog with filters (sport, brand, keyword, sort) + pagination |
| product-detail.html | `/product-detail/{id}` | Single product view with variant size selection |
| login.html | `/login` | Auth login (form + Google OAuth2) |
| register.html | `/register` | Registration form |
| forgot-password.html | `/forgot-password` | Password reset request |
| cart.html | `/cart` | Shopping cart items + quantity edit |
| checkout.html | `/checkout` | Shipping info + VNPay payment |
| my-orders.html | `/orders` | Customer order history |
| my-order-detail.html | `/order/{id}` | Order detail view |
| profile.html | `/profile` | User profile edit |
| **Admin:** | | |
| admin/products.html | `/admin/products` | Product CRUD list |
| admin/products-edit.html | `/admin/products/{create|edit}` | Product form |
| admin/orders.html | `/admin/orders` | Order management list |
| admin/statistics.html | `/admin/statistics` | Dashboard charts |
| admin/users.html | `/admin/users` | User management (toggle active) + existing REST API |

### Authentication & Authorization
- **Roles:** `CUSTOMER`, `ADMIN`, `BOTH` (Super Admin)
- **Authorities:** `ROLE_CUSTOMER`, `ROLE_ADMIN`, `ROLE_BOTH`
- **Protected routes:**
  - Customer: `/cart`, `/checkout`, `/orders`, `/profile` (authenticated)
  - Admin: `/admin/**` (`ADMIN` or `BOTH`)
- **Public routes:** `/`, `/products/**`, `/product-detail/**`, `/login`, `/register`, `/forgot-password`

### Existing API Endpoints
```
GET    /api/admin/users              - List all users (JSON)
PATCH  /api/admin/users/{id}/toggle-active  - Toggle user active status
```
*Note: Most data currently passed via Thymeleaf Model attributes.*

### Real-time Features
- WebSocket chat (keep as-is, may need frontend integration adjustments)

---

## Migration Strategy: Hybrid Approach (Recommended)

**Option A: SPA + Backend-for-Frontend (BFF) Pattern**
- Vue 3 SPA running on `:5173` (Vite dev) or `/frontend/dist` (production)
- Spring Boot serves API at `/api/**` + also serves static Vue files from `src/main/resources/static/frontend/`
- Vue Router handles client-side navigation
- Axios/Fetch for API calls
- Session cookie + CSRF token for auth (keep current session-based auth)
- No JWT needed

**Option B: In-place Replacement (Not Recommended)**
- Embed Vite build output into `src/main/resources/static/`
- Thymeleaf removed entirely
- Higher risk, harder to iterate

**Selected: Option A** - Cleaner separation, easier development, modern workflow.

---

## Implementation Phases

### Phase 0: Project Setup & Home Page (Foundation)

**Goal:** Configure build tooling, project structure, AND complete Home page với Navbar + Footer

1. **Create frontend directory**
   ```
   frontend/
   ├── src/
   │   ├── components/
   │   │   ├── layout/         # AppLayout, AdminLayout, Navbar, Footer
   │   │   └── ui/             # Design system components
   │   ├── views/
   │   │   ├── Home.vue        # TRANG CHỦ - Batch đầu tiên
   │   │   ├── Products.vue    # Sau
   │   │   ├── ProductDetail.vue
   │   │   ├── Cart.vue
   │   │   ├── Checkout.vue
   │   │   ├── Orders.vue
   │   │   ├── OrderDetail.vue
   │   │   ├── Profile.vue
   │   │   ├── Login.vue
   │   │   ├── Register.vue
   │   │   ├── ForgotPassword.vue
   │   │   └── admin/          # Admin pages sau
   │   ├── router/
   │   ├── stores/
   │   ├── services/
   │   ├── types/
   │   ├── composables/
   │   ├── assets/
   │   ├── App.vue
   │   └── main.ts
   ├── index.html
   ├── vite.config.ts
   ├── tsconfig.json
   ├── tailwind.config.js
   ├── postcss.config.js
   └── package.json
   ```

2. **Add frontend dependencies**
   - `vue`: ^3.5.0
   - `vue-router`: ^4.5.0
   - `pinia`: ^2.3.0
   - `axios`: ^1.7.0
   - `@vueuse/core`: ^11.0.0
   - `tailwindcss`: ^3.4.0 + `@tailwindcss/forms`, `@tailwindcss/typography`
   - `vue-toastification`: ^2.0.0
   - `swiper`: ^11.0.0 (carousel cho home)

3. **Update build.gradle**
   - Keep Spring Boot as is
   - Add npm tasks
   - Configure `processResources` to copy `frontend/dist` to `src/main/resources/static/frontend/`

4. **Tailwind Configuration**
   - Define brand colors từ Figma
   - Configure fonts: từ current CSS (`Russo One`, `Inter`)
   - Enable responsive utilities
   - **Mobile-first approach:** tất cả styles mặc định cho mobile, `md:` `lg:` cho desktop

---

### Workflow: Per-Page Migration (SINGLE PAGE AT A TIME)

**Quy trình mới - MỖI LẦN 1 TRANG:**

```
┌─────────────────────────────────────────────────────────────┐
│  VỚI MỖI TRANG/PAGE:                                         │
│  1. Agent báo cáo: sẽ làm trang nào, cần Figma gì           │
│  2. Anh gửi Figma selection (chỉ 1 frame/component)        │
│  3. Agent đọc → báo cáo layout, responsive breakpoints      │
│  4. Anh confirm hoặc chỉnh                                  │
│  5. Agent code + commit                                     │
│  6. Agent screenshot kết quả so với Figma                   │
│  7. Anh duyệt → mới qua trang tiếp                         │
└─────────────────────────────────────────────────────────────┘
```

**Batch Sequence (theo trang):**

| Order | Trang | Figma selection cần | APIs cần | Notes |
|-------|-------|-------------------|----------|-------|
| **0.1** | **Home** (trang chủ) | Frame: Home page (desktop + mobile) + Navbar + Footer | `GET /api/products/new` (4-8 items) | ⭐ **✅ HOÀN THÀNH** - [see commit 1183788] |
| 0.2 | Products List | Frame: Products page với filters | `GET /api/products`, `GET /api/categories/sports`, `GET /api/categories/brands` | |
| 0.3 | Product Detail | Frame: Product detail page | `GET /api/products/:id` | |
| 0.4 | Cart | Frame: Cart page | `GET /api/cart` | |
| 0.5 | Checkout | Frame: Checkout page | `POST /api/checkout/create` | |
| 0.6 | Orders List | Frame: Orders/My Orders page | `GET /api/orders` | |
| 0.7 | Order Detail | Frame: Order detail page | `GET /api/orders/:id` | |
| 0.8 | Profile | Frame: Profile edit page | `GET /api/profile`, `PUT /api/profile` | |
| 0.9 | Login | Frame: Login page | `POST /api/auth/login` | |
| 1.0 | Register | Frame: Register page | `POST /api/auth/register` | |
| 1.1 | Forgot Password | Frame: Forgot password page | `POST /api/auth/forgot-password` | |
| 1.2+ | Admin Pages** | Sau, anh sẽ gửi | | Anh báo khi nào cần |

**Definition of Done — CHO MỖI TRANG:**

- [ ] **Figma fidelity:** Pixel-perfect với Figma selection (màu, spacing, font, border-radius)
- [ ] **Responsive:** Mobile (375px) + Tablet (768px) + Desktop (1280px+) - **TỰ ĐỘNG** adjust
- [ ] **Auth/CSRF:** Mutating requests gửi CSRF token; 401→redirect `/login`; 403→redirect `/` hoặc `/403`
- [ ] **Loading state:** Spinner/skeleton khi fetch data
- [ ] **Error state:** Toast error khi API fail; không crash
- [ ] **Empty state:** Message rõ ràng khi data rỗng
- [ ] **Accessibility cơ bản:** Buttons có text, inputs có labels, contrast đủ
- [ ] **Browser console sạch:** Không error/warning đỏ
- [ ] **Vue dev tools:** Component tree đúng, không warning
- [ ] **Router links:** Dùng `<router-link>`, không full reload
- [ ] **Commit message:** Conventional Commits (`feat:`, `fix:`, `refactor:`)
- [ ] **No dead code:** Không TODO, file rỗng, import thừa

**Checkpoint Report Format (mỗi trang xong):**

```markdown
## 📋 Checkpoint — Trang [X]: [Tên trang]

### ✅ Đã hoàn thành
- [ ] Component: `src/views/XXX.vue`
- [ ] Commit: `abc123 feat: ...`

### 🎨 So với Figma
- **Figma selection:** [mô tả frame/component đã dùng]
- **Responsive:** Mobile ✅ / Tablet ✅ / Desktop ✅
- **Pixel match:** ✅ Khớp hoàn toàn / ⚠️ Lệch tại: [liệt kê]
- **Screenshot:** [đính kèm ảnh 3 breakpoints]

### 🧪 Test đã chạy
- [ ] Route: `/trang-xyz` → render đúng
- [ ] Data fetch: API trả data → hiển thị
- [ ] Loading: spinner hiện khi loading
- [ ] Error: API fail → toast error
- [ ] Auth: chưa login → 401 → redirect `/login`

### ⏭️ Tiếp theo
- **Trang tiếp theo:** [Tên trang tiếp theo trong queue]
- **Cần Figma:** [Frame/component cần anh gửi]
```

---

### Phase 1: API Layer Preparation (Parallel với Batch 0+)

**Goal:** Expose JSON APIs cho tất cả data needs

**Create REST controllers:** Reuse existing services

**Products API:**
```
GET    /api/v1/products                    - List with filters
GET    /api/v1/products/{id}               - Detail với variants
GET    /api/v1/categories/sports           - List sports
GET    /api/v1/categories/brands           - List brands
GET    /api/v1/products/new                - New arrivals
```

**Cart API:**
```
GET    /api/v1/cart                        - Get current cart
POST   /api/v1/cart/items                  - Add item
PUT    /api/v1/cart/items/{id}             - Update quantity
DELETE /api/v1/cart/items/{id}             - Remove item
```

**Orders API:**
```
GET    /api/v1/orders                      - My orders
GET    /api/v1/orders/{id}                 - Order detail
POST   /api/v1/orders/place                - Create order from cart
POST   /api/v1/orders/{id}/cancel          - Cancel order
```

**Profile API:**
```
GET    /api/v1/profile                     - Current user info
PUT    /api/v1/profile                     - Update profile
```

**Checkout:**
```
POST   /api/v1/checkout/create             - Create order + VNPay URL
POST   /api/v1/checkout/confirm           - Confirm payment
```

**Auth API:**
```
POST   /api/v1/auth/login                  - Login
POST   /api/v1/auth/logout                 - Logout
GET    /api/v1/auth/user                   - Current user
POST   /api/v1/auth/register              - Register
POST   /api/v1/auth/forgot-password       - Forgot password
```

**Admin APIs** (khi làm admin pages):
```
GET    /api/v1/admin/products              - List
POST   /api/v1/admin/products              - Create
PUT    /api/v1/admin/products/{id}         - Update
DELETE /api/v1/admin/products/{id}         - Delete
GET    /api/v1/admin/orders                - List
PATCH  /api/v1/admin/orders/{id}/status    - Update status
GET    /api/v1/admin/statistics            - Dashboard stats
GET    /api/v1/admin/users                 - List (existing ✓)
PATCH  /api/v1/admin/users/{id}/toggle-active - Toggle (existing ✓)
```

---

### Phase 2: Build Core UI Components (Design System)

**Sau khi Home page xong, build design system:**

Create reusable components:
- `UIButton.vue` - primary, secondary, ghost variants
- `UIInput.vue` - text, email, password, error states
- `UICard.vue`
- `UISelect.vue`
- `UIModal.vue`
- `LoadingSpinner.vue`
- `ErrorMessage.vue`
- `EmptyState.vue`

**Figma cần:** Design system file với tất cả variants

---

### Phase 3: Continue Sequential Pages

Theo queue ở trên (0.2 → 0.3 → ... → 1.1 → Admin)

---

### Phase 4: Integrations & Polish

1. **WebSocket Chat** - Connect on login
2. **Images & Assets** - Move to frontend assets
3. **Icons** - Heroicons (SVG inline) thay Font Awesome
4. **Responsive Design** - Tailwind breakpoints: mobile-first
5. **Error Handling** - Global error boundary, toast notifications
6. **Loading States** - Skeleton loaders
7. **SEO** - Vue Meta (optional)

---

## Critical Rules (User's Requirements)

### 0. 🚨 TUÂN THỦ FIGMA — BẮT BUỘC

**Workflow cho MỖI trang/component:**

```
1. BÁO CÁO PHẠM VI: Trước khi code, nói rõ sẽ làm trang nào, cần Figma selection nào.
2. ANH GỬI FIGMA SELECTION: Chỉ 1 frame/component cho trang đó.
3. ĐỌC + BÁO CÁO: Tóm tắt layout, colors, spacing, responsive breakpoints.
4. CHECKPOINT — CHỜ CONFIRM.
5. CODE THEO FIGMA: Lấy đúng pixels, colors, fonts từ Figma.
6. SHOWCASE & CHECKPOINT TIẾP.
```

**Responsive design rule:**
- **Mobile-first:** Base styles = mobile (375px)
- **Tablet:** `md:` breakpoint (768px)
- **Desktop:** `lg:` breakpoint (1024px), `xl:` (1280px)
- **Auto-adjust:** Layout phải tự động thích ứng, không có overflow horizontal
- **Navigation:** Mobile = hamburger menu; Desktop = full nav

**Khi Figma có desktop + mobile:**
- Code responsive classes: `flex-col md:flex-row`, `hidden md:block`, etc.
- Nếu Figma chỉ có desktop → HỎI anh về mobile layout, không tự ý làm

**🚫 Khi Figma bị lỗi:**
1. DỪNG NGAY. Không tự ý làm design tự chế.
2. Báo cáo lỗi cụ thể.
3. Đề xuất: Anh gửi lại selection hoặc export PNG.

---

### 1. NO component movement without approval
   - Nếu Figma có layout order khác, hỏi user trước.

2. **Preserve ALL existing functionality**
   - Admin role restrictions
   - Product size logic
   - View count increment
   - WebSocket chat
   - VNPay integration
   - Google OAuth2
   - CSRF protection

3. **Keep backend logic unchanged**
   - Reuse services, repositories
   - Maintain validation rules

4. **API versioning**
   - Use `/api/v1/` prefix

5. **Figma design fidelity**
   - Extract exact spacing, colors, typography
   - Match border-radius, shadows, hover effects

---

## Risks & Mitigations

| Risk | Impact | Mitigation |
|------|--------|------------|
| Breaking auth flow | High | Test login/logout thoroughly |
| Admin pages lose role UI | High | Route guards + v-if checks |
| Cart count not syncing | Medium | Pinia reactive + localStorage |
| SEO drop (SPA) | Medium | Add meta tags |
| Build complexity | Low | Separate frontend dir |
| CSRF missing → 403 | High | Axios interceptor |
| API endpoint gaps | High | Audit all Model attributes |
| Large bundle size | Low | Code splitting by route |

---

## Questions for User

1. **Migration style:** Big bang (all pages at once) or hybrid? → **Recommendation: Big bang**
2. **Figma integration:** Can you share the Figma file? Cần design tokens.
3. **Component movement:** Follow Figma exactly even if layout differs from current? → **Recommendation: Yes, follow Figma**
4. **Router mode:** History mode (`/products`) hoặc Hash? → **Recommendation: History mode**
5. **API versioning:** Use `/api/v1/`? → **Recommendation: Yes**
6. **Auth persistence:** Session cookie (giữ nguyên) hay JWT? → **Recommendation: Session cookie**
7. **Deployment:** Embedded in JAR hay separate? → **Recommendation: Embedded**
8. **Admin charts:** Metrics gì? → Revenue, Orders, Users, Products + charts by status/category

---

## Next Steps

1. ✅ User answers questions + share Figma
2. ✅ Create `frontend/` directory structure
3. ✅ Configure Tailwind + Vite + TypeScript
4. ✅ Setup Gradle tasks
5. ✅ Create API endpoints (Batch 0 APIs)
6. ✅ **HOME PAGE COMPLETED** - Pixel-perfect Figma implementation with responsive design
7. ⏭️ **Next: Products page (0.2)** - awaiting Figma selection for Products list page with filters

---

## ✅ LỜI NHẮN ĐẾN HERMES (Batch processor)

**Task cho Hermes (song song với migration):**
- Vui lòng xem `orderstatus.md` và `voucher.md`
- Thực hiện công việc liên quan đến OrderStatus enum và voucher system
- **KHÔNG** can thiệp vào migration Vue (đây là frontend migration)
- **KHÔNG** sửa code backend APIs đang được dùng cho Vue migration

**Clarification:** Em (frontend migration agent) và Hermes (backend orderstatus/voucher agent) làm song song nhưng độc lập.
