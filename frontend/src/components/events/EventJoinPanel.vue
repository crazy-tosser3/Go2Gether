<script setup lang="ts">
import { computed, ref, useId } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { ApiError, eventsApi, type EventDetails } from '@/api'
import { useSessionStore } from '@/stores/session'
import { StatusBadge, UiButton, UiModal, UiTextArea } from '@/components/ui'
import { CANCEL_REASON_MAX, cancelReasonError } from '@/features/events/cancelEvent'
import { joinAction } from '@/features/events/participation'
import { formatEventWhen } from '@/utils/datetime'
import { t } from '@/i18n'

const props = defineProps<{ event: EventDetails }>()
const emit = defineEmits<{ changed: [] }>()

const session = useSessionStore()
const route = useRoute()

const action = computed(() => joinAction(props.event, session.user))
const mine = computed(() => props.event.my_participation)

const busy = ref(false)
const error = ref('')
const notice = ref('')
const applyOpen = ref(false)
const cancelOpen = ref(false)
const message = ref('')
const messageId = useId()

async function run(fn: () => Promise<unknown>, done: string) {
  busy.value = true
  error.value = ''
  notice.value = ''
  try {
    await fn()
    notice.value = done
    emit('changed')
  } catch (e) {
    error.value = e instanceof ApiError ? e.message : t('join.error')
    if (e instanceof ApiError && e.isGone) emit('changed')
  } finally {
    busy.value = false
  }
}

function join() {
  return run(() => eventsApi.join(props.event.id), t('join.joined'))
}

async function apply() {
  applyOpen.value = false
  await run(
    () => eventsApi.join(props.event.id, message.value.trim() || undefined),
    t('join.applied'),
  )
  message.value = ''
}

async function leave() {
  cancelOpen.value = false
  await run(() => eventsApi.leave(props.event.id), t('join.left'))
}

const editable = computed(() => props.event.status === 'published' && !props.event.is_past)
const cancelEventOpen = ref(false)
const cancelReason = ref('')
const cancelReasonErr = ref<string | null>(null)

function openCancelEvent() {
  cancelReason.value = ''
  cancelReasonErr.value = null
  cancelEventOpen.value = true
}

async function cancelEvent() {
  cancelReasonErr.value = cancelReasonError(cancelReason.value)
  if (cancelReasonErr.value) return
  cancelEventOpen.value = false
  await run(
    () => eventsApi.cancel(props.event.id, cancelReason.value.trim()),
    t('join.eventCancelled'),
  )
}

const when = computed(() => formatEventWhen(props.event.starts_at, props.event.ends_at))
</script>

