<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { sportsApi, type EventFilters } from '@/api'
import EventFiltersSheet from '@/components/events/EventFiltersSheet.vue'
import EventListCard from '@/components/events/EventListCard.vue'
import NearbyMap from '@/components/map/NearbyMap.vue'
import GuestPlaceNotice from '@/components/events/GuestPlaceNotice.vue'
import { UiButton, UiTextField, type UiOption } from '@/components/ui'
import { t } from '@/i18n'
import {
  countFilters,
  filterChips,
  filtersFromQuery,
  filtersToQuery,
  withoutFilter,
  type FilterChipKey,
} from '@/features/events/filters'
import {
  SORT_OPTIONS,
  sortEvents,
  sortFromQuery,
  sortToQuery,
  type EventSort,
} from '@/features/events/sort'
import { useEventList } from '@/features/events/useEventList'
import { geocode } from '@/features/map/geocode'
import { DEFAULT_CENTER, type LngLat, type MapPoint } from '@/features/map/geo'
import { mapKey } from '@/features/map/mapgl'
import { formatDistance, visibleNearby, type Bounds } from '@/features/map/nearby'
import { useEventCoords } from '@/features/map/useEventCoords'
import { useSessionStore } from '@/stores/session'

const route = useRoute()
const session = useSessionStore()
const router = useRouter()

const filters = computed(() => filtersFromQuery(route.query))
const sort = computed(() => sortFromQuery(route.query))
// Сортируем на фронте, поэтому при выбранном порядке берём сразу побольше — чтобы он был по всему списку.
const listFilters = computed(() =>
  sort.value === 'soon' ? filters.value : { ...filters.value, size: 100 },
)
const { items, total, state, error, loadingMore, load, loadMore } = useEventList(listFilters)
const sorted = computed(() => sortEvents(items.value, sort.value))

const sportOptions = ref<UiOption[]>([])
const sheetOpen = ref(false)

// Вид «Карта» живёт в адресе рядом с фильтрами, чтобы ссылкой можно было поделиться.
type View = 'list' | 'map'
const view = computed<View>(() => (route.query.view === 'map' ? 'map' : 'list'))
const viewQuery = () => ({
  ...(view.value === 'map' ? { view: 'map' } : {}),
  ...sortToQuery(sort.value),
})

function applyFilters(next: EventFilters) {
  router.replace({ query: { ...filtersToQuery({ ...next, page: undefined }), ...viewQuery() } })
}

function setView(v: View) {
  router.replace({
    query: {
      ...filtersToQuery(filters.value),
      ...(v === 'map' ? { view: v } : {}),
      ...sortToQuery(sort.value),
    },
  })
}

function setSort(next: EventSort) {
  router.replace({
    query: {
      ...filtersToQuery(filters.value),
      ...(view.value === 'map' ? { view: 'map' } : {}),
      ...sortToQuery(next),
    },
  })
}

const search = ref(filters.value.q ?? '')
let searchTimer: ReturnType<typeof setTimeout> | undefined
watch(search, (value) => {
  clearTimeout(searchTimer)
  searchTimer = setTimeout(
    () => applyFilters({ ...filters.value, q: value.trim() || undefined }),
    350,
  )
})
watch(
  () => filters.value.q,
  (q) => {
    if ((q ?? '') !== search.value.trim()) search.value = q ?? ''
  },
)

const filterCount = computed(() => countFilters(filters.value))
const chips = computed(() =>
  filterChips(filters.value, (id) => sportOptions.value.find((o) => o.value === String(id))?.label),
)
function removeChip(key: FilterChipKey) {
  applyFilters(withoutFilter(filters.value, key))
}

const hasFilters = computed(() => filterCount.value > 0 || !!filters.value.q)
function reset() {
  search.value = ''
  router.replace({ query: viewQuery() })
}

const foundText = computed(() => t('events.found', { events: t('events.count', total.value) }))

