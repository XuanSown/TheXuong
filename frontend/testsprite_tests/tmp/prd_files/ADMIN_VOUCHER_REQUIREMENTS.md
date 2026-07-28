# Yêu Cầu Thiết Kế: AdminVoucher.vue

**Ngày tạo:** 24/06/2026  
**Dự án:** TheXuong Sport — Loyalty & Voucher System  
**Source of Truth:** `voucher.md` (Batch 2 + Batch 4)  
**Trạng thái:** 🟡 Cần xác nhận từ Anh

---

## 📋 Tổng Quan

Trang admin quản lý **Voucher Catalog** — nơi admin có thể CRUD (Create, Read, Update, Delete) các voucher template với mệnh giá cố định: `10k, 20k, 50k, 100k, 200k, 500k`.

> **⚠️ Phân biệt rõ:**
> - **Vouchers** (catalog) — template do admin tạo, có `required_points`, `discount_amount`
> - **UserVouchers** — voucher đang sở hữu bởi user (INSTANCE từ catalog, có `expiresAt = issuedAt + 30 ngày`)

---

---

## 🗄️ **COMPLETE DATABASE SCHEMA**

### Overview: All Tables in Loyalty & Voucher System

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│    PointTiers   │     │    UserPoints   │     │ PointTrans.    │
└─────────────────┘     └─────────────────┘     └─────────────────┘
         │                       │                       │
         │                       │                       │
         ▼                       ▼                       ▼
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│     Vouchers    │◄────┤   UserVouchers  │────►│   OrderEvents  │
└─────────────────┘     └─────────────────┘     └─────────────────┘
         │                                               │
         │                                               │
         └───────────────────────────────────────────────┘
                            │
                            ▼
                      ┌─────────┐
                      │ Orders  │
                      └─────────┘
```

---

### 1. Vouchers (Catalog Table — Main Focus)

```sql
CREATE TABLE Vouchers (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    code NVARCHAR(50) UNIQUE NOT NULL,           -- Format: TX-XXXXXX
    discount_amount DECIMAL(18,0) NOT NULL,     -- 10000, 20000, 50000...
    required_points INT NOT NULL,               -- 1, 2, 5, 10, 20, 50
    min_order_amount DECIMAL(18,0) DEFAULT 0,  -- Điều kiện đơn tối thiểu
    applicable_category_ids NVARCHAR(MAX),      -- JSON array: [1,2,3] or NULL
    applicable_product_ids NVARCHAR(MAX),      -- JSON array: [101,205] or NULL
    vip_only BIT DEFAULT 0,                    -- 0=No, 1=Yes
    status NVARCHAR(20) DEFAULT 'ACTIVE',      -- ACTIVE, LOCKED, EXPIRED
    expires_at DATETIME2 NULL,                 -- Admin-set expiration (NULL = never)
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE(),
    created_by NVARCHAR(100) NULL,             -- Admin username
    updated_by NVARCHAR(100) NULL              -- Admin username
);

-- Indexes
CREATE INDEX IX_Vouchers_status ON Vouchers(status);
CREATE INDEX IX_Vouchers_vip_only ON Vouchers(vip_only);
CREATE INDEX IX_Vouchers_required_points ON Vouchers(required_points);
```

---

### 2. UserVouchers (Instance Table — User's Owned Vouchers)

```sql
CREATE TABLE UserVouchers (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    user_id BIGINT NOT NULL FOREIGN KEY REFERENCES Users(id) ON DELETE CASCADE,
    voucher_id BIGINT NOT NULL FOREIGN KEY REFERENCES Vouchers(id) ON DELETE RESTRICT,
    code NVARCHAR(50) NOT NULL UNIQUE,          -- Snapshot của Vouchers.code
    discount_amount_snapshot DECIMAL(18,0) NOT NULL,  -- Snapshot tại thời điểm redeem
    required_points_snapshot INT NOT NULL,      -- Snapshot
    min_order_amount_snapshot DECIMAL(18,0) NOT NULL, -- Snapshot
    vip_only_snapshot BIT NOT NULL,            -- Snapshot
    status NVARCHAR(20) DEFAULT 'UNUSED',      -- UNUSED, USED, EXPIRED
    issued_at DATETIME2 DEFAULT GETDATE(),
    expires_at DATETIME2 NOT NULL,             -- = issued_at + 30 days
    used_at DATETIME2 NULL,
    used_in_order_id BIGINT NULL FOREIGN KEY REFERENCES Orders(id),
    created_at DATETIME2 DEFAULT GETDATE()
);

-- Critical Indexes
CREATE UNIQUE INDEX IX_UserVouchers_user_voucher
    ON UserVouchers(user_id, voucher_id)
    WHERE status IN ('UNUSED', 'USED');  -- Mỗi user chỉ 1 active voucher/catalog

