import { expect, test } from '@playwright/test';

type Availability = { productId: number; availableQuantity: number; reservedQuantity: number };

test('customer can register, purchase, receive confirmation, review, and cancel', async ({ page, request }) => {
  const unique = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
  const email = `e2e-${unique}@example.com`;

  await page.goto('/register');
  await page.getByLabel('Email').fill(email);
  await page.getByLabel('Password').fill('OneStop!234');
  await page.getByLabel('First name').fill('E2E');
  await page.getByLabel('Last name').fill('Customer');
  await page.getByLabel('Phone').fill('5550101234');
  await page.getByRole('button', { name: 'Create account' }).click();
  await expect(page).toHaveURL(/\/profile$/);
  await expect(page.getByText(new RegExp(email))).toBeVisible();

  await page.goto('/products');
  const productCards = page.locator('.product-card');
  await expect(productCards).not.toHaveCount(0);
  const card = productCards.first();
  const productHref = await card.locator('a.product-name').getAttribute('href');
  expect(productHref).toMatch(/^\/products\/\d+$/);
  const productId = Number(productHref!.split('/').pop());

  const beforeResponse = await request.get(`http://localhost:8080/api/inventory/products/${productId}`);
  expect(beforeResponse.ok()).toBeTruthy();
  const before = await beforeResponse.json() as Availability;
  expect(before.availableQuantity).toBeGreaterThan(0);

  await card.getByRole('button', { name: 'Add to cart' }).click();
  await expect(card.getByRole('button', { name: /Added/ })).toBeVisible();
  await page.getByRole('link', { name: /Cart/ }).click();
  await expect(page.getByRole('heading', { name: 'Your cart' })).toBeVisible();
  await page.getByRole('link', { name: /Proceed to checkout/ }).click();

  await page.getByLabel('Recipient name').fill('E2E Customer');
  await page.getByLabel('Phone').fill('5550101234');
  await page.getByLabel('Address line 1').fill('1 Test Avenue');
  await page.getByLabel('City').fill('Chicago');
  await page.getByLabel('State').fill('IL');
  await page.getByLabel('Postal code').fill('60601');
  await page.getByLabel('Country').fill('US');
  await page.getByRole('button', { name: /Place order/ }).click();

  await expect(page).toHaveURL(/\/orders\/\d+$/);
  await expect(page.getByText('Order placed!')).toBeVisible();
  await expect(page.getByText('CONFIRMED', { exact: true })).toBeVisible();
  const orderId = Number(page.url().split('/').pop());

  await expect.poll(async () => {
    const response = await request.get(`http://localhost:8086/internal/notifications/orders/${orderId}`);
    if (!response.ok()) return 0;
    const rows = await response.json() as unknown[];
    return rows.length;
  }, { timeout: 20_000 }).toBe(1);

  await page.getByRole('link', { name: 'Orders', exact: true }).click();
  await expect(page.getByRole('link', { name: `Order #${orderId}` })).toBeVisible();
  await page.getByRole('link', { name: `Order #${orderId}` }).click();
  await page.getByRole('button', { name: 'Cancel order' }).click();
  await expect(page.getByText('CANCELLED', { exact: true })).toBeVisible();

  await expect.poll(async () => {
    const response = await request.get(`http://localhost:8080/api/inventory/products/${productId}`);
    const current = await response.json() as Availability;
    return current.availableQuantity;
  }).toBe(before.availableQuantity);
});
