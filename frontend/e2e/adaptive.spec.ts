import { expect, test, type Page } from '@playwright/test'
import { ADMIN, ANNA, go, loginAs, start } from './helpers'

// Ширины из требований к адаптиву: маленький телефон, планшет, большой монитор и телефон боком.
const VIEWPORTS = [
  { name: '320', width: 320, height: 640 },
  { name: '768', width: 768, height: 1024 },
  { name: '1920', width: 1920, height: 1080 },
  { name: 'телефон боком', width: 812, height: 375 },
  // Английские подписи длиннее — узкий экран проверяем и на нём.
  { name: '320, English', width: 320, height: 640, locale: 'en' },
]

const USER_ROUTES = [
  '/',
  '/events',
  '/events?sort=seats',
  '/events/1',
  '/events/new',
  '/me',
  '/me/events',
  '/profile',
  '/organizations',
  '/organizations/1',
  '/privacy',
]
const ADMIN_ROUTES = ['/admin', '/admin/organizations', '/admin/events']

async function overflow(page: Page) {
  // Ответы моков идут через Service Worker — «сеть затихла» их не ждёт; ждём, пока уйдут заглушки загрузки.
  await page.waitForFunction(() => !document.querySelector('.animate-pulse'), null, {
    timeout: 5000,
  })
  return page.evaluate(
    () => document.documentElement.scrollWidth - document.documentElement.clientWidth,
  )
}

for (const vp of VIEWPORTS) {
  test(`адаптив ${vp.name}: нет горизонтальной прокрутки`, async ({ page }) => {
    await page.setViewportSize({ width: vp.width, height: vp.height })
    if (vp.locale) await page.addInitScript((l) => localStorage.setItem('locale', l), vp.locale)
    await start(page)
    const wide: string[] = []

    await loginAs(page, ANNA, vp.locale === 'en' ? 'Sign in as' : undefined)
    for (const route of USER_ROUTES) {
      await go(page, route)
      if ((await overflow(page)) > 0) wide.push(route)
    }

    await loginAs(page, ADMIN, vp.locale === 'en' ? 'Sign in as' : undefined)
    for (const route of ADMIN_ROUTES) {
      await go(page, route)
      if ((await overflow(page)) > 0) wide.push(route)
    }

    expect(wide).toEqual([])
  })
}
