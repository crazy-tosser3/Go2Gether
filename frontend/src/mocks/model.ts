import { HttpResponse } from 'msw'
import type {
  ApiErrorBody,
  Comment,
  EventDetails,
  EventSummary,
  Id,
  Organization,
  OrganizationShort,
  Organizer,
  Participant,
  Participation,
  Report,
  Sport,
  User,
  UserShort,
} from '@/api/types'
import {
  db,
  type CommentRow,
  type EventRow,
  type OrganizationRow,
  type ParticipationRow,
  type ReportRow,
  type SportRow,
  type UserRow,
} from './db'

export class MockError extends Error {
  constructor(
    readonly status: number,
    readonly code: string,
    message: string,
    readonly fields: Record<string, string> | null = null,
  ) {
    super(message)
  }

  toResponse() {
    const body: ApiErrorBody = {
      error: { code: this.code, message: this.message, fields: this.fields },
    }
    return HttpResponse.json(body, { status: this.status })
  }
}

export const fail = (
  status: number,
  code: string,
  message: string,
  fields: Record<string, string> | null = null,
): never => {
  throw new MockError(status, code, message, fields)
}

export const validation = (fields: Record<string, string>): never =>
  fail(422, 'VALIDATION_ERROR', 'Ошибка валидации', fields)

/** Как у бэка: токен в cookie jwt (HttpOnly), Bearer тоже принимается. */
export function sessionToken(request: Request) {
  const header = request.headers.get('Authorization') ?? ''
  if (header.startsWith('Bearer ')) return header.slice(7)
  return /(?:^|;\s*)jwt=([^;]+)/.exec(request.headers.get('cookie') ?? '')?.[1] ?? ''
}

export function currentUser(request: Request): UserRow | null {
  const userId = db.sessions.get(sessionToken(request))
  return db.users.find((u) => u.id === userId) ?? null
}

export function requireUser(request: Request): UserRow {
  return (
    currentUser(request) ??
    fail(401, 'UNAUTHORIZED', 'Требуется авторизация или токен недействителен')
  )
}

export function requireAdmin(request: Request): UserRow {
  const user = requireUser(request)
  if (user.role !== 'admin') fail(403, 'FORBIDDEN', 'Раздел только для администратора')
  return user
}

export const findUser = (id: Id) => db.users.find((u) => u.id === id)
export const findOrg = (id: Id) => db.organizations.find((o) => o.id === id)
export const findSport = (id: Id) => db.sports.find((s) => s.id === id)
export const findEvent = (id: Id) =>
  db.events.find((e) => e.id === id) ?? fail(404, 'NOT_FOUND', 'Мероприятие не найдено')

export const ownedOrgs = (userId: Id) => db.organizations.filter((o) => o.owner_id === userId)

export function isOrganizer(event: EventRow, user: UserRow | null) {
  if (!user) return false
  if (event.organizer_user_id === user.id) return true
  const org = event.organizer_org_id ? findOrg(event.organizer_org_id) : undefined
  return org?.owner_id === user.id
}

export const isPast = (event: EventRow, now = new Date()) => new Date(event.starts_at) <= now

export const isActive = (p: ParticipationRow) => p.status === 'pending' || p.status === 'confirmed'

export function confirmedCount(eventId: Id) {
  return db.participations.filter((p) => p.event_id === eventId && p.status === 'confirmed').length
}

export function myParticipation(eventId: Id, userId: Id) {
  const mine = db.participations
    .filter((p) => p.event_id === eventId && p.user_id === userId)
    .sort((a, b) => b.created_at.localeCompare(a.created_at) || b.id - a.id)
  return mine.find(isActive) ?? mine[0] ?? null
}

export function unavailable(event: EventRow): never {
  const reason = event.moderation_reason ?? 'мероприятие снято с публикации'
  return fail(410, 'EVENT_UNAVAILABLE', `Мероприятие недоступно: ${reason}`, {
    reason,
    status: event.status,
  })
}

export const toUserShort = (u: UserRow): UserShort => ({
  id: u.id,
  name: u.name,
  avatar_url: u.avatar_url,
})

export function toUser(u: UserRow): User {
  return {
    id: u.id,
    role: u.role,
    name: u.name,
    email: u.email,
    avatar_url: u.avatar_url,
    city: u.city,
    bio: u.bio,
    level: u.level,
    sport_ids: u.sport_ids,
    sport_levels: u.sport_levels,
    birth_year: u.birth_year,
    radius_km: u.radius_km,
    preferred_age_from: u.preferred_age_from,
    preferred_age_to: u.preferred_age_to,
    preferred_times: u.preferred_times,
    is_demo: u.is_demo,
    created_at: u.created_at,
  }
}

export const toSport = (s: SportRow): Sport => ({
  id: s.id,
  name: s.name,
  category: s.category,
  icon: s.icon,
  is_custom: s.is_custom,
})

export const toOrgShort = (o: OrganizationRow): OrganizationShort => ({
  id: o.id,
  name: o.name,
  type: o.type,
  city: o.city,
  status: o.status,
})

