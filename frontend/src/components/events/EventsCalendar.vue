<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { eventsApi, type EventFilters } from '@/api'
import { UiIconButton } from '@/components/ui'
import { intlLocale, t } from '@/i18n'
import {
  weekdays,
  countByDay,
  formatMonth,
  monthGrid,
  monthOf,
  monthRange,
  shiftMonth,
} from '@/features/events/calendar'
import { localDate } from '@/utils/datetime'

const props = defineProps<{ filters: EventFilters; selected: string | null }>()
const emit = defineEmits<{ select: [date: string | null] }>()

const month = ref(monthOf(props.selected ?? new Date()))
const counts = ref<Record<string, number>>({})
const state = ref<'loading' | 'ready' | 'error'>('loading')
const today = localDate(new Date())

const weeks = computed(() => monthGrid(month.value))
const title = computed(() => formatMonth(month.value))
const weekdayNames = computed(() => weekdays())

// Отметки дней — по тем же фильтрам, что и лента, но за весь месяц.
const monthFilters = computed(() => {
  const range = monthRange(month.value)
  return { ...props.filters, page: 0, size: 100, date_from: range.from, date_to: range.to }
})

let requestId = 0
async function load() {
  const id = ++requestId
  state.value = 'loading'
  try {
    const page = await eventsApi.list(monthFilters.value)
    if (id !== requestId) return
    counts.value = countByDay(page.items)
    state.value = 'ready'
  } catch {
    if (id === requestId) state.value = 'error'
  }
}

watch(monthFilters, load, { immediate: true, deep: true })

function pick(date: string) {
  emit('select', props.selected === date ? null : date)
}

function dayLabel(date: string) {
  const n = counts.value[date] ?? 0
  const when = new Intl.DateTimeFormat(intlLocale(), { day: 'numeric', month: 'long' }).format(
    new Date(`${date}T00:00:00`),
  )
  return n ? `${when}: ${t('events.count', n)}` : t('calendar.none', { date: when })
}
</script>

<template>
  <section
    class="flex w-full max-w-xl flex-col gap-3 rounded-card bg-card p-4"
    aria-labelledby="calendar-title"
  >
    <div class="flex items-center justify-between gap-2">
      <UiIconButton :label="$t('calendar.prev')" @click="month = shiftMonth(month, -1)"
        >‹</UiIconButton
      >
      <h2 id="calendar-title" class="text-lg font-bold" aria-live="polite">{{ title }}</h2>
      <UiIconButton :label="$t('calendar.next')" @click="month = shiftMonth(month, 1)"
        >›</UiIconButton
      >
    </div>

    <table
      class="w-full table-fixed border-separate border-spacing-1"
      :aria-busy="state === 'loading'"
    >
      <thead>
        <tr>
          <th
            v-for="d in weekdayNames"
            :key="d"
            scope="col"
            class="text-xs font-bold tracking-wider text-ink-2 uppercase"
          >
            {{ d }}
          </th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="(week, i) in weeks" :key="i">
          <td v-for="d in week" :key="d.date" class="p-0">
            <button
              type="button"
              class="relative flex aspect-square w-full max-w-14 mx-auto flex-col items-center justify-center rounded-full text-[15px] font-semibold"
              :class="[
                d.date === selected
                  ? 'bg-ink text-white'
                  : counts[d.date]
                    ? 'bg-accent text-ink hover:bg-accent-strong'
                    : 'text-ink hover:bg-field',
                d.inMonth ? '' : 'opacity-40',
                d.date === today && d.date !== selected ? 'ring-2 ring-ink ring-inset' : '',
              ]"
              :aria-pressed="d.date === selected"
              :aria-label="dayLabel(d.date)"
              @click="pick(d.date)"
            >
              {{ d.day }}
              <span
                v-if="counts[d.date]"
                aria-hidden="true"
                class="absolute bottom-1 size-1.5 rounded-full"
                :class="d.date === selected ? 'bg-white' : 'bg-ink'"
              />
            </button>
          </td>
        </tr>
      </tbody>
    </table>

    <p v-if="state === 'error'" class="text-sm text-ink-2" role="alert">
      {{ $t('calendar.error') }}
    </p>
    <div class="flex flex-wrap items-center justify-between gap-2 text-sm text-ink-2">
      <span class="flex items-center gap-2">
        <span class="size-4 rounded-full bg-accent" aria-hidden="true" /> {{ $t('calendar.has') }}
      </span>
      <button
        v-if="selected"
        type="button"
        class="inline-flex min-h-8 items-center font-semibold text-ink underline underline-offset-4"
        @click="emit('select', null)"
      >
        {{ $t('filters.anyDate') }}
      </button>
    </div>
  </section>
</template>
