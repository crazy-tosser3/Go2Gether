<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { ApiError, adminApi, type Report } from '@/api'
import AdminNav from '@/components/admin/AdminNav.vue'
import {
  StatusBadge,
  UiBadge,
  UiButton,
  UiChip,
  UiModal,
  UiTable,
  UiTextArea,
} from '@/components/ui'
import { moderationReasonError } from '@/features/admin/events'
import {
  REPORT_FILTERS,
  parseReportFilter,
  reasonLabel,
  replaceReport,
  statusLabel,
} from '@/features/reports/reports'
import { intlLocale, t } from '@/i18n'
import { formatEventWhen } from '@/utils/datetime'

const route = useRoute()
const router = useRouter()

const filter = computed(() => parseReportFilter(route.query.status))
const selectedId = computed(() => (route.query.id ? Number(route.query.id) : null))

const rows = ref<Report[]>([])
const total = ref(0)
const state = ref<'loading' | 'ready' | 'error'>('loading')
const listError = ref('')

const selected = computed(() => rows.value.find((r) => r.id === selectedId.value) ?? null)

const columns = computed(() =>
  (['event', 'reason', 'reporter', 'status'] as const).map((key) => ({
    key,
    label: t(`admin.reports.columns.${key}`),
  })),
)

function setQuery(patch: Record<string, string | undefined>) {
  router.replace({ query: { ...route.query, ...patch } })
}

async function load() {
  state.value = 'loading'
  try {
    const page = await adminApi.reports({
      status: filter.value === 'all' ? undefined : filter.value,
      size: 50,
    })
    rows.value = page.items
    total.value = page.total
    state.value = 'ready'
  } catch (e) {
    listError.value = e instanceof ApiError ? e.message : t('admin.reports.loadFailed')
    state.value = 'error'
  }
}

watch(filter, load, { immediate: true })

function select(r: Report) {
  setQuery({ id: String(r.id) })
  window.scrollTo({ top: 0 })
}

const busy = ref(false)
const actionError = ref('')
const decision = ref<'accept' | 'reject' | null>(null)
const note = ref('')
const noteError = ref<string | null>(null)

const decisionOpen = computed({
  get: () => decision.value !== null,
  set: (v) => {
    if (!v) decision.value = null
  },
})

function openDecision(kind: 'accept' | 'reject') {
  decision.value = kind
  note.value = ''
  noteError.value = null
  actionError.value = ''
}

async function decide() {
  const report = selected.value
  if (!report || !decision.value) return
  // Принять — значит снять мероприятие: причину увидит организатор, поэтому она обязательна.
  noteError.value = decision.value === 'accept' ? moderationReasonError(note.value) : null
  if (noteError.value) return
  busy.value = true
  try {
    const updated =
      decision.value === 'accept'
        ? await adminApi.acceptReport(report.id, note.value.trim())
        : await adminApi.rejectReport(report.id, note.value.trim() || undefined)
    rows.value = replaceReport(rows.value, updated)
    decision.value = null
    // Принятие закрывает и остальные жалобы на это мероприятие — перечитываем очередь.
    if (updated.status === 'accepted') await load()
  } catch (e) {
    if (e instanceof ApiError && e.fields.reason) noteError.value = e.fields.reason
    else {
      decision.value = null
      actionError.value = e instanceof ApiError ? e.message : t('admin.reports.decideFailed')
    }
  } finally {
    busy.value = false
  }
}

const date = computed(
  () =>
    new Intl.DateTimeFormat(intlLocale(), {
      day: 'numeric',
      month: 'long',
      hour: '2-digit',
      minute: '2-digit',
    }),
)
</script>

