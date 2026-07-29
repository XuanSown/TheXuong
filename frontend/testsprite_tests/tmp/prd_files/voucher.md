# Tính năng Tích điểm & Đổi Voucher — TheXuong

> **Trạng thái:** Checklist đã được anh xác nhận **lần 1 (21/06/2026)** + **lần 2 (22/06/2026)** + **lần 3 (22/06/2026)** — bao gồm 7 Open Questions đã chốt + tier rules + quy tắc báo cáo.
> File này là **source of truth** cho plan triển khai. Mọi thay đổi rule phải update tại đây trước khi sửa code.

---

## 🚨 QUY TẮC BÁO CÁO BẮT BUỘC (áp dụng MỌI task + batch)

> Cập nhật: **22/06/2026** — theo yêu cầu của anh. Áp dụng cho mọi file `orderstatus.md`, `voucher.md` và mọi task được triển khai từ plan này.

### A. Quy tắc báo cáo cuối MỖI TASK

Sau khi hoàn thành 1 task bất kỳ (kể cả task nhỏ), subagent / agent **BẮT BUỘC** phải output report theo template sau:

```
## Task Report: <task_id> — <task_name>

**Trạng thái:** ✅ SUCCESS | ⚠️ PARTIAL | ❌ FAIL
**Tiến độ:** XX% (đánh giá theo tiêu chí đúng yêu cầu chưa)

### 1. Đã làm được
- <bullet 1>
- <bullet 2>

### 2. Check yêu cầu (Definition of Done)
- [ ] `<tiêu chí 1 từ task>` → ✅ / ❌ / ⚠️ (lý do)
- [ ] `<tiêu chí 2 từ task>` → ✅ / ❌ / ⚠️ (lý do)
- [ ] `./gradlew build` pass → ✅ / ❌ (output cuối)

### 3. File đã thay đổi
- `path/to/file1.java` — <mô tả ngắn thay đổi>
- `path/to/file2.sql` — <mô tả ngắn>

### 4. Nếu FAIL hoặc PARTIAL
- **Lý do fail:** <mô tả cụ thể>
- **Đã thử:** <các bước retry đã làm, tối đa 2 lần>
- **Cần support:** <cần anh quyết định gì / thông tin gì để retry>

### 5. Commit
- ✅ Đã commit: `<commit hash ngắn>` — `<commit message tiếng Việt>`
  hoặc
- ⏸ Chưa commit (lý do: <build fail / chờ review>)
```

**Quy tắc retry:**
- Lần 1 fail → tự retry, fix root cause, chạy lại verify.
- Lần 2 fail → báo anh, **KHÔNG retry lần 3** mà không hỏi.
- Sau 2 lần fail → cập nhật report với tiến độ % chính xác và lý do, **DỪNG** chờ anh.

**Tiêu chí đánh giá %:**
- 100%: tất cả Definition of Done ✅, build pass, commit xong.
- 80-99%: hầu hết ✅, 1-2 tiêu chí phụ chưa đạt nhưng không blocker.
- 50-79%: lõi chính OK nhưng còn warning / edge case chưa cover.
- <50%: fail nặng, cần redesign.

---

### B. Quy tắc báo cáo cuối MỖI BATCH

Sau khi tất cả task trong batch xong, agent tổng hợp report batch:

```
## 📦 BATCH REPORT: <Batch N> — <Tên batch>

**Trạng thái batch:** ✅ SUCCESS | ⚠️ PARTIAL | ❌ FAIL
**Số task:** X / Y hoàn thành (Z%)
**Ngày:** YYYY-MM-DD

### 1. Tổng kết
- ✅ Đã làm: <X task>
- ⚠️ PARTIAL: <Y task> — <lý do ngắn>
- ❌ FAIL: <Z task> — <lý do ngắn>

### 2. Kết quả test tổng
- `./gradlew build` → ✅ / ❌
- `./gradlew test --tests "*ServiceTest"` → ✅ / ❌ (X passed, Y failed)
- Manual smoke test → ✅ / ❌ (mô tả)

### 3. Definition of Done của batch
- [ ] `<DoD 1>` → ✅ / ❌
- [ ] `<DoD 2>` → ✅ / ❌
- [ ] `<DoD 3>` → ✅ / ❌

### 4. File đã thay đổi (tổng hợp)
- Tổng: <N files> thay đổi (<X created>, <Y modified>)
- Liệt kê quan trọng:
  - `path/to/file1.java` (created/modified) — <1 dòng mô tả>
  - `path/to/file2.sql` (modified) — <1 dòng>

### 5. Commits trong batch
1. `<hash>` — `<message>`
2. `<hash>` — `<message>`
...

### 6. Rủi ro / Vấn đề còn lý
- <rủi ro 1>: <cách giải quyết đề xuất>
- <rủi ro 2>: ...

### 7. Sẵn sàng cho batch tiếp theo?
- ✅ YES — đủ điều kiện chạy Batch N+1
- ⚠️ Cần anh review X trước khi đi tiếp
- ❌ Cần fix blocker trước khi đi tiếp: <mô tả>
```

