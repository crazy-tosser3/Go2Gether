import type { SafetyBanner } from '@/api'

export type CoverKind =
  'intro' | 'warmup' | 'pack' | 'run' | 'bruise' | 'bleeding' | 'emergency' | 'cpr'

export type SlideIcon =
  | 'bulb'
  | 'bookmark'
  | 'swipe'
  | 'stop'
  | 'cold'
  | 'elevate'
  | 'doctor'
  | 'press'
  | 'bandage'
  | 'call112'
  | 'safety'
  | 'breath'
  | 'cpr'
  | 'timer'
  | 'joints'
  | 'stretch'
  | 'wellbeing'
  | 'reflect'
  | 'road'
  | 'headphones'
  | 'route'
  | 'water'
  | 'kit'
  | 'phone'
  | 'compass'
  | 'numbers'
  | 'speech'
  | 'hold'

/** Оформление темы памятки: обложка, короткое имя для карточки, цвета просмотра, иконки слайдов. */
export interface StoryArt {
  cover: CoverKind
  /** Ключ словаря с коротким названием (`safety.short.<cover>`). */
  short: CoverKind
  /** Цвет фона и текста просмотра — пары из макета, контраст проверен для обеих тем. */
  bg: string
  fg: string
  icons: SlideIcon[]
  /** Слайды, на которых нужна кнопка «Позвонить 112». */
  call: number[]
}

const art = (
  cover: CoverKind,
  bg: string,
  fg: string,
  icons: SlideIcon[],
  call: number[] = [],
): StoryArt => ({ cover, short: cover, bg, fg, icons, call })

export const INTRO_ART = art('intro', '#fff7ec', '#111111', ['bulb', 'bookmark', 'swipe'])

// Тексты памяток приходят с бэка, а рисунки в макете сделаны под конкретные темы — сопоставляем по названию.
// Порядок — от бытового к экстренному: реанимация последней, чтобы не пугать с первого экрана.
const KNOWN: [string, StoryArt][] = [
  [
    'разминка перед тренировкой',
    art('warmup', '#a5b4ff', '#111111', ['timer', 'joints', 'stretch', 'wellbeing']),
  ],
  ['что взять с собой', art('pack', '#ffffff', '#111111', ['water', 'kit', 'phone', 'compass'])],
  [
    'безопасная пробежка',
    art('run', '#111111', '#ffffff', ['reflect', 'road', 'headphones', 'route']),
  ],
  [
    'ушибы и растяжения',
    art('bruise', '#c9d3ff', '#111111', ['stop', 'cold', 'elevate', 'doctor']),
  ],
  [
    'остановка кровотечения',
    art('bleeding', '#e9b5f5', '#111111', ['press', 'bandage', 'call112'], [2]),
  ],
  ['экстренные номера', art('emergency', '#e2432b', '#111111', ['numbers', 'speech', 'hold'], [0])],
  [
    'сердечно-лёгочная реанимация',
    art('cpr', '#f28c28', '#111111', ['safety', 'breath', 'call112', 'cpr'], [2]),
  ],
]

const normalize = (title: string) => title.trim().toLowerCase().replace(/ё/g, 'е')
const BY_TITLE = new Map(KNOWN.map(([title, a]) => [normalize(title), a]))

/** Оформление известной темы; для памяток, созданных в админке, — null (карточка в цвете категории). */
export function artFor(title: string): StoryArt | null {
  return BY_TITLE.get(normalize(title)) ?? null
}

/** Известные темы — в порядке макета, новые из админки — после них, в порядке бэка. */
export function sortByArt<T extends Pick<SafetyBanner, 'title'>>(banners: T[]): T[] {
  const rank = (b: T) => {
    const a = artFor(b.title)
    return a ? KNOWN.findIndex(([, k]) => k === a) : KNOWN.length
  }
  return banners
    .map((b, i) => ({ b, i }))
    .sort((x, y) => rank(x.b) - rank(y.b) || x.i - y.i)
    .map((x) => x.b)
}
