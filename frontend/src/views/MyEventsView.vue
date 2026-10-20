<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { ApiError, usersApi, type OrganizerEvent } from '@/api'
import SportIcon from '@/components/illustrations/SportIcon.vue'
import { sportIconName } from '@/components/illustrations/sportIcon'
import { StatusBadge, UiBadge, UiButton, plural, seatsShort } from '@/components/ui'
import { formatEventWhen } from '@/utils/datetime'

const items = ref<OrganizerEvent[]>([])
const state = ref<'loading' | 'ready' | 'error'>('loading')
const message = ref('')

async function load() {
  state.value = 'loading'
  try {
    const list = await usersApi.myEvents()
    // Сначала те, где ждут ответа, потом по дате.
    items.value = [...list].sort(
      (a, b) =>
        Number(b.pending_count > 0) - Number(a.pending_count > 0) ||
        a.event.starts_at.localeCompare(b.event.starts_at),
    )
    state.value = 'ready'
  } catch (e) {
    message.value = e instanceof ApiError ? e.message : 'Не получилось загрузить мероприятия.'
    state.value = 'error'
  }
}

onMounted(load)

// Архив — прошедшие, снятые и отменённые: их можно повторить, заполнив форму заново.
const upcoming = computed(() =>
  items.value.filter((i) => i.event.status === 'published' && !i.event.is_past),
)
const archive = computed(() =>
  items.value.filter((i) => !(i.event.status === 'published' && !i.event.is_past)),
)
const sections = computed(() =>
  [
    { key: 'upcoming', title: 'Предстоящие', list: upcoming.value, repeat: false },
    { key: 'archive', title: 'Архив', list: archive.value, repeat: true },
  ].filter((s) => s.list.length),
)
</script>

<template>
  <section class="flex flex-col gap-4" aria-live="polite">
    <h1 class="text-[30px] lg:text-[42px]">Мои мероприятия</h1>

    <div v-if="state === 'loading'" class="flex flex-col gap-3" aria-label="Загрузка">
      <div v-for="i in 3" :key="i" class="h-24 animate-pulse rounded-card bg-card" />
    </div>

    <div
      v-else-if="state === 'error'"
      class="flex flex-col items-center gap-3 rounded-card bg-card p-6"
      role="alert"
    >
      <p class="font-bold">{{ message }}</p>
      <UiButton variant="ghost" class="!w-auto" @click="load">Повторить</UiButton>
    </div>

    <div
      v-else-if="!items.length"
      class="flex flex-col items-center gap-3 rounded-card bg-accent p-8 text-center"
    >
      <p class="text-xl font-bold">Вы ещё ничего не создали</p>
      <RouterLink
        :to="{ name: 'event-create' }"
        class="rounded-full bg-ink px-6 py-3 font-bold text-white no-underline"
      >
        Создать мероприятие
      </RouterLink>
    </div>

    <template v-else>
      <section
        v-for="section in sections"
        :key="section.key"
        class="flex flex-col gap-3"
        :aria-labelledby="`my-${section.key}`"
      >
        <h2 :id="`my-${section.key}`" class="text-xl font-bold">{{ section.title }}</h2>
        <ul class="grid grid-cols-1 gap-3 lg:grid-cols-2">
          <li
            v-for="{ event, pending_count } in section.list"
            :key="event.id"
            class="relative flex items-center gap-4 rounded-card bg-card p-4 focus-within:outline-3 focus-within:outline-offset-2 focus-within:outline-accent-ink hover:shadow-raised"
          >
            <span class="grid size-14 shrink-0 place-items-center rounded-full bg-accent">
              <SportIcon :name="sportIconName(event.sport)" :size="40" />
            </span>
            <div class="flex min-w-0 flex-1 flex-col gap-0.5">
              <p class="font-semibold text-accent-ink">{{ formatEventWhen(event.starts_at) }}</p>
              <h3 class="line-clamp-2 font-display text-xl leading-tight font-extrabold">
                <RouterLink
                  :to="{ name: 'event-manage', params: { id: event.id } }"
                  class="text-ink no-underline outline-none after:absolute after:inset-0 after:content-['']"
                >
                  {{ event.title }}
                </RouterLink>
              </h3>
              <p class="truncate text-ink-2">
                {{ seatsShort(event.confirmed_count, event.capacity) }}
              </p>
              <StatusBadge
                v-if="event.status !== 'published'"
                :of="{ kind: 'event', status: event.status }"
                class="mt-1"
              />
              <UiBadge v-else-if="pending_count" tone="wait" class="mt-1">
                {{ pending_count }} {{ plural(pending_count, ['заявка', 'заявки', 'заявок']) }}
              </UiBadge>
            </div>
            <RouterLink
              v-if="section.repeat"
              :to="{ name: 'event-create', query: { repeat: event.id } }"
              class="relative z-10 inline-flex min-h-11 shrink-0 items-center rounded-full bg-field px-4 font-bold text-ink no-underline hover:bg-mute"
              :aria-label="`Повторить: ${event.title}`"
            >
              Повторить
            </RouterLink>
          </li>
        </ul>
      </section>
    </template>
  </section>
</template>
