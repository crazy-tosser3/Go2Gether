<script setup lang="ts">
import { RouterLink } from 'vue-router'
import { useSessionStore } from '@/stores/session'
import NavIcon from './NavIcon.vue'
import { CREATE_ITEM, tabBarItems } from './nav'

const session = useSessionStore()

const [left, right] = tabBarItems()
</script>

<template>
  <nav
    :aria-label="$t('nav.main')"
    class="fixed inset-x-2 bottom-2 z-20 grid grid-cols-5 items-end rounded-xl bg-card px-1 py-2 shadow-[0_8px_30px_rgba(14,14,16,0.12)] short:items-center short:py-1 lg:hidden"
  >
    <RouterLink
      v-for="item in left"
      :key="item.label"
      :to="item.to"
      class="flex min-h-11 min-w-0 flex-col items-center justify-center gap-0.5 text-center text-[11px] break-words leading-tight text-ink-2 no-underline short:min-h-10 short:flex-row short:justify-center short:gap-1.5 short:text-xs"
      exact-active-class="font-bold !text-ink"
    >
      <NavIcon :name="item.icon" />
      <span class="max-[359px]:sr-only">{{ item.label }}</span>
    </RouterLink>

    <!-- Администратор не создаёт мероприятия — на его месте в центре админка. -->
    <RouterLink
      v-if="session.isAdmin"
      :to="{ name: 'admin' }"
      class="flex min-h-11 min-w-0 flex-col items-center justify-center gap-0.5 text-center text-[11px] break-words font-bold leading-tight text-ink no-underline short:min-h-10 short:flex-row short:justify-center short:gap-1.5 short:text-xs"
    >
      <span
        aria-hidden="true"
        class="-mt-6 grid size-12 place-items-center rounded-full bg-ink text-white short:mt-0 short:size-9"
        ><NavIcon name="shield"
      /></span>
      <span class="max-[359px]:sr-only">{{ $t('nav.admin') }}</span>
    </RouterLink>
    <RouterLink
      v-else
      :to="CREATE_ITEM.to"
      class="flex min-h-11 min-w-0 flex-col items-center justify-center gap-0.5 text-center text-[11px] break-words font-bold leading-tight text-ink no-underline short:min-h-10 short:flex-row short:justify-center short:gap-1.5 short:text-xs"
    >
      <span
        aria-hidden="true"
        class="-mt-6 grid size-14 place-items-center rounded-full bg-orange text-3xl leading-none text-orange-ink shadow-[0_6px_16px_-6px_rgb(0_0_0/0.35)] short:mt-0 short:size-9 short:text-2xl"
        >+</span
      >
      <span class="max-[359px]:sr-only">{{ CREATE_ITEM.label }}</span>
    </RouterLink>

    <RouterLink
      v-for="item in right"
      :key="item.label"
      :to="item.to"
      class="flex min-h-11 min-w-0 flex-col items-center justify-center gap-0.5 text-center text-[11px] break-words leading-tight text-ink-2 no-underline short:min-h-10 short:flex-row short:justify-center short:gap-1.5 short:text-xs"
      active-class="font-bold !text-ink"
    >
      <NavIcon :name="item.icon" />
      <span class="max-[359px]:sr-only">{{ item.label }}</span>
    </RouterLink>
  </nav>
</template>
