import { describe, it, expect } from 'vitest'
import type { DemoAccount } from '@/api'
import { groupDemoAccounts } from '@/features/auth/demoRoles'

const acc = (id: number, email: string, role: 'user' | 'admin' = 'user'): DemoAccount => ({
  id,
  name: `Аккаунт ${id}`,
  email,
  role,
  description: '',
})

describe('groupDemoAccounts', () => {
  it('раскладывает демо-аккаунты по ролям в порядке пользователи → организации → админ', () => {
    const groups = groupDemoAccounts([
      acc(5, 'admin@demo.local', 'admin'),
      acc(3, 'org@demo.local'),
      acc(1, 'user@demo.local'),
      acc(4, 'Org2@demo.local'),
      acc(2, 'user2@demo.local'),
    ])
    expect(groups.map((g) => [g.title, g.accounts.map((a) => a.id)])).toEqual([
      ['Пользователи', [1, 2]],
      ['Организации', [3, 4]],
      ['Администратор', [5]],
    ])
    expect(groups[0]!.accounts[0]!.scenarios).toContain('С1')
  })

  it('незнакомый аккаунт — по роли и без подсказки', () => {
    const groups = groupDemoAccounts([acc(9, 'x@demo.local', 'admin'), acc(8, 'y@demo.local')])
    expect(groups.map((g) => g.key)).toEqual(['users', 'admin'])
    expect(groups[1]!.accounts[0]!.scenarios).toBeNull()
  })

  it('пустые группы не показываем', () => {
    expect(groupDemoAccounts([])).toEqual([])
  })
})
