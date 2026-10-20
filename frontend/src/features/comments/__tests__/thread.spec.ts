import { describe, expect, it } from 'vitest'
import type { Comment } from '@/api'
import { buildThread } from '../thread'

const c = (id: number, created_at: string, parent_id: number | null = null) =>
  ({ id, created_at, parent_id }) as Comment

describe('buildThread', () => {
  it('группирует ответы под вопросом по времени, вложенные ответы поднимает в ту же ветку', () => {
    const thread = buildThread([
      c(12, '2026-10-21T10:05:00Z', 1),
      c(1, '2026-10-21T10:00:00Z'),
      c(2, '2026-10-21T10:02:00Z'),
      c(13, '2026-10-21T10:06:00Z', 12),
    ])
    expect(thread.map((t) => [t.comment.id, t.replies.map((r) => r.id)])).toEqual([
      [1, [12, 13]],
      [2, []],
    ])
  })

  it('ответ на удалённое сообщение становится отдельной веткой', () => {
    expect(buildThread([c(11, '2026-10-21T10:00:00Z', 99)]).map((t) => t.comment.id)).toEqual([11])
  })
})
