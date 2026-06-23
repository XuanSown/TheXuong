# Cấu Trúc Use Case - TheXuong E-commerce

## 1. Tổng Quan Dự Án

**Tên dự án:** TheXuong - Sport Apparel E-commerce  
**Mục tiêu:** Nền tảng thương mại điện tử chuyên biệt cho sản phẩm thể thao  
**Công nghệ:** Spring Boot 3.5.9, JDK 21, SQL Server, Thymeleaf/Bootstrap 5 (hiện tại), Vue 3 (tương lai)

---

## 2. Actor Định Nghĩa

### 2.1 Primary Actors
| Actor | Mô tả |
|-------|-------|
| **Customer** (Người dùng chưa đăng nhập) | Truy cập public pages, xem sản phẩm |
| **Registered User** (Người dùng đã đăng nhập) | User đã xác thực, có đầy đủ quyền mua hàng |
| **Admin** (Quản trị viên) | Quản lý toàn bộ hệ thống: sản phẩm, đơn hàng, người dùng |
| **Staff** (Nhân viên) | Xử lý đơn hàng, quản lý kho (nhỏ hơn Admin) |
| **System** (Hệ thống) | Cron jobs, email notifications, payment callbacks |

### 2.2 Secondary Actors
| Actor | Mô tả |
|-------|-------|
| **Google OAuth2** | Xác thực đăng nhập |
| **VNPay** | Cổng thanh toán |
| **Email Server** | Gửi email thông báo |
| **WebSocket** | Real-time chat |

---

## 3. Use Case Hierarchy

### 3.1 Level 1: Main Features (Tính năng chính)

```
TheXuong E-commerce System
├── UC-01: Authentication & Authorization
├── UC-02: Product Catalog Management
├── UC-03: Shopping Cart
├── UC-04: Order Management
├── UC-05: Payment Processing
├── UC-06: Loyalty & Rewards (Planned Batch 1-5)
├── UC-07: Admin Management
├── UC-08: User Profile
├── UC-09: Chat & Support
└── UC-10: System Maintenance
```

---

## 4. Chi Tiết Use Case

### UC-01: Authentication & Authorization

#### UC-01.1: Register/Login via Google OAuth2
- **Actor:** Customer, Registered User
- **Trigger:** User click "Login with Google"
- **Preconditions:** Google OAuth2 credentials configured
- **Main Flow:**
  1. User click "Login with Google"
  2. Redirect to Google OAuth consent screen
  3. User authorize TheXuong app
  4. Google redirects back with authorization code
  5. System exchanges code for access token
  6. System fetches user profile (email, name, avatar)
  7. System creates new User record if first time
  8. System generates JWT token
  9. System redirects to homepage/dashboard
- **Postconditions:** User authenticated, JWT token stored
- **Extensions:**
  - 1a. User cancels OAuth → Redirect back to login page
  - 5a. Token exchange fails → Show error message
- **Special Requirements:** HTTPS required, token expiration configurable

#### UC-01.2: Logout
- **Actor:** Registered User
- **Main Flow:** Clear JWT token/cookie, redirect to home

---

### UC-02: Product Catalog Management

#### UC-02.1: Browse Products
- **Actor:** Customer, Registered User
- **Main Flow:**
  1. User visits homepage or /products
  2. System displays product grid (image, name, price, rating)
  3. User can filter by category, sort by price/popularity
  4. User can search products by name
- **Extensions:** Pagination for >20 products

#### UC-02.2: View Product Detail
- **Actor:** Customer, Registered User
- **Main Flow:**
  1. User clicks product card
  2. System shows detail page with:
     - Multiple images
     - Product description
     - Available sizes
     - Available colors
     - Stock quantity
     - Reviews/ratings
     - Related products
- **Preconditions:** Product ID valid

#### UC-02.3: Admin Create Product
- **Actor:** Admin
- **Main Flow:**
  1. Admin navigates to /admin/products/new
  2. Admin fills product form:
     - Name, description, category
     - Base price
     - Upload images
     - Set sizes (S/M/L/XL)
     - Set colors
  3. Admin clicks Save
  4. System validates and creates Product + ProductVariants
