import type { Comment } from '@/api'

export interface ThreadItem {
  comment: Comment
  replies: Comment[]
}

/** Один уровень вложенности: ответы на ответы показываем в ветке исходного вопроса. */
export function buildThread(comments: Comment[]): ThreadItem[] {
  const byId = new Map(comments.map((c) => [c.id, c]))
  const rootOf = (c: Comment): Comment => {
    let current = c
    const seen = new Set<number>()
    while (current.parent_id && byId.has(current.parent_id) && !seen.has(current.id)) {
      seen.add(current.id)
      current = byId.get(current.parent_id)!
    }
    return current
  }

  const sorted = [...comments].sort((a, b) => a.created_at.localeCompare(b.created_at))
  const items = new Map<number, ThreadItem>()
  for (const c of sorted) {
    const root = rootOf(c)
    if (root.id === c.id) items.set(c.id, { comment: c, replies: [] })
  }
  for (const c of sorted) {
    const root = rootOf(c)
    if (root.id !== c.id) items.get(root.id)?.replies.push(c)
  }
  return [...items.values()]
}
