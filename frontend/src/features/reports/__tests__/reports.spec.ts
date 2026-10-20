import { describe, it, expect } from 'vitest'
import type { EventDetails, Report, User } from '@/api'
import {
  canReport,
  parseReportFilter,
  replaceReport,
  reportError,
} from '@/features/reports/reports'

describe('reportError', () => {
  it('причина обязательна', () => {
    expect(reportError(null, '').reason).toBeTruthy()
    expect(reportError('spam', '')).toEqual({})
  })

  it('для «Другое» нужен комментарий', () => {
    expect(reportError('other', '  ').comment).toBeTruthy()
    expect(reportError('other', 'Адрес неактуален')).toEqual({})
  })

  it('комментарий не длиннее 1000', () => {
    expect(reportError('misleading', 'x'.repeat(1001)).comment).toBeTruthy()
  })
})

describe('canReport', () => {
  const event = (extra: Partial<EventDetails> = {}) =>
    ({ status: 'published', is_organizer: false, ...extra }) as EventDetails
  const user = (role: User['role'] = 'user') => ({ role }) as User

  it('только вошедший, не админ, не организатор, опубликованное', () => {
    expect(canReport(event(), user())).toBe(true)
    expect(canReport(event(), null)).toBe(false)
    expect(canReport(event(), user('admin'))).toBe(false)
    expect(canReport(event({ is_organizer: true }), user())).toBe(false)
    expect(canReport(event({ status: 'unpublished' }), user())).toBe(false)
  })
})

describe('очередь', () => {
  it('фильтр из адреса, по умолчанию — новые', () => {
    expect(parseReportFilter('rejected')).toBe('rejected')
    expect(parseReportFilter('x')).toBe('pending')
  })

  it('строка обновляется ответом сервера', () => {
    const list = [
      { id: 1, status: 'pending' },
      { id: 2, status: 'pending' },
    ] as Report[]
    const next = replaceReport(list, { id: 2, status: 'accepted' } as Report)
    expect(next.map((r) => r.status)).toEqual(['pending', 'accepted'])
  })
})