<template>
  <section aria-labelledby="join-title" class="flex flex-col gap-3 rounded-card bg-card p-4">
    <h2 id="join-title" class="sr-only">{{ $t('join.title') }}</h2>

    <template v-if="action.kind === 'confirmed'">
      <StatusBadge :of="{ kind: 'participation', status: 'confirmed' }" />
      <p class="font-medium">{{ $t('join.waiting', { when: when.toLowerCase() }) }}</p>
      <UiButton variant="ghost" :disabled="busy" @click="cancelOpen = true">{{
        $t('join.leave')
      }}</UiButton>
    </template>

    <template v-else-if="action.kind === 'pending'">
      <StatusBadge :of="{ kind: 'participation', status: 'pending' }" />
      <p class="text-ink-2">
        {{ $t('join.pendingText') }}
      </p>
      <UiButton variant="ghost" :disabled="busy" @click="cancelOpen = true">{{
        $t('join.withdraw')
      }}</UiButton>
    </template>

    <template v-else-if="action.kind === 'join'">
      <p class="text-ink-2">{{ $t('join.openText') }}</p>
      <UiButton :loading="busy" @click="join">{{ $t('join.join') }}</UiButton>
    </template>

    <template v-else-if="action.kind === 'apply'">
      <div v-if="action.again && mine" class="flex flex-col gap-1.5">
        <StatusBadge :of="{ kind: 'participation', status: 'rejected' }" />
        <p v-if="mine.decision_reason" class="text-ink-2">
          {{ $t('join.reason', { reason: mine.decision_reason }) }}
        </p>
      </div>
      <p class="text-ink-2">
        {{ $t('join.approvalText') }}
      </p>
      <UiButton :loading="busy" @click="applyOpen = true">
        {{ action.again ? $t('join.applyAgain') : $t('join.apply') }}
      </UiButton>
    </template>

    <template v-else-if="action.kind === 'login'">
      <p class="text-ink-2">{{ $t('join.loginText') }}</p>
      <RouterLink
        :to="{ name: 'login', query: { redirect: route.fullPath } }"
        class="inline-flex min-h-13 items-center justify-center rounded-full bg-ink px-5 font-bold text-white no-underline"
      >
        {{ $t('join.loginCta') }}
      </RouterLink>
    </template>

    <template v-else-if="action.kind === 'organizer'">
      <p class="font-medium">{{ $t('join.organizer') }}</p>
      <RouterLink
        :to="{ name: 'event-manage', params: { id: event.id } }"
        class="inline-flex min-h-13 items-center justify-center rounded-full bg-ink px-5 font-bold text-white no-underline"
      >
        {{ $t('join.manage') }}
      </RouterLink>
      <RouterLink
        v-if="editable"
        :to="{ name: 'event-edit', params: { id: event.id } }"
        class="inline-flex min-h-13 items-center justify-center rounded-full bg-field px-5 font-bold text-ink no-underline"
      >
        {{ $t('common.edit') }}
      </RouterLink>
      <button
        v-if="editable"
        type="button"
        class="inline-flex min-h-8 w-fit items-center self-center text-sm font-semibold text-ink-2 underline underline-offset-4 hover:text-ink"
        :disabled="busy"
        @click="openCancelEvent"
      >
        {{ $t('join.cancelEvent') }}
      </button>
    </template>
    <p v-else-if="action.kind === 'admin'" class="text-ink-2">
      {{ $t('join.admin') }}
    </p>
    <p v-else-if="action.kind === 'past'" class="text-ink-2">{{ $t('join.past') }}</p>
    <p v-else-if="action.kind === 'unavailable'" class="font-medium">
      {{ $t('join.closed') }}
    </p>
    <p v-else-if="action.kind === 'full'" class="font-medium">{{ $t('join.full') }}</p>

    <p v-if="notice" class="font-medium text-accent-ink" role="status">{{ notice }}</p>
    <p v-if="error" class="font-medium text-accent-ink" role="alert">✕ {{ error }}</p>

    <UiModal
      v-model:open="applyOpen"
      :title="$t('join.applyTitle')"
      :description="$t('join.applyDescription')"
    >
      <label :for="messageId" class="font-medium">{{ $t('join.message') }}</label>
      <textarea
        :id="messageId"
        v-model="message"
        rows="3"
        maxlength="500"
        class="rounded-field bg-card p-3 text-ink"
        :placeholder="$t('join.messagePlaceholder')"
      />
      <template #actions>
        <UiButton variant="ghost" @click="applyOpen = false">{{ $t('common.cancel') }}</UiButton>
        <UiButton @click="apply">{{ $t('join.send') }}</UiButton>
      </template>
    </UiModal>

    <UiModal
      v-model:open="cancelOpen"
      :title="action.kind === 'pending' ? $t('join.withdrawQ') : $t('join.leaveQ')"
      :description="$t('join.leaveDescription')"
    >
      <template #actions>
        <UiButton variant="ghost" @click="cancelOpen = false">{{ $t('join.keep') }}</UiButton>
        <UiButton @click="leave">{{ $t('join.yesCancel') }}</UiButton>
      </template>
    </UiModal>

    <UiModal
      v-model:open="cancelEventOpen"
      :title="$t('join.cancelEventQ')"
      :description="$t('join.cancelEventDescription')"
    >
      <UiTextArea
        v-model="cancelReason"
        :label="$t('join.reasonLabel')"
        :rows="3"
        :maxlength="CANCEL_REASON_MAX"
        :placeholder="$t('join.reasonPlaceholder')"
        :error="cancelReasonErr ?? undefined"
      />
      <template #actions>
        <UiButton variant="ghost" @click="cancelEventOpen = false">{{ $t('join.keep') }}</UiButton>
        <UiButton @click="cancelEvent">{{ $t('join.cancelEvent') }}</UiButton>
      </template>
    </UiModal>
  </section>
</template>
