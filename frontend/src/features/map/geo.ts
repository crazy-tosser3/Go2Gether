import type { EventSummary } from '@/api'

export type LngLat = [number, number]

// Центр Красноярска — по умолчанию, если у событий нет координат.
export const DEFAULT_CENTER: LngLat = [92.8526, 56.0106]

export interface MapPoint {
  id: number
  title: string
  coordinates: LngLat
}

type WithCoords = Pick<EventSummary, 'id' | 'title'> & { lat?: number | null; lng?: number | null }

export function toPoints(events: WithCoords[]): MapPoint[] {
  return events
    .filter((e) => typeof e.lat === 'number' && typeof e.lng === 'number')
    .map((e) => ({ id: e.id, title: e.title, coordinates: [e.lng as number, e.lat as number] }))
}

/** Границы для вписывания точек; null — точек меньше двух, тогда просто центрируем. */
export function boundsOf(points: MapPoint[]) {
  if (points.length < 2) return null
  const lngs = points.map((p) => p.coordinates[0])
  const lats = points.map((p) => p.coordinates[1])
  return {
    southWest: [Math.min(...lngs), Math.min(...lats)],
    northEast: [Math.max(...lngs), Math.max(...lats)],
  }
}

export function centerOf(points: MapPoint[]): LngLat {
  return points[0]?.coordinates ?? DEFAULT_CENTER
}

/** Ссылка на точку в 2ГИС — открыть маршрут в приложении или на сайте. */
export function twoGisLink([lng, lat]: LngLat) {
  return `https://2gis.ru/geo/${lng},${lat}`
}
