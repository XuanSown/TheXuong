# Account Lock Kick Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Chặn tài khoản bị khóa: login báo rõ "tài khoản bị khóa" (423), kick phiên đang hoạt động (xóa cookie + blacklist token), chặn Google login, frontend hiển thị thông báo.

**Architecture:** Backend Spring Boot (JWT cookie-based, stateless). `JwtAuthenticationFilter` chạy mọi request — thêm check `isEnabled()` sau khi load user, nếu khóa thì blacklist token + xóa cookie + trả 423. `GlobalExceptionHandler` thêm handler `DisabledException` → 423. `OAuth2SuccessHandler` chặn Google login khi khóa. Frontend Vue: axios interceptor bắt 423 → toast + redirect `/login?locked=1`, Login.vue hiện alert.

**Tech Stack:** Java 21, Spring Boot 3.5.9, Spring Security (JWT jjwt 0.11.5, cookie httpOnly), JUnit 5 + Mockito + AssertJ; Vue 3 + axios + vue-toastification + vue-i18n, Vitest 4 + @vue/test-utils (jsdom).

## Global Constraints

- Message tiếng Việt chuẩn (dùng y nguyên mọi nơi): `Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.`
- HTTP 423: `HttpStatus.LOCKED` (controller/handler) hoặc `HttpServletResponse.SC_LOCKED` (filter).
- Filter JSON body dùng key `error` (đồng bộ AuthenticationEntryPoint hiện có).
- Frontend redirect path: `/login?locked=1` (query value `'1'`).
- KHÔNG đổi hành vi 401/403 hiện có; KHÔNG thêm dependency mới.
- Frontend test file theo include pattern của vitest: `src/**/*.{test,spec}.{js,ts,jsx,tsx}`; đặt trong thư mục `__tests__`.
- Backend test: JUnit 5 + Mockito + AssertJ (đã có trong `spring-boot-starter-test`).
- Commit style repo: `fix:`/`feat:` + mô tả ngắn tiếng Anh.

---

### Task 1: GlobalExceptionHandler — login tài khoản bị khóa trả 423 + message

**Files:**
- Modify: `src/main/java/com/example/thexuong/exception/GlobalExceptionHandler.java`
- Test: `src/test/java/com/example/thexuong/exception/GlobalExceptionHandlerTest.java`

**Interfaces:**
- Consumes: `com.example.thexuong.dto.ApiResponse` (`ApiResponse.error(String)` → `{success:false, message, data:null}`, getters `isSuccess()`, `getMessage()`).
- Produces: handler `handleDisabledAccount(DisabledException)` → `ResponseEntity<ApiResponse<Void>>` status 423. Không task nào khác phụ thuộc.

- [ ] **Step 1: Write the failing test**

Create `src/test/java/com/example/thexuong/exception/GlobalExceptionHandlerTest.java`:

```java
package com.example.thexuong.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.DisabledException;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionHandlerTest {

    @Test
    void handleDisabledAccount_returns423WithLockedMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        var response = handler.handleDisabledAccount(new DisabledException("User is disabled"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.LOCKED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        assertThat(response.getBody().getMessage())
                .isEqualTo("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `.\gradlew.bat test --tests "com.example.thexuong.exception.GlobalExceptionHandlerTest"`
Expected: FAIL — compile error "cannot find symbol: method handleDisabledAccount".

- [ ] **Step 3: Implement**

In `src/main/java/com/example/thexuong/exception/GlobalExceptionHandler.java`:

Add import after line 7 (`import org.springframework.security.access.AccessDeniedException;`):

```java
import org.springframework.security.authentication.DisabledException;
```

Add handler method ngay trước method `handleAuthenticationException` (dòng 123):

```java
  /**
   * 423 — Tài khoản bị khóa (DisabledException khi đăng nhập).
   * Handler riêng để phân biệt với 401 sai email/mật khẩu.
   */
  @ExceptionHandler(DisabledException.class)
  public ResponseEntity<ApiResponse<Void>> handleDisabledAccount(DisabledException ex) {
    return ResponseEntity
      .status(HttpStatus.LOCKED)
      .body(ApiResponse.error("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên."));
  }
