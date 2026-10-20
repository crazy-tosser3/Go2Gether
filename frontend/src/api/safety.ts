import { http } from './http'
import type { Id, SafetyBanner, SafetyBannerDetails, SafetyCategory } from './types'

export const safetyApi = {
  /** Опубликованные баннеры; с `sport` — этого вида спорта и общие. */
  list: (query: { category?: SafetyCategory; sport?: Id } = {}) =>
    http.get<SafetyBanner[]>('/safety/banners', { query }),
  get: (id: Id) => http.get<SafetyBannerDetails>(`/safety/banners/${id}`),
}
