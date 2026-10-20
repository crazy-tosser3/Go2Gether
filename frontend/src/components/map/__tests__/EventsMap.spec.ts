import { afterEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import EventsMap from '../EventsMap.vue'

// Подменяем MapGL: в jsdom нет WebGL, проверяем, что компонент правильно им управляет.
const created: {
  coordinates: number[]
  label: { text: string }
  click?: () => void
  destroyed: boolean
}[] = []
const mapCalls = { fitBounds: 0, setCenter: 0, destroyed: false }

class FakeMap {
  fitBounds() {
    mapCalls.fitBounds++
  }
  setCenter() {
    mapCalls.setCenter++
  }
  destroy() {
    mapCalls.destroyed = true
  }
}
class FakeMarker {
  rec: (typeof created)[number]
  constructor(_map: unknown, opts: { coordinates: number[]; label: { text: string } }) {
    this.rec = { ...opts, destroyed: false }
    created.push(this.rec)
  }
  on(_e: string, cb: () => void) {
    this.rec.click = cb
  }
  destroy() {
    this.rec.destroyed = true
  }
}

const loadMapgl = vi.fn<() => Promise<unknown>>()
vi.mock('@/features/map/mapgl', async (orig) => ({
  ...(await orig<typeof import('@/features/map/mapgl')>()),
  loadMapgl: () => loadMapgl(),
  mapKey: () => 'test',
}))

afterEach(() => {
  created.length = 0
  Object.assign(mapCalls, { fitBounds: 0, setCenter: 0, destroyed: false })
  loadMapgl.mockReset()
})

const points = [
  { id: 1, title: 'Йога', coordinates: [92.8, 56.0] as [number, number] },
  { id: 2, title: 'Бег', coordinates: [92.9, 56.1] as [number, number] },
]

describe('EventsMap', () => {
  it('ставит маркеры с подписями, вписывает их и сообщает о выборе', async () => {
    loadMapgl.mockResolvedValue({ Map: FakeMap, Marker: FakeMarker })
    const wrapper = mount(EventsMap, { props: { points, label: 'Карта' }, attachTo: document.body })
    await flushPromises()
    expect(created.map((m) => m.label.text)).toEqual(['Йога', 'Бег'])
    expect(mapCalls.fitBounds).toBe(1)
    created[1]!.click!()
    expect(wrapper.emitted('select')).toEqual([[2]])
    wrapper.unmount()
    expect(mapCalls.destroyed).toBe(true)
    expect(created.every((m) => m.destroyed)).toBe(true)
  })

  it('одна точка — центр на ней, без вписывания', async () => {
    loadMapgl.mockResolvedValue({ Map: FakeMap, Marker: FakeMarker })
    mount(EventsMap, { props: { points: points.slice(0, 1), label: 'Карта' } })
    await flushPromises()
    expect(mapCalls.setCenter).toBe(1)
    expect(mapCalls.fitBounds).toBe(0)
  })

  it('если карту не загрузить — понятное сообщение', async () => {
    const { MapUnavailableError } = await import('@/features/map/mapgl')
    loadMapgl.mockRejectedValue(new MapUnavailableError('load-failed'))
    const wrapper = mount(EventsMap, { props: { points, label: 'Карта' } })
    await flushPromises()
    expect(wrapper.get('[role="status"]').text()).toContain('Не получилось загрузить карту')
  })
})
