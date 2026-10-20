import { describe, it, expect } from 'vitest'
import type { User } from '@/api'
import {
  isPhoto,
  photoError,
  profileFormFrom,
  toProfileInput,
  toggleSport,
  validateProfileForm,
} from '@/features/profile/profileForm'

const user = {
  name: 'Анна Смирнова',
  city: null,
  level: null,
  bio: null,
  sport_ids: [1, 2],
  sport_levels: { 1: 'pro' },
} as unknown as User

describe('profileForm', () => {
  it('заполняет форму из профиля, пустые поля — пустые строки', () => {
    const form = profileFormFrom(user)
    expect(form).toEqual({
      name: 'Анна Смирнова',
      city: '',
      level: '',
      bio: '',
      sportIds: [1, 2],
      sportLevels: { 1: 'pro' },
      avatar: '',
    })
    form.sportIds.push(3)
    expect(user.sport_ids).toEqual([1, 2])
  })

  it('имя обязательно, о себе — не длиннее 500', () => {
    expect(validateProfileForm({ ...profileFormFrom(user), name: '  ' }).name).toBeDefined()
    expect(
      validateProfileForm({ ...profileFormFrom(user), bio: 'x'.repeat(501) }).bio,
    ).toBeDefined()
    expect(validateProfileForm(profileFormFrom(user))).toEqual({})
  })

  it('собирает тело PATCH /users/me', () => {
    const input = toProfileInput({
      ...profileFormFrom(user),
      city: ' Красноярск ',
      level: 'amateur',
      avatar: 'preset:tennis',
    })
    expect(input).toEqual({
      name: 'Анна Смирнова',
      city: 'Красноярск',
      bio: '',
      level: 'amateur',
      sport_ids: [1, 2],
      sport_levels: { 1: 'pro' },
      avatar_url: 'preset:tennis',
    })
    expect(toProfileInput(profileFormFrom(user)).level).toBeUndefined()
  })

  it('переключает интересы', () => {
    expect(toggleSport([1, 2], 2)).toEqual([1])
    expect(toggleSport([1], 3)).toEqual([1, 3])
  })

  it('уровни уходят только для выбранных видов', () => {
    const form = {
      ...profileFormFrom(user),
      sportIds: [2],
      sportLevels: { 1: 'pro' as const, 2: 'amateur' as const },
    }
    expect(toProfileInput(form).sport_levels).toEqual({ 2: 'amateur' })
  })

  it('фото: тип и размер', () => {
    const file = (type: string, size: number) => new File([new Uint8Array(size)], 'a', { type })
    expect(photoError(file('image/png', 10))).toBeNull()
    expect(photoError(file('image/gif', 10))).toBeTruthy()
    expect(photoError(file('image/jpeg', 6 * 1024 * 1024))).toBeTruthy()
  })

  it('загруженное фото отличается от рисунка из набора', () => {
    expect(isPhoto('http://localhost:8081/api/v1/avatars/a.png')).toBe(true)
    expect(isPhoto('preset:tennis')).toBe(false)
    expect(isPhoto(null)).toBe(false)
  })
})
