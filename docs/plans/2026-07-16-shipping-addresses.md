# Shipping Addresses + Auto-detect Location — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` to implement task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking. Update the **Progress Table** below as you complete each task.
>
> **Quyết định kiến trúc (lựa chọn A — ponytail):** Xóa cột `Users.address` (1 string legacy), Orders KHÔNG đụng (0 schema change). Chỉ thêm 1 bảng mới `UserAddresses` (sổ địa chỉ multi). `Orders.address` vẫn là text snapshot, được FE format từ `UserAddresses` rồi gửi lên khi đặt hàng (BE `placeOrder` không đổi signature). 1 nguồn sự thật cho địa chỉ = `UserAddresses`.

**Goal:** Cho phép user lưu nhiều địa chỉ giao hàng (structured: Tỉnh/Quận/Phường + số đường, có tên/SĐT người nhận riêng, có địa chỉ mặc định), tự động xác định vị trí qua nút "Dùng vị trí của tôi" (Google Maps reverse-geocode), và chọn địa chỉ đã lưu khi thanh toán (auto-sync tên/SĐT/địa chỉ vào form, user vẫn sửa được).

**Architecture:** Cách B — Dropdown Tỉnh→Quận→Phường cascading từ JSON tĩnh VN (bundle vào FE, 0 API key) + Google Maps cho phần "ma thuật": Autocomplete ô số nhà/đường (restrict VN) + reverse-geocode lat/lng → match code tỉnh/quận/phường gần nhất (proxy qua BE, giấu API key server-side). Mỗi địa chỉ lưu `recipientName`/`recipientPhone` riêng. Tại checkout chọn 1 địa chỉ từ sổ → FE format text qua `vn-regions.formatAddress()` → prefill vào form (user vẫn sửa) → gửi payload text như cũ. `Orders` giữ nguyên (text snapshot, 0 cột mới). Chưa làm phí ship (`shippingFee` vẫn = 0).

**Tech Stack:**
- BE: Spring Boot 3.5.9 / Java 21 / Spring Data JPA + Hibernate / SQL Server (MSSQL) / Gradle. Validation: Jakarta Bean Validation (`@Valid`). Auth: session-based. **Không có migration tool** — schema quản thủ công qua `dbTheXuong.sql` (gitignored, trên disk) + chạy `ALTER`/`CREATE`/`DROP` trực tiếp trên DB.
- FE: Vue 3.5 + Vite 8 + Pinia + Vue Router + Tailwind 3.4 + vee-validate/zod + vue-toastification. Maps: `@googlemaps/js-api-loader` (dynamic import, ESM) + `@types/google.maps`. HTTPS qua Cloudflare Tunnel → `navigator.geolocation` hoạt động ở dev (localhost) + prod.
- Maps API key: 2 key riêng (Google Console): (1) FE-restricted HTTP referrer (`thexuong.xuansown.id.vn` + `localhost`) cho Autocomplete, (2) BE server-restricted cho reverse-geocode qua proxy `/api/v1/maps/**`.

## Global Constraints

- **API URL:** FE dùng relative path `/api/v1` (Vite proxy → `localhost:8080`). KHÔNG hardcode `http://localhost:8080` (AGENTS.md).
- **Naming:** Hibernate `PhysicalNamingStrategyStandardImpl` → tên cột giữ nguyên, map qua `@Column(name=...)`. String → `NVARCHAR` (`use_nationalized_character_data=true`).
- **DB:** `spring.jpa.hibernate.ddl-auto=none` → Hibernate KHÔNG tự tạo/sửa schema. Phải chạy SQL tay trên SQL Server (`localhost:1444`) + cập nhật `dbTheXuong.sql` (file gitignored, giữ trên disk làm tham chiếu).
- **Validation:** Jakarta Bean Validation, message tiếng Việt, regex SĐT `^0[0-9][0-9]{9}$` (10 số) hoặc `^0[0-9]{9,10}$` (10-11 số, theo pattern hiện tại của repo).
- **FE env:** `VITE_API_URL=/api/v1`. Thêm `VITE_GOOGLE_MAPS_API_KEY=` (FE-restricted key, để trống trong file commit, set khi deploy).
- **Ponytail:** Shortest working diff. Reuse `BaseModal.vue` (đang chết), `BaseInput.vue`, `BaseButton.vue`, `vue-toastification`. Không tạo `BaseSelect`/`BaseTextarea` mới (`<select>` native + `<textarea>` inline Tailwind đã đủ). Không tạo factory/interface 1-impl.
- **Backward-compat orders:** `PlaceOrderRequest` không đổi (vẫn `{ fullName, phoneNumber, address: string, paymentMethod, voucherCode, pointsToUse }`). FE điền text từ địa chỉ đã chọn. BE `OrderService.placeOrder` không đổi signature.
- **Commits:** Mỗi task = 1 commit. Convention: `feat:`, `fix:`, `chore:`, `docs:`.

---

## 📊 Progress Table

Cập nhật cột **Status** thành `[x]` khi hoàn thành + ghi chú. Đồng bộ với `todowrite` của session implement.

| # | Phase | Task | Files (key) | Status | Notes |
|---|-------|------|-------------|--------|-------|
| 0.1 | Data | Bundle VN administrative JSON + helpers | `frontend/src/data/vn-administrative.json`, `frontend/src/utils/vn-regions.ts` | `[x]` | 63 tỉnh/705 quận/10599 phường (vietnam-provinces dump). Đổi `nearestWard`→`matchByGoogleComponents` (match by name, không cần lat/lng) |
| 1.1 | DB | Bảng `UserAddresses` + `ALTER Users DROP COLUMN address` | `dbTheXuong.sql` | `[x]` | File SQL cập nhật (CREATE Users bỏ address, thêm bảng UserAddresses). **Cần chạy tay `ALTER TABLE Users DROP COLUMN address` trên DB existing** |
| 2.1 | BE | Entity `UserAddress` + Repository + `User.addresses` | `entity/UserAddress.java`, `repository/UserAddressRepository.java`, `entity/User.java` | `[x]` | Order entity KHÔNG đổi. `compileJava` PASS |
| 2.2 | BE | DTOs `AddressRequest`/`AddressResponse` + mở rộng `UserResponse` (bỏ `address`, thêm `addresses`) + sửa controllers | `dto/...`, `AuthRestController.java`, `CheckoutRestController.java` | `[x]` | `UpdateProfileRequest` bỏ field address. Sửa cả `AdminUserRestController`. `compileJava` PASS |
| 2.3 | BE | `AddressService` (CRUD + default + ownership) | `service/AddressService.java` | `[x]` | `compileJava` PASS |
| 2.4 | BE | `AddressRestController` `/api/v1/addresses` | `controller/AddressRestController.java` | `[x]` | `compileJava` PASS |
| 2.5 | BE | Google Maps proxy `MapsRestController` + `MapsService` | `controller/MapsRestController.java`, `service/MapsService.java`, `application.yml` | `[x]` | Key server-side qua env `GOOGLE_MAPS_API_KEY`. **Chưa set key — endpoint sẽ throw nếu gọi** |
| 2.6 | BE | `SecurityConfig` thêm `/addresses/**`, `/maps/**` | `config/SecurityConfig.java` | `[x]` | `compileJava` PASS |
| 3.1 | FE | Types `Address` + mở rộng `User` (bỏ `address`, thêm `addresses`) | `frontend/src/types/auth.types.ts` | `[x]` | Order type KHÔNG đổi. `type-check` PASS |
| 3.2 | FE | API service addresses + maps | `frontend/src/services/api.ts` | `[x]` | `type-check` PASS |
| 3.3 | FE | `address.store.ts` (Pinia) | `frontend/src/stores/address.store.ts` | `[x]` | `type-check` PASS |
| 3.4 | FE | Composable `useGoogleMaps` (loader + Autocomplete) | `frontend/src/composables/useGoogleMaps.ts`, `frontend/package.json` | `[x]` | `@googlemaps/js-api-loader` + `@types/google.maps` cài. **Đã gỡ khỏi AddressForm (user không có Google billing)** — composable giữ lại harmless |
| 3.5 | FE | Component `AddressForm.vue` (3 dropdown + street input + note "đang phát triển") | `frontend/src/components/address/AddressForm.vue` | `[x]` | **Thay đổi quan trọng:** Bỏ Google Autocomplete + nút định vị thật. 3 dropdown cascading hoạt động (0 API key). Nút "Dùng vị trí của tôi" → toast.info "đang phát triển". Note italic "Gợi ý địa chỉ & định vị tự động đang được phát triển" |
| 3.6 | FE | Profile.vue — sổ địa chỉ (list + CRUD modal + default), bỏ textarea address | `frontend/src/views/Profile.vue` | `[x]` | `type-check` + `build` PASS |
| 3.7 | FE | Checkout.vue — picker địa chỉ + auto-sync vào form (payload text, không addressId) | `frontend/src/views/Checkout.vue` | `[x]` | Prefill default address. `type-check` + `build` PASS |
| 3.8 | FE | OrderDetail.vue — verify hiển thị (order.address text vẫn hoạt động) | `frontend/src/views/OrderDetail.vue` | `[x]` | `order.address` (line 77) — text snapshot, OK không cần sửa |
| 4.1 | Verify | type-check + lint + build + manual E2E checklist | — | `[x]` | `type-check` ✅ + `build` ✅ (3.25s) + `compileJava` ✅. **Lint: eslint chưa cài trong project (pre-existing). E2E manual: CHƯA CHẠY (cần chạy SQL tay + bootRun + dev trước)** |