```

- [ ] **Step 4: Run test to verify it passes**

Run: `.\gradlew.bat test --tests "com.example.thexuong.exception.GlobalExceptionHandlerTest"`
Expected: PASS (Spring chọn handler `DisabledException` cụ thể hơn thay vì handler `AuthenticationException` chung — không cần thay đổi gì khác).

- [ ] **Step 5: Run full backend test suite (regression)**

Run: `.\gradlew.bat test`
Expected: tất cả test PASS (4 test class cũ + 1 mới).

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/example/thexuong/exception/GlobalExceptionHandler.java src/test/java/com/example/thexuong/exception/GlobalExceptionHandlerTest.java
git commit -m "fix: login locked account returns 423 with locked message"
```

---

### Task 2: JwtAuthenticationFilter — kick user đang đăng nhập bị khóa

**Files:**
- Modify: `src/main/java/com/example/thexuong/security/JwtAuthenticationFilter.java`
- Test: `src/test/java/com/example/thexuong/security/JwtAuthenticationFilterTest.java`

**Interfaces:**
- Consumes: `JwtService` (`isValid`, `isAccessToken`, `isExpired`, `isRefreshToken`, `extractUsername`, `extractClaims` → `io.jsonwebtoken.Claims` có `getId()`, `getExpiration()`), `JwtCookieService` (`readCookie`, `clearAuthCookies`, `setAuthCookies`), `UserDetailsService.loadUserByUsername`, `TokenBlacklist.blacklist(jti, Instant)`.
- Produces: `JwtAuthenticationFilter` với `doFilterInternal` — khi user bị khóa: status 423, body `{"error":"Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên."}`, cookie bị xóa, cả 2 token bị blacklist, KHÔNG gọi `filterChain.doFilter`. Constructor: `new JwtAuthenticationFilter(jwtService, cookieService, userDetailsService, tokenBlacklist)` (không đổi).

- [ ] **Step 1: Write the failing tests**

Create `src/test/java/com/example/thexuong/security/JwtAuthenticationFilterTest.java`:

```java
package com.example.thexuong.security;

import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JwtAuthenticationFilterTest {

    private final JwtService jwtService = mock(JwtService.class);
    private final JwtCookieService cookieService = mock(JwtCookieService.class);
    private final UserDetailsService userDetailsService = mock(UserDetailsService.class);
    private final TokenBlacklist tokenBlacklist = mock(TokenBlacklist.class);
    private final FilterChain chain = mock(FilterChain.class);

    private final JwtAuthenticationFilter filter =
            new JwtAuthenticationFilter(jwtService, cookieService, userDetailsService, tokenBlacklist);

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private UserDetails user(String email, boolean enabled) {
        return org.springframework.security.core.userdetails.User.withUsername(email)
                .password("")
                .disabled(!enabled)
                .authorities(List.of(new SimpleGrantedAuthority("CUSTOMER")))
                .build();
    }

    private MockHttpServletRequest requestWithTokens() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        when(cookieService.readCookie(request, "access_token")).thenReturn("access-token");
        when(cookieService.readCookie(request, "refresh_token")).thenReturn("refresh-token");
        return request;
    }

    @Test
    void validTokenEnabledUser_setsAuthenticationAndContinues() throws Exception {
        MockHttpServletRequest request = requestWithTokens();
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isValid("access-token")).thenReturn(true);
        when(jwtService.isAccessToken("access-token")).thenReturn(true);
        when(jwtService.extractUsername("access-token")).thenReturn("user@test.com");
        when(userDetailsService.loadUserByUsername("user@test.com")).thenReturn(user("user@test.com", true));

        filter.doFilterInternal(request, response, chain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
        verify(chain).doFilter(request, response);
        verify(cookieService, never()).clearAuthCookies(any());
    }

    @Test
    void validTokenLockedUser_returns423BlacklistsAndClearsCookies() throws Exception {
        MockHttpServletRequest request = requestWithTokens();
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isValid("access-token")).thenReturn(true);
        when(jwtService.isAccessToken("access-token")).thenReturn(true);
        when(jwtService.extractUsername("access-token")).thenReturn("locked@test.com");
        when(userDetailsService.loadUserByUsername("locked@test.com")).thenReturn(user("locked@test.com", false));

        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("jti-x");
        when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 3600_000));
        when(jwtService.extractClaims(anyString())).thenReturn(claims);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(423);
        assertThat(response.getContentAsString()).contains("Tài khoản của bạn đã bị khóa");
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(chain, never()).doFilter(any(), any());
        verify(cookieService).clearAuthCookies(response);
        verify(tokenBlacklist, times(2)).blacklist(anyString(), any(Instant.class));
    }

    @Test
    void expiredAccessLockedUser_autoRefreshRejectedWith423() throws Exception {
        MockHttpServletRequest request = requestWithTokens();
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(jwtService.isValid("access-token")).thenReturn(false);
        when(jwtService.isExpired("access-token")).thenReturn(true);
        when(jwtService.isValid("refresh-token")).thenReturn(true);
        when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);
        when(jwtService.extractUsername("refresh-token")).thenReturn("locked@test.com");
        when(userDetailsService.loadUserByUsername("locked@test.com")).thenReturn(user("locked@test.com", false));

        Claims claims = mock(Claims.class);
        when(claims.getId()).thenReturn("jti-x");
        when(claims.getExpiration()).thenReturn(new Date(System.currentTimeMillis() + 3600_000));
        when(jwtService.extractClaims(anyString())).thenReturn(claims);

        filter.doFilterInternal(request, response, chain);

        assertThat(response.getStatus()).isEqualTo(423);
        verify(chain, never()).doFilter(any(), any());
        verify(cookieService, never()).setAuthCookies(any(), anyString(), anyString());
        verify(cookieService).clearAuthCookies(response);
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `.\gradlew.bat test --tests "com.example.thexuong.security.JwtAuthenticationFilterTest"`
Expected: FAIL — test "validTokenLockedUser..." lỗi assert `expected: 423 but was: 200`; test "expiredAccessLockedUser..." lỗi assert tương tự (filter hiện không chặn user disabled).

- [ ] **Step 3: Implement — thay toàn bộ nội dung `JwtAuthenticationFilter.java`**

```java
package com.example.thexuong.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final JwtCookieService cookieService;
    private final UserDetailsService userDetailsService;
    private final TokenBlacklist tokenBlacklist;

    private static final List<String> SKIP_PREFIXES = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/register",
            "/api/v1/auth/forgot-password",
            "/api/v1/auth/reset-password",
            "/api/v1/products",
            "/api/v1/categories",
            "/api/v1/chatbot"
    );

    private static final String LOCKED_MESSAGE = "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return SKIP_PREFIXES.stream().anyMatch(p -> path.equals(p) || path.startsWith(p + "/"));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String accessToken = cookieService.readCookie(request, "access_token");
        if (accessToken == null) {
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                accessToken = header.substring(7);
            }
        }
        String refreshToken = cookieService.readCookie(request, "refresh_token");

        if (accessToken != null && jwtService.isValid(accessToken) && jwtService.isAccessToken(accessToken)) {
            UserDetails user = tryLoadUser(jwtService.extractUsername(accessToken));
            if (user == null) {
                filterChain.doFilter(request, response);
                return;
            }
            if (!user.isEnabled()) {
                log.warn("[JWT] Rejected request from locked user {}", user.getUsername());
                kickLockedUser(response, accessToken, refreshToken);
                return;
            }
            setAuthentication(user, request);
        } else if (accessToken != null && jwtService.isExpired(accessToken)
                && refreshToken != null && jwtService.isValid(refreshToken) && jwtService.isRefreshToken(refreshToken)) {
            String username = jwtService.extractUsername(refreshToken);
            UserDetails user = tryLoadUser(username);
            if (user == null) {
                filterChain.doFilter(request, response);
                return;
            }
            if (!user.isEnabled()) {
                log.warn("[JWT] Rejected auto-refresh for locked user {}", username);
                kickLockedUser(response, accessToken, refreshToken);
                return;
            }
            String newAccess = jwtService.generateAccessToken(user);
            String newRefresh = jwtService.generateRefreshToken(user);
            cookieService.setAuthCookies(response, newAccess, newRefresh);
            // ponytail: blacklist old refresh jti so it can't be reused
            String oldJti = jwtService.extractClaims(refreshToken).getId();
            java.time.Instant oldExp = jwtService.extractClaims(refreshToken).getExpiration().toInstant();
            tokenBlacklist.blacklist(oldJti, oldExp);
            setAuthentication(user, request);
            log.debug("[JWT] Auto-refreshed access token for user {}", username);
        }

        filterChain.doFilter(request, response);
    }

    private UserDetails tryLoadUser(String username) {
        try {
            return userDetailsService.loadUserByUsername(username);
        } catch (Exception e) {
            log.warn("[JWT] Load user failed for {}: {}", username, e.getMessage());
            SecurityContextHolder.clearContext();
            return null;
        }
    }

    private void setAuthentication(UserDetails user, HttpServletRequest request) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private void kickLockedUser(HttpServletResponse response, String accessToken, String refreshToken)
            throws IOException {
        SecurityContextHolder.clearContext();
        blacklistToken(accessToken);
        blacklistToken(refreshToken);
        cookieService.clearAuthCookies(response);
        if (!response.isCommitted()) {
            response.setStatus(HttpServletResponse.SC_LOCKED);
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"" + LOCKED_MESSAGE + "\"}");
        }
    }

    private void blacklistToken(String token) {
        if (token == null) return;
        try {
            var claims = jwtService.extractClaims(token);
            tokenBlacklist.blacklist(claims.getId(), claims.getExpiration().toInstant());
        } catch (Exception ignored) {
            log.debug("[JWT] Không blacklist được token: {}", ignored.getMessage());
        }
    }
}
```

- [ ] **Step 4: Run tests to verify they pass**

Run: `.\gradlew.bat test --tests "com.example.thexuong.security.JwtAuthenticationFilterTest"`
Expected: PASS — cả 3 test.

- [ ] **Step 5: Run full backend test suite (regression)**

Run: `.\gradlew.bat test`
Expected: tất cả PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/java/com/example/thexuong/security/JwtAuthenticationFilter.java src/test/java/com/example/thexuong/security/JwtAuthenticationFilterTest.java
git commit -m "fix: kick locked users out of active sessions with 423"
```