### C. Quy tắc Handoff giữa agent ↔ user

| Tình huống | Action |
|---|---|
| Task 100% pass | Report + commit + tiếp tục task kế |
| Task PARTIAL (≥80%) | Report + commit + note warning + tiếp tục |
| Task FAIL | Report fail, **KHÔNG** tự đi task khác — DỪNG chờ anh |
| Batch 100% pass | Batch report + chờ anh duyệt → mới đi batch kế |
| Batch có 1 task FAIL | Batch report PARTIAL + **DỪNG** chờ anh quyết định (skip task fail? fix lại? rollback batch?) |

---

## 1. Quy tắc tích điểm (Earn rules)

- [x] **Tỷ lệ quy đổi:** `100.000 VND = 1 điểm` (tương đương `1.000.000 VND = 10 điểm`)
- [x] **Làm tròn xuống** (floor) — dùng `Math.floor(total / 100_000)`
- [x] **Phạm vi cộng điểm:** tất cả đơn, dựa trên `total` của đơn vừa mua.
  - Dùng `totalMoney` gốc (chưa trừ voucher).
- [x] **Không có giới hạn max điểm / đơn**
- [x] **Thời điểm cộng điểm:** khi khách bấm **"Xác nhận đã nhận hàng"** thành công (status `DELIVERED` → `COMPLETED`).
- [x] **Đơn huỷ / hoàn tiền:**
  - Huỷ trước khi `COMPLETED` → không cộng điểm (chưa cộng).
  - Hoàn tiền sau khi đã cộng điểm → **trừ toàn bộ điểm của đơn đó** + ghi `PointTransaction` loại `REVERSE`.
- [x] **Điểm khởi đầu:** `0`
- [x] **Thời hạn điểm:** hết hạn sau **12 tháng** không có giao dịch `EARN` nào cập nhật dòng điểm đó (FIFO theo `PointTransaction`).

## 2. Quy đổi voucher (Redeem rules)

- [x] **Mệnh giá voucher cố định:** `10k, 20k, 50k, 100k, 200k, 500k` — admin/BOTH có thể CRUD thêm/sửa mệnh giá.
- [x] **Số điểm cần:**
  - `10k = 1 điểm`
  - `20k = 2 điểm`
  - `50k = 5 điểm`
  - `100k = 10 điểm`
  - `200k = 20 điểm`
  - `500k = 50 điểm`
  - (tỷ lệ chuẩn: 1 điểm = 10k giảm — admin/BOTH có thể override).
- [x] **Điều kiện voucher:** có thể set `minOrderAmount`, `applicableCategoryIds`, `applicableProductIds`.
- [x] **Mã voucher:** format `TX-` + 6 ký tự `[A-Z0-9]` random (uppercase alphanumeric, loại trừ `0/O/1/I/L` để tránh nhầm).
- [x] **Hạn dùng:** **30 ngày** kể từ ngày đổi (lưu `expiresAt`).
- [x] **Mỗi đơn được dùng tối đa 1 voucher. KHÔNG cộng dồn với mã giảm giá khác** (vd: không áp song song với mã sale event khác — chọn 1).

## 3. Phân hạng thành viên

