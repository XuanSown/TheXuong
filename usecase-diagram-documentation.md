# TheXuong E-commerce - Use Case Documentation

## PlantUML Diagram File
**File:** `usecase-diagram.puml`

This file contains a comprehensive use case diagram for the TheXuong project with proper actor classification:
- **Customer** (Guest/Unauthorized + Registered User)
- **Admin** (System Administrator)
- **System** (Background processes, external integrations)
- **Both** (Planned features for Batch 1-5 - Loyalty/Voucher)

---

## Actors Overview

| Actor | Description | Primary Features |
|-------|-------------|------------------|
| **Customer** (Guest) | Users not logged in | Browse products, view details |
| **Registered User** | Authenticated users | Full shopping + loyalty features |
| **Admin** | System administrators | All management features |
| **System** | Automated processes | Cron jobs, callbacks, emails |
| **Google OAuth2** | External auth provider | Login/Register |
| **VNPay Gateway** | Payment processor | Payment handling |
| **Email Server** | Notification system | Transactional emails |

---

## Use Case Categories

### 1. CUSTOMER USE CASES (Guest)

| Use Case | Description | Page/Endpoint |
|----------|-------------|---------------|
| Browse Products | Search, filter by sport/brand, sort by price | `/products` |
| View Product Detail | See product info, variants, stock | `/product-detail/{id}` |
| View Homepage | See featured/new products | `/`, `/index` |

---

### 2. CUSTOMER USE CASES (Registered User)

#### Authentication & Profile
| Use Case | Description | Page/Endpoint |
|----------|-------------|---------------|
| Register Account | Sign up with email/password or Google | `/register` |
| Login | Google OAuth2 login | `/login` |
| Logout | Clear session/JWT | `/logout` |
| View Profile | See account info | `/profile` |
| Update Profile | Edit name, address | `/profile/update` |
| Change Password | Update local account password | `/profile/update` |
| Forgot Password | Reset via email token | `/forgot-password` |

#### Shopping Workflow
| Use Case | Description | Page/Endpoint |
|----------|-------------|---------------|
| View Shopping Cart | See cart items, quantities, total | `/cart` (GET) |
| Add Item to Cart | Add product variant with size/quantity | `/cart/add` (POST) |
| Update Cart Quantity | Change item quantities | `/cart` (POST via form) |
| Remove from Cart | Delete cart item | `/cart/remove/{id}` |

#### Order Management
| Use Case | Description | Page/Endpoint |
|----------|-------------|---------------|
| Checkout | Enter shipping info, select payment | `/checkout` |
| Place Order | Create order, reserve stock | `/place-order` (POST) |
| Make Payment | VNPay redirect or COD selection | `VNPay gateway` / N/A |
| View My Orders | List all user orders | `/orders` |
| View Order Detail | See order items, status, timeline | `/order/{id}` |
| Cancel Order | Cancel PENDING orders, restore stock | `/order/cancel` (POST) |
| Confirm Received | Mark DELIVERED → COMPLETED, earn points | `/order/{id}/confirm-received` (POST) |

#### Communication
| Use Case | Description | Endpoint |
|----------|-------------|----------|
| Chat with Admin | Real-time WebSocket messaging | `/ws`, `/api/chat/online-users` |

#### Loyalty (Batch 1-5 - Planned)
| Use Case | Description | Endpoint |
|----------|-------------|----------|
| View Points Balance | See current points, earned/spent | `/loyalty` (planned) |
| View Points History | Transaction history | `/loyalty` (planned) |
| Redeem Voucher | Exchange points for discount codes | `/loyalty/redeem` (planned) |
| Apply Voucher at Checkout | Use voucher code for discount | `/checkout` (planned extension) |
| View My Vouchers | List unused/used/expired vouchers | `/my-vouchers` (planned) |

---

### 3. ADMIN USE CASES

#### Dashboard & Reports
| Use Case | Description | Page/Endpoint |
|----------|-------------|---------------|
| Admin Dashboard | Overview with quick stats | `/admin/statistics` |
| View Statistics | Top selling, revenue, inventory, user segments | `/admin/statistics` |

#### Product Management
| Use Case | Description | Page/Endpoint |
|----------|-------------|---------------|
| Manage Products | List all products | `/admin/products` |
| Create Product | Add new product with variants | `/admin/products/create` |
| Edit Product | Modify product details, variant stocks | `/admin/products/edit/{id}` |
| Delete Product | Remove product (if no orders) | `/admin/products/delete/{id}` |

