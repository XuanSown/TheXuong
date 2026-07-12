# Pull Request Template

## Mô tả (Description)
<!-- Mô tả ngắn gọn tính năng / sửa lỗi trong PR này -->

## Loại thay đổi (Type of change)
- [ ] `feature/...` — Tính năng mới
- [ ] `bugfix/...` — Sửa lỗi nội bộ (dev)
- [ ] `hotfix/...` — Vá lỗi khẩn cấp (từ `main`)
- [ ] `release/...` — Chuẩn bị phát hành

## Nhánh (Branches)
- Tách từ (Base): `develop` (hoặc `main` với `hotfix`)
- Gộp về (Target): `develop` (hoặc `main` + `develop` với `hotfix`/`release`)

## Checklist trước khi merge
- [ ] Code biên dịch/thành công (`gradlew build` + `npm run build`)
- [ ] Không để lại secret / thông tin nhạy cảm
- [ ] Đã tự test luồng liên quan (login, giỏ hàng, thanh toán, admin)
- [ ] Cập nhật tài liệu nếu thay đổi API/quy trình

## Cách test (How to test)
<!-- Các bước reviewer có thể chạy để xác minh -->
