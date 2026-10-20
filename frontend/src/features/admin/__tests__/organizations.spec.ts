import { describe, it, expect } from 'vitest'
import type { Organization } from '@/api'
import {
  parseStatusFilter,
  rejectReasonError,
  replaceOrganization,
} from '@/features/admin/organizations'

const org = (id: number, status: Organization['status']) => ({ id, status }) as Organization

describe('parseStatusFilter', () => {
  it('по умолчанию показывает тех, кто ждёт решения', () => {
    expect(parseStatusFilter(undefined)).toBe('pending')
    expect(parseStatusFilter('blocked')).toBe('pending')
    expect(parseStatusFilter('approved')).toBe('approved')
    expect(parseStatusFilter('all')).toBe('all')
  })
})

describe('replaceOrganization', () => {
  it('меняет только нужную строку', () => {
    const list = [org(1, 'pending'), org(2, 'pending')]
    const next = replaceOrganization(list, org(2, 'approved'))
    expect(next.map((o) => o.status)).toEqual(['pending', 'approved'])
    expect(next[0]).toBe(list[0])
  })
})

describe('rejectReasonError', () => {
  it('причина обязательна и не формальна', () => {
    expect(rejectReasonError('   ')).not.toBeNull()
    expect(rejectReasonError('нет')).not.toBeNull()
    expect(rejectReasonError('x'.repeat(501))).not.toBeNull()
    expect(rejectReasonError('Добавьте контакты и адрес занятий')).toBeNull()
  })
})
