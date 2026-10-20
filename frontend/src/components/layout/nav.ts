import type { RouteLocationRaw } from 'vue-router'
import { t } from '@/i18n'

export type NavIconName =
  'home' | 'list' | 'calendar' | 'user' | 'building' | 'shield' | 'doc' | 'flag'

export interface NavItem {
  label: string
  to: RouteLocationRaw
  icon: NavIconName
}

export const CREATE_ITEM = {
  get label() {
    return t('nav.create')
  },
  to: { name: 'event-create' },
} as const

// Подписи — геттеры: читаются при отрисовке и меняются вместе с языком.
const item = (key: string, name: string, icon: NavIconName): NavItem => ({
  get label() {
    return t(`nav.${key}`)
  },
  to: { name },
  icon,
})

const HOME = item('home', 'home', 'home')
const EVENTS = item('events', 'events', 'list')
const MINE = item('mine', 'my-participations', 'calendar')
const PROFILE = item('profile', 'profile', 'user')
const MY_EVENTS = item('myEvents', 'my-events', 'flag')
const MY_ORG = item('myOrg', 'my-organization', 'building')
const ORGS = item('orgs', 'organizations', 'building')
const ADMIN = item('admin', 'admin', 'shield')
const PRIVACY = item('privacy', 'privacy', 'doc')

/** Нижняя панель на телефоне: «Создать» стоит посередине отдельной кнопкой. */
export function tabBarItems(): [NavItem[], NavItem[]] {
  return [
    [HOME, EVENTS],
    [MINE, PROFILE],
  ]
}

/** Разделы, не вошедшие в нижнюю панель. */
export function menuItems(isAdmin: boolean): NavItem[] {
  return isAdmin ? [ORGS, ADMIN, PRIVACY] : [MY_EVENTS, MY_ORG, ORGS, PRIVACY]
}

/** Шапка на компьютере: главные разделы. На узком экране — только первые два (`wide: false`). */
export function headerItems(isAdmin: boolean): { item: NavItem; wide: boolean }[] {
  const list = isAdmin ? [HOME, EVENTS, ORGS, ADMIN] : [HOME, EVENTS, MINE, ORGS]
  return list.map((item, n) => ({ item, wide: n > 1 }))
}

/** Меню аватара в шапке: личные разделы, которых нет в навигации. */
export function userMenuItems(isAdmin: boolean): NavItem[] {
  return isAdmin ? [PROFILE] : [PROFILE, MY_EVENTS, MY_ORG]
}
