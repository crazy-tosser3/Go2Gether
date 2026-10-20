import { describe, it, expect } from 'vitest'
import { guestPlaceText, visiblePlace } from '@/features/events/place'

describe('visiblePlace', () => {
  const ev = { address: 'Красноярск, ул. Ленина 11', city: 'Красноярск' }

  it('вошедшему — адрес, гостю — только город', () => {
    expect(visiblePlace(ev, true)).toBe('Красноярск, ул. Ленина 11')
    expect(visiblePlace(ev, false)).toBe('Красноярск')
    expect(visiblePlace({ address: 'ул. Ленина 11', city: null }, false)).toBeNull()
  })

  it('подсказка гостю с городом и без', () => {
    expect(guestPlaceText('Красноярск')).toContain('Красноярск')
    expect(guestPlaceText(null)).toBeTruthy()
  })
})