- **Postconditions:** Product visible on public catalog

#### UC-02.4: Admin Update Product
- **Actor:** Admin
- **Main Flow:** Similar to create, but updates existing product

#### UC-02.5: Admin Manage Product Variants
- **Actor:** Admin
- **Main Flow:**
  1. Admin selects product
  2. Admin can add/remove size/color combinations
  3. Admin can set stock quantity per variant
  4. System updates ProductVariant table

---

### UC-03: Shopping Cart

#### UC-03.1: Add to Cart
- **Actor:** Registered User
- **Main Flow:**
  1. User on product detail page selects size, color, quantity
  2. User clicks "Add to Cart"
  3. System checks stock availability
  4. System creates CartItem in user's Cart
  5. System shows confirmation toast
- **Extensions:**
  - 3a. Item exists in cart → Update quantity instead of create new
  - 3b. Insufficient stock → Show error "Only X items available"

#### UC-03.2: View Cart
- **Actor:** Registered User
- **Main Flow:**
  1. User navigates to /cart
  2. System displays:
     - Cart items (product image, name, variant, quantity, price)
     - Subtotal per item
     - Editable quantity
     - Remove item button
     - Cart summary (subtotal, shipping fee, total)
- **Postconditions:** Cart persisted in database

#### UC-03.3: Update Cart Quantity
- **Actor:** Registered User
- **Main Flow:**
  1. User changes quantity input
  2. System validates against stock
  3. System updates CartItem quantity
  4. System recalculates cart total

#### UC-03.4: Remove from Cart
- **Actor:** Registered User
- **Main Flow:** User clicks remove → System deletes CartItem

---

### UC-04: Order Management

#### UC-04.1: Place Order (Checkout)
- **Actor:** Registered User
- **Trigger:** User clicks "Place Order" on checkout page
- **Main Flow:**
  1. User on /checkout page reviews cart items
  2. User enters shipping info (name, phone, address)
  3. User selects payment method (VNPay/Cod)
  4. User optionally enters voucher code or uses points
  5. System calculates:
     - Subtotal (sum of cart items)
     - Discount (from voucher/points)
     - Shipping fee
     - Total
  6. User confirms order
  7. System:
     - Creates Order record (status = PENDING)
     - Creates OrderDetail for each cart item
     - Deducts stock from ProductVariant
     - Clears cart
  8. System redirects to payment gateway (if VNPay) or shows order confirmation
- **Postconditions:** Order created, stock reserved

#### UC-04.2: View Order History
- **Actor:** Registered User
- **Main Flow:**
  1. User navigates to /my-orders
  2. System displays list of user's orders with:
     - Order ID, date, status badge
     - Total amount
     - Payment method
  3. User can filter by status
  4. User can click to view order detail

#### UC-04.3: View Order Detail
- **Actor:** Registered User
- **Main Flow:** System shows full order info + item list + shipping + payment details

#### UC-04.4: Cancel Order
- **Actor:** Registered User
- **Preconditions:** Order status = PENDING (not yet CONFIRMED)
- **Main Flow:**
  1. User on order detail page clicks "Cancel Order"
  2. System confirms cancellation
  3. System updates Order status to CANCELLED
  4. System returns stock to ProductVariant
  5. If voucher used → marks voucher as UNUSED again
  6. If points were used → refunds points

#### UC-04.5: Confirm Received Delivery
- **Actor:** Registered User
- **Preconditions:** Order status = DELIVERED
- **Main Flow:**
  1. User on order detail clicks "Confirm Received"
  2. System updates Order status to COMPLETED
  3. System sets `completed_at` timestamp
  4. **Loyalty Hook:** System adds loyalty points based on order total
  5. System updates user tier if eligible
  6. System sends email notification
- **Postconditions:** Order completed, points earned

---

### UC-05: Payment Processing

