<script setup lang="ts">
import { computed, nextTick, onMounted, reactive, ref } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { ApiError, eventsApi, sportsApi } from '@/api'
import { UiButton, UiCombobox, UiSelect, UiTextField, type UiOption } from '@/components/ui'
import UiTextArea from '@/components/ui/UiTextArea.vue'
import LocationPicker from '@/components/map/LocationPicker.vue'
import { LEVEL_LABELS } from '@/features/events/filters'
import { useSessionStore } from '@/stores/session'
import {
  authorOptions,
  emptyEventForm,
  eventFormFrom,
  repeatFormFrom,
  mapServerErrors,
  newSportValue,
  toEventInput,
  toEventUpdate,
  validateEventEdit,
  validateEventForm,
  type EventFormErrors,
  type EventFormState,
} from '@/utils/eventForm'

const router = useRouter()
const route = useRoute()
const session = useSessionStore()

// Город по умолчанию — из профиля: по нему мероприятие находят в фильтре каталога.
const form = reactive({ ...emptyEventForm(), city: session.user?.city ?? '' })
const errors = ref<EventFormErrors>({})
const formError = ref('')
const submitting = ref(false)

// /events/:id/edit — та же форма, но заполнена мероприятием и сохраняет только изменения.
const editId = route.name === 'event-edit' ? Number(route.params.id) : null
const original = ref<EventFormState | null>(null)
// /events/new?repeat=<id> — «Повторить» из архива: поля прошлого мероприятия, дата и время заново.
const repeatId = !editId && route.query.repeat ? Number(route.query.repeat) : null
const repeatNotice = ref('')
const loadState = ref<'loading' | 'ready' | 'error' | 'locked'>(
  editId || repeatId ? 'loading' : 'ready',
)
const loadMessage = ref('')

async function loadRepeat(id: number) {
  try {
    const event = await eventsApi.get(id)
    Object.assign(
      form,
      repeatFormFrom(
        event,
        session.approvedOrganizations.map((o) => o.id),
      ),
    )
    repeatNotice.value = `Скопировано из «${event.title}». Укажите новые дату и время.`
  } catch {
    repeatNotice.value = 'Не получилось скопировать прошлое мероприятие — заполните поля вручную.'
  } finally {
    loadState.value = 'ready'
  }
}

async function loadEvent() {
  if (repeatId) return loadRepeat(repeatId)
  if (!editId) return
  loadState.value = 'loading'
  try {
    const event = await eventsApi.get(editId)
    if (!event.is_organizer) {
      loadMessage.value = 'Изменять мероприятие может только его организатор.'
      loadState.value = 'locked'
    } else if (event.status !== 'published' || event.is_past) {
      loadMessage.value = 'Прошедшее или снятое мероприятие изменить нельзя.'
      loadState.value = 'locked'
    } else {
      original.value = eventFormFrom(event)
      Object.assign(form, original.value)
      loadState.value = 'ready'
    }
  } catch (e) {
    loadMessage.value = e instanceof ApiError ? e.message : 'Не получилось загрузить мероприятие.'
    loadState.value = 'error'
  }
}

const sportOptions = ref<UiOption[]>([])
const sportsFailed = ref(false)

const levels: UiOption[] = Object.entries(LEVEL_LABELS).map(([value, label]) => ({
  value,
  label,
}))

const joinModes = [
  {
    value: 'open' as const,
    title: 'Свободно',
    text: 'Сразу участник',
  },
  {
    value: 'approval' as const,
    title: 'По заявке',
    text: 'Вы решаете, кого принять',
  },
]

const authors = computed(() => authorOptions(session.approvedOrganizations))

// «Мероприятие от организации» со страницы «Моя организация» сразу выбирает её.
const preselected = Number(route.query.organization)
if (session.approvedOrganizations.some((o) => o.id === preselected))
  form.organizationId = preselected
const isNewSport = computed(() => form.sport?.startsWith('new:') ?? false)
const now = new Date()
const today = [
  now.getFullYear(),
  String(now.getMonth() + 1).padStart(2, '0'),
  String(now.getDate()).padStart(2, '0'),
].join('-')

onMounted(async () => {
  loadEvent()
  try {
    const sports = await sportsApi.list()
    sportOptions.value = sports.map((s) => ({ value: String(s.id), label: s.name }))
  } catch {
    sportsFailed.value = true
  }
})

