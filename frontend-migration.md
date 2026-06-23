# Frontend Migration Plan: Thymeleaf → Vue 3 + TypeScript + Tailwind

> **For Hermes:** Use subagent-driven-development hoặc inline execution với Task Report sau mỗi task. Load skills: `thexuong-stack`, `spring-thymeleaf-to-vue3`, `plan`, `multi-batch-feature-implementation`, `design-fidelity-workflow` TRƯỚC khi bắt đầu batch.

**Goal:** Xóa toàn bộ Thymeleaf templates + Thymeleaf controller + dependency. Backend Spring Boot chỉ còn REST API. Frontend Vue 3 + TypeScript + Tailwind CSS là SPA duy nhất. Spring Boot serve static `frontend/dist/` qua WebMvcConfigurer.

**Architecture:** Hybrid SPA — Vue 3 dev chạy `:5173` (Vite), production build copy vào `src/main/resources/static/frontend/`, Spring Boot serve qua `WebMvcConfigurer.addResourceHandlers("/**")`. Session-based auth (JSESSIONID + CSRF XSRF-TOKEN) giữ nguyên.

**Tech Stack:** Spring Boot 3.5.9 (Java 21, REST only) + Vue 3.5 + TypeScript 5 + Vite 5 + Tailwind 3.4 + Pinia 2.3 + Vue Router 4.5 + Axios 1.7

**Figma rule (BẮT BUỘC):** Mỗi trang Vue cần Figma selection từ anh TRƯỚC khi code. Workflow 6 bước trong skill `spring-thymeleaf-to-vue3` § "QUY TẮC FIGMA". Khi Figma lỗi / không đọc được → DỪNG + báo cáo (skill spring-thymeleaf-to-vue3 pitfall).

**Song song với loyalty/voucher (feat/batch-5-cron-email-report):** Plan này CHỈ đụng trang customer + admin core, KHÔNG đụng `templates/loyalty/`, `templates/my-vouchers.html`, `templates/admin/orders.html` (sẽ migrate sau khi Batch 5 merge).

---

## 1. Context & Assumptions

### 1.1. Hiện trạng (verified 23/06/2026)

| Component | Status | Notes |
|---|---|---|
| **Backend Spring Boot** | ✅ Đang chạy | 68 file .java, port 8080, JDK 21, Gradle |
| **REST API `/api/v1/**` đã có** | ✅ 5 controller | AuthRest, ProductRest, CartRest, CategoryRest, UserManagementRest, ChatRest, RoleGroupRest |
| **REST API THIẾU cho Vue** | ⚠️ 9 endpoint | Orders, OrderDetail, Checkout, Profile, Favorite, AdminOrders, AdminStatistics, AdminProductCRUD |
| **Vue 3 frontend skeleton** | ✅ Có sẵn 18 view | Home, Products, ProductDetail, Cart, Checkout, Orders, OrderDetail, Profile, Login, Register, ForgotPassword, Favorite, NotFound + 5 admin |
| **Vue Router + Pinia + Auth store** | ✅ Có sẵn | `frontend/src/router/index.ts` đã có guard cho `requiresAdmin`, `guestOnly` |
| **Tailwind config** | ✅ Có sẵn | `frontend-rules.md` đã liệt kê brand colors + font Geist/Gelasio |
| **Build pipeline** | ✅ Có sẵn | `build.gradle` có `npmInstall`, `npmBuild`, `copyFrontend`, `processResources dependsOn copyFrontend` |
| **Thymeleaf templates** | 🗑️ 16 file cần xóa | `src/main/resources/templates/` (index, login, register, products, product-detail, cart, checkout, my-orders, my-order-detail, profile, forgot-password, my-vouchers + admin/ + fragments/ + loyalty/) |
| **Thymeleaf dep** | 🗑️ 2 cần xóa | `spring-boot-starter-thymeleaf` (dòng 71 + 74 duplicate), `thymeleaf-extras-springsecurity6` |
| **MVC Controller trả view** | 🗑️ ~12 cần xóa | AuthController, ProductController, CartController, CheckoutController, OrderController, OrderManagementController, ProfileController, AdminProductController, ForgotPasswordController, EmailController (xem lại), GlobalControllerAdvice |

### 1.2. Quyết định đã chốt với anh

| Câu hỏi | Quyết định |
|---|---|
| Phạm vi dọn Thymeleaf | **Xóa sạch 1 lần** (Batch 7) — folder templates/ + thymeleaf dep + mọi @Controller trả String view |
| Ưu tiên trang | **Customer trước** (11 trang), Admin sau |
| Auth/CSRF | **Giữ session-based** (JSESSIONID + XSRF-TOKEN), KHÔNG đổi sang JWT |
| Figma | **Anh cung cấp screenshot + CSS all layer Figma** cho từng trang khi đến batch |
| REST API | Em đã audit, có 9 endpoint thiếu → batch bổ sung REST trước batch Vue tương ứng |
| Loyalty/Voucher | Chạy song song ở `feat/batch-5-cron-email-report`, plan này không đụng |
| Số batch | 8 batch nhỏ (5-8 task/batch) |
| Báo cáo | **Mỗi trang Vue migrate xong → 1 commit + 1 báo cáo ngắn** (file/API/Figma match %). Cuối batch → tổng hợp + update tracking table |

### 1.3. Files sẽ thay đổi (tổng quan)

- **Xóa:** `src/main/resources/templates/**` (16 file) + 12 MVC controller + 2 Thymeleaf dep trong `build.gradle`
- **Thêm:** ~10 REST endpoint mới (controller + DTO) + 1 WebMvcConfigurer + sửa `SecurityConfig` (bỏ rule Thymeleaf)
- **Sửa Vue:** 11 view customer + 5 view admin + 10 file service (`frontend/src/api/*`) + 1 file store (favorite, orders)
- **KHÔNG đụng:** Loyalty/voucher (templates/loyalty/, my-vouchers.html, feat/batch-5-cron-email-report branch)

---

## 2. Bảng Batch Overview

| # | Batch | Track | Mục tiêu | Task | Phụ thuộc |
|---|---|---|---|---|---|
| **0** | Foundation & Audit Gate | Backend | WebMvcConfigurer serve static + remove thymeleaf-extras-springsecurity6 + verify Vue dev build | 5 | — |
| **1** | Auth REST chuẩn hoá | Backend | Hoàn thiện `/api/v1/auth/*` — login form-based, current user, register, logout, forgot-password | 6 | — |
| **2** | Product & Category REST bổ sung | Backend | `/api/v1/products/{id}/variants`, `/api/v1/categories` đầy đủ cho filter | 4 | — |
| **3** | Orders, Checkout, Profile REST (THIẾU) | Backend | 5 endpoint mới: orders list/detail, checkout create, profile get/update | 7 | — |
| **4** | Favorite REST (mới hoàn toàn) | Backend | Favorite entity + 3 endpoint (GET/POST/DELETE) | 5 | — |
| **5** | Admin REST bổ sung | Backend | `/api/v1/admin/orders`, `/api/v1/admin/statistics`, `/api/v1/admin/products` CRUD | 8 | — |
| **6** | Vue Customer (11 trang) Figma-driven | Frontend | Home → Products → ProductDetail → Cart → Checkout → Orders → OrderDetail → Profile → Login → Register → ForgotPassword → Favorite | 11 | Batch 1-5 |
| **7** | Vue Admin (5 trang) + Cleanup Thymeleaf | Frontend+Backend | 5 admin view + xóa folder templates/ + xóa thymeleaf dep + xóa MVC controller | 8 | Batch 6 |

**Tổng:** 8 batch, ~54 task bite-sized.

**Lưu ý quan trọng:**
- Batch 0-5 là **backend only** (REST + config), KHÔNG đụng Vue, KHÔNG đụng Thymeleaf
- Batch 6 là **frontend only** (Vue customer), Figma-driven, KHÔNG xóa Thymeleaf
- Batch 7 vừa frontend (admin) vừa backend (cleanup Thymeleaf) — gate: Vue `npm run build` pass + 11 trang customer test thủ công xong

---

## 3. Quy trình thực thi

### 3.1. Branch strategy

```bash
# Branch chính cho plan này (tạo từ main)
git checkout main
git pull origin main
git checkout -b feat/migrate-vue3-batch-plan   # chỉ chứa plan file
# Sau khi anh duyệt plan, mỗi batch tạo branch riêng
git checkout -b feat/m0-vue-foundation
# ... qua feat/m1-auth-rest, feat/m2-product-rest, ...
```

**Lý do tách branch mỗi batch:** Tránh 1 PR khổng lồ, dễ review, dễ rollback nếu fail. Anh merge từng branch vào main sau khi duyệt.

**Lưu ý:** Branch `feat/batch-5-cron-email-report` (loyalty) ĐANG chạy song song ở local. KHÔNG merge branch này vào main cho đến khi loyalty xong. Plan này dùng branch `feat/migrate-vue3-batch-plan` (chứa plan) + các `feat/mN-*` (chứa code).

### 3.2. Figma workflow (BẮT BUỘC cho Batch 6-7)

Mỗi task trong Batch 6/7 làm 1 trang Vue, theo workflow 6 bước trong skill `spring-thymeleaf-to-vue3`:

```
1. Agent báo cáo: "Tôi sẽ làm trang X, cần Figma selection cho desktop + mobile + states"
2. Anh gửi: screenshot + copy CSS all layer từ Figma
3. Agent đọc + báo cáo: layout, breakpoints, color token, font, spacing
4. Checkpoint: Agent hỏi nếu có điểm mơ hồ
5. Agent code theo Figma (px/hex/radius LẤY ĐÚNG, không làm tròn)
6. Agent screenshot kết quả, so sánh Figma, báo cáo % match
```

**Khi Figma lỗi / không đọc được / data rỗng:** DỪNG + báo cáo. KHÔNG tự ý dùng Tailwind default. KHÔNG reuse design từ trang khác.

