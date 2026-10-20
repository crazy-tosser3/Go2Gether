import type { Id, Participant, Participation } from '@/api'

export interface ParticipantGroups {
  pending: Participant[]
  confirmed: Participant[]
  rejected: Participant[]
}

export function emptyGroups(): ParticipantGroups {
  return { pending: [], confirmed: [], rejected: [] }
}

const byCreated = (a: Participant, b: Participant) => a.created_at.localeCompare(b.created_at)

/** Раскладывает людей по спискам экрана; первые заявки — сверху, как в очереди. */
export function groupParticipants(all: Participant[]): ParticipantGroups {
  const groups = emptyGroups()
  for (const p of all) {
    if (p.status === 'pending' || p.status === 'waitlist') groups.pending.push(p)
    else if (p.status === 'confirmed') groups.confirmed.push(p)
    else if (p.status === 'rejected') groups.rejected.push(p)
  }
  groups.pending.sort(byCreated)
  groups.confirmed.sort(byCreated)
  groups.rejected.sort(byCreated).reverse()
  return groups
}

/**
 * Переносит человека в нужный список по ответу сервера — без перезагрузки экрана.
 * Статус берём из ответа, а не из нажатой кнопки: сервер мог отказать или поставить в очередь.
 */
export function applyDecision(
  groups: ParticipantGroups,
  participationId: Id,
  result: Participation,
): ParticipantGroups {
  const all = [...groups.pending, ...groups.confirmed, ...groups.rejected]
  const person = all.find((p) => p.participation_id === participationId)
  if (!person) return groups
  const updated: Participant = {
    ...person,
    status: result.status,
    decision_reason: result.decision_reason,
  }
  return groupParticipants(all.map((p) => (p.participation_id === participationId ? updated : p)))
}

export const REJECT_REASONS = ['Группа уже собрана', 'Не подходит уровень', 'Другое время'] as const
