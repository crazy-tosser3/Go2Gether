<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { ApiError, eventsApi, participationsApi, type EventDetails, type Participant } from '@/api'
import {
  UiAvatar,
  UiBadge,
  UiButton,
  UiChip,
  UiModal,
  UiSeatsRing,
  UiTextArea,
  plural,
} from '@/components/ui'
import {
  REJECT_REASONS,
  applyDecision,
  emptyGroups,
  groupParticipants,
  type ParticipantGroups,
} from '@/features/events/organizer'
import { formatEventWhen } from '@/utils/datetime'

const route = useRoute()
const id = computed(() => Number(route.params.id))

const event = ref<EventDetails | null>(null)
const groups = ref<ParticipantGroups>(emptyGroups())
const state = ref<'loading' | 'ready' | 'forbidden' | 'not-found' | 'error'>('loading')
const message = ref('')
const tab = ref<'pending' | 'confirmed' | 'rejected'>('pending')

const busyId = ref<number | null>(null)
const rowError = ref<{ id: number; text: string } | null>(null)

const rejecting = ref<Participant | null>(null)
const rejectOpen = computed({
  get: () => rejecting.value !== null,
  set: (v) => {
    if (!v) rejecting.value = null
  },
})
const reasonPreset = ref<string>('')
const reasonText = ref('')

async function load() {
  state.value = 'loading'
  try {
    const details = await eventsApi.get(id.value)
    event.value = details
    if (!details.is_organizer) {
      state.value = 'forbidden'
      return
    }
    const [confirmed, pending, waitlist, rejected] = await Promise.all([
      eventsApi.participants(id.value),
      eventsApi.requests(id.value, 'pending'),
      eventsApi.requests(id.value, 'waitlist'),
      eventsApi.requests(id.value, 'rejected'),
    ])
    groups.value = groupParticipants([
      ...confirmed.participants,
      ...pending,
      ...waitlist,
      ...rejected,
    ])
    if (!groups.value.pending.length && groups.value.confirmed.length) tab.value = 'confirmed'
    state.value = 'ready'
  } catch (e) {
    message.value = e instanceof ApiError ? e.message : 'Не получилось загрузить данные.'
    state.value =
      e instanceof ApiError && (e.status === 404 || e.isGone)
        ? 'not-found'
        : e instanceof ApiError && e.status === 403
          ? 'forbidden'
          : 'error'
  }
}

watch(id, load, { immediate: true })

const when = computed(() =>
  event.value ? formatEventWhen(event.value.starts_at, event.value.ends_at) : '',
)
const closed = computed(() => event.value !== null && event.value.status !== 'published')
const confirmedCount = computed(() => groups.value.confirmed.length)

async function decide(p: Participant, action: 'approve' | 'reject', reason?: string) {
  busyId.value = p.participation_id
  rowError.value = null
  try {
    const result =
      action === 'approve'
        ? await participationsApi.approve(p.participation_id)
        : await participationsApi.reject(p.participation_id, reason)
    groups.value = applyDecision(groups.value, p.participation_id, result)
  } catch (e) {
    rowError.value = {
      id: p.participation_id,
      text: e instanceof ApiError ? e.message : 'Не получилось. Попробуйте ещё раз.',
    }
  } finally {
    busyId.value = null
  }
}

function openReject(p: Participant) {
  reasonPreset.value = ''
  reasonText.value = ''
  rejecting.value = p
}

async function confirmReject() {
  const p = rejecting.value
  if (!p) return
  const reason = [reasonPreset.value, reasonText.value.trim()].filter(Boolean).join('. ')
  rejecting.value = null
  await decide(p, 'reject', reason || undefined)
}

const shortDate = new Intl.DateTimeFormat('ru-RU', {
  day: 'numeric',
  month: 'short',
  hour: '2-digit',
  minute: '2-digit',
})
</script>

