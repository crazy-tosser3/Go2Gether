import { localDate } from '@/utils/datetime'
import type { EventDetails, EventInput, EventUpdate, Id, JoinMode, Level } from '@/api/types'

export const NEW_SPORT_PREFIX = 'new:'

export interface EventFormState {
  title: string
  description: string
  /** id направления из справочника или `new:<название>` для своего. */
  sport: string | null
  date: string
  startTime: string
  endTime: string
  city: string
  address: string
  locationNote: string
  level: Level | ''
  joinMode: JoinMode | ''
  capacity: string
  requirements: string
  equipment: string
  organizationId: Id | null
  /** Точка на карте; без неё мероприятие не видно на карте. */
  point: { lat: number; lng: number } | null
}

export type EventFormErrors = Partial<Record<keyof EventFormState, string>>

export function emptyEventForm(): EventFormState {
  return {
    title: '',
    description: '',
    sport: null,
    date: '',
    startTime: '',
    endTime: '',
    city: '',
    address: '',
    locationNote: '',
    level: '',
    joinMode: '',
    capacity: '',
    requirements: '',
    equipment: '',
    organizationId: null,
    point: null,
  }
}

export function newSportValue(name: string): string {
  return `${NEW_SPORT_PREFIX}${name}`
}

// Дата и время из полей — местные; на сервер уходят в ISO с учётом часового пояса браузера.
export function toIso(date: string, time: string): string | null {
  if (!/^\d{4}-\d{2}-\d{2}$/.test(date) || !/^\d{2}:\d{2}$/.test(time)) return null
  const d = new Date(`${date}T${time}:00`)
  return Number.isNaN(d.getTime()) ? null : d.toISOString()
}

export function validateEventForm(form: EventFormState, now = new Date()): EventFormErrors {
  const errors: EventFormErrors = {}
  if (!form.title.trim()) errors.title = 'Укажите название'
  else if (form.title.trim().length > 120) errors.title = 'Не длиннее 120 символов'
  if (!form.sport) errors.sport = 'Выберите направление или впишите своё'
  if (!form.date) errors.date = 'Укажите дату'
  if (!form.startTime) errors.startTime = 'Укажите время начала'

  const start = toIso(form.date, form.startTime)
  if (start && new Date(start) <= now) errors.startTime = 'Время начала уже прошло'
  if (start && form.endTime) {
    const end = toIso(form.date, form.endTime)
    if (!end || new Date(end) <= new Date(start)) errors.endTime = 'Окончание позже начала'
  }

  if (!form.address.trim()) errors.address = 'Укажите место'
  if (!form.joinMode) errors.joinMode = 'Выберите, как записываться'
  const capacity = capacityText(form)
  if (capacity) {
    const n = Number(capacity)
    if (!Number.isInteger(n) || n < 1) errors.capacity = 'Целое число, не меньше 1'
    else if (n > 1000) errors.capacity = 'Не больше 1000'
  }
  return errors
}

// Vue приводит значение поля type="number" к числу, поэтому capacity может прийти не строкой.
function capacityText(form: EventFormState): string {
  return String(form.capacity ?? '').trim()
}

function optional(s: string): string | undefined {
  const t = s.trim()
  return t ? t : undefined
}

/** Собирает тело POST /events. Вызывать только для формы без ошибок. */
export function toEventInput(form: EventFormState): EventInput {
  const startsAt = toIso(form.date, form.startTime)!
  const endsAt = form.endTime ? (toIso(form.date, form.endTime) ?? undefined) : undefined
  const sport = form.sport ?? ''
  const input: EventInput = {
    title: form.title.trim(),
    description: form.description.trim(),
    starts_at: startsAt,
    ends_at: endsAt,
    city: optional(form.city),
    address: form.address.trim(),
    location_note: optional(form.locationNote),
    requirements: optional(form.requirements),
    equipment: optional(form.equipment),
    level: form.level || undefined,
    join_mode: form.joinMode as JoinMode,
    capacity: capacityText(form) ? Number(capacityText(form)) : undefined,
    organization_id: form.organizationId ?? undefined,
    lat: form.point?.lat,
    lng: form.point?.lng,
  }
  if (sport.startsWith(NEW_SPORT_PREFIX)) input.sport_name = sport.slice(NEW_SPORT_PREFIX.length)
  else input.sport_id = Number(sport)
  return input
}

const SERVER_FIELDS: Record<string, keyof EventFormState> = {
  title: 'title',
  description: 'description',
  sport_id: 'sport',
  sport_name: 'sport',
  starts_at: 'startTime',
  ends_at: 'endTime',
  city: 'city',
  address: 'address',
  location_note: 'locationNote',
  level: 'level',
  join_mode: 'joinMode',
  capacity: 'capacity',
  requirements: 'requirements',
  equipment: 'equipment',
  organization_id: 'organizationId',
}

