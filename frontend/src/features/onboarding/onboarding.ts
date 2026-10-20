import type { Id, Level, ProfileInput, User } from '@/api'
import { t } from '@/i18n'

export const STEPS = 5
export const MAX_SPORTS = 10

export type TimeSlot = 'morning' | 'day' | 'evening' | 'weekend'
export type AgePreset = 'any' | 'young' | 'mid' | 'senior'

// Подписи — геттеры: текст берётся на языке интерфейса в момент показа.
export const TIME_SLOTS: { id: TimeSlot; label: string; hint: string }[] = (
  ['morning', 'day', 'evening', 'weekend'] as const
).map((id) => ({
  id,
  get label() {
    return t(`onboarding.place.times.${id}`)
  },
  get hint() {
    return t(`onboarding.place.times.${id}Hint`)
  },
}))

export const AGE_PRESETS: { id: AgePreset; label: string; from: string; to: string }[] = [
  {
    id: 'any',
    get label() {
      return t('onboarding.age.any')
    },
    from: '',
    to: '',
  },
  { id: 'young', label: '18–25', from: '18', to: '25' },
  { id: 'mid', label: '25–40', from: '25', to: '40' },
  { id: 'senior', label: '40+', from: '40', to: '99' },
]

export interface OnboardingAnswers {
  avatar: string
  name: string
  birthYear: string
  city: string
  /** Вид спорта → уровень в нём. */
  sports: Record<Id, Level>
  radiusKm: number
  times: TimeSlot[]
  ageFrom: string
  ageTo: string
}

/** Черновик ответов между шагами: переживает перезагрузку, стирается после сохранения в профиль. */
export type LocalPrefs = Pick<
  OnboardingAnswers,
  'birthYear' | 'sports' | 'radiusKm' | 'times' | 'ageFrom' | 'ageTo'
>

export type OnboardingErrors = Partial<Record<'name' | 'birthYear' | 'ageFrom' | 'ageTo', string>>

// Vue отдаёт значение поля type="number" числом, а не строкой — приводим перед проверками.
const text = (v: unknown) => String(v ?? '').trim()

const str = (n: number | null) => (n == null ? '' : String(n))

/** Ответы из профиля; незаконченный черновик (если есть) важнее сохранённого. */
export function answersFrom(user: User, local: LocalPrefs | null): OnboardingAnswers {
  const sports: Record<Id, Level> = {}
  for (const id of user.sport_ids)
    sports[id] = local?.sports[id] ?? user.sport_levels[id] ?? user.level ?? 'beginner'
  return {
    avatar: user.avatar_url ?? '',
    name: user.name,
    birthYear: local?.birthYear ?? str(user.birth_year),
    city: user.city ?? '',
    sports,
    radiusKm: local?.radiusKm ?? user.radius_km ?? 10,
    times: local?.times ?? user.preferred_times,
    ageFrom: local?.ageFrom ?? str(user.preferred_age_from),
    ageTo: local?.ageTo ?? str(user.preferred_age_to),
  }
}

export function validateAbout(a: OnboardingAnswers, now = new Date()): OnboardingErrors {
  const errors: OnboardingErrors = {}
  const name = a.name.trim()
  if (!name) errors.name = t('onboarding.errors.name')
  else if (name.length > 60) errors.name = t('onboarding.errors.maxLength', { n: 60 })
  const year = text(a.birthYear)
  if (year) {
    const y = Number(year)
    const max = now.getFullYear() - 10
    if (!Number.isInteger(y) || y < 1920 || y > max)
      errors.birthYear = t('onboarding.errors.birthYear', { max })
  }
  return errors
}