---

## File Structure

### Tạo mới
- `frontend/src/data/vn-administrative.json` — dataset Tỉnh/Quận/Phường VN (contract dưới).
- `frontend/src/utils/vn-regions.ts` — helpers đọc dataset (cascading + nearest-ward match + formatAddress).
- `frontend/src/composables/useGoogleMaps.ts` — lazy-load Google Maps JS API (places library, Autocomplete).
- `frontend/src/stores/address.store.ts` — Pinia store cho sổ địa chỉ.
- `frontend/src/components/address/AddressForm.vue` — form structured address + nút vị trí.
- `src/main/java/com/example/thexuong/entity/UserAddress.java` — entity.
- `src/main/java/com/example/thexuong/repository/UserAddressRepository.java` — repo.
- `src/main/java/com/example/thexuong/dto/address/AddressRequest.java`, `AddressResponse.java` — DTOs.
- `src/main/java/com/example/thexuong/service/AddressService.java` — CRUD + default.
- `src/main/java/com/example/thexuong/controller/AddressRestController.java` — REST.
- `src/main/java/com/example/thexuong/service/MapsService.java` — Google Maps client.
- `src/main/java/com/example/thexuong/controller/MapsRestController.java` — proxy endpoint.

### Sửa
- `dbTheXuong.sql` — thêm bảng `UserAddresses` + `ALTER TABLE Users DROP COLUMN address`.
- `src/main/java/.../entity/User.java` — **xóa** field `address`, thêm `@OneToMany List<UserAddress> addresses` (lazy).
- `src/main/java/.../dto/UserResponse.java` — **xóa** field `address`, thêm `List<AddressResponse> addresses`.
- `src/main/java/.../dto/auth/UpdateProfileRequest.java` — **xóa** field `address` + `@NotBlank`.
- `src/main/java/.../service/UserService.java` — `updateProfile` bỏ param/setter `address` (2 overload).
- `src/main/java/.../controller/AuthRestController.java` — `toUserResponse` bỏ `.address()`, thêm `.addresses(...)`; `updateProfile` (`:195-201`) bỏ `request.getAddress()`.
- `src/main/java/.../controller/CheckoutRestController.java` (`:152-158`) — bỏ `.address(user.getAddress())` (cột đã xóa).
- `src/main/java/.../controller/AdminUserRestController.java` — kiểm tra chỗ nào gọi `updateProfile(...,address,...)` với address không null → truyền null (cột đã xóa).
- `src/main/java/.../config/SecurityConfig.java` — thêm rules `/addresses/**`, `/maps/**`.
- `src/main/resources/application.yml` — thêm `app.google.maps.api-key`, `app.google.maps.language`, `app.google.maps.region`.
- `frontend/.env` / `.env.production` — thêm `VITE_GOOGLE_MAPS_API_KEY=`.
- `frontend/package.json` — thêm `@googlemaps/js-api-loader` + devDep `@types/google.maps`.
- `frontend/src/types/auth.types.ts` — **xóa** `address?: string` trong `User`, thêm `Address` interface + `addresses?: Address[]`.
- `frontend/src/services/api.ts` — thêm methods addresses + maps; `updateProfile` bỏ field `address`.
- `frontend/src/stores/auth.store.ts` — `updateProfile` bỏ field `address` trong type.
- `frontend/src/views/Profile.vue` — **xóa** block textarea "ĐỊA CHỈ NHẬN HÀNG" + `profileForm.address`, thay bằng sổ địa chỉ (list + CRUD modal).
- `frontend/src/views/Checkout.vue` — thêm picker địa chỉ + auto-sync vào form; prefill từ `addressStore.defaultAddress` thay vì `authStore.user.address`.
- `frontend/src/utils/validators.ts` — kiểm tra `checkoutSchema` field `address` (vẫn giữ, FE format text gửi lên).

### KHÔNG đụng (lựa chọn A)
- `entity/Order.java` — KHÔNG thêm `addressId`/`lat`/`lng`. Giữ nguyên.
- `dto/order/PlaceOrderRequest.java` — KHÔNG đổi (vẫn text `address`).
- `service/OrderService.java` — `placeOrder` KHÔNG đổi signature.
- `controller/OrderRestController.java` — KHÔNG đổi.
- `dbTheXuong.sql` phần `Orders` — KHÔNG ALTER.
- View `VW_OrderSummary` — KHÔNG sửa (vẫn select `o.address` text).

---

## Contract — VN Administrative JSON

`frontend/src/data/vn-administrative.json` (commit vào repo, lazy-load, gzip ~150KB). Pin 1 snapshot từ dataset maintained (vd `vietnam-provinces` npm `getProvincesWithDetail()` dump ra, hoặc github raw đã verify). Contract:

```jsonc
[
  {
    "code": "01",                 // string, mã Tổng cục TK
    "name": "Hà Nội",             // tên ngắn (không có loại)
    "nameWithType": "Thành phố Hà Nội",
    "latitude": 21.0285,          // centroid tỉnh
    "longitude": 105.8542,
    "districts": [
      {
        "code": "001",
        "name": "Ba Đình",
        "nameWithType": "Quận Ba Đình",
        "latitude": 21.0416,
        "longitude": 105.8197,
        "wards": [
          { "code": "00001", "name": "Phúc Xá", "nameWithType": "Phường Phúc Xá", "latitude": 21.05, "longitude": 105.85 },
          { "code": "00002", "name": "Trúc Bạch", "nameWithType": "Phường Trúc Bạch", "latitude": 21.05, "longitude": 105.84 }
        ]
      }
    ]
  }
]
```

> Nếu dataset nguồn thiếu centroid lat/lng cho ward → Task 0.1 chỉ require centroid ở cấp **tỉnh + quận** (đủ match reverse-geocode tới cấp quận, user tự chọn phường). Tốt nhất có centroid ward.

---

## Task 0.1: Bundle VN administrative JSON + helpers

**Files:**
- Create: `frontend/src/data/vn-administrative.json`
- Create: `frontend/src/utils/vn-regions.ts`

**Interfaces (Produces):**
- `getProvinces(): Region[]` — `{ code, name, nameWithType }`.
- `getDistricts(provinceCode: string): Region[]`.
- `getWards(provinceCode: string, districtCode: string): Region[]`.
- `findProvince(code)`, `findDistrict(provinceCode, code)`, `findWard(provinceCode, districtCode, code)` — return `Region | null`.
- `formatAddress({ streetDetail, wardCode, districtCode, provinceCode }): string` — ghép "số nhà, Phường X, Quận Y, Tỉnh Z" (dùng `nameWithType`).
- `nearestWard(lat, lng): { provinceCode, districtCode, wardCode | null }` — match centroid gần nhất (Haversine) cho reverse-geocode fallback.

