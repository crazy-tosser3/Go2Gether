import type { EventSummary } from '@/api'
import { intlLocale } from '@/i18n'
import { localDate } from '@/utils/datetime'

export interface CalendarDay {
  date: string
  day: number
  inMonth: boolean
}

export interface Month {
  year: number
  /** 0–11, как в Date. */
  month: number
}

export function monthOf(date: string | Date): Month {
  const d = typeof date === 'string' ? new Date(`${date}T00:00:00`) : date
  return { year: d.getFullYear(), month: d.getMonth() }
}

export function shiftMonth({ year, month }: Month, by: number): Month {
  const d = new Date(year, month + by, 1)
  return { year: d.getFullYear(), month: d.getMonth() }
}

export function monthRange({ year, month }: Month): { from: string; to: string } {
  return {
    from: localDate(new Date(year, month, 1)),
    to: localDate(new Date(year, month + 1, 0)),
  }
}

/** Сетка месяца по неделям с понедельника; края добиты днями соседних месяцев. */
export function monthGrid({ year, month }: Month): CalendarDay[][] {
  const first = new Date(year, month, 1)
  const lead = (first.getDay() + 6) % 7
  const start = new Date(year, month, 1 - lead)
  const weeks: CalendarDay[][] = []
  for (let w = 0; w < 6; w++) {
    const week: CalendarDay[] = []
    for (let i = 0; i < 7; i++) {
      const d = new Date(start.getFullYear(), start.getMonth(), start.getDate() + w * 7 + i)
      week.push({ date: localDate(d), day: d.getDate(), inMonth: d.getMonth() === month })
    }
    if (w > 3 && !week.some((d) => d.inMonth)) break
    weeks.push(week)
  }
  return weeks
}

/** Сколько мероприятий в каждый день — по местному времени пользователя. */
export function countByDay(events: Pick<EventSummary, 'starts_at'>[]): Record<string, number> {
  const counts: Record<string, number> = {}
  for (const e of events) {
    const key = localDate(new Date(e.starts_at))
    counts[key] = (counts[key] ?? 0) + 1
  }
  return counts
}

export function formatMonth({ year, month }: Month): string {
  const title = new Intl.DateTimeFormat(intlLocale(), { month: 'long', year: 'numeric' })
    .format(new Date(year, month, 1))
    .replace(/\s*г\.$/, '')
  return title.charAt(0).toUpperCase() + title.slice(1)
}

/** Короткие дни недели с понедельника на языке интерфейса: «пн»… или «Mon»… */
export function weekdays(): string[] {
  const f = new Intl.DateTimeFormat(intlLocale(), { weekday: 'short' })
  // 5 января 2026 — понедельник.
  return Array.from({ length: 7 }, (_, i) => f.format(new Date(2026, 0, 5 + i)))
}
