import type { LocationQuery } from 'vue-router'
import type { EventSummary } from '@/api'

// Сортировки в API нет — сортируем загруженное на фронте.
export type EventSort = 'soon' | 'seats' | 'popular' | 'new'

// Подписи — в словаре: sort.<value>.
export const SORT_OPTIONS: { value: EventSort }[] = [
  { value: 'soon' },
  { value: 'seats' },
  { value: 'popular' },
  { value: 'new' },
]

export function sortFromQuery(query: LocationQuery): EventSort {
  const value = Array.isArray(query.sort) ? query.sort[0] : query.sort
  return SORT_OPTIONS.some((o) => o.value === value) ? (value as EventSort) : 'soon'
}

/** Для адреса: порядок по умолчанию не пишем. */
export function sortToQuery(sort: EventSort): { sort?: EventSort } {
  return sort === 'soon' ? {} : { sort }
}

function freeSeats(e: EventSummary): number {
  return e.capacity == null ? Infinity : Math.max(0, e.capacity - e.confirmed_count)
}

const byStart = (a: EventSummary, b: EventSummary) => a.starts_at.localeCompare(b.starts_at)

const compare: Record<EventSort, (a: EventSummary, b: EventSummary) => number> = {
  soon: byStart,
  // Без ограничения мест — свободнее всех; полные — в конце.
  seats: (a, b) => freeSeats(b) - freeSeats(a) || byStart(a, b),
  popular: (a, b) => b.confirmed_count - a.confirmed_count || byStart(a, b),
  // Даты создания в карточке ленты нет; id растёт с каждым новым мероприятием.
  new: (a, b) => b.id - a.id,
}

export function sortEvents(events: EventSummary[], sort: EventSort): EventSummary[] {
  return [...events].sort(compare[sort])
}