function addSport(label: string) {
  const value = newSportValue(label)
  sportOptions.value = [
    ...sportOptions.value.filter((o) => !o.value.startsWith('new:')),
    { value, label },
  ]
  form.sport = value
}

async function focusFirstError() {
  await nextTick()
  document.querySelector<HTMLElement>('[aria-invalid="true"]')?.focus()
}

async function submit() {
  formError.value = ''
  errors.value = original.value ? validateEventEdit(form, original.value) : validateEventForm(form)
  if (Object.keys(errors.value).length) return focusFirstError()

  submitting.value = true
  try {
    if (editId && original.value) {
      const update = toEventUpdate(form, original.value)
      if (Object.keys(update).length) await eventsApi.update(editId, update)
      await router.push({ name: 'event', params: { id: editId } })
      return
    }
    const { id } = await eventsApi.create(toEventInput(form))
    await router.push({ name: 'event', params: { id } })
  } catch (e) {
    if (e instanceof ApiError && e.status === 422 && Object.keys(e.fields).length) {
      const mapped = mapServerErrors(e.fields)
      errors.value = mapped.errors
      formError.value = mapped.rest.join(' ') || e.message
      focusFirstError()
    } else {
      formError.value =
        e instanceof ApiError
          ? e.message
          : editId
            ? 'Не получилось сохранить. Попробуйте ещё раз.'
            : 'Не получилось опубликовать. Попробуйте ещё раз.'
    }
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <section class="mx-auto flex w-full max-w-2xl flex-col gap-6">
    <h1 class="text-[30px] lg:text-[42px]">
      {{ editId ? 'Изменить мероприятие' : 'Новое мероприятие' }}
    </h1>

    <div v-if="loadState === 'loading'" class="h-96 animate-pulse rounded-card bg-card" />
    <section
      v-else-if="loadState !== 'ready'"
      class="flex flex-col items-start gap-3 rounded-card bg-card p-6"
      role="alert"
    >
      <p class="font-bold">{{ loadMessage }}</p>
      <UiButton v-if="loadState === 'error'" variant="ghost" class="!w-auto" @click="loadEvent">
        Повторить
      </UiButton>
      <RouterLink
        v-else
        :to="{ name: 'event', params: { id: editId } }"
        class="inline-flex min-h-8 items-center font-semibold underline underline-offset-4"
      >
        ← К мероприятию
      </RouterLink>
    </section>

    <form v-else class="flex flex-col gap-4" novalidate @submit.prevent="submit">
      <p v-if="repeatNotice" class="rounded-card bg-accent-soft p-4 text-accent-ink" role="status">
        {{ repeatNotice }}
      </p>
      <p v-if="editId" class="rounded-card bg-accent-soft p-4 text-accent-ink">
        Участники увидят отметку «изменено». От чьего имени мероприятие, поменять нельзя.
      </p>
      <fieldset v-if="!editId && authors.length > 1">
        <legend class="mb-2 ml-1 text-[15px] font-semibold">От чьего имени</legend>
        <div class="flex flex-wrap gap-1.5 rounded-xl bg-card p-1.5">
          <label
            v-for="a in authors"
            :key="a.value ?? 'me'"
            class="min-w-0 flex-1 cursor-pointer truncate rounded-full px-4 py-3 text-center font-semibold whitespace-nowrap has-checked:bg-ink has-checked:text-white has-focus-visible:outline-3 has-focus-visible:-outline-offset-4 has-focus-visible:outline-accent-ink"
          >
            <input
              v-model="form.organizationId"
              type="radio"
              name="author"
              :value="a.value"
              class="sr-only"
            />
            {{ a.label }}
          </label>
        </div>
      </fieldset>

      <div class="flex flex-col gap-4 rounded-card bg-card p-5">
        <h2 class="text-xl font-bold">Что и когда</h2>
        <UiTextField
          v-model="form.title"
          label="Название"
          required
          placeholder="Теннис после работы"
          :error="errors.title"
        />
        <UiCombobox
          v-model="form.sport"
          label="Направление"
          required
          allow-create
          :options="sportOptions"
          placeholder="Футбол, йога, шахматы…"
          :hint="sportsFailed ? 'Список не загрузился — впишите своё' : undefined"
          :error="errors.sport"
          @create="addSport"
        />
        <p v-if="isNewSport" class="-mt-2 ml-1 text-sm font-semibold">✦ Новое направление</p>
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-3">
          <UiTextField
            v-model="form.date"
            type="date"
            label="Дата"
            required
            :min="today"
            :error="errors.date"
          />
          <UiTextField
            v-model="form.startTime"
            type="time"
            label="Начало"
            required
            :error="errors.startTime"
          />
          <UiTextField
            v-model="form.endTime"
            type="time"
            label="Окончание"
            :error="errors.endTime"
          />
        </div>
        <UiTextArea
          v-model="form.description"
          label="Описание"
          :maxlength="2000"
          placeholder="Что будем делать и для кого"
          :error="errors.description"
        />
      </div>

      <div class="flex flex-col gap-4 rounded-card bg-card p-5">
        <h2 class="text-xl font-bold">Где</h2>
        <UiTextField
          v-model="form.city"
          label="Город"
          placeholder="Красноярск"
          :error="errors.city"
        />
        <UiTextField
          v-model="form.address"
          label="Адрес"
          required
          placeholder="Красноярск, стадион «Енисей»"
          :error="errors.address"
        />
        <UiTextField
          v-model="form.locationNote"
          label="Как найти"
          placeholder="Корт 3, вход со стороны парковки"
          :error="errors.locationNote"
        />
        <LocationPicker v-model="form.point" :address="form.address" :city="form.city" />
      </div>

      <div class="flex flex-col gap-4 rounded-card bg-card p-5">
        <h2 class="text-xl font-bold">Запись</h2>
        <fieldset
          class="flex flex-col gap-2"
          :aria-describedby="errors.joinMode ? 'join-err' : undefined"
        >
          <legend class="mb-1.5 ml-1 text-[15px] font-semibold">
            Как записываться<span aria-hidden="true" class="text-orange"> *</span>
          </legend>
          <label
            v-for="m in joinModes"
            :key="m.value"
            class="flex cursor-pointer items-center gap-3 rounded-field bg-field p-4 has-checked:bg-accent-soft has-focus-visible:outline-3 has-focus-visible:outline-offset-2 has-focus-visible:outline-accent-ink"
            :class="errors.joinMode ? 'ring-2 ring-danger ring-inset' : ''"
          >
            <input
              v-model="form.joinMode"
              type="radio"
              name="join-mode"
              :value="m.value"
              :aria-invalid="errors.joinMode ? 'true' : undefined"
              class="size-5 flex-none accent-ink"
            />
            <span class="flex flex-col">
              <b>{{ m.title }}</b>
              <span class="text-sm text-ink-2">{{ m.text }}</span>
            </span>
          </label>
          <p
            v-if="errors.joinMode"
            id="join-err"
            class="ml-1 text-sm font-semibold text-danger"
            role="alert"
          >
            ✕ {{ errors.joinMode }}
          </p>
        </fieldset>
        <div class="grid grid-cols-1 gap-4 sm:grid-cols-2">
          <UiSelect
            v-model="form.level"
            label="Уровень"
            :options="levels"
            :placeholder="original?.level ? undefined : 'Любой'"
          />
          <UiTextField
            v-model="form.capacity"
            type="number"
            label="Сколько мест"
            placeholder="Без ограничения"
            :error="errors.capacity"
          />
        </div>
        <UiTextField
          v-model="form.equipment"
          label="Что взять с собой"
          placeholder="Своя ракетка"
          :error="errors.equipment"
        />
        <UiTextField
          v-model="form.requirements"
          label="Требования"
          placeholder="С 16 лет"
          :error="errors.requirements"
        />
      </div>

      <p
        v-if="formError"
        role="alert"
        class="rounded-card bg-warning-bg p-4 font-semibold text-warning-fg"
      >
        ✕ {{ formError }}
      </p>

      <div class="flex flex-col gap-2 sm:flex-row-reverse">
        <UiButton type="submit" :loading="submitting">
          {{ editId ? 'Сохранить' : 'Опубликовать' }}
        </UiButton>
        <RouterLink
          v-if="editId"
          :to="{ name: 'event', params: { id: editId } }"
          class="inline-flex min-h-13 w-full items-center justify-center rounded-full bg-field px-5 font-bold text-ink no-underline"
        >
          Отмена
        </RouterLink>
      </div>
    </form>
  </section>
</template>