- [ ] **Step 1:** Tạo `frontend/src/data/vn-administrative.json` với contract trên. Pin snapshot, không fetch runtime.
- [ ] **Step 2:** Viết `frontend/src/utils/vn-regions.ts`:
```ts
import data from '@/data/vn-administrative.json'

export interface Region { code: string; name: string; nameWithType: string; latitude?: number; longitude?: number }
export interface ProvinceNode extends Region { districts: DistrictNode[] }
export interface DistrictNode extends Region { wards: Region[] }

const provinces = data as ProvinceNode[]

export const getProvinces = (): Region[] => provinces.map(({ districts, ...r }) => r)
export const getDistricts = (pc: string): Region[] => provinces.find(p => p.code === pc)?.districts.map(({ wards, ...r }) => r) ?? []
export const getWards = (pc: string, dc: string): Region[] => provinces.find(p => p.code === pc)?.districts.find(d => d.code === dc)?.wards ?? []
export const findProvince = (c: string) => provinces.find(p => p.code === c) ?? null
export const findDistrict = (pc: string, c: string) => findProvince(pc)?.districts.find(d => d.code === c) ?? null
export const findWard = (pc: string, dc: string, c: string) => findDistrict(pc, dc)?.wards.find(w => w.code === c) ?? null

export function formatAddress(a: { streetDetail?: string; wardCode?: string; districtCode?: string; provinceCode?: string }): string {
  const parts: string[] = []
  if (a.streetDetail) parts.push(a.streetDetail.trim())
  const w = a.wardCode && findWard(a.provinceCode!, a.districtCode!, a.wardCode); if (w) parts.push(w.nameWithType)
  const d = a.districtCode && findDistrict(a.provinceCode!, a.districtCode); if (d) parts.push(d.nameWithType)
  const p = a.provinceCode && findProvince(a.provinceCode); if (p) parts.push(p.nameWithType)
  return parts.filter(Boolean).join(', ')
}

function haversine(lat1: number, lng1: number, lat2: number, lng2: number): number {
  const R = 6371, toRad = (d: number) => d * Math.PI / 180
  const dLat = toRad(lat2 - lat1), dLng = toRad(lng2 - lng1)
  const a = Math.sin(dLat/2)**2 + Math.cos(toRad(lat1)) * Math.cos(toRad(lat2)) * Math.sin(dLng/2)**2
  return R * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a))
}

// ponytail: O(provinces×districts) scan, fine for ~700 districts; spatial index if throughput matters
export function nearestWard(lat: number, lng: number): { provinceCode: string; districtCode: string; wardCode: string | null } {
  let best = { provinceCode: '', districtCode: '', wardCode: null as string | null, dist: Infinity }
  for (const p of provinces) {
    for (const d of p.districts) {
      if (d.latitude == null) continue
      const dist = haversine(lat, lng, d.latitude, d.longitude)
      if (dist < best.dist) {
        let wardCode: string | null = null
        if (d.wards.length) {
          let wd = Infinity
          for (const w of d.wards) { if (w.latitude == null) continue; const wdist = haversine(lat, lng, w.latitude, w.longitude); if (wdist < wd) { wd = wdist; wardCode = w.code } }
        }
        best = { provinceCode: p.code, districtCode: d.code, wardCode, dist }
      }
    }
  }
  return { provinceCode: best.provinceCode, districtCode: best.districtCode, wardCode: best.wardCode }
}
```
- [ ] **Step 3:** Verify type-check: `cd frontend && npm run type-check` → PASS.
- [ ] **Step 4:** Commit: `git add frontend/src/data/vn-administrative.json frontend/src/utils/vn-regions.ts` → `feat: thêm dataset + helpers địa chỉ hành chính VN`.

---

## Task 1.1: Bảng `UserAddresses` + DROP `Users.address` (DB)

**Files:** Modify `dbTheXuong.sql` (chạy tay trên MSSQL `localhost:1444`).

- [ ] **Step 1:** Thêm vào `dbTheXuong.sql` (sau bảng `Users`):
```sql
CREATE TABLE UserAddresses (
    id              BIGINT IDENTITY(1,1) PRIMARY KEY,
    user_id         BIGINT NOT NULL,
    label           NVARCHAR(50),              -- 'Nhà', 'Công ty', ...
    recipient_name  NVARCHAR(255) NOT NULL,
    recipient_phone NVARCHAR(20)  NOT NULL,
    province_code   NVARCHAR(5)   NOT NULL,
    district_code   NVARCHAR(5)   NOT NULL,
    ward_code       NVARCHAR(5)   NOT NULL,
    street_detail   NVARCHAR(255),
    latitude        DECIMAL(10,7),
    longitude       DECIMAL(10,7),
    is_default      BIT           DEFAULT 0,
    created_at      DATETIME2     DEFAULT GETDATE(),
    updated_at      DATETIME2     DEFAULT GETDATE(),
    CONSTRAINT FK_UserAddresses_Users FOREIGN KEY (user_id) REFERENCES Users(id) ON DELETE CASCADE,
    CONSTRAINT CHK_UserAddresses_phone CHECK (recipient_phone LIKE '0[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]%')
);
CREATE INDEX IX_UserAddresses_user ON UserAddresses(user_id);
```
> **Default per user:** KHÔNG dùng partial unique index (MSSQL cồng kềnh). Thực thi "chỉ 1 default" trong `AddressService.setDefault` (transaction: set tất cả address của user `is_default=0` rồi set target `is_default=1`).
- [ ] **Step 2:** Xóa cột `address` khỏi `Users`:
```sql
ALTER TABLE Users DROP COLUMN address;
```
> Lưu ý: `Orders.address` (snapshot text) KHÔNG bị ảnh hưởng — là cột riêng trên bảng Orders. View `VW_OrderSummary` select `o.address` (Orders) → vẫn OK.
- [ ] **Step 3:** Chạy SQL tay trên SQL Server (`localhost:1444`) — tạo bảng + DROP cột.
- [ ] **Step 4:** Verify: `SELECT TOP 1 * FROM UserAddresses` (bảng tồn tại) + `SELECT * FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_NAME='Users' AND COLUMN_NAME='address'` (trả 0 row = cột đã xóa).
- [ ] **Step 5:** Cập nhật `dbTheXuong.sql` trên disk (xóa dòng `address NVARCHAR(MAX),` khỏi phần CREATE TABLE Users — giữ cho lịch sử nếu muốn, hoặc comment). Commit: `feat(db): bảng UserAddresses + xóa cột Users.address`. (File gitignored — commit có thể skip nhưng giữ file đồng bộ.)

---

## Task 2.1: Entity `UserAddress` + Repository + `User.addresses`

**Files:**
- Create: `src/main/java/com/example/thexuong/entity/UserAddress.java`
- Create: `src/main/java/com/example/thexuong/repository/UserAddressRepository.java`
- Modify: `src/main/java/com/example/thexuong/entity/User.java` (xóa field `address`, thêm `@OneToMany addresses`)

- [ ] **Step 1:** Tạo `UserAddress.java` (theo pattern `User.java` — Lombok `@Getter/@Setter/@Builder`, `@NoArgsConstructor/@AllArgsConstructor`):
```java
package com.example.thexuong.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "UserAddresses")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class UserAddress {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "label", length = 50)
    private String label;

    @Column(name = "recipient_name", columnDefinition = "NVARCHAR(255)", nullable = false)
    private String recipientName;

    @Column(name = "recipient_phone", nullable = false)
    private String recipientPhone;

    @Column(name = "province_code", length = 5, nullable = false)
    private String provinceCode;

    @Column(name = "district_code", length = 5, nullable = false)
    private String districtCode;

    @Column(name = "ward_code", length = 5, nullable = false)
    private String wardCode;

    @Column(name = "street_detail", columnDefinition = "NVARCHAR(255)")
    private String streetDetail;

    @Column(name = "latitude", precision = 10, scale = 7)
    private Double latitude;
    @Column(name = "longitude", precision = 10, scale = 7)
    private Double longitude;

    @Builder.Default
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
```
- [ ] **Step 2:** Tạo `UserAddressRepository.java`:
```java
package com.example.thexuong.repository;

import com.example.thexuong.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUserIdOrderByIsDefaultDescIdAsc(Long userId);
    long countByUserId(Long userId);
}
```
- [ ] **Step 3:** Sửa `User.java`:
  - **Xóa** field `address` (`:57-58` — `@Column(columnDefinition = "NVARCHAR(MAX)") private String address;`).
  - **Thêm** (KHÔNG cascade, orphan removal false — địa chỉ độc lập):
```java
@OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
@Builder.Default
private List<UserAddress> addresses = new java.util.ArrayList<>();
```
- [ ] **Step 4:** Build BE: `./gradlew compileJava` → sẽ FAIL vì nhiều chỗ còn dùng `user.getAddress()` / `user.setAddress(...)`. Task 2.2 sẽ sửa hết. Tạm chấp nhận fail compile ở task này, sửa tiếp 2.2 rồi build lại. **Hoặc** làm 2.1 + 2.2 cùng 1 commit để build xanh. Khuyến nghị: làm 2.1+2.2 liên tiếp, build 1 lần.
- [ ] **Step 5:** Commit (sau khi 2.2 xong): `feat(be): entity UserAddress + xóa User.address`.

---

## Task 2.2: DTOs `AddressRequest`/`AddressResponse` + mở rộng `UserResponse` + sửa controllers

**Files:**
- Create: `src/main/java/com/example/thexuong/dto/address/AddressRequest.java`, `AddressResponse.java`
- Modify: `src/main/java/com/example/thexuong/dto/UserResponse.java` (xóa `address`, thêm `addresses`)
- Modify: `src/main/java/com/example/thexuong/dto/auth/UpdateProfileRequest.java` (xóa field `address`)
- Modify: `src/main/java/com/example/thexuong/service/UserService.java` (updateProfile bỏ param address)
- Modify: `src/main/java/com/example/thexuong/controller/AuthRestController.java` (toUserResponse + updateProfile)
- Modify: `src/main/java/com/example/thexuong/controller/CheckoutRestController.java` (bỏ `.address(user.getAddress())`)
- Modify: `src/main/java/com/example/thexuong/controller/AdminUserRestController.java` (kiểm tra gọi updateProfile)