---

### Task 3: Chặn refresh endpoint + Google OAuth2 khi bị khóa

**Files:**
- Modify: `src/main/java/com/example/thexuong/controller/AuthRestController.java` (method `refresh`, dòng 119-139)
- Modify: `src/main/java/com/example/thexuong/security/OAuth2SuccessHandler.java`
- Test: `src/test/java/com/example/thexuong/controller/AuthRestControllerRefreshTest.java`
- Test: `src/test/java/com/example/thexuong/security/OAuth2SuccessHandlerTest.java`

**Interfaces:**
- Consumes: `LoginHistoryService.recordLogin(String email, String ip, String userAgent, String provider, boolean success, String failureReason)`; `UserRepository.findByEmail(String)` → `Optional<User>`; `JwtService.generateAccessToken/generateRefreshToken(UserDetails)`; `JwtCookieService.setAuthCookies(res, access, refresh)`.
- Produces: `AuthRestController.refresh` trả 423 khi user bị khóa; `OAuth2SuccessHandler` redirect `{frontendUrl}/login?locked=1` khi user bị khóa (không cấp token).

- [ ] **Step 1: Write the failing tests**

Create `src/test/java/com/example/thexuong/controller/AuthRestControllerRefreshTest.java`:

```java
package com.example.thexuong.controller;

import com.example.thexuong.security.JwtCookieService;
import com.example.thexuong.security.JwtService;
import com.example.thexuong.security.TokenBlacklist;
import com.example.thexuong.service.LoginHistoryService;
import com.example.thexuong.service.PasswordResetService;
import com.example.thexuong.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuthRestControllerRefreshTest {

    @Test
    void refresh_returns423WhenUserLocked() {
        JwtService jwtService = mock(JwtService.class);
        JwtCookieService cookieService = mock(JwtCookieService.class);
        UserDetailsService userDetailsService = mock(UserDetailsService.class);

        AuthRestController controller = new AuthRestController(
                mock(AuthenticationManager.class),
                mock(UserService.class),
                mock(PasswordResetService.class),
                mock(LoginHistoryService.class),
                jwtService,
                cookieService,
                mock(TokenBlacklist.class),
                userDetailsService);

        when(cookieService.readCookie(any(), eq("refresh_token"))).thenReturn("refresh-token");
        when(jwtService.isValid("refresh-token")).thenReturn(true);
        when(jwtService.isRefreshToken("refresh-token")).thenReturn(true);
        when(jwtService.extractUsername("refresh-token")).thenReturn("locked@test.com");

        UserDetails locked = org.springframework.security.core.userdetails.User.withUsername("locked@test.com")
                .password("")
                .disabled(true)
                .authorities(List.of(new SimpleGrantedAuthority("CUSTOMER")))
                .build();
        when(userDetailsService.loadUserByUsername("locked@test.com")).thenReturn(locked);

        ResponseEntity<?> resp = controller.refresh(new MockHttpServletRequest(), new MockHttpServletResponse());

        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.LOCKED);
        Map<?, ?> body = (Map<?, ?>) resp.getBody();
        assertThat(body.get("error")).isEqualTo("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
    }
}
```

