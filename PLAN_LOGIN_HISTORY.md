# PLAN — Lịch Sử Đăng Nhập (Login History) cho TheXuong

> Mục tiêu: ghi lại lịch sử đăng nhập hệ thống và hiển thị trong trang Admin Audit Logs.
> Phạm vi đã chốt với anh (brainstorming):
> - **Ghi**: login admin (ADMIN/BOTH) THÀNH CÔNG + **mọi lần login THẤT BẠI** (mọi email, để phát hiện brute-force).
> - **Hiển thị**: tab "Đăng Nhập" trong trang `/admin/audit-logs` (không tạo route mới).
> - **Dữ liệu mỗi bản ghi**: email, IP, User-Agent, provider (LOCAL/GOOGLE), kết quả + lý do thất bại, thời gian.
> - **Nút "Khoá tài khoản"** từ log → dùng API `PATCH /api/v1/admin/users/{id}/toggle-active` **đã có sẵn** (không viết API chặn mới).
> - **Phương án đã duyệt (A)**: hook trực tiếp vào `AuthRestController.login` (thành công + thất bại) và `OAuth2SuccessHandler` (Google thành công). Không đụng `SecurityConfig`.

## Quyết định thiết kế

| # | Quyết định | Lý do |
|---|---|---|
| 1 | Bảng mới `LoginHistory` (không đổ vào `SystemAuditLog`) | Bản ghi login khác bản chất (mỗi lần thử, kể cả thất bại); tránh trộn với audit thao tác admin; query riêng sạch hơn |
| 2 | Lưu `user_id` NULL-able (nullable FK Users) | Thất bại có thể do email không tồn tại → user_id = NULL, vẫn giữ email string |
| 3 | `success` + `failure_reason` (lý do: sai mật khẩu / user disabled / không tồn tại...) | `DisabledException` message cho ta biết tài khoản bị khoá |
| 4 | Hook: `AuthRestController.login()` — catch `AuthenticationException` → ghi fail → **rethrow** giữ nguyên response 401 | Không đổi hành vi API hiện tại |
| 5 | Hook: `OAuth2SuccessHandler.onAuthenticationSuccess` — ghi success (GOOGLE) | Google fail hiếm, không hook (YAGNI) |
| 6 | IP dùng `request.getRemoteAddr()` | Anh không chọn X-Forwarded-For; lưu ý: khi deploy sau nginx/proxy, IP sẽ là proxy — chấp nhận cho phiên này |
| 7 | `User-Agent` cắt tối đa 500 ký tự | Khớp cột DB |
| 8 | API: `GET /api/v1/admin/login-history` (filters: email, provider, success, from, to, page, size, sort) — shape `ApiResponse<PageResponse<...>>` như customer-care | Đồng bộ convention backend hiện có |
| 9 | UI: tab trong `AdminAuditLogs.vue` + component mới `LoginHistoryTab.vue` | Giữ file gọn, đúng phạm vi |
| 10 | Nút "Khoá tài khoản": gọi `adminService.toggleUserActive(userId)` đã có; chỉ hiện khi bản ghi có `userId` | Không thêm API backend mới |
| 11 | Không tự động xoá log cũ (retention) | YAGNI — chưa cần |
| 12 | Không ghi register/refresh/logout | Ngoài phạm vi "lịch sử đăng nhập" |

---

## 1. File Structure

### Backend (tạo mới)
- `src/main/java/com/example/thexuong/entity/LoginHistory.java` — entity
- `src/main/java/com/example/thexuong/repository/LoginHistoryRepository.java`
- `src/main/java/com/example/thexuong/dto/loginhistory/AdminLoginHistoryResponse.java`
- `src/main/java/com/example/thexuong/service/LoginHistoryService.java`
- `src/main/java/com/example/thexuong/controller/LoginHistoryRestController.java`

### Backend (sửa)
- `src/main/java/com/example/thexuong/controller/AuthRestController.java` — hook login success/fail
- `src/main/java/com/example/thexuong/security/OAuth2SuccessHandler.java` — hook Google success

