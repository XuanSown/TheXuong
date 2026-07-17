# Design: JWT Auth + Rate Limit Fix (bỏ Thymeleaf vĩnh viễn)

- **Ngày:** 2026-07-17
- **Trạng thái:** Approved (design choices đã chốt qua brainstorming)
- **Phạm vi:** Backend Spring Boot + Frontend Vue SPA
- **Repo:** https://github.com/XuanSown/TheXuong.git

---

## 1. Mục tiêu & ngữ cảnh

### Vấn đề hiện tại
1. **Rate limit không hoạt động** — `LoginRateLimitFilter.recordFailedAttempt()` là dead code vì `CustomAuthenticationFailureHandler` không được wire vào formLogin (dùng `.failureUrl` thay `.failureHandler`), và REST login (`AuthRestController.login`) không đi qua failure handler. → brute-force vô hạn.
2. **Không có JWT** — dependency `jjwt` 0.11.5 khai báo trong `build.gradle` nhưng 0 dòng code dùng. Auth hiện tại session-based (JSESSIONID).
3. **Thymeleaf footprint gần như 0** — 0 controller `@Controller`, 0 templates, chỉ còn dependency `thymeleaf-extras-springsecurity6` + config `formLogin`/`logout`/route-page trong `SecurityConfig` + vài comment outdated.

### Quyết định thiết kế (chốt qua brainstorming)
- Chuyển sang **JWT** vì multi-instance / scale ngang.
- **Access + Refresh + Blacklist logout** (revoke access ngay khi logout).
- FE token storage: **HttpOnly cookie** (BE Set-Cookie, FE `withCredentials`).
- Rate limit state: **giữ in-memory** `ConcurrentHashMap` (single-instance, YAGNI Redis).
- **Bỏ Thymeleaf vĩnh viễn** — toàn bộ auth thống nhất qua REST + JWT.

### Ngoài phạm vi (tách riêng, hỏi sau)
- Secrets hardcoded trong git (`application.properties`/`.yml`) — cần rotate credential thật.
- User enumeration (`ApplicationConfig.java:78` `setHideUserNotFoundExceptions(false)`).
- Role string inconsistent `USER` vs `CUSTOMER`.

---

## 2. Kiến trúc tổng quan

```
Browser ──cookie access+refresh (httpOnly, secure, sameSite=Strict)──▶ BE
                                                                        │
                          ┌─────────────────────────────────────────────┤
                          ▼                                             ▼
                 JwtAuthenticationFilter                          Controllers
            (mỗi request: đọc access cookie,                 (login/refresh/logout/user)
             validate + check blacklist,                                   │
             set SecurityContext)                                         ▼
                          │                                   JwtService / RefreshTokenService
                          │                                        │              │
                          │                                        ▼              ▼
                          │                                   generate/validate  DB: RefreshToken, TokenBlacklist
                          ▼
                 SecurityContext (Authentication) ──▶ @PreAuthorize / @AuthenticationPrincipal
```

**Auth flow:**
- Login REST → `authenticate()` → issue access (30p) + refresh (7d) → Set 2 cookie httpOnly → return `{user}`.
- Mọi request sau → `JwtAuthenticationFilter` đọc access cookie → validate → check blacklist → set `SecurityContext`.
- Access hết hạn → FE interceptor 401 → gọi `/auth/refresh` → BE verify refresh cookie → issue access mới → retry request gốc.
- Logout → blacklist access `jti` + revoke refresh trong DB + clear 2 cookie.
- OAuth2 Google → success handler issue JWT + redirect FE `/oauth/callback`.

---

## 3. Data model + schema

### 3.1. Entity `RefreshToken`
| Cột | Kiểu | Ràng buộc |
|---|---|---|
| `id` | BIGINT | PK IDENTITY |
| `user_id` | BIGINT | NOT NULL FK → users(id) |
| `token_hash` | VARCHAR(64) | NOT NULL UNIQUE — SHA-256 của raw refresh token |
| `expires_at` | DATETIME2 | NOT NULL |
| `created_at` | DATETIME2 | NOT NULL DEFAULT SYSUTCDATETIME() |
| `revoked` | BIT | NOT NULL DEFAULT 0 |

Index: `UNIQUE(token_hash)`, `IDX(user_id, revoked, expires_at)`.

