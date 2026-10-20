<script setup lang="ts">
import { computed, nextTick, onMounted, ref, useId } from 'vue'
import { RouterLink, useRouter } from 'vue-router'
import { ApiError, sportsApi, usersApi, type Sport } from '@/api'
import NavIcon from '@/components/layout/NavIcon.vue'
import type { NavIconName } from '@/components/layout/nav'
import {
  UiAvatar,
  UiButton,
  UiChip,
  UiModal,
  UiSelect,
  UiTextArea,
  UiTextField,
} from '@/components/ui'
import { LEVEL_LABELS } from '@/features/events/filters'
import { t } from '@/i18n'
import { AVATAR_PRESETS, presetAvatarUrl } from '@/features/profile/avatar'
import { TIME_SLOTS } from '@/features/onboarding/onboarding'
import {
  BIO_MAX,
  PHOTO_TYPES,
  isPhoto,
  photoError,
  profileFormFrom,
  toProfileInput,
  toggleSport,
  validateProfileForm,
  type ProfileFormErrors,
  type ProfileFormState,
} from '@/features/profile/profileForm'
import { useSessionStore } from '@/stores/session'

const session = useSessionStore()
const router = useRouter()

const sports = ref<Sport[]>([])
const sportsFailed = ref(false)
const editing = ref(false)
const form = ref<ProfileFormState | null>(null)
const errors = ref<ProfileFormErrors>({})
const formError = ref('')
const busy = ref(false)
const saved = ref(false)

const user = computed(() => session.user)
const mySports = computed(() => sports.value.filter((s) => user.value?.sport_ids.includes(s.id)))
// Подписи пересчитываются при смене языка. Загруженное фото — отдельным вариантом в начале.
const avatarOptions = computed(() => [
  ...(isPhoto(user.value?.avatar_url)
    ? [{ value: user.value!.avatar_url!, label: t('profile.photo.mine') }]
    : []),
  { value: '', label: t('profile.initials') },
  ...AVATAR_PRESETS.map((p) => ({ value: presetAvatarUrl(p.id), label: p.label })),
])

const formSports = computed(() => sports.value.filter((s) => form.value?.sportIds.includes(s.id)))

// Сводка подбора: заполняется в онбординге («Настроить подбор»).
const prefs = computed(() => {
  const u = user.value
  if (!u) return []
  const rows: { key: string; value: string }[] = []
  if (u.radius_km) rows.push({ key: 'radius', value: t('profile.prefs.km', { n: u.radius_km }) })
  if (u.preferred_times.length)
    rows.push({
      key: 'times',
      value: TIME_SLOTS.filter((s) => u.preferred_times.includes(s.id))
        .map((s) => s.label)
        .join(', '),
    })
  if (u.preferred_age_from || u.preferred_age_to)
    rows.push({
      key: 'age',
      value: [u.preferred_age_from ?? '…', u.preferred_age_to ?? '…'].join('–'),
    })
  return rows
})

const photoInputId = useId()
const photoBusy = ref(false)
const photoNotice = ref('')

async function uploadPhoto(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file || !form.value) return
  photoNotice.value = photoError(file) ?? ''
  if (photoNotice.value) return
  photoBusy.value = true
  try {
    const updated = await usersApi.uploadAvatar(file)
    await session.refresh()
    form.value.avatar = updated.avatar_url ?? ''
    photoNotice.value = t('profile.photo.uploaded')
  } catch (err) {
    photoNotice.value = err instanceof ApiError ? err.message : t('profile.photo.failed')
  } finally {
    photoBusy.value = false
  }
}

async function removePhoto() {
  if (!form.value) return
  photoBusy.value = true
  try {
    await usersApi.deleteAvatar()
    await session.refresh()
    form.value.avatar = ''
    photoNotice.value = t('profile.photo.removed')
  } catch (err) {
    photoNotice.value = err instanceof ApiError ? err.message : t('profile.photo.failed')
  } finally {
    photoBusy.value = false
  }
}

