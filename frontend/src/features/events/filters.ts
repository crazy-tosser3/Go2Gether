import type { LocationQuery, LocationQueryRaw } from 'vue-router'
import type { EventFilters, JoinMode, Level } from '@/api'
import { intlLocale, t } from '@/i18n'
import { quickDateRange, type QuickDate } from '@/utils/datetime'

const LEVELS: Level[] = ['beginner', 'amateur', 'pro']
const JOIN_MODES: JoinMode[] = ['open', 'approval']
const DATE_RE = /^\d{4}-\d{2}-\d{2}$/

// Подписи — геттеры: читаются при отрисовке и меняются вместе с языком; Object.entries их тоже видит.
function labels<K extends string>(prefix: string, keys: readonly K[]): Record<K, string> {
  const out = {} as Record<K, string>
  for (const key of keys)
    Object.defineProperty(out, key, { enumerable: true, get: () => t(`${prefix}.${key}`) })
  return out
}

export const LEVEL_LABELS = labels('level', LEVELS)

export const JOIN_MODE_LABELS = labels('joinMode', JOIN_MODES)

function str(value: LocationQuery[string] | undefined): string | undefined {
  const v = Array.isArray(value) ? value[0] : value
  return typeof v === 'string' && v.trim() ? v.trim() : undefined
}

/** Фильтры из адресной строки: мусор отбрасываем, чтобы не слать его в API. */
export function filtersFromQuery(query: LocationQuery): EventFilters {
  const f: EventFilters = {}
  const q = str(query.q)
  if (q) f.q = q
  const sport = Number(str(query.sport))
  if (Number.isInteger(sport) && sport > 0) f.sport = sport
  const city = str(query.city)
  if (city) f.city = city
  const from = str(query.date_from)
  if (from && DATE_RE.test(from)) f.date_from = from
  const to = str(query.date_to)
  if (to && DATE_RE.test(to)) f.date_to = to
  const level = str(query.level) as Level | undefined
  if (level && LEVELS.includes(level)) f.level = level
  const mode = str(query.join_mode) as JoinMode | undefined
  if (mode && JOIN_MODES.includes(mode)) f.join_mode = mode
  return f
}

export function filtersToQuery(f: EventFilters): LocationQueryRaw {
  const query: LocationQueryRaw = {}
  for (const [key, value] of Object.entries(f)) {
    if (key === 'page' || key === 'size' || value === undefined || value === '') continue
    query[key] = String(value)
  }
  return query
}

export function activeQuickDate(f: EventFilters, now = new Date()): QuickDate | null {
  for (const kind of ['today', 'tomorrow', 'weekend'] as const) {
    const r = quickDateRange(kind, now)
    if (f.date_from === r.from && f.date_to === r.to) return kind
  }
  return null
}

/** Сколько фильтров выбрано (текст поиска не считаем). */
export function countFilters(f: EventFilters) {
  return [f.sport, f.city, f.date_from || f.date_to, f.level, f.join_mode].filter(Boolean).length
}

export type FilterChipKey = 'date' | 'city' | 'sport' | 'level' | 'join_mode'

export interface FilterChip {
  key: FilterChipKey
  label: string
}

const QUICK_LABELS = labels('quickDate', ['today', 'tomorrow', 'weekend'] as const)

const day = (iso: string) =>
  new Intl.DateTimeFormat(intlLocale(), { day: 'numeric', month: 'long' }).format(
    new Date(`${iso}T00:00:00`),
  )

/** Подпись выбранных дат: «Завтра», «24 октября», «24 октября — 26 октября». */
export function dateLabel(f: EventFilters, now = new Date()): string | null {
  const quick = activeQuickDate(f, now)
  if (quick) return QUICK_LABELS[quick]
  if (f.date_from && f.date_to)
    return f.date_from === f.date_to ? day(f.date_from) : `${day(f.date_from)} — ${day(f.date_to)}`
  if (f.date_from) return t('filters.from', { date: day(f.date_from) })
  if (f.date_to) return t('filters.to', { date: day(f.date_to) })
  return null
}

/** Выбранные фильтры чипами под кнопкой «Фильтры» — каждый можно снять отдельно. */
export function filterChips(
  f: EventFilters,
  sportName: (id: number) => string | undefined,
  now = new Date(),
): FilterChip[] {
  const chips: FilterChip[] = []
  const date = dateLabel(f, now)
  if (date) chips.push({ key: 'date', label: date })
  if (f.city) chips.push({ key: 'city', label: f.city })
  if (f.sport) chips.push({ key: 'sport', label: sportName(f.sport) ?? t('filters.sport') })
  if (f.level) chips.push({ key: 'level', label: LEVEL_LABELS[f.level] })
  if (f.join_mode) chips.push({ key: 'join_mode', label: JOIN_MODE_LABELS[f.join_mode] })
  return chips
}

export function withoutFilter(f: EventFilters, key: FilterChipKey): EventFilters {
  const next = { ...f }
  if (key === 'date') {
    delete next.date_from
    delete next.date_to
  } else delete next[key]
  return next
}