### 3.2. Entity `TokenBlacklist`
| Cột | Kiểu | Ràng buộc |
|---|---|---|
| `id` | BIGINT | PK IDENTITY |
| `jti` | VARCHAR(64) | NOT NULL UNIQUE — JWT ID của access token bị revoke |
| `expires_at` | DATETIME2 | NOT NULL — = exp của access token |
| `created_at` | DATETIME2 | NOT NULL DEFAULT SYSUTCDATETIME() |

`@Scheduled` job mỗi 1h: `DELETE WHERE expires_at < now()`.

### 3.3. Quyết định thiết kế
- Refresh token lưu **hash** (SHA-256) — DB leak không reuse được raw token.
- Blacklist chỉ lưu `jti` + `expires_at` — row nhỏ, tự purge. Access TTL 30p → bảng tự giới hạn kích thước.
- Rate limit **không** dùng DB (giữ in-memory).

---

## 4. JwtService + claims + filter

### 4.1. `JwtService` API
```java
String generateAccessToken(User user)       // TTL 30p, jti = UUID
String generateRefreshToken(User user)      // TTL 7d, jti = UUID (raw trả client, hash lưu DB)
Claims parseAndValidate(String token)       // throw nếu invalid/expired
String extractJti(String token)
boolean isExpired(String token)
```
- Dùng `io.jsonwebtoken.Jwts` 0.12.x (upgrade từ 0.11.5 dead).
- Thuật toán: HS256.
- Secret: env `JWT_SECRET` (≥ 256-bit), fallback không hardcode — fail-fast nếu thiếu.

### 4.2. Access token claims
```json
{
  "sub": "<userId>",
  "email": "<email>",
  "role": "ADMIN|CUSTOMER|BOTH",
  "jti": "<UUID>",
  "iat": <now>,
  "exp": <now+30p>,
  "iss": "thexuong"
}
```
- `sub = userId` (đồng định, email có đổi).
- `role` cho check `@PreAuthorize` nhanh (filter set authorities từ claim).

### 4.3. `UserPrincipal` record
```java
public record UserPrincipal(Long userId, String email, String role) implements Serializable {}
```
- Dùng làm principal trong `JwtAuthenticationToken` — đủ cho `@AuthenticationPrincipal` mà không query DB mỗi request.
- Khi cần chi tiết (addresses, phone) → controller query DB bằng `userId`.

### 4.4. `JwtAuthenticationFilter` (OncePerRequestFilter)
```
doFilterInternal:
  1. Đọc cookie "access_token" → null? → chain.doFilter (anonymous)
  2. parseAndValidate(token) → throw? → clear cookie + chain.doFilter (anonymous)
  3. jti = extractJti(token)
  4. tokenBlacklistService.isBlacklisted(jti)? → true → chain.doFilter (anonymous)
  5. Tạo JwtAuthenticationToken(UserPrincipal, authorities từ "role")
  6. SecurityContextHolder.setContext(auth)
  7. chain.doFilter
  finally: SecurityContextHolder.clearContext()
```

---

## 5. Auth endpoints + cookie

### 5.1. `AuthRestController` endpoints
| Endpoint | Method | Flow |
|---|---|---|
| `/api/v1/auth/login` | POST | `authenticate()` → issue access+refresh → set 2 cookie → return `{user}` |
| `/api/v1/auth/refresh` | POST | Đọc refresh cookie → `refreshTokenService.verify()` → issue access mới → set access cookie → return `{user}` |
| `/api/v1/auth/logout` | POST | Đọc access → blacklist `jti`; đọc refresh → revoke DB; clear 2 cookie → 200 |
| `/api/v1/auth/user` | GET | Đọc `@AuthenticationPrincipal UserPrincipal` → query DB → return `{user}` |
| `/api/v1/auth/register` | POST | (giữ nguyên) tạo user → return message |
| `/api/v1/auth/forgot-password` | POST | (giữ nguyên) + rate limit |
| `/api/v1/auth/reset-password` | POST | (giữ nguyên) |

### 5.2. Cookie spec
```
access_token:  HttpOnly, Secure, SameSite=Strict, Path=/,                          Max-Age=30m
refresh_token: HttpOnly, Secure, SameSite=Strict, Path=/api/v1/auth/refresh,       Max-Age=7d
```
- `SameSite=Strict` (chống CSRF triệt để, SPA cùng origin qua Vite proxy).
- `Path` refresh hẹp → chỉ gửi tới `/auth/refresh`, giảm lộ diện.
- Logout: set cookie `Max-Age=0` để browser xóa.
- Production: `Secure=true` (HTTPS qua Cloudflare tunnel). Dev: `Secure=false` (localhost HTTP) — dùng `@Value` hoặc profile.

