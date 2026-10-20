import type { Id, SafetyBannerDetails, SafetyBannerInput, SafetyCategory } from '@/api'
import { t } from '@/i18n'

export const TITLE_MAX = 120
export const SUBTITLE_MAX = 255
export const BODY_MAX = 2000
export const SLIDES_MAX = 20

export interface SlideForm {
  title: string
  body: string
}

export interface BannerForm {
  title: string
  subtitle: string
  category: SafetyCategory | ''
  accentColor: string
  sportId: Id | null
  published: boolean
  slides: SlideForm[]
}

export interface BannerErrors {
  title?: string
  subtitle?: string
  category?: string
  accentColor?: string
  slides?: string
  /** Ошибки слайдов по номеру слайда. */
  slideBodies: Record<number, string>
}

export function emptyBannerForm(): BannerForm {
  return {
    title: '',
    subtitle: '',
    category: '',
    accentColor: '#1F6F43',
    sportId: null,
    published: false,
    slides: [{ title: '', body: '' }],
  }
}

export function bannerFormFrom(b: SafetyBannerDetails): BannerForm {
  return {
    title: b.title,
    subtitle: b.subtitle ?? '',
    category: b.category,
    accentColor: b.accent_color ?? '#1F6F43',
    sportId: b.sport_id,
    published: b.published,
    slides: [...b.slides]
      .sort((a, c) => a.position - c.position)
      .map((s) => ({ title: s.title ?? '', body: s.body })),
  }
}

export function validateBanner(f: BannerForm): BannerErrors {
  const e: BannerErrors = { slideBodies: {} }
  if (!f.title.trim()) e.title = t('safety.errors.title')
  else if (f.title.trim().length > TITLE_MAX) e.title = t('common.maxLength', { n: TITLE_MAX })
  if (f.subtitle.trim().length > SUBTITLE_MAX)
    e.subtitle = t('common.maxLength', { n: SUBTITLE_MAX })
  if (!f.category) e.category = t('safety.errors.category')
  if (!/^#[0-9A-Fa-f]{6}$/.test(f.accentColor)) e.accentColor = t('safety.errors.color')
  if (!f.slides.length) e.slides = t('safety.errors.noSlides')
  f.slides.forEach((s, i) => {
    if (!s.body.trim()) e.slideBodies[i] = t('safety.errors.body')
    else if (s.body.trim().length > BODY_MAX)
      e.slideBodies[i] = t('common.maxLength', { n: BODY_MAX })
  })
  return e
}

export const hasErrors = (e: BannerErrors) =>
  Object.keys(e).some((k) => k !== 'slideBodies' && e[k as keyof BannerErrors]) ||
  Object.keys(e.slideBodies).length > 0

/** Тело POST/PUT /admin/safety/banners. Вызывать для формы без ошибок. */
export function toBannerInput(f: BannerForm): SafetyBannerInput {
  return {
    title: f.title.trim(),
    subtitle: f.subtitle.trim() || undefined,
    category: f.category as SafetyCategory,
    accent_color: f.accentColor,
    sport_id: f.sportId ?? undefined,
    published: f.published,
    slides: f.slides.map((s) => ({ title: s.title.trim() || undefined, body: s.body.trim() })),
  }
}

/** Переставить слайд на шаг вверх или вниз. */
export function moveSlide(slides: SlideForm[], i: number, dir: -1 | 1): SlideForm[] {
  const j = i + dir
  if (j < 0 || j >= slides.length) return slides
  const next = [...slides]
  ;[next[i], next[j]] = [next[j]!, next[i]!]
  return next
}
