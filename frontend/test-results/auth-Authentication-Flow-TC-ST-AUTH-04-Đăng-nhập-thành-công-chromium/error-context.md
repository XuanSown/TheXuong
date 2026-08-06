# Instructions

- Following Playwright test failed.
- Explain why, be concise, respect Playwright best practices.
- Provide a snippet of code with the fix, if possible.

# Test info

- Name: auth.spec.ts >> Authentication Flow >> TC_ST_AUTH_04: Đăng nhập thành công
- Location: tests\e2e\auth.spec.ts:29:3

# Error details

```
Error: expect(page).toHaveURL(expected) failed

Expected pattern: /.*\/cart/
Received string:  "http://localhost:5173/login"
Timeout: 5000ms

Call log:
  - Expect "toHaveURL" with timeout 5000ms
    13 × locator resolved to <html lang="vi">…</html>
       - unexpected value "http://localhost:5173/login"

```

```yaml
- paragraph: Welcome Back!
- img
- textbox "Email của bạn": user@example.com
- img
- textbox "Mật khẩu": user123
- button "Toggle password visibility":
  - img
- link "Quên mật khẩu?":
  - /url: /forgot-password
- button "ĐĂNG NHẬP"
- text: HOẶC
- button "Đăng nhập bằng Google":
  - img
  - text: Đăng nhập bằng Google
- text: Chưa có tài khoản?
- link "Đăng ký ngay":
  - /url: /register
- img
- button "Ẩn nút chat": ×
- text: Chat tư vấn
```

# Test source

```ts
  1  | import { test, expect } from '@playwright/test';
  2  | 
  3  | test.describe('Authentication Flow', () => {
  4  |   const timestamp = Date.now();
  5  |   const testEmail = `auto${timestamp}@example.com`;
  6  |   const testPass = 'Password123';
  7  | 
  8  |   test('TC_ST_AUTH_01: Đăng ký tài khoản', async ({ page }) => {
  9  |     // 1. Truy cập trang /register
  10 |     await page.goto('http://localhost:5173/register');
  11 |     
  12 |     // 2. Nhập thông tin hợp lệ
  13 |     await page.fill('input[type="text"]', `Auto User ${timestamp}`);
  14 |     await page.fill('input[type="email"]', testEmail);
  15 |     
  16 |     // Fill both password and confirmPassword
  17 |     const passInputs = page.locator('input[type="password"]');
  18 |     await passInputs.nth(0).fill(testPass);
  19 |     await passInputs.nth(1).fill(testPass);
  20 |     
  21 |     // 3. Click nút Đăng ký
  22 |     await page.click('button[type="submit"]');
  23 |     
  24 |     // 4. Kiểm tra URL redirect sang /login và có thông báo success
  25 |     await expect(page).toHaveURL(/.*\/login\?registered=success/);
  26 |     await expect(page.locator('text=Đăng ký thành công')).toBeVisible();
  27 |   });
  28 | 
  29 |   test('TC_ST_AUTH_04: Đăng nhập thành công', async ({ page }) => {
  30 |     // Note: Assuming the DB has a seeded user or we use one created above. 
  31 |     // We will use a fallback valid user 'user@example.com' 'user123' if the one above wasn't created.
  32 |     
  33 |     // 1. Truy cập trang /login
  34 |     await page.goto('http://localhost:5173/login');
  35 |     
  36 |     // 2. Nhập Email và Password hợp lệ
  37 |     // For test reliability, we use a known user or the one just registered. 
  38 |     // If backend is running, `user@example.com` / `user123` is the standard seed.
  39 |     await page.fill('input[type="email"]', 'user@example.com');
  40 |     await page.fill('input[type="password"]', 'user123');
  41 |     
  42 |     // 3. Bấm Đăng nhập
  43 |     await page.click('button[type="submit"]');
  44 |     
  45 |     // 4. Xác minh giao diện Header hiển thị Avatar User (chuyển sang /cart hoặc home)
> 46 |     await expect(page).toHaveURL(/.*\/cart/); // By default, User role redirects to /cart
     |                        ^ Error: expect(page).toHaveURL(expected) failed
  47 |     await expect(page.locator('.lucide-user')).toBeVisible(); // Avatar icon
  48 |   });
  49 | });
  50 | 
```