---

## 6. Rate limit fix (bỏ Thymeleaf, mở rộng)

### 6.1. Sửa `LoginRateLimitFilter`
1. **Lấy IP đúng:** bỏ parse `X-Forwarded-For` tay → dùng `request.getRemoteAddr()` (Spring đã rewrite nhờ `server.forward-headers-strategy=framework` + Cloudflare tunnel tin cậy).
2. **Atomic:** `AttemptInfo.failedCount` → `AtomicInteger`.
3. **Mở rộng path:** áp dụng cho `POST /api/v1/auth/login` **và** `POST /api/v1/auth/forgot-password`.
4. **Externalize ngưỡng:** `@Value` từ `application.properties`:
   - `rate.login.max-attempts=5`
   - `rate.login.window-minutes=15`
   - `rate.login.lockout-minutes=15`
   - ponytail: default hardcode nếu không set.
5. **Trigger `recordFailedAttempt` đúng chỗ:** bỏ formLogin → không có failure handler flow. `AuthRestController.login` bắt `AuthenticationException` → gọi `recordFailedAttempt(ip)` trước khi return 401. Tương tự `forgot-password` (catch exception → record).

### 6.2. Dọn dẹp
- Xóa `filter/CustomAuthenticationFailureHandler.java` (chỉ dùng cho formLogin đã xóa).
- Xóa file stub `security/CustomAuthenticationFailureHandler.java`.

---

## 7. SecurityConfig refactor (bỏ Thymeleaf)

```java
filterChain:
  .cors(...)
  .csrf(csrf -> csrf.disable())              // STATELESS JWT, SameSite=Strict → CSRF không cần
  .sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
  .exceptionHandling(ex -> ex.authenticationEntryPoint(jwtAuthEntryPoint))  // 401 JSON
  .authorizeHttpRequests(auth -> auth
      .requestMatchers("/api/v1/auth/login", "/api/v1/auth/register",
                       "/api/v1/auth/forgot-password", "/api/v1/auth/reset-password",
                       "/api/v1/auth/refresh").permitAll()
      .requestMatchers("/api/v1/products/**", "/api/v1/categories/**").permitAll()
      .requestMatchers("/api/v1/chatbot/**").permitAll()
      .requestMatchers("/api/v1/admin/**").hasAnyAuthority("ADMIN", "BOTH")
      .anyRequest().authenticated()
  )
  .authenticationProvider(authenticationProvider)
  .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
  .oauth2Login(oauth2 -> oauth2
      .loginPage("/login")   // FE xử lý page, BE chỉ permitAll path để Google redirect
      .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService()))
      .successHandler(oAuth2SuccessHandler)  // sửa: issue JWT + redirect FE
  )
```

**Bỏ:**
- `formLogin`, `logout` (SecurityConfig), `loginProcessingUrl`, `failureUrl`.
- `permitAll` cho Thymeleaf pages (`/`, `/index`, `/products/**` Thymeleaf, `/product-detail/**`, `/vnpay-return`...).
- Dependency `thymeleaf-extras-springsecurity6` trong `build.gradle`.

**OAuth2 login page:** Google flow cần BE trigger `/oauth2/authorization/google`. `oauth2Login.loginPage` trỏ tới FE URL hoặc permitAll path. `OAuth2SuccessHandler` sửa: issue JWT + redirect FE `/oauth/callback`.

---

## 8. Frontend changes

### 8.1. `frontend/src/services/api.ts`
- `withCredentials: true` giữ (cookie cross-origin qua proxy).
- **Bỏ CSRF interceptor** (JWT + SameSite=Strict không cần CSRF).
- Bỏ `getCsrfToken`/`setCsrfToken`/`clearCsrfToken`.
- `logout()`: gọi `/auth/logout` (BE clear cookie).
- **Response interceptor 401 → silent refresh:**
  1. Nếu url KHÔNG phải `/auth/refresh` và KHÔNG phải `/auth/user` (tránh loop) → gọi `POST /auth/refresh`.
  2. Refresh thành công → retry request gốc 1 lần.
  3. Refresh cũng 401 → clear store + redirect `/login`.

