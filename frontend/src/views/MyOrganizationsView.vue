<script setup lang="ts">
import { nextTick, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { ApiError, organizationsApi, type Organization } from '@/api'
import OrgForm from '@/components/organizations/OrgForm.vue'
import { StatusBadge, UiButton } from '@/components/ui'
import { ORG_TYPE_LABELS } from '@/features/admin/organizations'
import {
  emptyOrgForm,
  mapOrgServerErrors,
  orgFormFrom,
  toOrgInput,
  validateOrgForm,
  type OrgFormErrors,
  type OrgFormState,
} from '@/features/organizations/orgForm'
import { useSessionStore } from '@/stores/session'

const session = useSessionStore()

const orgs = ref<Organization[]>([])
const state = ref<'loading' | 'ready' | 'error'>('loading')
const message = ref('')

/** null — форма закрыта, 'new' — новая заявка, число — правка отклонённой. */
const editing = ref<'new' | number | null>(null)
const initial = ref<OrgFormState>(emptyOrgForm())
const errors = ref<OrgFormErrors>({})
const formError = ref('')
const busy = ref(false)
const notice = ref('')

async function load() {
  state.value = 'loading'
  try {
    orgs.value = await organizationsApi.my()
    state.value = 'ready'
    if (!orgs.value.length) openNew()
  } catch (e) {
    message.value = e instanceof ApiError ? e.message : 'Не получилось загрузить организации.'
    state.value = 'error'
  }
}

onMounted(load)

function openNew() {
  initial.value = emptyOrgForm()
  errors.value = {}
  formError.value = ''
  editing.value = 'new'
}

function openFix(org: Organization) {
  initial.value = orgFormFrom(org)
  errors.value = {}
  formError.value = ''
  editing.value = org.id
}

async function focusFirstError() {
  await nextTick()
  document.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus()
}

function upsert(org: Organization) {
  const i = orgs.value.findIndex((o) => o.id === org.id)
  orgs.value = i === -1 ? [org, ...orgs.value] : orgs.value.map((o) => (o.id === org.id ? org : o))
}

async function submit(form: OrgFormState) {
  errors.value = validateOrgForm(form)
  formError.value = ''
  if (Object.keys(errors.value).length) return focusFirstError()

  busy.value = true
  try {
    const input = toOrgInput(form)
    let org: Organization
    if (editing.value === 'new') {
      org = await organizationsApi.create(input)
    } else {
      await organizationsApi.update(editing.value as number, input)
      org = await organizationsApi.resubmit(editing.value as number)
    }
    upsert(org)
    editing.value = null
    notice.value = 'Заявка отправлена. Ответ появится здесь.'
    await session.refresh()
  } catch (e) {
    if (e instanceof ApiError && Object.keys(e.fields).length) {
      errors.value = mapOrgServerErrors(e.fields)
      focusFirstError()
    }
    formError.value = e instanceof ApiError ? e.message : 'Не получилось отправить заявку.'
  } finally {
    busy.value = false
  }
}
</script>

<template>
  <section class="mx-auto flex max-w-3xl flex-col gap-4" aria-live="polite">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <h1 class="text-[30px] lg:text-[42px]">Моя организация</h1>
      <UiButton
        v-if="state === 'ready' && orgs.length && editing === null"
        variant="ghost"
        class="!w-auto"
        @click="openNew"
      >
        + Ещё одна
      </UiButton>
    </div>

    <div v-if="state === 'loading'" class="h-40 animate-pulse rounded-card bg-card" />

    <div
      v-else-if="state === 'error'"
      class="flex flex-col items-center gap-3 rounded-card bg-card p-6"
      role="alert"
    >
      <p class="font-bold">{{ message }}</p>
      <UiButton variant="ghost" class="!w-auto" @click="load">Повторить</UiButton>
    </div>

    <template v-else>
      <p v-if="notice" class="rounded-card bg-accent-soft p-4 font-semibold" role="status">
        ✓ {{ notice }}
      </p>

      <template v-if="editing === 'new'">
        <div class="flex flex-col gap-1 rounded-card bg-accent p-5">
          <h2 class="text-xl font-bold">Новая организация</h2>
          <p class="text-accent-ink">
            После проверки администратором сможете публиковать от её имени.
          </p>
        </div>
        <OrgForm
          :key="'new'"
          :initial="initial"
          :errors="errors"
          :busy="busy"
          ask-consent
          submit-label="Отправить на проверку"
          @submit="submit"
          @cancel="editing = null"
        />
        <p v-if="formError" class="font-semibold text-danger" role="alert">✕ {{ formError }}</p>
      </template>

      <div
        v-if="!orgs.length && editing === null"
        class="flex flex-col items-center gap-3 rounded-card bg-accent p-8 text-center"
      >
        <p class="text-xl font-bold">Организаций пока нет</p>
        <UiButton class="!w-auto" @click="openNew">Подать заявку</UiButton>
      </div>

      <ul class="flex flex-col gap-3">
        <li v-for="org in orgs" :key="org.id" class="flex flex-col gap-3">
          <article
            class="flex flex-col gap-3 rounded-card p-5"
            :class="org.status === 'approved' ? 'bg-accent' : 'bg-card'"
          >
            <div class="flex items-start justify-between gap-3">
              <div class="min-w-0">
                <h2 class="text-xl leading-tight font-extrabold normal-case">{{ org.name }}</h2>
                <p class="text-ink-2">
                  {{ ORG_TYPE_LABELS[org.type]
                  }}<template v-if="org.city"> · {{ org.city }}</template>
                </p>
              </div>
              <StatusBadge
                :of="{ kind: 'organization', status: org.status }"
                :class="org.status === 'approved' ? '!bg-card !text-ink' : ''"
              />
            </div>

            <p
              v-if="org.status === 'pending'"
              class="rounded-lg bg-warning-bg p-4 font-medium text-warning-fg"
            >
              Заявка на проверке. Публиковать от имени организации можно после допуска.
            </p>

            <template v-else-if="org.status === 'rejected'">
              <p class="rounded-lg bg-field p-4">
                <b>Отклонена.</b>
                {{ org.reject_reason ? `Причина: ${org.reject_reason}` : '' }}
              </p>
              <UiButton v-if="editing !== org.id" class="!w-auto self-start" @click="openFix(org)">
                Исправить и отправить снова
              </UiButton>
            </template>

            <div v-else class="flex flex-wrap gap-2">
              <RouterLink
                :to="{ name: 'event-create', query: { organization: String(org.id) } }"
                class="rounded-full bg-ink px-5 py-3 font-bold text-white no-underline hover:bg-ink/85"
              >
                + Мероприятие от организации
              </RouterLink>
              <RouterLink
                :to="{ name: 'organization', params: { id: org.id } }"
                class="rounded-full bg-card px-5 py-3 font-bold text-ink no-underline hover:shadow-raised"
              >
                Страница организации
              </RouterLink>
            </div>
          </article>

          <template v-if="editing === org.id">
            <OrgForm
              :key="org.id"
              :initial="initial"
              :errors="errors"
              :busy="busy"
              :ask-consent="false"
              submit-label="Отправить снова"
              @submit="submit"
              @cancel="editing = null"
            />
            <p v-if="formError" class="font-semibold text-danger" role="alert">✕ {{ formError }}</p>
          </template>
        </li>
      </ul>
    </template>
  </section>
</template>
