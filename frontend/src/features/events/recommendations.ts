import type { RecommendationReason } from '@/api'
import { t } from '@/i18n'

// Коды причин бэка (GET /recommendations/events, /events/{id}/similar). Новые коды бэк может добавить —
// их пропускаем, пока нет подписи.
const KNOWN = new Set<string>([
  'interest',
  'played_before',
  'same_city',
  'nearby',
  'level_match',
  'friends_going',
  'familiar_organizer',
  'popular',
  'soon',
  'same_sport',
  'same_category',
  'same_organizer',
  'shared_participants',
])

/** Подписи 1–2 самых весомых причин; у «Похожих» город и расстояние — от этого мероприятия, а не от вас. */
export function reasonLabels(
  reasons: readonly (RecommendationReason | string)[],
  kind: 'personal' | 'similar' = 'personal',
  max = 2,
): string[] {
  return reasons
    .filter((r) => KNOWN.has(r))
    .slice(0, max)
    .map((r) =>
      kind === 'similar' && (r === 'same_city' || r === 'nearby')
        ? t(`forYou.reasons.${r}_similar`)
        : t(`forYou.reasons.${r}`),
    )
}
