import asyncio
import re
from playwright import async_api
from playwright.async_api import expect

async def run_test():
    pw = None
    browser = None
    context = None

    try:
        # Start a Playwright session in asynchronous mode
        pw = await async_api.async_playwright().start()

        # Launch a Chromium browser in headless mode with custom arguments
        browser = await pw.chromium.launch(
            headless=True,
            args=[
                "--window-size=1280,720",
                "--disable-dev-shm-usage",
                "--ipc=host",
                "--single-process"
            ],
        )

        # Create a new browser context (like an incognito window)
        context = await browser.new_context()
        # Wider default timeout to match the agent's DOM-stability budget;
        # auto-waiting Playwright APIs (expect, locator.wait_for) inherit this.
        context.set_default_timeout(15000)

        # Open a new page in the browser context
        page = await context.new_page()

        # Interact with the page elements to simulate user flow
        # -> navigate
        await page.goto("https://thexuong.xuansown.id.vn")
        try:
            await page.wait_for_load_state("domcontentloaded", timeout=5000)
        except Exception:
            pass
        
        # -> Open the products listing page (click the 'SẢN PHẨM' link or navigate to the Products page) so product cards are visible.
        await page.goto("https://thexuong.xuansown.id.vn/products")
        try:
            await page.wait_for_load_state("domcontentloaded", timeout=5000)
        except Exception:
            pass
        
        # -> Click the product titled 'Áo Đấu Sân Khách Đội Tuyển Đức 26 Phiên Bản Authentic' from the product listing to open its product detail page.
        # Áo Đấu Sân Khách Đội Tuyển Đức 26 Phiên Bản...
        elem = page.locator('xpath=/html/body/div/div/div/div/main/div/main/section[2]/section/div/div/div/div[2]/h3')
        await elem.click(timeout=10000)
        
        # --> Assertions to verify final state
        
        # --> Verify the product detail page is displayed
        # Assert: The URL indicates the product detail page is displayed.
        await expect(page).to_have_url(re.compile("/product\\-detail/"), timeout=15000), "The URL indicates the product detail page is displayed."
        await page.locator("xpath=/html/body/div[1]/div/div/div/main/div/main/section/div/div[1]/div[1]/div[1]/img").nth(0).scroll_into_view_if_needed()
        # Assert: The main product image is visible on the product detail page.
        await expect(page.locator("xpath=/html/body/div[1]/div/div/div/main/div/main/section/div/div[1]/div[1]/div[1]/img").nth(0)).to_be_visible(timeout=15000), "The main product image is visible on the product detail page."
        # Assert: The product image alt text matches the product title.
        await expect(page.locator("xpath=/html/body/div[1]/div/div/div/main/div/main/section/div/div[1]/div[1]/div[1]/img").nth(0)).to_have_attribute("alt", "\u00c1o \u0110\u1ea5u S\u00e2n Kh\u00e1ch \u0110\u1ed9i Tuy\u1ec3n \u0110\u1ee9c 26 Phi\u00ean B\u1ea3n Authentic", timeout=15000), "The product image alt text matches the product title."
        await page.locator("xpath=/html/body/div[1]/div/div/div/main/div/main/section/div/div[2]/div[4]/button[1]").nth(0).scroll_into_view_if_needed()
        # Assert: The Add to Cart button is visible on the product detail page.
        await expect(page.locator("xpath=/html/body/div[1]/div/div/div/main/div/main/section/div/div[2]/div[4]/button[1]").nth(0)).to_be_visible(timeout=15000), "The Add to Cart button is visible on the product detail page."
        
        # --> Verify product information is displayed
        # Assert: The product title appears in the main product image alt attribute.
        await expect(page.locator("xpath=/html/body/div[1]/div/div/div/main/div/main/section/div/div[1]/div[1]/div[1]/img").nth(0)).to_have_attribute("alt", "\u00c1o \u0110\u1ea5u S\u00e2n Kh\u00e1ch \u0110\u1ed9i Tuy\u1ec3n \u0110\u1ee9c 26 Phi\u00ean B\u1ea3n Authentic", timeout=15000), "The product title appears in the main product image alt attribute."
        await page.locator("xpath=/html/body/div[1]/div/div/div/main/div/main/section/div/div[2]/div[4]/button[1]").nth(0).scroll_into_view_if_needed()
        # Assert: The Add to Cart button is visible on the product page.
        await expect(page.locator("xpath=/html/body/div[1]/div/div/div/main/div/main/section/div/div[2]/div[4]/button[1]").nth(0)).to_be_visible(timeout=15000), "The Add to Cart button is visible on the product page."
        await page.locator("xpath=/html/body/div[1]/div/div/div/main/div/main/section/div/div[2]/div[2]/div[2]/button[1]").nth(0).scroll_into_view_if_needed()
        # Assert: A size selection option (XL) is visible on the product page.
        await expect(page.locator("xpath=/html/body/div[1]/div/div/div/main/div/main/section/div/div[2]/div[2]/div[2]/button[1]").nth(0)).to_be_visible(timeout=15000), "A size selection option (XL) is visible on the product page."
        await asyncio.sleep(5)

    finally:
        if context:
            await context.close()
        if browser:
            await browser.close()
        if pw:
            await pw.stop()

asyncio.run(run_test())
    