### DB
- `dbTheXuong.sql` — thêm bảng `LoginHistory`
- Script chạy trên DB prod/local (xem Task 1)

### Frontend (tạo mới)
- `frontend/src/components/admin/audit-logs/LoginHistoryTab.vue`
- `frontend/src/services/loginHistoryAdmin.service.ts`

### Frontend (sửa)
- `frontend/src/views/admin/AdminAuditLogs.vue` — thêm tab switcher

---

## 2. PHASE 0 — Review context

- [x] **P0-T1** Đọc lại: `AuthRestController.login()`, `OAuth2SuccessHandler`, `AdminCustomerCareService.getLogs` (pattern Specification + parseDateParam), `PageResponse`, `ApiResponse.ok`.
- [x] **P0-T2** Xác nhận `adminService.toggleUserActive(id)` đã có trong `admin.service.ts:11` và `User.active` map cột `active BIT DEFAULT 1`.

---

## 3. PHASE 1 — DB + Entity + Repository

### P1-T1: Script SQL

```sql
-- LoginHistory: lịch sử đăng nhập
IF OBJECT_ID('LoginHistory', 'U') IS NULL
BEGIN
    CREATE TABLE LoginHistory (
        id             BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id        BIGINT NULL,
        email          NVARCHAR(255) NOT NULL,
        ip_address     NVARCHAR(45)  NULL,
        user_agent     NVARCHAR(500) NULL,
        provider       NVARCHAR(20)  NOT NULL DEFAULT 'LOCAL',
        success        BIT           NOT NULL DEFAULT 1,
        failure_reason NVARCHAR(255) NULL,
        created_at     DATETIME2     NOT NULL DEFAULT SYSDATETIME()
    );

    CREATE INDEX IX_LoginHistory_createdAt ON LoginHistory(created_at DESC);
    CREATE INDEX IX_LoginHistory_email     ON LoginHistory(email);
    CREATE INDEX IX_LoginHistory_userId    ON LoginHistory(user_id);
END
```

- [x] **P1-T1a** Chạy script trên DB **local** (SQL Server localhost:1444 dbTheXuong) và ghi chú cho anh chạy trên **prod**.
- [x] **P1-T1b** Thêm bảng vào `dbTheXuong.sql` (sau phần `SystemAuditLog`).

### P1-T2: Entity `LoginHistory.java`

```java
package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "LoginHistory")
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(nullable = false, length = 20)
    private String provider;

    @Column(nullable = false)
    private Boolean success;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
```

### P1-T3: Repository `LoginHistoryRepository.java`

```java
package com.example.thexuong.repository;

import com.example.thexuong.entity.LoginHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoginHistoryRepository
        extends JpaRepository<LoginHistory, Long>, JpaSpecificationExecutor<LoginHistory> {
}
```

- [x] **Gate P1**: `gradlew compileJava` PASS.

---

## 4. PHASE 2 — DTO + Service

### P2-T1: DTO `AdminLoginHistoryResponse.java`

```java
package com.example.thexuong.dto.loginhistory;

import com.example.thexuong.entity.LoginHistory;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminLoginHistoryResponse {

    private Long id;
    private Long userId;
    private String email;
    private String ipAddress;
    private String userAgent;
    private String provider;
    private Boolean success;
    private String failureReason;
    private LocalDateTime createdAt;

    public static AdminLoginHistoryResponse fromEntity(LoginHistory e) {
        return AdminLoginHistoryResponse.builder()
                .id(e.getId())
                .userId(e.getUserId())
                .email(e.getEmail())
                .ipAddress(e.getIpAddress())
                .userAgent(e.getUserAgent())
                .provider(e.getProvider())
                .success(e.getSuccess())
                .failureReason(e.getFailureReason())
                .createdAt(e.getCreatedAt())
                .build();
    }
}
```

### P2-T2: Service `LoginHistoryService.java`

