# Frontend Rules — TheXuong

> Quy tắc bắt buộc cho mọi code frontend (Vue 3 + Tailwind) trong `frontend/`.
> Mọi PR/commit vi phạm rule dưới đây đều phải sửa lại trước khi merge.
> Cập nhật lần cuối: 23/06/2026.

---

## 1. Font — CHỈ ĐƯỢC DÙNG GEIST

### 1.1. Font được phép
- **Geist** (sans-serif) — font chính cho **toàn bộ** giao diện.
- **Geist Mono** (monospace) — dùng cho code snippet, ID đơn hàng, số tiền dạng tabular, technical label.
- **Geist Fallback** — font dự phòng (Geist cung cấp sẵn fallback metrics), dùng kèm trong stack.

### 1.2. Font BỊ CẤM (danh sách đen — tuyệt đối không dùng)
Các font phổ biến dưới đây và mọi biến thể của chúng đều **không được phép** xuất hiện trong code:

- ❌ **Inter** (kể cả Inter Tight, Inter Display…)
- ❌ **Roboto** (kể cả Roboto Mono, Roboto Slab…)
- ❌ **SF Pro / San Francisco** (kể cả SF Pro Display, SF Pro Text, SF Mono…)
- ❌ **Plus Jakarta Sans**
- ❌ Mọi font không nằm trong danh sách cho phép ở mục 1.1 (Gelasio, Russo One, Nimbus Sans, system-ui mặc định… đều bị cấm trừ khi user duyệt riêng).

### 1.3. Cách cấu hình

**`frontend/index.html`** — chỉ load Geist:
```html
<link rel="preconnect" href="https://fonts.googleapis.com">
<link rel="preconnect" href="https://fonts.gstatic.com" crossorigin>
<link href="https://fonts.googleapis.com/css2?family=Geist:wght@300;400;500;600;700&family=Geist+Mono:wght@400;500&display=swap" rel="stylesheet">
```

**`frontend/tailwind.config.js`** — `fontFamily` chỉ chứa Geist:
```js
fontFamily: {
  sans:   ['Geist', 'Geist Fallback', 'sans-serif'],
  mono:   ['Geist Mono', 'Geist Fallback', 'monospace'],
  geist:  ['Geist', 'Geist Fallback', 'sans-serif'],
  // ❌ XOÁ: gelasio, brand, sans mặc định Inter, …
}
```

**`frontend/src/assets/main.css`** — `body` dùng Geist, **xoá** mọi `@import` Inter/Russo One:
```css
body {
  font-family: 'Geist', 'Geist Fallback', sans-serif;
}
```

### 1.4. Quy tắc sử dụng trong component
- **Không** khai báo `font-family: 'Inter'`, `font-family: 'Gelasio'`, `font-family: 'Nimbus Sans'`… trong bất kỳ file `.vue` / `.css` / `.scss` nào.
- **Không** dùng `style="font-family: '...' "` inline trừ khi đó là Geist/Geist Mono/Geist Fallback.
- **Không** dùng class `font-sans` của Tailwind mặc định (mặc định trỏ Inter) — thay bằng `font-geist` (đã map ở trên) hoặc `font-mono` cho Geist Mono.
- **Không** `@import` Google Fonts trong `<style>` của component — chỉ import 1 lần duy nhất ở `index.html`.

### 1.5. Trọng số (weight) được phép
- Geist: **300, 400, 500, 600, 700** (mỏng → đậm).
- Geist Mono: **400, 500**.
- ❌ Không dùng weight 800/900 (Geist không thiết kế cho display quá đậm).

---

## 2. Màu — CHỈ ĐƯỢC DÙNG #000000 & #FFFFFF

### 2.1. Bảng màu bắt buộc
| Token        | Giá trị     | Vai trò                                            |
|--------------|-------------|----------------------------------------------------|
| `--color-ink`| `#000000`   | Chữ chính, nền tối, viền đậm, icon active.       |
| `--color-paper` | `#FFFFFF` | Nền chính, chữ trên nền tối, viền sáng.         |

### 2.2. Màu BỊ CẤM (trừ khi user duyệt riêng từng case)
- ❌ Mọi màu ngoài `#000000` và `#FFFFFF` cho text, background, border, icon, shadow, gradient.
- ❌ Các bảng màu có sẵn trong Tailwind: `gray-*`, `neutral-*`, `slate-*`, `red-*`, `blue-*`… (kể cả `transparent` chỉ được dùng cho mục đích kỹ thuật như overlay loading, không dùng làm màu nền/thay thế).
- ❌ Tailwind config hiện tại có `primary` (cam) và `secondary` (xanh) — **xoá hết** cho tới khi user duyệt lại palette.
- ❌ Các mã màu cũ đang rải rác trong code (`#9CA3AF`, `#D1D5DB`, `#6B7280`, `#FF6B35`, `#E0F2FE`…) đều **không thuộc** palette hợp lệ và phải được thay bằng `#000000` / `#FFFFFF` (kèm opacity nếu cần tạo sắc độ).

### 2.3. Cách tạo sắc độ phụ (vì chỉ có 2 màu)
Dùng **CSS Color Module 4** opacity syntax (Tailwind v3.3+ hỗ trợ) hoặc `rgba`:

```html
<!-- Tailwind opacity modifier (khuyến nghị) -->
<div class="bg-black/10">     <!-- đen 10% — viền nhẹ, divider -->
<div class="bg-white/80">     <!-- trắng 80% — overlay -->
<div class="text-black/60">   <!-- chữ phụ, placeholder -->
<div class="border-black/20"> <!-- viền mảnh -->

<!-- Hoặc rgba trong style -->
<div style="background: rgba(0,0,0,0.04);">
```

Bảng opacity khuyến nghị:
| Mục đích                    | Giá trị      |
|----------------------------|--------------|
| Divider, border nhẹ       | `black/10`   |
| Placeholder, text phụ     | `black/60`   |
| Text disabled              | `black/30`   |
| Hover overlay trên dark    | `white/10`   |
| Active overlay trên dark   | `white/20`   |

### 2.4. Quy tắc sử dụng trong component
- **Không** hardcode mã màu ngoài `#000000` / `#FFFFFF` trong `style="..."` hoặc file CSS.
- **Không** dùng class Tailwind có màu (`bg-gray-100`, `text-blue-500`, `border-red-300`…).
- **Không** dùng gradient nhiều màu (`bg-gradient-to-r from-red-500 to-blue-500`) — nếu cần gradient, chỉ dùng `from-black to-white` hoặc ngược lại.
- **Được phép** dùng `text-black/60`, `bg-black/5`… (Tailwind opacity modifier trên `black` / `white`).
- **Được phép** dùng `transparent` cho mục đích kỹ thuật (overlay loading, mask), nhưng phải có comment giải thích.

### 2.5. Tailwind config — gỡ bảng màu thừa
Trong `frontend/tailwind.config.js`, **xoá toàn bộ** `theme.extend.colors` (primary, secondary) — chỉ giữ 2 màu chính:

```js
theme: {
  extend: {
    colors: {
      ink:    '#000000',
      paper:  '#FFFFFF',
    },
  },
}
```

Hoặc đơn giản hơn: **không extend** gì cả, dùng trực tiếp `text-black`, `bg-white`, `border-black/20`.

---

## 3. Checklist trước khi commit/PR

- [ ] Không còn `@import` Google Fonts trong bất kỳ file `.vue` / `.css` nào (trừ `index.html`).
- [ ] Không còn chuỗi `Inter`, `Roboto`, `SF Pro`, `San Francisco`, `Plus Jakarta`, `Gelasio`, `Russo One`, `Nimbus Sans` trong toàn bộ `frontend/`.
- [ ] Không còn mã màu hex ngoài `#000000` và `#FFFFFF` trong toàn bộ `frontend/`.
- [ ] Không còn Tailwind class có màu (`text-gray-*`, `bg-blue-*`, `border-red-*`…).
- [ ] `tailwind.config.js` chỉ khai báo `ink: #000000` và `paper: #FFFFFF` (hoặc bỏ luôn phần extend).
- [ ] Mọi sắc độ phụ dùng `black/{n}` hoặc `white/{n}` opacity modifier.
- [ ] Font-weight chỉ nằm trong tập {300, 400, 500, 600, 700} cho Geist, {400, 500} cho Geist Mono.

---

## 4. Lệnh kiểm tra nhanh (grep)

```bash
# Tìm font bị cấm
cd frontend
grep -rn --include="*.vue" --include="*.css" --include="*.scss" --include="*.ts" --include="*.js" -E "Inter|Roboto|SF Pro|San Francisco|Plus Jakarta|Gelasio|Russo One|Nimbus Sans" .

# Tìm mã màu hex ngoài #000000 / #FFFFFF
grep -rn --include="*.vue" --include="*.css" --include="*.scss" -E "#[0-9A-Fa-f]{3,6}" . | grep -vE "#000000|#FFFFFF|#000|#FFF"

# Tìm @import Google Fonts sai chỗ
grep -rn --include="*.vue" --include="*.css" --include="*.scss" "fonts.googleapis.com" . | grep -v "index.html"
```

Nếu bất kỳ lệnh nào trả về kết quả → chưa đạt rule, phải fix.

---

## 5. Lý do & phạm vi

- **Lý do font**: Geist là brand font chính thức của dự án TheXuong, đã chốt với user ngày 23/06/2026. Mọi font khác đều làm giao diện thiếu đồng bộ và tốn thêm bandwidth Google Fonts.
- **Lý do màu**: Brand guideline hiện tại của TheXuong là **monochrome tuyệt đối** (đen/trắng). Mọi màu thêm sẽ phá tone chung. Khi nào cần mở rộng palette, sẽ cập nhật file này.
- **Phạm vi áp dụng**: tất cả file trong `frontend/` (Vue components, CSS, Tailwind config, HTML shell).
- **Không áp dụng**: backend Thymeleaf templates ở `src/main/resources/templates/` (dự án đang migrate dần sang Vue 3).

---

## 6. Lịch sử thay đổi

| Ngày       | Thay đổi                                                  |
|------------|-----------------------------------------------------------|
| 23/06/2026 | Khởi tạo rule: cấm Inter/Roboto/SF Pro/Plus Jakarta, ép Geist + #000/#FFF. |
