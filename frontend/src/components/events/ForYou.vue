<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { recommendationsApi, type Recommendation } from '@/api'
import EventListCard from '@/components/events/EventListCard.vue'
import { UiButton } from '@/components/ui'
import { reasonLabels } from '@/features/events/recommendations'
import { useSessionStore } from '@/stores/session'

const LIMIT = 4

const session = useSessionStore()
const items = ref<Recommendation[]>([])
const state = ref<'loading' | 'ready' | 'error'>('loading')

// Подбирает бэк: интересы, история записей, город, уровень, знакомые организаторы и люди.
// Пока в профиле нет интересов — честно называем подборку «Популярное» и просим рассказать о себе.
const emptyProfile = computed(() => !session.user?.sport_ids.length)

const cards = computed(() =>
  items.value.map((r) => ({ event: r.event, reasons: reasonLabels(r.reasons) })),
)

async function load() {
  state.value = 'loading'
  try {
    items.value = await recommendationsApi.events({ limit: LIMIT })
    state.value = 'ready'
  } catch {
    state.value = 'error'
  }
}

onMounted(load)
</script>

<template>
  <section class="flex flex-col gap-3" aria-labelledby="for-you-title" aria-live="polite">
    <div class="flex flex-wrap items-baseline justify-between gap-2">
      <h2 id="for-you-title" class="font-display text-2xl uppercase">
        {{ emptyProfile ? $t('forYou.popular') : $t('forYou.title') }}
      </h2>
      <RouterLink
        :to="{ name: 'events' }"
        class="inline-flex min-h-8 items-center font-semibold underline underline-offset-4"
      >
        {{ $t('forYou.all') }}
      </RouterLink>
    </div>

    <RouterLink
      v-if="emptyProfile"
      :to="{ name: 'profile' }"
      class="flex items-center justify-between gap-3 rounded-card bg-lilac p-4 font-semibold text-ink no-underline"
    >
      {{ $t('forYou.fillProfile') }}
      <span aria-hidden="true">→</span>
    </RouterLink>

    <ul
      v-if="state === 'loading'"
      class="grid grid-cols-[minmax(0,1fr)] gap-3 md:grid-cols-2"
      :aria-label="$t('common.loading')"
    >
      <li v-for="i in 2" :key="i" class="h-64 animate-pulse rounded-card bg-card" />
    </ul>
    <div
      v-else-if="state === 'error'"
      class="flex flex-col items-start gap-2 rounded-card bg-card p-4"
      role="alert"
    >
      <p class="font-semibold">{{ $t('forYou.error') }}</p>
      <UiButton variant="ghost" class="!w-auto" @click="load">{{ $t('common.retry') }}</UiButton>
    </div>
    <p v-else-if="!cards.length" class="rounded-card bg-card p-4 text-ink-2">
      {{ $t('forYou.empty') }}
      <RouterLink
        :to="{ name: 'event-create' }"
        class="font-semibold text-ink underline underline-offset-4"
      >
        {{ $t('forYou.create') }}</RouterLink
      >.
    </p>
    <ul
      v-else
      class="grid grid-cols-[minmax(0,1fr)] gap-3 md:grid-cols-2 xl:grid-cols-3 min-[1800px]:grid-cols-4"
    >
      <li v-for="{ event, reasons } in cards" :key="event.id" class="flex min-w-0 flex-col gap-1.5">
        <EventListCard :event="event" :reasons="reasons" />
      </li>
    </ul>
  </section>
</template>
