<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref, shallowRef, watch } from 'vue'
import { UiButton } from '@/components/ui'
import { geocode, type GeoPoint } from '@/features/map/geocode'
import { DEFAULT_CENTER, type LngLat } from '@/features/map/geo'
import {
  loadMapgl,
  mapKey,
  MapUnavailableError,
  UNAVAILABLE_TEXT,
  type MapglApi,
} from '@/features/map/mapgl'

const props = defineProps<{ address: string; city: string }>()
const point = defineModel<GeoPoint | null>({ required: true })

const enabled = !!mapKey()
const container = ref<HTMLDivElement | null>(null)
const mapState = ref<'loading' | 'ready' | 'unavailable'>('loading')
const mapMessage = ref('')
const searching = ref(false)
const notice = ref('')

const api = shallowRef<MapglApi | null>(null)
// Объекты MapGL держим вне реактивности: прокси Vue ломают их внутреннее состояние.
let map: InstanceType<MapglApi['Map']> | null = null
let marker: InstanceType<MapglApi['Marker']> | null = null

const toLngLat = (p: GeoPoint): LngLat => [p.lng, p.lat]

function drawMarker() {
  if (!api.value || !map) return
  marker?.destroy()
  marker = point.value ? new api.value.Marker(map, { coordinates: toLngLat(point.value) }) : null
}

watch(point, (p) => {
  drawMarker()
  if (p && map) map.setCenter(toLngLat(p))
})

async function findByAddress() {
  notice.value = ''
  if (!props.address.trim()) {
    notice.value = 'Сначала укажите адрес'
    return
  }
  searching.value = true
  try {
    const found = await geocode(props.address, props.city)
    if (found) {
      point.value = found
      map?.setZoom(16)
      notice.value = 'Нашли — поправьте точку на карте, если нужно'
    } else notice.value = 'Не нашли такой адрес — отметьте место на карте'
  } catch {
    notice.value = 'Поиск по адресу недоступен — отметьте место на карте'
  } finally {
    searching.value = false
  }
}

onMounted(async () => {
  if (!enabled) return
  try {
    api.value = await loadMapgl()
    if (!container.value) return
    map = new api.value.Map(container.value, {
      center: point.value ? toLngLat(point.value) : DEFAULT_CENTER,
      zoom: point.value ? 16 : 12,
      key: mapKey(),
      lang: 'ru',
      zoomControl: 'topRight',
    })
    map.on('click', (e) => {
      const [lng, lat] = e.lngLat
      if (lng === undefined || lat === undefined) return
      point.value = { lat: Number(lat.toFixed(6)), lng: Number(lng.toFixed(6)) }
    })
    drawMarker()
    mapState.value = 'ready'
  } catch (error) {
    mapMessage.value =
      UNAVAILABLE_TEXT[error instanceof MapUnavailableError ? error.reason : 'load-failed']
    mapState.value = 'unavailable'
  }
})

onBeforeUnmount(() => {
  marker?.destroy()
  map?.destroy()
  map = null
})
</script>

<template>
  <fieldset v-if="enabled" class="flex min-w-0 flex-col gap-2">
    <legend class="mb-1.5 ml-1 text-[15px] font-semibold">Точка на карте</legend>
    <p class="ml-1 text-sm text-ink-2">
      Без точки мероприятие не появится на карте. Найдите по адресу или нажмите на карту.
    </p>
    <div class="relative h-64 overflow-hidden rounded-field bg-field">
      <div ref="container" role="region" aria-label="Выбор места на карте" class="h-full w-full" />
      <p
        v-if="mapState !== 'ready'"
        class="absolute inset-0 grid place-items-center p-4 text-center text-ink-2"
        role="status"
      >
        {{ mapState === 'loading' ? 'Загружаем карту…' : mapMessage }}
      </p>
    </div>
    <div class="flex flex-wrap items-center gap-2">
      <UiButton variant="ghost" class="!w-auto" :loading="searching" @click="findByAddress">
        Найти по адресу
      </UiButton>
      <button
        v-if="point"
        type="button"
        class="inline-flex min-h-8 items-center text-sm font-semibold text-ink-2 underline underline-offset-4 hover:text-ink"
        @click="point = null"
      >
        Убрать точку
      </button>
    </div>
    <p class="ml-1 text-sm" role="status">
      <template v-if="notice">{{ notice }}</template>
      <template v-else-if="point">✓ Место отмечено</template>
    </p>
  </fieldset>
</template>
