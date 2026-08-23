# Design: Phân quyền quản lý User cho role ADMIN vs BOTH

Date: 2026-08-23
Status: Approved

## Bối cảnh / Vấn đề

Hiện tại `AdminUserRestController` chỉ chặn bằng `@PreAuthorize("hasAnyAuthority('ADMIN', 'BOTH')")` — nghĩa là ADMIN (role thuần) có quyền ngang BOTH trong việc quản lý user:

- ADMIN tự sửa chính mình được.
- ADMIN sửa/xóa/toggle được cả user role BOTH (role cao hơn).

Yêu cầu:

- Chỉ **BOTH** mới có quyền thêm/xóa/sửa tài khoản **ADMIN**.
- ADMIN thuần chỉ được quản lý tài khoản **CUSTOMER** (không được đụng ADMIN — kể cả chính mình — và BOTH).

## Quy tắc phân quyền

| Hành động | ADMIN (thuần) | BOTH |
|---|---|---|
| Xem danh sách user | Cho phép | Cho phép |
| Tạo user | Chỉ role CUSTOMER | Mọi role |
| Sửa user (PATCH) | Chỉ user CUSTOMER; không đổi role target thành ADMIN/BOTH; không sửa chính mình | Mọi user |
| Xóa user | Chỉ user CUSTOMER (tự xóa đã bị chặn sẵn) | Mọi user (trừ chính mình — đã chặn sẵn) |
| Toggle active | Chỉ user CUSTOMER (tự khóa đã bị chặn sẵn) | Mọi user (trừ chính mình — đã chặn sẵn) |

## Thay đổi

### 1. Backend — `src/main/java/com/example/thexuong/controller/AdminUserRestController.java`

Nguồn quyền chuẩn (enforce ở controller, không chỉ dựa vào UI):

- Thêm helper `getCurrentUserRole()` (đọc role của user đang đăng nhập qua `UserRepository`).
- Helper kiểm tra `isBOTH(role)` hoặc rule `isAdminOnly(...)`.
- `createUser`: nếu current role = ADMIN và role trong body ≠ CUSTOMER → 403 với message tiếng Việt rõ ràng.
- `updateUser`: nếu current role = ADMIN:
  - Target user có role ≠ CUSTOMER → 403.
  - `id == currentUserId` → 403 (không tự sửa).
  - Body có `role` ≠ CUSTOMER → 403.
- `deleteUser`: nếu current role = ADMIN và target role ≠ CUSTOMER → 403.
- `toggleActive`: nếu current role = ADMIN và target role ≠ CUSTOMER → 403.
- BOTH: giữ nguyên hành vi hiện tại.

Response lỗi dùng `ApiResponse.error(...)` + HTTP 403 để frontend hiển thị toast.

### 2. Frontend — `frontend/src/views/admin/AdminUsers.vue`

- Import `useAuthStore`, lấy `roles` hiện tại.
- Computed `isBOTH = roles.includes('BOTH')`.
- Khi **không phải BOTH** (tức ADMIN thuần):
  - Form "Thêm người dùng mới": ẩn 2 nút role ADMIN và BOTH (chỉ hiển thị CUSTOMER).
  - Role badge trong bảng: bỏ handler cycle role (không click được).
  - Hàng user có `role !== 'CUSTOMER'`: ẩn nút Sửa/Xóa, vô hiệu toggle trạng thái.
- BOTH: giữ nguyên toàn bộ UI hiện tại.

## Error handling

- Backend trả 403 + `ApiResponse.error(message tiếng Việt)`.
- Frontend hiển thị lỗi qua toast như cơ chế hiện có (`error.response?.data?.message`).

## Testing

- Backend: compile + `mvn test` (không có test controller hiện tại cho AdminUser; chạy bộ test sẵn có để chắc không vỡ).
- Frontend: `npm run test` (vitest) + build; test tay:
  - Đăng nhập ADMIN thuần → thấy UI bị ẩn theo quyền; gọi API trực tiếp vẫn bị chặn 403.
  - Đăng nhập BOTH → mọi thứ hoạt động như cũ.

## Ngoài phạm vi

- Không đổi cơ chế role (vẫn là 1 field String `CUSTOMER/ADMIN/BOTH`).
- Không thêm role mới, không đổi quyền ở các controller admin khác.
