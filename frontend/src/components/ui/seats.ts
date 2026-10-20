import { t } from '@/i18n'

export function seatsPercent(taken: number, capacity: number | null): number {
  if (!capacity || capacity <= 0) return 0
  return Math.min(100, Math.max(0, Math.round((taken / capacity) * 100)))
}

export function seatsLabel(taken: number, capacity: number | null): string {
  if (!capacity) return t('seats.participants', taken)
  const left = Math.max(0, capacity - taken)
  if (left === 0) return t('seats.ofFull', { taken, capacity })
  return t('seats.ofLeft', { taken, capacity, left: t('seats.places', left) })
}

/** Русские формы слова по числу. Для переводимых текстов — t('…', n) из словаря. */
export function plural(n: number, forms: [string, string, string]): string {
  const m10 = n % 10
  const m100 = n % 100
  if (m10 === 1 && m100 !== 11) return forms[0]
  if (m10 >= 2 && m10 <= 4 && (m100 < 12 || m100 > 14)) return forms[1]
  return forms[2]
}

/** Короткая подпись для карточки в списке: только то, что важно для решения. */
export function seatsShort(taken: number, capacity: number | null): string {
  if (!capacity) return t('seats.participants', taken)
  const left = Math.max(0, capacity - taken)
  return left === 0 ? t('seats.full') : t('seats.more', { left: t('seats.places', left) })
}