- [ ] **Step 1:** `AddressRequest.java`:
```java
package com.example.thexuong.dto.address;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class AddressRequest {
    @Size(max = 50, message = "Nhãn tối đa 50 ký tự")
    private String label;

    @NotBlank(message = "Tên người nhận không được để trống")
    @Size(max = 255, message = "Tên người nhận tối đa 255 ký tự")
    private String recipientName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^0[0-9]{9,10}$", message = "Số điện thoại không hợp lệ (bắt đầu 0, 10-11 số)")
    private String recipientPhone;

    @NotBlank(message = "Vui lòng chọn tỉnh/thành phố")
    private String provinceCode;
    @NotBlank(message = "Vui lòng chọn quận/huyện")
    private String districtCode;
    @NotBlank(message = "Vui lòng chọn phường/xã")
    private String wardCode;

    @Size(max = 255, message = "Số nhà/đường tối đa 255 ký tự")
    private String streetDetail;

    private Double latitude;
    private Double longitude;

    private Boolean isDefault = false;
}
```
- [ ] **Step 2:** `AddressResponse.java`:
```java
package com.example.thexuong.dto.address;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AddressResponse {
    private Long id;
    private String label;
    private String recipientName;
    private String recipientPhone;
    private String provinceCode;
    private String districtCode;
    private String wardCode;
    private String streetDetail;
    private Double latitude;
    private Double longitude;
    private Boolean isDefault;
    // KHÔNG có provinceName/fullAddress — FE tự resolve từ vn-regions.ts (BE không có dataset VN)
}
```
- [ ] **Step 3:** Sửa `UserResponse.java` — **xóa** field `address`, **thêm**:
```java
private java.util.List<com.example.thexuong.dto.address.AddressResponse> addresses;
```
- [ ] **Step 4:** Sửa `UpdateProfileRequest.java` — **xóa** field `address` + `@Size` + `@NotBlank`. Giữ `fullName` + `phoneNumber` (+ password nếu có).
- [ ] **Step 5:** Sửa `UserService.java` — cả 2 overload `updateProfile`:
  - Overload by email (`:37-54`): xóa param `String address`, xóa `user.setAddress(address);`.
  - Overload by userId (`:57-73`): xóa param `String address`, xóa `if (address != null) user.setAddress(address);`.
  - **Lưu ý:** mọi caller phải cập nhật signature. Grep `updateProfile(` trong `*.java` để tìm hết (AuthRestController, AdminUserRestController, OrderService nếu có).
- [ ] **Step 6:** Sửa `AuthRestController.java`:
  - `toUserResponse` (`:256-268`): **xóa** `.address(user.getAddress())`, **thêm** `.addresses(...)`:
```java
.addresses(user.getAddresses() == null ? java.util.List.of() :
    user.getAddresses().stream().map(a -> com.example.thexuong.dto.address.AddressResponse.builder()
        .id(a.getId()).label(a.getLabel())
        .recipientName(a.getRecipientName()).recipientPhone(a.getRecipientPhone())
        .provinceCode(a.getProvinceCode()).districtCode(a.getDistrictCode()).wardCode(a.getWardCode())
        .streetDetail(a.getStreetDetail())
        .latitude(a.getLatitude()).longitude(a.getLongitude())
        .isDefault(a.getIsDefault())
        .build()).toList())
```
  - `updateProfile` (`:195-201`): bỏ `request.getAddress()` khỏi lời gọi `userService.updateProfile(...)` (giờ thiếu param address).
  - **Lưu ý lazy load:** `user.getAddresses()` trong `toUserResponse` trigger query. Đảm bảo trong transaction hoặc OSIV (Spring Boot mặc định `spring.jpa.open-in-view=true` → OK).
- [ ] **Step 7:** Sửa `CheckoutRestController.java` (`:152-158`) — **xóa** dòng `.address(user.getAddress())` (cột đã xóa, sẽ không compile nếu giữ). Có thể thêm `addresses` vào response để FE tiện (1 request thay vì 2), nhưng ponytail: FE tự fetch qua `GET /addresses` → KHÔNG thêm, giữ response checkout nguyên (chỉ bỏ `.address()`). Nếu FE cần addresses, gọi `addressStore.fetch()` riêng.
- [ ] **Step 8:** Sửa `AdminUserRestController.java` — grep `updateProfile` (`:154` gọi `userService.updateProfile(id, fullName, phoneNumber, null, password)`). Giờ signature bỏ param address → sửa thành `userService.updateProfile(id, fullName, phoneNumber, password)`. Kiểm tra chỗ nào khác truyền address.
- [ ] **Step 9:** Build: `./gradlew compileJava` → PASS. `./gradlew bootRun` → start OK (verify không lỗi runtime do Hibernate map User không còn cột address — DB đã DROP ở 1.1).
- [ ] **Step 10:** Commit (cùng 2.1): `feat(be): entity UserAddress + xóa User.address + DTOs address`.

---

## Task 2.3: `AddressService` (CRUD + default + ownership)

**Files:** Create: `src/main/java/com/example/thexuong/service/AddressService.java`

**Interfaces (Produces):**
- `List<AddressResponse> listByUser(String username)`
- `AddressResponse create(String username, AddressRequest req)`
- `AddressResponse update(String username, Long id, AddressRequest req)`
- `void delete(String username, Long id)`
- `void setDefault(String username, Long id)`

- [ ] **Step 1:** Tạo `AddressService.java`:
```java
package com.example.thexuong.service;

import com.example.thexuong.dto.address.AddressRequest;
import com.example.thexuong.dto.address.AddressResponse;
import com.example.thexuong.entity.User;
import com.example.thexuong.entity.UserAddress;
import com.example.thexuong.repository.UserAddressRepository;
import com.example.thexuong.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.access.AccessDeniedException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressService {
    private final UserAddressRepository addressRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<AddressResponse> listByUser(String username) {
        return addressRepository.findByUserIdOrderByIsDefaultDescIdAsc(userIdOf(username)).stream()
                .map(this::toResponse).toList();
    }

    @Transactional
    public AddressResponse create(String username, AddressRequest req) {
        User user = userOf(username);
        UserAddress a = UserAddress.builder()
                .user(user).label(req.getLabel())
                .recipientName(req.getRecipientName()).recipientPhone(req.getRecipientPhone())
                .provinceCode(req.getProvinceCode()).districtCode(req.getDistrictCode()).wardCode(req.getWardCode())
                .streetDetail(req.getStreetDetail()).latitude(req.getLatitude()).longitude(req.getLongitude())
                .isDefault(req.getIsDefault() != null && req.getIsDefault())
                .createdAt(java.time.LocalDateTime.now()).updatedAt(java.time.LocalDateTime.now())
                .build();
        a = addressRepository.save(a);
        if (Boolean.TRUE.equals(a.getIsDefault())) clearOtherDefaults(user.getId(), a.getId());
        // ponytail: address đầu tiên auto default để user luôn có 1 default
        if (addressRepository.countByUserId(user.getId()) == 1) { a.setIsDefault(true); addressRepository.save(a); }
        return toResponse(a);
    }

    @Transactional
    public AddressResponse update(String username, Long id, AddressRequest req) {
        UserAddress a = getOwnedOrThrow(username, id);
        a.setLabel(req.getLabel());
        a.setRecipientName(req.getRecipientName()); a.setRecipientPhone(req.getRecipientPhone());
        a.setProvinceCode(req.getProvinceCode()); a.setDistrictCode(req.getDistrictCode()); a.setWardCode(req.getWardCode());
        a.setStreetDetail(req.getStreetDetail()); a.setLatitude(req.getLatitude()); a.setLongitude(req.getLongitude());
        if (req.getIsDefault() != null && req.getIsDefault() && !Boolean.TRUE.equals(a.getIsDefault())) {
            a.setIsDefault(true); clearOtherDefaults(a.getUser().getId(), a.getId());
        }
        a.setUpdatedAt(java.time.LocalDateTime.now());
        return toResponse(addressRepository.save(a));
    }

    @Transactional
    public void delete(String username, Long id) {
        UserAddress a = getOwnedOrThrow(username, id);
        boolean wasDefault = Boolean.TRUE.equals(a.getIsDefault());
        addressRepository.delete(a);
        // ponytail: reassign default cho address còn lại nếu xóa cái default
        if (wasDefault) {
            List<UserAddress> rest = addressRepository.findByUserIdOrderByIsDefaultDescIdAsc(a.getUser().getId());
            if (!rest.isEmpty()) { rest.get(0).setIsDefault(true); addressRepository.save(rest.get(0)); }
        }
    }

    @Transactional
    public void setDefault(String username, Long id) {
        UserAddress a = getOwnedOrThrow(username, id);
        clearOtherDefaults(a.getUser().getId(), a.getId());
        a.setIsDefault(true);
        addressRepository.save(a);
    }

    @Transactional(readOnly = true)
    public UserAddress getOwnedOrThrow(String username, Long id) {
        UserAddress a = addressRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy địa chỉ"));
        if (!a.getUser().getEmail().equals(username)) throw new AccessDeniedException("Không có quyền");
        return a;
    }

    private void clearOtherDefaults(Long userId, Long keepId) {
        addressRepository.findByUserIdOrderByIsDefaultDescIdAsc(userId).forEach(x -> {
            if (!x.getId().equals(keepId) && Boolean.TRUE.equals(x.getIsDefault())) { x.setIsDefault(false); addressRepository.save(x); }
        });
    }
    private Long userIdOf(String username) { return userOf(username).getId(); }
    private User userOf(String username) { return userRepository.findByEmail(username).orElseThrow(() -> new EntityNotFoundException("User không tồn tại")); }

    private AddressResponse toResponse(UserAddress a) {
        return AddressResponse.builder()
                .id(a.getId()).label(a.getLabel())
                .recipientName(a.getRecipientName()).recipientPhone(a.getRecipientPhone())
                .provinceCode(a.getProvinceCode()).districtCode(a.getDistrictCode()).wardCode(a.getWardCode())
                .streetDetail(a.getStreetDetail()).latitude(a.getLatitude()).longitude(a.getLongitude())
                .isDefault(a.getIsDefault()).build();
    }
}
```
- [ ] **Step 2:** Verify `UserRepository` có `findByEmail` (đã có — `UserService` dùng). Build: `./gradlew compileJava` → PASS.
- [ ] **Step 3:** Commit: `feat(be): AddressService CRUD + default + ownership`.

