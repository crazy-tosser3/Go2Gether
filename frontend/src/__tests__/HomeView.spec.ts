import { describe, it, expect } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia } from 'pinia'
import { createMemoryHistory, createRouter } from 'vue-router'
import HomeView from '@/views/HomeView.vue'

describe('HomeView', () => {
  it('показывает заголовок и ведёт к мероприятиям', async () => {
    const router = createRouter({
      history: createMemoryHistory(),
      routes: [
        { path: '/', component: HomeView },
        { path: '/events', name: 'events', component: { template: '<div />' } },
        { path: '/organizations', name: 'organizations', component: { template: '<div />' } },
      ],
    })
    const wrapper = mount(HomeView, { global: { plugins: [router, createPinia()] } })
    expect(wrapper.get('h1').text()).toBe('Найдите компанию для спорта')
    expect(wrapper.get('a').attributes('href')).toBe('/events')
    // Гостю подборку не показываем — подбирать не по чему.
    expect(wrapper.text()).not.toContain('Для вас')
  })
})
