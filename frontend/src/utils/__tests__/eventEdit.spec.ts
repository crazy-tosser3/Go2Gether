import { describe, it, expect } from 'vitest'
import type { EventDetails } from '@/api/types'
import {
  eventFormFrom,
  newSportValue,
  repeatFormFrom,
  toEventUpdate,
  validateEventEdit,
  type EventFormState,
} from '@/utils/eventForm'

const now = new Date('2026-10-19T12:00:00')

const event = {
  id: 7,
  title: 'Йога на траве',
  description: 'Мягкая практика',
  sport: { id: 3, name: 'Йога' },
  organizer: { type: 'organization', id: 4, name: 'Клуб', avatar_url: null },
  starts_at: new Date('2026-10-25T08:00:00').toISOString(),
  ends_at: new Date('2026-10-25T09:30:00').toISOString(),
  city: 'Красноярск',
  address: 'Остров Татышев',
  location_note: null,
  level: 'beginner',
  requirements: null,
  equipment: 'Коврик',
  join_mode: 'open',
  capacity: 12,
} as unknown as EventDetails

function edited(patch: Partial<EventFormState>): [EventFormState, EventFormState] {
  const original = eventFormFrom(event)
  return [{ ...original, ...patch }, original]
}

describe('eventFormFrom', () => {
  it('раскладывает мероприятие по полям формы в местном времени', () => {
    expect(eventFormFrom(event)).toMatchObject({
      sport: '3',
      date: '2026-10-25',
      startTime: '08:00',
      endTime: '09:30',
      locationNote: '',
      capacity: '12',
      organizationId: 4,
    })
  })
})

describe('toEventUpdate', () => {
  it('без изменений — пустое тело', () => {
    expect(toEventUpdate(...edited({}))).toEqual({})
  })

  it('отправляет только изменённые поля, очищенный текст — пустой строкой', () => {
    expect(
      toEventUpdate(...edited({ title: ' Йога у реки ', equipment: '', capacity: '20' })),
    ).toEqual({ title: 'Йога у реки', equipment: '', capacity: 20 })
  })

  it('новая дата переносит и начало, и окончание', () => {
    const body = toEventUpdate(...edited({ date: '2026-10-26' }))
    expect(Object.keys(body).sort()).toEqual(['ends_at', 'starts_at'])
    expect(body.starts_at).toBe(new Date('2026-10-26T08:00:00').toISOString())
  })

  it('своё направление уходит названием', () => {
    expect(toEventUpdate(...edited({ sport: newSportValue('Городки') }))).toEqual({
      sport_name: 'Городки',
    })
  })
})

describe('validateEventEdit', () => {
  it('не даёт убрать то, что бэк очистить не может', () => {
    const errors = validateEventEdit(...edited({ capacity: '', endTime: '', level: '' }), now)
    expect(Object.keys(errors).sort()).toEqual(['capacity', 'endTime', 'level'])
  })

  it('уже начавшееся время не мешает править остальное, а перенос в прошлое — ошибка', () => {
    const late = new Date('2026-10-25T08:30:00')
    expect(validateEventEdit(...edited({ title: 'Новое' }), late)).toEqual({})
    expect(validateEventEdit(...edited({ startTime: '07:00' }), now)).toEqual({})
    expect(validateEventEdit(...edited({ date: '2026-10-18' }), now).startTime).toBeDefined()
  })
})

describe('repeatFormFrom', () => {
  it('копирует мероприятие без даты и времени', () => {
    expect(repeatFormFrom(event, [4])).toMatchObject({
      title: 'Йога на траве',
      sport: '3',
      address: 'Остров Татышев',
      capacity: '12',
      date: '',
      startTime: '',
      endTime: '',
      organizationId: 4,
    })
  })

  it('организация больше не допущена — от себя', () => {
    expect(repeatFormFrom(event, []).organizationId).toBeNull()
  })
})

describe('точка на карте', () => {
  it('новая точка уходит в PATCH, без изменений — нет', () => {
    expect(toEventUpdate(...edited({ point: { lat: 56.01, lng: 92.85 } }))).toEqual({
      lat: 56.01,
      lng: 92.85,
    })
    expect(toEventUpdate(...edited({}))).toEqual({})
  })

  it('точка из карточки мероприятия', () => {
    expect(eventFormFrom({ ...event, lat: 56, lng: 92 } as EventDetails).point).toEqual({
      lat: 56,
      lng: 92,
    })
    expect(eventFormFrom(event).point).toBeNull()
  })
})