#### UC-05.1: VNPay Payment
- **Actor:** Registered User, System (VNPay callback)
- **Main Flow:**
  1. User selects VNPay on checkout
  2. System redirects to VNPay gateway with order info
  3. User completes payment on VNPay site
  4. VNPay sends POST to `/vnpay-return` callback
  5. System verifies VNPay signature
  6. System checks VNPay response code (success/fail)
  7. System updates Order:
     - If success: status = CONFIRMED, set `paid_at`
     - If fail: status = CANCELLED
  8. System redirects user to order result page
- **Extensions:**
  - 6a. Invalid signature → Log error, show generic message
  - 7a. Voucher in order → mark UserVoucher as USED

#### UC-05.2: Cash on Delivery (COD)
- **Actor:** Registered User, Admin/Staff
- **Main Flow:**
  1. User selects COD on checkout
  2. Order created with status = PENDING
  3. Staff delivers order
  4. Staff updates order status → SHIPPING → DELIVERED
  5. User confirms received → COMPLETED
  6. Staff collects cash payment

---

### UC-06: Loyalty & Rewards System (Batch 1-5)

#### UC-06.1: Earn Points (Batch 1)
- **Actor:** System (triggered on order COMPLETED)
- **Main Flow:**
  1. Order status changes to COMPLETED
  2. System calculates points = floor(order.total_for_point_calc / 100,000)
  3. System adds EARN transaction to PointTransactions
  4. System updates UserPoints.current_points
  5. System updates UserPoints.total_earned
  6. System records transaction timestamp
- **Business Rules:** Min 100k spent to earn 1 point

#### UC-06.2: View Points Balance (Batch 2)
- **Actor:** Registered User
- **Main Flow:**
  1. User navigates to /loyalty
  2. System displays:
     - Current points balance
     - Total earned
     - Total spent
     - Current tier badge
     - Points history table
  3. System shows progress to next tier (if applicable)

#### UC-06.3: Redeem Voucher (Batch 2)
- **Actor:** Registered User
- **Preconditions:** User has enough points
- **Main Flow:**
  1. User navigates to /loyalty/redeem
  2. System shows voucher catalog (6 denominations: 10k/1pt, 20k/2pt, 50k/5pt, 100k/10pt, 200k/20pt, 500k/50pt)
  3. User clicks "Redeem" on desired voucher
  4. System:
     - Checks user has sufficient points
     - Locks UserPoints row (optimistic lock)
     - Deducts points (SPEND transaction)
     - Generates unique voucher code (format: TX-XXXXXX)
     - Creates UserVoucher record with 30-day expiry
     - Sends email notification
  5. System redirects to /my-vouchers
- **Extensions:**
  - 3a. Insufficient points → Show error
  - 3b. Race condition (2 users redeem same catalog) → Optimistic lock retry

#### UC-06.4: Apply Voucher at Checkout (Batch 3)
- **Actor:** Registered User
- **Main Flow:**
  1. User on checkout page enters voucher code OR selects from "My Vouchers" dropdown
  2. System calls `validateAndApplyVoucher()`:
     - Check voucher exists and belongs to user
     - Check voucher status = UNUSED
     - Check not expired
     - Check order total >= min_order_amount
     - Check applicable categories/products
  3. If valid → System shows discount preview
  4. Order placed → voucher marked USED, stored in orders.voucher_code
- **Business Rules:** One voucher per order only

#### UC-06.5: Tier Evaluation & Upgrade (Batch 4)
- **Actor:** System (cron job + on order complete)
- **Main Flow (On-order-complete):**
  1. Order COMPLETED → OrderService triggers tier evaluation
  2. System calculates:
     - Total spent in last 365 days (completed orders)
     - Total points earned in last 365 days
  3. System matches against PointTier thresholds
  4. If new tier different from current:
     - Updates Users.tier_code
     - Sets Users.tier_promoted_at
     - Creates OrderEvent record
     - Sends VIP welcome email (if VIP)
- **Cron Re-evaluation (Monthly):**
  1. System runs on 1st of each month
  2. System queries VIP users promoted >365 days ago
  3. System re-evaluates each user
  4. If no longer eligible → downgrade to THUONG
  5. System sends downgrade notification email

