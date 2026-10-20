import { watch } from 'vue'
import {
  createRouter,
  createWebHistory,
  type RouteLocationNormalizedLoaded,
  type RouteRecordRaw,
} from 'vue-router'
import { useSessionStore } from '@/stores/session'
import HomeView from '@/views/HomeView.vue'
import { i18n, t } from '@/i18n'
import { documentTitle, resolveAccess } from './access'

const routes: RouteRecordRaw[] = [
  { path: '/', name: 'home', component: HomeView },
  {
    path: '/events',
    name: 'events',
    component: () => import('@/views/EventsView.vue'),
    meta: { title: 'Мероприятия' },
  },
  {
    path: '/events/new',
    name: 'event-create',
    component: () => import('@/views/EventFormView.vue'),
    meta: { title: 'Новое мероприятие', requiresAuth: true, noAdmin: true },
  },
  {
    path: '/events/:id/edit',
    name: 'event-edit',
    component: () => import('@/views/EventFormView.vue'),
    meta: { title: 'Изменить мероприятие', requiresAuth: true, noAdmin: true },
  },
  {
    path: '/events/:id/manage',
    name: 'event-manage',
    component: () => import('@/views/EventManageView.vue'),
    meta: { title: 'Заявки и участники', requiresAuth: true },
  },
  {
    path: '/events/:id',
    name: 'event',
    component: () => import('@/views/EventView.vue'),
    meta: { title: 'Мероприятие' },
  },
  {
    path: '/me/events',
    name: 'my-events',
    component: () => import('@/views/MyEventsView.vue'),
    meta: { title: 'Мои мероприятия', requiresAuth: true },
  },
  {
    path: '/me',
    name: 'my-participations',
    component: () => import('@/views/MyParticipationsView.vue'),
    meta: { title: 'Мои записи', requiresAuth: true },
  },
  {
    path: '/onboarding',
    name: 'onboarding',
    component: () => import('@/views/OnboardingView.vue'),
    meta: { title: 'Настройка подбора', requiresAuth: true, noAdmin: true },
  },
  {
    path: '/profile',
    name: 'profile',
    component: () => import('@/views/ProfileView.vue'),
    meta: { title: 'Профиль', requiresAuth: true },
  },
  {
    path: '/users/:id',
    name: 'user',
    component: () => import('@/views/UserView.vue'),
    meta: { title: 'Профиль' },
  },
  {
    path: '/organizations',
    name: 'organizations',
    component: () => import('@/views/OrganizationsView.vue'),
    meta: { title: 'Организации' },
  },
  {
    path: '/organizations/my',
    name: 'my-organization',
    component: () => import('@/views/MyOrganizationsView.vue'),
    meta: { title: 'Моя организация', requiresAuth: true },
  },
  {
    path: '/organizations/:id',
    name: 'organization',
    component: () => import('@/views/OrganizationView.vue'),
    meta: { title: 'Организация' },
  },
  {
    path: '/admin',
    meta: { adminOnly: true },
    children: [
      {
        path: '',
        name: 'admin',
        component: () => import('@/views/admin/AdminDashboardView.vue'),
        meta: { title: 'Админка' },
      },
      {
        path: 'organizations',
        name: 'admin-organizations',
        component: () => import('@/views/admin/AdminOrganizationsView.vue'),
        meta: { title: 'Админка: организации' },
      },
      {
        path: 'events',
        name: 'admin-events',
        component: () => import('@/views/admin/AdminEventsView.vue'),
        meta: { title: 'Админка: мероприятия' },
      },
      {
        path: 'reports',
        name: 'admin-reports',
        component: () => import('@/views/admin/AdminReportsView.vue'),
        meta: { title: 'Админка: жалобы' },
      },
      {
        path: 'safety',
        name: 'admin-safety',
        component: () => import('@/views/admin/AdminSafetyView.vue'),
        meta: { title: 'Админка: безопасность' },
      },
      {
        path: 'safety/new',
        name: 'admin-safety-new',
        component: () => import('@/views/admin/AdminSafetyEditView.vue'),
        meta: { title: 'Новая памятка' },
      },
      {
        path: 'safety/:id',
        name: 'admin-safety-edit',
        component: () => import('@/views/admin/AdminSafetyEditView.vue'),
        meta: { title: 'Памятка' },
      },
    ],
  },
  {
    path: '/login',
    name: 'login',
    component: () => import('@/views/LoginView.vue'),
    meta: { title: 'Вход' },
  },
  {
    path: '/register',
    name: 'register',
    component: () => import('@/views/RegisterView.vue'),
    meta: { title: 'Регистрация', guestOnly: true },
  },
  {
    path: '/privacy',
    name: 'privacy',
    component: () => import('@/views/PrivacyView.vue'),
    meta: { title: 'Политика обработки персональных данных' },
  },
  {
    path: '/forbidden',
    name: 'forbidden',
    component: () => import('@/views/ForbiddenView.vue'),
    meta: { title: 'Нет доступа' },
  },
  {
    path: '/ui-kit',
    name: 'ui-kit',
    component: () => import('@/views/UiKitView.vue'),
    meta: { title: 'UI-кит' },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'not-found',
    component: () => import('@/views/NotFoundView.vue'),
    meta: { title: 'Страница не найдена' },
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  // Смена фильтров в адресе на той же странице не должна уводить к началу.
  scrollBehavior: (to, from, saved) => saved ?? (to.path === from.path ? false : { top: 0 }),
})

router.beforeEach(async (to) => {
  const session = useSessionStore()
  await session.restore()
  return resolveAccess(to, { isAuthenticated: session.isAuthenticated, isAdmin: session.isAdmin })
})

// Заголовок вкладки — из словаря по имени маршрута; meta.title остаётся запасным.
function setTitle(to: RouteLocationNormalizedLoaded) {
  const key = `routes.${String(to.name)}`
  document.title = documentTitle(i18n.global.te(key) ? t(key) : to.meta.title)
}

router.afterEach(setTitle)

// Сменили язык — переводим и заголовок. Страницы с названием из данных (мероприятие, организация,
// профиль) ставят его сами и на смену языка не реагируют.
watch(i18n.global.locale, () => {
  const to = router.currentRoute.value
  if (!['event', 'organization', 'user'].includes(String(to.name))) setTitle(to)
})

export default router
