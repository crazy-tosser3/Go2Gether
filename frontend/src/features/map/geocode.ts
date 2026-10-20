import { mapKey } from './mapgl'

export interface GeoPoint {
  lat: number
  lng: number
}

// Геокодер 2ГИС по тому же ключу, что и карта; город добавляем, чтобы «Центральный парк» искался в нужном городе.
export function geocodeQuery(address: string, city: string): string {
  const a = address.trim()
  const c = city.trim()
  return c && !a.toLowerCase().includes(c.toLowerCase()) ? `${c}, ${a}` : a
}

export function parseGeocode(body: unknown): GeoPoint | null {
  const items = (body as { result?: { items?: { point?: { lat?: number; lon?: number } }[] } })
    ?.result?.items
  const point = items?.find((i) => typeof i.point?.lat === 'number')?.point
  return point && typeof point.lon === 'number' ? { lat: point.lat!, lng: point.lon } : null
}

/** Точка по адресу; null — не нашли. Бросает ошибку, если геокодер недоступен. */
export async function geocode(address: string, city: string): Promise<GeoPoint | null> {
  const key = mapKey()
  if (!key) throw new Error('no-key')
  const url = new URL('https://catalog.api.2gis.com/3.0/items/geocode')
  url.searchParams.set('q', geocodeQuery(address, city))
  url.searchParams.set('fields', 'items.point')
  url.searchParams.set('key', key)
  const res = await fetch(url)
  if (!res.ok) throw new Error(`geocode ${res.status}`)
  return parseGeocode(await res.json())
}
