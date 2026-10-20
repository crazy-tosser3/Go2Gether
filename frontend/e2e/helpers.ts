import { expect, type Page } from '@playwright/test'

// Демо-аккаунты моков (id 1–5 совпадают с бэком).
export const ANNA = 'Анна Смирнова'
export const PETR = 'Пётр Иванов'
export const IGOR = 'Игорь Белов'
export const OLGA = 'Ольга Ким'
export const ADMIN = 'Администратор'

/** Переход внутри приложения: полная загрузка страницы сбросила бы данные моков и вход. */
export async function go(page: Page, path: string) {
  await page.evaluate(async (to) => {
    type VueRoot = HTMLElement & {
      __vue_app__: {
        config: { globalProperties: { $router: { push(to: string): Promise<unknown> } } }
      }
    }
    const root = document.querySelector('#app') as VueRoot
    await root.__vue_app__.config.globalProperties.$router.push(to)
  }, path)
  await page.waitForLoadState('networkidle')
}

export async function start(page: Page) {
  await page.goto('/')
  await expect(page.getByRole('heading', { level: 1 })).toBeVisible()
}

/** Вход или смена роли через экран входа, как на демо. */
export async function loginAs(page: Page, name: string, prefix = 'Войти:') {
  await go(page, '/login')
  await page.getByRole('button', { name: `${prefix} ${name}` }).click()
  await expect(page).not.toHaveURL(/\/login/)
}

export function localDate(daysAhead: number) {
  const d = new Date()
  d.setDate(d.getDate() + daysAhead)
  return [
    d.getFullYear(),
    String(d.getMonth() + 1).padStart(2, '0'),
    String(d.getDate()).padStart(2, '0'),
  ].join('-')
}
