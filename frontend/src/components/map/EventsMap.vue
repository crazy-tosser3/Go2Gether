<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue'
import { boundsOf, centerOf, type MapPoint } from '@/features/map/geo'
import {
  loadMapgl,
  mapKey,
  MapUnavailableError,
  UNAVAILABLE_TEXT,
  type MapglApi,
} from '@/features/map/mapgl'

const props = withDefaults(
  defineProps<{ points: MapPoint[]; selectedId?: number | null; label: string; zoom?: number }>(),
  { selectedId: null, zoom: 13 },
)
const emit = defineEmits<{ select: [id: number] }>()

const container = ref<HTMLDivElement | null>(null)
const state = ref<'loading' | 'ready' | 'unavailable'>('loading')
const message = ref('')

const api = shallowRef<MapglApi | null>(null)
// Объекты MapGL держим вне реактивности: прокси Vue ломают их внутреннее состояние.
let map: InstanceType<MapglApi['Map']> | null = null
let markers: InstanceType<MapglApi['Marker']>[] = []

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

function frame() {
  if (!map) return
  const bounds = boundsOf(props.points)
  if (bounds) map.fitBounds(bounds, { padding: { top: 48, right: 48, bottom: 48, left: 48 } })
  else map.setCenter(centerOf(props.points))
}

onMounted(async () => {
  try {
    api.value = await loadMapgl()
    if (!container.value) return
    map = new api.value.Map(container.value, {
      center: centerOf(props.points),
      zoom: props.zoom,
      key: mapKey(),
      lang: 'ru',
      zoomControl: 'topRight',
    })
    drawMarkers()
    frame()
    state.value = 'ready'
  } catch (error) {
    message.value =
      UNAVAILABLE_TEXT[error instanceof MapUnavailableError ? error.reason : 'load-failed']
    state.value = 'unavailable'
  }
})

watch(
  () => props.points,
  () => {
    drawMarkers()
    frame()
  },
)
watch(() => props.selectedId, drawMarkers)

onBeforeUnmount(() => {
  for (const m of markers) m.destroy()
  markers = []
  map?.destroy()
  map = null
})
</script>

<template>
  <div class="relative overflow-hidden rounded-card bg-field">
    <div ref="container" role="region" :aria-label="label" class="h-full min-h-64 w-full" />
    <p
      v-if="state === 'loading'"
      class="absolute inset-0 grid place-items-center text-ink-2"
      role="status"
    >
      {{ $t('map.loading') }}
    </p>
    <p
      v-else-if="state === 'unavailable'"
      class="absolute inset-0 grid place-items-center p-6 text-center text-ink-2"
      role="status"
    >
      {{ message }}
    </p>
  </div>
</template>
