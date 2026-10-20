import { describe, expect, it } from 'vitest'
import {
  activeQuickDate,
  countFilters,
  dateLabel,
  filterChips,
  filtersFromQuery,
  filtersToQuery,
  withoutFilter,
} from '../filters'

describe('фильтры каталога', () => {
  it('читает адресную строку и отбрасывает мусор', () => {
    expect(
      filtersFromQuery({
        q: ' бег ',
        sport: '3',
        level: 'god',
        join_mode: 'open',
        date_from: '2026-10-24',
        date_to: 'завтра',
        page: '0',
      }),
    ).toEqual({ q: 'бег', sport: 3, join_mode: 'open', date_from: '2026-10-24' })
  })

  it('пишет в адресную строку только заданное, без страниц', () => {
    expect(filtersToQuery({ q: 'йога', page: 2, sport: 3, level: 'beginner' })).toEqual({
      q: 'йога',
      sport: '3',
      level: 'beginner',
    })
  })

  it('узнаёт быструю дату', () => {
    const now = new Date(2026, 9, 21, 12)
    expect(activeQuickDate({ date_from: '2026-10-24', date_to: '2026-10-25' }, now)).toBe('weekend')
    expect(activeQuickDate({ date_from: '2026-10-24' }, now)).toBeNull()
  })

  it('считает выбранные фильтры без текста поиска', () => {
    expect(
      countFilters({ q: 'бег', sport: 1, date_from: 'x', date_to: 'y', join_mode: 'open' }),
    ).toBe(3)
  })
})

describe('чипы выбранных фильтров', () => {
  const now = new Date('2026-10-20T12:00:00')
  const sportName = (id: number) => (id === 3 ? 'Бег' : undefined)

  it('подписывают даты по-человечески', () => {
    expect(dateLabel({ date_from: '2026-10-21', date_to: '2026-10-21' }, now)).toBe('Завтра')
    expect(dateLabel({ date_from: '2026-10-29', date_to: '2026-10-29' }, now)).toBe('29 октября')
    expect(dateLabel({}, now)).toBeNull()
  })

  it('по чипу на каждый выбранный фильтр, поиск не считается', () => {
    const chips = filterChips(
      { q: 'йога', sport: 3, city: 'Красноярск', join_mode: 'open', level: 'pro' },
      sportName,
      now,
    )
    expect(chips.map((c) => c.label)).toEqual(['Красноярск', 'Бег', 'Опытный', 'Свободная запись'])
  })

  it('снятие даты убирает оба края', () => {
    expect(
      withoutFilter({ date_from: '2026-10-24', date_to: '2026-10-25', city: 'Красноярск' }, 'date'),
    ).toEqual({ city: 'Красноярск' })
    expect(withoutFilter({ sport: 3, level: 'pro' }, 'sport')).toEqual({ level: 'pro' })
  })
})
