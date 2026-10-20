import { http } from './http'
import type {
  EventSummary,
  Id,
  Organization,
  OrganizationInput,
  OrganizationUpdate,
  Page,
} from './types'

export const organizationsApi = {
  create: (body: OrganizationInput) => http.post<Organization>('/organizations', body),
  my: () => http.get<Organization[]>('/organizations/my'),
  update: (id: Id, body: OrganizationUpdate) =>
    http.patch<Organization>(`/organizations/${id}`, body),
  resubmit: (id: Id) => http.post<Organization>(`/organizations/${id}/resubmit`),
  list: (query: { q?: string; sport?: string; city?: string; page?: number; size?: number } = {}) =>
    http.get<Page<Organization>>('/organizations', { query }),
  get: (id: Id) => http.get<Organization>(`/organizations/${id}`),
  events: (id: Id) => http.get<EventSummary[]>(`/organizations/${id}/events`),
}
