import type { RouteLocationRaw } from 'vue-router'
import type { SportIconName } from '@/components/illustrations/SportIcon.vue'
import { intlLocale, t } from '@/i18n'

// Акций в API нет — набор задаём здесь. Баннер виден только в свои даты (включительно, по местному времени).
export interface Promo {
  id: string
  tag: string
  title: string
  text: string
  starts: string
  ends: string
  icon: SportIconName
  /** Большая иллюстрация из макета; без неё — рисунок вида спорта. */
  art?: 'runners' | 'kick'
  bg: string
  cta: string
  to: RouteLocationRaw
}

// Тексты — в словарях (promos.<id>), здесь только даты, оформление и ссылки.
function promo(id: string, rest: Omit<Promo, 'id' | 'tag' | 'title' | 'text' | 'cta'>): Promo {
  return {
    id,
    ...rest,
    get tag() {
      return t(`promos.${id}.tag`)
    },
    get title() {
      return t(`promos.${id}.title`)
    },
    get text() {
      return t(`promos.${id}.text`)
    },
    get cta() {
      return t(`promos.${id}.cta`)
    },
  }
}

export const PROMOS: Promo[] = [
  promo('clubBattle', {
    starts: '2026-10-01',
    ends: '2026-11-30',
    icon: 'football',
    art: 'kick',
    bg: 'bg-orange',
    to: { name: 'organizations' },
  }),
  promo('activityMarathon', {
    starts: '2026-10-15',
    ends: '2026-11-15',
    icon: 'running',
    bg: 'bg-lilac',
    to: { name: 'events' },
  }),
  promo('runSeason', {
    starts: '2026-09-15',
    ends: '2026-11-30',
    icon: 'running',
    art: 'runners',
    bg: 'bg-accent-soft',
    to: { name: 'events', query: { q: 'бег' } },
  }),
  promo('chessWeek', {
    starts: '2026-10-19',
    ends: '2026-11-01',
    icon: 'chess',
    bg: 'bg-card',
    to: { name: 'events', query: { q: 'шахмат' } },
  }),
]

const day = (iso: string) => {
  const [y, m, d] = iso.split('-').map(Number)
  return new Date(y!, m! - 1, d!)
}

export function activePromos(now: Date, promos: Promo[] = PROMOS): Promo[] {
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate()).getTime()
  return promos.filter((p) => day(p.starts).getTime() <= today && today <= day(p.ends).getTime())
}

export function promoPeriod(p: Promo): string {
  const date = new Intl.DateTimeFormat(intlLocale(), { day: 'numeric', month: 'long' })
  return t('promos.until', { date: date.format(day(p.ends)) })
}
