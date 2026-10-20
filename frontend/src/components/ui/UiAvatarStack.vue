<script setup lang="ts">
import { computed } from 'vue'
import type { UserShort } from '@/api/types'
import UiAvatar from './UiAvatar.vue'

const props = withDefaults(defineProps<{ users: UserShort[]; max?: number }>(), { max: 3 })

const shown = computed(() => props.users.slice(0, props.max))
const rest = computed(() => props.users.length - shown.value.length)
</script>

<template>
  <div
    class="flex items-center"
    :aria-label="$t('event.participantsList', { list: users.map((u) => u.name).join(', ') })"
  >
    <UiAvatar
      v-for="(u, i) in shown"
      :key="u.id"
      :name="u.name"
      :url="u.avatar_url"
      size="sm"
      class="ring-2 ring-card"
      :class="{ '-ml-2.5': i > 0 }"
    />
    <span
      v-if="rest > 0"
      class="-ml-2.5 grid size-8 place-items-center rounded-full bg-field ring-2 ring-card text-[11px] font-bold"
      aria-hidden="true"
      >+{{ rest }}</span
    >
  </div>
</template>
