# Báo cáo Fix CORS Frontend — TheXuong

> Ngày fix: 15/07/2026  
> Plan gốc: `C:\Users\dell\.gemini\antigravity-cli\brain\f7e47645-5613-4ac2-8f6f-0c09514c59cb\fix-cors-plan.md`

---

## Vấn đề gốc

Frontend hardcode `http://localhost:8080/api/v1` làm fallback URL trong 2 file service. Khi người dùng truy cập `https://thexuong.xuansown.id.vn`, trình duyệt họ gọi **thẳng `localhost:8080` (máy của họ)** thay vì đi qua Cloudflare Tunnel → Vite Proxy → Spring Boot (máy anh) → **CORS error**.

### Luồng lỗi (trước fix)

```
Browser người dùng
    │  GET http://localhost:8080/api/v1/products  ← hardcode
    ▼
localhost:8080 (máy người dùng, không có Spring Boot)
    │  → KHÔNG RESPONSE / CORS ERROR
    ✗
```

---

## Giải pháp

Bỏ hardcode `localhost:8080`, dùng **relative path `/api/v1`** → request same-origin → đi qua Vite proxy → Spring Boot.

### Luồng request sau fix

```
Người dùng truy cập https://thexuong.xuansown.id.vn
    │  GET /api/v1/products  (relative, same-origin)
    ▼
Cloudflare Tunnel
    │  Forward → localhost:5173
    ▼
Vite Dev Server (máy anh)
    │  Proxy match: /api/** → http://localhost:8080
    ▼
Spring Boot (máy anh)
    │  Query database
    ▼
Database (máy anh)
    │  Trả kết quả
    ▼
Ngược lại về trình duyệt người dùng ✅
```

---

## Files đã thay đổi (3 files)

### 1. `frontend/src/services/api.ts` (dòng 4)

**Trước:**
```ts
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1'
```

**Sau:**
```ts
const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1'
```

**File này dùng cho:** auth.store.ts (login, register, get user, change password), admin APIs, cart, order, profile.

---

### 2. `frontend/src/services/http.ts` (dòng 3)

**Trước:**
```ts
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080/api/v1'
```

**Sau:**
```ts
const API_BASE_URL = import.meta.env.VITE_API_URL || '/api/v1'
```

**File này dùng cho:** product.service.ts (danh sách sản phẩm, chi tiết SP).

---

### 3. `frontend/.env` (file mới)

```env
# Development - Vite proxy sẽ forward /api/** → localhost:8080
VITE_API_URL=/api/v1
VITE_APP_TITLE=TheXuong Sport
VITE_APP_VERSION=0.0.1
```

**Lý do tạo:** Trước chỉ có `.env.production`, không có `.env` mặc định. Tạo file này để đảm bảo `VITE_API_URL` luôn có giá trị khi `npm run dev`, không phụ thuộc fallback trong code.

---

## Files KHÔNG thay đổi (giữ nguyên, đã đúng)

| File | Lý do |
|---|---|
| `frontend/.env.production` | Đã đúng: `VITE_API_URL=/api/v1` ✅ |
| `frontend/vite.config.ts` | Proxy config đã đúng: `/api` → `localhost:8080` ✅ |
| `backend/.../SecurityConfig.java` | CORS đã cho phép `thexuong.xuansown.id.vn` ✅ |
| `backend/.../application.yml` | Không cần thay đổi ✅ |
| Cloudflare Tunnel | Giữ nguyên cấu hình ✅ |

---

## Kết quả Verify

### Tự động

| Check | Command | Kết quả |
|---|---|---|
| Type-check | `npm run type-check` | ✅ PASS (0 error) |
| Build | `npm run build` | ✅ PASS (291 modules, 7.00s) |
| Grep hardcode | `localhost:8080` trong `frontend/src` | ✅ 0 kết quả (sạch hoàn toàn) |

### Output Build (subset)

```
✓ 291 modules transformed.
dist/index.html                                        1.25 kB
dist/assets/index-TkoKU63n.js                        101.38 kB
dist/assets/http-BaX2F0Hz.js                           0.85 kB   ← http.ts đã build
dist/assets/product.service-DU4AdSm0.js                0.46 kB   ← product service
✓ built in 7.00s
```

---

## Checklist test thủ công (cần kiểm tra trên production)

Chạy `npm run dev` rồi truy cập `https://thexuong.xuansown.id.vn` để verify:

- [ ] Trang chủ load sản phẩm mới bình thường
- [ ] Đăng nhập / đăng ký hoạt động
- [ ] Giỏ hàng, đặt hàng hoạt động
- [ ] Admin panel hoạt động
- [ ] Console không còn lỗi CORS
- [ ] OAuth Google login hoạt động (nếu có dùng)

---

## Tóm tắt

- **3 thay đổi** (2 dòng edit + 1 file `.env` mới 4 dòng)
- **Root cause đã sửa**, không phải fix symptoms
- **Type-check + build PASS** — không break gì
- **Không còn hardcode `localhost:8080`** nào trong frontend/src
- **Không động backend/database/Cloudflare** — đúng phạm vi plan