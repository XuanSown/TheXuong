# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: cart.spec.ts >> Cart & Checkout Flow >> TC_ST_CART_01: Thêm 1 Sản phẩm vào giỏ hàng thành công
- Location: tests\e2e\cart.spec.ts:4:3

# Error details

```
Test timeout of 30000ms exceeded.
```

```
Error: page.click: Test timeout of 30000ms exceeded.
Call log:
  - waiting for locator('.grid.grid-cols-1 > div:first-child a')

```

# Page snapshot

```yaml
- generic [active] [ref=e1]:
  - generic [ref=e5]:
    - banner [ref=e6]:
      - navigation [ref=e7]:
        - link "Sportify Home" [ref=e8] [cursor=pointer]:
          - /url: /
        - generic [ref=e9]:
          - link "SẢN PHẨM" [ref=e10] [cursor=pointer]:
            - /url: /products
          - link "THỂ THAO" [ref=e12] [cursor=pointer]:
            - /url: /products?sport=all
          - link "THƯƠNG HIỆU" [ref=e14] [cursor=pointer]:
            - /url: /products?brand=all
        - link "ĐĂNG NHẬP" [ref=e17] [cursor=pointer]:
          - /url: /login
    - main [ref=e19]:
      - main [ref=e21]:
        - heading "DANH SÁCH SẢN PHẨM" [level=1] [ref=e24]
        - generic [ref=e25]:
          - complementary [ref=e26]:
            - generic [ref=e27]:
              - generic [ref=e28]:
                - heading "Thể thao" [level=3] [ref=e29]
                - list [ref=e30]:
                  - listitem [ref=e31]:
                    - link "Tất cả" [ref=e32] [cursor=pointer]:
                      - /url: /products
                  - listitem [ref=e33]:
                    - link "Bóng đá" [ref=e34] [cursor=pointer]:
                      - /url: /products?sport=football
                  - listitem [ref=e35]:
                    - link "Cầu lông" [ref=e36] [cursor=pointer]:
                      - /url: /products?sport=badminton
                  - listitem [ref=e37]:
                    - link "Chạy bộ" [ref=e38] [cursor=pointer]:
                      - /url: /products?sport=running
                  - listitem [ref=e39]:
                    - link "Khác" [ref=e40] [cursor=pointer]:
                      - /url: /products?sport=other
              - generic [ref=e42]:
                - heading "Thương hiệu" [level=3] [ref=e43]
                - list [ref=e44]:
                  - listitem [ref=e45]:
                    - link "Tất cả" [ref=e46] [cursor=pointer]:
                      - /url: /products
                  - listitem [ref=e47]:
                    - link "Nike" [ref=e48] [cursor=pointer]:
                      - /url: /products?brand=nike
                  - listitem [ref=e49]:
                    - link "Adidas" [ref=e50] [cursor=pointer]:
                      - /url: /products?brand=adidas
                  - listitem [ref=e51]:
                    - link "Li-Ning" [ref=e52] [cursor=pointer]:
                      - /url: /products?brand=lining
                  - listitem [ref=e53]:
                    - link "Puma" [ref=e54] [cursor=pointer]:
                      - /url: /products?brand=puma
                  - listitem [ref=e55]:
                    - link "Khác" [ref=e56] [cursor=pointer]:
                      - /url: /products?brand=other
          - generic [ref=e57]:
            - generic [ref=e59]:
              - generic [ref=e60] [cursor=pointer]:
                - img "Áo Đấu Sân Khách Đội Tuyển Đức 26 Phiên Bản Authentic" [ref=e62]
                - generic [ref=e63]:
                  - paragraph [ref=e64]: BÓNG ĐÁ
                  - heading "Áo Đấu Sân Khách Đội Tuyển Đức 26 Phiên Bản Authentic" [level=3] [ref=e65]
                  - paragraph [ref=e66]: 3.000.000 đ
              - generic [ref=e67] [cursor=pointer]:
                - img "Bóng Trionda Pro FIFA World Cup 26™" [ref=e69]
                - generic [ref=e70]:
                  - paragraph [ref=e71]: BÓNG ĐÁ
                  - heading "Bóng Trionda Pro FIFA World Cup 26™" [level=3] [ref=e72]
                  - paragraph [ref=e73]: 3.300.000 đ
              - generic [ref=e74] [cursor=pointer]:
                - img "Mũ Lưỡi Trai Trucker BMW M MOTORSPORT Lifestyle" [ref=e76]
                - generic [ref=e77]:
                  - paragraph [ref=e78]: KHÁC
                  - heading "Mũ Lưỡi Trai Trucker BMW M MOTORSPORT Lifestyle" [level=3] [ref=e79]
                  - paragraph [ref=e80]: 720.000 đ
              - generic [ref=e81] [cursor=pointer]:
                - img "M AK 3S SHO" [ref=e83]
                - generic [ref=e84]:
                  - paragraph [ref=e85]: KHÁC
                  - heading "M AK 3S SHO" [level=3] [ref=e86]
                  - paragraph [ref=e87]: 1.200.000 đ
              - generic [ref=e88] [cursor=pointer]:
                - img "Adizero EVO" [ref=e90]
                - generic [ref=e91]:
                  - paragraph [ref=e92]: CHẠY BỘ
                  - heading "Adizero EVO" [level=3] [ref=e93]
                  - paragraph [ref=e94]: 4.000.000 đ
              - generic [ref=e95] [cursor=pointer]:
                - img "Nike Phantom 6 Low Academy 'Erling Haaland'" [ref=e97]
                - generic [ref=e98]:
                  - paragraph [ref=e99]: BÓNG ĐÁ
                  - heading "Nike Phantom 6 Low Academy 'Erling Haaland'" [level=3] [ref=e100]
                  - paragraph [ref=e101]: 2.779.000 đ
              - generic [ref=e102] [cursor=pointer]:
                - img "Nike Vomero 18" [ref=e104]
                - generic [ref=e105]:
                  - paragraph [ref=e106]: CHẠY BỘ
                  - heading "Nike Vomero 18" [level=3] [ref=e107]
                  - paragraph [ref=e108]: 4.259.000 đ
              - generic [ref=e109] [cursor=pointer]:
                - img "Nike ACG Ultrafly Trail" [ref=e111]
                - generic [ref=e112]:
                  - paragraph [ref=e113]: CHẠY BỘ
                  - heading "Nike ACG Ultrafly Trail" [level=3] [ref=e114]
                  - paragraph [ref=e115]: 7.349.000 đ
              - generic [ref=e116] [cursor=pointer]:
                - img "Nike Pegasus 42" [ref=e118]
                - generic [ref=e119]:
                  - paragraph [ref=e120]: CHẠY BỘ
                  - heading "Nike Pegasus 42" [level=3] [ref=e121]
                  - paragraph [ref=e122]: 3.829.000 đ
            - generic [ref=e123]:
              - button "PREV" [disabled] [ref=e124]
              - button "1" [ref=e125] [cursor=pointer]
              - button "2" [ref=e126] [cursor=pointer]
              - button "NEXT" [ref=e127] [cursor=pointer]
    - contentinfo [ref=e128]:
      - generic [ref=e129]:
        - generic [ref=e131]:
          - generic [ref=e132]:
            - paragraph [ref=e134]: Nơi đam mê thể thao hội tụ. Chúng tôi cam kết mang đến những sản phẩm chất lượng nhất cho cộng đồng yêu thể thao Việt Nam.
            - generic [ref=e135]:
              - generic [ref=e136]: Quận 12, TP. Hồ Chí Minh
              - generic [ref=e141]: thexuong.sport@gmail.com
              - generic [ref=e146]: +84 909 123 456
          - generic [ref=e150]:
            - heading "HỖ TRỢ KHÁCH HÀNG" [level=4] [ref=e151]
            - generic [ref=e152]:
              - link "Hướng dẫn chọn size" [ref=e153] [cursor=pointer]:
                - /url: /guide/size
              - link "Chính sách đổi trả" [ref=e154] [cursor=pointer]:
                - /url: /policy/returns
              - link "Phương thức thanh toán" [ref=e155] [cursor=pointer]:
                - /url: /payment-methods
              - link "Kiểm tra đơn hàng" [ref=e156] [cursor=pointer]:
                - /url: /order-tracking
          - generic [ref=e157]:
            - heading "ĐIỀU KHOẢN & CHÍNH SÁCH" [level=4] [ref=e158]
            - generic [ref=e159]:
              - link "Chính sách bảo mật" [ref=e160] [cursor=pointer]:
                - /url: /policy/privacy
              - link "Điều khoản dịch vụ" [ref=e161] [cursor=pointer]:
                - /url: /terms-of-service
              - link "Chính sách vận chuyển" [ref=e162] [cursor=pointer]:
                - /url: /policy/shipping
          - generic [ref=e163]:
            - heading "ĐĂNG KÝ NHẬN ƯU ĐÃI" [level=4] [ref=e164]
            - generic [ref=e165]:
              - textbox "Email của bạn..." [ref=e166]
              - button "Submit email" [ref=e167] [cursor=pointer]
            - heading "MẠNG XÃ HỘI" [level=5] [ref=e170]
            - generic [ref=e171]:
              - link "Facebook" [ref=e172] [cursor=pointer]:
                - /url: "#"
              - link "Instagram" [ref=e175] [cursor=pointer]:
                - /url: "#"
              - link "TikTok" [ref=e179] [cursor=pointer]:
                - /url: "#"
              - link "YouTube" [ref=e182] [cursor=pointer]:
                - /url: "#"
        - generic [ref=e186]:
          - generic [ref=e187]: © 2026 THE XUONG SPORT. ALL RIGHTS RESERVED.
          - link "Về chúng tôi" [ref=e189] [cursor=pointer]:
            - /url: /about
  - generic [ref=e190]:
    - generic "Chat với tư vấn viên" [ref=e191] [cursor=pointer]
    - button "Ẩn nút chat" [ref=e194] [cursor=pointer]: ×
    - generic: Chat tư vấn
```

