# Design: JWT Implementation + Rate Limit Fix

**Date:** 2026-07-18
**Stack:** Spring Boot 3.5.9 / Java 21 / Spring Security 6.x / jjwt 0.11.5 (dep đã có)
**Scope:** BE-only JWT (no DB tables) + fix rate limit dead code. Thymeleaf cleanup đã làm riêng. n8n chatbot auth tách task riêng.

## Quyết định từ brainstorming

| Quyết định | Lựa chọn |
|---|---|
| Auth strategy | JWT cho tất cả (`/api/v1/**` + Google OAuth), bỏ session hoàn toàn, bỏ formLogin |
| Token transport | JWT trong **httpOnly cookie** (giữ bảo vệ XSS như JSESSIONID hiện tại) |
| Refresh strategy | **Stateless** — refresh token là JWT dài hạn có claim `type=refresh`, TTL 7d |
| Token lifetime | Access 15m / Refresh 7d (override qua `application.properties`) |
| n8n chatbot auth | Tách task riêng, giữ `permitAll` cho now |
| Thymeleaf cleanup | Đã làm task riêng (xong) |

## 1. Kiến trúc tổng thể

| Trạng thái | Hiện tại | Sau |
|---|---|---|
| Vue SPA `/api/v1/**` | Session JSESSIONID | **JWT trong httpOnly cookie** |
| Google OAuth2 | Session redirect | **Issue JWT sau callback, set cookie, redirect FE** |
| Refresh token | — | **Stateless** (JWT claim `type=refresh`, TTL 7d) |
| Logout revoke | invalidate session | **In-memory jti blacklist** (`ConcurrentHashMap` + `@Scheduled` cleanup) |
| Session policy | IF_REQUIRED (default) | **STATELESS** |

**Token transport:** httpOnly cookie `access_token` (TTL 15m) + `refresh_token` (TTL 7d). `SameSite=Lax`, `Secure=true`, path=`/`, domain từ config (rỗng cho localhost). FE giữ `withCredentials:true`.

**Luồng refresh tự động:** `JwtAuthenticationFilter` đọc `access_token` cookie → nếu hợp lệ, set SecurityContext. Nếu hết hạn, đọc `refresh_token` → nếu hợp lệ, sign access mới, set cookie response, set SecurityContext. Nếu cả 2 invalid → anonymous (auth rules quyết định 401). FE không cần biết refresh tồn tại.

## 2. Các component mới/sửa

### Files mới (4)

**`security/JwtService.java`** (~80 dòng)
- `generateAccessToken(UserDetails user)` → claims `sub=email, uid, role, type=access, iat, exp, jti=UUID`, HS256, TTL từ config
- `generateRefreshToken(UserDetails user)` → claims `sub, uid, type=refresh, iat, exp, jti`, TTL 7d
- `extractClaims(token)`, `extractUsername(token)`, `isAccessToken(token)`, `isRefreshToken(token)`, `isExpired(token)`, `isJtiBlacklisted(jti)`
- Signing key: `Keys.hmacShaKeyFor(secret)` từ `${app.security.jwt.secret:dev-only-secret-key-change-in-prod-...}` (env var, dev fallback warning)

**`security/TokenBlacklist.java`** (~40 dòng)
- `ConcurrentHashMap<String, Instant>` jti → exp
- `blacklist(jti, exp)`, `isBlacklisted(jti)`
- `@Scheduled(fixedDelay=300_000)` cleanup entry quá exp
- `ponytail: global map OK cho single instance; per-user map nếu throughput matters`

**`security/JwtAuthenticationFilter.java`** (~90 dòng) extends `OncePerRequestFilter`
- Đọc cookie `access_token` (fallback header `Authorization: Bearer` cho Postman/test)
- Valid + not blacklisted → load UserDetails → set SecurityContext
- Access expired + refresh valid → sign access mới, set cookie, set context
- Cả 2 invalid/missing → continue anonymous (không throw)
- Skip path permitAll (`/api/v1/auth/login`, `/register`, `/forgot-password`, `/reset-password`, `/api/v1/products/**`, `/api/v1/categories/**`, `/api/v1/chatbot/**`)

