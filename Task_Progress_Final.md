# Tiến Độ Viết Unit Test (Agile Sprint Backlog)

Danh sách này được tổ chức lại theo 4 Sprint của dự án. Mục tiêu là Unit Test đạt 100% Branch Coverage cho tất cả các class.

## SPRINT 1 (Nền tảng & Đăng ký/Đăng nhập)
- [x] **`UserService` (Đã test: 29 TCs)**: 
  * `getUserByEmail`, `getUserByEmailWithAddresses`, `getUserById`, `updateProfile` (2 hàm overload), `changePassword`, `toggleActive`, `setRole`, `deleteUser`, `createUser`
- [x] **`PasswordResetService` (Đã test: 9 TCs)**: 
  * `createPasswordResetToken`, `resetPassword`
- [x] **`CartService` (Đã test: 13 TCs)**: 
  * `getCartByUser`, `addToCart`, `removeCartItem`, `updateCartItemQuantity`, `clearCart`
- [x] **`ProductService`**: *(Logic nằm ở `ProductRestController` & `AdminProductRestController` -> Sẽ test ở pha Integration Test)*
- [x] **`CloudflareR2Service` (Đã test: 7 TCs)**: (Dịch vụ Upload ảnh/file - Bổ sung ngoài Backlog)
  * `uploadMultiple`, `deleteFile`
- [x] **`SizeService` (Đã test: 9 TCs)**: (Quản lý phân loại Size sản phẩm - Bổ sung ngoài Backlog)
  * `parseSizeQuantities`, `createVariants`, `updateVariants`
- [x] **`InventoryService` (Đã test: 9 TCs)**: (Quản lý kho hàng - Bổ sung ngoài Backlog)
  * `deductStock`, `restoreStock`
- [x] Cập nhật lại `Sprint1_TestReport_Final.csv` (Đã gộp chung 3 module cũ, chờ tích hợp 3 module bổ sung)

## SPRINT 2 (Trải nghiệm & Mua sắm)
- [x] **`OrderService` (Đã test: 21 TCs)**
- [x] **`OrderEventService` (Đã test: 2 TCs)**
- [x] **`AddressService` (Đã test: 13 TCs)**:
  * `listByUser`, `create`, `update`, `delete`, `setDefault`, `getOwnedOrThrow`
- [x] **`CategoryService`**: *(Logic nằm ở `AdminProductRestController` -> Sẽ test ở pha Integration Test)*
- [x] **`VoucherService` (Đã test: 26 TCs)**:
  * `generateUniqueCode`, `redeemVoucher`, `issueVoucherToUser`, `validateAndGetDiscount`, `markAsUsed`, `restoreVoucher`, `expireOldVouchers`, `createVoucher`, `updateVoucher`, `deleteVoucher`, `bulkAction`
- [x] **`VNPayService` (Đã test: 2 TCs)**: (Cổng thanh toán độc lập - Bổ sung ngoài Backlog)
  * `createOrder`
- [x] **`MapsService` (Đã test: 4 TCs)**: (Tích hợp bản đồ/Tọa độ - Bổ sung ngoài Backlog)
  * `reverseGeocode`
- [x] **`VoucherValidator` (Đã test: 17 TCs)**: (Class Validate mã giảm giá - Bổ sung ngoài Backlog)
  * `validateCreate`, `validateUpdate`, `generateUniqueCode`
- [x] Cập nhật lại `Sprint2_TestReport_Final.csv` (Đã gộp toàn bộ module kế hoạch + 3 module bổ sung)

## SPRINT 3 (Loyalty, Tương tác & Thống kê)
- [x] **`PointService` (Đã test: 24 TCs)**:
  * `earnPoints`, `spendPoints`, `reversePoints`, `refundSpentPoints`, `adjustPoints`, `expireOldPoints`, `getOrCreateUserPoints`, `getCurrentPoints`, `getHistory`
- [x] **`PointTierService` (Đã test: 19 TCs)**: (Hạng thành viên)
  * `getTierForUser`, `getLoyaltyProgress`, `upgradeTierIfEligible`, `setFirstOrderTier`, `updateTierManually`
- [x] **`TierReevaluateService` (Đã test: 7 TCs)**: (Quét đánh giá lại hạng định kỳ)
  * `reevaluateUser`, `reevaluateAllActiveVip`
- [x] **`EmailService` (Đã test: 20 TCs)**: (Gửi mail thông báo)
  * `sendEmail`, `sendNewPassword`, `sendPasswordResetLink`, `sendPasswordChangedConfirmation`, `sendVipWelcome`, `sendVipDowngraded`, `sendVipExpiryWarning`, `sendPointsEarned`, `sendVoucherRedeemed`, `sendVoucherExpiring`
- [x] **`ChatbotService` (Đã test: 15 TCs)**: (Đại diện cho ChatService)
  * `getAllProductsForChatbot`, `getAllFaqsForChatbot`, `getChatMemory`, `saveChatMemory`, `trackOrder`, `logInteraction`
- [x] **`ReportService`**: *(Logic nằm ở `AdminStatisticsRestController` -> Sẽ test ở pha Integration Test)*
- [x] Tích hợp `Sprint3_TestReport_Final.csv` (Tổng hợp)

## SPRINT 4 (Hệ thống & Tối ưu hóa)
- [x] **`AdminStatisticsService`**: *(Logic nằm ở `AdminStatisticsRestController` -> Sẽ test ở pha Integration Test)*
- [x] **`BrandService`**: *(Logic nằm ở `AdminProductRestController` -> Sẽ test ở pha Integration Test)*
- [x] **`AuditLogService`**: *(Đã được tích hợp và test chung trong `VoucherService`)*
- [x] **`NotificationService`**: *(Skipped: Backend chưa code)*
- [x] **`WishlistService`**: *(Skipped: Backend chưa code)*
- [x] **`TelegramService`**: *(Skipped: Backend chưa code)*
- [x] **`NewsletterService`**: *(Skipped: Backend chưa code)*
- [x] Tích hợp `Master_TestReport_Final.csv` (Tổng hợp toàn bộ 3 Sprints)
