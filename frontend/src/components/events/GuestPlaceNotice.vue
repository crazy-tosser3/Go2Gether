<script setup lang="ts">
import { RouterLink, useRoute } from 'vue-router'

defineProps<{ text: string }>()

const route = useRoute()
</script>

<template>
  <div class="flex flex-col items-start gap-3 rounded-card bg-card p-5">
    <p class="flex items-start gap-2">
      <span aria-hidden="true">🔒</span>
      <span>{{ text }}</span>
    </p>
    <RouterLink
      :to="{ name: 'login', query: { redirect: route.fullPath } }"
      class="inline-flex min-h-11 items-center rounded-full bg-ink px-5 font-bold text-white no-underline hover:bg-ink/85"
    >
      {{ $t('place.login') }}
    </RouterLink>
  </div>
</template>
