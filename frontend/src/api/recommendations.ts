import { http } from './http'
import type { Id, Recommendation } from './types'

export const recommendationsApi = {
  /** Подборка «Для вас»: со входом — персональная, без входа — популярное и ближайшее. */
  events: (query: { limit?: number; lat?: number; lng?: number } = {}) =>
    http.get<Recommendation[]>('/recommendations/events', { query }),
  /** Похожие на мероприятие `id`; само оно в ответ не попадает. */
  similar: (id: Id, limit = 4) =>
    http.get<Recommendation[]>(`/events/${id}/similar`, { query: { limit } }),
}
