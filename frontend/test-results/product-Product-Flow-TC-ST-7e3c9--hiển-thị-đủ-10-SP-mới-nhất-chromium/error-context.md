# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: product.spec.ts >> Product Flow >> TC_ST_PROD_01: Load giao diện trang chủ, hiển thị đủ 10 SP mới nhất
- Location: tests\e2e\product.spec.ts:4:3

# Error details

```
TimeoutError: page.waitForSelector: Timeout 10000ms exceeded.
Call log:
  - waiting for locator('a[href^="/product/"]') to be visible

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
        - generic [ref=e22]:
          - generic [ref=e23]:
            - heading "THE XUONG SPORT" [level=1] [ref=e24]
            - paragraph [ref=e25]: Nâng tầm trải nghiệm sản phẩm thể thao
          - generic [ref=e26]:
            - link "KHÁM PHÁ NGAY" [ref=e27] [cursor=pointer]:
              - /url: /products
            - link "VỀ CHÚNG TÔI" [ref=e28] [cursor=pointer]:
              - /url: /about
          - generic [ref=e29]:
            - heading "Layers hold tales of time" [level=1]:
              - generic: Layers hold
              - generic: tales of time
            - paragraph [ref=e32]: — "Sports are not just about winning; they are about pushing your limits, embracing the sweat, and discovering the champion hidden inside you. Every drop of sweat brings you closer to your goals."
            - generic [ref=e33]:
              - paragraph [ref=e34]: — "Your body can stand almost anything; it is your mind that you have to convince. Train hard, stay focused, and let your passion speak louder than your excuses today."
              - link "KHÁM PHÁ NGAY" [ref=e35] [cursor=pointer]:
                - /url: /products
        - generic [ref=e37]:
          - generic [ref=e38]:
            - generic [ref=e39]: NIKE
            - generic [ref=e40]: ADIDAS
            - generic [ref=e41]: LI-NING
            - generic [ref=e42]: PUMA
          - generic [ref=e43]:
            - generic [ref=e44]: NIKE
            - generic [ref=e45]: ADIDAS
            - generic [ref=e46]: LI-NING
            - generic [ref=e47]: PUMA
        - generic [ref=e48]:
          - generic [ref=e49]:
            - heading "CÔNG NGHỆ VƯỢT TRỘI" [level=3] [ref=e52]
            - paragraph [ref=e53]: Tất cả sản phẩm tại TheXuong đều được tuyển chọn kỹ lưỡng, đảm bảo tích hợp những công nghệ hỗ trợ vận động tiên tiến nhất hiện nay.
          - generic [ref=e58]:
            - heading "CAM KẾT CHÍNH HÃNG" [level=3] [ref=e59]
            - paragraph [ref=e61]: Chúng tôi cam kết 100% sản phẩm là hàng chính hãng, phát hiện hàng giả đền bù gấp 10 lần.
          - generic [ref=e62]:
            - heading "UY TÍN" [level=3] [ref=e67]
            - paragraph [ref=e68]: THEXUONG tự tin với chất lượng sản phẩm.
          - generic [ref=e71]:
            - heading "CHĂM SÓC" [level=3] [ref=e72]
            - paragraph [ref=e73]: THEXUONG luôn luôn hỗ trợ khi khách hàng cần chúng tôi.
        - generic [ref=e74]:
          - generic [ref=e75]:
            - paragraph [ref=e76]: BẢN SẮC THỂ THAO
            - heading "SẢN PHẨM MỚI" [level=2] [ref=e77]
          - generic [ref=e78]:
            - generic [ref=e79]:
              - generic [ref=e80] [cursor=pointer]:
                - img "Áo Đấu Sân Khách Đội Tuyển Đức 26 Phiên Bản Authentic" [ref=e82]
                - generic [ref=e83]:
                  - paragraph [ref=e84]: BÓNG ĐÁ
                  - heading "Áo Đấu Sân Khách Đội Tuyển Đức 26 Phiên Bản Authentic" [level=3] [ref=e85]
                  - paragraph [ref=e86]: 3.000.000 đ
              - generic [ref=e87] [cursor=pointer]:
                - img "Bóng Trionda Pro FIFA World Cup 26™" [ref=e89]
                - generic [ref=e90]:
                  - paragraph [ref=e91]: BÓNG ĐÁ
                  - heading "Bóng Trionda Pro FIFA World Cup 26™" [level=3] [ref=e92]
                  - paragraph [ref=e93]: 3.300.000 đ
              - generic [ref=e94] [cursor=pointer]:
                - img "Mũ Lưỡi Trai Trucker BMW M MOTORSPORT Lifestyle" [ref=e96]
                - generic [ref=e97]:
                  - paragraph [ref=e98]: KHÁC
                  - heading "Mũ Lưỡi Trai Trucker BMW M MOTORSPORT Lifestyle" [level=3] [ref=e99]
                  - paragraph [ref=e100]: 720.000 đ
              - generic [ref=e101] [cursor=pointer]:
                - img "M AK 3S SHO" [ref=e103]
                - generic [ref=e104]:
                  - paragraph [ref=e105]: KHÁC
                  - heading "M AK 3S SHO" [level=3] [ref=e106]
                  - paragraph [ref=e107]: 1.200.000 đ
            - link "XEM TẤT CẢ" [ref=e109] [cursor=pointer]:
              - /url: /products
    - contentinfo [ref=e110]:
      - generic [ref=e111]:
        - generic [ref=e113]:
          - generic [ref=e114]:
            - paragraph [ref=e116]: Nơi đam mê thể thao hội tụ. Chúng tôi cam kết mang đến những sản phẩm chất lượng nhất cho cộng đồng yêu thể thao Việt Nam.
            - generic [ref=e117]:
              - generic [ref=e118]: Quận 12, TP. Hồ Chí Minh
              - generic [ref=e123]: thexuong.sport@gmail.com
              - generic [ref=e128]: +84 909 123 456
          - generic [ref=e132]:
            - heading "HỖ TRỢ KHÁCH HÀNG" [level=4] [ref=e133]
            - generic [ref=e134]:
              - link "Hướng dẫn chọn size" [ref=e135] [cursor=pointer]:
                - /url: /guide/size
              - link "Chính sách đổi trả" [ref=e136] [cursor=pointer]:
                - /url: /policy/returns
              - link "Phương thức thanh toán" [ref=e137] [cursor=pointer]:
                - /url: /payment-methods
              - link "Kiểm tra đơn hàng" [ref=e138] [cursor=pointer]:
                - /url: /order-tracking
          - generic [ref=e139]:
            - heading "ĐIỀU KHOẢN & CHÍNH SÁCH" [level=4] [ref=e140]
            - generic [ref=e141]:
              - link "Chính sách bảo mật" [ref=e142] [cursor=pointer]:
                - /url: /policy/privacy
              - link "Điều khoản dịch vụ" [ref=e143] [cursor=pointer]:
                - /url: /terms-of-service
              - link "Chính sách vận chuyển" [ref=e144] [cursor=pointer]:
                - /url: /policy/shipping
          - generic [ref=e145]:
            - heading "ĐĂNG KÝ NHẬN ƯU ĐÃI" [level=4] [ref=e146]
            - generic [ref=e147]:
              - textbox "Email của bạn..." [ref=e148]
              - button "Submit email" [ref=e149] [cursor=pointer]
            - heading "MẠNG XÃ HỘI" [level=5] [ref=e152]
            - generic [ref=e153]:
              - link "Facebook" [ref=e154] [cursor=pointer]:
                - /url: "#"
              - link "Instagram" [ref=e157] [cursor=pointer]:
                - /url: "#"
              - link "TikTok" [ref=e161] [cursor=pointer]:
                - /url: "#"
              - link "YouTube" [ref=e164] [cursor=pointer]:
                - /url: "#"
        - generic [ref=e168]:
          - generic [ref=e169]: © 2026 THE XUONG SPORT. ALL RIGHTS RESERVED.
          - link "Về chúng tôi" [ref=e171] [cursor=pointer]:
            - /url: /about
  - generic [ref=e172]:
    - generic "Chat với tư vấn viên" [ref=e173] [cursor=pointer]
    - button "Ẩn nút chat" [ref=e176] [cursor=pointer]: ×
    - generic: Chat tư vấn
```