**`security/JwtCookieService.java`** (~50 dòng) — helper set/clear cookie
- `setAuthCookies(response, accessToken, refreshToken)` — httpOnly, secure, sameSite=Lax, path=/
- `clearAuthCookies(response)` — set Max-Age=0
- Dùng chung cho `AuthRestController` và `OAuth2SuccessHandler`

### Files sửa (5)

**`controller/AuthRestController.java`**
- `POST /login` (dòng 48-63): thay `setContext` bằng issue access+refresh, set cookie, return `{user, message}`. **Try-catch `AuthenticationException` → `recordFailedAttempt(ip)` + rethrow** (fix rate limit)
- `POST /logout` (dòng 70-82): blacklist 2 jti (extract từ cookie), clear cookie, clear context
- `POST /refresh` (NEW): đọc refresh cookie → validate → issue access+refresh mới → set cookie → return `{user}` hoặc 401
- Các endpoint khác (`register`, `forgot-password`, `reset-password`, `profile`, `password`): không đụng

**`config/SecurityConfig.java`**
- Thêm `http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)`
- Thêm `sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))`
- **Bỏ `formLogin` block** (dòng 185-193)
- **Sửa `oauth2Login`** (dòng 194-200): giữ `userInfoEndpoint` + `successHandler(oAuth2SuccessHandler)` — `OAuth2SuccessHandler` sẽ tự issue JWT
- **Sửa `logout`** (dòng 201-207): bỏ formLogin logout; `/api/v1/auth/logout` (JWT) đảm nhiệm
- `authorizeHttpRequests` đã dọn sạch Thymeleaf (task trước), giữ nguyên
- `authenticationEntryPoint` API path 401 JSON (dòng 102-117): giữ

**`security/OAuth2SuccessHandler.java`** (refactor)
- Inject `JwtService` + `JwtCookieService`
- `onAuthenticationSuccess`: load/create user (logic hiện có) → issue access+refresh → set httpOnly cookie → redirect `app.frontend.url + /`
- **Xóa redirect `?authenticated=true` session-based**

**`filter/LoginRateLimitFilter.java`**
- `getClientIp` (dòng 141-152): đổi `private` → `public` (controller cần gọi)
- Các method khác giữ nguyên
- Auto-reset trên `response.getStatus()==200` (dòng 71-74): giữ

**`filter/CustomAuthenticationFailureHandler.java`** — KHÔNG đụng (formLogin bị bỏ → dead code tự nhiên)

### Files config (1)

**`application.properties`** thêm:
```properties
app.security.jwt.secret=${JWT_SECRET:dev-only-secret-key-change-in-prod-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx}
app.security.jwt.access-ttl-seconds=900
app.security.jwt.refresh-ttl-seconds=604800
app.security.jwt.cookie-domain=
app.security.jwt.cookie-secure=true
app.security.jwt.cookie-same-site=lax
```

## 3. FE thay đổi tối thiểu (httpOnly cookie giữ gần như nguyên)

- **`frontend/src/services/api.ts` + `http.ts`**: bỏ CSRF interceptor (dòng 18-31 / 11-18) — không còn cần. Giữ `withCredentials:true`.
- **`frontend/src/stores/auth.store.ts`**: `login` action không đổi (BE set cookie, `fetchUser` đọc cookie). Không lưu token trong JS.
- 401 interceptor: giữ (redirect `/login` khi cả 2 token invalid).
- KHÔNG sửa các component dùng `useAuthStore`.

## 4. Rate Limit Fix (độc lập, làm trước)

**Root cause:** `recordFailedAttempt()` chỉ gọi từ `CustomAuthenticationFailureHandler` — handler không được wire (SecurityConfig dùng `.failureUrl` cho formLogin; API login đi qua controller → exception rơi vào `GlobalExceptionHandler` không record).

