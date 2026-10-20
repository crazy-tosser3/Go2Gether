import type { EventStatus, OrganizationStatus, ParticipationStatus } from '@/api/types'
import { t } from '@/i18n'

// Статус всегда текстом и значком, цвет только помогает (дизайн-система Go2Gether): так его читают и при нарушении цветовосприятия.
export type StatusTone = 'ok' | 'wait' | 'queue' | 'no' | 'off'

export interface StatusView {
  tone: StatusTone
  label: string
}

// Подпись — геттер: читается при отрисовке, поэтому меняется вместе с языком.
const sv = (tone: StatusTone, key: string): StatusView => ({
  tone,
  get label() {
    return t(`status.${key}`)
  },
})

const participation: Record<ParticipationStatus, StatusView> = {
  confirmed: sv('ok', 'participation.confirmed'),
  pending: sv('wait', 'participation.pending'),
  waitlist: sv('queue', 'participation.waitlist'),
  rejected: sv('no', 'participation.rejected'),
  cancelled: sv('off', 'participation.cancelled'),
}

const event: Record<EventStatus, StatusView> = {
  published: sv('ok', 'event.published'),
  unpublished: sv('off', 'event.unpublished'),
  deleted: sv('off', 'event.deleted'),
  cancelled: sv('off', 'event.cancelled'),
}

const organization: Record<OrganizationStatus, StatusView> = {
  approved: sv('ok', 'organization.approved'),
  pending: sv('wait', 'organization.pending'),
  rejected: sv('no', 'organization.rejected'),
}

export type StatusOf =
  | { kind: 'participation'; status: ParticipationStatus }
  | { kind: 'event'; status: EventStatus }
  | { kind: 'organization'; status: OrganizationStatus }

export function statusView(s: StatusOf): StatusView {
  switch (s.kind) {
    case 'participation':
      return participation[s.status]
    case 'event':
      return event[s.status]
    case 'organization':
      return organization[s.status]
  }
}

export const toneIcon: Record<StatusTone, string> = {
  ok: '✓',
  wait: '◷',
  queue: '≡',
  no: '✕',
  off: '',
}