```java
package com.example.thexuong.service;

import com.example.thexuong.dto.customercare.PageResponse;
import com.example.thexuong.dto.loginhistory.AdminLoginHistoryResponse;
import com.example.thexuong.entity.LoginHistory;
import com.example.thexuong.entity.User;
import com.example.thexuong.repository.LoginHistoryRepository;
import com.example.thexuong.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class LoginHistoryService {

    private final LoginHistoryRepository loginHistoryRepository;
    private final UserRepository userRepository;

    /**
     * Ghi 1 lần đăng nhập. KHÔNG BAO GIỜ throw — không được phá luồng login.
     * success=true chỉ ghi khi user là ADMIN/BOTH.
     * success=false ghi mọi email (kể cả email không tồn tại).
     */
    @Transactional
    public void recordLogin(String email, String ip, String userAgent, String provider,
                            boolean success, String failureReason) {
        try {
            String cleanEmail = (email == null ? "" : email.trim());
            User user = userRepository.findByEmail(cleanEmail).orElse(null);

            if (success && (user == null || !isAdminRole(user.getRole()))) {
                return; // login khách hàng: không ghi
            }

            LoginHistory history = LoginHistory.builder()
                    .userId(user != null ? user.getId() : null)
                    .email(cleanEmail)
                    .ipAddress(truncate(ip, 45))
                    .userAgent(truncate(userAgent, 500))
                    .provider(provider)
                    .success(success)
                    .failureReason(truncate(failureReason, 255))
                    .build();
            loginHistoryRepository.save(history);
        } catch (Exception e) {
            log.error("Không ghi được login history: {}", e.getMessage());
        }
    }

    private boolean isAdminRole(String role) {
        return "ADMIN".equalsIgnoreCase(role) || "BOTH".equalsIgnoreCase(role);
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }

    /**
     * Danh sách login history: filter email (like), provider, success, from/to (yyyy-MM-dd).
     * Default sort createdAt DESC, id DESC.
     */
    @Transactional(readOnly = true)
    public PageResponse<AdminLoginHistoryResponse> getHistory(
            String email, String provider, Boolean success, String from, String to,
            int page, int size, String sort) {

        Specification<LoginHistory> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (email != null && !email.isBlank()) {
                predicates.add(cb.like(cb.lower(root.get("email")),
                        "%" + email.trim().toLowerCase() + "%"));
            }
            if (provider != null && !provider.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("provider")),
                        provider.trim().toLowerCase()));
            }
            if (success != null) {
                predicates.add(cb.equal(root.get("success"), success));
            }
            LocalDateTime fromDt = parseDateParam(from, true);
            if (fromDt != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fromDt));
            }
            LocalDateTime toDt = parseDateParam(to, false);
            if (toDt != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), toDt));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Sort.Direction direction = Sort.Direction.DESC;
        if (sort != null && sort.toLowerCase().startsWith("createdat,asc")) {
            direction = Sort.Direction.ASC;
        }

        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                Math.min(Math.max(1, size), 100),
                Sort.by(direction, "createdAt").and(Sort.by(direction, "id"))
        );

        Page<LoginHistory> historyPage = loginHistoryRepository.findAll(spec, pageable);
        return PageResponse.from(historyPage.map(AdminLoginHistoryResponse::fromEntity));
    }

    /** yyyy-MM-dd → from=đầu ngày, to=cuối ngày; sai format → null (bỏ qua, không crash). */
    private LocalDateTime parseDateParam(String value, boolean startOfDay) {
        if (value == null || value.isBlank()) return null;
        try {
            LocalDate date = LocalDate.parse(value.trim());
            return startOfDay ? date.atStartOfDay() : date.atTime(23, 59, 59);
        } catch (Exception e) {
            return null;
        }
    }
}
```

> Lưu ý: cần xác nhận `UserRepository` có `findByEmail` trả `Optional<User>` (dùng trong `OAuth2SuccessHandler` — có). Nếu signature khác (vd trả User), điều chỉnh 1 dòng.

