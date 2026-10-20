import { afterAll, afterEach, beforeAll, beforeEach, describe, expect, it, vi } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import { http, HttpResponse } from 'msw'
import { setupServer } from 'msw/node'
import { handlers } from '@/mocks/handlers'
import { resetDb } from '@/mocks/db'
import { authApi } from '@/api'
import { useSessionStore } from '@/stores/session'
import EventsView from '../EventsView.vue'

const server = setupServer(...handlers)

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
beforeEach(() => {
  // Тест проверяет сборку без ключа карты — локальный ключ из .env.local не должен на него влиять.
  vi.stubEnv('VITE_2GIS_KEY', '')
  resetDb()
  setActivePinia(createPinia())
})
afterEach(async () => {
  server.resetHandlers()
  vi.unstubAllEnvs()
  await authApi.logout().catch(() => {})
})
afterAll(() => server.close())

async function setup(path = '/events') {
  const Stub = { template: '<div />' }
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/events', name: 'events', component: EventsView },
      { path: '/events/new', name: 'event-create', component: Stub },
      { path: '/events/:id', name: 'event', component: Stub },
      { path: '/login', name: 'login', component: Stub },
    ],
  })
  await router.push(path)
  const wrapper = mount(EventsView, { global: { plugins: [router] } })
  await flushPromises()
  return { wrapper, router }
}

const button = (wrapper: Awaited<ReturnType<typeof setup>>['wrapper'], text: string) =>
  wrapper.findAll('button').find((b) => b.text().startsWith(text))!

const titles = (wrapper: Awaited<ReturnType<typeof setup>>['wrapper']) =>
  wrapper.findAll('article h3').map((h) => h.text())

describe('EventsView', () => {
  it('показывает опубликованные будущие мероприятия без снятых', async () => {
    const { wrapper } = await setup()
    expect(titles(wrapper)).toContain('Дворовый футбол 5×5')
    expect(titles(wrapper)).not.toContain('Велопрогулка вдоль Енисея')
    expect(wrapper.text()).toContain('Найдено 9 мероприятий')
  })

  it('фильтры открываются шторкой и применяются по «Показать»', async () => {
    const { wrapper, router } = await setup()
    await button(wrapper, 'Фильтры').trigger('click')
    await flushPromises()
    await button(wrapper, 'По заявке').trigger('click')
    // До «Показать» адрес не меняется — условия копятся в шторке.
    expect(router.currentRoute.value.query).toEqual({})
    await new Promise((r) => setTimeout(r, 350))
    await flushPromises()
    const apply = wrapper.findAll('button').find((b) => b.text().startsWith('Показать 3'))!
    await apply.trigger('click')
    await flushPromises()
    expect(router.currentRoute.value.query).toEqual({ join_mode: 'approval' })
    expect(wrapper.text()).toContain('По заявке')
    expect(titles(wrapper).sort()).toEqual(
      ['Дворовый футбол 5×5', 'Интервальная тренировка на стадионе', 'Пляжный волейбол'].sort(),
    )
  })

  it('читает фильтры из ссылки и показывает пустое состояние со сбросом', async () => {
    const { wrapper, router } = await setup('/events?q=керлинг')
    expect(wrapper.text()).toContain('Пока никто не собирается')
    const reset = wrapper.findAll('button').find((b) => b.text() === 'Сбросить фильтры')!
    await reset.trigger('click')
    await flushPromises()
    expect(router.currentRoute.value.query).toEqual({})
    expect(titles(wrapper).length).toBe(9)
  })

  it('выбранный фильтр снимается чипом', async () => {
    const { wrapper, router } = await setup('/events?join_mode=open&q=йога')
    await wrapper.get('[aria-label="Убрать фильтр: Свободная запись"]').trigger('click')
    await flushPromises()
    expect(router.currentRoute.value.query).toEqual({ q: 'йога' })
  })

  it('переключается на карту, сохраняя фильтры в адресе', async () => {
    await useSessionStore().loginDemo(2)
    const { wrapper, router } = await setup('/events?join_mode=open')
    await button(wrapper, 'Карта').trigger('click')
    await flushPromises()
    expect(router.currentRoute.value.query).toEqual({ join_mode: 'open', view: 'map' })
    // В тестах ключа 2ГИС нет — карта честно говорит, что не подключена, список остаётся.
    expect(wrapper.text()).toContain('Карта не подключена')
    expect(wrapper.findAll('article h3').length).toBeGreaterThan(0)
  })

  it('гостю вместо карты — предложение войти, адресов в карточках нет', async () => {
    const { wrapper } = await setup('/events?view=map')
    expect(wrapper.text()).toContain('Карта мероприятий доступна после входа')
    expect(wrapper.findAll('article h3')).toHaveLength(0)
    const list = await setup('/events')
    expect(list.wrapper.text()).not.toMatch(/ул\.|набережная/i)
  })

  it('при ошибке сервера даёт повторить', async () => {
    server.use(http.get('*/api/v1/events', () => HttpResponse.json({}, { status: 500 })))
    const { wrapper } = await setup()
    expect(wrapper.get('[role="alert"]').text()).toContain('Не получилось загрузить мероприятия')
    server.resetHandlers()
    await wrapper
      .findAll('button')
      .find((b) => b.text() === 'Повторить')!
      .trigger('click')
    await flushPromises()
    expect(titles(wrapper).length).toBe(9)
  })
})
