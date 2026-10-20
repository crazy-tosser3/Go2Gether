<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { ApiError, usersApi, type PublicUser } from '@/api'
import EventListCard from '@/components/events/EventListCard.vue'
import { UiAvatar, UiButton } from '@/components/ui'
import { LEVEL_LABELS } from '@/features/events/filters'
import { APP_NAME } from '@/router/access'
import { useSessionStore } from '@/stores/session'

const route = useRoute()
const session = useSessionStore()
const id = computed(() => Number(route.params.id))

const person = ref<PublicUser | null>(null)
const state = ref<'loading' | 'ready' | 'not-found' | 'error'>('loading')
const message = ref('')
const isMe = computed(() => session.user?.id === id.value)

async function load() {
  state.value = 'loading'
  try {
    person.value = await usersApi.get(id.value)
    document.title = `${person.value.name} — ${APP_NAME}`
    state.value = 'ready'
  } catch (e) {
    person.value = null
    message.value = e instanceof ApiError ? e.message : 'Не получилось загрузить профиль.'
    state.value =
      !Number.isInteger(id.value) || (e instanceof ApiError && e.status === 404)
        ? 'not-found'
        : 'error'
  }
}

watch(id, load, { immediate: true })
</script>

<template>
  <div aria-live="polite" :aria-busy="state === 'loading'">
    <div v-if="state === 'loading'" class="flex flex-col gap-4" aria-label="Загрузка">
      <div class="h-40 animate-pulse rounded-card bg-mute" />
      <div class="h-28 animate-pulse rounded-card bg-card" />
    </div>

    <section
      v-else-if="state === 'not-found'"
      class="mx-auto flex max-w-md flex-col items-center gap-3 rounded-card bg-card p-8 text-center"
    >
      <h1 class="text-[30px] lg:text-[42px]">Пользователь не найден</h1>
      <RouterLink :to="{ name: 'events' }" class="font-bold underline underline-offset-4">
        К мероприятиям
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

    <article v-else-if="person" class="mx-auto flex max-w-4xl flex-col gap-5">
      <header class="flex flex-col gap-4 rounded-card bg-accent p-6 sm:flex-row sm:items-center">
        <UiAvatar :name="person.name" :url="person.avatar_url" size="xl" />
        <div class="flex min-w-0 flex-1 flex-col gap-1">
          <h1 class="text-[30px] lg:text-[42px]">{{ person.name }}</h1>
          <p class="text-accent-ink">
            {{ person.city || 'Город не указан' }}
            <template v-if="person.level"> · {{ LEVEL_LABELS[person.level] }}</template>
          </p>
        </div>
        <RouterLink
          v-if="isMe"
          :to="{ name: 'profile' }"
          class="self-start rounded-full bg-ink px-5 py-3 font-bold text-white no-underline sm:self-center"
        >
          Изменить профиль
        </RouterLink>
      </header>

      <section
        v-if="person.bio || person.sports.length"
        class="flex flex-col gap-3 rounded-card bg-card p-5"
      >
        <p v-if="person.bio" class="text-lg whitespace-pre-line">{{ person.bio }}</p>
        <div v-if="person.sports.length" class="flex flex-wrap gap-2" aria-label="Интересы">
          <span
            v-for="s in person.sports"
            :key="s.id"
            class="rounded-full bg-field px-4 py-2 font-semibold"
          >
            {{ s.name }}
          </span>
        </div>
      </section>

      <section class="flex flex-col gap-3" aria-labelledby="user-events">
        <h2 id="user-events" class="text-xl font-bold">
          Организует<template v-if="person.created_events.length">
            · {{ person.created_events.length }}</template
          >
        </h2>
        <p
          v-if="!person.created_events.length"
          class="rounded-card bg-card p-6 text-center text-ink-2"
        >
          Пока ничего не организует
        </p>
        <ul v-else class="grid grid-cols-1 gap-3 lg:grid-cols-2">
          <li v-for="e in person.created_events" :key="e.id"><EventListCard :event="e" /></li>
        </ul>
      </section>
    </article>
  </div>
</template>
