<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { ApiError, organizationsApi, type EventSummary, type Organization } from '@/api'
import EventListCard from '@/components/events/EventListCard.vue'
import NavIcon from '@/components/layout/NavIcon.vue'
import { StatusBadge, UiButton } from '@/components/ui'
import { ORG_TYPE_LABELS } from '@/features/admin/organizations'
import { safeHref, shortHost } from '@/features/organizations/catalog'
import { APP_NAME } from '@/router/access'
import { useSessionStore } from '@/stores/session'

const route = useRoute()
const session = useSessionStore()
const id = computed(() => Number(route.params.id))

const org = ref<Organization | null>(null)
const events = ref<EventSummary[]>([])
const state = ref<'loading' | 'ready' | 'not-found' | 'error'>('loading')
const eventsFailed = ref(false)
const message = ref('')

async function load() {
  state.value = 'loading'
  eventsFailed.value = false
  try {
    org.value = await organizationsApi.get(id.value)
    document.title = `${org.value.name} — ${APP_NAME}`
    state.value = 'ready'
  } catch (e) {
    org.value = null
    message.value = e instanceof ApiError ? e.message : 'Не получилось загрузить организацию.'
    state.value =
      !Number.isInteger(id.value) || (e instanceof ApiError && e.status === 404)
        ? 'not-found'
        : 'error'
    return
  }
  // Мероприятия есть только у допущенной; владелец видит свою страницу и до допуска.
  if (org.value.status !== 'approved') {
    events.value = []
    return
  }
  try {
    events.value = await organizationsApi.events(id.value)
  } catch {
    eventsFailed.value = true
  }
}

watch(id, load, { immediate: true })

const website = computed(() => safeHref(org.value?.website_url))
</script>

<template>
  <div aria-live="polite" :aria-busy="state === 'loading'">
    <div v-if="state === 'loading'" class="flex flex-col gap-4" aria-label="Загрузка">
      <div class="h-48 animate-pulse rounded-card bg-mute" />
      <div class="h-28 animate-pulse rounded-card bg-card" />
    </div>

    <section
      v-else-if="state === 'not-found'"
      class="mx-auto flex max-w-md flex-col items-center gap-3 rounded-card bg-card p-8 text-center"
    >
      <h1 class="text-[30px] lg:text-[42px]">Страница не найдена</h1>
      <p class="text-ink-2">Организации нет или она ещё не прошла проверку.</p>
      <RouterLink :to="{ name: 'organizations' }" class="font-bold underline underline-offset-4">
        Все организации
      </RouterLink>
    </section>

    <section
      v-else-if="state === 'error'"
      class="mx-auto flex max-w-md flex-col items-center gap-3 rounded-card bg-card p-6 text-center"
      role="alert"
    >
      <p class="text-lg font-bold">{{ message }}</p>
      <UiButton variant="ghost" class="!w-auto" @click="load">Повторить</UiButton>
    </section>

    <article
      v-else-if="org"
      class="grid grid-cols-[minmax(0,1fr)] gap-5 lg:grid-cols-[minmax(0,1fr)_360px]"
    >
      <div class="flex min-w-0 flex-col gap-5">
        <header class="flex flex-col gap-3 rounded-card bg-accent p-6">
          <div class="flex items-center gap-3">
            <span class="grid size-14 flex-none place-items-center rounded-full bg-card">
              <NavIcon name="building" />
            </span>
            <p class="font-semibold text-accent-ink">
              {{ ORG_TYPE_LABELS[org.type] }}<template v-if="org.city"> · {{ org.city }}</template>
            </p>
          </div>
          <h1 class="text-[30px] lg:text-[42px]">{{ org.name }}</h1>
          <StatusBadge
            v-if="org.status !== 'approved'"
            :of="{ kind: 'organization', status: org.status }"
            class="!bg-card !text-ink"
          />
        </header>

        <p
          v-if="org.status !== 'approved'"
          class="rounded-card bg-warning-bg p-4 font-medium text-warning-fg"
          role="status"
        >
          <template v-if="session.isAdmin">
            Эту страницу видят только владелец и администраторы. Для всех она откроется после
            допуска.
          </template>
          <template v-else
            >Эту страницу видите только вы. Она откроется для всех после допуска.</template
          >
        </p>

        <p v-if="org.description" class="text-lg whitespace-pre-line">{{ org.description }}</p>

        <section class="flex flex-col gap-3" aria-labelledby="org-events">
          <h2 id="org-events" class="text-xl font-bold">
            Мероприятия<template v-if="events.length"> · {{ events.length }}</template>
          </h2>
          <p v-if="eventsFailed" class="rounded-card bg-card p-4" role="alert">
            Не получилось загрузить мероприятия.
            <button type="button" class="font-semibold underline" @click="load">Повторить</button>
          </p>
          <p v-else-if="!events.length" class="rounded-card bg-card p-6 text-center text-ink-2">
            Ближайших мероприятий нет
          </p>
          <ul v-else class="grid grid-cols-1 gap-3 xl:grid-cols-2">
            <li v-for="e in events" :key="e.id"><EventListCard :event="e" /></li>
          </ul>
        </section>
      </div>

      <aside class="flex flex-col gap-3 lg:sticky lg:top-6 lg:self-start">
        <dl class="flex flex-col gap-3 rounded-card bg-card p-5">
          <div v-if="org.sports_description">
            <dt class="text-sm text-ink-2">Направления</dt>
            <dd class="font-semibold">{{ org.sports_description }}</dd>
          </div>
          <div v-if="org.contacts">
            <dt class="text-sm text-ink-2">Контакты</dt>
            <dd class="font-semibold break-words select-all">{{ org.contacts }}</dd>
          </div>
          <div v-if="website">
            <dt class="text-sm text-ink-2">Сайт</dt>
            <dd>
              <a
                :href="website"
                target="_blank"
                rel="noopener noreferrer"
                class="font-semibold underline underline-offset-4"
              >
                {{ shortHost(website) }}
              </a>
            </dd>
          </div>
          <div>
            <dt class="text-sm text-ink-2">Представитель</dt>
            <dd class="font-semibold">
              <RouterLink
                :to="{ name: 'user', params: { id: org.owner.id } }"
                class="underline underline-offset-4"
              >
                {{ org.owner.name }}
              </RouterLink>
            </dd>
          </div>
        </dl>
      </aside>
    </article>
  </div>
</template>
