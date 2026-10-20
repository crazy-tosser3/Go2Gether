<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<template>
  <section class="mx-auto flex max-w-md flex-col items-center gap-3 py-10 text-center">
    <p class="font-hand text-2xl text-accent-ink">{{ $t('notFound.hand') }}</p>
    <h1 class="text-[30px] lg:text-[42px]">{{ $t('notFound.title') }}</h1>
    <p class="text-ink-2">{{ $t('notFound.text') }}</p>
    <RouterLink
      :to="{ name: 'events' }"
      class="font-bold text-accent-ink underline underline-offset-4"
    >
      {{ $t('notFound.cta') }}
    </RouterLink>
  </section>
</template>
