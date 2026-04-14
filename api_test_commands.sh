# ============================================================
# PHASE 3 — API TEST COMMANDS
# Base URL: http://localhost:8080
# Auth: Session-based (dùng -c/-b để giữ cookie giữa các lệnh)
# ============================================================

# ── BƯỚC 0: Đăng nhập và lưu session cookie ─────────────────
# Thay email/password bằng tài khoản ADMIN của bạn

curl -s -c cookies.txt -X POST http://localhost:8080/perform_login \
  -d "email=admin@gmail.com&password=Admin123@" \
  -L -o /dev/null -w "Login status: %{http_code}\n"

# ============================================================
# USERS API — /api/admin/users
# ============================================================

# ── GET: Danh sách tất cả Users ──────────────────────────────
curl -s -b cookies.txt -X GET http://localhost:8080/api/admin/users \
  -H "Accept: application/json" | python -m json.tool

# ── PATCH: Toggle Active user ID=2 (Thành công) ──────────────
curl -s -b cookies.txt -X PATCH http://localhost:8080/api/admin/users/2/toggle-active \
  -H "Accept: application/json" | python -m json.tool

# Kết quả mong đợi (thành công):
# {
#   "success": true,
#   "message": "Đã khóa tài khoản thành công.",
#   "data": { "id": 2, "email": "user@gmail.com", "fullName": "User", "active": false }
# }

# ── PATCH: Toggle Active chính mình (Phải báo lỗi 400) ───────
# Giả sử Admin đang login có ID=1
curl -s -b cookies.txt -X PATCH http://localhost:8080/api/admin/users/1/toggle-active \
  -H "Accept: application/json" | python -m json.tool

# Kết quả mong đợi (lỗi 400):
# {
#   "success": false,
#   "message": "Không thể tự khóa tài khoản của chính mình.",
#   "data": null
# }

# ── PATCH: Toggle Active user không tồn tại (Phải báo 404) ───
curl -s -b cookies.txt -X PATCH http://localhost:8080/api/admin/users/9999/toggle-active \
  -H "Accept: application/json" | python -m json.tool

# ── PATCH: Gọi API không có session (Phải báo 302/403) ───────
curl -s -X PATCH http://localhost:8080/api/admin/users/2/toggle-active \
  -H "Accept: application/json" | python -m json.tool

# ============================================================
# ROLE GROUPS API — /api/admin/role-groups
# ============================================================

# ── GET: Danh sách tất cả RoleGroups ─────────────────────────
curl -s -b cookies.txt -X GET http://localhost:8080/api/admin/role-groups \
  -H "Accept: application/json" | python -m json.tool

# ── GET: Chi tiết RoleGroup ID=1 ─────────────────────────────
curl -s -b cookies.txt -X GET http://localhost:8080/api/admin/role-groups/1 \
  -H "Accept: application/json" | python -m json.tool

# ── POST: Tạo RoleGroup mới ───────────────────────────────────
curl -s -b cookies.txt -X POST http://localhost:8080/api/admin/role-groups \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "name": "Quản lý kho",
    "description": "Nhân viên phụ trách tồn kho và nhập xuất",
    "roleIds": [1]
  }' | python -m json.tool

# Kết quả mong đợi (201 Created):
# {
#   "success": true,
#   "message": "Tạo chức danh thành công.",
#   "data": { "id": 3, "name": "Quản lý kho", "description": "...", "roles": [...] }
# }

# ── POST: Tạo RoleGroup không có tên (Phải báo 400) ──────────
curl -s -b cookies.txt -X POST http://localhost:8080/api/admin/role-groups \
  -H "Content-Type: application/json" \
  -d '{ "name": "", "description": "Test" }' | python -m json.tool

# ── PUT: Cập nhật RoleGroup ID=3 ─────────────────────────────
curl -s -b cookies.txt -X PUT http://localhost:8080/api/admin/role-groups/3 \
  -H "Content-Type: application/json" \
  -H "Accept: application/json" \
  -d '{
    "name": "Quản lý kho (Updated)",
    "description": "Mô tả đã cập nhật",
    "roleIds": [1, 2]
  }' | python -m json.tool

# ── PUT: Cập nhật chỉ description (roleIds=null → giữ roles cũ) ──
curl -s -b cookies.txt -X PUT http://localhost:8080/api/admin/role-groups/3 \
  -H "Content-Type: application/json" \
  -d '{ "description": "Chỉ đổi mô tả, giữ nguyên roles" }' | python -m json.tool

# ── POST: Thêm Role ID=2 vào RoleGroup ID=3 ──────────────────
curl -s -b cookies.txt -X POST http://localhost:8080/api/admin/role-groups/3/roles/2 \
  -H "Accept: application/json" | python -m json.tool

# ── DELETE: Gỡ Role ID=2 khỏi RoleGroup ID=3 ─────────────────
curl -s -b cookies.txt -X DELETE http://localhost:8080/api/admin/role-groups/3/roles/2 \
  -H "Accept: application/json" | python -m json.tool

# ── DELETE: Xóa RoleGroup ID=3 (không còn User) ──────────────
curl -s -b cookies.txt -X DELETE http://localhost:8080/api/admin/role-groups/3 \
  -H "Accept: application/json" | python -m json.tool

# ── DELETE: Xóa RoleGroup đang có User (Phải báo 409) ────────
curl -s -b cookies.txt -X DELETE http://localhost:8080/api/admin/role-groups/1 \
  -H "Accept: application/json" | python -m json.tool

# Kết quả mong đợi (409 Conflict):
# {
#   "success": false,
#   "message": "Không thể xóa chức danh 'Khách hàng' vì vẫn còn người dùng...",
#   "data": null
# }

# ============================================================
# POSTMAN — Import Collection JSON
# Dán JSON sau vào Postman: File → Import → Raw text
# ============================================================
: '
{
  "info": { "name": "TheXuong Admin API", "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json" },
  "item": [
    {
      "name": "Login",
      "request": { "method": "POST", "url": "{{base_url}}/perform_login",
        "body": { "mode": "urlencoded", "urlencoded": [
          {"key": "email", "value": "admin@gmail.com"},
          {"key": "password", "value": "Admin123@"}
        ]}
      }
    },
    {
      "name": "GET All Users",
      "request": { "method": "GET", "url": "{{base_url}}/api/admin/users" }
    },
    {
      "name": "PATCH Toggle Active User",
      "request": { "method": "PATCH", "url": "{{base_url}}/api/admin/users/2/toggle-active" }
    },
    {
      "name": "GET All RoleGroups",
      "request": { "method": "GET", "url": "{{base_url}}/api/admin/role-groups" }
    },
    {
      "name": "POST Create RoleGroup",
      "request": { "method": "POST", "url": "{{base_url}}/api/admin/role-groups",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": { "mode": "raw", "raw": "{\"name\": \"Quản lý kho\", \"description\": \"Test\", \"roleIds\": [1]}" }
      }
    },
    {
      "name": "PUT Update RoleGroup",
      "request": { "method": "PUT", "url": "{{base_url}}/api/admin/role-groups/3",
        "header": [{"key": "Content-Type", "value": "application/json"}],
        "body": { "mode": "raw", "raw": "{\"name\": \"Updated\", \"roleIds\": [1, 2]}" }
      }
    },
    {
      "name": "DELETE RoleGroup",
      "request": { "method": "DELETE", "url": "{{base_url}}/api/admin/role-groups/3" }
    }
  ],
  "variable": [{"key": "base_url", "value": "http://localhost:8080"}]
}
'
