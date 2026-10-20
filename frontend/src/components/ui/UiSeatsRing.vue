<script setup lang="ts">
import { computed } from 'vue'
import { seatsLabel, seatsPercent } from './seats'

const props = withDefaults(
  defineProps<{ taken: number; capacity: number | null; size?: number }>(),
  {
    size: 60,
  },
)

const R = 25
const C = 2 * Math.PI * R
const dash = computed(() => (seatsPercent(props.taken, props.capacity) / 100) * C)
const label = computed(() => seatsLabel(props.taken, props.capacity))
</script>

<template>
  <span
    class="relative grid flex-none place-items-center font-bold tabular-nums"
    :style="{ width: `${size}px`, height: `${size}px` }"
    role="img"
    :aria-label="label"
  >
    <svg viewBox="0 0 58 58" class="absolute inset-0" aria-hidden="true">
      <circle
        cx="29"
        cy="29"
        :r="R"
        fill="none"
        stroke="currentColor"
        stroke-opacity=".2"
        stroke-width="5"
      />
      <circle
        v-if="capacity"
        cx="29"
        cy="29"
        :r="R"
        fill="none"
        stroke="currentColor"
        stroke-width="5"
        stroke-linecap="round"
        :stroke-dasharray="`${dash} ${C}`"
        transform="rotate(-90 29 29)"
      />
    </svg>
    <span class="text-sm">{{ capacity ? `${taken}/${capacity}` : taken }}</span>
  </span>
</template>
