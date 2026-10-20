import { afterAll, beforeAll, beforeEach, describe, expect, it } from 'vitest'
import { setupServer } from 'msw/node'
import { adminApi, ApiError, authApi, eventsApi, usersApi } from '@/api'
import { handlers } from '../handlers'
import { resetDb } from '../db'
import { E, U } from '../seed'

const server = setupServer(...handlers)

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
beforeEach(() => resetDb())
afterAll(() => server.close())

const loginAs = (id: number) => authApi.login({ demo_account_id: id })
const fail = (p: Promise<unknown>) =>
  p.then(
    () => null,
    (e: unknown) => e as ApiError,
  )

describe('жалобы на мероприятия (как в reports.md бэка)', () => {
  it('подача, повтор, своё мероприятие и «Другое» без комментария', async () => {
    await loginAs(U.petr)
    const report = await eventsApi.report(E.football, {
      reason: 'misleading',
      comment: 'Адрес неверный',
    })
    expect(report).toMatchObject({ status: 'pending', reason: 'misleading' })
    expect((await fail(eventsApi.report(E.football, { reason: 'spam' })))?.code).toBe(
      'ALREADY_REPORTED',
    )
    expect(
      (await fail(eventsApi.report(E.runMorning, { reason: 'other' })))?.fields.comment,
    ).toBeTruthy()
    expect((await usersApi.myReports()).map((r) => r.id)).toContain(report.id)

    await loginAs(U.anna)
    expect((await fail(eventsApi.report(E.football, { reason: 'spam' })))?.code).toBe('OWN_EVENT')
  })

  it('принятие снимает мероприятие и закрывает все жалобы на него', async () => {
    await loginAs(U.petr)
    await eventsApi.report(E.runMorning, { reason: 'dangerous' })
    await loginAs(U.admin)
    expect((await adminApi.dashboard()).pending_reports).toBe(2)
    const queue = await adminApi.reports({ status: 'pending' })
    const accepted = await adminApi.acceptReport(queue.items[0]!.id, 'Место небезопасно')
    expect(accepted.status).toBe('accepted')
    expect((await adminApi.reports({ status: 'pending' })).total).toBe(0)
    expect((await fail(adminApi.rejectReport(accepted.id)))?.code).toBe('ALREADY_REVIEWED')

    await authApi.logout()
    expect((await fail(eventsApi.get(E.runMorning)))?.status).toBe(410)
  })

  it('отклонение оставляет мероприятие опубликованным', async () => {
    await loginAs(U.admin)
    const [first] = (await adminApi.reports({ status: 'pending' })).items
    const rejected = await adminApi.rejectReport(first!.id, 'Нарушений нет')
    expect(rejected).toMatchObject({ status: 'rejected', resolution_note: 'Нарушений нет' })
    expect((await eventsApi.get(E.runMorning)).status).toBe('published')
  })
})
