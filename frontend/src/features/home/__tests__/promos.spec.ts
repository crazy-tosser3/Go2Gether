import { describe, it, expect } from 'vitest'
import { activePromos, promoPeriod, type Promo } from '@/features/home/promos'

const promo = (id: string, starts: string, ends: string) => ({ id, starts, ends }) as Promo
const list = [promo('a', '2026-10-01', '2026-10-31'), promo('b', '2026-11-01', '2026-11-30')]

describe('activePromos', () => {
  it('показывает только идущие акции', () => {
    expect(activePromos(new Date(2026, 9, 20, 15), list).map((p) => p.id)).toEqual(['a'])
  })

  it('первый и последний день включительно', () => {
    expect(activePromos(new Date(2026, 9, 31, 23, 59), list).map((p) => p.id)).toEqual(['a'])
    expect(activePromos(new Date(2026, 10, 1, 0, 1), list).map((p) => p.id)).toEqual(['b'])
  })

  it('после всех акций — пусто', () => {
    expect(activePromos(new Date(2027, 0, 1), list)).toEqual([])
  })
})

describe('promoPeriod', () => {
  it('пишет дату окончания', () => {
    expect(promoPeriod(promo('a', '2026-10-01', '2026-11-30'))).toBe('до 30 ноября')
  })
})
