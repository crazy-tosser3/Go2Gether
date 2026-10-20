import { afterAll, beforeAll, beforeEach, describe, expect, it } from 'vitest'
import { flushPromises, mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import { setupServer } from 'msw/node'
import { handlers } from '@/mocks/handlers'
import { resetDb } from '@/mocks/db'
import { E, U } from '@/mocks/seed'
import { useSessionStore } from '@/stores/session'
import EventView from '../EventView.vue'

const server = setupServer(...handlers)

beforeAll(() => {
  server.listen({ onUnhandledRequest: 'error' })
  // jsdom не умеет модальные <dialog>: подменяем, чтобы окно открывалось в тестах.
  HTMLDialogElement.prototype.showModal ??= function (this: HTMLDialogElement) {
    this.open = true
  }
  HTMLDialogElement.prototype.close ??= function (this: HTMLDialogElement) {
    this.open = false
  }
})
beforeEach(() => {
  resetDb()
  setActivePinia(createPinia())
})
afterAll(() => server.close())

async function open(id: number, as?: number) {
  if (as) await useSessionStore().loginDemo(as)
  const Stub = { template: '<div />' }
  const router = createRouter({
    history: createMemoryHistory(),
    routes: [
      { path: '/events/:id', name: 'event', component: EventView },
      { path: '/events/:id/manage', name: 'event-manage', component: Stub },
      { path: '/events/:id/edit', name: 'event-edit', component: Stub },
      { path: '/events', name: 'events', component: Stub },
      { path: '/login', name: 'login', component: Stub },
      { path: '/users/:id', name: 'user', component: Stub },
      { path: '/organizations/:id', name: 'organization', component: Stub },
    ],
  })
  await router.push(`/events/${id}`)
  const wrapper = mount(EventView, { global: { plugins: [router] }, attachTo: document.body })
  await flushPromises()
  return wrapper
}

const button = (wrapper: Awaited<ReturnType<typeof open>>, text: string) =>
  wrapper.findAll('button').find((b) => b.text().includes(text))!

describe('EventView', () => {
  it('С3: свободная запись — сразу участник, имя появляется в списке', async () => {
    const wrapper = await open(E.yoga, U.petr)
    expect(wrapper.get('h1').text()).toBe('Йога на траве')
    await button(wrapper, 'Записаться').trigger('click')
    await flushPromises()
    expect(wrapper.text()).toContain('Вы участник')
    expect(wrapper.text()).toContain('Пётр Иванов')
    wrapper.unmount()
  })

  it('С4: заявка с сообщением — ждёт решения, её можно отозвать', async () => {
    const wrapper = await open(E.runInterval, U.petr)
    await button(wrapper, 'Подать заявку').trigger('click')
    await flushPromises()
    await wrapper.get('textarea').setValue('Бегаю 5 км')
    await button(wrapper, 'Отправить заявку').trigger('click')
    await flushPromises()
    expect(wrapper.text()).toContain('Ждёт решения')

    await button(wrapper, 'Отозвать заявку').trigger('click')
    await flushPromises()
    await button(wrapper, 'Да, отменить').trigger('click')
    await flushPromises()
    expect(wrapper.text()).toContain('Подать заявку')
    wrapper.unmount()
  })

  it('показывает отказ с причиной и даёт подать снова', async () => {
    const { db } = await import('@/mocks/db')
    const p = db.participations.find((x) => x.id === 5)!
    Object.assign(p, { status: 'rejected', decision_reason: 'Состав уже набран' })
    const wrapper = await open(E.volleyball, U.anna)
    expect(wrapper.text()).toContain('Заявка отклонена')
    expect(wrapper.text()).toContain('Состав уже набран')
    expect(button(wrapper, 'Подать заявку снова')).toBeTruthy()
    wrapper.unmount()
  })

  it('С6: снятое мероприятие по прямой ссылке — недоступно, записаться нельзя', async () => {
    const wrapper = await open(E.cycling, U.igor)
    expect(wrapper.get('h1').text()).toBe('Мероприятие недоступно')
    expect(wrapper.text()).toContain('Не указано место встречи')
    expect(wrapper.findAll('button').some((b) => b.text().includes('Записаться'))).toBe(false)
    wrapper.unmount()
  })

  it('участник снятого мероприятия видит страницу с причиной, но записи и вопросов нет', async () => {
    const wrapper = await open(E.cycling, U.anna)
    expect(wrapper.get('h1').text()).toBe('Велопрогулка вдоль Енисея')
    expect(wrapper.get('[role="alert"]').text()).toContain('Не указано место встречи')
    expect(wrapper.text()).toContain('Запись закрыта')
    expect(wrapper.text()).toContain('Обсуждение закрыто')
    expect(wrapper.find('textarea').exists()).toBe(false)
    wrapper.unmount()
  })

  it('гостю предлагает войти, организатору — не записываться', async () => {
    const guest = await open(E.football)
    expect(guest.text()).toContain('Войти и записаться')
    guest.unmount()
    const organizer = await open(E.football, U.anna)
    expect(organizer.text()).toContain('Вы организатор этого мероприятия')
    organizer.unmount()
  })

  it('гость видит только город, адрес и карта — после входа', async () => {
    const guest = await open(E.football)
    expect(guest.text()).toContain('Точный адрес — после входа')
    expect(guest.text()).not.toContain('ул. Ленина')
    expect(guest.find('a[href^="/login"]').exists()).toBe(true)
    guest.unmount()
    const user = await open(E.football, U.petr)
    expect(user.text()).toContain('ул. Ленина')
    expect(user.text()).not.toContain('Точный адрес — после входа')
    user.unmount()
  })

  it('несуществующее мероприятие', async () => {
    const wrapper = await open(999999)
    expect(wrapper.get('h1').text()).toBe('Мероприятие не найдено')
    wrapper.unmount()
  })
})