function toggleFormSport(id: number) {
  if (!form.value) return
  form.value.sportIds = toggleSport(form.value.sportIds, id)
  if (form.value.sportIds.includes(id) && !form.value.sportLevels[id])
    form.value.sportLevels[id] = form.value.level || 'beginner'
}
const levels = computed(() =>
  Object.entries(LEVEL_LABELS).map(([value, label]) => ({ value, label })),
)

const links: { key: string; to: { name: string }; icon: NavIconName }[] = [
  { key: 'participations', to: { name: 'my-participations' }, icon: 'calendar' },
  { key: 'events', to: { name: 'my-events' }, icon: 'flag' },
  { key: 'organization', to: { name: 'my-organization' }, icon: 'building' },
]

async function loadSports() {
  sportsFailed.value = false
  try {
    sports.value = await sportsApi.list()
  } catch {
    sports.value = []
    sportsFailed.value = true
  }
}

onMounted(loadSports)

function startEdit() {
  if (!user.value) return
  form.value = profileFormFrom(user.value)
  errors.value = {}
  formError.value = ''
  photoNotice.value = ''
  saved.value = false
  editing.value = true
}

async function save() {
  if (!form.value) return
  errors.value = validateProfileForm(form.value)
  formError.value = ''
  if (Object.keys(errors.value).length) {
    await nextTick()
    document.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus()
    return
  }
  busy.value = true
  try {
    await usersApi.updateMe(toProfileInput(form.value))
    await session.refresh()
    editing.value = false
    saved.value = true
  } catch (e) {
    if (e instanceof ApiError && e.fields.name) errors.value = { name: e.fields.name }
    formError.value = e instanceof ApiError ? e.message : t('profile.saveFailed')
  } finally {
    busy.value = false
  }
}

const deleteOpen = ref(false)
const deleting = ref(false)
const deleteError = ref('')
const deletePassword = ref('')

function openDelete() {
  deleteError.value = ''
  deletePassword.value = ''
  deleteOpen.value = true
}

async function deleteAccount() {
  if (!deletePassword.value) {
    deleteError.value = t('profile.passwordRequired')
    return
  }
  deleting.value = true
  deleteError.value = ''
  try {
    await usersApi.deleteMe(deletePassword.value)
    session.clear()
    deleteOpen.value = false
    await router.push({ name: 'home' })
  } catch (e) {
    // Демо-аккаунты бэк защищает от удаления — объясняем это без технических слов.
    deleteError.value =
      e instanceof ApiError && /демо|demo/i.test(e.message)
        ? t('profile.demoCantDelete')
        : e instanceof ApiError
          ? e.message
          : t('profile.deleteFailed')
  } finally {
    deleting.value = false
  }
}

async function logout() {
  await session.logout()
  await router.push({ name: 'home' })
}
</script>

