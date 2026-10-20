import type {
  EventStatus,
  Id,
  JoinMode,
  Level,
  OrganizationStatus,
  OrganizationType,
  ParticipationStatus,
  SafetyCategory,
  Sport,
  User,
  ReportReason,
  ReportStatus,
} from '@/api/types'
import { createSeed } from './seed'

export interface UserRow extends User {
  password: string
  contacts: string | null
  /** Описание демо-аккаунта для экрана входа; null — обычный пользователь. */
  demo_description: string | null
  /** Аккаунт удалён: строка остаётся, чтобы старые сообщения и записи не ломались. */
  deleted?: boolean
}

export interface OrganizationRow {
  id: Id
  owner_id: Id
  name: string
  description: string | null
  type: OrganizationType
  sports_description: string | null
  city: string | null
  website_url: string | null
  avatar_url: string | null
  contacts: string | null
  status: OrganizationStatus
  reject_reason: string | null
  decided_at: string | null
  created_at: string
  updated_at: string
}

export interface SportRow extends Sport {
  normalized_name: string
  created_by: Id | null
}

export interface EventRow {
  id: Id
  title: string
  description: string | null
  sport_id: Id
  organizer_type: 'user' | 'organization'
  organizer_user_id: Id
  organizer_org_id: Id | null
  starts_at: string
  ends_at: string | null
  city: string | null
  address: string
  lat: number | null
  lng: number | null
  location_note: string | null
  level: Level | null
  requirements: string | null
  equipment: string | null
  join_mode: JoinMode
  capacity: number | null
  status: EventStatus
  moderation_reason: string | null
  created_at: string
  updated_at: string
}

export interface ParticipationRow {
  id: Id
  event_id: Id
  user_id: Id
  status: ParticipationStatus
  message: string | null
  decision_reason: string | null
  decided_at: string | null
  created_at: string
}

export interface CommentRow {
  id: Id
  event_id: Id
  author_id: Id
  as_organization_id: Id | null
  parent_id: Id | null
  text: string
  is_hidden: boolean
  hidden_reason: string | null
  created_at: string
  updated_at: string
}

export interface ModerationRow {
  id: Id
  admin_id: Id
  action:
    | 'org_approved'
    | 'org_rejected'
    | 'event_unpublished'
    | 'event_deleted'
    | 'event_restored'
    | 'comment_hidden'
    | 'report_rejected'
  target_type: 'organization' | 'event' | 'comment'
  target_id: Id
  reason: string | null
  created_at: string
}

export interface ReportRow {
  id: Id
  event_id: Id
  reporter_id: Id
  reason: ReportReason
  comment: string | null
  status: ReportStatus
  resolution_note: string | null
  resolved_at: string | null
  created_at: string
}

export interface SafetyBannerRow {
  id: Id
  title: string
  subtitle: string | null
  category: SafetyCategory
  cover_url: string | null
  accent_color: string | null
  sport_id: Id | null
  sort_order: number
  published: boolean
  slides: { title: string | null; body: string; image_url: string | null }[]
}

export interface Db {
  users: UserRow[]
  organizations: OrganizationRow[]
  sports: SportRow[]
  events: EventRow[]
  participations: ParticipationRow[]
  comments: CommentRow[]
  moderation: ModerationRow[]
  safetyBanners: SafetyBannerRow[]
  reports: ReportRow[]
  sessions: Map<string, Id>
}

export const db: Db = {
  users: [],
  organizations: [],
  sports: [],
  events: [],
  participations: [],
  comments: [],
  moderation: [],
  safetyBanners: [],
  reports: [],
  sessions: new Map(),
}

let lastId = 1000

/** Возвращает моки к исходным демо-данным (для тестов и кнопки сброса). */
export function resetDb(now = new Date()) {
  const seed = createSeed(now)
  db.users = seed.users
  db.organizations = seed.organizations
  db.sports = seed.sports
  db.events = seed.events
  db.participations = seed.participations
  db.comments = seed.comments
  db.moderation = []
  db.safetyBanners = seed.safetyBanners
  db.reports = seed.reports
  db.sessions = new Map()
  lastId = 1000
}

export function newId(): Id {
  return ++lastId
}

export function newToken() {
  return crypto.randomUUID()
}

export function normalizeName(name: string) {
  return name.trim().toLowerCase().replace(/ё/g, 'е').replace(/\s+/g, ' ')
}

resetDb()
