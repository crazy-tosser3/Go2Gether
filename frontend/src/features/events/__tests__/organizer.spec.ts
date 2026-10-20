import { describe, it, expect } from 'vitest'
import type { Participant, Participation, ParticipationStatus } from '@/api'
import { applyDecision, groupParticipants } from '@/features/events/organizer'

function person(id: number, status: ParticipationStatus, created: string): Participant {
  return {
    participation_id: id,
    user: { id: id * 10, name: `Человек ${id}`, avatar_url: null },
    email: null,
    status,
    message: null,
    decision_reason: null,
    created_at: created,
  }
}

function result(id: number, status: ParticipationStatus, reason: string | null = null) {
  return { id, status, decision_reason: reason } as Participation
}

describe('groupParticipants', () => {
  it('раскладывает по спискам, заявки — в порядке подачи', () => {
    const g = groupParticipants([
      person(1, 'pending', '2026-10-19T10:00:00Z'),
      person(2, 'confirmed', '2026-10-18T10:00:00Z'),
      person(3, 'pending', '2026-10-19T09:00:00Z'),
      person(4, 'rejected', '2026-10-17T10:00:00Z'),
      person(5, 'cancelled', '2026-10-17T10:00:00Z'),
      person(6, 'waitlist', '2026-10-19T11:00:00Z'),
    ])
    expect(g.pending.map((p) => p.participation_id)).toEqual([3, 1, 6])
    expect(g.confirmed.map((p) => p.participation_id)).toEqual([2])
    expect(g.rejected.map((p) => p.participation_id)).toEqual([4])
  })
})

describe('applyDecision', () => {
  const groups = groupParticipants([
    person(1, 'pending', '2026-10-19T10:00:00Z'),
    person(2, 'confirmed', '2026-10-18T10:00:00Z'),
  ])

  it('принятая заявка переходит к участникам', () => {
    const g = applyDecision(groups, 1, result(1, 'confirmed'))
    expect(g.pending).toEqual([])
    expect(g.confirmed.map((p) => p.participation_id)).toEqual([2, 1])
  })

  it('отклонённая — в отказы с причиной', () => {
    const g = applyDecision(groups, 1, result(1, 'rejected', 'Группа уже собрана'))
    expect(g.pending).toEqual([])
    expect(g.rejected[0]!.decision_reason).toBe('Группа уже собрана')
  })

  it('неизвестная заявка ничего не меняет', () => {
    expect(applyDecision(groups, 99, result(99, 'confirmed'))).toBe(groups)
  })
})