---

## Task 2.4: `AddressRestController` `/api/v1/addresses`

**Files:** Create: `src/main/java/com/example/thexuong/controller/AddressRestController.java`

- [ ] **Step 1:** Tạo controller (pattern theo `OrderRestController.java` — auth check qua `Authentication`):
```java
package com.example.thexuong.controller;

import com.example.thexuong.dto.address.AddressRequest;
import com.example.thexuong.dto.address.AddressResponse;
import com.example.thexuong.service.AddressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/addresses")
@RequiredArgsConstructor
public class AddressRestController {
    private final AddressService addressService;

    @GetMapping
    public ResponseEntity<List<AddressResponse>> list(Authentication auth) {
        return ResponseEntity.ok(addressService.listByUser(auth.getName()));
    }

    @PostMapping
    public ResponseEntity<AddressResponse> create(Authentication auth, @Valid @RequestBody AddressRequest req) {
        return ResponseEntity.ok(addressService.create(auth.getName(), req));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> update(Authentication auth, @PathVariable Long id, @Valid @RequestBody AddressRequest req) {
        return ResponseEntity.ok(addressService.update(auth.getName(), id, req));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String,String>> delete(Authentication auth, @PathVariable Long id) {
        addressService.delete(auth.getName(), id);
        return ResponseEntity.ok(Map.of("message", "Xóa địa chỉ thành công"));
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<Map<String,String>> setDefault(Authentication auth, @PathVariable Long id) {
        addressService.setDefault(auth.getName(), id);
        return ResponseEntity.ok(Map.of("message", "Đặt địa chỉ mặc định thành công"));
    }
}
```
- [ ] **Step 2:** Build: `./gradlew compileJava` → PASS.
- [ ] **Step 3:** Commit: `feat(be): AddressRestController CRUD /api/v1/addresses`.

---

## Task 2.5: Google Maps proxy `MapsRestController` + `MapsService`

**Files:**
- Modify: `src/main/resources/application.yml` (thêm `app.google.maps.api-key`)
- Create: `src/main/java/com/example/thexuong/service/MapsService.java`
- Create: `src/main/java/com/example/thexuong/controller/MapsRestController.java`

**Endpoints (Produces):**
- `GET /api/v1/maps/reverse-geocode?lat=&lng=` → `{ formattedAddress, addressComponents }` (BE gọi Google Geocoding API server-side, giấu key). FE match text sang code VN qua `nearestWard` (centroid) — BE chỉ trả text Google.

- [ ] **Step 1:** `application.yml` thêm (sau `app:`):
```yaml
app:
  google:
    maps:
      api-key: ${GOOGLE_MAPS_API_KEY:}        # env var, KHÔNG commit key thật
      language: vi
      region: vn
```
> Đặt key qua env var khi chạy. KHÔNG commit key thật vào file.
- [ ] **Step 2:** `MapsService.java` (dùng `RestClient` Spring 3.5):
```java
package com.example.thexuong.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MapsService {
    @Value("${app.google.maps.api-key:}") private String apiKey;
    @Value("${app.google.maps.language:vi}") private String language;
    @Value("${app.google.maps.region:vn}") private String region;

    // ponytail: trả Map thô thay vì DTO, đủ dùng cho FE match code
    @SuppressWarnings("unchecked")
    public Map<String, Object> reverseGeocode(double lat, double lng) {
        if (apiKey == null || apiKey.isBlank()) throw new IllegalStateException("Google Maps API key chưa cấu hình");
        RestClient client = RestClient.builder().baseUrl("https://maps.googleapis.com/maps/api/geocode").build();
        Map<String,Object> resp = client.get().uri(uri -> uri.path("/json")
                .queryParam("latlng", lat + "," + lng)
                .queryParam("language", language).queryParam("region", region)
                .queryParam("key", apiKey).build()).retrieve().body(Map.class);
        List<Map<String,Object>> results = (List<Map<String,Object>>) resp.get("results");
        if (results == null || results.isEmpty()) return Map.of("formattedAddress", "", "addressComponents", List.of());
        Map<String,Object> best = results.get(0);
        return Map.of(
            "formattedAddress", best.getOrDefault("formatted_address", ""),
            "addressComponents", best.getOrDefault("address_components", List.of())
        );
    }
}
```
- [ ] **Step 3:** `MapsRestController.java`:
```java
package com.example.thexuong.controller;

import com.example.thexuong.service.MapsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/maps")
@RequiredArgsConstructor
public class MapsRestController {
    private final MapsService mapsService;

    @GetMapping("/reverse-geocode")
    public ResponseEntity<Map<String,Object>> reverseGeocode(Authentication auth,
            @RequestParam double lat, @RequestParam double lng) {
        return ResponseEntity.ok(mapsService.reverseGeocode(lat, lng));
    }
}
```
- [ ] **Step 4:** Build: `./gradlew compileJava` → PASS. Test chạy: set env `GOOGLE_MAPS_API_KEY=xxx`, `./gradlew bootRun`, gọi `GET /api/v1/maps/reverse-geocode?lat=21.0285&lng=105.8542` (cần auth session) → trả `formattedAddress` + `addressComponents`.
- [ ] **Step 5:** Commit: `feat(be): proxy Google Maps reverse-geocode /api/v1/maps`.

---

## Task 2.6: `SecurityConfig` thêm `/addresses/**`, `/maps/**`

**Files:** Modify: `src/main/java/com/example/thexuong/config/SecurityConfig.java` (`:176` vùng authenticated).

- [ ] **Step 1:** Thêm vào `.requestMatchers(...).authenticated()`:
```java
.requestMatchers("/addresses", "/addresses/**", "/maps", "/maps/**").authenticated()
```
> `/maps/**` yêu cầu auth để tránh abuse API key (chỉ user đã login mới reverse-geocode được).
- [ ] **Step 2:** Build + verify: gọi `GET /api/v1/addresses` chưa login → 401; đã login → 200. Commit: `feat(be): SecurityConfig thêm /addresses /maps`.

---

## Task 3.1: Types `Address` + mở rộng `User` (bỏ `User.address`)

**Files:** Modify `frontend/src/types/auth.types.ts`.

- [ ] **Step 1:** `auth.types.ts` — **xóa** `address?: string` khỏi `User`. **Thêm**:
```ts
export interface Address {
  id: number
  label?: string
  recipientName: string
  recipientPhone: string
  provinceCode: string
  districtCode: string
  wardCode: string
  streetDetail?: string
  latitude?: number
  longitude?: number
  isDefault: boolean
}
```
Thêm vào `User`: `addresses?: Address[]`
- [ ] **Step 2:** `order.types.ts` — KHÔNG đổi (Order vẫn có `address?: string` text snapshot).
- [ ] **Step 3:** Grep FE còn dùng `user.address` / `authStore.user?.address` / `profileForm.address` — sẽ sửa ở Task 3.6 (Profile) + 3.7 (Checkout) + 3.2 (api). Tạm chấp nhận type-check fail cho tới 3.2/3.6/3.7 xong. Hoặc làm 3.1+3.2+3.6+3.7 liên tiếp.
- [ ] **Step 4:** Commit (sau khi các task FE依赖 xong): `feat(fe): types Address + bỏ User.address`.

---

## Task 3.2: API service addresses + maps + bỏ `updateProfile.address`

**Files:** Modify `frontend/src/services/api.ts`, `frontend/src/stores/auth.store.ts`.

