import type { Id, Level, ProfileInput, User } from '@/api'
import { t } from '@/i18n'

export interface ProfileFormState {
  name: string
  city: string
  level: Level | ''
  bio: string
  sportIds: Id[]
  /** Уровень в каждом выбранном виде спорта. */
  sportLevels: Record<Id, Level>
  avatar: string
}

export type ProfileFormErrors = Partial<Record<keyof ProfileFormState, string>>

export const BIO_MAX = 500

export function profileFormFrom(user: User): ProfileFormState {
  return {
    name: user.name,
    city: user.city ?? '',
    level: user.level ?? '',
    bio: user.bio ?? '',
    sportIds: [...user.sport_ids],
    sportLevels: { ...user.sport_levels },
    avatar: user.avatar_url ?? '',
  }
}

export function validateProfileForm(form: ProfileFormState): ProfileFormErrors {
  const errors: ProfileFormErrors = {}
  const name = form.name.trim()
  if (!name) errors.name = t('profile.errors.name')
  else if (name.length > 60) errors.name = t('common.maxLength', { n: 60 })
  if (form.bio.trim().length > BIO_MAX) errors.bio = t('common.maxLength', { n: BIO_MAX })
  return errors
}

export function toProfileInput(form: ProfileFormState): ProfileInput {
  return {
    name: form.name.trim(),
    city: form.city.trim(),
    bio: form.bio.trim(),
    level: form.level || undefined,
    sport_ids: form.sportIds,
    // Бэк принимает уровни только для выбранных видов — снятые отбрасываем.
    sport_levels: Object.fromEntries(
      form.sportIds.flatMap((id) => (form.sportLevels[id] ? [[id, form.sportLevels[id]]] : [])),
    ),
    avatar_url: form.avatar,
  }
}

// Фото принимает бэк: JPG, PNG или WEBP; размер проверяем заранее, чтобы не ждать отказа.
export const PHOTO_TYPES = ['image/jpeg', 'image/png', 'image/webp']
export const PHOTO_MAX_MB = 5

export function photoError(file: File): string | null {
  if (!PHOTO_TYPES.includes(file.type)) return t('profile.photo.type')
  if (file.size > PHOTO_MAX_MB * 1024 * 1024) return t('profile.photo.size', { n: PHOTO_MAX_MB })
  return null
}

/** Загруженное фото, а не рисунок из набора и не инициалы. */
export const isPhoto = (url: string | null | undefined) =>
  /^(https?:|data:image\/)/i.test(url ?? '')

export function toggleSport(ids: Id[], id: Id): Id[] {
  return ids.includes(id) ? ids.filter((x) => x !== id) : [...ids, id]
}
