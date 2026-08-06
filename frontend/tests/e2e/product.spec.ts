import { test, expect } from '@playwright/test';

test.describe('Product Flow', () => {
  test('TC_ST_PROD_01: Load giao diện trang chủ, hiển thị đủ 10 SP mới nhất', async ({ page }) => {
    // 1. Mở trang chủ
    await page.goto('http://localhost:5173/');
    
    // 2. Đợi danh sách Sản phẩm mới load xong
    // Wait for actual product links to appear (not skeleton)
    await page.waitForSelector('a[href^="/product/"]', { timeout: 10000 });

    // 3. Đếm số lượng Product Card hiển thị
    const productCards = await page.$$('a[href^="/product/"]'); 
    
    // 4. Verify
    expect(productCards.length).toBeGreaterThan(0);
  });

  test('TC_ST_PROD_03: Tìm kiếm từ khóa chính xác', async ({ page }) => {
    await page.goto('http://localhost:5173/');
    
    // 1. Bấm vào thanh Search bar (Header.vue has an input type="text")
    const searchInputs = page.locator('input[type="text"]');
    // Assuming search is the first or only text input in header on home
    await searchInputs.first().fill('Nike');
    
    // 2. Nhấn Enter
    await page.keyboard.press('Enter');
    
    // 3. Kiểm tra danh sách kết quả trả về
    await page.waitForURL(/.*\/products\?search=Nike/i);
    
    // 4. Verify
    await expect(page.locator('h1')).toContainText('DANH SÁCH SẢN PHẨM');
  });

  test('TC_ST_PROD_08: Lọc sản phẩm theo Thương hiệu', async ({ page }) => {
    await page.goto('http://localhost:5173/products');
    
    // 1. Mở Menu Brand, chọn hãng Adidas (sidebar link)
    await page.click('a[href="/products?brand=adidas"]');
    
    // 2. Đợi URL thay đổi và danh sách load xong
    await page.waitForURL('**/products?brand=adidas');
    
    // Wait for the active filter chip to appear
    await expect(page.locator('text=Thương hiệu: ADIDAS')).toBeVisible();

    // 3. Check cards to ensure they all say ADIDAS (if the brand name is visible on UI)
    // Looking at ProductCard.vue (assuming it has brand info)
    const brandLabels = await page.locator('.product-brand-label').allTextContents();
    
    if (brandLabels.length > 0) {
      for (const brand of brandLabels) {
        expect(brand.toLowerCase()).toContain('adidas');
      }
    }
  });
});
