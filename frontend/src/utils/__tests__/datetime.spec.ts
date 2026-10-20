import { describe, expect, it } from 'vitest'
import { formatEventWhen, localDate, quickDateRange } from '../datetime'

// Среда, 21 октября 2026, 12:00 по местному времени.
const now = new Date(2026, 9, 21, 12, 0)

describe('formatEventWhen', () => {
  it('сегодня и завтра — словами', () => {
    expect(formatEventWhen(new Date(2026, 9, 21, 19, 0).toISOString(), null, now)).toBe(
      'Сегодня · 19:00',
    )
    expect(formatEventWhen(new Date(2026, 9, 22, 7, 30).toISOString(), null, now)).toBe(
      'Завтра · 07:30',
    )
  })

  it('дальше — день недели и дата, с интервалом окончания', () => {
    const text = formatEventWhen(
      new Date(2026, 9, 24, 10, 0).toISOString(),
      new Date(2026, 9, 24, 11, 30).toISOString(),
      now,
    )
    expect(text).toMatch(/^Сб, 24 окт\.? · 10:00–11:30$/)
  })
})

describe('quickDateRange', () => {
  it('сегодня и завтра', () => {
    expect(quickDateRange('today', now)).toEqual({ from: '2026-10-21', to: '2026-10-21' })
    expect(quickDateRange('tomorrow', now)).toEqual({ from: '2026-10-22', to: '2026-10-22' })
  })

  it('выходные — ближайшие суббота и воскресенье', () => {
    expect(quickDateRange('weekend', now)).toEqual({ from: '2026-10-24', to: '2026-10-25' })
  })

  it('в субботу и воскресенье — оставшиеся дни выходных', () => {
    expect(quickDateRange('weekend', new Date(2026, 9, 24, 9))).toEqual({
      from: '2026-10-24',
      to: '2026-10-25',
    })
    expect(quickDateRange('weekend', new Date(2026, 9, 25, 9))).toEqual({
      from: '2026-10-25',
      to: '2026-10-25',
    })
  })

  it('переход через месяц', () => {
    expect(quickDateRange('tomorrow', new Date(2026, 9, 31, 9))).toEqual({
      from: '2026-11-01',
      to: '2026-11-01',
    })
    expect(localDate(new Date(2027, 0, 1))).toBe('2027-01-01')
  })
})