Create `src/test/java/com/example/thexuong/security/OAuth2SuccessHandlerTest.java`:

```java
package com.example.thexuong.security;

import com.example.thexuong.entity.User;
import com.example.thexuong.repository.UserRepository;
import com.example.thexuong.service.LoginHistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OAuth2SuccessHandlerTest {

    @Test
    void lockedUser_redirectsToLoginWithLockedFlagAndNoTokens() throws Exception {
        UserRepository userRepository = mock(UserRepository.class);
        JwtService jwtService = mock(JwtService.class);
        JwtCookieService cookieService = mock(JwtCookieService.class);
        LoginHistoryService loginHistoryService = mock(LoginHistoryService.class);

        OAuth2SuccessHandler handler =
                new OAuth2SuccessHandler(userRepository, jwtService, cookieService, loginHistoryService);
        ReflectionTestUtils.setField(handler, "frontendUrl", "http://localhost:5173");

        User locked = User.builder()
                .email("locked@test.com")
                .username("locked@test.com")
                .fullName("Locked User")
                .provider("GOOGLE")
                .role("CUSTOMER")
                .active(false)
                .build();
        when(userRepository.findByEmail("locked@test.com")).thenReturn(Optional.of(locked));

        OAuth2User oAuth2User = new DefaultOAuth2User(
                List.of(), Map.of("email", "locked@test.com", "name", "Locked User"), "email");
        Authentication authentication = mock(Authentication.class);
        when(authentication.getPrincipal()).thenReturn(oAuth2User);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        handler.onAuthenticationSuccess(request, response, authentication);

        assertThat(response.getRedirectedUrl()).isEqualTo("http://localhost:5173/login?locked=1");
        verify(jwtService, never()).generateAccessToken(any());
        verify(loginHistoryService).recordLogin(
                eq("locked@test.com"), anyString(), any(), eq("GOOGLE"), eq(false), anyString());
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `.\gradlew.bat test --tests "com.example.thexuong.controller.AuthRestControllerRefreshTest" --tests "com.example.thexuong.security.OAuth2SuccessHandlerTest"`
Expected: FAIL — refresh trả 200 (không check isEnabled); OAuth redirect về `/oauth/callback` (không chặn).

- [ ] **Step 3: Implement — `AuthRestController.java` method `refresh`**

Thay block hiện tại (dòng 126-138):

```java
        String email = jwtService.extractUsername(refreshToken);
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            String newAccess = jwtService.generateAccessToken(userDetails);
            String newRefresh = jwtService.generateRefreshToken(userDetails);
            jwtCookieService.setAuthCookies(response, newAccess, newRefresh);

            User user = userService.getUserByEmailWithAddresses(email);
            return ResponseEntity.ok(Map.of("user", toUserResponse(user)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Không thể refresh token"));
        }
```

bằng:

```java
        String email = jwtService.extractUsername(refreshToken);
        try {
            UserDetails userDetails = userDetailsService.loadUserByUsername(email);
            if (!userDetails.isEnabled()) {
                return ResponseEntity.status(HttpStatus.LOCKED)
                        .body(Map.of("error", "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên."));
            }
            String newAccess = jwtService.generateAccessToken(userDetails);
            String newRefresh = jwtService.generateRefreshToken(userDetails);
            jwtCookieService.setAuthCookies(response, newAccess, newRefresh);

            User user = userService.getUserByEmailWithAddresses(email);
            return ResponseEntity.ok(Map.of("user", toUserResponse(user)));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Không thể refresh token"));
        }
```

- [ ] **Step 4: Implement — `OAuth2SuccessHandler.java`**

Sau bước load user (hết dòng 56, trước comment "3. Build UserDetails") chèn:

```java
        // 2b. Nếu tài khoản bị khóa → không cấp token, redirect về login kèm thông báo
        if (!Boolean.TRUE.equals(user.getActive())) {
            loginHistoryService.recordLogin(
                    email,
                    request.getRemoteAddr(),
                    request.getHeader("User-Agent"),
                    "GOOGLE", false, "Tài khoản bị khóa");
            getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/login?locked=1");
            return;
        }
```

- [ ] **Step 5: Run tests to verify they pass**

Run: `.\gradlew.bat test --tests "com.example.thexuong.controller.AuthRestControllerRefreshTest" --tests "com.example.thexuong.security.OAuth2SuccessHandlerTest"`
Expected: PASS.

- [ ] **Step 6: Run full backend test suite (regression)**

Run: `.\gradlew.bat test`
Expected: tất cả PASS.

- [ ] **Step 7: Commit**

```bash
git add src/main/java/com/example/thexuong/controller/AuthRestController.java src/main/java/com/example/thexuong/security/OAuth2SuccessHandler.java src/test/java/com/example/thexuong/controller/AuthRestControllerRefreshTest.java src/test/java/com/example/thexuong/security/OAuth2SuccessHandlerTest.java
git commit -m "fix: block locked users from refresh and Google login"
```

---

### Task 4: Frontend — axios interceptor bắt 423

**Files:**
- Modify: `frontend/src/services/http.ts`
- Test: `frontend/src/services/__tests__/http.spec.ts`

**Interfaces:**
- Consumes: `vue-toastification` `useToast()` (plugin đã cài trong `main.ts` — gọi bên trong interceptor lúc runtime).
- Produces: export `LOCKED_REDIRECT_PATH = '/login?locked=1'`; export `shouldRedirectToLogin(status: number | undefined, pathname: string): boolean` — Task 5 không phụ thuộc, nhưng test phụ thuộc 2 export này.

- [ ] **Step 1: Write the failing test**

Create `frontend/src/services/__tests__/http.spec.ts`:

```ts
import { describe, it, expect } from 'vitest'
import { shouldRedirectToLogin, LOCKED_REDIRECT_PATH } from '@/services/http'

describe('shouldRedirectToLogin', () => {
  it('redirects on 423 outside login page', () => {
    expect(shouldRedirectToLogin(423, '/checkout')).toBe(true)
  })

  it('does not redirect on 423 when already on /login', () => {
    expect(shouldRedirectToLogin(423, '/login')).toBe(false)
  })

  it('does not redirect on other statuses', () => {
    expect(shouldRedirectToLogin(401, '/checkout')).toBe(false)
    expect(shouldRedirectToLogin(undefined, '/checkout')).toBe(false)
  })
})

describe('LOCKED_REDIRECT_PATH', () => {
  it('points to login with locked flag', () => {
    expect(LOCKED_REDIRECT_PATH).toBe('/login?locked=1')
  })
})
```

- [ ] **Step 2: Run test to verify it fails**

Run (workdir `frontend`): `npx vitest run src/services/__tests__/http.spec.ts`
Expected: FAIL — module không export `shouldRedirectToLogin`.

- [ ] **Step 3: Implement — thay toàn bộ `frontend/src/services/http.ts`**

```ts
import axios, { AxiosInstance } from 'axios'
import { useToast } from 'vue-toastification'

const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1'

export const LOCKED_REDIRECT_PATH = '/login?locked=1'

export function shouldRedirectToLogin(status: number | undefined, pathname: string): boolean {
  return status === 423 && pathname !== '/login'
}

const client: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: { 'Content-Type': 'application/json' },
  withCredentials: true
})

