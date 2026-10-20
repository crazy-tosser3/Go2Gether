import { http, HttpResponse, delay, type DefaultBodyType, type PathParams } from 'msw'
import type {
  CommentInput,
  DemoAccount,
  EventInput,
  EventStatus,
  Id,
  Me,
  OrganizationInput,
  OrganizationStatus,
  ParticipationStatus,
  ProfileInput,
  SafetyBanner,
  SafetyBannerDetails,
  SafetyBannerInput,
} from '@/api/types'
import { API_BASE } from '@/api/http'
import {
  db,
  newId,
  newToken,
  normalizeName,
  type EventRow,
  type ModerationRow,
  type OrganizationRow,
  type ParticipationRow,
  type ReportRow,
  type SafetyBannerRow,
  type UserRow,
} from './db'
import {
  MockError,
  confirmedCount,
  currentUser,
  fail,
  findEvent,
  findOrg,
  findUser,
  isActive,
  isOrganizer,
  isPast,
  myParticipation,
  ownedOrgs,
  paginate,
  requireAdmin,
  requireUser,
  sessionToken,
  toComment,
  toEventDetails,
  toEventSummary,
  toReport,
  toOrgShort,
  toOrganization,
  toParticipant,
  toParticipation,
  toSport,
  toUser,
  toUserShort,
  unavailable,
  validation,
} from './model'

type Ctx = { request: Request; params: PathParams }
type Resolver = (ctx: Ctx) => unknown | Promise<unknown>

const url = (path: string) => `*${API_BASE}${path}`

// В браузере сервис-воркер не видит заголовок Cookie — MSW отдаёт cookie отдельно.
// Подкладываем jwt в Authorization, чтобы обработчики одинаково работали в браузере и в тестах.
function withSession(request: Request, cookies: Record<string, string>) {
  if (!cookies.jwt || request.headers.has('Authorization')) return request
  const headers = new Headers(request.headers)
  headers.set('Authorization', `Bearer ${cookies.jwt}`)
  return new Request(request, { headers })
}

function route(resolver: Resolver) {
  return async ({ request, params, cookies }: Ctx & { cookies: Record<string, string> }) => {
    // Небольшая задержка, чтобы в интерфейсе были видны состояния загрузки.
    if (import.meta.env.MODE !== 'test') await delay(250)
    try {
      const result = await resolver({ request: withSession(request, cookies), params })
      if (result instanceof Response) return result
      return HttpResponse.json(result as DefaultBodyType)
    } catch (error) {
      if (error instanceof MockError) return error.toResponse()
      throw error
    }
  }
}

const created = (body: DefaultBodyType) => HttpResponse.json(body, { status: 201 })
const noContent = () => new HttpResponse(null, { status: 204 })

async function readJson<T>(request: Request): Promise<Partial<T>> {
  try {
    return ((await request.json()) ?? {}) as Partial<T>
  } catch {
    return {}
  }
}

const idParam = (params: PathParams, key: string): Id => {
  const id = Number(params[key])
  return Number.isInteger(id) ? id : fail(404, 'NOT_FOUND', 'Не найдено')
}
const nowIso = () => new Date().toISOString()
const search = (request: Request) => new URL(request.url).searchParams
const numberParam = (sp: URLSearchParams, key: string) => {
  const v = sp.get(key)
  return v === null || v === '' ? undefined : Number(v)
}
const pageOf = <T>(items: T[], sp: URLSearchParams) =>
  paginate(items, numberParam(sp, 'page') ?? 0, numberParam(sp, 'size') ?? 20)

// У бэка cookie HttpOnly. В браузере MSW ставит cookie через document.cookie, а HttpOnly так не поставить —
// поэтому в моках без него; для фронта разницы нет, он cookie не читает.
const SESSION_COOKIE = 'Path=/; Max-Age=86400; SameSite=Strict'
const CLEAR_COOKIE = 'jwt=; Path=/; Max-Age=0; SameSite=Strict'

/** Как у бэка: токен — только в cookie, в теле его нет. */
function login(user: UserRow, status = 200) {
  const token = newToken()
  db.sessions.set(token, user.id)
  return HttpResponse.json(
    { token_type: 'Bearer', expires_in: 86400, user: toUser(user) },
    { status, headers: { 'Set-Cookie': `jwt=${token}; ${SESSION_COOKIE}` } },
  )
}

function me(user: UserRow): Me {
  return { user: toUser(user), organizations: ownedOrgs(user.id).map(toOrgShort) }
}

const REPORT_REASONS = ['spam', 'fraud', 'offensive', 'dangerous', 'misleading', 'other']

/** Как у бэка: снятие или удаление мероприятия закрывает все его нерассмотренные жалобы как принятые. */
function acceptReports(eventId: Id, note: string | null) {
  for (const r of db.reports)
    if (r.event_id === eventId && r.status === 'pending')
      Object.assign(r, { status: 'accepted', resolution_note: note, resolved_at: nowIso() })
}

function log(
  admin: UserRow,
  action: ModerationRow['action'],
  target: ModerationRow['target_type'],
  id: Id,
  reason: string | null,
) {
  db.moderation.unshift({
    id: newId(),
    admin_id: admin.id,
    action,
    target_type: target,
    target_id: id,
    reason,
    created_at: nowIso(),
  })
}

function resolveSport(input: Partial<EventInput>, user: UserRow) {
  if (input.sport_id) {
    return (
      db.sports.find((s) => s.id === input.sport_id) ??
      validation({ sport_id: 'Такого направления нет' })
    )
  }
  const name = input.sport_name?.trim()
  if (!name) return validation({ sport_id: 'Выберите направление или впишите своё' })
  const normalized = normalizeName(name)
  const existing = db.sports.find((s) => s.normalized_name === normalized)
  if (existing) return existing
  const sport = {
    id: newId(),
    name,
    category: 'other' as const,
    icon: null,
    is_custom: true,
    normalized_name: normalized,
    created_by: user.id,
  }
  db.sports.push(sport)
  return sport
}

function validateEvent(input: Partial<EventInput>) {
  const fields: Record<string, string> = {}
  if (!input.title?.trim()) fields.title = 'не должно быть пустым'
  if (!input.address?.trim()) fields.address = 'не должно быть пустым'
  if (!input.starts_at || Number.isNaN(Date.parse(input.starts_at)))
    fields.starts_at = 'не должно равняться null'
  else if (new Date(input.starts_at) <= new Date())
    fields.starts_at = 'Время начала должно быть в будущем'
  if (input.join_mode !== 'open' && input.join_mode !== 'approval')
    fields.join_mode = 'Выберите режим записи'
  if (
    input.capacity !== undefined &&
    input.capacity !== null &&
    (!Number.isInteger(input.capacity) || input.capacity < 1)
  )
    fields.capacity = 'Не меньше одного места'
  if (Object.keys(fields).length) validation(fields)
}

