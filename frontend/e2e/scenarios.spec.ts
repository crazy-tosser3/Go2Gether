import { expect, test } from '@playwright/test'
import { ADMIN, ANNA, IGOR, OLGA, PETR, go, localDate, loginAs, start } from './helpers'

// Шесть сценариев проверки из кейса, на моках. Номера мероприятий — из демо-данных (src/mocks/seed.ts).
const EVENT = { football: 2, runInterval: 6, yoga: 8 }
const ORG = { ladya: 2 }

test.beforeEach(async ({ page }) => {
  await start(page)
})

test('С1: пользователь создаёт мероприятие со своим направлением, другой его находит', async ({
  page,
}) => {
  await loginAs(page, ANNA)
  await go(page, '/events/new')

  await page.getByLabel('Название').fill('Скандинавская ходьба в парке')
  await page.getByRole('combobox', { name: 'Направление' }).fill('Скандинавская ходьба')
  await page.getByRole('option', { name: '+ Добавить «Скандинавская ходьба»' }).click()
  await page.getByLabel('Дата').fill(localDate(2))
  await page.getByLabel('Начало').fill('10:00')
  await page.getByLabel('Адрес').fill('Красноярск, парк Гагарина')
  await page.getByRole('radio', { name: /Свободно/ }).check()
  await page.getByRole('button', { name: 'Опубликовать' }).click()

  await expect(page.getByRole('heading', { level: 1 })).toHaveText('Скандинавская ходьба в парке')

  await loginAs(page, PETR)
  await go(page, '/events')
  await page.getByRole('searchbox').fill('скандинавская')
  const card = page.getByRole('link', { name: 'Скандинавская ходьба в парке' })
  await expect(card).toBeVisible()
  await card.click()
  await expect(page.getByRole('heading', { level: 1 })).toHaveText('Скандинавская ходьба в парке')
  await expect(page.getByRole('main')).toContainText('Скандинавская ходьба')
})

test('С2: организация проходит допуск и публикует мероприятие от своего имени', async ({
  page,
}) => {
  await loginAs(page, OLGA)
  await go(page, '/organizations/my')
  await expect(page.getByRole('main')).toContainText('Шахматный клуб «Ладья»')
  await expect(page.getByRole('main')).toContainText('На рассмотрении')
  // Отклонённая заявка видна владельцу с причиной.
  await expect(page.getByRole('main')).toContainText('Добавьте описание занятий')

  await loginAs(page, PETR)
  await go(page, `/organizations/${ORG.ladya}`)
  await expect(page.getByRole('main')).toContainText(/не найдена|не опубликована/i)

  await loginAs(page, ADMIN)
  await go(page, `/admin/organizations?id=${ORG.ladya}`)
  await page.getByRole('button', { name: 'Допустить' }).click()
  await expect(page.getByRole('main')).toContainText('Допущена')

  await loginAs(page, OLGA)
  await go(page, '/events/new')
  // Сама радиокнопка скрыта — переключатель нарисован подписью, кликаем по ней, как пользователь.
  await page.locator('label', { hasText: 'Шахматный клуб «Ладья»' }).click()
  await expect(page.getByRole('radio', { name: 'Шахматный клуб «Ладья»' })).toBeChecked()
  await page.getByLabel('Название').fill('Блиц-турнир клуба')
  await page.getByRole('combobox', { name: 'Направление' }).fill('Шахматы')
  await page.getByRole('option', { name: 'Шахматы', exact: true }).click()
  await page.getByLabel('Дата').fill(localDate(3))
  await page.getByLabel('Начало').fill('12:00')
  await page.getByLabel('Адрес').fill('Красноярск, библиотека')
  await page.getByRole('radio', { name: /Свободно/ }).check()
  await page.getByRole('button', { name: 'Опубликовать' }).click()
  await expect(page.getByRole('heading', { level: 1 })).toHaveText('Блиц-турнир клуба')

  await loginAs(page, PETR)
  await go(page, `/organizations/${ORG.ladya}`)
  await expect(page.getByRole('heading', { level: 1 })).toHaveText('Шахматный клуб «Ладья»')
  await expect(page.getByRole('main')).toContainText('Блиц-турнир клуба')
})

