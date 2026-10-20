<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink, type RouteLocationRaw } from 'vue-router'
import {
  ApiError,
  adminApi,
  type AdminDashboard,
  type EventSummary,
  type ModerationLog,
} from '@/api'
import AdminNav from '@/components/admin/AdminNav.vue'
import StatBars from '@/components/admin/charts/StatBars.vue'
import StatDonut from '@/components/admin/charts/StatDonut.vue'
import WeekColumns from '@/components/admin/charts/WeekColumns.vue'
import { UiButton, UiCard } from '@/components/ui'
import { eventStatusSlices, eventsByWeek, moderationSlices, topBy } from '@/features/admin/stats'
import { downloadText, exportFileName, statsCsv } from '@/features/admin/export'
import { t } from '@/i18n'

// Разрезы считаем по последним мероприятиям и действиям: для прототипа этого хватает.
const SAMPLE = 200

const data = ref<AdminDashboard | null>(null)
const state = ref<'loading' | 'ready' | 'error'>('loading')
const message = ref('')

async function load() {
  state.value = 'loading'
  try {
    data.value = await adminApi.dashboard()
    state.value = 'ready'
  } catch (e) {
    message.value = e instanceof ApiError ? e.message : t('admin.dashboard.loadFailed')
    state.value = 'error'
  }
}

const events = ref<EventSummary[]>([])
const logs = ref<ModerationLog[]>([])
const chartsState = ref<'loading' | 'ready' | 'error'>('loading')

async function loadCharts() {
  chartsState.value = 'loading'
  try {
    const [e, l] = await Promise.all([
      adminApi.events({ size: SAMPLE }),
      adminApi.auditLog({ size: SAMPLE }),
    ])
    events.value = e.items.filter((x) => x.status !== 'deleted')
    logs.value = l.items
    chartsState.value = 'ready'
  } catch {
    chartsState.value = 'error'
  }
}

onMounted(() => {
  load()
  loadCharts()
})

function exportStats() {
  const now = new Date()
  downloadText(statsCsv(data.value, events.value, logs.value, now), exportFileName(now))
}

const statuses = computed(() => (data.value ? eventStatusSlices(data.value) : []))
const sports = computed(() => topBy(events.value, (e) => e.sport?.name))
const cities = computed(() => topBy(events.value, (e) => e.city))
const weeks = computed(() => eventsByWeek(events.value, new Date()))
const moderation = computed(() => moderationSlices(logs.value))

interface Tile {
  label: string
  value: number
  bg: string
  to?: RouteLocationRaw
}

const tiles = computed<Tile[]>(() => {
  const d = data.value
  if (!d) return []
  return [
    {
      label: t('admin.dashboard.pending'),
      value: d.pending_organizations,
      bg: d.pending_organizations ? 'bg-orange' : 'bg-card',
      to: { name: 'admin-organizations', query: { status: 'pending' } },
    },
    {
      label: t('admin.dashboard.pendingReports'),
      value: d.pending_reports,
      bg: d.pending_reports ? 'bg-orange' : 'bg-card',
      to: { name: 'admin-reports' },
    },
    {
      label: t('admin.dashboard.approved'),
      value: d.approved_organizations,
      bg: 'bg-card',
      to: { name: 'admin-organizations', query: { status: 'approved' } },
    },
    {
      label: t('admin.dashboard.published'),
      value: d.published_events,
      bg: 'bg-accent',
      to: { name: 'admin-events' },
    },
    { label: t('admin.dashboard.unpublished'), value: d.unpublished_events, bg: 'bg-card' },
    { label: t('admin.dashboard.deleted'), value: d.deleted_events, bg: 'bg-card' },
    { label: t('admin.dashboard.hiddenComments'), value: d.hidden_comments, bg: 'bg-card' },
    { label: t('admin.dashboard.users'), value: d.users, bg: 'bg-lilac' },
    { label: t('admin.dashboard.cancelled'), value: d.cancelled_events, bg: 'bg-card' },
  ]
})
</script>

