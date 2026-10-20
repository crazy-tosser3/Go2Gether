import { describe, expect, it } from 'vitest'
import { artFor, sortByArt } from '@/features/safety/storyArt'

describe('artFor', () => {
  it('узнаёт тему без учёта регистра и «ё»', () => {
    expect(artFor('Сердечно-легочная РЕАНИМАЦИЯ')?.cover).toBe('cpr')
    expect(artFor('Жара')).toBeNull()
  })

  it('у каждого слайда известной темы есть иконка', () => {
    expect(artFor('Ушибы и растяжения')?.icons).toHaveLength(4)
  })
})

describe('sortByArt', () => {
  it('порядок макета: реанимация последней из известных, новые темы — после', () => {
    const titles = [
      'Сердечно-лёгочная реанимация',
      'Жара',
      'Разминка перед тренировкой',
      'Ушибы и растяжения',
    ]
    expect(sortByArt(titles.map((title) => ({ title }))).map((b) => b.title)).toEqual([
      'Разминка перед тренировкой',
      'Ушибы и растяжения',
      'Сердечно-лёгочная реанимация',
      'Жара',
    ])
  })
})
