<script setup lang="ts">
import { computed, nextTick, onMounted, ref, useId, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import {
  ApiError,
  recommendationsApi,
  sportsApi,
  usersApi,
  type EventSummary,
  type Sport,
} from '@/api'
import EventListCard from '@/components/events/EventListCard.vue'
import CityField from '@/components/map/CityField.vue'
import AppLogo from '@/components/layout/AppLogo.vue'
import OnboardingArt, { type OnboardingArtName } from '@/components/illustrations/OnboardingArt.vue'
import AvatarArt from '@/components/illustrations/AvatarArt.vue'
import TeamPhotoArt from '@/components/illustrations/TeamPhotoArt.vue'
import { UiAvatar, UiButton, UiChip, UiTextField } from '@/components/ui'
import { reasonLabels } from '@/features/events/recommendations'
import { LEVEL_LABELS } from '@/features/events/filters'
import {
  AGE_PRESETS,
  MAX_SPORTS,
  STEPS,
  TIME_SLOTS,
  agePresetOf,
  answersFrom,
  clearLocalPrefs,
  loadLocalPrefs,
  saveLocalPrefs,
  toLocalPrefs,
  toProfileUpdate,
  toggleSport,
  validateAbout,
  validateAge,
  type OnboardingErrors,
  type TimeSlot,
} from '@/features/onboarding/onboarding'
import { AVATAR_PRESETS, presetAvatarUrl } from '@/features/profile/avatar'
import { PHOTO_TYPES, photoError } from '@/features/profile/profileForm'
import { safeRedirect } from '@/router/redirect'
import { useSessionStore } from '@/stores/session'
import { t } from '@/i18n'
import type { Level } from '@/api'

const session = useSessionStore()
const route = useRoute()
const router = useRouter()

// Шаг живёт в адресе: перезагрузка страницы не сбрасывает человека на начало.
const step = computed<number | 'done'>(() => {
  if (route.query.step === 'done') return 'done'
  const n = Number(route.query.step)
  return Number.isInteger(n) && n >= 1 && n <= STEPS ? n : 1
})

const user = session.user!

// Своё фото уходит на бэк сразу при выборе файла; в ответах остаётся его адрес.
const photoInputId = useId()
const photoBusy = ref(false)
const photoNotice = ref('')

async function uploadPhoto(e: Event) {
  const input = e.target as HTMLInputElement
  const file = input.files?.[0]
  input.value = ''
  if (!file) return
  photoNotice.value = photoError(file) ?? ''
  if (photoNotice.value) return
  photoBusy.value = true
  try {
    const updated = await usersApi.uploadAvatar(file)
    answers.value.avatar = updated.avatar_url ?? ''
    photoNotice.value = t('profile.photo.uploaded')
  } catch (err) {
    photoNotice.value = err instanceof ApiError ? err.message : t('profile.photo.failed')
  } finally {
    photoBusy.value = false
  }
}
const answers = ref(answersFrom(user, loadLocalPrefs(user.id)))
const errors = ref<OnboardingErrors>({})
const saving = ref(false)
const saveError = ref('')
const heading = ref<HTMLElement | null>(null)

const sports = ref<Sport[]>([])
const sportQuery = ref('')
const levels = Object.entries(LEVEL_LABELS) as [Level, string][]

// Рисунок и подложка каждого шага — как в макете онбординга.
const STEP_ART: { art: Exclude<OnboardingArtName, 'register' | 'result'>; bg: string }[] = [
  { art: 'photo', bg: 'bg-accent' },
  { art: 'about', bg: 'bg-accent-soft' },
  { art: 'interests', bg: 'bg-lilac' },
  { art: 'place', bg: 'bg-accent' },
  { art: 'level', bg: 'bg-orange' },
]
const RESULT_ART = { art: 'result' as const, bg: 'bg-accent' }
const art = computed(() => (step.value === 'done' ? RESULT_ART : STEP_ART[step.value - 1]!))

function go(next: number | 'done') {
  saveLocalPrefs(user.id, toLocalPrefs(answers.value))
  router.replace({ query: { ...route.query, step: String(next) } })
}

watch(step, async () => {
  errors.value = {}
  await nextTick()
  heading.value?.focus()
  window.scrollTo({ top: 0 })
})

function next() {
  if (step.value === 2) {
    errors.value = validateAbout(answers.value)
    if (Object.keys(errors.value).length) return
  }
  if (step.value === STEPS) return finish()
  go((step.value as number) + 1)
}

function back() {
  if (typeof step.value === 'number' && step.value > 1) go(step.value - 1)
}

/** Сохранить ответы в профиль; черновик в браузере больше не нужен. */
async function finish() {
  errors.value = { ...validateAbout(answers.value), ...validateAge(answers.value) }
  if (errors.value.name || errors.value.birthYear) return go(2)
  if (Object.keys(errors.value).length) return
  saving.value = true
  saveError.value = ''
  try {
    await usersApi.updateMe(toProfileUpdate(answers.value))
    await session.refresh()
    clearLocalPrefs(user.id)
    router.replace({ query: { ...route.query, step: 'done' } })
  } catch (e) {
    saveError.value = e instanceof ApiError ? e.message : t('onboarding.saveFailed')
  } finally {
    saving.value = false
  }
}

const filteredSports = computed(() => {
  const q = sportQuery.value.trim().toLowerCase()
  return q ? sports.value.filter((s) => s.name.toLowerCase().includes(q)) : sports.value
})
const chosenSports = computed(() => sports.value.filter((s) => s.id in answers.value.sports))
const chosenCount = computed(() => Object.keys(answers.value.sports).length)

function toggleTime(id: TimeSlot) {
  const t = answers.value.times
  answers.value.times = t.includes(id) ? t.filter((x) => x !== id) : [...t, id]
}
const agePreset = computed(() => agePresetOf(answers.value))
function setAgePreset(id: string) {
  const p = AGE_PRESETS.find((x) => x.id === id)!
  answers.value.ageFrom = p.from
  answers.value.ageTo = p.to
}

// Итог: подборка бэка по только что сохранённому профилю (интересы, город, уровень) и истории записей.
const picked = ref<{ event: EventSummary; reasons: string[] }[]>([])
const pickedState = ref<'idle' | 'loading' | 'ready' | 'error'>('idle')
async function loadPicked() {
  pickedState.value = 'loading'
  try {
    const list = await recommendationsApi.events({ limit: 50 })
    picked.value = list.map((r) => ({ event: r.event, reasons: reasonLabels(r.reasons) }))
    pickedState.value = 'ready'
  } catch {
    pickedState.value = 'error'
  }
}
watch(
  step,
  (s) => {
    if (s === 'done') loadPicked()
  },
  { immediate: true },
)
const pickedTitle = computed(() => {
  const n = picked.value.length
  return t('onboarding.result.count', n)
})
const afterwards = computed(() => safeRedirect(route.query.redirect))

onMounted(async () => {
  try {
    sports.value = await sportsApi.list()
  } catch {
    sports.value = []
  }
})
</script>

<template>
  <!-- Онбординг перекрывает меню приложения: на эти минуты человеку нужен только он. -->
  <div class="fixed inset-0 z-50 overflow-y-auto bg-page">
    <div class="flex min-h-full flex-col gap-4 p-4 lg:px-6 2xl:px-10">
      <header
        class="flex flex-wrap items-center justify-between gap-x-2 gap-y-3 rounded-card bg-card p-3"
      >
        <AppLogo />
        <div
          v-if="step !== 'done'"
          class="order-last flex w-full min-w-0 items-center gap-3 md:order-none md:w-auto md:max-w-md md:flex-[1_1_220px]"
        >
          <span class="text-sm font-semibold whitespace-nowrap text-ink-2">
            {{ $t('onboarding.step', { step, total: STEPS }) }}
          </span>
          <div
            class="flex flex-1 gap-1.5"
            role="progressbar"
            :aria-valuemin="1"
            :aria-valuemax="STEPS"
            :aria-valuenow="step"
            :aria-label="$t('onboarding.step', { step, total: STEPS })"
          >
            <span
              v-for="i in STEPS"
              :key="i"
              class="h-2 flex-1 rounded-full"
              :class="i <= (step as number) ? 'bg-ink' : 'bg-mute'"
            />
          </div>
        </div>
        <div class="flex items-center gap-1">
          <template v-if="step !== 'done'">
            <button
              type="button"
              class="inline-flex min-h-11 items-center rounded-full px-2.5 text-[15px] font-semibold text-ink-2 hover:text-ink"
              @click="step === STEPS ? finish() : go((step as number) + 1)"
            >
              {{ $t('onboarding.skip') }}
            </button>
            <button
              type="button"
              :aria-label="$t('onboarding.finishNow')"
              class="grid size-11 place-items-center rounded-full bg-field hover:bg-mute"
              @click="finish"
            >
              ✕
            </button>
          </template>
          <button
            v-else
            type="button"
            class="inline-flex min-h-11 items-center rounded-full px-4 font-semibold underline underline-offset-4"
            @click="go(1)"
          >
            {{ $t('onboarding.setup') }}
          </button>
        </div>
      </header>

      <div class="flex flex-1 flex-col gap-4 lg:flex-row">
        <section
          class="relative flex min-w-0 flex-col justify-between gap-4 overflow-hidden rounded-2xl p-4 lg:flex-[1_1_400px] lg:p-8"
          :class="[
            art.bg,
            art.art === 'interests' && 'min-h-[380px] short:min-h-0 lg:min-h-[560px]',
          ]"
          aria-hidden="true"
        >
          <!-- Командное фото занимает всю панель, подпись лежит поверх (макет «Чем любите заниматься?»). -->
          <TeamPhotoArt v-if="art.art === 'interests'" class="absolute inset-0 short:hidden" />
          <p
            class="relative max-w-xs -rotate-3 self-start rounded-sm bg-cream px-3.5 py-1 font-hand text-2xl leading-tight font-bold"
          >
            {{
              step === 'done'
                ? $t('onboarding.phrases.done', { name: answers.name })
                : $t(`onboarding.phrases.${art.art}`)
            }}
          </p>
          <div
            v-if="art.art !== 'interests'"
            class="mx-auto w-full max-w-[280px] short:hidden sm:max-w-[420px] lg:max-w-none"
          >
            <OnboardingArt :key="art.art" :name="art.art" />
          </div>
        </section>

        <section
          class="flex min-w-0 flex-col gap-5 rounded-2xl bg-card p-6 lg:flex-[1.25_1_480px] lg:p-9"
          :aria-labelledby="`onb-title`"
        >
          <!-- 1. Фото -->
          <template v-if="step === 1">
            <div class="flex flex-col gap-2">
              <h1
                id="onb-title"
                ref="heading"
                tabindex="-1"
                class="text-3xl font-bold outline-none lg:text-4xl"
              >
                {{ $t('onboarding.photo.title') }}
              </h1>
              <p class="text-lg text-ink-2">
                {{ $t('onboarding.photo.text') }}
              </p>
            </div>
            <div class="flex items-center gap-4">
              <UiAvatar :name="answers.name" :url="answers.avatar" size="xl" />
              <div class="flex flex-col items-start gap-1.5">
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
                <p class="text-sm text-ink-2" role="status">
                  {{ photoNotice || $t('onboarding.photo.upload') }}
                </p>
              </div>
            </div>
            <fieldset class="flex flex-col gap-2">
              <legend class="mb-2 font-bold">{{ $t('onboarding.photo.pick') }}</legend>
              <div class="grid grid-cols-4 gap-2.5 sm:grid-cols-6">
                <label
                  v-for="p in AVATAR_PRESETS"
                  :key="p.id"
                  :title="p.label"
                  class="relative cursor-pointer overflow-hidden rounded-lg ring-ink has-checked:ring-3 has-focus-visible:outline-3 has-focus-visible:outline-offset-2 has-focus-visible:outline-accent-strong"
                >
                  <input
                    v-model="answers.avatar"
                    type="radio"
                    name="avatar"
                    :value="presetAvatarUrl(p.id)"
                    class="sr-only"
                  />
                  <span class="sr-only">{{ p.label }}</span>
                  <span class="block aspect-square">
                    <AvatarArt v-if="p.art" :kind="p.art" />
                  </span>
                  <span
                    v-if="answers.avatar === presetAvatarUrl(p.id)"
                    aria-hidden="true"
                    class="absolute top-1 right-1 grid size-6 place-items-center rounded-full bg-[#111111] text-xs font-bold text-[#ffffff]"
                    >✓</span
                  >
                </label>
              </div>
            </fieldset>
          </template>

          <!-- 2. О себе -->
          <template v-else-if="step === 2">
            <div class="flex flex-col gap-2">
              <h1
                id="onb-title"
                ref="heading"
                tabindex="-1"
                class="text-3xl font-bold outline-none lg:text-4xl"
              >
                {{ $t('onboarding.about.title') }}
              </h1>
              <p class="text-lg text-ink-2">{{ $t('onboarding.about.text') }}</p>
            </div>
            <div class="flex max-w-xl flex-col gap-4">
              <UiTextField
                v-model="answers.name"
                :label="$t('onboarding.about.name')"
                required
                :error="errors.name"
              />
              <UiTextField
                v-model="answers.birthYear"
                :label="`${$t('onboarding.about.birthYear')} ${$t('onboarding.optional')}`"
                type="number"
                :placeholder="$t('onboarding.about.birthYearPlaceholder')"
                :hint="$t('onboarding.about.birthYearHint')"
                :error="errors.birthYear"
              />
              <CityField
                v-model="answers.city"
                :label="`${$t('onboarding.about.city')} ${$t('onboarding.optional')}`"
                :placeholder="$t('onboarding.about.cityPlaceholder')"
                :hint="$t('onboarding.about.cityHint')"
              />
            </div>
          </template>

          <!-- 3. Интересы -->
          <template v-else-if="step === 3">
            <div class="flex flex-col gap-2">
              <h1
                id="onb-title"
                ref="heading"
                tabindex="-1"
                class="text-3xl font-bold outline-none lg:text-4xl"
              >
                {{ $t('onboarding.interests.title') }}
              </h1>
              <p class="text-lg text-ink-2">
                {{ $t('onboarding.interests.text', { max: MAX_SPORTS }) }}
              </p>
            </div>
            <UiTextField
              v-model="sportQuery"
              :label="$t('onboarding.interests.search')"
              hide-label
              type="search"
              :placeholder="$t('onboarding.interests.search')"
            />
            <div
              class="flex flex-wrap gap-2"
              role="group"
              :aria-label="$t('onboarding.interests.sports')"
            >
              <UiChip
                v-for="s in filteredSports"
                :key="s.id"
                :pressed="s.id in answers.sports"
                :disabled="!(s.id in answers.sports) && chosenCount >= MAX_SPORTS"
                @click="answers.sports = toggleSport(answers.sports, s.id)"
              >
                {{ s.name }}
              </UiChip>
            </div>
            <div v-if="chosenSports.length" class="flex flex-col gap-3 rounded-field bg-field p-4">
              <div class="flex items-baseline justify-between gap-2">
                <h2 class="font-bold">{{ $t('onboarding.interests.yourLevel') }}</h2>
                <span class="text-sm text-ink-2">{{
                  $t('onboarding.interests.chosen', { n: chosenCount, max: MAX_SPORTS })
                }}</span>
              </div>
              <div
                v-for="s in chosenSports"
                :key="s.id"
                class="flex flex-wrap items-center gap-2"
                role="group"
                :aria-label="$t('onboarding.interests.levelOf', { sport: s.name })"
              >
                <span class="w-full font-semibold">{{ s.name }}</span>
                <UiChip
                  v-for="[lv, label] in levels"
                  :key="lv"
                  :pressed="answers.sports[s.id] === lv"
                  @click="answers.sports = { ...answers.sports, [s.id]: lv }"
                >
                  {{ label }}
                </UiChip>
              </div>
            </div>
          </template>

          <!-- 4. Где и когда -->
          <template v-else-if="step === 4">
            <div class="flex flex-col gap-2">
              <h1
                id="onb-title"
                ref="heading"
                tabindex="-1"
                class="text-3xl font-bold outline-none lg:text-4xl"
              >
                {{ $t('onboarding.place.title') }}
              </h1>
              <p class="text-lg text-ink-2">{{ $t('onboarding.place.text') }}</p>
            </div>
            <div class="flex flex-col gap-2">
              <label for="onb-radius" class="flex items-baseline justify-between font-bold">
                {{ $t('onboarding.place.radius') }}
                <span class="text-xl">{{
                  $t('onboarding.place.km', { n: answers.radiusKm })
                }}</span>
              </label>
              <input
                id="onb-radius"
                v-model.number="answers.radiusKm"
                type="range"
                min="1"
                max="50"
                class="w-full accent-ink"
              />
              <div class="flex justify-between text-sm text-ink-2">
                <span>{{ $t('onboarding.place.km', { n: 1 }) }}</span
                ><span>{{ $t('onboarding.place.fromCity') }}</span
                ><span>{{ $t('onboarding.place.km', { n: 50 }) }}</span>
              </div>
            </div>
            <fieldset class="flex flex-col gap-2">
              <legend class="mb-2 font-bold">{{ $t('onboarding.place.when') }}</legend>
              <div class="grid gap-2 sm:grid-cols-2">
                <button
                  v-for="slot in TIME_SLOTS"
                  :key="slot.id"
                  type="button"
                  class="flex min-h-16 flex-col items-start justify-center rounded-field px-4 py-2 text-left"
                  :class="
                    answers.times.includes(slot.id) ? 'bg-ink text-white' : 'bg-field hover:bg-mute'
                  "
                  :aria-pressed="answers.times.includes(slot.id)"
                  @click="toggleTime(slot.id)"
                >
                  <b>{{ slot.label }}</b>
                  <span class="text-sm opacity-80">{{ slot.hint }}</span>
                </button>
              </div>
              <p class="text-sm text-ink-2">{{ $t('onboarding.place.several') }}</p>
            </fieldset>
          </template>

          <!-- 5. С кем комфортно -->
          <template v-else-if="step === 5">
            <div class="flex flex-col gap-2">
              <h1
                id="onb-title"
                ref="heading"
                tabindex="-1"
                class="text-3xl font-bold outline-none lg:text-4xl"
              >
                {{ $t('onboarding.age.title') }}
              </h1>
              <p class="text-lg text-ink-2">
                {{ $t('onboarding.age.text') }}
              </p>
            </div>
            <fieldset class="flex flex-col gap-3">
              <legend class="mb-2 font-bold">
                {{ $t('onboarding.age.legend') }} {{ $t('onboarding.optional') }}
              </legend>
              <div class="flex flex-wrap gap-2">
                <UiChip
                  v-for="p in AGE_PRESETS"
                  :key="p.id"
                  :pressed="agePreset === p.id"
                  @click="setAgePreset(p.id)"
                >
                  {{ p.label }}
                </UiChip>
              </div>
              <div class="grid max-w-sm grid-cols-2 gap-3">
                <UiTextField
                  v-model="answers.ageFrom"
                  :label="$t('onboarding.age.from')"
                  type="number"
                  :error="errors.ageFrom"
                />
                <UiTextField
                  v-model="answers.ageTo"
                  :label="$t('onboarding.age.to')"
                  type="number"
                  :error="errors.ageTo"
                />
              </div>
              <p class="text-sm text-ink-2">
                {{ $t('onboarding.age.hint') }}
              </p>
            </fieldset>
          </template>

          <!-- Итог -->
          <template v-else>
            <div class="flex flex-col gap-2">
              <h1
                id="onb-title"
                ref="heading"
                tabindex="-1"
                class="text-3xl font-bold outline-none lg:text-4xl"
              >
                <i18n-t
                  v-if="pickedState === 'ready'"
                  keypath="onboarding.result.title"
                  scope="global"
                >
                  <template #count>
                    <span class="text-accent-strong">{{ pickedTitle }}</span>
                  </template>
                </i18n-t>
                <template v-else>{{ $t('onboarding.result.titleLoading') }}</template>
              </h1>
              <p class="text-lg text-ink-2">
                {{ $t('onboarding.result.text') }}
              </p>
            </div>
            <div v-if="pickedState === 'loading'" class="flex flex-col gap-3">
              <div v-for="i in 3" :key="i" class="h-24 animate-pulse rounded-card bg-field" />
            </div>
            <p v-else-if="pickedState === 'error'" class="rounded-field bg-field p-4" role="alert">
              {{ $t('onboarding.result.failed') }}
            </p>
            <p v-else-if="!picked.length" class="rounded-field bg-field p-4 text-ink-2">
              {{ $t('onboarding.result.empty') }}
            </p>
            <ul v-else class="flex flex-col gap-3">
              <li
                v-for="{ event, reasons } in picked.slice(0, 3)"
                :key="event.id"
                class="flex min-w-0 flex-col gap-1.5"
              >
                <EventListCard :event="event" :reasons="reasons" />
              </li>
            </ul>
            <RouterLink
              :to="afterwards"
              class="mt-auto inline-flex min-h-13 items-center justify-center rounded-full bg-ink px-6 font-bold text-white no-underline"
            >
              {{ $t('onboarding.result.open') }}
            </RouterLink>
          </template>

          <p v-if="saveError" class="font-semibold text-danger" role="alert">✕ {{ saveError }}</p>

          <div v-if="step !== 'done'" class="mt-auto flex items-center justify-between gap-3 pt-2">
            <UiButton v-if="step > 1" variant="ghost" class="!w-auto" @click="back">
              {{ $t('onboarding.back') }}
            </UiButton>
            <span v-else />
            <UiButton class="!w-auto !px-8" :loading="saving" @click="next">
              {{ step === STEPS ? $t('onboarding.done') : $t('onboarding.next') }}
            </UiButton>
          </div>
        </section>
      </div>
    </div>
  </div>
</template>
