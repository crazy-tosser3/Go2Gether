<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { ApiError, adminApi, type AdminOrganizationDetails, type Organization } from '@/api'
import AdminNav from '@/components/admin/AdminNav.vue'
import {
  StatusBadge,
  UiButton,
  UiChip,
  UiModal,
  UiTable,
  UiTextArea,
  UiTextField,
} from '@/components/ui'
import {
  ORG_TYPE_LABELS,
  REJECT_REASON_MAX,
  STATUS_FILTERS,
  parseStatusFilter,
  rejectReasonError,
  replaceOrganization,
  type StatusFilter,
} from '@/features/admin/organizations'
import { intlLocale, t } from '@/i18n'
import { formatEventWhen } from '@/utils/datetime'

const route = useRoute()
const router = useRouter()

const status = computed<StatusFilter>(() => parseStatusFilter(route.query.status))
const selectedId = computed(() => (route.query.id ? Number(route.query.id) : null))
const query = ref(typeof route.query.q === 'string' ? route.query.q : '')

const rows = ref<Organization[]>([])
const total = ref(0)
const listState = ref<'loading' | 'ready' | 'error'>('loading')
const listError = ref('')

const details = ref<AdminOrganizationDetails | null>(null)
const detailsState = ref<'idle' | 'loading' | 'ready' | 'error'>('idle')

const busy = ref(false)
const actionError = ref('')
const rejectOpen = ref(false)
const reason = ref('')
const reasonError = ref<string | null>(null)

const columns = computed(() =>
  (['name', 'type', 'owner', 'status'] as const).map((key) => ({
    key,
    label: t(`admin.orgs.columns.${key}`),
  })),
)

function setQuery(patch: Record<string, string | undefined>) {
  router.replace({ query: { ...route.query, ...patch } })
}

async function loadList() {
  listState.value = 'loading'
  try {
    const page = await adminApi.organizations({
      status: status.value === 'all' ? undefined : status.value,
      q: query.value.trim() || undefined,
      size: 50,
    })
    rows.value = page.items
    total.value = page.total
    listState.value = 'ready'
  } catch (e) {
    listError.value = e instanceof ApiError ? e.message : t('admin.orgs.loadFailed')
    listState.value = 'error'
  }
}

