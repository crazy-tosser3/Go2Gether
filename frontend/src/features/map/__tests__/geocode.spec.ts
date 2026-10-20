import { describe, it, expect } from 'vitest'
import { geocodeQuery, parseGeocode } from '@/features/map/geocode'

describe('geocodeQuery', () => {
  it('добавляет город, если его нет в адресе', () => {
    expect(geocodeQuery(' Центральный парк ', 'Красноярск')).toBe('Красноярск, Центральный парк')
    expect(geocodeQuery('Красноярск, Мира 1', 'Красноярск')).toBe('Красноярск, Мира 1')
    expect(geocodeQuery('Мира 1', '')).toBe('Мира 1')
  })
})

describe('parseGeocode', () => {
  it('берёт первую точку, lon → lng', () => {
    expect(
      parseGeocode({ result: { items: [{}, { point: { lat: 56.01, lon: 92.85 } }] } }),
    ).toEqual({ lat: 56.01, lng: 92.85 })
  })

  it('ничего не нашли — null', () => {
    expect(parseGeocode({ meta: { code: 404 } })).toBeNull()
    expect(parseGeocode(null)).toBeNull()
  })
})
