import { afterAll, beforeAll, beforeEach, describe, expect, it } from 'vitest'
import { createPinia, setActivePinia } from 'pinia'
import { setupServer } from 'msw/node'
import { authApi } from '@/api'
import { handlers } from '@/mocks/handlers'
import { resetDb } from '@/mocks/db'
import { useSessionStore } from '../session'

const server = setupServer(...handlers)

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
beforeEach(async () => {
  resetDb()
  await authApi.logout().catch(() => {})
  setActivePinia(createPinia())
})
afterAll(() => server.close())

describe('session', () => {
  it('входит по демо-аккаунту и подтягивает организацию', async () => {
    const session = useSessionStore()
    await session.loginDemo(3)
    expect(session.isAuthenticated).toBe(true)
    expect(session.isAdmin).toBe(false)
    expect(session.hasApprovedOrganization).toBe(true)
  })

  it('распознаёт администратора', async () => {
    const session = useSessionStore()
    await session.loginDemo(5)
    expect(session.isAdmin).toBe(true)
  })

  it('при неверном пароле остаётся гостем', async () => {
    const session = useSessionStore()
    await expect(session.loginWithEmail('user@demo.local', 'нет')).rejects.toMatchObject({
      status: 401,
    })
    expect(session.isAuthenticated).toBe(false)
  })

  it('после перезагрузки страницы восстанавливает вход по cookie', async () => {
    await useSessionStore().loginDemo(1)
    // Новая страница — новое хранилище, cookie осталась.
    setActivePinia(createPinia())
    const fresh = useSessionStore()
    await fresh.restore()
    expect(fresh.user?.name).toBe('Анна Смирнова')
  })

  it('гость после перезагрузки — просто гость, без ошибок', async () => {
    const session = useSessionStore()
    await session.restore()
    expect(session.isAuthenticated).toBe(false)
  })

  it('выход очищает пользователя и закрывает сессию на сервере', async () => {
    const session = useSessionStore()
    await session.loginDemo(1)
    await session.logout()
    expect(session.user).toBeNull()
    await expect(authApi.me()).rejects.toMatchObject({ status: 401 })
  })
})
