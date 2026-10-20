import type { AdminDashboard, EventSummary, ModerationLog } from '@/api'
import { intlLocale, t } from '@/i18n'

export interface Slice {
  key: string
  label: string
  value: number
}

export interface WeekSlice extends Slice {
  current: boolean
}

export function eventStatusSlices(d: AdminDashboard): Slice[] {
  return [
    { key: 'published', label: t('status.event.published'), value: d.published_events },
    { key: 'cancelled', label: t('status.event.cancelled'), value: d.cancelled_events },
    { key: 'unpublished', label: t('status.event.unpublished'), value: d.unpublished_events },
    { key: 'deleted', label: t('status.event.deleted'), value: d.deleted_events },
  ]
}

/** Самые частые значения по убыванию; хвост сворачивается в «Другие». */
export function topBy<T>(
  items: T[],
  keyOf: (item: T) => string | null | undefined,
  limit = 5,
): Slice[] {
  const counts = new Map<string, number>()
  for (const item of items) {
    const key = keyOf(item)?.trim()
    if (key) counts.set(key, (counts.get(key) ?? 0) + 1)
  }
  const sorted = [...counts]
    .map(([key, value]) => ({ key, label: key, value }))
    .sort((a, b) => b.value - a.value || a.label.localeCompare(b.label, 'ru'))
  if (sorted.length <= limit) return sorted
  const rest = sorted.slice(limit).reduce((sum, s) => sum + s.value, 0)
  return [...sorted.slice(0, limit), { key: '__other', label: t('stats.other'), value: rest }]
}

function startOfWeek(date: Date): Date {
  const d = new Date(date.getFullYear(), date.getMonth(), date.getDate())
  // Неделя с понедельника: getDay() у воскресенья — 0.
  d.setDate(d.getDate() - ((d.getDay() + 6) % 7))
  return d
}

/** Мероприятия по неделям начала: `before` недель назад, текущая и `after` вперёд. */
export function eventsByWeek(
  events: EventSummary[],
  now: Date,
  before = 3,
  after = 4,
): WeekSlice[] {
  const first = startOfWeek(now)
  first.setDate(first.getDate() - before * 7)
  const weeks: WeekSlice[] = []
  for (let i = 0; i <= before + after; i++) {
    const start = new Date(first)
    start.setDate(first.getDate() + i * 7)
    weeks.push({
      key: start.toISOString(),
      label: new Intl.DateTimeFormat(intlLocale(), { day: 'numeric', month: 'short' })
        .format(start)
        .replace('.', ''),
      value: 0,
      current: i === before,
    })
  }
  const firstMs = first.getTime()
  for (const e of events) {
    // Округляем: из-за перехода на летнее время между неделями не ровно 7 суток.
    const index = Math.floor(
      (startOfWeek(new Date(e.starts_at)).getTime() - firstMs) / 6048e5 + 0.5,
    )
    const week = weeks[index]
    if (week) week.value++
  }
  return weeks
}

export function moderationSlices(logs: ModerationLog[]): Slice[] {
  return topBy(logs, (l) => l.action, Infinity).map((s) => ({
    ...s,
    label: t(`stats.actions.${s.key}`),
  }))
}
