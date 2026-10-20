<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { RouterLink } from 'vue-router'
import { ApiError, adminApi, sportsApi, type SafetyBanner, type Sport } from '@/api'
import AdminNav from '@/components/admin/AdminNav.vue'
import { UiButton, UiModal } from '@/components/ui'
import { categoryBg, categoryLabel } from '@/features/safety/safety'
import { t } from '@/i18n'

const banners = ref<SafetyBanner[]>([])
const sports = ref<Sport[]>([])
const state = ref<'loading' | 'ready' | 'error'>('loading')
const message = ref('')
const busyId = ref<number | null>(null)
const actionError = ref('')
const deleting = ref<SafetyBanner | null>(null)
const deleteOpen = computed({
  get: () => deleting.value !== null,
  set: (v) => {
    if (!v) deleting.value = null
  },
})

const sportName = (id: number | null) =>
  id === null ? t('safety.allSports') : (sports.value.find((s) => s.id === id)?.name ?? '—')

async function load() {
  state.value = 'loading'
  try {
    const [list, s] = await Promise.all([adminApi.safetyBanners(), sportsApi.list()])
    banners.value = list
    sports.value = s
    state.value = 'ready'
  } catch (e) {
    message.value = e instanceof ApiError ? e.message : t('safety.loadFailed')
    state.value = 'error'
  }
}

onMounted(load)

async function togglePublished(b: SafetyBanner) {
  busyId.value = b.id
  actionError.value = ''
  try {
    const updated = b.published
      ? await adminApi.unpublishSafetyBanner(b.id)
      : await adminApi.publishSafetyBanner(b.id)
    banners.value = banners.value.map((x) => (x.id === b.id ? { ...x, ...updated } : x))
  } catch (e) {
    actionError.value = e instanceof ApiError ? e.message : t('safety.actionFailed')
  } finally {
    busyId.value = null
  }
}

async function confirmDelete() {
  const b = deleting.value
  if (!b) return
  busyId.value = b.id
  try {
    await adminApi.deleteSafetyBanner(b.id)
    banners.value = banners.value.filter((x) => x.id !== b.id)
    deleting.value = null
  } catch (e) {
    deleting.value = null
    actionError.value = e instanceof ApiError ? e.message : t('safety.actionFailed')
  } finally {
    busyId.value = null
  }
}
</script>

<template>
  <section class="flex flex-col gap-5">
    <div class="flex flex-wrap items-end justify-between gap-3">
      <h1 class="text-[30px] lg:text-[42px]">{{ $t('admin.nav.safety') }}</h1>
      <RouterLink
        :to="{ name: 'admin-safety-new' }"
        class="inline-flex min-h-12 items-center rounded-full bg-ink px-5 font-bold text-white no-underline"
      >
        {{ $t('safety.create') }}
      </RouterLink>
    </div>
    <AdminNav />
    <p class="max-w-2xl text-ink-2">{{ $t('safety.adminHint') }}</p>

    <div
      v-if="state === 'loading'"
      class="grid gap-3 lg:grid-cols-2"
      :aria-label="$t('common.loading')"
    >
      <div v-for="i in 4" :key="i" class="h-28 animate-pulse rounded-card bg-card" />
    </div>
    <div
      v-else-if="state === 'error'"
      class="flex flex-col items-center gap-3 rounded-card bg-card p-6"
      role="alert"
    >
      <p class="font-bold">{{ message }}</p>
      <UiButton variant="ghost" class="!w-auto" @click="load">{{ $t('common.retry') }}</UiButton>
    </div>
    <p v-else-if="!banners.length" class="rounded-card bg-card p-6 text-center text-ink-2">
      {{ $t('safety.empty') }}
    </p>

    <template v-else>
      <p v-if="actionError" class="font-semibold text-danger" role="alert">✕ {{ actionError }}</p>
      <ul class="grid grid-cols-[minmax(0,1fr)] gap-3 lg:grid-cols-2">
        <li v-for="b in banners" :key="b.id" class="flex flex-col gap-3 rounded-card bg-card p-4">
          <div class="flex items-start gap-3">
            <span
              class="grid size-12 flex-none place-items-center rounded-2xl"
              :class="categoryBg(b.category)"
              aria-hidden="true"
            >
              <span
                class="size-4 rounded-full ring-2 ring-card"
                :style="{ background: b.accent_color ?? 'currentColor' }"
              />
            </span>
            <div class="flex min-w-0 flex-1 flex-col">
              <span class="text-xs font-bold tracking-wider text-ink-2 uppercase">
                {{ categoryLabel(b.category) }} · {{ sportName(b.sport_id) }}
              </span>
              <b class="leading-tight">{{ b.title }}</b>
              <span v-if="b.subtitle" class="truncate text-sm text-ink-2">{{ b.subtitle }}</span>
              <span
                class="mt-1.5 self-start rounded-full px-2.5 py-0.5 text-sm font-semibold"
                :class="b.published ? 'bg-ink text-white' : 'bg-field text-ink-2'"
              >
                {{ b.published ? $t('safety.published') : $t('safety.draft') }}
              </span>
            </div>
          </div>
          <div class="flex flex-wrap gap-2">
            <RouterLink
              :to="{ name: 'admin-safety-edit', params: { id: b.id } }"
              class="inline-flex min-h-11 items-center rounded-full bg-field px-4 font-semibold text-ink no-underline hover:bg-mute"
            >
              {{ $t('common.edit') }}
            </RouterLink>
            <UiButton
              variant="ghost"
              class="!w-auto"
              :loading="busyId === b.id"
              @click="togglePublished(b)"
            >
              {{ b.published ? $t('safety.unpublish') : $t('safety.publish') }}
            </UiButton>
            <button
              type="button"
              class="inline-flex min-h-11 items-center px-2 font-semibold text-ink-2 underline underline-offset-4 hover:text-ink"
              @click="deleting = b"
            >
              {{ $t('common.delete') }}
            </button>
          </div>
        </li>
      </ul>
    </template>

    <UiModal
      v-model:open="deleteOpen"
      :title="$t('safety.deleteTitle', { name: deleting?.title ?? '' })"
      :description="$t('safety.deleteText')"
    >
      <template #actions>
        <UiButton variant="ghost" @click="deleteOpen = false">{{ $t('common.cancel') }}</UiButton>
        <UiButton :loading="busyId === deleting?.id" @click="confirmDelete">
          {{ $t('common.delete') }}
        </UiButton>
      </template>
    </UiModal>
  </section>
</template>
