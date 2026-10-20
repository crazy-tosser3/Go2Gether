import type { EventDetails, User } from '@/api'

export type JoinAction =
  | { kind: 'login' }
  | { kind: 'organizer' }
  | { kind: 'admin' }
  | { kind: 'past' }
  | { kind: 'full' }
  | { kind: 'join' }
  | { kind: 'apply'; again: boolean }
  | { kind: 'pending' }
  | { kind: 'confirmed' }
  | { kind: 'unavailable' }

/**
 * Что показать в блоке записи. Порядок важен: своя запись важнее, чем «мест нет».
 * Кто организатор и прошло ли событие, решает сервер (is_organizer, is_past).
 */
export function joinAction(event: EventDetails, user: User | null): JoinAction {
  // Снятое событие бэк отдаёт только участникам, организатору и админу — записи уже нет.
  if (event.status !== 'published') return { kind: 'unavailable' }
  if (!user) return { kind: 'login' }
  if (user.role === 'admin') return { kind: 'admin' }
  if (event.is_organizer) return { kind: 'organizer' }

  const mine = event.my_participation
  if (mine?.status === 'confirmed') return { kind: 'confirmed' }
  if (mine?.status === 'pending' || mine?.status === 'waitlist') return { kind: 'pending' }

  if (event.is_past) return { kind: 'past' }
  if (event.capacity !== null && event.confirmed_count >= event.capacity) return { kind: 'full' }

  if (event.join_mode === 'open') return { kind: 'join' }
  return { kind: 'apply', again: mine?.status === 'rejected' }
}
