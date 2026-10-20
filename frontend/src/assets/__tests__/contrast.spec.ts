import { readFileSync } from 'node:fs'
import { resolve } from 'node:path'
import { describe, expect, it } from 'vitest'

// Контраст текста по ГОСТ Р 52872-2019 (≈ WCAG 2.1 AA): обычный текст — не ниже 4,5:1.
// Vitest подменяет импорт CSS пустой строкой, поэтому читаем файл напрямую.
const css = readFileSync(resolve(process.cwd(), 'src/assets/main.css'), 'utf-8')

function tokensOf(block: string): Record<string, string> {
  return Object.fromEntries(
    [...block.matchAll(/--color-([\w-]+):\s*(#[0-9a-f]{6})/gi)].map((m) => [m[1]!, m[2]!]),
  )
}

const light = tokensOf(css.match(/@theme \{[^}]*\}/)![0])
const dark = tokensOf(css.match(/:root\[data-theme='dark'\] \{[^}]*\}/)![0])

function luminance(hex: string) {
  const [r, g, b] = [1, 3, 5].map((i) => {
    const c = parseInt(hex.slice(i, i + 2), 16) / 255
    return c <= 0.03928 ? c / 12.92 : ((c + 0.055) / 1.055) ** 2.4
  })
  return 0.2126 * r! + 0.7152 * g! + 0.0722 * b!
}

function contrast(tokens: Record<string, string>, fg: string, bg: string) {
  const [a, b] = [luminance(tokens[fg]!), luminance(tokens[bg]!)].sort((x, y) => y - x)
  return (a! + 0.05) / (b! + 0.05)
}

// Нейтральные поверхности — по ним текст темы; бренд-поверхности одинаковы в обеих темах, текст на них #111111.
const NEUTRAL = ['page', 'card', 'field']
const BRAND = ['accent', 'accent-soft', 'lilac', 'orange', 'cream']
const STATUSES = ['success', 'warning', 'queue', 'danger']
const onBrand = { ink: '#111111', 'ink-2': '#34364a', 'accent-ink': '#1b1f6b' }

describe.each([
  ['светлой', light],
  ['тёмной', dark],
])('контраст %s темы', (_, tokens) => {
  it.each(NEUTRAL)('основной и вторичный текст читаются на фоне %s', (bg) => {
    expect(contrast(tokens, 'ink', bg)).toBeGreaterThanOrEqual(4.5)
    expect(contrast(tokens, 'ink-2', bg)).toBeGreaterThanOrEqual(4.5)
  })

  it.each(['page', 'card'])('даты и ошибки читаются на фоне %s', (bg) => {
    expect(contrast(tokens, 'accent-ink', bg)).toBeGreaterThanOrEqual(4.5)
    expect(contrast(tokens, 'danger', bg)).toBeGreaterThanOrEqual(4.5)
  })

  it('текст на главной кнопке читается', () => {
    expect(contrast(tokens, 'white', 'ink')).toBeGreaterThanOrEqual(4.5)
  })
})

describe('бренд-цвета и статусы', () => {
  it.each(BRAND)('на %s текст, вторичный текст и даты читаются', (bg) => {
    for (const fg of Object.values(onBrand)) {
      expect(contrast({ ...light, fg }, 'fg', bg)).toBeGreaterThanOrEqual(4.5)
    }
  })

  it('на бренд-поверхностях цвета текста переопределены правилом', () => {
    expect(css).toMatch(
      /:is\(\.bg-accent, \.bg-accent-soft, \.bg-lilac, \.bg-orange, \.bg-cream\) \{\s*--color-ink: #111111;/,
    )
  })

  it.each(STATUSES)('статус %s: пара фон и текст читается', (k) => {
    expect(contrast(light, `${k}-fg`, `${k}-bg`)).toBeGreaterThanOrEqual(4.5)
  })

  it('бренд-цвета и статусы в тёмной теме не меняются', () => {
    for (const k of [...BRAND, ...STATUSES.flatMap((x) => [`${x}-bg`, `${x}-fg`])]) {
      expect(dark[k]).toBeUndefined()
    }
  })

  it('блок для системной тёмной темы совпадает с явным выбором', () => {
    const system = css.match(/:root:not\(\[data-theme='light'\]\) \{[^}]*\}/)![0]
    expect(tokensOf(system)).toEqual(dark)
  })
})