/** Переносит ошибки 422 сервера на поля формы; неизвестные поля возвращает отдельно. */
export function mapServerErrors(fields: Record<string, string>): {
  errors: EventFormErrors
  rest: string[]
} {
  const errors: EventFormErrors = {}
  const rest: string[] = []
  for (const [key, message] of Object.entries(fields)) {
    const field = SERVER_FIELDS[key]
    if (field) errors[field] = message
    else rest.push(message)
  }
  return { errors, rest }
}

export interface AuthorOption {
  value: Id | null
  label: string
}

/** «От себя» и все допущенные организации пользователя — от их имени можно публиковать. */
export function authorOptions(organizations: { id: Id; name: string }[]): AuthorOption[] {
  return [
    { value: null, label: 'От себя' },
    ...organizations.map((o) => ({ value: o.id, label: o.name })),
  ]
}

function localTime(d: Date): string {
  return `${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

/** Форма редактирования из карточки мероприятия; дата и время — в часовом поясе браузера. */
export function eventFormFrom(event: EventDetails): EventFormState {
  const start = new Date(event.starts_at)
  return {
    title: event.title,
    description: event.description ?? '',
    sport: String(event.sport.id),
    date: localDate(start),
    startTime: localTime(start),
    endTime: event.ends_at ? localTime(new Date(event.ends_at)) : '',
    city: event.city ?? '',
    address: event.address,
    locationNote: event.location_note ?? '',
    level: event.level ?? '',
    joinMode: event.join_mode,
    capacity: event.capacity != null ? String(event.capacity) : '',
    requirements: event.requirements ?? '',
    equipment: event.equipment ?? '',
    organizationId: event.organizer.type === 'organization' ? event.organizer.id : null,
    point:
      typeof event.lat === 'number' && typeof event.lng === 'number'
        ? { lat: event.lat, lng: event.lng }
        : null,
  }
}

// PATCH бэка: null — «не менять», поэтому число мест, окончание и уровень, если они были, очистить нельзя.
export function validateEventEdit(
  form: EventFormState,
  original: EventFormState,
  now = new Date(),
): EventFormErrors {
  const errors = validateEventForm(form, now)
  const timeChanged = form.date !== original.date || form.startTime !== original.startTime
  if (!timeChanged && errors.startTime === 'Время начала уже прошло') delete errors.startTime
  if (original.capacity && !capacityText(form))
    errors.capacity = 'Убрать ограничение нельзя — укажите число'
  if (original.endTime && !form.endTime) errors.endTime = 'Окончание можно изменить, но не убрать'
  if (original.level && !form.level) errors.level = 'Уровень можно изменить, но не убрать'
  return errors
}

const TEXT_FIELDS = {
  title: 'title',
  description: 'description',
  city: 'city',
  address: 'address',
  locationNote: 'location_note',
  requirements: 'requirements',
  equipment: 'equipment',
} as const

/** Тело PATCH /events/{id}: только изменённые поля. Вызывать для формы без ошибок. */
export function toEventUpdate(form: EventFormState, original: EventFormState): EventUpdate {
  const update: EventUpdate = {}
  for (const [key, api] of Object.entries(TEXT_FIELDS) as [keyof typeof TEXT_FIELDS, string][]) {
    const value = form[key].trim()
    if (value !== original[key].trim()) (update as Record<string, string>)[api] = value
  }
  if (form.sport !== original.sport && form.sport) {
    if (form.sport.startsWith(NEW_SPORT_PREFIX))
      update.sport_name = form.sport.slice(NEW_SPORT_PREFIX.length)
    else update.sport_id = Number(form.sport)
  }
  if (form.date !== original.date || form.startTime !== original.startTime)
    update.starts_at = toIso(form.date, form.startTime)!
  if (form.endTime && (form.endTime !== original.endTime || form.date !== original.date))
    update.ends_at = toIso(form.date, form.endTime) ?? undefined
  if (form.level && form.level !== original.level) update.level = form.level
  if (form.joinMode && form.joinMode !== original.joinMode) update.join_mode = form.joinMode
  const capacity = capacityText(form)
  if (capacity && capacity !== original.capacity) update.capacity = Number(capacity)
  if (
    form.point &&
    (form.point.lat !== original.point?.lat || form.point.lng !== original.point?.lng)
  ) {
    update.lat = form.point.lat
    update.lng = form.point.lng
  }
  return update
}

/**
 * Форма «Повторить» из прошлого мероприятия: всё, кроме даты и времени.
 * От имени организации — только если она по-прежнему допущена.
 */
export function repeatFormFrom(event: EventDetails, approvedOrganizationIds: Id[]): EventFormState {
  const form = eventFormFrom(event)
  return {
    ...form,
    date: '',
    startTime: '',
    endTime: '',
    organizationId:
      form.organizationId !== null && approvedOrganizationIds.includes(form.organizationId)
        ? form.organizationId
        : null,
  }
}
