import { describe, expect, it } from 'vitest'
import { createMemoryHistory, createRouter } from 'vue-router'
import { resolveAccess, type AccessState } from '../access'

const Stub = { template: '<div />' }

const router = createRouter({
  history: createMemoryHistory(),
  routes: [
    { path: '/', name: 'home', component: Stub },
    { path: '/me', name: 'mine', component: Stub, meta: { requiresAuth: true } },
    {
      path: '/admin',
      meta: { adminOnly: true },
      children: [{ path: 'events', name: 'admin-events', component: Stub }],
    },
    { path: '/login', name: 'login', component: Stub },
    {
      path: '/events/new',
      name: 'event-create',
      component: Stub,
      meta: { requiresAuth: true, noAdmin: true },
    },
    { path: '/register', name: 'register', component: Stub, meta: { guestOnly: true } },
  ],
})

const guest: AccessState = { isAuthenticated: false, isAdmin: false }
const user: AccessState = { isAuthenticated: true, isAdmin: false }
const admin: AccessState = { isAuthenticated: true, isAdmin: true }

const check = (path: string, state: AccessState) => resolveAccess(router.resolve(path), state)

describe('resolveAccess', () => {
  it('пускает гостя на публичные страницы', () => {
    expect(check('/', guest)).toBe(true)
  })

  it('отправляет гостя на вход и запоминает, куда он шёл', () => {
    expect(check('/me?tab=archive', guest)).toEqual({
      name: 'login',
      query: { redirect: '/me?tab=archive' },
    })
  })

  it('закрывает админку для обычного пользователя, в том числе вложенные страницы', () => {
    expect(check('/admin/events', user)).toEqual({ name: 'forbidden' })
    expect(check('/admin/events', admin)).toBe(true)
  })

  it('гостя в админку сначала отправляет на вход', () => {
    expect(check('/admin/events', guest)).toMatchObject({ name: 'login' })
  })

  it('пускает вошедшего на вход (смена роли), но уводит с регистрации', () => {
    expect(check('/login', user)).toBe(true)
    expect(check('/register', user)).toEqual({ name: 'home' })
  })

  it('администратора не пускает создавать мероприятия — уводит в админку', () => {
    expect(check('/events/new', admin)).toEqual({ name: 'admin' })
    expect(check('/events/new', user)).toBe(true)
  })
})
