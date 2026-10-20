import { describe, expect, it } from 'vitest'
import { DEFAULT_CENTER, boundsOf, centerOf, toPoints, twoGisLink } from '../geo'

const ev = (id: number, lat: number | null, lng: number | null) => ({
  id,
  title: `Событие ${id}`,
  lat,
  lng,
})

describe('geo', () => {
  it('берёт на карту только события с координатами, в порядке [долгота, широта]', () => {
    expect(toPoints([ev(1, 56.01, 92.87), ev(2, null, null)])).toEqual([
      { id: 1, title: 'Событие 1', coordinates: [92.87, 56.01] },
    ])
  })

  it('границы — по крайним точкам; для одной точки — центр на ней', () => {
    const pts = toPoints([ev(1, 56.0, 92.8), ev(2, 56.1, 92.9), ev(3, 55.9, 92.7)])
    expect(boundsOf(pts)).toEqual({ southWest: [92.7, 55.9], northEast: [92.9, 56.1] })
    expect(boundsOf(pts.slice(0, 1))).toBeNull()
    expect(centerOf(pts.slice(0, 1))).toEqual([92.8, 56.0])
  })

  it('без точек — центр Красноярска', () => {
    expect(centerOf([])).toEqual(DEFAULT_CENTER)
  })

  it('ссылка на точку в 2ГИС', () => {
    expect(twoGisLink([92.87, 56.01])).toBe('https://2gis.ru/geo/92.87,56.01')
  })
})
