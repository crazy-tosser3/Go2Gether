<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { ApiError, authApi, type DemoAccount } from '@/api'
import { groupDemoAccounts } from '@/features/auth/demoRoles'
import { useSessionStore } from '@/stores/session'
import { safeRedirect } from '@/router/redirect'
import { UiButton, UiCard, UiTextField } from '@/components/ui'
import { t } from '@/i18n'

const session = useSessionStore()
const route = useRoute()
const router = useRouter()

const accounts = ref<DemoAccount[]>([])
const groups = computed(() => groupDemoAccounts(accounts.value))
const accountsState = ref<'loading' | 'ready' | 'error'>('loading')
const pendingDemo = ref<number | null>(null)
const demoError = ref('')

const email = ref('')
const password = ref('')
const formError = ref('')
const submitting = ref(false)

async function loadAccounts() {
  accountsState.value = 'loading'
  try {
    accounts.value = await authApi.demoAccounts()
    accountsState.value = 'ready'
  } catch {
    accountsState.value = 'error'
  }
}

async function goNext() {
  await router.replace(safeRedirect(route.query.redirect))
}

async function enterDemo(id: number) {
  pendingDemo.value = id
  demoError.value = ''
  try {
    await session.loginDemo(id)
    await goNext()
  } catch (error) {
    demoError.value = error instanceof ApiError ? error.message : t('login.error')
  } finally {
    pendingDemo.value = null
  }
}

async function submit() {
  formError.value = ''
  if (!email.value.trim() || !password.value) {
    formError.value = t('login.required')
    return
  }
  submitting.value = true
  try {
    await session.loginWithEmail(email.value.trim(), password.value)
    await goNext()
  } catch (error) {
    formError.value =
      error instanceof ApiError && error.status === 401
        ? t('login.wrong')
        : error instanceof ApiError
          ? error.message
          : t('login.error')
  } finally {
    submitting.value = false
  }
}

onMounted(loadAccounts)
</script>

<template>
  <section class="mx-auto flex max-w-md flex-col gap-6">
    <header class="flex flex-col gap-1 rounded-card bg-accent p-6">
      <p class="font-hand text-2xl text-accent-ink">{{ $t('home.slogan') }}</p>
      <h1 class="text-[30px] lg:text-[42px]">
        {{ session.user ? $t('common.switchRole') : $t('login.title') }}
      </h1>
      <p v-if="session.user" class="text-accent-ink">
        {{ $t('login.current', { name: session.user.name }) }}
      </p>
    </header>

    <section aria-labelledby="demo-title" class="flex flex-col gap-3">
      <h2 id="demo-title" class="text-lg font-bold">{{ $t('login.quick') }}</h2>

      <p v-if="accountsState === 'loading'" class="text-ink-2" role="status">
        {{ $t('login.loadingDemo') }}
      </p>
      <div v-else-if="accountsState === 'error'" class="flex flex-col gap-2" role="alert">
        <p class="text-ink-2">{{ $t('login.demoError') }}</p>
        <UiButton variant="ghost" @click="loadAccounts">{{ $t('common.retry') }}</UiButton>
      </div>
      <div v-else class="flex flex-col gap-5">
        <section
          v-for="group in groups"
          :key="group.key"
          class="flex flex-col gap-2.5"
          :aria-labelledby="`demo-${group.key}`"
        >
          <h3
            :id="`demo-${group.key}`"
            class="px-1 text-xs font-bold tracking-wider text-ink-2 uppercase"
          >
            {{ group.title }}
          </h3>
          <ul class="flex flex-col gap-2.5">
            <li v-for="account in group.accounts" :key="account.id">
              <UiCard class="!flex-row items-center gap-3 !py-3 !pr-3 !pl-5">
                <div class="min-w-0 flex-1">
                  <p class="font-bold">{{ account.name }}</p>
                  <p class="text-sm text-ink-2">{{ account.description }}</p>
                  <p v-if="account.scenarios" class="mt-1 text-sm">
                    <span class="font-semibold">{{ $t('login.check') }}</span>
                    {{ account.scenarios }}
                  </p>
                </div>
                <UiButton
                  class="!w-auto !min-h-11 shrink-0"
                  :loading="pendingDemo === account.id"
                  :disabled="pendingDemo !== null"
                  :aria-label="$t('login.enterAs', { name: account.name })"
                  @click="enterDemo(account.id)"
                >
                  {{ $t('common.login') }}
                </UiButton>
              </UiCard>
            </li>
          </ul>
        </section>
      </div>
      <p v-if="demoError" class="font-semibold text-danger" role="alert">✕ {{ demoError }}</p>
    </section>

    <form
      v-if="!session.user"
      class="flex flex-col gap-4 rounded-card bg-card p-5"
      novalidate
      aria-labelledby="email-title"
      @submit.prevent="submit"
    >
      <h2 id="email-title" class="text-lg font-bold">{{ $t('login.byEmail') }}</h2>
      <UiTextField
        v-model="email"
        :label="$t('login.email')"
        type="email"
        placeholder="anna@example.ru"
      />
      <UiTextField
        v-model="password"
        :label="$t('login.password')"
        type="password"
        :error="formError"
      />
      <UiButton type="submit" :loading="submitting">{{ $t('common.login') }}</UiButton>
      <p class="text-center">
        {{ $t('login.noAccount') }}
        <RouterLink
          :to="{ name: 'register' }"
          class="font-bold text-accent-ink underline underline-offset-4"
        >
          {{ $t('login.register') }}
        </RouterLink>
      </p>
    </form>
  </section>
</template>
