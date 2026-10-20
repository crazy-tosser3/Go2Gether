import type { EventDetails } from '@/api'

type CalendarEvent = Pick<
  EventDetails,
  'id' | 'title' | 'description' | 'starts_at' | 'ends_at' | 'city' | 'address'
>

const HOUR = 60 * 60 * 1000

/** Время в формате iCalendar (UTC): 20261025T010000Z. */
export function icsDate(iso: string): string {
  return new Date(iso)
    .toISOString()
    .replace(/[-:]/g, '')
    .replace(/\.\d{3}/, '')
}

// RFC 5545: в тексте экранируются \ ; , и переводы строк.
export function icsEscape(text: string): string {
  return text
    .replace(/\\/g, '\\\\')
    .replace(/;/g, '\\;')
    .replace(/,/g, '\\,')
    .replace(/\r?\n/g, '\\n')
}

// RFC 5545: строка не длиннее 75 байт, продолжение — с пробела. Режем по символам, чтобы не порвать UTF-8.
export function icsFold(line: string): string {
  const encoder = new TextEncoder()
  const parts: string[] = []
  let current = ''
  for (const ch of line) {
    const limit = parts.length ? 74 : 75
    if (encoder.encode(current + ch).length > limit) {
      parts.push(current)
      current = ch
    } else current += ch
  }
  parts.push(current)
  return parts.join('\r\n ')
}

// Адрес часто уже начинается с города — тогда город не повторяем.
export function eventPlace(event: Pick<EventDetails, 'city' | 'address'>): string {
  const city = event.city?.trim()
  if (!city || event.address.toLowerCase().includes(city.toLowerCase())) return event.address
  return `${city}, ${event.address}`
}

/** Файл .ics для «Добавить в календарь»; без окончания — считаем, что занятие идёт час. */
export function buildIcs(event: CalendarEvent, url: string, now = new Date()): string {
  const end = event.ends_at ?? new Date(new Date(event.starts_at).getTime() + HOUR).toISOString()
  const description = [event.description?.trim(), url].filter(Boolean).join('\n\n')
  const lines = [
    'BEGIN:VCALENDAR',
    'VERSION:2.0',
    'PRODID:-//Vmeste//Events//RU',
    'CALSCALE:GREGORIAN',
    'METHOD:PUBLISH',
    'BEGIN:VEVENT',
    `UID:event-${event.id}@go2gether`,
    `DTSTAMP:${icsDate(now.toISOString())}`,
    `DTSTART:${icsDate(event.starts_at)}`,
    `DTEND:${icsDate(end)}`,
    `SUMMARY:${icsEscape(event.title)}`,
    `LOCATION:${icsEscape(eventPlace(event))}`,
    `DESCRIPTION:${icsEscape(description)}`,
    `URL:${url}`,
    'END:VEVENT',
    'END:VCALENDAR',
  ]
  return lines.map(icsFold).join('\r\n') + '\r\n'
}

export function icsFileName(event: Pick<EventDetails, 'id'>): string {
  return `go2gether-${event.id}.ics`
}

/** Календарь — тем, кто точно придёт: организатору, подтверждённым и на свободную запись. */
export function canAddToCalendar(
  event: Pick<EventDetails, 'status' | 'is_past' | 'is_organizer' | 'join_mode'> & {
    my_participation?: { status: string } | null
  },
): boolean {
  if (event.status !== 'published' || event.is_past) return false
  return (
    event.is_organizer ||
    event.my_participation?.status === 'confirmed' ||
    event.join_mode === 'open'
  )
}
