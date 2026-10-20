import { describe, it, expect } from 'vitest'
import { reasonLabels } from '@/features/events/recommendations'

describe('reasonLabels', () => {
  it('две самые весомые причины по-человечески', () => {
    expect(reasonLabels(['interest', 'same_city', 'soon'])).toEqual([
      'Ваш интерес',
      'В вашем городе',
    ])
  })

  it('неизвестные коды бэка пропускает', () => {
    expect(reasonLabels(['weather_good', 'popular'])).toEqual(['Популярное'])
    expect(reasonLabels([])).toEqual([])
  })

  it('для похожих город — «в том же городе»', () => {
    expect(reasonLabels(['same_sport', 'same_city'], 'similar')).toEqual([
      'Тот же вид спорта',
      'В том же городе',
    ])
    expect(reasonLabels(['nearby'], 'similar')).toEqual(['Неподалёку'])
  })
})