- [x] **Gate P2**: `gradlew compileJava` PASS.

---

## 5. PHASE 3 — Controller + Hook login

### P3-T1: Controller `LoginHistoryRestController.java`

```java
package com.example.thexuong.controller;

import com.example.thexuong.dto.ApiResponse;
import com.example.thexuong.dto.customercare.PageResponse;
import com.example.thexuong.dto.loginhistory.AdminLoginHistoryResponse;
import com.example.thexuong.service.LoginHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/login-history")
@RequiredArgsConstructor
@PreAuthorize("hasAnyAuthority('ADMIN', 'BOTH')")
public class LoginHistoryRestController {

    private final LoginHistoryService loginHistoryService;

    /**
     * GET /api/v1/admin/login-history?email=&provider=&success=&from=&to=&page=&size=&sort=
     */
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AdminLoginHistoryResponse>>> getHistory(
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String provider,
            @RequestParam(required = false) Boolean success,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return ResponseEntity.ok(ApiResponse.ok("Lấy lịch sử đăng nhập thành công.",
                loginHistoryService.getHistory(email, provider, success, from, to, page, size, sort)));
    }
}
```

> Kiểm tra `ApiResponse.ok` có overload (String message, T data) — dùng như `AdminCustomerCareRestController`.

### P3-T2: Hook vào `AuthRestController.login()`

Sửa block try/catch hiện tại (dòng 55-62) + sau khi set cookies:

```java
Authentication authentication;
try {
    authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
    );
} catch (org.springframework.security.core.AuthenticationException e) {
    loginHistoryService.recordLogin(
            request.getEmail(),
            httpRequest.getRemoteAddr(),
            httpRequest.getHeader("User-Agent"),
            "LOCAL", false, e.getMessage());
    throw e; // GlobalExceptionHandler returns 401
}
SecurityContextHolder.getContext().setAuthentication(authentication);

loginHistoryService.recordLogin(
        request.getEmail(),
        httpRequest.getRemoteAddr(),
        httpRequest.getHeader("User-Agent"),
        "LOCAL", true, null);
```

+ Thêm field `private final LoginHistoryService loginHistoryService;` (class đang `@RequiredArgsConstructor`).

### P3-T3: Hook vào `OAuth2SuccessHandler.onAuthenticationSuccess`

Thêm sau khi build `userDetails` (trước khi issue JWT):

```java
loginHistoryService.recordLogin(
        email,
        request.getRemoteAddr(),
        request.getHeader("User-Agent"),
        "GOOGLE", true, null);
```

+ Thêm field `private final LoginHistoryService loginHistoryService;`.

- [x] **Gate P3**: `gradlew compileJava` PASS.

---

## 6. PHASE 4 — Frontend: service + types

### P4-T1: `frontend/src/services/loginHistoryAdmin.service.ts`

```ts
import http from './http'

export interface AdminLoginHistory {
  id: number
  userId: number | null
  email: string
  ipAddress: string | null
  userAgent: string | null
  provider: string
  success: boolean
  failureReason: string | null
  createdAt: string
}

export interface LoginHistoryPage {
  content: AdminLoginHistory[]
  totalElements: number
  totalPages: number
  size: number
  number: number
}

export const loginHistoryAdminService = {
  async getHistory(params: {
    email?: string
    provider?: string
    success?: boolean
    from?: string
    to?: string
    page?: number
    size?: number
    sort?: string
  }): Promise<LoginHistoryPage> {
    const res = await http.get('/admin/login-history', { params })
    return res.data?.data
  },
}
```

> Xác nhận `http` instance hỗ trợ `params` (axios wrapper `frontend/src/services/http.ts` — kiểm tra tại P4).

- [x] **Gate P4**: `npm run build` PASS.

---

## 7. PHASE 5 — Frontend: tab "Đăng Nhập" trong AdminAuditLogs.vue

### P5-T1: Component `LoginHistoryTab.vue`