<template>
  <div
    aria-live="polite"
    :aria-busy="state === 'loading'"
    class="mx-auto flex max-w-3xl flex-col gap-4"
  >
    <div v-if="state === 'loading'" class="flex flex-col gap-4" aria-label="Загрузка">
      <div class="h-40 animate-pulse rounded-card bg-mute" />
      <div class="h-24 animate-pulse rounded-card bg-card" />
    </div>

    <section
      v-else-if="state !== 'ready'"
      class="flex flex-col items-center gap-3 rounded-card bg-card p-6 text-center"
      role="alert"
    >
      <p class="text-lg font-bold">
        {{
          state === 'forbidden'
            ? 'Управлять может только организатор'
            : state === 'not-found'
              ? 'Мероприятие не найдено'
              : 'Не получилось загрузить'
        }}
      </p>
      <p v-if="state === 'error'" class="text-ink-2">{{ message }}</p>
      <UiButton v-if="state === 'error'" variant="ghost" class="!w-auto" @click="load">
        Повторить
      </UiButton>
      <RouterLink v-else :to="{ name: 'my-events' }" class="font-bold underline underline-offset-4">
        Мои мероприятия
      </RouterLink>
    </section>

    <template v-else-if="event">
      <header class="flex items-center gap-4 rounded-card bg-accent p-5">
        <div class="flex min-w-0 flex-1 flex-col gap-1">
          <RouterLink
            :to="{ name: 'event', params: { id: event.id } }"
            class="inline-flex min-h-8 items-center w-fit text-sm font-semibold text-accent-ink underline underline-offset-4"
          >
            ← К мероприятию
          </RouterLink>
          <h1 class="text-[30px] lg:text-[42px]">{{ event.title }}</h1>
          <p class="font-semibold text-accent-ink">{{ when }}</p>
        </div>
        <UiSeatsRing :taken="confirmedCount" :capacity="event.capacity" :size="64" />
      </header>

      <p
        v-if="closed"
        class="rounded-card bg-warning-bg p-4 font-semibold text-warning-fg"
        role="status"
      >
        Снято с публикации{{ event.unavailable_reason ? `: ${event.unavailable_reason}` : '' }}.
        Новые записи закрыты.
      </p>

      <div class="flex gap-2 overflow-x-auto" role="group" aria-label="Списки">
        <UiChip
          :pressed="tab === 'pending'"
          :count="groups.pending.length"
          @click="tab = 'pending'"
        >
          Заявки
        </UiChip>
        <UiChip
          :pressed="tab === 'confirmed'"
          :count="groups.confirmed.length"
          @click="tab = 'confirmed'"
        >
          Участники
        </UiChip>
        <UiChip
          :pressed="tab === 'rejected'"
          :count="groups.rejected.length"
          @click="tab = 'rejected'"
        >
          Отказы
        </UiChip>
      </div>

      <ul v-if="tab === 'pending'" class="flex flex-col gap-3">
        <li v-if="!groups.pending.length" class="rounded-card bg-card p-6 text-center text-ink-2">
          Новых заявок нет
        </li>
        <li
          v-for="p in groups.pending"
          :key="p.participation_id"
          class="flex flex-col gap-3 rounded-card bg-card p-4"
        >
          <div class="flex items-center gap-3">
            <UiAvatar :name="p.user.name" :url="p.user.avatar_url" size="lg" />
            <div class="min-w-0 flex-1">
              <p class="truncate font-bold">{{ p.user.name }}</p>
              <p class="text-sm text-ink-2">{{ shortDate.format(new Date(p.created_at)) }}</p>
            </div>
            <UiBadge v-if="p.status === 'waitlist'" tone="wait">Лист ожидания</UiBadge>
          </div>
          <p v-if="p.message" class="rounded-field bg-field px-4 py-2.5">«{{ p.message }}»</p>
          <div class="flex gap-2">
            <UiButton
              class="!min-h-11"
              :loading="busyId === p.participation_id"
              :disabled="busyId !== null || closed"
              @click="decide(p, 'approve')"
            >
              Принять
            </UiButton>
            <UiButton
              variant="ghost"
              class="!min-h-11"
              :disabled="busyId !== null || closed"
              @click="openReject(p)"
            >
              Отклонить
            </UiButton>
          </div>
          <p
            v-if="rowError?.id === p.participation_id"
            class="font-semibold text-danger"
            role="alert"
          >
            ✕ {{ rowError.text }}
          </p>
        </li>
      </ul>

      <ul v-else-if="tab === 'confirmed'" class="flex flex-col gap-2">
        <li v-if="!groups.confirmed.length" class="rounded-card bg-card p-6 text-center text-ink-2">
          Пока никто не записался
        </li>
        <li
          v-for="p in groups.confirmed"
          :key="p.participation_id"
          class="flex items-center gap-3 rounded-full bg-card py-2.5 pr-5 pl-2.5"
        >
          <UiAvatar :name="p.user.name" :url="p.user.avatar_url" size="lg" />
          <div class="min-w-0 flex-1">
            <p class="truncate font-bold">{{ p.user.name }}</p>
            <p v-if="p.email" class="truncate text-sm text-ink-2">{{ p.email }}</p>
          </div>
          <UiBadge tone="ok">Участник</UiBadge>
        </li>
        <li v-if="groups.confirmed.length" class="px-2 text-sm text-ink-2">
          {{ groups.confirmed.length }}
          {{ plural(groups.confirmed.length, ['участник', 'участника', 'участников']) }}
        </li>
      </ul>

      <ul v-else class="flex flex-col gap-2">
        <li v-if="!groups.rejected.length" class="rounded-card bg-card p-6 text-center text-ink-2">
          Отказов нет
        </li>
        <li
          v-for="p in groups.rejected"
          :key="p.participation_id"
          class="flex flex-col gap-1 rounded-card bg-card p-4"
        >
          <div class="flex items-center justify-between gap-3">
            <p class="truncate font-bold">{{ p.user.name }}</p>
            <UiBadge tone="no">Отклонена</UiBadge>
          </div>
          <p v-if="p.decision_reason" class="text-ink-2">{{ p.decision_reason }}</p>
        </li>
      </ul>
    </template>

    <UiModal
      v-model:open="rejectOpen"
      :title="`Отклонить заявку: ${rejecting?.user.name ?? ''}`"
      description="Человек увидит причину."
    >
      <div class="flex flex-wrap gap-2" role="group" aria-label="Причина">
        <UiChip
          v-for="r in REJECT_REASONS"
          :key="r"
          :pressed="reasonPreset === r"
          @click="reasonPreset = reasonPreset === r ? '' : r"
        >
          {{ r }}
        </UiChip>
      </div>
      <UiTextArea v-model="reasonText" label="Комментарий" :rows="3" :maxlength="300" />
      <template #actions>
        <UiButton variant="ghost" @click="rejectOpen = false">Отмена</UiButton>
        <UiButton @click="confirmReject">Отклонить</UiButton>
      </template>
    </UiModal>
  </div>
</template>
