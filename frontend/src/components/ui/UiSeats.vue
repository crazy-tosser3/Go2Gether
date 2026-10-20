<script setup lang="ts">
import { computed } from 'vue'
import { seatsLabel, seatsPercent } from './seats'

const props = defineProps<{ taken: number; capacity: number | null }>()

const percent = computed(() => seatsPercent(props.taken, props.capacity))
const label = computed(() => seatsLabel(props.taken, props.capacity))
</script>

<template>
  <div class="flex flex-col gap-1">
    <div
      v-if="capacity"
      class="h-2.5 overflow-hidden rounded-full bg-mute"
      role="progressbar"
      :aria-valuenow="taken"
      :aria-valuemin="0"
      :aria-valuemax="capacity"
      :aria-label="label"
    >
      <div
        class="h-full rounded-full bg-ink transition-[width]"
        :style="{ width: `${percent}%` }"
      />
    </div>
    <div class="flex items-center justify-between gap-2 text-sm text-ink-2 tabular-nums">
      <slot />
      <span>{{ label }}</span>
    </div>
  </div>
</template>