Cấu trúc:
- Header: "Lịch Sử Đăng Nhập" + tổng số bản ghi + nút TẢI LẠI.
- Filter bar: input email, select provider (TẤT CẢ/LOCAL/GOOGLE), select kết quả (TẤT CẢ/THÀNH CÔNG/THẤT BẠI), input date from, input date to, nút "ÁP DỤNG".
- Bảng: THỜI GIAN | EMAIL | IP | NGUỒN | KẾT QUẢ | LÝ DO | THAO TÁC
  - KẾT QUẢ: badge xanh "Thành công" / đỏ "Thất bại" (style `badge-create`/`badge-delete` của file hiện tại).
  - THAO TÁC: nút "Khoá tài khoản" (chỉ hiện khi `log.userId != null`) → confirm → `adminService.toggleUserActive(log.userId)` → toast theo message API + reload.
- Phân trang giống `CustomerCareOverview` pattern (nút ← → + "Trang X/Y").
- Style đồng bộ Admin hiện tại (table, badge, button như `AdminAuditLogs.vue`).

### P5-T2: Sửa `AdminAuditLogs.vue`

- Thêm tab switcher đầu trang:
  - Tab 1: "LỊCH SỬ HỆ THỐNG" (nội dung hiện tại giữ nguyên).
  - Tab 2: "ĐĂNG NHẬP" → render `<LoginHistoryTab />`.
- Style tab: hàng button đơn giản, tab active có nền đen chữ trắng (đồng bộ menu AdminLayout).

- [x] **Gate P5**: `npm run build` PASS; `npm run type-check` không lỗi mới (2 lỗi pre-existing ngoài scope).

---

## 8. PHASE 6 — Smoke test + checklist

- [ ] **P6-T1** Login thành công với `both@gmail.com` → gọi `GET /api/v1/admin/login-history` → thấy bản ghi success=true, provider=LOCAL, IP/UA đúng.
- [ ] **P6-T2** Login sai mật khẩu với email bất kỳ → xuất hiện bản ghi success=false + lý do.
- [ ] **P6-T3** Login với tài khoản khách (vd customer) thành công → KHÔNG xuất hiện bản ghi mới (đúng phạm vi).
- [ ] **P6-T4** Không token gọi `/api/v1/admin/login-history` → 401.
- [ ] **P6-T5** Filter: email, success, provider, from/to, phân trang hoạt động.
- [ ] **P6-T6** Nút "Khoá tài khoản" → khoá OK → login tài khoản đó thất bại → log thất bại mới xuất hiện. Mở khoá lại.
- [ ] **P6-T7** `gradlew build -x test` PASS; `npm run build` PASS.
- [ ] **P6-T8** Cập nhật `PLAN_LOGIN_HISTORY.md` phần Progress Report.

---

## 9. Acceptance Checklist (đối chiếu yêu cầu anh)

- [ ] Ghi mọi login ADMIN/BOTH thành công (LOCAL + GOOGLE).
- [ ] Ghi mọi login thất bại (mọi email, có lý do).
- [ ] Login khách hàng thành công không spam log.
- [ ] Tab "Đăng Nhập" trong `/admin/audit-logs` hiển thị đủ: thời gian, email, IP, nguồn, kết quả, lý do.
- [ ] Có filter + phân trang.
- [ ] Khoá/mở khoá tài khoản trực tiếp từ log.
- [ ] Không phá luồng login hiện tại (response/status giữ nguyên).

---

## 10. Những việc cần anh làm

- [ ] Chạy script SQL PHASE 1 trên DB **prod** (DB local tôi tự chạy).
- [ ] Deploy backend + frontend mới lên prod.
- [ ] (Tuỳ chọn) Dọn log thử nghiệm cũ trong `SystemAuditLog` (FAQ test 10002) — không liên quan feature này.

---

## 11. Progress Report

