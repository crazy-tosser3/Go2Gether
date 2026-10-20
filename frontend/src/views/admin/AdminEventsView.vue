<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { ApiError, adminApi, type AdminEventDetails, type Comment, type EventSummary } from '@/api'
import AdminNav from '@/components/admin/AdminNav.vue'
import { organizerLink, organizerName } from '@/components/events/organizer'
import {
  StatusBadge,
  UiBadge,
  UiButton,
  UiChip,
  UiModal,
  UiTable,
  UiTextArea,
  UiTextField,
} from '@/components/ui'
import {
  EVENT_STATUS_FILTERS,
  moderationActions,
  moderationReasonError,
  parseEventStatusFilter,
  replaceComment,
  replaceEventStatus,
  type EventStatusFilter,
} from '@/features/admin/events'
import { intlLocale, t } from '@/i18n'
import { formatEventWhen } from '@/utils/datetime'

const route = useRoute()
const router = useRouter()

const status = computed<EventStatusFilter>(() => parseEventStatusFilter(route.query.status))
const selectedId = computed(() => (route.query.id ? Number(route.query.id) : null))
const query = ref(typeof route.query.q === 'string' ? route.query.q : '')

const rows = ref<EventSummary[]>([])
const total = ref(0)
const listState = ref<'loading' | 'ready' | 'error'>('loading')
const listError = ref('')

const details = ref<AdminEventDetails | null>(null)
const detailsState = ref<'idle' | 'loading' | 'ready' | 'error'>('idle')

const busy = ref(false)
const actionError = ref('')

type Dialog = { kind: 'unpublish' | 'delete' } | { kind: 'hide'; comment: Comment }
const dialog = ref<Dialog | null>(null)
const dialogOpen = computed({
  get: () => dialog.value !== null,
  set: (v) => {
    if (!v) dialog.value = null
  },
})
const reason = ref('')
const reasonError = ref<string | null>(null)

const columns = computed(() =>
  (['title', 'organizer', 'when', 'status'] as const).map((key) => ({
    key,
    label: t(`admin.events.columns.${key}`),
  })),
)

const dialogText = computed(() => {
  if (!dialog.value) return null
  const k = `admin.events.dialogs.${dialog.value.kind}` as const
  return { title: t(`${k}.title`), text: t(`${k}.text`), button: t(`${k}.button`) }
})
const actions = computed(() => (details.value ? moderationActions(details.value.event.status) : []))

function setQuery(patch: Record<string, string | undefined>) {
  router.replace({ query: { ...route.query, ...patch } })
}

function select(event: EventSummary) {
  setQuery({ id: String(event.id) })
  // На узком экране карточка открывается вместо списка — показываем её с начала.
  window.scrollTo({ top: 0 })
}

async function loadList() {
  listState.value = 'loading'
  try {
    const page = await adminApi.events({
      status: status.value === 'all' ? undefined : status.value,
      q: query.value.trim() || undefined,
      size: 50,
    })
    rows.value = page.items
    total.value = page.total
    listState.value = 'ready'
  } catch (e) {
    listError.value = e instanceof ApiError ? e.message : t('admin.events.loadFailed')
    listState.value = 'error'
  }
}

async function loadDetails(id: number) {
  detailsState.value = 'loading'
  actionError.value = ''
  try {
    details.value = await adminApi.event(id)
    detailsState.value = 'ready'
  } catch {
    detailsState.value = 'error'
  }
}

watch(status, loadList, { immediate: true })

let timer: ReturnType<typeof setTimeout> | undefined
watch(query, (q) => {
  clearTimeout(timer)
  timer = setTimeout(() => {
    setQuery({ q: q.trim() || undefined })
    loadList()
  }, 300)
})

watch(
  selectedId,
  (id) => {
    if (id) loadDetails(id)
    else {
      details.value = null
      detailsState.value = 'idle'
    }
  },
  { immediate: true },
)

function open(kind: 'unpublish' | 'delete') {
  reason.value = ''
  reasonError.value = null
  dialog.value = { kind }
}

function openHide(comment: Comment) {
  reason.value = ''
  reasonError.value = null
  dialog.value = { kind: 'hide', comment }
}

async function restore() {
  const id = details.value?.event.id
  if (!id) return
  busy.value = true
  actionError.value = ''
  try {
    const event = await adminApi.restoreEvent(id)
    rows.value = replaceEventStatus(rows.value, id, event.status)
    await loadDetails(id)
  } catch (e) {
    actionError.value = e instanceof ApiError ? e.message : t('admin.events.restoreFailed')
  } finally {
    busy.value = false
  }
}

async function confirm() {
  const d = dialog.value
  const current = details.value
  reasonError.value = moderationReasonError(reason.value)
  if (!d || !current || reasonError.value) return
  const text = reason.value.trim()
  busy.value = true
  try {
    if (d.kind === 'hide') {
      const updated = await adminApi.hideComment(d.comment.id, text)
      current.comments = replaceComment(current.comments, updated)
    } else {
      const id = current.event.id
      const event =
        d.kind === 'unpublish'
          ? await adminApi.unpublishEvent(id, text)
          : await adminApi.deleteEvent(id, text)
      rows.value = replaceEventStatus(rows.value, id, event.status)
      await loadDetails(id)
    }
    dialog.value = null
  } catch (e) {
    if (e instanceof ApiError && e.fields.reason) reasonError.value = e.fields.reason
    else {
      dialog.value = null
      actionError.value = e instanceof ApiError ? e.message : t('admin.events.failed')
    }
  } finally {
    busy.value = false
  }
}

