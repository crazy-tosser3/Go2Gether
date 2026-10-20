import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { handlers } from '@/mocks/handlers'
import { resetDb } from '@/mocks/db'
import { U } from '@/mocks/seed'
import { useSessionStore } from '@/stores/session'
import ProfileView from '../ProfileView.vue'

const server = setupServer(...handlers)

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
beforeEach(() => {
  resetDb()
  setActivePinia(createPinia())
})
afterEach(() => server.resetHandlers())
afterAll(() => server.close())

async function open() {
  await useSessionStore().loginDemo(U.anna)
  const Stub = { template: '<div />' }
  const names = [
    'home',
    'user',
    'my-participations',
    'my-events',
    'my-organization',
    'login',
    'onboarding',
    'privacy',
  ]
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/profile', name: 'profile', component: ProfileView },
      ...names.map((name) => ({ path: `/${name}/:id?`, name, component: Stub })),
    ],
  })
  await router.push('/profile')
  const wrapper = mount(ProfileView, { global: { plugins: [router] } })
  await flushPromises()
  return wrapper
}

describe('ProfileView', () => {
  it('показывает интересы пользователя', async () => {
    const wrapper = await open()
    expect(wrapper.text()).toContain('Бег')
    expect(wrapper.text()).toContain('Йога')
  })

  it('без связи не пишет «Не выбраны», а предлагает повторить', async () => {
    server.use(http.get('*/api/v1/sports', () => HttpResponse.error()))
    const wrapper = await open()
    expect(wrapper.text()).not.toContain('Не выбраны')
    expect(wrapper.get('[role="alert"]').text()).toContain('Не получилось загрузить интересы')

    server.resetHandlers()
    await wrapper
      .findAll('button')
      .find((b) => b.text() === 'Повторить')!
      .trigger('click')
    await flushPromises()
    expect(wrapper.text()).toContain('Бег')
  })
})