function publishedEvent(id: Id) {
  const event = findEvent(id)
  if (event.status !== 'published') unavailable(event)
  return event
}

/** Как у бэка: снятое событие видят участники, организатор и админ (с причиной), остальным — 410. */
function visibleEvent(id: Id, viewer: UserRow | null) {
  const event = findEvent(id)
  const involved =
    viewer &&
    (viewer.role === 'admin' ||
      isOrganizer(event, viewer) ||
      db.participations.some((p) => p.event_id === event.id && p.user_id === viewer.id))
  if (event.status !== 'published' && !involved) unavailable(event)
  return event
}

function requireOrganizer(event: EventRow, user: UserRow) {
  if (!isOrganizer(event, user))
    fail(403, 'FORBIDDEN', 'Это может сделать только организатор мероприятия')
}

function findOrgOr404(id: Id): OrganizationRow {
  return findOrg(id) ?? fail(404, 'NOT_FOUND', 'Организация не найдена')
}

function participationOr404(id: Id): ParticipationRow {
  return db.participations.find((x) => x.id === id) ?? fail(404, 'NOT_FOUND', 'Заявка не найдена')
}

export const handlers = [
  // ── Auth
  http.get(
    url('/auth/demo-accounts'),
    route(() =>
      db.users
        .filter((u) => u.is_demo)
        .map((u): DemoAccount => ({
          id: u.id,
          name: u.name,
          email: u.email,
          role: u.role,
          description: u.demo_description ?? '',
        })),
    ),
  ),

  http.post(
    url('/auth/login'),
    route(async ({ request }) => {
      const body = await readJson<{ email: string; password: string; demo_account_id: number }>(
        request,
      )
      const user = body.demo_account_id
        ? db.users.find((u) => u.is_demo && u.id === Number(body.demo_account_id))
        : db.users.find(
            (u) => u.email === body.email?.trim().toLowerCase() && u.password === body.password,
          )
      if (!user) fail(401, 'INVALID_CREDENTIALS', 'Неверный email или пароль')
      return login(user!)
    }),
  ),

  http.post(
    url('/auth/register'),
    route(async ({ request }) => {
      const body = await readJson<{
        name: string
        email: string
        password: string
        consent?: boolean
      }>(request)
      const fields: Record<string, string> = {}
      const email = body.email?.trim().toLowerCase() ?? ''
      if (!body.name?.trim()) fields.name = 'не должно быть пустым'
      if (!/^\S+@\S+\.\S+$/.test(email))
        fields.email = 'должно иметь формат адреса электронной почты'
      if (!body.password || body.password.length < 6) fields.password = 'Не короче 6 символов'
      if (body.consent !== true)
        fields.consent = 'Необходимо согласие на обработку персональных данных'
      if (Object.keys(fields).length) validation(fields)
      if (db.users.some((u) => u.email === email))
        fail(409, 'EMAIL_TAKEN', 'Этот email уже зарегистрирован')
      const user: UserRow = {
        id: newId(),
        role: 'user',
        name: body.name!.trim(),
        email,
        password: body.password!,
        avatar_url: null,
        city: null,
        bio: null,
        level: null,
        sport_ids: [],
        birth_year: null,
        radius_km: null,
        preferred_age_from: null,
        preferred_age_to: null,
        preferred_times: [],
        sport_levels: {},
        is_demo: false,
        created_at: nowIso(),
        contacts: null,
        demo_description: null,
      }
      db.users.push(user)
      return login(user, 201)
    }),
  ),

  http.post(
    url('/auth/logout'),
    route(({ request }) => {
      db.sessions.delete(sessionToken(request))
      return new HttpResponse(null, { status: 204, headers: { 'Set-Cookie': CLEAR_COOKIE } })
    }),
  ),

  http.get(
    url('/auth/me'),
    route(({ request }) => me(requireUser(request))),
  ),

  // ── Users
  http.get(
    url('/users/me/events'),
    route(({ request }) => {
      const user = requireUser(request)
      const count = (eventId: Id, status: ParticipationStatus) =>
        db.participations.filter((p) => p.event_id === eventId && p.status === status).length
      return db.events
        .filter((e) => isOrganizer(e, user) && e.status !== 'deleted')
        .sort((a, b) => a.starts_at.localeCompare(b.starts_at))
        .map((e) => ({
          event: toEventSummary(e),
          pending_count: count(e.id, 'pending'),
          waitlisted_count: count(e.id, 'waitlist'),
        }))
    }),
  ),

  http.get(
    url('/users/me/participations'),
    route(({ request }) => {
      const user = requireUser(request)
      const status = search(request).get('status') as ParticipationStatus | null
      return db.participations
        .filter((p) => p.user_id === user.id && (!status || p.status === status))
        .sort((a, b) => b.created_at.localeCompare(a.created_at) || b.id - a.id)
        .map((p) => ({
          participation: toParticipation(p),
          event: toEventSummary(findEvent(p.event_id)),
        }))
    }),
  ),

  http.patch(
    url('/users/me'),
    route(async ({ request }) => {
      const user = requireUser(request)
      const body = await readJson<ProfileInput>(request)
      // Как у бэка: null — «не менять», уровни — только для выбранных видов, радиус от 1 км.
      const patch = Object.fromEntries(
        Object.entries(body).filter(([, v]) => v !== null && v !== undefined),
      ) as Partial<UserRow>
      const fields: Record<string, string> = {}
      if (patch.name !== undefined && !patch.name.trim()) fields.name = 'не должно быть пустым'
      const sportIds = patch.sport_ids ?? user.sport_ids
      const levels = patch.sport_levels ?? user.sport_levels
      const extra = Object.keys(levels).filter((id) => !sportIds.includes(Number(id)))
      if (patch.sport_levels && extra.length)
        fields.sport_levels = `уровень можно указать только для видов из sport_ids: ${extra.join(', ')}`
      if (patch.radius_km != null && patch.radius_km < 1)
        fields.radius_km = 'должно быть не меньше 1'
      const from = patch.preferred_age_from ?? user.preferred_age_from
      const to = patch.preferred_age_to ?? user.preferred_age_to
      if (from != null && to != null && from > to)
        fields.preferred_age_to = 'должно быть не меньше preferred_age_from'
      if (Object.keys(fields).length) validation(fields)
      Object.assign(user, patch)
      user.sport_levels = Object.fromEntries(
        Object.entries(user.sport_levels).filter(([id]) => user.sport_ids.includes(Number(id))),
      )
      return toUser(user)
    }),
  ),

  http.post(
    url('/users/me/avatar'),
    route(async ({ request }) => {
      const user = requireUser(request)
      const entry = (await request.formData()).get('file')
      const file = entry instanceof File ? entry : fail(400, 'BAD_REQUEST', 'Некорректный запрос')
      if (!['image/jpeg', 'image/png', 'image/webp'].includes(file.type))
        fail(415, 'UNSUPPORTED_MEDIA_TYPE', 'Поддерживаются только JPG, PNG и WEBP')
      if (file.size > 5 * 1024 * 1024) fail(413, 'PAYLOAD_TOO_LARGE', 'Файл больше 5 МБ')
      // Файлового хранилища у моков нет — держим картинку в памяти как data URL.
      const bytes = new Uint8Array(await file.arrayBuffer())
      let binary = ''
      for (const b of bytes) binary += String.fromCharCode(b)
      user.avatar_url = `data:${file.type};base64,${btoa(binary)}`
      return toUser(user)
    }),
  ),

  http.delete(
    url('/users/me/avatar'),
    route(({ request }) => {
      const user = requireUser(request)
      user.avatar_url = null
      return toUser(user)
    }),
  ),

  // Как у бэка: нужен пароль, демо-аккаунты удалить нельзя (152-ФЗ, тема back/delete-account).
  http.delete(
    url('/users/me'),
    route(async ({ request }) => {
      const user = requireUser(request)
      const { password } = await readJson<{ password: string }>(request)
      if (!password) fail(400, 'BAD_REQUEST', 'Некорректный запрос')
      if (password !== user.password) fail(403, 'FORBIDDEN', 'Неверный пароль')
      if (user.is_demo) fail(403, 'FORBIDDEN', 'Демо-аккаунт удалить нельзя')
      if (ownedOrgs(user.id).length)
        fail(409, 'HAS_ORGANIZATIONS', 'Сначала передайте или удалите свою организацию')
      for (const p of db.participations)
        if (
          p.user_id === user.id &&
          (p.status === 'confirmed' || p.status === 'pending' || p.status === 'waitlist')
        )
          Object.assign(p, { status: 'cancelled', updated_at: nowIso() })
      for (const e of db.events)
        if (
          e.organizer_type === 'user' &&
          e.organizer_user_id === user.id &&
          e.status === 'published'
        )
          Object.assign(e, {
            status: 'cancelled',
            moderation_reason: 'Организатор удалил аккаунт',
            updated_at: nowIso(),
          })
      Object.assign(user, {
        name: 'Удалённый пользователь',
        email: `deleted-${user.id}@deleted.local`,
        password: crypto.randomUUID(),
        city: null,
        bio: null,
        avatar_url: null,
        contacts: null,
        sport_ids: [],
        level: null,
        is_demo: false,
        demo_description: null,
        deleted: true,
      })
      for (const [token, id] of db.sessions) if (id === user.id) db.sessions.delete(token)
      return new HttpResponse(null, { status: 204, headers: { 'Set-Cookie': CLEAR_COOKIE } })
    }),
  ),

  http.get(
    url('/users/:id'),
    route(({ params }) => {
      const found = findUser(idParam(params, 'id'))
      const user =
        found && !found.deleted ? found : fail(404, 'NOT_FOUND', 'Пользователь не найден')
      return {
        id: user.id,
        name: user.name,
        avatar_url: user.avatar_url,
        city: user.city,
        bio: user.bio,
        level: user.level,
        sports: db.sports.filter((s) => user.sport_ids.includes(s.id)).map(toSport),
        created_events: db.events
          .filter(
            (e) =>
              e.organizer_type === 'user' &&
              e.organizer_user_id === user.id &&
              e.status === 'published' &&
              !isPast(e),
          )
          .map(toEventSummary),
      }
    }),
  ),

  // ── Sports
  http.get(
    url('/sports'),
    route(({ request }) => {
      const q = normalizeName(search(request).get('q') ?? '')
      return db.sports
        .filter((s) => !q || s.normalized_name.includes(q))
        .sort(
          (a, b) => Number(a.is_custom) - Number(b.is_custom) || a.name.localeCompare(b.name, 'ru'),
        )
        .map(toSport)
    }),
  ),

  http.post(
    url('/sports'),
    route(async ({ request }) => {
      const user = requireUser(request)
      const body = await readJson<{ name: string }>(request)
      return created(toSport(resolveSport({ sport_name: body.name }, user)))
    }),
  ),

  http.get(
    url('/sports/:id'),
    route(({ params }) => {
      const sport =
        db.sports.find((s) => s.id === idParam(params, 'id')) ??
        fail(404, 'NOT_FOUND', 'Направление не найдено')
      const active = db.events.filter(
        (e) => e.sport_id === sport.id && e.status === 'published' && !isPast(e),
      ).length
      return { ...toSport(sport), active_events_count: active }
    }),
  ),

  // ── Events
  http.get(
    url('/events'),
    route(({ request }) => {
      const sp = search(request)
      const q = normalizeName(sp.get('q') ?? '')
      const sport = numberParam(sp, 'sport')
      const city = normalizeName(sp.get('city') ?? '')
      const from = sp.get('date_from')
      const to = sp.get('date_to')
      const level = sp.get('level')
      const mode = sp.get('join_mode')
      const organizer = numberParam(sp, 'organizer')
      const organization = numberParam(sp, 'organization')
      const items = db.events
        .filter((e) => e.status === 'published' && !isPast(e))
        .filter((e) => !q || normalizeName(`${e.title} ${e.description ?? ''}`).includes(q))
        .filter((e) => !sport || e.sport_id === sport)
        .filter((e) => !city || normalizeName(`${e.city ?? ''} ${e.address}`).includes(city))
        .filter((e) => !from || e.starts_at.slice(0, 10) >= from)
        .filter((e) => !to || e.starts_at.slice(0, 10) <= to)
        .filter((e) => !level || e.level === level)
        .filter((e) => !mode || e.join_mode === mode)
        .filter((e) => !organizer || e.organizer_user_id === organizer)
        .filter((e) => !organization || e.organizer_org_id === organization)
        .sort((a, b) => a.starts_at.localeCompare(b.starts_at))
        .map(toEventSummary)
      return pageOf(items, sp)
    }),
  ),

  http.post(
    url('/events'),
    route(async ({ request }) => {
      const user = requireUser(request)
      if (user.role === 'admin') fail(403, 'FORBIDDEN', 'Администратор не создаёт мероприятия')
      const input = await readJson<EventInput>(request)
      if (input.organization_id) {
        const org = findOrg(input.organization_id)
        if (!org || org.owner_id !== user.id || org.status !== 'approved')
          fail(403, 'ORGANIZATION_NOT_APPROVED', 'Организация ещё не допущена к публикации')
      }
      validateEvent(input)
      const sport = resolveSport(input, user)
      const ts = nowIso()
      const event: EventRow = {
        id: newId(),
        title: input.title!.trim(),
        description: input.description?.trim() || null,
        sport_id: sport.id,
        organizer_type: input.organization_id ? 'organization' : 'user',
        organizer_user_id: user.id,
        organizer_org_id: input.organization_id ?? null,
        starts_at: input.starts_at!,
        ends_at: input.ends_at ?? null,
        city: input.city?.trim() || null,
        address: input.address!.trim(),
        lat: input.lat ?? null,
        lng: input.lng ?? null,
        location_note: input.location_note?.trim() || null,
        level: input.level ?? null,
        requirements: input.requirements?.trim() || null,
        equipment: input.equipment?.trim() || null,
        join_mode: input.join_mode!,
        capacity: input.capacity ?? null,
        status: 'published',
        moderation_reason: null,
        created_at: ts,
        updated_at: ts,
      }
      db.events.push(event)
      return created(toEventDetails(event, user))
    }),
  ),

  http.get(
    url('/events/:id'),
    route(({ request, params }) => {
      const viewer = currentUser(request)
      return toEventDetails(visibleEvent(idParam(params, 'id'), viewer), viewer)
    }),
  ),

  http.patch(
    url('/events/:id'),
    route(async ({ request, params }) => {
      const user = requireUser(request)
      const event = publishedEvent(idParam(params, 'id'))
      requireOrganizer(event, user)
      const input = await readJson<EventInput>(request)
      validateEvent({
        title: input.title ?? event.title,
        address: input.address ?? event.address,
        starts_at: input.starts_at ?? event.starts_at,
        join_mode: input.join_mode ?? event.join_mode,
        capacity: input.capacity ?? event.capacity ?? undefined,
      })
      const sport = input.sport_id || input.sport_name ? resolveSport(input, user) : null
      const { sport_name: _name, organization_id: _org, ...rest } = input
      void _name
      void _org
      Object.assign(event, rest, { sport_id: sport?.id ?? event.sport_id, updated_at: nowIso() })
      return toEventDetails(event, user)
    }),
  ),

  http.post(
    url('/events/:id/cancel'),
    route(async ({ request, params }) => {
      const user = requireUser(request)
      const event = publishedEvent(idParam(params, 'id'))
      requireOrganizer(event, user)
      const { reason } = await readJson<{ reason: string }>(request)
      if (!reason?.trim()) validation({ reason: 'не должно быть пустым' })
      Object.assign(event, {
        status: 'cancelled',
        moderation_reason: reason!.trim(),
        updated_at: nowIso(),
      })
      return toEventDetails(event, user)
    }),
  ),

  http.post(
    url('/events/:id/join'),
    route(async ({ request, params }) => {
      const user = requireUser(request)
      if (user.role === 'admin')
        fail(403, 'FORBIDDEN', 'Администратор не записывается на мероприятия')
      const event = publishedEvent(idParam(params, 'id'))
      if (isPast(event)) fail(409, 'EVENT_PAST', 'Мероприятие уже прошло')
      if (isOrganizer(event, user)) fail(409, 'OWN_EVENT', 'Нельзя записаться на своё мероприятие')
      const current = myParticipation(event.id, user.id)
      if (current && isActive(current))
        fail(409, 'ALREADY_JOINED', 'У вас уже есть активная запись', {
          participation_id: String(current.id),
          status: current.status,
        })
      if (event.capacity && confirmedCount(event.id) >= event.capacity)
        fail(409, 'NO_PLACES', 'Мест больше нет')
      const { message } = await readJson<{ message: string }>(request)
      const ts = nowIso()
      const open = event.join_mode === 'open'
      const participation: ParticipationRow = {
        id: newId(),
        event_id: event.id,
        user_id: user.id,
        status: open ? 'confirmed' : 'pending',
        message: message?.trim() || null,
        decision_reason: null,
        decided_at: open ? ts : null,
        created_at: ts,
      }
      db.participations.push(participation)
      return created({
        message: open ? 'Вы записаны' : 'Заявка на рассмотрении',
        participation: toParticipation(participation),
        event: toEventDetails(event, user),
      })
    }),
  ),

  http.delete(
    url('/events/:id/join'),
    route(({ request, params }) => {
      const user = requireUser(request)
      const event = findEvent(idParam(params, 'id'))
      const current = myParticipation(event.id, user.id)
      if (!current || !isActive(current)) fail(404, 'NOT_FOUND', 'Активной записи нет')
      Object.assign(current!, { status: 'cancelled', decided_at: nowIso() })
      return toParticipation(current!)
    }),
  ),

  http.get(
    url('/events/:id/my-participation'),
    route(({ request, params }) => {
      const user = requireUser(request)
      const p = myParticipation(idParam(params, 'id'), user.id)
      return p ? toParticipation(p) : null
    }),
  ),

  http.get(
    url('/events/:id/participants'),
    route(({ request, params }) => {
      const event = findEvent(idParam(params, 'id'))
      const viewer = currentUser(request)
      const withEmail = isOrganizer(event, viewer) || viewer?.role === 'admin'
      const participants = db.participations
        .filter((p) => p.event_id === event.id && p.status === 'confirmed')
        .map((p) => toParticipant(p, withEmail))
      return { count: participants.length, capacity: event.capacity, participants }
    }),
  ),

  http.get(
    url('/events/:id/requests'),
    route(({ request, params }) => {
      const user = requireUser(request)
      const event = findEvent(idParam(params, 'id'))
      requireOrganizer(event, user)
      const status = search(request).get('status') ?? 'pending'
      return db.participations
        .filter((p) => p.event_id === event.id && p.status === status)
        .map((p) => toParticipant(p, true))
    }),
  ),

  http.post(
    url('/participations/:pid/:action'),
    route(async ({ request, params }) => {
      const user = requireUser(request)
      const action = String(params.action)
      if (action !== 'approve' && action !== 'reject') return fail(404, 'NOT_FOUND', 'Не найдено')
      const p = participationOr404(idParam(params, 'pid'))
      const event = findEvent(p.event_id)
      requireOrganizer(event, user)
      if (p.status !== 'pending') fail(409, 'ALREADY_DECIDED', 'Заявка уже рассмотрена')
      if (event.status !== 'published') unavailable(event)
      if (isPast(event)) fail(409, 'EVENT_PAST', 'Мероприятие уже прошло')
      if (action === 'approve' && event.capacity && confirmedCount(event.id) >= event.capacity)
        fail(409, 'NO_PLACES', 'Мест больше нет')
      const { reason } =
        action === 'reject' ? await readJson<{ reason: string }>(request) : { reason: undefined }
      Object.assign(p, {
        status: action === 'approve' ? 'confirmed' : 'rejected',
        decision_reason: reason?.trim() || null,
        decided_at: nowIso(),
      })
      return toParticipation(p)
    }),
  ),

  // ── Comments
  http.get(
    url('/events/:id/comments'),
    route(({ request, params }) => {
      const event = visibleEvent(idParam(params, 'id'), currentUser(request))
      if (event.status === 'deleted') unavailable(event)
      const items = db.comments
        .filter((c) => c.event_id === event.id)
        .sort((a, b) => a.created_at.localeCompare(b.created_at) || a.id - b.id)
        .map((c) => toComment(c))
      return pageOf(items, search(request))
    }),
  ),

  http.post(
    url('/events/:id/comments'),
    route(async ({ request, params }) => {
      const user = requireUser(request)
      const event = publishedEvent(idParam(params, 'id'))
      const input = await readJson<CommentInput>(request)
      const text = input.text?.trim()
      if (!text) validation({ text: 'не должно быть пустым' })
      if (
        input.parent_id &&
        !db.comments.some((c) => c.id === input.parent_id && c.event_id === event.id)
      )
        validation({ parent_id: 'Сообщение, на которое вы отвечаете, не найдено' })
      if (input.as_organization_id) {
        const org = findOrg(input.as_organization_id)
        if (!org || org.owner_id !== user.id || org.status !== 'approved')
          fail(403, 'FORBIDDEN', 'Писать от имени этой организации нельзя')
      }
      const ts = nowIso()
      const comment = {
        id: newId(),
        event_id: event.id,
        author_id: user.id,
        as_organization_id: input.as_organization_id ?? null,
        parent_id: input.parent_id ?? null,
        text: text!,
        is_hidden: false,
        hidden_reason: null,
        created_at: ts,
        updated_at: ts,
      }
      db.comments.push(comment)
      return created(toComment(comment))
    }),
  ),

  http.patch(
    url('/comments/:cid'),
    route(async ({ request, params }) => {
      const user = requireUser(request)
      const comment =
        db.comments.find((c) => c.id === idParam(params, 'cid')) ??
        fail(404, 'NOT_FOUND', 'Сообщение не найдено')
      if (comment.author_id !== user.id)
        fail(403, 'FORBIDDEN', 'Можно править только свои сообщения')
      if (comment.is_hidden) fail(409, 'COMMENT_HIDDEN', 'Сообщение скрыто модератором')
      const { text } = await readJson<{ text: string }>(request)
      if (!text?.trim()) validation({ text: 'не должно быть пустым' })
      Object.assign(comment, {
        text: text!.trim(),
        updated_at: new Date(Date.now() + 1).toISOString(),
      })
      return toComment(comment)
    }),
  ),

  http.delete(
    url('/comments/:cid'),
    route(({ request, params }) => {
      const user = requireUser(request)
      const comment =
        db.comments.find((c) => c.id === idParam(params, 'cid')) ??
        fail(404, 'NOT_FOUND', 'Сообщение не найдено')
      if (comment.author_id !== user.id)
        fail(403, 'FORBIDDEN', 'Можно удалить только свои сообщения')
      db.comments = db.comments.filter((c) => c.id !== comment.id && c.parent_id !== comment.id)
      return noContent()
    }),
  ),

  // ── Organizations
  http.post(
    url('/organizations'),
    route(async ({ request }) => {
      const user = requireUser(request)
      const input = await readJson<OrganizationInput>(request)
      const fields: Record<string, string> = {}
      if (!input.name?.trim()) fields.name = 'не должно быть пустым'
      if (!input.type) fields.type = 'не должно равняться null'
      if (!input.contacts?.trim()) fields.contacts = 'не должно быть пустым'
      if (Object.keys(fields).length) validation(fields)
      const ts = nowIso()
      const org: OrganizationRow = {
        id: newId(),
        owner_id: user.id,
        name: input.name!.trim(),
        description: input.description?.trim() || null,
        type: input.type!,
        sports_description: input.sports_description?.trim() || null,
        city: input.city?.trim() || null,
        website_url: input.website_url?.trim() || null,
        avatar_url: input.avatar_url?.trim() || null,
        contacts: input.contacts!.trim(),
        status: 'pending',
        reject_reason: null,
        decided_at: null,
        created_at: ts,
        updated_at: ts,
      }
      db.organizations.push(org)
      return created(toOrganization(org))
    }),
  ),

  http.get(
    url('/organizations/my'),
    route(({ request }) => ownedOrgs(requireUser(request).id).map(toOrganization)),
  ),

  http.get(
    url('/organizations'),
    route(({ request }) => {
      const sp = search(request)
      const q = normalizeName(sp.get('q') ?? '')
      const sport = normalizeName(sp.get('sport') ?? '')
      const city = normalizeName(sp.get('city') ?? '')
      const items = db.organizations
        .filter((o) => o.status === 'approved')
        .filter((o) => !q || normalizeName(`${o.name} ${o.description ?? ''}`).includes(q))
        .filter((o) => !sport || normalizeName(o.sports_description ?? '').includes(sport))
        // Как у бэка: город организации — точное совпадение, направление — по части текста.
        .filter((o) => !city || normalizeName(o.city ?? '') === city)
        .map(toOrganization)
      return pageOf(items, sp)
    }),
  ),

  http.get(
    url('/organizations/:id'),
    route(({ request, params }) => {
      const org = findOrg(idParam(params, 'id'))
      const viewer = currentUser(request)
      if (
        !org ||
        (org.status !== 'approved' && org.owner_id !== viewer?.id && viewer?.role !== 'admin')
      )
        fail(404, 'NOT_FOUND', 'Организация не найдена')
      return toOrganization(org!)
    }),
  ),

  http.patch(
    url('/organizations/:id'),
    route(async ({ request, params }) => {
      const user = requireUser(request)
      const org = findOrgOr404(idParam(params, 'id'))
      if (org.owner_id !== user.id) fail(403, 'FORBIDDEN', 'Править может только владелец')
      const input = await readJson<OrganizationInput>(request)
      Object.assign(org, input, { updated_at: nowIso() })
      return toOrganization(org)
    }),
  ),

  http.post(
    url('/organizations/:id/resubmit'),
    route(({ request, params }) => {
      const user = requireUser(request)
      const org = findOrgOr404(idParam(params, 'id'))
      if (org.owner_id !== user.id) fail(403, 'FORBIDDEN', 'Подать заявку может только владелец')
      if (org.status !== 'rejected')
        fail(409, 'INVALID_STATUS', 'Повторно подать можно только отклонённую заявку')
      Object.assign(org, {
        status: 'pending',
        reject_reason: null,
        decided_at: null,
        updated_at: nowIso(),
      })
      return toOrganization(org)
    }),
  ),

  http.get(
    url('/organizations/:id/events'),
    route(({ params }) => {
      const org = findOrg(idParam(params, 'id'))
      if (!org || org.status !== 'approved') fail(404, 'NOT_FOUND', 'Организация не найдена')
      return db.events
        .filter((e) => e.organizer_org_id === org!.id && e.status === 'published' && !isPast(e))
        .sort((a, b) => a.starts_at.localeCompare(b.starts_at))
        .map(toEventSummary)
    }),
  ),

  // ── Admin
  http.post(
    url('/events/:id/reports'),
    route(async ({ request, params }) => {
      const user = requireUser(request)
      const event =
        findEvent(idParam(params, 'id')) ?? fail(404, 'NOT_FOUND', 'Мероприятие не найдено')
      if (event.status !== 'published') unavailable(event)
      if (isOrganizer(event, user))
        fail(409, 'OWN_EVENT', 'Нельзя пожаловаться на своё мероприятие')
      const body = await readJson<{ reason?: string; comment?: string }>(request)
      if (body.reason && !REPORT_REASONS.includes(body.reason))
        fail(400, 'BAD_REQUEST', 'Некорректный запрос')
      const fields: Record<string, string> = {}
      if (!body.reason) fields.reason = 'не должно быть пустым'
      const comment = body.comment?.trim() || null
      if (body.reason === 'other' && !comment) fields.comment = 'Опишите, что не так'
      if (comment && comment.length > 1000) fields.comment = 'Не длиннее 1000 символов'
      if (Object.keys(fields).length) validation(fields)
      if (
        db.reports.some(
          (r) => r.event_id === event.id && r.reporter_id === user.id && r.status === 'pending',
        )
      )
        fail(
          409,
          'ALREADY_REPORTED',
          'Вы уже пожаловались на это мероприятие, жалоба на рассмотрении',
        )
      const report: ReportRow = {
        id: newId(),
        event_id: event.id,
        reporter_id: user.id,
        reason: body.reason as ReportRow['reason'],
        comment,
        status: 'pending',
        resolution_note: null,
        resolved_at: null,
        created_at: nowIso(),
      }
      db.reports.push(report)
      return HttpResponse.json(toReport(report), { status: 201 })
    }),
  ),

  http.get(
    url('/users/me/reports'),
    route(({ request }) => {
      const user = requireUser(request)
      return db.reports
        .filter((r) => r.reporter_id === user.id)
        .sort((a, b) => b.created_at.localeCompare(a.created_at))
        .map(toReport)
    }),
  ),

  http.get(
    url('/admin/reports'),
    route(({ request }) => {
      requireAdmin(request)
      const sp = search(request)
      const status = sp.get('status')
      const eventId = numberParam(sp, 'event_id')
      const items = db.reports
        .filter((r) => !status || r.status === status)
        .filter((r) => !eventId || r.event_id === eventId)
        .sort((a, b) => a.created_at.localeCompare(b.created_at) || a.id - b.id)
        .map(toReport)
      return pageOf(items, sp)
    }),
  ),

  http.post(
    url('/admin/reports/:id/accept'),
    route(async ({ request, params }) => {
      const admin = requireAdmin(request)
      const report =
        db.reports.find((r) => r.id === idParam(params, 'id')) ??
        fail(404, 'NOT_FOUND', 'Жалоба не найдена')
      const { reason } = await readJson<{ reason?: string }>(request)
      if (!reason?.trim()) validation({ reason: 'не должно быть пустым' })
      if (report.status !== 'pending') fail(409, 'ALREADY_REVIEWED', 'Жалоба уже рассмотрена')
      const event = findEvent(report.event_id)!
      if (event.status === 'published') {
        Object.assign(event, {
          status: 'unpublished',
          moderation_reason: reason!.trim(),
          updated_at: nowIso(),
        })
        log(admin, 'event_unpublished', 'event', event.id, reason!.trim())
      }
      acceptReports(event.id, reason!.trim())
      return toReport(report)
    }),
  ),

  http.post(
    url('/admin/reports/:id/reject'),
    route(async ({ request, params }) => {
      const admin = requireAdmin(request)
      const report =
        db.reports.find((r) => r.id === idParam(params, 'id')) ??
        fail(404, 'NOT_FOUND', 'Жалоба не найдена')
      const { reason } = await readJson<{ reason?: string }>(request)
      if (report.status !== 'pending') fail(409, 'ALREADY_REVIEWED', 'Жалоба уже рассмотрена')
      const note = reason?.trim() || null
      Object.assign(report, { status: 'rejected', resolution_note: note, resolved_at: nowIso() })
      log(admin, 'report_rejected', 'event', report.event_id, note)
      return toReport(report)
    }),
  ),

  http.get(
    url('/admin/dashboard'),
    route(({ request }) => {
      requireAdmin(request)
      const orgs = (s: OrganizationStatus) => db.organizations.filter((o) => o.status === s).length
      const events = (s: EventStatus) => db.events.filter((e) => e.status === s).length
      return {
        pending_organizations: orgs('pending'),
        approved_organizations: orgs('approved'),
        published_events: events('published'),
        unpublished_events: events('unpublished'),
        deleted_events: events('deleted'),
        cancelled_events: events('cancelled'),
        hidden_comments: db.comments.filter((c) => c.is_hidden).length,
        users: db.users.length,
        pending_reports: db.reports.filter((r) => r.status === 'pending').length,
      }
    }),
  ),

  http.get(
    url('/admin/organizations'),
    route(({ request }) => {
      requireAdmin(request)
      const sp = search(request)
      const status = sp.get('status')
      const q = normalizeName(sp.get('q') ?? '')
      const items = db.organizations
        .filter((o) => !status || o.status === status)
        .filter((o) => !q || normalizeName(o.name).includes(q))
        .map(toOrganization)
      return pageOf(items, sp)
    }),
  ),

  http.get(
    url('/admin/organizations/:id'),
    route(({ request, params }) => {
      requireAdmin(request)
      const org = findOrgOr404(idParam(params, 'id'))
      return {
        organization: toOrganization(org),
        owner: toUser(findUser(org.owner_id)!),
        events: db.events.filter((e) => e.organizer_org_id === org.id).map(toEventSummary),
      }
    }),
  ),

  http.post(
    url('/admin/organizations/:id/:action'),
    route(async ({ request, params }) => {
      const admin = requireAdmin(request)
      const action = String(params.action)
      if (action !== 'approve' && action !== 'reject') return fail(404, 'NOT_FOUND', 'Не найдено')
      const org = findOrgOr404(idParam(params, 'id'))
      if (org.status !== 'pending') fail(409, 'ALREADY_DECIDED', 'Заявка уже рассмотрена')
      if (action === 'reject') {
        const { reason } = await readJson<{ reason: string }>(request)
        if (!reason?.trim()) validation({ reason: 'не должно быть пустым' })
        Object.assign(org, { status: 'rejected', reject_reason: reason!.trim() })
        log(admin, 'org_rejected', 'organization', org.id, reason!.trim())
      } else {
        Object.assign(org, { status: 'approved', reject_reason: null })
        log(admin, 'org_approved', 'organization', org.id, null)
      }
      Object.assign(org, { decided_at: nowIso(), updated_at: nowIso() })
      return toOrganization(org)
    }),
  ),

  http.get(
    url('/admin/events'),
    route(({ request }) => {
      requireAdmin(request)
      const sp = search(request)
      const status = sp.get('status')
      const q = normalizeName(sp.get('q') ?? '')
      const organizer = numberParam(sp, 'organizer')
      const organization = numberParam(sp, 'organization')
      const items = db.events
        .filter((e) => !status || e.status === status)
        .filter((e) => !q || normalizeName(e.title).includes(q))
        .filter((e) => !organizer || e.organizer_user_id === organizer)
        .filter((e) => !organization || e.organizer_org_id === organization)
        .sort((a, b) => b.starts_at.localeCompare(a.starts_at))
        .map(toEventSummary)
      return pageOf(items, sp)
    }),
  ),

  http.get(
    url('/admin/events/:id'),
    route(({ request, params }) => {
      const admin = requireAdmin(request)
      const event = findEvent(idParam(params, 'id'))
      const entry = db.moderation.find((m) => m.target_type === 'event' && m.target_id === event.id)
      const by = entry ? findUser(entry.admin_id) : undefined
      return {
        event: toEventDetails(event, admin),
        moderated_by: by ? toUserShort(by) : null,
        moderated_at: entry?.created_at ?? null,
        participations: db.participations
          .filter((p) => p.event_id === event.id)
          .map((p) => toParticipant(p, true)),
        comments: db.comments
          .filter((c) => c.event_id === event.id)
          .map((c) => toComment(c, { reveal: true })),
      }
    }),
  ),

  http.post(
    url('/admin/events/:id/unpublish'),
    route(async ({ request, params }) => {
      const admin = requireAdmin(request)
      const event = findEvent(idParam(params, 'id'))
      const { reason } = await readJson<{ reason: string }>(request)
      if (!reason?.trim()) validation({ reason: 'не должно быть пустым' })
      if (event.status !== 'published')
        fail(409, 'INVALID_STATUS', 'Мероприятие уже не опубликовано')
      Object.assign(event, {
        status: 'unpublished',
        moderation_reason: reason!.trim(),
        updated_at: nowIso(),
      })
      log(admin, 'event_unpublished', 'event', event.id, reason!.trim())
      acceptReports(event.id, reason!.trim())
      return toEventDetails(event, admin)
    }),
  ),

  http.delete(
    url('/admin/events/:id'),
    route(async ({ request, params }) => {
      const admin = requireAdmin(request)
      const event = findEvent(idParam(params, 'id'))
      const { reason } = await readJson<{ reason: string }>(request)
      if (!reason?.trim()) validation({ reason: 'не должно быть пустым' })
      Object.assign(event, {
        status: 'deleted',
        moderation_reason: reason!.trim(),
        updated_at: nowIso(),
      })
      log(admin, 'event_deleted', 'event', event.id, reason!.trim())
      acceptReports(event.id, reason!.trim())
      return toEventDetails(event, admin)
    }),
  ),

  http.post(
    url('/admin/events/:id/restore'),
    route(({ request, params }) => {
      const admin = requireAdmin(request)
      const event = findEvent(idParam(params, 'id'))
      if (event.status !== 'unpublished')
        fail(409, 'INVALID_STATUS', 'Восстановить можно только снятое с публикации')
      Object.assign(event, { status: 'published', moderation_reason: null, updated_at: nowIso() })
      log(admin, 'event_restored', 'event', event.id, null)
      return toEventDetails(event, admin)
    }),
  ),

  http.post(
    url('/admin/comments/:cid/hide'),
    route(async ({ request, params }) => {
      const admin = requireAdmin(request)
      const comment =
        db.comments.find((c) => c.id === idParam(params, 'cid')) ??
        fail(404, 'NOT_FOUND', 'Сообщение не найдено')
      const { reason } = await readJson<{ reason: string }>(request)
      if (!reason?.trim()) validation({ reason: 'не должно быть пустым' })
      Object.assign(comment, { is_hidden: true, hidden_reason: reason!.trim() })
      log(admin, 'comment_hidden', 'comment', comment.id, reason!.trim())
      return toComment(comment, { reveal: true })
    }),
  ),

  http.get(
    url('/admin/audit-log'),
    route(({ request }) => {
      requireAdmin(request)
      const sp = search(request)
      const target = sp.get('target_type')
      const items = db.moderation
        .filter((m) => !target || m.target_type === target)
        .map((m) => ({
          id: m.id,
          admin: toUserShort(findUser(m.admin_id)!),
          action: m.action,
          target_type: m.target_type,
          target_id: m.target_id,
          reason: m.reason,
          created_at: m.created_at,
        }))
      return pageOf(items, sp)
    }),
  ),

  // Упрощённая копия RecommendationEngine бэка: те же коды причин, грубые веса.
  http.get(
    url('/recommendations/events'),
    route(({ request }) => {
      const user = currentUser(request)
      const limit = Math.min(Math.max(numberParam(search(request), 'limit') ?? 10, 1), 50)
      const now = Date.now()
      const horizon = now + 60 * 24 * 60 * 60 * 1000
      const joined = new Set(
        db.participations.filter((p) => p.user_id === user?.id).map((p) => p.event_id),
      )
      const mine = new Set(user ? ownedOrgs(user.id).map((o) => o.id) : [])
      return db.events
        .filter((e) => e.status === 'published' && !isPast(e))
        .filter((e) => new Date(e.starts_at).getTime() <= horizon)
        .filter((e) => !user || (!isOrganizer(e, user) && !mine.has(e.organizer_org_id ?? -1)))
        .filter((e) => !joined.has(e.id))
        .map((e) => {
          const reasons: string[] = []
          if (user?.sport_ids.includes(e.sport_id)) reasons.push('interest')
          if (user?.city && e.city === user.city) reasons.push('same_city')
          if (user?.level && e.level === user.level) reasons.push('level_match')
          if (confirmedCount(e.id) >= 10) reasons.push('popular')
          if (new Date(e.starts_at).getTime() - now < 3 * 24 * 60 * 60 * 1000) reasons.push('soon')
          return { e, reasons, score: reasons.length }
        })
        .sort((a, b) => b.score - a.score || a.e.starts_at.localeCompare(b.e.starts_at))
        .slice(0, limit)
        .map(({ e, reasons, score }) => ({ event: toEventSummary(e), score, reasons }))
    }),
  ),

  http.get(
    url('/events/:id/similar'),
    route(({ request, params }) => {
      const base = findEvent(idParam(params, 'id'))
      if (base.status !== 'published') unavailable(base)
      const limit = Math.min(Math.max(numberParam(search(request), 'limit') ?? 10, 1), 50)
      return db.events
        .filter((e) => e.id !== base.id && e.status === 'published' && !isPast(e))
        .map((e) => {
          const reasons: string[] = []
          if (e.sport_id === base.sport_id) reasons.push('same_sport')
          if (e.city && e.city === base.city) reasons.push('same_city')
          if (e.level && e.level === base.level) reasons.push('level_match')
          const sameOrganizer =
            e.organizer_type === base.organizer_type &&
            (e.organizer_type === 'organization'
              ? e.organizer_org_id === base.organizer_org_id
              : e.organizer_user_id === base.organizer_user_id)
          if (sameOrganizer) reasons.push('same_organizer')
          const score = (reasons.includes('same_sport') ? 4 : 0) + reasons.length
          return { e, reasons, score }
        })
        .filter((x) => x.score > 0)
        .sort((a, b) => b.score - a.score || a.e.starts_at.localeCompare(b.e.starts_at))
        .slice(0, limit)
        .map(({ e, reasons, score }) => ({ event: toEventSummary(e), score, reasons }))
    }),
  ),

  // Памятки по безопасности: публично — только опубликованные, с sport — этого вида и общие.
  http.get(
    url('/safety/banners'),
    route(({ request }) => {
      const sp = search(request)
      const sport = numberParam(sp, 'sport')
      const category = sp.get('category')
      return db.safetyBanners
        .filter((b) => b.published)
        .filter((b) => !category || b.category === category)
        .filter((b) => sport === undefined || b.sport_id === null || b.sport_id === sport)
        .sort((a, b) => a.sort_order - b.sort_order)
        .map(toSafetyCard)
    }),
  ),
  http.get(
    url('/safety/banners/:id'),
    route(({ params }) => {
      const banner = db.safetyBanners.find((b) => b.id === idParam(params, 'id') && b.published)
      return toSafetyDetails(banner ?? fail(404, 'NOT_FOUND', 'Не найдено'))
    }),
  ),
  http.get(
    url('/admin/safety/banners'),
    route(({ request }) => {
      requireAdmin(request)
      return [...db.safetyBanners].sort((a, b) => a.sort_order - b.sort_order).map(toSafetyCard)
    }),
  ),
  http.get(
    url('/admin/safety/banners/:id'),
    route(({ request, params }) => {
      requireAdmin(request)
      return toSafetyDetails(findBanner(idParam(params, 'id')))
    }),
  ),
  http.post(
    url('/admin/safety/banners'),
    route(async ({ request }) => {
      requireAdmin(request)
      const body = await readJson<SafetyBannerInput>(request)
      validateBanner(body)
      const banner: SafetyBannerRow = {
        id: newId(),
        ...bannerFields(body),
        sort_order: body.sort_order ?? db.safetyBanners.length + 1,
        published: body.published ?? false,
      }
      db.safetyBanners.push(banner)
      return created(toSafetyDetails(banner))
    }),
  ),
  http.put(
    url('/admin/safety/banners/:id'),
    route(async ({ request, params }) => {
      requireAdmin(request)
      const banner = findBanner(idParam(params, 'id'))
      const body = await readJson<SafetyBannerInput>(request)
      validateBanner(body)
      Object.assign(banner, bannerFields(body), {
        sort_order: body.sort_order ?? banner.sort_order,
        published: body.published ?? banner.published,
      })
      return toSafetyDetails(banner)
    }),
  ),
  http.delete(
    url('/admin/safety/banners/:id'),
    route(({ request, params }) => {
      requireAdmin(request)
      const banner = findBanner(idParam(params, 'id'))
      db.safetyBanners = db.safetyBanners.filter((b) => b !== banner)
      return noContent()
    }),
  ),
  http.post(
    url('/admin/safety/banners/:id/publish'),
    route(({ request, params }) => {
      requireAdmin(request)
      const banner = findBanner(idParam(params, 'id'))
      banner.published = true
      return toSafetyCard(banner)
    }),
  ),
  http.post(
    url('/admin/safety/banners/:id/unpublish'),
    route(({ request, params }) => {
      requireAdmin(request)
      const banner = findBanner(idParam(params, 'id'))
      banner.published = false
      return toSafetyCard(banner)
    }),
  ),
]

