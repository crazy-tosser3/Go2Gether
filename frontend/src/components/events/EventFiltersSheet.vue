<script setup lang="ts">
import { computed, nextTick, ref, useId, watch } from 'vue'
import { eventsApi, type EventFilters, type JoinMode, type Level } from '@/api'
import EventsCalendar from '@/components/events/EventsCalendar.vue'
import { UiButton, UiChip, UiSelect, UiTextField, type UiOption } from '@/components/ui'
import { t } from '@/i18n'
import {
  JOIN_MODE_LABELS,
  LEVEL_LABELS,
  activeQuickDate,
  dateLabel,
} from '@/features/events/filters'
import { quickDateRange, type QuickDate } from '@/utils/datetime'

const props = defineProps<{ filters: EventFilters; total: number; sportOptions: UiOption[] }>()
const open = defineModel<boolean>('open', { default: false })
const emit = defineEmits<{ apply: [filters: EventFilters] }>()

const id = useId()
const dialog = ref<HTMLDialogElement>()

// Черновик: условия меняются внутри шторки и применяются только по «Показать».
const draft = ref<EventFilters>({})
const count = ref<number | null>(null)
const counting = ref(false)
let skipCount = false

const QUICK: QuickDate[] = ['today', 'tomorrow', 'weekend']
const levelOptions = computed<UiOption[]>(() =>
  (Object.keys(LEVEL_LABELS) as Level[]).map((value) => ({ value, label: LEVEL_LABELS[value] })),
)
const JOIN_MODES: (JoinMode | undefined)[] = [undefined, 'open', 'approval']

watch(
  open,
  async (value) => {
    if (value) {
      // Для уже применённых условий число известно — считаем заново только после изменений.
      skipCount = true
      count.value = props.total
      draft.value = { ...props.filters, page: undefined }
    }
    await nextTick()
    const el = dialog.value
    if (!el) return
    if (value && !el.open) el.showModal?.()
    if (!value && el.open) el.close()
  },
  { immediate: true },
)

let countTimer: ReturnType<typeof setTimeout> | undefined
let countId = 0
watch(
  draft,
  (f) => {
    if (!open.value) return
    if (skipCount) {
      skipCount = false
      return
    }
    clearTimeout(countTimer)
    counting.value = true
    countTimer = setTimeout(async () => {
      const id = ++countId
      try {
        const page = await eventsApi.list({ ...f, page: 0, size: 1 })
        if (id === countId) count.value = page.total
      } catch {
        if (id === countId) count.value = null
      } finally {
        if (id === countId) counting.value = false
      }
    }, 300)
  },
  { deep: true },
)

const quick = computed(() => activeQuickDate(draft.value))
const day = computed(() =>
  draft.value.date_from && draft.value.date_from === draft.value.date_to
    ? draft.value.date_from
    : null,
)
const dateText = computed(() => dateLabel(draft.value))

function toggleQuick(kind: QuickDate) {
  if (quick.value === kind) setDates(undefined, undefined)
  else {
    const r = quickDateRange(kind)
    setDates(r.from, r.to)
  }
}
function setDates(from: string | undefined, to: string | undefined) {
  draft.value = { ...draft.value, date_from: from, date_to: to }
}

const sport = computed({
  get: () => (draft.value.sport ? String(draft.value.sport) : ''),
  set: (v: string) => (draft.value = { ...draft.value, sport: v ? Number(v) : undefined }),
})
const level = computed({
  get: () => draft.value.level ?? '',
  set: (v: string) =>
    (draft.value = { ...draft.value, level: (v || undefined) as Level | undefined }),
})
const city = computed({
  get: () => draft.value.city ?? '',
  set: (v: string) => (draft.value = { ...draft.value, city: v.trim() ? v : undefined }),
})

function clear() {
  draft.value = { q: draft.value.q }
}

function apply() {
  emit('apply', { ...draft.value, city: draft.value.city?.trim() || undefined })
  open.value = false
}

