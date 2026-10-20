import type { RouteLocationRaw } from 'vue-router'
import type { SportIconName } from '@/components/illustrations/SportIcon.vue'
import { INTRO_ART, type CoverKind, type SlideIcon } from '@/features/safety/storyArt'
import { t } from '@/i18n'

// Сторис будут приходить с бэка (данные в БД); пока набор задан здесь — заменить источник, форма та же.
export interface StorySlide {
  title: string
  text: string
  cta?: { label: string; to: RouteLocationRaw }
  /** Пиктограмма слайда памятки; без неё — рисунок вида спорта. */
  icon?: SlideIcon
  /** Кнопка «Позвонить 112». */
  call?: boolean
}

export interface Story {
  id: string
  label: string
  icon: SportIconName
  /** Фон карточки, если нет обложки. */
  bg: string
  /** Обложка памятки и короткое имя на стикере карточки. */
  cover?: CoverKind
  short?: string
  /** Карточка-вступление «Полезно знать»: текст вместо рисунка. */
  intro?: { note: string }
  /** Цвета просмотра (фон и текст) из макета; без них — фон карточки и цвет текста темы. */
  palette?: { bg: string; fg: string }
  /** Рукописная подпись, категория и цвет её метки — у памяток. */
  subtitle?: string
  badge?: string
  badgeColor?: string
  /** Оговорка под каждым слайдом. */
  disclaimer?: string
  slides: StorySlide[]
}

/** Сколько показывается один слайд, мс. */
export const SLIDE_MS = 6000

// Тексты — в словарях (stories.<id>: label и слайды s0, s1…), здесь только оформление и ссылки.
function story(
  id: string,
  icon: SportIconName,
  bg: string,
  links: (RouteLocationRaw | null)[],
): Story {
  return {
    id,
    icon,
    bg,
    get label() {
      return t(`stories.${id}.label`)
    },
    slides: links.map((to, i) => ({
      get title() {
        return t(`stories.${id}.s${i}.title`)
      },
      get text() {
        return t(`stories.${id}.s${i}.text`)
      },
      cta: to
        ? {
            get label() {
              return t(`stories.${id}.s${i}.cta`)
            },
            to,
          }
        : undefined,
    })),
  }
}

/** Первая карточка ленты: объясняет, что памятки по безопасности всегда здесь. */
export const INTRO_STORY: Story = {
  id: 'intro',
  icon: 'custom',
  bg: 'bg-ink',
  cover: 'intro',
  palette: { bg: INTRO_ART.bg, fg: INTRO_ART.fg },
  get label() {
    return t('stories.intro.label')
  },
  get short() {
    return t('stories.intro.label')
  },
  get subtitle() {
    return t('stories.intro.subtitle')
  },
  intro: {
    get note() {
      return t('stories.intro.note')
    },
  },
  slides: INTRO_ART.icons.map((icon, i) => ({
    icon,
    get title() {
      return t(`stories.intro.s${i}.title`)
    },
    get text() {
      return t(`stories.intro.s${i}.text`)
    },
  })),
}

export const STORIES: Story[] = [
  story('start', 'custom', 'bg-accent', [{ name: 'events' }, null, null]),
  story('clubs', 'football', 'bg-orange', [null, { name: 'organizations' }]),
  story('running', 'running', 'bg-accent-soft', [{ name: 'events', query: { q: 'бег' } }]),
  story('chess', 'chess', 'bg-lilac', [{ name: 'events', query: { q: 'шахмат' } }]),
  story('organize', 'tennis', 'bg-card', [{ name: 'event-create' }]),
]

export interface StoryPosition {
  story: number
  slide: number
}

/** Следующий или предыдущий слайд с переходом между сторис; null — дальше некуда. */
export function stepStory(
  stories: Story[],
  at: StoryPosition,
  direction: 1 | -1,
): StoryPosition | null {
  const slide = at.slide + direction
  if (slide >= 0 && slide < (stories[at.story]?.slides.length ?? 0)) {
    return { story: at.story, slide }
  }
  const story = at.story + direction
  const target = stories[story]
  if (!target) return null
  return { story, slide: direction === 1 ? 0 : target.slides.length - 1 }
}

// Что уже смотрели — удобство конкретного браузера, не данные: при ошибке хранилища просто всё «новое».
const KEY = 'stories-seen'

export function readSeen(): Set<string> {
  try {
    const raw: unknown = JSON.parse(localStorage.getItem(KEY) ?? '[]')
    return new Set(Array.isArray(raw) ? raw.filter((x) => typeof x === 'string') : [])
  } catch {
    return new Set()
  }
}

export function saveSeen(seen: ReadonlySet<string>) {
  try {
    localStorage.setItem(KEY, JSON.stringify([...seen]))
  } catch {
    // Хранилище недоступно — отметка живёт до перезагрузки.
  }
}
