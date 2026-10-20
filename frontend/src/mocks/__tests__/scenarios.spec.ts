import { afterAll, beforeAll, beforeEach, describe, expect, it } from 'vitest'
import { setupServer } from 'msw/node'
import {
  adminApi,
  ApiError,
  authApi,
  commentsApi,
  eventsApi,
  organizationsApi,
  participationsApi,
  recommendationsApi,
  safetyApi,
  sportsApi,
  usersApi,
} from '@/api'
import type { EventInput } from '@/api'
import { handlers } from '../handlers'
import { resetDb } from '../db'
import { E, O, SP, U } from '../seed'

const server = setupServer(...handlers)

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
beforeEach(() => {
  resetDb()
})
afterAll(() => server.close())

// Вход держится в cookie (MSW хранит её между запросами, как браузер).
async function loginAs(userId: number) {
  await authApi.login({ demo_account_id: userId })
}

const fail = (promise: Promise<unknown>) =>
  promise.then(
    () => null,
    (e: unknown) => e as ApiError,
  )

const tomorrow = () => new Date(Date.now() + 24 * 3600 * 1000).toISOString()

const eventInput = (extra: Partial<EventInput> = {}): EventInput => ({
  title: 'Скандинавская ходьба в парке',
  description: 'Палки дадим',
  sport_name: 'Скандинавская ходьба',
  starts_at: tomorrow(),
  address: 'Красноярск, парк Гагарина',
  join_mode: 'open',
  ...extra,
})

const ids = (items: { id: number }[]) => items.map((i) => i.id)
const names = (list: { participants: { user: { name: string } }[] }) =>
  list.participants.map((p) => p.user.name)

describe('демо-аккаунты как у бэка', () => {
  it('пять аккаунтов с числовыми id, вход по id', async () => {
    const accounts = await authApi.demoAccounts()
    expect(accounts.map((a) => a.id)).toEqual([U.anna, U.petr, U.igor, U.olga, U.admin])
    expect(accounts[4]).toMatchObject({ role: 'admin', email: 'admin@demo.local' })
    await loginAs(U.igor)
    const me = await authApi.me()
    expect(me.organizations).toEqual([
      expect.objectContaining({ id: O.enisey, status: 'approved' }),
    ])
  })
})

describe('С1: создание мероприятия пользователем', () => {
  it('создаёт событие с новым направлением, другой пользователь его находит', async () => {
    await loginAs(U.anna)
    const created = await eventsApi.create(eventInput())
    expect(created).toMatchObject({ status: 'published', is_organizer: true })

    await loginAs(U.petr)
    const feed = await eventsApi.list({ q: 'скандинавская' })
    expect(ids(feed.items)).toContain(created.id)
    const event = await eventsApi.get(created.id)
    expect(event.sport).toMatchObject({ name: 'Скандинавская ходьба', is_custom: true })
    expect(event.organizer).toMatchObject({ type: 'user', id: U.anna, name: 'Анна Смирнова' })
    expect(event.is_organizer).toBe(false)
  })

  it('не плодит дубли направления при другом написании', async () => {
    await loginAs(U.anna)
    await eventsApi.create(eventInput())
    await eventsApi.create(eventInput({ sport_name: '  скандинавская  ХОДЬБА ' }))
    expect(await sportsApi.list('скандинав')).toHaveLength(1)
  })

  it('возвращает ошибки по полям', async () => {
    await loginAs(U.anna)
    const error = await fail(
      eventsApi.create(eventInput({ title: '', starts_at: '2000-01-01T00:00:00Z' })),
    )
    expect(error).toMatchObject({ status: 422, code: 'VALIDATION_ERROR' })
    expect(error?.fields).toMatchObject({
      title: expect.any(String),
      starts_at: expect.any(String),
    })
  })
})

describe('С2: допуск организации', () => {
  it('до допуска публиковать нельзя, после одобрения событие видно на странице организации', async () => {
    await loginAs(U.olga)
    const pending = (await organizationsApi.my()).find((o) => o.id === O.ladya)!
    expect(pending.status).toBe('pending')
    const denied = await fail(eventsApi.create(eventInput({ organization_id: O.ladya })))
    expect(denied?.status).toBe(403)

    await loginAs(U.anna)
    expect((await fail(organizationsApi.get(O.ladya)))?.status).toBe(404)

    await loginAs(U.admin)
    await adminApi.approveOrganization(O.ladya)

    await loginAs(U.olga)
    const created = await eventsApi.create(
      eventInput({ organization_id: O.ladya, sport_id: SP.chess, sport_name: undefined }),
    )
    expect(created.organizer).toMatchObject({ type: 'organization', id: O.ladya })

    await loginAs(U.anna)
    expect((await organizationsApi.get(O.ladya)).status).toBe('approved')
    expect(ids(await organizationsApi.events(O.ladya))).toContain(created.id)
  })

  it('отклонение требует причину, организация её видит и может подать снова', async () => {
    await loginAs(U.admin)
    expect((await fail(adminApi.rejectOrganization(O.ladya, ' ')))?.status).toBe(422)
    await adminApi.rejectOrganization(O.ladya, 'Добавьте контакты')

    await loginAs(U.olga)
    const mine = (await organizationsApi.my()).find((o) => o.id === O.ladya)
    expect(mine).toMatchObject({ status: 'rejected', reject_reason: 'Добавьте контакты' })
    expect((await organizationsApi.resubmit(O.ladya)).status).toBe('pending')
  })

  it('решения админа попадают в журнал', async () => {
    await loginAs(U.admin)
    await adminApi.approveOrganization(O.ladya)
    const log = await adminApi.auditLog()
    expect(log.items[0]).toMatchObject({ action: 'org_approved', target_id: O.ladya })
  })
})

