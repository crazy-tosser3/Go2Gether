<script setup lang="ts">
import { computed } from 'vue'
import type { Slice } from '@/features/admin/stats'

const props = defineProps<{ items: Slice[]; caption: string }>()

// Цвета различимы и без цвета: у каждого сегмента в легенде подпись и число.
const colors = [
  'var(--color-accent-strong)',
  'var(--color-orange)',
  'var(--color-ink-2)',
  'var(--color-lilac)',
]
const R = 15.9155 // длина окружности ровно 100 — доли пишутся прямо в dasharray

const total = computed(() => props.items.reduce((sum, i) => sum + i.value, 0))

const segments = computed(() => {
  let offset = 0
  return props.items.map((item, i) => {
    const share = total.value ? (item.value / total.value) * 100 : 0
    const seg = { ...item, color: colors[i % colors.length], share, offset }
    offset += share
    return seg
  })
})
</script>

<template>
  <div class="flex flex-col items-center gap-4 sm:flex-row sm:gap-6 lg:flex-col xl:flex-row">
    <div class="relative size-36 shrink-0">
      <svg viewBox="0 0 36 36" class="size-full -rotate-90" aria-hidden="true">
        <circle cx="18" cy="18" :r="R" fill="none" stroke="var(--color-field)" stroke-width="5" />
        <circle
          v-for="s in segments.filter((x) => x.share > 0)"
          :key="s.key"
          cx="18"
          cy="18"
          :r="R"
          fill="none"
          :stroke="s.color"
          stroke-width="5"
          :stroke-dasharray="`${s.share} ${100 - s.share}`"
          :stroke-dashoffset="-s.offset"
        />
      </svg>
      <div class="absolute inset-0 grid place-content-center text-center">
        <b class="text-2xl tabular-nums">{{ total }}</b>
        <span class="text-xs text-ink-2">{{ caption }}</span>
      </div>
    </div>
    <ul class="flex w-full min-w-0 flex-col gap-2">
      <li v-for="s in segments" :key="s.key" class="flex items-center gap-2">
        <span class="size-3 shrink-0 rounded-full" :style="{ background: s.color }" />
        <span class="min-w-0 flex-1">{{ s.label }}</span>
        <b class="tabular-nums">{{ s.value }}</b>
      </li>
    </ul>
  </div>
</template>