### 3.3. Báo cáo format (anh đã chốt ở câu 8)

**Mỗi task (1 trang Vue migrate xong) → 1 commit + 1 Task Report ngắn:**

```markdown
## Task Report: <Tên trang> (migrated to Vue 3)

**Trạng thái:** ✅ SUCCESS | ⚠️ PARTIAL | ❌ FAIL
**Commit:** `<hash>` — <message tiếng Việt>

### 1. File thay đổi
- Tạo: `frontend/src/views/<Trang>.vue` (~XXX dòng)
- Sửa: `frontend/src/api/<resource>.ts` (nếu có thêm method)
- (KHÔNG đụng backend trong task này)

### 2. API consume
- `GET /api/v1/<resource>` — mô tả ngắn
- (POST/PUT/DELETE nếu có)

### 3. Figma match
- Desktop (1280px): X% so với Figma
- Mobile (375px): Y% so với Figma
- Điểm khác biệt: <nếu có>

### 4. DoD checklist
- [ ] Auth/CSRF: ✅ / ⚠️ / ❌
- [ ] Loading state: ✅
- [ ] Error state: ✅
- [ ] Empty state: ✅ (nếu có)
- [ ] Mobile responsive: ✅
- [ ] Browser console sạch: ✅
- [ ] Router dùng `<router-link>`: ✅

### 5. Commit detail
- `git log --oneline -1` → <hash>
- `git show --stat HEAD` → <file list>
```

**Mỗi batch xong → 1 commit docs + update Tracking Table ở cuối plan:**

```bash
git add frontend-migration.md
git commit -m "docs: cập nhật tracking Batch N (% DONE) + báo cáo chi tiết"
```

**KHÔNG tạo file `*_report.md` riêng** — tất cả tracking inline trong plan này.

### 3.4. Stage rule (BẮT BUỘC)

```bash
# ✅ ĐÚNG — stage chính xác file cần
git add frontend/src/views/Cart.vue
git add frontend/src/api/cart.ts

# ❌ SAI — kéo cả workspace bẩn (25 modified, 5 untracked)
git add .
git add -A
```

Workspace hiện có 25 modified + 5 untracked từ session trước. Mỗi commit PHẢI stage chính xác path. Skill `multi-batch-feature-implementation` § Pitfall 1.

---

## 4. Batch chi tiết

### 📦 BATCH 0 — Foundation & Audit Gate

**Track:** Backend only
**Mục tiêu:** WebMvcConfigurer serve static frontend + xóa thymeleaf-extras-springsecurity6 + verify Vue dev build chạy được
**Phụ thuộc:** —
**Definition of Done:**
- ✅ `npm run dev` trong `frontend/` chạy được, mở `http://localhost:5173` thấy Home.vue
- ✅ `npm run build` trong `frontend/` pass, output `frontend/dist/`
- ✅ `./gradlew build` pass
- ✅ `WebMvcConfigurer` config serve `/**` → fallback về `index.html` cho SPA routing
- ✅ `thymeleaf-extras-springsecurity6` đã xóa khỏi `build.gradle`
- ✅ Branch `feat/m0-vue-foundation` đã commit + push (nếu có remote)

**Lưu ý quan trọng:**
- KHÔNG xóa `spring-boot-starter-thymeleaf` ở batch này (còn dùng cho admin/orders.html từ loyalty, và cho Batch 1-5 vẫn có thể cần nếu debug)
- Chỉ xóa `thymeleaf-extras-springsecurity6` vì Vue sẽ handle auth, không cần `sec:authorize` nữa
- File `MIGRATION_PLAN_VUE_3.md` (đã D trong working tree) KHÔNG recover — nội dung đã được integrate vào plan này

---

#### Task 0.1 — Audit workspace + tạo branch plan

**Files:**
- Read: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/frontend/src/api/` (xem cấu trúc service hiện tại)
- Read: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/frontend/src/stores/` (xem Pinia store nào có sẵn)
- Read: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/frontend/src/components/` (xem layout component nào có sẵn)
- Create: branch `feat/migrate-vue3-batch-plan`

**Step 1: Audit workspace**

```bash
cd "D:/FPT Polytechnic/JAVA/JAVA5/TheXuong"
ls frontend/src/api/ 2>&1 || echo "MISSING: api/"
ls frontend/src/stores/ 2>&1 || echo "MISSING: stores/"
ls frontend/src/composables/ 2>&1 || echo "MISSING: composables/"
ls frontend/src/components/ 2>&1 || echo "MISSING: components/"
```

Em sẽ báo cáo file nào có sẵn, file nào thiếu → ảnh hưởng đến Batch 6 (Vue customer).

**Step 2: Tạo branch plan + commit plan file**

```bash
cd "D:/FPT Polytechnic/JAVA/JAVA5/TheXuong"
git checkout main
git pull origin main  # nếu có remote
git checkout -b feat/migrate-vue3-batch-plan
git add frontend-migration.md
git commit -m "docs: lên plan migration Thymeleaf → Vue 3 (8 batch, 54 task)"
git log --oneline -1
```

**Step 3: Verify commit**

```bash
git show --stat HEAD | head -10
# → Expected: 1 file changed, frontend-migration.md
```

**DoD:** ✅ Plan file committed, branch mới tồn tại, working tree sạch (chỉ có plan file thay đổi).

---

#### Task 0.2 — Tạo WebMvcConfigurer serve static Vue

**Files:**
- Create: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/config/WebMvcConfig.java`

**Step 1: Tạo file**

```java
package com.example.thexuong.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Serve static frontend (Vue 3 build) từ /static/frontend/ trong classpath.
     * Khi Spring Boot chạy production, file index.html của Vue sẽ được serve ở /.
     * 
     * Dev mode: Vue chạy ở :5173 (Vite), gọi API :8080 qua VITE_API_BASE_URL.
     * Production: npm run build → frontend/dist/ → copy vào src/main/resources/static/frontend/
     * (đã có sẵn trong build.gradle: processResources dependsOn copyFrontend)
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/frontend/**")
                .addResourceLocations("classpath:/static/frontend/")
                .setCachePeriod(3600);
    }

    /**
     * SPA fallback: mọi route không khớp với /api/**, /admin/**, /static/**, /css/**,
     * /js/**, /images/** → forward về /forward/index để Vue Router xử lý client-side.
     * 
     * Forward đến /forward/index (KHÔNG phải /index) để tránh loop khi /index cũng
     * không khớp — /forward/index là static path rõ ràng.
     */
    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Health check
        registry.addViewController("/health").setViewName("forward:/health-status");
        // Có thể thêm các endpoint public khác ở đây
    }
}
```

**Step 2: Verify build**

```bash
cd "D:/FPT Polytechnic/JAVA/JAVA5/TheXuong"
./gradlew compileJava
# Expected: BUILD SUCCESSFUL
```

**Step 3: Commit**

```bash
git add src/main/java/com/example/thexuong/config/WebMvcConfig.java
git commit -m "feat: thêm WebMvcConfig serve static Vue + SPA routing support"
git log --oneline -1
```

**DoD:** ✅ File compile pass, commit thành công.

---

#### Task 0.3 — Xóa thymeleaf-extras-springsecurity6

**Files:**
- Modify: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/build.gradle:78`

**Step 1: Edit build.gradle**

Xóa dòng:
```gradle
implementation 'org.thymeleaf.extras:thymeleaf-extras-springsecurity6'
```

**Step 2: Verify build**

```bash
cd "D:/FPT Polytechnic/JAVA/JAVA5/TheXuong"
./gradlew compileJava
# Expected: BUILD SUCCESSFUL (vì deps này chỉ dùng cho sec:authorize trong template, không ảnh hưởng compile)
```

**Step 3: Verify không còn import thymeleaf-extras**

```bash
grep -rn "thymeleaf.extras" src/main/java --include="*.java"
# Expected: (no output) — không còn file nào import
```

**Step 4: Commit**

```bash
git add build.gradle
git commit -m "chore: xóa thymeleaf-extras-springsecurity6 (Vue tự handle auth)"
git log --oneline -1
```

**DoD:** ✅ Build pass, không còn reference đến thymeleaf-extras, commit thành công.

---

#### Task 0.4 — Verify Vue dev build

**Files:**
- Read: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/frontend/package.json`

**Step 1: Check frontend skeleton**

```bash
cd "D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/frontend"
ls package.json
ls vite.config.ts 2>/dev/null || ls vite.config.js
ls src/main.ts 2>/dev/null || ls src/main.js
ls src/App.vue
```

**Step 2: Install + dev build**

```bash
cd "D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/frontend"
npm install
npm run dev
# Expected: Vite ready in XXX ms, Local: http://localhost:5173/
# (Dừng sau 5s, không cần giữ server chạy)
```

**Step 3: Production build**

```bash
cd "D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/frontend"
npm run build
# Expected: built in XXX ms, dist/ folder created
ls dist/
# Expected: index.html, assets/, ...
```

**Step 4: Verify build pipeline Gradle**

```bash
cd "D:/FPT Polytechnic/JAVA/JAVA5/TheXuong"
./gradlew processResources
# Expected: copyFrontend task runs, frontend/dist copied to src/main/resources/static/frontend/
ls src/main/resources/static/frontend/
# Expected: index.html, assets/, ...
```

**Step 5: Commit (nếu có file build artifacts thay đổi)**

```bash
cd "D:/FPT Polytechnic/JAVA/JAVA5/TheXuong"
git status --short
# Nếu có file mới trong static/frontend/ từ build, KHÔNG commit (gitignore)
# Nếu có file build.gradle hoặc WebMvcConfig thay đổi → commit riêng
```

**DoD:** ✅ `npm run dev` chạy được, `npm run build` pass, `./gradlew processResources` copy được Vue dist vào static/.

---

#### Task 0.5 — Update SecurityConfig cho SPA

