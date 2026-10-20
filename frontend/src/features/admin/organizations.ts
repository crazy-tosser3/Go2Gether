import type { Organization, OrganizationStatus, OrganizationType } from '@/api'
import { t } from '@/i18n'

// Подписи — геттеры: текст берётся на языке интерфейса в момент показа.
export const ORG_TYPE_LABELS: Record<OrganizationType, string> = {
  get club() {
    return t('admin.orgTypes.club')
  },
  get community() {
    return t('admin.orgTypes.community')
  },
  get federation() {
    return t('admin.orgTypes.federation')
  },
  get company() {
    return t('admin.orgTypes.company')
  },
}

export type StatusFilter = OrganizationStatus | 'all'

export const STATUS_FILTERS: { value: StatusFilter; label: string }[] = (
  ['pending', 'approved', 'rejected', 'all'] as const
).map((value) => ({
  value,
  get label() {
    return t(`admin.filters.${value}`)
  },
}))

export function parseStatusFilter(value: unknown): StatusFilter {
  return STATUS_FILTERS.some((f) => f.value === value) ? (value as StatusFilter) : 'pending'
}

/** Обновляет строку в таблице ответом сервера, не перезагружая весь список. */
export function replaceOrganization(list: Organization[], updated: Organization): Organization[] {
  return list.map((o) => (o.id === updated.id ? updated : o))
}

export const REJECT_REASON_MAX = 500

/** Причину отказа организация увидит у себя, поэтому она обязательна и осмысленна. */
export function rejectReasonError(text: string): string | null {
  const v = text.trim()
  if (!v) return t('admin.reason.orgEmpty')
  if (v.length < 5) return t('admin.reason.orgShort')
  if (v.length > REJECT_REASON_MAX) return t('common.maxLength', { n: REJECT_REASON_MAX })
  return null
}
