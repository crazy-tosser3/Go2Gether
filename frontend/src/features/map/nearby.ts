import type { LngLat } from './geo'
import { intlLocale, t } from '@/i18n'

export interface Bounds {
  southWest: LngLat
  northEast: LngLat
}

export function inBounds([lng, lat]: LngLat, { southWest, northEast }: Bounds): boolean {
  return lng >= southWest[0] && lng <= northEast[0] && lat >= southWest[1] && lat <= northEast[1]
}

// Расстояние по поверхности Земли (формула гаверсинуса), в километрах.
export function distanceKm([lng1, lat1]: LngLat, [lng2, lat2]: LngLat): number {
  const R = 6371
  const rad = (d: number) => (d * Math.PI) / 180
  const dLat = rad(lat2 - lat1)
  const dLng = rad(lng2 - lng1)
  const a =
    Math.sin(dLat / 2) ** 2 + Math.cos(rad(lat1)) * Math.cos(rad(lat2)) * Math.sin(dLng / 2) ** 2
  return 2 * R * Math.asin(Math.sqrt(a))
}

/** «350 м», «1,2 км», «15 км». */
export function formatDistance(km: number): string {
  if (km < 1) return t('map.m', { n: Math.max(10, Math.round((km * 1000) / 10) * 10) })
  const n = new Intl.NumberFormat(intlLocale(), { maximumFractionDigits: km < 10 ? 1 : 0 })
  return t('map.km', { n: n.format(km) })
}

/** Мероприятия в видимой части карты — ближайшие к точке отсчёта первыми. */
export function visibleNearby<T extends { coordinates: LngLat }>(
  items: T[],
  bounds: Bounds | null,
  origin: LngLat,
): (T & { distance: number })[] {
  return items
    .filter((i) => !bounds || inBounds(i.coordinates, bounds))
    .map((i) => ({ ...i, distance: distanceKm(origin, i.coordinates) }))
    .sort((a, b) => a.distance - b.distance)
}
