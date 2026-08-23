# Design: Khóa tài khoản — chặn đăng nhập, kick phiên đang hoạt động & thông báo

Date: 2026-08-23
Status: Approved

## Bối cảnh / Vấn đề

Trạng thái khóa tài khoản = `Users.active = false` (admin BOTH/ADMIN toggle qua `AdminUserRestController.toggleActive`).

Hành vi hiện tại:

- **Chưa đăng nhập**: user bị khóa không login được (đúng, không sửa cơ chế) — nhưng thông báo trả về là 401 `"Email hoặc mật khẩu không đúng"`, người dùng không biết mình bị khóa. Cần thông báo rõ ràng.
- **Đã đăng nhập (bug cần fix)**: `JwtAuthenticationFilter.setAuthentication()` load `UserDetails` từ DB nhưng **không kiểm tra `isEnabled()`** → người dùng đã đăng nhập vẫn thao tác bình thường dù bị khóa. Cần kick ra ngay và thông báo.
- **Google OAuth2 (lỗ hổng)**: `OAuth2SuccessHandler` và `oauth2UserService()` không kiểm tra `active` → user bị khóa vẫn vào được bằng "Đăng nhập bằng Google". Cần chặn.

## Quyết định đã chốt với user

| Vấn đề | Quyết định |
|---|---|
| Phạm vi | Backend + Frontend (Vue) |
| Mã HTTP khi phát hiện bị khóa | **423 Locked** (phân biệt với 401 token hết hạn) |
| Xử lý token khi kick | Xóa cookie + **blacklist cả 2 token** + clear SecurityContext (ép đăng nhập lại sau khi mở khóa) |
| Google login khi bị khóa | Chặn + thông báo, không cấp token |
| Hiển thị thông báo frontend | Toast + alert trên trang login qua `?locked=1` |

## Thay đổi

### 1. Backend — Login thường khi bị khóa

`src/main/java/com/example/thexuong/exception/GlobalExceptionHandler.java`:

- Thêm `@ExceptionHandler(DisabledException.class)` → trả **423** + `ApiResponse.error("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.")`.
- Spring tự chọn handler cụ thể hơn thay vì handler `AuthenticationException` chung hiện có.
- Login history vẫn ghi failure như cũ (`AuthRestController.login` đã có sẵn).

### 2. Backend — Kick user đang đăng nhập (fix chính)

`src/main/java/com/example/thexuong/security/JwtAuthenticationFilter.java`:

- Trong `setAuthentication()`: sau khi load `UserDetails`, nếu `!user.isEnabled()`:
  1. Blacklist access token + refresh token (đọc từ cookie/header) qua `TokenBlacklist` sẵn có.
  2. Xóa cookie `access_token`/`refresh_token` qua `JwtCookieService.clearAuthCookies()`.
  3. `SecurityContextHolder.clearContext()`.
  4. Trả 423 JSON `{"error": "Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên."}` và **không đi tiếp** chuỗi filter.
- Chặn nhánh auto-refresh: khi access token hết hạn và cần cấp token mới từ refresh token, nếu user bị khóa → xử lý kick như trên (không cấp token mới).

`src/main/java/com/example/thexuong/controller/AuthRestController.java`:

- `refresh()`: sau khi load `UserDetails`, nếu `!isEnabled()` → 423 + message khóa (defense-in-depth; filter đã chặn trước).

### 3. Backend — Google OAuth2 khi bị khóa

`src/main/java/com/example/thexuong/security/OAuth2SuccessHandler.java`:

- Sau khi load `User` từ DB, nếu `!Boolean.TRUE.equals(user.getActive())`:
  - Ghi `loginHistoryService.recordLogin(email, ip, ua, "GOOGLE", false, "Tài khoản bị khóa")`.
  - Không cấp token; redirect về `frontendUrl + "/login?locked=1"`.

### 4. Frontend

- `frontend/src/services/http.ts` interceptor:
  - Bắt `error.response?.status === 423`:
    - Nếu **không** ở `/login`: toast error (dùng message backend, fallback i18n `auth.accountLocked`) → redirect `window.location.href = '/login?locked=1'`.
    - Nếu **đang** ở `/login`: không làm gì (Login.vue tự toast + hiện alert qua `?locked=1`) — tránh toast trùng.
- `frontend/src/views/Login.vue`:
  - Thêm alert lỗi (style tương tự `showSuccessAlert`/`showRegisterSuccess`) hiển thị khi `route.query.locked === '1'`, nội dung = `t('auth.accountLocked')`, tự ẩn sau ~8 giây.
- i18n `frontend/src/i18n/locales/vi.json` + `en.json`: thêm key `auth.accountLocked`:
  - vi: `"Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên."`
  - en: `"Your account has been locked. Please contact the administrator."`
- Login thất bại do bị khóa: toast đã tự hiển thị message backend qua `getApiErrorMessage` (Login.vue) — không sửa thêm.

## Error handling

- Mọi trường hợp bị khóa → HTTP **423** + message tiếng Việt `"Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên."` (body JSON: filter dùng `{"error": ...}`, handler dùng `ApiResponse.error(...)`).
- 401 giữ nguyên nghĩa cũ (chưa đăng nhập/token hết hạn/sai mật khẩu) — không đụng.

## Kiểm thử

1. **Backend test** (JUnit + MockMvc nếu có hạ tầng): user bị khóa gọi API có token hợp lệ → 423, cookie bị xóa, token bị blacklist, SecurityContext rỗng; auto-refresh không cấp token mới.
2. **Frontend test** (Vitest): interceptor bắt 423 → redirect `/login?locked=1`; Login.vue hiện alert khi query có `locked=1`.
3. **Test tay**:
   - Admin khóa tài khoản CUSTOMER đang đăng nhập → user bị đá ra, nhận thông báo, mọi thao tác bị chặn 423.
   - User bị khóa đăng nhập lại bằng mật khẩu → 423 + toast "tài khoản bị khóa".
   - User bị khóa đăng nhập bằng Google → redirect về login + alert khóa.
   - Admin mở khóa → user đăng nhập lại bình thường (token cũ đã blacklist, không tự sống lại).
4. Chạy `./gradlew test` và frontend test script hiện có.
