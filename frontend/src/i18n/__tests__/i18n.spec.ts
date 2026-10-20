import { afterEach, describe, expect, it } from 'vitest'
import en from '@/i18n/en'
import ru from '@/i18n/ru'
import { i18n, intlLocale, parseLocale, ruPlural, setLocale, t } from '@/i18n'

afterEach(() => setLocale('ru'))

function leaves(obj: object, prefix = ''): string[] {
  return Object.entries(obj).flatMap(([k, v]) =>
    typeof v === 'string' ? [`${prefix}${k}`] : leaves(v, `${prefix}${k}.`),
  )
}

describe('ruPlural', () => {
  it('один, два–четыре, пять и больше', () => {
    expect([1, 2, 5, 11, 21, 22, 25, 111, 112].map((n) => ruPlural(n, 3))).toEqual([
      0, 1, 2, 2, 0, 1, 2, 2, 2,
    ])
  })
})

describe('язык', () => {
  it('по умолчанию русский, неизвестное значение — тоже', () => {
    expect(parseLocale('en')).toBe('en')
    expect(parseLocale('de')).toBe('ru')
    expect(parseLocale(null)).toBe('ru')
  })

  it('переключение меняет тексты, lang и локаль дат, выбор запоминается', () => {
    expect(t('nav.events')).toBe('Мероприятия')
    setLocale('en')
    expect(t('nav.events')).toBe('Events')
    expect(document.documentElement.lang).toBe('en')
    expect(intlLocale()).toBe('en-GB')
    expect(localStorage.getItem('locale')).toBe('en')
    setLocale('ru')
    expect(localStorage.getItem('locale')).toBeNull()
  })

  it('числа в обоих языках', () => {
    expect(t('events.count', 3)).toBe('3 мероприятия')
    expect(t('events.count', 5)).toBe('5 мероприятий')
    setLocale('en')
    expect(t('events.count', 1)).toBe('1 event')
    expect(t('events.count', 3)).toBe('3 events')
  })
})

describe('словари', () => {
  it('ключи совпадают', () => {
    expect(leaves(en)).toEqual(leaves(ru))
  })

  it.each(['ru', 'en'] as const)('все строки «%s» разбираются', (locale) => {
    setLocale(locale)
    const errors: string[] = []
    for (const key of leaves(locale === 'ru' ? ru : en)) {
      try {
        i18n.global.t(key, { n: 1, name: 'x', date: 'x', events: 'x', list: 'x', total: 2 })
      } catch {
        errors.push(key)
      }
    }
    expect(errors).toEqual([])
  })
})
