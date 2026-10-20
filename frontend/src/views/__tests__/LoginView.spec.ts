import { afterAll, beforeAll, beforeEach, describe, expect, it } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import { setupServer } from 'msw/node'
import { handlers } from '@/mocks/handlers'
import { resetDb } from '@/mocks/db'
import { useSessionStore } from '@/stores/session'
import LoginView from '../LoginView.vue'

const server = setupServer(...handlers)

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
beforeEach(() => {
  resetDb()
  setActivePinia(createPinia())
})
afterAll(() => server.close())

async function setup(path = '/login') {
  const Stub = { template: '<div />' }
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', name: 'home', component: Stub },
      { path: '/me', name: 'mine', component: Stub },
      { path: '/login', name: 'login', component: LoginView },
      { path: '/register', name: 'register', component: Stub },
    ],
  })
  await router.push(path)
  const wrapper = mount(LoginView, { global: { plugins: [router] } })
  await flushPromises()
  return { wrapper, router }
}

describe('LoginView', () => {
  it('входит по демо-аккаунту и возвращает туда, куда шёл пользователь', async () => {
    const { wrapper, router } = await setup('/login?redirect=/me')
    const button = wrapper.get('button[aria-label="Войти: Пётр Иванов"]')
    await button.trigger('click')
    await flushPromises()
    expect(useSessionStore().user?.name).toBe('Пётр Иванов')
    expect(router.currentRoute.value.path).toBe('/me')
  })

  it('показывает ошибку при неверном пароле', async () => {
    const { wrapper } = await setup()
    await wrapper.get('input[type="email"]').setValue('user@demo.local')
    await wrapper.get('input[type="password"]').setValue('не тот')
    await wrapper.get('form').trigger('submit')
    await flushPromises()
    expect(wrapper.get('[role="alert"]').text()).toContain('Неверная почта или пароль')
    expect(useSessionStore().isAuthenticated).toBe(false)
  })

  it('входит по почте и паролю', async () => {
    const { wrapper, router } = await setup()
    await wrapper.get('input[type="email"]').setValue('user@demo.local')
    await wrapper.get('input[type="password"]').setValue('Demo1234!')
    await wrapper.get('form').trigger('submit')
    await flushPromises()
    expect(useSessionStore().user?.name).toBe('Анна Смирнова')
    expect(router.currentRoute.value.path).toBe('/')
  })
})
