import type { DemoAccount } from '@/api'
import { t } from '@/i18n'

export type DemoGroupKey = 'users' | 'organizations' | 'admin'

export interface DemoGroup {
  key: DemoGroupKey
  title: string
  accounts: (DemoAccount & { scenarios: string | null })[]
}

// Почты демо-аккаунтов одинаковы в моках и на бэке; роль организации API не отдаёт, поэтому группу берём отсюда.
// scenarios — ключ подсказки в словаре (login.scenarios.*).
const KNOWN: Record<string, { group: DemoGroupKey; scenarios: string }> = {
  'user@demo.local': {
    group: 'users',
    scenarios: 'user',
  },
  'user2@demo.local': {
    group: 'users',
    scenarios: 'user2',
  },
  'org@demo.local': {
    group: 'organizations',
    scenarios: 'org',
  },
  'org2@demo.local': {
    group: 'organizations',
    scenarios: 'org2',
  },
  'admin@demo.local': {
    group: 'admin',
    scenarios: 'admin',
  },
}

export function groupDemoAccounts(accounts: DemoAccount[]): DemoGroup[] {
  const groups: DemoGroup[] = (['users', 'organizations', 'admin'] as const).map((key) => ({
    key,
    title: t(`login.groups.${key}`),
    accounts: [],
  }))
  for (const account of accounts) {
    const known = KNOWN[account.email.toLowerCase()]
    const key = known?.group ?? (account.role === 'admin' ? 'admin' : 'users')
    groups
      .find((g) => g.key === key)!
      .accounts.push({
        ...account,
        scenarios: known ? t(`login.scenarios.${known.scenarios}`) : null,
      })
  }
  return groups.filter((g) => g.accounts.length)
}
