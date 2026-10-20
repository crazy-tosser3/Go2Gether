import type { MyParticipation, Participation } from '@/api'

export type MyTab = 'upcoming' | 'pending' | 'rejected' | 'archive'

export const MY_TABS: { value: MyTab; label: string }[] = [
  { value: 'upcoming', label: 'Предстоящие' },
  { value: 'pending', label: 'Ждут решения' },
  { value: 'rejected', label: 'Отказы' },
  { value: 'archive', label: 'Архив' },
]

export type MyGroups = Record<MyTab, MyParticipation[]>

export function isPast(item: MyParticipation, now: Date): boolean {
  const end = item.event.ends_at ?? item.event.starts_at
  return new Date(end) < now
}

function tabOf(item: MyParticipation, now: Date): MyTab {
  const { participation, event } = item
  // Снятое, удалённое или прошедшее — уже история, даже если запись была подтверждена.
  if (event.status !== 'published' || isPast(item, now)) return 'archive'
  switch (participation.status) {
    case 'confirmed':
      return 'upcoming'
    case 'pending':
    case 'waitlist':
      return 'pending'
    case 'rejected':
      return 'rejected'
    default:
      return 'archive'
  }
}

const byStart = (a: MyParticipation, b: MyParticipation) =>
  a.event.starts_at.localeCompare(b.event.starts_at)

/** Раскладывает записи по вкладкам: ближайшие — сверху, в архиве — свежие сверху. */
export function groupMyParticipations(items: MyParticipation[], now = new Date()): MyGroups {
  const groups: MyGroups = { upcoming: [], pending: [], rejected: [], archive: [] }
  for (const item of items) groups[tabOf(item, now)].push(item)
  groups.upcoming.sort(byStart)
  groups.pending.sort(byStart)
  groups.rejected.sort(byStart)
  groups.archive.sort((a, b) => byStart(b, a))
  return groups
}

/** Отменить можно только действующую запись на ещё не прошедшее опубликованное событие. */
export function canCancel(item: MyParticipation, now = new Date()): boolean {
  const s = item.participation.status
  return (
    (s === 'confirmed' || s === 'pending' || s === 'waitlist') &&
    item.event.status === 'published' &&
    !isPast(item, now)
  )
}

export function withParticipation(
  items: MyParticipation[],
  updated: Participation,
): MyParticipation[] {
  return items.map((i) =>
    i.participation.id === updated.id ? { ...i, participation: updated } : i,
  )
}
