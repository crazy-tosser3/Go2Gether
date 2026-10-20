<script setup lang="ts">
import { computed } from 'vue'

export type SportIconName = 'football' | 'tennis' | 'chess' | 'bike' | 'running' | 'custom'

const props = withDefaults(defineProps<{ name?: string | null; size?: number }>(), {
  name: null,
  size: 44,
})

const known: SportIconName[] = ['football', 'tennis', 'chess', 'bike', 'running', 'custom']
const icon = computed<SportIconName>(() =>
  known.includes(props.name as SportIconName) ? (props.name as SportIconName) : 'custom',
)
</script>

<template>
  <svg
    :width="size"
    :height="size"
    viewBox="0 0 48 48"
    aria-hidden="true"
    fill="none"
    stroke="currentColor"
    stroke-width="2.5"
    stroke-linecap="round"
    stroke-linejoin="round"
    class="text-ink"
  >
    <template v-if="icon === 'football'">
      <circle cx="24" cy="24" r="18" class="fill-card" />
      <path d="M24 15l7 5-3 8h-8l-3-8z" class="fill-orange" />
      <path d="M24 6v9M31 20l9-3M28 28l5 8M20 28l-5 8M17 20l-9-3" />
    </template>
    <template v-else-if="icon === 'tennis'">
      <ellipse cx="20" cy="18" rx="12" ry="14" transform="rotate(-35 20 18)" class="fill-card" />
      <path d="M12 12l14 10M10 19l12 9M15 8l14 10M14 25l11-15M19 29l11-15" stroke-width="1.5" />
      <path d="M28 29l11 11" stroke-width="5" />
      <circle cx="38" cy="12" r="5" class="fill-orange" />
    </template>
    <template v-else-if="icon === 'chess'">
      <circle cx="24" cy="12" r="6" class="fill-orange" />
      <path d="M18 20h12l-2 4 3 12H17l3-12z" class="fill-card" />
      <rect x="12" y="36" width="24" height="6" rx="2" class="fill-card" />
    </template>
    <template v-else-if="icon === 'bike'">
      <circle cx="11" cy="31" r="8" class="fill-card" />
      <circle cx="37" cy="31" r="8" class="fill-orange" />
      <path d="M11 31l8-14h12l6 14M19 17l6 14 6-14M17 13h6M31 17l-2-5h5" />
    </template>
    <template v-else-if="icon === 'running'">
      <circle cx="30" cy="8" r="4.5" class="fill-orange" />
      <path d="M27 14l-7 9 7 5-3 12M20 23l-8 2M27 14l7 6 6-2M27 28l9 4" />
    </template>
    <template v-else>
      <circle cx="24" cy="24" r="17" stroke-dasharray="4 5" />
      <path
        d="M24 13c.6 5 2.6 7.6 8 8.5-5.4.9-7.4 3.5-8 8.5-.6-5-2.6-7.6-8-8.5 5.4-.9 7.4-3.5 8-8.5z"
        class="fill-orange"
        stroke-width="1.5"
      />
    </template>
  </svg>
</template>
