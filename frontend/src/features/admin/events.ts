import type { Comment, EventStatus, EventSummary } from '@/api'
import { t } from '@/i18n'

export type EventStatusFilter = EventStatus | 'all'

export const EVENT_STATUS_FILTERS: { value: EventStatusFilter; label: string }[] = (
  ['published', 'unpublished', 'deleted', 'all'] as const
).map((value) => ({
  value,
  get label() {
    return t(`admin.filters.${value}`)
  },
}))

export function parseEventStatusFilter(value: unknown): EventStatusFilter {
  return EVENT_STATUS_FILTERS.some((f) => f.value === value)
    ? (value as EventStatusFilter)
    : 'published'
}

export type ModerationAction = 'unpublish' | 'delete' | 'restore'

/** Что админ может сделать с мероприятием в этом статусе — по правилам бэка. */
export function moderationActions(status: EventStatus): ModerationAction[] {
  switch (status) {
    case 'published':
      return ['unpublish', 'delete']
    case 'unpublished':
      return ['restore', 'delete']
    default:
      return []
  }
}

/** Новый статус строки таблицы по ответу сервера. */
export function replaceEventStatus(
  list: EventSummary[],
  id: number,
  status: EventStatus,
): EventSummary[] {
  return list.map((e) => (e.id === id ? { ...e, status } : e))
}

export function replaceComment(list: Comment[], updated: Comment): Comment[] {
  return list.map((c) => (c.id === updated.id ? updated : c))
}

/** Причину увидят организатор и участники, поэтому она обязательна и осмысленна. */
export function moderationReasonError(text: string): string | null {
  const v = text.trim()
  if (!v) return t('admin.reason.empty')
  if (v.length < 5) return t('admin.reason.short')
  if (v.length > 500) return t('common.maxLength', { n: 500 })
  return null
}
