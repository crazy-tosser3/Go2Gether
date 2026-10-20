import { t } from '@/i18n'

// Точный адрес и карта — только вошедшим: место встречи не должно быть видно случайным людям.
// Бэк пока отдаёт адрес всем, поэтому это лишь интерфейс, а не защита данных.
export function visiblePlace(
  event: { address: string | null; city: string | null },
  isAuthenticated: boolean,
): string | null {
  if (isAuthenticated) return event.address || event.city
  return event.city
}

export function guestPlaceText(city: string | null): string {
  return city ? t('place.guestCity', { city }) : t('place.guest')
}