# Test source

```ts
  1  | import { test, expect } from '@playwright/test';
  2  | 
  3  | test.describe('Cart & Checkout Flow', () => {
  4  |   test('TC_ST_CART_01: Thêm 1 Sản phẩm vào giỏ hàng thành công', async ({ page }) => {
  5  |     // 1. Mở trang Sản phẩm
  6  |     await page.goto('http://localhost:5173/products');
  7  |     
  8  |     // Click vào sản phẩm đầu tiên
> 9  |     await page.click('.grid.grid-cols-1 > div:first-child a');
     |                ^ Error: page.click: Test timeout of 30000ms exceeded.
  10 |     
  11 |     // Đợi trang chi tiết sản phẩm
  12 |     await page.waitForURL(/.*\/product\/.*/);
  13 | 
  14 |     // 2. Chọn Size (assuming there are size buttons, e.g., '39', '40')
  15 |     const sizeButtons = page.locator('button.border-[#CFC4C6]'); // Usually standard sizes
  16 |     if (await sizeButtons.count() > 0) {
  17 |       await sizeButtons.first().click();
  18 |     }
  19 |     
  20 |     // 3. Bấm "Thêm vào giỏ"
  21 |     await page.click('button:has-text("THÊM VÀO GIỎ")');
  22 |     
  23 |     // 4. Kiểm tra Badge giỏ hàng trên Header tăng lên
  24 |     // Look for the red dot badge in header
  25 |     await expect(page.locator('.absolute.top-0.right-0.w-4.h-4.bg-red-500')).toBeVisible({ timeout: 5000 });
  26 |   });
  27 | 
  28 |   test('TC_ST_COUT_01: Kiểm tra bắt buộc điền địa chỉ khi Checkout', async ({ page }) => {
  29 |     // Go directly to checkout
  30 |     await page.goto('http://localhost:5173/checkout');
  31 |     
  32 |     // Since user is not logged in or has no address, check if it redirects to login OR forces address
  33 |     // In our implementation, unauthenticated users are usually redirected to /login first.
  34 |     // Assuming we reach checkout:
  35 |     if (page.url().includes('/login')) {
  36 |       // Expected behavior: forced login
  37 |       expect(true).toBe(true); 
  38 |     } else {
  39 |       // If we are on checkout, look for address form
  40 |       await expect(page.locator('text=Địa chỉ giao hàng')).toBeVisible();
  41 |       // Must have form fields
  42 |       await expect(page.locator('input[placeholder="Số điện thoại"]')).toBeVisible();
  43 |     }
  44 |   });
  45 | });
  46 | 
```