- [ ] **Step 1:** `api.ts` — sửa `updateProfile` (`:165-171`) **bỏ** field `address`:
```ts
async updateProfile(data: {
  fullName?: string
  phoneNumber?: string
}): Promise<any> {
  return (await this.client.put('/auth/profile', data)).data
}
```
- [ ] **Step 2:** Thêm methods vào `ApiService` (sau `updateProfile`):
```ts
// Address APIs
async getAddresses(): Promise<Address[]> { return (await this.client.get('/addresses')).data }
async createAddress(data: Omit<Address,'id'|'isDefault'> & { isDefault?: boolean }): Promise<Address> {
  return (await this.client.post('/addresses', data)).data
}
async updateAddress(id: number, data: Omit<Address,'id'|'isDefault'> & { isDefault?: boolean }): Promise<Address> {
  return (await this.client.put(`/addresses/${id}`, data)).data
}
async deleteAddress(id: number): Promise<void> { await this.client.delete(`/addresses/${id}`) }
async setDefaultAddress(id: number): Promise<void> { await this.client.patch(`/addresses/${id}/default`) }

// Maps proxy
async reverseGeocode(lat: number, lng: number): Promise<{ formattedAddress: string; addressComponents: any[] }> {
  return (await this.client.get('/maps/reverse-geocode', { params: { lat, lng } })).data
}
```
> Import `Address` từ `@/types`.
- [ ] **Step 3:** `auth.store.ts` — sửa `updateProfile` (`:77-94`) **bỏ** `address` khỏi type `profileData`:
```ts
async updateProfile(profileData: {
  fullName?: string
  phoneNumber?: string
}) {
  // ... phần còn lại giữ nguyên
}
```
- [ ] **Step 4:** `npm run type-check` → PASS. Commit: `feat(fe): api service addresses + maps + bỏ updateProfile.address`.

---

## Task 3.3: `address.store.ts` (Pinia)

**Files:** Create `frontend/src/stores/address.store.ts`.

- [ ] **Step 1:**
```ts
import { defineStore } from 'pinia'
import type { Address } from '@/types'
import api from '@/services/api'

export const useAddressStore = defineStore('address', {
  state: () => ({ addresses: [] as Address[], loading: false, loaded: false }),
  getters: {
    defaultAddress: (s) => s.addresses.find(a => a.isDefault) || null,
    hasAddresses: (s) => s.addresses.length > 0
  },
  actions: {
    async fetch() { this.loading = true; try { this.addresses = await api.getAddresses(); this.loaded = true } finally { this.loading = false } },
    async create(data: Parameters<typeof api.createAddress>[0]) { const a = await api.createAddress(data); await this.fetch(); return a },
    async update(id: number, data: Parameters<typeof api.updateAddress>[1]) { const a = await api.updateAddress(id, data); await this.fetch(); return a },
    async remove(id: number) { await api.deleteAddress(id); await this.fetch() },
    async setDefault(id: number) { await api.setDefaultAddress(id); await this.fetch() }
  }
})
```
- [ ] **Step 2:** `npm run type-check` → PASS. Commit: `feat(fe): address.store Pinia`.

---

## Task 3.4: Composable `useGoogleMaps` (loader + Autocomplete)

**Files:**
- Modify `frontend/package.json` (thêm `@googlemaps/js-api-loader` + dev `@types/google.maps`)
- Create `frontend/src/composables/useGoogleMaps.ts`
- Modify `frontend/.env` + `.env.production` (thêm `VITE_GOOGLE_MAPS_API_KEY=`)

> **Quyết định key:** Place Autocomplete chạy FE-direct (Google khuyến nghị). Dùng FE key riêng, restrict HTTP referrer `thexuong.xuansown.id.vn` + `localhost` trong Google Console. Reverse-geocode qua BE (giấu key BE). 2 key riêng biệt — best practice.

- [ ] **Step 1:** `cd frontend && npm install @googlemaps/js-api-loader && npm install -D @types/google.maps`
- [ ] **Step 2:** `.env` + `.env.production` thêm: `VITE_GOOGLE_MAPS_API_KEY=` (để trống trong file commit, set khi deploy).
- [ ] **Step 3:** `useGoogleMaps.ts`:
```ts
import { Loader } from '@googlemaps/js-api-loader'

let loader: Loader | null = null

async function ensureLoaded() {
  if (!loader) {
    loader = new Loader({
      apiKey: import.meta.env.VITE_GOOGLE_MAPS_API_KEY || '',
      version: 'weekly',
      libraries: ['places'],
      language: 'vi', region: 'VN'
    })
    await loader.importLibrary('places')
  }
  return window.google
}

// ponytail: Autocomplete gắn vào 1 input, restrict VN, bias theo province đã chọn (optional bounds)
export function useAutocomplete(inputEl: HTMLInputElement, opts: {
  bounds?: google.maps.LatLngBoundsLiteral
  onPlace: (p: { text: string; lat?: number; lng?: number }) => void
}) {
  let ac: google.maps.places.Autocomplete | null = null
  ensureLoaded().then(g => {
    ac = new g.maps.places.Autocomplete(inputEl, {
      types: ['address'],
      componentRestrictions: { country: 'vn' },
      bounds: opts.bounds,
      strictBounds: false
    })
    ac!.addListener('place_changed', () => {
      const place = ac!.getPlace()
      if (!place.geometry) return
      opts.onPlace({
        text: place.formatted_address || '',
        lat: place.geometry.location?.lat(),
        lng: place.geometry.location?.lng()
      })
    })
  })
  return {
    setBounds: (b?: google.maps.LatLngBoundsLiteral) => { if (ac && b) ac.setBounds(b) },
    destroy: () => { if (ac) { google.maps.event.clearInstanceListeners(ac); ac = null } }
  }
}
```
- [ ] **Step 4:** `npm run type-check` → PASS. Commit: `feat(fe): composable useGoogleMaps + Autocomplete`.

---

## Task 3.5: Component `AddressForm.vue`

**Files:** Create `frontend/src/components/address/AddressForm.vue`.

**Behavior:** 3 `<select>` cascading (Tỉnh → Quận → Phường) từ `vn-regions.ts` + ô "Số nhà/đường" chạy Google Autocomplete (bias theo tỉnh đã chọn) + nút "Dùng vị trí của tôi" (`navigator.geolocation` → `api.reverseGeocode` → match code qua `nearestWard` + fill selects + street). Props: `modelValue` (Address partial), emit `submit`/`cancel`, dùng trong `BaseModal`.

