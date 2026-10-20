import { describe, it, expect } from 'vitest'
import {
  authorOptions,
  emptyEventForm,
  mapServerErrors,
  newSportValue,
  toEventInput,
  toIso,
  validateEventForm,
  type EventFormState,
} from '@/utils/eventForm'

const now = new Date('2026-10-19T12:00:00')

function filled(patch: Partial<EventFormState> = {}): EventFormState {
  return {
    ...emptyEventForm(),
    title: '  Теннис после работы ',
    sport: '5',
    date: '2026-10-24',
    startTime: '19:00',
    address: 'Стадион «Енисей»',
    joinMode: 'approval',
    ...patch,
  }
}

describe('validateEventForm', () => {
  it('пустая форма — ошибки в обязательных полях', () => {
    const e = validateEventForm(emptyEventForm(), now)
    expect(Object.keys(e).sort()).toEqual(
      ['address', 'date', 'joinMode', 'sport', 'startTime', 'title'].sort(),
    )
  })

  it('заполненная форма без ошибок', () => {
    expect(validateEventForm(filled(), now)).toEqual({})
  })

  it('время в прошлом и окончание раньше начала', () => {
    expect(
      validateEventForm(filled({ date: '2026-10-19', startTime: '11:00' }), now).startTime,
    ).toBe('Время начала уже прошло')
    expect(validateEventForm(filled({ endTime: '18:30' }), now).endTime).toBe(
      'Окончание позже начала',
    )
  })

  it('лимит мест — целое от 1 до 1000, пустой допустим', () => {
    expect(validateEventForm(filled({ capacity: '0' }), now).capacity).toBeDefined()
    expect(validateEventForm(filled({ capacity: '2.5' }), now).capacity).toBeDefined()
    expect(validateEventForm(filled({ capacity: '1001' }), now).capacity).toBeDefined()
    expect(validateEventForm(filled({ capacity: '8' }), now).capacity).toBeUndefined()
    expect(validateEventForm(filled({ capacity: '  ' }), now).capacity).toBeUndefined()
  })
})

describe('числовое значение лимита', () => {
  it('поле type="number" отдаёт число — форма это переживает', () => {
    const form = filled({ capacity: 8 as unknown as string })
    expect(validateEventForm(form, now)).toEqual({})
    expect(toEventInput(form).capacity).toBe(8)
  })
})

describe('toEventInput', () => {
  it('направление из справочника — sport_id, пустые поля не отправляем', () => {
    const input = toEventInput(filled({ capacity: '8', equipment: '  ' }))
    expect(input.sport_id).toBe(5)
    expect(input.sport_name).toBeUndefined()
    expect(input.title).toBe('Теннис после работы')
    expect(input.capacity).toBe(8)
    expect(input.equipment).toBeUndefined()
    expect(input.starts_at).toBe(toIso('2026-10-24', '19:00'))
    expect(input.organization_id).toBeUndefined()
  })

  it('своё направление — sport_name', () => {
    const input = toEventInput(filled({ sport: newSportValue('Скандинавская ходьба') }))
    expect(input.sport_name).toBe('Скандинавская ходьба')
    expect(input.sport_id).toBeUndefined()
  })

  it('от имени организации', () => {
    expect(toEventInput(filled({ organizationId: 7 })).organization_id).toBe(7)
  })

  it('точка на карте — lat/lng, без точки не отправляем', () => {
    expect(toEventInput(filled({ point: { lat: 56.01, lng: 92.85 } }))).toMatchObject({
      lat: 56.01,
      lng: 92.85,
    })
    expect(toEventInput(filled()).lat).toBeUndefined()
  })
})

describe('mapServerErrors', () => {
  it('переносит поля сервера на поля формы', () => {
    const { errors, rest } = mapServerErrors({
      sport_id: 'Такого направления нет',
      starts_at: 'Время начала должно быть в будущем',
      something: 'Другое',
    })
    expect(errors).toEqual({
      sport: 'Такого направления нет',
      startTime: 'Время начала должно быть в будущем',
    })
    expect(rest).toEqual(['Другое'])
  })
})

describe('toIso', () => {
  it('отклоняет неполные значения', () => {
    expect(toIso('', '10:00')).toBeNull()
    expect(toIso('2026-10-24', '')).toBeNull()
  })
})

describe('authorOptions', () => {
  it('«От себя» первым, затем все допущенные организации', () => {
    expect(
      authorOptions([
        { id: 3, name: 'Беговой клуб «Енисей»' },
        { id: 7, name: 'Шахматный клуб «Ладья»' },
      ]),
    ).toEqual([
      { value: null, label: 'От себя' },
      { value: 3, label: 'Беговой клуб «Енисей»' },
      { value: 7, label: 'Шахматный клуб «Ладья»' },
    ])
  })

  it('без организаций — только «От себя», выбор не показываем', () => {
    expect(authorOptions([])).toHaveLength(1)
  })
})

describe('город мероприятия', () => {
  it('уходит на сервер, пустой — не отправляем', () => {
    const base = {
      ...emptyEventForm(),
      title: 'Футбол',
      sport: '1',
      date: '2099-01-01',
      startTime: '10:00',
      address: 'Стадион',
      joinMode: 'open' as const,
    }
    expect(toEventInput({ ...base, city: ' Красноярск ' }).city).toBe('Красноярск')
    expect(toEventInput({ ...base, city: '' }).city).toBeUndefined()
  })
})