describe('С3: свободная запись', () => {
  it('пользователь сразу участник, организатор видит его в составе', async () => {
    await loginAs(U.petr)
    const result = await eventsApi.join(E.yoga)
    expect(result.participation.status).toBe('confirmed')
    expect(result.event.confirmed_count).toBe(1)
    expect((await eventsApi.get(E.yoga)).my_participation?.status).toBe('confirmed')

    await loginAs(U.anna)
    const list = await eventsApi.participants(E.yoga)
    expect(names(list)).toContain('Пётр Иванов')
    expect(list.participants[0]!.email).toBe('user2@demo.local')
  })

  it('не даёт записаться дважды и на своё событие', async () => {
    await loginAs(U.petr)
    await eventsApi.join(E.yoga)
    expect(await fail(eventsApi.join(E.yoga))).toMatchObject({
      status: 409,
      code: 'ALREADY_JOINED',
    })

    await loginAs(U.anna)
    expect((await fail(eventsApi.join(E.yoga)))?.code).toBe('OWN_EVENT')
  })
})

describe('С4: запись по заявке', () => {
  it('заявка ждёт, организатор принимает — статус обновляется у обеих сторон', async () => {
    await loginAs(U.anna)
    const petr = (await eventsApi.requests(E.football)).find((p) => p.user.name === 'Пётр Иванов')!
    await participationsApi.approve(petr.participation_id)
    expect(names(await eventsApi.participants(E.football))).toContain('Пётр Иванов')

    await loginAs(U.petr)
    expect((await eventsApi.get(E.football)).my_participation?.status).toBe('confirmed')
  })

  it('отклонение с причиной видно пользователю, повторная заявка возможна', async () => {
    await loginAs(U.petr)
    expect((await eventsApi.join(E.runInterval, 'Хочу попробовать')).participation.status).toBe(
      'pending',
    )

    await loginAs(U.igor)
    const req = (await eventsApi.requests(E.runInterval)).find(
      (p) => p.user.name === 'Пётр Иванов',
    )!
    await participationsApi.reject(
      req.participation_id,
      'Пока рано, приходите на утреннюю пробежку',
    )
    expect((await fail(participationsApi.approve(req.participation_id)))?.code).toBe(
      'ALREADY_DECIDED',
    )

    await loginAs(U.petr)
    const mine = (await eventsApi.get(E.runInterval)).my_participation
    expect(mine).toMatchObject({
      status: 'rejected',
      decision_reason: 'Пока рано, приходите на утреннюю пробежку',
    })
    expect((await eventsApi.join(E.runInterval)).participation.status).toBe('pending')
  })

  it('принять заявку может только организатор, не админ', async () => {
    await loginAs(U.admin)
    expect((await fail(participationsApi.approve(4)))?.status).toBe(403)
  })
})

describe('С5: обсуждение', () => {
  it('вопрос и ответ организатора различимы по авторству', async () => {
    await loginAs(U.petr)
    await commentsApi.create(E.yoga, { text: 'Коврик обязателен?' })
    await loginAs(U.anna)
    await commentsApi.create(E.yoga, { text: 'Можно плед' })

    const { items } = await commentsApi.list(E.yoga)
    expect(items.map((c) => [c.author.name, c.is_organizer])).toEqual([
      ['Пётр Иванов', false],
      ['Анна Смирнова', true],
    ])
  })

  it('скрытое сообщение приходит без текста, остальное обсуждение на месте', async () => {
    await loginAs(U.admin)
    await adminApi.hideComment(1, 'Не по теме')
    await authApi.logout()
    const { items } = await commentsApi.list(E.football)
    expect(items.find((c) => c.id === 1)).toMatchObject({ is_hidden: true, text: null })
    expect(items.find((c) => c.id === 2)?.text).toBeTruthy()
  })
})

describe('С6: снятие с публикации', () => {
  it('событие пропадает из ленты, по прямой ссылке — 410 с причиной, записаться нельзя', async () => {
    await loginAs(U.admin)
    await adminApi.unpublishEvent(E.yoga, 'Неверное место')

    await loginAs(U.petr)
    expect(ids((await eventsApi.list()).items)).not.toContain(E.yoga)
    const gone = await fail(eventsApi.get(E.yoga))
    expect(gone?.isGone).toBe(true)
    expect(gone?.message).toContain('Неверное место')
    expect((await fail(eventsApi.join(E.yoga)))?.status).toBe(410)
  })

  it('админ видит снятое событие в своём разделе и не может удалить без причины', async () => {
    await loginAs(U.admin)
    const unpublished = await adminApi.events({ status: 'unpublished' })
    expect(ids(unpublished.items)).toContain(E.cycling)
    expect((await fail(adminApi.deleteEvent(E.yoga, '')))?.status).toBe(422)
  })
})

