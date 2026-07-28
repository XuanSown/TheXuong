# Frontend Rules — TheXuong

> Quy tắc bắt buộc cho mọi code frontend (Vue 3 + Tailwind) trong `frontend/`.
> Mọi PR/commit vi phạm rule dưới đây đều phải sửa lại trước khi merge.

---

## 1. Font — CHỈ ĐƯỢC DÙNG GEIST

* **Font được phép**: `Geist` (sans-serif) làm font chính, `Geist Mono` (monospace) cho code/ID/số tiền, và `Geist Fallback`.
* **Font BỊ CẤM**: Tuyệt đối không dùng `Inter`, `Roboto`, `SF Pro / San Francisco`, `Plus Jakarta Sans`, v.v.
* **Cách cấu hình**:
  * Load Geist qua Google Fonts duy nhất tại `frontend/index.html`.
  * Khai báo `sans` và `mono` trong `frontend/tailwind.config.js` trỏ về Geist.
  * Chỉ định `font-family: 'Geist'` trong `body` tại `frontend/src/assets/main.css`.
* **Sử dụng**: Không inline font-family lạ, không `@import` font khác trong file `.vue`.
* **Trọng số (weight)**: Chỉ dùng `{300, 400, 500, 600, 700}` cho Geist và `{400, 500}` cho Geist Mono.

---

## 2. Màu — CHỈ ĐƯỢC DÙNG #000000 & #FFFFFF (Monochrome)

* **Bảng màu bắt buộc**:
  * `--color-ink`: `#000000` (Text chính, nền tối, viền đậm)
  * `--color-paper`: `#FFFFFF` (Nền chính, text sáng)
* **Quy tắc nghiêm ngặt**:
  * Cấm dùng màu khác hoặc các bảng màu có sẵn của Tailwind (`gray-*`, `blue-*`, `red-*`, v.v.).
  * Gỡ bỏ hoàn toàn `primary` và `secondary` trong `tailwind.config.js`.
* **Tạo sắc độ phụ**: Sử dụng opacity trên nền đen/trắng (ví dụ: `text-black/60` cho text phụ, `border-black/10` cho border nhẹ).

---

## 3. Quy tắc API URL (Bắt buộc từ dự án)

* **Cấm hardcode**: Tuyệt đối không viết cứng địa chỉ API của Spring Boot (như `http://localhost:8080`) vào code frontend.
* **Relative Path**: Sử dụng endpoint dạng relative path `/api/v1` (Vite proxy sẽ tự động forward tới backend port 8080 trong quá trình phát triển).
* **Cấu hình Env**: Mọi file môi trường (`.env`, `.env.production`) bắt buộc đặt `VITE_API_URL=/api/v1`.

---

## 4. Quy trình kiểm tra chất lượng code trước khi Commit/PR

Trước khi tạo commit hoặc Pull Request, lập trình viên bắt buộc phải chạy các lệnh sau trong thư mục `frontend/` và đảm bảo không có lỗi:

```bash
# 1. Cài đặt các package phụ thuộc (nếu có mới)
npm install --legacy-peer-deps

# 2. Định dạng và kiểm tra cú pháp ESLint
npm run lint

# 3. Kiểm tra kiểu dữ liệu TypeScript (Vue)
npm run type-check

# 4. Chạy toàn bộ các unit test
npm test
```
