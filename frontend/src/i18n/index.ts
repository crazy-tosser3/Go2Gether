import { computed } from 'vue'
import { createI18n } from 'vue-i18n'
import en from './en'
import ru from './ru'

export type Locale = 'ru' | 'en'

export const LOCALES: { value: Locale; label: string }[] = [
  { value: 'ru', label: 'Русский' },
  { value: 'en', label: 'English' },
]

// Язык — настройка интерфейса, хранится в браузере. По умолчанию русский (53-ФЗ о государственном языке).
const KEY = 'locale'

export function parseLocale(value: unknown): Locale {
  return value === 'en' ? 'en' : 'ru'
}

function readLocale(): Locale {
  try {
    return parseLocale(localStorage.getItem(KEY))
  } catch {
    return 'ru'
  }
}

/** Русские формы через «|»: один | два–четыре | пять и больше (4 формы — первой идёт ноль). */
export function ruPlural(choice: number, choicesLength: number): number {
  const n = Math.abs(choice)
  const shift = choicesLength === 4 ? 1 : 0
  if (shift && n === 0) return 0
  if (choicesLength < 3) return n === 1 ? 0 : 1
  const m10 = n % 10
  const m100 = n % 100
  if (m10 === 1 && m100 !== 11) return shift
  if (m10 >= 2 && m10 <= 4 && (m100 < 12 || m100 > 14)) return shift + 1
  return shift + 2
}

export const i18n = createI18n({
  legacy: false,
  locale: readLocale(),
  fallbackLocale: 'ru',
  messages: { ru, en },
  pluralRules: { ru: ruPlural },
  missingWarn: false,
  fallbackWarn: false,
})

/** Перевод вне компонентов (утилиты, подписи статусов). Внутри computed и шаблонов реактивен к смене языка. */
export const t = i18n.global.t

export function currentLocale(): Locale {
  return parseLocale(i18n.global.locale.value)
}

/** Локаль для Intl: даты и числа. */
export function intlLocale(): string {
  return currentLocale() === 'en' ? 'en-GB' : 'ru-RU'
}

export function setLocale(locale: Locale) {
  i18n.global.locale.value = locale
  if (typeof document !== 'undefined') document.documentElement.lang = locale
  try {
    if (locale === 'ru') localStorage.removeItem(KEY)
    else localStorage.setItem(KEY, locale)
  } catch {
    // Хранилище недоступно — язык действует до перезагрузки.
  }
}

export function initLocale() {
  setLocale(currentLocale())
}

export function useLocale() {
  return computed({ get: currentLocale, set: setLocale })
}