CREATE INDEX IX_UserVouchers_user_id ON UserVouchers(user_id);
CREATE INDEX IX_UserVouchers_status ON UserVouchers(status);
CREATE INDEX IX_UserVouchers_expires_at ON UserVouchers(expires_at);
```

**Key Design Decisions:**
- **ON DELETE RESTRICT** on `voucher_id`: Prevent deleting catalog voucher if UserVouchers exist
- **Unique constraint (user_id, voucher_id)**: Each user can claim each catalog voucher only once
- **Snapshot fields**: Snapshot values at redeem time (edit catalog doesn't affect existing UserVouchers)

---

### 3. PointTiers (Tier Configuration)

```sql
CREATE TABLE PointTiers (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    code NVARCHAR(20) UNIQUE NOT NULL,        -- 'THUONG', 'VIP'
    name NVARCHAR(100) NOT NULL,             -- 'Khách hàng thường', 'Khách hàng VIP'
    min_total_spent DECIMAL(18,0) NOT NULL,  -- 0, 5000000
    min_total_points INT NOT NULL,           -- 0, 50
    benefits NVARCHAR(MAX),                  -- JSON: {"vipBonus": true, "freeShipping": true}
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

-- Seed data (theo voucher.md)
INSERT INTO PointTiers (code, name, min_total_spent, min_total_points, benefits) VALUES
('THUONG', 'Khách hàng thường', 0, 0, '{"vipBonus": false, "freeShipping": false}'),
('VIP', 'Khách hàng VIP', 5000000, 50, '{"vipBonus": true, "freeShipping": true}');
```

---

### 4. UserPoints (User's Point Balance)

```sql
CREATE TABLE UserPoints (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    user_id BIGINT UNIQUE NOT NULL FOREIGN KEY REFERENCES Users(id) ON DELETE CASCADE,
    current_points INT DEFAULT 0,             -- Số điểm hiện tại (không âm)
    total_earned BIGINT DEFAULT 0,           -- Tổng điểm đã tích lũy (không giảm)
    total_spent BIGINT DEFAULT 0,            -- Tổng điểm đã dùng (không giảm)
    last_activity_at DATETIME2 NULL,
    version BIGINT DEFAULT 0,                -- Optimistic locking
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);

CREATE INDEX IX_UserPoints_user_id ON UserPoints(user_id);
```

---

### 5. PointTransactions (Point History)

```sql
CREATE TABLE PointTransactions (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    user_id BIGINT NOT NULL FOREIGN KEY REFERENCES Users(id),
    order_id BIGINT NULL FOREIGN KEY REFERENCES Orders(id),
    user_voucher_id BIGINT NULL FOREIGN KEY REFERENCES UserVouchers(id),
    type NVARCHAR(20) NOT NULL,              -- EARN, SPEND, REVERSE, EXPIRE, ADJUST
    points INT NOT NULL,                     -- Positive or negative
    balance_after INT NOT NULL,              -- Số điểm sau khi transaction
    expires_at DATETIME2 NULL,               -- Chỉ dùng cho EARN (expiry 12 months)
    admin_id NVARCHAR(100) NULL,             -- Nếu type=ADJUST
    note NVARCHAR(500) NULL,                -- Ghi chú (bắt buộc với ADJUST)
    created_at DATETIME2 DEFAULT GETDATE()
);

CREATE INDEX IX_PointTransactions_user_id ON PointTransactions(user_id);
CREATE INDEX IX_PointTransactions_created_at ON PointTransactions(created_at);
CREATE INDEX IX_PointTransactions_expires_at ON PointTransactions(expires_at);
CREATE INDEX IX_PointTransactions_type ON PointTransactions(type);
```

---

### 6. tier_evaluation_log (VIP Tier Re-evaluation Audit)

```sql
CREATE TABLE tier_evaluation_log (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    user_id BIGINT NOT NULL FOREIGN KEY REFERENCES Users(id),
    evaluated_at DATETIME2 NOT NULL,
    window_start DATETIME2 NOT NULL,         -- 365 ngày trước evaluated_at
    window_end DATETIME2 NOT NULL,           -- = evaluated_at
    total_spent DECIMAL(18,0) NOT NULL,     -- Tổng chi tiêu trong 365 ngày
    total_points_earned INT NOT NULL,       -- Tổng điểm earned trong 365 ngày
    old_tier_code NVARCHAR(20) NOT NULL,
    new_tier_code NVARCHAR(20) NOT NULL,
    reason NVARCHAR(500) NOT NULL,
    created_at DATETIME2 DEFAULT GETDATE()
);

CREATE INDEX IX_TierEvalLog_user_id ON tier_evaluation_log(user_id);
CREATE INDEX IX_TierEvalLog_evaluated_at ON tier_evaluation_log(evaluated_at);
```

---

### 7. VoucherAuditLog (Voucher Change Audit — NEW for AdminVoucher)

```sql
CREATE TABLE VoucherAuditLog (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    voucher_id BIGINT NOT NULL FOREIGN KEY REFERENCES Vouchers(id),
    admin_id NVARCHAR(100) NOT NULL,         -- Username của admin
    action NVARCHAR(20) NOT NULL,           -- CREATE, UPDATE, DELETE, LOCK, UNLOCK
    old_values NVARCHAR(MAX) NULL,          -- JSON snapshot trước khi thay đổi
    new_values NVARCHAR(MAX) NULL,          -- JSON snapshot sau khi thay đổi
    changed_fields NVARCHAR(MAX) NULL,      -- JSON array: ["discount_amount", "status"]
    note NVARCHAR(500) NULL,                -- Ghi chú admin (ví dụ: "Lock do hết số lượng")
    created_at DATETIME2 DEFAULT GETDATE()
);

CREATE INDEX IX_VoucherAuditLog_voucher_id ON VoucherAuditLog(voucher_id);
CREATE INDEX IX_VoucherAuditLog_created_at ON VoucherAuditLog(created_at);
CREATE INDEX IX_VoucherAuditLog_admin_id ON VoucherAuditLog(admin_id);
```

---

### 8. Orders (Extended — Existing Table with New Columns)

```sql
-- Cột mới trong Orders (đã có sẵn từ batch 0/3)
ALTER TABLE Orders ADD (
    -- Monetary breakdown
    subtotal DECIMAL(18,0) NULL,            -- Tổng tiền hàng (chưa phí, chưa discount)
    shipping_fee DECIMAL(18,0) NULL,        -- Phí vận chuyển
    discount_amount DECIMAL(18,0) NULL,     -- Tổng discount (voucher + other)
    points_used INT NULL,                   -- Số điểm đã dùng để trừ
    voucher_code NVARCHAR(50) NULL,         -- Mã voucher đã áp dụng
    total_for_point_calc DECIMAL(18,0) NULL, -- = subtotal (snapshot để tính điểm)

    -- Timestamps
    paid_at DATETIME2 NULL,
    shipped_at DATETIME2 NULL,
    delivered_at DATETIME2 NULL,
    completed_at DATETIME2 NULL,
    cancelled_at DATETIME2 NULL,
    refunded_at DATETIME2 NULL
);

-- Indexes
CREATE INDEX IX_Orders_user_id_status ON Orders(user_id, status);
CREATE INDEX IX_Orders_completed_at ON Orders(completed_at);
CREATE INDEX IX_Orders_voucher_code ON Orders(voucher_code);
```

---

### 9. OrderEvents (Order Status History Audit)

```sql
CREATE TABLE OrderEvents (
    id BIGINT PRIMARY KEY IDENTITY(1,1),
    order_id BIGINT NOT NULL FOREIGN KEY REFERENCES Orders(id),
    from_status NVARCHAR(20) NULL,
    to_status NVARCHAR(20) NOT NULL,
    actor_id NVARCHAR(100) NOT NULL,        -- Username (user hoặc admin)
    actor_type NVARCHAR(20) NOT NULL,       -- USER, ADMIN, SYSTEM, VNPAY
    note NVARCHAR(500) NULL,
    created_at DATETIME2 DEFAULT GETDATE()
);

CREATE INDEX IX_OrderEvents_order_id ON OrderEvents(order_id);
CREATE INDEX IX_OrderEvents_created_at ON OrderEvents(created_at);
```

---

## 🎯 Trạng Thái (Status) Voucher Catalog

| Status   | Ý Nghĩa | Khi Nào Dùng |
|----------|---------|--------------|
| `ACTIVE` | Mở, cho phép user đổi | Mặc định khi tạo |
| `LOCKED` | Khoá, không cho đổi | Admin khoá tạm (hết số lượng, lỗi bug) |
| `EXPIRED` | Hết hạn (admin đặt) | Admin set thủ công hoặc auto-expire cron |

---

## 🎨 UI/UX Design Requirements

### Layout Pattern
Theo pattern admin hiện tại (`AdminUsers.vue`, `AdminProducts.vue`):
- **Header Section**: Title + "THÊM VOUCHER MỚI" button (nút đen)
- **Search + Filters**: Search by code, filter by status (ACTIVE/LOCKED/EXPIRED), VIP only toggle
- **Data Table**: Danh sách voucher với columns và pagination
- **Form Section**: Add/Edit voucher form (nằm dưới table hoặc trong modal)

### Table Columns

| Column | Width | Content | Notes |
|--------|-------|---------|-------|
| Mã voucher | 150px | `code` (TX-XXXXXX) | Monospace font |
| Mệnh giá | 120px | `discount_amount` (định dạng tiền) | 10,000 đ |
| Điểm cần | 100px | `required_points` | Integer |
| Min order | 120px | `min_order_amount` | Điều kiện |
| VIP only | 80px | Badge (Có/Không) | Conditional display |
| Trạng thái | 100px | Status badge | Màu sắc khác nhau |
| Hành động | 150px | Edit | Delete buttons |

### Status Badges Styling
```css
.active-badge { background: #DCFCE7; color: #166534; }      /* xanh lá */
.locked-badge { background: #FEF3C7; color: #92400E; }     /* vàng cam */
.expired-badge { background: #FEE2E2; color: #991B1B; }   /* đỏ */
.vip-badge { background: #F3E8FF; color: #6B21A8; }       /* tím */
```

### Action Buttons
- **Edit (SỬA)**: Mở form edit với dữ liệu hiện tại
- **Delete (XÓA)**: Xác nhận xóa (và cleanup UserVouchers liên quan?)

---

## 📝 Form Fields (Add/Edit Voucher)

Sử dụng pattern form grid 2 columns từ `AdminUsers.vue`.

### Required Fields (Bắt buộc)

| Field | Type | Validation | Notes |
|-------|------|------------|-------|
| Mã voucher | Text input | Required, Unique, Format `TX-[A-Z0-9]{6}` | Auto-generate nếu để trống? |
| Mệnh giá giảm | Number input | Required, Min 1000, In [10k,20k,50k,100k,200k,500k] | Dropdown có sẵn 6 mệnh giá? |
| Điểm cần | Number input | Required, Min 1, Max 50 | Tự tính từ mệnh giá? (10k=1pt) |
| Min order amount | Number input | Optional, Default 0 | Điều kiện đơn tối thiểu |
| VIP only | Checkbox/Toggle | Optional | Chỉ VIP được dùng |
| Trạng thái | Select dropdown | Required | ACTIVE / LOCKED / EXPIRED |

### Optional Fields (Nâng cao)

| Field | Type | Notes |
|-------|------|-------|
| Applicable categories | Multi-select dropdown | Chọn từ ProductCategories |
| Applicable products | Multi-select dropdown | Chọn từ Products |
| Expires at | Date picker | Admin đặt hạn sử dụng catalog voucher (khác với UserVouchers expiresAt) |

---

## 🔍 Filter & Search

1. **Search by Code** — tìm theo `code` (TX-XXXXXX)
2. **Filter by Status** — Tất cả / ACTIVE / LOCKED / EXPIRED (button group)
3. **Filter by VIP only** — Toggle "Chỉ VIP" (hiển thị voucher `vip_only = 1`)
4. **Filter by Discount Amount** — Range slider hoặc dropdown [10k, 20k, 50k, 100k, 200k, 500k]

---

## 📊 Data Table Behavior

- **Pagination**: 5 items/page (theo pattern `AdminProducts.vue`)
- **Sortable**: Click column header để sort (code, discount, points, status)
- **Bulk Actions**: Select multiple → Bulk lock/unlock (nếu cần)
- **Row Click**: Click row → open edit modal/form

---

## 🔄 CRUD Operations

### CREATE (THÊM MỚI)
```
POST /admin/loyalty/vouchers
Body: {
  code: "TX-ABCD12",
  discountAmount: 100000,
  requiredPoints: 10,
  minOrderAmount: 0,
  applicableCategoryIds: [1,2],
  applicableProductIds: null,
  vipOnly: false,
  status: "ACTIVE"
}
```

**Auto-generate code logic?**
- ❓ **Câu hỏi**: Admin nhập code thủ công hay tự động `TX-` + 6 random (loại trừ 0/O/1/I/L)?

### READ (XEM DANH SÁCH)
```
GET /admin/loyalty/vouchers
Query params: page, size, search, status, vipOnly, minPoints, maxPoints
```

**Response:**
```json
{
  "vouchers": [
    {
      "id": 1,
      "code": "TX-ABCD12",
      "discountAmount": 100000,
      "requiredPoints": 10,
      "minOrderAmount": 0,
      "applicableCategoryIds": [1,2],
      "applicableProductIds": null,
      "vipOnly": false,
      "status": "ACTIVE",
      "createdAt": "2026-06-22T10:00:00",
      "updatedAt": "2026-06-22T10:00:00",
      "claimedCount": 150  // ← optional: COUNT từ UserVouchers
    }
  ],
  "total": 6,
  "page": 1,
  "size": 5
}
```

### UPDATE (CHỈNH SỬA)
```
PUT /admin/loyalty/vouchers/{id}
Body: { ...fields to update ... }
```

**Các field được cho phép sửa:**
- ✅ `discount_amount`
- ✅ `required_points`
- ✅ `min_order_amount`
- ✅ `applicable_category_ids`
- ✅ `applicable_product_ids`
- ✅ `vip_only`
- ✅ `status`

**⚠️ field KHÔNG cho sửa:**
- ❌ `id`
- ❌ `code` (nếu đã có UserVoucher claim thì không đổi code)

**Audit log:** Lưu `admin_id` + `note` vào bảng `VoucherAuditLog` (mới)?

### DELETE (XÓA)
```
DELETE /admin/loyalty/vouchers/{id}
```

**Soft delete vs Hard delete?**
- ❓ **Câu hỏi**: Soft delete (status=EXPIRED) hay hard delete?
- Nếu hard delete → phải cleanup `UserVouchers` có `voucher_id` đó?

---

## 📈 **claimedCount — Detailed Specification**

### Definition
`claimedCount` = **Tổng số lần voucher này đã được user đổi** (total redemption count)

**Rationale:**
- Mỗi user chỉ được claim 1 lần (unique constraint `(user_id, voucher_id)`)
- → `COUNT(*)` = `COUNT(DISTINCT user_id)` (2 query cho cùng 1 result)
- Hiển thị "popularity" của voucher

### SQL Query
```sql
SELECT COUNT(*) 
FROM UserVouchers 
WHERE voucher_id = :voucherId;
```

### Alternative: Separate Counts
Nếu muốn phân biệt:
- `claimedCount` = tổng số lần đổi (bao gồm cả đã dùng và chưa dùng)
- `usedCount` = số voucher đã dùng trong đơn hàng (`status = 'USED'`)
- `activeCount` = số voucher đang có thể dùng (`status = 'UNUSED'`)

**❓ Câu hỏi cho anh:**
> Cần phân biệt `claimedCount` / `usedCount` / `activeCount` không?
> - **Option A**: Chỉ hiển thị `claimedCount` (tổng số lần đổi)
> - **Option B**: Hiển thị cả 3 columns trong table (thêm 2 columns nữa)

**Em đề xuất Option A** (đơn giản, đủ để biết popularity). Nếu anh cần phân tích sâu hơn thì Option B.

---

## 🖥️ **Bulk Operations — Detailed UI/UX**

### Selection Mechanism
```
┌─────────────────────────────────────────────────────────────┐
│ ☐ Select All                                                │
├─────────────────────────────────────────────────────────────┤
│ ☐ TX-ABCD12  100,000đ  10  ACTIVE  [SỬA][XÓA]             │
│ ☑ TX-XYZ789  500,000đ  50  ACTIVE  [SỬA][XÓA]             │
│ ☐ TX-DEF456   20,000đ   2  LOCKED  [SỬA][XÓA]             │
│ ☑ TX-GHI789  100,000đ  10  ACTIVE  [SỬA][XÓA]             │
└─────────────────────────────────────────────────────────────┘
```

**Implementation:**
- Checkbox ở header → toggle all visible rows
- Checkbox ở mỗi row → individual selection
- Selected IDs lưu vào `selectedVoucherIds: []`

### Bulk Action Buttons
Hiển thị khi `selectedVoucherIds.length > 0`:

```
[LOCK 🔒] [UNLOCK 🔓] [DELETE 🗑️] [SET VIP ⭐]
```

**❓ Câu hỏi cho anh:**
> Bulk actions có nên có cả 4 nút trên không?
> - **Option A**: Chỉ Lock/Unlock (đơn giản)
> - **Option B**: Đủ 4 (Lock, Unlock, Delete→EXPIRED, Set VIP)
>
> **Em đề xuất Option B** vì đã đồng ý bulk scope, nhưng nếu anh sợ user nhầm thì có thể ẩn Set VIP (chỉ hiện với ACTIVE vouchers).

### Confirmation Dialog
Khi click bulk action:
```
┌─────────────────────────────────────────────────────┐
│ XÁC NHẬN THAY ĐỔI BULK                              │
├─────────────────────────────────────────────────────┤
│ Bạn có chắc chắn thay đổi trạng thái của 4 voucher│
│ đã chọn?                                            │
│                                                     │
│ [HỦY]  [XÁC NHẬN]                                  │
└─────────────────────────────────────────────────────┘
```

**❓ Câu hỏi cho anh:**
> Bulk delete có cần dialog warning đặc biệt không?
> - **Option A**: Dialog chung "Thay đổi 4 voucher?"
> - **Option B**: Dialog riêng cho delete: "Bạn có chắc chuyển 4 voucher sang EXPIRED? Hành động này không thể hoàn tác."
>
> **Em đề xuất Option B** (warning mạnh cho delete).

---

## 🔽 **Multi-Select Dropdown — Implementation Details**

### Component Choice
**❓ Câu hỏi cho anh:**
> Dùng component nào cho multi-select?
> - **Option A**: `<select multiple>` thuần (browser default) — đơn giản, không dependency
> - **Option B**: Vue component thư viện (VueMultiselect, PrimeVue Dropdown) — đẹp, có search
> - **Option C**: Custom component (build từ scratch) — fl
>
> **Em đề xuất Option A** để tránh dependency, kèm search box bên ngoài.

### Data Source
```
GET /api/categories           → [{ id: 1, name: "Footwear" }, ...]
GET /api/products?search=xxx  → [{ id: 101, name: "Nike Air Zoom", sku: "NAZ001" }, ...]
```

**❓ Câu hỏi cho anh:**
> Multi-select cần load tất cả categories/products hay search-on-demand?
> - **Option A**: Load tất cả lúc mở modal (cache, dùng lại)
> - **Option B**: Search-on-demand (gọi API mỗi lần type)
>
> **Em đề xuất Option A** vì số lượng categories/products không quá lớn (< 1000).

### Display Format
```
Selected: [Footwear ×] [Apparel ×] [Accessories ×]
              [x]        [x]          [x]
```

**❓ Câu hỏi cho anh:**
> Hiển thị selected items như thế nào?
> - **Option A**: Chips với [x] để remove (như tags input)
> - **Option B**: Chỉ hiển thị count: "Đã chọn 3 danh mục"
>
> **Em đề xuất Option A** (clear UX).

---

## 🔢 **Code Generation Algorithm**

### Character Set
Excluded ambiguous: `0, O, 1, I, L`
```
ALLOWED_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"
                 // 26 letters - 5 (OIL) = 21 + 10 digits - 1 (0,1) = 8 → Total 29 chars
```

**❓ Câu hỏi cho anh:**
> Characters set đầy đủ: 29 chars (A-Z exclude OIL + 0-9 exclude 01) đủ không?
> Hay cần include thêm ký tự đặc biệt? (Không khuyến nghị vì user dễ nhầm)

### Java Implementation
```java
private static final String ALLOWED_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
private static final SecureRandom RAND = new SecureRandom();

public String generateVoucherCode() {
    // Try max 5 times to avoid infinite loop
    for (int i = 0; i < 5; i++) {
        String code = "TX-" + randomString(6);
        if (!voucherRepository.existsByCode(code)) {
            return code;
        }
    }
    throw new IllegalStateException("Failed to generate unique voucher code after 5 attempts");
}

private String randomString(int length) {
    StringBuilder sb = new StringBuilder(length);
    for (int i = 0; i < length; i++) {
        int idx = RAND.nextInt(ALLOWED_CHARS.length());
        sb.append(ALLOWED_CHARS.charAt(idx));
    }
    return sb.toString();
}
```

**❓ Câu hỏi cho anh:**
> Format code có nên cho admin chỉnh sửa sau khi generate không?
> - **Option A**: Auto-generate, không cho sửa (readonly)
> - **Option B**: Auto-generate nhưng admin có thể override
>
> **Em đề xuất Option B** (linh hoạt, admin có thể dùng code memorable nếu muốn).

---

## ✅ **Validation Rules — Complete Spec**

### Client-Side (Vue Form)
| Field | Rule | Error Message |
|-------|------|---------------|
| code | Required, matches `^TX-[A-Z0-9]{6}$` | "Code phải định dạng TX-XXXXXX" |
| discountAmount | Required, ≥ 1000, in [10k,20k,50k,100k,200k,500k] | "Mệnh giá phải là 10k, 20k, 50k, 100k, 200k, hoặc 500k" |
| requiredPoints | Required, ≥ 1, ≤ 50 | "Điểm cần từ 1-50" |
| minOrderAmount | ≥ discountAmount | "Min order phải ≥ mệnh giá giảm" |
| expiresAt | If set, ≥ today | "Ngày hết hạn phải ≥ hôm nay" |
| applicableCategoryIds | Valid IDs (if set) | "Danh mục không hợp lệ" |
| applicableProductIds | Valid IDs (if set) | "Sản phẩm không hợp lệ" |

### Server-Side (Java Validator)
```java
public class VoucherValidator {
    
    private static final List<Integer> VALID_DISCOUNTS = 
        List.of(10000, 20000, 50000, 100000, 200000, 500000);
    
    public void validateCreate(VoucherCreateRequest req) {
        // 1. Code format
        if (!req.getCode().matches("^TX-[A-HJ-NP-RT-Z0-9]{6}$")) {
            throw new ValidationException("Invalid code format");
        }
        
        // 2. Discount amount validity
        if (!VALID_DISCOUNTS.contains(req.getDiscountAmount())) {
            throw new ValidationException(
                "Discount must be one of: 10k, 20k, 50k, 100k, 200k, 500k"
            );
        }
        
        // 3. Points consistency: requiredPoints = discountAmount / 10000
        int expectedPoints = req.getDiscountAmount() / 10000;
        if (req.getRequiredPoints() != expectedPoints) {
            throw new ValidationException(
                String.format("Required points must be %d for this discount", expectedPoints)
            );
        }
        
        // 4. Min order check
        if (req.getMinOrderAmount() < req.getDiscountAmount()) {
            throw new ValidationException("Min order must be ≥ discount amount");
        }
        
        // 5. Expiry check
        if (req.getExpiresAt() != null && req.getExpiresAt().isBefore(LocalDate.now())) {
            throw new ValidationException("Expiry date must be in the future");
        }
        
        // 6. Category/Product validation (if provided)
        validateIds(req.getApplicableCategoryIds(), "category");
        validateIds(req.getApplicableProductIds(), "product");
    }
    
    private void validateIds(List<Integer> ids, String type) {
        if (ids == null || ids.isEmpty()) return;
        // Check all IDs exist in DB
        long count = repository.countExistingIds(type, ids);
        if (count != ids.size()) {
            throw new ValidationException("Some " + type + " IDs are invalid");
        }
    }
}
```

**❓ Câu hỏi cho anh:**
> Rule #3 (points = discount / 10000) có nên strict không?
> - **Option A**: Strict — admin không thể override (luôn 1:1)
> - **Option B**: Soft warning — admin có thể set khác nhưng có warning
> - **Option C**: No rule — admin tự do (có thể 50k discount cho 40 points)
>
> **Em đề xuất Option A** (giữ economics balance). Nếu cần flexibility thì dùng Option B.

---

## 📡 **API Endpoints — Request/Response DTOs**

### Request DTOs

#### VoucherCreateRequest
```java
public class VoucherCreateRequest {
    private String code;               // optional: nếu null → auto generate
    private BigDecimal discountAmount; // required
    private Integer requiredPoints;    // required
    private BigDecimal minOrderAmount; // default 0
    private List<Integer> applicableCategoryIds; // optional
    private List<Integer> applicableProductIds; // optional
    private Boolean vipOnly;          // default false
    private String status;            // default "ACTIVE"
    private LocalDate expiresAt;      // optional, null = never
    // Getters & setters
}
```

#### VoucherUpdateRequest
```java
public class VoucherUpdateRequest {
    private BigDecimal discountAmount; // optional (partial update)
    private Integer requiredPoints;    // optional
    private BigDecimal minOrderAmount; // optional
    private List<Integer> applicableCategoryIds; // optional (null = keep)
    private List<Integer> applicableProductIds; // optional
    private Boolean vipOnly;          // optional
    private String status;            // optional
    private LocalDate expiresAt;      // optional
    private String adminNote;         // required if status changed to LOCKED/EXPIRED?
    // Getters & setters
}
```

#### BulkVoucherRequest
```java
public class BulkVoucherRequest {
    private List<Long> ids;           // required, max 100
    private String action;            // required: "LOCK", "UNLOCK", "DELETE", "SET_VIP"
    private Boolean value;            // required for SET_VIP (true/false)
    private String adminNote;         // optional
    // Getters & setters
}
```

#### BulkVoucherResponse
```java
public class BulkVoucherResponse {
    private int totalRequested;
    private int successCount;
    private int failureCount;
    private List<BulkResult> failures; // [{id: 1, error: "Voucher not found"}]
    // Getters & setters
}
```

### Response DTOs

#### VoucherResponse
```java
public class VoucherResponse {
    private Long id;
    private String code;
    private BigDecimal discountAmount;
    private Integer requiredPoints;
    private BigDecimal minOrderAmount;
    private List<Integer> applicableCategoryIds;
    private List<Integer> applicableProductIds;
    private Boolean vipOnly;
    private String status;
    private LocalDate expiresAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String createdBy;
    private String updatedBy;
    private Integer claimedCount;      // computed from UserVouchers
    // Getters & setters
}
```

**❓ Câu hỏi cho anh:**
> Response có cần include `usedCount` (số voucher đã dùng) không?
> - **Option A**: Chỉ `claimedCount`
> - **Option B**: Cả `claimedCount` và `usedCount`
>
> **Em đề xuất Option A** (đơn giản). Nếu cần phân tích thì Option B.

#### VoucherStatsResponse (optional endpoint)
```java
public class VoucherStatsResponse {
    private int totalVouchers;
    private int activeVouchers;
    private int lockedVouchers;
    private int expiredVouchers;
    private int totalClaimed;      // sum of claimedCount
    private int vipVouchers;
    private Map<String, Integer> byStatus;   // {"ACTIVE": 5, "LOCKED": 2}
    private Map<Boolean, Integer> byVip;     // {false: 6, true: 1}
    // Getters & setters
}
```

---

## 🔖 **Audit Log Format — JSON Structure**

### VoucherAuditLog Table Entry Example

**CREATE action:**
```json
{
  "voucher_id": 1,
  "admin_id": "admin_user",
  "action": "CREATE",
  "old_values": null,
  "new_values": {
    "code": "TX-ABC123",
    "discount_amount": 100000,
    "required_points": 10,
    "min_order_amount": 0,
    "vip_only": false,
    "status": "ACTIVE",
    "expires_at": null,
    "applicable_category_ids": null,
    "applicable_product_ids": null
  },
  "changed_fields": ["code", "discount_amount", "required_points", "status"],
  "note": "Tạo voucher mới",
  "created_at": "2026-06-24T10:30:00"
}
```

**UPDATE action:**
```json
{
  "voucher_id": 1,
  "admin_id": "admin_user",
  "action": "UPDATE",
  "old_values": {
    "discount_amount": 100000,
    "required_points": 10,
    "status": "ACTIVE"
  },
  "new_values": {
    "discount_amount": 200000,
    "required_points": 20,
    "status": "LOCKED"
  },
  "changed_fields": ["discount_amount", "required_points", "status"],
  "note": "Tăng discount cho event sale, khoá tạm do bug",
  "created_at": "2026-06-24T11:00:00"
}
```

**DELETE (soft) action:**
```json
{
  "voucher_id": 1,
  "admin_id": "admin_user",
  "action": "DELETE",
  "old_values": {
    "code": "TX-ABC123",
    "status": "ACTIVE",
    "claimed_count": 45
  },
  "new_values": {
    "status": "EXPIRED"
  },
  "changed_fields": ["status"],
  "note": "Chuyển EXPIRED vì đã có user claim",
  "created_at": "2026-06-24T12:00:00"
}
```

---

## ⚠️ **Edge Cases & Error Handling**

| Scenario | Handling | User Message |
|----------|----------|--------------|
| Edit voucher với `code` đã tồn tại khác ID | Reject 409 Conflict | "Mã voucher TX-XXX đã tồn tại" |
| Delete voucher có `claimedCount > 0` | Soft delete (EXPIRED) | "Voucher đã được X user sở hữu. Đã chuyển sang EXPIRED" |
| Bulk update voucher khác status (mix ACTIVE/LOCKED) | Apply to all, return failures | "4 thành công, 1 thất bại (voucher không tồn tại)" |
| Voucher `LOCKED` được apply ở checkout | Allow (backward compatible) | N/A (checkout vẫn works) |
| Search không tìm thấy | Show empty state | "Không tìm thấy voucher nào" |
| API 500 error | Toast error + log | "Lỗi server, vui lòng thử lại" |
| Concurrent edit (version mismatch) | Optimistic lock fail | "Dữ liệu đã thay đổi, vui lòng reload" |
| Expired catalog voucher | Filter out từ list? | "Voucher đã hết hạn (catalog)" |
| Min order < discount | Validation error | "Min order phải ≥ mệnh giá giảm" |

**Empty State UI:**
```
┌─────────────────────────────────────┐
│           (icon: package)           │
│   Chưa có voucher nào               │
│   Nhấn "THÊM VOUCHER MỚI" để tạo   │
└─────────────────────────────────────┘
```

---

## 🎛️ **Component State Management**

### Modal State
```javascript
const showModal = ref(false)           // boolean
const modalMode = ref('create')        // 'create' | 'edit'
const currentVoucherId = ref(null)     // null for create, ID for edit
const formData = ref({                 // reactive form data
  code: '',
  discountAmount: null,
  requiredPoints: null,
  minOrderAmount: 0,
  applicableCategoryIds: [],
  applicableProductIds: [],
  vipOnly: false,
  status: 'ACTIVE',
  expiresAt: null
})
const isLoading = ref(false)           // submit loading
const errors = ref({})                // validation errors
```

### Table State
```javascript
const vouchers = ref([])              // current page data
const total = ref(0)                  // total count
const currentPage = ref(1)
const itemsPerPage = ref(5)
const searchQuery = ref('')
const statusFilter = ref('all')       // 'all' | 'ACTIVE' | 'LOCKED' | 'EXPIRED'
const vipOnlyFilter = ref(false)
const selectedIds = ref([])           // bulk selection
const sortBy = ref('code')            // 'code' | 'discountAmount' | 'requiredPoints'
const sortOrder = ref('asc')          // 'asc' | 'desc'
```

### Modal Lifecycle
```javascript
const openCreateModal = () => {
  resetForm()
  modalMode.value = 'create'
  currentVoucherId.value = null
  showModal.value = true
}

const openEditModal = (voucher) => {
  resetForm()
  modalMode.value = 'edit'
  currentVoucherId.value = voucher.id
  // Populate form with voucher data
  formData.value = {
    code: voucher.code,
    discountAmount: voucher.discountAmount,
    requiredPoints: voucher.requiredPoints,
    minOrderAmount: voucher.minOrderAmount,
    applicableCategoryIds: voucher.applicableCategoryIds || [],
    applicableProductIds: voucher.applicableProductIds || [],
    vipOnly: voucher.vipOnly,
    status: voucher.status,
    expiresAt: voucher.expiresAt
  }
  showModal.value = true
}

const closeModal = () => {
  showModal.value = false
  resetForm()
}

const resetForm = () => {
  formData.value = {
    code: '',
    discountAmount: null,
    requiredPoints: null,
    minOrderAmount: 0,
    applicableCategoryIds: [],
    applicableProductIds: [],
    vipOnly: false,
    status: 'ACTIVE',
    expiresAt: null
  }
  errors.value = {}
}
```

---

## 📋 **Definition of Done (DoD) — Updated**

- [ ] UI hoàn chỉnh: table + search + filter + pagination + sort
- [ ] Bulk selection với checkbox header + row checkbox
- [ ] Bulk action buttons (Lock, Unlock, Delete, Set VIP) với confirmation
- [ ] Modal Add/Edit với validation đầy đủ (client + server errors)
- [ ] claimedCount column hiển thị đúng (COUNT from UserVouchers)
- [ ] Status badges với màu sắc chính xác
- [ ] Multi-select dropdown cho categories/products (có search)
- [ ] Code auto-generate (TX- + 6 random) + editable
- [ ] Min order ≥ discount validation
- [ ] VIP only toggle trong form + filter
- [ ] Expires at date picker
- [ ] Audit log integration (ghi CREATE/UPDATE/DELETE)
- [ ] API integration: CRUD + bulk operations
- [ ] Error handling: toast/notification cho mọi API error
- [ ] Loading states: skeleton/loading indicator
- [ ] Confirmation dialogs: delete single, bulk actions
- [ ] Sortable columns (code, discount, points, status)
- [ ] Empty states (no vouchers, no search results)
- [ ] Admin permission check (router meta)
- [ ] Responsive check (fixed width 1280px theo pattern)
- [ ] claimedCount refresh sau mỗi CRUD operation

---

## 🔗 **Related Files — Expanded**

| File | Purpose |
|------|---------|
| `dbTheXuong.sql` | Migration scripts cho TẤT CẢ bảng loyalty |
| `VoucherService.java` | Backend CRUD logic + audit logging |
| `VoucherController.java` | REST API endpoints |
| `VoucherRepository.java` | Data access + custom queries |
| `VoucherValidator.java` | Server-side validation rules |
| `VoucherMapper.java` | Entity ↔ DTO mapping |
| `AdminLayout.vue` | Layout wrapper (dùng shared) |
| `frontend-rules.md` | Vue 3 conventions |
| `frontend/src/services/voucherService.ts` | Frontend API client |
| `frontend/src/types/voucher.ts` | TypeScript interfaces |
| `frontend/src/components/VoucherModal.vue` | Add/Edit modal component |

---

## 🚨 **Open Questions — Remaining (Post-Decision)**

| # | Question | Status | Decision |
|---|----------|--------|----------|
| 1 | Code auto-generate + editable? | ✅ | Yes |
| 2 | Voucher scope (null = all)? | ✅ | Yes |
| 3 | Edit affect UserVouchers? | ✅ | No (snapshot) |
| 4 | Delete = soft (EXPIRED)? | ✅ | Yes |
| 5 | Catalog expiration? | ✅ | Admin-set expires_at |
| 6 | Form: modal popup? | ✅ | Yes |
| 7 | Bulk operations scope? | ✅ | Lock/Unlock/Delete/Set VIP |
| 8 | Audit log? | ✅ | Yes |
| 9 | Category/Product selector? | ✅ | Multi-select + search |
| 10 | claimedCount vs usedCount? | ⏳ | **PENDING** |
| 11 | Bulk delete confirmation? | ⏳ | **PENDING** (special warning?) |
| 12 | Multi-select component? | ⏳ | **PENDING** (A/B/C) |
| 13 | Code generation chars? | ⏳ | **PENDING** (29 chars đủ?) |
| 14 | Points rule strict? | ⏳ | **PENDING** (Option A/B/C) |
| 15 | Bulk action buttons display? | ⏳ | **PENDING** (4 buttons all?) |

### A. Business Logic (Quan trọng)

1. **Code generation**
   - Admin nhập code thủ công hay tự động `TX-` + 6 random (loại trừ `0/O/1/I/L` để tránh nhầm lẫn)?

2. **Voucher scope**
   - Admin tạo voucher → có áp dụng toàn global (có điều kiện) hay chỉ dành riêng cho 1 category/product?
   - Nếu `applicable_category_ids = null` → áp dụng cho tất cả sản phẩm?

3. **Editability**
   - Khi admin sửa `discount_amount` hoặc `required_points` → có ảnh hưởng đến `UserVouchers` đã claim trước đó?
   - ❌ **Recommendation**: Sửa catalog KHÔNG ảnh hưởng đến UserVouchers đã issue (snapshot values).

4. **Delete behavior**
   - Xóa voucher catalog → UserVouchers đang có còn dùng được không?
   - ❌ **Recommendation**: Chỉ cho xóa nếu `claimedCount = 0`, nếu có user đã claim → chuyển status=EXPIRED.

5. **Expiration**
   - Catalog voucher có `expires_at` (admin đặt) hay vĩnh viễn cho đến khi admin tắt?
   - UserVouchers có `expiresAt = issuedAt + 30 ngày` (đã chốt).

### B. UI/UX (Trực quan)

6. **Form placement**
   - Add/Edit form đặt ở đâu?
     - Option A: Form inline dưới table (như `AdminUsers.vue`)
     - Option B: Modal popup (click "THÊM VOUCHER" → mở modal)
     - Option C: Separate page `/admin/loyalty/vouchers/new`

7. **Applicable products/categories UI**
   - Multi-select dropdown với search? (dùng `<select multiple>` hay component riêng)
   - Nếu có nhiều category → search để chọn?

8. **Statistics display**
   - Hiển thị `claimedCount` (số user đã đổi voucher này)?
   - Hiển thị `usedCount` (số voucher đã dùng trong đơn hàng)?

9. **Bulk operations**
   - Cần bulk lock/unlock nhiều voucher cùng lúc không?
   - Checkbox row selection?

10. **Confirmation dialogs**
    - Xóa voucher: "Bạn có chắc chắn muốn xóa voucher TX-XXXXXX?"
    - Khi xóa mà có user đã claim: "Voucher này đã được [N] người dùng sở hữu. Xóa sẽ làm mất hiệu lực của tất cả voucher đã phát hành. Tiếp tục?"

### C. Data & Validation

11. **Code format validation**
    - `TX-` + 6 ký tự uppercase A-Z, loại trừ `0/O/1/I/L`
    - Client-side validation trước khi submit?

12. **Points economy check**
    - Khi tạo voucher, admin có thể set `required_points` tùy ý (ví dụ 1000 điểm) hay limit theo rule 10k=1pt?
    - ❓ **Recommendation**: Limit `required_points` ≤ 50 (tương đương 500k).

13. **Discount amount vs Required points consistency**
    - Enforce rule: `discount_amount / required_points ≈ 10000`?
    - Hay admin có thể override (ví dụ 50k discount cho 40 points)?

### D. Integration & Permissions

14. **Route protection**
    - Route `/admin/loyalty/vouchers` cần permission `ROLE_ADMIN` hoặc `ROLE_BOTH`?

15. **Real-time claimed count**
    - `claimedCount` lấy từ `COUNT(UserVouchers WHERE voucher_id = X)` mỗi lần load?
    - Hay cache trong Redis?

16. **API endpoints** (backend cần triển khai)
    ```
    GET    /api/admin/loyalty/vouchers               [ADMIN]
    POST   /api/admin/loyalty/vouchers               [ADMIN]
    GET    /api/admin/loyalty/vouchers/{id}          [ADMIN]
    PUT    /api/admin/loyalty/vouchers/{id}          [ADMIN]
    DELETE /api/admin/loyalty/vouchers/{id}          [ADMIN]
    GET    /api/admin/loyalty/vouchers/{id}/stats    [ADMIN] (optional)
    ```

### E. Edge Cases

17. **Voucher đang được dùng trong đơn hàng**
    - Nếu admin set `status = LOCKED` → các đơn đang checkout có dùng được voucher đó không?
    - ❓ **Recommendation**: LOCKED → không cho áp dụng VÀO MỚI, nhưng đơn đã apply trước đó vẫn valid.

18. **Audit trail**
    - Cần lưu lịch sử thay đổi voucher (ai sửa gì, khi nào)?
    - Table `VoucherAuditLog`: `id, voucher_id, admin_id, action, old_values, new_values, created_at`

---

## 📋 Definition of Done (DoD)

- [ ] UI hoàn chỉnh: table + search + filter + pagination
- [ ] Add/Edit form với validation đầy đủ
- [ ] API integration: CRUD operations với backend
- [ ] Error handling: toast/notification khi API fail
- [ ] Loading states: skeleton/loading indicator
- [ ] Confirmation dialog cho delete
- [ ] Status badges với màu sắc đúng
- [ ] Sortable columns (ít nhất: code, discount, points)
- [ ] Responsive check (đề xuất: giữ fixed width 1280px như pattern)
- [ ] Admin permission check (gate ở router)

---

## 🔗 Related Files

| File | Purpose |
|------|---------|
| `dbTheXuong.sql` | Table `Vouchers` definition |
| `VoucherService.java` | Backend CRUD logic |
| `VoucherController.java` | REST API endpoints |
| `AdminLayout.vue` | Layout wrapper (dùng shared) |
| `frontend-rules.md` | Vue 3 conventions |

---

## 📐 Mock Data (Theo pattern hiện tại)

```javascript
const mockVouchers = [
  {
    id: 1,
    code: 'TX-ABCD12',
    discountAmount: 100000,
    requiredPoints: 10,
    minOrderAmount: 0,
    applicableCategoryIds: [1, 2, 3],
    applicableProductIds: null,
    vipOnly: false,
    status: 'ACTIVE',
    createdAt: '2026-06-22T10:00:00',
    updatedAt: '2026-06-22T10:00:00',
    claimedCount: 45
  },
  {
    id: 2,
    code: 'TX-XYZ789',
    discountAmount: 500000,
    requiredPoints: 50,
    minOrderAmount: 2000000,
    applicableCategoryIds: null,
    applicableProductIds: [101, 205],
    vipOnly: true,
    status: 'ACTIVE',
    createdAt: '2026-06-21T14:30:00',
    updatedAt: '2026-06-21T14:30:00',
    claimedCount: 12
  },
  {
    id: 3,
    code: 'TX-DEF456',
    discountAmount: 20000,
    requiredPoints: 2,
    minOrderAmount: 100000,
    applicableCategoryIds: [5],
    applicableProductIds: null,
    vipOnly: false,
    status: 'LOCKED',
    createdAt: '2026-06-20T09:15:00',
    updatedAt: '2026-06-23T11:00:00',
    claimedCount: 89
  }
]
```

---

## 🎨 Wireframe (Text-based)

```
┌─────────────────────────────────────────────────────────────────────┐
│ DANH SÁCH VOUCHER                                                  │
│ Quản lý catalog voucher — 6 mệnh giá: 10k, 20k, 50k, 100k, 200k, 500k │
├─────────────────────────────────────────────────────────────────────┤
│ [Search: TX-           ]  [TẤT CẢ▐][ACTIVE][LOCKED][EXPIRED]        │
│                                                                     │
│ ┌─────────────────────────────────────────────────────────────────┐ │
│ │ MÃ VOUCHER  MỆNG GIÁ  ĐIỂM  MIN ORDER  VIP  TRẠNG THÁI  HÀNH ĐỘNG │ │
│ │ TX-ABCD12  100,000đ  10    0          Không  ACTIVE      [SỬA][XÓA]│ │
│ │ TX-XYZ789  500,000đ  50    2,000,000  Có    ACTIVE      [SỬA][XÓA]│ │
│ │ TX-DEF456   20,000đ   2    100,000    Không  LOCKED      [SỬA][XÓA]│ │
│ │ ... (pagination below)                                           │ │
│ └─────────────────────────────────────────────────────────────────┘ │
│                                                                     │
│ Hiển thị 1-5 của 6 kết quả               [<] [1] [2] [>]          │
│                                                                     │
│ ┌─────────────────────────────────────────────────────────────────┐ │
│ │ THÊM VOUCHER MỚI                                                │ │
│ ├─────────────────────────────────────────────────────────────────┤ │
│ │ Mã voucher:      [ TX-XXXXXX                    ]               │ │
│ │ Mệnh giá (đồng): [ 100000                      ]               │ │
│ │ Điểm cần:        [ 10                          ]               │ │
│ │ Min order (đồng):[ 0                            ]               │ │
│ │ VIP only:        [☑] Chỉ dành cho VIP                           │ │
│ │ Trạng thái:      [ ACTIVE ▼ ]                                    │ │
│ │                                                                  │ │
│ │                    [LƯU]        [LÀM MỚI]                        │ │
│ └─────────────────────────────────────────────────────────────────┘ │
└─────────────────────────────────────────────────────────────────────┘
```

---

## 🔄 Workflow Integration

### Batch 2 Context (Voucher Catalog)
- File này thuộc **Batch 2**: "Voucher catalog: `Vouchers`, `UserVouchers`, admin CRUD `/admin/loyalty/vouchers`"
- Batch 2 status: ✅ DONE 80% (backend done, frontend mock UI)
- **Cần hoàn thiện**: Real API integration, validation, error handling

### Tương tác với Batch 4 (Redeem)
- User xem catalog voucher ở `/loyalty/redeem` → filter chỉ `status = ACTIVE`
- User click "Đổi" → `POST /loyalty/redeem` → tạo `UserVouchers` row
- Checkout: user chọn `UserVouchers` → `POST /place-order` gửi `voucherCode`

---

## 🚨 Open Questions Summary

| # | Question | Category | Priority | Decision |
|---|----------|----------|----------|----------|
| 1 | Code auto-generate hay manual? | UX | High | ⏳ |
| 2 | Voucher scope (global vs category-specific)? | Business Logic | High | ⏳ |
| 3 | Edit affect existing UserVouchers? | Data | High | ⏳ |
| 4 | Delete soft vs hard? | Data | High | ⏳ |
| 5 | Catalog expiration needed? | Feature | Medium | ⏳ |
| 6 | Form: inline vs modal vs separate page? | UX | Medium | ⏳ |
| 7 | Bulk operations cần không? | UX | Low | ⏳ |
| 8 | Audit log cần không? | Compliance | Medium | ⏳ |
| 9 | Category/Product selector UI? | UX | Medium | ⏳ |
| 10 | Admin có thể xem claimedCount? | Feature | Low | ⏳ |

---

## 📌 Next Steps

1. **Anh confirm các question trên** → em mới code đúng expectation
2. Sau khi xác nhận → em triển khai:
   - Component `AdminVouchers.vue` (đổi tên từ suggestion)
   - API service `voucherService.ts` (nếu chưa có)
   - Router config: `{ path: '/admin/loyalty/vouchers', component: AdminVouchers, meta: { requiresAuth: true, roles: ['ADMIN', 'BOTH'] } }`
   - Integration test: create/edit/delete voucher end-to-end

**⚠️ Lưu ý:** Theo quy tắc báo cáo, mỗi task xong phải output **Task Report** theo template đã định. Nếu FAIL → KHÔNG tự retry lần 3 mà chờ anh.

---

**Prepared by:** Claude Opus 4.8  
**For:** Anh (Project Architect)
