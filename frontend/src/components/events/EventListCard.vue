<script setup lang="ts">
import { computed, useId } from 'vue'
import { RouterLink } from 'vue-router'
import type { EventSummary } from '@/api'
import SportIcon from '@/components/illustrations/SportIcon.vue'
import { sportIconName } from '@/components/illustrations/sportIcon'
import { seatsShort } from '@/components/ui'
import { LEVEL_LABELS } from '@/features/events/filters'
import { visiblePlace } from '@/features/events/place'
import { useSessionStore } from '@/stores/session'
import { formatEventWhen } from '@/utils/datetime'

const props = defineProps<{ event: EventSummary; reasons?: string[] }>()

const uid = useId()
const when = computed(() => formatEventWhen(props.event.starts_at))
const seats = computed(() => seatsShort(props.event.confirmed_count, props.event.capacity))
const icon = computed(() => sportIconName(props.event.sport))
const session = useSessionStore()
const place = computed(() => visiblePlace(props.event, session.isAuthenticated))

// Обложка — бренд-цвет по виду спорта, как в карточке события дизайн-системы.
const COVERS: Record<string, string> = {
  football: 'bg-orange',
  tennis: 'bg-lilac',
  running: 'bg-accent',
  chess: 'bg-accent-soft',
  bike: 'bg-lilac',
  custom: 'bg-accent-soft',
}
</script>

<template>
  <article
    class="relative flex h-full flex-col overflow-hidden rounded-card bg-card shadow-raised focus-within:outline-3 focus-within:outline-offset-2 focus-within:outline-accent-strong"
  >
    <div class="relative h-28 overflow-hidden" :class="COVERS[icon]" aria-hidden="true">
      <svg class="absolute inset-0 size-full">
        <defs>
          <pattern :id="`dots-${uid}`" width="9" height="9" patternUnits="userSpaceOnUse">
            <circle cx="4.5" cy="4.5" r="1.5" fill="#111111" fill-opacity="0.2" />
          </pattern>
        </defs>
        <rect width="100%" height="100%" :fill="`url(#dots-${uid})`" />
      </svg>
      <span class="absolute -right-3 -bottom-4 opacity-90">
        <SportIcon :name="icon" :size="104" />
      </span>
    </div>
    <div class="absolute inset-x-3 top-3 flex items-start justify-between gap-2">
      <span class="truncate rounded-full bg-[#ffffff] px-3 py-1 text-sm font-bold text-[#111111]">
        {{ event.sport.name }}
      </span>
      <span
        v-if="event.join_mode === 'approval'"
        class="flex-none rounded-full bg-[#111111] px-3 py-1 text-sm font-bold text-[#ffffff]"
      >
        {{ $t('joinMode.approval') }}
      </span>
    </div>

    <div class="flex flex-1 flex-col gap-1.5 p-4">
      <p class="text-[15px] font-bold text-accent-ink">{{ when }}</p>
      <h3 class="line-clamp-2 font-display text-xl leading-tight font-extrabold normal-case">
        <RouterLink
          :to="{ name: 'event', params: { id: event.id } }"
          class="text-ink no-underline outline-none after:absolute after:inset-0 after:content-['']"
        >
          {{ event.title }}
        </RouterLink>
      </h3>
      <p v-if="place" class="truncate text-ink-2">{{ place }}</p>
      <p class="flex flex-wrap gap-1.5 text-sm font-semibold">
        <span v-if="event.level" class="rounded-full bg-field px-2.5 py-1">{{
          LEVEL_LABELS[event.level]
        }}</span>
        <span class="rounded-full bg-field px-2.5 py-1">{{ seats }}</span>
      </p>
      <div class="mt-auto flex flex-wrap items-center justify-between gap-2 pt-1">
        <p v-if="reasons?.length" class="flex flex-wrap gap-1.5 text-sm font-bold">
          <span v-for="r in reasons" :key="r" class="rounded-full bg-reason px-2.5 py-1">
            <span aria-hidden="true">★ </span>{{ r }}
          </span>
        </p>
        <span class="ml-auto truncate text-sm text-ink-2">{{ event.organizer.name }}</span>
      </div>
    </div>
  </article>
</template>
