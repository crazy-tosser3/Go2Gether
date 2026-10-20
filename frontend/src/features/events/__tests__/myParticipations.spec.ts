import { describe, it, expect } from 'vitest'
import type { EventStatus, MyParticipation, Participation, ParticipationStatus } from '@/api'
import {
  canCancel,
  groupMyParticipations,
  withParticipation,
} from '@/features/events/myParticipations'

const now = new Date('2026-10-19T12:00:00Z')

function item(
  id: number,
  status: ParticipationStatus,
  starts: string,
  eventStatus: EventStatus = 'published',
): MyParticipation {
  return {
    participation: { id, status, decision_reason: null } as Participation,
    event: { id: id * 10, starts_at: starts, ends_at: null, status: eventStatus },
  } as MyParticipation
}

describe('groupMyParticipations', () => {
  it('раскладывает по вкладкам', () => {
    const g = groupMyParticipations(
      [
        item(1, 'confirmed', '2026-10-21T10:00:00Z'),
        item(2, 'confirmed', '2026-10-20T10:00:00Z'),
        item(3, 'pending', '2026-10-22T10:00:00Z'),
        item(4, 'rejected', '2026-10-23T10:00:00Z'),
        item(5, 'confirmed', '2026-10-10T10:00:00Z'),
        item(6, 'cancelled', '2026-10-25T10:00:00Z'),
        item(7, 'confirmed', '2026-10-26T10:00:00Z', 'unpublished'),
      ],
      now,
    )
    expect(g.upcoming.map((i) => i.participation.id)).toEqual([2, 1])
    expect(g.pending.map((i) => i.participation.id)).toEqual([3])
    expect(g.rejected.map((i) => i.participation.id)).toEqual([4])
    expect(g.archive.map((i) => i.participation.id)).toEqual([7, 6, 5])
  })
})

describe('canCancel', () => {
  it('только действующая запись на будущее опубликованное событие', () => {
    expect(canCancel(item(1, 'confirmed', '2026-10-21T10:00:00Z'), now)).toBe(true)
    expect(canCancel(item(1, 'pending', '2026-10-21T10:00:00Z'), now)).toBe(true)
    expect(canCancel(item(1, 'confirmed', '2026-10-10T10:00:00Z'), now)).toBe(false)
    expect(canCancel(item(1, 'rejected', '2026-10-21T10:00:00Z'), now)).toBe(false)
    expect(canCancel(item(1, 'confirmed', '2026-10-21T10:00:00Z', 'deleted'), now)).toBe(false)
  })
})

describe('withParticipation', () => {
  it('подменяет запись ответом сервера', () => {
    const list = [item(1, 'confirmed', '2026-10-21T10:00:00Z')]
    const next = withParticipation(list, { id: 1, status: 'cancelled' } as Participation)
    expect(next[0]!.participation.status).toBe('cancelled')
  })
})
