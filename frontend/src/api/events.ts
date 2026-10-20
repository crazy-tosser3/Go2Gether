import { http } from './http'
import type {
  Report,
  ReportInput,
  EventDetails,
  EventFilters,
  EventInput,
  EventSummary,
  EventUpdate,
  Id,
  JoinResult,
  Page,
  Participant,
  ParticipantsList,
  Participation,
  ParticipationStatus,
} from './types'

export const eventsApi = {
  list: (filters: EventFilters = {}) =>
    http.get<Page<EventSummary>>('/events', { query: { ...filters } }),
  get: (id: Id) => http.get<EventDetails>(`/events/${id}`),
  report: (id: Id, body: ReportInput) => http.post<Report>(`/events/${id}/reports`, body),
  create: (body: EventInput) => http.post<EventDetails>('/events', body),
  update: (id: Id, body: EventUpdate) => http.patch<EventDetails>(`/events/${id}`, body),
  cancel: (id: Id, reason: string) => http.post<EventDetails>(`/events/${id}/cancel`, { reason }),

  join: (id: Id, message?: string) =>
    http.post<JoinResult>(`/events/${id}/join`, message ? { message } : {}),
  leave: (id: Id) => http.delete<Participation>(`/events/${id}/join`),
  myParticipation: (id: Id) => http.get<Participation | null>(`/events/${id}/my-participation`),
  participants: (id: Id) => http.get<ParticipantsList>(`/events/${id}/participants`),
  requests: (id: Id, status: ParticipationStatus = 'pending') =>
    http.get<Participant[]>(`/events/${id}/requests`, { query: { status } }),
}

export const participationsApi = {
  approve: (pid: Id) => http.post<Participation>(`/participations/${pid}/approve`),
  reject: (pid: Id, reason?: string) =>
    http.post<Participation>(`/participations/${pid}/reject`, reason ? { reason } : {}),
}