// Карта: точки мероприятий, «Где я» и список того, что сейчас видно на карте, — ближайшие первыми.
const hasMap = !!mapKey()
const isMap = computed(() => view.value === 'map')
// Гостю карту не показываем — и координаты не догружаем.
const { coords } = useEventCoords(
  items,
  computed(() => isMap.value && session.isAuthenticated),
)
const points = computed<MapPoint[]>(() =>
  items.value.flatMap((e) => {
    const c = coords.value.get(e.id)
    return c ? [{ id: e.id, title: e.title, coordinates: c }] : []
  }),
)
const withoutCoords = computed(() => items.value.filter((e) => coords.value.get(e.id) === null))

const center = ref<LngLat>(DEFAULT_CENTER)
const me = ref<LngLat | null>(null)
const bounds = ref<Bounds | null>(null)
const selectedId = ref<number | null>(null)
const locating = ref(false)
const locateNotice = ref('')

const visible = computed(() => {
  const byId = new Map(items.value.map((e) => [e.id, e]))
  return visibleNearby(points.value, bounds.value, me.value ?? center.value).flatMap((p) => {
    const event = byId.get(p.id)
    return event ? [{ event, distance: p.distance }] : []
  })
})

function locate() {
  locateNotice.value = ''
  if (!navigator.geolocation) {
    locateNotice.value = t('map.noGeo')
    return
  }
  locating.value = true
  navigator.geolocation.getCurrentPosition(
    (pos) => {
      me.value = [pos.coords.longitude, pos.coords.latitude]
      center.value = me.value
      locating.value = false
    },
    () => {
      locating.value = false
      locateNotice.value = t('map.geoFailed')
    },
    { enableHighAccuracy: false, timeout: 10000, maximumAge: 60000 },
  )
}

function selectOnMap(id: number) {
  selectedId.value = id
  document.getElementById(`event-${id}`)?.scrollIntoView({ behavior: 'smooth', block: 'nearest' })
}

// Пока человек не нажал «Где я», карта стоит на городе из фильтра или профиля.
async function centerOnCity() {
  const city = (filters.value.city ?? session.user?.city ?? '').trim()
  if (!hasMap || !city || me.value) return
  const found = await geocode(city, '').catch(() => null)
  if (found && !me.value) center.value = [found.lng, found.lat]
}
watch(
  () => [isMap.value, filters.value.city] as const,
  ([map]) => {
    if (map) centerOnCity()
  },
  { immediate: true },
)

watch(() => route.query, load, { immediate: true, deep: true })

onMounted(async () => {
  try {
    const sports = await sportsApi.list()
    sportOptions.value = sports.map((s) => ({ value: String(s.id), label: s.name }))
  } catch {
    sportOptions.value = []
  }
})
</script>

