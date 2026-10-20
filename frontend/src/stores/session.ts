import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { ApiError, authApi, type Id, type OrganizationShort, type User } from '@/api'

export const useSessionStore = defineStore('session', () => {
  const user = ref<User | null>(null)
  // У владельца может быть несколько организаций в разных статусах.
  const organizations = ref<OrganizationShort[]>([])

  const isAuthenticated = computed(() => user.value !== null)
  const isAdmin = computed(() => user.value?.role === 'admin')
  const approvedOrganizations = computed(() =>
    organizations.value.filter((o) => o.status === 'approved'),
  )
  const hasApprovedOrganization = computed(() => approvedOrganizations.value.length > 0)

  async function refresh() {
    const me = await authApi.me()
    user.value = me.user
    organizations.value = me.organizations
  }

  let restoring: Promise<void> | null = null

  /**
   * Вход живёт в cookie, поэтому после перезагрузки страницы спрашиваем у бэка, кто мы.
   * Вызывается один раз до первой навигации; 401 — просто гость.
   */
  function restore() {
    restoring ??= refresh().catch((error: unknown) => {
      clear()
      if (!(error instanceof ApiError && error.isUnauthorized)) {
        console.warn('Сессия не восстановлена', error)
      }
    })
    return restoring
  }

  async function loginDemo(demoAccountId: Id) {
    await authApi.login({ demo_account_id: demoAccountId })
    await refresh()
  }

  async function register(input: {
    name: string
    email: string
    password: string
    consent: boolean
  }) {
    await authApi.register(input)
    await refresh()
  }

  async function loginWithEmail(email: string, password: string) {
    await authApi.login({ email, password })
    await refresh()
  }

  function clear() {
    user.value = null
    organizations.value = []
  }

  async function logout() {
    try {
      await authApi.logout()
    } finally {
      clear()
    }
  }

  return {
    user,
    organizations,
    isAuthenticated,
    isAdmin,
    approvedOrganizations,
    hasApprovedOrganization,
    refresh,
    restore,
    loginDemo,
    loginWithEmail,
    register,
    logout,
    clear,
  }
})