export function toOrganization(o: OrganizationRow): Organization {
  return {
    id: o.id,
    name: o.name,
    description: o.description,
    type: o.type,
    sports_description: o.sports_description,
    city: o.city,
    website_url: o.website_url,
    avatar_url: o.avatar_url,
    contacts: o.contacts,
    status: o.status,
    reject_reason: o.reject_reason,
    decided_at: o.decided_at,
    owner: toUserShort(findUser(o.owner_id)!),
    created_at: o.created_at,
    updated_at: o.updated_at,
  }
}

export function toParticipation(p: ParticipationRow): Participation {
  return {
    id: p.id,
    event_id: p.event_id,
    user_id: p.user_id,
    status: p.status,
    message: p.message,
    decision_reason: p.decision_reason,
    decided_at: p.decided_at,
    created_at: p.created_at,
  }
}

export function toParticipant(p: ParticipationRow, withEmail: boolean): Participant {
  const u = findUser(p.user_id)!
  return {
    participation_id: p.id,
    user: toUserShort(u),
    email: withEmail ? u.email : null,
    status: p.status,
    message: p.message,
    decision_reason: p.decision_reason,
    created_at: p.created_at,
  }
}

function toOrganizer(e: EventRow): Organizer {
  const org =
    e.organizer_type === 'organization' && e.organizer_org_id
      ? findOrg(e.organizer_org_id)
      : undefined
  if (org) return { type: 'organization', id: org.id, name: org.name, avatar_url: org.avatar_url }
  const u = findUser(e.organizer_user_id)!
  return { type: 'user', id: u.id, name: u.name, avatar_url: u.avatar_url }
}

export function toEventSummary(e: EventRow): EventSummary {
  return {
    id: e.id,
    title: e.title,
    sport: toSport(findSport(e.sport_id)!),
    organizer: toOrganizer(e),
    starts_at: e.starts_at,
    ends_at: e.ends_at,
    city: e.city,
    address: e.address,
    level: e.level,
    join_mode: e.join_mode,
    capacity: e.capacity,
    confirmed_count: confirmedCount(e.id),
    status: e.status,
    is_past: isPast(e),
    is_edited: e.updated_at !== e.created_at,
    lat: e.lat,
    lng: e.lng,
    unavailable_reason: e.status === 'published' ? null : e.moderation_reason,
  }
}

export function toEventDetails(e: EventRow, viewer: UserRow | null): EventDetails {
  const mine = viewer ? myParticipation(e.id, viewer.id) : null
  const organizer = isOrganizer(e, viewer)
  // Точное место и контакты организатора видят организатор, подтверждённые участники и админ.
  const fullInfo = organizer || mine?.status === 'confirmed' || viewer?.role === 'admin'
  const owner = findUser(e.organizer_user_id)
  return {
    ...toEventSummary(e),
    description: e.description,
    organizer_contacts: fullInfo ? (owner?.contacts ?? owner?.email ?? null) : null,
    location_note: fullInfo ? e.location_note : null,
    lat: e.lat,
    lng: e.lng,
    requirements: e.requirements,
    equipment: e.equipment,
    participants: db.participations
      .filter((p) => p.event_id === e.id && p.status === 'confirmed')
      .map((p) => toUserShort(findUser(p.user_id)!)),
    unavailable_reason: e.status === 'published' ? null : e.moderation_reason,
    is_organizer: organizer,
    my_participation: mine ? toParticipation(mine) : null,
    created_at: e.created_at,
    updated_at: e.updated_at,
  }
}

export function toComment(c: CommentRow, { reveal = false } = {}): Comment {
  const event = db.events.find((e) => e.id === c.event_id)
  const author = findUser(c.author_id)!
  const asOrg = c.as_organization_id ? findOrg(c.as_organization_id) : undefined
  return {
    id: c.id,
    event_id: c.event_id,
    parent_id: c.parent_id,
    author: toUserShort(author),
    as_organization: asOrg ? toOrgShort(asOrg) : null,
    is_organizer: event ? isOrganizer(event, author) : false,
    text: c.is_hidden && !reveal ? null : c.text,
    is_hidden: c.is_hidden,
    hidden_reason: c.hidden_reason,
    is_edited: c.updated_at !== c.created_at,
    created_at: c.created_at,
    updated_at: c.updated_at === c.created_at ? null : c.updated_at,
  }
}

/** Страницы с нуля, как у бэка (Spring). */
export function paginate<T>(items: T[], page = 0, size = 20) {
  const safePage = Number.isInteger(page) && page > 0 ? page : 0
  const safeSize = Number.isInteger(size) && size > 0 ? Math.min(100, size) : 20
  return {
    items: items.slice(safePage * safeSize, (safePage + 1) * safeSize),
    total: items.length,
    page: safePage,
    size: safeSize,
    total_pages: Math.max(1, Math.ceil(items.length / safeSize)),
  }
}

export function toReport(r: ReportRow): Report {
  return {
    id: r.id,
    event: toEventSummary(findEvent(r.event_id)!),
    reporter: toUserShort(findUser(r.reporter_id)!),
    reason: r.reason,
    comment: r.comment,
    status: r.status,
    resolution_note: r.resolution_note,
    resolved_at: r.resolved_at,
    created_at: r.created_at,
  }
}
