import { describe, expect, it } from 'vitest'
import type { EventDetails, Participation, User } from '@/api'
import { joinAction } from '../participation'

const user = (id: number, role: User['role'] = 'user') => ({ id, role }) as User

function event(extra: Partial<EventDetails> = {}): EventDetails {
  return {
    id: 1,
    title: 'Футбол',
    status: 'published',
    join_mode: 'open',
    capacity: null,
    confirmed_count: 0,
    is_past: false,
    is_organizer: false,
    my_participation: null,
    ...extra,
  } as EventDetails
}

const mine = (status: Participation['status']) => ({ status }) as Participation

describe('joinAction', () => {
  it('гостю предлагает войти', () => {
    expect(joinAction(event(), null)).toEqual({ kind: 'login' })
  })

  it('организатор и администратор не записываются', () => {
    expect(joinAction(event({ is_organizer: true }), user(1)).kind).toBe('organizer')
    expect(joinAction(event(), user(5, 'admin')).kind).toBe('admin')
  })

  it('свободная запись и заявка', () => {
    expect(joinAction(event(), user(1))).toEqual({ kind: 'join' })
    expect(joinAction(event({ join_mode: 'approval' }), user(1))).toEqual({
      kind: 'apply',
      again: false,
    })
  })

  it('своя запись важнее, чем «мест нет» и «прошло»', () => {
    const full = event({ capacity: 2, confirmed_count: 2, my_participation: mine('confirmed') })
    expect(joinAction(full, user(1)).kind).toBe('confirmed')
    expect(
      joinAction(event({ join_mode: 'approval', my_participation: mine('pending') }), user(1)).kind,
    ).toBe('pending')
    expect(joinAction(event({ my_participation: mine('waitlist') }), user(1)).kind).toBe('pending')
  })

  it('прошедшее и заполненное', () => {
    expect(joinAction(event({ is_past: true }), user(1)).kind).toBe('past')
    expect(joinAction(event({ capacity: 2, confirmed_count: 2 }), user(1)).kind).toBe('full')
  })

  it('снятое мероприятие — записи нет никому', () => {
    expect(joinAction(event({ status: 'unpublished' }), user(1)).kind).toBe('unavailable')
    expect(joinAction(event({ status: 'unpublished' }), null).kind).toBe('unavailable')
  })

  it('после отказа можно подать заявку снова, после отмены — записаться снова', () => {
    const rejected = event({ join_mode: 'approval', my_participation: mine('rejected') })
    expect(joinAction(rejected, user(1))).toEqual({ kind: 'apply', again: true })
    expect(joinAction(event({ my_participation: mine('cancelled') }), user(1)).kind).toBe('join')
  })
})