- [x] **Hạng:** `THUONG` (Khách hàng thường) và `VIP` (Khách hàng VIP).
- [x] **Khởi đầu mặc định:** Khi user **đặt đơn hàng đầu tiên** (bất kể status nào) → tự động set `tier_code = 'THUONG'`.
  - User mới đăng ký nhưng chưa mua gì → `tier_code = NULL` (chưa phải khách hàng).
  - Logic đặt trong `OrderService.placeOrder()` — set tier lần đầu + tạo `UserPoints` row.
- [x] **Tiêu chí lên hạng VIP (đề xuất của em):**
  - **Phương án A — theo tổng chi tiêu:** User có **tổng chi tiêu** (cộng dồn `Orders.total_for_point_calc` của các đơn `COMPLETED`) **≥ 5.000.000 VND** → lên VIP.
  - **Phương án B — theo điểm tích luỹ:** User có `UserPoints.total_earned ≥ 50 điểm` (tương đương 5M chi tiêu nếu tỷ lệ 100k/điểm) → lên VIP.
  - **Phương án C — kết hợp:** Đạt 1 trong 2 điều kiện trên → lên VIP.
  - **📋 ĐỀ XUẤT CỦA EM:** Dùng **Phương án C** (OR logic) — vì:
    - Tổng chi tiêu phản ánh đúng "khách hàng giá trị cao"
    - Điểm tích luỹ cover trường hợp user mua nhiều đơn nhỏ + đổi voucher (không trừ tier nhưng vẫn loyal)
    - Cho phép admin chỉnh ngưỡng qua `/admin/loyalty/config` (lưu vào bảng `PointTiers`)
  - ⏳ **Anh quyết định cuối cùng:** A / B / C? (mặc định em code theo C nếu anh không nói gì)
- [x] **Ưu đãi VIP:**
  - **Free ship tất cả các đơn** (kể cả đơn < ngưỡng free-ship thường).
  - **+1 điểm thưởng cho mỗi đơn COMPLETED** (bonus tier, không tính từ tỷ lệ 100k/điểm).
  - **Voucher VIP riêng** (catalog riêng, admin tạo qua `/admin/loyalty/vouchers` với flag `vip_only=1`).
- [x] **Logic nâng hạng (chi tiết):**
  - Hook trong `OrderService.confirmReceived(orderId, username)`:
    1. Set `Orders.status = COMPLETED`, set `completed_at`
    2. `pointService.earnPoints(userId, orderId, totalForPointCalc, ...)`
    3. Nếu user có VIP bonus enabled → `+1` điểm
    4. `pointTierService.checkAndUpgradeTier(userId)`:
       - Tính tổng chi tiêu (SUM total_for_point_calc WHERE status=COMPLETED)
       - Tính tổng điểm đã earn (`UserPoints.total_earned`)
       - Match ngưỡng tier → nếu tier mới cao hơn tier hiện tại → `UPDATE Users.tier_code`
       - Ghi `OrderEvent` (special type `TIER_UPGRADE`) để audit
       - Nếu vừa lên VIP lần đầu → `emailService.sendVipWelcome(user.email)`
  - Hook trong `OrderService.placeOrder(...)`:
    1. Set `Orders.status = PENDING`
    2. **Nếu user chưa có `tier_code` (NULL) và đây là đơn đầu tiên** → set `Users.tier_code = 'THUONG'`
    3. Tạo `UserPoints` row nếu chưa có (với `current_points = 0`)
- [x] **Schema bảng `PointTiers` chi tiết (theo đề xuất C):**
  ```
  code           | name              | min_total_spent | min_total_points | benefits (JSON)
  -------------- | ----------------- | --------------- | ---------------- | ----------------------------
  THUONG         | Khách hàng thường | 0               | 0                | {"vipBonus": false, ...}
  VIP            | Khách hàng VIP    | 5000000         | 50               | {"vipBonus": true, "freeShipping": true, ...}
  ```
  - 2 row này được INSERT seed trong `dbTheXuong.sql` Batch 4.
  - Ngưỡng có thể admin sửa qua `/admin/loyalty/config` (sẽ UPDATE 2 row).
- [x] **Edge case:**
  - User chi 6M (VIP) → refund 2M → tổng còn 4M → **KHÔNG hạ xuống THUONG** (đã là VIP thì giữ). Đây là rule chuẩn Shopee/Tiki.
  - User từng VIP, lâu không mua → **CÓ THỂ bị hạ** theo Phương án Y bên dưới.