#### UC-06.6: Points Expiration (Batch 5)
- **Actor:** System (cron job daily 00:00)
- **Main Flow:**
  1. Cron job runs `PointExpireJob`
  2. System queries EARN transactions where:
     - expires_at < current date
     - No corresponding REVERSE or EXPIRE transaction exists
  3. For each expired point:
     - Creates EXPIRE transaction (negative points)
     - Decrements UserPoints.current_points
  4. System logs expired points count

---

### UC-07: Admin Management

#### UC-07.1: Admin Dashboard
- **Actor:** Admin
- **Main Flow:** System shows overview stats:
  - Total orders today/week/month
  - Revenue
  - Top selling products
  - New customers

#### UC-07.2: Admin Manage Orders
- **Actor:** Admin
- **Main Flow:**
  1. Admin navigates to /admin/orders
  2. System shows filterable order list (by status, date, customer)
  3. Admin clicks order → shows detail
  4. Admin can update order status (PENDING → CONFIRMED → SHIPPING → DELIVERED → COMPLETED)
  5. System validates state transitions (via OrderStatus.canTransitionTo)
  6. System records OrderEvent for audit trail

#### UC-07.3: Admin Manage Products
- **Actor:** Admin
- **Main Flow:** See UC-02.3, UC-02.4, UC-02.5

#### UC-07.4: Admin Manage Users
- **Actor:** Admin
- **Main Flow:**
  1. Admin views user list with filters
  2. Admin can:
     - View user details (orders, points)
     - Deactivate/reactivate user
     - Reset password (send email)
     - Change user role
  3. System prevents self-deactivation

#### UC-07.5: Admin Manage Role Groups (RBAC)
- **Actor:** Admin
- **Main Flow:**
  1. Admin creates RoleGroup (e.g., "Customer Service", "Warehouse")
  2. Admin assigns permissions/roles to group
  3. Admin assigns users to groups

#### UC-07.6: Admin Loyalty Configuration (Batch 4)
- **Actor:** Admin
- **Main Flow:**
  1. Admin navigates to /admin/loyalty/config
  2. System shows configurable settings:
     - Points earning rate (per 100k spent)
     - Tier thresholds (THUONG → VIP min spent/min points)
     - Voucher denominations
  3. Admin updates values → System persists to database

#### UC-07.7: Admin Voucher Management (Batch 2)
- **Actor:** Admin
- **Main Flow:**
  1. Admin navigates to /admin/loyalty/vouchers
  2. System shows catalog of voucher types:
     - Discount amount
     - Required points
     - Min order amount
     - Validity period
     - Applicable categories
  3. Admin can CRUD voucher catalog entries

#### UC-07.8: Admin Loyalty Reports (Batch 5)
- **Actor:** Admin
- **Main Flow:**
  1. Admin navigates to /admin/loyalty/report
  2. System displays:
     - Total points issued/redemed/expired
     - Top 10 users by points balance
     - Top 10 users by spending
     - Tier distribution chart
     - Voucher usage statistics

---

### UC-08: User Profile

#### UC-08.1: View Profile
- **Actor:** Registered User
- **Main Flow:** System shows user info (name, email, avatar from Google, loyalty tier)

#### UC-08.2: Update Profile Info
- **Actor:** Registered User
- **Main Flow:**
  1. User edits display name
  2. System updates User entity
  3. System updates related records if needed

#### UC-08.3: Manage Saved Addresses
- **Actor:** Registered User
- **Main Flow:**
  1. User views saved addresses from previous orders
  2. User can select address for checkout
  3. User can add/delete addresses

---

### UC-09: Chat & Support

#### UC-09.1: Real-time Chat with Admin
- **Actor:** Registered User, Admin/Staff
- **Main Flow:**
  1. User clicks chat icon
  2. WebSocket connection established
  3. User sends message → Admin receives in real-time
  4. Admin replies → User receives in real-time
  5. Chat history persisted to database

---

### UC-10: System Maintenance

