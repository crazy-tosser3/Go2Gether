<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue'
import type { LngLat, MapPoint } from '@/features/map/geo'
import type { Bounds } from '@/features/map/nearby'
import {
  loadMapgl,
  mapKey,
  MapUnavailableError,
  UNAVAILABLE_TEXT,
  type MapglApi,
} from '@/features/map/mapgl'

const props = defineProps<{
  points: MapPoint[]
  center: LngLat
  me: LngLat | null
  selectedId: number | null
}>()
const emit = defineEmits<{ select: [id: number]; bounds: [bounds: Bounds] }>()

const container = ref<HTMLDivElement | null>(null)
const state = ref<'loading' | 'ready' | 'unavailable'>('loading')
const message = ref('')

const api = shallowRef<MapglApi | null>(null)
// Объекты MapGL держим вне реактивности: прокси Vue ломают их внутреннее состояние.
let map: InstanceType<MapglApi['Map']> | null = null
let markers: InstanceType<MapglApi['Marker']>[] = []
let meMarker: InstanceType<MapglApi['CircleMarker']> | null = null

function drawMarkers() {
  if (!api.value || !map) return
  for (const m of markers) m.destroy()
  markers = props.points.map((p) => {
    const selected = p.id === props.selectedId
    const marker = new api.value!.Marker(map!, {
      coordinates: p.coordinates,
      zIndex: selected ? 2 : 1,
      label: { text: p.title, fontSize: selected ? 14 : 12, color: '#0e0e10' },
    })
    marker.on('click', () => emit('select', p.id))
    return marker
  })
}

function drawMe() {
  if (!api.value || !map) return
  meMarker?.destroy()
  meMarker = props.me
    ? new api.value.CircleMarker(map, {
        coordinates: props.me,
        diameter: 18,
        color: '#6b82ee',
        strokeColor: '#ffffff',
        strokeWidth: 3,
        zIndex: 3,
      })
    : null
}

function emitBounds() {
  if (!map) return
  const b = map.getBounds()
  emit('bounds', { southWest: b.southWest as LngLat, northEast: b.northEast as LngLat })
}

onMounted(async () => {
  try {
    api.value = await loadMapgl()
    if (!container.value) return
    map = new api.value.Map(container.value, {
      center: props.center,
      zoom: 12,
      key: mapKey(),
      lang: 'ru',
      zoomControl: 'topRight',
    })
    map.on('moveend', emitBounds)
    drawMarkers()
    drawMe()
    emitBounds()
    state.value = 'ready'
  } catch (error) {
    message.value =
      UNAVAILABLE_TEXT[error instanceof MapUnavailableError ? error.reason : 'load-failed']
    state.value = 'unavailable'
  }
})

watch(() => [props.points, props.selectedId], drawMarkers)
watch(() => props.me, drawMe)
watch(
  () => props.center,
  (c) => map?.setCenter(c),
)

onBeforeUnmount(() => {
  for (const m of markers) m.destroy()
  markers = []
  meMarker?.destroy()
  map?.destroy()
  map = null
})
</script>

<template>
  <div class="relative overflow-hidden rounded-card bg-field">
    <div ref="container" role="region" :aria-label="$t('map.nearbyLabel')" class="h-full w-full" />
    <p
      v-if="state !== 'ready'"
      class="absolute inset-0 grid place-items-center p-6 text-center text-ink-2"
      role="status"
    >
      {{ state === 'loading' ? $t('map.loading') : message }}
    </p>
  </div>
</template>
