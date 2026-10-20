import type { components } from './schema'

// В OpenAPI бэка все поля необязательные, но на деле приходят всегда — пустые как null.
// Здесь — типы «как приходит»: поля обязательны, null допускается только там, где бэк его шлёт.
type S = components['schemas']

type Present<T> = { [K in keyof T]-?: Exclude<T[K], undefined> }
type WithNull<T, K extends keyof T> = Omit<T, K> & { [P in K]: T[P] | null }
type Model<T, N extends keyof T = never> = WithNull<Present<T>, N>

export type Id = number
/** ISO 8601 */
export type DateTime = string

export type UserRole = NonNullable<S['UserResponse']['role']>
export type Level = NonNullable<S['UserResponse']['level']>
export type SportCategory = NonNullable<S['SportResponse']['category']>
export type OrganizationType = NonNullable<S['OrganizationResponse']['type']>
export type OrganizationStatus = NonNullable<S['OrganizationResponse']['status']>
export type JoinMode = NonNullable<S['EventShortResponse']['join_mode']>
export type EventStatus = NonNullable<S['EventShortResponse']['status']>
export type ParticipationStatus = NonNullable<S['ParticipationResponse']['status']>

export type UserShort = Model<S['UserShortResponse'], 'avatar_url'>

export type TimeSlot = NonNullable<S['UserResponse']['preferred_times']>[number]

// В схеме уровни по видам спорта заглавными, а бэк принимает и отдаёт их строчными, как level.
type SportLevels = Record<string, Level>

export type User = Model<
  Omit<S['UserResponse'], 'sport_ids' | 'sport_levels'> & {
    sport_ids: Id[]
    sport_levels: SportLevels
  },
  | 'avatar_url'
  | 'city'
  | 'bio'
  | 'level'
  | 'birth_year'
  | 'radius_km'
  | 'preferred_age_from'
  | 'preferred_age_to'
>

export type Sport = Model<S['SportResponse'], 'icon'>

export type Organizer = Model<S['OrganizerResponse'], 'avatar_url'>

type EventShared = 'ends_at' | 'city' | 'level' | 'capacity'

export type EventSummary = Model<
  Omit<S['EventShortResponse'], 'sport' | 'organizer'> & { sport: Sport; organizer: Organizer },
  EventShared | 'lat' | 'lng' | 'unavailable_reason'
>

export type PublicUser = Model<
  Omit<S['PublicUserResponse'], 'sports' | 'created_events'> & {
    sports: Sport[]
    created_events: EventSummary[]
  },
  'avatar_url' | 'city' | 'bio' | 'level'
>

export type OrganizationShort = Model<S['OrganizationShortResponse'], 'city'>

export type Organization = Model<
  Omit<S['OrganizationResponse'], 'owner'> & { owner: UserShort },
  | 'description'
  | 'sports_description'
  | 'city'
  | 'website_url'
  | 'avatar_url'
  | 'contacts'
  | 'reject_reason'
  | 'decided_at'
>

export type Participation = Model<
  S['ParticipationResponse'],
  'message' | 'decision_reason' | 'decided_at'
>

export type Participant = Model<
  Omit<S['ParticipantResponse'], 'user'> & { user: UserShort },
  'email' | 'message' | 'decision_reason'
>

export type ParticipantsList = Model<
  Omit<S['ParticipantsResponse'], 'participants'> & { participants: Participant[] },
  'capacity'
>

export type EventDetails = Model<
  Omit<S['EventDetailsResponse'], 'sport' | 'organizer' | 'participants' | 'my_participation'> & {
    sport: Sport
    organizer: Organizer
    participants: UserShort[]
    my_participation: Participation | null
  },
  | EventShared
  | 'description'
  | 'organizer_contacts'
  | 'location_note'
  | 'lat'
  | 'lng'
  | 'requirements'
  | 'equipment'
  | 'unavailable_reason'
>

export type Comment = Model<
  Omit<S['CommentResponse'], 'author' | 'as_organization'> & {
    author: UserShort
    as_organization: OrganizationShort | null
  },
  'parent_id' | 'hidden_reason' | 'text' | 'updated_at'
>

export type DemoAccount = Model<S['DemoAccountResponse']>

export type Me = { user: User; organizations: OrganizationShort[] }

export type LoginResult = Present<Omit<S['AuthResponse'], 'user'>> & { user: User }

export type JoinResult = { message: string; participation: Participation; event: EventDetails }

export type MyParticipation = { participation: Participation; event: EventSummary }

export type OrganizerEvent = {
  event: EventSummary
  pending_count: number
  waitlisted_count: number
}

export type SportDetails = Sport & { active_events_count: number }

export type AdminDashboard = Present<S['DashboardResponse']>

export type AdminOrganizationDetails = {
  organization: Organization
  owner: User
  events: EventSummary[]
}

export type AdminEventDetails = {
  event: EventDetails
  moderated_by: UserShort | null
  moderated_at: DateTime | null
  participations: Participant[]
  comments: Comment[]
}

export type ModerationLog = Model<
  Omit<S['ModerationLogResponse'], 'admin'> & { admin: UserShort },
  'reason'
>

export type Notification = Model<S['NotificationResponse'], 'event_id' | 'body'>

export type ReportReason = NonNullable<S['ReportResponse']['reason']>
export type ReportStatus = NonNullable<S['ReportResponse']['status']>

export type Report = Model<
  Omit<S['ReportResponse'], 'event' | 'reporter'> & { event: EventSummary; reporter: UserShort },
  'comment' | 'resolution_note' | 'resolved_at'
>

export type ReportInput = S['CreateReportRequest']

export interface Page<T> {
  items: T[]
  total: number
  page: number
  size: number
  total_pages: number
}

export type EventInput = S['CreateEventRequest']
export type EventUpdate = S['UpdateEventRequest']
export type CommentInput = S['CreateCommentRequest']
export type OrganizationInput = S['CreateOrganizationRequest']
export type OrganizationUpdate = S['UpdateOrganizationRequest']
// null в PATCH у бэка — «не менять»: стереть год рождения или радиус после сохранения нельзя.
export type ProfileInput = Omit<S['UpdateProfileRequest'], 'sport_levels'> & {
  sport_levels?: SportLevels
}

export interface EventFilters {
  q?: string
  sport?: Id
  city?: string
  date_from?: string
  date_to?: string
  level?: Level
  join_mode?: JoinMode
  organizer?: Id
  organization?: Id
  page?: number
  size?: number
}

export interface ApiErrorBody {
  error: {
    code: string
    message: string
    fields?: Record<string, string> | null
  }
}

export type RecommendationReason = NonNullable<S['RecommendationResponse']['reasons']>[number]

export type Recommendation = {
  event: EventSummary
  score: number
  /** По убыванию вклада; неизвестные коды фронт пропускает. */
  reasons: RecommendationReason[]
}

export type SafetyCategory = NonNullable<S['SafetyBannerCard']['category']>

export type SafetyBanner = Model<
  S['SafetyBannerCard'],
  'subtitle' | 'cover_url' | 'accent_color' | 'sport_id'
>

export type SafetySlide = Model<S['SafetySlideResponse'], 'title' | 'image_url'>

export type SafetyBannerDetails = SafetyBanner & {
  slides: SafetySlide[]
  /** Оговорка «не заменяет врача» — показываем мелко на последнем слайде. */
  disclaimer: string | null
}

export type SafetyBannerInput = S['SafetyBannerRequest']