<template>
  <section class="flex flex-col gap-5" aria-live="polite">
    <h1 class="text-[30px] lg:text-[42px]">{{ $t('admin.title') }}</h1>
    <AdminNav />

    <div v-if="state === 'loading'" class="grid grid-cols-2 gap-3 lg:grid-cols-3">
      <div v-for="i in 8" :key="i" class="h-28 animate-pulse rounded-card bg-card" />
    </div>

    <div
      v-else-if="state === 'error'"
      class="flex flex-col items-center gap-3 rounded-card bg-card p-6"
      role="alert"
    >
      <p class="font-bold">{{ message }}</p>
      <UiButton variant="ghost" class="!w-auto" @click="load">{{ $t('common.retry') }}</UiButton>
    </div>

    <ul v-else class="grid grid-cols-[repeat(2,minmax(0,1fr))] gap-3 lg:grid-cols-3">
      <li v-for="tile in tiles" :key="tile.label">
        <component
          :is="tile.to ? RouterLink : 'div'"
          :to="tile.to"
          class="flex h-full flex-col gap-1 rounded-card p-4 text-ink no-underline sm:p-5"
          :class="[tile.bg, tile.to ? 'hover:brightness-95' : '']"
        >
          <b class="text-4xl font-bold tabular-nums">{{ tile.value }}</b>
          <span class="font-medium hyphens-auto [overflow-wrap:anywhere]">{{ tile.label }}</span>
        </component>
      </li>
    </ul>

    <div class="mt-2 flex flex-wrap items-end justify-between gap-3">
      <h2 class="text-2xl font-bold">{{ $t('admin.dashboard.stats') }}</h2>
      <div class="flex flex-col items-start gap-1 sm:items-end">
        <UiButton
          variant="ghost"
          class="!w-auto"
          :disabled="chartsState !== 'ready'"
          @click="exportStats"
        >
          {{ $t('admin.dashboard.export') }}
        </UiButton>
        <p class="text-sm text-ink-2">{{ $t('admin.dashboard.exportHint') }}</p>
      </div>
    </div>
    <div
      v-if="chartsState === 'loading'"
      class="grid grid-cols-[minmax(0,1fr)] gap-3 lg:grid-cols-2"
    >
      <div v-for="i in 4" :key="i" class="h-56 animate-pulse rounded-card bg-card" />
    </div>
    <div
      v-else-if="chartsState === 'error'"
      class="flex flex-col items-center gap-3 rounded-card bg-card p-6"
      role="alert"
    >
      <p class="font-bold">{{ $t('admin.dashboard.statsFailed') }}</p>
      <UiButton variant="ghost" class="!w-auto" @click="loadCharts">{{
        $t('common.retry')
      }}</UiButton>
    </div>
    <div v-else class="grid grid-cols-[minmax(0,1fr)] gap-3 lg:grid-cols-2">
      <UiCard as="section" class="gap-4 p-5 lg:col-span-2" aria-labelledby="st-weeks">
        <h3 id="st-weeks" class="font-display text-lg font-extrabold">
          {{ $t('admin.dashboard.byWeek') }}
        </h3>
        <WeekColumns :weeks="weeks" />
      </UiCard>
      <UiCard v-if="data" as="section" class="gap-4 p-5" aria-labelledby="st-status">
        <h3 id="st-status" class="font-display text-lg font-extrabold">
          {{ $t('admin.dashboard.byStatus') }}
        </h3>
        <StatDonut :items="statuses" :caption="$t('admin.dashboard.total')" />
      </UiCard>
      <UiCard as="section" class="gap-4 p-5" aria-labelledby="st-sports">
        <h3 id="st-sports" class="font-display text-lg font-extrabold">
          {{ $t('admin.dashboard.sports') }}
        </h3>
        <StatBars v-if="sports.length" :items="sports" />
        <p v-else class="text-ink-2">{{ $t('admin.dashboard.noEvents') }}</p>
      </UiCard>
      <UiCard as="section" class="gap-4 p-5" aria-labelledby="st-cities">
        <h3 id="st-cities" class="font-display text-lg font-extrabold">
          {{ $t('admin.dashboard.cities') }}
        </h3>
        <StatBars v-if="cities.length" :items="cities" />
        <p v-else class="text-ink-2">{{ $t('admin.dashboard.noEvents') }}</p>
      </UiCard>
      <UiCard as="section" class="gap-4 p-5" aria-labelledby="st-moderation">
        <h3 id="st-moderation" class="font-display text-lg font-extrabold">
          {{ $t('admin.dashboard.moderation') }}
        </h3>
        <StatBars v-if="moderation.length" :items="moderation" />
        <p v-else class="text-ink-2">{{ $t('admin.dashboard.noDecisions') }}</p>
      </UiCard>
    </div>
  </section>
</template>
