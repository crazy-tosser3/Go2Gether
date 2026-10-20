import { describe, it, expect } from 'vitest'
import { distanceKm, formatDistance, inBounds, visibleNearby } from '@/features/map/nearby'
import type { LngLat } from '@/features/map/geo'

// Центр Красноярска и точка на острове Татышев — около 3,5 км.
const center: LngLat = [92.8526, 56.0106]
const tatyshev: LngLat = [92.9, 56.03]

describe('distanceKm', () => {
  it('считает расстояние по поверхности', () => {
    expect(distanceKm(center, center)).toBe(0)
    expect(distanceKm(center, tatyshev)).toBeGreaterThan(3)
    expect(distanceKm(center, tatyshev)).toBeLessThan(4)
  })
})

describe('formatDistance', () => {
  it('метры, километры с запятой, крупно — целыми', () => {
    expect(formatDistance(0.344)).toBe('340 м')
    expect(formatDistance(0.001)).toBe('10 м')
    expect(formatDistance(1.25)).toBe('1,3 км')
    expect(formatDistance(14.6)).toBe('15 км')
  })
})

describe('inBounds / visibleNearby', () => {
  const bounds = { southWest: [92.8, 56.0] as LngLat, northEast: [92.88, 56.02] as LngLat }

  it('только то, что видно на карте, ближайшие первыми', () => {
    const items = [
      { id: 1, coordinates: tatyshev },
      { id: 2, coordinates: [92.86, 56.012] as LngLat },
      { id: 3, coordinates: [92.81, 56.001] as LngLat },
    ]
    expect(inBounds(tatyshev, bounds)).toBe(false)
    expect(visibleNearby(items, bounds, center).map((i) => i.id)).toEqual([2, 3])
  })

  it('без рамки — все, по расстоянию', () => {
    const items = [
      { id: 1, coordinates: tatyshev },
      { id: 2, coordinates: center },
    ]
    expect(visibleNearby(items, null, center).map((i) => i.id)).toEqual([2, 1])
  })
})