<template>
  <section class="flex flex-col gap-5">
    <h1 class="text-[30px] lg:text-[42px]">{{ $t('admin.nav.reports') }}</h1>
    <AdminNav />

    <div
      class="gap-2 overflow-x-auto rounded-card bg-card p-4"
      :class="selectedId ? 'hidden xl:flex' : 'flex'"
      role="group"
      :aria-label="$t('admin.filters.status')"
    >
      <UiChip
        v-for="f in REPORT_FILTERS"
        :key="f"
        :pressed="filter === f"
        @click="setQuery({ status: f, id: undefined })"
      >
        {{ f === 'all' ? $t('admin.filters.all') : statusLabel(f) }}
      </UiChip>
    </div>

    <div class="grid grid-cols-[minmax(0,1fr)] gap-5 xl:grid-cols-[minmax(0,1fr)_400px]">
      <div
        class="min-w-0 flex-col gap-2"
        :class="selectedId ? 'hidden xl:flex' : 'flex'"
        aria-live="polite"
      >
        <div
          v-if="state === 'error'"
          class="flex flex-col items-center gap-3 rounded-card bg-card p-6"
          role="alert"
        >
          <p class="font-bold">{{ listError }}</p>
          <UiButton variant="ghost" class="!w-auto" @click="load">{{
            $t('common.retry')
          }}</UiButton>
        </div>
        <template v-else>
          <UiTable
            :caption="$t('admin.nav.reports')"
            :columns="columns"
            :rows="rows"
            :row-key="(r) => String(r.id)"
            :selected-key="selectedId ? String(selectedId) : null"
            :loading="state === 'loading'"
            clickable
            :empty-text="$t('admin.reports.empty')"
            @select="select"
          >
            <template #cell-event="{ row }">
              <b>{{ row.event.title }}</b>
              <span class="block text-sm text-ink-2">{{
                date.format(new Date(row.created_at))
              }}</span>
            </template>
            <template #cell-reason="{ row }">{{ reasonLabel(row.reason) }}</template>
            <template #cell-reporter="{ row }">{{ row.reporter.name }}</template>
            <template #cell-status="{ row }">
              <UiBadge
                :tone="
                  row.status === 'pending' ? 'wait' : row.status === 'accepted' ? 'ok' : 'outline'
                "
              >
                {{ statusLabel(row.status) }}
              </UiBadge>
            </template>
          </UiTable>
          <p v-if="state === 'ready' && total > rows.length" class="px-2 text-sm text-ink-2">
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
          {{ $t('admin.reports.back') }}
        </button>
        <p v-if="!selected" class="hidden rounded-card bg-card p-6 text-center text-ink-2 xl:block">
          {{ $t('admin.reports.pick') }}
        </p>
        <article
          v-else
          class="flex flex-col gap-4 rounded-card bg-accent p-5"
          :aria-label="selected.event.title"
        >
          <div class="flex items-start justify-between gap-3">
            <div class="min-w-0">
              <p class="text-sm font-semibold text-accent-ink">
                {{ reasonLabel(selected.reason) }}
              </p>
              <h2 class="text-xl leading-tight font-extrabold normal-case">
                <RouterLink
                  :to="{ name: 'event', params: { id: selected.event.id } }"
                  class="text-ink underline underline-offset-4"
                >
                  {{ selected.event.title }}
                </RouterLink>
              </h2>
              <p class="text-accent-ink">{{ formatEventWhen(selected.event.starts_at) }}</p>
            </div>
            <StatusBadge
              :of="{ kind: 'event', status: selected.event.status }"
              class="!bg-card !text-ink"
            />
          </div>

          <dl
            class="grid grid-cols-[auto_1fr] gap-x-4 gap-y-1.5 rounded-lg bg-card p-4 text-[15px]"
          >
            <dt class="text-ink-2">{{ $t('admin.reports.reporter') }}</dt>
            <dd>
              <RouterLink
                :to="{ name: 'user', params: { id: selected.reporter.id } }"
                class="font-semibold underline underline-offset-4"
              >
                {{ selected.reporter.name }}
              </RouterLink>
            </dd>
            <dt class="text-ink-2">{{ $t('admin.reports.submitted') }}</dt>
            <dd>{{ date.format(new Date(selected.created_at)) }}</dd>
            <template v-if="selected.comment">
              <dt class="text-ink-2">{{ $t('admin.reports.comment') }}</dt>
              <dd class="break-words whitespace-pre-line">{{ selected.comment }}</dd>
            </template>
            <template v-if="selected.status !== 'pending'">
              <dt class="text-ink-2">{{ $t('admin.reports.decision') }}</dt>
              <dd>
                {{ statusLabel(selected.status)
                }}<template v-if="selected.resolution_note"
                  >: {{ selected.resolution_note }}</template
                >
              </dd>
            </template>
          </dl>

          <div v-if="selected.status === 'pending'" class="flex flex-col gap-2 sm:flex-row">
            <UiButton :disabled="busy" @click="openDecision('accept')">
              {{ $t('admin.reports.accept') }}
            </UiButton>
            <UiButton
              variant="ghost"
              class="!bg-card"
              :disabled="busy"
              @click="openDecision('reject')"
            >
              {{ $t('admin.reports.reject') }}
            </UiButton>
          </div>
          <p v-if="actionError" class="font-semibold text-danger" role="alert">
            ✕ {{ actionError }}
          </p>
        </article>
      </aside>
    </div>

    <UiModal
      v-model:open="decisionOpen"
      :title="
        decision === 'accept' ? $t('admin.reports.acceptTitle') : $t('admin.reports.rejectTitle')
      "
      :description="
        decision === 'accept' ? $t('admin.reports.acceptText') : $t('admin.reports.rejectText')
      "
    >
      <UiTextArea
        v-model="note"
        :label="decision === 'accept' ? $t('admin.reason.label') : $t('admin.reports.noteOptional')"
        :rows="3"
        :maxlength="500"
        :error="noteError ?? undefined"
      />
      <template #actions>
        <UiButton variant="ghost" @click="decision = null">{{ $t('common.cancel') }}</UiButton>
        <UiButton :loading="busy" @click="decide">
          {{ decision === 'accept' ? $t('admin.reports.accept') : $t('admin.reports.reject') }}
        </UiButton>
      </template>
    </UiModal>
  </section>
</template>