#### Order Management
| Use Case | Description | Page/Endpoint |
|----------|-------------|---------------|
| View All Orders | Complete order list with filters | `/admin/orders` |
| Update Order Status | Change status via state machine | `/admin/orders/save` (info) <br> `/admin/orders/status/{id}` (status) |
| Delete Order | Remove order record | `/admin/orders/delete/{id}` |

#### User Management
| Use Case | Description | Page/Endpoint |
|----------|-------------|---------------|
| View All Users | User list with roles/role-groups | `/admin/users` |
| Create User | Add local user or Google user | `/admin/users/save` (POST, new) |
| Edit User | Update info, roles, role-groups | `/admin/users/save` (POST, edit) |
| Toggle Active | Enable/disable account (blocks login) | `/admin/users/toggle-active/{id}` |
| Delete User | Remove user (if no orders/reviews) | `/admin/users/delete/{id}` |

#### Role & Permission Management
| Use Case | Description | Endpoint |
|----------|-------------|----------|
| Manage Role Groups | Create/edit role groups with permissions | RoleGroupRestController (REST API) |
| Assign Roles to Users | Add/remove individual role permissions | `/admin/users/save` |

#### Loyalty Management (Batch 2-5 - Planned)
| Use Case | Description | Endpoint |
|----------|-------------|----------|
| Configure Loyalty Settings | Set points rate, tier thresholds | `/admin/loyalty/config` (planned) |
| Manage Voucher Catalog | CRUD voucher types (denominations) | `/admin/loyalty/vouchers` (planned) |
| View Loyalty Reports | Points issued/spent/expired, tier distribution | `/admin/loyalty/report` (planned) |

---

### 4. SYSTEM USE CASES (Automated)

#### Payment Processing
| Use Case | Trigger | Action |
|----------|---------|--------|
| Handle VNPay Callback | VNPay POST to `/vnpay-return` | Verify signature, update order to CONFIRMED, set `paid_at` |

#### Email Notifications
| Use Case | Trigger | Email Type |
|----------|---------|------------|
| Send Order Confirmation | Order placed | "Your order #X is confirmed" |
| Send Payment Success | VNPay success | "Payment received" |
| Send Shipping Notice | Order → SHIPPING | "Your order is on the way" |
| Send Points Earned | Order → COMPLETED | "You earned X points" |
| Send Voucher Redeemed | Points → Voucher | "Your voucher TX-XXX" |
| Send Voucher Expiring | 3 days before expiry | "Voucher expires soon" |
| Send Tier Upgrade | VIP promotion | "Welcome to VIP!" |
| Send VIP Downgrade | Tier evaluation | "Tier status changed" |
| Send Password Reset | Forgot password flow | "Your new password" |

#### Cron Jobs (Scheduled)
| Job | Schedule | Action |
|-----|----------|--------|
| PointExpireJob | Daily 00:00 | Expire EARN transactions > 1 year, create EXPIRE records |
| VoucherExpireJob | Daily 00:30 | Set unused vouchers past expiry to EXPIRED |
| VoucherExpiringSoonJob | Daily 09:00 | Send email warnings for vouchers expiring in 3 days |
| TierReevaluateJob | Monthly 1st | Re-evaluate VIP users (365-day window), downgrade if ineligible |
| TierWarningJob | Daily 09:00 | Email VIP users approaching reevaluation date (within 30 days) |

---

## Order State Machine

```
┌─────────┐
│ PENDING │ ← Place Order (before payment)
└────┬────┘
     │ ✓ CONFIRMED
     │   (VNPay success / Admin confirms)
     ▼
┌──────────┐
│CONFIRMED │ ← Payment received, awaiting fulfillment
└────┬─────┘
     │ ✓ SHIPPING
     │   (Admin marks shipped)
     ▼
┌─────────┐
│SHIPPING │ ← Order in transit
└────┬────┘
     │ ✓ DELIVERED
     │   (Shipping company delivered)
     ▼
┌────────────┐
│ DELIVERED  │ ← Awaiting customer confirmation
└────┬───────┘
     │ ✓ COMPLETED ✓
     │   (User confirms received) → Trigger: Earn points!
     ▼
┌────────────┐
│ COMPLETED  │ ← Terminal state ✓
└────────────┘

ANY STATUS ──✗──► CANCELLED
(PENDING/CONFIRMED only) ← Cancel before fulfillment

CONFIRMED/SHIPPING/DELIVERED ──✗──► REFUNDED
  (Admin processes refund) ← Trigger: Reverse points!
```

