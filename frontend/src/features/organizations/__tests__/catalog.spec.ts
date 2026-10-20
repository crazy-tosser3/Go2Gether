import { describe, it, expect } from 'vitest'
import type { Organization } from '@/api'
import { filterByType, safeHref, shortHost } from '@/features/organizations/catalog'

describe('filterByType', () => {
  const list = [
    { id: 1, type: 'club' },
    { id: 2, type: 'community' },
  ] as Organization[]

  it('«все» — без фильтра, иначе только нужный тип', () => {
    expect(filterByType(list, 'all')).toHaveLength(2)
    expect(filterByType(list, 'community').map((o) => o.id)).toEqual([2])
  })
})

describe('safeHref', () => {
  it('пропускает только http и https', () => {
    expect(safeHref('https://shag.example/club')).toBe('https://shag.example/club')
    expect(safeHref('javascript:alert(1)')).toBeNull()
    expect(safeHref('shag.example')).toBeNull()
    expect(safeHref(null)).toBeNull()
  })

  it('коротко показывает домен', () => {
    expect(shortHost('https://www.shag.example/club')).toBe('shag.example')
  })
})
