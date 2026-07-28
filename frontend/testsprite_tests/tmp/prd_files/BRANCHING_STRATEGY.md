# Branching Workflow (Quy trình phân nhánh Git)

Repo dùng mô hình **GitHub Flow mở rộng** với 2 nhánh cố định và các nhánh tạm thời.

## 1. Nhánh cố định (Long-lived)
| Nhánh | Mô tả |
|-------|-------|
| `main` | Code ổn định, đã phát hành (Production). Chỉ nhận merge từ `hotfix/*` và `release/*`. **Được bảo vệ (protected).** |
| `develop` | Tích hợp các tính năng đang phát triển. Mọi `feature/*`, `bugfix/*`, `release/*` gộp về đây. **Được bảo vệ (protected).** |

## 2. Nhánh tạm thời (Short-lived)
| Tiền tố | Tách ra từ | Gộp về | Mục đích |
|---------|-----------|--------|---------|
| `feature/...` | `develop` | `develop` | Phát triển tính năng mới / cải tiến chức năng. |
| `bugfix/...` | `develop` | `develop` | Sửa lỗi nhỏ phát hiện trong kiểm thử nội bộ (dev). |
| `hotfix/...` | `main` | `main` + `develop` | Vá lỗi khẩn cấp trên Production, không chờ release tiếp theo. |
| `release/...` | `develop` | `main` + `develop` | Đóng gói, QA/QC kiểm thử cuối, sửa lỗi phát sinh trước khi lên `main`. |

## Quy tắc làm việc
1. Không commit trực tiếp lên `main` / `develop` (đều có Branch Protection).
2. Mọi thay đổi phải qua Pull Request (tối thiểu 1 approval).
3. Tên nhánh: `feature/jwt-auth`, `bugfix/cart-total`, `hotfix/login-500`, `release/v1.2.0`.
4. Sau khi merge feature/bugfix xong, xóa nhánh tạm thời.
5. Khi merge `hotfix/*` / `release/*` vào `main`, nhớ merge ngược lại `develop` để không mất commit.

## Branch Protection (đã cấu hình trên GitHub)
- `main` & `develop`: yêu cầu Pull Request, tối thiểu 1 review duyệt, không cho force-push / xóa nhánh.
