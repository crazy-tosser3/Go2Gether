import { describe, expect, it } from 'vitest'
import { sportIconName } from '../sportIcon'

describe('sportIconName', () => {
  it('подбирает рисунок по названию', () => {
    expect(sportIconName({ name: 'Мини-футбол' })).toBe('football')
    expect(sportIconName({ name: 'Настольный теннис' })).toBe('tennis')
    expect(sportIconName({ name: 'Шахматы' })).toBe('chess')
    expect(sportIconName({ name: 'Велоспорт' })).toBe('bike')
    expect(sportIconName({ name: 'Бег' })).toBe('running')
  })

  it('для своего направления — общий рисунок', () => {
    expect(sportIconName({ name: 'Скандинавская ходьба' })).toBe('custom')
  })
})