### PHASE 0–3 (Backend)
- [x] **P0** Context verified: `UserRepository.findByEmail` → `Optional<User>`; `ApiResponse.ok(String, T)`; `http.get(url, {params})` hỗ trợ axios params; `ddl-auto=none` → phải chạy SQL tay.
- [x] **P1** Bảng `LoginHistory` đã tạo trên DB **local** (localhost:1444) + FK `ON DELETE SET NULL`; cập nhật `dbTheXuong.sql` (DROP + CREATE + index). Entity + repository tạo xong.
- [x] **P2** `AdminLoginHistoryResponse` + `LoginHistoryService` (recordLogin không throw; ghi admin thành công + mọi thất bại; filter email/provider/success/from/to + phân trang + sort).
- [x] **P3** `LoginHistoryRestController` (`GET /api/v1/admin/login-history`, @PreAuthorize ADMIN/BOTH) + hook `AuthRestController.login` (fail → ghi → rethrow; success → ghi) + hook `OAuth2SuccessHandler` (GOOGLE success).

### PHASE 4–5 (Frontend)
- [x] **P4** `types/loginHistory.ts` + `services/loginHistoryAdmin.service.ts` (theo convention `ApiResponse<PageData<T>>`).
- [x] **P5** `LoginHistoryTab.vue` (filter email/nguồn/kết quả/ngày, bảng 7 cột, badge kết quả, nút KHOÁ/MỞ KHOÁ tài khoản có confirm, phân trang) + tab switcher "LỊCH SỬ HỆ THỐNG | ĐĂNG NHẬP" trong `AdminAuditLogs.vue`.

### PHASE 6 — Smoke test (instance test localhost:8081, DB local)
- [x] **P6-T1** Login `both@gmail.com` thành công → bản ghi success=true, provider=LOCAL, userId=6, IP/UA đúng.
- [x] **P6-T2** Login sai mật khẩu → 401 + bản ghi success=false, reason="Bad credentials".
- [x] **P6-T3** Register + login user thường thành công → **không** có bản ghi mới (đúng phạm vi).
- [x] **P6-T4** Không token gọi API → 401.
- [x] **P6-T5** Filter email/success/provider/from/to/page/size đều trả đúng.
- [x] **P6-T6** Khoá test user → login thất bại (401) + bản ghi reason="User is disabled" → mở khoá OK. Đã xoá user test + dọn toàn bộ log test khỏi DB local.
- [x] **P6-T7** `gradlew build -x test` PASS (32s); `npm run build` PASS; `npm run type-check` chỉ còn 2 lỗi pre-existing (AdminProducts/AdminUsers).
- [x] **P6-T8** Cập nhật Progress Report (đây).

### Ghi chú cho anh (chạy tay trên PROD)
1. **SQL prod** — chạy trên DB prod:
```sql
IF OBJECT_ID('LoginHistory', 'U') IS NULL
BEGIN
    CREATE TABLE LoginHistory (
        id             BIGINT IDENTITY(1,1) PRIMARY KEY,
        user_id        BIGINT NULL,
        email          NVARCHAR(255) NOT NULL,
        ip_address     NVARCHAR(45)  NULL,
        user_agent     NVARCHAR(500) NULL,
        provider       NVARCHAR(20)  NOT NULL DEFAULT 'LOCAL',
        success        BIT           NOT NULL DEFAULT 1,
        failure_reason NVARCHAR(255) NULL,
        created_at     DATETIME2     NOT NULL DEFAULT SYSDATETIME(),
        CONSTRAINT FK_LoginHistory_Users FOREIGN KEY (user_id)
            REFERENCES Users(id) ON DELETE SET NULL
    );
    CREATE INDEX IX_LoginHistory_createdAt ON LoginHistory(created_at DESC);
    CREATE INDEX IX_LoginHistory_email     ON LoginHistory(email);
    CREATE INDEX IX_LoginHistory_userId    ON LoginHistory(user_id);
END
```
2. Deploy backend + frontend mới lên prod rồi test lại trên browser (tab ĐĂNG NHẬP).
3. Login GOOGLE chưa test runtime (cần flow OAuth thật) — hook code đã compile, nên test sau khi deploy.