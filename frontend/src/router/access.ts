import type { RouteLocationNormalized, RouteLocationRaw } from 'vue-router'
import { t } from '@/i18n'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    requiresAuth?: boolean
    adminOnly?: boolean
    guestOnly?: boolean
    /** Действие не для администратора (по матрице доступа: создавать мероприятия он не может). */
    noAdmin?: boolean
  }
}

export interface AccessState {
  isAuthenticated: boolean
  isAdmin: boolean
}

/** Куда пустить пользователя: true — на запрошенную страницу, иначе — адрес перенаправления. */
export function resolveAccess(
  to: Pick<RouteLocationNormalized, 'matched' | 'fullPath'>,
  state: AccessState,
): true | RouteLocationRaw {
  const needsAuth = to.matched.some((r) => r.meta.requiresAuth || r.meta.adminOnly)
  if (needsAuth && !state.isAuthenticated) {
    return { name: 'login', query: { redirect: to.fullPath } }
  }
  if (to.matched.some((r) => r.meta.adminOnly) && !state.isAdmin) {
    return { name: 'forbidden' }
  }
  if (to.matched.some((r) => r.meta.noAdmin) && state.isAdmin) {
    return { name: 'admin' }
  }
  if (to.matched.some((r) => r.meta.guestOnly) && state.isAuthenticated) {
    return { name: 'home' }
  }
  return true
}

export const APP_NAME = 'Go2Gether'

export function documentTitle(title?: string) {
  return title ? `${title} — ${APP_NAME}` : `${APP_NAME} — ${t('home.tagline')}`
}