#### UC-10.1: Email Notifications
- **Actor:** System
- **Triggers:**
  - Order placed → "Order confirmation"
  - Order shipped → "Shipping notification"
  - Order delivered → "Delivery reminder"
  - Points earned → "Points earned" email
  - Voucher redeemed → "Voucher code" email
  - Voucher expiring soon (3 days) → "Expiry warning"
  - Tier upgrade/downgrade → "Tier change" email
  - Password reset → "Reset password" email

#### UC-10.2: Cron Jobs
- **Actor:** System
- **Jobs:**
  - `PointExpireJob` (daily 00:00) - Expire old points
  - `VoucherExpireJob` (daily 00:30) - Expire unused vouchers
  - `VoucherExpiringSoonJob` (daily 09:00) - Send expiry warnings
  - `TierReevaluateJob` (monthly 1st) - Downgrade ineligible VIP users
  - `TierWarningJob` (daily 09:00) - Warn VIP users approaching reevaluation

#### UC-10.3: Error Handling & Logging
- **Actor:** System
- **Main Flow:**
  - GlobalExceptionHandler catches all exceptions
  - Returns appropriate HTTP status and error message
  - Logs error with stack trace

---

## 5. Use Case Diagram (Textual)

```
┌─────────────────────────────────────────────────────────────┐
│                      THEXUONG SYSTEM                        │
├─────────────────────────────────────────────────────────────┤
│                                                             │
│  ┌────────────┐     ┌──────────────┐   ┌────────────┐   │
│  │  Customer  │     │ Registered   │   │   Admin    │   │
│  │ (Chưa đăng)│     │    User      │   │            │   │
│  └─────┬──────┘     └──────┬───────┘   └─────┬──────┘   │
│        │                   │                  │          │
│        │   UC-01.1: Login  │                  │          │
│        │◄──────────────────┼──────────────────┼─────────┤
│        │                   │                  │          │
│        │                   │   UC-07: Admin Mgmt           │
│        │                   │◄────────────────────────────┤
│        │                   │                  │          │
│        │   UC-02: Products │                  │          │
│        │◄──────────────────┼──────────────────┼─────────┤
│        │                   │                  │          │
│        │   UC-03: Cart     │                  │          │
│        │◄──────────────────┼──────────────────┼─────────┤
│        │                   │                  │          │
│        │   UC-04: Orders   │                  │          │
│        │◄──────────────────┼──────────────────┼─────────┤
│        │                   │                  │          │
│        │   UC-05: Payment  │                  │          │
│        │◄──────────────────┼──────────────────┼─────────┤
│        │                   │                  │          │
│        │   UC-08: Profile  │                  │          │
│        │◄──────────────────┼──────────────────┼─────────┤
│        │                   │                  │          │
│        │   UC-09: Chat     │                  │          │
│        │◄──────────────────┼──────────────────┼─────────┤
│        │                   │                  │          │
│        │   UC-06: Loyalty  │                  │          │
│        │◄──────────────────┼──────────────────┼─────────┤
│        │                   │                  │          │
│  ┌─────▼──────┐     ┌──────▼───────┐   ┌────▼─────┐    │
│  │   Google   │     │    VNPay      │   │  System  │    │
│  │   OAuth2   │     │  (Payment)    │   │  (Cron)  │    │
│  └────────────┘     └──────────────┘   └───────────┘    │
│                                                             │
└─────────────────────────────────────────────────────────────┘
```

---

## 6. Data Models (Entity Relationships)

```
User (1) ─── (N) Order
User (1) ─── (1) UserPoints
User (1) ─── (N) UserVoucher
User (1) ─── (N) Cart
User (N) ─── (N) RoleGroup (through UserRoleGroup)

Order (1) ─── (N) OrderDetail
Order (N) ─── (1) Voucher (optional)
Order (1) ─── (N) OrderEvent

Product (1) ─── (N) ProductVariant
Product (N) ─── (N) Category

Cart (1) ─── (N) CartItem
CartItem (N) ─── (1) ProductVariant

UserPoints (1) ─── (N) PointTransaction

Voucher (N) ─── (N) User (through UserVoucher)
Voucher (1) ─── (N) UserVoucher
```

---