### 3.1. Quy tắc hạ hạng VIP → THUONG (đã chốt)

> **Phương án đã chốt: Y — Re-evaluate theo năm** (giống Tiki, 22/06/2026).

- [x] **Cơ chế:** Mỗi **365 ngày** kể từ lần cuối đạt đủ điều kiện VIP, cron job check lại:
  - Tính **tổng chi tiêu trong 365 ngày gần nhất** = `SUM(Orders.total_for_point_calc) WHERE status='COMPLETED' AND completed_at >= :evalStart`
  - Tính **tổng điểm đã earn trong 365 ngày** = query `SUM(points) FROM PointTransactions WHERE type='EARN' AND created_at >= :evalStart`
  - Match ngưỡng VIP theo **Phương án C** (đã chốt ở mục 3):
    - Chi tiêu 365 ngày ≥ 5.000.000 VND → **Giữ VIP** ✅
    - Hoặc điểm earn 365 ngày ≥ 50 → **Giữ VIP** ✅
    - Cả 2 đều < ngưỡng → **Hạ xuống THUONG** ⬇
- [x] **Tần suất chạy:** Cron **00:00 ngày 1 hàng tháng** (12 lần/năm) — đủ để kịp thời phát hiện, không quá tải.
  - Expression: `@Scheduled(cron = "0 0 0 1 * *", zone = "Asia/Ho_Chi_Minh")`
- [x] **Lịch sử tier lưu ở đâu:** Bảng mới `tier_evaluation_log`:
  ```
  tier_evaluation_log:
    id BIGINT PK
    user_id BIGINT FK -> Users(id)
    evaluated_at DATETIME
    window_start DATETIME2    -- 365 ngày trước evaluated_at
    window_end DATETIME2      -- = evaluated_at
    total_spent DECIMAL(18,0) -- tổng chi tiêu trong window
    total_points_earned INT   -- tổng điểm earn trong window
    old_tier_code NVARCHAR(20)
    new_tier_code NVARCHAR(20)
    reason NVARCHAR(500)      -- "Giữ VIP - chi tiêu 6.5M trong 365 ngày"
                              -- "Hạ THUONG - chỉ chi 2M, 12 điểm trong 365 ngày"
  ```
- [x] **Thông báo email:**
  - **Cảnh báo trước 30 ngày** (chạy cron daily, query user sắp đến hạn evaluation): "Tài khoản anh/chị sắp đến kỳ đánh giá VIP vào YYYY-MM-DD. Hiện tại anh/chị đã chi Xđ / Y điểm trong 365 ngày. Nếu không đạt 5M hoặc 50 điểm, hạng sẽ tự động chuyển về Khách hàng thường."
  - **Sau khi hạ**: "Tài khoản anh/chị đã được hạ xuống Khách hàng thường vào YYYY-MM-DD. Để lên lại VIP, hãy đạt 5M chi tiêu hoặc 50 điểm tích luỹ."
- [x] **Edge case quan trọng:**
  - User VIP bị hạ THUONG → mua tiếp đạt 5M → **được lên lại VIP bình thường** (không phải trừng phạt vĩnh viễn).
  - User chỉ vừa lên VIP < 365 ngày → **KHÔNG evaluate** (chờ đủ 365 ngày). Lưu `tier_promoted_at` để track.
  - User vừa bị hạ xuống THUONG → ngay ngày hôm sau mua đủ 5M → **không tự động lên VIP ngay**, chờ cron tháng sau hoặc admin manual upgrade.
- [x] **So sánh với sàn lớn (theo em đã phân tích):**
  - Shopee: giữ vĩnh viễn (chọn nếu muốn simple nhất → Phương án X)
  - **Tiki: re-evaluate 12 tháng → chọn Phương án Y** ✅
  - Lazada: re-evaluate theo quý (3 tháng) → quá aggressive, không phù hợp sport apparel
- [x] **Câu hỏi cần anh xác nhận thêm (không blocker):**
  - ⏳ Có cần **mua ít nhất 1 đơn trong 365 ngày** để giữ VIP không? (nếu 365 ngày không đơn nào → tự động hạ dù tổng chi cũ > 5M)
  - ⏳ User có active session ở thời điểm hạ → có flash message trên web không?