**Valid Transitions:**
```
PENDING → {CONFIRMED, CANCELLED}
CONFIRMED → {SHIPPING, CANCELLED, REFUNDED}
SHIPPING → {DELIVERED, REFUNDED}
DELIVERED → {COMPLETED, REFUNDED}
COMPLETED → (none - terminal)
CANCELLED → (none - terminal)
REFUNDED → (none - terminal)
```

---

## Entity Relationships (Key)

```
User 1 ── N Order
User 1 ── 1 Cart
User 1 ── N CartItem (via Cart)
User 1 ── N Order
User 1 ── N RoleGroup (M:N via user_role_groups)
User 1 ── N Role (M:N via user_roles)
User 1 ── 1 UserPoints (planned Batch 1)
User 1 ── N UserVoucher (planned Batch 2)
User 1 ── N PointTransaction (planned Batch 1)

Order 1 ── N OrderDetail
Order N ── 0..1 Voucher (planned Batch 3)
Order 1 ── N OrderEvent (planned Batch 4)

Product 1 ── N ProductVariant

Cart 1 ── N CartItem
CartItem N ── 1 ProductVariant
ProductVariant N ── 1 Product
ProductVariant N ── 1 Size
```

---

## Permission Matrix

| Feature | Customer | Admin | Notes |
|---------|----------|-------|-------|
| Browse products | ✅ | ✅ | Public access |
| View product detail | ✅ | ✅ | Public access |
| Add to cart | ✅ | ❌ | Cart belongs to user |
| View/edit cart | ✅ | ❌ | Personal cart only |
| Checkout/Place order | ✅ | ❌ | Users buy for themselves |
| View own orders | ✅ (own only) | ✅ (all) | Admin sees all |
| Cancel order | ✅ (own PENDING) | ✅ (any) | Admin can cancel any |
| Update order status | ❌ | ✅ | Admin only via state machine |
| Confirm received | ✅ (own DELIVERED) | ❌ | Customer action only |
| View profile | ✅ (own) | ✅ (all) | Admin can view/edit any |
| Edit profile | ✅ (own) | ✅ (any) | Admin can edit any user |
| Manage products | ❌ | ✅ | Full CRUD |
| Manage users | ❌ | ✅ | Full CRUD, toggle active |
| Manage role groups | ❌ | ✅ | RBAC configuration |
| View statistics | ❌ | ✅ | Dashboard + reports |
| Chat | ✅ | ✅ | Both can chat |
| View points | ✅ (own) | ✅ (all) | Planned Batch 1-2 |
| Redeem voucher | ✅ (own) | ❌ | Customer only |
| Manage voucher catalog | ❌ | ✅ | Admin CRUD, Batch 2 |
| Loyalty reports | ❌ | ✅ | Admin only, Batch 5 |

---

## Technology Stack Mapping

| Layer | Technology |
|-------|------------|
| **Backend Framework** | Spring Boot 3.5.9 |
| **Java Version** | JDK 21 |
| **Database** | SQL Server |
| **ORM** | JPA/Hibernate |
| **Security** | Spring Security + OAuth2 (Google) |
| **Frontend (Current)** | Thymeleaf + Bootstrap 5 |
| **Frontend (Planned)** | Vue 3 + TypeScript + Tailwind |
| **Payment** | VNPay |
| **Email** | Spring JavaMailSender |
| **Real-time** | WebSocket (STOMP) |
| **Build Tool** | Gradle |
| **Authentication** | JWT tokens |
| **Authorization** | RBAC (Roles + RoleGroups) |

---

## API Endpoints Summary

### Public/Protected by Authentication

| Path | Method | Purpose | Auth Required |
|------|--------|---------|--------------|
| `/` | GET | Homepage | No |
| `/products` | GET | Product catalog | No |
| `/product-detail/{id}` | GET | Product details | No |
| `/login` | GET/POST | Login page/handler | No |
| `/register` | GET/POST | Registration | No |
| `/forgot-password` | GET/POST | Password reset | No |
| `/cart` | GET | View cart | Yes |
| `/cart/add` | POST | Add to cart | Yes |
| `/cart/remove/{id}` | GET | Remove item | Yes |
| `/checkout` | GET | Checkout page | Yes |
| `/place-order` | POST | Create order | Yes |
| `/orders` | GET | My orders | Yes |
| `/order/{id}` | GET | Order detail | Yes (owner) |
| `/order/cancel` | POST | Cancel order | Yes (owner) |
| `/order/{id}/confirm-received` | POST | Confirm delivery | Yes (owner) |
| `/vnpay-return` | GET | VNPay callback | No (IPN) |
| `/profile` | GET/POST | User profile | Yes |
| `/subscribe` | POST | Email subscription | No |
| `/admin/**` | Various | Admin features | Yes (ADMIN role) |

