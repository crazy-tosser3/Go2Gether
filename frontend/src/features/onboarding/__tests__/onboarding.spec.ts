import { describe, it, expect, beforeEach } from 'vitest'
import type { User } from '@/api'
import {
  MAX_SPORTS,
  agePresetOf,
  answersFrom,
  clearLocalPrefs,
  inTimes,
  loadLocalPrefs,
  overallLevel,
  saveLocalPrefs,
  toLocalPrefs,
  toProfileUpdate,
  toggleSport,
  validateAbout,
  validateAge,
} from '@/features/onboarding/onboarding'

const user = {
  id: 7,
  name: 'Анна',
  city: 'Красноярск',
  level: 'amateur',
  sport_ids: [3],
  avatar_url: null,
  sport_levels: {},
  birth_year: null,
  radius_km: null,
  preferred_times: [],
  preferred_age_from: null,
  preferred_age_to: null,
} as unknown as User

const now = new Date('2026-10-20T12:00:00')

describe('answersFrom', () => {
  it('берёт сохранённые в профиле предпочтения', () => {
    const saved = {
      ...user,
      sport_levels: { 3: 'pro' },
      birth_year: 1996,
      radius_km: 25,
      preferred_times: ['evening'],
      preferred_age_from: 25,
      preferred_age_to: 40,
    } as unknown as User
    expect(answersFrom(saved, null)).toMatchObject({
      sports: { 3: 'pro' },
      birthYear: '1996',
      radiusKm: 25,
      times: ['evening'],
      ageFrom: '25',
      ageTo: '40',
    })
  })

  it('черновик важнее профиля', () => {
    const a = answersFrom(user, null)
    expect(a).toMatchObject({ name: 'Анна', city: 'Красноярск', sports: { 3: 'amateur' } })
    expect(answersFrom(user, { ...toLocalPrefs(a), sports: { 3: 'pro' } }).sports).toEqual({
      3: 'pro',
    })
  })
})

describe('проверки', () => {
  it('имя обязательно, год — разумный', () => {
    const a = answersFrom(user, null)
    expect(validateAbout({ ...a, name: ' ' }, now).name).toBeDefined()
    expect(validateAbout({ ...a, birthYear: '1890' }, now).birthYear).toBeDefined()
    expect(validateAbout({ ...a, birthYear: '2020' }, now).birthYear).toBeDefined()
    expect(validateAbout({ ...a, birthYear: '1996' }, now)).toEqual({})
  })

  it('числовые поля приходят числом — проверка не падает', () => {
    const a = answersFrom(user, null)
    const asNumber = { ...a, birthYear: 1996, ageFrom: 25, ageTo: 40 } as unknown as typeof a
    expect(validateAbout(asNumber, now)).toEqual({})
    expect(validateAge(asNumber)).toEqual({})
    expect(toLocalPrefs(asNumber)).toMatchObject({ birthYear: '1996', ageFrom: '25', ageTo: '40' })
    expect(agePresetOf(asNumber)).toBe('mid')
  })

  it('возраст компании: 10–99 и «до» не меньше «от»', () => {
    const a = answersFrom(user, null)
    expect(validateAge({ ...a, ageFrom: '5' }).ageFrom).toBeDefined()
    expect(validateAge({ ...a, ageFrom: '40', ageTo: '25' }).ageTo).toBeDefined()
    expect(validateAge({ ...a, ageFrom: '25', ageTo: '40' })).toEqual({})
  })
})

describe('виды спорта', () => {
  it('не больше десяти', () => {
    let s = {}
    for (let id = 1; id <= 12; id++) s = toggleSport(s, id)
    expect(Object.keys(s)).toHaveLength(MAX_SPORTS)
    expect(toggleSport({ 1: 'pro' }, 1)).toEqual({})
  })

  it('общий уровень — самый частый, при равенстве выше', () => {
    expect(overallLevel({ 1: 'beginner', 2: 'beginner', 3: 'pro' })).toBe('beginner')
    expect(overallLevel({ 1: 'beginner', 2: 'pro' })).toBe('pro')
    expect(overallLevel({})).toBeUndefined()
  })
})

describe('toProfileUpdate', () => {
  it('всё уходит в профиль, пустые поля не шлём', () => {
    const a = {
      ...answersFrom(user, null),
      avatar: 'preset:running',
      birthYear: '1996',
      sports: { 3: 'amateur' as const, 5: 'amateur' as const },
    }
    expect(toProfileUpdate(a)).toEqual({
      name: 'Анна',
      city: 'Красноярск',
      avatar_url: 'preset:running',
      sport_ids: [3, 5],
      level: 'amateur',
      sport_levels: { 3: 'amateur', 5: 'amateur' },
      birth_year: 1996,
      radius_km: 10,
      preferred_times: [],
      preferred_age_from: undefined,
      preferred_age_to: undefined,
    })
  })
})

describe('inTimes', () => {
  it('утро, вечер и выходные по местному времени', () => {
    expect(inTimes(new Date('2026-10-21T08:00:00').toISOString(), ['morning'])).toBe(true)
    expect(inTimes(new Date('2026-10-21T19:00:00').toISOString(), ['morning'])).toBe(false)
    expect(inTimes(new Date('2026-10-24T13:00:00').toISOString(), ['weekend'])).toBe(true)
    expect(inTimes(new Date('2026-10-24T13:00:00').toISOString(), [])).toBe(false)
  })
})

describe('agePresetOf', () => {
  it('узнаёт готовый диапазон', () => {
    expect(agePresetOf({ ageFrom: '25', ageTo: '40' })).toBe('mid')
    expect(agePresetOf({ ageFrom: '', ageTo: '' })).toBe('any')
    expect(agePresetOf({ ageFrom: '30', ageTo: '35' })).toBeNull()
  })
})

describe('черновик', () => {
  beforeEach(() => localStorage.clear())

  it('сохраняется по пользователю и стирается после сохранения', () => {
    const prefs = toLocalPrefs(answersFrom(user, null))
    saveLocalPrefs(7, prefs)
    expect(loadLocalPrefs(7)).toEqual(prefs)
    expect(loadLocalPrefs(8)).toBeNull()
    clearLocalPrefs(7)
    expect(loadLocalPrefs(7)).toBeNull()
  })
})