### 8.2. `frontend/src/stores/auth.store.ts`
- `fetchUser()`: gọi `/auth/user` — nếu 401 → interceptor tự refresh + retry (không cần try/catch riêng).
- `logout()`: gọi `api.logout()` → `clear()` → redirect.
- `clear()`: bỏ `api.clearCsrfToken()` (không còn CSRF).

### 8.3. `frontend/src/router/index.ts`
- Guard `beforeEach`: giữ `fetchUser()` init. Nhờ silent refresh, reload trang vẫn giữ đăng nhập nếu refresh token còn hạn.

### 8.4. Vite proxy
- `/api` proxy → `localhost:8080` (đã có, không đổi). Cookie `SameSite=Strict` cùng origin qua proxy → OK.

---

## 9. Cleanup / xóa

| Xóa | Lý do |
|---|---|
| `build.gradle:79` `thymeleaf-extras-springsecurity6` | Không còn Thymeleaf |
| `filter/CustomAuthenticationFailureHandler.java` | Chỉ dùng cho formLogin đã xóa |
| `security/CustomAuthenticationFailureHandler.java` (stub) | File rác |
| `SecurityConfig` formLogin/logout/route-page blocks | Thymeleaf |
| Comment outdated trong `AuthRestController`, `GlobalExceptionHandler`, `AdminUserRestController` | Tham chiếu Thymeleaf không còn |

---

## 10. Testing strategy

- **BE:** không có test infra hiện tại. Ponytail: viết test MockMvc tối thiểu cho flow JWT (login → access valid → logout → access rejected) + rate limit (5 fail → 429). Nếu project thêm `spring-boot-starter-test` thì dùng.
- **FE:** không có test infra. Verify thủ công qua TestSprite e2e (đã có config `.testsprite/`).
- **Verify checklist runtime:**
  1. Login → 2 cookie set, `/auth/user` 200.
  2. Reload trang → vẫn authenticated (silent refresh).
  3. Logout → access blacklist, refresh revoke, cookie clear, `/auth/user` 401.
  4. Brute-force 5 lần fail → request 6 → 429.
  5. OAuth Google → redirect FE `/oauth/callback` → authenticated.

---

## 11. Thứ tự implement (gợi ý cho plan)

1. **BE data layer:** entity `RefreshToken`, `TokenBlacklist` + repo + SQL migration (DBA).
2. **BE services:** `JwtService`, `RefreshTokenService`, `TokenBlacklistService` (+ scheduled cleanup).
3. **BE filter + principal:** `UserPrincipal`, `JwtAuthenticationFilter`, `JwtAuthEntryPoint`.
4. **BE controllers:** sửa `AuthRestController` (login/refresh/logout/user), sửa `OAuth2SuccessHandler`.
5. **BE SecurityConfig refactor:** bỏ Thymeleaf/formLogin/logout, add filter, STATELESS.
6. **BE rate limit:** sửa `LoginRateLimitFilter` (IP đúng, atomic, mở rộng path, externalize), trigger trong controller.
7. **BE cleanup:** xóa dependency Thymeleaf, xóa failure handler files, xóa comment outdated.
8. **FE api.ts:** bỏ CSRF, silent refresh interceptor, sửa `logout()`.
9. **FE auth.store.ts:** sửa `fetchUser`/`logout`/`clear`.
10. **Verify:** build BE + FE, runtime checklist, TestSprite e2e.

---

## 12. Risks / open questions

- **OAuth2 + JWT:** Google redirect qua BE → BE issue JWT → redirect FE với cookie. Cần verify cookie set được khi response là redirect (Spring `oauth2SuccessHandler` chạy trong BE request, Set-Cookie header đi cùng 302 → OK).
- **Silent refresh race:** nhiều request 401 cùng lúc → nhiều `/auth/refresh` song song. Ponytail: chấp nhận (refresh idempotent, worst case issue nhiều access mới, chỉ 1 dùng được). Nếu vấn đề → queue refresh sau.
- **Refresh token rotation:** design hiện tại KHÔNG rotate (refresh token cũ vẫn dùng được đến hết hạn sau khi issue access mới). Có thể thêm rotation (issue refresh mới mỗi `/auth/refresh`, revoke cũ) nếu cần an toàn hơn — để open.
- **Secret management:** `JWT_SECRET` phải qua env var, không hardcode. Trùng với vấn đề secrets trong git (out of scope, hỏi sau).