## 4. Phạm vi & vai trò

| Role | Quyền với Loyalty |
|---|---|
| CUSTOMER | Xem số dư điểm, lịch sử `PointTransaction`, đổi voucher, áp voucher khi checkout |
| ADMIN | CRUD tỷ lệ quy đổi, CRUD catalog voucher, khoá/mở khoá voucher, xem báo cáo |
| BOTH | Kế thừa cả CUSTOMER + ADMIN |

## 5. Tích hợp với flow hiện tại

- [x] **Hook cộng điểm:** gắn vào `OrderService.confirmReceived(orderId, username)` — method **đã có sẵn** ở Batch 0, hook loyalty gắn ở Batch 1.
- [x] **Hook trừ điểm khi hoàn tiền:** gắn vào `OrderService.refundOrder(orderId, adminUsername)` — **đã có sẵn** ở Batch 0, hook loyalty gắn ở Batch 1.
- [x] **Áp voucher tại checkout:** cần thêm `voucherCode` (optional) vào form `POST /place-order` trong `OrderController` — Batch 3.
- [x] **VNPay return URL:** thêm param `vnp_OrderInfo` chứa cả mã voucher (format: `"Thanh toan don hang ma so X voucher=Y"`) — `vnpay-return` handler parse ra.

## 6. Database — bảng mới / cột mới

> Migration được viết vào `dbTheXuong.sql` (idempotent với `IF NOT EXISTS`).

### Bảng mới

- [x] **`PointTiers`** — phân hạng (Batch 1)
  - `id`, `code` (`THUONG`/`VIP`), `name`, `min_total_spent` (VND), `min_total_points`, `benefits` (JSON), `created_at`
- [x] **`UserPoints`** — số dư điểm của user (Batch 1)
  - `user_id` (FK → Users, UNIQUE)
  - `current_points` (INT DEFAULT 0)
  - `total_earned` (BIGINT, cộng dồn)
  - `total_spent` (BIGINT, cộng dồn)
  - `last_activity_at` (DATETIME2)
  - `version` (BIGINT, optimistic lock)
- [x] **`PointTransactions`** — lịch sử (Batch 1)
  - `id`, `user_id`, `order_id`, `user_voucher_id` (nullable), `type` (EARN/SPEND/REVERSE/EXPIRE/ADJUST)
  - `points` (INT), `expires_at`, `admin_id`, `note`, `created_at`
- [x] **`Vouchers`** — catalog (Batch 2)
  - `id`, `code` (UNIQUE), `discount_amount`, `required_points`, `min_order_amount`, `applicable_category_ids` (JSON), `applicable_product_ids` (JSON), `vip_only` (BIT), `status` (ACTIVE/LOCKED/EXPIRED), `created_at`, `updated_at`
- [x] **`UserVouchers`** — voucher user sở hữu (Batch 2)
  - `id`, `user_id`, `voucher_id`, `code` (UNIQUE, snapshot), `status` (UNUSED/USED/EXPIRED)
  - `issued_at`, `expires_at` (= issued_at + 30 ngày), `used_at`, `used_in_order_id`
- [x] **`OrderEvents`** — audit timeline status transitions (Batch 4)
  - `id`, `order_id`, `from_status`, `to_status`, `actor_id`, `actor_type` (USER/ADMIN/SYSTEM/VNPAY), `note`, `created_at`
- [x] **`tier_evaluation_log`** — audit re-evaluate theo Phương án Y (Batch 4)
  - `id`, `user_id`, `evaluated_at`, `window_start`, `window_end`, `total_spent`, `total_points_earned`, `old_tier_code`, `new_tier_code`, `reason`
- [x] **Cột mới trên `Users`:** (Batch 4)
  - `tier_code` (NVARCHAR(20) DEFAULT 'THUONG')
  - `tier_promoted_at` (DATETIME2 NULL)
- [x] **Cột mới trên `Orders`:** (Batch 0 + Batch 3)
  - `paid_at`, `shipped_at`, `delivered_at`, `completed_at`, `cancelled_at`, `refunded_at` (Batch 0)
  - `subtotal`, `shipping_fee`, `discount_amount`, `points_used`, `voucher_code`, `total_for_point_calc` (Batch 3)