## 7. API Endpoints Structure

### 7.1 Web Controllers (Thymeleaf - HTML)
| Path | Purpose |
|------|---------|
| `/` | Homepage - Product listing |
| `/products` | Product catalog |
| `/product/{id}` | Product detail |
| `/cart` | Shopping cart page |
| `/checkout` | Checkout page |
| `/my-orders` | User's order history |
| `/order/{id}` | Order detail |
| `/loyalty` | Loyalty points page (Batch 2) |
| `/loyalty/redeem` | Voucher redemption (Batch 2) |
| `/my-vouchers` | User's vouchers (Batch 2) |
| `/admin/orders` | Admin order management |
| `/admin/products` | Admin product management |
| `/admin/users` | Admin user management |
| `/admin/loyalty/config` | Loyalty config (Batch 4) |
| `/admin/loyalty/vouchers` | Voucher catalog (Batch 2) |
| `/admin/loyalty/report` | Loyalty reports (Batch 5) |

### 7.2 REST Controllers (JSON API)
| Path | Method | Purpose |
|------|--------|---------|
| `/api/auth/login` | POST | JWT login (alternative to OAuth) |
| `/api/cart/*` | Various | Cart operations |
| `/api/orders/*` | Various | Order operations |
| `/api/loyalty/*` | Various | Loyalty endpoints |
| `/api/admin/*` | Various | Admin APIs |

---

## 8. State Machine - OrderStatus

```
         ┌─────────┐
         │ PENDING │  (chờ thanh toán)
         └────┬────┘
              │ ✓ CONFIRMED (thanh toán thành công)
              ▼
         ┌─────────┐
         │CONFIRMED│  (đã thanh toán, chờ xử lý)
         └────┬────┘
              │ ✓ SHIPPING (đang giao)
              ▼
         ┌─────────┐
         │ SHIPPING│  (đang giao hàng)
         └────┬────┘
              │ ✓ DELIVERED (đã giao, chờ xác nhận)
              ▼
         ┌────────────┐
         │  DELIVERED │ (chờ user nhận hàng)
         └────┬───────┘
              │ ✓ COMPLETED (user xác nhận → EARN POINTS)
              ▼
         ┌────────────┐
         │ COMPLETED  │ (hoàn tất - trạng thái cuối)
         └────────────┘

    ANY STATUS ──✗──► CANCELLED (huỷ trước khi CONFIRMED)
                    │
                    ▼
                CANCELLED

    CONFIRMED/SHIPPING/DELIVERED ──✗──► REFUNDED (hoàn tiền → REVERSE POINTS)
                                        │
                                        ▼
                                    REFUNDED
```

**Valid Transitions:**
- PENDING → CONFIRMED | CANCELLED
- CONFIRMED → SHIPPING | CANCELLED | REFUNDED
- SHIPPING → DELIVERED | REFUNDED
- DELIVERED → COMPLETED | REFUNDED
- COMPLETED, CANCELLED, REFUNDED → Terminal (no transitions)

---

## 9. Business Rules Summary

### 9.1 Order Rules
- Order total = subtotal - discount + shipping_fee
- Stock deducted when order created (status = PENDING)
- Stock returned when order cancelled (before CONFIRMED)
- Voucher usage: 1 voucher per order only

