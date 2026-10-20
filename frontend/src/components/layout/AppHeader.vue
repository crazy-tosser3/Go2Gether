<script setup lang="ts">
import { computed } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { useSessionStore } from '@/stores/session'
import { UiIconButton } from '@/components/ui'
import AppLogo from './AppLogo.vue'
import AppUserMenu from './AppUserMenu.vue'
import LocaleSwitch from './LocaleSwitch.vue'
import ThemeSwitch from './ThemeSwitch.vue'
import { CREATE_ITEM, headerItems } from './nav'

defineEmits<{ openMenu: [] }>()

const session = useSessionStore()
const items = computed(() => headerItems(session.isAdmin))
const route = useRoute()
const onAuthPage = computed(() => route.name === 'login' || route.name === 'register')
</script>

<template>
  <header class="sticky top-0 z-20 px-2 pt-2 short:static lg:px-6 lg:pt-3 2xl:px-10">
    <div
      class="flex items-center gap-2 rounded-xl sm:gap-3 bg-card py-2 pr-2 pl-2.5 shadow sm:py-2.5 sm:pr-3 sm:pl-3.5-[0_6px_18px_-12px_rgb(0_0_0/0.25)] short:py-1.5"
    >
      <AppLogo />

      <nav :aria-label="$t('nav.sections')" class="ml-2 hidden min-w-0 lg:block">
        <ul class="flex gap-0.5">
          <li v-for="h in items" :key="h.item.label" :class="h.wide && 'hidden xl:block'">
            <RouterLink
              :to="h.item.to"
              class="inline-flex min-h-[42px] items-center rounded-full px-4 text-[15px] font-semibold whitespace-nowrap text-ink-2 no-underline hover:bg-field hover:text-ink"
              :exact-active-class="h.item.icon === 'home' ? '!bg-ink !text-white font-bold' : ''"
              :active-class="h.item.icon === 'home' ? '' : '!bg-ink !text-white font-bold'"
            >
              {{ h.item.label }}
            </RouterLink>
          </li>
        </ul>
      </nav>

      <div class="ml-auto flex items-center gap-1.5 sm:gap-2">
        <ThemeSwitch :class="!session.user && 'max-sm:hidden'" />
        <LocaleSwitch class="hidden lg:block" />
        <RouterLink
          v-if="!session.isAdmin"
          :to="CREATE_ITEM.to"
          class="hidden min-h-[42px] items-center gap-1.5 rounded-full bg-orange px-[18px] text-[15px] font-extrabold whitespace-nowrap text-orange-ink no-underline hover:brightness-95 lg:inline-flex"
        >
          <span aria-hidden="true" class="text-xl leading-none">+</span>
          {{ CREATE_ITEM.label }}
        </RouterLink>
        <AppUserMenu v-if="session.user" class="hidden lg:flex" />
        <RouterLink
          v-else-if="!onAuthPage"
          :to="{ name: 'login' }"
          class="inline-flex min-h-[42px] flex-none items-center rounded-full bg-ink px-4 font-bold text-white no-underline sm:px-5"
        >
          {{ $t('common.login') }}
        </RouterLink>

        <UiIconButton class="lg:hidden" :label="$t('nav.allSections')" @click="$emit('openMenu')">
          <svg
            viewBox="0 0 20 20"
            width="20"
            height="20"
            aria-hidden="true"
            fill="none"
            stroke="currentColor"
            stroke-width="2.2"
            stroke-linecap="round"
          >
            <path d="M3 5H17M3 10H17M3 15H17" />
          </svg>
        </UiIconButton>
      </div>
    </div>
  </header>
</template>