client.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status
    if (shouldRedirectToLogin(status, window.location.pathname)) {
      const msg = error.response?.data?.error || error.response?.data?.message ||
        'Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.'
      useToast().error(msg)
      window.location.href = LOCKED_REDIRECT_PATH
      return Promise.reject(error)
    }
    if (status === 401) {
      if (error.config?.url && !error.config.url.includes('/auth/user') && window.location.pathname !== '/login') {
        window.location.href = '/login'
      }
    }
    if (status === 403) {
      // Review dùng 403 cho lỗi nghiệp vụ (chưa mua, không phải chủ review) → không redirect, để component hiện toast.
      const url = error.config?.url || ''
      if (!url.startsWith('/reviews')) window.location.href = '/'
    }
    return Promise.reject(error)
  }
)

const http = {
  get: (url: string, config?: any) => client.get(url, config),
  post: (url: string, data?: any, config?: any) => client.post(url, data, config),
  put: (url: string, data?: any, config?: any) => client.put(url, data, config),
  patch: (url: string, data?: any, config?: any) => client.patch(url, data, config),
  delete: (url: string, config?: any) => client.delete(url, config)
}

export default http
```

- [ ] **Step 4: Run test to verify it passes**

Run (workdir `frontend`): `npx vitest run src/services/__tests__/http.spec.ts`
Expected: PASS.

- [ ] **Step 5: Run frontend type-check (regression)**

Run (workdir `frontend`): `npm run type-check`
Expected: PASS, không có lỗi mới.

- [ ] **Step 6: Commit**

```bash
git add frontend/src/services/http.ts frontend/src/services/__tests__/http.spec.ts
git commit -m "feat: intercept 423 and redirect to login with locked notice"
```

---

### Task 5: Frontend — alert "tài khoản bị khóa" trên trang login + i18n + map message

**Files:**
- Modify: `frontend/src/views/Login.vue`
- Modify: `frontend/src/i18n/locales/vi.json` (section `auth`, sau key `logoutSuccess` dòng 98)
- Modify: `frontend/src/i18n/locales/en.json` (section `auth`, sau key `logoutSuccess` dòng 98)
- Modify: `frontend/src/utils/apiError.ts` (map `BACKEND_MESSAGE_KEYS`)
- Test: `frontend/src/views/__tests__/Login.spec.ts`

**Interfaces:**
- Consumes: i18n key `auth.accountLocked` (định nghĩa trong task này); `route.query.locked === '1'`.
- Produces: alert hiển thị trên Login.vue; `BACKEND_MESSAGE_KEYS` map message backend → key i18n.

- [ ] **Step 1: Write the failing test**

Create `frontend/src/views/__tests__/Login.spec.ts`:

```ts
import { describe, it, expect, vi, beforeEach, afterEach } from 'vitest'
import { mount } from '@vue/test-utils'
import Login from '@/views/Login.vue'

