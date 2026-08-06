import { test, expect } from '@playwright/test';

test.describe('Authentication Flow', () => {
  const timestamp = Date.now();
  const testEmail = `auto${timestamp}@example.com`;
  const testPass = 'Password123';

  test('TC_ST_AUTH_01: Đăng ký tài khoản', async ({ page }) => {
    // 1. Truy cập trang /register
    await page.goto('http://localhost:5173/register');
    
    // 2. Nhập thông tin hợp lệ
    await page.fill('input[type="text"]', `Auto User ${timestamp}`);
    await page.fill('input[type="email"]', testEmail);
    
    // Fill both password and confirmPassword
    const passInputs = page.locator('input[type="password"]');
    await passInputs.nth(0).fill(testPass);
    await passInputs.nth(1).fill(testPass);
    
    // 3. Click nút Đăng ký
    await page.click('button[type="submit"]');
    
    // 4. Kiểm tra URL redirect sang /login và có thông báo success
    await expect(page).toHaveURL(/.*\/login\?registered=success/);
    await expect(page.locator('text=Đăng ký thành công')).toBeVisible();
  });

  test('TC_ST_AUTH_04: Đăng nhập thành công', async ({ page }) => {
    // Note: Assuming the DB has a seeded user or we use one created above. 
    // We will use a fallback valid user 'user@example.com' 'user123' if the one above wasn't created.
    
    // 1. Truy cập trang /login
    await page.goto('http://localhost:5173/login');
    
    // 2. Nhập Email và Password hợp lệ
    // For test reliability, we use a known user or the one just registered. 
    // If backend is running, `user@example.com` / `user123` is the standard seed.
    await page.fill('input[type="email"]', 'user@example.com');
    await page.fill('input[type="password"]', 'user123');
    
    // 3. Bấm Đăng nhập
    await page.click('button[type="submit"]');
    
    // 4. Xác minh giao diện Header hiển thị Avatar User (chuyển sang /cart hoặc home)
    await expect(page).toHaveURL(/.*\/cart/); // By default, User role redirects to /cart
    await expect(page.locator('.lucide-user')).toBeVisible(); // Avatar icon
  });
});
