import { http } from './http'
import type { Id, Sport, SportCategory, SportDetails } from './types'

export const sportsApi = {
  list: (q?: string) => http.get<Sport[]>('/sports', { query: { q } }),
  create: (name: string, category?: SportCategory) =>
    http.post<Sport>('/sports', { name, category }),
  get: (id: Id) => http.get<SportDetails>(`/sports/${id}`),
}