describe('пагинация как у бэка', () => {
  it('страницы с нуля', async () => {
    const first = await eventsApi.list({ size: 4 })
    const second = await eventsApi.list({ size: 4, page: 1 })
    expect(first).toMatchObject({ page: 0, size: 4, total: 9, total_pages: 3 })
    expect(ids(first.items)).not.toEqual(expect.arrayContaining(ids(second.items)))
  })
})

describe('152-ФЗ: удаление аккаунта (как у бэка)', () => {
  const register = (email: string) =>
    authApi.register({ name: 'Вера Новикова', email, password: 'Secret123!', consent: true })

  it('по паролю обезличивает профиль, отменяет записи и закрывает вход', async () => {
    const { user } = await register('vera@example.ru')
    await eventsApi.join(E.yoga)
    await usersApi.deleteMe('Secret123!')

    expect((await fail(authApi.me()))?.status).toBe(401)
    expect((await fail(usersApi.get(user.id)))?.status).toBe(404)
    expect(
      (await fail(authApi.login({ email: 'vera@example.ru', password: 'Secret123!' })))?.status,
    ).toBe(401)

    await loginAs(U.anna)
    expect(names(await eventsApi.participants(E.yoga))).not.toContain('Вера Новикова')
  })

  it('без верного пароля и для демо-аккаунтов — отказ', async () => {
    await register('vera@example.ru')
    expect(await fail(usersApi.deleteMe('не тот'))).toMatchObject({
      status: 403,
      message: 'Неверный пароль',
    })
    await loginAs(U.petr)
    expect(await fail(usersApi.deleteMe('Demo1234!'))).toMatchObject({
      status: 403,
      message: 'Демо-аккаунт удалить нельзя',
    })
  })

  // Предположение: на бэке проверить не на чем — демо-владельцев удалить нельзя.
  it('владельца организации не удаляет', async () => {
    await register('vera@example.ru')
    await organizationsApi.create({ name: 'Клуб Веры', type: 'club', contacts: '+7 900 000-00-99' })
    expect(await fail(usersApi.deleteMe('Secret123!'))).toMatchObject({
      status: 409,
      code: 'HAS_ORGANIZATIONS',
    })
  })
})

describe('рекомендации (как у бэка)', () => {
  it('без своих и тех, куда уже записан; с причинами-кодами', async () => {
    await loginAs(U.anna)
    const mine = await usersApi.myParticipations()
    const list = await recommendationsApi.events({ limit: 50 })
    const ids = list.map((r) => r.event.id)
    expect(
      list.every((r) => r.event.organizer.id !== U.anna || r.event.organizer.type !== 'user'),
    ).toBe(true)
    for (const p of mine) expect(ids).not.toContain(p.event.id)
    expect(list.some((r) => r.reasons.includes('interest'))).toBe(true)
  })

  it('похожие — без самого мероприятия, тот же вид спорта первым', async () => {
    const { items } = await eventsApi.list({ size: 100 })
    const base = items.find((e) => items.some((o) => o.id !== e.id && o.sport.id === e.sport.id))!
    const similar = await recommendationsApi.similar(base.id, 10)
    expect(similar.map((r) => r.event.id)).not.toContain(base.id)
    expect(similar[0]!.reasons[0]).toBe('same_sport')
  })
})

describe('памятки безопасности', () => {
  it('публично — только опубликованные, по виду спорта — его и общие', async () => {
    const all = await safetyApi.list()
    expect(all.every((b) => b.published)).toBe(true)
    const running = await safetyApi.list({ sport: SP.running })
    expect(running.every((b) => b.sport_id === null || b.sport_id === SP.running)).toBe(true)
    expect(running.some((b) => b.sport_id === SP.running)).toBe(true)
    const details = await safetyApi.get(all[0]!.id)
    expect(details.slides.length).toBeGreaterThan(0)
  })

  it('админ создаёт черновик, публикует и удаляет', async () => {
    expect((await fail(adminApi.safetyBanners()))?.status).toBe(401)
    await loginAs(U.admin)
    const created = await adminApi.createSafetyBanner({
      title: 'Жара',
      category: 'safety_rules',
      published: false,
      slides: [{ body: 'Пейте воду' }],
    })
    expect((await safetyApi.list()).map((b) => b.id)).not.toContain(created.id)
    await adminApi.publishSafetyBanner(created.id)
    expect((await safetyApi.list()).map((b) => b.id)).toContain(created.id)
    await adminApi.deleteSafetyBanner(created.id)
    expect((await fail(safetyApi.get(created.id)))?.status).toBe(404)
  })

  it('не админ в раздел не попадает', async () => {
    await loginAs(U.anna)
    expect((await fail(adminApi.safetyBanners()))?.status).toBe(403)
  })
})
