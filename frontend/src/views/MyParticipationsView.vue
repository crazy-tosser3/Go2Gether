<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { ApiError, eventsApi, usersApi, type MyParticipation } from '@/api'
import SportIcon from '@/components/illustrations/SportIcon.vue'
import { sportIconName } from '@/components/illustrations/sportIcon'
import { StatusBadge, UiButton, UiChip, UiModal } from '@/components/ui'
import {
  MY_TABS,
  canCancel,
  groupMyParticipations,
  withParticipation,
  type MyTab,
} from '@/features/events/myParticipations'
import { formatEventWhen } from '@/utils/datetime'

const items = ref<MyParticipation[]>([])
const state = ref<'loading' | 'ready' | 'error'>('loading')
const message = ref('')
const tab = ref<MyTab>('upcoming')

const cancelling = ref<MyParticipation | null>(null)
const cancelOpen = computed({
  get: () => cancelling.value !== null,
  set: (v) => {
    if (!v) cancelling.value = null
  },
})
const busy = ref(false)
const actionError = ref('')

const groups = computed(() => groupMyParticipations(items.value))
const current = computed(() => groups.value[tab.value])

async function load() {
  state.value = 'loading'
  try {
    items.value = await usersApi.myParticipations()
    const g = groupMyParticipations(items.value)
    if (!g.upcoming.length) tab.value = MY_TABS.find((t) => g[t.value].length)?.value ?? 'upcoming'
    state.value = 'ready'
  } catch (e) {
    message.value = e instanceof ApiError ? e.message : 'Не получилось загрузить записи.'
    state.value = 'error'
  }
}

onMounted(load)

async function confirmCancel() {
  const item = cancelling.value
  if (!item) return
  busy.value = true
  actionError.value = ''
  try {
    const updated = await eventsApi.leave(item.event.id)
    items.value = withParticipation(items.value, updated)
    cancelling.value = null
  } catch (e) {
    cancelling.value = null
    actionError.value = e instanceof ApiError ? e.message : 'Не получилось отменить запись.'
  } finally {
    busy.value = false
  }
}

const EMPTY: Record<MyTab, string> = {
  upcoming: 'Предстоящих записей нет',
  pending: 'Заявок на рассмотрении нет',
  rejected: 'Отказов нет',
  archive: 'Архив пуст',
}
</script>

<template>
  <section class="flex flex-col gap-4" aria-live="polite">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <h1 class="text-[30px] lg:text-[42px]">Мои записи</h1>
      <RouterLink
        :to="{ name: 'my-events' }"
        class="font-semibold text-accent-ink underline underline-offset-4"
      >
        Созданные мной →
      </RouterLink>
    </div>

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

    <template v-else>
      <div class="flex gap-2 overflow-x-auto" role="group" aria-label="Вкладки">
        <UiChip
          v-for="t in MY_TABS"
          :key="t.value"
          :pressed="tab === t.value"
          :count="groups[t.value].length"
          @click="tab = t.value"
        >
          {{ t.label }}
        </UiChip>
      </div>

      <p v-if="actionError" class="font-semibold text-danger" role="alert">✕ {{ actionError }}</p>

      <div
        v-if="!current.length"
        class="flex flex-col items-center gap-3 rounded-card bg-card p-8 text-center"
      >
        <p class="text-lg font-bold">{{ EMPTY[tab] }}</p>
        <RouterLink
          v-if="tab === 'upcoming'"
          :to="{ name: 'events' }"
          class="rounded-full bg-ink px-6 py-3 font-bold text-white no-underline"
        >
          Найти мероприятие
        </RouterLink>
      </div>

      <ul v-else class="grid grid-cols-1 gap-3 lg:grid-cols-2">
        <li
          v-for="item in current"
          :key="item.participation.id"
          class="relative flex items-start gap-4 rounded-card p-4"
          :class="tab === 'archive' ? 'bg-card/60' : 'bg-card'"
        >
          <span class="grid size-14 shrink-0 place-items-center rounded-full bg-accent">
            <SportIcon :name="sportIconName(item.event.sport)" :size="40" />
          </span>
          <div class="flex min-w-0 flex-1 flex-col gap-1">
            <p class="font-semibold text-accent-ink">{{ formatEventWhen(item.event.starts_at) }}</p>
            <h2 class="line-clamp-2 text-xl leading-tight font-extrabold normal-case">
              <RouterLink
                :to="{ name: 'event', params: { id: item.event.id } }"
                class="text-ink no-underline hover:underline"
              >
                {{ item.event.title }}
              </RouterLink>
            </h2>
            <div class="flex flex-wrap items-center gap-2">
              <StatusBadge
                v-if="item.event.status !== 'published'"
                :of="{ kind: 'event', status: item.event.status }"
              />
              <StatusBadge
                v-else
                :of="{ kind: 'participation', status: item.participation.status }"
              />
            </div>
            <p v-if="item.event.unavailable_reason" class="text-ink-2">
              {{ item.event.unavailable_reason }}
            </p>
            <p
              v-else-if="
                item.participation.status === 'rejected' && item.participation.decision_reason
              "
              class="text-ink-2"
            >
              {{ item.participation.decision_reason }}
            </p>
            <button
              v-if="canCancel(item)"
              type="button"
              class="inline-flex min-h-8 items-center mt-1 w-fit text-sm font-semibold text-ink-2 underline underline-offset-4 hover:text-ink"
              @click="cancelling = item"
            >
              {{
                item.participation.status === 'confirmed' ? 'Отменить запись' : 'Отозвать заявку'
              }}
            </button>
          </div>
        </li>
      </ul>
    </template>

    <UiModal
      v-model:open="cancelOpen"
      :title="`Отменить запись на «${cancelling?.event.title ?? ''}»?`"
      description="Место освободится для других. Записаться снова можно, пока есть места."
    >
      <template #actions>
        <UiButton variant="ghost" @click="cancelOpen = false">Оставить</UiButton>
        <UiButton :loading="busy" @click="confirmCancel">Отменить запись</UiButton>
      </template>
    </UiModal>
  </section>
</template>
