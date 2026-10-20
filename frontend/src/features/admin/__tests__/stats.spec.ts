import { describe, it, expect } from 'vitest'
import type { AdminDashboard, EventSummary, ModerationLog } from '@/api'
import { eventStatusSlices, eventsByWeek, moderationSlices, topBy } from '@/features/admin/stats'

const ev = (starts_at: string) => ({ starts_at }) as EventSummary

describe('eventStatusSlices', () => {
  it('берёт счётчики сводки', () => {
    const d = {
      published_events: 7,
      cancelled_events: 1,
      unpublished_events: 2,
      deleted_events: 0,
    } as AdminDashboard
    expect(eventStatusSlices(d).map((s) => s.value)).toEqual([7, 1, 2, 0])
  })
})

describe('topBy', () => {
  it('сортирует по убыванию и пропускает пустые', () => {
    const r = topBy(['Бег', 'Йога', 'Бег', null, ' ', 'Бег', 'Йога', 'Шахматы'], (x) => x)
    expect(r).toEqual([
      { key: 'Бег', label: 'Бег', value: 3 },
      { key: 'Йога', label: 'Йога', value: 2 },
      { key: 'Шахматы', label: 'Шахматы', value: 1 },
    ])
  })

  it('сворачивает хвост в «Другие»', () => {
    const r = topBy(['а', 'а', 'б', 'в', 'г'], (x) => x, 2)
    expect(r.map((s) => [s.label, s.value])).toEqual([
      ['а', 2],
      ['б', 1],
      ['Другие', 2],
    ])
  })
})

describe('eventsByWeek', () => {
  // Среда, 21 октября 2026.
  const now = new Date(2026, 9, 21, 12)

  it('восемь недель с понедельника, текущая — четвёртая', () => {
    const weeks = eventsByWeek([], now)
    expect(weeks).toHaveLength(8)
    expect(weeks.findIndex((w) => w.current)).toBe(3)
    expect(weeks[3]!.label).toBe('19 окт')
  })

  it('раскладывает мероприятия по неделям, чужие отбрасывает', () => {
    const weeks = eventsByWeek(
      [
        ev(new Date(2026, 9, 19, 0, 30).toISOString()),
        ev(new Date(2026, 9, 25, 23).toISOString()),
        ev(new Date(2026, 9, 26, 9).toISOString()),
        ev(new Date(2026, 8, 1).toISOString()),
        ev(new Date(2027, 0, 1).toISOString()),
      ],
      now,
    )
    expect(weeks.map((w) => w.value)).toEqual([0, 0, 0, 2, 1, 0, 0, 0])
  })
})

describe('moderationSlices', () => {
  it('считает действия и подписывает по-русски', () => {
    const logs = [
      { action: 'comment_hidden' },
      { action: 'org_approved' },
      { action: 'comment_hidden' },
    ] as ModerationLog[]
    expect(moderationSlices(logs)).toEqual([
      { key: 'comment_hidden', label: 'Скрыто сообщений', value: 2 },
      { key: 'org_approved', label: 'Допущено организаций', value: 1 },
    ])
  })
})
