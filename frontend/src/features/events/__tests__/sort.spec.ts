import { describe, it, expect } from 'vitest'
import type { EventSummary } from '@/api'
import { sortEvents, sortFromQuery, sortToQuery } from '@/features/events/sort'

const ev = (id: number, starts_at: string, confirmed_count: number, capacity: number | null) =>
  ({ id, starts_at, confirmed_count, capacity }) as EventSummary

const list = [
  ev(1, '2026-10-25T10:00', 5, 10), // 5 свободно
  ev(2, '2026-10-21T10:00', 8, 8), // полное
  ev(3, '2026-10-23T10:00', 2, null), // без ограничения
  ev(4, '2026-10-22T10:00', 8, 20), // 12 свободно
]
const ids = (sort: Parameters<typeof sortEvents>[1]) => sortEvents(list, sort).map((e) => e.id)

describe('sortEvents', () => {
  it('ближайшие — по дате начала', () => {
    expect(ids('soon')).toEqual([2, 4, 3, 1])
  })

  it('больше свободных мест: без ограничения первым, полные в конце', () => {
    expect(ids('seats')).toEqual([3, 4, 1, 2])
  })

  it('популярные — по числу участников, при равенстве раньше начало', () => {
    expect(ids('popular')).toEqual([2, 4, 1, 3])
  })

  it('новые — недавно созданные первыми', () => {
    expect(ids('new')).toEqual([4, 3, 2, 1])
  })

  it('исходный список не меняется', () => {
    sortEvents(list, 'new')
    expect(list.map((e) => e.id)).toEqual([1, 2, 3, 4])
  })
})

describe('sort в адресе', () => {
  it('читает известные значения, остальное — ближайшие', () => {
    expect(sortFromQuery({ sort: 'seats' })).toBe('seats')
    expect(sortFromQuery({ sort: 'price' })).toBe('soon')
    expect(sortFromQuery({})).toBe('soon')
  })

  it('порядок по умолчанию в адрес не пишет', () => {
    expect(sortToQuery('soon')).toEqual({})
    expect(sortToQuery('new')).toEqual({ sort: 'new' })
  })
})