<template>
  <section class="flex flex-col gap-4">
    <header class="flex flex-wrap items-end justify-between gap-3">
      <div>
        <h1 class="text-[30px] lg:text-[42px]">{{ $t('nav.events') }}</h1>
        <p v-if="state === 'ready'" class="text-ink-2" role="status">{{ foundText }}</p>
      </div>
      <RouterLink
        v-if="!session.isAdmin"
        :to="{ name: 'event-create' }"
        class="hidden rounded-full px-4 py-1.5 font-bold text-ink no-underline hover:bg-field md:inline-block"
      >
        {{ $t('events.createOwn') }}
      </RouterLink>
    </header>

    <div class="flex flex-col gap-3" role="search" :aria-label="$t('events.searchLabel')">
      <UiTextField
        v-model="search"
        :label="$t('events.search')"
        hide-label
        type="search"
        :placeholder="$t('events.searchPlaceholder')"
      />
      <div class="flex items-center gap-2">
        <button
          type="button"
          class="inline-flex min-h-12 shrink-0 items-center gap-1.5 rounded-full px-3.5 font-bold max-[359px]:px-3 sm:gap-2 sm:px-5"
          :class="filterCount ? 'bg-ink text-white' : 'bg-card text-ink hover:bg-mute'"
          aria-haspopup="dialog"
          @click="sheetOpen = true"
        >
          <svg
            viewBox="0 0 24 24"
            width="20"
            height="20"
            class="max-[359px]:hidden"
            aria-hidden="true"
            fill="none"
            stroke="currentColor"
            stroke-width="2"
            stroke-linecap="round"
          >
            <path d="M4 7h10M18 7h2M4 17h4M12 17h8" />
            <circle cx="16" cy="7" r="2" />
            <circle cx="10" cy="17" r="2" />
          </svg>
          {{ $t('filters.title') }}
          <span
            v-if="filterCount"
            class="grid min-w-6 place-items-center rounded-full bg-white px-1.5 text-sm text-ink"
          >
            {{ filterCount }}
          </span>
        </button>
        <div
          class="ml-auto flex shrink-0 rounded-full bg-card p-1 max-[359px]:p-0.5"
          role="group"
          :aria-label="$t('events.viewLabel')"
        >
          <button
            v-for="v in [
              { key: 'list', label: $t('events.list') },
              { key: 'map', label: $t('events.map') },
            ] as const"
            :key="v.key"
            type="button"
            class="min-h-11 rounded-full px-2.5 text-[15px] font-semibold max-[359px]:px-1.5 sm:px-4 sm:text-base"
            :class="view === v.key ? 'bg-ink text-white' : 'text-ink hover:bg-field'"
            :aria-pressed="view === v.key"
            @click="setView(v.key)"
          >
            {{ v.label }}
          </button>
        </div>
      </div>

      <ul
        v-if="chips.length"
        class="flex gap-2 overflow-x-auto pb-1"
        :aria-label="$t('filters.chosen')"
      >
        <li v-for="c in chips" :key="c.key" class="shrink-0">
          <button
            type="button"
            class="inline-flex min-h-11 items-center gap-1.5 rounded-full bg-accent-soft px-4 font-semibold text-accent-ink"
            :aria-label="$t('filters.remove', { name: c.label })"
            @click="removeChip(c.key)"
          >
            {{ c.label }} <span aria-hidden="true">✕</span>
          </button>
        </li>
        <li v-if="chips.length > 1" class="shrink-0">
          <button
            type="button"
            class="inline-flex min-h-11 items-center px-2 font-semibold underline underline-offset-4"
            @click="reset"
          >
            {{ $t('filters.resetAll') }}
          </button>
        </li>
      </ul>
    </div>

    <EventFiltersSheet
      v-model:open="sheetOpen"
      :filters="filters"
      :total="total"
      :sport-options="sportOptions"
      @apply="applyFilters"
    />

    <div aria-live="polite" :aria-busy="state === 'loading'">
      <ul
        v-if="state === 'loading'"
        class="grid grid-cols-1 gap-4 md:grid-cols-2 2xl:grid-cols-3"
        :aria-label="$t('common.loading')"
      >
        <li v-for="n in 4" :key="n" class="h-44 animate-pulse rounded-card bg-card" />
      </ul>

      <div
        v-else-if="state === 'error'"
        class="flex flex-col items-center gap-3 rounded-card p-6 text-center"
        role="alert"
      >
        <p class="text-lg font-bold">{{ $t('events.loadError') }}</p>
        <p class="text-ink-2">{{ error }}</p>
        <UiButton variant="ghost" class="!w-auto" @click="load">{{ $t('common.retry') }}</UiButton>
      </div>

      <div
        v-else-if="items.length === 0"
        class="flex flex-col items-center gap-3 rounded-card bg-card p-6 text-center"
      >
        <p class="font-hand text-2xl text-accent-ink">{{ $t('events.emptyHand') }}</p>
        <p class="text-lg font-bold">{{ $t('events.emptyTitle') }}</p>
        <p class="max-w-md text-ink-2">
          {{ $t('events.emptyText') }}
          <template v-if="!session.isAdmin">{{ $t('events.emptyCreate') }}</template>
        </p>
        <div class="flex flex-wrap justify-center gap-3">
          <RouterLink
            v-if="!session.isAdmin"
            :to="{ name: 'event-create' }"
            class="rounded-full bg-ink px-5 py-2.5 font-bold text-white no-underline"
          >
            {{ $t('events.create') }}
          </RouterLink>
          <button
            v-if="hasFilters"
            type="button"
            class="rounded-full px-5 py-2.5 font-bold"
            @click="reset"
          >
            {{ $t('filters.resetFilters') }}
          </button>
        </div>
      </div>

      <GuestPlaceNotice
        v-else-if="isMap && !session.isAuthenticated"
        :text="$t('place.mapGuest')"
      />
      <div
        v-else-if="isMap"
        class="grid grid-cols-[minmax(0,1fr)] gap-4 xl:grid-cols-[minmax(0,1fr)_340px]"
      >
        <div class="relative">
          <NearbyMap
            class="h-[60vh] min-h-72 xl:h-[calc(100vh-17rem)]"
            :points="points"
            :center="center"
            :me="me"
            :selected-id="selectedId"
            @select="selectOnMap"
            @bounds="bounds = $event"
          />
          <UiButton
            v-if="hasMap"
            variant="ghost"
            class="!absolute bottom-4 left-4 !w-auto !bg-card shadow-md"
            :loading="locating"
            @click="locate"
          >
            {{ $t('map.whereAmI') }}
          </UiButton>
        </div>
        <div
          class="flex min-w-0 flex-col gap-3 xl:h-[calc(100vh-17rem)] xl:overflow-y-auto xl:pr-1"
        >
          <p v-if="locateNotice" class="text-sm text-ink-2" role="status">{{ locateNotice }}</p>
          <h2 class="font-bold">
            {{ $t('map.onMap', { events: $t('events.count', visible.length) }) }}
          </h2>
          <p v-if="hasMap && !visible.length" class="rounded-card bg-card p-4 text-ink-2">
            {{ $t('map.emptyArea') }}
          </p>
          <ul class="grid grid-cols-[minmax(0,1fr)] gap-3 md:grid-cols-2 xl:grid-cols-1">
            <li
              v-for="{ event, distance } in visible"
              :id="`event-${event.id}`"
              :key="event.id"
              class="flex min-w-0 scroll-mt-4 flex-col gap-1 rounded-card"
              :class="selectedId === event.id ? 'outline-3 outline-accent-strong' : ''"
            >
              <EventListCard :event="event" />
              <p class="px-3 text-sm font-semibold text-ink-2">
                {{ formatDistance(distance) }} {{ me ? $t('map.fromYou') : $t('map.fromCenter') }}
              </p>
            </li>
          </ul>
          <p v-if="withoutCoords.length" class="text-sm text-ink-2">
            {{ $t('map.noPoint', { list: withoutCoords.map((e) => e.title).join(', ') }) }}
          </p>
        </div>
      </div>

      <template v-else>
        <div class="mb-3 flex items-center justify-between gap-3">
          <h2 class="sr-only">{{ $t('events.found_title') }}</h2>
          <label class="ml-auto flex max-w-full min-w-0 items-center gap-2 font-semibold">
            <span class="text-ink-2">{{ $t('sort.title') }}</span>
            <select
              :value="sort"
              class="min-h-11 min-w-0 flex-1 cursor-pointer truncate rounded-full bg-card px-4 font-semibold text-ink"
              @change="setSort(($event.target as HTMLSelectElement).value as EventSort)"
            >
              <option v-for="o in SORT_OPTIONS" :key="o.value" :value="o.value">
                {{ $t(`sort.${o.value}`) }}
              </option>
            </select>
          </label>
        </div>
        <ul class="grid grid-cols-1 gap-4 md:grid-cols-2 xl:grid-cols-3 min-[1800px]:grid-cols-4">
          <li v-for="event in sorted" :key="event.id" class="rounded-card">
            <EventListCard :event="event" />
          </li>
        </ul>
        <div v-if="items.length < total" class="mt-5 flex flex-col items-center gap-2">
          <UiButton variant="ghost" class="!w-auto" :loading="loadingMore" @click="loadMore">
            {{ $t('events.more') }}
          </UiButton>
          <p v-if="error" class="text-accent-ink" role="alert">✕ {{ error }}</p>
        </div>
      </template>
    </div>
  </section>
</template>