### REST APIs (for Vue future migration)

| Path | Method | Purpose |
|------|--------|---------|
| `/api/auth/login` | POST | JWT login |
| `/api/chat/online-users` | GET | List online users |
| `/api/chat/**` | Various | Chat messaging |
| `/api/role-groups/**` | Various | Role group CRUD (REST) |

---

## Implementation Status

### ✅ Completed (Batch 0)
- OrderStatus enum with 7 states and state machine
- Order entity refactored to use enum
- JPA AttributeConverter for backward compatibility
- VNPay bug fixed (CONFIRMED instead of PENDING)
- Database migration (timestamp columns added)
- Thymeleaf templates updated for 7 statuses
- Admin order status routed through state machine

### ⏳ Planned (Batch 1-5)

| Batch | Focus | Tasks | Status |
|-------|-------|-------|--------|
| 1 | Loyalty Core | 19 | Pending |
| 2 | Voucher Catalog & Redemption | 21 | Pending |
| 3 | Apply Voucher at Checkout | 16 | Pending |
| 4 | Tier Upgrade + Cron | 25 | Pending |
| 5 | Cron Expire + Emails + Reports | 18 | Pending |
| 6 | Cleanup & Documentation | 5 | Pending |

**Total:** 109 tasks remaining

---

## Key Business Rules

### Order Rules
1. Stock is deducted when order created (PENDING)
2. Stock returned when order cancelled (before CONFIRMED)
3. Only PENDING orders can be cancelled by customer
4. Admin can force status changes but must follow state machine
5. Order total = subtotal - discount + shipping
6. One voucher per order maximum

### Loyalty Rules (Planned)
1. Earn 1 point per 100,000 VNĐ spent (completed orders only)
2. Points snapshot at order creation (discounts don't affect earning)
3. Tier based on 365-day rolling window (spent + points earned)
4. Vouchers expire 30 days after redemption
5. Points expire 1 year after earn date
6. FIFO expiration: oldest points expire first

### Payment Rules
1. VNPay success → CONFIRMED status, set `paid_at`
2. COD → stays PENDING until staff updates
3. Refund only after CONFIRMED, triggers point reversal

---

## Database Schema Highlights

### Core Tables
- **Users** - User accounts, tier_code (planned)
- **Roles** - Individual permissions (USER, ADMIN, etc.)
- **RoleGroups** - Permission bundles
- **user_roles** - M:N User ↔ Role
- **user_role_groups** - M:N User ↔ RoleGroup

- **Products** - Product catalog
- **ProductVariants** - Size/color combinations with SKU and stock
- **Sizes** - Available sizes (S, M, L, XL or numeric for shoes)
- **Categories** (implied)

- **Orders** - Order headers with 7 status enum, timestamps
- **OrderDetails** - Order line items (snapshot product data)
- **OrderEvents** (planned) - Audit log for status changes

- **Cart** - User's current shopping cart
- **CartItem** - Items in cart → ProductVariant

### Loyalty Tables (Batch 1-5)
- **UserPoints** - Current balance, total earned/spent, version (optimistic lock)
- **PointTransactions** - EARN/SPEND/REVERSE/EXPIRE/ADJUST records
- **PointTiers** - Tier definitions (THUONG, VIP thresholds)
- **Vouchers** - Voucher catalog (discount, required points, conditions)
- **UserVouchers** - User's vouchers (UNUSED/USED/EXPIRED)
- **tier_evaluation_log** (planned) - Tier change history

---

## How to Generate Diagram

1. Install PlantUML (or use online editor at https://www.plantuml.com/plantuml)
2. Open `usecase-diagram.puml`
3. Render to PNG/SVG

Or use command line:
```bash
plantuml usecase-diagram.puml
```

---

## References

- Main README: `README.md`
- Implementation Plan: `orderstatus.md`
- Use Case Structure: `usecase-structure.md`
- Database Schema: `dbTheXuong.sql`
- Source Code: `src/main/java/com/example/thexuong/`

---

**Last Updated:** 2026-06-23  
**Project:** TheXuong Sport Apparel E-commerce  
**Framework:** Spring Boot 3.5.9, JDK 21, SQL Server