const confirmedCount = computed(
  () => details.value?.participations.filter((p) => p.status === 'confirmed').length ?? 0,
)
const shortDate = computed(
  () =>
    new Intl.DateTimeFormat(intlLocale(), {
      day: 'numeric',
      month: 'short',
      hour: '2-digit',
      minute: '2-digit',
    }),
)
</script>

<template>
  <section class="flex flex-col gap-5">
    <h1 class="text-[30px] lg:text-[42px]">{{ $t('admin.nav.events') }}</h1>
    <AdminNav />

    <div
      class="flex-col gap-3 rounded-card bg-card p-4 lg:flex-row lg:items-end"
      :class="selectedId ? 'hidden xl:flex' : 'flex'"
    >
      <UiTextField
        v-model="query"
        :label="$t('admin.search')"
        hide-label
        type="search"
        :placeholder="$t('admin.events.searchPlaceholder')"
        class="lg:w-80"
      />
      <div class="flex gap-2 overflow-x-auto" role="group" :aria-label="$t('admin.filters.status')">
        <UiChip
          v-for="f in EVENT_STATUS_FILTERS"
          :key="f.value"
          :pressed="status === f.value"
          @click="setQuery({ status: f.value, id: undefined })"
        >
          {{ f.label }}
        </UiChip>
      </div>
    </div>

    <div class="grid grid-cols-[minmax(0,1fr)] gap-5 xl:grid-cols-[minmax(0,1fr)_420px]">
      <div
        class="min-w-0 flex-col gap-2"
        :class="selectedId ? 'hidden xl:flex' : 'flex'"
        aria-live="polite"
      >
        <div
          v-if="listState === 'error'"
          class="flex flex-col items-center gap-3 rounded-card bg-card p-6"
          role="alert"
        >
          <p class="font-bold">{{ listError }}</p>
          <UiButton variant="ghost" class="!w-auto" @click="loadList">{{
            $t('common.retry')
          }}</UiButton>
        </div>
        <template v-else>
          <UiTable
            :caption="$t('admin.nav.events')"
            :columns="columns"
            :rows="rows"
            :row-key="(e) => String(e.id)"
            :selected-key="selectedId ? String(selectedId) : null"
            :loading="listState === 'loading'"
            clickable
            :empty-text="$t('admin.empty')"
            @select="select"
          >
            <template #cell-title="{ row }">
              <b>{{ row.title }}</b>
              <span class="block text-sm text-ink-2">{{ row.sport.name }}</span>
            </template>
            <template #cell-organizer="{ row }">{{ organizerName(row.organizer) }}</template>
            <template #cell-when="{ row }">{{ formatEventWhen(row.starts_at) }}</template>
            <template #cell-status="{ row }">
              <StatusBadge :of="{ kind: 'event', status: row.status }" />
            </template>
          </UiTable>
          <p v-if="listState === 'ready' && total > rows.length" class="px-2 text-sm text-ink-2">
            {{ $t('admin.firstOf', { shown: rows.length, total }) }}
          </p>
        </template>
      </div>

      <aside class="flex flex-col gap-3 xl:sticky xl:top-6 xl:self-start" aria-live="polite">
        <button
          v-if="selectedId"
          type="button"
          class="inline-flex min-h-8 w-fit items-center font-semibold underline underline-offset-4 xl:hidden"
          @click="setQuery({ id: undefined })"
        >
          {{ $t('admin.events.back') }}
        </button>
        <p
          v-if="detailsState === 'idle'"
          class="hidden rounded-card bg-card p-6 text-center text-ink-2 xl:block"
        >
          {{ $t('admin.events.pick') }}
        </p>
        <div
          v-else-if="detailsState === 'loading'"
          class="h-64 animate-pulse rounded-card bg-card"
        />
        <p v-else-if="detailsState === 'error'" class="rounded-card bg-card p-6" role="alert">
          {{ $t('admin.events.openFailed') }}
        </p>

        <div v-else-if="details" class="flex flex-col gap-4">
          <article class="flex flex-col gap-3 rounded-card bg-accent p-5">
            <div class="flex items-start justify-between gap-3">
              <div class="min-w-0">
                <p class="font-semibold text-accent-ink">
                  {{ formatEventWhen(details.event.starts_at, details.event.ends_at) }}
                </p>
                <h2 class="text-xl leading-tight font-extrabold normal-case">
                  {{ details.event.title }}
                </h2>
              </div>
              <StatusBadge
                :of="{ kind: 'event', status: details.event.status }"
                class="!bg-card !text-ink"
              />
            </div>

            <dl
              class="grid grid-cols-[auto_1fr] gap-x-4 gap-y-1.5 rounded-lg bg-card p-4 text-[15px]"
            >
              <dt class="text-ink-2">{{ $t('admin.events.organizer') }}</dt>
              <dd>
                <RouterLink
                  :to="organizerLink(details.event.organizer)"
                  class="font-semibold underline underline-offset-4"
                >
                  {{ organizerName(details.event.organizer) }}
                </RouterLink>
              </dd>
              <dt class="text-ink-2">{{ $t('admin.events.participants') }}</dt>
              <dd>
                {{ $t('admin.events.people', confirmedCount) }}
              </dd>
              <dt class="text-ink-2">{{ $t('admin.events.place') }}</dt>
              <dd>{{ details.event.address }}</dd>
            </dl>

            <p
              v-if="details.event.status !== 'published' && details.event.unavailable_reason"
              class="rounded-lg bg-card p-4"
            >
              <b>{{ $t('admin.events.reason') }}</b> {{ details.event.unavailable_reason }}
              <span v-if="details.moderated_by" class="block text-sm text-ink-2">
                {{ details.moderated_by.name }}
                <template v-if="details.moderated_at">
                  · {{ shortDate.format(new Date(details.moderated_at)) }}
                </template>
              </span>
            </p>

            <div v-if="actions.length" class="flex flex-wrap gap-2">
              <UiButton
                v-if="actions.includes('restore')"
                class="!w-auto flex-1"
                :loading="busy"
                @click="restore"
              >
                {{ $t('admin.events.restore') }}
              </UiButton>
              <UiButton
                v-if="actions.includes('unpublish')"
                class="!w-auto flex-1"
                :disabled="busy"
                @click="open('unpublish')"
              >
                {{ $t('admin.events.unpublish') }}
              </UiButton>
              <UiButton
                v-if="actions.includes('delete')"
                variant="ghost"
                class="!w-auto flex-1 !bg-card"
                :disabled="busy"
                @click="open('delete')"
              >
                {{ $t('admin.events.delete') }}
              </UiButton>
            </div>
            <p v-if="actionError" class="font-semibold text-danger" role="alert">
              ✕ {{ actionError }}
            </p>
            <RouterLink
              :to="{ name: 'event', params: { id: details.event.id } }"
              class="w-fit text-sm font-semibold text-accent-ink underline underline-offset-4"
            >
              {{ $t('admin.events.openPage') }}
            </RouterLink>
          </article>

          <section
            class="flex flex-col gap-2 rounded-card bg-card p-4"
            :aria-label="$t('admin.events.discussion')"
          >
            <h3 class="font-bold">
              {{ $t('admin.events.discussionCount', { n: details.comments.length }) }}
            </h3>
            <p v-if="!details.comments.length" class="text-ink-2">
              {{ $t('admin.events.noComments') }}
            </p>
            <ul v-else class="flex flex-col gap-2">
              <li
                v-for="c in details.comments"
                :key="c.id"
                class="flex flex-col gap-1 rounded-lg px-4 py-3"
                :class="
                  c.is_hidden
                    ? 'bg-page text-ink-2'
                    : c.is_organizer
                      ? 'bg-accent-soft'
                      : 'bg-field'
                "
              >
                <div class="flex items-center gap-2 text-sm">
                  <b class="truncate">{{ c.as_organization?.name ?? c.author.name }}</b>
                  <UiBadge v-if="c.is_organizer" tone="accent" class="!px-2 !py-0 !text-xs">
                    {{ $t('admin.events.organizerBadge') }}
                  </UiBadge>
                  <span class="ml-auto text-ink-2">{{
                    shortDate.format(new Date(c.created_at))
                  }}</span>
                </div>
                <template v-if="c.is_hidden">
                  <p class="italic">
                    {{ $t('admin.events.hidden')
                    }}{{ c.hidden_reason ? `: ${c.hidden_reason}` : '' }}
                  </p>
                  <p v-if="c.text" class="text-sm line-through">{{ c.text }}</p>
                </template>
                <template v-else>
                  <p class="whitespace-pre-line">{{ c.text }}</p>
                  <button
                    type="button"
                    class="w-fit text-sm font-semibold underline underline-offset-4"
                    @click="openHide(c)"
                  >
                    {{ $t('admin.events.hide') }}
                  </button>
                </template>
              </li>
            </ul>
          </section>
        </div>
      </aside>
    </div>

    <UiModal
      v-model:open="dialogOpen"
      :title="dialogText?.title ?? ''"
      :description="dialogText?.text"
    >
      <p v-if="dialog?.kind === 'hide'" class="rounded-field bg-field px-4 py-3">
        «{{ dialog.comment.text }}»
      </p>
      <UiTextArea
        v-model="reason"
        :label="$t('admin.reason.label')"
        :rows="3"
        :maxlength="500"
        :error="reasonError ?? undefined"
      />
      <template #actions>
        <UiButton variant="ghost" @click="dialogOpen = false">{{ $t('common.cancel') }}</UiButton>
        <UiButton :loading="busy" @click="confirm">{{ dialogText?.button }}</UiButton>
      </template>
    </UiModal>
  </section>
</template>