## 7. UX / Frontend (Vue 3 + TypeScript + Tailwind)

> ❗ **Lưu ý:** Skill `spring-thymeleaf-to-vue3` yêu cầu mọi quyết định UI **phải có Figma selection** từ anh trước khi code. Không tự ý thiết kế.

### Trang customer
- [ ] `/loyalty` — "Điểm thưởng của tôi" (số dư + lịch sử + nút đổi voucher)
- [ ] `/loyalty/redeem` — danh sách 6 mệnh giá có thể đổi
- [ ] `/my-vouchers` — 3 tab UNUSED/USED/EXPIRED
- [ ] Widget ở trang `/checkout` — hiển thị điểm khả dụng + ô nhập mã voucher

### Trang admin
- [ ] `/admin/loyalty/config` — cấu hình tỷ lệ quy đổi (mặc định 100k = 1 điểm)
- [ ] `/admin/loyalty/vouchers` — CRUD catalog voucher (thêm mệnh giá, điểm yêu cầu, điều kiện)
- [ ] `/admin/loyalty/report` — báo cáo (tổng điểm phát hành, top 10 user theo điểm + chi tiêu)

## 8. Yêu cầu phi chức năng

- [x] **Email thông báo:**
  - ✅ Cộng điểm thành công (`sendPointsEarned`)
  - ✅ Đổi voucher thành công (`sendVoucherRedeemed`)
  - ✅ Voucher sắp hết hạn (cron `VoucherExpiringSoonJob` gửi email cho user có voucher còn 3 ngày → hết hạn)
  - ✅ Lên hạng VIP (`sendVipWelcome`)
  - ✅ Hạ hạng (`sendVipDowngraded`)
  - ✅ Cảnh báo VIP sắp đánh giá lại (`sendVipExpiryWarning`)
- [x] **Báo cáo admin:** xem mục `/admin/loyalty/report` ở trên.
- [x] **REST API cho mobile:** **CÓ** — toàn bộ controller sẽ expose dạng `/api/loyalty/**` (JSON) để tương lai gọi từ mobile. Thymeleaf controller cũ vẫn giữ, không phá vỡ flow hiện tại.
- [x] **Audit log khi admin sửa thủ công:**
  - Mọi `PointTransaction.type = ADJUST` phải có `note` bắt buộc + lưu `admin_id` vào `PointTransactions.admin_id` (cột có sẵn).
- [x] **Cron job expire:**
  - `PointExpireJob` daily lúc `00:00` quét `PointTransactions` đã quá `expires_at` → tạo giao dịch `EXPIRE` âm → cập nhật `UserPoints.current_points`.
  - `VoucherExpireJob` daily lúc `00:30` quét `UserVouchers` quá `expires_at` mà vẫn `UNUSED` → chuyển `EXPIRED`.
  - `VoucherExpiringSoonJob` daily lúc `09:00` cảnh báo user có voucher còn 3 ngày.
  - `TierReevaluateJob` monthly lúc `00:00 ngày 1` re-evaluate user VIP theo Phương án Y.
  - `TierWarningJob` daily lúc `09:00` cảnh báo user VIP sắp đến hạn (còn 30 ngày).

---

## Phân tích sâu: Luồng OrderService & đề xuất cải thiện

### A. Tình trạng hiện tại (Batch 0)

**File:** `src/main/java/com/example/thexuong/service/OrderService.java` (refactored)
**File:** `src/main/java/com/example/thexuong/entity/Order.java` (refactored)
**File:** `src/main/java/com/example/thexuong/controller/OrderController.java` (refactored)

