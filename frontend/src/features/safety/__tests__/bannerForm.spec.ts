import { describe, it, expect } from 'vitest'
import {
  bannerFormFrom,
  emptyBannerForm,
  hasErrors,
  moveSlide,
  toBannerInput,
  validateBanner,
} from '@/features/safety/bannerForm'

describe('validateBanner', () => {
  it('пустая форма — название, категория и текст слайда', () => {
    const e = validateBanner(emptyBannerForm())
    expect(e.title).toBeDefined()
    expect(e.category).toBeDefined()
    expect(e.slideBodies[0]).toBeDefined()
    expect(hasErrors(e)).toBe(true)
  })

  it('цвет — только #RRGGBB, без слайдов нельзя', () => {
    const f = { ...emptyBannerForm(), title: 'Жара', category: 'safety_rules' as const }
    expect(validateBanner({ ...f, accentColor: 'red', slides: [] }).accentColor).toBeDefined()
    expect(validateBanner({ ...f, slides: [] }).slides).toBeDefined()
    expect(hasErrors(validateBanner({ ...f, slides: [{ title: '', body: 'Пейте воду' }] }))).toBe(
      false,
    )
  })
})

describe('toBannerInput / bannerFormFrom', () => {
  it('пустые необязательные поля не отправляем, слайды — по порядку', () => {
    const input = toBannerInput({
      ...emptyBannerForm(),
      title: ' Жара ',
      category: 'safety_rules',
      slides: [{ title: '', body: ' Пейте воду ' }],
    })
    expect(input).toEqual({
      title: 'Жара',
      subtitle: undefined,
      category: 'safety_rules',
      accent_color: '#1F6F43',
      sport_id: undefined,
      published: false,
      slides: [{ title: undefined, body: 'Пейте воду' }],
    })
  })

  it('из баннера с бэка — слайды по position', () => {
    const f = bannerFormFrom({
      id: 1,
      title: 'Ушибы',
      subtitle: null,
      category: 'first_aid',
      cover_url: null,
      accent_color: null,
      sport_id: 3,
      published: true,
      disclaimer: null,
      slides: [
        { position: 2, title: null, body: 'Второй', image_url: null },
        { position: 1, title: 'Первый', body: 'Первый', image_url: null },
      ],
    })
    expect(f.slides.map((s) => s.body)).toEqual(['Первый', 'Второй'])
    expect(f).toMatchObject({ sportId: 3, accentColor: '#1F6F43', published: true })
  })
})

describe('moveSlide', () => {
  it('меняет местами соседей и не выходит за края', () => {
    const s = [
      { title: 'a', body: 'a' },
      { title: 'b', body: 'b' },
    ]
    expect(moveSlide(s, 0, 1).map((x) => x.title)).toEqual(['b', 'a'])
    expect(moveSlide(s, 0, -1)).toBe(s)
  })
})
