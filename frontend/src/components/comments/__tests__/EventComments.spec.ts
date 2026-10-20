import { afterAll, beforeAll, beforeEach, describe, expect, it } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import { setupServer } from 'msw/node'
import { eventsApi } from '@/api'
import { handlers } from '@/mocks/handlers'
import { resetDb } from '@/mocks/db'
import { E, U } from '@/mocks/seed'
import { useSessionStore } from '@/stores/session'
import EventComments from '../EventComments.vue'

const server = setupServer(...handlers)

beforeAll(() => server.listen({ onUnhandledRequest: 'error' }))
beforeEach(() => {
  resetDb()
  setActivePinia(createPinia())
})
afterAll(() => server.close())

async function open(eventId: number, as?: number) {
  if (as) await useSessionStore().loginDemo(as)
  const event = await eventsApi.get(eventId)
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/', component: { template: '<div />' } },
      { path: '/login', name: 'login', component: { template: '<div />' } },
    ],
  })
  await router.push('/')
  const wrapper = mount(EventComments, { props: { event }, global: { plugins: [router] } })
  await flushPromises()
  return wrapper
}

const button = (w: Awaited<ReturnType<typeof open>>, text: string) =>
  w.findAll('button').find((b) => b.text() === text)!

describe('EventComments', () => {
  it('С5: вопрос участника и ответ организатора различимы по авторству', async () => {
    const asker = await open(E.yoga, U.petr)
    await asker.get('textarea').setValue('Коврик обязателен?')
    await asker.get('form').trigger('submit')
    await flushPromises()
    expect(asker.text()).toContain('Коврик обязателен?')
    asker.unmount()

    const organizer = await open(E.yoga, U.anna)
    await button(organizer, 'Ответить').trigger('click')
    await organizer.get('textarea').setValue('Можно плед')
    await organizer.get('form').trigger('submit')
    await flushPromises()
    const items = organizer.findAll('li li')
    expect(items).toHaveLength(1)
    expect(items[0]!.text()).toContain('Анна Смирнова')
    expect(items[0]!.text()).toContain('Организатор')
    expect(organizer.text()).toContain('2 сообщения')
  })

  it('владелец организации отвечает от её имени', async () => {
    const w = await open(E.runMorning, U.igor)
    expect(w.text()).toContain('Сообщение будет от имени организации')
    await w.get('textarea').setValue('Сбор у часов')
    await w.get('form').trigger('submit')
    await flushPromises()
    const items = w.findAll('ul > li')
    const last = items[items.length - 1]!
    expect(last.text()).toContain('Беговой клуб «Енисей»')
  })

  it('скрытое сообщение показывает заглушку, гостю предлагает войти', async () => {
    const w = await open(E.chessPark)
    expect(w.text()).toContain('Сообщение скрыто модератором')
    expect(w.text()).not.toContain('абонементы')
    expect(w.find('textarea').exists()).toBe(false)
    expect(w.text()).toContain('чтобы задать вопрос')
  })

  it('не отправляет пустое сообщение', async () => {
    const w = await open(E.yoga, U.petr)
    await w.get('form').trigger('submit')
    await flushPromises()
    expect(w.get('[role="alert"]').text()).toContain('Напишите сообщение')
  })
})
