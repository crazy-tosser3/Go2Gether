import { describe, it, expect } from 'vitest'
import {
  countByDay,
  formatMonth,
  monthGrid,
  monthOf,
  monthRange,
  shiftMonth,
} from '@/features/events/calendar'

const oct = { year: 2026, month: 9 }

describe('monthGrid', () => {
  it('октябрь 2026: с понедельника 28 сентября, 5 недель', () => {
    const grid = monthGrid(oct)
    expect(grid).toHaveLength(5)
    expect(grid[0]![0]).toEqual({ date: '2026-09-28', day: 28, inMonth: false })
    expect(grid[0]![3]).toEqual({ date: '2026-10-01', day: 1, inMonth: true })
    expect(grid[4]![6]).toEqual({ date: '2026-11-01', day: 1, inMonth: false })
  })

  it('каждая неделя — 7 дней, все дни месяца на месте', () => {
    const grid = monthGrid({ year: 2027, month: 1 })
    expect(grid.every((w) => w.length === 7)).toBe(true)
    expect(grid.flat().filter((d) => d.inMonth)).toHaveLength(28)
  })
})

describe('monthRange / shiftMonth / monthOf', () => {
  it('границы месяца включительно', () => {
    expect(monthRange(oct)).toEqual({ from: '2026-10-01', to: '2026-10-31' })
  })

  it('листает через год', () => {
    expect(shiftMonth({ year: 2026, month: 11 }, 1)).toEqual({ year: 2027, month: 0 })
    expect(shiftMonth({ year: 2026, month: 0 }, -1)).toEqual({ year: 2025, month: 11 })
  })

  it('месяц выбранной даты', () => {
    expect(monthOf('2026-10-24')).toEqual(oct)
  })
})

describe('countByDay', () => {
  it('считает мероприятия по местной дате начала', () => {
    const at = (s: string) => ({ starts_at: new Date(s).toISOString() })
    expect(
      countByDay([at('2026-10-24T10:00:00'), at('2026-10-24T19:00:00'), at('2026-10-25T08:00:00')]),
    ).toEqual({ '2026-10-24': 2, '2026-10-25': 1 })
  })
})

describe('formatMonth', () => {
  it('«Октябрь 2026»', () => {
    expect(formatMonth(oct)).toBe('Октябрь 2026')
  })
})