### 9.2 Loyalty Rules
- Earn rate: 1 point per 100,000 VNĐ spent (completed orders only)
- Points snapshot: `total_for_point_calc` saved at order creation (discounts don't affect)
- Tier evaluation: Based on total spent + points earned in last 365 days
- Points expire: After 1 year from earn date (configurable)
- Voucher expiration: 30 days from redemption

### 9.3 Payment Rules
- VNPay: Order must be CONFIRMED on successful payment
- COD: Order remains PENDING until staff updates
- Refund: Only possible after CONFIRMED, reverses points

### 9.4 Admin Rules
- Admin can force status change (still validates state machine)
- Self-deactivation prevented
- Tier config only editable by super-admin

---

## 10. Implementation Batches (Current Plan)

| Batch | Focus | Status | Tasks |
|-------|-------|--------|-------|
| 0 | OrderStatus enum + state machine + VNPay fix | ✅ Done | 15 |
| 1 | Loyalty Core (UserPoints, PointTransaction) | ⏳ Pending | 19 |
| 2 | Voucher Catalog & Redemption | ⏳ Pending | 21 |
| 3 | Apply Voucher at Checkout | ⏳ Pending | 16 |
| 4 | Tier Upgrade + Cron Re-evaluate | ⏳ Pending | 25 |
| 5 | Cron Expire + Emails + Reports | ⏳ Pending | 18 |
| 6 | Cleanup & Documentation | ⏳ Pending | 5 |

**Total:** 109 tasks across 6 batches

---

## 11. Current Implementation Status

### ✅ Completed (Batch 0)
- OrderStatus enum with 7 states
- State machine validation (canTransitionTo)
- Order entity refactored to use enum
- JPA AttributeConverter for backward compatibility
- VNPay bug fixed (set CONFIRMED instead of PENDING)
- Database migration (timestamps columns added)
- Thymeleaf templates updated
- Admin order status update routed through service

### ⏳ To Implement (Batch 1-5)
See detailed task breakdown in `orderstatus.md`

---

## 12. Testing Strategy

### Unit Tests
- `OrderStatus.canTransitionTo()` - all valid/invalid combinations
- `PointService.earnPoints()`, `spendPoints()`, `reversePoints()`
- `VoucherService.validateAndApplyVoucher()`
- Race condition tests with @Version optimistic locking

### Integration Tests
- Checkout flow with voucher
- Order lifecycle PENDING → COMPLETED
- VNPay callback handling

### Manual E2E Tests
1. Register via Google
2. Browse products, add to cart
3. Apply voucher, place order
4. VNPay sandbox payment
5. Admin ships order
6. User confirms received → points earned
7. Check loyalty page shows points
8. Redeem voucher with points

---

## 13. Non-Functional Requirements

| Requirement | Implementation |
|-------------|----------------|
| **Performance** | Database indexes on foreign keys, pagination (20 items/page) |
| **Security** | JWT tokens, role-based access control, SQL injection prevention (JPA), XSS prevention (Thymeleaf) |
| **Availability** | SQL Server, Spring Boot embedded Tomcat |
| **Scalability** | Stateless JWT auth, horizontal scaling possible |
| **Auditability** | OrderEvent table for all status changes |
| **Data Integrity** | Optimistic locking on UserPoints, foreign key constraints |

---

## 14. Glossary

| Term | Definition |
|------|------------|
| **OrderStatus** | Enum đóng gói 7 trạng thái đơn hàng với state machine validation |
| **Loyalty Points** | Điểm thưởng earned từ completed orders, có thể redeem voucher |
| **Tier** | Hạng thành viên (THUONG, VIP) dựa trên chi tiêu/điểm |
| **Voucher** | Mã giảm giá được đổi từ points, áp dụng tại checkout |
| **ProductVariant** | Biến thể sản phẩm (kết hợp size + color) với stock riêng |
| **CONFIRMED** | Trạng thái đơn hàng sau khi thanh toán thành công |
| **COMPLETED** | Trạng thái cuối sau khi user xác nhận nhận hàng (trigger earn points) |

---

## 15. Future Enhancements (Post-Batch 6)

1. **Vue 3 Frontend Migration** - Convert Thymeleaf pages to Vue SPA
2. **Advanced Analytics** - Sales dashboard with charts
3. **Inventory Management** - Low stock alerts, bulk import
4. **Multiple Payment Gateways** - Momo, ZaloPay, Banking
5. **Push Notifications** - Browser notifications for order updates
6. **Recommendation Engine** - "You may also like" based on history
7. **Multi-language Support** - English/Vietnamese
8. **Mobile App** - React Native wrapper

---

**Tài liệu tham khảo:**
- `orderstatus.md` - Chi tiết implementation plan Batch 0-6
- `README.md` - Project overview, tech stack, setup guide
- `dbTheXuong.sql` - Database schema

**Last Updated:** 2026-06-23
