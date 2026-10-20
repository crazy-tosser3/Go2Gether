import { describe, it, expect } from 'vitest'
import type { AdminDashboard, EventSummary, ModerationLog } from '@/api'
import { exportFileName, statsCsv, toCsv } from '@/features/admin/export'

describe('toCsv', () => {
  it('BOM, «;» и перевод строки CRLF', () => {
    expect(
      toCsv([
        ['а', 1],
        ['б', null],
      ]),
    ).toBe('﻿а;1\r\nб;\r\n')
  })

  it('экранирует кавычки, разделитель и переносы', () => {
    expect(toCsv([['Бег; утро', 'клуб "Енисей"', 'две\nстроки']])).toBe(
      '﻿"Бег; утро";"клуб ""Енисей""";"две\nстроки"\r\n',
    )
  })

  it('не даёт подсунуть формулу', () => {
    expect(toCsv([['=HYPERLINK("x")', '-5', '+7', '@a']])).toBe(
      `﻿"'=HYPERLINK(""x"")";'-5;'+7;'@a\r\n`,
    )
  })
})

describe('statsCsv', () => {
  const dashboard = {
    users: 8,
    pending_organizations: 1,
    approved_organizations: 2,
    published_events: 5,
    cancelled_events: 0,
    unpublished_events: 1,
    deleted_events: 0,
    hidden_comments: 3,
  } as AdminDashboard
  const events = [
    {
      id: 7,
      title: 'Пробежка',
      sport: { name: 'Бег' },
      city: 'Красноярск',
      starts_at: '2026-10-24T08:00:00',
      status: 'published',
      confirmed_count: 3,
      capacity: null,
      join_mode: 'open',
      organizer: { type: 'user', name: 'Анна Смирнова' },
    },
    {
      id: 8,
      title: 'Турнир',
      sport: { name: 'Шахматы' },
      city: 'Красноярск',
      starts_at: '2026-10-25T12:00:00',
      status: 'cancelled',
      confirmed_count: 10,
      capacity: 16,
      join_mode: 'approval',
      organizer: { type: 'organization', name: 'Клуб «Ладья»' },
    },
  ] as EventSummary[]
  const logs = [{ action: 'org_approved' }] as ModerationLog[]
  const csv = statsCsv(dashboard, events, logs, new Date(2026, 9, 20, 15, 30))
  const lines = csv.slice(1).split('\r\n')

  it('сводка, разрезы и мероприятия', () => {
    expect(lines).toContain('Выгружено;20.10.2026, 15:30')
    expect(lines).toContain('Пользователей;8')
    expect(lines).toContain('Красноярск;2')
    expect(lines).toContain('Допущено организаций;1')
    expect(lines).toContain(
      '7;Пробежка;Бег;Красноярск;24.10.2026, 08:00;Опубликовано;3;без ограничений;свободная;Частное лицо',
    )
    expect(lines).toContain(
      '8;Турнир;Шахматы;Красноярск;25.10.2026, 12:00;Отменено организатором;10;16;по заявке;Клуб «Ладья»',
    )
  })

  it('имени частного организатора в файле нет', () => {
    expect(csv).not.toContain('Анна Смирнова')
  })

  it('без сводки — только разрезы и список', () => {
    expect(statsCsv(null, [], [], new Date())).not.toContain('Пользователей')
  })
})

describe('exportFileName', () => {
  it('дата в имени файла', () => {
    expect(exportFileName(new Date(2026, 0, 5))).toBe('go2gether-statistika-2026-01-05.csv')
  })
})
