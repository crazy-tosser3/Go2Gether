<script setup lang="ts">
import { APP_NAME } from '@/router/access'

const SECTIONS = ['who', 'data', 'basis', 'storage', 'cookie', 'rights'] as const
</script>

<template>
  <article class="mx-auto flex max-w-2xl flex-col gap-5">
    <header>
      <p class="font-hand text-xl text-accent-ink">{{ $t('privacy.hand') }}</p>
      <h1 class="text-[30px] lg:text-[42px]">{{ $t('privacy.title') }}</h1>
      <p v-if="$t('privacy.note')" class="mt-2 text-sm text-ink-2">{{ $t('privacy.note') }}</p>
    </header>

    <section v-for="s in SECTIONS" :key="s" class="flex flex-col gap-1">
      <h2 class="text-lg font-bold">{{ $t(`privacy.${s}.title`) }}</h2>
      <p class="text-ink-2">{{ $t(`privacy.${s}.text`, { app: APP_NAME }) }}</p>
    </section>

    <p class="text-sm text-ink-2">{{ $t('privacy.revision') }}</p>
  </article>
</template>