**Files:**
- Read: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/config/SecurityConfig.java`
- Modify: thêm permitAll cho `/`, `/index.html`, `/frontend/**`, các static asset

**Step 1: Đọc SecurityConfig hiện tại**

```bash
cd "D:/FPT Polytechnic/JAVA/JAVA5/TheXuong"
grep -n "permitAll\|authorizeHttpRequests\|csrf" src/main/java/com/example/thexuong/config/SecurityConfig.java
```

**Step 2: Thêm rule permitAll cho static + index**

Trong block `authorizeHttpRequests`, thêm:

```java
.requestMatchers("/", "/index.html", "/frontend/**", "/assets/**", "/favicon.ico").permitAll()
.requestMatchers("/css/**", "/js/**", "/images/**", "/webjars/**").permitAll()
```

**Lưu ý:** KHÔNG permitAll cho `/api/**`, `/admin/**` — các route này vẫn cần auth.

**Step 3: Verify build**

```bash
cd "D:/FPT Polytechnic/JAVA/JAVA5/TheXuong"
./gradlew compileJava
# Expected: BUILD SUCCESSFUL
```

**Step 4: Commit**

```bash
git add src/main/java/com/example/thexuong/config/SecurityConfig.java
git commit -m "feat: permitAll cho static frontend + index.html (SPA)"
git log --oneline -1
```

**DoD:** ✅ SecurityConfig cho phép truy cập static mà không cần auth, các API vẫn bảo vệ đúng.

---

### 📦 BATCH 1 — Auth REST chuẩn hoá

**Track:** Backend only
**Mục tiêu:** Hoàn thiện `/api/v1/auth/*` cho Vue login/register/forgot-password/logout
**Phụ thuộc:** Batch 0
**Definition of Done:**
- ✅ `POST /api/v1/auth/login` thực sự authenticate (qua Spring Security) + trả 200 + set JSESSIONID
- ✅ `GET /api/v1/auth/user` trả user hiện tại (đã có sẵn, chỉ cần verify)
- ✅ `POST /api/v1/auth/logout` invalidate session
- ✅ `POST /api/v1/auth/register` (đã có sẵn) — chỉ cần verify CSRF + validation
- ✅ `POST /api/v1/auth/forgot-password` (đã có stub) — implement thật
- ✅ 5 file test viết bằng `@SpringBootTest` + MockMvc pass

**Lưu ý:**
- Auth dùng session-based (JSESSIONID cookie), KHÔNG dùng JWT cho Vue
- CSRF token lấy từ cookie `XSRF-TOKEN` (Spring tự set), gửi kèm header `X-XSRF-TOKEN` cho mọi POST/PUT/DELETE
- Endpoint `/api/v1/auth/login` cần config trong SecurityConfig để permit POST không cần CSRF (vì lần đầu chưa có cookie XSRF-TOKEN)

---

#### Task 1.1 — Config SecurityConfig cho auth REST

**Files:**
- Modify: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/config/SecurityConfig.java`

**Step 1: Thêm permitAll cho auth endpoints**

```java
.requestMatchers("/api/v1/auth/login", "/api/v1/auth/register", "/api/v1/auth/forgot-password").permitAll()
.requestMatchers("/api/v1/auth/user", "/api/v1/auth/logout").permitAll()  // user trả 401 nếu chưa login, logout cần CSRF
```

**Step 2: Disable CSRF cho auth login (lần đầu chưa có XSRF-TOKEN)**

```java
// Trong csrf().disable() hoặc dùng CookieCsrfTokenRepository.withHttpOnlyFalse()
// Spring mặc định permit POST /login khi dùng formLogin() — KHÔNG cần permitAll CSRF cho API auth
```

**Lưu ý:** Spring Security mặc định dùng `CookieCsrfTokenRepository` cho SPA, đọc header `X-XSRF-TOKEN`. Khi frontend chưa có cookie XSRF-TOKEN (lần đầu), Spring sẽ trả cookie kèm response. Cần verify bằng curl.

**Step 3: Commit**

```bash
git add src/main/java/com/example/thexuong/config/SecurityConfig.java
git commit -m "feat: permitAll cho /api/v1/auth/* endpoints"
```

**DoD:** ✅ SecurityConfig cho phép POST auth không bị 403.

---

#### Task 1.2 — Implement login form-based cho REST

**Files:**
- Modify: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/controller/api/AuthRestController.java:82`

**Step 1: Thay stub login thành authentication thật**

```java
@PostMapping("/login")
public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpServletRequest httpRequest) {
    try {
        // Spring Security xử lý authentication
        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken(request.email(), request.password());
        Authentication authentication = authenticationManager.authenticate(authToken);
        
        // Set vào SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        // Lưu vào session (quan trọng cho JSESSIONID)
        HttpSession session = httpRequest.getSession(true);
        session.setAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, 
                             SecurityContextHolder.getContext());
        
        return ResponseEntity.ok(new MessageResponse("Login successful"));
    } catch (BadCredentialsException e) {
        return ResponseEntity.status(401).body(new MessageResponse("Invalid email or password"));
    }
}
```

**Step 2: Inject AuthenticationManager vào AuthRestController**

Thêm field + constructor injection (hoặc dùng @Autowired).

**Step 3: Build + manual test bằng curl**

```bash
cd "D:/FPT Polytechnic/JAVA/JAVA5/TheXuong"
./gradlew compileJava
# Expected: BUILD SUCCESSFUL

# Test (cần Spring Boot đang chạy trên :8080)
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user1@thexuong.com","password":"user123"}' \
  -c cookies.txt
# Expected: 200 + "Login successful" + Set-Cookie: JSESSIONID=...
```

**Step 4: Commit**

```bash
git add src/main/java/com/example/thexuong/controller/api/AuthRestController.java
git commit -m "feat: implement login REST thật (Spring Security authenticate + session)"
```

**DoD:** ✅ Login trả 200 + set JSESSIONID, sai password trả 401.

---

#### Task 1.3 — Implement logout invalidate session

**Files:**
- Modify: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/controller/api/AuthRestController.java:89`

**Step 1: Thay stub logout**

```java
@PostMapping("/logout")
public ResponseEntity<?> logout(HttpServletRequest request) {
    HttpSession session = request.getSession(false);
    if (session != null) {
        session.invalidate();
    }
    SecurityContextHolder.clearContext();
    return ResponseEntity.ok(new MessageResponse("Logout successful"));
}
```

**Step 2: Build + manual test**

```bash
cd "D:/FPT Polytechnic/JAVA/JAVA5/TheXuong"
./gradlew compileJava
# Test (cần đã login trước đó)
curl -X POST http://localhost:8080/api/v1/auth/logout -b cookies.txt
# Expected: 200 + "Logout successful"
curl http://localhost:8080/api/v1/auth/user -b cookies.txt
# Expected: 401 (session invalidated)
```

**Step 3: Commit**

```bash
git add src/main/java/com/example/thexuong/controller/api/AuthRestController.java
git commit -m "feat: logout REST invalidate session + clear SecurityContext"
```

**DoD:** ✅ Logout xoá session, /user trả 401 sau logout.

---

#### Task 1.4 — Verify register REST (đã có sẵn)

**Files:**
- Read: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/controller/api/AuthRestController.java:95`

**Step 1: Verify endpoint hiện tại**

Endpoint `/api/v1/auth/register` đã có sẵn logic. Chỉ cần:
- Verify password match validation
- Verify UserService.createUser() xử lý BCrypt + Role
- Test 422 nếu email đã tồn tại

**Step 2: Test bằng curl**

```bash
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test User","email":"newuser@test.com","password":"test123","confirmPassword":"test123"}'
# Expected: 200 + "Registration successful"

# Test password mismatch
curl -X POST http://localhost:8080/api/v1/auth/register \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Test","email":"x@y.com","password":"a","confirmPassword":"b"}'
# Expected: 400 + "Passwords do not match"
```

**Step 3: Commit (nếu có sửa)**

Không có thay đổi nếu logic đã đúng. Nếu cần sửa (vd: thêm logging, fix edge case), commit riêng.

**DoD:** ✅ Register REST hoạt động đúng.

---

#### Task 1.5 — Implement forgot-password thật

**Files:**
- Modify: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/controller/api/AuthRestController.java:122`

**Step 1: Inject EmailService**

```java
private final EmailService emailService;
```

**Step 2: Implement reset email**

```java
@PostMapping("/forgot-password")
public ResponseEntity<?> forgotPassword(@RequestParam String email) {
    User user = userService.getUserByEmail(email);
    if (user == null) {
        // KHÔNG tiết lộ email có tồn tại hay không (security best practice)
        return ResponseEntity.ok(new MessageResponse("Password reset email sent if email exists"));
    }
    
    // Generate token (random 32 bytes hex)
    String token = java.util.UUID.randomUUID().toString();
    
    // Lưu token vào DB (cần thêm bảng PasswordResetToken)
    // TODO: Batch sau sẽ refactor
    // Tạm thời log token ra console
    System.out.println("Reset token for " + email + ": " + token);
    
    // Send email
    String resetLink = "http://localhost:5173/reset-password?token=" + token;
    emailService.sendSimpleMessage(email, "Reset Password - TheXuong", 
        "Click vào link sau để reset password: " + resetLink);
    
    return ResponseEntity.ok(new MessageResponse("Password reset email sent if email exists"));
}
```

**Step 3: Commit**

```bash
git add src/main/java/com/example/thexuong/controller/api/AuthRestController.java
git commit -m "feat: forgot-password gửi email reset link (TODO: lưu token vào DB)"
```

**DoD:** ✅ Forgot-password gửi email (có thể log token thay vì lưu DB ở batch này).

---

#### Task 1.6 — Viết test cho AuthRestController

**Files:**
- Create: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/test/java/com/example/thexuong/controller/api/AuthRestControllerTest.java`

**Step 1: Tạo test file**

```java
package com.example.thexuong.controller.api;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AuthRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testGetCurrentUserWithoutLogin() throws Exception {
        mockMvc.perform(get("/api/v1/auth/user"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testRegisterValidation() throws Exception {
        String body = "{\"fullName\":\"Test\",\"email\":\"x\",\"password\":\"a\",\"confirmPassword\":\"b\"}";
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testForgotPassword() throws Exception {
        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .param("email", "user1@thexuong.com"))
                .andExpect(status().isOk());
    }
}
```

**Step 2: Run test**

```bash
cd "D:/FPT Polytechnic/JAVA/JAVA5/TheXuong"
./gradlew test --tests AuthRestControllerTest
# Expected: 3 tests passed
```

**Step 3: Commit**

```bash
git add src/test/java/com/example/thexuong/controller/api/AuthRestControllerTest.java
git commit -m "test: thêm AuthRestControllerTest (3 test case)"
```

**DoD:** ✅ 3/3 test pass, build pass.

---

### 📦 BATCH 2 — Product & Category REST bổ sung

**Track:** Backend only
**Mục tiêu:** Bổ sung REST variants + categories cho Vue Products/ProductDetail filter
**Phụ thuộc:** Batch 0
**Definition of Done:**
- ✅ `GET /api/v1/products/{id}/variants` trả variants riêng (cho ProductDetail.vue chọn size)
- ✅ `GET /api/v1/categories` trả list category (cho filter Products)
- ✅ `GET /api/v1/categories/sports` + `/brands` (đã có từ CategoryRestController, chỉ verify)
- ✅ 2 file test mới pass

**Lưu ý:** ProductRestController đã có `getProducts()`, `getProduct()`, `getNewProducts()`. Cần thêm variants endpoint riêng (hoặc đã có trong ProductResponse.variants — kiểm tra lại).

---

#### Task 2.1 — Verify ProductRestController hiện tại

**Files:**
- Read: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/controller/api/ProductRestController.java`

**Step 1: Đọc file**

Endpoint `getProduct(id)` đã trả `ProductResponse` có chứa `variants`. KHÔNG cần endpoint riêng `/products/{id}/variants` nếu ProductDetail.vue chỉ cần load 1 lần.

**Step 2: Báo cáo cho anh**

Nếu ProductResponse đã đủ → Task 2.1 DONE, không cần code thêm.
Nếu thiếu (vd: thiếu field `stock`, `color`, `image`) → sửa ProductResponse, commit riêng.

**DoD:** ✅ Audit xong, báo cáo field nào có/thiếu.

---

#### Task 2.2 — Tạo CategoryRestController (nếu chưa có)

**Files:**
- Read: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/controller/api/CategoryRestController.java`

**Step 1: Đọc file**

Verify có `/api/v1/categories/sports`, `/brands`, `/categories` endpoints. Nếu đã có → DONE. Nếu thiếu → thêm.

**Step 2: Bổ sung nếu thiếu**

```java
@GetMapping
public List<String> getAllCategories() {
    return productRepository.findAll().stream()
        .map(Product::getCategory)
        .filter(Objects::nonNull)
        .distinct()
        .sorted()
        .collect(Collectors.toList());
}
```

**Step 3: Commit (nếu có thay đổi)**

**DoD:** ✅ CategoryRestController có đủ endpoint cho filter.

---

#### Task 2.3 — Tạo ProductVariantRestController (nếu cần)

**Files:**
- Create (nếu cần): `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/controller/api/ProductVariantRestController.java`

**Lý do cần:** Nếu Vue ProductDetail cần load variants riêng (không qua ProductResponse), cần endpoint riêng.

**Step 1: Audit khi nào cần**

Nếu ProductResponse đã chứa variants → KHÔNG cần.
Nếu Vue cần endpoint riêng (vd: check stock real-time khi chọn size) → tạo.

**Step 2: Nếu cần tạo**

```java
@RestController
@RequestMapping("/api/v1/variants")
@RequiredArgsConstructor
public class ProductVariantRestController {
    private final ProductVariantRepository variantRepository;
    
    @GetMapping("/{id}")
    public ResponseEntity<?> getVariant(@PathVariable Long id) {
        return variantRepository.findById(id)
            .map(v -> ResponseEntity.ok(new VariantResponse(
                v.getId(),
                v.getSize().getName(),
                v.getQuantity()
            )))
            .orElse(ResponseEntity.notFound().build());
    }
    
    public record VariantResponse(Long id, String size, Integer quantity) {}
}
```

**Step 3: Commit**

**DoD:** ✅ Variant endpoint hoạt động (hoặc confirmed không cần).

---

#### Task 2.4 — Viết test cho Product & Category

**Files:**
- Create: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/test/java/com/example/thexuong/controller/api/ProductRestControllerTest.java`

**Step 1: Tạo test**

```java
@SpringBootTest
@AutoConfigureMockMvc
class ProductRestControllerTest {
    @Autowired private MockMvc mockMvc;
    
    @Test
    void testGetProductsList() throws Exception {
        mockMvc.perform(get("/api/v1/products?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }
    
    @Test
    void testGetProductDetail() throws Exception {
        mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }
    
    @Test
    void testGetNewProducts() throws Exception {
        mockMvc.perform(get("/api/v1/products/new?limit=4"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
```

**Step 2: Run test**

```bash
./gradlew test --tests ProductRestControllerTest
# Expected: 3 tests passed (nếu DB live + seed data có sẵn)
# Nếu DB không live → dùng @ActiveProfiles("test") với H2 in-memory (TODO Batch 5)
```

**Step 3: Commit**

**DoD:** ✅ Test pass (hoặc skip với `-x test` nếu DB không live).

---

### 📦 BATCH 3 — Orders, Checkout, Profile REST (THIẾU)

**Track:** Backend only
**Mục tiêu:** Tạo 5 endpoint mới: orders list/detail, checkout create, profile get/update
**Phụ thuộc:** Batch 0
**Definition of Done:**
- ✅ `GET /api/v1/orders` — list orders của user hiện tại
- ✅ `GET /api/v1/orders/{id}` — chi tiết order
- ✅ `POST /api/v1/checkout/create` — tạo order từ cart
- ✅ `GET /api/v1/profile` — get current user profile
- ✅ `PUT /api/v1/profile` — update profile
- ✅ 1 OrderRestController + 1 CheckoutRestController + 1 ProfileRestController
- ✅ 5 test pass

**Lưu ý:**
- Checkout hiện đã có `OrderController.vnpayReturn` + `OrderController.checkout` (trả Thymeleaf). Cần TÁCH thành REST mới + xóa method cũ (sẽ xóa hẳn ở Batch 7)
- Profile hiện có `ProfileController` (trả Thymeleaf). Tạo REST mới tương ứng
- Order sử dụng `OrderStatus` enum (đã refactor ở orderstatus.md Batch 0)

---

#### Task 3.1 — Tạo OrderRestController

**Files:**
- Create: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/controller/api/OrderRestController.java`

**Step 1: Tạo file**

```java
package com.example.thexuong.controller.api;

import com.example.thexuong.entity.Order;
import com.example.thexuong.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderRestController {
    private final OrderRepository orderRepository;
    
    @GetMapping
    public List<Order> getMyOrders() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return orderRepository.findByUserEmailOrderByCreatedAtDesc(email);
    }
    
    @GetMapping("/{id}")
    public Order getOrder(@PathVariable Long id) {
        // Verify ownership
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Order order = orderRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!order.getUser().getEmail().equals(email)) {
            throw new RuntimeException("Forbidden");
        }
        return order;
    }
}
```

**Step 2: Verify repository method tồn tại**

```bash
grep -n "findByUserEmail" src/main/java/com/example/thexuong/repository/OrderRepository.java
# Nếu KHÔNG có → thêm method vào repository
```

**Step 3: Commit**

```bash
git add src/main/java/com/example/thexuong/controller/api/OrderRestController.java
git add src/main/java/com/example/thexuong/repository/OrderRepository.java  # nếu có sửa
git commit -m "feat: OrderRestController (GET /api/v1/orders, /orders/{id})"
```

**DoD:** ✅ 2 endpoint orders hoạt động, test pass.

---

#### Task 3.2 — Tạo CheckoutRestController

**Files:**
- Create: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/controller/api/CheckoutRestController.java`

**Step 1: Tạo file**

```java
package com.example.thexuong.controller.api;

import com.example.thexuong.entity.Order;
import com.example.thexuong.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutRestController {
    private final OrderService orderService;
    
    public record CheckoutRequest(
        String shippingAddress,
        String phone,
        String note,
        String paymentMethod  // "COD" hoặc "VNPAY"
    ) {}
    
    public record CheckoutResponse(
        Long orderId,
        String status,
        String paymentUrl  // null nếu COD
    ) {}
    
    @PostMapping("/create")
    public ResponseEntity<?> create(@RequestBody CheckoutRequest request) {
        try {
            Order order = orderService.createOrderFromCart(
                request.shippingAddress(),
                request.phone(),
                request.note(),
                request.paymentMethod()
            );
            
            CheckoutResponse response = new CheckoutResponse(
                order.getId(),
                order.getStatus().name(),
                null  // VNPay URL sẽ handle ở batch sau
            );
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
```

**Step 2: Verify OrderService có method `createOrderFromCart`**

Nếu KHÔNG có → tạo method mới trong OrderService. Tương tự logic `OrderController.checkout()` cũ nhưng return Order entity thay vì redirect.

**Step 3: Commit**

```bash
git add src/main/java/com/example/thexuong/controller/api/CheckoutRestController.java
git add src/main/java/com/example/thexuong/service/OrderService.java  # nếu có sửa
git commit -m "feat: CheckoutRestController (POST /api/v1/checkout/create)"
```

**DoD:** ✅ Checkout tạo order thành công, test pass.

---

#### Task 3.3 — Tạo ProfileRestController

**Files:**
- Create: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/controller/api/ProfileRestController.java`

**Step 1: Tạo file**

```java
package com.example.thexuong.controller.api;

import com.example.thexuong.entity.User;
import com.example.thexuong.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class ProfileRestController {
    private final UserService userService;
    
    public record ProfileResponse(
        Long id,
        String username,
        String email,
        String fullName,
        String phone,
        String address
    ) {}
    
    public record UpdateProfileRequest(
        String fullName,
        String phone,
        String address,
        String currentPassword,  // optional, chỉ cần nếu đổi password
        String newPassword
    ) {}
    
    @GetMapping
    public ResponseEntity<?> getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.getUserByEmail(email);
        if (user == null) return ResponseEntity.notFound().build();
        
        return ResponseEntity.ok(new ProfileResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName(),
            user.getPhoneNumber(),
            user.getAddress()
        ));
    }
    
    @PutMapping
    public ResponseEntity<?> updateProfile(@RequestBody UpdateProfileRequest request) {
        try {
            String email = SecurityContextHolder.getContext().getAuthentication().getName();
            userService.updateProfile(email, request.fullName(), request.phone(), request.address());
            
            if (request.newPassword() != null && !request.newPassword().isBlank()) {
                userService.changePassword(email, request.currentPassword(), request.newPassword());
            }
            
            return ResponseEntity.ok(Map.of("message", "Profile updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
```

**Step 2: Verify UserService có method `updateProfile` + `changePassword`**

Nếu KHÔNG có → tạo method mới.

**Step 3: Commit**

```bash
git add src/main/java/com/example/thexuong/controller/api/ProfileRestController.java
git add src/main/java/com/example/thexuong/service/UserService.java  # nếu có sửa
git commit -m "feat: ProfileRestController (GET/PUT /api/v1/profile)"
```

**DoD:** ✅ Profile get/update hoạt động, test pass.

---

#### Task 3.4 — SecurityConfig permitAll cho checkout (nếu cần)

**Files:**
- Modify: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/config/SecurityConfig.java`

**Step 1: Verify `/api/v1/checkout/**` cần authenticated**

Có thể đã có sẵn rule. Verify bằng curl:

```bash
# Cần login trước
curl -X POST http://localhost:8080/api/v1/checkout/create -b cookies.txt \
  -H "Content-Type: application/json" \
  -d '{"shippingAddress":"...","phone":"...","paymentMethod":"COD"}'
# Expected: 200 (nếu authenticated) hoặc 401 (nếu chưa login)
```

**Step 2: Nếu cần permitAll hoặc authenticated rule → thêm**

**Step 3: Commit (nếu có sửa)**

**DoD:** ✅ Checkout endpoint protected đúng.

---

#### Task 3.5 — SecurityConfig cho orders + profile

Tương tự Task 3.4. Verify 2 endpoint cần authenticated.

**DoD:** ✅ Auth rules đúng.

---

#### Task 3.6 — Viết test cho Orders/Checkout/Profile

**Files:**
- Create: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/test/java/com/example/thexuong/controller/api/OrderRestControllerTest.java`
- Create: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/test/java/com/example/thexuong/controller/api/CheckoutRestControllerTest.java`
- Create: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/test/java/com/example/thexuong/controller/api/ProfileRestControllerTest.java`

**Step 1: Tạo 3 file test (template giống Batch 1.6)**

Mỗi file test 2-3 case: get list, get detail, create/update.

**Step 2: Run tests**

```bash
./gradlew test --tests "OrderRestControllerTest,CheckoutRestControllerTest,ProfileRestControllerTest"
# Expected: 6-9 tests passed
```

**Step 3: Commit**

```bash
git add src/test/java/com/example/thexuong/controller/api/OrderRestControllerTest.java
git add src/test/java/com/example/thexuong/controller/api/CheckoutRestControllerTest.java
git add src/test/java/com/example/thexuong/controller/api/ProfileRestControllerTest.java
git commit -m "test: thêm test cho Orders/Checkout/Profile REST"
```

**DoD:** ✅ 6-9 test pass, build pass.

---

### 📦 BATCH 4 — Favorite REST (mới hoàn toàn)

**Track:** Backend only
**Mục tiêu:** Tạo entity Favorite + 3 endpoint REST
**Phụ thuộc:** Batch 0
**Definition of Done:**
- ✅ Entity `Favorite` (id, user_id, product_id, created_at)
- ✅ Repository `FavoriteRepository` (findByUserId, findByUserIdAndProductId, deleteByUserIdAndProductId)
- ✅ `FavoriteRestController`: GET /api/v1/favorites, POST /api/v1/favorites, DELETE /api/v1/favorites/{productId}
- ✅ 1 migration SQL tạo bảng Favorites
- ✅ 3 test pass

**Lưu ý:** Tính năng Favorite là MỚI hoàn toàn (chưa có entity, không có trong Thymeleaf). Plan cho Favorite.vue trong Batch 6 sẽ consume các endpoint này.

---

#### Task 4.1 — Tạo Favorite entity

**Files:**
- Create: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/entity/Favorite.java`

**Step 1: Tạo file**

```java
package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "favorites", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "product_id"})
})
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Favorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
```

**Step 2: Commit**

```bash
git add src/main/java/com/example/thexuong/entity/Favorite.java
git commit -m "feat: entity Favorite (user_id, product_id, created_at)"
```

**DoD:** ✅ Compile pass.

---

#### Task 4.2 — Tạo FavoriteRepository

**Files:**
- Create: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/repository/FavoriteRepository.java`

**Step 1: Tạo file**

```java
package com.example.thexuong.repository;

import com.example.thexuong.entity.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface FavoriteRepository extends JpaRepository<Favorite, Long> {
    List<Favorite> findByUserIdOrderByCreatedAtDesc(Long userId);
    Optional<Favorite> findByUserIdAndProductId(Long userId, Long productId);
    
    @Modifying
    @Query("DELETE FROM Favorite f WHERE f.user.id = :userId AND f.product.id = :productId")
    void deleteByUserIdAndProductId(@Param("userId") Long userId, @Param("productId") Long productId);
}
```

**Step 2: Commit**

```bash
git add src/main/java/com/example/thexuong/repository/FavoriteRepository.java
git commit -m "feat: FavoriteRepository (findByUser, delete custom)"
```

**DoD:** ✅ Compile pass.

---

#### Task 4.3 — Tạo FavoriteRestController

**Files:**
- Create: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/controller/api/FavoriteRestController.java`

**Step 1: Tạo file**

```java
package com.example.thexuong.controller.api;

import com.example.thexuong.entity.Favorite;
import com.example.thexuong.entity.Product;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.FavoriteRepository;
import com.example.thexuong.repository.ProductRepository;
import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteRestController {
    private final FavoriteRepository favoriteRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    
    public record FavoriteResponse(
        Long id,
        Long productId,
        String productName,
        Double price,
        String imageUrl
    ) {}
    
    @GetMapping
    public List<FavoriteResponse> getFavorites() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return favoriteRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
            .stream()
            .map(f -> new FavoriteResponse(
                f.getId(),
                f.getProduct().getId(),
                f.getProduct().getName(),
                f.getProduct().getPrice() != null ? f.getProduct().getPrice().doubleValue() : null,
                f.getProduct().getImageUrl()
            ))
            .toList();
    }
    
    @PostMapping
    public ResponseEntity<?> addFavorite(@RequestBody Map<String, Long> body) {
        Long productId = body.get("productId");
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        Product product = productRepository.findById(productId).orElseThrow();
        
        // Check duplicate
        if (favoriteRepository.findByUserIdAndProductId(user.getId(), productId).isPresent()) {
            return ResponseEntity.ok(Map.of("message", "Already in favorites"));
        }
        
        Favorite favorite = Favorite.builder().user(user).product(product).build();
        favoriteRepository.save(favorite);
        return ResponseEntity.ok(Map.of("message", "Added to favorites"));
    }
    
    @DeleteMapping("/{productId}")
    public ResponseEntity<?> removeFavorite(@PathVariable Long productId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        favoriteRepository.deleteByUserIdAndProductId(user.getId(), productId);
        return ResponseEntity.ok(Map.of("message", "Removed from favorites"));
    }
}
```

**Step 2: Commit**

```bash
git add src/main/java/com/example/thexuong/controller/api/FavoriteRestController.java
git commit -m "feat: FavoriteRestController (GET/POST/DELETE /api/v1/favorites)"
```

**DoD:** ✅ 3 endpoint hoạt động, test pass.

---

#### Task 4.4 — Migration SQL tạo bảng Favorites

**Files:**
- Modify: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/dbTheXuong.sql`

**Step 1: Thêm bảng**

```sql
CREATE TABLE favorites (
    id BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT GETDATE(),
    CONSTRAINT FK_favorites_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT FK_favorites_product FOREIGN KEY (product_id) REFERENCES products(id),
    CONSTRAINT UQ_favorites_user_product UNIQUE (user_id, product_id)
);

CREATE INDEX IX_favorites_user ON favorites(user_id);
```

**Step 2: Commit**

```bash
git add dbTheXuong.sql
git commit -m "feat: migration tạo bảng favorites"
```

**DoD:** ✅ SQL script sẵn sàng, manual apply OK.

---

#### Task 4.5 — Test FavoriteRestController

**Files:**
- Create: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/test/java/com/example/thexuong/controller/api/FavoriteRestControllerTest.java`

**Step 1: Tạo test**

```java
@SpringBootTest
@AutoConfigureMockMvc
class FavoriteRestControllerTest {
    @Autowired private MockMvc mockMvc;
    
    @Test
    void testGetFavoritesUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/favorites"))
                .andExpect(status().isUnauthorized());
    }
    
    // Thêm 2 test nữa: addFavorite, removeFavorite với @WithMockUser
}
```

**Step 2: Run test**

**Step 3: Commit**

**DoD:** ✅ 3 test pass.

---

### 📦 BATCH 5 — Admin REST bổ sung

**Track:** Backend only
**Mục tiêu:** Hoàn thiện Admin REST cho AdminOrders, AdminStatistics, AdminProductCRUD
**Phụ thuộc:** Batch 0
**Definition of Done:**
- ✅ `GET /api/v1/admin/orders` — list all orders (admin only)
- ✅ `PUT /api/v1/admin/orders/{id}/status` — update order status
- ✅ `GET /api/v1/admin/statistics` — dashboard data (revenue, order count, user count)
- ✅ `POST /api/v1/admin/products` — create product
- ✅ `PUT /api/v1/admin/products/{id}` — update product
- ✅ `DELETE /api/v1/admin/products/{id}` — delete product
- ✅ 5-8 test pass

**Lưu ý:**
- Admin endpoints cần `@PreAuthorize("hasAnyRole('ADMIN', 'BOTH')")` ở controller hoặc SecurityConfig rule
- Dashboard data dùng `DashboardController` (đã có sẵn từ orderstatus.md Batch 3) — cần adapt thành REST JSON thay vì trả Thymeleaf

---

#### Task 5.1 — Tạo AdminOrderRestController

**Files:**
- Create: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/controller/api/AdminOrderRestController.java`

**Step 1: Tạo file**

```java
package com.example.thexuong.controller.api;

import com.example.thexuong.entity.Order;
import com.example.thexuong.entity.OrderStatus;
import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/orders")
@PreAuthorize("hasAnyRole('ADMIN', 'BOTH')")
@RequiredArgsConstructor
public class AdminOrderRestController {
    private final OrderRepository orderRepository;
    private final OrderService orderService;
    
    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
    
    @PutMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            OrderStatus newStatus = OrderStatus.valueOf(body.get("status"));
            orderService.updateOrderStatus(id, newStatus);
            return ResponseEntity.ok(Map.of("message", "Status updated"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
```

**Step 2: Verify OrderService có `updateOrderStatus`**

Nếu KHÔNG có → tạo (tương tự logic `OrderManagementController` cũ nhưng return Order thay vì redirect).

**Step 3: Commit**

```bash
git add src/main/java/com/example/thexuong/controller/api/AdminOrderRestController.java
git add src/main/java/com/example/thexuong/service/OrderService.java  # nếu có sửa
git commit -m "feat: AdminOrderRestController (GET all, PUT status)"
```

**DoD:** ✅ 2 endpoint admin orders hoạt động, test pass.

---

#### Task 5.2 — Tạo AdminStatisticsRestController

**Files:**
- Create: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/controller/api/AdminStatisticsRestController.java`

**Step 1: Đọc DashboardController cũ**

```bash
cat src/main/java/com/example/thexuong/controller/DashboardController.java
```

**Step 2: Adapt thành REST**

```java
package com.example.thexuong.controller.api;

import com.example.thexuong.repository.OrderRepository;
import com.example.thexuong.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin/statistics")
@PreAuthorize("hasAnyRole('ADMIN', 'BOTH')")
@RequiredArgsConstructor
public class AdminStatisticsRestController {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    
    public record StatisticsResponse(
        double totalRevenue,
        long totalOrders,
        long totalUsers,
        long totalCustomers,
        List<Object[]> revenueByDay,
        Map<String, Long> ordersByStatus
    ) {}
    
    @GetMapping
    public StatisticsResponse getStatistics(
        @RequestParam(required = false) LocalDate startDate,
        @RequestParam(required = false) LocalDate endDate
    ) {
        // Tính toán từ repository
        // ...
        return new StatisticsResponse(...);
    }
}
```

**Step 3: Commit**

**DoD:** ✅ AdminStatistics trả JSON đầy đủ.

---

#### Task 5.3 — Tạo AdminProductRestController

**Files:**
- Create: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/controller/api/AdminProductRestController.java`

**Step 1: Tạo file với 3 method (POST/PUT/DELETE)**

Tương tự pattern AdminProductController cũ nhưng return JSON thay vì redirect.

**Step 2: Commit**

**DoD:** ✅ AdminProduct CRUD hoạt động.

---

#### Task 5.4 — SecurityConfig cho admin endpoints

**Files:**
- Modify: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/src/main/java/com/example/thexuong/config/SecurityConfig.java`

**Step 1: Enable @PreAuthorize**

```java
@EnableMethodSecurity(prePostEnabled = true)
```

**Step 2: Verify rule admin**

`/api/v1/admin/**` cần role ADMIN hoặc BOTH. Đã có sẵn trong SecurityConfig hay cần thêm.

**Step 3: Commit (nếu có sửa)**

**DoD:** ✅ Admin endpoint protected đúng.

---

#### Task 5.5-5.8 — Tests cho admin REST

Tương tự các batch trước. Tạo 3-4 file test cho AdminOrder, AdminStatistics, AdminProduct.

**DoD:** ✅ 8-12 test pass, build pass.

---

### 📦 BATCH 6 — Vue Customer (11 trang) Figma-driven

**Track:** Frontend only
**Mục tiêu:** Migrate 11 trang customer từ Thymeleaf sang Vue 3
**Phụ thuộc:** Batch 0-5
**Definition of Done:**
- ✅ 11 view Vue render đúng với Figma
- ✅ 11 file `frontend/src/api/*.ts` (1 file/resource) consume REST từ Batch 1-5
- ✅ Auth flow: login → token → request authenticated → logout
- ✅ Vue Router guard hoạt động (public/customer/admin)
- ✅ Pinia store: auth, cart, favorite, orders
- ✅ `npm run build` pass
- ✅ `./gradlew processResources` copy dist thành công

**Lưu ý CỰC KỲ QUAN TRỌNG:**
- **Figma workflow 6 bước** (skill spring-thymeleaf-to-vue3) BẮT BUỘC cho MỖI trang
- Mỗi task = 1 trang Vue = 1 commit + 1 Task Report (format anh đã chốt ở câu 8)
- Nếu Figma lỗi / không đọc → DỪNG + báo cáo, KHÔNG tự ý design
- KHÔNG xóa Thymeleaf ở batch này — chỉ migrate Vue

**Thứ tự migrate** (theo critical path + dependency):

1. **Home** (đã có skeleton, cần Figma fidelity check)
2. **Login** (cần auth flow hoàn chỉnh)
3. **Register** (cùng auth)
4. **ForgotPassword** (cùng auth)
5. **Products** (list với filter)
6. **ProductDetail** (cần variants)
7. **Cart** (cần cart REST)
8. **Checkout** (cần checkout REST)
9. **Orders** (list)
10. **OrderDetail**
11. **Profile** (get/update)
12. **Favorite** (mới, cần Favorite REST)

---

#### Task 6.1 — Migrate Home.vue theo Figma

**Files:**
- Modify: `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/frontend/src/views/Home.vue`
- Create (nếu chưa có): `D:/FPT Polytechnic/JAVA/JAVA5/TheXuong/frontend/src/api/products.ts`

**Figma cần:** Desktop Home + Mobile Home + 4 trust brand logos + 4 product card

**Step 1: Báo cáo Figma selection cần**

Em báo: "Trang Home cần Figma selection cho: (a) Hero section desktop, (b) Hero section mobile, (c) Trust brands marquee, (d) Bento grid features 2x2, (e) New products grid (4 items). Gửi screenshot + CSS all layer từng phần."

**Step 2: Anh gửi Figma + copy CSS**

(Anh gửi)

**Step 3: Em đọc + báo cáo**

- Layout: grid/flex, breakpoints
- Color tokens: primary, secondary, accent
- Font: Geist (sans), Gelasio (serif) - đã có
- Spacing: px values
- Điểm conflict/mơ hồ: (nếu có)

**Step 4: Checkpoint**

DỪNG + hỏi anh nếu cần.

**Step 5: Code theo Figma**

```vue
<template>
  <!-- Sử dụng ĐÚNG px/hex/radius từ Figma -->
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { fetchNewProducts } from '@/api/products'
import type { Product } from '@/types'

const products = ref<Product[]>([])

onMounted(async () => {
  products.value = await fetchNewProducts(4)
})
</script>
```

**Step 6: Screenshot + so sánh Figma**

- Desktop (1280px): X% match
- Mobile (375px): Y% match
- Điểm khác: (nếu có)

**Step 7: Commit + Task Report**

```bash
git add frontend/src/views/Home.vue
git add frontend/src/api/products.ts  # nếu tạo mới
git commit -m "feat: Home.vue theo Figma (hero + trust brands + bento + new products)"
```

**Task Report format** (xem §3.3).

**DoD:** ✅ Home render đúng Figma, consume `GET /api/v1/products/new`, build pass.

---

#### Task 6.2 — Migrate Login.vue

(Pattern tương tự Task 6.1, theo Figma)

**API consume:**
- `POST /api/v1/auth/login` (email + password)
- Sau login → set Pinia auth store → redirect to `?redirect=` query hoặc `/`

**DoD:** ✅ Login flow hoàn chỉnh, lưu user vào Pinia, redirect đúng.

---

#### Task 6.3 — Migrate Register.vue

**API:** `POST /api/v1/auth/register`

**DoD:** ✅ Register form + validation + redirect to login.

---

#### Task 6.4 — Migrate ForgotPassword.vue

**API:** `POST /api/v1/auth/forgot-password`

**DoD:** ✅ Form nhập email + submit + thông báo.

---

#### Task 6.5 — Migrate Products.vue

**API:**
- `GET /api/v1/products?keyword=&sport=&brand=&sort=&page=&size=`
- `GET /api/v1/categories/sports`, `/brands`

**DoD:** ✅ Filter + sort + pagination hoạt động.

---

#### Task 6.6 — Migrate ProductDetail.vue

**API:** `GET /api/v1/products/{id}` (đã có variants)

**DoD:** ✅ Chọn size + add to cart hoạt động.

---

#### Task 6.7 — Migrate Cart.vue

**API:**
- `GET /api/v1/cart`
- `PUT /api/v1/cart/items/{id}` (update qty)
- `DELETE /api/v1/cart/items/{id}` (remove)
- `POST /api/v1/cart/items` (add - từ ProductDetail)

**DoD:** ✅ Cart CRUD hoạt động, hiển thị tổng tiền.

---

#### Task 6.8 — Migrate Checkout.vue

**API:**
- `POST /api/v1/checkout/create`
- (VNPay flow sẽ handle ở batch sau nếu cần)

**DoD:** ✅ Form shipping + payment method + submit → order created.

---

#### Task 6.9 — Migrate Orders.vue

**API:** `GET /api/v1/orders`

**DoD:** ✅ List orders + filter theo status.

---

#### Task 6.10 — Migrate OrderDetail.vue

**API:** `GET /api/v1/orders/{id}`

**DoD:** ✅ Hiển thị chi tiết order + status timeline.

---

#### Task 6.11 — Migrate Profile.vue

**API:**
- `GET /api/v1/profile`
- `PUT /api/v1/profile`

**DoD:** ✅ Form edit + update thành công.

---

#### Task 6.12 — Migrate Favorite.vue

**API:**
- `GET /api/v1/favorites`
- `POST /api/v1/favorites` (add)
- `DELETE /api/v1/favorites/{productId}` (remove)

**DoD:** ✅ List favorites + remove hoạt động.

---

### 📦 BATCH 7 — Vue Admin (5 trang) + Cleanup Thymeleaf

**Track:** Frontend + Backend
**Mục tiêu:** Migrate 5 admin view + XÓA HẾT Thymeleaf (templates, dep, controller)
**Phụ thuộc:** Batch 0-6 + Vue customer đã test thủ công
**Definition of Done:**
- ✅ 5 admin view render đúng
- ✅ `npm run build` pass + manual test 16 view (11 customer + 5 admin)
- ✅ Folder `src/main/resources/templates/` ĐÃ XÓA
- ✅ `spring-boot-starter-thymeleaf` + `thymeleaf-extras-springsecurity6` ĐÃ XÓA khỏi `build.gradle`
- ✅ Mọi `@Controller` trả String view ĐÃ XÓA
- ✅ `SecurityConfig` đã bỏ rule cho Thymeleaf
- ✅ `./gradlew build` pass
- ✅ `./gradlew bootRun` chạy, truy cập `http://localhost:8080/` → thấy Vue app

**Lưu ý CỰC KỲ QUAN TRỌNG:**
- **GATE trước batch này:** 11 trang customer ở Batch 6 đã test thủ công + không còn bug blocker
- Nếu Vue chưa stable → KHÔNG xóa Thymeleaf
- Xóa Thymeleaf là 1 commit lớn cuối cùng, có thể cần chia nhỏ:
  - Commit 1: xóa folder `templates/`
  - Commit 2: xóa thymeleaf dep
  - Commit 3: xóa MVC controller
  - Commit 4: cleanup SecurityConfig + application.yml

---

#### Task 7.1 — Migrate AdminProducts.vue

**Figma cần:** Admin products list (table + filter + pagination + "Tạo mới" button)

**API:** `GET /api/v1/admin/products` (cần thêm ở Batch 5 nếu chưa có)

**DoD:** ✅ List products + filter + click "Edit" → AdminProductEdit.

---

#### Task 7.2 — Migrate AdminProductEdit.vue

**Figma cần:** Product form (create + edit)

**API:**
- `GET /api/v1/admin/products/{id}` (load for edit)
- `POST /api/v1/admin/products` (create)
- `PUT /api/v1/admin/products/{id}` (update)

**DoD:** ✅ Form create/edit hoạt động, validation đầy đủ.

---

#### Task 7.3 — Migrate AdminOrders.vue

**Figma cần:** Admin orders table (status filter + sort)

**API:**
- `GET /api/v1/admin/orders`
- `PUT /api/v1/admin/orders/{id}/status` (update status)

**DoD:** ✅ List + filter + update status.

---

#### Task 7.4 — Migrate AdminUsers.vue

**Figma cần:** Admin users table (toggle active + role)

**API:** `GET /api/v1/admin/users` + `PATCH /api/v1/admin/users/{id}/toggle-active` (đã có)

**DoD:** ✅ List users + toggle active + thấy thay đổi.

---

#### Task 7.5 — Migrate AdminStatistics.vue

**Figma cần:** Dashboard charts (revenue, orders, users)

**API:** `GET /api/v1/admin/statistics`

**DoD:** ✅ Charts render đúng với data thật (dùng Chart.js hoặc ApexCharts).

---

#### Task 7.6 — Cleanup Thymeleaf (4 sub-commit)

**Gate:** Tất cả 16 view (11 customer + 5 admin) đã test thủ công, `npm run build` pass.

**Sub-task 7.6.1: Xóa folder templates/**

```bash
cd "D:/FPT Polytechnic/JAVA/JAVA5/TheXuong"
git rm -r src/main/resources/templates/
# Verify
ls src/main/resources/templates/ 2>&1
# Expected: No such file or directory
git status --short
# Expected: D src/main/resources/templates/...
git commit -m "chore: xóa toàn bộ Thymeleaf templates (16 file)"
```

**Sub-task 7.6.2: Xóa thymeleaf dep**

```bash
# Sửa build.gradle: xóa 2 dòng
# implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'  (dòng 71)
# implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'  (dòng 74 duplicate)
# implementation 'org.thymeleaf.extras:thymeleaf-extras-springsecurity6'  (dòng 78, đã xóa ở Batch 0)

cd "D:/FPT Polytechnic/JAVA/JAVA5/TheXuong"
git add build.gradle
./gradlew build
# Expected: BUILD SUCCESSFUL
git commit -m "chore: xóa spring-boot-starter-thymeleaf dep"
```

**Sub-task 7.6.3: Xóa MVC Controller trả String view**

Các controller cần xóa (dựa trên audit):
- `AuthController` (trả login/register template)
- `ProductController` (trả products/product-detail)
- `CartController` (trả cart template)
- `CheckoutController` (trả checkout + vnpayReturn)
- `OrderController` (trả my-orders/my-order-detail + vnpayReturn)
- `OrderManagementController` (trả admin/orders - KHÔNG xóa nếu admin/orders.html bị loyalty dùng)
- `ProfileController` (trả profile)
- `AdminProductController` (trả admin/products)
- `ForgotPasswordController` (trả forgot-password)
- `EmailController` (verify - có thể chỉ là REST)
- `GlobalControllerAdvice` (có thể chỉ là @ControllerAdvice không trả view)

**Lưu ý:** KHÔNG xóa `OrderManagementController` nếu `templates/admin/orders.html` đang được loyalty/voucher dùng (branch `feat/batch-5-cron-email-report`). Plan sẽ migrate admin orders ở đợt 2 sau khi loyalty merge.

```bash
# Xóa từng file
cd "D:/FPT Polytechnic/JAVA/JAVA5/TheXuong"
git rm src/main/java/com/example/thexuong/controller/AuthController.java
git rm src/main/java/com/example/thexuong/controller/ProductController.java
git rm src/main/java/com/example/thexuong/controller/CartController.java
git rm src/main/java/com/example/thexuong/controller/CheckoutController.java
git rm src/main/java/com/example/thexuong/controller/OrderController.java
git rm src/main/java/com/example/thexuong/controller/ProfileController.java
git rm src/main/java/com/example/thexuong/controller/AdminProductController.java
git rm src/main/java/com/example/thexuong/controller/ForgotPasswordController.java
# ... (tùy audit thực tế)
./gradlew compileJava
# Expected: BUILD SUCCESSFUL (vì REST controllers mới thay thế)
git commit -m "chore: xóa MVC controllers trả Thymeleaf view (8 file)"
```

**Sub-task 7.6.4: Cleanup SecurityConfig + application.yml**

```bash
# SecurityConfig: bỏ mọi rule permitAll cho /login, /register (form login cũ)
# application.yml: xóa spring.thymeleaf.* config (nếu có)
cd "D:/FPT Polytechnic/JAVA/JAVA5/TheXuong"
git add src/main/java/com/example/thexuong/config/SecurityConfig.java
git add src/main/resources/application.yml  # nếu có sửa
./gradlew build
# Expected: BUILD SUCCESSFUL
git commit -m "chore: cleanup SecurityConfig + application.yml sau khi xóa Thymeleaf"
```

**Sub-task 7.6.5: Verify end-to-end**

```bash
cd "D:/FPT Polytechnic/JAVA/JAVA5/TheXuong"
./gradlew clean build
# Expected: BUILD SUCCESSFUL

# Run app
./gradlew bootRun --no-daemon
# (Đợi 10s, Spring Boot start)

# Test từ curl
curl -I http://localhost:8080/
# Expected: 200 OK + Content-Type: text/html
# (Hoặc cần /frontend/index.html nếu Vite build copy đúng)

curl -I http://localhost:8080/api/v1/products
# Expected: 200 OK + Content-Type: application/json

curl -I http://localhost:8080/login  # Cũ
# Expected: 404 Not Found (vì đã xóa AuthController)
```

**DoD:** ✅ Tất cả curl trả status đúng, không có Thymeleaf error log.

---

#### Task 7.7 — Commit docs cuối + update tracking

```bash
cd "D:/FPT Polytechnic/JAVA/JAVA5/TheXuong"
git add frontend-migration.md
git commit -m "docs: cập nhật tracking Batch 7 (100% DONE) + báo cáo chi tiết"
git push origin feat/m7-vue-admin-cleanup  # nếu có remote
```

**DoD:** ✅ Plan file updated, branch sẵn sàng merge.

---

## 5. Risks & Trade-offs

### 5.1. Rủi ro cao

| # | Rủi ro | Xác suất | Tác động | Cách giảm thiểu |
|---|---|---|---|---|
| **R1** | Figma không đọc được / data rỗng | Trung bình | Block toàn bộ Batch 6-7 | Workflow 6 bước BẮT BUỘC, DỪNG khi lỗi, hỏi anh |
| **R2** | REST API backend chưa đủ (còn thiếu endpoint ngoài 9 đã list) | Thấp | Block 1-2 trang Vue | Em audit kỹ trong Batch 2-5, bổ sung ngay khi phát hiện |
| **R3** | Session-based auth + CSRF conflict với Vue SPA | Thấp | Login fail liên tục | Dùng `CookieCsrfTokenRepository.withHttpOnlyFalse()` (skill pitfall #1) |
| **R4** | Workspace bẩn (25 modified + 5 untracked) kéo theo commit | Cao | Commit lẫn file session cũ | Stage chính xác path, KHÔNG `git add .` (skill pitfall 1) |
| **R5** | Loyalty/voucher merge conflict với plan này | Trung bình | Conflict ở SecurityConfig hoặc OrderController | Branch tách biệt, merge tuần tự |
| **R6** | Test fail vì SQL Server không live | Cao (môi trường user) | `./gradlew test` fail | Dùng `./gradlew build -x test`, manual test khi có DB |
| **R7** | Subagent silent fail (skill pitfall 15) | Trung bình | Task không hoàn thành | Khi task đơn giản < 50 dòng → inline thay vì subagent |
| **R8** | Xóa Thymeleaf quá sớm (Vue chưa stable) | Trung bình | Không rollback được | Gate: 11 trang customer test thủ công xong mới xóa |
| **R9** | VNPay flow phức tạp, chưa handle ở CheckoutRestController | Cao | Checkout fail khi chọn VNPay | Batch 7 chỉ support COD, VNPay làm ở batch riêng sau |
| **R10** | Admin product CRUD có nhiều field phức tạp (variants, images) | Trung bình | Form quá dài, UI xấu | Chia form thành nhiều bước (wizard) hoặc làm đơn giản trước |

### 5.2. Trade-offs

| Decision | Alternative | Lý do chọn |
|---|---|---|
| **Xóa Thymeleaf 1 lần (Batch 7)** | Xóa 2 đợt | Khớp yêu cầu "tuyệt đối không dùng Thymeleaf". Rủi ro: gate Vue stable |
| **Customer trước (Batch 6)** | Admin trước | Khớp câu trả lời của anh |
| **8 batch nhỏ** | 4 batch lớn | Khớp câu trả lời của anh (ưa review sát) |
| **Báo cáo ngắn per trang** | Task Report dài | Khớp câu trả lời của anh (câu 8) |
| **Favorite là entity mới** | Favorite lưu localStorage Vue | Cần đồng bộ giữa các device → cần entity |
| **VNPay để Batch sau** | Làm trong Batch 6.8 | Cần thêm time test VNPay sandbox, không nên block frontend |
| **Không xóa `OrderManagementController` ở Batch 7** | Xóa hết | Loyalty đang dùng cho admin/orders.html, đợi merge xong |

### 5.3. Câu hỏi mở (cần anh trả lời trước khi chạy)

| # | Câu hỏi | Default nếu không trả lời |
|---|---|---|
| Q1 | Figma cho 11 trang customer: gửi theo thứ tự nào? | Anh gửi theo thứ tự Task 6.1 → 6.12 (Home trước) |
| Q2 | VNPay flow: tích hợp vào Batch 6.8 hay để batch sau? | Batch sau (chỉ COD ở Batch 6) |
| Q3 | Admin product CRUD: làm đầy đủ variants + images hay MVP trước? | MVP (chỉ field chính) |
| Q4 | Khi 2 admin user có cùng role, cần phân quyền chi tiết không? | Không (chỉ ADMIN/BOTH) |
| Q5 | Test infrastructure: H2 in-memory hay Testcontainers hay manual test? | Manual test (DB live) |
| Q6 | Có cần audit log cho admin actions (xóa user, update order) không? | Không (làm ở feature riêng) |
| Q7 | Mobile-first hay Desktop-first CSS? | Mobile-first (đã chốt trong MIGRATION_PLAN_VUE_3.md) |
| Q8 | Pinia store: 1 store lớn hay nhiều store nhỏ (auth, cart, orders, favorite)? | Nhiều store nhỏ (cleaner) |

---

## 6. Definition of Done (toàn plan)

### 6.1. Toàn plan hoàn thành khi:

- ✅ 8 batch đã merge vào main
- ✅ 16 view Vue (11 customer + 5 admin) render đúng với Figma
- ✅ 0 file Thymeleaf còn lại trong repo
- ✅ 0 MVC controller trả String view còn lại
- ✅ 0 dep Thymeleaf trong build.gradle
- ✅ `./gradlew build` pass
- ✅ `./gradlew bootRun` chạy, `http://localhost:8080/` trả Vue app
- ✅ Auth flow (login → request → logout) hoạt động end-to-end
- ✅ Test coverage ≥ 60% cho REST controllers mới
- ✅ Manual test toàn bộ 16 trang trên Chrome + Firefox + mobile (375px)

### 6.2. Mỗi trang Vue xong khi:

- ✅ Figma match ≥ 90% (desktop + mobile)
- ✅ Loading + error + empty state đầy đủ
- ✅ Browser console sạch (0 error đỏ)
- ✅ Responsive: 375px / 768px / 1280px+
- ✅ Commit message tiếng Việt, Conventional Commits
- ✅ Task Report ngắn đã gửi anh review

---

## 📊 TRACKING TIẾN ĐỘ CÁC BATCH

| # | Batch | Track | Trạng thái | % | Commits | Ngày xong | Ghi chú |
|---|---|---|---|---|---|---|---|
| **0** | Foundation & Audit Gate | Backend | ⏳ PENDING | 0% | — | — | 5 task. WebMvcConfig + SecurityConfig SPA + verify Vue dev build |
| **1** | Auth REST chuẩn hoá | Backend | ⏳ PENDING | 0% | — | — | 6 task. Login/Logout/ForgotPassword thật + 3 test |
| **2** | Product & Category REST bổ sung | Backend | ⏳ PENDING | 0% | — | — | 4 task. Variants + categories filter |
| **3** | Orders, Checkout, Profile REST | Backend | ⏳ PENDING | 0% | — | — | 7 task. 5 endpoint mới + 6-9 test |
| **4** | Favorite REST (mới) | Backend | ⏳ PENDING | 0% | — | — | 5 task. Entity + repo + 3 endpoint + 3 test |
| **5** | Admin REST bổ sung | Backend | ⏳ PENDING | 0% | — | — | 8 task. AdminOrders + AdminStatistics + AdminProductCRUD |
| **6** | Vue Customer (11 trang) | Frontend | ⏳ PENDING | 0% | — | — | 12 task. Figma-driven, 1 commit/trang |
| **7** | Vue Admin (5 trang) + Cleanup Thymeleaf | Frontend+Backend | ⏳ PENDING | 0% | — | — | 8 task. 5 admin view + xóa templates/dep/controller |

**Tổng:** 8 batch, 54 task. Đang chờ anh duyệt plan + Figma selection cho Batch 0-5 (backend có thể bắt đầu ngay, không cần Figma).

---

## 7. Commit convention (áp dụng cho mọi task trong plan)

```
feat: <mô tả ngắn tiếng Việt>     # Tính năng mới
fix: <mô tả ngắn tiếng Việt>      # Sửa bug
refactor: <mô tả ngắn>             # Refactor không đổi behavior
chore: <mô tả ngắn>                # Build/cleanup/dep
test: <mô tả ngắn>                 # Thêm/sửa test
docs: <mô tả ngắn>                 # Cập nhật doc/plan
```

**Ví dụ:**
- `feat: thêm WebMvcConfig serve static Vue + SPA routing support`
- `feat: implement login REST thật (Spring Security authenticate + session)`
- `feat: Home.vue theo Figma (hero + trust brands + bento + new products)`
- `chore: xóa toàn bộ Thymeleaf templates (16 file)`
- `docs: cập nhật tracking Batch 6 (50% DONE) + báo cáo chi tiết`

---

## 8. Quy tắc DỪNG

Agent DỪNG ngay và báo cáo anh khi:

1. **Task FAIL sau 2 lần retry** — root cause không rõ hoặc không tự fix được
2. **Build fail không recover được** — `./gradlew build` exit ≠ 0, đã thử `./gradlew clean build`
3. **Test fail > 50%** — dù đã verify setup đúng
4. **Figma không đọc được / conflict** — không tự ý design thay
5. **Cần owner quyết định open question không có default** — vd: đổi tech stack, đổi flow nghiệp vụ
6. **Phát hiện bug nghiêm trọng ngoài scope plan** — vd: phát hiện lỗi VNPay set PENDING (đã fix ở orderstatus.md Batch 0)
7. **Merge conflict với loyalty/voucher** — không tự ý giải quyết
8. **Workspace bẩn xóa file quan trọng** — recover + báo cáo

---

## 9. Bắt đầu thế nào

Anh duyệt plan này → em sẽ:

1. **Ngay bây giờ (không cần Figma):**
   - Chạy Batch 0 (5 task) + Batch 1-5 backend (còn lại 30 task)
   - ~5-10 ngày làm việc (mỗi ngày 5-6 task)
   - Em sẽ báo cáo Task Report sau mỗi task, chờ anh duyệt từng batch

2. **Khi Backend xong (sau Batch 5):**
   - Anh gửi Figma selection cho từng trang customer
   - Em chạy Batch 6 theo Figma workflow 6 bước
   - ~5-7 ngày (mỗi ngày 1-2 trang)

3. **Khi Customer xong (sau Batch 6):**
   - Test thủ công 11 trang
   - Anh gửi Figma cho admin
   - Em chạy Batch 7 admin + cleanup
   - ~3-5 ngày

**Tổng thời gian ước tính:** 13-22 ngày làm việc (nếu 4-6h/ngày). Tương đương 3-5 tuần.

---

**Plan version:** 1.0  
**Ngày tạo:** 2026-06-23  
**Author:** Hermes Agent  
**Skill tham chiếu:** `thexuong-stack`, `spring-thymeleaf-to-vue3`, `multi-batch-feature-implementation`, `plan`, `design-fidelity-workflow`