# Test source

```ts
  1  | import { test, expect } from '@playwright/test';
  2  | 
  3  | test.describe('Product Flow', () => {
  4  |   test('TC_ST_PROD_01: Load giao diện trang chủ, hiển thị đủ 10 SP mới nhất', async ({ page }) => {
  5  |     // 1. Mở trang chủ
  6  |     await page.goto('http://localhost:5173/');
  7  |     
  8  |     // 2. Đợi danh sách Sản phẩm mới load xong
  9  |     // Wait for actual product links to appear (not skeleton)
> 10 |     await page.waitForSelector('a[href^="/product/"]', { timeout: 10000 });
     |                ^ TimeoutError: page.waitForSelector: Timeout 10000ms exceeded.
  11 | 
  12 |     // 3. Đếm số lượng Product Card hiển thị
  13 |     const productCards = await page.$$('a[href^="/product/"]'); 
  14 |     
  15 |     // 4. Verify
  16 |     expect(productCards.length).toBeGreaterThan(0);
  17 |   });
  18 | 
  19 |   test('TC_ST_PROD_03: Tìm kiếm từ khóa chính xác', async ({ page }) => {
  20 |     await page.goto('http://localhost:5173/');
  21 |     
  22 |     // 1. Bấm vào thanh Search bar (Header.vue has an input type="text")
  23 |     const searchInputs = page.locator('input[type="text"]');
  24 |     // Assuming search is the first or only text input in header on home
  25 |     await searchInputs.first().fill('Nike');
  26 |     
  27 |     // 2. Nhấn Enter
  28 |     await page.keyboard.press('Enter');
  29 |     
  30 |     // 3. Kiểm tra danh sách kết quả trả về
  31 |     await page.waitForURL(/.*\/products\?search=Nike/i);
  32 |     
  33 |     // 4. Verify
  34 |     await expect(page.locator('h1')).toContainText('DANH SÁCH SẢN PHẨM');
  35 |   });
  36 | 
  37 |   test('TC_ST_PROD_08: Lọc sản phẩm theo Thương hiệu', async ({ page }) => {
  38 |     await page.goto('http://localhost:5173/products');
  39 |     
  40 |     // 1. Mở Menu Brand, chọn hãng Adidas (sidebar link)
  41 |     await page.click('a[href="/products?brand=adidas"]');
  42 |     
  43 |     // 2. Đợi URL thay đổi và danh sách load xong
  44 |     await page.waitForURL('**/products?brand=adidas');
  45 |     
  46 |     // Wait for the active filter chip to appear
  47 |     await expect(page.locator('text=Thương hiệu: ADIDAS')).toBeVisible();
  48 | 
  49 |     // 3. Check cards to ensure they all say ADIDAS (if the brand name is visible on UI)
  50 |     // Looking at ProductCard.vue (assuming it has brand info)
  51 |     const brandLabels = await page.locator('.product-brand-label').allTextContents();
  52 |     
  53 |     if (brandLabels.length > 0) {
  54 |       for (const brand of brandLabels) {
  55 |         expect(brand.toLowerCase()).toContain('adidas');
  56 |       }
  57 |     }
  58 |   });
  59 | });
  60 | 
```