test('С3: свободная запись — сразу участник, организатор видит его', async ({ page }) => {
  await loginAs(page, PETR)
  await go(page, `/events/${EVENT.yoga}`)
  await page.getByRole('button', { name: 'Записаться' }).click()
  await expect(page.getByRole('main')).toContainText('Вы участник')

  await loginAs(page, ANNA)
  await go(page, `/events/${EVENT.yoga}/manage`)
  await page.getByRole('button', { name: /Участники/ }).click()
  await expect(page.getByRole('main')).toContainText(PETR)
})

test('С4: заявка — организатор принимает, статус обновляется у обеих сторон', async ({ page }) => {
  await loginAs(page, PETR)
  await go(page, `/events/${EVENT.runInterval}`)
  await page.getByRole('button', { name: 'Подать заявку' }).click()
  await page.getByRole('button', { name: 'Отправить заявку' }).click()
  await expect(page.getByRole('main')).toContainText('Ждёт решения')

  await loginAs(page, IGOR)
  await go(page, `/events/${EVENT.runInterval}/manage`)
  const request = page.getByRole('listitem').filter({ hasText: PETR })
  await request.getByRole('button', { name: 'Принять' }).click()
  await page.getByRole('button', { name: /Участники/ }).click()
  await expect(page.getByRole('main')).toContainText(PETR)

  await loginAs(page, PETR)
  await go(page, `/events/${EVENT.runInterval}`)
  await expect(page.getByRole('main')).toContainText('Вы участник')
})

test('С4: заявка — организатор отклоняет с причиной, пользователь её видит', async ({ page }) => {
  await loginAs(page, ANNA)
  await go(page, `/events/${EVENT.football}/manage`)
  const request = page.getByRole('listitem').filter({ hasText: PETR })
  await request.getByRole('button', { name: 'Отклонить' }).click()
  const dialog = page.getByRole('dialog')
  await dialog.getByLabel('Комментарий').fill('Состав уже набран')
  await dialog.getByRole('button', { name: 'Отклонить' }).click()
  await page.getByRole('button', { name: /Отказы/ }).click()
  await expect(page.getByRole('main')).toContainText(PETR)

  await loginAs(page, PETR)
  await go(page, `/events/${EVENT.football}`)
  await expect(page.getByRole('main')).toContainText('Заявка отклонена')
  await expect(page.getByRole('main')).toContainText('Состав уже набран')
})

test('С5: вопрос в обсуждении и ответ организатора различимы по авторству', async ({ page }) => {
  await loginAs(page, PETR)
  await go(page, `/events/${EVENT.yoga}`)
  await page.getByPlaceholder('Задать вопрос организатору…').fill('Коврик обязателен?')
  await page.getByRole('button', { name: 'Отправить сообщение' }).click()
  await expect(page.getByRole('main')).toContainText('Коврик обязателен?')

  await loginAs(page, ANNA)
  await go(page, `/events/${EVENT.yoga}`)
  await page.getByRole('button', { name: 'Ответить' }).first().click()
  await page.getByPlaceholder('Ответьте участникам или добавьте уточнение…').fill('Можно плед')
  await page.getByRole('button', { name: 'Отправить сообщение' }).click()

  const answer = page.getByRole('listitem').filter({ hasText: 'Можно плед' }).last()
  await expect(answer).toContainText(ANNA)
  await expect(answer).toContainText('Организатор')
})

test('С6: админ снимает мероприятие — его нет в выдаче, записаться нельзя даже по ссылке', async ({
  page,
}) => {
  await loginAs(page, ADMIN)
  await go(page, `/admin/events?id=${EVENT.yoga}`)
  await page.getByRole('button', { name: 'Снять', exact: true }).click()
  const dialog = page.getByRole('dialog')
  await dialog.getByLabel('Причина').fill('Неверное место')
  await dialog.getByRole('button', { name: 'Снять' }).click()
  await expect(page.getByRole('main')).toContainText('Снято с публикации')

  await loginAs(page, PETR)
  await go(page, '/events')
  await expect(page.getByRole('main')).toContainText('Дворовый футбол')
  await expect(page.getByRole('link', { name: 'Йога на траве' })).toHaveCount(0)

  await go(page, `/events/${EVENT.yoga}`)
  await expect(page.getByRole('heading', { level: 1 })).toHaveText('Мероприятие недоступно')
  await expect(page.getByRole('main')).toContainText('Неверное место')
  await expect(page.getByRole('button', { name: 'Записаться' })).toHaveCount(0)
})
