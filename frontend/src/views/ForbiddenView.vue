<script setup lang="ts">
import { RouterLink } from 'vue-router'
</script>

<template>
  <section class="mx-auto flex max-w-md flex-col items-center gap-3 py-10 text-center">
    <h1 class="text-[30px] lg:text-[42px]">{{ $t('forbidden.title') }}</h1>
    <p class="text-ink-2">{{ $t('forbidden.text') }}</p>
    <RouterLink
      :to="{ name: 'home' }"
      class="font-bold text-accent-ink underline underline-offset-4"
    >
      {{ $t('forbidden.cta') }}
    </RouterLink>
  </section>
</template>