function findBanner(id: Id): SafetyBannerRow {
  return db.safetyBanners.find((b) => b.id === id) ?? fail(404, 'NOT_FOUND', 'Не найдено')
}

function toSafetyCard(b: SafetyBannerRow): SafetyBanner {
  return {
    id: b.id,
    title: b.title,
    subtitle: b.subtitle,
    category: b.category,
    cover_url: b.cover_url,
    accent_color: b.accent_color,
    sport_id: b.sport_id,
    published: b.published,
  }
}

function toSafetyDetails(b: SafetyBannerRow): SafetyBannerDetails {
  return {
    ...toSafetyCard(b),
    slides: b.slides.map((s, i) => ({ position: i + 1, ...s })),
    disclaimer: 'Памятка не заменяет медицинскую помощь. При угрозе жизни звоните 112.',
  }
}

function validateBanner(body: Partial<SafetyBannerInput>) {
  const fields: Record<string, string> = {}
  if (!body.title?.trim()) fields.title = 'не должно быть пустым'
  if (!body.category) fields.category = 'не должно быть пустым'
  if (body.accent_color && !/^#[0-9A-Fa-f]{6}$/.test(body.accent_color))
    fields.accent_color = 'должно соответствовать формату #RRGGBB'
  if (!body.slides?.length) fields.slides = 'Добавьте хотя бы один слайд'
  body.slides?.forEach((s, i) => {
    if (!s.body?.trim()) fields[`slides[${i}].body`] = 'не должно быть пустым'
  })
  if (Object.keys(fields).length) validation(fields)
}

function bannerFields(body: Partial<SafetyBannerInput>) {
  return {
    title: body.title!.trim(),
    subtitle: body.subtitle?.trim() || null,
    category: body.category!,
    cover_url: body.cover_url?.trim() || null,
    accent_color: body.accent_color || null,
    sport_id: body.sport_id ?? null,
    slides: (body.slides ?? []).map((s) => ({
      title: s.title?.trim() || null,
      body: s.body.trim(),
      image_url: s.image_url?.trim() || null,
    })),
  }
}