- [ ] **Step 1:** Tạo `AddressForm.vue` (scoped style theo pattern `Profile.vue`, dùng `BaseInput`/`BaseButton` cho recipient/street, `<select>` native Tailwind cho 3 dropdown):
```vue
<template>
  <form @submit.prevent="onSubmit" class="flex flex-col gap-4">
    <BaseInput v-model="form.label" label="Nhãn (vd: Nhà, Công ty)" placeholder="Nhà" />
    <div class="grid grid-cols-2 gap-3">
      <BaseInput v-model="form.recipientName" label="Tên người nhận" required />
      <BaseInput v-model="form.recipientPhone" type="tel" label="SĐT người nhận" required />
    </div>
    <div class="grid grid-cols-3 gap-3">
      <div>
        <label class="text-xs uppercase text-[#4C4546]">Tỉnh/Thành</label>
        <select v-model="form.provinceCode" @change="onProvinceChange" class="w-full h-[50px] border border-[#CFC4C5] rounded-lg px-2 font-gelasio text-[16px]">
          <option value="">Chọn...</option>
          <option v-for="p in provinces" :key="p.code" :value="p.code">{{ p.nameWithType }}</option>
        </select>
      </div>
      <div>
        <label class="text-xs uppercase text-[#4C4546]">Quận/Huyện</label>
        <select v-model="form.districtCode" :disabled="!form.provinceCode" @change="onDistrictChange" class="w-full h-[50px] border border-[#CFC4C5] rounded-lg px-2 font-gelasio text-[16px] disabled:bg-gray-100">
          <option value="">Chọn...</option>
          <option v-for="d in districts" :key="d.code" :value="d.code">{{ d.nameWithType }}</option>
        </select>
      </div>
      <div>
        <label class="text-xs uppercase text-[#4C4546]">Phường/Xã</label>
        <select v-model="form.wardCode" :disabled="!form.districtCode" class="w-full h-[50px] border border-[#CFC4C5] rounded-lg px-2 font-gelasio text-[16px] disabled:bg-gray-100">
          <option value="">Chọn...</option>
          <option v-for="w in wards" :key="w.code" :value="w.code">{{ w.nameWithType }}</option>
        </select>
      </div>
    </div>
    <div class="relative">
      <label class="text-xs uppercase text-[#4C4546]">Số nhà, tên đường</label>
      <input ref="streetEl" v-model="form.streetDetail" placeholder="Gõ để gợi ý (Google)" class="w-full h-[50px] border border-[#CFC4C5] rounded-lg px-3 pr-44 font-gelasio text-[16px] outline-none focus:border-black" />
      <button type="button" @click="useMyLocation" :disabled="locating" class="absolute right-2 top-[26px] text-xs flex items-center gap-1 bg-black text-white px-2 py-2 rounded disabled:opacity-50">
        {{ locating ? 'Đang xác định...' : 'Dùng vị trí của tôi' }}
      </button>
    </div>
    <label class="flex items-center gap-2"><input type="checkbox" v-model="form.isDefault" /> Đặt làm mặc định</label>
    <div class="flex justify-end gap-2">
      <BaseButton variant="outline" label="Hủy" @click="$emit('cancel')" />
      <BaseButton variant="primary" type="submit" label="Lưu" />
    </div>
  </form>
</template>
<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { getProvinces, getDistricts, getWards, nearestWard } from '@/utils/vn-regions'
import { useAutocomplete } from '@/composables/useGoogleMaps'
import api from '@/services/api'
import { useToast } from 'vue-toastification'
import BaseInput from '@/components/ui/BaseInput.vue'
import BaseButton from '@/components/ui/BaseButton.vue'
import type { Address } from '@/types'

const props = defineProps<{ modelValue?: Partial<Address> }>()
const emit = defineEmits<{ submit: [data: any]; cancel: [] }>()
const toast = useToast()
const provinces = getProvinces()
const locating = ref(false)
const form = reactive({
  id: props.modelValue?.id,
  label: props.modelValue?.label || '',
  recipientName: props.modelValue?.recipientName || '',
  recipientPhone: props.modelValue?.recipientPhone || '',
  provinceCode: props.modelValue?.provinceCode || '',
  districtCode: props.modelValue?.districtCode || '',
  wardCode: props.modelValue?.wardCode || '',
  streetDetail: props.modelValue?.streetDetail || '',
  latitude: props.modelValue?.latitude,
  longitude: props.modelValue?.longitude,
  isDefault: props.modelValue?.isDefault || false
})
const districts = computed(() => form.provinceCode ? getDistricts(form.provinceCode) : [])
const wards = computed(() => (form.provinceCode && form.districtCode) ? getWards(form.provinceCode, form.districtCode) : [])
const onProvinceChange = () => { form.districtCode = ''; form.wardCode = '' }
const onDistrictChange = () => { form.wardCode = '' }

const streetEl = ref<HTMLInputElement>()
let ac: ReturnType<typeof useAutocomplete> | null = null
onMounted(() => {
  if (streetEl.value) ac = useAutocomplete(streetEl.value, {
    onPlace: (p) => {
      if (p.text) form.streetDetail = p.text
      if (p.lat) { form.latitude = p.lat; form.longitude = p.lng }
    }
  })
})
onUnmounted(() => ac?.destroy())

async function useMyLocation() {
  if (!navigator.geolocation) { toast.error('Trình duyệt không hỗ trợ định vị'); return }
  locating.value = true
  navigator.geolocation.getCurrentPosition(async (pos) => {
    const { latitude, longitude } = pos.coords
    form.latitude = latitude; form.longitude = longitude
    try {
      const res = await api.reverseGeocode(latitude, longitude)
      // ponytail: match lat/lng sang code VN qua nearestWard (centroid) — đủ đúng cho dân dụng
      const match = nearestWard(latitude, longitude)
      form.provinceCode = match.provinceCode
      form.districtCode = match.districtCode
      if (match.wardCode) form.wardCode = match.wardCode
      form.streetDetail = res.formattedAddress?.split(',')[0] || ''
      toast.success('Đã xác định vị trí')
    } catch { toast.error('Không lấy được địa chỉ từ vị trí') }
    finally { locating.value = false }
  }, () => { toast.error('Không lấy được vị trí. Cho phép truy cập vị trí trong trình duyệt.'); locating.value = false },
  { enableHighAccuracy: true, timeout: 10000 })
}

const onSubmit = () => {
  if (!form.provinceCode || !form.districtCode || !form.wardCode) { toast.error('Vui lòng chọn đủ Tỉnh/Quận/Phường'); return }
  if (!form.recipientName || !form.recipientPhone) { toast.error('Vui lòng nhập tên và SĐT người nhận'); return }
  emit('submit', { ...form })
}
</script>
```
- [ ] **Step 2:** `npm run type-check` + `npm run lint` → PASS.
- [ ] **Step 3:** Commit: `feat(fe): component AddressForm (dropdown cascading + vị trí)`.

---

## Task 3.6: Profile.vue — sổ địa chỉ (bỏ textarea address, thêm list + CRUD modal)

**Files:** Modify `frontend/src/views/Profile.vue`.

- [ ] **Step 1:** Thay block "ĐỊA CHỈ NHẬN HÀNG" textarea (`:79-87`) bằng section "Sổ địa chỉ":
  - List `addressStore.addresses` (mỗi item: label icon, recipient name + phone, text format qua `formatAddress`, badge "Mặc định" nếu `isDefault`, nút Sửa/Xóa/Đặt mặc định).
  - Nút "Thêm địa chỉ mới" → mở `BaseModal` chứa `AddressForm` (mode create).
  - Nút "Sửa" → mở `BaseModal` chứa `AddressForm` (mode edit, `:model-value` = address).
  - Xóa → `confirm()` native → `addressStore.remove`.
  - "Đặt mặc định" → `addressStore.setDefault(id)`.
- [ ] **Step 2:** Script setup — thêm imports + state + logic:
```ts
import { useAddressStore } from '@/stores/address.store'
import AddressForm from '@/components/address/AddressForm.vue'
import BaseModal from '@/components/ui/BaseModal.vue'
import { formatAddress } from '@/utils/vn-regions'
const addressStore = useAddressStore()
onMounted(() => { addressStore.fetch() })
const showAddressModal = ref(false)
const editingAddress = ref<Address | null>(null)
const openCreateAddress = () => { editingAddress.value = null; showAddressModal.value = true }
const openEditAddress = (a: Address) => { editingAddress.value = a; showAddressModal.value = true }
const onSubmitAddress = async (data: any) => {
  if (editingAddress.value) await addressStore.update(editingAddress.value.id, data)
  else await addressStore.create(data)
  showAddressModal.value = false
  toast.success('Lưu địa chỉ thành công')
}
const onDeleteAddress = async (id: number) => {
  if (confirm('Xóa địa chỉ này?')) { await addressStore.remove(id); toast.success('Đã xóa') }
}
const onSetDefault = async (id: number) => { await addressStore.setDefault(id); toast.success('Đã đặt mặc định') }
```
- [ ] **Step 3:** **Xóa** `profileForm.address` khỏi reactive (`:190-194`), bỏ `profileForm.address = user.value.address || ''` ở `onMounted` (`:208`) và `handleCancel` (`:256`). Bỏ field `address` khỏi `authStore.updateProfile(...)` call (`:220-224`):
```ts
await authStore.updateProfile({
  fullName: profileForm.fullName,
  phoneNumber: profileForm.phoneNumber
})
```
- [ ] **Step 4:** GIỮ block "ĐỔI MẬT KHẨU" nguyên. Profile giờ có: Họ tên + SĐT (thông tin cá nhân) + Sổ địa chỉ (multi) + Đổi mật khẩu.
- [ ] **Step 5:** `npm run type-check` + chạy dev (`npm run dev`), vào `/profile` → thêm/sửa/xóa địa chỉ, đặt mặc định. Verify modal mở/đóng, dropdown cascading, nút vị trí. Commit: `feat(fe): Profile sổ địa chỉ + bỏ textarea address`.

---

## Task 3.7: Checkout.vue — picker địa chỉ + auto-sync vào form (payload text, không addressId)

**Files:** Modify `frontend/src/views/Checkout.vue`.

**Behavior (theo yêu cầu user):**
1. Hiển thị radio list địa chỉ đã lưu (từ `addressStore.addresses`, default checked = `defaultAddress`).
2. Nút "Thêm địa chỉ mới" → `BaseModal` + `AddressForm` → sau khi create, `addressStore.fetch()` rồi auto-select địa chỉ mới.
3. Khi chọn 1 địa chỉ → auto-sync vào form `fullName`/`phoneNumber`/`address` (text từ `formatAddress`) — **user vẫn sửa được** (input editable).
4. Prefill default address on mount (thay prefill từ `authStore.user.address` hiện tại `:342-349` — field đã xóa).
5. Payload `POST /orders` **KHÔNG đổi** (vẫn `{ fullName, phoneNumber, address: string, paymentMethod, note, voucherCode, pointsToUse }`) — FE điền text từ địa chỉ đã chọn.

