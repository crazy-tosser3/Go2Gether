<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { APP_NAME } from '@/router/access'
</script>

<template>
  <RouterLink
    :to="{ name: 'home' }"
    class="flex flex-none items-center gap-2 text-ink no-underline sm:gap-2.5"
    :aria-label="$t('nav.logo', { app: APP_NAME })"
  >
    <span
      class="grid size-10 place-items-center rounded-full bg-accent sm:size-[42px]"
      aria-hidden="true"
    >
      <svg
        width="28"
        height="28"
        viewBox="0 0 28 28"
        fill="none"
        stroke-linecap="round"
        stroke-linejoin="round"
      >
        <g transform="translate(-4 1)">
          <path
            d="M14 9L11 16L15 20L14 25M11 16L6 21M13 11L18 14L21 12M13 11L8 12"
            stroke="#111111"
            stroke-width="4.2"
          />
          <path
            d="M14 9L11 16L15 20L14 25M11 16L6 21M13 11L18 14L21 12M13 11L8 12"
            stroke="#ffffff"
            stroke-width="2"
          />
          <circle cx="15" cy="5" r="2.6" fill="#ffffff" stroke="#111111" stroke-width="1.4" />
        </g>
        <g transform="translate(3 0)">
          <path
            d="M14 9L11 16L15 20L14 25M11 16L6 21M13 11L18 14L21 12M13 11L8 12"
            stroke="#111111"
            stroke-width="2.6"
          />
          <circle cx="15" cy="5" r="3" fill="#f28c28" stroke="#111111" stroke-width="1.4" />
        </g>
      </svg>
    </span>
    <span
      class="font-display text-[17px] whitespace-nowrap uppercase max-[359px]:hidden sm:text-[19px]"
      >{{ APP_NAME }}</span
    >
  </RouterLink>
</template>
