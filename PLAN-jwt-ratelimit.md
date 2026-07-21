# Plan: Sửa JWT & Rate Limit (TheXuong)

Trạng thái: code chưa đủ an toàn cho production. Mục tiêu: vá các CRITICAL/IMPORTANT
theo hướng ponytail (tối thiểu, stdlib, không thêm dependency, không abstraction thừa).

## Nguyên tắc
- Chỉ sửa những gì cần; không refactor ngoài phạm vi.
- Không hardcode secret; không đổi behavior ngoài mô tả.
- Mỗi fix kèm `ponytail:` comment nếu là simplification có chủ ý.

---

## CRITICAL (bắt buộc trước prod)

### C1. Bỏ fallback dev secret — `security/JwtService.java:33-36`
- Xoá khối `if (secret == null || ...)` fallback `DEV_SECRET`.
- Ném `IllegalStateException` nếu secret null/<32 byte lúc khởi tạo.
- Đảm bảo `JWT_SECRET` đã có trong `.env` (đang có).
- Verify: boot without JWT_SECRET → app fail-fast.

### C2. Rotate refresh token — `security/JwtAuthenticationFilter.java:60-74`
- Khi cấp `newRefresh`, blacklist ngay jti của `refreshToken` cũ (exp = exp cũ).
- Dùng `jwtService.extractClaims(refreshToken).getId()` + `.getExpiration().toInstant()`.
- Verify: đăng nhập → lấy refresh cũ gọi lại → bị từ chối (blacklisted).

### C3. Blacklist in-memory — `security/TokenBlacklist.java` ✅ DONE
- User quyết định: CHỈ 1 node BE → giữ `ConcurrentHashMap` in-memory (code cũ, đã đúng, có scheduled cleanup, không leak).
- ĐÃ REVERT Redis: xoá `spring-boot-starter-data-redis` + config redis trong application.properties.
- `ponytail: single-node; đổi Redis nếu chạy >1 BE instance qua Cloudflare Tunnel.`

### C4. Trust IP từ Cloudflare — `filter/LoginRateLimitFilter.java:141-152`
> OBSOLETE — superseded by docs/superpowers/plans/2026-07-21-rate-limiting.md.
> `LoginRateLimitFilter` đã bị xóa. IP trust giờ do `ClientIpUtil` + `server.forward-headers-strategy=native` đảm nhận, được reuse bởi interceptor mới.
- Bật `server.forward-headers-strategy=NATIVE` (Tomcat RemoteIpValve).
- Cấu hình valve trust Cloudflare ranges; dùng `request.getRemoteAddr()`.
- Xoá đọc `X-Forwarded-For[0]` thủ công (spoofable).
- Verify: gửi XFF giả → IP vẫn là IP thực từ Cloudflare.

### C5. Race condition counter — `filter/LoginRateLimitFilter.java:81-104`
> OBSOLETE — superseded by docs/superpowers/plans/2026-07-21-rate-limiting.md.
> `LoginRateLimitFilter` đã bị xóa; logic counter cũ không còn.
- Đổi `recordFailedAttempt` sang `failedAttempts.compute(ip, ...)` (atomic).
- `AttemptInfo.failedCount` giữ nguyên (hoặc `AtomicInteger`).
- Verify: load test 10 thread cùng fail 1 IP → counter đúng = 10.

---

## IMPORTANT

### I1. Max-Age theo config — `security/JwtCookieService.java:24-27`
- Inject `accessTtlSeconds`/`refreshTtlSeconds` (từ `JwtService` hoặc `@Value`).
- `buildCookie("access_token", ..., accessTtlSeconds)`; refresh tương tự.
- Verify: đổi TTL trong yaml → cookie Max-Age khớp.

### I2. Gỡ double login path — `filter/CustomAuthenticationFailureHandler.java`
- Xác nhận `AuthRestController.login` đã tự gọi `recordFailedAttempt` (line 64).
- Xoá `filter/CustomAuthenticationFailureHandler.java` nếu là dead code (không gắn chain).
- Gộp `getClientIp` vào 1 util chung (tránh trùng `security/CustomAuthenticationFailureHandler.java`).

### I3. Thêm Retry-After — `filter/LoginRateLimitFilter.java:58`
> OBSOLETE — superseded by docs/superpowers/plans/2026-07-21-rate-limiting.md.
> Filter cũ đã gỡ; Retry-After sẽ do interceptor mới đảm nhận.
- `response.setHeader("Retry-After", String.valueOf(Math.max(remainingSeconds, 1)));`
- Verify: IP bị khóa → 429 có header Retry-After.

---

## MINOR (làm nếu rảnh)
- M1: Window sliding thực sự (hiện first-anchored) — chấp nhận, ghi chú.
- M2: `shouldNotFilter` khớp exact + `**` thay vì `startsWith` (`JwtAuthenticationFilter.java:29-43`).
- M3: Cache `UserDetails` (`@Cacheable("users")`) hoặc build từ claim `role`.
- M4: `Math.max(remainingSeconds, 0)` tránh âm (`LoginRateLimitFilter.java:56`).
  > OBSOLETE — file `LoginRateLimitFilter.java` đã xóa.

---

## Thứ tự thực hiện
1. C1 (1 dòng throw)
2. C2 (blacklist refresh cũ)
3. ~~C4 (trust Cloudflare IP)~~ — OBSOLETE
4. ~~C5 (compute atomic)~~ — OBSOLETE
5. I1 (Max-Age config)
6. I2 (xoá dead code + gộp getClientIp)
7. ~~I3 (Retry-After)~~ — OBSOLETE
8. C3 (chỉ nếu >1 node)
9. MINOR cuối cùng (trừ M4)

## Verification chung
- `./gradlew build` (compile + test) pass.
- Boot app: HikariPool connect OK, không warn liên quan security.
- Test tay: login fail 5 lần → 429; login ok → reset; refresh rotate; logout blacklist.
- Không commit `.env`; chỉ commit code + `.env.example`.