export function validateAge(a: OnboardingAnswers): OnboardingErrors {
  const errors: OnboardingErrors = {}
  const from = text(a.ageFrom) ? Number(a.ageFrom) : null
  const to = text(a.ageTo) ? Number(a.ageTo) : null
  const bad = (n: number | null) => n !== null && (!Number.isInteger(n) || n < 10 || n > 99)
  if (bad(from)) errors.ageFrom = t('onboarding.errors.age')
  if (bad(to)) errors.ageTo = t('onboarding.errors.age')
  if (!errors.ageFrom && !errors.ageTo && from !== null && to !== null && from > to)
    errors.ageTo = t('onboarding.errors.ageOrder')
  return errors
}

/** Выбрать или снять вид спорта; больше MAX_SPORTS не даём. */
export function toggleSport(sports: Record<Id, Level>, id: Id): Record<Id, Level> {
  const next = { ...sports }
  if (id in next) delete next[id]
  else if (Object.keys(next).length < MAX_SPORTS) next[id] = 'beginner'
  return next
}

// Общий уровень профиля (по нему фильтрует лента) — самый частый из выбранных, при равенстве — что выше.
const LEVEL_ORDER: Level[] = ['beginner', 'amateur', 'pro']
export function overallLevel(sports: Record<Id, Level>): Level | undefined {
  const counts = new Map<Level, number>()
  for (const l of Object.values(sports)) counts.set(l, (counts.get(l) ?? 0) + 1)
  let best: Level | undefined
  for (const l of LEVEL_ORDER) if ((counts.get(l) ?? 0) >= (counts.get(best!) ?? 1)) best = l
  return best
}

const num = (v: unknown) => (text(v) ? Number(v) : undefined)

/** Тело PATCH /users/me. Пустое поле не шлём: null у бэка значит «не менять». */
export function toProfileUpdate(a: OnboardingAnswers): ProfileInput {
  return {
    name: a.name.trim(),
    city: a.city.trim(),
    avatar_url: a.avatar || undefined,
    sport_ids: Object.keys(a.sports).map(Number),
    level: overallLevel(a.sports),
    sport_levels: { ...a.sports },
    birth_year: num(a.birthYear),
    radius_km: a.radiusKm,
    preferred_times: a.times,
    preferred_age_from: num(a.ageFrom),
    preferred_age_to: num(a.ageTo),
  }
}

export function toLocalPrefs(a: OnboardingAnswers): LocalPrefs {
  return {
    birthYear: text(a.birthYear),
    sports: a.sports,
    radiusKm: a.radiusKm,
    times: a.times,
    ageFrom: text(a.ageFrom),
    ageTo: text(a.ageTo),
  }
}

export function agePresetOf(a: Pick<OnboardingAnswers, 'ageFrom' | 'ageTo'>): AgePreset | null {
  return AGE_PRESETS.find((p) => p.from === text(a.ageFrom) && p.to === text(a.ageTo))?.id ?? null
}

/** Попадает ли начало мероприятия в удобное время (по местным часам). */
export function inTimes(startsAt: string, times: TimeSlot[]): boolean {
  if (!times.length) return false
  const d = new Date(startsAt)
  const weekend = d.getDay() === 0 || d.getDay() === 6
  const h = d.getHours()
  const slot: TimeSlot = h < 11 ? 'morning' : h < 17 ? 'day' : 'evening'
  return (weekend && times.includes('weekend')) || times.includes(slot)
}

const KEY = (userId: Id) => `vmeste:onboarding:${userId}`

// Хранилище может быть недоступно (приватный режим) — тогда просто не помним ответы.
export function loadLocalPrefs(userId: Id): LocalPrefs | null {
  try {
    const raw = localStorage.getItem(KEY(userId))
    return raw ? (JSON.parse(raw) as LocalPrefs) : null
  } catch {
    return null
  }
}

export function saveLocalPrefs(userId: Id, prefs: LocalPrefs) {
  try {
    localStorage.setItem(KEY(userId), JSON.stringify(prefs))
  } catch {
    // нет хранилища — ответы останутся только на время сессии
  }
}

export function clearLocalPrefs(userId: Id) {
  try {
    localStorage.removeItem(KEY(userId))
  } catch {
    // нет хранилища — и стирать нечего
  }
}
