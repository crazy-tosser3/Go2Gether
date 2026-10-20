import { http } from './http'
import type {
  AdminDashboard,
  AdminEventDetails,
  AdminOrganizationDetails,
  Comment,
  EventDetails,
  EventStatus,
  EventSummary,
  Id,
  ModerationLog,
  Organization,
  OrganizationStatus,
  Page,
  Report,
  ReportStatus,
  SafetyBanner,
  SafetyBannerDetails,
  SafetyBannerInput,
} from './types'

type PageQuery = { page?: number; size?: number }

export const adminApi = {
  dashboard: () => http.get<AdminDashboard>('/admin/dashboard'),

  reports: (query: { status?: ReportStatus; event_id?: Id } & PageQuery = {}) =>
    http.get<Page<Report>>('/admin/reports', { query }),
  acceptReport: (id: Id, reason: string) =>
    http.post<Report>(`/admin/reports/${id}/accept`, { reason }),
  rejectReport: (id: Id, reason?: string) =>
    http.post<Report>(`/admin/reports/${id}/reject`, reason ? { reason } : {}),

  organizations: (query: { status?: OrganizationStatus; q?: string } & PageQuery = {}) =>
    http.get<Page<Organization>>('/admin/organizations', { query }),
  organization: (id: Id) => http.get<AdminOrganizationDetails>(`/admin/organizations/${id}`),
  approveOrganization: (id: Id) => http.post<Organization>(`/admin/organizations/${id}/approve`),
  rejectOrganization: (id: Id, reason: string) =>
    http.post<Organization>(`/admin/organizations/${id}/reject`, { reason }),

  events: (
    query: { status?: EventStatus; q?: string; organizer?: Id; organization?: Id } & PageQuery = {},
  ) => http.get<Page<EventSummary>>('/admin/events', { query }),
  event: (id: Id) => http.get<AdminEventDetails>(`/admin/events/${id}`),
  unpublishEvent: (id: Id, reason: string) =>
    http.post<EventDetails>(`/admin/events/${id}/unpublish`, { reason }),
  deleteEvent: (id: Id, reason: string) =>
    http.delete<EventDetails>(`/admin/events/${id}`, { reason }),
  restoreEvent: (id: Id) => http.post<EventDetails>(`/admin/events/${id}/restore`),

  hideComment: (id: Id, reason: string) =>
    http.post<Comment>(`/admin/comments/${id}/hide`, { reason }),

  auditLog: (query: { target_type?: 'organization' | 'event' | 'comment' } & PageQuery = {}) =>
    http.get<Page<ModerationLog>>('/admin/audit-log', { query }),

  safetyBanners: () => http.get<SafetyBanner[]>('/admin/safety/banners'),
  safetyBanner: (id: Id) => http.get<SafetyBannerDetails>(`/admin/safety/banners/${id}`),
  createSafetyBanner: (body: SafetyBannerInput) =>
    http.post<SafetyBannerDetails>('/admin/safety/banners', body),
  updateSafetyBanner: (id: Id, body: SafetyBannerInput) =>
    http.put<SafetyBannerDetails>(`/admin/safety/banners/${id}`, body),
  deleteSafetyBanner: (id: Id) => http.delete<void>(`/admin/safety/banners/${id}`),
  publishSafetyBanner: (id: Id) => http.post<SafetyBanner>(`/admin/safety/banners/${id}/publish`),
  unpublishSafetyBanner: (id: Id) =>
    http.post<SafetyBanner>(`/admin/safety/banners/${id}/unpublish`),
}
