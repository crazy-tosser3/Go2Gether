<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { RouterLink } from 'vue-router'
import { ApiError, organizationsApi, type Organization } from '@/api'
import NavIcon from '@/components/layout/NavIcon.vue'
import { UiButton, UiChip, UiTextField } from '@/components/ui'
import { ORG_TYPE_LABELS } from '@/features/admin/organizations'
import { t } from '@/i18n'
import { filterByType, type TypeFilter } from '@/features/organizations/catalog'

const PAGE = 20

const query = ref('')
const sport = ref('')
const city = ref('')
const type = ref<TypeFilter>('all')
const items = ref<Organization[]>([])
const total = ref(0)
const page = ref(0)
const state = ref<'loading' | 'ready' | 'error'>('loading')
const loadingMore = ref(false)
const message = ref('')

const shown = computed(() => filterByType(items.value, type.value))
const types = computed(
  () =>
    [['all', t('admin.filters.all')], ...Object.entries(ORG_TYPE_LABELS)] as [TypeFilter, string][],
)

async function load(more = false) {
  if (more) loadingMore.value = true
  else state.value = 'loading'
  try {
    const next = more ? page.value + 1 : 0
    const res = await organizationsApi.list({
      q: query.value.trim() || undefined,
      sport: sport.value.trim() || undefined,
      city: city.value.trim() || undefined,
      page: next,
      size: PAGE,
    })
    items.value = more ? [...items.value, ...res.items] : res.items
    total.value = res.total
    page.value = next
    state.value = 'ready'
  } catch (e) {
    message.value = e instanceof ApiError ? e.message : 'Не получилось загрузить организации.'
    if (!more) state.value = 'error'
  } finally {
    loadingMore.value = false
  }
}

onMounted(() => load())

let timer: ReturnType<typeof setTimeout> | undefined
watch([query, sport, city], () => {
  clearTimeout(timer)
  timer = setTimeout(() => load(), 300)
})
</script>

<template>
  <section class="flex flex-col gap-4">
    <div class="flex flex-wrap items-center justify-between gap-3">
      <h1 class="text-[30px] lg:text-[42px]">Организации</h1>
      <RouterLink
        :to="{ name: 'my-organization' }"
        class="font-semibold text-accent-ink underline underline-offset-4"
      >
        Зарегистрировать свою →
      </RouterLink>
    </div>

    <div class="flex flex-col gap-3 rounded-card bg-card p-4" role="search">
      <UiTextField
        v-model="query"
        label="Поиск организаций"
        hide-label
        type="search"
        placeholder="Название или направление"
      />
      <div class="grid gap-3 sm:grid-cols-2">
        <UiTextField v-model="sport" label="Направление" placeholder="Например, футбол" />
        <UiTextField
          v-model="city"
          label="Город"
          placeholder="Красноярск"
          hint="Полное название города"
        />
      </div>
      <div class="flex gap-2 overflow-x-auto" role="group" aria-label="Тип">
        <UiChip
          v-for="[value, label] in types"
          :key="value"
          :pressed="type === value"
          @click="type = value"
        >
          {{ label }}
        </UiChip>
      </div>
    </div>

    <div aria-live="polite" class="flex flex-col gap-3">
      <ul
        v-if="state === 'loading'"
        class="grid grid-cols-1 gap-3 lg:grid-cols-2"
        aria-label="Загрузка"
      >
        <li v-for="i in 4" :key="i" class="h-28 animate-pulse rounded-card bg-card" />
      </ul>

      <div
        v-else-if="state === 'error'"
        class="flex flex-col items-center gap-3 rounded-card bg-card p-6"
        role="alert"
      >
        <p class="font-bold">{{ message }}</p>
        <UiButton variant="ghost" class="!w-auto" @click="load()">Повторить</UiButton>
      </div>

      <div
        v-else-if="!shown.length"
        class="flex flex-col items-center gap-3 rounded-card bg-card p-8 text-center"
      >
        <p class="text-lg font-bold">Ничего не нашлось</p>
        <p class="text-ink-2">Попробуйте другое название или тип.</p>
      </div>

      <template v-else>
        <ul class="grid grid-cols-1 gap-3 lg:grid-cols-2">
          <li
            v-for="org in shown"
            :key="org.id"
            class="relative flex items-center gap-4 rounded-card bg-card p-4 focus-within:outline-3 focus-within:outline-offset-2 focus-within:outline-accent-ink hover:shadow-raised"
          >
            <span class="grid size-14 shrink-0 place-items-center rounded-full bg-accent">
              <NavIcon name="building" />
            </span>
            <div class="flex min-w-0 flex-1 flex-col gap-0.5">
              <h2 class="line-clamp-2 text-xl leading-tight font-extrabold normal-case">
                <RouterLink
                  :to="{ name: 'organization', params: { id: org.id } }"
                  class="text-ink no-underline outline-none after:absolute after:inset-0 after:content-['']"
                >
                  {{ org.name }}
                </RouterLink>
              </h2>
              <p class="truncate text-ink-2">
                {{ ORG_TYPE_LABELS[org.type]
                }}<template v-if="org.city"> · {{ org.city }}</template>
              </p>
              <p v-if="org.sports_description" class="truncate font-medium text-accent-ink">
                {{ org.sports_description }}
              </p>
            </div>
          </li>
        </ul>
        <UiButton
          v-if="items.length < total"
          variant="ghost"
          class="!w-auto self-center"
          :loading="loadingMore"
          @click="load(true)"
        >
          Показать ещё
        </UiButton>
      </template>
    </div>
  </section>
</template>
