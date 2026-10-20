<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { APP_NAME } from '@/router/access'
</script>

<template>
  <footer class="bg-page">
    <div
      class="flex flex-wrap items-center gap-x-5 gap-y-1 px-4 py-4 text-sm text-ink-2 lg:px-6 2xl:px-10"
    >
      <span>{{ $t('footer.prototype', { app: APP_NAME }) }}</span>
      <RouterLink
        :to="{ name: 'privacy' }"
        class="inline-flex min-h-8 items-center text-ink underline underline-offset-4"
      >
        {{ $t('footer.privacy') }}
      </RouterLink>
      <span :aria-label="$t('footer.age')" class="ml-auto font-bold text-ink">12+</span>
    </div>
  </footer>
</template>
