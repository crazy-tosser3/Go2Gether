import { ref, watch, type Ref } from 'vue'
import { eventsApi, type EventSummary } from '@/api'
import type { LngLat } from './geo'

// Координаты приходят в списке (EventShortResponse); карточку запрашиваем, только если их там нет
// (старые сборки бэка, моки без точек), — с кэшем.
const cache = new Map<number, LngLat | null>()

export function useEventCoords(events: Ref<EventSummary[]>, enabled: Ref<boolean>) {
  const coords = ref(new Map<number, LngLat | null>())
  const loading = ref(false)

  async function fill() {
    if (!enabled.value) return
    for (const e of events.value)
      if (typeof e.lat === 'number' && typeof e.lng === 'number') cache.set(e.id, [e.lng, e.lat])
    const missing = events.value.filter(
      (e) => !cache.has(e.id) && (e as { lat?: unknown }).lat === undefined,
    )
    if (missing.length) {
      loading.value = true
      await Promise.all(
        missing.map(async (e) => {
          try {
            const d = await eventsApi.get(e.id)
            cache.set(e.id, d.lat !== null && d.lng !== null ? [d.lng, d.lat] : null)
          } catch {
            cache.set(e.id, null)
          }
        }),
      )
      loading.value = false
    }
    coords.value = new Map(events.value.map((e) => [e.id, cache.get(e.id) ?? null]))
  }

  watch([events, enabled], fill, { immediate: true })
  return { coords, loading }
}

export function clearCoordsCache() {
  cache.clear()
}