const applyText = computed(() => {
  if (count.value === null) return t('filters.show')
  if (count.value === 0) return t('filters.nothing')
  return t('filters.showN', { n: count.value })
})
const applyLabel = computed(() =>
  count.value ? t('filters.showEvents', { events: t('events.count', count.value) }) : undefined,
)

function onBackdrop(e: MouseEvent) {
  if (e.target === dialog.value) open.value = false
}
</script>

<template>
  <!-- На телефоне — шторка снизу, на компьютере — панель справа: список остаётся виден. -->
  <dialog
    ref="dialog"
    :aria-labelledby="`${id}-title`"
    class="mx-0 mt-auto mb-0 max-h-[92dvh] w-full max-w-full overflow-hidden rounded-t-[32px] bg-card p-0 text-ink backdrop:bg-ink/40 md:mt-0 md:mr-0 md:ml-auto md:h-dvh md:max-h-dvh md:w-[440px] md:rounded-none md:rounded-l-[32px]"
    @close="open = false"
    @click="onBackdrop"
  >
    <div v-if="open" class="flex max-h-[92dvh] flex-col md:h-dvh md:max-h-dvh">
      <header class="flex items-center justify-between gap-3 px-5 pt-5 pb-3">
        <h2 :id="`${id}-title`" class="text-xl font-bold">{{ $t('filters.title') }}</h2>
        <button
          type="button"
          :aria-label="$t('common.close')"
          class="grid size-11 flex-none place-items-center rounded-full bg-field hover:bg-mute"
          @click="open = false"
        >
          ✕
        </button>
      </header>

      <div class="flex flex-1 flex-col gap-6 overflow-y-auto px-5 pb-4">
        <fieldset class="flex flex-col gap-3">
          <legend class="mb-2 font-bold">{{ $t('filters.when') }}</legend>
          <div class="flex flex-wrap gap-2">
            <UiChip v-for="q in QUICK" :key="q" :pressed="quick === q" @click="toggleQuick(q)">
              {{ $t(`quickDate.${q}`) }}
            </UiChip>
          </div>
          <EventsCalendar
            class="!max-w-none !bg-field"
            :filters="draft"
            :selected="day"
            @select="(d) => setDates(d ?? undefined, d ?? undefined)"
          />
          <p class="text-sm text-ink-2" aria-live="polite">
            {{ dateText ? $t('filters.selected', { date: dateText }) : $t('filters.anyDate') }}
          </p>
        </fieldset>

        <UiTextField
          v-model="city"
          :label="$t('filters.city')"
          :placeholder="$t('filters.anyCity')"
        />
        <UiSelect
          v-model="sport"
          :label="$t('filters.sport')"
          :options="sportOptions"
          :placeholder="$t('filters.allSports')"
        />
        <UiSelect
          v-model="level"
          :label="$t('filters.level')"
          :options="levelOptions"
          :placeholder="$t('filters.anyLevel')"
        />

        <fieldset class="flex flex-col gap-2">
          <legend class="mb-2 font-bold">{{ $t('filters.joinMode') }}</legend>
          <div class="flex flex-wrap gap-2">
            <UiChip
              v-for="mode in JOIN_MODES"
              :key="mode ?? 'any'"
              :pressed="draft.join_mode === mode"
              @click="draft = { ...draft, join_mode: mode }"
            >
              {{ mode ? JOIN_MODE_LABELS[mode] : $t('filters.anyJoin') }}
            </UiChip>
          </div>
        </fieldset>
      </div>

      <footer
        class="flex gap-2 border-t border-mute bg-card px-5 pt-3 pb-[max(1.25rem,env(safe-area-inset-bottom))]"
      >
        <UiButton variant="ghost" class="!w-auto !px-4" @click="clear">{{
          $t('filters.reset')
        }}</UiButton>
        <UiButton
          class="flex-1 !px-3"
          :loading="counting"
          :disabled="count === 0"
          :aria-label="applyLabel"
          @click="apply"
        >
          {{ applyText }}
        </UiButton>
      </footer>
    </div>
  </dialog>
</template>
