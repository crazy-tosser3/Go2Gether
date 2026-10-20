<script setup lang="ts">
import { computed, onBeforeUnmount, ref, useId, watch } from 'vue'
import { RouterLink, useRoute, useRouter } from 'vue-router'
import { useSessionStore } from '@/stores/session'
import UiAvatar from '@/components/ui/UiAvatar.vue'
import NavIcon from './NavIcon.vue'
import { userMenuItems } from './nav'

const session = useSessionStore()
const route = useRoute()
const router = useRouter()
const open = ref(false)
const root = ref<HTMLElement | null>(null)
const button = ref<HTMLButtonElement | null>(null)
const menuId = `user-menu-${useId()}`
const items = computed(() => userMenuItems(session.isAdmin))

watch(
  () => route.fullPath,
  () => (open.value = false),
)

function onDocument(e: Event) {
  if (e instanceof KeyboardEvent) {
    if (e.key !== 'Escape') return
    open.value = false
    button.value?.focus()
  } else if (!root.value?.contains(e.target as Node)) open.value = false
}

watch(open, (value) => {
  const method = value ? 'addEventListener' : 'removeEventListener'
  document[method]('pointerdown', onDocument)
  document[method]('keydown', onDocument)
})
onBeforeUnmount(() => (open.value = false))

async function logout() {
  open.value = false
  await session.logout()
  await router.push({ name: 'home' })
}
</script>

<template>
  <div v-if="session.user" ref="root" class="relative flex-none">
    <button
      ref="button"
      type="button"
      class="flex h-[42px] items-center gap-1.5 rounded-full bg-field pr-2.5 hover:bg-mute"
      :aria-expanded="open"
      :aria-controls="menuId"
      :aria-label="$t('nav.userMenu', { name: session.user.name })"
      @click="open = !open"
    >
      <UiAvatar :name="session.user.name" :url="session.user.avatar_url" size="lg" />
      <svg
        width="14"
        height="14"
        viewBox="0 0 14 14"
        aria-hidden="true"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
        stroke-linecap="round"
        stroke-linejoin="round"
        class="transition-transform"
        :class="open && 'rotate-180'"
      >
        <path d="M3 5L7 9L11 5" />
      </svg>
    </button>

    <div
      v-show="open"
      :id="menuId"
      class="absolute top-[calc(100%+8px)] right-0 z-30 flex w-72 flex-col gap-1 rounded-xl bg-card p-2 shadow-[0_12px_32px_rgb(0_0_0/0.18)]"
    >
      <p class="flex items-center gap-3 px-3 py-2">
        <UiAvatar :name="session.user.name" :url="session.user.avatar_url" />
        <span class="min-w-0 truncate font-bold">{{ session.user.name }}</span>
      </p>
      <nav :aria-label="$t('nav.personal')">
        <ul class="flex flex-col gap-0.5">
          <li v-for="item in items" :key="item.label">
            <RouterLink
              :to="item.to"
              class="flex items-center gap-3 rounded-full px-3 py-2.5 font-semibold text-ink no-underline hover:bg-field"
              active-class="bg-field"
            >
              <NavIcon :name="item.icon" />
              {{ item.label }}
            </RouterLink>
          </li>
        </ul>
      </nav>
      <div class="mt-1 grid grid-cols-2 gap-1 border-t border-field pt-2">
        <RouterLink
          :to="{ name: 'login' }"
          class="rounded-full bg-field px-3 py-2 text-center text-sm font-semibold whitespace-nowrap text-ink no-underline hover:bg-mute"
        >
          {{ $t('common.switchRole') }}
        </RouterLink>
        <button
          type="button"
          class="rounded-full bg-field px-3 py-2 text-sm font-semibold whitespace-nowrap hover:bg-mute"
          @click="logout"
        >
          {{ $t('common.logout') }}
        </button>
      </div>
    </div>
  </div>
</template>
