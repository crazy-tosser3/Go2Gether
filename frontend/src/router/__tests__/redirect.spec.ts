import { describe, expect, it } from 'vitest'
import { safeRedirect } from '../redirect'

describe('safeRedirect', () => {
  it('возвращает внутренний путь как есть', () => {
    expect(safeRedirect('/events/e-1?tab=chat')).toBe('/events/e-1?tab=chat')
  })

  it('не уводит на чужой сайт и не зацикливает на вход', () => {
    expect(safeRedirect('https://evil.example')).toBe('/')
    expect(safeRedirect('//evil.example')).toBe('/')
    expect(safeRedirect('/login?redirect=/me')).toBe('/')
    expect(safeRedirect(undefined)).toBe('/')
  })

  it('берёт первый из повторённых параметров', () => {
    expect(safeRedirect(['/me', '/admin'])).toBe('/me')
  })
})
