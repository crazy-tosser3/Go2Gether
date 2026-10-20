import { expect, test } from '@playwright/test'
import { PETR, loginAs, start } from './helpers'

test('телефон: из нижней панели находим мероприятие и записываемся', async ({ page }) => {
  await start(page)
  await loginAs(page, PETR)

  const tabs = page.getByRole('navigation', { name: 'Основные разделы' })
  await expect(tabs).toBeVisible()
  await tabs.getByRole('link', { name: 'Мероприятия' }).click()
  await expect(page.getByRole('heading', { level: 1 })).toHaveText('Мероприятия')

  await page.getByRole('link', { name: 'Йога на траве' }).click()
  await page.getByRole('button', { name: 'Записаться' }).click()
  await expect(page.getByRole('main')).toContainText('Вы участник')

  // Страница не шире экрана.
  const overflow = await page.evaluate(
    () => document.documentElement.scrollWidth - document.documentElement.clientWidth,
  )
  expect(overflow).toBe(0)
})
