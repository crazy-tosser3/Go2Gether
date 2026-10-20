import { describe, it, expect } from 'vitest'
import { parseCitySuggest } from '@/features/map/citySuggest'

const city = (name: string, region: string, country = 'Россия') => ({
  name,
  type: 'adm_div',
  subtype: 'city',
  adm_div: [
    { type: 'country', name: country },
    { type: 'region', name: region },
  ],
})

describe('parseCitySuggest', () => {
  it('города России с регионом, без повторов и чужих стран', () => {
    expect(
      parseCitySuggest({
        result: {
          items: [
            city('Красноярск', 'Красноярский край'),
            city('Краснодар', 'Краснодарский край'),
            city('Красноярск', 'Красноярский край'),
            city('Кракен', 'Где-то', 'Казахстан'),
            { name: 'Красная площадь', type: 'branch' },
          ],
        },
      }),
    ).toEqual([
      { name: 'Красноярск', region: 'Красноярский край' },
      { name: 'Краснодар', region: 'Краснодарский край' },
    ])
  })

  it('пустой или битый ответ — пустой список', () => {
    expect(parseCitySuggest({ meta: { code: 404 } })).toEqual([])
    expect(parseCitySuggest(null)).toEqual([])
  })
})
