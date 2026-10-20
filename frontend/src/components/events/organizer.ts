import type { Organizer } from '@/api'
import type { RouteLocationRaw } from 'vue-router'

export function organizerName(o: Organizer) {
  return o.name
}

export function organizerLink(o: Organizer): RouteLocationRaw {
  return o.type === 'organization'
    ? { name: 'organization', params: { id: o.id } }
    : { name: 'user', params: { id: o.id } }
}