<template>
  <section v-if="user" class="mx-auto flex max-w-3xl flex-col gap-4">
    <header class="flex flex-col gap-4 rounded-card bg-accent p-6 sm:flex-row sm:items-center">
      <UiAvatar :name="user.name" :url="user.avatar_url" size="xl" />
      <div class="flex min-w-0 flex-1 flex-col gap-1">
        <h1 class="text-[30px] lg:text-[42px]">{{ user.name }}</h1>
        <p class="text-accent-ink">
          {{ user.city || $t('profile.noCity') }}
          <template v-if="user.level"> · {{ LEVEL_LABELS[user.level] }}</template>
        </p>
      </div>
      <UiButton v-if="!editing" class="!w-auto self-start sm:self-center" @click="startEdit">
        {{ $t('common.edit') }}
      </UiButton>
    </header>

    <p v-if="saved" class="rounded-card bg-accent-soft p-4 font-semibold" role="status">
      {{ $t('profile.saved') }}
    </p>

    <form
      v-if="editing && form"
      class="flex flex-col gap-4 rounded-card bg-card p-5"
      novalidate
      @submit.prevent="save"
    >
      <UiTextField v-model="form.name" :label="$t('profile.name')" required :error="errors.name" />
      <fieldset class="flex flex-col gap-2">
        <legend class="mb-1.5 ml-1 text-[15px] font-semibold">{{ $t('profile.avatar') }}</legend>
        <div class="flex flex-wrap gap-2">
          <label
            v-for="option in avatarOptions"
            :key="option.value"
            class="flex cursor-pointer flex-col items-center gap-1 rounded-2xl p-1.5 ring-accent-strong has-checked:ring-3 has-focus-visible:outline-3 has-focus-visible:outline-accent-ink"
          >
            <input
              v-model="form.avatar"
              type="radio"
              name="avatar"
              :value="option.value"
              class="sr-only"
            />
            <UiAvatar :name="form.name || user.name" :url="option.value" size="lg" />
            <span class="text-xs font-medium">{{ option.label }}</span>
          </label>
        </div>
        <div class="flex flex-wrap items-center gap-2">
          <label
            :for="photoInputId"
            class="inline-flex min-h-11 cursor-pointer items-center rounded-full bg-field px-5 font-semibold hover:bg-mute has-focus-visible:outline-3 has-focus-visible:outline-accent-ink"
            :class="{ 'pointer-events-none opacity-60': photoBusy }"
          >
            {{ photoBusy ? $t('ui.wait') : $t('profile.photo.upload') }}
            <input
              :id="photoInputId"
              type="file"
              :accept="PHOTO_TYPES.join(',')"
              class="sr-only"
              :disabled="photoBusy"
              @change="uploadPhoto"
            />
          </label>
          <button
            v-if="isPhoto(user.avatar_url)"
            type="button"
            class="inline-flex min-h-11 items-center px-2 font-semibold underline underline-offset-4"
            :disabled="photoBusy"
            @click="removePhoto"
          >
            {{ $t('profile.photo.remove') }}
          </button>
          <span class="text-sm text-ink-2">{{ $t('profile.photo.hint') }}</span>
        </div>
        <p v-if="photoNotice" class="text-sm font-semibold" role="status">{{ photoNotice }}</p>
      </fieldset>
      <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
        <UiTextField
          v-model="form.city"
          :label="$t('profile.city')"
          :placeholder="$t('profile.cityPlaceholder')"
        />
        <UiSelect
          v-model="form.level"
          :label="$t('profile.level')"
          :options="levels"
          :placeholder="$t('profile.levelNone')"
        />
      </div>
      <fieldset class="flex flex-col gap-2">
        <legend class="mb-1.5 ml-1 text-[15px] font-semibold">{{ $t('profile.interests') }}</legend>
        <div class="flex flex-wrap gap-2">
          <UiChip
            v-for="s in sports"
            :key="s.id"
            :pressed="form.sportIds.includes(s.id)"
            @click="toggleFormSport(s.id)"
          >
            {{ s.name }}
          </UiChip>
        </div>
      </fieldset>
      <fieldset v-if="formSports.length" class="flex flex-col gap-2">
        <legend class="mb-1.5 ml-1 text-[15px] font-semibold">
          {{ $t('profile.sportLevels') }}
        </legend>
        <div class="grid grid-cols-1 gap-3 sm:grid-cols-2">
          <UiSelect
            v-for="s in formSports"
            :key="s.id"
            v-model="form.sportLevels[s.id]"
            :label="s.name"
            :options="levels"
            :placeholder="$t('profile.levelNone')"
          />
        </div>
      </fieldset>
      <UiTextArea
        v-model="form.bio"
        :label="$t('profile.bio')"
        :rows="3"
        :maxlength="BIO_MAX"
        :placeholder="$t('profile.bioPlaceholder')"
        :error="errors.bio"
      />
      <p v-if="formError" class="font-semibold text-danger" role="alert">✕ {{ formError }}</p>
      <div class="flex flex-col-reverse gap-2 sm:flex-row">
        <UiButton variant="ghost" @click="editing = false">{{ $t('common.cancel') }}</UiButton>
        <UiButton type="submit" :loading="busy">{{ $t('common.save') }}</UiButton>
      </div>
    </form>

    <template v-else>
      <section class="flex flex-col gap-3 rounded-card bg-card p-5">
        <p v-if="user.bio" class="whitespace-pre-line">{{ user.bio }}</p>
        <p v-else class="text-ink-2">{{ $t('profile.bioEmpty') }}</p>
        <div class="flex flex-col gap-2">
          <h2 class="font-bold">{{ $t('profile.interests') }}</h2>
          <div v-if="mySports.length" class="flex flex-wrap gap-2">
            <span
              v-for="s in mySports"
              :key="s.id"
              class="rounded-full bg-field px-4 py-2 font-semibold"
            >
              {{ s.name
              }}<template v-if="user.sport_levels[s.id]">
                · {{ LEVEL_LABELS[user.sport_levels[s.id]!] }}</template
              >
            </span>
          </div>
          <p v-else-if="sportsFailed && user.sport_ids.length" class="text-ink-2" role="alert">
            {{ $t('profile.interestsFailed') }}
            <button
              type="button"
              class="inline-flex min-h-8 items-center font-semibold text-ink underline underline-offset-4"
              @click="loadSports"
            >
              {{ $t('common.retry') }}
            </button>
          </p>
          <p v-else class="text-ink-2">{{ $t('profile.interestsNone') }}</p>
        </div>
        <div v-if="prefs.length" class="flex flex-col gap-2">
          <h2 class="font-bold">{{ $t('profile.prefs.title') }}</h2>
          <dl class="grid grid-cols-[auto_minmax(0,1fr)] gap-x-4 gap-y-1">
            <template v-for="p in prefs" :key="p.key">
              <dt class="text-ink-2">{{ $t(`profile.prefs.${p.key}`) }}</dt>
              <dd>{{ p.value }}</dd>
            </template>
          </dl>
        </div>
        <RouterLink
          :to="{ name: 'user', params: { id: user.id } }"
          class="inline-flex min-h-8 items-center w-fit text-sm font-semibold text-accent-ink underline underline-offset-4"
        >
          {{ $t('profile.publicView') }}
        </RouterLink>
      </section>

      <nav :aria-label="$t('profile.links.label')" class="grid grid-cols-1 gap-2 sm:grid-cols-3">
        <RouterLink
          v-for="l in links"
          :key="l.key"
          :to="l.to"
          class="flex items-center gap-3 rounded-card bg-card p-4 font-semibold text-ink no-underline hover:shadow-raised"
        >
          <NavIcon :name="l.icon" />
          {{ $t(`profile.links.${l.key}`) }}
        </RouterLink>
      </nav>

      <div class="flex flex-wrap gap-2">
        <RouterLink
          v-if="!session.isAdmin"
          :to="{ name: 'onboarding' }"
          class="rounded-full bg-field px-5 py-3 font-semibold text-ink no-underline hover:bg-mute"
        >
          {{ $t('profile.setup') }}
        </RouterLink>
        <RouterLink
          :to="{ name: 'login' }"
          class="rounded-full bg-field px-5 py-3 font-semibold text-ink no-underline hover:bg-mute"
        >
          {{ $t('common.switchRole') }}
        </RouterLink>
        <button
          type="button"
          class="rounded-full bg-field px-5 py-3 font-semibold hover:bg-mute"
          @click="logout"
        >
          {{ $t('common.logout') }}
        </button>
      </div>
      <button
        type="button"
        class="inline-flex min-h-8 items-center w-fit px-1 text-sm font-semibold text-ink-2 underline underline-offset-4 hover:text-ink"
        @click="openDelete"
      >
        {{ $t('profile.deleteAccount') }}
      </button>
    </template>

    <UiModal
      v-model:open="deleteOpen"
      :title="$t('profile.deleteTitle')"
      :description="$t('profile.deleteText')"
    >
      <UiTextField
        v-model="deletePassword"
        :label="$t('profile.password')"
        type="password"
        :hint="$t('profile.passwordHint')"
      />
      <p
        v-if="deleteError"
        class="rounded-field bg-warning-bg p-4 font-semibold text-warning-fg"
        role="alert"
      >
        {{ deleteError }}
      </p>
      <template #actions>
        <UiButton variant="ghost" @click="deleteOpen = false">{{ $t('common.cancel') }}</UiButton>
        <UiButton :loading="deleting" @click="deleteAccount">{{
          $t('profile.deleteForever')
        }}</UiButton>
      </template>
    </UiModal>
  </section>
</template>
