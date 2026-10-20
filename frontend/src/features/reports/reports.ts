import type { EventDetails, Report, ReportReason, ReportStatus, User } from '@/api'
import { t } from '@/i18n'

export const REPORT_REASONS: ReportReason[] = [
  'misleading',
  'dangerous',
  'fraud',
  'offensive',
  'spam',
  'other',
]

export const COMMENT_MAX = 1000

export const reasonLabel = (r: ReportReason) => t(`reports.reasons.${r}`)
export const statusLabel = (s: ReportStatus) => t(`reports.status.${s}`)

/** Как у бэка: причина обязательна, для «Другое» нужен комментарий, не длиннее 1000. */
export function reportError(
  reason: ReportReason | null,
  comment: string,
): { reason?: string; comment?: string } {
  const errors: { reason?: string; comment?: string } = {}
  if (!reason) errors.reason = t('reports.errors.reason')
  if (reason === 'other' && !comment.trim()) errors.comment = t('reports.errors.comment')
  if (comment.length > COMMENT_MAX) errors.comment = t('common.maxLength', { n: COMMENT_MAX })
  return errors
}

/** Пожаловаться можно на опубликованное чужое мероприятие; админ решает жалобы, а не подаёт. */
export function canReport(event: EventDetails, user: User | null): boolean {
  return !!user && user.role !== 'admin' && !event.is_organizer && event.status === 'published'
}

export type ReportFilter = ReportStatus | 'all'
export const REPORT_FILTERS: ReportFilter[] = ['pending', 'accepted', 'rejected', 'all']

export function parseReportFilter(value: unknown): ReportFilter {
  return REPORT_FILTERS.includes(value as ReportFilter) ? (value as ReportFilter) : 'pending'
}

export function replaceReport(list: Report[], updated: Report): Report[] {
  return list.map((r) => (r.id === updated.id ? updated : r))
}
