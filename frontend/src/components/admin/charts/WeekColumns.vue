<script setup lang="ts">
import { computed } from 'vue'
import type { WeekSlice } from '@/features/admin/stats'

const props = defineProps<{ weeks: WeekSlice[] }>()

const max = computed(() => Math.max(1, ...props.weeks.map((w) => w.value)))
</script>

<template>
  <ol class="grid h-48 grid-flow-col items-end gap-1.5 sm:gap-3">
    <li v-for="w in weeks" :key="w.key" class="flex h-full min-w-0 flex-col justify-end gap-1">
      <b class="text-center text-sm tabular-nums">{{ w.value }}</b>
      <div
        class="min-h-1 rounded-t-lg"
        :class="w.current ? 'bg-orange' : 'bg-accent-strong'"
        :style="{ height: `${(w.value / max) * 100}%` }"
        aria-hidden="true"
      />
      <span
        class="flex flex-col text-center text-xs leading-tight text-ink-2 sm:block"
        :class="{ 'font-bold text-ink': w.current }"
      >
        <span
          v-for="part in w.label.split(' ')"
          :key="part"
          class="sm:not-last:after:content-['_']"
          >{{ part }}</span
        >
        <span v-if="w.current" class="sr-only">, {{ $t('stats.thisWeek') }}</span>
      </span>
    </li>
  </ol>
</template>