**Fix 2 điểm:**
1. `AuthRestController.login`: try-catch `AuthenticationException` → `loginRateLimitFilter.recordFailedAttempt(ip)` → rethrow (GlobalExceptionHandler vẫn trả 401). Success path: `loginRateLimitFilter.resetAttempts(ip)` explicit.
2. `LoginRateLimitFilter.getClientIp`: `private` → `public`.

**Pre-check `isLocked(ip)`** (dòng 44-76) đã hoạt động độc lập — sẽ chặn 429 trước khi vào controller.
**Verification:** fail login 5 lần → lần 6 trả 429.

## 5. Thứ tự triển khai & verification

| Phase | Việc | Verify |
|---|---|---|
| 1 | Rate limit fix (Section 4) | Fail 5 login → 429; login đúng → reset |
| 2 | JWT infra: `JwtService`, `TokenBlacklist`, `JwtCookieService` | Unit check: generate → parse → validate |
| 3 | `JwtAuthenticationFilter` + wire vào `SecurityConfig` (STATELESS, bỏ formLogin) | Build pass, endpoint permitAll vẫn chạy |
| 4 | `AuthRestController` login/logout/refresh + `OAuth2SuccessHandler` | Manual: login → cookie → access protected → logout → revoked |
| 5 | FE: bỏ CSRF interceptor | Build FE pass, login flow vẫn chạy |
| 6 | E2E: email/password login, refresh auto, logout revoke, Google login | `./gradlew build` + FE build |

## 6. Default ponytail (không hỏi, ghi rõ)

- **JWT secret**: env var `JWT_SECRET`, dev fallback key (log warning). Prod phải set env.
- **Algorithm**: HS256 (jjwt 0.11.5 mặc định, key 256-bit từ base64 secret).
- **Blacklist restart**: mất khi restart → token đã logout có thể dùng đến khi expired. Window = access TTL 15m. Chấp nhận.
- **`jti`**: UUID random per token.
- **CORS, SQL injection**: không đụng (đã implemented).
- **CSRF**: vẫn disabled cho `/api/**` (đã có), FE bỏ interceptor. SameSite=Lax + httpOnly cookie là mitigation chính.
- **n8n chatbot**: giữ `permitAll`, task riêng.
- **Duplicate axios instance** (`api.ts` + `http.ts`): không đụng, ngoài scope.
- **CORS hardcode vs yml inconsistency**: không đụng, ngoài scope.
- **User enumeration** (`setHideUserNotFoundExceptions(false)`): không đụng, ngoài scope.

## Research evidence

Xem báo cáo research chi tiết trong task sub-agent explore (session `ses_08a9ca2a1ffeXO3VgSfBvyLS76`). Tóm tắt key evidence:

- `build.gradle:89-92` — jjwt 0.11.5 dep đã có, chưa dùng
- `AuthRestController.java:31` — comment "session-based (no JWT)"
- `SecurityConfig.java:189` — `.failureUrl("/login?error=true")` thay vì `.failureHandler()` → rate limit dead code
- `filter/CustomAuthenticationFailureHandler.java:40` — call site duy nhất của `recordFailedAttempt`, không wire
- `GlobalExceptionHandler.java:113-118` — bắt `AuthenticationException` trả 401 nhưng không record
- 23/25 `Authentication` call sites không cần đụng (JWT filter populate SecurityContext)
- `frontend/src/services/api.ts:15` + `http.ts:8` — `withCredentials:true` (giữ)
- `frontend/src/stores/auth.store.ts` — không lưu token JS, dựa cookie (giữ)
- `OAuth2SuccessHandler.java:52-53` — redirect `${app.frontend.url}/oauth/callback` (Vue SPA)
- `application.properties:26-32` — session cookie httpOnly+secure+sameSite=lax (pattern để follow cho JWT cookie)
