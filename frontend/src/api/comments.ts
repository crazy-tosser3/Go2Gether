import { http } from './http'
import type { Comment, CommentInput, Id, Page } from './types'

export const commentsApi = {
  list: (eventId: Id, page?: number) =>
    http.get<Page<Comment>>(`/events/${eventId}/comments`, { query: { page, size: 100 } }),
  create: (eventId: Id, body: CommentInput) =>
    http.post<Comment>(`/events/${eventId}/comments`, body),
  update: (id: Id, text: string) => http.patch<Comment>(`/comments/${id}`, { text }),
  remove: (id: Id) => http.delete<void>(`/comments/${id}`),
}
