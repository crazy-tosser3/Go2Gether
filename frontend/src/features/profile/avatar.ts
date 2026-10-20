import type { SportIconName } from '@/components/illustrations/SportIcon.vue'
import { t } from '@/i18n'

// Загрузки файлов в API нет: в avatar_url пишем выбранный рисунок из нашего набора как «preset:<id>».
const PREFIX = 'preset:'

export type AvatarArtId =
  | 'pool'
  | 'court'
  | 'track'
  | 'tennisCourt'
  | 'pitch'
  | 'runners'
  | 'basketball'
  | 'tennisBall'
  | 'volleyball'
  | 'blocks'
  | 'neon'
  | 'poster'

export interface AvatarPreset {
  id: string
  label: string
  bg: string
  /** Рисунок из набора макета; без него — рисунок вида спорта (старые аватары). */
  art?: AvatarArtId
  sport?: SportIconName
}

// Подпись — геттер из словаря (avatars.<id>), чтобы менялась вместе с языком.
const art = (id: AvatarArtId): AvatarPreset => ({
  id,
  art: id,
  bg: 'bg-field',
  get label() {
    return t(`avatars.${id}`)
  },
})

const sport = (id: SportIconName, bg: string): AvatarPreset => ({
  id,
  sport: id,
  bg,
  get label() {
    return t(`avatars.${id}`)
  },
})

/** Набор для выбора: 12 аватаров из макета. */
export const AVATAR_PRESETS: AvatarPreset[] = (
  [
    'pool',
    'court',
    'track',
    'tennisCourt',
    'pitch',
    'runners',
    'basketball',
    'tennisBall',
    'volleyball',
    'blocks',
    'neon',
    'poster',
  ] as const
).map(art)

// Прежний набор: такие аватары уже сохранены у людей — показываем их, но больше не предлагаем.
const LEGACY: AvatarPreset[] = [
  sport('running', 'bg-orange'),
  sport('football', 'bg-accent'),
  sport('tennis', 'bg-lilac'),
  sport('bike', 'bg-accent-soft'),
  sport('chess', 'bg-orange'),
  sport('custom', 'bg-lilac'),
]

export type AvatarView =
  | { kind: 'preset'; preset: AvatarPreset }
  | { kind: 'image'; src: string }
  | { kind: 'initials'; text: string; bg: string }

const INITIALS_BG = ['bg-orange', 'bg-accent', 'bg-lilac', 'bg-accent-soft']

export const presetAvatarUrl = (id: string) => PREFIX + id

export function initials(name: string): string {
  return name
    .split(/\s+/)
    .filter(Boolean)
    .slice(0, 2)
    .map((w) => w[0]!.toUpperCase())
    .join('')
}

export function avatarView(name: string, url: string | null | undefined): AvatarView {
  const value = url?.trim() ?? ''
  if (value.startsWith(PREFIX)) {
    const preset = [...AVATAR_PRESETS, ...LEGACY].find((p) => PREFIX + p.id === value)
    if (preset) return { kind: 'preset', preset }
  }
  // Картинка — только http(s) или data:image (фото, загруженное в моках), чтобы в src не попало javascript:.
  if (/^(https?:\/\/|data:image\/(png|jpeg|webp);)/i.test(value))
    return { kind: 'image', src: value }
  // Цвет от имени, а не случайный: у человека он один и тот же на всех экранах.
  let hash = 0
  for (const ch of name) hash = (hash * 31 + ch.codePointAt(0)!) >>> 0
  return { kind: 'initials', text: initials(name), bg: INITIALS_BG[hash % INITIALS_BG.length]! }
}