- [ ] **Step 1:** Thay block textarea address (`:57-67`) bằng:
```vue
<div class="flex flex-col gap-3">
  <label class="text-xs uppercase">Địa chỉ giao hàng</label>
  <div v-if="addressStore.hasAddresses" class="flex flex-col gap-2">
    <label v-for="a in addressStore.addresses" :key="a.id"
      class="flex gap-2 items-start p-3 border rounded-lg cursor-pointer"
      :class="selectedAddressId === a.id ? 'border-black bg-gray-50' : 'border-[#CFC4C5]'">
      <input type="radio" :value="a.id" v-model="selectedAddressId" @change="selectAddress(a)" />
      <div class="flex-1">
        <div class="font-medium">{{ a.recipientName }} · {{ a.recipientPhone }}
          <span v-if="a.isDefault" class="ml-2 text-[10px] bg-black text-white px-1 rounded">Mặc định</span>
          <span v-if="a.label" class="ml-2 text-[10px] text-gray-500">{{ a.label }}</span>
        </div>
        <div class="text-sm text-gray-600">
          {{ formatAddress({ streetDetail: a.streetDetail, wardCode: a.wardCode, districtCode: a.districtCode, provinceCode: a.provinceCode }) }}
        </div>
      </div>
      <button type="button" @click="editAtCheckout(a)" class="text-xs underline">Sửa</button>
    </label>
  </div>
  <div v-else class="text-sm text-gray-500 italic">Chưa có địa chỉ đã lưu. Thêm mới bên dưới hoặc nhập tay.</div>
  <button type="button" @click="openAddressModal" class="text-sm underline self-start">+ Thêm địa chỉ mới</button>
  <BaseModal v-model="showAddressModal" :title="editingAddress ? 'Sửa địa chỉ' : 'Thêm địa chỉ'">
    <AddressForm :model-value="editingAddress || undefined" @submit="onSubmitAddressAtCheckout" @cancel="showAddressModal = false" />
  </BaseModal>
</div>
```
- [ ] **Step 2:** Script setup — thêm imports + state + logic:
```ts
import { useAddressStore } from '@/stores/address.store'
import { formatAddress } from '@/utils/vn-regions'
import AddressForm from '@/components/address/AddressForm.vue'
import BaseModal from '@/components/ui/BaseModal.vue'
import { useToast } from 'vue-toastification'
import type { Address } from '@/types'
const addressStore = useAddressStore()
const checkoutToast = useToast()
const selectedAddressId = ref<number | null>(null)
const showAddressModal = ref(false)
const editingAddress = ref<Address | null>(null)

// Thay onMounted cũ: fetch addresses rồi prefill default
onMounted(async () => {
  await addressStore.fetch()
  const def = addressStore.defaultAddress
  if (def) selectAddress(def)
  else {
    // ponytail: fallback prefill tên/SĐT từ user (không còn user.address)
    setValues({ fullName: authStore.user?.fullName || '', phoneNumber: authStore.user?.phone || '' })
  }
})
function selectAddress(a: Address) {
  selectedAddressId.value = a.id
  setValues({
    fullName: a.recipientName,
    phoneNumber: a.recipientPhone,
    address: formatAddress({ streetDetail: a.streetDetail, wardCode: a.wardCode, districtCode: a.districtCode, provinceCode: a.provinceCode })
  })
}
const openAddressModal = () => { editingAddress.value = null; showAddressModal.value = true }
const editAtCheckout = (a: Address) => { editingAddress.value = a; showAddressModal.value = true }
const onSubmitAddressAtCheckout = async (data: any) => {
  const saved: Address = editingAddress.value
    ? await addressStore.update(editingAddress.value.id, data)
    : await addressStore.create(data)
  showAddressModal.value = false
  selectAddress(saved)  // auto-sync địa chỉ mới vào form
  checkoutToast.success('Đã lưu địa chỉ')
}
```
- [ ] **Step 3:** Payload submit (`:422-430`) — **KHÔNG đổi** (vẫn text address). `address` = `values.address` (text đã format, user có thể đã sửa tay). KHÔNG có `addressId`.
- [ ] **Step 4:** Checkout.vue thêm `useToast` + try/catch quanh `api.post('/orders')` (hiện đang nuốt error — `:445`):
```ts
try {
  const response = await api.post('/orders', orderData)
  cartStore.clearCart()
  if (response.data.order.paymentUrl) window.location.href = response.data.order.paymentUrl
  else router.push(`/order/${response.data.order.id}`)
} catch (err) {
  checkoutToast.error('Đặt hàng thất bại. Vui lòng thử lại.')
  throw err
}
```
- [ ] **Step 5:** `npm run type-check` + chạy: vào `/checkout` → chọn địa chỉ → form auto-fill → sửa tay 1 field → đặt hàng → order lưu đúng text. Commit: `feat(fe): Checkout picker địa chỉ + auto-sync`.

---

## Task 3.8: OrderDetail.vue — verify hiển thị

**Files:** Verify `frontend/src/views/OrderDetail.vue` (`:77` hiển thị `order.address`).

- [ ] **Step 1:** Vì `Order.address` vẫn là text snapshot (FE format text gửi lên, BE copy vào Order), `OrderDetail.vue` hiển thị `{{ order.address }}` **vẫn hoạt động** — không cần sửa. Verify: đặt 1 order từ checkout với địa chỉ đã chọn → vào `/order/:id` → địa chỉ hiển thị đúng text.
- [ ] **Step 2:** Nếu muốn hiển thị lat/lng (map mini) — YAGNI phase này, skip.
- [ ] **Step 3:** Commit (nếu có sửa): `chore(fe): verify OrderDetail hiển thị address`. Nếu không sửa → skip commit, đánh dấu task xong.

---

## Task 4.1: Verify toàn bộ

- [ ] **Step 1:** FE: `cd frontend && npm run type-check && npm run lint` → PASS.
- [ ] **Step 2:** BE: `./gradlew compileJava` → PASS. `./gradlew bootRun` → start OK.
- [ ] **Step 3:** FE build: `cd frontend && npm run build` → PASS.
- [ ] **Step 4:** Manual E2E checklist:
  - [ ] Đăng nhập → vào `/profile` → thấy sổ địa chỉ (không còn textarea address đơn lẻ).
  - [ ] Thêm địa chỉ (chọn Tỉnh/Quận/Phường + gõ đường có Autocomplete) → lưu → thấy trong list.
  - [ ] Bấm "Dùng vị trí của tôi" → cho phép → dropdown tự chọn Tỉnh/Quận/Phường + street fill.
  - [ ] Sửa địa chỉ, xóa địa chỉ, đặt mặc định → hoạt động. Địa chỉ đầu tiên auto default.
  - [ ] Vào `/checkout` → địa chỉ mặc định được prefill vào form (tên/SĐT/địa chỉ text).
  - [ ] Chọn địa chỉ khác → form auto-sync → sửa tay 1 field → đặt hàng → order lưu đúng text đã gửi.
  - [ ] Thêm địa chỉ mới ngay tại checkout → auto-select → đặt hàng OK.
  - [ ] Đặt hàng khi chưa có địa chỉ đã lưu (nhập tay text) → vẫn hoạt động (backward-compat).
  - [ ] Order detail `/order/:id` hiển thị đúng địa chỉ text.
  - [ ] `PUT /auth/profile` không còn gửi field `address` → BE không lỗi (field đã xóa khỏi DTO).
- [ ] **Step 5:** Cập nhật Progress Table (đánh `[x]` hết) + commit: `docs: hoàn thành shipping addresses feature`.

---

## Self-Review (post-write — lựa chọn A)

- **Spec coverage:** ✅ Tự động xác định vị trí (Task 3.5 nút vị trí + 2.5 proxy) · ✅ Nhiều địa chỉ (1.1 + 2.1-2.4 + 3.6) · ✅ Chọn địa chỉ khi thanh toán (3.7) · ✅ Auto-sync từ profile sang checkout (3.7 `selectAddress` + prefill default) · ✅ User vẫn sửa được (form editable).
- **Lựa chọn A áp dụng:** ✅ Xóa `Users.address` (1.1 DROP + 2.1 entity + 2.2 DTO + 3.1 type + 3.2 api/store + 3.6 Profile). ✅ Orders 0 schema change (KHÔNG có Task ALTER Orders). ✅ `OrderService.placeOrder` không đổi signature. ✅ `PlaceOrderRequest` không đổi. ✅ FE gửi text format từ `UserAddresses` (3.7 `formatAddress`).
- **Backward-compat:** ✅ User chưa có địa chỉ đã lưu → nhập tay text tại checkout → đặt hàng OK. ✅ `Orders.address` text snapshot giữ nguyên, view `VW_OrderSummary` không sửa.
- **Type consistency:** `Address` interface nhất quán FE (`types` → `api` → `store` → `AddressForm` → `Checkout`). BE `AddressResponse` khớp `Address` FE.
- **Rủi ro đã note:**
  1. `dbTheXuong.sql` gitignored — cập nhật trên disk + chạy SQL tay (DROP COLUMN address là destructive, backup trước).
  2. Lazy load `user.getAddresses()` trong `toUserResponse` — OSIV mặc định ON, chấp nhận. Optimize fetch join sau nếu perf vấn đề.
  3. Match reverse-geocode → code VN dùng centroid `nearestWard` — đủ dân dụng, lệch phường thì user tự chọn dropdown.
  4. 2 Google API key (FE-restricted + BE-server) — setup Google Console riêng.
  5. Xóa `Users.address` phá seed data 4 user (address text mất) — không quan trọng (data test). Nếu muốn migrate, chạy SQL copy `Users.address` sang 1 row `UserAddresses` trước khi DROP (optional, skip cho dự án sinh viên).
  6. `UserService.updateProfile` đổi signature (bỏ param address) — phải cập nhật ALL callers (AuthRestController, AdminUserRestController). Task 2.2 đã list.
