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
        
        # -> Click the 'SẢN PHẨM' link in the top navigation to open the products page.
        # SẢN PHẨM link
        elem = page.get_by_role('link', name='SẢN PHẨM', exact=True)
        await elem.click(timeout=10000)
        
        # -> Click the 'Bóng đá' sport filter to filter products by football and verify the product grid updates.
        # Bóng đá link
        elem = page.get_by_role('link', name='Bóng đá', exact=True)
        await elem.click(timeout=10000)
        
        # -> Click the 'Nike' brand filter to apply the brand filter and observe whether the product grid updates.
        # Nike link
        elem = page.get_by_role('link', name='Nike', exact=True)
        await elem.click(timeout=10000)
        
        # --> Assertions to verify final state
        
        # --> Verify the product grid is displayed
        await page.locator("xpath=/html/body/div[1]/div/div/div/main/div/main/section[2]/section/div[2]/div/div[1]").nth(0).scroll_into_view_if_needed()
        # Assert: Expected the product grid to be visible (a product card should be displayed).
        await expect(page.locator("xpath=/html/body/div[1]/div/div/div/main/div/main/section[2]/section/div[2]/div/div[1]").nth(0)).to_be_visible(timeout=15000), "Expected the product grid to be visible (a product card should be displayed)."
        
        # --> Verify updated search results are displayed
        # Assert: Expected the URL to include the sport filter parameter 'sport=football'.
        await expect(page).to_have_url(re.compile("sport=football"), timeout=15000), "Expected the URL to include the sport filter parameter 'sport=football'."
        await asyncio.sleep(5)

    finally:
        if context:
            await context.close()
        if browser:
            await browser.close()
        if pw:
            await pw.stop()

asyncio.run(run_test())
    