vi.mock('vue-router', () => ({
  useRouter: () => ({ push: vi.fn() }),
  useRoute: () => ({ query: { locked: '1' } })
}))
vi.mock('vue-i18n', () => ({
  useI18n: () => ({ t: (key: string) => key })
}))
vi.mock('vee-validate', () => ({
  useForm: () => ({ handleSubmit: (fn: unknown) => fn, isSubmitting: { value: false } }),
  useField: () => ({ value: { value: '' }, errorMessage: { value: '' } })
}))
vi.mock('@vee-validate/zod', () => ({ toTypedSchema: (schema: unknown) => schema }))
vi.mock('vue-toastification', () => ({ useToast: () => ({ error: vi.fn(), success: vi.fn(), info: vi.fn() }) }))
vi.mock('@/stores/auth.store', () => ({
  useAuthStore: () => ({
    login: vi.fn(),
    redirectTo: null,
    setRedirectPath: vi.fn(),
    isAdmin: false
  })
}))
vi.mock('@/utils/apiError', () => ({ getApiErrorMessage: (_e: unknown, key: string) => key }))

const stubs = {
  BaseInput: true,
  BaseButton: true,
  RouterLink: { template: '<a><slot /></a>' }
}

describe('Login.vue locked alert', () => {
  beforeEach(() => {
    vi.useFakeTimers()
  })
  afterEach(() => {
    vi.useRealTimers()
  })

  it('shows locked alert when route.query.locked=1', () => {
    const wrapper = mount(Login, { global: { stubs } })
    expect(wrapper.text()).toContain('auth.accountLocked')
  })

  it('hides locked alert after 8 seconds', () => {
    const wrapper = mount(Login, { global: { stubs } })
    vi.advanceTimersByTime(8000)
    expect(wrapper.text()).not.toContain('auth.accountLocked')
  })
})
```

- [ ] **Step 2: Run test to verify it fails**

Run (workdir `frontend`): `npx vitest run src/views/__tests__/Login.spec.ts`
Expected: FAIL — không tìm thấy text `auth.accountLocked`.

- [ ] **Step 3: Implement — `frontend/src/views/Login.vue`**

Chèn alert ngay SAU block "Register Success Alert" (sau dòng 59, trước comment `<!-- Login Form -->`):

```html
          <!-- Locked Account Alert -->
          <div
            v-if="showLockedAlert"
            class="bg-[#FDE8E8] border border-[#F5C6C6] rounded-lg p-4 flex items-center gap-3"
          >
            <svg
              class="w-5 h-5 text-[#9B1C1C] flex-shrink-0"
              viewBox="0 0 24 24"
              fill="none"
              stroke="currentColor"
              stroke-width="2"
              stroke-linecap="round"
              stroke-linejoin="round"
            >
              <circle cx="12" cy="12" r="10" />
              <line x1="12" y1="8" x2="12" y2="12" />
              <line x1="12" y1="16" x2="12.01" y2="16" />
            </svg>
            <span class="font-gelasio text-sm font-semibold text-[#9B1C1C]">
              {{ t('auth.accountLocked') }}
            </span>
          </div>
