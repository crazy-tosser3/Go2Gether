import { describe, it, expect } from 'vitest'
import type { SafetyBanner } from '@/api'
import { bannerToStory, pickForEvent } from '@/features/safety/safety'

const banner = (id: number, patch: Partial<SafetyBanner> = {}): SafetyBanner => ({
  id,
  title: `Баннер ${id}`,
  subtitle: null,
  category: 'safety_rules',
  cover_url: null,
  accent_color: '#1F6F43',
  sport_id: null,
  published: true,
  ...patch,
})

describe('bannerToStory', () => {
  const slides = [
    { position: 2, title: null, body: 'Навстречу движению', image_url: null },
    { position: 1, title: 'Будьте заметны', body: 'Светоотражатели', image_url: null },
  ]

  it('слайды по порядку, без заголовка — название памятки, оговорка — у всей сторис', () => {
    const story = bannerToStory(
      { ...banner(5, { title: 'Безопасная пробежка' }), slides, disclaimer: 'Не заменяет врача' },
      'Бег',
    )
    expect(story).toMatchObject({ id: 'safety-5', label: 'Безопасная пробежка', icon: 'running' })
    expect(story.slides.map((s) => s.title)).toEqual(['Будьте заметны', 'Безопасная пробежка'])
    expect(story.disclaimer).toBe('Не заменяет врача')
  })

  it('известная тема — обложка, короткое имя, иконки и кнопка 112 из макета', () => {
    const story = bannerToStory({
      ...banner(6, { title: 'Экстренные номера', category: 'emergency' }),
      slides: [
        { position: 1, title: 'Номера', body: '112', image_url: null },
        { position: 2, title: 'Что сказать', body: 'Адрес', image_url: null },
      ],
      disclaimer: null,
    })
    expect(story).toMatchObject({ cover: 'emergency', short: 'Номера 112' })
    expect(story.slides.map((s) => [s.icon, s.call])).toEqual([
      ['numbers', true],
      ['speech', false],
    ])
  })

  it('памятка из админки — без обложки, в цвете категории', () => {
    const story = bannerToStory({ ...banner(7, { title: 'Жара' }), slides, disclaimer: null })
    expect(story.cover).toBeUndefined()
    expect(story.bg).toBe('bg-accent-soft')
    expect(story.slides[0]!.icon).toBeUndefined()
  })
})

describe('pickForEvent', () => {
  it('сначала по этому виду спорта, потом общие правила; чужие и снятые — нет', () => {
    const list = [
      banner(1, { category: 'emergency' }),
      banner(2, { category: 'first_aid' }),
      banner(3, { sport_id: 3 }),
      banner(4, { sport_id: 7 }),
      banner(5),
      banner(6, { published: false }),
    ]
    expect(pickForEvent(list, 3).map((b) => b.id)).toEqual([3, 5, 2])
  })
})