**Status enum (dùng `OrderStatus` enum):**
```java
public enum OrderStatus {
    PENDING,      // chờ thanh toán
    CONFIRMED,    // đã thanh toán, chờ shop xử lý
    SHIPPING,     // đang giao
    DELIVERED,    // đã giao, chờ user confirm
    COMPLETED,    // user đã nhận → CỘNG ĐIỂM ở đây
    CANCELLED,    // huỷ trước khi CONFIRMED
    REFUNDED;     // hoàn tiền sau thanh toán → TRỪ ĐIỂM ở đây

    public boolean canTransitionTo(OrderStatus next) {
        return switch (this) {
            case PENDING    -> next == CONFIRMED || next == CANCELLED;
            case CONFIRMED  -> next == SHIPPING   || next == CANCELLED || next == REFUNDED;
            case SHIPPING   -> next == DELIVERED  || next == REFUNDED;
            case DELIVERED  -> next == COMPLETED  || next == REFUNDED;
            case COMPLETED, CANCELLED, REFUNDED -> false;
        };
    }
}
```

### B. So sánh với shop lớn (Shopee / Tiki / Lazada)

Luồng chuẩn của các sàn lớn thường là:

```
PENDING (chờ thanh toán)
  ↓ (VNPay/COD xong)
CONFIRMED (đã xác nhận / đang chuẩn bị)
  ↓ (shipper nhận)
SHIPPING (đang giao)
  ↓ (giao xong, chờ user xác nhận)
DELIVERED (đã giao — bắt đầu countdown auto-complete)
  ↓ (user bấm "Đã nhận hàng")
COMPLETED (hoàn tất → cộng điểm, cho phép đánh giá)
  ↓ (sau 7-15 ngày nếu user im lặng)
AUTO_COMPLETED (auto-complete)

CANCELLED (chỉ khi còn PENDING/CONFIRMED)
REFUNDED (sau khi đã thanh toán → hoàn tiền)
RETURNED (trả hàng sau khi nhận)
```

**Đặc biệt quan trọng cho Loyalty:**
- Shopee **chỉ cộng Shopee Xu** khi đơn `COMPLETED`, không bao giờ cộng sớm.
- Lazada có thêm trạng thái `DELIVERED` (chờ user confirm) và `COMPLETED` (đã confirm) — và đây là 2 state khác nhau.

### C. Các vấn đề cần cải thiện (Batch 0)

| # | Vấn đề | Mức độ | Đề xuất fix (đã làm) |
|---|---|---|---|
| 1 | Status không nhất quán (`SHIPPING` vs `SHIPPED` vs `COMPLETED`) | 🔴 Critical | Refactor `String status` → `enum OrderStatus` ✅ |
| 2 | Không có state machine | 🔴 Critical | Thêm `OrderStatus.canTransitionTo()` ✅ |
| 3 | `vnpayReturn` set lại `PENDING` | 🔴 Critical | Set `CONFIRMED` + `paid_at` ✅ |
| 4 | Thiếu "xác nhận đã nhận hàng" | 🟠 Major | Tạo `POST /order/{id}/confirm-received` ✅ |
| 5 | Thiếu "yêu cầu hoàn tiền" | 🟠 Major | Tạo `refundOrder` (admin) ✅ |
| 6 | Total price lưu 1 lần, không tách | 🟠 Major | Tách `subtotal` / `shippingFee` / `discountAmount` (Batch 3) ✅ |
| 7 | `getOrderByIdAndUser` query có thể duplicate rows | 🟡 Minor | (defer) |
| 8 | Không lưu payment status | 🟠 Major | Tách `paid_at` ✅ |
| 9 | Thiếu timestamp | 🟠 Major | Thêm `shipped_at` / `delivered_at` / `completed_at` / `cancelled_at` / `refunded_at` ✅ |
| 10 | OrderService thiếu method cho admin | 🟡 Minor | Tạo `adminUpdateStatus` ✅ |
| 11 | Thiếu OrderEventLog | 🟡 Minor | Tạo bảng `OrderEvents` (Batch 4) ✅ |
| 12 | Total bị tính lại không nhất quán | 🟠 Major | Snapshot `total_for_point_calc = subtotal` (Batch 3) ✅ |

### D. Đề xuất model mới cho Order (đã implement)