```

Thêm ref sau dòng 280 (`const showRegisterSuccess = ref(false)`):

```ts
const showLockedAlert = ref(false)
```

Trong `onMounted` (sau block kiểm tra `registered`, trước block `redirectPath` dòng 300), thêm:

```ts
// Show locked account alert if redirected after being kicked
if (route.query.locked === '1') {
  showLockedAlert.value = true
  setTimeout(() => {
    showLockedAlert.value = false
  }, 8000)
}
```

- [ ] **Step 4: Implement — `frontend/src/i18n/locales/vi.json`**

Sau dòng `"logoutSuccess": "Đăng xuất thành công!",` (trong section `auth`) thêm:

```json
    "accountLocked": "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.",
```

- [ ] **Step 5: Implement — `frontend/src/i18n/locales/en.json`**

Sau dòng `"logoutSuccess": "Logged out successfully!",` (trong section `auth`) thêm:

```json
    "accountLocked": "Your account has been locked. Please contact the administrator.",
```

- [ ] **Step 6: Implement — `frontend/src/utils/apiError.ts`**

Trong object `BACKEND_MESSAGE_KEYS` (sau dòng `'Email hoặc mật khẩu không đúng': 'backendError.loginFailed',`) thêm:

```ts
  'Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.': 'auth.accountLocked',
```

- [ ] **Step 7: Run test to verify it passes**

Run (workdir `frontend`): `npx vitest run src/views/__tests__/Login.spec.ts`
Expected: PASS — cả 2 test.

- [ ] **Step 8: Run full frontend test suite + type-check (regression)**

Run (workdir `frontend`): `npm run test`
Expected: PASS tất cả (auth.store.spec, cart.store.spec, http.spec, Login.spec).

Run (workdir `frontend`): `npm run type-check`
Expected: PASS.

- [ ] **Step 9: Commit**

```bash
git add frontend/src/views/Login.vue frontend/src/i18n/locales/vi.json frontend/src/i18n/locales/en.json frontend/src/utils/apiError.ts frontend/src/views/__tests__/Login.spec.ts
git commit -m "feat: show account locked alert on login page"
```

---

### Task 6: Verification toàn bộ

**Files:** không sửa file nào.

- [ ] **Step 1: Backend full test suite**

Run: `.\gradlew.bat test`
Expected: BUILD SUCCESSFUL, tất cả test PASS.

- [ ] **Step 2: Frontend full test suite + lint + type-check**

Run (workdir `frontend`): `npm run test && npm run type-check && npm run lint`
Expected: tất cả PASS.

- [ ] **Step 3: Manual test checklist (cần app chạy + DB thật)**

1. Login bằng tài khoản đang active → OK như cũ.
2. Admin (BOTH) khóa 1 tài khoản CUSTOMER đang đăng nhập → user đó thao tác tiếp bất kỳ API nào: nhận 423, toast "Tài khoản của bạn đã bị khóa...", redirect `/login?locked=1`, alert đỏ hiện trên trang login.
3. User bị khóa login lại bằng mật khẩu → 423 + toast "Tài khoản của bạn đã bị khóa...".
4. User bị khóa bấm "Đăng nhập bằng Google" → redirect về `/login?locked=1` + alert, không có cookie mới (DevTools → Application → Cookies).
5. Admin mở khóa → user đăng nhập lại bình thường (token cũ KHÔNG tự hoạt động lại vì đã blacklist).
6. Token hết hạn thông thường (không khóa) → vẫn tự refresh như cũ.
7. 401 sai mật khẩu vẫn hiển thị "Email hoặc mật khẩu không đúng" (không đổi).

- [ ] **Step 4: Commit nếu có thay đổi phát sinh từ test tay**

Chỉ commit nếu bước 3 phát hiện lỗi phải sửa. Nếu không có gì thay đổi thì bỏ qua bước này.