async function loadDetails(id: number) {
  detailsState.value = 'loading'
  actionError.value = ''
  try {
    details.value = await adminApi.organization(id)
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

function select(org: Organization) {
  setQuery({ id: String(org.id) })
  // На узком экране карточка открывается вместо списка — показываем её с начала.
  window.scrollTo({ top: 0 })
}

function applyResult(updated: Organization) {
  rows.value = replaceOrganization(rows.value, updated)
  if (details.value) details.value = { ...details.value, organization: updated }
}

async function approve() {
  const org = details.value?.organization
  if (!org) return
  busy.value = true
  actionError.value = ''
  try {
    applyResult(await adminApi.approveOrganization(org.id))
  } catch (e) {
    actionError.value = e instanceof ApiError ? e.message : t('admin.orgs.approveFailed')
  } finally {
    busy.value = false
  }
}

function openReject() {
  reason.value = ''
  reasonError.value = null
  rejectOpen.value = true
}

async function reject() {
  const org = details.value?.organization
  reasonError.value = rejectReasonError(reason.value)
  if (!org || reasonError.value) return
  busy.value = true
  try {
    applyResult(await adminApi.rejectOrganization(org.id, reason.value.trim()))
    rejectOpen.value = false
  } catch (e) {
    if (e instanceof ApiError && e.fields.reason) reasonError.value = e.fields.reason
    else {
      rejectOpen.value = false
      actionError.value = e instanceof ApiError ? e.message : t('admin.orgs.rejectFailed')
    }
  } finally {
    busy.value = false
  }
}

const shortDate = computed(
  () => new Intl.DateTimeFormat(intlLocale(), { day: 'numeric', month: 'long' }),
)
</script>

<template>
  <section class="flex flex-col gap-5">
    <h1 class="text-[30px] lg:text-[42px]">{{ $t('admin.nav.organizations') }}</h1>
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
        :placeholder="$t('admin.orgs.searchPlaceholder')"
        class="lg:w-80"
      />
      <div class="flex gap-2 overflow-x-auto" role="group" :aria-label="$t('admin.filters.status')">
        <UiChip
          v-for="f in STATUS_FILTERS"
          :key="f.value"
          :pressed="status === f.value"
          @click="setQuery({ status: f.value, id: undefined })"
        >
          {{ f.label }}
        </UiChip>
      </div>
    </div>

    <div class="grid grid-cols-[minmax(0,1fr)] gap-5 xl:grid-cols-[minmax(0,1fr)_400px]">
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
            :caption="$t('admin.nav.organizations')"
            :columns="columns"
            :rows="rows"
            :row-key="(o) => String(o.id)"
            :selected-key="selectedId ? String(selectedId) : null"
            :loading="listState === 'loading'"
            clickable
            :empty-text="$t('admin.empty')"
            @select="select"
          >
            <template #cell-name="{ row }">
              <b>{{ row.name }}</b>
              <span v-if="row.city" class="block text-sm text-ink-2">{{ row.city }}</span>
            </template>
            <template #cell-type="{ row }">{{ ORG_TYPE_LABELS[row.type] }}</template>
            <template #cell-owner="{ row }">{{ row.owner.name }}</template>
            <template #cell-status="{ row }">
              <StatusBadge :of="{ kind: 'organization', status: row.status }" />
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
          {{ $t('admin.orgs.back') }}
        </button>
        <p
          v-if="detailsState === 'idle'"
          class="hidden rounded-card bg-card p-6 text-center text-ink-2 xl:block"
        >
          {{ $t('admin.orgs.pick') }}
        </p>
        <div
          v-else-if="detailsState === 'loading'"
          class="h-64 animate-pulse rounded-card bg-card"
        />
        <p v-else-if="detailsState === 'error'" class="rounded-card bg-card p-6" role="alert">
          {{ $t('admin.orgs.openFailed') }}
        </p>

        <article
          v-else-if="details"
          class="flex flex-col gap-4 rounded-card bg-accent p-5"
          :aria-label="details.organization.name"
        >
          <div class="flex items-start justify-between gap-3">
            <div class="min-w-0">
              <h2 class="text-xl leading-tight font-extrabold normal-case">
                {{ details.organization.name }}
              </h2>
              <p class="text-accent-ink">
                {{ ORG_TYPE_LABELS[details.organization.type] }}
                <template v-if="details.organization.city">
                  · {{ details.organization.city }}
                </template>
              </p>
            </div>
            <StatusBadge
              :of="{ kind: 'organization', status: details.organization.status }"
              class="!bg-card !text-ink"
            />
          </div>

          <p v-if="details.organization.description" class="whitespace-pre-line">
            {{ details.organization.description }}
          </p>

          <dl
            class="grid grid-cols-[auto_1fr] gap-x-4 gap-y-1.5 rounded-lg bg-card p-4 text-[15px]"
          >
            <dt class="text-ink-2">{{ $t('admin.orgs.owner') }}</dt>
            <dd class="min-w-0">
              <RouterLink
                :to="{ name: 'user', params: { id: details.owner.id } }"
                class="font-semibold underline underline-offset-4"
              >
                {{ details.owner.name }}
              </RouterLink>
              <span class="block truncate text-ink-2">{{ details.owner.email }}</span>
            </dd>
            <template v-if="details.organization.sports_description">
              <dt class="text-ink-2">{{ $t('admin.orgs.sports') }}</dt>
              <dd>{{ details.organization.sports_description }}</dd>
            </template>
            <template v-if="details.organization.contacts">
              <dt class="text-ink-2">{{ $t('admin.orgs.contacts') }}</dt>
              <dd class="break-words">{{ details.organization.contacts }}</dd>
            </template>
            <dt class="text-ink-2">{{ $t('admin.orgs.submitted') }}</dt>
            <dd>{{ shortDate.format(new Date(details.organization.created_at)) }}</dd>
          </dl>

          <p
            v-if="details.organization.status === 'rejected' && details.organization.reject_reason"
            class="rounded-lg bg-card p-4"
          >
            <b>{{ $t('admin.orgs.rejectReason') }}</b> {{ details.organization.reject_reason }}
          </p>

          <div v-if="details.organization.status === 'pending'" class="flex gap-2">
            <UiButton :loading="busy" @click="approve">{{ $t('admin.orgs.approve') }}</UiButton>
            <UiButton variant="ghost" class="!bg-card" :disabled="busy" @click="openReject">
              {{ $t('admin.orgs.reject') }}
            </UiButton>
          </div>
          <p v-if="actionError" class="font-semibold text-danger" role="alert">
            ✕ {{ actionError }}
          </p>

          <div class="flex flex-col gap-2">
            <h3 class="font-bold">{{ $t('admin.orgs.events', { n: details.events.length }) }}</h3>
            <p v-if="!details.events.length" class="text-accent-ink">{{ $t('admin.orgs.none') }}</p>
            <ul v-else class="flex flex-col gap-1.5">
              <li v-for="e in details.events" :key="e.id">
                <RouterLink
                  :to="{ name: 'event', params: { id: e.id } }"
                  class="flex flex-col rounded-md bg-card px-4 py-2.5 text-ink no-underline hover:shadow-raised"
                >
                  <span class="font-semibold">{{ e.title }}</span>
                  <span class="text-sm text-ink-2">{{ formatEventWhen(e.starts_at) }}</span>
                </RouterLink>
              </li>
            </ul>
          </div>
        </article>
      </aside>
    </div>

    <UiModal
      v-model:open="rejectOpen"
      :title="$t('admin.orgs.rejectTitle', { name: details?.organization.name ?? '' })"
      :description="$t('admin.orgs.rejectText')"
    >
      <UiTextArea
        v-model="reason"
        :label="$t('admin.reason.label')"
        :rows="3"
        :maxlength="REJECT_REASON_MAX"
        :placeholder="$t('admin.orgs.reasonPlaceholder')"
        :error="reasonError ?? undefined"
      />
      <template #actions>
        <UiButton variant="ghost" @click="rejectOpen = false">{{ $t('common.cancel') }}</UiButton>
        <UiButton :loading="busy" @click="reject">{{ $t('admin.orgs.reject') }}</UiButton>
      </template>
    </UiModal>
  </section>
</template>
