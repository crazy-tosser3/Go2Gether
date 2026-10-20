<script setup lang="ts">
import { useId } from 'vue'
import { THEME_OPTIONS, useTheme } from '@/features/theme/theme'

const theme = useTheme()
// Переключатель есть и в шапке, и в мобильном меню — у каждой группы своё имя, иначе они сбрасывают друг друга.
const name = `theme-${useId()}`
</script>

<template>
  <fieldset class="flex-none">
    <legend class="sr-only">{{ $t('theme.title') }}</legend>
    <div class="flex gap-0.5 rounded-full bg-field p-[3px]">
      <label
        v-for="o in THEME_OPTIONS"
        :key="o.value"
        :title="$t(`theme.${o.value}`)"
        class="grid size-8 cursor-pointer place-items-center rounded-full text-ink-2 sm:size-9 hover:text-ink has-checked:bg-ink has-checked:text-white has-checked:shadow-[0_2px_6px_rgb(0_0_0/0.25)] has-focus-visible:outline-3 has-focus-visible:outline-accent-ink"
      >
        <input v-model="theme" type="radio" :name="name" :value="o.value" class="sr-only" />
        <span class="sr-only">{{ $t(`theme.${o.value}`) }}</span>
        <svg
          v-if="o.value === 'light'"
          width="18"
          height="18"
          viewBox="0 0 20 20"
          aria-hidden="true"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
          stroke-linecap="round"
        >
          <circle cx="10" cy="10" r="4" class="fill-orange" />
          <path
            d="M10 1.5V3.5M10 16.5V18.5M1.5 10H3.5M16.5 10H18.5M4 4L5.4 5.4M14.6 14.6L16 16M4 16L5.4 14.6M14.6 5.4L16 4"
          />
        </svg>
        <svg
          v-else-if="o.value === 'dark'"
          width="18"
          height="18"
          viewBox="0 0 20 20"
          aria-hidden="true"
        >
          <path
            d="M15.5 12.5A7 7 0 0 1 7.5 4.5A7 7 0 1 0 15.5 12.5Z"
            fill="#a6b8ff"
            stroke="currentColor"
            stroke-width="2"
            stroke-linejoin="round"
          />
          <circle cx="15" cy="4" r="1.2" fill="currentColor" />
        </svg>
        <svg
          v-else
          width="18"
          height="18"
          viewBox="0 0 20 20"
          aria-hidden="true"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
        >
          <circle cx="10" cy="10" r="7" />
          <path d="M10 3A7 7 0 0 1 10 17Z" fill="currentColor" />
        </svg>
      </label>
    </div>
  </fieldset>
</template>
