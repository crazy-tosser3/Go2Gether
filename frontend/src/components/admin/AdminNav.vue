<script setup lang="ts">
import { RouterLink } from 'vue-router'

const items = [
  { key: 'overview', to: { name: 'admin' } },
  { key: 'organizations', to: { name: 'admin-organizations' } },
  { key: 'events', to: { name: 'admin-events' } },
  { key: 'reports', to: { name: 'admin-reports' } },
  { key: 'safety', to: { name: 'admin-safety' } },
] as const
</script>

<template>
  <nav :aria-label="$t('admin.nav.label')" class="flex gap-2 overflow-x-auto">
    <RouterLink
      v-for="item in items"
      :key="item.key"
      :to="item.to"
      class="flex-none rounded-full bg-field px-4 py-2 font-semibold whitespace-nowrap text-ink no-underline hover:bg-mute"
      exact-active-class="!bg-ink !text-white"
    >
      {{ $t(`admin.nav.${item.key}`) }}
    </RouterLink>
  </nav>
</template>
