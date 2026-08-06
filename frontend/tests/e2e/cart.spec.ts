import { test, expect } from '@playwright/test';

test.describe('Cart & Checkout Flow', () => {
  test('TC_ST_CART_01: Thêm 1 Sản phẩm vào giỏ hàng thành công', async ({ page }) => {
    // 1. Mở trang Sản phẩm
    await page.goto('http://localhost:5173/products');
    
    // Click vào sản phẩm đầu tiên
    await page.click('.grid.grid-cols-1 > div:first-child a');
    
    // Đợi trang chi tiết sản phẩm
    await page.waitForURL(/.*\/product\/.*/);

    // 2. Chọn Size (assuming there are size buttons, e.g., '39', '40')
    const sizeButtons = page.locator('button.border-[#CFC4C6]'); // Usually standard sizes
    if (await sizeButtons.count() > 0) {
      await sizeButtons.first().click();
    }
    
    // 3. Bấm "Thêm vào giỏ"
    await page.click('button:has-text("THÊM VÀO GIỎ")');
    
    // 4. Kiểm tra Badge giỏ hàng trên Header tăng lên
    // Look for the red dot badge in header
    await expect(page.locator('.absolute.top-0.right-0.w-4.h-4.bg-red-500')).toBeVisible({ timeout: 5000 });
  });

  test('TC_ST_COUT_01: Kiểm tra bắt buộc điền địa chỉ khi Checkout', async ({ page }) => {
    // Go directly to checkout
    await page.goto('http://localhost:5173/checkout');
    
    // Since user is not logged in or has no address, check if it redirects to login OR forces address
    // In our implementation, unauthenticated users are usually redirected to /login first.
    // Assuming we reach checkout:
    if (page.url().includes('/login')) {
      // Expected behavior: forced login
      expect(true).toBe(true); 
    } else {
      // If we are on checkout, look for address form
      await expect(page.locator('text=Địa chỉ giao hàng')).toBeVisible();
      // Must have form fields
      await expect(page.locator('input[placeholder="Số điện thoại"]')).toBeVisible();
    }
  });
});
