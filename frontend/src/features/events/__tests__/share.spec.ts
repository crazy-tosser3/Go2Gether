import { describe, it, expect } from 'vitest'
import {
  buildIcs,
  canAddToCalendar,
  icsDate,
  icsEscape,
  icsFold,
  icsFileName,
  eventPlace,
} from '@/features/events/share'

const event = {
  id: 7,
  title: 'Йога, на траве; утро',
  description: 'Мягкая практика\nКоврик свой',
  starts_at: '2026-10-25T01:00:00.000Z',
  ends_at: null,
  city: 'Красноярск',
  address: 'Остров Татышев',
}

describe('icsDate / icsEscape', () => {
  it('время в UTC без разделителей', () => {
    expect(icsDate('2026-10-25T01:00:00.000Z')).toBe('20261025T010000Z')
  })

  it('экранирует спецсимволы и переводы строк', () => {
    expect(icsEscape('a,b;c\\d\ne')).toBe('a\\,b\\;c\\\\d\\ne')
  })
})

describe('icsFold', () => {
  it('режет длинную строку не длиннее 75 байт, не разрывая кириллицу', () => {
    const folded = icsFold(`SUMMARY:${'Йога'.repeat(30)}`)
    const enc = new TextEncoder()
    for (const line of folded.split('\r\n')) expect(enc.encode(line).length).toBeLessThanOrEqual(75)
    expect(
      folded
        .split('\r\n')
        .map((l, i) => (i ? l.slice(1) : l))
        .join(''),
    ).toBe(`SUMMARY:${'Йога'.repeat(30)}`)
  })
})

describe('buildIcs', () => {
  const ics = buildIcs(
    event,
    'https://go2gether.example/events/7',
    new Date('2026-10-20T00:00:00Z'),
  )

  it('собирает одно событие с местом, описанием и ссылкой', () => {
    expect(ics).toContain('BEGIN:VEVENT\r\n')
    expect(ics).toContain('UID:event-7@go2gether')
    expect(ics).toContain('DTSTART:20261025T010000Z')
    expect(ics).toContain('SUMMARY:Йога\\, на траве\\; утро')
    expect(ics).toContain('LOCATION:Красноярск\\, Остров Татышев')
    expect(ics.endsWith('END:VCALENDAR\r\n')).toBe(true)
  })

  it('без окончания — занятие на час', () => {
    expect(ics).toContain('DTEND:20261025T020000Z')
  })

  it('имя файла по id', () => {
    expect(icsFileName(event)).toBe('go2gether-7.ics')
  })
})

describe('canAddToCalendar', () => {
  const base = {
    status: 'published',
    is_past: false,
    is_organizer: false,
    join_mode: 'approval',
  } as const

  it('по заявке — только подтверждённым и организатору', () => {
    expect(canAddToCalendar({ ...base, my_participation: { status: 'pending' } })).toBe(false)
    expect(canAddToCalendar({ ...base, my_participation: { status: 'confirmed' } })).toBe(true)
    expect(canAddToCalendar({ ...base, is_organizer: true })).toBe(true)
  })

  it('свободная запись — всем; прошедшее или снятое — никому', () => {
    expect(canAddToCalendar({ ...base, join_mode: 'open' })).toBe(true)
    expect(canAddToCalendar({ ...base, join_mode: 'open', is_past: true })).toBe(false)
    expect(canAddToCalendar({ ...base, join_mode: 'open', status: 'cancelled' })).toBe(false)
  })
})

describe('eventPlace', () => {
  it('не повторяет город, если он уже в адресе', () => {
    expect(eventPlace({ city: 'Красноярск', address: 'Красноярск, Центральный парк' })).toBe(
      'Красноярск, Центральный парк',
    )
    expect(eventPlace({ city: 'Красноярск', address: 'Остров Татышев' })).toBe(
      'Красноярск, Остров Татышев',
    )
    expect(eventPlace({ city: null, address: 'Остров Татышев' })).toBe('Остров Татышев')
  })
})