```java
public enum OrderStatus {
    PENDING, CONFIRMED, SHIPPING, DELIVERED, COMPLETED, CANCELLED, REFUNDED;
    public boolean canTransitionTo(OrderStatus next) { /* state machine */ }
}

@Entity @Table(name = "Orders")
public class Order {
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status;

    private BigDecimal subtotal;
    private BigDecimal shippingFee;
    private BigDecimal discountAmount;
    private BigDecimal totalMoney;  // = subtotal + shippingFee - discountAmount

    private LocalDateTime paidAt;
    private LocalDateTime shippedAt;
    private LocalDateTime deliveredAt;
    private LocalDateTime completedAt;   // ← hook cộng điểm ở đây
    private LocalDateTime cancelledAt;
    private LocalDateTime refundedAt;

    // Loyalty columns
    private Integer pointsUsed;
    @Column(length = 20) private String voucherCode;
    private BigDecimal totalForPointCalc;  // = subtotal (không trừ discount)
}
```

### E. Plan triển khai (6 batch)

| Batch | Nội dung | Trạng thái |
|---|---|---|
| 0 | Refactor `OrderStatus` enum + sửa `OrderRepository` query cũ + migration cột mới | ✅ DONE 92% |
| 1 | Order lifecycle + `confirm-received`, `refund` endpoint + state machine + `paidAt`/`completedAt` | ✅ DONE 95% |
| 2 | Points core: `UserPoints`, `PointTransactions`, `PointService` (earn/spend/reverse/expire FIFO) | ✅ DONE 85% |
| 3 | Voucher catalog: `Vouchers`, `UserVouchers`, admin CRUD `/admin/loyalty/vouchers` | ✅ DONE 80% |
| 4 | Redeem & apply: `/loyalty/redeem`, `/my-vouchers`, widget checkout, áp voucher vào `placeOrder` | ✅ DONE 88% |
| 5 | Tier upgrade (VIP) + Re-evaluate Cron + Email + Admin Report | ⏳ đang làm |

### F. Open Questions đã chốt

> Cập nhật: **Anh đồng ý toàn bộ 7 open questions** (đã verify qua session chat). Code sẽ implement đúng.

| # | Câu hỏi | Quyết định cuối |
|---|---|---|
| 1 | Refactor `String status` → `enum OrderStatus` | ✅ Refactor luôn Batch 0 |
| 2 | `totalMoney` dùng để tính điểm = `subtotal` | ✅ Dùng `total_for_point_calc` snapshot |
| 3 | `OrderDetail` lưu `productId` lỏng | ✅ Giữ nguyên |
| 4 | VNPay return URL format | ✅ `"Thanh toan don hang ma so X voucher=TX-ABCDEF"` |
| 5 | Free ship VIP — làm schema ship fee trước | ✅ Loyalty trước, ship fee sau |
| 6 | Email HTML hay plain text | ✅ HTML đơn giản |
| 7 | Cron timezone | ✅ GMT+7 (Asia/Ho_Chi_Minh) |

### G. Câu hỏi bổ sung (đã chốt)

- [x] **Tier khởi đầu khi nào?** → Khi user đặt **đơn hàng đầu tiên** (PENDING), set `tier_code = 'THUONG'`.
- [x] **Tiêu chí lên VIP** → **Phương án C** (kết hợp chi tiêu HOẶC điểm).
- [x] **Quy tắc hạ VIP** → **Phương án Y** (Re-evaluate theo năm — giống Tiki).

### H. Câu hỏi phụ (không blocker, em sẽ default nếu anh không nói)

- [x] **Có cần mua ít nhất 1 đơn trong 365 ngày để giữ VIP?** → KHÔNG cần, chỉ cần tổng chi tiêu/điểm đạt ngưỡng (theo đề xuất).
- [x] **User có active session ở thời điểm hạ → có flash message?** → CÓ (deferred - không blocker Batch 6).

---

## Liên kết

- Skill đã load: `thexuong-stack`, `plan`, `spring-thymeleaf-to-vue3`
- **Plan triển khai:** `orderstatus.md` (đã đổi tên từ `.hermes/plans/2026-06-22_004349-orderstatus-loyalty-voucher.md`)
- File liên quan:
  - `dbTheXuong.sql` — nơi migration được append
  - `src/main/java/com/example/thexuong/service/OrderService.java` — đã refactor
  - `src/main/java/com/example/thexuong/entity/Order.java` — đã refactor
  - `src/main/java/com/example/thexuong/controller/OrderController.java` — đã thêm 2 endpoint
  - `src/main/java/com/example/thexuong/repository/OrderRepository.java` — query đã sửa
