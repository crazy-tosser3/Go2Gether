<script setup lang="ts">
import { computed, nextTick, onMounted, ref, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { ApiError, adminApi, sportsApi, type Sport } from '@/api'
import AdminNav from '@/components/admin/AdminNav.vue'
import StoryViewer from '@/components/home/StoryViewer.vue'
import { UiButton, UiCheckbox, UiSelect, UiTextArea, UiTextField } from '@/components/ui'
import type { StoryPosition } from '@/features/home/stories'
import {
  BODY_MAX,
  SLIDES_MAX,
  SUBTITLE_MAX,
  TITLE_MAX,
  emptyBannerForm,
  bannerFormFrom,
  hasErrors,
  moveSlide,
  toBannerInput,
  validateBanner,
  type BannerErrors,
  type BannerForm,
} from '@/features/safety/bannerForm'
import { SAFETY_CATEGORIES, bannerToStory, categoryLabel } from '@/features/safety/safety'
import { t } from '@/i18n'

const route = useRoute()
const router = useRouter()

const id = route.name === 'admin-safety-edit' ? Number(route.params.id) : null
const form = ref<BannerForm>(emptyBannerForm())
const errors = ref<BannerErrors>({ slideBodies: {} })
const state = ref<'loading' | 'ready' | 'error'>(id ? 'loading' : 'ready')
const loadMessage = ref('')
const saving = ref(false)
const formError = ref('')
const sports = ref<Sport[]>([])
const preview = ref<StoryPosition | null>(null)

const categoryOptions = computed(() =>
  SAFETY_CATEGORIES.map((c) => ({ value: c, label: categoryLabel(c) })),
)
const sportOptions = computed(() =>
  sports.value.map((s) => ({ value: String(s.id), label: s.name })),
)
const sport = computed({
  get: () => (form.value.sportId ? String(form.value.sportId) : ''),
  set: (v: string) => (form.value.sportId = v ? Number(v) : null),
})
const category = computed({
  get: () => form.value.category,
  set: (v: string) => (form.value.category = v as BannerForm['category']),
})

// Просмотр — тем же просмотрщиком, что увидят пользователи.
const previewStories = computed(() => [
  bannerToStory(
    {
      id: id ?? 0,
      title: form.value.title || t('safety.form.titlePlaceholder'),
      subtitle: form.value.subtitle || null,
      category: form.value.category || 'safety_rules',
      cover_url: null,
      accent_color: form.value.accentColor,
      sport_id: form.value.sportId,
      published: form.value.published,
      disclaimer: null,
      slides: form.value.slides.map((s, i) => ({
        position: i + 1,
        title: s.title || null,
        body: s.body || '…',
        image_url: null,
      })),
    },
    sports.value.find((s) => s.id === form.value.sportId)?.name,
  ),
])

async function load() {
  try {
    sports.value = await sportsApi.list()
  } catch {
    sports.value = []
  }
  if (!id) return
  state.value = 'loading'
  try {
    form.value = bannerFormFrom(await adminApi.safetyBanner(id))
    state.value = 'ready'
  } catch (e) {
    loadMessage.value = e instanceof ApiError ? e.message : t('safety.loadFailed')
    state.value = 'error'
  }
}

onMounted(load)

// После первой попытки сохранить ошибки пересчитываем на ходу — исправленное поле сразу гаснет.
const tried = ref(false)
watch(
  form,
  (f) => {
    if (tried.value) errors.value = validateBanner(f)
  },
  { deep: true },
)

function addSlide() {
  if (form.value.slides.length < SLIDES_MAX) form.value.slides.push({ title: '', body: '' })
}

async function submit() {
  formError.value = ''
  tried.value = true
  errors.value = validateBanner(form.value)
  if (hasErrors(errors.value)) {
    await nextTick()
    document.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus()
    return
  }
  saving.value = true
  try {
    const body = toBannerInput(form.value)
    if (id) await adminApi.updateSafetyBanner(id, body)
    else await adminApi.createSafetyBanner(body)
    await router.push({ name: 'admin-safety' })
  } catch (e) {
    formError.value = e instanceof ApiError ? e.message : t('safety.saveFailed')
  } finally {
    saving.value = false
  }
}
</script>

<template>
  <section class="flex flex-col gap-5">
    <h1 class="text-[30px] lg:text-[42px]">
      {{ id ? $t('safety.editTitle') : $t('safety.newTitle') }}
    </h1>
    <AdminNav />

    <div v-if="state === 'loading'" class="h-96 animate-pulse rounded-card bg-card" />
    <div
      v-else-if="state === 'error'"
      class="flex flex-col items-start gap-3 rounded-card bg-card p-6"
      role="alert"
    >
      <p class="font-bold">{{ loadMessage }}</p>
      <RouterLink :to="{ name: 'admin-safety' }" class="font-semibold underline">
        {{ $t('safety.back') }}
      </RouterLink>
    </div>

    <form v-else class="flex max-w-3xl flex-col gap-4" novalidate @submit.prevent="submit">
      <div class="flex flex-col gap-4 rounded-card bg-card p-5">
        <UiTextField
          v-model="form.title"
          :label="$t('safety.form.title')"
          required
          :maxlength="TITLE_MAX"
          :placeholder="$t('safety.form.titlePlaceholder')"
          :error="errors.title"
        />
        <UiTextField
          v-model="form.subtitle"
          :label="$t('safety.form.subtitle')"
          :maxlength="SUBTITLE_MAX"
          :placeholder="$t('safety.form.subtitlePlaceholder')"
          :error="errors.subtitle"
        />
        <div class="grid grid-cols-[minmax(0,1fr)] gap-4 sm:grid-cols-2">
          <div class="flex flex-col gap-1.5">
            <UiSelect
              v-model="category"
              :label="$t('safety.form.category')"
              :options="categoryOptions"
              :placeholder="$t('safety.form.categoryPlaceholder')"
            />
            <p v-if="errors.category" class="ml-1 text-sm font-semibold text-danger" role="alert">
              ✕ {{ errors.category }}
            </p>
          </div>
          <UiSelect
            v-model="sport"
            :label="$t('safety.form.sport')"
            :options="sportOptions"
            :placeholder="$t('safety.allSports')"
          />
        </div>
        <div class="flex flex-wrap items-end gap-3">
          <label class="flex flex-col gap-1.5">
            <span class="ml-1 text-[15px] font-semibold">{{ $t('safety.form.color') }}</span>
            <span class="flex items-center gap-2">
              <input
                v-model="form.accentColor"
                type="color"
                class="h-12 w-16 cursor-pointer rounded-field bg-field p-1"
                :aria-invalid="errors.accentColor ? 'true' : undefined"
              />
              <code class="text-ink-2">{{ form.accentColor }}</code>
            </span>
          </label>
          <p v-if="errors.accentColor" class="text-sm font-semibold text-danger" role="alert">
            ✕ {{ errors.accentColor }}
          </p>
        </div>
        <UiCheckbox v-model="form.published">{{ $t('safety.form.published') }}</UiCheckbox>
      </div>

      <section class="flex flex-col gap-3 rounded-card bg-card p-5" aria-labelledby="slides-title">
        <div class="flex items-baseline justify-between gap-2">
          <h2 id="slides-title" class="text-xl font-bold">{{ $t('safety.form.slides') }}</h2>
          <span class="text-sm text-ink-2">{{ form.slides.length }} / {{ SLIDES_MAX }}</span>
        </div>
        <p v-if="errors.slides" class="font-semibold text-danger" role="alert">
          ✕ {{ errors.slides }}
        </p>
        <div
          v-for="(s, i) in form.slides"
          :key="i"
          class="flex flex-col gap-3 rounded-field bg-field p-4"
          role="group"
          :aria-label="$t('safety.form.slideN', { n: i + 1 })"
        >
          <div class="flex items-center justify-between gap-2">
            <b>{{ $t('safety.form.slideN', { n: i + 1 }) }}</b>
            <div class="flex gap-1">
              <button
                type="button"
                class="grid size-10 place-items-center rounded-full bg-card disabled:opacity-40"
                :aria-label="$t('safety.form.up')"
                :disabled="i === 0"
                @click="form.slides = moveSlide(form.slides, i, -1)"
              >
                ↑
              </button>
              <button
                type="button"
                class="grid size-10 place-items-center rounded-full bg-card disabled:opacity-40"
                :aria-label="$t('safety.form.down')"
                :disabled="i === form.slides.length - 1"
                @click="form.slides = moveSlide(form.slides, i, 1)"
              >
                ↓
              </button>
              <button
                type="button"
                class="grid size-10 place-items-center rounded-full bg-card disabled:opacity-40"
                :aria-label="$t('safety.form.removeSlide')"
                :disabled="form.slides.length === 1"
                @click="form.slides.splice(i, 1)"
              >
                ✕
              </button>
            </div>
          </div>
          <UiTextField
            v-model="s.title"
            :label="$t('safety.form.slideTitle')"
            :maxlength="TITLE_MAX"
          />
          <UiTextArea
            v-model="s.body"
            :label="$t('safety.form.slideBody')"
            :rows="3"
            :maxlength="BODY_MAX"
            :error="errors.slideBodies[i]"
          />
        </div>
        <UiButton
          variant="ghost"
          class="!w-auto self-start"
          :disabled="form.slides.length >= SLIDES_MAX"
          @click="addSlide"
        >
          {{ $t('safety.form.addSlide') }}
        </UiButton>
      </section>

      <p v-if="formError" class="font-semibold text-danger" role="alert">✕ {{ formError }}</p>

      <div class="flex flex-wrap gap-2">
        <UiButton type="submit" class="!w-auto !px-8" :loading="saving">
          {{ $t('common.save') }}
        </UiButton>
        <UiButton variant="ghost" class="!w-auto" @click="preview = { story: 0, slide: 0 }">
          {{ $t('safety.form.preview') }}
        </UiButton>
        <RouterLink
          :to="{ name: 'admin-safety' }"
          class="inline-flex min-h-13 items-center px-4 font-semibold text-ink-2 underline underline-offset-4"
        >
          {{ $t('common.cancel') }}
        </RouterLink>
      </div>
    </form>

    <StoryViewer v-model:position="preview" :stories="previewStories" />
  </section>
</template>
