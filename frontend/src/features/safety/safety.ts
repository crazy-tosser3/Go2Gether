import type { Id, SafetyBanner, SafetyBannerDetails, SafetyCategory } from '@/api'
import type { SportIconName } from '@/components/illustrations/SportIcon.vue'
import type { Story } from '@/features/home/stories'
import { artFor } from './storyArt'
import { t } from '@/i18n'

// Цвет баннера с бэка тёмный — чёрный текст на нём не читается. Фон берём из токенов по категории,
// а цвет бэка показываем маленькой меткой.
const CATEGORY_BG: Record<SafetyCategory, string> = {
  first_aid: 'bg-orange',
  safety_rules: 'bg-accent-soft',
  equipment: 'bg-lilac',
  emergency: 'bg-orange',
}

const SPORT_ICONS: Partial<Record<string, SportIconName>> = {
  Бег: 'running',
  Футбол: 'football',
  Теннис: 'tennis',
  'Настольный теннис': 'tennis',
  Шахматы: 'chess',
  Велоспорт: 'bike',
}

export const SAFETY_CATEGORIES: SafetyCategory[] = [
  'first_aid',
  'safety_rules',
  'equipment',
  'emergency',
]

export const categoryBg = (c: SafetyCategory) => CATEGORY_BG[c]

export const categoryLabel = (c: SafetyCategory) => t(`safety.categories.${c}`)

/** Баннер — это сторис: те же карточка и просмотрщик со слайдами. Известные темы — с рисунками макета. */
export function bannerToStory(b: SafetyBannerDetails, sportName?: string): Story {
  const art = artFor(b.title)
  return {
    id: `safety-${b.id}`,
    label: b.title,
    icon: (sportName && SPORT_ICONS[sportName]) || 'custom',
    bg: categoryBg(b.category),
    cover: art?.cover,
    short: art ? t(`safety.short.${art.short}`) : undefined,
    palette: art ? { bg: art.bg, fg: art.fg } : undefined,
    subtitle: b.subtitle ?? undefined,
    badge: categoryLabel(b.category),
    badgeColor: b.accent_color ?? undefined,
    disclaimer: b.disclaimer ?? undefined,
    slides: [...b.slides]
      .sort((x, y) => x.position - y.position)
      .map((s, i) => ({
        title: s.title ?? b.title,
        text: s.body,
        icon: art?.icons[i],
        call: art?.call.includes(i),
      })),
  }
}

// Перед тренировкой важнее всего памятка по этому виду спорта, потом общие правила и экстренные номера.
const ORDER: SafetyCategory[] = ['safety_rules', 'equipment', 'first_aid', 'emergency']

export function pickForEvent(banners: SafetyBanner[], sportId: Id, max = 3): SafetyBanner[] {
  const rank = (b: SafetyBanner) => (b.sport_id === sportId ? 0 : 10) + ORDER.indexOf(b.category)
  return banners
    .filter((b) => b.published && (b.sport_id === null || b.sport_id === sportId))
    .sort((a, b) => rank(a) - rank(b))
    .slice(0, max)
}
