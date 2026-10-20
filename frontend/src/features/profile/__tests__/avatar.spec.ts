import { describe, it, expect } from 'vitest'
import { avatarView, initials, presetAvatarUrl } from '@/features/profile/avatar'

describe('avatar', () => {
  it('инициалы', () => {
    expect(initials('Анна Смирнова')).toBe('АС')
    expect(initials('  Пётр  ')).toBe('П')
  })

  it('рисунок из набора', () => {
    const v = avatarView('Анна', presetAvatarUrl('tennis'))
    expect(v).toMatchObject({ kind: 'preset', preset: { id: 'tennis' } })
  })

  it('картинка только по http(s)', () => {
    expect(avatarView('Анна', 'https://example.ru/a.png')).toEqual({
      kind: 'image',
      src: 'https://example.ru/a.png',
    })
    expect(avatarView('Анна', 'javascript:alert(1)').kind).toBe('initials')
  })

  it('неизвестный рисунок и пустое значение — инициалы с постоянным цветом', () => {
    const a = avatarView('Анна Смирнова', 'preset:нет-такого')
    const b = avatarView('Анна Смирнова', null)
    expect(a).toMatchObject({ kind: 'initials', text: 'АС' })
    expect(a).toEqual(b)
  })
})
